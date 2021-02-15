package com.tencent.smtt.sdk;

import android.content.Context;

import com.tencent.smtt.utils.RandomAccessFileWrap;

import java.io.IOException;
import java.util.UnknownFormatConversionException;

public class TbsInstallerOATHelper {
   static int a = 5;
   static int b = 16;
   static char[] c;
   static String d;
   static long e;

   public static String getOatCommand(Context context, String path) throws Exception {
      RandomAccessFileWrap randomAccessFileWrap = new RandomAccessFileWrap(path);
      randomAccessFileWrap.read(c);
      randomAccessFileWrap.setSmallEnd(c[a] == 1);
      randomAccessFileWrap.seek(e);
      char[] var3 = getOatCommand(randomAccessFileWrap);
      return getOatCommand(new String(var3));
   }

   private static String getOatCommand(String var0) {
      String[] var1 = var0.split(new String("\u0000"));
      int var2 = 0;

      String var3;
      String var4;
      do {
         if (var2 >= var1.length) {
            return "";
         }

         var3 = var1[var2++];
         var4 = var1[var2++];
      } while(!var3.equals(d));

      return var4;
   }

   public static char[] getOatCommand(RandomAccessFileWrap randomAccessFileWrap) throws IOException {
      char[] var21 = new char[4];
      char[] var22 = new char[4];
      randomAccessFileWrap.read(var21);
      if (var21[0] == 'o' && var21[1] == 'a' && var21[2] == 't') {
         randomAccessFileWrap.read(var22);
         int var1 = randomAccessFileWrap.readInt();
         int var2 = randomAccessFileWrap.readInt();
         int var3 = randomAccessFileWrap.readInt();
         int var4 = randomAccessFileWrap.readInt();
         int var5 = randomAccessFileWrap.readInt();
         int var6 = randomAccessFileWrap.readInt();
         int var7 = randomAccessFileWrap.readInt();
         int var8 = randomAccessFileWrap.readInt();
         if (var22[1] <= '4') {
            int var9 = randomAccessFileWrap.readInt();
            int var10 = randomAccessFileWrap.readInt();
            int var11 = randomAccessFileWrap.readInt();
         }

         int var12 = randomAccessFileWrap.readInt();
         int var13 = randomAccessFileWrap.readInt();
         int var14 = randomAccessFileWrap.readInt();
         int var15 = randomAccessFileWrap.readInt();
         int var16 = randomAccessFileWrap.readInt();
         int var17 = randomAccessFileWrap.readInt();
         int var18 = randomAccessFileWrap.readInt();
         int var19 = randomAccessFileWrap.readInt();
         char[] var20 = new char[var19];
         randomAccessFileWrap.read(var20);
         return var20;
      } else {
         throw new UnknownFormatConversionException(String.format("Invalid art magic %c%c%c", var21[0], var21[1], var21[2]));
      }
   }

   static {
      c = new char[b];
      d = "dex2oat-cmdline";
      e = 4096L;
   }
}
