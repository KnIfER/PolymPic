package com.tencent.smtt.export.external.interfaces;

public interface HttpAuthHandler {
   void proceed(String var1, String var2);

   void cancel();

   boolean useHttpAuthUsernamePassword();
}
