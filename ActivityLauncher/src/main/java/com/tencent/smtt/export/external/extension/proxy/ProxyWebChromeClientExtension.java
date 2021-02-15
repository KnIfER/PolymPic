package com.tencent.smtt.export.external.extension.proxy;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ValueCallback;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebChromeClientExtension;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.MediaAccessPermissionsCallback;
import java.util.HashMap;

public class ProxyWebChromeClientExtension implements IX5WebChromeClientExtension {
   private static boolean sCompatibleNewOnSavePassword = true;
   private static boolean sCompatibleOpenFileChooser = true;
   protected IX5WebChromeClientExtension mWebChromeClient;

   public IX5WebChromeClientExtension getmWebChromeClient() {
      return this.mWebChromeClient;
   }

   public void setWebChromeClientExtend(IX5WebChromeClientExtension var1) {
      this.mWebChromeClient = var1;
   }

   public View getVideoLoadingProgressView() {
      return this.mWebChromeClient != null ? this.mWebChromeClient.getVideoLoadingProgressView() : null;
   }

   public void onBackforwardFinished(int var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onBackforwardFinished(var1);
      }

   }

   public void onHitTestResultForPluginFinished(IX5WebViewExtension var1, IX5WebViewBase.HitTestResult var2, Bundle var3) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onHitTestResultForPluginFinished(var1, var2, var3);
      }

   }

   public void onHitTestResultFinished(IX5WebViewExtension var1, IX5WebViewBase.HitTestResult var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onHitTestResultFinished(var1, var2);
      }

   }

   public boolean onAddFavorite(IX5WebViewExtension var1, String var2, String var3, JsResult var4) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onAddFavorite(var1, var2, var3, var4) : false;
   }

   public void onPrepareX5ReadPageDataFinished(IX5WebViewExtension var1, HashMap<String, String> var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onPrepareX5ReadPageDataFinished(var1, var2);
      }

   }

   public void onPromptScaleSaved(IX5WebViewExtension var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onPromptScaleSaved(var1);
      }

   }

   public void onPromptNotScalable(IX5WebViewExtension var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onPromptNotScalable(var1);
      }

   }

   public boolean onSavePassword(String var1, String var2, String var3, boolean var4, Message var5) {
      if (this.mWebChromeClient != null) {
         try {
            return this.mWebChromeClient.onSavePassword(var1, var2, var3, var4, var5);
         } catch (NoSuchMethodError var7) {
            var7.printStackTrace();
         }
      }

      return false;
   }

   public boolean onSavePassword(ValueCallback<String> var1, String var2, String var3, String var4, String var5, String var6, boolean var7) {
      if (null != this.mWebChromeClient && sCompatibleNewOnSavePassword) {
         try {
            return this.mWebChromeClient.onSavePassword(var1, var2, var3, var4, var5, var6, var7);
         } catch (NoSuchMethodError var9) {
            if (var9.getMessage() == null || !var9.getMessage().contains("onSavePassword")) {
               throw var9;
            }
         }

         Log.d("incompatible-oldcore", "IX5WebChromeClientExtension.onSavePassword");
         sCompatibleNewOnSavePassword = false;
      }

      return false;
   }

   public void onX5ReadModeAvailableChecked(HashMap<String, String> var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onX5ReadModeAvailableChecked(var1);
      }

   }

   public Object getX5WebChromeClientInstance() {
      return this.mWebChromeClient;
   }

   public void addFlashView(View var1, LayoutParams var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.addFlashView(var1, var2);
      }

   }

   public void requestFullScreenFlash() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.requestFullScreenFlash();
      }

   }

   public void exitFullScreenFlash() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.exitFullScreenFlash();
      }

   }

   public void jsRequestFullScreen() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.jsRequestFullScreen();
      }

   }

   public void jsExitFullScreen() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.jsExitFullScreen();
      }

   }

   public void h5videoRequestFullScreen(String var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.h5videoRequestFullScreen(var1);
      }

   }

   public void h5videoExitFullScreen(String var1) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.h5videoExitFullScreen(var1);
      }

   }

   public void acquireWakeLock() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.acquireWakeLock();
      }

   }

   public void releaseWakeLock() {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.releaseWakeLock();
      }

   }

   public Context getApplicationContex() {
      return this.mWebChromeClient != null ? this.mWebChromeClient.getApplicationContex() : null;
   }

   public void onAllMetaDataFinished(IX5WebViewExtension var1, HashMap<String, String> var2) {
      if (this.mWebChromeClient != null) {
         this.mWebChromeClient.onAllMetaDataFinished(var1, var2);
      }

   }

   public boolean onPageNotResponding(Runnable var1) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onPageNotResponding(var1) : false;
   }

   public Object onMiscCallBack(String var1, Bundle var2) {
      return this.mWebChromeClient != null ? this.mWebChromeClient.onMiscCallBack(var1, var2) : null;
   }

   public void openFileChooser(ValueCallback<Uri[]> var1, String var2, String var3) {
      if (this.mWebChromeClient != null && sCompatibleOpenFileChooser) {
         try {
            this.mWebChromeClient.openFileChooser(var1, var2, var3);
         } catch (NoSuchMethodError var5) {
            if (var5.getMessage() == null || !var5.getMessage().contains("openFileChooser")) {
               throw var5;
            }

            Log.d("incompatible-oldcore", "IX5WebChromeClientExtension.openFileChooser");
            sCompatibleOpenFileChooser = false;
         }
      }

   }

   public void onPrintPage() {
   }

   public void onColorModeChanged(long var1) {
   }

   public boolean onPermissionRequest(String var1, long var2, MediaAccessPermissionsCallback var4) {
      return false;
   }
}
