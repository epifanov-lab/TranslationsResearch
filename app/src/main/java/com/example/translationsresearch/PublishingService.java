package com.example.translationsresearch;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.translationsresearch.utils.Json;
import com.webka.sdk.data.LocalStorage;
import com.webka.sdk.webrtc.WebRTC;

import org.json.JSONObject;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;
import ru.realtimetech.webka.client.Client;

/**
 * @author Konstantin Epifanov
 * @since 25.10.2019
 */
@Singleton
public class PublishingService {

  public static final String NAME = "webka.publishing";

  private final Client mClient;
  private final LocalStorage mStorage;

  @Inject
  PublishingService(Client client, LocalStorage storage) {
    mClient = client;
    mStorage = storage;
  }

  @SuppressLint("WrongConstant")
  public static Optional<PublishingService> obtain(Context context) {
    return Optional.ofNullable(context.getSystemService(NAME))
      .map(o -> (PublishingService) o);
  }


  public Mono<Translation> startTranslation(int systemTagId, String name) {
    return Translation.start(mClient, systemTagId, name);
  }

  public Mono<String> stopTranslation(long roomId, String translationId) {
    return Translation.stop(mClient, roomId, translationId);
  }

  public Mono<Tuple3<Long, Long, String>> msCreateSession(boolean isHoster, long roomId) {
    String params = Json.newJson(body -> body
      //.put("hosterMediaId", null)
      .put("isHoster", isHoster)
      .put("roomId", roomId)
      .put("hosterMediaId", JSONObject.NULL)
    ).toString();

    return Mono.from(mClient.command("ms_create_session", params))
      .map(Json::array)
      .map(jsonArray -> Json.getObject(jsonArray, 0))
      .map(jsonObject -> Tuples.of(roomId, Json.getLong(jsonObject, "mediaId"), Json.getString(jsonObject, "mediaIdSign")));
  }

  public Mono<String> msOffer(WebRTC.Session session, WebRTC.SDP sdp) {
    String params = Json.newJson(body -> body
      .put("hosterMediaId", JSONObject.NULL)
      .put("isHoster", true)
      .put("roomId", session.roomId)
      .put("mediaId", String.valueOf(session.mediaId))
      .put("mediaIdSign", session.mediaIdSign)
      .put("jsep", new JSONObject()
        .put("type", sdp.type)
        .put("sdp", sdp.description))
    ).toString();

    return Mono.from(mClient.command("ms_offer", params))
      .then(Flux.from(mClient.events("media_server_event")).next());
  }

  public Mono<String> msCandidate(WebRTC.Session session, WebRTC.ICE ice) {
    String params = Json.newJson(body -> body
      .put("isHoster", true)
      .put("hosterMediaId", JSONObject.NULL)
      .put("roomId", session.roomId)
      .put("mediaId", String.valueOf(session.mediaId))
      .put("mediaIdSign", session.mediaIdSign)
      .put("candidate", new JSONObject()
        .put("candidate", ice.sdp)
        .put("sdpMLineIndex", ice.index)
        .put("sdpMid", String.valueOf(ice.index)))
    ).toString();

    return Mono.from(mClient.command("ms_candidate", params));
  }

  //  42["command",{"name":"ms_create_session","commandId":1,"isHoster":true,"roomId":7733247237752183,"hosterMediaId":null,"windowId":"28f4026c-2f30-4b31-8e02-6f400a363178"}]
  //  42["command",{"name":"ms_create_session","commandId":3423744902,"hosterMediaId":null,"roomId":8194637177757622,"isHoster":true,"windowId":"8d75096f-9088-453a-80f7-bc04a878a79a"}]

  public Mono<String> msDestroy(boolean isHoster, String mediaId, String mediaIdSign) {
    String params = Json.newJson(body -> body
      //.put("hosterMediaId", null)
      .put("isHoster", isHoster)
      .put("mediaId", mediaId)
      .put("mediaIdSign", mediaIdSign)
    ).toString();

    return Mono.from(mClient.command("ms_destroy", params));
  }

}
