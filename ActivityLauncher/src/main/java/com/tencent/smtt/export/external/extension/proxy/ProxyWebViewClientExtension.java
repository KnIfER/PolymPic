package com.tencent.smtt.export.external.extension.proxy;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewClientExtension;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import java.util.HashMap;
import java.util.List;

public abstract class ProxyWebViewClientExtension implements IX5WebViewClientExtension {
   protected IX5WebViewClientExtension mWebViewClientExt;
   private static boolean sCompatibleOnPageLoadingStartedAndFinished = true;
   private static boolean sCompatibleOnMetricsSavedCountReceived = true;

   public void onPreReadFinished() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onPreReadFinished();
      }

   }

   public void onPromptScaleSaved() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onPromptScaleSaved();
      }

   }

   public void onUrlChange(String var1, String var2) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onUrlChange(var1, var2);
      }

   }

   public void onHideListBox() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onHideListBox();
      }

   }

   public void onShowListBox(String[] var1, int[] var2, int[] var3, int var4) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onShowListBox(var1, var2, var3, var4);
      }

   }

   public void onShowMutilListBox(String[] var1, int[] var2, int[] var3, int[] var4) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onShowMutilListBox(var1, var2, var3, var4);
      }

   }

   public void onInputBoxTextChanged(IX5WebViewExtension var1, String var2) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onInputBoxTextChanged(var1, var2);
      }

   }

   public void onTransitionToCommitted() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onTransitionToCommitted();
      }

   }

   public void showTranslateBubble(int var1, String var2, String var3, int var4) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.showTranslateBubble(var1, var2, var3, var4);
      }

   }

   public void onUploadProgressStart(int var1) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onUploadProgressStart(var1);
      }

   }

   public void onUploadProgressChange(int var1, int var2, String var3) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onUploadProgressChange(var1, var2, var3);
      }

   }

   public void onFlingScrollBegin(int var1, int var2, int var3) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onFlingScrollBegin(var1, var2, var3);
      }

   }

   public void onFlingScrollEnd() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onFlingScrollEnd();
      }

   }

   public void onScrollChanged(int var1, int var2, int var3, int var4) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onScrollChanged(var1, var2, var3, var4);
      }

   }

   public void onSoftKeyBoardShow() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onSoftKeyBoardShow();
      }

   }

   public void onSoftKeyBoardHide(int var1) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onSoftKeyBoardHide(var1);
      }

   }

   public void onSetButtonStatus(boolean var1, boolean var2) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onSetButtonStatus(var1, var2);
      }

   }

   public void onHistoryItemChanged() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onHistoryItemChanged();
      }

   }

   public void hideAddressBar() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.hideAddressBar();
      }

   }

   public void handlePluginTag(String var1, String var2, boolean var3, String var4) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.handlePluginTag(var1, var2, var3, var4);
      }

   }

   public void onDoubleTapStart() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onDoubleTapStart();
      }

   }

   public void onPinchToZoomStart() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onPinchToZoomStart();
      }

   }

   public void onSlidingTitleOffScreen(int var1, int var2) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onSlidingTitleOffScreen(var1, var2);
      }

   }

   public boolean preShouldOverrideUrlLoading(IX5WebViewExtension var1, String var2) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.preShouldOverrideUrlLoading(var1, var2) : false;
   }

   public void onMissingPluginClicked(String var1, String var2, String var3, int var4) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onMissingPluginClicked(var1, var2, var3, var4);
      }

   }

   public void onReportAdFilterInfo(int var1, int var2, String var3, boolean var4) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onReportAdFilterInfo(var1, var2, var3, var4);
      }

   }

   public void onReportHtmlInfo(int var1, String var2) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onReportHtmlInfo(var1, var2);
      }

   }

   public void onNativeCrashReport(int var1, String var2) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onNativeCrashReport(var1, var2);
      }

   }

   public Object onMiscCallBack(String var1, Bundle var2) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.onMiscCallBack(var1, var2) : null;
   }

   public Object onMiscCallBack(String var1, Bundle var2, Object var3, Object var4, Object var5, Object var6) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.onMiscCallBack(var1, var2, var3, var4, var5, var6) : null;
   }

   public boolean onInterceptTouchEvent(MotionEvent var1, View var2) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.onInterceptTouchEvent(var1, var2) : false;
   }

   public boolean onTouchEvent(MotionEvent var1, View var2) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.onTouchEvent(var1, var2) : false;
   }

   public boolean dispatchTouchEvent(MotionEvent var1, View var2) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.dispatchTouchEvent(var1, var2) : false;
   }

   public boolean overScrollBy(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, View var10) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.overScrollBy(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10) : false;
   }

   public void onScrollChanged(int var1, int var2, int var3, int var4, View var5) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onScrollChanged(var1, var2, var3, var4, var5);
      }

   }

   public void onOverScrolled(int var1, int var2, boolean var3, boolean var4, View var5) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onOverScrolled(var1, var2, var3, var4, var5);
      }

   }

   public void computeScroll(View var1) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.computeScroll(var1);
      }

   }

   public void onMetricsSavedCountReceived(String var1, boolean var2, long var3, String var5, int var6) {
      if (this.mWebViewClientExt != null && sCompatibleOnMetricsSavedCountReceived) {
         try {
            this.mWebViewClientExt.onMetricsSavedCountReceived(var1, var2, var3, var5, var6);
         } catch (NoSuchMethodError var8) {
            if (var8.getMessage() == null || !var8.getMessage().contains("onMetricsSavedCountReceived")) {
               throw var8;
            }

            Log.d("incompatible-oldcore", "IX5WebViewClientExtension.onMetricsSavedCountReceived");
            sCompatibleOnMetricsSavedCountReceived = false;
         }
      }

   }

   public boolean notifyAutoAudioPlay(String var1, JsResult var2) {
      if (this.mWebViewClientExt != null) {
         try {
            return this.mWebViewClientExt.notifyAutoAudioPlay(var1, var2);
         } catch (NoSuchMethodError var4) {
            var4.printStackTrace();
         }
      }

      return false;
   }

   public boolean allowJavaScriptOpenWindowAutomatically(String var1, String var2) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.allowJavaScriptOpenWindowAutomatically(var1, var2) : false;
   }

   public boolean notifyJavaScriptOpenWindowsBlocked(String var1, String[] var2, ValueCallback<Boolean> var3, boolean var4) {
      return this.mWebViewClientExt != null ? this.mWebViewClientExt.notifyJavaScriptOpenWindowsBlocked(var1, var2, var3, var4) : false;
   }

   public boolean onShowLongClickPopupMenu() {
      if (this.mWebViewClientExt != null) {
         try {
            return this.mWebViewClientExt.onShowLongClickPopupMenu();
         } catch (NoSuchMethodError var2) {
            var2.printStackTrace();
         }
      }

      return false;
   }

   public void onResponseReceived(WebResourceRequest var1, WebResourceResponse var2, int var3) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onResponseReceived(var1, var2, var3);
      }

   }

   public void didFirstVisuallyNonEmptyPaint() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.didFirstVisuallyNonEmptyPaint();
      }

   }

   public void documentAvailableInMainFrame() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.documentAvailableInMainFrame();
      }

   }

   public void onReceivedViewSource(String var1) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onReceivedViewSource(var1);
      }

   }

   public void onPrefetchResourceHit(boolean var1) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onPrefetchResourceHit(var1);
      }

   }

   public void onReceivedSslErrorCancel() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onReceivedSslErrorCancel();
      }

   }

   public void invalidate() {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.invalidate();
      }

   }

   public void onPreloadCallback(int var1, String var2) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onPreloadCallback(var1, var2);
      }

   }

   public void onFakeLoginRecognised(Bundle var1) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onFakeLoginRecognised(var1);
      }

   }

   public void onReportResponseHeaders(String var1, int var2, HashMap<String, String> var3) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onReportResponseHeaders(var1, var2, var3);
      }

   }

   public void onReportMemoryCachedResponse(String var1, int var2, HashMap<String, String> var3) {
      if (this.mWebViewClientExt != null) {
         this.mWebViewClientExt.onReportMemoryCachedResponse(var1, var2, var3);
      }

   }

   public int getHostByName(String var1, List<String> var2) {
      if (this.mWebViewClientExt != null) {
         try {
            return this.mWebViewClientExt.getHostByName(var1, var2);
         } catch (NoSuchMethodError var4) {
            return 0;
         }
      } else {
         return 0;
      }
   }
}
