package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import com.tencent.smtt.export.external.DexLoader;
import com.tencent.smtt.utils.ReflectionUtils;
import com.tencent.smtt.utils.TbsLog;
import java.io.File;
import java.io.IOException;

public class TbsExtensionFunctionManager {
   public static final String SP_NAME_FOR_COOKIE = "cookie_compatiable";
   public static final String SP_KEY_COOKIE_DB_VERSION = "cookie_db_version";
   public static final String USEX5_FILE_NAME = "usex5.txt";
   public static final String BUGLY_SWITCH_FILE_NAME = "bugly_switch.txt";
   public static final String COOKIE_SWITCH_FILE_NAME = "cookie_switch.txt";
   public static final String DISABLE_GET_APK_VERSION_SWITCH_FILE_NAME = "disable_get_apk_version_switch.txt";
   public static final String DISABLE_UNPREINIT = "disable_unpreinit.txt";
   public static final String DISABLE_USE_HOST_BACKUP_CORE = "disable_use_host_backup_core.txt";
   public static final int SWITCH_BYTE_COOKIE = 1;
   public static final int SWITCH_BYTE_DISABLE_GET_APK_VERSION = 2;
   public static final int SWITCH_BYTE_DISABLE_UNPREINIT = 4;
   public static final int SWITCH_BYTE_DISABLE_USE_HOST_BACKUPCORE = 8;
   private boolean a;
   private static TbsExtensionFunctionManager b;

   private TbsExtensionFunctionManager() {
   }

   public static TbsExtensionFunctionManager getInstance() {
      if (b == null) {
         Class var0 = TbsExtensionFunctionManager.class;
         synchronized(TbsExtensionFunctionManager.class) {
            if (b == null) {
               b = new TbsExtensionFunctionManager();
            }
         }
      }

      return b;
   }

   public synchronized void initTbsBuglyIfNeed(Context var1) {
      if (!this.a) {
         if (!this.canUseFunction(var1, "bugly_switch.txt")) {
            TbsLog.i("TbsExtensionFunMana", "bugly is forbiden!!");
         } else {
            String var2 = "";
            File var3;
            if (TbsShareManager.isThirdPartyApp(var1)) {
               var2 = TbsShareManager.c(var1);
            } else {
               var3 = TbsInstaller.a().getTbsCoreShareDir(var1);
               if (var3 == null) {
                  TbsLog.i("TbsExtensionFunMana", "getTbsCoreShareDir is null");
               }

               if (var3.listFiles() == null || var3.listFiles().length <= 0) {
                  TbsLog.i("TbsExtensionFunMana", "getTbsCoreShareDir is empty!");
                  return;
               }

               var2 = var3.getAbsolutePath();
            }

            if (TextUtils.isEmpty(var2)) {
               TbsLog.i("TbsExtensionFunMana", "bugly init ,corePath is null");
            } else {
               var3 = TbsInstaller.a().getTbsCoreShareDir(var1);
               if (var3 == null) {
                  TbsLog.i("TbsExtensionFunMana", "bugly init ,optDir is null");
               } else {
                  File var4 = new File(var2, "tbs_bugly_dex.jar");

                  try {
                     DexLoader var5 = new DexLoader(var4.getParent(), var1, new String[]{var4.getAbsolutePath()}, var3.getAbsolutePath(), QbSdk.getSettings());
                     Class var6 = var5.loadClass("com.tencent.smtt.tbs.bugly.TBSBuglyManager");
                     ReflectionUtils.a(var6, "initBugly", new Class[]{Context.class, String.class, String.class, String.class}, var1, var2, String.valueOf(WebView.getTbsSDKVersion(var1)), String.valueOf(WebView.getTbsCoreVersion(var1)));
                  } catch (Throwable var7) {
                     TbsLog.i("TbsExtensionFunMana", "bugly init ,try init bugly failed(need new core):" + Log.getStackTraceString(var7));
                     return;
                  }

                  this.a = true;
                  TbsLog.i("TbsExtensionFunMana", "initTbsBuglyIfNeed success!");
               }
            }
         }
      }
   }

   public synchronized boolean setFunctionEnable(Context var1, String var2, boolean var3) {
      if (var1 == null) {
         return false;
      } else {
         File var4 = new File(var1.getFilesDir(), var2);
         if (var4 == null) {
            TbsLog.e("TbsExtensionFunMana", "setFunctionEnable," + var2 + " is null!");
            return false;
         } else {
            if (var3) {
               if (!var4.exists()) {
                  try {
                     if (var4.createNewFile()) {
                        return true;
                     }
                  } catch (IOException var6) {
                     TbsLog.e("TbsExtensionFunMana", "setFunctionEnable,createNewFile fail:" + var2);
                     var6.printStackTrace();
                     return false;
                  }
               }
            } else if (var4.exists()) {
               if (var4.delete()) {
                  return true;
               }

               TbsLog.e("TbsExtensionFunMana", "setFunctionEnable,file.delete fail:" + var2);
               return false;
            }

            return true;
         }
      }
   }

   public synchronized boolean canUseFunction(Context var1, String var2) {
      File var3 = new File(var1.getFilesDir(), var2);
      if (var3 == null) {
         TbsLog.i("TbsExtensionFunMana", "canUseFunction," + var2 + " is null!");
         return false;
      } else {
         return var3.exists() && var3.isFile();
      }
   }

   public synchronized int getRomCookieDBVersion(Context var1) {
      SharedPreferences var2;
      if (VERSION.SDK_INT >= 11) {
         var2 = var1.getSharedPreferences("cookie_compatiable", 4);
      } else {
         var2 = var1.getSharedPreferences("cookie_compatiable", 0);
      }

      return var2 == null ? -1 : var2.getInt("cookie_db_version", -1);
   }
}
