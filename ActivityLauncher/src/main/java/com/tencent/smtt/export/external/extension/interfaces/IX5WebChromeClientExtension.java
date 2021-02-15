package com.tencent.smtt.export.external.extension.interfaces;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ValueCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.MediaAccessPermissionsCallback;
import java.util.HashMap;

public interface IX5WebChromeClientExtension {
   int AUDIO_BUFFERING_DISABLE = -3;
   int AUDIO_BUFFERING_START = -2;
   int AUDIO_BUFFERING_END = -1;

   Object getX5WebChromeClientInstance();

   View getVideoLoadingProgressView();

   void onAllMetaDataFinished(IX5WebViewExtension var1, HashMap<String, String> var2);

   void onBackforwardFinished(int var1);

   void onHitTestResultForPluginFinished(IX5WebViewExtension var1, IX5WebViewBase.HitTestResult var2, Bundle var3);

   void onHitTestResultFinished(IX5WebViewExtension var1, IX5WebViewBase.HitTestResult var2);

   void onPromptScaleSaved(IX5WebViewExtension var1);

   void onPromptNotScalable(IX5WebViewExtension var1);

   boolean onAddFavorite(IX5WebViewExtension var1, String var2, String var3, JsResult var4);

   void onPrepareX5ReadPageDataFinished(IX5WebViewExtension var1, HashMap<String, String> var2);

   boolean onSavePassword(String var1, String var2, String var3, boolean var4, Message var5);

   boolean onSavePassword(ValueCallback<String> var1, String var2, String var3, String var4, String var5, String var6, boolean var7);

   void onX5ReadModeAvailableChecked(HashMap<String, String> var1);

   void addFlashView(View var1, LayoutParams var2);

   void h5videoRequestFullScreen(String var1);

   void h5videoExitFullScreen(String var1);

   void requestFullScreenFlash();

   void exitFullScreenFlash();

   void jsRequestFullScreen();

   void jsExitFullScreen();

   void acquireWakeLock();

   void releaseWakeLock();

   Context getApplicationContex();

   boolean onPageNotResponding(Runnable var1);

   Object onMiscCallBack(String var1, Bundle var2);

   void openFileChooser(ValueCallback<Uri[]> var1, String var2, String var3);

   void onPrintPage();

   void onColorModeChanged(long var1);

   boolean onPermissionRequest(String var1, long var2, MediaAccessPermissionsCallback var4);
}
