package com.tencent.smtt.export.external.extension.interfaces;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import java.util.HashMap;
import java.util.List;

public interface IX5WebViewClientExtension {
   int FRAME_LOADTYPE_STANDARD = 0;
   int FRAME_LOADTYPE_BACK = 1;
   int FRAME_LOADTYPE_FORWARD = 2;
   int FRAME_LOADTYPE_INDEXEDBACKFORWARD = 3;
   int FRAME_LOADTYPE_RELOAD = 4;
   int FRAME_LOADTYPE_RELOADALLOWINGSTALEDATA = 5;
   int FRAME_LOADTYPE_SAME = 6;
   int FRAME_LOADTYPE_REDIRECT = 7;
   int FRAME_LOADTYPE_REPLACE = 8;
   int FRAME_LOADTYPE_RELOADFROMORIGIN = 9;
   int FRAME_LOADTYPE_BACKWMLDECKNOTACCESSIBLE = 10;
   int FRAME_LOADTYPE_PREREAD = 11;
   int HTMLTYPE_JS_SCROLLTO = 1;

   void onMissingPluginClicked(String var1, String var2, String var3, int var4);

   void onHideListBox();

   void onShowListBox(String[] var1, int[] var2, int[] var3, int var4);

   void onShowMutilListBox(String[] var1, int[] var2, int[] var3, int[] var4);

   void onFlingScrollBegin(int var1, int var2, int var3);

   void onScrollChanged(int var1, int var2, int var3, int var4);

   void onInputBoxTextChanged(IX5WebViewExtension var1, String var2);

   void onTransitionToCommitted();

   void onUploadProgressStart(int var1);

   void onUploadProgressChange(int var1, int var2, String var3);

   void onSoftKeyBoardShow();

   void onSoftKeyBoardHide(int var1);

   void onSetButtonStatus(boolean var1, boolean var2);

   void onHistoryItemChanged();

   void hideAddressBar();

   void handlePluginTag(String var1, String var2, boolean var3, String var4);

   void onDoubleTapStart();

   void onPinchToZoomStart();

   void onSlidingTitleOffScreen(int var1, int var2);

   boolean preShouldOverrideUrlLoading(IX5WebViewExtension var1, String var2);

   void onPreReadFinished();

   void onPromptScaleSaved();

   void onFlingScrollEnd();

   void onUrlChange(String var1, String var2);

   void onReportAdFilterInfo(int var1, int var2, String var3, boolean var4);

   void onNativeCrashReport(int var1, String var2);

   Object onMiscCallBack(String var1, Bundle var2);

   void onReportHtmlInfo(int var1, String var2);

   Object onMiscCallBack(String var1, Bundle var2, Object var3, Object var4, Object var5, Object var6);

   boolean onInterceptTouchEvent(MotionEvent var1, View var2);

   boolean onTouchEvent(MotionEvent var1, View var2);

   boolean dispatchTouchEvent(MotionEvent var1, View var2);

   boolean overScrollBy(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, View var10);

   void onScrollChanged(int var1, int var2, int var3, int var4, View var5);

   void onOverScrolled(int var1, int var2, boolean var3, boolean var4, View var5);

   void computeScroll(View var1);

   void onMetricsSavedCountReceived(String var1, boolean var2, long var3, String var5, int var6);

   void showTranslateBubble(int var1, String var2, String var3, int var4);

   boolean notifyAutoAudioPlay(String var1, JsResult var2);

   boolean onShowLongClickPopupMenu();

   void onResponseReceived(WebResourceRequest var1, WebResourceResponse var2, int var3);

   boolean allowJavaScriptOpenWindowAutomatically(String var1, String var2);

   boolean notifyJavaScriptOpenWindowsBlocked(String var1, String[] var2, ValueCallback<Boolean> var3, boolean var4);

   void documentAvailableInMainFrame();

   void didFirstVisuallyNonEmptyPaint();

   void onReceivedViewSource(String var1);

   void onPrefetchResourceHit(boolean var1);

   void onReceivedSslErrorCancel();

   void invalidate();

   void onPreloadCallback(int var1, String var2);

   void onFakeLoginRecognised(Bundle var1);

   void onReportResponseHeaders(String var1, int var2, HashMap<String, String> var3);

   void onReportMemoryCachedResponse(String var1, int var2, HashMap<String, String> var3);

   int getHostByName(String var1, List<String> var2);
}
