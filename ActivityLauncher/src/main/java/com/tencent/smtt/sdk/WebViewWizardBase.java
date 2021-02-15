package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.tencent.smtt.export.external.DexLoader;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewClientExtension;
import com.tencent.smtt.export.external.interfaces.IX5CoreServiceWorkerController;
import com.tencent.smtt.export.external.interfaces.IX5DateSorter;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.IX5WebViewClient;
import com.tencent.smtt.export.external.interfaces.IconListener;
import com.tencent.smtt.utils.TbsLog;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

class WebViewWizardBase {
   private DexLoader dexLoader;

   public WebViewWizardBase(DexLoader dexLoader) {
      this.dexLoader = dexLoader;
   }

   public boolean canUseX5() throws Throwable {
      Object var1 = null;

      try {
         Method var2 = this.dexLoader.getClassLoader().loadClass("com.tencent.tbs.tbsshell.WebCoreProxy").getMethod("canUseX5");
         var2.setAccessible(true);
         var1 = var2.invoke((Object)null);
         if (var1 instanceof Boolean) {
            return (Boolean)var1;
         }
      } catch (Throwable var3) {
         throw var3;
      }

      return (Boolean)var1;
   }

   public DexLoader getDexLoader() {
      return this.dexLoader;
   }

   public Object cacheDisabled() {
      return this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cacheDisabled", new Class[0]);
   }

   public boolean cookieManager_acceptCookie() {
      Object var1 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_acceptCookie", new Class[0]);
      return null == var1 ? false : (Boolean)var1;
   }

   public void cookieManager_removeAllCookie() {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_removeAllCookie", new Class[0]);
   }

