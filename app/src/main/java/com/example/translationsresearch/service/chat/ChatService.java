package com.example.translationsresearch.service.chat;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Optional;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.realtimetech.webka.client.Client;


/**
 * @author Konstantin Epifanov
 * @since 19.11.2019
 */
public class ChatService {

  /** Chat service. */
  public static final String NAME = "webka.ChatService";

  /** Web Client. */
  private final Client mClient;

  /** Constructor. */
  @Inject
  ChatService(Client client) {
    mClient = client;
  }

  /** Obtain from context. */
  @SuppressLint("WrongConstant")
  public static Optional<ChatService> obtain(Context context) {
    return Optional
      .ofNullable(context.getSystemService(NAME))
      .map(ChatService.class::cast);
  }

  public Mono<Message> postMessage(@NonNull String text, long chatId) {
    return Message.post(mClient, text, chatId);
  }

  public Flux<Message[]> source(@NonNull String sessionId, int chatId) {
    return Message.dataSource(mClient, sessionId, chatId);
  }

}
