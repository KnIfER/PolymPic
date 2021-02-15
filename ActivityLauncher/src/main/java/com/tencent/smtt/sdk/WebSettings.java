package com.tencent.smtt.sdk;

import android.annotation.TargetApi;
import android.content.Context;

import com.tencent.smtt.export.external.interfaces.IX5WebSettings;

public class WebSettings extends android.webkit.WebSettings{
   public static final int LOAD_DEFAULT = -1;
   public static final int LOAD_NORMAL = 0;
   public static final int LOAD_CACHE_ELSE_NETWORK = 1;
   public static final int LOAD_NO_CACHE = 2;
   public static final int LOAD_CACHE_ONLY = 3;
   private IX5WebSettings x5WebSettings = null;

   WebSettings(IX5WebSettings var1) {
      this.x5WebSettings = var1;
   }

   /** @deprecated */
   @Deprecated
   public void setNavDump(boolean var1) {
	   this.x5WebSettings.setNavDump(var1);
   }

   public synchronized int getMixedContentMode() {
	   try {
		   return this.x5WebSettings.getMixedContentMode();
	   } catch (Throwable var2) {
		   var2.printStackTrace();
		   return -1;
	   }
   }
	
	@Override
	public void setOffscreenPreRaster(boolean enabled) {
	
	}
	
	@Override
	public boolean getOffscreenPreRaster() {
		return false;
	}
	
	/** @deprecated */
   @Deprecated
   public boolean getNavDump() {
	   return this.x5WebSettings.getNavDump();
   }

   public void setSupportZoom(boolean var1) {
	   this.x5WebSettings.setSupportZoom(var1);
   }

   public boolean supportZoom() {
	   return this.x5WebSettings.supportZoom();
   }

   @TargetApi(3)
   public void setBuiltInZoomControls(boolean var1) {
	   this.x5WebSettings.setBuiltInZoomControls(var1);
   }

   @TargetApi(3)
   public boolean getBuiltInZoomControls() {
	   return this.x5WebSettings.getBuiltInZoomControls();
   }

   @TargetApi(11)
   public void setDisplayZoomControls(boolean var1) {
	   this.x5WebSettings.setDisplayZoomControls(var1);
   }

   @TargetApi(11)
   public boolean getDisplayZoomControls() {
	   return this.x5WebSettings.getDisplayZoomControls();
   }

   @TargetApi(3)
   public void setAllowFileAccess(boolean var1) {
	   this.x5WebSettings.setAllowFileAccess(var1);
   }

   @TargetApi(3)
   public boolean getAllowFileAccess() {
	   return this.x5WebSettings.getAllowFileAccess();
   }

   @TargetApi(11)
   public void setAllowContentAccess(boolean var1) {
	   this.x5WebSettings.setAllowContentAccess(var1);
   }

   @TargetApi(21)
   public void setMixedContentMode(int var1) {
   }

   @TargetApi(11)
   public boolean getAllowContentAccess() {
	   return this.x5WebSettings.getAllowContentAccess();
   }

   @TargetApi(7)
   public void setLoadWithOverviewMode(boolean var1) {
	   this.x5WebSettings.setLoadWithOverviewMode(var1);
   }

   @TargetApi(7)
   public boolean getLoadWithOverviewMode() {
	   return this.x5WebSettings.getLoadWithOverviewMode();
   }

   /** @deprecated */
   @Deprecated
   @TargetApi(11)
   public void setEnableSmoothTransition(boolean var1) {
	   this.x5WebSettings.setEnableSmoothTransition(var1);
   }

   /** @deprecated */
   @Deprecated
   public boolean enableSmoothTransition() {
	   return this.x5WebSettings.enableSmoothTransition();
   }

   /** @deprecated */
   @Deprecated
   public void setUseWebViewBackgroundForOverscrollBackground(boolean var1) {
	   this.x5WebSettings.setUseWebViewBackgroundForOverscrollBackground(var1);
   }

   /** @deprecated */
   @Deprecated
   public boolean getUseWebViewBackgroundForOverscrollBackground() {
	   return this.x5WebSettings.getUseWebViewBackgroundForOverscrollBackground();
   }

   /** @deprecated */
   @Deprecated
   public void setSaveFormData(boolean var1) {
	   this.x5WebSettings.setSaveFormData(var1);
   }

   /** @deprecated */
   @Deprecated
   public boolean getSaveFormData() {
	   return this.x5WebSettings.getSaveFormData();
   }

