package com.tencent.smtt.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import com.tencent.smtt.sdk.WebView;

public class QUA2Util {
   private static String snapshot = null;
   private static String b = "GA";
   private static String c = "GE";
   private static String d = "9422";
   private static String e = "0";
   private static String f = "";
   private static boolean g = false;
   private static boolean h = false;
   private static boolean i = false;

   public static String takeSnapshot(Context context) {
      if (!TextUtils.isEmpty(snapshot)) {
         return snapshot;
      } else {
         String tbsSDKVersion = String.valueOf(WebView.getTbsSDKVersion(context));
         String var2 = "0";
         snapshot = buildSnapshot(context, tbsSDKVersion, var2, b, c, d, e, f, g);
         return snapshot;
      }
   }

   private static String buildSnapshot(Context context, String tbsSDKVersion, String var2, String var3, String var4, String var5, String var6, String var7, boolean var8) {
      String var9 = "";
      String var10 = "";
      String var11 = "";
      String var12 = "PHONE";
      StringBuilder sb = new StringBuilder();
      String var14 = b(context) + "*" + c(context);

      PackageInfo var16;
      try {
         ApplicationInfo var15 = context.getApplicationContext().getApplicationInfo();
         var16 = context.getPackageManager().getPackageInfo(var15.packageName, 0);
         var10 = var15.packageName;
         if (!TextUtils.isEmpty(var7)) {
            var11 = var7;
         } else {
            var11 = var16.versionName;
         }
      } catch (NameNotFoundException var22) {
         var22.printStackTrace();
      }

      var9 = getAppShortCode(var10);
      if ("QB".equals(var9)) {
         if (var8) {
            var12 = "PAD";
         }
      } else if (d(context)) {
         var12 = "PAD";
      }

      sb.append("QV").append("=").append("3");
      appendParameter(sb, "PL", "ADR");
      appendParameter(sb, "PR", var9);
      appendParameter(sb, "PP", var10);
      appendParameter(sb, "PPVN", var11);
      if (!TextUtils.isEmpty(tbsSDKVersion)) {
         appendParameter(sb, "TBSVC", tbsSDKVersion);
      }

      appendParameter(sb, "CO", "SYS");
      if (!TextUtils.isEmpty(var2)) {
         appendParameter(sb, "COVC", var2);
      }

      appendParameter(sb, "PB", var4);
      appendParameter(sb, "VE", var3);
      appendParameter(sb, "DE", var12);
      appendParameter(sb, "CHID", TextUtils.isEmpty(var6) ? "0" : var6);
      appendParameter(sb, "LCID", var5);
      String var23 = getModelCode();
      var16 = null;

      String var24;
      try {
         var24 = new String(var23.getBytes("UTF-8"), "ISO8859-1");
      } catch (Exception var21) {
         var24 = var23;
      }

      if (!TextUtils.isEmpty(var24)) {
         appendParameter(sb, "MO", var24);
      }

      appendParameter(sb, "RL", var14);
      String var17 = VERSION.RELEASE;
      String var18 = null;

      try {
         var18 = new String(var17.getBytes("UTF-8"), "ISO8859-1");
      } catch (Exception var20) {
         var18 = var17;
      }

      if (!TextUtils.isEmpty(var18)) {
         appendParameter(sb, "OS", var18);
      }

      appendParameter(sb, "API", VERSION.SDK_INT + "");
      return sb.toString();
   }

   private static void appendParameter(StringBuilder var0, String var1, String var2) {
      var0.append("&").append(var1).append("=").append(var2);
   }

   private static String getAppShortCode(String var0) {
      if ("com.tencent.mm".equals(var0)) {
         return "WX";
      } else if ("com.tencent.mobileqq".equals(var0)) {
         return "QQ";
      } else if ("com.qzone".equals(var0)) {
         return "QZ";
      } else {
         return "com.tencent.mtt".equals(var0) ? "QB" : "TRD";
      }
   }

   private static int b(Context var0) {
      WindowManager var1 = (WindowManager)var0.getSystemService("window");
      Display var2 = var1.getDefaultDisplay();
      return var2 != null ? var2.getWidth() : -1;
   }

   private static int c(Context var0) {
      WindowManager window = (WindowManager)var0.getSystemService("window");
      Display var2 = window.getDefaultDisplay();
      return var2 != null ? var2.getHeight() : -1;
   }

   private static String getModelCode() {
      return " " + Build.MODEL.replaceAll("[ |\\/|\\_|\\&|\\|]", "") + " ";
   }

   private static boolean d(Context var0) {
      if (h) {
         return i;
      } else {
         try {
            int var2 = Math.min(b(var0), c(var0)) * 160 / getDensityDpi(var0);
            i = var2 >= 700;
            h = true;
         } catch (Throwable var3) {
            return false;
         }

         return i;
      }
   }

   private static int getDensityDpi(Context var0) {
      WindowManager var1 = (WindowManager)var0.getSystemService("window");
      DisplayMetrics var2 = new DisplayMetrics();
      Display var3 = var1.getDefaultDisplay();
      if (var3 != null) {
         var3.getMetrics(var2);
         return var2.densityDpi;
      } else {
         return 160;
      }
   }
}
