package com.tencent.smtt.export.external.proxy;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.IX5WebViewClient;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;

public abstract class ProxyWebViewClient implements IX5WebViewClient {
   protected IX5WebViewClient mWebViewClient;
   private boolean mCompatibleOnPageStartedOrFinishedMethod = false;

   public void setWebViewClient(IX5WebViewClient var1) {
      this.mWebViewClient = var1;
   }

   public void doUpdateVisitedHistory(IX5WebViewBase var1, String var2, boolean var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.doUpdateVisitedHistory(var1, var2, var3);
      }

   }

   public void onContentSizeChanged(IX5WebViewBase var1, int var2, int var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onContentSizeChanged(var1, var2, var3);
      }

   }

   public void onFormResubmission(IX5WebViewBase var1, Message var2, Message var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onFormResubmission(var1, var2, var3);
      }

   }

   public void onLoadResource(IX5WebViewBase var1, String var2) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onLoadResource(var1, var2);
      }

   }

   public void onPageFinished(IX5WebViewBase var1, String var2) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onPageFinished(var1, var2);
      }

   }

   public void onPageStarted(IX5WebViewBase var1, String var2, Bitmap var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onPageStarted(var1, var2, var3);
      }

   }

   public void onReceivedError(IX5WebViewBase var1, int var2, String var3, String var4) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onReceivedError(var1, var2, var3, var4);
      }

   }

   public void onReceivedError(IX5WebViewBase var1, WebResourceRequest var2, WebResourceError var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onReceivedError(var1, var2, var3);
      }

   }

   public void onReceivedHttpError(IX5WebViewBase var1, WebResourceRequest var2, WebResourceResponse var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onReceivedHttpError(var1, var2, var3);
      }

   }

   public void onReceivedHttpAuthRequest(IX5WebViewBase var1, HttpAuthHandler var2, String var3, String var4) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onReceivedHttpAuthRequest(var1, var2, var3, var4);
      }

   }

   public void onReceivedSslError(IX5WebViewBase var1, SslErrorHandler var2, SslError var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onReceivedSslError(var1, var2, var3);
      }

   }

   public void onReceivedClientCertRequest(IX5WebViewBase var1, ClientCertRequest var2) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onReceivedClientCertRequest(var1, var2);
      }

   }

   public void onScaleChanged(IX5WebViewBase var1, float var2, float var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onScaleChanged(var1, var2, var3);
      }

   }

   public void onUnhandledKeyEvent(IX5WebViewBase var1, KeyEvent var2) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onUnhandledKeyEvent(var1, var2);
      }

   }

   public boolean shouldOverrideKeyEvent(IX5WebViewBase var1, KeyEvent var2) {
      return this.mWebViewClient != null && this.mWebViewClient.shouldOverrideKeyEvent(var1, var2);
   }

   public boolean shouldOverrideUrlLoading(IX5WebViewBase var1, String var2) {
      return this.mWebViewClient != null && this.mWebViewClient.shouldOverrideUrlLoading(var1, var2);
   }

   public boolean shouldOverrideUrlLoading(IX5WebViewBase var1, WebResourceRequest var2) {
      return this.mWebViewClient != null && this.mWebViewClient.shouldOverrideUrlLoading(var1, var2);
   }

   public void onTooManyRedirects(IX5WebViewBase var1, Message var2, Message var3) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onTooManyRedirects(var1, var2, var3);
      }

   }

   public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, WebResourceRequest var2) {
      return this.mWebViewClient != null ? this.mWebViewClient.shouldInterceptRequest(var1, var2) : null;
   }

   public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, WebResourceRequest var2, Bundle var3) {
      return this.mWebViewClient != null ? this.mWebViewClient.shouldInterceptRequest(var1, var2, var3) : null;
   }

   public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, String var2) {
      return this.mWebViewClient != null ? this.mWebViewClient.shouldInterceptRequest(var1, var2) : null;
   }

   public void onReceivedLoginRequest(IX5WebViewBase var1, String var2, String var3, String var4) {
      if (this.mWebViewClient != null) {
         this.mWebViewClient.onReceivedLoginRequest(var1, var2, var3, var4);
      }

   }

   public void onPageStarted(IX5WebViewBase var1, int var2, int var3, String var4, Bitmap var5) {
   }

   public void onPageFinished(IX5WebViewBase var1, int var2, int var3, String var4) {
   }

   public void onDetectedBlankScreen(IX5WebViewBase var1, String var2, int var3) {
   }

   public void countPVContentCacheCallBack(String var1) {
   }

   public void onPageCommitVisible(IX5WebViewBase var1, String var2) {
   }
}
