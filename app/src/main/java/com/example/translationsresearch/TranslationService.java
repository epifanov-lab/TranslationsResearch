package com.example.translationsresearch;

import android.annotation.SuppressLint;
import android.content.Context;

import com.webka.sdk.data.LocalStorage;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import reactor.core.publisher.Mono;
import ru.realtimetech.webka.client.Client;

/**
 * @author Konstantin Epifanov
 * @since 17.10.2019
 */
@Singleton
public class TranslationService {

  public static final String NAME = "webka.translations";

  private final Client mClient;
  private final LocalStorage mStorage;

  @Inject
  TranslationService(Client client, LocalStorage storage) {
    mClient = client;
    mStorage = storage;
  }

  @SuppressLint("WrongConstant")
  public static Optional<TranslationService> obtain(Context context) {
    return Optional.ofNullable(context.getSystemService(NAME))
      .map(o -> (TranslationService) o);
  }

  public Mono<Translation> getTranslationBySessionId(String sessionId) {
    return Translation.bySessionId(mClient, sessionId);
  }

}
