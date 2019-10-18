package com.example.translationsresearch.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.translationsresearch.R;
import com.example.translationsresearch.entity.Translation;
import com.example.translationsresearch.service.TranslationService;
import com.example.translationsresearch.utils.Utils;
import com.webka.sdk.schedulers.Schedulers;

import reactor.core.Disposable;

import static com.example.translationsresearch.MainActivity.KEY_SESSION_ID;
import static com.example.translationsresearch.MainActivity.KEY_TRANSLATION;

/**
 * @author Konstantin Epifanov
 * @since 18.10.2019
 */
public class TranslationsLobbyView extends FrameLayout {

  private final TranslationService mService;
  private final String sessionId;

  private TextView mLabel;
  private Button mRetryButton;

  private Disposable mDisposable;

  public TranslationsLobbyView(@NonNull Context context) {
    this(context, null);
  }

  public TranslationsLobbyView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TranslationsLobbyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public TranslationsLobbyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mService = TranslationService.obtain(context).get();
    sessionId = Utils.getContextArg(context, KEY_SESSION_ID);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mLabel = findViewById(R.id.label_lobby);
    mRetryButton = findViewById(R.id.button_retry);

    mRetryButton.setOnClickListener(v -> start());
  }

  @Override
  public void onVisibilityAggregated(boolean isVisible) {
    if (!isVisible && mDisposable != null) {
      mDisposable.dispose();
      mDisposable = null;
    }

    super.onVisibilityAggregated(isVisible);

    if (isVisible && mDisposable == null) {
      mDisposable = start();
    }
  }

  private Disposable start() {

    updateViewsState();

    return mService.getTranslationBySessionId(sessionId)
      .transform(Schedulers::work_main)
      .subscribe(this::replace, this::onError);
  }

  private void replace(Translation translation) {
    System.out.println("TRANSLATION\n:" + translation);
    ViewGroup root = (ViewGroup) getParent();
    int index = root.indexOfChild(this);
    root.removeViewAt(index);
    Utils.createViewWithArgsAt(getContext(),
      root, R.layout.translations_room, index, KEY_TRANSLATION, translation);
  }

  private void onError(Throwable throwable) {
    throwable.printStackTrace();

    mLabel.setText("ERROR");
    mLabel.setBackgroundColor(getResources().getColor(R.color.red));
    mRetryButton.setVisibility(VISIBLE);

  }

  private void updateViewsState() {
    mLabel.setText("LOADING...");
    mLabel.setBackgroundColor(getResources().getColor(R.color.black));
    mRetryButton.setVisibility(INVISIBLE);
  }
}
