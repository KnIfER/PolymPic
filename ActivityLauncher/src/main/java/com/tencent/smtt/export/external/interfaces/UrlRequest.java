package com.tencent.smtt.export.external.interfaces;

import java.nio.ByteBuffer;
import java.util.HashMap;

public abstract class UrlRequest {
   public abstract void start();

   public abstract void followRedirect();

   public abstract void read(ByteBuffer var1);

   public abstract void cancel();

   public abstract boolean isDone();

   public abstract static class Callback {
      public abstract void onRedirectReceived(UrlRequest var1, UrlResponseInfo var2, String var3) throws Exception;

      public abstract void onResponseStarted(UrlRequest var1, UrlResponseInfo var2) throws Exception;

      public abstract void onReadCompleted(UrlRequest var1, UrlResponseInfo var2, ByteBuffer var3) throws Exception;

      public abstract void onSucceeded(UrlRequest var1, UrlResponseInfo var2);

      public abstract void onFailed(UrlRequest var1, UrlResponseInfo var2, X5netException var3);

      public void onCanceled(UrlRequest var1, UrlResponseInfo var2) {
      }

      public void shouldInterceptResponseHeader(UrlRequest var1, HashMap<String, String> var2) {
      }
   }

   public abstract static class Builder {
      public static final int REQUEST_PRIORITY_IDLE = 0;
      public static final int REQUEST_PRIORITY_LOWEST = 1;
      public static final int REQUEST_PRIORITY_LOW = 2;
      public static final int REQUEST_PRIORITY_MEDIUM = 3;
      public static final int REQUEST_PRIORITY_HIGHEST = 4;

      public abstract UrlRequest.Builder setHttpMethod(String var1);

      public abstract UrlRequest.Builder addHeader(String var1, String var2);

      public abstract UrlRequest.Builder disableCache();

      public abstract UrlRequest.Builder setRequestBody(String var1);

      public abstract UrlRequest.Builder setRequestBodyBytes(byte[] var1);

      public abstract UrlRequest.Builder setDns(String var1, String var2);

      public abstract UrlRequest.Builder setPriority(int var1);

      public abstract UrlRequest build();
   }
}