   /** @deprecated */
   @Deprecated
   public void setSavePassword(boolean var1) {
	   this.x5WebSettings.setSavePassword(var1);
   }

   /** @deprecated */
   @Deprecated
   public boolean getSavePassword() {
	   return this.x5WebSettings.getSavePassword();
   }

   @TargetApi(14)
   public synchronized void setTextZoom(int var1) {
	   this.x5WebSettings.setTextZoom(var1);
   }

   @TargetApi(14)
   public synchronized int getTextZoom() {
	   return this.x5WebSettings.getTextZoom();
   }

   /** @deprecated */
   @Deprecated
   public void setTextSize(android.webkit.WebSettings.TextSize var1) {
	   this.x5WebSettings.setTextSize(IX5WebSettings.TextSize.valueOf(var1.name()));
   }

   /** @deprecated
	* @return */
   @Deprecated
   public android.webkit.WebSettings.TextSize getTextSize() {
	   return android.webkit.WebSettings.TextSize.valueOf(this.x5WebSettings.getTextSize().name());
   }

   /** @deprecated */
   @Deprecated
   @TargetApi(7)
   public void setDefaultZoom(android.webkit.WebSettings.ZoomDensity var1) {
   	this.x5WebSettings.setDefaultZoom(IX5WebSettings.ZoomDensity.valueOf(var1.name()));
   }

   /** @deprecated
	* @return */
   @Deprecated
   @TargetApi(7)
   public android.webkit.WebSettings.ZoomDensity getDefaultZoom() {
	   return android.webkit.WebSettings.ZoomDensity.valueOf(this.x5WebSettings.getDefaultZoom().name());
   }

   /** @deprecated */
   @Deprecated
   public void setLightTouchEnabled(boolean var1) {
	   this.x5WebSettings.setLightTouchEnabled(var1);
   }

   /** @deprecated */
   @Deprecated
   public boolean getLightTouchEnabled() {
	   return this.x5WebSettings.getLightTouchEnabled();
   }

   public void setUserAgent(String var1) {
	   this.x5WebSettings.setUserAgent(var1);
   }

   @TargetApi(3)
   public String getUserAgentString() {
	   return this.x5WebSettings.getUserAgentString();
   }

   @TargetApi(3)
   public void setUserAgentString(String var1) {
	   this.x5WebSettings.setUserAgentString(var1);
   }

   public void setUseWideViewPort(boolean var1) {
	   this.x5WebSettings.setUseWideViewPort(var1);
   }

   public synchronized boolean getUseWideViewPort() {
	   return this.x5WebSettings.getUseWideViewPort();
   }

   public void setSupportMultipleWindows(boolean var1) {
	   this.x5WebSettings.setSupportMultipleWindows(var1);
   }

   public synchronized boolean supportMultipleWindows() {
	   return this.x5WebSettings.supportMultipleWindows();
   }

   public void setLayoutAlgorithm(android.webkit.WebSettings.LayoutAlgorithm var1) {
	   this.x5WebSettings.setLayoutAlgorithm(IX5WebSettings.LayoutAlgorithm.valueOf(var1.name()));
   }

   public synchronized android.webkit.WebSettings.LayoutAlgorithm getLayoutAlgorithm() {
         return android.webkit.WebSettings.LayoutAlgorithm.valueOf(this.x5WebSettings.getLayoutAlgorithm().name());
   }

   public synchronized void setStandardFontFamily(String var1) {
	   this.x5WebSettings.setStandardFontFamily(var1);
   }

   public synchronized String getStandardFontFamily() {
	   return this.x5WebSettings.getStandardFontFamily();
   }

   public synchronized void setFixedFontFamily(String var1) {
	   this.x5WebSettings.setFixedFontFamily(var1);
   }

   public synchronized String getFixedFontFamily() {
	   return this.x5WebSettings.getFixedFontFamily();
   }

   public synchronized void setSansSerifFontFamily(String var1) {
	   this.x5WebSettings.setSansSerifFontFamily(var1);
   }

   public synchronized String getSansSerifFontFamily() {
	   return this.x5WebSettings.getSansSerifFontFamily();
   }

   public synchronized void setSerifFontFamily(String var1) {
	   this.x5WebSettings.setSerifFontFamily(var1);
   }

   public synchronized String getSerifFontFamily() {
	   return this.x5WebSettings.getSerifFontFamily();
   }

   public synchronized void setCursiveFontFamily(String var1) {
	   this.x5WebSettings.setCursiveFontFamily(var1);
   }

