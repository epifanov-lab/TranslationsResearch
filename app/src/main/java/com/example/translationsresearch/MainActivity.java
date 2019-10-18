package com.example.translationsresearch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.TextureView;
import android.view.ViewGroup;

import com.example.translationsresearch.service.TranslationService;
import com.example.translationsresearch.utils.Utils;
import com.webka.sdk.data.DataSources;
import com.webka.sdk.data.LocalStorage;
import com.webka.sdk.players.WebkaPlayer;
import com.webka.sdk.players.WebkaPlayers;
import com.webka.sdk.schedulers.Schedulers;

import java.util.function.BiFunction;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import reactor.core.publisher.Mono;
import ru.realtimetech.webka.client.Client;

@Module
public class MainActivity extends AppCompatActivity {

  public static final String KEY_SESSION_ID = "key_session_id";
  public static final String KEY_TRANSLATION = "key_translation";

  /** MainActivity Injector. */
  public final Injector mInjector =
    DaggerMainActivity_Injector
      .builder()
      .mainActivity(this)
      .build();

  private ViewGroup mRoot;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.translations_root);
    mRoot = findViewById(R.id.root);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mRoot.removeAllViews();
    Utils.createViewWithArgs(this,
      mRoot, R.layout.translations_lobby,
      KEY_SESSION_ID, "3Lf2FHPW2fE"); //"3Lf2FHPW2fE"
  }

  @Override
  public Object getSystemService(@NonNull String name) {
    if (TranslationService.NAME.equals(name)) return mInjector.translations();
    if ("webka.player".equals(name)) return mInjector.webkaPlayer();
    else return super.getSystemService(name);
  }

  /** @return as application context */
  @Provides
  @NonNull
  Context context() {
    return getApplicationContext();
  }

  /** @return DataSources Configuration */
  @Provides
  @Singleton
  @NonNull
  DataSources.Config config() {
    return new DataSources.Config(
      BuildConfig.DEBUG,
      BuildConfig.VERSION_CODE,
      "{\n" +
        "  \"api\": {\n" +
        "    \"connect\": \"https://api.webka.com\",\n" +
        "     \"apipath\": \"/api/v1/\"\n" +
        "  },\n" +
        "  \"ws_router\": {\n" +
        "    \"connect\": \"https://webka.com/\",\n" +
        "    \"path\": \"/wss\"\n" +
        "  },\n" +
        "  \"storage\": {\n" +
        "    \"domain\": \"https://storage.webka.com\"\n" +
        "  },\n" +
        "  \"www\": {\n" +
        "    \"domain\": \"https://webka.com/\"\n" +
        "  }\n" +
        "}",
      null
    );
  }

  /** Application activity. */
  @Component(modules = {MainActivity.class, DataSources.class, WebkaPlayers.class})
  @Singleton
  public static abstract class Injector {
    public abstract Context context();
    public abstract Client client();
    public abstract LocalStorage storage();
    public abstract TranslationService translations();
    public abstract BiFunction<String, TextureView, Mono<WebkaPlayer>> webkaPlayer();
  }

}
