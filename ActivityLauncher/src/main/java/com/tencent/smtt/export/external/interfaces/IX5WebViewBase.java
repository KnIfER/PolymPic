package com.tencent.smtt.export.external.interfaces;

import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.Point;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.ValueCallback;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import java.io.BufferedWriter;
import java.io.File;
import java.util.Map;

public interface IX5WebViewBase {
   int OVER_SCROLL_ALWAYS = 0;
   int OVER_SCROLL_IF_CONTENT_SCROLLS = 1;
   int OVER_SCROLL_NEVER = 2;

   void setHorizontalScrollbarOverlay(boolean var1);

   void setVerticalScrollbarOverlay(boolean var1);

   boolean overlayHorizontalScrollbar();

   boolean overlayVerticalScrollbar();

   /** @deprecated */
   @Deprecated
   boolean savePicture(Bundle var1, File var2);

   /** @deprecated */
   @Deprecated
   boolean restorePicture(Bundle var1, File var2);

   /** @deprecated */
   @Deprecated
   void savePassword(String var1, String var2, String var3);

   void loadDataWithBaseURL(String var1, String var2, String var3, String var4, String var5);

   /** @deprecated */
   @Deprecated
   void clearView();

   void setInitialScale(int var1);

   void invokeZoomPicker();

   void requestFocusNodeHref(Message var1);

   void requestImageRef(Message var1);

   void clearFormData();

   boolean isPrivateBrowsingEnable();

   void clearSslPreferences();

   void documentHasImages(Message var1);

   /** @deprecated */
   @Deprecated
   View getZoomControls();

   /** @deprecated */
   @Deprecated
   boolean canZoomIn();

   boolean zoomIn();

   /** @deprecated */
   @Deprecated
   boolean canZoomOut();

   boolean zoomOut();

   /** @deprecated */
   @Deprecated
   void setMapTrackballToArrowKeys(boolean var1);

   void setNetworkAvailable(boolean var1);

   String getOriginalUrl();

   SslCertificate getCertificate();

   void flingScroll(int var1, int var2);

   void findNext(boolean var1);

   /** @deprecated */
   @Deprecated
   int findAll(String var1);

   void findAllAsync(String var1);

   void clearMatches();

   IX5WebBackForwardList copyBackForwardList();

   IX5WebSettings getSettings();

   void addJavascriptInterface(Object var1, String var2);

   void removeJavascriptInterface(String var1);

   void setPictureListener(IX5WebViewBase.PictureListener var1);

   boolean canGoBack();

   boolean canGoForward();

   void clearCache(boolean var1);

   void destroy();

   Bitmap getFavicon();

   String[] getHttpAuthUsernamePassword(String var1, String var2);

   int getProgress();

   String getTitle();

   String getUrl();

   void goBack();

   void goBackOrForward(int var1);

   void goForward();

   void loadData(String var1, String var2, String var3);

   void loadUrl(String var1);

   void loadUrl(String var1, Map<String, String> var2);

   boolean pageDown(boolean var1, int var2);

   boolean pageUp(boolean var1, int var2);

   void reload();

   void setDownloadListener(DownloadListener var1);

   void setHttpAuthUsernamePassword(String var1, String var2, String var3, String var4);

   void setWebViewClient(IX5WebViewClient var1);

   void setWebChromeClient(IX5WebChromeClient var1);

   void stopLoading();

   /** @deprecated */
   @Deprecated
   float getScale();

   boolean canGoBackOrForward(int var1);

   Picture capturePicture();

   Object createPrintDocumentAdapter(String var1);

   int getContentHeight();

   void pauseTimers();

   void resumeTimers();

   void clearHistory();

   void onPause();

   void onResume();

   void postUrl(String var1, byte[] var2);

   IX5WebBackForwardList restoreState(Bundle var1);

   IX5WebBackForwardList saveState(Bundle var1);

   void freeMemory();

   void saveWebArchive(String var1);

   void saveWebArchive(String var1, boolean var2, ValueCallback<String> var3);

