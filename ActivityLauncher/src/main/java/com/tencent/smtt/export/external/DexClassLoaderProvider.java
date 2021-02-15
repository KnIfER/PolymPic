package com.tencent.smtt.export.external;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.util.Log;
import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

public class DexClassLoaderProvider extends DexClassLoader {
   private static final String LOGTAG = "dexloader";
   private static final String LAST_DEX_NAME = "tbs_jars_fusion_dex.jar";
   private static final long LOAD_DEX_DELAY = 3000L;
   private static final String IS_FIRST_LOAD_DEX_FLAG_FILE = "is_first_load_dex_flag_file";
   private DexClassLoaderProvider.SpeedyDexClassLoader mClassLoader = null;
   protected static DexClassLoader mClassLoaderOriginal = null;
   private static DexClassLoaderProvider mInstance = null;
   private static String mRealDexPath = null;
   private static boolean mForceLoadDexFlag = false;
   protected static Service mService = null;
   private static Context mContext = null;

   private DexClassLoaderProvider(String var1, String var2, String var3, ClassLoader var4, boolean var5) {
      super(var1, var2, var3, var4);
      if (var5) {
         Log.e("dexloader", "SpeedyDexClassLoader: " + mRealDexPath);
         this.mClassLoader = new DexClassLoaderProvider.SpeedyDexClassLoader(mRealDexPath, (File)null, var3, var4);
      } else {
         Log.e("dexloader", "DexClassLoader: " + mRealDexPath);
         this.mClassLoader = null;
      }

   }

   public static DexClassLoader createDexClassLoader(String var0, String var1, String var2, ClassLoader var3, Context var4) {
      Log.i("dexloader", "new DexClassLoaderDelegate: " + var0 + ", context: " + var4);
      mContext = var4.getApplicationContext();
      mRealDexPath = var0;
      int var5 = var0.lastIndexOf("/");
      String var6 = var0.substring(0, var5 + 1);
      var6 = var6 + "fake_dex.jar";
      String var7 = var0.substring(var5 + 1);
      if (supportSpeedyClassLoader() && is_first_load_tbs_dex(var1, var7)) {
         Log.d("dexloader", "new DexClassLoaderDelegate -- fake: " + var6);
         set_first_load_tbs_dex(var1, var7);
         mInstance = new DexClassLoaderProvider(var6, var1, var2, var3, true);
         doAsyncDexLoad(var7, var0, var1, var2, var3);
      } else {
         Log.d("dexloader", "new DexClassLoaderDelegate -- real: " + var0);
         mInstance = new DexClassLoaderProvider(var0, var1, var2, var3, false);
      }

      return mInstance;
   }

   private static boolean supportSpeedyClassLoader() {
      return VERSION.SDK_INT != 21 || DexLoader.mCanUseDexLoaderProviderService;
   }

   private static boolean shouldUseDexLoaderService() {
      if (mForceLoadDexFlag) {
         return false;
      } else {
         return DexLoader.mCanUseDexLoaderProviderService;
      }
   }

   private static void doAsyncDexLoad(final String var0, final String var1, final String var2, final String var3, final ClassLoader var4) {
      if (shouldUseDexLoaderService()) {
         (new Timer()).schedule(new TimerTask() {
            public void run() {
               try {
                  ArrayList var1x = new ArrayList(4);
                  var1x.add(0, var0);
                  var1x.add(1, var1);
                  var1x.add(2, var2);
                  var1x.add(3, var3);
                  Intent var2x = new Intent(DexClassLoaderProvider.mContext, DexClassLoaderProviderService.class);
                  var2x.putStringArrayListExtra("dex2oat", var1x);
                  DexClassLoaderProvider.mContext.startService(var2x);
                  Log.d("dexloader", "shouldUseDexLoaderService(" + var0 + ", " + var2x + ")");
               } catch (SecurityException var3x) {
                  Log.e("dexloader", "start DexLoaderService exception", var3x);
               } catch (Throwable var4) {
                  Log.e("dexloader", "after shouldUseDexLoaderService exception: " + var4);
               }

            }
         }, 3000L);
      } else {
         Log.d("dexloader", "Background real dex loading(" + var0 + ")");
         (new Timer()).schedule(new TimerTask() {
            public void run() {
               try {
                  boolean var1x = false;
                  String var2x = var1.replace(".jar", ".dex");
                  File var3x = new File(var2x);
                  if (var3x.exists() && var3x.length() != 0L) {
                     Log.d("dexloader", "" + var3x + " existed!");
                     var1x = true;
                  } else {
                     Log.d("dexloader", "" + var3x + " does not existed!");
                     var1x = false;
                  }

                  File var4x = new File(var2);
                  File var5 = new File(var1);
                  boolean var6 = var4x.exists();
                  boolean var7 = var4x.isDirectory();
                  boolean var8 = var5.exists();
                  if (!var6 || !var7 || !var8) {
                     Log.d("dexloader", "dex loading exception(" + var6 + ", " + var7 + ", " + var8 + ")");
                     return;
                  }

                  long var9 = System.currentTimeMillis();
                  new DexClassLoader(var1, var2, var3, var4);
                  long var12 = System.currentTimeMillis() - var9;
                  String var14 = String.format("load_dex completed -- cl_cost: %d, existed: %b", var12, var1x);
                  Log.d("dexloader", "" + var14);
                  if (DexClassLoaderProvider.mForceLoadDexFlag && "tbs_jars_fusion_dex.jar".equals(var0)) {
                     Log.d("dexloader", "Stop provider service after loading " + var0);
                     if (DexClassLoaderProvider.mService != null) {
                        Log.d("dexloader", "##Stop service##... ");
                        DexClassLoaderProvider.mService.stopSelf();
                     }
                  }
               } catch (Throwable var15) {
                  Log.e("dexloader", "@AsyncDexLoad task exception: " + var15);
               }

            }
         }, 3000L);
      }
   }

