package com.example.translationsresearch.service.chat;

import com.example.translationsresearch.utils.Json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.reactivestreams.Publisher;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import androidx.annotation.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import ru.realtimetech.webka.client.Client;

import static reactor.core.publisher.ReplayProcessor.cacheLastOrDefault;


/**
 * Message structure.
 *
 * @author Gleb Nikitenko
 * @author Konstantin Epifanov
 * @since 20.03.19
 */
public class Message {

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
  private static Message[] just(JSONArray json) {
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
  private static Message just(JSONObject json) {
    try {
      return new Message(json);
    } catch (JSONException | NullPointerException exception) {
      throw new IllegalArgumentException(exception);
    }
  }

  /**
   * @param response ws event response string
   *
   * @return single message instance
   */
  @SuppressWarnings("OptionalGetWithoutIsPresent")
  private static Message parseFromEvent(String response) {
    final JSONArray ja = Json.array(response);
    final JSONObject jo0 = Json.getObject(ja, 0);
    final JSONObject jod = Json.getObject(jo0, "data").get();
    return Message.just(jod);
  }

  /**
   * @param client webka client
   * @param text   message text
   * @param chatId chatId
   *
   * @return single message instance
   */
  static Mono<Message> post(Client client, @NonNull String text, long chatId) {
    return Mono.from(client.post("chat/message", "chatId", chatId, "text", text))
      .map(json -> Message.just(Json.object(json)));
  }

  /**
   * @param client    webka client
   * @param sessionId translation sessionId
   * @param chatId chatId
   *
   * @return Flux of actual chat messages array
   */
  @SuppressWarnings("unchecked")
  static Flux<Message[]> dataSource(Client client, String sessionId, int chatId) {
    return Flux.from(
              Message.source( // TODO -> client.source
                      client.connected(),
                      Message::addToList,
                      () -> list(client, sessionId, 100),
                      eventMessageSentRaw(client, chatId)));
  }

  /**
   * @param client    webka client
   * @param sessionId translation sessionId
   * @param limit     count of requested messages
   *
   * @return messages array
   */
  private static Mono<Message[]> list(Client client, String sessionId, int limit) {
    return Mono.from(client.get("translation/chat",
                     "sessionId", sessionId, "limit", limit, "isForward", 0))
      .map(Json::array).map(Message::just);
  }

  /**
   * @param client webka client
   * @param chatId chatId
   *
   * @return flux of message event json strings
   */
  private static Flux<String> eventMessageSentRaw(Client client, int chatId) {
    return Flux.from(client.events("chat_message_sent", "chat",
                    Json.newJson(body -> body.put("chatId", chatId)).toString()));
  }

  /**
   * @param rawMessage  message event json string
   * @param messages    messages array
   *
   * @return messages array with new message added to end
   */
  private static Message[] addToList(String rawMessage, Message[] messages) {
    final int lastPos = messages.length; messages = Arrays.copyOf(messages, lastPos + 1);
    messages[lastPos] = Message.parseFromEvent(rawMessage); return messages;
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

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Message{" +
      "id=" + id +
      ", text='" + text +
      ", userId=" + userId +
      ", fullName='" + fullName +
      '}';
  }

   /** // TODO MOVE TO CLIENT
   * @param connected web socket connection state flux
   * @param mapper    applies events to last cached data from request
   * @param request   rest request supplier
   * @param events    ws events for apply changes
   *
   * @return T is a result of REST request
   */
  @SuppressWarnings("unchecked")
  private static <T> Publisher<T> source(Publisher<Boolean> connected,
                                        BiFunction<String, ? super T, ? extends T> mapper,
                                        Supplier<Mono<T>> request, Publisher<String>... events) {

    final T empty = (T) new Object();

    final ReplayProcessor<T> source = cacheLastOrDefault(empty);
    final FluxSink<T> sink = source.sink(FluxSink.OverflowStrategy.IGNORE);

    final int last = events.length;
    final Publisher[] publishers = Arrays.copyOf(events, last + 1);
    publishers[last] = Flux
      .from(connected)
      .flatMap(isConnected -> isConnected ? request.get() : Mono
        .just(empty)
        .log());

    return Flux
      .merge(publishers)
      .filter(v -> empty != v)
      .flatMap(o -> {
        if (o instanceof String) {
          return source.next()
                       .map(v -> mapper.apply(((String) o), v));
        } else return Mono.just((T) o);
      })
      .doOnNext(o -> sink.next((T) o));
  }
}
