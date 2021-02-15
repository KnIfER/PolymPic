package com.tencent.smtt.export.external.interfaces;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

public interface IX5WebChromeClient {
   void onExceededDatabaseQuota(String var1, String var2, long var3, long var5, long var7, QuotaUpdater var9);

   Bitmap getDefaultVideoPoster();

   /** @deprecated */
   @Deprecated
   void onConsoleMessage(String var1, int var2, String var3);

   boolean onConsoleMessage(ConsoleMessage var1);

   boolean onCreateWindow(IX5WebViewBase var1, boolean var2, boolean var3, Message var4);

   void onGeolocationPermissionsHidePrompt();

   void onGeolocationPermissionsShowPrompt(String var1, GeolocationPermissionsCallback var2);

   void onHideCustomView();

   boolean onJsAlert(IX5WebViewBase var1, String var2, String var3, JsResult var4);

   boolean onJsConfirm(IX5WebViewBase var1, String var2, String var3, JsResult var4);

   boolean onJsPrompt(IX5WebViewBase var1, String var2, String var3, String var4, JsPromptResult var5);

   boolean onJsBeforeUnload(IX5WebViewBase var1, String var2, String var3, JsResult var4);

   boolean onJsTimeout();

   void onProgressChanged(IX5WebViewBase var1, int var2);

   void onReachedMaxAppCacheSize(long var1, long var3, QuotaUpdater var5);

   void onReceivedIcon(IX5WebViewBase var1, Bitmap var2);

   void onReceivedTouchIconUrl(IX5WebViewBase var1, String var2, boolean var3);

   void onReceivedTitle(IX5WebViewBase var1, String var2);

   void onRequestFocus(IX5WebViewBase var1);

   void onShowCustomView(View var1, IX5WebChromeClient.CustomViewCallback var2);

   void onShowCustomView(View var1, int var2, IX5WebChromeClient.CustomViewCallback var3);

   void onCloseWindow(IX5WebViewBase var1);

   void getVisitedHistory(ValueCallback<String[]> var1);

   void openFileChooser(ValueCallback<Uri[]> var1, String var2, String var3, boolean var4);

   boolean onShowFileChooser(IX5WebViewBase var1, ValueCallback<Uri[]> var2, IX5WebChromeClient.FileChooserParams var3);

   void onGeolocationStopUpdating();

   void onGeolocationStartUpdating(ValueCallback<Location> var1, ValueCallback<Bundle> var2);

   public abstract static class FileChooserParams {
      public static final int MODE_OPEN = 0;
      public static final int MODE_OPEN_MULTIPLE = 1;
      public static final int MODE_OPEN_FOLDER = 2;
      public static final int MODE_SAVE = 3;

      public static Uri[] parseResult(int var0, Intent var1) {
         return null;
      }

      public abstract int getMode();

      public abstract String[] getAcceptTypes();

      public abstract boolean isCaptureEnabled();

      public abstract CharSequence getTitle();

      public abstract String getFilenameHint();

      public abstract Intent createIntent();
   }

   public interface CustomViewCallback extends WebChromeClient.CustomViewCallback {
      void onCustomViewHidden();
   }
}
