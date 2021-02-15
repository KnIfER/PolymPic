package com.tencent.smtt.export.external.embeddedwidget.interfaces;

import android.webkit.ValueCallback;

public interface IEmbeddedWidget {
   long getWidgetId();

   void setEventResponseType(IEmbeddedWidget.EventResponseType var1);

   void evaluateJavascript(String var1, ValueCallback<String> var2);

   void onClientError(IEmbeddedWidgetClient var1);

   public static enum EventResponseType {
      UNKNOWN,
      CONSUME_EVENT,
      NOT_CONSUME_EVENT;
   }
}
