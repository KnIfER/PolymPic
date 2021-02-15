package com.tencent.smtt.export.external;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import com.tencent.smtt.utils.TBSFileLockUtil;
import dalvik.system.DexClassLoader;
//import dalvik.system.VMStack;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

public class DexLoader {
   private static final String JAVACORE_PACKAGE_PREFIX = "org.chromium";
   private static final String TENCENT_PACKAGE_PREFIX = "com.tencent";
   private static final String TAF_PACKAGE_PREFIX = "com.taf";
   private static final String TAG = "DexLoader";
   private static final String TBS_FUSION_DEX = "tbs_jars_fusion_dex";
   private static final String TBS_WEBVIEW_DEX = "webview_dex";
   private static boolean mUseTbsCorePrivateClassLoader = false;
   private static boolean mUseSpeedyClassLoader = false;
   static boolean mCanUseDexLoaderProviderService = true;
   private static boolean mMttClassUseCorePrivate = false;
   private DexClassLoader mClassLoader;

   public static void initTbsSettings(Map<String, Object> var0) {
      Log.d("DexLoader", "initTbsSettings - " + var0);
      if (var0 != null) {
         try {
            Object var1 = var0.get("use_private_classloader");
            if (var1 instanceof Boolean) {
               mUseTbsCorePrivateClassLoader = (Boolean)var1;
            }

            var1 = var0.get("use_speedy_classloader");
            if (var1 instanceof Boolean) {
               mUseSpeedyClassLoader = (Boolean)var1;
            }

            var1 = var0.get("use_dexloader_service");
            if (var1 instanceof Boolean) {
               mCanUseDexLoaderProviderService = (Boolean)var1;
            }

            var1 = var0.get("use_mtt_classes");
            if (var1 instanceof Boolean) {
               mMttClassUseCorePrivate = (Boolean)var1;
            }
         } catch (Throwable var2) {
            var2.printStackTrace();
         }
      }

   }

   private boolean shouldUseTbsCorePrivateClassLoader(String var1) {
      if (!mUseTbsCorePrivateClassLoader) {
         return false;
      } else {
         return var1.contains("tbs_jars_fusion_dex") || var1.contains("webview_dex");
      }
   }

   public DexLoader(String var1, Context var2, String[] var3, String var4) {
      this(var1, var2, var3, var4, (Map)null);
   }

   public DexLoader(String var1, Context var2, String[] var3, String var4, Map<String, Object> var5) {
      initTbsSettings(var5);
      Object var6 = null;
     // Object var6 = VMStack.getCallingClassLoader();
      if (var6 == null) {
         var6 = var2.getClassLoader();
      }

      Log.d("dexloader", "Set base classLoader for DexClassLoader: " + var6);

      for(int var7 = 0; var7 < var3.length; ++var7) {
         var6 = this.mClassLoader = this.createDexClassLoader(var3[var7], var4, var1, (ClassLoader)var6, var2);
      }

   }

   public DexLoader(Context var1, String[] var2, String var3) {
      this((String)null, (Context)var1, (String[])var2, (String)var3);
   }

   public DexLoader(Context var1, String[] var2, String var3, String var4) {
      Object var5 = var1.getClassLoader();
      String var6 = var1.getApplicationInfo().nativeLibraryDir;
      if (!TextUtils.isEmpty(var4)) {
         var6 = var6 + File.pathSeparator + var4;
      }

      for(int var7 = 0; var7 < var2.length; ++var7) {
         var5 = this.mClassLoader = this.createDexClassLoader(var2[var7], var3, var6, (ClassLoader)var5, var1);
      }

   }

   public DexLoader(Context var1, String[] var2, String var3, DexLoader var4) {
      DexClassLoader var5 = var4.getClassLoader();

      for(int var6 = 0; var6 < var2.length; ++var6) {
         var5 = this.mClassLoader = this.createDexClassLoader(var2[var6], var3, var1.getApplicationInfo().nativeLibraryDir, var5, var1);
      }

   }

   public DexLoader(Context var1, String var2, String var3) {
      this(var1, new String[]{var2}, var3);
   }

