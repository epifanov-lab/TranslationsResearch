 public final Mono<Void> webrtc(@NonNull WebRTC.Session session) {

    final Tuple2<CompletableFuture<WebRTC.SDP>,
      CoreSubscriber<WebRTC.SDP>> answer = reactor.core.publisher.PublisherUtils.monoFuture();
    final CompletableFuture<WebRTC.SDP> get = answer.getT1();
    final CoreSubscriber<WebRTC.SDP> set = answer.getT2();

    final WebRTCConnection connection = mWebRTC.connection (
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
      .transform(Schedulers::io_work)
      .then();
  }