   public boolean cookieManager_setCookies(Map<String, String[]> var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_setCookies", new Class[]{Map.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public void webview_setWebContentsDebuggingEnabled(boolean var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webview_setWebContentsDebuggingEnabled", new Class[]{Boolean.TYPE}, var1);
   }

   public IX5WebViewBase createSDKWebview(Context var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "createSDKWebview", new Class[]{Context.class}, var1);
      IX5WebViewBase var3 = null;

      try {
         if (null == var2) {
            Object var4 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "getLoadFailureDetails", new Class[0]);
            if (var4 != null && var4 instanceof Throwable) {
               TbsCoreLoadStat.getInstance().a(var1, 325, (Throwable)var4);
            }

            if (var4 != null && var4 instanceof String) {
               TbsCoreLoadStat.getInstance().a(var1, 325, new Throwable((String)var4));
            }

            var2 = null;
         } else {
            var3 = (IX5WebViewBase)var2;
            if (var3 != null && var3.getView() == null) {
               TbsCoreLoadStat.getInstance().a(var1, 325, new Throwable("x5webview.getView is null!"));
               var2 = null;
            }
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return null == var2 ? null : var3;
   }

   public String getCookie(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "getCookie", new Class[]{String.class}, var1);
      return null == var2 ? null : (String)var2;
   }

   public String getMiniQBVersion() {
      Object var1 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "getMiniQBVersion", new Class[0]);
      return null == var1 ? null : (String)var1;
   }

   public InputStream getCacheFile(String var1, boolean var2) {
      Object var3 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "getCacheFile", new Class[]{String.class, Boolean.TYPE}, var1, var2);
      return null == var3 ? null : (InputStream)var3;
   }

   public Object getCachFileBaseDir() {
      return this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "getCachFileBaseDir", new Class[0]);
   }

   public boolean cookieManager_hasCookies() {
      Object var1 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_hasCookies", new Class[0]);
      return null == var1 ? false : (Boolean)var1;
   }

   public IX5WebChromeClient createDefaultX5WebChromeClient() {
      if (this.dexLoader == null) {
         return null;
      } else {
         Object var1 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "createDefaultX5WebChromeClient", new Class[0]);
         return null == var1 ? null : (IX5WebChromeClient)var1;
      }
   }

   public IX5WebViewClient createDefaultX5WebViewClient() {
      Object var1 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "createDefaultX5WebViewClient", new Class[0]);
      return null == var1 ? null : (IX5WebViewClient)var1;
   }

   public IX5WebViewClientExtension createDefaultX5WebChromeClientExtension() {
      Object var1 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "createDefaultX5WebChromeClientExtension", new Class[0]);
      return null == var1 ? null : (IX5WebViewClientExtension)var1;
   }

   public void openIconDB(String var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "openIconDB", new Class[]{String.class}, var1);
   }

   public Uri[] parseFileChooserResult(int var1, Intent var2) {
      Object var3 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "parseFileChooserResult", new Class[]{Integer.TYPE, Intent.class}, var1, var2);
      return null == var3 ? null : (Uri[])((Uri[])var3);
   }

   public void removeAllIcons() {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "removeAllIcons", (Class[])null);
   }

   public void requestIconForPageUrl(String var1, IconListener var2) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "requestIconForPageUrl", new Class[]{String.class, IconListener.class}, var1, var2);
   }

   public void retainIconForPageUrl(String var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "retainIconForPageUrl", new Class[]{String.class}, var1);
   }

   public void releaseIconForPageUrl(String var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "releaseIconForPageUrl", new Class[]{String.class}, var1);
   }

   public void closeIconDB() {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "closeIconDB", (Class[])null);
   }

   public boolean webViewDatabaseHasUsernamePassword(Context var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseHasUsernamePassword", new Class[]{Context.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public void webViewDatabaseClearUsernamePassword(Context var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseClearUsernamePassword", new Class[]{Context.class}, var1);
   }

   public boolean webViewDatabaseHasHttpAuthUsernamePassword(Context var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseHasHttpAuthUsernamePassword", new Class[]{Context.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public void webViewDatabaseClearHttpAuthUsernamePassword(Context var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseClearHttpAuthUsernamePassword", new Class[]{Context.class}, var1);
   }

   public boolean webViewDatabaseHasFormData(Context var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseHasFormData", new Class[]{Context.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public void webViewDatabaseClearFormData(Context var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseClearFormData", new Class[]{Context.class}, var1);
   }

   public void webStorageGetOrigins(android.webkit.ValueCallback<Map> var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webStorageGetOrigins", new Class[]{android.webkit.ValueCallback.class}, var1);
   }

   public void webStorageGetUsageForOrigin(String var1, android.webkit.ValueCallback<Long> var2) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webStorageGetUsageForOrigin", new Class[]{String.class, android.webkit.ValueCallback.class}, var1, var2);
   }

   public void webStorageGetQuotaForOrigin(String var1, android.webkit.ValueCallback<Long> var2) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webStorageGetQuotaForOrigin", new Class[]{String.class, android.webkit.ValueCallback.class}, var1, var2);
   }

   public void webStorageSetQuotaForOrigin(String var1, long var2) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webStorageSetQuotaForOrigin", new Class[]{String.class, Long.TYPE}, var1, var2);
   }

   public void webStorageDeleteOrigin(String var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webStorageDeleteOrigin", new Class[]{String.class}, var1);
   }

   public void webStorageDeleteAllData() {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webStorageDeleteAllData", (Class[])null);
   }

   public IX5DateSorter createDateSorter(Context var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "createDateSorter", new Class[]{Context.class}, var1);
      return null == var2 ? null : (IX5DateSorter)var2;
   }

   public void geolocationPermissionsGetOrigins(android.webkit.ValueCallback<Set<String>> var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "geolocationPermissionsGetOrigins", new Class[]{android.webkit.ValueCallback.class}, var1);
   }

   public void geolocationPermissionsGetAllowed(String var1, android.webkit.ValueCallback<Boolean> var2) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "geolocationPermissionsGetAllowed", new Class[]{String.class, android.webkit.ValueCallback.class}, var1, var2);
   }

   public void geolocationPermissionsClear(String var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "geolocationPermissionsClear", new Class[]{String.class}, var1);
   }

   public void geolocationPermissionsAllow(String var1) {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "geolocationPermissionsAllow", new Class[]{String.class}, var1);
   }

   public void geolocationPermissionsClearAll() {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "geolocationPermissionsClearAll", (Class[])null);
   }

   public String mimeTypeMapGetFileExtensionFromUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "mimeTypeMapGetFileExtensionFromUrl", new Class[]{String.class}, var1);
      return null == var2 ? null : (String)var2;
   }

   public boolean mimeTypeMapHasMimeType(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "mimeTypeMapHasMimeType", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public String mimeTypeMapGetMimeTypeFromExtension(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "mimeTypeMapGetMimeTypeFromExtension", new Class[]{String.class}, var1);
      return null == var2 ? null : (String)var2;
   }

   public boolean mimeTypeMapHasExtension(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "mimeTypeMapHasExtension", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public String l(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "mimeTypeMapGetMimeTypeFromExtension", new Class[]{String.class}, var1);
      return null == var2 ? null : (String)var2;
   }

   public String urlUtilGuessUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilGuessUrl", new Class[]{String.class}, var1);
      return null == var2 ? null : (String)var2;
   }

   public String urlUtilComposeSearchUrl(String var1, String var2, String var3) {
      Object var4 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilComposeSearchUrl", new Class[]{String.class, String.class, String.class}, var1, var2, var3);
      return null == var4 ? null : (String)var4;
   }

   public byte[] urlUtilDecode(byte[] var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilDecode", new Class[]{String.class}, var1);
      return null == var2 ? null : (byte[])((byte[])var2);
   }

   public boolean urlUtilIsAssetUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsAssetUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsCookielessProxyUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsCookielessProxyUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsFileUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsFileUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsAboutUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsAboutUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsDataUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsDataUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsJavaScriptUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsJavaScriptUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsHttpUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsHttpUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsHttpsUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsHttpsUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsNetworkUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsNetworkUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsContentUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsContentUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public boolean urlUtilIsValidUrl(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilIsValidUrl", new Class[]{String.class}, var1);
      return null == var2 ? false : (Boolean)var2;
   }

   public String urlUtilStripAnchor(String var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilStripAnchor", new Class[]{String.class}, var1);
      return null == var2 ? null : (String)var2;
   }

   public String urlUtilGuessFileName(String var1, String var2, String var3) {
      Object var4 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "urlUtilGuessFileName", new Class[]{String.class, String.class, String.class}, var1, var2, var3);
      return null == var4 ? null : (String)var4;
   }

   public void clearAllCache(Context var1, boolean var2) {
      TbsLog.w("desktop", " tbsWizard clearAllX5Cache");
      if (var2) {
         this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "clearAllCache", new Class[]{Context.class}, var1);
      } else {
         try {
            this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "clearAllCache", new Class[]{Context.class, Boolean.TYPE}, var1, var2);
         } catch (Exception var6) {
            this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseClearUsernamePassword", new Class[]{Context.class}, var1);
            this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseClearHttpAuthUsernamePassword", new Class[]{Context.class}, var1);
            this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "webViewDatabaseClearFormData", new Class[]{Context.class}, var1);
            this.dexLoader.invokeStaticMethod("com.tencent.smtt.webkit.CacheManager", "removeAllCacheFiles", (Class[])null);
            this.dexLoader.invokeStaticMethod("com.tencent.smtt.webkit.CacheManager", "clearLocalStorage", (Class[])null);
            Object var4 = this.dexLoader.invokeStaticMethod("com.tencent.smtt.net.http.DnsManager", "getInstance", (Class[])null);
            if (var4 != null) {
               this.dexLoader.invokeMethod(var4, "com.tencent.smtt.net.http.DnsManager", "removeAllDns", (Class[])null);
            }

            Object var5 = this.dexLoader.invokeStaticMethod("com.tencent.smtt.webkit.SmttPermanentPermissions", "getInstance", (Class[])null);
            if (var5 != null) {
               this.dexLoader.invokeMethod(var5, "com.tencent.smtt.webkit.SmttPermanentPermissions", "clearAllPermanentPermission", (Class[])null);
            }

            this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "removeAllIcons", (Class[])null);
         }
      }

   }

   public int startMiniQB(Context var1, String var2, Map<String, String> var3, String var4, android.webkit.ValueCallback<String> var5) {
      if (TbsDownloader.getOverSea(var1)) {
         return -103;
      } else {
         Object var6;
         if (var4 == null) {
            var6 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "startMiniQB", new Class[]{Context.class, String.class, Map.class, android.webkit.ValueCallback.class}, var1, var2, var3, var5);
            if (var6 == null) {
               var6 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "startMiniQB", new Class[]{Context.class, String.class, Map.class}, var1, var2, var3);
            }

            if (var6 == null) {
               var6 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "startMiniQB", new Class[]{Context.class, String.class}, var1, var2);
            }

            return null == var6 ? -104 : (Integer)var6;
         } else {
            var6 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "startMiniQB", new Class[]{Context.class, String.class, String.class}, var1, var2, var4);
            return null == var6 ? -104 : (Integer)var6;
         }
      }
   }

   public boolean canOpenFile(Context var1, String var2) {
      Object var3 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "canOpenFile", new Class[]{Context.class, String.class}, var1, var2);
      return var3 instanceof Boolean ? (Boolean)var3 : false;
   }

   public void closeFileReader() {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "closeFileReader", new Class[0]);
   }

   public String getDefaultUserAgent(Context var1) {
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "getDefaultUserAgent", new Class[]{Context.class}, var1);
      return var2 instanceof String ? (String)var2 : null;
   }

   public IX5CoreServiceWorkerController getServiceWorkerController() {
      Object var1 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "getServiceWorkerController", new Class[0]);
      return var1 instanceof IX5CoreServiceWorkerController ? (IX5CoreServiceWorkerController)var1 : null;
   }
}
