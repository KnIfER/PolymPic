package com.tencent.smtt.sdk;

import android.content.Context;
import com.tencent.smtt.export.external.DexLoader;
import java.lang.reflect.Field;

/** @deprecated */
@Deprecated
public class CookieSyncManager {
   private static android.webkit.CookieSyncManager a;
   private static CookieSyncManager b;
   private static boolean c = false;

   public static synchronized CookieSyncManager createInstance(Context var0) {
      a = android.webkit.CookieSyncManager.createInstance(var0);
      if (b == null || !c) {
         b = new CookieSyncManager(var0.getApplicationContext());
      }

      return b;
   }

   public static synchronized CookieSyncManager getInstance() {
      if (b == null) {
         throw new IllegalStateException("CookieSyncManager::createInstance() needs to be called before CookieSyncManager::getInstance()");
      } else {
         return b;
      }
   }

   private CookieSyncManager(Context var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         DexLoader var3 = var2.getWVWizardBase().getDexLoader();
         var3.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieSyncManager_createInstance", new Class[]{Context.class}, var1);
         c = true;
      }

   }

   public void sync() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();
         var2.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieSyncManager_Sync", new Class[0]);
      } else {
         a.sync();
      }

   }

   public void stopSync() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();
         var2.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieSyncManager_stopSync", new Class[0]);
      } else {
         a.stopSync();
      }

   }

   public void startSync() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var6 = var1.getWVWizardBase().getDexLoader();
         var6.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "cookieSyncManager_startSync", new Class[0]);
      } else {
         a.startSync();

         try {
            Class aClass = Class.forName("android.webkit.WebSyncManager");
            Field field = aClass.getDeclaredField("mSyncThread");
            field.setAccessible(true);
            Thread thread = (Thread)field.get(a);
            thread.setUncaughtExceptionHandler(new SQLExemptExceptionHandler());
         } catch (Exception var5) {
         }
      }

   }
}
