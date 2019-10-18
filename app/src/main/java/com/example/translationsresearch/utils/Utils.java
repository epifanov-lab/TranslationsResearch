package com.example.translationsresearch.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.HashMap;

/**
 * @author Konstantin Epifanov
 * @since 16.09.2019
 */
public class Utils {

  public static int getRandomColor() {
    return Color.rgb(
      (int) (Math.random() * 255),
      (int) (Math.random() * 255),
      (int) (Math.random() * 255)
    );
  }

  public static void createViewWithArgs(Context context, ViewGroup root, int layoutResId, Object... values) {
    Context argsContext = Utils.argsContext(context, values);
    LayoutInflater inflater = LayoutInflater.from(context).cloneInContext(argsContext);
    inflater.inflate(layoutResId, root, true);
  }

  public static void createViewWithArgsAt(Context context, ViewGroup root, int layoutResId, int index, Object... values) {
    Context argsContext = Utils.argsContext(context, values);
    LayoutInflater inflater = LayoutInflater.from(context).cloneInContext(argsContext);
    View view = inflater.inflate(layoutResId, root, false);
    root.addView(view, index);
  }

  public static Context argsContext(Context context, Object... values) {
    final HashMap<String, Object> args = new HashMap<>();

    for (int i = 0, size = values.length; i < size; i += 2) {
      args.put(String.valueOf(values[i]), values[i + 1]); }

    return new ContextWrapper(context) {
      @Override
      public Object getSystemService(String name) {
        return args.computeIfAbsent(name, context::getSystemService);
      }

      @NonNull
      @Override
      public String toString() {
        return "WRAPPED CONTEXT: " + args;
      }
    };
  }

  @SuppressWarnings("unchecked")
  @SuppressLint("WrongConstant")
  public static <T> T getContextArg(Context context, String key) {
    return (T) context.getSystemService(key);
  }

}
