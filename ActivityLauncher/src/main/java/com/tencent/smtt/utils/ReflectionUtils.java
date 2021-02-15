package com.tencent.smtt.utils;

import android.os.Build.VERSION;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

public class ReflectionUtils {
   public static Object a(String var0, String var1) {
      try {
         Class var2 = Class.forName(var0);
         Method var3 = var2.getMethod(var1);
         return var3.invoke((Object)null);
      } catch (Throwable var4) {
         TbsLog.addLog(997, String.valueOf(var4));
         return null;
      }
   }

   public static Object a(Class<?> var0, String var1, Class<?>[] var2, Object... var3) {
      try {
         Method var4 = var0.getMethod(var1, var2);
         var4.setAccessible(true);
         return var4.invoke((Object)null, var3);
      } catch (Throwable var5) {
         TbsLog.addLog(997, String.valueOf(var5));
         var5.printStackTrace();
         return null;
      }
   }

   public static Object a(Object var0, String var1) {
      return a((Object)var0, var1, (Class[])null);
   }

   public static Object invokeInstance(Object var0, String var1, Class<?>[] var2, Object... var3) {
      if (var0 == null) {
         return null;
      } else {
         StringWriter var5;
         try {
            Class var4 = var0.getClass();
            var5 = null;
            Method var8;
            if (VERSION.SDK_INT > 10) {
               var8 = var4.getMethod(var1, var2);
            } else {
               var8 = var4.getDeclaredMethod(var1, var2);
            }

            var8.setAccessible(true);
            return var8.invoke(var0, var3.length == 0 ? null : var3);
         } catch (Throwable var6) {
            TbsLog.addLog(997, String.valueOf(var6));
            if (var6.getCause() != null && var6.getCause().toString().contains("AuthenticationFail")) {
               String var7 = new String("AuthenticationFail");
               return var7;
            } else if (var1 == null || !var1.equalsIgnoreCase("canLoadX5Core") && !var1.equalsIgnoreCase("initTesRuntimeEnvironment")) {
               var5 = new StringWriter();
               var6.printStackTrace(new PrintWriter(var5));
               TbsLog.i("ReflectionUtils", "invokeInstance -- exceptions:" + var5.toString());
               return null;
            } else {
               return null;
            }
         }
      }
   }

   public static Method a(Object var0, String var1, Class<?>... var2) {
      Method var3 = null;
      Class var4 = var0.getClass();

      while(var4 != Object.class) {
         try {
            if (var4 == null) {
               return null;
            }

            var3 = var4.getDeclaredMethod(var1, var2);
            return var3;
         } catch (Exception var6) {
            var4 = var4.getSuperclass();
         }
      }

      return null;
   }
}
