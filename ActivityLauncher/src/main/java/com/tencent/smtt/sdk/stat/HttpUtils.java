package com.tencent.smtt.sdk.stat;

import MTT.ThirdAppInfoNew;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsCoreLoadStat;
import com.tencent.smtt.sdk.TbsDownloadConfig;
import com.tencent.smtt.sdk.TbsDownloadUpload;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsLogReport;
import com.tencent.smtt.sdk.TbsPVConfig;
import com.tencent.smtt.sdk.TbsShareManager;
import com.tencent.smtt.utils.AppUtil;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.TbsLog;
import com.tencent.smtt.utils.DESedeUtils;
import com.tencent.smtt.utils.QUA2Util;
import com.tencent.smtt.utils.TbsCommonConfig;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import org.json.JSONObject;

public class HttpUtils {
   public static byte[] a = null;

   public static void a(final ThirdAppInfoNew var0, final Context var1) {
      (new Thread("HttpUtils") {
         public void run() {
            AppUtil.b(var1, var0.sGuid);
            var0.sCpu = AppUtil.b();
            if (VERSION.SDK_INT >= 8) {
               if (HttpUtils.a == null) {
                  try {
                     HttpUtils.a = "65dRa93L".getBytes("utf-8");
                  } catch (UnsupportedEncodingException var17) {
                     HttpUtils.a = null;
                     TbsLog.e("sdkreport", "Post failed -- get POST_DATA_KEY failed!");
                  }
               }

               if (HttpUtils.a == null) {
                  TbsLog.e("sdkreport", "Post failed -- POST_DATA_KEY is null!");
               } else {
                  String var1x = TbsDownloadConfig.getInstance(var1).mPreferences.getString("tbs_deskey_token", "");
                  String var2 = "";
                  String var3 = "";
                  if (!TextUtils.isEmpty(var1x)) {
                     var2 = var1x.substring(0, var1x.indexOf("&"));
                     var3 = var1x.substring(var1x.indexOf("&") + 1, var1x.length());
                  }

                  boolean var4 = TextUtils.isEmpty(var2) || var2.length() != 96 || TextUtils.isEmpty(var3) || var3.length() != 24;
                  HttpURLConnection var5 = null;

                  String var7;
                  try {
                     TbsCommonConfig var8 = TbsCommonConfig.getInstance();
                     if (var4) {
                        var7 = var8.b() + DESedeUtils.getInstance().b();
                     } else {
                        var7 = var8.f() + var2;
                     }

                     URL var6 = new URL(var7);
                     var5 = (HttpURLConnection)var6.openConnection();
                     var5.setRequestMethod("POST");
                  } catch (IOException var14) {
                     TbsLog.e("sdkreport", "Post failed -- IOException:" + var14);
                     return;
                  } catch (AssertionError var15) {
                     TbsLog.e("sdkreport", "Post failed -- AssertionError:" + var15);
                     return;
                  } catch (NoClassDefFoundError var16) {
                     TbsLog.e("sdkreport", "Post failed -- NoClassDefFoundError:" + var16);
                     return;
                  }

                  var5.setDoOutput(true);
                  var5.setDoInput(true);
                  var5.setUseCaches(false);
                  var5.setConnectTimeout(20000);
				   var5.setRequestProperty("Connection", "close");
	
				   JSONObject var18 = null;

                  try {
                     var18 = HttpUtils.c(var0, var1);
                  } catch (Exception var13) {
                     TbsLog.i(var13);
                  }

                  if (var18 == null) {
                     TbsLog.e("sdkreport", "post -- jsonData is null!");
                  } else {
                     var7 = null;

                     byte[] var19;
                     try {
                        var19 = var18.toString().getBytes("utf-8");
                        if (var4) {
                           var19 = DESedeUtils.getInstance().a(var19);
                        } else {
                           var19 = DESedeUtils.a(var19, var3);
                        }
                     } catch (Throwable var12) {
                        return;
                     }

                     var5.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                     var5.setRequestProperty("Content-Length", String.valueOf(var19.length));

                     TbsLogReport.TbsLogInfo var9;
                     try {
                        OutputStream var20 = var5.getOutputStream();
                        var20.write(var19);
                        var20.flush();
                        if (var5.getResponseCode() == 200) {
                           TbsLog.i("sdkreport", "Post successful!");
                           TbsLog.i("sdkreport", "SIGNATURE is " + var18.getString("SIGNATURE"));
                           String var21 = HttpUtils.getResponseFromConnection(var5, var3, var4);
                           HttpUtils.b(var1, var21);
                           TbsDownloadUpload var10 = new TbsDownloadUpload(var1);
                           var10.clearUploadCode();
                        } else {
                           TbsLog.e("sdkreport", "Post failed -- not 200 code is " + var5.getResponseCode());
                           var9 = TbsLogReport.getInstance(var1).tbsLogInfo();
                           var9.setErrorCode(126);
                           var9.setFailDetail("" + var5.getResponseCode());
                           TbsLogReport.getInstance(var1).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, var9);
                        }
                     } catch (Throwable var11) {
                        TbsLog.e("sdkreport", "Post failed -- exceptions:" + var11.getMessage());
                        var9 = TbsLogReport.getInstance(var1).tbsLogInfo();
                        var9.setErrorCode(126);
                        var9.setFailDetail(var11);
                        TbsLogReport.getInstance(var1).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, var9);
                     }
                  }
               }
            }
         }
      }).start();
   }

   private static JSONObject c(ThirdAppInfoNew var0, Context var1) {
      JSONObject var2 = null;

      try {
         var2 = new JSONObject();
         var2.put("APPNAME", var0.sAppName);
         var2.put("TIME", var0.sTime);
         var2.put("QUA2", var0.sQua2);
         var2.put("LC", var0.sLc);
         var2.put("GUID", var0.sGuid);
         var2.put("IMEI", var0.sImei);
         var2.put("IMSI", var0.sImsi);
         var2.put("MAC", var0.sMac);
         var2.put("PV", var0.iPv);
         var2.put("CORETYPE", var0.iCoreType);
         var2.put("APPVN", var0.sAppVersionName);
         var2.put("APPMETADATA", var0.sMetaData);
         var2.put("VERSION_CODE", var0.sVersionCode);
         var2.put("CPU", var0.sCpu);
         if (!"com.tencent.mm".equals(var0.sAppName) && !"com.tencent.mobileqq".equals(var0.sAppName) && !"com.tencent.tbs".equals(var0.sAppName)) {
            if (var0.sAppSignature == null) {
               var2.put("SIGNATURE", "0");
            } else {
               var2.put("SIGNATURE", var0.sAppSignature);
            }
         } else {
            TbsDownloadUpload var3 = new TbsDownloadUpload(var1);
            var3.readTbsDownloadInfo(var1);
            int var4 = var3.getNeedDownloadCode();
            int var5 = var3.getStartDownloadCode();
            int var6 = var3.getNeedDownloadReturn();
            int var7 = var3.getLocalCoreVersion();
            var2.put("SIGNATURE", "" + var4 + ":" + var5 + ":" + var6 + ":" + var7);
         }

         var2.put("PROTOCOL_VERSION", 3);
         var2.put("ANDROID_ID", var0.sAndroidID);
         if (TbsShareManager.isThirdPartyApp(var1)) {
            var2.put("HOST_COREVERSION", TbsShareManager.getHostCoreVersions(var1));
         } else {
            var2.put("HOST_COREVERSION", TbsDownloader.getCoreShareDecoupleCoreVersionByContext(var1));
            var2.put("DECOUPLE_COREVERSION", TbsDownloader.getCoreShareDecoupleCoreVersionByContext(var1));
         }

         var2.put("WIFICONNECTEDTIME", var0.sWifiConnectedTime);
         var2.put("CORE_EXIST", var0.localCoreVersion);
         int var10 = TbsCoreLoadStat.mLoadErrorCode;
         if (var0.localCoreVersion <= 0) {
            var2.put("TBS_ERROR_CODE", TbsDownloadConfig.getInstance(var1).getDownloadInterruptCode());
         } else {
            var2.put("TBS_ERROR_CODE", var10);
         }

         if (var10 == -1) {
            TbsLog.e("sdkreport", "ATTENTION: Load errorCode missed!");
         }

         TbsDownloadConfig.getInstance(var1).uploadDownloadInterruptCodeIfNeeded(var1);

         try {
            if (QbSdk.getTID() != null) {
               if (var0.sAppName.equals("com.tencent.mobileqq")) {
                  var2.put("TID", QbSdk.getTID());
                  var2.put("TIDTYPE", 0);
               } else if (var0.sAppName.equals("com.tencent.mm")) {
                  var2.put("TID", QbSdk.getTID());
                  var2.put("TIDTYPE", 0);
               }
            }
         } catch (Exception var8) {
         }

         return var2;
      } catch (Exception var9) {
         TbsLog.e("sdkreport", "getPostData exception!");
         return null;
      }
   }

   public static void doReport(Context var0, String var1, String var2, String var3, int var4, boolean var5, long var6, boolean var8) {
      if (QbSdk.getSettings() != null && QbSdk.getSettings().containsKey("SET_SENDREQUEST_AND_UPLOAD") && QbSdk.getSettings().get("SET_SENDREQUEST_AND_UPLOAD").equals("false")) {
         TbsLog.i("sdkreport", "[HttpUtils.doReport] -- SET_SENDREQUEST_AND_UPLOAD is false");
      } else {
         String var9 = "";

         try {
            ApplicationInfo var10 = var0.getApplicationInfo();
            if ("com.tencent.mobileqq".equals(var10.packageName)) {
               PackageInfo var11 = var0.getPackageManager().getPackageInfo(var10.packageName, 0);
               var9 = var11.versionName;
               if (!TextUtils.isEmpty(QbSdk.getQQBuildNumber())) {
                  var9 = var9 + "." + QbSdk.getQQBuildNumber();
               }
            }
         } catch (Exception var20) {
            TbsLog.i(var20);
         }

         try {
            ThirdAppInfoNew var21 = new ThirdAppInfoNew();
            var21.sAppName = var0.getApplicationContext().getApplicationInfo().packageName;
            TbsCommonConfig.getInstance(var0);
            SimpleDateFormat var22 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            var22.setTimeZone(TimeZone.getTimeZone("GMT+08"));
            Calendar var12 = Calendar.getInstance();
            Date var13 = var12.getTime();
            var21.sTime = var22.format(var13);
            var21.sVersionCode = AppUtil.getVersionCode(var0);
            String var14 = AppUtil.getMetaHex(var0, "com.tencent.mm.BuildInfo.CLIENT_VERSION");
            if (!TextUtils.isEmpty(var14)) {
               var21.sMetaData = var14;
            }

            var21.sGuid = var1;
            if (var5) {
               var21.sQua2 = var2;
               var21.bIsSandboxMode = var8;
            } else {
               var21.sQua2 = QUA2Util.takeSnapshot(var0);
            }

            var21.sLc = var3;
            String var15 = AppUtil.h(var0);
            String var16 = AppUtil.f(var0);
            String var17 = AppUtil.g(var0);
            String var18 = AppUtil.i(var0);
            if (var16 != null && !"".equals(var16)) {
               var21.sImei = var16;
            }

            if (var17 != null && !"".equals(var17)) {
               var21.sImsi = var17;
            }

            if (!TextUtils.isEmpty(var18)) {
               var21.sAndroidID = var18;
            }

            if (var15 != null && !"".equals(var15)) {
               var21.sMac = var15;
            }

            var21.iPv = (long)var4;
            if (TbsShareManager.isThirdPartyApp(var0)) {
               if (var5) {
                  if (TbsShareManager.getCoreFormOwn()) {
                     var21.iCoreType = 2;
                  } else {
                     var21.iCoreType = 1;
                  }

                  if (var8) {
                     var21.iCoreType = 3;
                  }
               } else {
                  var21.iCoreType = 0;
               }
            } else {
               var21.iCoreType = var5 ? 1 : 0;
               if (var5 && var8) {
                  var21.iCoreType = 3;
               }
            }

            var21.sAppVersionName = var9;
            var21.sAppSignature = a(var0);
            if (!var5) {
               var21.sWifiConnectedTime = var6;
               var21.localCoreVersion = QbSdk.getTbsVersion(var0);
            }

            a(var21, var0.getApplicationContext());
         } catch (Throwable var19) {
            TbsLog.i(var19);
         }

      }
   }

   private static String a(Context var0) {
      Object var1 = null;

      try {
         PackageInfo var2 = var0.getPackageManager().getPackageInfo(var0.getPackageName(), 64);
         Signature[] var3 = var2.signatures;
         Signature var4 = var3[0];
         byte[] var13 = var4.toByteArray();
         if (var13 != null) {
            Object var5 = null;
            MessageDigest var6 = MessageDigest.getInstance("SHA-1");
            var6.update(var13);
            byte[] var7 = var6.digest();
            byte[] var14 = var7;
            if (var7 != null) {
               StringBuilder var8 = new StringBuilder("");
               if (var7 != null && var7.length > 0) {
                  for(int var9 = 0; var9 < var14.length; ++var9) {
                     int var10 = var14[var9] & 255;
                     String var11 = Integer.toHexString(var10).toUpperCase();
                     if (var9 > 0) {
                        var8.append(":");
                     }

                     if (var11.length() < 2) {
                        var8.append(0);
                     }

                     var8.append(var11);
                  }

                  return var8.toString();
               }

               return null;
            }
         }
      } catch (Exception var12) {
         TbsLog.i(var12);
      }

      return null;
   }

   private static String getResponseFromConnection(HttpURLConnection var0, String var1, boolean var2) {
      String var3 = "";
      Object var4 = null;
      ByteArrayOutputStream var5 = null;

      try {
         InputStream var6 = var0.getInputStream();
         String var7 = var0.getContentEncoding();
         if (var7 != null && var7.equalsIgnoreCase("gzip")) {
            var4 = new GZIPInputStream(var6);
         } else if (var7 != null && var7.equalsIgnoreCase("deflate")) {
            var4 = new InflaterInputStream(var6, new Inflater(true));
         } else {
            var4 = var6;
         }

         var5 = new ByteArrayOutputStream();
         byte[] var8 = new byte[128];
         boolean var9 = false;

         int var24;
         while((var24 = ((InputStream)var4).read(var8)) != -1) {
            var5.write(var8, 0, var24);
         }

         if (var2) {
            var3 = new String(DESedeUtils.getInstance().c(var5.toByteArray()));
         } else {
            var3 = new String(DESedeUtils.b(var5.toByteArray(), var1));
         }
      } catch (Exception var22) {
         TbsLog.i(var22);
      } finally {
         if (var5 != null) {
            try {
               var5.close();
            } catch (IOException var21) {
               TbsLog.i(var21);
            }
         }

         if (var4 != null) {
            try {
               ((InputStream)var4).close();
            } catch (IOException var20) {
               TbsLog.i(var20);
            }
         }

      }

      TbsLog.i("HttpUtils", "getResponseFromConnection,response=" + var3 + ";isUseRSA=" + var2);
      return var3;
   }

   private static void b(Context var0, String var1) {
      try {
         TbsPVConfig.releaseInstance();
         TbsPVConfig.getInstance(var0).clear();
         if (TextUtils.isEmpty(var1)) {
            return;
         }

         String[] var2 = var1.split("\\|");
         String[] var3 = var2;
         int var4 = var2.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String var6 = var3[var5];

            try {
               String[] var7 = var6.split("=");
               if (var7.length == 2) {
                  String var8 = var7[0];
                  String var9 = var7[1];
                  a(var0, var8, var9);
               }
            } catch (Exception var10) {
               TbsLog.i(var10);
            }
         }

         TbsPVConfig.getInstance(var0).commit();
      } catch (Exception var11) {
         TbsLog.i(var11);
      }

   }

   private static void a(Context var0, String var1, String var2) {
      if ("reset".equals(var1) && "true".equals(var2)) {
         QbSdk.reset(var0);
      } else if (var1.startsWith("rmfile")) {
         try {
            SharedPreferences var3 = var0.getSharedPreferences("tbs_status", 0);
            boolean var4 = var3.getBoolean(var1, false);
            if (var4) {
               return;
            }

            File var5 = new File(var2);
            if (var2 != null && var5.exists()) {
               TbsLog.i("HttpUtils", "received command,delete" + var2);
               FileHelper.delete(var5);
            }

            var3.edit().putBoolean(var1, true).apply();
         } catch (Exception var6) {
            TbsLog.i(var6);
         }
      } else {
         TbsPVConfig.getInstance(var0).putData(var1, var2);
      }

   }

   static {
      try {
         a = "65dRa93L".getBytes("utf-8");
      } catch (UnsupportedEncodingException var1) {
      }

   }
}
