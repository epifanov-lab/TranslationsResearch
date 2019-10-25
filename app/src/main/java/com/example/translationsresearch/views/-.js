  /**
   * @param session    media session
   * @param subscriber answer subscriber
   * @param sdp        offer sdp
   */
  private void offer
    (@NonNull WebRTC.Session session,
     @NonNull CoreSubscriber<WebRTC.SDP> subscriber,
     @NonNull WebRTC.SDP sdp) {
    System.out.println("PublishingService.offer");
    /*mSocket.sendAndWait("ms_add", body -> body
      .put("isHoster", true)
      .put("mediaId", session.mediaId)
      .put("mediaIdSign", session.mediaIdSign)
      .put("msSessionId", session.sessionId)
      .put("handlerId", session.handlerId)
      .put("jsep", new JSONObject()
        .put("type", sdp.type)
        .put("sdp", sdp.description)
      ), "answer", "jsep")
      .cast(JSONObject.class)
      .map(WebRTC.SDP::just)
      .subscribeWith(subscriber);*/
  }


  /**
   * @param session media session
   * @param ice     sent ice candidate
   */
  private void ice(WebRTC.Session session, WebRTC.ICE ice) {
    System.out.println("PublishingService.ice");
    /*mSocket.command("ms_candidate", body -> body
      .put("isHoster", true)
      .put("mediaId", session.mediaId)
      .put("mediaIdSign", session.mediaIdSign)
      .put("msSessionId", session.sessionId)
      .put("handlerId", session.handlerId)
      .put("candidate", new JSONObject()
        .put("candidate", ice.sdp)
        .put("sdpMLineIndex", ice.index)
        .put("sdpMid", String.valueOf(ice.index))
      )
    ).block();*/
  }

  public Mono<String> wsTest(int chatId) {
    String params = Json.newJson(body -> body.put("chatId", chatId)).toString();
    System.out.println("ws: " + params);
    return Mono.from(mClient.command("subscribe_chat", params));
    //return Mono.empty();
  }

  /** @param session media session */
  @SuppressWarnings("unused")
  private void ice(WebRTC.Session session) {}

  /** @param media media pair */
  @SuppressWarnings("unused")
  private void media(WebRTC.Session session, WebRTC.MediaPair media) {}