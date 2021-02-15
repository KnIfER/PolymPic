package com.tencent.smtt.sdk;

import com.tencent.smtt.utils.ReflectionUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

/** @deprecated */
@Deprecated
public final class CacheManager {
   /** @deprecated */
   @Deprecated
   public static File getCacheFileBaseDir() {
      X5CoreEngine var0 = X5CoreEngine.getInstance();
      return null != var0 && var0.isInCharge() ? (File)var0.getWVWizardBase().getCachFileBaseDir() : (File) ReflectionUtils.a("android.webkit.CacheManager", "getCacheFileBaseDir");
   }

   /** @deprecated */
   @Deprecated
   public static boolean cacheDisabled() {
      X5CoreEngine var0 = X5CoreEngine.getInstance();
      if (null != var0 && var0.isInCharge()) {
         return (Boolean)var0.getWVWizardBase().cacheDisabled();
      } else {
         Object var1 = ReflectionUtils.a("android.webkit.CacheManager", "cacheDisabled");
         return var1 == null ? false : (Boolean)var1;
      }
   }

   /** @deprecated */
   public static Object getCacheFile(String var0, Map<String, String> var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         return var2.getWVWizardBase().getCachFileBaseDir();
      } else {
         try {
            return ReflectionUtils.a(Class.forName("android.webkit.CacheManager"), "getCacheFile", new Class[]{String.class, Map.class}, var0, var1);
         } catch (Exception var4) {
            return null;
         }
      }
   }

   public static InputStream getCacheFile(String var0, boolean var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      return null != var2 && var2.isInCharge() ? var2.getWVWizardBase().getCacheFile(var0, var1) : null;
   }
}
