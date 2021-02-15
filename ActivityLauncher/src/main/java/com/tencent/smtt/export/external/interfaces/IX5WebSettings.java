package com.tencent.smtt.export.external.interfaces;

public interface IX5WebSettings {
   int LOAD_DEFAULT = -1;
   int LOAD_NORMAL = 0;
   int LOAD_CACHE_ELSE_NETWORK = 1;
   int LOAD_NO_CACHE = 2;
   int LOAD_CACHE_ONLY = 3;
   int LOAD_CACHE_AD = 100;
   int DEFAULT_CACHE_CAPACITY = 15;

   void setLoadsImagesAutomatically(boolean var1);

   void setSavePassword(boolean var1);

   void setUserAgent(String var1);

   void setUserAgent(String var1, boolean var2);

   void setJavaScriptEnabled(boolean var1);

   void setAllowFileAccess(boolean var1);

   void setSupportZoom(boolean var1);

   void setBuiltInZoomControls(boolean var1);

   void setUseWideViewPort(boolean var1);

   void setSupportMultipleWindows(boolean var1);

   void setLoadWithOverviewMode(boolean var1);

   void setAppCacheEnabled(boolean var1);

   void setAppCacheMaxSize(long var1);

   void setAppCachePath(String var1);

   void setDatabaseEnabled(boolean var1);

   boolean getDatabaseEnabled();

   void setDatabasePath(String var1);

   String getDatabasePath();

   void setDefaultDatabasePath(boolean var1);

   void setDomStorageEnabled(boolean var1);

   void setGeolocationEnabled(boolean var1);

   void setGeolocationDatabasePath(String var1);

   int getMixedContentMode();

   String getUserAgent();

   void setLayoutAlgorithm(IX5WebSettings.LayoutAlgorithm var1);

   void setPluginState(IX5WebSettings.PluginState var1);

   IX5WebSettings.PluginState getPluginState();

   void setTextSize(IX5WebSettings.TextSize var1);

   IX5WebSettings.TextSize getTextSize();

   void setJavaScriptCanOpenWindowsAutomatically(boolean var1);

   void setDefaultTextEncodingName(String var1);

   void setMinimumFontSize(int var1);

   void setMinimumLogicalFontSize(int var1);

   void setDefaultFontSize(int var1);

   void setDefaultFixedFontSize(int var1);

   void setNavDump(boolean var1);

   void setLightTouchEnabled(boolean var1);

   void setSaveFormData(boolean var1);

   void setNeedInitialFocus(boolean var1);

   void setAllowUniversalAccessFromFileURLs(boolean var1);

   void setAllowFileAccessFromFileURLs(boolean var1);

   void setCacheMode(int var1);

   void setPluginEnabled(boolean var1);

   void setUserAgentString(String var1);

   String getUserAgentString();

   void setBlockNetworkImage(boolean var1);

   void setRenderPriority(IX5WebSettings.RenderPriority var1);

   void setPluginsEnabled(boolean var1);

   void setAllowContentAccess(boolean var1);

   boolean getAllowContentAccess();

   boolean getAllowFileAccess();

   boolean getBuiltInZoomControls();

   int getDefaultFixedFontSize();

   String getCursiveFontFamily();

   int getDefaultFontSize();

   String getDefaultTextEncodingName();

   String getFantasyFontFamily();

   String getFixedFontFamily();

   boolean getJavaScriptCanOpenWindowsAutomatically();

   boolean getJavaScriptEnabled();

   IX5WebSettings.LayoutAlgorithm getLayoutAlgorithm();

   boolean getLightTouchEnabled();

   int getMinimumFontSize();

   int getMinimumLogicalFontSize();

   boolean getNavDump();

   boolean getPluginsEnabled();

   String getPluginsPath();

   String getSansSerifFontFamily();

   boolean getSaveFormData();

   boolean getSavePassword();

   String getSerifFontFamily();

   String getStandardFontFamily();

   boolean supportMultipleWindows();

   boolean supportZoom();

   boolean getUseWideViewPort();

   boolean getLoadsImagesAutomatically();

   void setFantasyFontFamily(String var1);

   void setFixedFontFamily(String var1);

   void setCursiveFontFamily(String var1);

   int getCacheMode();

   void setSansSerifFontFamily(String var1);

   void setSerifFontFamily(String var1);

   void setStandardFontFamily(String var1);

   void setBlockNetworkLoads(boolean var1);

   boolean getBlockNetworkImage();

   boolean getBlockNetworkLoads();

   void setDisplayZoomControls(boolean var1);

   boolean getDisplayZoomControls();

   boolean getLoadWithOverviewMode();

   void setEnableSmoothTransition(boolean var1);

   boolean enableSmoothTransition();

   void setUseWebViewBackgroundForOverscrollBackground(boolean var1);

   boolean getUseWebViewBackgroundForOverscrollBackground();

   void setTextZoom(int var1);

   int getTextZoom();

   void setDefaultZoom(IX5WebSettings.ZoomDensity var1);

   IX5WebSettings.ZoomDensity getDefaultZoom();

   void setPluginsPath(String var1);

   boolean getDomStorageEnabled();

   boolean getMediaPlaybackRequiresUserGesture();

   void setMediaPlaybackRequiresUserGesture(boolean var1);

   void setSafeBrowsingEnabled(boolean var1);

   boolean getSafeBrowsingEnabled();

   public static enum ZoomDensity {
      FAR(150),
      MEDIUM(100),
      CLOSE(75);

      int value;

      private ZoomDensity(int var3) {
         this.value = var3;
      }
   }

   public static enum TextSize {
      SMALLEST(50),
      SMALLER(75),
      NORMAL(100),
      LARGER(125),
      LARGEST(150);

      int value;

      private TextSize(int var3) {
         this.value = var3;
      }
   }

   public static enum RenderPriority {
      NORMAL,
      HIGH,
      LOW;
   }

   public static enum PluginState {
      ON,
      ON_DEMAND,
      OFF;
   }

   public static enum LayoutAlgorithm {
      NORMAL,
      SINGLE_COLUMN,
      NARROW_COLUMNS;
   }
}
