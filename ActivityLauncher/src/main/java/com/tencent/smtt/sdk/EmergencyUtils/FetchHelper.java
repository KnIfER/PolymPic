package com.tencent.smtt.sdk.EmergencyUtils;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import com.tencent.smtt.utils.TbsLog;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchHelper {
   private static String a = "EmergencyManager";
   private String requestedUrl;
   private String params;
   private String method;
   private Handler handler;
   private static final Object mLock = new Object();
   private static HandlerThread handlerThread;
   private static Handler handlerInstance;

   public FetchHelper(Context context, String requestedUrl, String params) {
      this(context, requestedUrl, params, "POST");
   }

   public FetchHelper(Context context, String requestedUrl, String params, String method) {
      this.requestedUrl = requestedUrl;
      this.params = params;
      this.method = method;
      this.handler = new Handler(context.getMainLooper());
   }

   private static Handler getHandler() {
      synchronized(mLock) {
         if (handlerInstance == null) {
            handlerThread = new HandlerThread("HttpThread");
            handlerThread.start();
            handlerInstance = new Handler(handlerThread.getLooper());
         }

         return handlerInstance;
      }
   }

   public void postFetch(final OnResponsedCallback callback) {
      getHandler().post(new Runnable() {
         public void run() {
            final String responseText = FetchHelper.this.fetchUrl(FetchHelper.this.requestedUrl);
            if (responseText != null) {
               FetchHelper.this.handler.post(new Runnable() {
                  public void run() {
                     if (callback != null) {
                        callback.onResponsed(responseText);
                     }

                  }
               });
            } else {
               TbsLog.e(FetchHelper.a, "Unexpected result for an empty http response: " + FetchHelper.this.requestedUrl);
            }

         }
      });
   }

   public String fetchUrl(String url) {
      TbsLog.e(a, "Request url: " + this.requestedUrl + ",params: " + this.params);

      try {
         URL url1 = new URL(url.trim());
         HttpURLConnection httpURLConnection = (HttpURLConnection)url1.openConnection();
         httpURLConnection.setRequestMethod(this.method);
         httpURLConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
         httpURLConnection.setRequestProperty("Content-Length", this.params.length() + "");
         httpURLConnection.setDoOutput(true);
         OutputStream var4 = httpURLConnection.getOutputStream();
         var4.write(this.params.getBytes());
         int responseCode = httpURLConnection.getResponseCode();
         if (200 == responseCode) {
            InputStream inputStream = httpURLConnection.getInputStream();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            boolean var9 = false;

            int var11;
            while(-1 != (var11 = inputStream.read(buff))) {
               bos.write(buff, 0, var11);
               bos.flush();
            }

            return bos.toString("utf-8");
         }

         TbsLog.e(a, "Bad http request, code: " + responseCode);
      } catch (IOException var10) {
         TbsLog.e(a, "Http exception: " + var10.getMessage());
      }

      return null;
   }

   public interface OnResponsedCallback {
      void onResponsed(String var1);
   }
}
