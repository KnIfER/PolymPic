package com.tencent.smtt.utils;

public class ByteUtils {
   public static void Word2Byte(byte[] var0, int var1, short var2) {
      var0[var1] = (byte)(var2 >> 8);
      var0[var1 + 1] = (byte)var2;
   }

   public static String a(byte[] var0) {
      if (var0 != null && var0.length > 0) {
         StringBuffer var1 = new StringBuffer(var0.length * 2);

         for(int var2 = 0; var2 < var0.length; ++var2) {
            if ((var0[var2] & 255) < 16) {
               var1.append("0");
            }

            var1.append(Long.toString((long)(var0[var2] & 255), 16));
         }

         return var1.toString();
      } else {
         return null;
      }
   }

   public static byte[] subByte(byte[] var0, int var1, int var2) {
      int var3 = var0.length;
      if (var1 >= 0 && var1 + var2 <= var3) {
         if (var2 < 0) {
            var2 = var0.length - var1;
         }

         byte[] var4 = new byte[var2];

         for(int var5 = 0; var5 < var2; ++var5) {
            var4[var5] = var0[var5 + var1];
         }

         return var4;
      } else {
         return null;
      }
   }
}
