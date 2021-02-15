package com.tencent.smtt.export.external.extension.interfaces;

import java.util.List;
import java.util.Map;

public interface IX5WebSettingsExtension {
   int PicModel_NORMAL = 1;
   int PicModel_NoPic = 2;
   int PicModel_NetNoPic = 3;

   void setEnableUnderLine(boolean var1);

   void setPreFectch(boolean var1);

   void setPreFectchEnableWhenHasMedia(boolean var1);

   void setRememberScaleValue(boolean var1);

   void setDayOrNight(boolean var1);

   void setShouldTrackVisitedLinks(boolean var1);

   void setPageSolarEnableFlag(boolean var1);

   boolean getPageSolarEnableFlag();

   void setPageCacheCapacity(int var1);

   void setReadModeWebView(boolean var1);

   boolean isReadModeWebView();

   void setFitScreen(boolean var1);

   boolean isFitScreen();

   boolean isWapSitePreferred();

   void setWapSitePreferred(boolean var1);

   void setImgAsDownloadFile(boolean var1);

   void setWebViewInBackground(boolean var1);

   boolean isWebViewInBackground();

   void setOnlyDomTreeBuild(boolean var1);

   void setAdditionalHttpHeaders(Map<String, String> var1);

   void setAcceptCookie(boolean var1);

   void setRecordRequestEnabled(boolean var1);

   void setOnContextMenuEnable(boolean var1);

   void setContentCacheEnable(boolean var1);

   void setJavaScriptOpenWindowsBlockedNotifyEnabled(boolean var1);

   void setAutoRecoredAndRestoreScaleEnabled(boolean var1);

   void setTextDecorationUnlineEnabled(boolean var1);

   void setAutoDetectToOpenFitScreenEnabled(boolean var1);

   void setForcePinchScaleEnabled(boolean var1);

   void setSmartFullScreenEnabled(boolean var1);

   void setSelectionColorEnabled(boolean var1);

   void setIsViewSourceMode(boolean var1);

   void setUseQProxy(boolean var1);

   boolean setHttpDnsDomains(List<String> var1);

   void setFramePerformanceRecordEnable(boolean var1);

   void setJSPerformanceRecordEnable(boolean var1);

   void setShouldRequestFavicon(boolean var1);

   void setARModeEnable(boolean var1);

   void setTbsARShareType(int var1);

   void customDiskCachePathEnabled(boolean var1, String var2);

   void setImageScanEnable(boolean var1);

   void setFirstScreenSoftwareTextureDraw(boolean var1);

   void setBlockLocalAddressEnable(boolean var1);

   boolean getBlockLocalAddressEnable();

   void setFirstScreenDetect(boolean var1);

   void setPicModel(int var1);

   void setDisplayCutoutEnable(boolean var1);
}
