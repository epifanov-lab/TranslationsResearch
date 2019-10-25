package com.example.translationsresearch;

import android.annotation.SuppressLint;
import android.content.Context;

import com.example.translationsresearch.utils.Json;
import com.webka.sdk.data.LocalStorage;

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
    ).toString();

    return Mono.from(mClient.command("ms_create_session", params))
      .map(Json::array)
      .map(jsonArray -> Json.getObject(jsonArray, 0))
      .map(jsonObject -> Tuples.of(roomId, Json.getLong(jsonObject, "mediaId"), Json.getString(jsonObject, "mediaIdSign")));
  }

  public Mono<String> msOffer(boolean isHoster, long roomId, long mediaId, String mediaIdSign, JSONObject jsep) {

    String params = Json.newJson(body -> body
      //.put("hosterMediaId", null)
      .put("isHoster", isHoster)
      .put("roomId", roomId)
      .put("mediaId", mediaId)
      .put("mediaIdSign", mediaIdSign)
      .put("jsep", jsep)
    ).toString();

    return Mono.from(mClient.command("ms_offer", params))
      .then(Flux.from(mClient.events2("media_server_event")).next());
  }

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
