package com.tencent.smtt.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class Apn {
   public static final int APN_UNKNOWN = 0;
   public static final int APN_2G = 1;
   public static final int APN_3G = 2;
   public static final int APN_WIFI = 3;
   public static final int APN_4G = 4;

   public static String getApnInfo(Context var0) {
      String var1 = "unknown";

      try {
         ConnectivityManager var2 = (ConnectivityManager)var0.getSystemService("connectivity");
         NetworkInfo var3 = var2.getActiveNetworkInfo();
         if (var3 != null && var3.isConnectedOrConnecting()) {
            switch(var3.getType()) {
            case 0:
               var1 = var3.getExtraInfo();
               break;
            case 1:
               var1 = "wifi";
            }
         }
      } catch (Exception var4) {
      }

      return var1;
   }

   public static int getApnType(Context var0) {
      byte var1 = 0;
      ConnectivityManager var2 = (ConnectivityManager)var0.getSystemService("connectivity");
      NetworkInfo var3 = var2.getActiveNetworkInfo();
      if (var3 != null && var3.isConnectedOrConnecting()) {
         switch(var3.getType()) {
         case 0:
            switch(var3.getSubtype()) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
               var1 = 1;
               return var1;
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
               var1 = 2;
               return var1;
            case 13:
               var1 = 4;
               return var1;
            default:
               var1 = 0;
               return var1;
            }
         case 1:
            var1 = 3;
            break;
         default:
            var1 = 0;
         }
      }

      return var1;
   }

   public static boolean isNetworkAvailable(Context var0) {
      ConnectivityManager var1 = (ConnectivityManager)var0.getSystemService("connectivity");
      NetworkInfo var2 = var1.getActiveNetworkInfo();
      if (var2 == null) {
         return false;
      } else {
         return var2.isConnected() || var2.isAvailable();
      }
   }

   public static String getWifiSSID(Context var0) {
      try {
         String var1 = null;
         WifiManager var2 = (WifiManager)var0.getSystemService("wifi");
         WifiInfo var3 = var2.getConnectionInfo();
         if (var3 != null) {
            var1 = var3.getBSSID();
         }

         return var1;
      } catch (Throwable var4) {
         var4.printStackTrace();
         return "";
      }
   }
}