   public synchronized String getCursiveFontFamily() {
	   return this.x5WebSettings.getCursiveFontFamily();
   }

   public synchronized void setFantasyFontFamily(String var1) {
	   this.x5WebSettings.setFantasyFontFamily(var1);
   }

   public synchronized String getFantasyFontFamily() {
	   return this.x5WebSettings.getFantasyFontFamily();
   }

   public synchronized void setMinimumFontSize(int var1) {
	   this.x5WebSettings.setMinimumFontSize(var1);
   }

   public synchronized int getMinimumFontSize() {
	   return this.x5WebSettings.getMinimumFontSize();
   }

   public synchronized void setMinimumLogicalFontSize(int var1) {
	   this.x5WebSettings.setMinimumLogicalFontSize(var1);
   }

   public synchronized int getMinimumLogicalFontSize() {
	   return this.x5WebSettings.getMinimumLogicalFontSize();
   }

   public synchronized void setDefaultFontSize(int var1) {
	   this.x5WebSettings.setDefaultFontSize(var1);
   }

   public synchronized int getDefaultFontSize() {
	   return this.x5WebSettings.getDefaultFontSize();
   }

   public synchronized void setDefaultFixedFontSize(int var1) {
	   this.x5WebSettings.setDefaultFixedFontSize(var1);
   }

   public synchronized int getDefaultFixedFontSize() {
	   return this.x5WebSettings.getDefaultFixedFontSize();
   }

   public void setLoadsImagesAutomatically(boolean var1) {
	   this.x5WebSettings.setLoadsImagesAutomatically(var1);
   }

   public synchronized boolean getLoadsImagesAutomatically() {
	   return this.x5WebSettings.getLoadsImagesAutomatically();
   }

   public void setBlockNetworkImage(boolean var1) {
	   this.x5WebSettings.setBlockNetworkImage(var1);
   }

   public synchronized boolean getBlockNetworkImage() {
	   return this.x5WebSettings.getBlockNetworkImage();
   }

   @TargetApi(8)
   public synchronized void setBlockNetworkLoads(boolean var1) {
	   this.x5WebSettings.setBlockNetworkLoads(var1);
   }

   @TargetApi(8)
   public synchronized boolean getBlockNetworkLoads() {
	   return this.x5WebSettings.getBlockNetworkLoads();
   }

   /** @deprecated */
   @Deprecated
   public void setJavaScriptEnabled(boolean var1) {
      try {
		  this.x5WebSettings.setJavaScriptEnabled(var1);
      } catch (Throwable var3) {
         var3.printStackTrace();
      }
   }

   @TargetApi(16)
   public void setAllowUniversalAccessFromFileURLs(boolean var1) {
	   this.x5WebSettings.setAllowUniversalAccessFromFileURLs(var1);
   }

   @TargetApi(16)
   public void setAllowFileAccessFromFileURLs(boolean var1) {
	   this.x5WebSettings.setAllowFileAccessFromFileURLs(var1);
   }

   /** @deprecated */
   @Deprecated
   public void setPluginsEnabled(boolean var1) {
	   this.x5WebSettings.setPluginsEnabled(var1);
   }

   /** @deprecated */
   @Deprecated
   @TargetApi(8)
   public synchronized void setPluginState(android.webkit.WebSettings.PluginState var1) {
	   this.x5WebSettings.setPluginState(IX5WebSettings.PluginState.valueOf(var1.name()));
   }

   /** @deprecated */
   @Deprecated
   public synchronized void setPluginsPath(String var1) {
	   this.x5WebSettings.setPluginsPath(var1);
   }

   /** @deprecated */
   @Deprecated
   @TargetApi(5)
   public void setDatabasePath(String var1) {
	   this.x5WebSettings.setDatabasePath(var1);
   }

   /** @deprecated */
   @Deprecated
   @TargetApi(5)
   public void setGeolocationDatabasePath(String var1) {
	   this.x5WebSettings.setGeolocationDatabasePath(var1);
   }

   @TargetApi(7)
   public void setAppCacheEnabled(boolean var1) {
	   this.x5WebSettings.setAppCacheEnabled(var1);
   }

   @TargetApi(7)
   public void setAppCachePath(String var1) {
	   this.x5WebSettings.setAppCachePath(var1);
   }

   /** @deprecated */
   @Deprecated
   @TargetApi(7)
   public void setAppCacheMaxSize(long var1) {
	   this.x5WebSettings.setAppCacheMaxSize(var1);
   }

