package com.tencent.smtt.utils;

import android.content.Context;
import android.os.Looper;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import com.tencent.smtt.sdk.QbSdk;
import java.io.File;
import java.io.FileFilter;

public class TbsCheckUtils {
   private static File getCorShareDir_inTbsFolder(Context var0) {
      File var1 = QbSdk.getTbsFolderDir(var0);
      File var2 = new File(var1, "core_share");
      if (var2 != null) {
         return var2.isDirectory() ? var2 : null;
      } else {
         return null;
      }
   }

   public static boolean a(Context var0) {
      boolean var1 = true;
      if (Looper.getMainLooper() != Looper.myLooper()) {
         var1 = checkTbsValidity(var0);
      }

      return var1;
   }

   public static boolean checkTbsValidity(Context context) {
      try {
         if (VERSION.SDK_INT < 21 || VERSION.SDK_INT > 25) {
            return true;
         }

         File shareDir_inTbsFolder = getCorShareDir_inTbsFolder(context);
         if (shareDir_inTbsFolder == null) {
            return true;
         }

         File[] listFiles = shareDir_inTbsFolder.listFiles(new FileFilter() {
            public boolean accept(File var1) {
               String var2 = var1.getName();
               boolean var3 = !TextUtils.isEmpty(var2) && var2.endsWith(".dex");
               return var3;
            }
         });
         File[] var3 = listFiles;
         int var4 = listFiles.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            File var6 = var3[var5];
            if (var6.isFile() && var6.exists()) {
               boolean var7 = isOatFileBroken(var6);
               if (var7) {
                  TbsLog.w("TbsCheckUtils", "" + var6 + " is invalid --> check failed!");
                  var6.delete();
                  return false;
               }

               TbsLog.i("TbsCheckUtils", "" + var6 + " #4 check success!");
            }
         }
      } catch (Throwable var8) {
         var8.printStackTrace();
      }

      TbsLog.i("TbsCheckUtils", "checkTbsValidity -->#5 check ok!");
      return true;
   }

   private static boolean isOatFileBroken(File var0) {
      try {
         if (!ELFUtils.checkElfFile(var0)) {
            return true;
         }
      } catch (Throwable var2) {
         Log.e("TbsCheckUtils", "isOatFileBroken exception: " + var2);
      }

      return false;
   }
}
