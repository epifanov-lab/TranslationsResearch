package com.example.translationsresearch.service.chat;

import android.annotation.SuppressLint;
import android.content.Context;

import org.reactivestreams.Publisher;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;
import ru.realtimetech.webka.client.Client;

import static reactor.core.publisher.ReplayProcessor.cacheLastOrDefault;


/**
 * @author Konstantin Epifanov
 * @since 14.11.2019
 */
public class ChatService {

  public static final String NAME = "webka.ChatService";

  private final Client mClient;

  private static final Message[] EMPTY = new Message[0];

  private final ReplayProcessor<Message[]> cacheSource = cacheLastOrDefault(EMPTY);

  private final FluxSink<Message[]> cacheSink = cacheSource.sink(FluxSink.OverflowStrategy.IGNORE);

  @Inject
  ChatService(Client client) {
    mClient = client;
  }

  @SuppressLint("WrongConstant")
  public static Optional<ChatService> obtain(Context context) {
    return Optional
      .ofNullable(context.getSystemService(NAME))
      .map(ChatService.class::cast);
  }

  public Flux<Message[]> source(String sessionId, int chatId) {
    return Flux
      .merge(connected(), eventMessageSent(chatId))
      .filter(o -> o == Boolean.TRUE || o instanceof Message)
      .switchMap(o -> split(sessionId, o))
      .flatMap(o -> processing(cacheSource, o, ChatService::add))
      .doOnNext(cacheSink::next);
  }

  private Publisher<?> split(String sessionId, Object o) {
    if (o instanceof Boolean) return list(sessionId);
    else return Mono.just(o);
  }

  private static Mono<Message[]> processing(Flux<Message[]> source, Object o,
                                            BiFunction<Object, ? super Message[], ?
                                              extends Message[]> mapper) {
    if (o instanceof Message[]) {
      return Mono.just((Message[]) o);
    }
    else {
      return source
        .next()
        .filter(messages -> EMPTY != messages)
        .map(messages -> mapper.apply(o, messages));
    }
  }

  private static Message[] add(Object message, Message[] array) {
    Set<Message> set = Stream
      .of(array)
      .collect(Collectors.toSet()); set.add((Message) message); return set.toArray(new Message[0]);
  }

  private static Message[] add2(Object message, Message[] array) {
    int lastPos = array.length; array = Arrays.copyOf(array, lastPos + 1);
    array[lastPos] = (Message) message;
    //Set<Message> set = Stream.of(array).collect(Collectors.toSet());
    //set.add((Message) message);
    //return set.toArray(new Message[0]);
    return array;
  }

  private Mono<Message[]> list(String sessionId) {
    return Message.list(mClient, sessionId, 20);
  }

  private Flux<Boolean> connected() {
    return Flux.from(mClient.connected());
  }

  private Flux<Message> eventMessageSent(int chatId) {
    return Message.eventMessageSent(mClient, chatId);
  }

  private Flux<String> eventMessageSentRaw(int chatId) {
    return Message.eventMessageSentRaw(mClient, chatId).log();
  }

  public Flux<Message[]> testGenSource1(String sessionId, int chatId) {
    return Flux.from(genSource(mClient.connected(),
                               ChatService::mapper,
                               () -> list(sessionId),
                               eventMessageSentRaw(chatId)));
  }

  private static Message[] mapper(String rawMessage, Message[] messages) {
    final int lastPos = messages.length; messages = Arrays.copyOf(messages, lastPos + 1);
    messages[lastPos] = Message.parseFromEvent(rawMessage); return messages;
  }

  /**
   * @param connected websocket connection flux
   * @param mapper    applies events to last cached data from request
   * @param request   rest request supplier
   * @param events    ws events for apply changes
   *
   * @return T is a result of REST request
   */
  @SuppressWarnings("unchecked")
  public static <T> Publisher<T> genSource(Publisher<Boolean> connected, //todo спрятать
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