   private DexClassLoader createDexClassLoader(String var1, String var2, String var3, ClassLoader var4, Context var5) {
      String var6;
      if (VERSION.SDK_INT >= 29) {
         var6 = var1 + "_code";
         String var7 = var1 + "_name";
         String var8 = var1 + "_display";
         SharedPreferences var9 = var5.getSharedPreferences("tbs_oat_status", 0);
         File var10 = new File(var1);
         File var11 = new File(var5.getDir("tbs", 0), "core_private");
         TBSFileLockUtil var12 = null;

         try {
            int var13 = var9.getInt(var6, -1);
            String var14 = var9.getString(var7, "");
            String var15 = var9.getString(var8, "");
            String var16 = var5.getPackageName();
            PackageManager var17 = var5.getPackageManager();
            PackageInfo var18 = var17.getPackageInfo(var16, 0);
            int var19 = var18.versionCode;
            String var20 = var18.versionName;
            String var21 = Build.DISPLAY;
            Log.i("DexLoader", "createDexClassLoader,old VerisonCode=" + var14 + ";newVersionCode=" + var19 + "oldVersionName" + var14 + ";newVersionName+" + var20 + "oldDisplay" + var15 + ";newDisplay=" + var21);
            if (var19 != var13 || !var20.equals(var14) || !var21.equals(var15)) {
               Log.e("DexLoader", "version updated!,clear oat file");
               var12 = new TBSFileLockUtil(var11, var10.getName() + "_loading.lock");
               var12.b();
               var13 = var9.getInt(var6, -1);
               var14 = var9.getString(var7, "");
               var15 = var9.getString(var8, "");
               if (var19 != var13 || !var20.equals(var14) || !var21.equals(var15)) {
                  File var22 = new File(var10.getParent(), "oat");
                  String var23 = getFileNameNoEx(var10.getName());
                  File var24 = new File(var22, var10.getName() + ".prof");
                  File var25 = new File(var22, var10.getName() + ".cur.prof");
                  File var26 = new File(var22, "arm");
                  File var27 = new File(var26, var23 + ".odex");
                  File var28 = new File(var26, var23 + ".vdex");
                  delete(var24);
                  delete(var25);
                  delete(var27);
                  delete(var28);
                  Log.i("DexLoader", "delete file:" + var24 + var25 + var27 + var28);
                  Editor var29 = var9.edit();
                  var29.putString(var7, var20);
                  var29.putInt(var6, var19);
                  var29.putString(var8, var21);
                  var29.commit();
               }
            }
         } catch (Exception var35) {
            var35.printStackTrace();
         } finally {
            if (var12 != null) {
               var12.e();
            }

         }
      }

      var6 = null;
      Log.d("dexloader", "createDexClassLoader: " + var1);
      Object var37;
      if (this.shouldUseTbsCorePrivateClassLoader(var1)) {
         var37 = new DexLoader.TbsCorePrivateClassLoader(var1, var2, var3, var4);
      } else if (VERSION.SDK_INT >= 21 && VERSION.SDK_INT <= 25 && mUseSpeedyClassLoader) {
         Log.d("dexloader", "async odex...DexClassLoaderProvider.createDexClassLoader");

         try {
            var37 = DexClassLoaderProvider.createDexClassLoader(var1, var2, var3, var4, var5);
         } catch (Throwable var34) {
            Log.e("dexloader", "createDexClassLoader exception: " + var34);
            Log.d("dexloader", "sync odex...new DexClassLoader#2");
            var37 = new DexClassLoader(var1, var2, var3, var4);
         }
      } else {
         Log.d("dexloader", "sync odex...new DexClassLoader");
         var37 = new DexClassLoader(var1, var2, var3, var4);
      }

      Log.d("dexloader", "createDexClassLoader result: " + var37);
      return (DexClassLoader)var37;
   }

   public static String getFileNameNoEx(String var0) {
      if (var0 != null && var0.length() > 0) {
         int var1 = var0.lastIndexOf(46);
         if (var1 > -1 && var1 < var0.length()) {
            return var0.substring(0, var1);
         }
      }

      return var0;
   }

   public static void delete(File var0) {
      if (var0 != null && var0.exists()) {
         if (var0.isFile()) {
            var0.delete();
         } else {
            File[] var1 = var0.listFiles();
            if (var1 != null) {
               File[] var2 = var1;
               int var3 = var1.length;

               for(int var4 = 0; var4 < var3; ++var4) {
                  File var5 = var2[var4];
                  delete(var5);
               }

               var0.delete();
            }
         }
      }
   }

   public DexClassLoader getClassLoader() {
      return this.mClassLoader;
   }