   @TargetApi(5)
   public void setDatabaseEnabled(boolean var1) {
	   this.x5WebSettings.setDatabaseEnabled(var1);
   }

   @TargetApi(7)
   public void setDomStorageEnabled(boolean var1) {
	   this.x5WebSettings.setDomStorageEnabled(var1);
   }

   @TargetApi(7)
   public synchronized boolean getDomStorageEnabled() {
	   return this.x5WebSettings.getDomStorageEnabled();
   }

   /** @deprecated */
   @Deprecated
   @TargetApi(5)
   public synchronized String getDatabasePath() {
	   return this.x5WebSettings.getDatabasePath();
   }

   @TargetApi(5)
   public synchronized boolean getDatabaseEnabled() {
	   return this.x5WebSettings.getDatabaseEnabled();
   }

   @TargetApi(5)
   public void setGeolocationEnabled(boolean var1) {
	   this.x5WebSettings.setGeolocationEnabled(var1);
   }

   public synchronized boolean getJavaScriptEnabled() {
	   return this.x5WebSettings.getJavaScriptEnabled();
   }
	
	@Override
	public boolean getAllowUniversalAccessFromFileURLs() {
   		//ttt
		return false;
	}
	
	@Override
	public boolean getAllowFileAccessFromFileURLs() {
		//ttt
		return false;
	}
	
	/** @deprecated */
   @Deprecated
   @TargetApi(8)
   public synchronized boolean getPluginsEnabled() {
	   return this.x5WebSettings.getPluginsEnabled();
   }

   /** @deprecated */
   @Deprecated
   @TargetApi(8)
   public synchronized android.webkit.WebSettings.PluginState getPluginState() {
	   return android.webkit.WebSettings.PluginState.valueOf(this.x5WebSettings.getPluginState().name());
   }

   /** @deprecated */
   @Deprecated
   public synchronized String getPluginsPath() {
	   return this.x5WebSettings.getPluginsPath();
   }

   public synchronized void setJavaScriptCanOpenWindowsAutomatically(boolean var1) {
	   this.x5WebSettings.setJavaScriptCanOpenWindowsAutomatically(var1);
   }

   public synchronized boolean getJavaScriptCanOpenWindowsAutomatically() {
	   return this.x5WebSettings.getJavaScriptCanOpenWindowsAutomatically();
   }

   public synchronized void setDefaultTextEncodingName(String var1) {
	   this.x5WebSettings.setDefaultTextEncodingName(var1);
   }

   public synchronized String getDefaultTextEncodingName() {
	   return this.x5WebSettings.getDefaultTextEncodingName();
   }

   @TargetApi(17)
   public static String getDefaultUserAgent(Context var0) {
	   return X5CoreEngine.getInstance().getWVWizardBase().getDefaultUserAgent(var0);
   }

   @TargetApi(17)
   public boolean getMediaPlaybackRequiresUserGesture() {
	   return this.x5WebSettings.getMediaPlaybackRequiresUserGesture();
   }

   @TargetApi(17)
   public void setMediaPlaybackRequiresUserGesture(boolean var1) {
	   this.x5WebSettings.setMediaPlaybackRequiresUserGesture(var1);
   }

   public void setNeedInitialFocus(boolean var1) {
	   this.x5WebSettings.setNeedInitialFocus(var1);
   }

   /** @deprecated */
   @Deprecated
   public void setRenderPriority(android.webkit.WebSettings.RenderPriority var1) {
   	this.x5WebSettings.setRenderPriority(IX5WebSettings.RenderPriority.valueOf(var1.name()));
   }

   public void setCacheMode(int var1) {
	   this.x5WebSettings.setCacheMode(var1);
   }

   public int getCacheMode() {
	   return this.x5WebSettings.getCacheMode();
   }

   public void setSafeBrowsingEnabled(boolean var1) {
	   try {
		   this.x5WebSettings.setSafeBrowsingEnabled(var1);
	   } catch (Throwable var3) {
		   var3.printStackTrace();
	   }
   }

   public boolean getSafeBrowsingEnabled() {
	   try {
		   return this.x5WebSettings.getSafeBrowsingEnabled();
	   } catch (Throwable var2) {
		   var2.printStackTrace();
	   }
      return false;
   }
	
	@Override
	public void setDisabledActionModeMenuItems(int menuItems) {
		//ttt
	}
	
	@Override
	public int getDisabledActionModeMenuItems() {
		//ttt
		return 0;
	}
}
