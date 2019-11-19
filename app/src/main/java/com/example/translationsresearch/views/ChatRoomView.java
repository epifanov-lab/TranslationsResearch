package com.example.translationsresearch.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.translationsresearch.R;
import com.example.translationsresearch.service.chat.ChatService;
import com.example.translationsresearch.service.chat.Message;
import com.webka.sdk.schedulers.Schedulers;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import reactor.core.Disposable;
import reactor.core.Disposables;


/**
 * @author Konstantin Epifanov
 * @since 14.11.2019
 */
public class ChatRoomView extends RelativeLayout {

  private final ChatService mChatService;

  private TextView mTextReceive;

  private TextView mTextSend;

  private final StringBuilder chat;

  private Disposable.Composite mDisposable;

  public ChatRoomView(@NonNull Context context) {
    this(context, null);
  }

  public ChatRoomView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ChatRoomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public ChatRoomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes); mChatService = ChatService
      .obtain(context)
      .get(); chat = new StringBuilder();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate(); mTextReceive = findViewById(R.id.receive);
    mTextSend = findViewById(R.id.send);
  }

  @Override
  public void onVisibilityAggregated(boolean isVisible) {
    if (!isVisible && mDisposable != null) {
      mDisposable.dispose(); mDisposable = null;
    }

    super.onVisibilityAggregated(isVisible);

    if (isVisible && mDisposable == null) {
      mDisposable = Disposables.composite(

        mChatService
          .testGenSource1("3LePVpRx5SG", 1)
          .transform(Schedulers::work_main)
          .subscribe(this::handle, Throwable::printStackTrace)

      );
    }
  }

  private void handle(Message[] messages) {
    String text = Stream
      .of(messages)
      .map(Message::toString)
      .collect(Collectors.joining("\n"));

    mTextReceive.setText(text);
  }

}
