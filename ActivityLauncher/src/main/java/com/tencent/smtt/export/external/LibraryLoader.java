package com.tencent.smtt.export.external;

import android.content.Context;
import android.os.Build.VERSION;
import java.io.File;
import java.util.ArrayList;

public class LibraryLoader {
   private static String[] sLibrarySearchPaths = null;

   public static String[] getLibrarySearchPaths(Context var0) {
      if (sLibrarySearchPaths != null) {
         return sLibrarySearchPaths;
      } else if (var0 == null) {
         String[] var3 = new String[]{"/system/lib"};
         return var3;
      } else {
         ArrayList var1 = new ArrayList();
         var1.add(getNativeLibraryDir(var0));
         var1.add("/system/lib");
         String[] var2 = new String[var1.size()];
         var1.toArray(var2);
         sLibrarySearchPaths = var2;
         return sLibrarySearchPaths;
      }
   }

   public static String getNativeLibraryDir(Context var0) {
      int var1 = VERSION.SDK_INT;
      if (var1 >= 9) {
         return var0.getApplicationInfo().nativeLibraryDir;
      } else {
         return var1 >= 4 ? var0.getApplicationInfo().dataDir + "/lib" : "/data/data/" + var0.getPackageName() + "/lib";
      }
   }

   public static void loadLibrary(Context var0, String var1) throws UnsatisfiedLinkError {
      String[] var2 = getLibrarySearchPaths(var0);
      String var3 = System.mapLibraryName(var1);
      String[] var4 = var2;
      int var5 = var2.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         String var7 = var4[var6];
         var7 = var7 + "/" + var3;
         if ((new File(var7)).exists()) {
            try {
               System.load(var7);
            } catch (Exception var9) {
               var9.printStackTrace();
            }

            return;
         }
      }

      try {
         System.loadLibrary(var1);
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }
}
