package com.example.translationsresearch.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.translationsresearch.R;
import com.example.translationsresearch.service.chat.ChatService;
import com.example.translationsresearch.service.chat.Message;
import com.webka.sdk.schedulers.Schedulers;

import java.util.function.Consumer;

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
  public ChatRoomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    mChatService = ChatService.obtain(context).get();
    chat = new StringBuilder();
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mTextReceive = findViewById(R.id.receive);
    mTextSend = findViewById(R.id.send);
  }

  @Override
  public void onVisibilityAggregated(boolean isVisible) {
    if (!isVisible && mDisposable != null) {
      mDisposable.dispose();
      mDisposable = null;
    }

    super.onVisibilityAggregated(isVisible);

    if (isVisible && mDisposable == null) {
      mDisposable = Disposables.composite(

        mChatService.messages("3LePVpRx5SG")
          .transform(Schedulers::work_main)
          .subscribe(this::handle, Throwable::printStackTrace),

        mChatService.source()
          .transform(Schedulers::work_main)
          .log()
          .subscribe(this::handle, Throwable::printStackTrace)
      );
    }
  }

  private void handle(Message[] messages) {
    chat.setLength(0);
    for (Message m : messages) handle(m);
    mTextReceive.setText(chat.toString());
  }

  private void handle(Message message) {
    chat.append(message.fullName).append(": ").append(message.text).append("\n");
    mTextReceive.setText(chat.toString());
  }

}
