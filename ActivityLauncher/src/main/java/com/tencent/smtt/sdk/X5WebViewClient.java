package com.tencent.smtt.sdk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;

import androidx.annotation.RequiresApi;

import com.knziha.polymer.Utils.CMN;
import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.IX5WebViewClient;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.export.external.proxy.X5ProxyWebViewClient;
import com.tencent.smtt.utils.DebugConfigUtil;
import com.tencent.smtt.utils.TbsLog;

import static com.knziha.polymer.Utils.CMN.dummyWV;
import static com.tencent.smtt.export.external.interfaces.WebResourceResponse.new_WebResourceResponse;

class X5WebViewClient extends X5ProxyWebViewClient {
   private android.webkit.WebViewClient webViewClient;
   private WebView webView;
   private static String c = null;

   public X5WebViewClient(IX5WebViewClient var1, WebView var2, android.webkit.WebViewClient var3) {
      super(var1);
      this.webView = var2;
      this.webViewClient = var3;
      //this.webViewClient.x5WebViewClient = this;
   }

   public void doUpdateVisitedHistory(IX5WebViewBase var1, String var2, boolean var3) {
      this.webView.a(var1);
      this.webViewClient.doUpdateVisitedHistory(getLockedView(), var2, var3);
	   unlock();
   }
	
	private android.webkit.WebView getLockedView() {
		CMN.lock.lock();
		return getTaggedView();
	}
	
	private void unlock() {
		try {
			CMN.lock.unlock();
		} catch (Exception ignored) { }
	}
	
	private android.webkit.WebView getTaggedView() {
		dummyWV.setTag(webView);
		return dummyWV;
	}
	
	public void onFormResubmission(IX5WebViewBase var1, Message var2, Message var3) {
      this.webView.a(var1);
      this.webViewClient.onFormResubmission(getLockedView(), var2, var3);
		unlock();
   }

   public void onLoadResource(IX5WebViewBase var1, String var2) {
      this.webView.a(var1);
      this.webViewClient.onLoadResource(getLockedView(), var2);
	   unlock();
   }

   public void onPageFinished(IX5WebViewBase var1, int var2, int var3, String var4) {
      if (c == null) {
         DebugConfigUtil var5 = DebugConfigUtil.a();
         if (var5 != null) {
            var5.setSystemWebviewForceUsedResult(false);
            c = Boolean.toString(false);
         }
      }

      this.webView.a(var1);
      ++this.webView.a;
      this.webViewClient.onPageFinished(getLockedView(), var4);
	   unlock();
      if ("com.qzone".equals(var1.getView().getContext().getApplicationInfo().packageName)) {
         this.webView.a(var1.getView().getContext());
      }

      TbsLog.app_extra("SmttWebViewClient", var1.getView().getContext());

      try {
         super.onPageFinished(var1, var2, var3, var4);
      } catch (Exception var6) {
      }

      WebView.d();
      if (!TbsShareManager.mHasQueryed && this.webView.getContext() != null && TbsShareManager.isThirdPartyApp(this.webView.getContext())) {
         TbsShareManager.mHasQueryed = true;
         (new Thread(new Runnable() {
            public void run() {
               if (!TbsShareManager.forceLoadX5FromTBSDemo(X5WebViewClient.this.webView.getContext()) && TbsDownloader.needDownload(X5WebViewClient.this.webView.getContext(), false)) {
                  TbsDownloader.startDownload(X5WebViewClient.this.webView.getContext());
               }

            }
         })).start();
      }

      if (this.webView.getContext() != null && !TbsLogReport.getInstance(this.webView.getContext()).getShouldUploadEventReport()) {
         TbsLogReport.getInstance(this.webView.getContext()).setShouldUploadEventReport(true);
         TbsLogReport.getInstance(this.webView.getContext()).dailyReport();
      }

   }

   public void onPageStarted(IX5WebViewBase var1, int var2, int var3, String var4, Bitmap var5) {
      this.webView.a(var1);
      this.webViewClient.onPageStarted(getLockedView(), var4, var5);
	   unlock();
   }

   @RequiresApi(api = Build.VERSION_CODES.M)
   public void onReceivedError(IX5WebViewBase var1, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
      this.webView.a(var1);
	   android.webkit.WebView wv = getLockedView();
	   this.webView.wrr = webResourceRequest;
	   this.webView.wre = webResourceError;
      this.webViewClient.onReceivedError(wv, null, null);
	   unlock();
   }

   public void onReceivedError(IX5WebViewBase var1, int var2, String var3, String var4) {
      if (var2 < -15) {
         if (var2 != -17) {
            return;
         }

         var2 = -1;
      }

      this.webView.a(var1);
      this.webViewClient.onReceivedError(getLockedView(), var2, var3, var4);
	   unlock();
   }

   public void onReceivedHttpError(IX5WebViewBase var1, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
      this.webView.a(var1);
	   android.webkit.WebView wv = getLockedView();
	   this.webView.wrr = webResourceRequest;
	   this.webView.wret = webResourceResponse;
      this.webViewClient.onReceivedHttpError(wv, null, null);
	   unlock();
   }

   public void onReceivedHttpAuthRequest(IX5WebViewBase var1, HttpAuthHandler httpAuthHandler, String var3, String var4) {
      this.webView.a(var1);
	   android.webkit.WebView wv = getLockedView();
	   this.webView.httpAuthHandler = httpAuthHandler;
      this.webViewClient.onReceivedHttpAuthRequest(wv, null, var3, var4);
	   unlock();
   }

   public void onReceivedSslError(IX5WebViewBase var1, SslErrorHandler sslErrorHandler, SslError sslError) {
      this.webView.a(var1);
	   android.webkit.WebView wv = getLockedView();
	   this.webView.sslErrorHandler = sslErrorHandler;
	   this.webView.sslError = sslError;
      this.webViewClient.onReceivedSslError(wv, null, null);
	   unlock();
   }