   /** @deprecated */
   @Deprecated
   boolean showFindDialog(String var1, boolean var2);

   void setFindListener(IX5WebViewBase.FindListener var1);

   IX5WebViewBase.HitTestResult getHitTestResult();

   void dumpViewHierarchyWithProperties(BufferedWriter var1, int var2);

   View findHierarchyView(String var1, int var2);

   void refreshPlugins(boolean var1);

   void computeScroll();

   void setBackgroundColor(int var1);

   View getView();

   int getVisibleTitleHeight();

   void setCertificate(SslCertificate var1);

   int getContentWidth();

   IX5WebViewExtension getX5WebViewExtension();

   public static class WebViewTransport {
      private IX5WebViewBase mWebview;

      public synchronized void setWebView(IX5WebViewBase var1) {
         this.mWebview = var1;
      }

      public synchronized IX5WebViewBase getWebView() {
         return this.mWebview;
      }
   }

   /** @deprecated */
   @Deprecated
   public interface PictureListener {
      /** @deprecated */
      @Deprecated
      void onNewPicture(IX5WebViewBase var1, Picture var2, boolean var3);

      void onNewPictureIfHaveContent(IX5WebViewBase var1, Picture var2);
   }

   public interface FindListener {
      void onFindResultReceived(int var1, int var2, boolean var3);
   }

   public static class ImageInfo {
      public String mPicUrl;
      public long mRawDataSize;
      public boolean mIsGif;

      public String getPicUrl() {
         return this.mPicUrl;
      }

      public long getPicSize() {
         return this.mRawDataSize;
      }

      public boolean isGif() {
         return this.mIsGif;
      }
   }

   public static class HitTestResult {
      public static final int UNKNOWN_TYPE = 0;
      /** @deprecated */
      @Deprecated
      public static final int ANCHOR_TYPE = 1;
      public static final int PHONE_TYPE = 2;
      public static final int GEO_TYPE = 3;
      public static final int EMAIL_TYPE = 4;
      public static final int IMAGE_TYPE = 5;
      /** @deprecated */
      @Deprecated
      public static final int IMAGE_ANCHOR_TYPE = 6;
      public static final int SRC_ANCHOR_TYPE = 7;
      public static final int SRC_IMAGE_ANCHOR_TYPE = 8;
      public static final int EDIT_TEXT_TYPE = 9;
      public static final int BUTTON_TYPE = 10;
      private int mType = 0;
      private boolean mIsFromSinglePress = false;
      private Object mData;
      private Point mPoint;
      private String mExtra;

      public boolean isFromSinglePress() {
         return this.mIsFromSinglePress;
      }

      public void setIsFromSinglePress(boolean var1) {
         this.mIsFromSinglePress = var1;
      }

      public void setType(int var1) {
         this.mType = var1;
      }

      public void setData(Object var1) {
         this.mData = var1;
      }

      public int getType() {
         return this.mType;
      }

      public Object getData() {
         return this.mData;
      }

      public void setHitTestPoint(Point var1) {
         this.mPoint = var1;
      }

      public Point getHitTestPoint() {
         return new Point(this.mPoint);
      }

      public void setExtra(String var1) {
         this.mExtra = var1;
      }

      public String getExtra() {
         return this.mExtra;
      }

      protected Bitmap getBitmapData() {
         return null;
      }

      public class EditableData {
         public String mEditableText;
         public boolean mIsPassword;
      }

      public class AnchorData {
         public String mAnchorUrl;
         public String mAnchorTitle;
      }

      public class ImageAnchorData {
         public String mPicUrl;
         public String mAHref;
         public Bitmap mBmp;
         public long mRawDataSize;

         public Bitmap getBitmap() {
            return HitTestResult.this.getBitmapData();
         }
      }

      public class ImageData {
         public String mPicUrl;
         public Bitmap mBmp;
         public long mRawDataSize;
         public int mImgWidth;
         public int mImgHeight;

         public Bitmap getBitmap() {
            return HitTestResult.this.getBitmapData();
         }
      }
   }
}
