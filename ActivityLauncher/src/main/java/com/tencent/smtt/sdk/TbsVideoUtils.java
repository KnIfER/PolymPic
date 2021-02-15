package com.tencent.smtt.sdk;

import android.content.Context;
import com.tencent.smtt.export.external.DexLoader;

public class TbsVideoUtils {
   private static TbsVideoUtilsProxyHelper a = null;

   private static void a(Context var0) {
      Class var1 = TbsVideoUtils.class;
      synchronized(TbsVideoUtils.class) {
         if (a == null) {
            SDKEngine.getInstance(true).init(var0, false, false);
            TbsWizard var2 = SDKEngine.getInstance(true).a();
            DexLoader var3 = null;
            if (var2 != null) {
               var3 = var2.getDexLoader();
            }

            if (var3 != null) {
               a = new TbsVideoUtilsProxyHelper(var3);
            }
         }

      }
   }

   public static void deleteVideoCache(Context var0, String var1) {
      a(var0);
      if (a != null) {
         a.a(var0, var1);
      }

   }

   public static String getCurWDPDecodeType(Context var0) {
      a(var0);
      return a != null ? a.a(var0) : "";
   }
}
