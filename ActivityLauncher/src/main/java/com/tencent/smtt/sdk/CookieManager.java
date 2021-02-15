package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.tencent.smtt.export.external.DexLoader;
import com.tencent.smtt.utils.ReflectionUtils;
import com.tencent.smtt.utils.TbsLog;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class CookieManager {
   public static String LOGTAG = "CookieManager";
   private static CookieManager cookieManager;
   CopyOnWriteArrayList<CookieItem> cookieItems;
   String b;
   CookieEnum c;
   private boolean e;
   private boolean f;

   private CookieManager() {
      this.c = CookieManager.CookieEnum.a;
      this.e = false;
      this.f = false;
   }

   public static CookieManager getInstance() {
      if (null == cookieManager) {
         Class var0 = CookieManager.class;
         synchronized(CookieManager.class) {
            if (null == cookieManager) {
               cookieManager = new CookieManager();
            }
         }
      }

      return cookieManager;
   }

   /** @deprecated */
   @Deprecated
   public void removeSessionCookie() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();
         var2.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_removeSessionCookie", new Class[0]);
      } else {
         android.webkit.CookieManager.getInstance().removeSessionCookie();
      }

   }

   public void removeSessionCookies(ValueCallback<Boolean> var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         DexLoader var3 = var2.getWVWizardBase().getDexLoader();
         var3.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_removeSessionCookies", new Class[]{android.webkit.ValueCallback.class}, var1);
      } else {
         if (VERSION.SDK_INT < 21) {
            return;
         }

         ReflectionUtils.invokeInstance((Object)android.webkit.CookieManager.getInstance(), "removeSessionCookies", new Class[]{android.webkit.ValueCallback.class}, var1);
      }

   }

   /** @deprecated */
   @Deprecated
   public void removeAllCookie() {
      if (this.cookieItems != null) {
         this.cookieItems.clear();
      }

      X5CoreEngine x5CoreEngine = X5CoreEngine.getInstance();
      if (null != x5CoreEngine && x5CoreEngine.isInCharge()) {
         x5CoreEngine.getWVWizardBase().cookieManager_removeAllCookie();
      } else {
         android.webkit.CookieManager.getInstance().removeAllCookie();
      }

   }

   public void removeAllCookies(ValueCallback<Boolean> var1) {
      if (this.cookieItems != null) {
         this.cookieItems.clear();
      }

      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         DexLoader var3 = var2.getWVWizardBase().getDexLoader();
         var3.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_removeAllCookies", new Class[]{android.webkit.ValueCallback.class}, var1);
      } else {
         if (VERSION.SDK_INT < 21) {
            return;
         }

         ReflectionUtils.invokeInstance((Object)android.webkit.CookieManager.getInstance(), "removeAllCookies", new Class[]{android.webkit.ValueCallback.class}, var1);
      }

   }

   public void flush() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();
         var2.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_flush", new Class[0]);
      } else {
         if (VERSION.SDK_INT < 21) {
            return;
         }

         ReflectionUtils.a((Object)android.webkit.CookieManager.getInstance(), "flush", new Class[0]);
      }

   }

   /** @deprecated */
   @Deprecated
   public void removeExpiredCookie() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();
         var2.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_removeExpiredCookie", new Class[0]);
      } else {
         android.webkit.CookieManager.getInstance().removeExpiredCookie();
      }

   }

   public synchronized void setAcceptCookie(boolean var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         DexLoader var3 = var2.getWVWizardBase().getDexLoader();
         var3.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_setAcceptCookie", new Class[]{Boolean.TYPE}, var1);
      } else {
         try {
            android.webkit.CookieManager.getInstance().setAcceptCookie(var1);
         } catch (Throwable var4) {
            var4.printStackTrace();
         }
      }

   }

   public synchronized void setAcceptThirdPartyCookies(WebView var1, boolean var2) {
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      if (null != var3 && var3.isInCharge()) {
         DexLoader var4 = var3.getWVWizardBase().getDexLoader();
         var4.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_setAcceptThirdPartyCookies", new Class[]{Object.class, Boolean.TYPE}, var1.getView(), var2);
      } else {
         if (VERSION.SDK_INT < 21) {
            return;
         }

         ReflectionUtils.invokeInstance((Object)android.webkit.CookieManager.getInstance(), "setAcceptThirdPartyCookies", new Class[]{android.webkit.WebView.class, Boolean.TYPE}, var1.getView(), var2);
      }

   }

   public synchronized boolean acceptThirdPartyCookies(WebView var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         DexLoader var5 = var2.getWVWizardBase().getDexLoader();
         Object var4 = var5.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_acceptThirdPartyCookies", new Class[]{Object.class}, var1.getView());
         return var4 != null ? (Boolean)var4 : true;
      } else if (VERSION.SDK_INT < 21) {
         return true;
      } else {
         Object var3 = ReflectionUtils.invokeInstance((Object)android.webkit.CookieManager.getInstance(), "acceptThirdPartyCookies", new Class[]{android.webkit.WebView.class}, var1.getView());
         return var3 != null ? (Boolean)var3 : false;
      }
   }

   public synchronized void setCookie(String var1, String var2) {
      this.setCookie(var1, var2, false);
   }

   public synchronized void setCookie(String key, String value, boolean var3) {
      X5CoreEngine x5CoreEngine = X5CoreEngine.getInstance();
      if (null != x5CoreEngine && x5CoreEngine.isInCharge()) {
         DexLoader dexLoader = x5CoreEngine.getWVWizardBase().getDexLoader();
         dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_setCookie", new Class[]{String.class, String.class}, key, value);
      } else {
         if (this.f || var3) {
            android.webkit.CookieManager.getInstance().setCookie(key, value);
         }

         if (!X5CoreEngine.getInstance().getInitialized()) {
            CookieItem var5 = new CookieItem();
            var5.type = 2;
            var5.key = key;
            var5.value = value;
            var5.booleanValueCallback = null;
            if (this.cookieItems == null) {
               this.cookieItems = new CopyOnWriteArrayList();
            }

            this.cookieItems.add(var5);
         }
      }

   }

   public synchronized void setCookie(String var1, String var2, ValueCallback<Boolean> var3) {
      X5CoreEngine var4 = X5CoreEngine.getInstance();
      if (null != var4 && var4.isInCharge()) {
         DexLoader var6 = var4.getWVWizardBase().getDexLoader();
         var6.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieManager_setCookie", new Class[]{String.class, String.class, android.webkit.ValueCallback.class}, var1, var2, var3);
      } else {
         if (!X5CoreEngine.getInstance().getInitialized()) {
            CookieItem var5 = new CookieItem();
            var5.type = 1;
            var5.key = var1;
            var5.value = var2;
            var5.booleanValueCallback = var3;
            if (this.cookieItems == null) {
               this.cookieItems = new CopyOnWriteArrayList();
            }

            this.cookieItems.add(var5);
         }

         if (this.f) {
            if (VERSION.SDK_INT < 21) {
               return;
            }

            ReflectionUtils.invokeInstance((Object)android.webkit.CookieManager.getInstance(), "setCookie", new Class[]{String.class, String.class, android.webkit.ValueCallback.class}, var1, var2, var3);
         }
      }

   }

   public boolean hasCookies() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().cookieManager_hasCookies() : android.webkit.CookieManager.getInstance().hasCookies();
   }

   public boolean acceptCookie() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().cookieManager_acceptCookie() : android.webkit.CookieManager.getInstance().acceptCookie();
   }

   public String getCookie(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         return var2.getWVWizardBase().getCookie(var1);
      } else {
         String var3 = null;

         try {
            var3 = android.webkit.CookieManager.getInstance().getCookie(var1);
         } catch (Throwable var5) {
            var5.printStackTrace();
         }

         return var3;
      }
   }

   public void setCookies(Map<String, String[]> var1) {
      boolean var2 = false;
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      if (null != var3 && var3.isInCharge()) {
         var2 = var3.getWVWizardBase().cookieManager_setCookies(var1);
      }

      if (!var2) {
         Iterator var4 = var1.keySet().iterator();

         while(var4.hasNext()) {
            String var5 = (String)var4.next();
            String[] var6 = (String[])var1.get(var5);
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               String var9 = var6[var8];
               this.setCookie(var5, var9);
            }
         }
      }

   }

   synchronized void a() {
      this.f = true;
      if (this.cookieItems != null && this.cookieItems.size() != 0) {
         X5CoreEngine x5CoreEngine = X5CoreEngine.getInstance();
         Iterator iterator;
         CookieItem cookieItem;
         if (null != x5CoreEngine && x5CoreEngine.isInCharge()) {
            iterator = this.cookieItems.iterator();

            while(iterator.hasNext()) {
               cookieItem = (CookieItem)iterator.next();
               switch(cookieItem.type) {
               case 1:
                  this.setCookie(cookieItem.key, cookieItem.value, cookieItem.booleanValueCallback);
                  break;
               case 2:
                  this.setCookie(cookieItem.key, cookieItem.value);
               }
            }
         } else {
            iterator = this.cookieItems.iterator();

            while(iterator.hasNext()) {
               cookieItem = (CookieItem)iterator.next();
               switch(cookieItem.type) {
               case 1:
                  if (VERSION.SDK_INT >= 21) {
                     ReflectionUtils.invokeInstance((Object)android.webkit.CookieManager.getInstance(), "setCookie", new Class[]{String.class, String.class, android.webkit.ValueCallback.class}, cookieItem.key, cookieItem.value, cookieItem.booleanValueCallback);
                  }
                  break;
               case 2:
                  android.webkit.CookieManager.getInstance().setCookie(cookieItem.key, cookieItem.value);
               }
            }
         }

         this.cookieItems.clear();
      }
   }

   public boolean setCookieCompatialbeMode(Context var1, CookieEnum var2, String var3, boolean var4) {
      long var5 = System.currentTimeMillis();
      if (var1 != null && TbsExtensionFunctionManager.getInstance().canUseFunction(var1, "cookie_switch.txt")) {
         this.c = var2;
         if (var3 != null) {
            this.b = var3;
         }

         if (this.c != CookieManager.CookieEnum.a && var4 && !X5CoreEngine.getInstance().getInitialized()) {
            X5CoreEngine.getInstance().init(var1);
         }

         return true;
      } else {
         return false;
      }
   }

   protected synchronized void a(Context var1, boolean var2, boolean var3) {
      if (this.c != CookieManager.CookieEnum.a && var1 != null && TbsExtensionFunctionManager.getInstance().canUseFunction(var1, "cookie_switch.txt") && !this.e) {
         long var4 = System.currentTimeMillis();
         long var6 = 0L;
         TbsLog.i(LOGTAG, "compatiableCookieDatabaseIfNeed,isX5Inited:" + var2 + ",useX5:" + var3);
         if (!var2 && !QbSdk.getIsSysWebViewForcedByOuter() && !QbSdk.forcedSysByInner) {
            X5CoreEngine var16 = X5CoreEngine.getInstance();
            var16.init(var1);
         } else {
            if (QbSdk.getIsSysWebViewForcedByOuter() || QbSdk.forcedSysByInner) {
               var3 = false;
            }

            boolean var8 = TbsExtensionFunctionManager.getInstance().canUseFunction(var1, "usex5.txt");
            TbsLog.i(LOGTAG, "usex5 : mUseX5LastProcess->" + var8 + ",useX5:" + var3);
            TbsExtensionFunctionManager.getInstance().setFunctionEnable(var1, "usex5.txt", var3);
            if (var8 != var3) {
               int var9 = 0;
               int var10 = 0;
               boolean var11 = false;
               TbsLogReport.TbsLogInfo var12 = TbsLogReport.getInstance(var1).tbsLogInfo();
               if (!TextUtils.isEmpty(this.b)) {
                  if (TbsInstaller.a().getTbsCoreInstalledVerInNolock(var1) > 0 && TbsInstaller.a().getTbsCoreInstalledVerInNolock(var1) < 36001) {
                     return;
                  }

                  if (var8) {
                     var9 = CookieManagerSqlHelper.getSqlVersion(var1);
                     if (var9 > 0) {
                        var10 = getROMCookieDBVersion(var1);
                        if (var10 <= 0) {
                           var11 = true;
                        }
                     }
                  } else {
                     var9 = CookieManagerSqlHelper.getSqlVersion(var1);
                     if (var9 > 0) {
                        String var13 = TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareConfig(var1, "cookies_database_version");
                        if (!TextUtils.isEmpty(var13)) {
                           try {
                              var10 = Integer.parseInt(var13);
                           } catch (Exception var15) {
                           }
                        }
                     }
                  }

                  if (!var11 && (var9 <= 0 || var10 <= 0)) {
                     var12.setErrorCode(702);
                  } else if (var10 >= var9) {
                     var12.setErrorCode(703);
                  } else {
                     CookieManagerSqlHelper.readCookiesFromDB(var1, this.c, this.b, var11, var3);
                     var12.setErrorCode(704);
                     var6 = System.currentTimeMillis() - var4;
                  }
               } else {
                  var12.setErrorCode(701);
               }

               var12.setFailDetail("x5->sys:" + var8 + " from:" + var9 + " to:" + var10 + ",timeused:" + var6);
               TbsLogReport.getInstance(var1).eventReport(TbsLogReport.EventType.TYPE_COOKIE_DB_SWITCH, var12);
            }
         }
      }
   }

   public static int getROMCookieDBVersion(Context var0) {
      SharedPreferences var1;
      if (VERSION.SDK_INT >= 11) {
         var1 = var0.getSharedPreferences("cookiedb_info", 4);
      } else {
         var1 = var0.getSharedPreferences("cookiedb_info", 0);
      }

      return var1.getInt("db_version", -1);
   }

   public static void setROMCookieDBVersion(Context var0, int var1) {
      SharedPreferences var2 = null;
      Editor var3 = null;
      if (VERSION.SDK_INT >= 11) {
         var2 = var0.getSharedPreferences("cookiedb_info", 4);
      } else {
         var2 = var0.getSharedPreferences("cookiedb_info", 0);
      }

      var3 = var2.edit();
      var3.putInt("db_version", var1);
      var3.commit();
   }

   class CookieItem {
      int type;
      String key;
      String value;
      ValueCallback<Boolean> booleanValueCallback;
   }

   public static enum CookieEnum {
      a,
      b,
      c;
   }
}
