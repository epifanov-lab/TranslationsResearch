package com.example.translationsresearch.entity;

import com.example.translationsresearch.utils.Json;

import org.json.JSONObject;

/**
 * @author Konstantin Epifanov
 * @since 18.10.2019
 */
public class TranslationBySessionId {

  /** Translation id. */
  public final String id;

  /** Session id. */
  public final String sessionId;

  /** Chat id. */
  public final int chatId;

  /** Translation name. */
  public final String name;

  /** Translation description. */
  public final String description;

  /** User nickname */
  public final String userNickName;

  /** User full name */
  public final String userFullName;

  /** View count */
  public final String viewCount;

  /** Stream Width Pixels */
  public final int streamWidth;

  /** Stream Height Pixels */
  public final int streamHeight;

  /** Time stamp. */
  public final String time;

  /** Media stream. */
  public final String media;

  /** Media stream id. */
  public final int streamMediaId;

  /**
   * Translation Item global position
   * of backend by last response.
   */
  //public final int pagingOffset;

  /** Cover picture. */
  public final String cover;

  /** Object hash. */
  private final int mHash;

  private TranslationBySessionId(JSONObject json, String covers) {
    id = Json.getString(json, "translationId");
    sessionId = Json.getString(json, "sessionId");
    chatId = Json.getInteger(json, "chatId");
    name = Json.getString(json, "translationName");
    description = Json.getString(json, "translationDescription");
    userNickName = Json.getString(json, "nickname");
    userFullName = Json.getString(json, "fullName");
    viewCount = Json.getString(json, "viewCount");
    time = Json.getString(json, "startTime");
    //pagingOffset = Json.getInteger(json, "pageOffset");
    media = Json.getString(json, "streamMediaUrl");

    //try {
    //final String url = requireNonNull(json.getString("streamMediaUrl"), json::toString)
    /*.replace("10.10.0.8", "s1.rtt.space").replace("http", "https")*/
    //media = "null".equals(url) ? "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8" : url;
    //media = "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_adv_example_hevc/v3/prog_index.m3u8";
    //media = "http://online.video.rbc.ru/online/rbctv_480p/index.m3u8";
    //media = "https://strm.yandex.ru/kal/1hd/1hd0_169_480p.json/index-v1-a1.m3u8";
    /*} catch (JSONException e) {
      media = null;
    }*/

    streamWidth = Json.optInteger(json, "streamWidth").orElse(-1);
    streamHeight = Json.optInteger(json, "streamHeight").orElse(-1);
    streamMediaId = Json.optInteger(json, "streamMediaId").orElse(-1);

    cover = covers + "/" + streamMediaId;

    mHash = id.hashCode();
  }

}
