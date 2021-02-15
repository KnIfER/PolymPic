package com.tencent.smtt.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import com.tencent.smtt.sdk.QbSdk;
import java.io.File;

public class FsSpaceUtil {
   private static File a = null;

   public static long calcFsSpaceAvailable() {
      File dataDirectory = Environment.getDataDirectory();
      StatFs statFs = new StatFs(dataDirectory.getPath());
      long var2 = (long)statFs.getBlockSize();
      long var4 = (long)statFs.getAvailableBlocks();
      return var2 * var4;
   }

   @TargetApi(9)
   public static boolean a(Context context) {
      if (context == null) {
         return false;
      } else {
         if (a == null) {
            try {
               if (!context.getApplicationInfo().processName.contains("com.tencent.mm")) {
                  return false;
               }

               File file = QbSdk.getTbsFolderDir(context);
               if (file == null || !file.isDirectory()) {
                  return false;
               }

               File shareDir = new File(file, "share");
               if (shareDir != null) {
                  if (!shareDir.isDirectory()) {
                     boolean var3 = shareDir.mkdir();
                     if (!var3) {
                        return false;
                     }
                  }

                  a = shareDir;
                  shareDir.setExecutable(true, false);
                  return true;
               }
            } catch (Exception e) {
               e.printStackTrace();
               return false;
            }
         }

         return true;
      }
   }
}
