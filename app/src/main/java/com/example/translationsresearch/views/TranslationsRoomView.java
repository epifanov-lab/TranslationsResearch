package com.example.translationsresearch.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.translationsresearch.R;
import com.example.translationsresearch.Translation;
import com.example.translationsresearch.TranslationService;
import com.example.translationsresearch.utils.Utils;

import java.io.IOException;
import java.io.UncheckedIOException;

import reactor.core.Disposable;

import static com.example.translationsresearch.MainActivity.KEY_TRANSLATION;

/**
 * @author Konstantin Epifanov
 * @since 18.10.2019
 */
public class TranslationsRoomView extends FrameLayout {

  private final TranslationService mTranslationService;
  private final Translation mTranslation;

  private Disposable mDisposable;

  public TranslationsRoomView(@NonNull Context context) {
    this(context, null);
  }

  public TranslationsRoomView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TranslationsRoomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public TranslationsRoomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mTranslationService = TranslationService.obtain(context).get();
    mTranslation = Utils.getContextArg(context, KEY_TRANSLATION);

    /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      setForegroundGravity(Gravity.CENTER);
    final Resources resources = getResources();
    final int resource = R.drawable.output_lossy;
      CompletableFuture.supplyAsync(() -> load(resources, resource))
        .thenAcceptAsync(this::setForeground, this::post);
      this.postDelayed(() -> ((AnimatedImageDrawable)this.getForeground()).start(), 1000);
    }*/
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    ((TextView) findViewById(R.id.label_result)).setText(mTranslation.toString());
    findViewById(R.id.texture).setTag(mTranslation.streamMediaUrl);
  }

  @Override
  public void onVisibilityAggregated(boolean isVisible) {
    if (!isVisible && mDisposable != null) {
      mDisposable.dispose(); mDisposable = null; }
    super.onVisibilityAggregated(isVisible);
    if (isVisible && mDisposable == null) {
      // mDisposable = start();
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.P)
  private static AnimatedImageDrawable load(Resources resources, int resource) {
    final ImageDecoder.Source source = ImageDecoder.createSource(resources, resource);
    try {return (AnimatedImageDrawable) ImageDecoder.decodeDrawable(source);}
    catch (IOException e) {throw new UncheckedIOException(e);}
  }

}
