package com.tencent.smtt.export.external.extension.interfaces;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.ValueCallback;
import com.tencent.smtt.export.external.interfaces.ISelectionInterface;
import com.tencent.smtt.export.external.interfaces.IX5ScrollListener;
import com.tencent.smtt.export.external.interfaces.IX5WebBackForwardListClient;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.IX5WebHistoryItem;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.IX5WebViewClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IX5WebViewExtension {
   int OVER_SCROLL_ALWAYS = 0;
   int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;
   int OVER_SCROLL_NEVER = 2;
   int HANLDEVIEW_POSITION_LEFT = 0;
   int HANDLEVIEW_POSITION_CENTER = 1;
   int HANDLEVIEW_POSITION_RIGHT = 2;
   int HANLDEVIEW_ALIGNMENT_LEFT = 0;
   int HANDLEVIEW_ALIGNMENT_CENTER = 1;
   int HANDLEVIEW_ALIGNMENT_RIGHT = 2;
   int RENDER_MODE_DEFAULT = 0;
   int RENDER_MODE_SMOOTHNESS_NORMAL = 1;
   int RENDER_MODE_SMOOTHNESS_AGGRESSIVE = 2;

   int getQQBrowserVersion();

   /** @deprecated */
   void clearTextEntry();

   void cutText(CharSequence var1);

   void copyText();

   void pasteText(CharSequence var1);

   String getFocusCandidateText();

   /** @deprecated */
   void clearTextFieldLongPressStatus();

   /** @deprecated */
   void setTextFieldInLongPressStatus(boolean var1);

   void replaceAllInputText(String var1);

   /** @deprecated */
   boolean isEditingMode();

   /** @deprecated */
   boolean isEnableSetFont();

   boolean inputNodeIsPasswordType();

   boolean inputNodeIsPhoneType();

   boolean requestFocusForInputNode(int var1);

   boolean isSelectionMode();

   void enterSelectionMode(boolean var1);

   String getSelectionText();

   void leaveSelectionMode();

   int seletionStatus();

   void setScrollListener(IX5ScrollListener var1);

   /** @deprecated */
   Point getSinglePressPoint();

   /** @deprecated */
   void setBackFromSystem();

   void setSelectListener(ISelectionInterface var1);

   IX5WebHistoryItem getHistoryItem(int var1);

   IX5WebChromeClient getWebChromeClient();

   IX5WebViewClient getWebViewClient();

   void setEmbTitleView(View var1, LayoutParams var2);

   void showImage(int var1, int var2);

   /** @deprecated */
   void setDisableDrawingWhileLosingFocus(boolean var1);

   int getCurrentHistoryItemIndex();

   void removeHistoryItem(int var1);

   void scrollTo(int var1, int var2);

   void scrollBy(int var1, int var2);

   /** @deprecated */
   void onPauseActiveDomObject();

   /** @deprecated */
   void onResumeActiveDomObject();

   void loaddataWithHeaders(String var1, String var2, String var3, Map<String, String> var4);

   void loadDataWithBaseURLWithHeaders(String var1, String var2, String var3, String var4, String var5, Map<String, String> var6);

   boolean isPreReadCanGoForward();

   /** @deprecated */
   int getWebTextScrollDis();

   void setWebBackForwardListClient(IX5WebBackForwardListClient var1);

   /** @deprecated */
   void trimMemory(int var1);

   /** @deprecated */
   boolean isPluginFullScreen();

   /** @deprecated */
   void exitPluginFullScreen();

   /** @deprecated */
   void setOrientation(int var1);

   /** @deprecated */
   void setScreenState(int var1);

   /** @deprecated */
   boolean inFullScreenMode();

   int getTitleHeight();

   void cancelLongPress();

   void reloadCustomMetaData();

   void preConnectQProxy();

   /** @deprecated */
   void replyListBox(int var1);

   /** @deprecated */
   void replyMultiListBox(int var1, boolean[] var2);

   /** @deprecated */
   void sendNeverRememberMsg(String var1, String var2, String var3, Message var4);

   /** @deprecated */
   void sendResumeMsg(String var1, String var2, String var3, Message var4);

   /** @deprecated */
   void sendRememberMsg(String var1, String var2, String var3, Message var4);

   void sendRememberMsg(String var1, String var2, String var3, String var4, String var5);

   void pruneMemoryCache();

   void snapshotVisible(Canvas var1, boolean var2, boolean var3, boolean var4, boolean var5);

   void snapshotVisible(Bitmap var1, boolean var2, boolean var3, boolean var4, boolean var5, float var6, float var7, Runnable var8);

   void snapshotWholePage(Canvas var1, boolean var2, boolean var3);

   void snapshotWholePage(Canvas var1, boolean var2, boolean var3, Runnable var4);

   /** @deprecated */
   Drawable snapshot(int var1, boolean var2);

   /** @deprecated */
   boolean capturePageToFile(Config var1, String var2, boolean var3, int var4, int var5);

   /** @deprecated */
   void savePageToDisk(String var1, Message var2);

   /** @deprecated */
   void savePageToDisk(String var1, boolean var2, int var3, ValueCallback<String> var4);

   void waitSWInstalled(String var1, Message var2);

   int getSharedVideoTime();

   void setSharedVideoTime(int var1);

   void setSniffVideoInfo(String var1, int var2, String var3, String var4);

   int getSniffVideoID();

   String getSniffVideoRefer();

   void setIsForVideoSniff(boolean var1);

   void onPauseNativeVideo();

   void pauseAudio();

   void playAudio();

   void deactive();

   void active();

   boolean isActive();

   void doFingerSearchIfNeed();

   void onFingerSearchResult(String var1, int var2, int var3);

   void retrieveFingerSearchContext(int var1);

   void focusAndPopupIM(String var1);

   boolean drawPreReadBaseLayer(Canvas var1, boolean var2);

   /** @deprecated */
   void invalidateContent();

   boolean getSolarMode();

   void onAppExit();

   void onPageTransFormationSettingChanged(boolean var1);

   /** @deprecated */
   void updateSelectionPosition();

   /** @deprecated */
   void forceSyncOffsetToCore();

   int getScrollX();

   int getScrollY();

   void updateContext(Context var1);

   /** @deprecated */
   void setDrawWithBuffer(boolean var1);

   /** @deprecated */
   boolean getDrawWithBuffer();

   /** @deprecated */
   boolean isMobileSite();

   /** @deprecated */
   void dumpDisplayTree();

   /** @deprecated */
   void documentAsText(Message var1);

   /** @deprecated */
   void documentDumpRenderTree(Message var1);

   /** @deprecated */
   void dumpViewportForLayoutTest(Message var1);

   void setHorizontalScrollBarDrawable(Drawable var1);

   void setHorizontalTrackDrawable(Drawable var1);

   void setVerticalScrollBarDrawable(Drawable var1);

   void setHorizontalScrollBarEnabled(boolean var1);

   void setVerticalTrackDrawable(Drawable var1);

   void setVerticalScrollBarEnabled(boolean var1);

   boolean isHorizontalScrollBarEnabled();

   boolean isVerticalScrollBarEnabled();

   void setScrollBarSize(int var1);

   void setScrollBarFadingEnabled(boolean var1);

   void setScrollBarFadeDuration(int var1);

   void setScrollBarDefaultDelayBeforeFade(int var1);

   void setOverScrollParams(int var1, int var2, int var3, int var4, int var5, int var6, Drawable var7, Drawable var8, Drawable var9);

   IX5WebChromeClientExtension getWebChromeClientExtension();

   void setWebChromeClientExtension(IX5WebChromeClientExtension var1);

   void setWebViewClientExtension(IX5WebViewClientExtension var1);

   IX5WebViewClientExtension getWebViewClientExtension();

   IX5WebSettingsExtension getSettingsExtension();

   /** @deprecated */
   @Deprecated
   Drawable wrapDrawableWithNativeBitmap(Drawable var1, int var2, Config var3);

   /** @deprecated */
   boolean shouldFitScreenLayout();

   Bundle getSdkQBStatisticsInfo();

   Object invokeMiscMethod(String var1, Bundle var2);

   void setLongPressTextExtensionMenu(int var1);

   boolean needSniff();

   void setForceEnableZoom(boolean var1);

   /** @deprecated */
   void setHandleViewBitmap(Bitmap var1, Bitmap var2, int var3, int var4);

   /** @deprecated */
   void setHandleViewLineIsShowing(boolean var1, int var2);

   /** @deprecated */
   void setHandleViewSelectionColor(int var1, int var2);

   /** @deprecated */
   void setHandleViewLineColor(int var1, int var2);

   void doTranslateAction(int var1);

   /** @deprecated */
   void setRenderMode(int var1);

   ArrayList<IX5WebViewBase.ImageInfo> getAllImageInfo();

   void updateImageList(int var1, int var2, boolean var3);

   Bitmap getBitmapByIndex(int var1);

   /** @deprecated */
   String getDocumentOuterHTML();

   void setAudioAutoPlayNotify(boolean var1);

   void registerServiceWorkerBackground(String var1, String var2);

   void updateServiceWorkerBackground(String var1);

   void registerServiceWorkerOffline(String var1, List<String> var2, boolean var3);

   void clearServiceWorkerCache();

   void unRegisterServiceWorker(String var1, boolean var2);

   void stopPreLoad(String var1);

   void preLoad(String var1, int var2, int var3, Map<String, String> var4);

   boolean isX5CoreSandboxMode();

   void setFakeLoginParams(Bundle var1);

   void getFakeLoginStatus(Bundle var1, ValueCallback<Bundle> var2);

   boolean registerEmbeddedWidget(String[] var1, Object var2);
}