   public void onReceivedClientCertRequest(IX5WebViewBase var1, ClientCertRequest clientCertRequest) {
      this.webView.a(var1);
	   android.webkit.WebView wv = getLockedView();
	   this.webView.clientCertRequest = clientCertRequest;
      this.webViewClient.onReceivedClientCertRequest(wv, null);
	   unlock();
   }

   public void onScaleChanged(IX5WebViewBase var1, float var2, float var3) {
      this.webView.a(var1);
      this.webViewClient.onScaleChanged(getLockedView(), var2, var3);
	   unlock();
   }

   public void onUnhandledKeyEvent(IX5WebViewBase var1, KeyEvent var2) {
      this.webView.a(var1);
      this.webViewClient.onUnhandledKeyEvent(getLockedView(), var2);
	   unlock();
   }

   public boolean shouldOverrideKeyEvent(IX5WebViewBase var1, KeyEvent var2) {
      this.webView.a(var1);
	   boolean ret = this.webViewClient.shouldOverrideKeyEvent(getLockedView(), var2);
	   unlock();
	   return ret;
   }

   public void a(String var1) {
      Intent var2 = new Intent("android.intent.action.DIAL", Uri.parse(var1));
      var2.addFlags(268435456);

      try {
         if (this.webView.getContext() != null) {
            this.webView.getContext().startActivity(var2);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public boolean shouldOverrideUrlLoading(IX5WebViewBase var1, String var2) {
      if (var2 != null && !this.webView.showDebugView(var2)) {
         this.webView.a(var1);
         boolean var3 = this.webViewClient.shouldOverrideUrlLoading(getLockedView(), var2);
         unlock();
         if (!var3) {
            if (var2.startsWith("wtai://wp/mc;")) {
               Intent var4 = new Intent("android.intent.action.VIEW", Uri.parse("tel:" + var2.substring("wtai://wp/mc;".length())));
               this.webView.getContext().startActivity(var4);
               return true;
            }

            if (var2.startsWith("tel:")) {
               this.a(var2);
               return true;
            }
         }

         return var3;
      } else {
         return true;
      }
   }

   public void onTooManyRedirects(IX5WebViewBase var1, Message var2, Message var3) {
      this.webView.a(var1);
      this.webViewClient.onTooManyRedirects(getLockedView(), var2, var3);
	   unlock();
   }

   public boolean shouldOverrideUrlLoading(IX5WebViewBase var1, WebResourceRequest webResourceRequest) {
      String var3 = null;
      if (webResourceRequest != null && webResourceRequest.getUrl() != null) {
         var3 = webResourceRequest.getUrl().toString();
      }

      if (var3 != null && !this.webView.showDebugView(var3)) {
         this.webView.a(var1);
		  android.webkit.WebView wv = getLockedView();
		  this.webView.wrr = webResourceRequest;
         boolean var4 = this.webViewClient.shouldOverrideUrlLoading(wv, (String)null);
		  unlock();
         if (!var4) {
            if (var3.startsWith("wtai://wp/mc;")) {
               Intent var5 = new Intent("android.intent.action.VIEW", Uri.parse("tel:" + var3.substring("wtai://wp/mc;".length())));
               this.webView.getContext().startActivity(var5);
               return true;
            }

            if (var3.startsWith("tel:")) {
               this.a(var3);
               return true;
            }
         }

         return var4;
      } else {
         return true;
      }
   }

   public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, String var2) {
      this.webView.a(var1);
	   WebResourceResponse ret = new_WebResourceResponse(this.webViewClient.shouldInterceptRequest(getLockedView(), var2));
	   unlock();
	   return ret;
   }
	
	public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, WebResourceRequest webResourceRequest) {
      this.webView.a(var1);
	   android.webkit.WebView wv = getLockedView();
	   this.webView.wrr = webResourceRequest;
	   WebResourceResponse ret = new_WebResourceResponse(this.webViewClient.shouldInterceptRequest(wv, (String)null));
      unlock();
	   return ret;
   }

   public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, WebResourceRequest webResourceRequest, Bundle bundle) {
      this.webView.a(var1);
	   android.webkit.WebView wv = getLockedView();
	   this.webView.wrr = webResourceRequest;
	   this.webView.bundle = bundle;
	   WebResourceResponse ret = new_WebResourceResponse(this.webViewClient.shouldInterceptRequest(wv, (String)null));
	   unlock();
	   return ret;
   }

   public void onReceivedLoginRequest(IX5WebViewBase var1, String var2, String var3, String var4) {
      this.webView.a(var1);
      this.webViewClient.onReceivedLoginRequest(getLockedView(), var2, var3, var4);
	   unlock();
   }

   public void a(WebView var1, String var2, Bitmap var3) {
      super.onPageStarted(this.webView.c(), 0, 0, var2, var3);
   }

   public void onDetectedBlankScreen(IX5WebViewBase var1, String var2, int var3) {
      this.webView.a(var1);
      //hhh
      // this.webViewClient.onDetectedBlankScreen(var2, var3);
   }

   public void onPageFinished(IX5WebViewBase var1, String var2) {
      this.onPageFinished(var1, 0, 0, var2);
   }

   public void onPageStarted(IX5WebViewBase var1, String var2, Bitmap var3) {
      this.onPageStarted(var1, 0, 0, var2, var3);
   }

   public void countPVContentCacheCallBack(String var1) {
      ++this.webView.a;
   }

   public void onPageCommitVisible(IX5WebViewBase var1, String var2) {
      this.webView.a(var1);
      this.webViewClient.onPageCommitVisible(getLockedView(), var2);
	   unlock();
   }
}
