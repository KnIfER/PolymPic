package com.tencent.smtt.export.external.interfaces;

public interface ConsoleMessage {
   ConsoleMessage.MessageLevel messageLevel();

   String message();

   String sourceId();

   int lineNumber();

   public static enum MessageLevel {
      TIP,
      LOG,
      WARNING,
      ERROR,
      DEBUG;
   }
}
