/*
 * Message.java
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

package com.example.translationsresearch.service.chat;

import com.example.translationsresearch.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Objects;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.realtimetech.webka.client.Client;


/**
 * Message structure.
 *
 * @author Gleb Nikitenko
 * @since 20.03.19
 */
public final class Message {

  /** Message id. */
  public final long id;

  /** Chat ID. */
  public final long chatId;

  /** Time stamp. */
  public final ZonedDateTime createTime;

  /** Message text. */
  public final String text;

  /** Avatar. */
  public final String avatarBigUrl;

  /** User Id */
  public final long userId;

  /** Author name. */
  public final String fullName;

  /** Author nik */
  public final String nickName;

  /** User Id if personal */
  public final long toUserId;

  /** Is Admin flag */
  public final boolean isAdmin;

  /** Object hash. */
  private final int mHash;

  /**
   * Constructs a new {@link Message}.
   *
   * @param json json response object
   *
   * @throws JSONException json parsing exception
   */
  private Message(JSONObject json) throws JSONException {
    id = Json.getInteger(json, "messageId");
    chatId = json.optInt("chatId", -1);

    String dateString = Json.optString(json, "startTime", null);
    createTime = dateString != null ? ZonedDateTime.parse(dateString) : null;

    text = Json.getString(json, "text");
    avatarBigUrl = Json
      .getJsonObject(json, "avatar")
      .flatMap(object -> Json.getJsonObject(object, "big"))
      .flatMap(object -> Json.getJsonString(object, "url"))
      .orElse(null);
    userId = Json.getInteger(json, "userId");
    fullName = Json.getString(json, "fullName");
    nickName = Json.getString(json, "nickname");
    toUserId = Json.optInteger(json, "toUserId").orElse(-1);
    isAdmin = Json.getBoolean(json, "isAdmin");
    mHash = Math.toIntExact(id);
  }

  /**
   * @param json json array
   *
   * @return set of messages
   */
  static Message[] just(JSONArray json) {
    try {
      final Message[] result = new Message[json.length()];
      for (int i = 0; i < result.length; i++)
        result[i] = new Message(json.getJSONObject(i));
      return result;
    } catch (JSONException | NullPointerException exception) {
      throw new IllegalArgumentException(exception);
    }
  }

  /**
   * @param json json-object
   *
   * @return single message instance
   */
  static Message just(JSONObject json) {
    try {
      return new Message(json);
    } catch (JSONException | NullPointerException exception) {
      throw new IllegalArgumentException(exception);
    }
  }

  static Mono<Message[]> list(Client client, String sessionId, int limit) {
    return Mono.from(client.get("translation/chat",
      "sessionId", sessionId, "limit", limit, "isForward", 0))
      .map(Json::array).map(Message::just);
  }

  static Flux<Message> eventMessageSent(Client client, int chatId) {
    return Flux.from(client.events("chat_message_sent", "chat",
      Json.newJson(body -> body.put("chatId", chatId)).toString()))
      .flatMap(Message::asyncParseFromEvent);
  }

  static Flux<String> eventMessageSentRaw(Client client, int chatId) {
    return Flux.from(client.events("chat_message_sent", "chat",
      Json.newJson(body -> body.put("chatId", chatId)).toString()));
  }

  private static Mono<Message> asyncParseFromEvent(String response) {
    Message message = null;
    JSONArray ja = Json.array(response);
    JSONObject jo0 = Json.getObject(ja, 0);
    JSONObject jod = Json.getObject(jo0, "data").orElse(null);
    if (jod != null) message = Message.just(jod);
    return message != null ? Mono.just(message)
      : Mono.error(() -> new IOException("Can't parse message from json: " + response));
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  static Message parseFromEvent(String response) {
    final JSONArray ja = Json.array(response);
    final JSONObject jo0 = Json.getObject(ja, 0);
    final JSONObject jod = Json.getObject(jo0, "data").get();
    return Message.just(jod);
  }

  /** {@inheritDoc} */
  @Override
  public final boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    final Message message = (Message) obj;
    return Objects.equals(id, message.id) && Objects.equals(text, message.text) && Objects.equals(
      createTime, message.createTime) && Objects.equals(fullName, message.fullName)
      && Objects.equals(nickName, message.nickName) && chatId == message.chatId;
  }

  /** {@inheritDoc} */
  @Override
  public final int hashCode() {
    return mHash;
  }

  @Override
  public String toString() {
    return fullName + ": " + text;
  }
}
