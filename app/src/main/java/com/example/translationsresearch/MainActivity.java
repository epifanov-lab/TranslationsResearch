package com.example.translationsresearch;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

    goPublish();
  }

  private void goPublish() {
    Utils.createViewWithArgs(this, mRoot,
      R.layout.translations_start);
  }

  private void goPreview() {
    Utils.createViewWithArgs(this, mRoot,
      R.layout.translations_lobby,
      KEY_SESSION_ID, "3Lf2FHPW2fW");
  }

  @Override
  public Object getSystemService(@NonNull String name) {
    if (TranslationService.NAME.equals(name)) return mInjector.translations();
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
      "{\n" +
        "\t\"www\": {\n" +
        "\t\t\"domain\": \"https://www-075dffd2a583.sandbox.rtt.space/\",\n" +
        "\t\t\"stun\": [\n" +
        "\t\t\t\"stun.l.google.com:19302\"\n" +
        "\t\t],\n" +
        "\t\t\"turn\": [\n" +
        "\t\t\t\"udp://rtt:rttpass@95.211.25.98:3478\",\n" +
        "\t\t\t\"tcp://rtt:rttpass@95.211.25.98:3478\"\n" +
        "\t\t]\n" +
        "\t},\n" +
        "\t\"api\": {\n" +
        "\t\t\"env\": \"integration\",\n" +
        "\t\t\"secret\": \"69a39612c583bf149a3371d980c1a44f\",\n" +
        "\t\t\"connect\": \"https://api-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"apipath\": \"/api/v1/\",\n" +
        "\t\t\"admpath\": \"/admin/v1/\",\n" +
        "\t\t\"partnerpath\": \"/partner/v1/\",\n" +
        "\t\t\"cookie_domain\": \".sandbox.rtt.space\",\n" +
        "\t\t\"log\": {\n" +
        "\t\t\t\"stdout\": 1,\n" +
        "\t\t\t\"directory\": \"/var/www/log/\"\n" +
        "\t\t}\n" +
        "\t},\n" +
        "\t\"api_local\": {\n" +
        "\t\t\"connect\": \"http://api-075dffd2a583.sandbox.rtt.space:81\"\n" +
        "\t},\n" +
        "\t\"promo\": {\n" +
        "\t\t\"domain\": \"https://promo-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"preview_domain\": \"https://promo-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"live_banner_cache_time\": 5,\n" +
        "\t\t\"clickunder_cache_time\": 5,\n" +
        "\t\t\"clickunder_gallery_cache_time\": 5\n" +
        "\t},\n" +
        "\t\"user-counters\": {\n" +
        "\t\t\"connect\": \"http://user-counters-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"database\": {\n" +
        "\t\t\t\"connect\": \"mysql:dbname=webka_counters\",\n" +
        "\t\t\t\"username\": \"root\",\n" +
        "\t\t\t\"password\": \"\"\n" +
        "\t\t},\n" +
        "\t\t\"log\": {\n" +
        "\t\t\t\"directory\": \"/var/www/log/\"\n" +
        "\t\t}\n" +
        "\t},\n" +
        "\t\"resource_manager\": {\n" +
        "\t\t\"connect\": \"http://resource-manager-075dffd2a583.sandbox.rtt.space:9000\"\n" +
        "\t},\n" +
        "\t\"resource_manager_db\": {\n" +
        "\t\t\"dbname\": \"webka_resource_manager\",\n" +
        "\t\t\"host\": \"resource-manager-db-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"port\": \"5432\",\n" +
        "\t\t\"user\": \"webka_resource_manager\"\n" +
        "\t},\n" +
        "\t\"storage\": {\n" +
        "\t\t\"domain\": \"https://storage-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"api\": \"http://10.30.178.21:81\"\n" +
        "\t},\n" +
        "\t\"media_server\": {\n" +
        "\t\t\"cluster_id\": \"96171610\",\n" +
        "\t\t\"manager_node\": \"http://ms-manager-node-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"media_domain\": \"https://ms-rhls-node-075dffd2a583.sandbox.rtt.space/\",\n" +
        "\t\t\"ws_domain\": \"wss://ms-ws-node-075dffd2a583.sandbox.rtt.space/\"\n" +
        "\t},\n" +
        "\t\"ws_transport\": {\n" +
        "\t\t\"env\": \"integration\",\n" +
        "\t\t\"connect\": \"https://{{WSTRANSPORT}}\",\n" +
        "\t\t\"port\": 9091,\n" +
        "\t\t\"path\": \"/wss\",\n" +
        "\t\t\"ping-interval\": 15000,\n" +
        "\t\t\"ping-timeout\": 15000,\n" +
        "\t\t\"log\": {\n" +
        "\t\t\t\"directory\": \"/var/www/log/\"\n" +
        "\t\t}\n" +
        "\t},\n" +
        "\t\"ws_router\": {\n" +
        "\t\t\"env\": \"integration\",\n" +
        "\t\t\"connect\": \"https://ws-router-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"port\": 9091,\n" +
        "\t\t\"path\": \"/wss\",\n" +
        "\t\t\"ping-interval\": 15000,\n" +
        "\t\t\"ping-timeout\": 15000,\n" +
        "\t\t\"log\": {\n" +
        "\t\t\t\"directory\": \"/var/www/log/\"\n" +
        "\t\t}\n" +
        "\t},\n" +
        "\t\"support_chat\": {\n" +
        "\t\t\"connect\": \"https://support-chat-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"admin_url\": \"https://admin-075dffd2a583.sandbox.rtt.space\"\n" +
        "\t},\n" +
        "\t\"monitoring\": {\n" +
        "\t\t\"connect\": \"https://support-monitoring-075dffd2a583.sandbox.rtt.space\",\n" +
        "\t\t\"admin_url\": \"https://admin-075dffd2a583.sandbox.rtt.space\"\n" +
        "\t},\n" +
        "\t\"redis\": {\n" +
        "\t\t\"host\": \"10.30.178.18\",\n" +
        "\t\t\"port\": 6379,\n" +
        "\t\t\"password\": \"\"\n" +
        "\t},\n" +
        "\t\"amqp\": {\n" +
        "\t\t\"connect\": \"amqp://guest:guest@10.30.178.4\"\n" +
        "\t},\n" +
        "\t\"api_db_master\": {\n" +
        "\t\t\"dbname\": \"webka_api\",\n" +
        "\t\t\"host\": \"10.30.178.7\",\n" +
        "\t\t\"port\": \"5432\",\n" +
        "\t\t\"user\": \"webka_api\"\n" +
        "\t},\n" +
        "\t\"admin_db_master\": {\n" +
        "\t\t\"dbname\": \"webka_admin\",\n" +
        "\t\t\"host\": \"10.30.178.3\",\n" +
        "\t\t\"port\": \"5432\",\n" +
        "\t\t\"user\": \"webka_admin\"\n" +
        "\t},\n" +
        "\t\"partners_db_master\": {\n" +
        "\t\t\"dbname\": \"webka_partners\",\n" +
        "\t\t\"host\": \"10.30.178.16\",\n" +
        "\t\t\"port\": \"5432\",\n" +
        "\t\t\"user\": \"webka_partners\"\n" +
        "\t},\n" +
        "\t\"analytic_db\": {\n" +
        "\t\t\"dbname\": \"analytics_db\",\n" +
        "\t\t\"host\": \"10.30.178.5\",\n" +
        "\t\t\"port\": \"8123\",\n" +
        "\t\t\"native_port\": \"9000\",\n" +
        "\t\t\"user\": \"default\",\n" +
        "\t\t\"password\": \"\"\n" +
        "\t},\n" +
        "\t\"external_service\": {\n" +
        "\t\t\"log\": {\n" +
        "\t\t\t\"path\": \"/var/www/log/external/\"\n" +
        "\t\t},\n" +
        "\t\t\"postgres\": {\n" +
        "\t\t\t\"timeout\": \"100\"\n" +
        "\t\t},\n" +
        "\t\t\"rabbitmq\": {\n" +
        "\t\t\t\"timeout\": \"100\"\n" +
        "\t\t},\n" +
        "\t\t\"redis\": {\n" +
        "\t\t\t\"timeout\": \"100\"\n" +
        "\t\t},\n" +
        "\t\t\"clickhouse\": {\n" +
        "\t\t\t\"timeout\": \"1000\"\n" +
        "\t\t},\n" +
        "\t\t\"curl\": {\n" +
        "\t\t\t\"timeout\": \"200\"\n" +
        "\t\t},\n" +
        "\t\t\"mariadb\": {\n" +
        "\t\t\t\"timeout\": \"100\"\n" +
        "\t\t}\n" +
        "\t}\n" +
        "}\n",
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
        /*"{\n" +
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
        "}",*/
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
    public abstract PublishingService publishing();
    public abstract WebkaPlayerFactory players();
    public abstract WebRTC.Factory webrtc();
  }

}
