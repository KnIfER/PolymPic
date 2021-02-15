package com.tencent.smtt.sdk;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MemInfoHelper {
   private static int b = 0;
   public static int a = 600;

   public static int getTotalMemory() {
      if (b > 0) {
         return b;
      } else {
         int var0 = 0;
         String path = "/proc/meminfo";
         String var2 = "";
         boolean var3 = true;

         try(BufferedReader bufferedReader
					 = new BufferedReader(new FileReader(path), 8192)) {
            while((var2 = bufferedReader.readLine()) != null) {
               int var20 = var2.indexOf("MemTotal:");
               if (-1 != var20) {
                  String var6 = var2.substring(var20 + "MemTotal:".length()).trim();
                  if (null != var6 && var6.length() != 0 && var6.contains("k")) {
                     var0 = Integer.parseInt(var6.substring(0, var6.indexOf("k")).trim()) / 1024;
                  }
                  break;
               }
            }
         } catch (IOException e) {
            e.printStackTrace();
         }

         b = var0;
         return b;
      }
   }
}
