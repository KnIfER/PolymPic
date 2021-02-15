package com.tencent.smtt.utils;

import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LogFileUtils {
   private static OutputStream a = null;

   public static synchronized void writeDataToStorage(File var0, String var1, byte[] var2, String var3, boolean var4) {
      byte[] var5 = null;
      String var6 = null;
      byte[] var7 = encrypt(var1, var3);
      if (null != var7) {
         var5 = var7;
      } else {
         var6 = var3;
      }

      try {
         var0.getParentFile().mkdirs();
         if (var0.isFile() && var0.exists() && var0.length() > 2097152L) {
            var0.delete();
            var0.createNewFile();
         }

         if (a == null) {
            FileOutputStream var18 = new FileOutputStream(var0, var4);
            a = new BufferedOutputStream(var18);
         }

         if (var6 != null) {
            a.write(var6.getBytes());
         } else {
            a.write(var2);
            a.write(var5);
            a.write(new byte[]{10, 10});
         }
      } catch (Throwable var16) {
      } finally {
         if (a != null) {
            try {
               a.flush();
            } catch (Throwable var15) {
            }
         }

      }

   }

   public static void closeOutputStream(OutputStream var0) {
      try {
         if (var0 != null) {
            var0.close();
         }
      } catch (IOException var2) {
         Log.e("LOG_FILE", "Couldn't close stream!", var2);
      }

   }

   public static byte[] encrypt(String var0, String var1) {
      try {
         byte[] var3 = var1.getBytes("UTF-8");
         Cipher var4 = Cipher.getInstance("RC4");
         SecretKeySpec var5 = new SecretKeySpec(var0.getBytes("UTF-8"), "RC4");
         var4.init(1, var5);
         byte[] var2 = var4.update(var3);
         return var2;
      } catch (Throwable var6) {
         Log.e("LOG_FILE", "encrypt exception:" + var6.getMessage());
         return null;
      }
   }

   public static String createKey() {
      return String.valueOf(System.currentTimeMillis());
   }

   public static byte[] encryptKey(String var0, String var1) {
      try {
         byte[] var3 = var1.getBytes("UTF-8");
         Cipher var4 = Cipher.getInstance("RC4");
         SecretKeySpec var5 = new SecretKeySpec(var0.getBytes("UTF-8"), "RC4");
         var4.init(1, var5);
         byte[] var2 = var4.update(var3);
         return var2;
      } catch (Throwable var6) {
         Log.e("LOG_FILE", "encrypt exception:" + var6.getMessage());
         return null;
      }
   }

   public static byte[] createHeaderText(String var0, String var1) {
      try {
         byte[] var2 = encryptKey(var0, var1);
         String var3 = String.format("%03d", var2.length);
         byte[] var4 = new byte[var2.length + 3];
         var4[0] = (byte)var3.charAt(0);
         var4[1] = (byte)var3.charAt(1);
         var4[2] = (byte)var3.charAt(2);
         System.arraycopy(var2, 0, var4, 3, var2.length);
         return var4;
      } catch (Exception var5) {
         return null;
      }
   }
}
