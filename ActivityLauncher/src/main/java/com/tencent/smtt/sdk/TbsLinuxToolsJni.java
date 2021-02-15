package com.tencent.smtt.sdk;

import android.content.Context;
import com.tencent.smtt.utils.TbsLog;
import java.io.File;

class TbsLinuxToolsJni {
   private static boolean a = false;
   private static boolean b = false;

   private native int ChmodInner(String var1, String var2);

   public int a(String var1, String var2) {
      if (!a) {
         TbsLog.e("TbsLinuxToolsJni", "jni not loaded!", true);
         return -1;
      } else {
         return this.ChmodInner(var1, var2);
      }
   }

   public TbsLinuxToolsJni(Context var1) {
      this.a(var1);
   }

   private void a(Context var1) {
      Class var2 = TbsLinuxToolsJni.class;
      synchronized(TbsLinuxToolsJni.class) {
         TbsLog.i("TbsLinuxToolsJni", "TbsLinuxToolsJni init mbIsInited is " + b);
         if (!b) {
            b = true;

            try {
               File var3 = null;
               if (TbsShareManager.isThirdPartyApp(var1)) {
                  String var4 = TbsShareManager.a();
                  if (var4 == null) {
                     var4 = TbsShareManager.c(var1);
                  }

                  var3 = new File(var4);
               } else {
                  var3 = TbsInstaller.a().getTbsCoreShareDir(var1);
               }

               if (null != var3) {
                  File var8 = new File(var3.getAbsolutePath() + File.separator + "liblinuxtoolsfortbssdk_jni.so");
                  if ((var8 == null || !var8.exists()) && !TbsShareManager.isThirdPartyApp(var1)) {
                     var3 = TbsInstaller.a().getTbsCoreShareDecoupleDir(var1);
                  }

                  if (null != var3) {
                     TbsLog.i("TbsLinuxToolsJni", "TbsLinuxToolsJni init tbsSharePath is " + var3.getAbsolutePath());
                     System.load(var3.getAbsolutePath() + File.separator + "liblinuxtoolsfortbssdk_jni.so");
                     a = true;
                  }
               }

               this.ChmodInner("/checkChmodeExists", "700");
            } catch (Throwable var6) {
               var6.printStackTrace();
               a = false;
               TbsLog.i("TbsLinuxToolsJni", "TbsLinuxToolsJni init error !!! " + var6.getMessage() + " ## " + var6.getCause());
            }

         }
      }
   }
}
