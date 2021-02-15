package com.tencent.smtt.sdk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.os.Build.VERSION;
import android.view.View;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.PermissionRequest;
import com.tencent.smtt.utils.TbsLog;

public class WebChromeClient extends android.webkit.WebChromeClient{
   /** @deprecated */
   @Deprecated
   public void onExceededDatabaseQuota(String var1, String var2, long var3, long var5, long var7, WebStorage.QuotaUpdater var9) {
      var9.updateQuota(var5);
   }

   public Bitmap getDefaultVideoPoster() {
      return null;
   }

   public void getVisitedHistory(ValueCallback<String[]> var1) {
   }

   public boolean onConsoleMessage(ConsoleMessage var1) {
      return false;
   }

   public boolean onCreateWindow(WebView var1, boolean var2, boolean var3, Message var4) {
      return false;
   }

   public void onGeolocationPermissionsHidePrompt() {
   }

   public void onGeolocationPermissionsShowPrompt(String var1, GeolocationPermissionsCallback var2) {
      var2.invoke(var1, true, true);
   }

   public void onHideCustomView() {
   }

   public boolean onJsAlert(WebView var1, String var2, String var3, JsResult var4) {
      return false;
   }

   public boolean onJsConfirm(WebView var1, String var2, String var3, JsResult var4) {
      return false;
   }

   public boolean onJsPrompt(WebView var1, String var2, String var3, String var4, JsPromptResult var5) {
      return false;
   }

   public boolean onJsBeforeUnload(WebView var1, String var2, String var3, JsResult var4) {
      return false;
   }

   /** @deprecated */
   @Deprecated
   public boolean onJsTimeout() {
      return true;
   }

   public void onProgressChanged(WebView var1, int var2) {
   }

   /** @deprecated */
   @Deprecated
   public void onReachedMaxAppCacheSize(long var1, long var3, WebStorage.QuotaUpdater var5) {
      var5.updateQuota(var3);
   }

   public void onReceivedIcon(WebView var1, Bitmap var2) {
   }

   public void onReceivedTouchIconUrl(WebView var1, String var2, boolean var3) {
   }

   public void onReceivedTitle(WebView var1, String var2) {
   }

   public void onRequestFocus(WebView var1) {
   }

   public void onShowCustomView(View var1, IX5WebChromeClient.CustomViewCallback var2) {
   }

   /** @deprecated */
   @Deprecated
   public void onShowCustomView(View var1, int var2, IX5WebChromeClient.CustomViewCallback var3) {
   }

   public void onCloseWindow(WebView var1) {
   }

   public View getVideoLoadingProgressView() {
      return null;
   }

   public void openFileChooser(ValueCallback<Uri> var1, String var2, String var3) {
      var1.onReceiveValue(null);
   }

   public boolean onShowFileChooser(WebView var1, ValueCallback<Uri[]> var2, WebChromeClient.FileChooserParams var3) {
      return false;
   }

   public void onPermissionRequest(PermissionRequest var1) {
   }

   public void onPermissionRequestCanceled(PermissionRequest var1) {
   }

   public abstract static class FileChooserParams {
      public static final int MODE_OPEN = 0;
      public static final int MODE_OPEN_MULTIPLE = 1;
      public static final int MODE_OPEN_FOLDER = 2;
      public static final int MODE_SAVE = 3;

      public static Uri[] parseResult(int var0, Intent var1) {
         try {
            X5CoreEngine var2 = X5CoreEngine.getInstance();
            if (null != var2 && var2.isInCharge()) {
               return var2.getWVWizardBase().parseFileChooserResult(var0, var1);
            } else {
               Uri[] var3 = null;
               if (VERSION.SDK_INT >= 21) {
                  var3 = android.webkit.WebChromeClient.FileChooserParams.parseResult(var0, var1);
               }

               return var3;
            }
         } catch (Exception var4) {
            TbsLog.i("WebChromeClient", "parseResult:" + var4.toString());
            return null;
         }
      }

      public abstract int getMode();

      public abstract String[] getAcceptTypes();

      public abstract boolean isCaptureEnabled();

      public abstract CharSequence getTitle();

      public abstract String getFilenameHint();

      public abstract Intent createIntent();
   }
}