   public Object newInstance(String var1) {
      try {
         return this.mClassLoader.loadClass(var1).newInstance();
      } catch (Throwable var3) {
         Log.e(this.getClass().getSimpleName(), "create " + var1 + " instance failed", var3);
         return null;
      }
   }

   public Object newInstance(String var1, Class<?>[] var2, Object... var3) {
      try {
         return this.mClassLoader.loadClass(var1).getConstructor(var2).newInstance(var3);
      } catch (Throwable var5) {
         if ("com.tencent.smtt.webkit.adapter.X5WebViewAdapter".equalsIgnoreCase(var1)) {
            Log.e(this.getClass().getSimpleName(), "'newInstance " + var1 + " failed", var5);
            return var5;
         } else {
            Log.e(this.getClass().getSimpleName(), "create '" + var1 + "' instance failed", var5);
            return null;
         }
      }
   }

   public Class<?> loadClass(String var1) {
      try {
         return this.mClassLoader.loadClass(var1);
      } catch (Throwable var3) {
         Log.e(this.getClass().getSimpleName(), "loadClass '" + var1 + "' failed", var3);
         return null;
      }
   }

   public Object invokeStaticMethod(String var1, String var2, Class<?>[] var3, Object... var4) {
      try {
         Method var5 = this.mClassLoader.loadClass(var1).getMethod(var2, var3);
         var5.setAccessible(true);
         return var5.invoke((Object)null, var4);
      } catch (Throwable var6) {
         if (var2 != null && var2.equalsIgnoreCase("initTesRuntimeEnvironment")) {
            Log.e(this.getClass().getSimpleName(), "'" + var1 + "' invoke static method '" + var2 + "' failed", var6);
            return var6;
         } else {
            Log.i(this.getClass().getSimpleName(), "'" + var1 + "' invoke static method '" + var2 + "' failed", var6);
            return null;
         }
      }
   }

   public Object invokeMethod(Object var1, String var2, String var3, Class<?>[] var4, Object... var5) {
      try {
         Method var6 = this.mClassLoader.loadClass(var2).getMethod(var3, var4);
         var6.setAccessible(true);
         return var6.invoke(var1, var5);
      } catch (Throwable var7) {
         Log.e(this.getClass().getSimpleName(), "'" + var2 + "' invoke method '" + var3 + "' failed", var7);
         return null;
      }
   }

   public Object getStaticField(String var1, String var2) {
      try {
         Field var3 = this.mClassLoader.loadClass(var1).getField(var2);
         var3.setAccessible(true);
         return var3.get((Object)null);
      } catch (Throwable var4) {
         Log.e(this.getClass().getSimpleName(), "'" + var1 + "' get field '" + var2 + "' failed", var4);
         return null;
      }
   }

   public void setStaticField(String var1, String var2, Object var3) {
      try {
         Field var4 = this.mClassLoader.loadClass(var1).getField(var2);
         var4.setAccessible(true);
         var4.set((Object)null, var3);
      } catch (Throwable var5) {
         Log.e(this.getClass().getSimpleName(), "'" + var1 + "' set field '" + var2 + "' failed", var5);
      }
   }

   private static class TbsCorePrivateClassLoader extends DexClassLoader {
      public TbsCorePrivateClassLoader(String var1, String var2, String var3, ClassLoader var4) {
         super(var1, var2, var3, var4);
      }

      protected Class<?> loadClass(String var1, boolean var2) throws ClassNotFoundException {
         if (var1 == null) {
            return super.loadClass(var1, var2);
         } else {
            boolean var3 = var1.startsWith("org.chromium");
            if (DexLoader.mMttClassUseCorePrivate) {
               var3 = var3 || var1.startsWith("com.tencent") || var1.startsWith("com.taf");
            }

            if (!var3) {
               return super.loadClass(var1, var2);
            } else {
               Class var4 = this.findLoadedClass(var1);
               if (var4 == null) {
                  try {
                     Log.d("DexLoader", "WebCoreClassLoader - loadClass(" + var1 + "," + var2 + ")...");
                     var4 = this.findClass(var1);
                  } catch (ClassNotFoundException var6) {
                  }

                  if (var4 == null) {
                     ClassLoader var5 = this.getParent();
                     if (var5 != null) {
                        var4 = var5.loadClass(var1);
                     }
                  }
               }

               return var4;
            }
         }
      }
   }
}
