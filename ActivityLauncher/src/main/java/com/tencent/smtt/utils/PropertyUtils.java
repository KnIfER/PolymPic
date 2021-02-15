package com.tencent.smtt.utils;

import android.text.TextUtils;
import java.lang.reflect.Method;

public class PropertyUtils {
   private static Class a;
   private static Method b;

   public static String getQuickly(String var0, String var1) {
      return TextUtils.isEmpty(var0) ? var1 : a(var0, var1);
   }

   private static String a(String var0, String var1) {
      if (a != null && b != null) {
         String var2 = var1;

         try {
            var2 = (String)b.invoke(a, var0, var1);
         } catch (Throwable var4) {
            var4.printStackTrace();
         }

         return var2;
      } else {
         return var1;
      }
   }

   static {
      try {
         a = Class.forName("android.os.SystemProperties");
         b = a.getDeclaredMethod("get", String.class, String.class);
      } catch (Throwable var1) {
         var1.printStackTrace();
      }

   }
}
