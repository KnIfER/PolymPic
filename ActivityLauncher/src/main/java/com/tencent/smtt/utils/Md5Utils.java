package com.tencent.smtt.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
   public static String getMD5(String var0) {
      Object var1 = null;
      if (var0 == null) {
         return null;
      } else {
         try {
            byte[] var2 = var0.getBytes();
            MessageDigest var3 = MessageDigest.getInstance("MD5");
            var3.update(var2);
            return ByteUtils.a(var3.digest());
         } catch (Exception var4) {
            return (String)var1;
         }
      }
   }

   public static byte[] getMD5(byte[] var0) {
      try {
         MessageDigest var1 = MessageDigest.getInstance("MD5");
         var1.update(var0);
         return var1.digest();
      } catch (Exception var2) {
         return null;
      }
   }

   public static String getMD5(File var0) {
      FileInputStream var1 = null;

      Object var3;
      try {
         MessageDigest var2 = null;

         try {
            var2 = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException var18) {
            var18.printStackTrace();
         }

         var1 = new FileInputStream(var0);
         byte[] var22 = new byte[8192];

         int var4;
         while((var4 = var1.read(var22)) != -1) {
            var2.update(var22, 0, var4);
         }

         String var5 = ByteUtils.a(var2.digest());
         return var5;
      } catch (FileNotFoundException var19) {
         var3 = null;
      } catch (IOException var20) {
         var3 = null;
         return (String)var3;
      } finally {
         try {
            if (var1 != null) {
               var1.close();
            }
         } catch (IOException var17) {
            var17.printStackTrace();
         }

      }

      return (String)var3;
   }

   public static byte[] getMD5(InputStream var0) {
      byte[] var1 = null;
      if (var0 != null) {
         try {
            MessageDigest var2 = null;
            var2 = MessageDigest.getInstance("MD5");
            if (var2 != null) {
               byte[] var3 = new byte[8192];

               int var4;
               while((var4 = var0.read(var3)) != -1) {
                  var2.update(var3, 0, var4);
               }

               var1 = var2.digest();
            }
         } catch (Throwable var5) {
            var1 = null;
         }
      }

      return var1;
   }
}
