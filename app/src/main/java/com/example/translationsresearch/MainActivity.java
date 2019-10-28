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
      /*"{\n" +
        "        \"www\": {\n" +
        "      \"domain\": \"https://www-b45d2a81e8e0.sandbox.rtt.space/\"\n" +
        "    },\n" +
        "    \"api\": {\n" +
        "      \"env\": \"integration\",\n" +
        "        \"secret\": \"69a39612c583bf149a3371d980c1a44f\",\n" +
        "        \"connect\": \"https://api-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"apipath\": \"/api/v1/\",\n" +
        "        \"admpath\": \"/admin/v1/\",\n" +
        "        \"partnerpath\": \"/partner/v1/\",\n" +
        "        \"cookie_domain\": \".sandbox.rtt.space\",\n" +
        "        \"log\": {\n" +
        "        \"stdout\": 1,\n" +
        "          \"directory\": \"/var/www/log/\"\n" +
        "      }\n" +
        "    },\n" +
        "    \"api_local\": {\n" +
        "      \"connect\": \"http://api-b45d2a81e8e0.sandbox.rtt.space:81\"\n" +
        "    },\n" +
        "    \"promo\": {\n" +
        "      \"domain\": \"https://promo-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"preview_domain\": \"https://promo-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"live_banner_cache_time\": 5,\n" +
        "        \"clickunder_cache_time\": 5\n" +
        "    },\n" +
        "    \"user-counters\": {\n" +
        "      \"connect\": \"http://user-counters-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"database\": {\n" +
        "        \"connect\": \"mysql:dbname=webka_counters\",\n" +
        "          \"username\": \"root\",\n" +
        "          \"password\": \"\"\n" +
        "      },\n" +
        "      \"log\": {\n" +
        "        \"directory\": \"/var/www/log/\"\n" +
        "      }\n" +
        "    },\n" +
        "    \"resource_manager\": {\n" +
        "      \"connect\": \"http://resource-manager-b45d2a81e8e0.sandbox.rtt.space:9000\"\n" +
        "    },\n" +
        "    \"resource_manager_db\": {\n" +
        "      \"dbname\": \"webka_resource_manager\",\n" +
        "        \"host\": \"resource-manager-db-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"port\": \"5432\",\n" +
        "        \"user\": \"webka_resource_manager\"\n" +
        "    },\n" +
        "    \"storage\": {\n" +
        "      \"domain\": \"https://storage-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"api\": \"http://10.33.81.21:81\"\n" +
        "    },\n" +
        "    \"media_server\": {\n" +
        "      \"cluster_id\": \"86008303\",\n" +
        "        \"manager_node\": \"http://ms-manager-node-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"media_domain\": \"https://ms-rhls-node-b45d2a81e8e0.sandbox.rtt.space/\"\n" +
        "    },\n" +
        "    \"ws_transport\": {\n" +
        "      \"env\": \"integration\",\n" +
        "        \"connect\": \"https://{{WSTRANSPORT}}\",\n" +
        "        \"port\": 9091,\n" +
        "        \"path\": \"/wss\",\n" +
        "        \"ping-interval\": 15000,\n" +
        "        \"ping-timeout\": 15000,\n" +
        "        \"log\": {\n" +
        "        \"directory\": \"/var/www/log/\"\n" +
        "      }\n" +
        "    },\n" +
        "    \"ws_router\": {\n" +
        "      \"env\": \"integration\",\n" +
        "        \"connect\": \"https://ws-router-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"port\": 9091,\n" +
        "        \"path\": \"/wss\",\n" +
        "        \"ping-interval\": 15000,\n" +
        "        \"ping-timeout\": 15000,\n" +
        "        \"log\": {\n" +
        "        \"directory\": \"/var/www/log/\"\n" +
        "      }\n" +
        "    },\n" +
        "    \"support_chat\": {\n" +
        "      \"connect\": \"https://support-chat-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"admin_url\": \"https://admin-b45d2a81e8e0.sandbox.rtt.space\"\n" +
        "    },\n" +
        "    \"monitoring\": {\n" +
        "      \"connect\": \"https://support-monitoring-b45d2a81e8e0.sandbox.rtt.space\",\n" +
        "        \"admin_url\": \"https://admin-b45d2a81e8e0.sandbox.rtt.space\"\n" +
        "    },\n" +
        "    \"redis\": {\n" +
        "      \"host\": \"10.33.81.18\",\n" +
        "        \"port\": 6379,\n" +
        "        \"password\": \"\"\n" +
        "    },\n" +
        "    \"amqp\": {\n" +
        "      \"connect\": \"amqp://guest:guest@10.33.81.4\"\n" +
        "    },\n" +
        "    \"api_db_master\": {\n" +
        "      \"dbname\": \"webka_api\",\n" +
        "        \"host\": \"10.33.81.7\",\n" +
        "        \"port\": \"5432\",\n" +
        "        \"user\": \"webka_api\"\n" +
        "    },\n" +
        "    \"admin_db_master\": {\n" +
        "      \"dbname\": \"webka_admin\",\n" +
        "        \"host\": \"10.33.81.3\",\n" +
        "        \"port\": \"5432\",\n" +
        "        \"user\": \"webka_admin\"\n" +
        "    },\n" +
        "    \"partners_db_master\": {\n" +
        "      \"dbname\": \"webka_partners\",\n" +
        "        \"host\": \"10.33.81.16\",\n" +
        "        \"port\": \"5432\",\n" +
        "        \"user\": \"webka_partners\"\n" +
        "    },\n" +
        "    \"analytic_db\": {\n" +
        "      \"dbname\": \"analytics_db\",\n" +
        "        \"host\": \"10.33.81.5\",\n" +
        "        \"port\": \"8123\",\n" +
        "        \"native_port\": \"9000\",\n" +
        "        \"user\": \"default\",\n" +
        "        \"password\": \"\"\n" +
        "    },\n" +
        "    \"external_service\": {\n" +
        "      \"log\": {\n" +
        "        \"path\": \"/var/www/log/external/\"\n" +
        "      },\n" +
        "      \"postgres\": {\n" +
        "        \"timeout\": \"100\"\n" +
        "      },\n" +
        "      \"rabbitmq\": {\n" +
        "        \"timeout\": \"100\"\n" +
        "      },\n" +
        "      \"redis\": {\n" +
        "        \"timeout\": \"100\"\n" +
        "      },\n" +
        "      \"clickhouse\": {\n" +
        "        \"timeout\": \"1000\"\n" +
        "      },\n" +
        "      \"curl\": {\n" +
        "        \"timeout\": \"200\"\n" +
        "      },\n" +
        "      \"mariadb\": {\n" +
        "        \"timeout\": \"100\"\n" +
        "      }\n" +
        "    }\n" +
        "}",*/
      "{\n" +
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
        "}",
        /*
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
