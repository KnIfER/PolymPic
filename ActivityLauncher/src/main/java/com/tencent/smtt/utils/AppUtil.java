package com.tencent.smtt.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.tencent.smtt.sdk.TbsDownloadConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AppUtil {
   public static String a = "";
   public static String b = "";
   public static String c = "";
   public static String d = "";
   public static String e = "";

   public static String getPackageName(Context var0) {
      String var1 = null;

      try {
         var1 = var0.getPackageName();
      } catch (Exception var3) {
      }

      return var1;
   }

   public static int getVersion(Context var0) {
      return VERSION.SDK_INT;
   }

   public static String getIsoModel() {
      try {
         return new String(Build.MODEL.getBytes("UTF-8"), "ISO8859-1");
      } catch (Exception var1) {
         return Build.MODEL;
      }
   }

   public static String getVersionName(Context var0) {
      String versionName = null;

      try {
         String packageName = var0.getPackageName();
         PackageManager packageManager = var0.getPackageManager();
         PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
         versionName = packageInfo.versionName;
      } catch (Exception var5) {
      }

      return versionName;
   }

   public static int getVersionCode(Context var0) {
      int var1 = 0;

      try {
         String var2 = var0.getPackageName();
         PackageManager var3 = var0.getPackageManager();
         PackageInfo var4 = var3.getPackageInfo(var2, 0);
         var1 = var4.versionCode;
      } catch (Exception var5) {
      }

      return var1;
   }

   public static String getMetaHex(Context context, String var1) {
      String ret = null;

      try {
         String packageName = context.getPackageName();
         PackageManager packageManager = context.getPackageManager();
         ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName, 128);
         ret = String.valueOf(applicationInfo.metaData.get(var1));

         try {
            int var6 = Integer.parseInt(ret);
            ret = String.valueOf(Integer.toHexString(var6));
         } catch (Exception var7) {
         }
      } catch (Exception var8) {
      }

      return ret;
   }

   public static void b(Context var0, String var1) {
      Log.d("0816", "saveGuid guid is " + var1);

      try {
         TbsDownloadConfig var2 = TbsDownloadConfig.getInstance(var0);
         var2.mSyncMap.put("tbs_guid", var1);
         var2.commit();
      } catch (Exception var3) {
      }

   }

   public static String e(Context var0) {
      String var1 = "";

      try {
         TbsDownloadConfig var2 = TbsDownloadConfig.getInstance(var0);
         var1 = var2.mPreferences.getString("tbs_guid", "");
      } catch (Exception var3) {
      }

      Log.d("0816", "getGuid guid is " + var1);
      return var1;
   }

   public static String f(Context var0) {
      String var1 = "";
      if (!TextUtils.isEmpty(a)) {
         var1 = a;
      } else {
         try {
            TelephonyManager var2 = (TelephonyManager)var0.getSystemService("phone");
            var1 = var2.getDeviceId();
         } catch (Exception var3) {
            TbsLog.i(var3);
         }
      }

      return var1;
   }

   public static String g(Context var0) {
      String var1 = "";
      if (!TextUtils.isEmpty(b)) {
         var1 = b;
      } else {
         try {
            TelephonyManager var2 = (TelephonyManager)var0.getSystemService("phone");
            var1 = var2.getSubscriberId();
         } catch (Exception var3) {
            TbsLog.i(var3);
         }
      }

      return var1;
   }

   public static String b() {
      if (!TextUtils.isEmpty(c)) {
         return c;
      } else {
         String var0 = null;
         InputStreamReader var1 = null;
         BufferedReader var2 = null;

         try {
            Process var3 = Runtime.getRuntime().exec("getprop ro.product.cpu.abi");
            var1 = new InputStreamReader(var3.getInputStream());
            var2 = new BufferedReader(var1);
            String var4 = var2.readLine();
            if (var4.contains("x86")) {
               var0 = a("i686");
            } else {
               var0 = a(System.getProperty("os.arch"));
            }
         } catch (Throwable var17) {
            var0 = a(System.getProperty("os.arch"));
            var17.printStackTrace();
         } finally {
            try {
               if (var2 != null) {
                  var2.close();
               }
            } catch (IOException var16) {
            }

            try {
               if (var1 != null) {
                  var1.close();
               }
            } catch (IOException var15) {
            }

         }

         return var0;
      }
   }

   public static String h(Context var0) {
      if (TextUtils.isEmpty(d)) {
         if (VERSION.SDK_INT < 23) {
            try {
               WifiManager var1 = (WifiManager)var0.getApplicationContext().getSystemService("wifi");
               WifiInfo var2 = null == var1 ? null : var1.getConnectionInfo();
               d = var2 == null ? "" : var2.getMacAddress();
            } catch (Exception var3) {
               TbsLog.i(var3);
            }
         } else {
            d = c();
         }
      }

      return d;
   }

   public static String c() {
      try {
         ArrayList var0 = Collections.list(NetworkInterface.getNetworkInterfaces());
         Iterator var1 = var0.iterator();

         while(var1.hasNext()) {
            NetworkInterface var2 = (NetworkInterface)var1.next();
            if (var2.getName().equalsIgnoreCase("wlan0")) {
               byte[] var3 = var2.getHardwareAddress();
               if (var3 == null) {
                  return "";
               }

               StringBuilder var4 = new StringBuilder();
               byte[] var5 = var3;
               int var6 = var3.length;

               for(int var7 = 0; var7 < var6; ++var7) {
                  byte var8 = var5[var7];
                  var4.append(String.format("%02X:", var8));
               }

               if (var4.length() > 0) {
                  var4.deleteCharAt(var4.length() - 1);
               }

               return var4.toString();
            }
         }
      } catch (Exception var9) {
      }

      return "02:00:00:00:00:00";
   }

   private static String a(String var0) {
      return var0 == null ? "" : var0;
   }

   public static String i(Context var0) {
      if (!TextUtils.isEmpty(e)) {
         return e;
      } else {
         try {
            e = Secure.getString(var0.getContentResolver(), "android_id");
         } catch (Exception var2) {
            var2.printStackTrace();
         }

         return e;
      }
   }

   public static String a(Context var0, boolean var1, File var2) {
      String var3 = "";
      if (var2 != null && var2.exists()) {
         if (var1) {
            label131: {
               RandomAccessFile var4 = null;

               String var7;
               try {
                  byte[] var5 = new byte[2];
                  var4 = new RandomAccessFile(var2, "r");
                  var4.read(var5);
                  String var6 = new String(var5);
                  if (var6.equalsIgnoreCase("PK")) {
                     break label131;
                  }

                  var7 = "";
               } catch (Exception var20) {
                  var20.printStackTrace();
                  break label131;
               } finally {
                  try {
                     var4.close();
                  } catch (IOException var18) {
                     var18.printStackTrace();
                  }

               }

               return var7;
            }
         }

         try {
            if (var0.getApplicationContext().getPackageName().contains("com.jd.jrapp")) {
               TbsLog.i("AppUtil", "[AppUtil.getSignatureFromApk]  #1");
               var3 = a(var2);
               if (var3 != null) {
                  TbsLog.i("AppUtil", "[AppUtil.getSignatureFromApk]  #2");
                  return var3;
               }
            }
         } catch (Throwable var19) {
            TbsLog.i("AppUtil", "[AppUtil.getSignatureFromApk]  #3");
         }

         TbsLog.i("AppUtil", "[AppUtil.getSignatureFromApk]  #4");
         var3 = a(var0, var2);
         TbsLog.i("AppUtil", "[AppUtil.getSignatureFromApk]  android api signature=" + var3);
         if (var3 == null) {
            var3 = a(var2);
            TbsLog.i("AppUtil", "[AppUtil.getSignatureFromApk]  java get signature=" + var3);
         }

         return var3;
      } else {
         return "";
      }
   }

   private static String a(Context var0, File var1) {
      String var2 = null;

      try {
         PackageInfo var3 = null;
         var3 = var0.getPackageManager().getPackageArchiveInfo(var1.getAbsolutePath(), 65);
         Signature var4 = null;
         if (var3 != null) {
            if (var3.signatures != null && var3.signatures.length > 0) {
               var4 = var3.signatures[0];
            } else {
               TbsLog.w("AppUtil", "[getSignatureFromApk] pkgInfo is not null BUT signatures is null!");
            }
         }

         if (var4 != null) {
            var2 = var4.toCharsString();
         }
      } catch (Exception var5) {
         TbsLog.i("AppUtil", "getSign " + var1 + "failed");
      }

      return var2;
   }

   private static String a(File var0) {
      String ret = null;

      try {
         JarFile var2 = new JarFile(var0);
         JarEntry var3 = var2.getJarEntry("AndroidManifest.xml");
         byte[] var4 = new byte[8192];
         Certificate[] var5 = a(var2, var3, var4);
         ret = a(var5[0].getEncoded());
         Enumeration var6 = var2.entries();
         String var7 = null;
         Certificate[] var8 = null;

         while(var6.hasMoreElements()) {
            JarEntry var9 = (JarEntry)var6.nextElement();
            String var10 = var9.getName();
            if (var10 != null) {
               var8 = a(var2, var9, var4);
               var7 = null;
               if (var8 != null) {
                  var7 = a(var8[0].getEncoded());
               }

               if (var7 == null) {
                  if (!var10.startsWith("META-INF/")) {
                     ret = null;
                     break;
                  }
               } else {
                  boolean var11 = var7.equals(ret);
                  if (!var11) {
                     ret = null;
                     break;
                  }
               }
            }
         }
      } catch (Exception var12) {
         ret = null;
         var12.printStackTrace();
      }

      return ret;
   }

   private static Certificate[] a(JarFile var0, JarEntry var1, byte[] var2) throws Exception {
      InputStream var3 = var0.getInputStream(var1);

      while(var3.read(var2, 0, var2.length) != -1) {
      }

      var3.close();
      return var1 != null ? var1.getCertificates() : null;
   }

   private static String a(byte[] var0) {
      byte[] var1 = var0;
      int var2 = var0.length;
      int var3 = var2 * 2;
      char[] var4 = new char[var3];

      for(int var5 = 0; var5 < var2; ++var5) {
         byte var6 = var1[var5];
         int var7 = var6 >> 4 & 15;
         var4[var5 * 2] = (char)(var7 >= 10 ? 97 + var7 - 10 : 48 + var7);
         var7 = var6 & 15;
         var4[var5 * 2 + 1] = (char)(var7 >= 10 ? 97 + var7 - 10 : 48 + var7);
      }

      return new String(var4);
   }

   public static boolean d() {
      try {
         if (VERSION.SDK_INT < 21) {
            return false;
         }

         Class var0 = Class.forName("dalvik.system.VMRuntime");
         if (var0 == null) {
            return false;
         }

         Method var1 = var0.getDeclaredMethod("getRuntime");
         if (var1 == null) {
            return false;
         }

         Object var2 = var1.invoke((Object)null);
         if (var2 == null) {
            return false;
         }

         Method var3 = var0.getDeclaredMethod("is64Bit");
         if (var3 == null) {
            return false;
         }

         Object var4 = var3.invoke(var2);
         if (var4 instanceof Boolean) {
            return (Boolean)var4;
         }
      } catch (Throwable var5) {
         var5.printStackTrace();
      }

      return false;
   }
}
