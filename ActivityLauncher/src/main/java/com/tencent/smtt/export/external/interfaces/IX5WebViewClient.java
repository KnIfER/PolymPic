package com.tencent.smtt.export.external.interfaces;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;

public interface IX5WebViewClient {
   int ERROR_UNKNOWN = -1;
   int ERROR_HOST_LOOKUP = -2;
   int ERROR_UNSUPPORTED_AUTH_SCHEME = -3;
   int ERROR_AUTHENTICATION = -4;
   int ERROR_PROXY_AUTHENTICATION = -5;
   int ERROR_CONNECT = -6;
   int ERROR_IO = -7;
   int ERROR_TIMEOUT = -8;
   int ERROR_REDIRECT_LOOP = -9;
   int ERROR_UNSUPPORTED_SCHEME = -10;
   int ERROR_FAILED_SSL_HANDSHAKE = -11;
   int ERROR_BAD_URL = -12;
   int ERROR_FILE = -13;
   int ERROR_FILE_NOT_FOUND = -14;
   int ERROR_TOO_MANY_REQUESTS = -15;
   int INTERCEPT_BY_ISP = -16;

   boolean shouldOverrideUrlLoading(IX5WebViewBase var1, String var2);

   boolean shouldOverrideUrlLoading(IX5WebViewBase var1, WebResourceRequest var2);

   void onPageStarted(IX5WebViewBase var1, String var2, Bitmap var3);

   void onPageFinished(IX5WebViewBase var1, String var2);

   void onPageStarted(IX5WebViewBase var1, int var2, int var3, String var4, Bitmap var5);

   void onPageFinished(IX5WebViewBase var1, int var2, int var3, String var4);

   void onReceivedError(IX5WebViewBase var1, int var2, String var3, String var4);

   void onReceivedError(IX5WebViewBase var1, WebResourceRequest var2, WebResourceError var3);

   void onReceivedHttpError(IX5WebViewBase var1, WebResourceRequest var2, WebResourceResponse var3);

   void onLoadResource(IX5WebViewBase var1, String var2);

   WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, WebResourceRequest var2, Bundle var3);

   WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, WebResourceRequest var2);

   WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, String var2);

   void doUpdateVisitedHistory(IX5WebViewBase var1, String var2, boolean var3);

   void onFormResubmission(IX5WebViewBase var1, Message var2, Message var3);

   void onReceivedHttpAuthRequest(IX5WebViewBase var1, HttpAuthHandler var2, String var3, String var4);

   void onReceivedSslError(IX5WebViewBase var1, SslErrorHandler var2, SslError var3);

   void onReceivedClientCertRequest(IX5WebViewBase var1, ClientCertRequest var2);

   void onScaleChanged(IX5WebViewBase var1, float var2, float var3);

   void onUnhandledKeyEvent(IX5WebViewBase var1, KeyEvent var2);

   boolean shouldOverrideKeyEvent(IX5WebViewBase var1, KeyEvent var2);

   /** @deprecated */
   @Deprecated
   void onTooManyRedirects(IX5WebViewBase var1, Message var2, Message var3);

   void onReceivedLoginRequest(IX5WebViewBase var1, String var2, String var3, String var4);

   void onContentSizeChanged(IX5WebViewBase var1, int var2, int var3);

   void onDetectedBlankScreen(IX5WebViewBase var1, String var2, int var3);

   void onPageCommitVisible(IX5WebViewBase var1, String var2);
}
