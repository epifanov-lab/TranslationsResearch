package com.example.translationsresearch.service.chat;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Optional;

import javax.inject.Inject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.realtimetech.webka.client.Client;

/**
 * @author Konstantin Epifanov
 * @since 14.11.2019
 */
public class ChatService {

  public static final String NAME = "webka.ChatService";

  private final Client mClient;

  @Inject
  ChatService(Client client) {
    mClient = client;

    Flux.from(mClient.connected()).log().subscribe();
  }

  @SuppressLint("WrongConstant")
  public static Optional<ChatService> obtain(Context context) {
    return Optional.ofNullable(context.getSystemService(NAME))
      .map(o -> (ChatService) o);
  }

  public Mono<Message[]> messages(String sessionId) {
    return Message.list(mClient, sessionId, 20);
  }

  public Flux<Message> source() {
    return Message.subscribe(mClient, 1);
  }

}
