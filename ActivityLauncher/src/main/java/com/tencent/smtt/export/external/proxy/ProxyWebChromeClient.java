package com.tencent.smtt.export.external.proxy;

import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.ValueCallback;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.QuotaUpdater;

public class ProxyWebChromeClient implements IX5WebChromeClient {
   protected IX5WebChromeClient mWebChromeClient;

   public IX5WebChromeClient getmWebChromeClient() {
      return this.mWebChromeClient;
   }

   public void setWebChromeClient(IX5WebChromeClient var1) {
      this.mWebChromeClient = var1;
   }

   public boolean onConsoleMessage(ConsoleMessage var1) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onConsoleMessage(var1) : false;
   }

   public boolean onCreateWindow(IX5WebViewBase var1, boolean var2, boolean var3, Message var4) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onCreateWindow(var1, var2, var3, var4) : false;
   }

   public void onGeolocationPermissionsShowPrompt(String var1, GeolocationPermissionsCallback var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onGeolocationPermissionsShowPrompt(var1, var2);
      }

   }

   public void onHideCustomView() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onHideCustomView();
      }

   }

   public boolean onJsAlert(IX5WebViewBase var1, String var2, String var3, JsResult var4) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onJsAlert(var1, var2, var3, var4) : false;
   }

   public boolean onJsPrompt(IX5WebViewBase var1, String var2, String var3, String var4, JsPromptResult var5) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onJsPrompt(var1, var2, var3, var4, var5) : false;
   }

   public boolean onJsBeforeUnload(IX5WebViewBase var1, String var2, String var3, JsResult var4) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onJsBeforeUnload(var1, var2, var3, var4) : false;
   }

   public boolean onJsTimeout() {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onJsTimeout() : false;
   }

   public void onProgressChanged(IX5WebViewBase var1, int var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onProgressChanged(var1, var2);
      }

   }

   public void onReachedMaxAppCacheSize(long var1, long var3, QuotaUpdater var5) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onReachedMaxAppCacheSize(var1, var3, var5);
      }

   }

   public void onReceivedIcon(IX5WebViewBase var1, Bitmap var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onReceivedIcon(var1, var2);
      }

   }

   public void onReceivedTouchIconUrl(IX5WebViewBase var1, String var2, boolean var3) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onReceivedTouchIconUrl(var1, var2, var3);
      }

   }

   public void onReceivedTitle(IX5WebViewBase var1, String var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onReceivedTitle(var1, var2);
      }

   }

   public void onRequestFocus(IX5WebViewBase var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onRequestFocus(var1);
      }

   }

   public void onShowCustomView(View var1, IX5WebChromeClient.CustomViewCallback var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onShowCustomView(var1, var2);
      }

   }

   public void onShowCustomView(View var1, int var2, IX5WebChromeClient.CustomViewCallback var3) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onShowCustomView(var1, var3);
      }

   }

   public void onExceededDatabaseQuota(String var1, String var2, long var3, long var5, long var7, QuotaUpdater var9) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onExceededDatabaseQuota(var1, var2, var3, var5, var7, var9);
      }

   }

   public Bitmap getDefaultVideoPoster() {
      return null;
   }

   public void onConsoleMessage(String var1, int var2, String var3) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onConsoleMessage(var1, var2, var3);
      }

   }

   public void onGeolocationPermissionsHidePrompt() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onGeolocationPermissionsHidePrompt();
      }

   }

   public boolean onJsConfirm(IX5WebViewBase var1, String var2, String var3, JsResult var4) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onJsConfirm(var1, var2, var3, var4) : false;
   }

   public void onCloseWindow(IX5WebViewBase var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onCloseWindow(var1);
      }

   }

   public void getVisitedHistory(ValueCallback<String[]> var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.getVisitedHistory(var1);
      }

   }

   public void openFileChooser(ValueCallback<Uri[]> var1, String var2, String var3, boolean var4) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.openFileChooser(var1, var2, var3, var4);
      }

   }

   public void onGeolocationStartUpdating(ValueCallback<Location> var1, ValueCallback<Bundle> var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onGeolocationStartUpdating(var1, var2);
      }

   }

   public void onGeolocationStopUpdating() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onGeolocationStopUpdating();
      }

   }

   public boolean onShowFileChooser(IX5WebViewBase var1, ValueCallback<Uri[]> var2, IX5WebChromeClient.FileChooserParams var3) {
      return false;
   }
}
