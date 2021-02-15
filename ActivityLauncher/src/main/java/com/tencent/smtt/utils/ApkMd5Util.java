package com.tencent.smtt.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import com.tencent.smtt.sdk.TbsExtensionFunctionManager;
import com.tencent.smtt.sdk.TbsPVConfig;
import com.tencent.smtt.sdk.TbsShareManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApkMd5Util {
   public static boolean a(Context context, File file, long var2, int var4) {
      if (file != null && file.exists()) {
         if (var2 > 0L && var2 != file.length()) {
            return false;
         } else {
            try {
               int var5 = getApkVersion(context, file);
               if (var4 != var5) {
                  return false;
               } else {
                  String var6 = AppUtil.a(context, true, file);
                  return "3082023f308201a8a00302010202044c46914a300d06092a864886f70d01010505003064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f301e170d3130303732313036313835305a170d3430303731333036313835305a3064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f30819f300d06092a864886f70d010101050003818d0030818902818100c209077044bd0d63ea00ede5b839914cabcc912a87f0f8b390877e0f7a2583f0d5933443c40431c35a4433bc4c965800141961adc44c9625b1d321385221fd097e5bdc2f44a1840d643ab59dc070cf6c4b4b4d98bed5cbb8046e0a7078ae134da107cdf2bfc9b440fe5cb2f7549b44b73202cc6f7c2c55b8cfb0d333a021f01f0203010001300d06092a864886f70d010105050003818100b007db9922774ef4ccfee81ba514a8d57c410257e7a2eba64bfa17c9e690da08106d32f637ac41fbc9f205176c71bde238c872c3ee2f8313502bee44c80288ea4ef377a6f2cdfe4d3653c145c4acfedbfbadea23b559d41980cc3cdd35d79a68240693739aabf5c5ed26148756cf88264226de394c8a24ac35b712b120d4d23a".equals(var6);
               }
            } catch (Exception var7) {
               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static String getMD5(File file) {
      char[] mould = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
      FileInputStream inputStream = null;
      char[] strBuff = new char[32];
      int cc = 0;

      Object var7;
      try {
         MessageDigest messageDigest = MessageDigest.getInstance("MD5");
         inputStream = new FileInputStream(file);
         byte[] buff = new byte[8192];
         boolean var8 = true;

         int len;
         while((len = inputStream.read(buff)) != -1) {
            messageDigest.update(buff, 0, len);
         }

         byte[] digest = messageDigest.digest();

         for(int i = 0; i < 16; ++i) {
            byte dI = digest[i];
            strBuff[cc++] = mould[dI >>> 4 & 15];
            strBuff[cc++] = mould[dI & 15];
         }

         return new String(strBuff);
      } catch (Exception var20) {
         var20.printStackTrace();
         var7 = null;
      } finally {
         if (inputStream != null) {
            try {
               inputStream.close();
            } catch (IOException var19) {
               var19.printStackTrace();
            }
         }

      }

      return (String)var7;
   }

   public static int getApkVersion(Context var0, File var1) {
      int var2 = 0;

      try {
         boolean var3 = false;
         if (VERSION.SDK_INT >= 20) {
            TbsExtensionFunctionManager var4 = TbsExtensionFunctionManager.getInstance();
            boolean var5 = var4.canUseFunction(var0, "disable_get_apk_version_switch.txt");
            var3 = !var5;
         }

         var2 = a(var0, var1, var3);
      } catch (Exception var6) {
         TbsLog.i("ApkUtil", "getApkVersion Failed");
      }

      return var2;
   }

   public static final String a(boolean var0) {
      if (AppUtil.d()) {
         return var0 ? "x5.64.decouple.backup" : "x5.64.backup";
      } else {
         return var0 ? "x5.decouple.backup" : "x5.backup";
      }
   }

   private static int a(boolean var0, File var1) {
      try {
         File var2 = var1.getParentFile();
         if (var2 != null) {
            File[] var3 = var2.listFiles();
            String var4 = a(var0) + "(.*)";
            Pattern var5 = Pattern.compile(var4);
            File[] var6 = var3;
            int var7 = var3.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               File var9 = var6[var8];
               Matcher var10 = var5.matcher(var9.getName());
               if (var10.find() && var9.isFile() && var9.exists()) {
                  return Integer.parseInt(var9.getName().substring(var9.getName().lastIndexOf(".") + 1));
               }
            }
         }
      } catch (Exception var11) {
      }

      return -1;
   }

   public static int a(Context var0, File var1, boolean var2) {
      int var3 = 0;
      boolean var4 = false;

      try {
         if (var1 != null && var1.exists()) {
            boolean var5 = var1.getName().contains("tbs.org");
            boolean var6 = var1.getName().contains("x5.tbs.decouple");
            if (var5 || var6) {
               int var7 = a(var6, var1);
               if (var7 > 0) {
                  return var7;
               }

               if (!TbsShareManager.isThirdPartyApp(var0) && !var1.getAbsolutePath().contains(var0.getApplicationInfo().packageName)) {
                  return var3;
               }
            }

            if ((VERSION.SDK_INT == 23 || VERSION.SDK_INT == 25) && Build.MANUFACTURER.toLowerCase().contains("mi")) {
               var4 = true;
            }

            TbsPVConfig.releaseInstance();
            TbsPVConfig var13 = TbsPVConfig.getInstance(var0);
            int var8 = var13.getReadApk();
            if (var8 == 1) {
               var4 = false;
               var2 = false;
            } else if (var8 == 2) {
               return var3;
            }

            if (var2 || var4) {
               var3 = b(var1);
               if (var3 > 0) {
                  return var3;
               }

               var3 = 0;
            }
         }
      } catch (Throwable var10) {
         var10.printStackTrace();
         var3 = 0;
      }

      if (var1 != null && var1.exists()) {
         try {
            PackageManager var11 = var0.getPackageManager();
            PackageInfo var12 = var11.getPackageArchiveInfo(var1.getAbsolutePath(), 1);
            if (var12 != null) {
               var3 = var12.versionCode;
            }
         } catch (Throwable var9) {
            var9.printStackTrace();
            return -1;
         }
      }

      return var3;
   }

   public static int b(File var0) {
      Class aClass = ApkMd5Util.class;
      synchronized(ApkMd5Util.class) {
         JarFile var2 = null;
         BufferedReader var3 = null;

         try {
            var2 = new JarFile(var0);
            JarEntry var4 = var2.getJarEntry("assets/webkit/tbs.conf");
            InputStream var5 = var2.getInputStream(var4);
            InputStreamReader var6 = new InputStreamReader(var5);
            var3 = new BufferedReader(var6);

            String var7;
            while((var7 = var3.readLine()) != null) {
               if (var7.contains("tbs_core_version")) {
                  String[] var8 = var7.split("=");
                  if (var8 != null && var8.length == 2) {
                     String var9 = var8[1].trim();
                     int var10 = Integer.parseInt(var9);
                     return var10;
                  }
               }
            }

            return -1;
         } catch (Exception var23) {
            var23.printStackTrace();
            return -1;
         } finally {
            try {
               if (var3 != null) {
                  var3.close();
               }

               if (var2 != null) {
                  var2.close();
               }
            } catch (Exception var22) {
            }

         }
      }
   }
}
