package com.example.translationsresearch.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.translationsresearch.PublishingService;
import com.example.translationsresearch.R;
import com.example.translationsresearch.Translation;
import com.example.translationsresearch.utils.Json;
import com.webka.sdk.schedulers.Schedulers;
import com.webka.sdk.webrtc.WebRTC;
import com.webka.sdk.webrtc.WebRTCCapturer;
import com.webka.sdk.webrtc.WebRTCConnection;

import java.util.concurrent.CompletableFuture;

import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

/**
 * @author Konstantin Epifanov
 * @since 18.10.2019
 */
public class TranslationsStartView extends FrameLayout {

  public static final int TAG_CHIPS = 1;

  private final PublishingService mPublishingService;
  private final WebRTC.Factory mWebRTC;

  private TextView mLabel;
  private Button mRetryButton;

  private Disposable mDisposable;

  public TranslationsStartView(@NonNull Context context) {
    this(context, null);
  }

  public TranslationsStartView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public TranslationsStartView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public TranslationsStartView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mPublishingService = PublishingService.obtain(context).get();

    mWebRTC = WebRTC.from(context);
    //System.out.println("WEBRTC: " + mWebRTC);
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
      //mDisposable = Disposables.disposed();
    }
  }

  private Disposable start() {

    final WebRTCCapturer capturer = mWebRTC.capturer();

    final Disposable.Swap swap = Disposables.swap();

    updateViewsState("START", false);

    final Translation[] translations = new Translation[1];

    return Disposables.composite(

      mPublishingService.startTranslation(TAG_CHIPS, "android_publishing _test_" + System.currentTimeMillis())
        .flatMap(translation -> {
            translations[0] = translation;
            return mPublishingService.msCreateSession(true, translation.roomId)
              .flatMap(this::webrtc);
          }

          //.flatMap(d -> mPublishingService.msOffer(true, d.getT1(), d.getT2(), d.getT3(), null))
          //return mPublishingService.msOffer(true, d.getT1(), d.getT2(), d.getT3());
        )

        .transform(Schedulers::work_main)
        .subscribe(
          s -> {} /*s -> System.out.println("WEBRTC CONNECTED @%^$^@$#^@$#^%@: " + s)*/,

          t -> updateViewsState("error: " + t.getMessage(), true),

          () -> System.out.println("WEBRTC CONNECTED @%^$^@$#^@$#^%@: ")),


      () -> mPublishingService.stopTranslation(translations[0].roomId, translations[0].id).subscribe()

    );
  }

  public final Mono<Void> webrtc(@NonNull Tuple3<Long, Long, String> tuple) {

    final WebRTC.Session session = new WebRTC.Session(tuple);

    final Tuple2<CompletableFuture<WebRTC.SDP>, CoreSubscriber<WebRTC.SDP>>
      answer = reactor.core.publisher.PublisherUtils.monoFuture();

    final CompletableFuture<WebRTC.SDP> get = answer.getT1();
    final CoreSubscriber<WebRTC.SDP> set = answer.getT2();

    final WebRTCConnection connection = mWebRTC.connection(
      true,
      get::join,                    /// дождаться ансвер от мс и заакцептить в пеер кон
      v -> offer(session, set, v),  /// пеерконнекшн зарегал сдп и отдал мне и я отпр на мс офер
      v -> ice(session, v),         /// пеерконекшн выплюнул айс, отправляем на мс
      () -> ice(session),           /// айсы закончились
      v -> media(session, v)        /// получен удаленные трэки
    );

    return Mono.create(sink -> sink
      .onCancel(connection::release)
      .success(connection.connect()))
      .doOnNext(o -> System.out.println("CONNECTION CONNECT ^*%*&@%^%#^&#%@"))
      .transform(Schedulers::io_work)
      .then();
  }

  /**
   * @param session    media session
   * @param subscriber answer subscriber
   * @param sdp        offer sdp
   */
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private void offer
  (@NonNull WebRTC.Session session,
   @NonNull CoreSubscriber<WebRTC.SDP> subscriber,
   @NonNull WebRTC.SDP sdp) {
    System.out.println("PublishingService.offer");
    mPublishingService.msOffer(session, sdp)
      .log()
      .map(Json::array)
      .map(arr -> Json.getObject(arr, 0))
      .map(obj -> Json.getObject(obj, "jsep").get())
      .map(WebRTC.SDP::just)
      .subscribeWith(subscriber);
  }

  /**
   * @param session media session
   * @param ice     sent ice candidate
   */
  private Void ice(WebRTC.Session session, WebRTC.ICE ice) {
    System.out.println("PublishingService.ice: " + ice);
    return mPublishingService.msCandidate(session, ice).then().block();
  }

  /** @param session media session */
  @SuppressWarnings("unused")
  private void ice(WebRTC.Session session) {
  }

  /** @param media media pair */
  @SuppressWarnings("unused")
  private void media(WebRTC.Session session, WebRTC.MediaPair media) {
  }

  private void updateViewsState(String text, boolean isError) {
    mLabel.setText(text);
    mLabel.setBackgroundColor(getResources().getColor(isError ? R.color.red : R.color.black));
    mRetryButton.setVisibility(isError ? VISIBLE : INVISIBLE);
  }
}
