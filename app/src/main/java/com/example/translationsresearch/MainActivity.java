package com.example.translationsresearch;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.translationsresearch.service.chat.ChatService;
import com.example.translationsresearch.service.translations.PublishingService;
import com.example.translationsresearch.service.translations.TranslationService;
import com.example.translationsresearch.utils.Utils;
import com.webka.sdk.data.DataSources;
import com.webka.sdk.data.LocalStorage;
import com.webka.sdk.players.WebkaPlayerFactory;
import com.webka.sdk.players.WebkaPlayers;
import com.webka.sdk.webrtc.WebRTC;


import org.webrtc.ContextUtils;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import ru.realtimetech.webka.client.Client;

@Module
public class MainActivity extends AppCompatActivity {

  public static final String KEY_SESSION_ID = "key_session_id";
  public static final String KEY_TRANSLATION = "key_translation";

  private WebRTC.Factory mWebRTC;

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

    ContextUtils.initialize(context());
    mWebRTC = mInjector.webrtc();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mRoot.removeAllViews();

    goChat();
  }

  private void goPublish() {
    Utils.createViewWithArgs(this, mRoot,
      R.layout.translations_start);
  }

  private void goPreview() {
    Utils.createViewWithArgs(this, mRoot,
      R.layout.translations_lobby, KEY_SESSION_ID, "3Lf2FHPW2fW");
  }

  private void goChat() {
    Utils.createViewWithArgs(this, mRoot,
      R.layout.translations_chat);
  }

  @Override
  public Object getSystemService(@NonNull String name) {
    if (TranslationService.NAME.equals(name)) return mInjector.translations();
    if (ChatService.NAME.equals(name)) return mInjector.chat();
    if (PublishingService.NAME.equals(name)) return mInjector.publishing();
    if (WebkaPlayerFactory.NAME.equals(name)) return mInjector.players();
    if (WebRTC.NAME.equals(name)) return mWebRTC;
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
      /*"{\n" +
        "        \"api\": {\n" +
        "      \"connect\": \"https://api.int.rtt.space\",\n" +
        "        \"apipath\": \"/api/v1/\"\n" +
        "    },\n" +
        "    \"ws_router\": {\n" +
        "      \"connect\": \"https://int.rtt.space/\",\n" +
        "        \"path\": \"/wss\"\n" +
        "    },\n" +
        "    \"storage\": {\n" +
        "      \"domain\": \"https://storage.rtt.space\"\n" +
        "    },\n" +
        "    \"www\": {\n" +
        "      \"domain\": \"https://int.rtt.space/\"\n" +
        "    }\n" +
        "}",*/
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

  @Override
  protected void onDestroy() {
    mWebRTC.close();
    mWebRTC = null;
    super.onDestroy();
  }

  /** Application activity. */
  @Component(modules = {MainActivity.class, DataSources.class, WebkaPlayers.class})
  @Singleton
  public static abstract class Injector {
    public abstract Context context();
    public abstract Client client();
    public abstract LocalStorage storage();
    public abstract TranslationService translations();
    public abstract ChatService chat();
    public abstract PublishingService publishing();
    public abstract WebkaPlayerFactory players();
    public abstract WebRTC.Factory webrtc();
  }

}
