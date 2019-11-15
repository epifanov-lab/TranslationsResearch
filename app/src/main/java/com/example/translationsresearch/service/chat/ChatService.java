package com.example.translationsresearch.service.chat;

import android.annotation.SuppressLint;
import android.content.Context;

import org.reactivestreams.Publisher;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
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
    return Optional.ofNullable(context.getSystemService(NAME))
      .map(o -> (ChatService) o);
  }

  public Flux<Message[]> source(String sessionId, int chatId) {
    return Flux.merge(connected(), eventMessageSent(chatId))
      .filter(o -> o == Boolean.TRUE || o instanceof Message)
      .switchMap(o -> split(sessionId, o))
      .flatMap(o -> processing(cacheSource, o, ChatService::add))
      .doOnNext(cacheSink::next);
  }

  public Flux<Message[]> source2(String sessionId, int chatId) {
    return genSource(
      Flux.from(mClient.connected()),
      (s, t) -> new Message[0],
      () -> list(sessionId),
      eventMessageSentRaw(chatId)
    );
  }

  /**
   * T is a result of REST request
   * */
  @SuppressWarnings("unchecked")
  public static <T> Flux<T> genSource(Flux<Boolean> connected,
                                      BiFunction<String, ? super T, ? extends T> mapper,
                                      Supplier<Mono<T>> requestFactory,
                                      Flux<String>... events) {

    final T empty = (T) new Object();
    final ReplayProcessor<T> source = ReplayProcessor.cacheLastOrDefault(empty);
    final FluxSink<T> sink = source.sink(FluxSink.OverflowStrategy.IGNORE);
    final Publisher[] publishers =
      Stream
        .concat(Stream.of(connected), Stream.of(events))
        .toArray(Publisher[]::new);
    return Flux.merge(publishers)
      .filter(o -> o == Boolean.TRUE || o instanceof String)
      .switchMap((Function<Object, Publisher<?>>) o -> {
        if (o instanceof Boolean) return requestFactory.get();
        else return Mono.just(o);
      })
      .flatMap(o -> {
        if (o instanceof String) {
          return source.next()
            .filter(s1 -> EMPTY != s1)
            .map(v -> mapper.apply(((String) o), v));
        } else return Mono.just((T) o);
      })
      .doOnNext(o -> sink.next((T) o));
  }


  private static <T> Publisher<?> split(Supplier<Mono<T>> rester, Object o) {
    if (o instanceof Boolean) return rester.get();
    else return Mono.just(o);
  }

  private Publisher<?> split(String sessionId, Object o) {
    if (o instanceof Boolean) return list(sessionId);
    else return Mono.just(o);
  }

  private static Mono<Message[]> processing(Flux<Message[]> source, Object o,
                                         BiFunction<Object, ? super Message[], ? extends Message[]> mapper) {
    if (o instanceof Message[]) {
      return Mono.just((Message[]) o);
    } else {
      return source.next()
        .filter(s1 -> EMPTY != s1)
        .map(messages -> mapper.apply(o, messages));
    }
  }

  private static <T> Mono<T> genProcessing(Flux<T> source, Object o,
                                           BiFunction<String, ? super T, ? extends T> mapper) {
    if (o instanceof String) {
      return source.next()
        .filter(s1 -> EMPTY != s1)
        .map(v -> mapper.apply(((String) o), v));
    } else
      return Mono.just((T) o);

  }

  private static Message[] add(Object message, Message[] array) {
    Set<Message> set = Stream.of(array).collect(Collectors.toSet());
    set.add((Message) message);
    return set.toArray(new Message[0]);
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
    return Message.eventMessageSentRaw(mClient, chatId);
  }
}