   private static boolean is_first_load_tbs_dex(String var0, String var1) {
      if (mForceLoadDexFlag) {
         return true;
      } else {
         String var2 = var1 + "_" + "is_first_load_dex_flag_file";
         File var3 = new File(var0, var2);
         return !var3.exists();
      }
   }

   private static void set_first_load_tbs_dex(String var0, String var1) {
      String var2 = var1 + "_" + "is_first_load_dex_flag_file";
      File var3 = new File(var0, var2);
      if (!var3.exists()) {
         try {
            var3.createNewFile();
         } catch (Throwable var5) {
            var5.printStackTrace();
         }
      }

   }

   protected Class<?> findClass(String var1) throws ClassNotFoundException {
      return this.useSelfClassloader() ? super.findClass(var1) : this.mClassLoader.findClass(var1);
   }

   public String findLibrary(String var1) {
      return this.useSelfClassloader() ? super.findLibrary(var1) : this.mClassLoader.findLibrary(var1);
   }

   protected URL findResource(String var1) {
      return this.useSelfClassloader() ? super.findResource(var1) : this.mClassLoader.findResource(var1);
   }

   protected Enumeration<URL> findResources(String var1) {
      return this.useSelfClassloader() ? super.findResources(var1) : this.mClassLoader.findResources(var1);
   }

   protected synchronized Package getPackage(String var1) {
      return this.useSelfClassloader() ? super.getPackage(var1) : this.mClassLoader.getPackage(var1);
   }

   public String toString() {
      return this.useSelfClassloader() ? super.toString() : this.mClassLoader.toString();
   }

   public void clearAssertionStatus() {
      if (this.useSelfClassloader()) {
         super.clearAssertionStatus();
      } else {
         this.mClassLoader.clearAssertionStatus();
      }

   }

   protected Package definePackage(String var1, String var2, String var3, String var4, String var5, String var6, String var7, URL var8) throws IllegalArgumentException {
      return this.useSelfClassloader() ? super.definePackage(var1, var2, var3, var4, var5, var6, var7, var8) : this.mClassLoader.definePackage(var1, var2, var3, var4, var5, var6, var7, var8);
   }

   protected Package[] getPackages() {
      return this.useSelfClassloader() ? super.getPackages() : this.mClassLoader.getPackages();
   }

   public URL getResource(String var1) {
      return this.useSelfClassloader() ? super.getResource(var1) : this.mClassLoader.getResource(var1);
   }

   public InputStream getResourceAsStream(String var1) {
      return this.useSelfClassloader() ? this.getResourceAsStream(var1) : this.mClassLoader.getResourceAsStream(var1);
   }

   public Enumeration<URL> getResources(String var1) throws IOException {
      return this.useSelfClassloader() ? super.getResources(var1) : this.mClassLoader.getResources(var1);
   }

   protected Class<?> loadClass(String var1, boolean var2) throws ClassNotFoundException {
      return this.useSelfClassloader() ? super.loadClass(var1, var2) : this.mClassLoader.loadClass(var1, var2);
   }

   public Class<?> loadClass(String var1) throws ClassNotFoundException {
      return this.useSelfClassloader() ? super.loadClass(var1) : this.mClassLoader.loadClass(var1);
   }

   private boolean useSelfClassloader() {
      return this.mClassLoader == null;
   }

   public void setClassAssertionStatus(String var1, boolean var2) {
      if (this.useSelfClassloader()) {
         super.setClassAssertionStatus(var1, var2);
      } else {
         this.mClassLoader.setClassAssertionStatus(var1, var2);
      }

   }

   public void setDefaultAssertionStatus(boolean var1) {
      if (this.useSelfClassloader()) {
         super.setDefaultAssertionStatus(var1);
      } else {
         this.mClassLoader.setDefaultAssertionStatus(var1);
      }

   }

   public void setPackageAssertionStatus(String var1, boolean var2) {
      if (this.useSelfClassloader()) {
         super.setPackageAssertionStatus(var1, var2);
      } else {
         this.mClassLoader.setPackageAssertionStatus(var1, var2);
      }

   }

   static void setForceLoadDexFlag(boolean var0, Service var1) {
      mForceLoadDexFlag = var0;
      mService = var1;
   }

   private static class SpeedyDexClassLoader extends BaseDexClassLoader {
      public SpeedyDexClassLoader(String var1, File var2, String var3, ClassLoader var4) {
         super(var1, (File)null, var3, var4);
      }

      public Class<?> findClass(String var1) throws ClassNotFoundException {
         return super.findClass(var1);
      }

      public URL findResource(String var1) {
         return super.findResource(var1);
      }

      public Enumeration<URL> findResources(String var1) {
         return super.findResources(var1);
      }

      public synchronized Package getPackage(String var1) {
         return super.getPackage(var1);
      }

      public Package definePackage(String var1, String var2, String var3, String var4, String var5, String var6, String var7, URL var8) throws IllegalArgumentException {
         return super.definePackage(var1, var2, var3, var4, var5, var6, var7, var8);
      }

      public Package[] getPackages() {
         return super.getPackages();
      }

      public Class<?> loadClass(String var1, boolean var2) throws ClassNotFoundException {
         return super.loadClass(var1, var2);
      }
   }
}
