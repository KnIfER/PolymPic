package com.tencent.smtt.sdk;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;

public class WebViewClient extends android.webkit.WebViewClient{
   public static final int ERROR_UNKNOWN = -1;
   public static final int ERROR_HOST_LOOKUP = -2;
   public static final int ERROR_UNSUPPORTED_AUTH_SCHEME = -3;
   public static final int ERROR_AUTHENTICATION = -4;
   public static final int ERROR_PROXY_AUTHENTICATION = -5;
   public static final int ERROR_CONNECT = -6;
   public static final int ERROR_IO = -7;
   public static final int ERROR_TIMEOUT = -8;
   public static final int ERROR_REDIRECT_LOOP = -9;
   public static final int ERROR_UNSUPPORTED_SCHEME = -10;
   public static final int ERROR_FAILED_SSL_HANDSHAKE = -11;
   public static final int ERROR_BAD_URL = -12;
   public static final int ERROR_FILE = -13;
   public static final int ERROR_FILE_NOT_FOUND = -14;
   public static final int ERROR_TOO_MANY_REQUESTS = -15;
   public static final int INTERCEPT_BY_ISP = -16;
   X5WebViewClient x5WebViewClient;

   public void onLoadResource(WebView var1, String var2) {
   }

   public boolean shouldOverrideUrlLoading(WebView var1, String var2) {
      return false;
   }

   public boolean shouldOverrideUrlLoading(WebView var1, WebResourceRequest var2) {
      return this.x5WebViewClient != null ? this.x5WebViewClient.shouldOverrideUrlLoading(var1.c(), var2.getUrl().toString()) : this.shouldOverrideUrlLoading(var1, var2.getUrl().toString());
   }

   public void onPageStarted(WebView var1, String var2, Bitmap var3) {
      if (this.x5WebViewClient != null) {
         this.x5WebViewClient.a(var1, var2, var3);
      }

   }

   public void onPageFinished(WebView var1, String var2) {
   }

   public void onReceivedError(WebView var1, int var2, String var3, String var4) {
   
   }

   public void onReceivedError(WebView var1, WebResourceRequest var2, WebResourceError var3) {
      if (this.x5WebViewClient != null) {
         if (var2.isForMainFrame()) {
            this.x5WebViewClient.onReceivedError(var1.c(), var3.getErrorCode(), var3.getDescription().toString(), var2.getUrl().toString());
         }
      } else if (var2.isForMainFrame()) {
         this.onReceivedError(var1, var3.getErrorCode(), var3.getDescription().toString(), var2.getUrl().toString());
      }

   }

   public void onReceivedHttpError(WebView var1, WebResourceRequest var2, WebResourceResponse var3) {
   }

   public WebResourceResponse shouldInterceptRequest(WebView var1, String var2) {
      return null;
   }

   public WebResourceResponse shouldInterceptRequest(WebView var1, WebResourceRequest var2) {
      return this.x5WebViewClient != null ? this.x5WebViewClient.shouldInterceptRequest(var1.c(), var2.getUrl().toString()) : this.shouldInterceptRequest(var1, var2.getUrl().toString());
   }

   public WebResourceResponse shouldInterceptRequest(WebView var1, WebResourceRequest var2, Bundle var3) {
      return this.x5WebViewClient != null ? this.x5WebViewClient.shouldInterceptRequest(var1.c(), var2) : null;
   }

   public void doUpdateVisitedHistory(WebView var1, String var2, boolean var3) {
   	
   }

   public void onFormResubmission(WebView var1, Message var2, Message var3) {
      var2.sendToTarget();
   }

   public void onReceivedHttpAuthRequest(WebView var1, HttpAuthHandler var2, String var3, String var4) {
      var2.cancel();
   }

   public void onReceivedSslError(WebView var1, SslErrorHandler var2, SslError var3) {
      var2.cancel();
   }

   public void onReceivedClientCertRequest(WebView var1, ClientCertRequest var2) {
      var2.cancel();
   }

   public void onScaleChanged(WebView var1, float var2, float var3) {
   }

   public void onUnhandledKeyEvent(WebView var1, KeyEvent var2) {
   }

   public boolean shouldOverrideKeyEvent(WebView var1, KeyEvent var2) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public void onTooManyRedirects(WebView var1, Message var2, Message var3) {
   }

   public void onReceivedLoginRequest(WebView var1, String var2, String var3, String var4) {
   }

   public void onDetectedBlankScreen(String var1, int var2) {
   }

   public void onPageCommitVisible(WebView var1, String var2) {
   }

   public boolean onRenderProcessGone(WebView var1, WebViewClient.a var2) {
      return false;
   }

   public interface a {
   }
}
