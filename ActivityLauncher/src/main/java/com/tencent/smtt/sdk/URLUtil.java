package com.tencent.smtt.sdk;

public final class URLUtil {
   public static String guessUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilGuessUrl(var0) : android.webkit.URLUtil.guessUrl(var0);
   }

   public static String composeSearchUrl(String var0, String var1, String var2) {
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      return null != var3 && var3.isInCharge() ? var3.getWVWizardBase().urlUtilComposeSearchUrl(var0, var1, var2) : android.webkit.URLUtil.composeSearchUrl(var0, var1, var2);
   }

   public static byte[] decode(byte[] var0) throws IllegalArgumentException {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilDecode(var0) : android.webkit.URLUtil.decode(var0);
   }

   public static boolean isAssetUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsAssetUrl(var0) : android.webkit.URLUtil.isAssetUrl(var0);
   }

   /** @deprecated */
   @Deprecated
   public static boolean isCookielessProxyUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsCookielessProxyUrl(var0) : android.webkit.URLUtil.isCookielessProxyUrl(var0);
   }

   public static boolean isFileUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsFileUrl(var0) : android.webkit.URLUtil.isFileUrl(var0);
   }

   public static boolean isAboutUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsAboutUrl(var0) : android.webkit.URLUtil.isAboutUrl(var0);
   }

   public static boolean isDataUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsDataUrl(var0) : android.webkit.URLUtil.isDataUrl(var0);
   }

   public static boolean isJavaScriptUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsJavaScriptUrl(var0) : android.webkit.URLUtil.isJavaScriptUrl(var0);
   }

   public static boolean isHttpUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsHttpUrl(var0) : android.webkit.URLUtil.isHttpUrl(var0);
   }

   public static boolean isHttpsUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsHttpsUrl(var0) : android.webkit.URLUtil.isHttpsUrl(var0);
   }

   public static boolean isNetworkUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsNetworkUrl(var0) : android.webkit.URLUtil.isNetworkUrl(var0);
   }

   public static boolean isContentUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsContentUrl(var0) : android.webkit.URLUtil.isContentUrl(var0);
   }

   public static boolean isValidUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilIsValidUrl(var0) : android.webkit.URLUtil.isValidUrl(var0);
   }

   public static String stripAnchor(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().urlUtilStripAnchor(var0) : android.webkit.URLUtil.stripAnchor(var0);
   }

   public static final String guessFileName(String var0, String var1, String var2) {
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      return null != var3 && var3.isInCharge() ? var3.getWVWizardBase().urlUtilGuessFileName(var0, var1, var2) : android.webkit.URLUtil.guessFileName(var0, var1, var2);
   }
}
