/*
 * PlayerTextureView.java
 * webka
 *
 * Copyright (C) 2019, Realtime Technologies Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the
 * property of Realtime Technologies Limited and its SUPPLIERS, if any.
 *
 * The intellectual and technical concepts contained herein are
 * proprietary to Realtime Technologies Limited and its suppliers and
 * may be covered by Russian Federation and Foreign Patents, patents
 * in process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Realtime Technologies Limited.
 */

package com.example.translationsresearch.views.playertextureview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.Checkable;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;

import com.example.translationsresearch.utils.RatioKeeper;
import com.webka.sdk.players.WebkaPlayer;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;


import static com.example.translationsresearch.utils.Utils.getContextArg;
import static java.util.Optional.ofNullable;


/**
 * @author Konstantin Epifanov
 * @since 02.08.2019
 */
public final class PlayerTextureView extends TextureView implements Checkable, Consumer<Point> {

  /** Contract class. */
  public static final Class<?> CONTRACT = PlayerTextureContract.class;

  /** The default attr resource. */
  @AttrRes
  private static final int DEFAULT_ATTRS = 0;

  /** The empty style resource. */
  @StyleRes
  private static final int DEFAULT_STYLE = 0;

  /** Default styleable attributes */
  @StyleableRes
  private static final int[] DEFAULT_STYLEABLE = new int[0];

  private final PlayerTextureContract mContract;
  private PlayerTexturePresenter mPresenter;


  ReplayProcessor<Optional<String>> mProcessorItem = ReplayProcessor.cacheLast();

  ReplayProcessor<Boolean>
  mProcessorActivated = ReplayProcessor.cacheLastOrDefault(true),
  mProcessorChecked = ReplayProcessor.cacheLastOrDefault(true);

  private Runnable mDisposable = null;

  private boolean isChecked;

  /** Aspect Ratio Keeper. */
  private final RatioKeeper mRatioKeeper = new RatioKeeper(this::setTransform);


  public PlayerTextureView(Context context) {
    this(context, null);
  }

  public PlayerTextureView(Context context, AttributeSet attrs) {
    this(context, attrs, DEFAULT_ATTRS);
  }

  public PlayerTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, DEFAULT_STYLE);
  }

  public PlayerTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    mContract = new PlayerTextureContract(mProcessorItem, mProcessorActivated, mProcessorChecked) {
      @Override PlayerTextureView getView() { return PlayerTextureView.this; }};

    final Resources.Theme theme = context.getTheme();
    final TypedArray attributes = theme.obtainStyledAttributes(attrs, DEFAULT_STYLEABLE, defStyleAttr, defStyleRes);
    try {
      //ignore
    } finally {
      attributes.recycle();
    }
  }

  /** {@inheritDoc} */
  @Override
  protected final void onSizeChanged(int nw, int nh, int ow, int oh) {
    mRatioKeeper.viewPort(nw, nh);
    super.onSizeChanged(nw, nh, ow, oh);
  }

  /** {@inheritDoc} */
  @Override
  public final void setScaleX(float value) {
    mRatioKeeper.scaleX(value);
    super.setScaleX(value);
  }

  @Override
  public final void setScaleY(float value) {
    mRatioKeeper.scaleY(value);
    super.setScaleY(value);
  }

  /** {@inheritDoc} */
  @Override
  public synchronized final void accept(Point size) {
    mRatioKeeper.videoSize(size.x, size.y);
  }

  /** {@inheritDoc} */
  @Override public final void setTag(Object tag) {
    super.setTag(tag);
    mProcessorItem.onNext(ofNullable((String) tag));
  }

  @Override
  public void setActivated(boolean activated) {
    boolean changed = isActivated() != activated;
    super.setActivated(activated);
    if (!changed) return;
    mProcessorActivated.onNext(isActivated());
  }

  @Override
  public void setChecked(boolean checked) {
    if (isChecked == checked) return;
    isChecked = checked;
    mProcessorChecked.onNext(isChecked());
  }

  @Override
  public boolean isChecked() {
    return isChecked;
  }

  @Override
  public void toggle() {
    isChecked = !isChecked;
  }

  @NonNull
  public static Function<Object, ? extends Disposable> presenter(
    Supplier<BiFunction<String, TextureView, Mono<WebkaPlayer>>> webkaPlayerSupplier) {
    return (Function<Object, Disposable>) view ->
      new PlayerTexturePresenter((PlayerTextureContract) view, webkaPlayerSupplier.get());
  }

  @Override
  public final void onVisibilityAggregated(boolean value) {
    if (mDisposable != null) {
      mDisposable.run();
      mDisposable = null;
    }

    if (!value) detach();
    super.onVisibilityAggregated(value);
    if (value)
      if (isAvailable()) attach();
      else mDisposable = attachWhenReady(this, this::attach);
  }

  private static Runnable attachWhenReady(@NonNull TextureView view, @NonNull Runnable runnable) {
    final boolean[] disposed = { false };
    final SurfaceTextureListener listener = view.getSurfaceTextureListener();
    view.setSurfaceTextureListener(
      onAvailable(
        () -> view.setSurfaceTextureListener(listener),
        () -> { if (!disposed[0]) runnable.run(); }
      ));
    return () -> disposed[0] = true;
  }

  @Override
  protected void onDetachedFromWindow() {
    // exoplayer adds it own listener to the texture and call it on release in main thread,
    // that's why we must clean all listeners before release
    setSurfaceTextureListener(null);

    if (mDisposable != null) {
      mDisposable.run();
      mDisposable = null;
    }

   detach();
    super.onDetachedFromWindow();
  }

  private void attach() {
    if (mPresenter == null && !this.isInEditMode()) {
      mPresenter = new PlayerTexturePresenter(mContract, getContextArg(getContext(), "webka.player"));
    }
  }

  private void detach() {
    if (mPresenter != null) {
      mPresenter.dispose();
      if (mPresenter.isDisposed()) mPresenter = null;
    }
  }

  @NonNull
  private static SurfaceTextureListener onAvailable(@NonNull Runnable... callbacks) {

    return new SurfaceTextureListener() {

      private void throwAsNotSupported() {
        throw new IllegalStateException("Not supported method");
      }

      @Override
      public final void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        for (final Runnable task : callbacks) task.run();
      }

      @Override
      public final void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        throwAsNotSupported();
      }

      @Override
      public final boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        throwAsNotSupported();
        return true;
      }

      @Override
      public final void onSurfaceTextureUpdated(SurfaceTexture surface) {
        throwAsNotSupported();
      }
    };

  }



  /**
   * @author Konstantin Epifanov
   * @since 02.08.2019
   */
  private static class PlayerTextureContractImpl extends PlayerTextureContract {

    private PlayerTextureView view;

    PlayerTextureContractImpl(PlayerTextureView view) {
        super(
        view.mProcessorItem,
        view.mProcessorActivated,
        view.mProcessorChecked);

      this.view = view;
    }

    @Override
    PlayerTextureView getView() {
      return view;
    }

  }
}
