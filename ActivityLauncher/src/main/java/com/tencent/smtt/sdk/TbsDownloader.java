package com.tencent.smtt.sdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.utils.ApkMd5Util;
import com.tencent.smtt.utils.Apn;
import com.tencent.smtt.utils.AppUtil;
import com.tencent.smtt.utils.DESedeUtils;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.TbsCommonConfig;
import com.tencent.smtt.utils.TbsLog;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public class TbsDownloader {
   public static final String LOGTAG = "TbsDownload";
   public static final boolean DEBUG_DISABLE_DOWNLOAD = false;
   public static final String TBS_METADATA = "com.tencent.mm.BuildInfo.CLIENT_VERSION";
   private static String b;
   private static Context c;
   private static Handler d;
   private static String e;
   public static boolean DOWNLOAD_OVERSEA_TBS = false;
   private static Object f = new byte[0];
   private static TbsDownload g;
   private static HandlerThread h;
   static boolean downloading;
   private static boolean i = false;
   private static boolean j = false;
   private static boolean k = false;
   private static long l = -1L;

   public static HandlerThread getsTbsHandlerThread() {
      return h;
   }

   public static String getBackupFileName(boolean var0) {
      if (var0) {
         return AppUtil.d() ? "x5.tbs.decouple.64" : "x5.tbs.decouple";
      } else {
         return AppUtil.d() ? "x5.tbs.org.64" : "x5.tbs.org";
      }
   }

   private static boolean c() {
      try {
         String[] var0 = TbsShareManager.getCoreProviderAppList();
         String[] var1 = var0;
         int var2 = var0.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String var4 = var1[var3];
            int var5 = TbsShareManager.getSharedTbsCoreVersion(c, var4);
            if (var5 > 0) {
               return true;
            }
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return false;
   }

   public static void setAppContext(Context var0) {
      if (var0 != null && var0.getApplicationContext() != null) {
         c = var0.getApplicationContext();
      }

   }

   public static boolean needSendRequest(Context var0, boolean var1) {
      c = var0.getApplicationContext();
      TbsLog.initIfNeed(var0);
      if (!a(c, var1)) {
         return false;
      } else {
         int var2 = TbsInstaller.a().getTbsCoreInstalledVerWithLock(var0);
         TbsLog.i("TbsDownload", "[TbsDownloader.needSendRequest] localTbsVersion=" + var2);
         if (var2 > 0) {
            return false;
         } else if (a(c, false, true)) {
            return true;
         } else {
            TbsDownloadConfig var3 = TbsDownloadConfig.getInstance(c);
            boolean var4 = var3.mPreferences.contains("tbs_needdownload");
            TbsLog.i("TbsDownload", "[TbsDownloader.needSendRequest] hasNeedDownloadKey=" + var4);
            boolean var5 = false;
            if (!var4) {
               var5 = true;
            } else {
               var5 = var3.mPreferences.getBoolean("tbs_needdownload", false);
            }

            TbsLog.i("TbsDownload", "[TbsDownloader.needSendRequest] needDownload=" + var5);
            boolean var6 = var5 && h();
            TbsLog.i("TbsDownload", "[TbsDownloader.needSendRequest] ret=" + var6);
            return var6;
         }
      }
   }

   private static boolean a(Context var0, boolean var1, boolean var2) {
      final TbsDownloadConfig var3 = TbsDownloadConfig.getInstance(var0);
      SDKEcService var4 = SDKEcService.a();
      var4.executedCommand(1000, (SDKEcService.a)(new SDKEcService.a() {
         public void a(String var1) {
            Log.e("TBSEmergency", "Execute command [1000](" + var1 + "), force tbs downloader request");
            Editor var2 = var3.mPreferences.edit();
            var2.putLong("last_check", 0L);
            var2.apply();
            var2.commit();
         }
      }));
      boolean var5 = false;
      boolean var6 = false;
      String var7 = null;
      if (!var1) {
         String var8 = var3.mPreferences.getString("app_versionname", (String)null);
         int var9 = var3.mPreferences.getInt("app_versioncode", 0);
         String var10 = var3.mPreferences.getString("app_metadata", (String)null);
         String var11 = AppUtil.getVersionName(c);
         int var12 = AppUtil.getVersionCode(c);
         String var13 = AppUtil.getMetaHex(c, "com.tencent.mm.BuildInfo.CLIENT_VERSION");
         TbsLog.i("TbsDownload", "[TbsDownloader.needSendQueryRequest] appVersionName=" + var11 + " oldAppVersionName=" + var8 + " appVersionCode=" + var12 + " oldAppVersionCode=" + var9 + " appMetadata=" + var13 + " oldAppVersionMetadata=" + var10);
         long var14 = System.currentTimeMillis();
         long var16 = var3.mPreferences.getLong("last_check", 0L);
         TbsLog.i("TbsDownload", "[TbsDownloader.needSendQueryRequest] timeLastCheck=" + var16 + " timeNow=" + var14);
         if (var2) {
            boolean var18 = var3.mPreferences.contains("last_check");
            TbsLog.i("TbsDownload", "[TbsDownloader.needSendQueryRequest] hasLaskCheckKey=" + var18);
            if (var18 && var16 == 0L) {
               var16 = var14;
            }
         }

         long var28 = var3.mPreferences.getLong("last_request_success", 0L);
         long var20 = var3.mPreferences.getLong("count_request_fail_in_24hours", 0L);
         long var22 = var3.getRetryInterval();
         TbsLog.i("TbsDownload", "retryInterval = " + var22 + " s");
         TbsPVConfig.releaseInstance();
         TbsPVConfig var24 = TbsPVConfig.getInstance(c);
         int var25 = var24.getEmergentCoreVersion();
         int var26 = TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version", 0);
         if (var14 - var16 > var22 * 1000L) {
            var5 = true;
         } else if (var25 > TbsInstaller.a().getTbsCoreInstalledVerInNolock(c) && var25 > var26) {
            var5 = true;
         } else if (TbsShareManager.isThirdPartyApp(c) && var28 > 0L && var14 - var28 > var22 * 1000L && var20 < 11L) {
            var5 = true;
         } else if (TbsShareManager.isThirdPartyApp(c) && TbsShareManager.findCoreForThirdPartyApp(c) == 0 && !e()) {
            var5 = true;
            TbsInstaller.a().d(c);
         } else if (var11 != null && var12 != 0 && var13 != null) {
            if (!var11.equals(var8) || var12 != var9 || !var13.equals(var10)) {
               var5 = true;
            }
         } else if (TbsShareManager.isThirdPartyApp(c)) {
            var7 = "timeNow - timeLastCheck is " + (var14 - var16) + " TbsShareManager.findCoreForThirdPartyApp(sAppContext) is " + TbsShareManager.findCoreForThirdPartyApp(c) + " sendRequestWithSameHostCoreVersion() is " + e() + " appVersionName is " + var11 + " appVersionCode is " + var12 + " appMetadata is " + var13 + " oldAppVersionName is " + var8 + " oldAppVersionCode is " + var9 + " oldAppVersionMetadata is " + var10;
         }
      } else {
         var5 = true;
      }

      if (!var5 && TbsShareManager.isThirdPartyApp(c)) {
         TbsLogReport.TbsLogInfo var27 = TbsLogReport.getInstance(c).tbsLogInfo();
         var27.setErrorCode(-119);
         var27.setFailDetail(var7);
         TbsLogReport.getInstance(c).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, var27);
      }

      return var5;
   }

   private static boolean a(Context var0, boolean var1) {
      TbsDownloadConfig var2 = TbsDownloadConfig.getInstance(var0);
      if (VERSION.SDK_INT < 8) {
         var2.setDownloadInterruptCode(-102);
         return false;
      } else if (!QbSdk.c && TbsShareManager.isThirdPartyApp(c) && !c()) {
         return false;
      } else {
         if (!var2.mPreferences.contains("is_oversea")) {
            if (var1 && !"com.tencent.mm".equals(var0.getApplicationInfo().packageName)) {
               var1 = false;
               TbsLog.i("TbsDownload", "needDownload-oversea is true, but not WX");
            }

            var2.mSyncMap.put("is_oversea", var1);
            var2.commit();
            j = var1;
            TbsLog.i("TbsDownload", "needDownload-first-called--isoversea = " + var1);
         }

         if (getOverSea(var0) && VERSION.SDK_INT != 16 && VERSION.SDK_INT != 17 && VERSION.SDK_INT != 18) {
            TbsLog.i("TbsDownload", "needDownload- return false,  because of  version is " + VERSION.SDK_INT + ", and overea");
            var2.setDownloadInterruptCode(-103);
            return false;
         } else {
            e = var2.mPreferences.getString("device_cpuabi", (String)null);
            if (!TextUtils.isEmpty(e)) {
               Matcher var3 = null;

               try {
                  var3 = Pattern.compile("i686|mips|x86_64").matcher(e);
               } catch (Exception var5) {
               }

               if (var3 != null && var3.find()) {
                  TbsLog.e("TbsDownload", "can not support x86 devices!!");
                  var2.setDownloadInterruptCode(-104);
                  return false;
               }
            }

            return true;
         }
      }
   }

   public static boolean needDownload(Context var0, boolean var1) {
      return needDownload(var0, var1, false, true, (TbsDownloader.TbsDownloaderCallback)null);
   }

   public static boolean needDownload(Context var0, boolean var1, boolean var2, TbsDownloader.TbsDownloaderCallback var3) {
      return needDownload(var0, var1, var2, true, var3);
   }

   public static boolean needDownload(Context var0, boolean var1, boolean var2, boolean var3, TbsDownloader.TbsDownloaderCallback var4) {
      TbsLog.i("TbsDownload", "needDownload,process=" + QbSdk.getCurrentProcessName(var0) + "stack=" + Log.getStackTraceString(new Throwable()));
      TbsDownloadUpload.clear();
      TbsDownloadUpload var5 = TbsDownloadUpload.getInstance(var0);
      var5.a.put("tbs_needdownload_code", 140);
      var5.commit();
      TbsLog.i("TbsDownload", "[TbsDownloader.needDownload] oversea=" + var1 + ",isDownloadForeground=" + var2);
      boolean var6 = false;
      TbsLog.initIfNeed(var0);
      if (TbsInstaller.b) {
         if (var4 != null) {
            var4.onNeedDownloadFinish(false, 0);
         }

         TbsLog.i("TbsDownload", "[TbsDownloader.needDownload]#1,return " + var6);
         var5.a.put("tbs_needdownload_return", var6 ? 170 : 171);
         var5.commit();
         return var6;
      } else {
         TbsLog.app_extra("TbsDownload", var0);
         c = var0.getApplicationContext();
         TbsDownloadConfig var7 = TbsDownloadConfig.getInstance(c);
         var7.setDownloadInterruptCode(-100);
         if (!a(c, var1)) {
            TbsLog.i("TbsDownload", "[TbsDownloader.needDownload]#2,return " + var6);
            var5.a.put("tbs_needdownload_code", 141);
            var5.commit();
            var5.a.put("tbs_needdownload_return", var6 ? 170 : 172);
            var5.commit();
            if (var4 != null) {
               var4.onNeedDownloadFinish(false, 0);
            }

            return var6;
         } else {
            d();
            if (i) {
               if (var4 != null) {
                  var4.onNeedDownloadFinish(false, 0);
               }

               var7.setDownloadInterruptCode(-105);
               TbsLog.i("TbsDownload", "[TbsDownloader.needDownload]#3,return " + var6);
               var5.a.put("tbs_needdownload_code", 142);
               var5.commit();
               var5.a.put("tbs_needdownload_return", 173);
               var5.commit();
               if (var4 != null) {
                  var4.onNeedDownloadFinish(false, 0);
               }

               return false;
            } else {
               boolean var8 = false;
               boolean var9 = a(c, var2, false);
               TbsLog.i("TbsDownload", "[TbsDownloader.needDownload],needSendRequest=" + var9);
               if (var9) {
                  a(var2, var4, var3);
                  var7.setDownloadInterruptCode(-114);
               } else {
                  var5.a.put("tbs_needdownload_code", 143);
                  var5.commit();
               }

               d.removeMessages(102);
               Message.obtain(d, 102).sendToTarget();
               boolean var10 = false;
               if (QbSdk.c || !TbsShareManager.isThirdPartyApp(var0)) {
                  var10 = var7.mPreferences.contains("tbs_needdownload");
                  TbsLog.i("TbsDownload", "[TbsDownloader.needDownload] hasNeedDownloadKey=" + var10);
                  if (!var10 && !TbsShareManager.isThirdPartyApp(var0)) {
                     var6 = true;
                  } else {
                     var6 = var7.mPreferences.getBoolean("tbs_needdownload", false);
                  }
               }

               TbsLog.i("TbsDownload", "[TbsDownloader.needDownload]#4,needDownload=" + var6 + ",hasNeedDownloadKey=" + var10);
               if (var6) {
                  if (!h()) {
                     var6 = false;
                     TbsLog.i("TbsDownload", "[TbsDownloader.needDownload]#5,set needDownload = false");
                  } else {
                     var7.setDownloadInterruptCode(-118);
                     TbsLog.i("TbsDownload", "[TbsDownloader.needDownload]#6");
                  }
               } else {
                  int var11 = TbsInstaller.a().getTbsCoreInstalledVerWithLock(c);
                  TbsLog.i("TbsDownload", "[TbsDownloader.needDownload]#7,tbsLocalVersion=" + var11 + ",needSendRequest=" + var9);
                  if (!var9 && var11 > 0) {
                     var7.setDownloadInterruptCode(-119);
                  } else {
                     d.removeMessages(103);
                     if (var11 <= 0 && !var9) {
                        Message.obtain(d, 103, 0, 0, c).sendToTarget();
                     } else {
                        Message.obtain(d, 103, 1, 0, c).sendToTarget();
                     }

                     var7.setDownloadInterruptCode(-121);
                  }
               }

               if (!var9 && var4 != null) {
                  var4.onNeedDownloadFinish(false, 0);
               }

               TbsLog.i("TbsDownload", "[TbsDownloader.needDownload] needDownload=" + var6);
               var5.a.put("tbs_needdownload_return", var6 ? 170 : 174);
               var5.commit();
               return var6;
            }
         }
      }
   }

   static boolean a(Context var0) {
      return TbsDownloadConfig.getInstance(var0).mPreferences.getInt("tbs_downloaddecouplecore", 0) == 1;
   }

   public static void startDownload(Context var0) {
      startDownload(var0, false);
   }

   public static synchronized void startDownload(Context var0, boolean var1) {
      TbsDownloadUpload var2 = TbsDownloadUpload.getInstance(var0);
      var2.a.put("tbs_startdownload_code", 160);
      var2.commit();
      TbsLog.i("TbsDownload", "[TbsDownloader.startDownload] sAppContext=" + c);
      if (TbsInstaller.b) {
         var2.a.put("tbs_startdownload_code", 161);
         var2.commit();
      } else {
         downloading = true;
         c = var0.getApplicationContext();
         TbsDownloadConfig.getInstance(c).setDownloadInterruptCode(-200);
         if (VERSION.SDK_INT < 8) {
            QbSdk.m.onDownloadFinish(110);
            TbsDownloadConfig.getInstance(c).setDownloadInterruptCode(-201);
            var2.a.put("tbs_startdownload_code", 162);
            var2.commit();
         } else {
            d();
            if (i) {
               QbSdk.m.onDownloadFinish(121);
               TbsDownloadConfig.getInstance(c).setDownloadInterruptCode(-202);
               var2.a.put("tbs_startdownload_code", 163);
               var2.commit();
            } else {
               if (var1) {
                  stopDownload();
               }

               d.removeMessages(101);
               d.removeMessages(100);
               Message var3 = Message.obtain(d, 101, QbSdk.m);
               var3.arg1 = var1 ? 1 : 0;
               var3.sendToTarget();
            }
         }
      }
   }

   public static int getCoreShareDecoupleCoreVersionByContext(Context var0) {
      return TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var0);
   }

   public static int getCoreShareDecoupleCoreVersion() {
      return TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(c);
   }

   public static boolean needDownloadDecoupleCore() {
      if (TbsShareManager.isThirdPartyApp(c)) {
         return false;
      } else if (a(c)) {
         return false;
      } else {
         long var0 = TbsDownloadConfig.getInstance(c).mPreferences.getLong("last_download_decouple_core", 0L);
         long var2 = System.currentTimeMillis();
         long var4 = TbsDownloadConfig.getInstance(c).getRetryInterval();
         if (var2 - var0 < var4 * 1000L) {
            return false;
         } else {
            int var6 = TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_decouplecoreversion", 0);
            return var6 > 0 && var6 != TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(c) && TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version", 0) != var6;
         }
      }
   }

   public static boolean startDecoupleCoreIfNeeded() {
      TbsLog.i("TbsDownload", "startDecoupleCoreIfNeeded ");
      if (TbsShareManager.isThirdPartyApp(c)) {
         return false;
      } else {
         TbsLog.i("TbsDownload", "startDecoupleCoreIfNeeded #1");
         if (a(c)) {
            return false;
         } else if (d == null) {
            return false;
         } else {
            TbsLog.i("TbsDownload", "startDecoupleCoreIfNeeded #2");
            long var0 = TbsDownloadConfig.getInstance(c).mPreferences.getLong("last_download_decouple_core", 0L);
            long var2 = System.currentTimeMillis();
            long var4 = TbsDownloadConfig.getInstance(c).getRetryInterval();
            if (var2 - var0 < var4 * 1000L) {
               return false;
            } else {
               TbsLog.i("TbsDownload", "startDecoupleCoreIfNeeded #3");
               int var6 = TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_decouplecoreversion", 0);
               if (var6 > 0 && var6 != TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(c)) {
                  if (TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version", 0) != var6 || TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version_type", 0) == 1) {
                     TbsLog.i("TbsDownload", "startDecoupleCoreIfNeeded #4");
                     downloading = true;
                     d.removeMessages(108);
                     Message var7 = Message.obtain(d, 108, QbSdk.m);
                     var7.arg1 = 0;
                     var7.sendToTarget();
                     TbsDownloadConfig.getInstance(c).mSyncMap.put("last_download_decouple_core", System.currentTimeMillis());
                     return true;
                  }

                  TbsLog.i("TbsDownload", "startDecoupleCoreIfNeeded no need, KEY_TBS_DOWNLOAD_V is " + TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version", 0) + " deCoupleCoreVersion is " + var6 + " KEY_TBS_DOWNLOAD_V_TYPE is " + TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version_type", 0));
               } else {
                  TbsLog.i("TbsDownload", "startDecoupleCoreIfNeeded no need, deCoupleCoreVersion is " + var6 + " getTbsCoreShareDecoupleCoreVersion is " + TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(c));
               }

               return false;
            }
         }
      }
   }

   public static void stopDownload() {
      if (!i) {
         TbsLog.i("TbsDownload", "[TbsDownloader.stopDownload]");
         if (g != null) {
            g.b();
         }

         if (d != null) {
            d.removeMessages(100);
            d.removeMessages(101);
            d.removeMessages(108);
         }

      }
   }

   public static synchronized boolean isDownloading() {
      TbsLog.i("TbsDownload", "[TbsDownloader.isDownloading] is " + downloading);
      return downloading;
   }

   public static boolean isDownloadForeground() {
      return g != null && g.isDownloadForeground();
   }

   private static synchronized void d() {
      if (h == null) {
         h = TbsHandlerThread.getInstance();

         try {
            g = new TbsDownload(c);
         } catch (Exception var1) {
            i = true;
            TbsLog.e("TbsDownload", "TbsApkDownloader init has Exception");
            return;
         }

         d = new Handler(h.getLooper()) {
            public void handleMessage(Message var1) {
               boolean var2 = false;
               boolean var3 = true;
               boolean var4;
               switch(var1.what) {
               case 100:
                  var2 = var1.arg1 == 1;
                  var3 = var1.arg2 == 1;
                  var4 = TbsDownloader.b(true, false, false, var3);
                  if (var1.obj != null && var1.obj instanceof TbsDownloader.TbsDownloaderCallback) {
                     TbsLog.i("TbsDownload", "needDownload-onNeedDownloadFinish needStartDownload=" + var4);
                     String var9 = "";
                     if (TbsDownloader.c != null && TbsDownloader.c.getApplicationContext() != null && TbsDownloader.c.getApplicationContext().getApplicationInfo() != null) {
                        var9 = TbsDownloader.c.getApplicationContext().getApplicationInfo().packageName;
                     }

                     if (var4 && !var2) {
                        if ("com.tencent.mm".equals(var9) || "com.tencent.mobileqq".equals(var9)) {
                           TbsLog.i("TbsDownload", "needDownload-onNeedDownloadFinish in mm or QQ callback needStartDownload = " + var4);
                           ((TbsDownloader.TbsDownloaderCallback)var1.obj).onNeedDownloadFinish(var4, TbsDownloadConfig.getInstance(TbsDownloader.c).mPreferences.getInt("tbs_download_version", 0));
                        }
                     } else {
                        ((TbsDownloader.TbsDownloaderCallback)var1.obj).onNeedDownloadFinish(var4, TbsDownloadConfig.getInstance(TbsDownloader.c).mPreferences.getInt("tbs_download_version", 0));
                     }
                  }

                  if (TbsShareManager.isThirdPartyApp(TbsDownloader.c) && var4) {
                     TbsDownloader.startDownload(TbsDownloader.c);
                  }
                  break;
               case 101:
               case 108:
                  if (Apn.getApnType(TbsDownloader.c) != 3 && !QbSdk.getDownloadWithoutWifi()) {
                     TbsLog.i("TbsDownload", "not wifi,no need send request");
                     return;
                  }

                  FileOutputStream var5 = null;
                  FileLock var6 = null;
                  if (!TbsShareManager.isThirdPartyApp(TbsDownloader.c)) {
                     String var7 = "tbs_download_lock_file" + TbsDownloadConfig.getInstance(TbsDownloader.c).mPreferences.getInt("tbs_download_version", 0) + ".txt";
                     var5 = FileHelper.getLockFile(TbsDownloader.c, false, var7);
                     if (var5 != null) {
                        var6 = FileHelper.lockStream(TbsDownloader.c, var5);
                        if (var6 == null) {
                           QbSdk.m.onDownloadFinish(177);
                           TbsLog.i("TbsDownload", "file lock locked,wx or qq is downloading");
                           TbsDownloadConfig.getInstance(TbsDownloader.c).setDownloadInterruptCode(-203);
                           TbsLog.i("TbsDownload", "MSG_START_DOWNLOAD_DECOUPLECORE return #1");
                           return;
                        }
                     } else if (FileHelper.hasWriteExternalStoragePermission(TbsDownloader.c)) {
                        TbsDownloadConfig.getInstance(TbsDownloader.c).setDownloadInterruptCode(-204);
                        TbsLog.i("TbsDownload", "MSG_START_DOWNLOAD_DECOUPLECORE return #2");
                        return;
                     }
                  }

                  var2 = var1.arg1 == 1;
                  TbsDownloadConfig var10 = TbsDownloadConfig.getInstance(TbsDownloader.c);
                  var4 = TbsDownloader.b(false, var2, 108 == var1.what, var3);
                  if (var4) {
                     if (var2 && TbsInstaller.a().b(TbsDownloader.c, TbsDownloadConfig.getInstance(TbsDownloader.c).mPreferences.getInt("tbs_download_version", 0))) {
                        QbSdk.m.onDownloadFinish(122);
                        var10.setDownloadInterruptCode(-213);
                     } else if (var10.mPreferences.getBoolean("tbs_needdownload", false)) {
                        TbsDownloadConfig.getInstance(TbsDownloader.c).setDownloadInterruptCode(-215);
                        TbsDownloader.g.startDownload(var2, 108 == var1.what);
                     } else {
                        QbSdk.m.onDownloadFinish(110);
                     }
                  } else {
                     QbSdk.m.onDownloadFinish(110);
                  }

                  TbsLog.i("TbsDownload", "------freeFileLock called :");
                  FileHelper.releaseFileLock(var6, var5);
                  break;
               case 102:
                  TbsLog.i("TbsDownload", "[TbsDownloader.handleMessage] MSG_REPORT_DOWNLOAD_STAT");
                  int var8 = TbsShareManager.isThirdPartyApp(TbsDownloader.c) ? TbsShareManager.a(TbsDownloader.c, false) : TbsInstaller.a().getTbsCoreInstalledVerWithLock(TbsDownloader.c);
                  TbsLog.i("TbsDownload", "[TbsDownloader.handleMessage] localTbsVersion=" + var8);
                  TbsDownloader.g.a(var8);
                  TbsLogReport.getInstance(TbsDownloader.c).dailyReport();
                  break;
               case 103:
                  TbsLog.i("TbsDownload", "[TbsDownloader.handleMessage] MSG_CONTINUEINSTALL_TBSCORE");
                  if (var1.arg1 == 0) {
                     TbsInstaller.a().a((Context)var1.obj, true);
                  } else {
                     TbsInstaller.a().a((Context)var1.obj, false);
                  }
                  break;
               case 104:
                  TbsLog.i("TbsDownload", "[TbsDownloader.handleMessage] MSG_UPLOAD_TBSLOG");
                  TbsLogReport.getInstance(TbsDownloader.c).reportTbsLog();
               case 105:
               case 106:
               case 107:
               default:
                  break;
               case 109:
                  if (TbsDownloader.g != null) {
                     TbsDownloader.g.f();
                  }
               }

            }
         };
      }

   }

   private static void a(boolean var0, TbsDownloader.TbsDownloaderCallback var1, boolean var2) {
      TbsLog.i("TbsDownload", "[TbsDownloader.queryConfig]");
      d.removeMessages(100);
      Message var3 = Message.obtain(d, 100);
      if (var1 != null) {
         var3.obj = var1;
      }

      var3.arg1 = 0;
      var3.arg1 = var0 ? 1 : 0;
      var3.arg2 = var2 ? 1 : 0;
      var3.sendToTarget();
   }

   private static boolean e() {
      try {
         JSONArray var0 = g();
         String var1 = TbsDownloadConfig.getInstance(c).mPreferences.getString("last_thirdapp_sendrequest_coreversion", "");
         return var1.equals(var0.toString());
      } catch (Exception var2) {
         return false;
      }
   }

   private static String[] f() {
      String[] var0;
      if (QbSdk.getOnlyDownload()) {
         var0 = new String[]{c.getApplicationContext().getPackageName()};
      } else {
         var0 = TbsShareManager.getCoreProviderAppList();
         String var1 = c.getApplicationContext().getPackageName();
         if (var1.equals(TbsShareManager.f(c))) {
            int var2 = var0.length;
            String[] var3 = new String[var2 + 1];
            System.arraycopy(var0, 0, var3, 0, var2);
            var3[var2] = var1;
            var0 = var3;
         }
      }

      return var0;
   }

   static boolean a(Context var0, int var1) {
      return VERSION.SDK_INT > 28 && var0.getApplicationInfo().targetSdkVersion > 28 && var1 > 0 && var1 < 45114;
   }

   private static void a(JSONArray var0) {
      String[] var1 = f();
      int var2 = var1.length;

      int var3;
      String var4;
      int var5;
      Context var6;
      boolean var7;
      int var8;
      for(var3 = 0; var3 < var2; ++var3) {
         var4 = var1[var3];
         var5 = TbsShareManager.getSharedTbsCoreVersion(c, var4);
         if (var5 > 0) {
            var6 = TbsShareManager.getPackageContext(c, var4, true);
            if (var6 != null && !TbsInstaller.a().f(var6)) {
               TbsLog.e("TbsDownload", "host check failed,packageName = " + var4);
            } else if (a(c, var5)) {
               TbsLog.i("TbsDownload", "add CoreVersionToJsonData,version+" + var5 + " is in black list");
            } else {
               var7 = false;

               for(var8 = 0; var8 < var0.length(); ++var8) {
                  if (var0.optInt(var8) == var5) {
                     var7 = true;
                     break;
                  }
               }

               if (!var7) {
                  var0.put(var5);
               }
            }
         }
      }

      var1 = f();
      var2 = var1.length;

      for(var3 = 0; var3 < var2; ++var3) {
         var4 = var1[var3];
         var5 = TbsShareManager.getCoreShareDecoupleCoreVersion(c, var4);
         if (var5 > 0) {
            var6 = TbsShareManager.getPackageContext(c, var4, true);
            if (var6 != null && !TbsInstaller.a().f(var6)) {
               TbsLog.e("TbsDownload", "host check failed,packageName = " + var4);
            } else {
               var7 = false;

               for(var8 = 0; var8 < var0.length(); ++var8) {
                  if (var0.optInt(var8) == var5) {
                     var7 = true;
                     break;
                  }
               }

               if (!var7) {
                  var0.put(var5);
               }
            }
         }
      }

   }

   private static void b(JSONArray var0) {
      if (TbsShareManager.getHostCorePathAppDefined() != null) {
         int var1 = TbsInstaller.a().getTbsCoreVersionIdDir(TbsShareManager.getHostCorePathAppDefined());
         boolean var2 = false;

         for(int var3 = 0; var3 < var0.length(); ++var3) {
            if (var0.optInt(var3) == var1) {
               var2 = true;
               break;
            }
         }

         if (!var2) {
            var0.put(var1);
         }
      }

   }

   private static JSONArray g() {
      if (TbsShareManager.isThirdPartyApp(c)) {
         JSONArray var0 = new JSONArray();
         a(var0);
         c(var0);
         b(var0);
         return var0;
      } else {
         return null;
      }
   }

   private static void c(JSONArray var0) {
      if (!TbsPVConfig.getInstance(c).isDisableHostBackupCore()) {
         String[] var1 = f();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String var4 = var1[var3];
            int var5 = TbsShareManager.getBackupCoreVersion(c, var4);
            Context var6;
            boolean var7;
            int var8;
            if (var5 > 0) {
               var6 = TbsShareManager.getPackageContext(c, var4, false);
               if (var6 != null && !TbsInstaller.a().f(var6)) {
                  TbsLog.e("TbsDownload", "host check failed,packageName = " + var4);
                  continue;
               }

               if (a(c, var5)) {
                  TbsLog.i("TbsDownload", "add addBackupVersionToJsonData,version+" + var5 + " is in black list");
                  continue;
               }

               var7 = false;

               for(var8 = 0; var8 < var0.length(); ++var8) {
                  if (var0.optInt(var8) == var5) {
                     var7 = true;
                     break;
                  }
               }

               if (!var7) {
                  var0.put(var5);
               }
            }

            var5 = TbsShareManager.getBackupDecoupleCoreVersion(c, var4);
            if (var5 > 0) {
               var6 = TbsShareManager.getPackageContext(c, var4, false);
               if (var6 != null && !TbsInstaller.a().f(var6)) {
                  TbsLog.e("TbsDownload", "host check failed,packageName = " + var4);
               } else {
                  var7 = false;

                  for(var8 = 0; var8 < var0.length(); ++var8) {
                     if (var0.optInt(var8) == var5) {
                        var7 = true;
                        break;
                     }
                  }

                  if (!var7) {
                     var0.put(var5);
                  }
               }
            }
         }
      }

   }

   private static JSONObject a(boolean var0, boolean var1, boolean var2) {
      TbsLog.i("TbsDownload", "[TbsDownloader.postJsonData]isQuery: " + var0 + " forDecoupleCore is " + var2);
      TbsDownloadConfig var3 = TbsDownloadConfig.getInstance(c);
      String var4 = b(c);
      String var5 = AppUtil.g(c);
      String var6 = AppUtil.f(c);
      String var7 = AppUtil.i(c);
      String var8 = "";
      String var9 = "";
      String var10 = "";
      var10 = TimeZone.getDefault().getID();
      if (var10 != null) {
         var8 = var10;
      }

      try {
         TelephonyManager var11 = (TelephonyManager)((TelephonyManager)c.getSystemService("phone"));
         if (var11 != null) {
            var10 = var11.getSimCountryIso();
         }
      } catch (Exception var20) {
         var20.printStackTrace();
      }

      if (var10 != null) {
         var9 = var10;
      }

      JSONObject var21 = new JSONObject();

      try {
         int var12 = TbsCoreInstallPropertiesHelper.getInstance(c).getIntProperty("tpatch_num");
         if (var12 < 5) {
            var21.put("REQUEST_TPATCH", 1);
         } else {
            var21.put("REQUEST_TPATCH", 0);
         }

         var21.put("TIMEZONEID", var8);
         var21.put("COUNTRYISO", var9);
         if (AppUtil.d()) {
            var21.put("REQUEST_64", 1);
         }

         var21.put("PROTOCOLVERSION", 1);
         boolean var13 = false;
         int var22;
         if (TbsShareManager.isThirdPartyApp(c)) {
            if (QbSdk.c) {
               var22 = TbsShareManager.a(c, false);
            } else {
               var22 = TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version", 0);
            }
         } else {
            if (var2) {
               var22 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(c);
            } else {
               var22 = TbsInstaller.a().getTbsCoreInstalledVerWithLock(c);
            }

            if (var22 == 0 && TbsInstaller.a().getHasTbsCoreShareConfigF(c)) {
               var22 = -1;
               if ("com.tencent.mobileqq".equals(c.getApplicationInfo().packageName)) {
                  TbsDownloadUpload.clear();
                  TbsDownloadUpload var14 = TbsDownloadUpload.getInstance(c);
                  var14.a.put("tbs_local_core_version", var22);
                  var14.commit();
                  TbsPVConfig.releaseInstance();
                  TbsPVConfig var15 = TbsPVConfig.getInstance(c);
                  int var16 = var15.getLocalCoreVersionMoreTimes();
                  if (var16 == 1) {
                     var22 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(c);
                  }
               }
            }

            TbsLog.i("TbsDownload", "[TbsDownloader.postJsonData] tbsLocalVersion=" + var22 + " isDownloadForeground=" + var1);
            if (var1) {
               var22 = TbsInstaller.a().getHasTbsCoreShareConfigF(c) ? var22 : 0;
            }
         }

         if (var0) {
            var21.put("FUNCTION", 2);
         } else {
            var21.put("FUNCTION", var22 == 0 ? 0 : 1);
         }

         JSONArray var23;
         if (TbsShareManager.isThirdPartyApp(c)) {
            var23 = g();
            var21.put("TBSVLARR", var23);
            var3.mSyncMap.put("last_thirdapp_sendrequest_coreversion", var23.toString());
            var3.commit();
            if (QbSdk.c) {
               var21.put("THIRDREQ", 1);
            }
         } else {
            var23 = a(var2);
            if (Apn.getApnType(c) != 3 && var23.length() != 0 && var22 == 0 && var0) {
               var21.put("TBSBACKUPARR", var23);
            }
         }

         String var24 = c.getPackageName();
         var21.put("APPN", var24);
         var21.put("APPVN", a(var3.mPreferences.getString("app_versionname", (String)null)));
         var21.put("APPVC", var3.mPreferences.getInt("app_versioncode", 0));
         var21.put("APPMETA", a(var3.mPreferences.getString("app_metadata", (String)null)));
         var21.put("TBSSDKV", 43967);
         var21.put("TBSV", var22);
         var21.put("DOWNLOADDECOUPLECORE", var2 ? 1 : 0);
         var3.mSyncMap.put("tbs_downloaddecouplecore", var2 ? 1 : 0);
         var3.commit();
         if (var22 != 0) {
            var21.put("TBSBACKUPV", g.c(var2));
         }

         var21.put("CPU", e);
         var21.put("UA", var4);
         var21.put("IMSI", a(var5));
         var21.put("IMEI", a(var6));
         var21.put("ANDROID_ID", a(var7));
         var21.put("GUID", AppUtil.e(c));
         if (!TbsShareManager.isThirdPartyApp(c)) {
            if (var22 != 0) {
               var21.put("STATUS", QbSdk.a(c, var22) ? 0 : 1);
            } else {
               var21.put("STATUS", 0);
            }

            var21.put("TBSDV", TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(c));
         }

         boolean var25 = false;
         boolean var26 = TbsDownloadConfig.getInstance(c).mPreferences.getBoolean("request_full_package", false);
         boolean var17 = false;
         Object var18 = QbSdk.a(c, (String)"can_unlzma", (Bundle)null);
         if (var18 != null && var18 instanceof Boolean) {
            var17 = (Boolean)var18;
         }

         if (var17) {
            var25 = !var26;
         } else {
            var25 = false;
         }

         if (var25) {
            var21.put("REQUEST_LZMA", 1);
         }

         if (getOverSea(c)) {
            var21.put("OVERSEA", 1);
         }

         if (var1) {
            var21.put("DOWNLOAD_FOREGROUND", 1);
         }
      } catch (Exception var19) {
      }

      TbsLog.i("TbsDownload", "[TbsDownloader.postJsonData] jsonData=" + var21.toString());
      return var21;
   }

   public static synchronized boolean getOverSea(Context var0) {
      if (!k) {
         k = true;
         TbsDownloadConfig var1 = TbsDownloadConfig.getInstance(var0);
         if (var1.mPreferences.contains("is_oversea")) {
            j = var1.mPreferences.getBoolean("is_oversea", false);
            TbsLog.i("TbsDownload", "[TbsDownloader.getOverSea]  first called. sOverSea = " + j);
         }

         TbsLog.i("TbsDownload", "[TbsDownloader.getOverSea]  sOverSea = " + j);
      }

      return j;
   }

   private static boolean b(final boolean var0, boolean var1, boolean var2, boolean var3) {
      TbsDownloadUpload var4;
      if (var0) {
         var4 = TbsDownloadUpload.getInstance(c);
         var4.a.put("tbs_needdownload_code", 144);
         var4.commit();
      } else if (!var2) {
         var4 = TbsDownloadUpload.getInstance(c);
         var4.a.put("tbs_startdownload_code", 164);
         var4.commit();
      }

      if (QbSdk.n != null && QbSdk.n.containsKey("SET_SENDREQUEST_AND_UPLOAD") && QbSdk.n.get("SET_SENDREQUEST_AND_UPLOAD").equals("false")) {
         TbsLog.i("TbsDownload", "[TbsDownloader.sendRequest] -- SET_SENDREQUEST_AND_UPLOAD is false");
         if (var0) {
            var4 = TbsDownloadUpload.getInstance(c);
            var4.a.put("tbs_needdownload_code", 145);
            var4.commit();
         } else if (!var2) {
            var4 = TbsDownloadUpload.getInstance(c);
            var4.a.put("tbs_startdownload_code", 165);
            var4.commit();
         }

         return false;
      } else {
         TbsLog.i("TbsDownload", "[TbsDownloader.sendRequest]isQuery: " + var0 + " forDecoupleCore is " + var2);
         boolean var23 = TbsInstaller.a().c(c);
         if (var23) {
            TbsLog.i("TbsDownload", "[TbsDownloader.sendRequest] -- isTbsLocalInstalled!");
            TbsDownloadUpload var24;
            if (var0) {
               var24 = TbsDownloadUpload.getInstance(c);
               var24.a.put("tbs_needdownload_code", 146);
               var24.commit();
            } else if (!var2) {
               var24 = TbsDownloadUpload.getInstance(c);
               var24.a.put("tbs_startdownload_code", 166);
               var24.commit();
            }

            return false;
         } else {
            boolean var5 = false;
            final TbsDownloadConfig var6 = TbsDownloadConfig.getInstance(c);
            File var7 = new File(FileHelper.getBackUpDir(c, 1), getOverSea(c) ? "x5.oversea.tbs.org" : getBackupFileName(false));
            File var8 = new File(FileHelper.getBackUpDir(c, 2), getOverSea(c) ? "x5.oversea.tbs.org" : getBackupFileName(false));
            File var9 = new File(FileHelper.getBackUpDir(c, 3), getOverSea(c) ? "x5.oversea.tbs.org" : getBackupFileName(false));
            File var10 = new File(FileHelper.getBackUpDir(c, 4), getOverSea(c) ? "x5.oversea.tbs.org" : getBackupFileName(false));
            if (!var10.exists()) {
               if (var9.exists()) {
                  var9.renameTo(var10);
               } else if (var8.exists()) {
                  var8.renameTo(var10);
               } else if (var7.exists()) {
                  var7.renameTo(var10);
               }
            }

            if (e == null) {
               e = AppUtil.b();
               var6.mSyncMap.put("device_cpuabi", e);
               var6.commit();
            }

            boolean var11 = false;
            if (!TextUtils.isEmpty(e)) {
               Matcher var12 = null;

               try {
                  var12 = Pattern.compile("i686|mips|x86_64").matcher(e);
               } catch (Exception var21) {
               }

               if (var12 != null && var12.find()) {
                  if (TbsShareManager.isThirdPartyApp(c)) {
                     TbsLog.e("TbsDownload", "don't support x86 devices,skip send request");
                     TbsLogReport.TbsLogInfo var13 = TbsLogReport.getInstance(c).tbsLogInfo();
                     if (var0) {
                        var6.setDownloadInterruptCode(-104);
                        var13.setErrorCode(-104);
                     } else {
                        var6.setDownloadInterruptCode(-205);
                        var13.setErrorCode(-205);
                     }

                     var13.setFailDetail("mycpu is " + e);
                     TbsLogReport.getInstance(c).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, var13);
                  } else if (var0) {
                     var6.setDownloadInterruptCode(-104);
                  } else {
                     var6.setDownloadInterruptCode(-205);
                  }

                  var11 = true;
               }
            }

            var6.mSyncMap.put("app_versionname", AppUtil.getVersionName(c));
            var6.mSyncMap.put("app_versioncode", AppUtil.getVersionCode(c));
            var6.commit();
            JSONObject var25 = a(var0, var1, var2);
            int var26 = -1;

            try {
               var26 = var25.getInt("TBSV");
            } catch (Exception var20) {
            }

            TbsDownloadUpload var29;
            if (var11 || var26 != -1) {
               long var14 = System.currentTimeMillis();
               if (TbsShareManager.isThirdPartyApp(c)) {
                  long var16 = var6.mPreferences.getLong("request_fail", 0L);
                  long var18 = var6.mPreferences.getLong("count_request_fail_in_24hours", 0L);
                  if (var14 - var16 < var6.getRetryInterval() * 1000L) {
                     ++var18;
                  } else {
                     var18 = 1L;
                  }

                  var6.mSyncMap.put("count_request_fail_in_24hours", var18);
               }

               var6.mSyncMap.put("request_fail", var14);
               var6.mSyncMap.put("app_versionname", AppUtil.getVersionName(c));
               var6.mSyncMap.put("app_versioncode", AppUtil.getVersionCode(c));
               var6.mSyncMap.put("app_metadata", AppUtil.getMetaHex(c, "com.tencent.mm.BuildInfo.CLIENT_VERSION"));
               var6.commit();
               if (var11) {
                  if (var0) {
                     var29 = TbsDownloadUpload.getInstance(c);
                     var29.a.put("tbs_needdownload_code", 147);
                     var29.commit();
                  } else if (!var2) {
                     var29 = TbsDownloadUpload.getInstance(c);
                     var29.a.put("tbs_startdownload_code", 167);
                     var29.commit();
                  }

                  return false;
               }
            }

            if (var26 == -1 && !var2) {
               TbsDownloadUpload var28;
               if (var0) {
                  var28 = TbsDownloadUpload.getInstance(c);
                  var28.a.put("tbs_needdownload_code", 149);
                  var28.commit();
               } else if (!var2) {
                  var28 = TbsDownloadUpload.getInstance(c);
                  var28.a.put("tbs_startdownload_code", 169);
                  var28.commit();
               }
            } else {
               try {
                  TbsCommonConfig var27 = TbsCommonConfig.getInstance(c);
                  String var15 = var27.d();
                  TbsLog.i("TbsDownload", "[TbsDownloader.sendRequest] postUrl=" + var15);
                  if (var0) {
                     var29 = TbsDownloadUpload.getInstance(c);
                     var29.a.put("tbs_needdownload_code", 148);
                     var29.a.put("tbs_needdownload_sent", 1);
                     var29.commit();
                     TbsLog.i("TbsDownload", "sendRequest query 148");
                  } else if (!var2) {
                     var29 = TbsDownloadUpload.getInstance(c);
                     var29.a.put("tbs_startdownload_code", 168);
                     var29.a.put("tbs_startdownload_sent", 1);
                     var29.commit();
                     TbsLog.i("TbsDownload", "sendRequest download 168");
                  }

                  String var30 = com.tencent.smtt.utils.f.a(var15, var25.toString().getBytes("utf-8"), new com.tencent.smtt.utils.f.a() {
                     public void a(int var1) {
                        long var2 = System.currentTimeMillis();
                        var6.mSyncMap.put("last_check", var2);
                        var6.commit();
                        TbsLog.i("TbsDownload", "[TbsDownloader.sendRequest] httpResponseCode=" + var1);
                        if (TbsShareManager.isThirdPartyApp(TbsDownloader.c) && var1 == 200) {
                           var6.mSyncMap.put("last_request_success", System.currentTimeMillis());
                           var6.mSyncMap.put("request_fail", 0L);
                           var6.mSyncMap.put("count_request_fail_in_24hours", 0L);
                           var6.commit();
                        }

                        if (var1 >= 300) {
                           if (var0) {
                              var6.setDownloadInterruptCode(-107);
                           } else {
                              var6.setDownloadInterruptCode(-207);
                           }
                        }

                     }
                  }, false);
                  var5 = a(var30, var26, var0, var1, var3);
               } catch (Throwable var22) {
                  var22.printStackTrace();
                  if (var0) {
                     var6.setDownloadInterruptCode(-106);
                  } else {
                     var6.setDownloadInterruptCode(-206);
                  }
               }
            }

            return var5;
         }
      }
   }

   @TargetApi(11)
   private static boolean a(String var0, int var1, boolean var2, boolean var3, boolean var4) throws Exception {
      TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] response=" + var0 + "isNeedInstall=" + var4);
      TbsDownloadConfig var5 = TbsDownloadConfig.getInstance(c);
      if (TextUtils.isEmpty(var0)) {
         if (var2) {
            var5.setDownloadInterruptCode(-108);
         } else {
            var5.setDownloadInterruptCode(-208);
         }

         TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] return #1,response is empty...");
         return false;
      } else {
         JSONObject var6 = new JSONObject(var0);
         int var7 = var6.getInt("RET");
         if (var7 != 0) {
            if (var2) {
               var5.setDownloadInterruptCode(-109);
            } else {
               var5.setDownloadInterruptCode(-209);
            }

            TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] return #2,returnCode=" + var7);
            return false;
         } else {
            int var8 = var6.getInt("RESPONSECODE");
            String var9 = var6.getString("DOWNLOADURL");
            String var10 = var6.optString("URLLIST", "");
            int var11 = var6.getInt("TBSAPKSERVERVERSION");
            int var12 = var6.getInt("DOWNLOADMAXFLOW");
            int var13 = var6.getInt("DOWNLOAD_MIN_FREE_SPACE");
            int var14 = var6.getInt("DOWNLOAD_SUCCESS_MAX_RETRYTIMES");
            int var15 = var6.getInt("DOWNLOAD_FAILED_MAX_RETRYTIMES");
            long var16 = var6.getLong("DOWNLOAD_SINGLE_TIMEOUT");
            long var18 = var6.getLong("TBSAPKFILESIZE");
            long var20 = var6.optLong("RETRY_INTERVAL", 0L);
            int var22 = var6.optInt("FLOWCTR", -1);
            int var23 = 0;

            try {
               var23 = var6.getInt("USEBBACKUPVER");
            } catch (Exception var48) {
            }

            var5.mSyncMap.put("use_backup_version", var23);
            int var50;
            if (var2 && QbSdk.i && TbsShareManager.isThirdPartyApp(c)) {
               boolean var24 = false;

               try {
                  var50 = var6.optInt("BUGLY", 0);
                  TbsExtensionFunctionManager.getInstance().setFunctionEnable(c, "bugly_switch.txt", var50 == 1);
               } catch (Throwable var47) {
                  TbsLog.i("qbsdk", "throwable:" + var47.toString());
               }
            }

            boolean var28;
            if (var2) {
               try {
                  var50 = var6.optInt("TEMPLATESWITCH", 0);
                  boolean var25 = (var50 & 1) != 0;
                  TbsExtensionFunctionManager.getInstance().setFunctionEnable(c, "cookie_switch.txt", var25);
                  TbsLog.w("TbsDownload", "useCookieCompatiable:" + var25);
                  boolean var26 = (var50 & 2) != 0;
                  TbsExtensionFunctionManager.getInstance().setFunctionEnable(c, "disable_get_apk_version_switch.txt", var26);
                  TbsLog.w("TbsDownload", "disableGetApkVersionByReadFile:" + var26);
                  boolean var27 = (var50 & 4) != 0;
                  TbsExtensionFunctionManager.getInstance().setFunctionEnable(c, "disable_unpreinit.txt", var27);
                  QbSdk.setDisableUnpreinitBySwitch(var27);
                  TbsLog.i("TbsDownload", "disableUnpreinitBySwitch:" + var27);
                  var28 = (var50 & 8) != 0;
                  TbsExtensionFunctionManager.getInstance().setFunctionEnable(c, "disable_use_host_backup_core.txt", var28);
                  QbSdk.setDisableUseHostBackupCoreBySwitch(var28);
                  TbsLog.i("TbsDownload", "disableUseHostBackupCoreBySwitch:" + var28);
               } catch (Throwable var46) {
                  TbsLog.i("qbsdk", "throwable:" + var46.toString());
               }
            }

            String var51 = null;
            int var52 = 0;
            int var53 = 0;
            int var54 = 0;
            var28 = false;
            boolean var29 = true;
            boolean var30 = true;
            String var31 = "";

            try {
               var51 = var6.getString("PKGMD5");
               var52 = var6.getInt("RESETX5");
               var54 = var6.getInt("UPLOADLOG");
               if (var6.has("RESETTOKEN")) {
                  var28 = var6.getInt("RESETTOKEN") != 0;
               }

               if (var6.has("SETTOKEN")) {
                  var31 = var6.getString("SETTOKEN");
               }

               if (var6.has("ENABLE_LOAD_RENAME_FILE_LOCK")) {
                  var29 = var6.getInt("ENABLE_LOAD_RENAME_FILE_LOCK") != 0;
               }

               if (var6.has("ENABLE_LOAD_RENAME_FILE_LOCK_WAIT")) {
                  var30 = var6.getInt("ENABLE_LOAD_RENAME_FILE_LOCK_WAIT") != 0;
               }
            } catch (Exception var45) {
            }

            try {
               var53 = var6.getInt("RESETDECOUPLECORE");
            } catch (Exception var44) {
            }

            int var32 = 0;

            try {
               var32 = var6.getInt("RESETTODECOUPLECORE");
            } catch (Exception var43) {
            }

            synchronized(f) {
               if (var28) {
                  var5.mSyncMap.put("tbs_deskey_token", "");
               }

               if (!TextUtils.isEmpty(var31) && var31.length() == 96) {
                  String var34 = var31 + "&" + DESedeUtils.c();
                  var5.mSyncMap.put("tbs_deskey_token", var34);
               }
            }

            if (var52 == 1) {
               if (var2) {
                  var5.setDownloadInterruptCode(-110);
               } else {
                  var5.setDownloadInterruptCode(-210);
               }

               QbSdk.reset(c, var32 == 1);
               TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] return #3,needResetTbs=1,isQuery=" + var2);
               return false;
            } else {
               if (!var29) {
                  var5.setTbsCoreLoadRenameFileLockEnable(var29);
               }

               if (!var30) {
                  var5.setTbsCoreLoadRenameFileLockWaitEnable(var30);
               }

               if (var53 == 1) {
                  QbSdk.resetDecoupleCore(c);
               }

               if (var54 == 1) {
                  d.removeMessages(104);
                  Message.obtain(d, 104).sendToTarget();
               }

               long var33 = 86400L;
               if (var22 == 1) {
                  if (var20 > 604800L) {
                     var20 = 604800L;
                  }

                  if (var20 > 0L) {
                     var33 = var20;
                  }
               }

               if (getRetryIntervalInSeconds() >= 0L) {
                  var33 = getRetryIntervalInSeconds();
               }

               var5.mSyncMap.put("retry_interval", var33);
               int var35 = 0;
               int var36 = 0;

               try {
                  if (var2) {
                     var35 = var6.getInt("DECOUPLECOREVERSION");
                  } else {
                     var35 = TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_decouplecoreversion", 0);
                  }
               } catch (Exception var42) {
               }

               try {
                  var36 = TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_downloaddecouplecore", 0);
               } catch (Exception var41) {
               }

               if (var2 && !TbsShareManager.isThirdPartyApp(c) && var35 == 0) {
                  var35 = TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(c);
               }

               TbsLog.i("TbsDownload", "in response decoupleCoreVersion is " + var35);
               var5.mSyncMap.put("tbs_decouplecoreversion", var35);
               var5.mSyncMap.put("tbs_downloaddecouplecore", var36);
               if (!TbsShareManager.isThirdPartyApp(c)) {
                  if (var35 > 0 && var35 != TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(c) && var35 == TbsInstaller.a().getTbsCoreInstalledVerInNolock(c)) {
                     TbsInstaller.a().coreShareCopyToDecouple(c);
                  } else if (var35 == 0) {
                     try {
                        File var37 = TbsInstaller.a().getTbsCoreShareDecoupleDir(c);
                        FileHelper.delete(var37);
                     } catch (Throwable var40) {
                     }
                  }
               }

               if (TextUtils.isEmpty(var9) && TbsShareManager.isThirdPartyApp(c)) {
                  var5.mSyncMap.put("tbs_needdownload", false);
                  var5.commit();
                  if (var2) {
                     TbsShareManager.writeCoreInfoForThirdPartyApp(c, var11, false);
                  }

                  TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] return #4,current app is third app...");
                  return false;
               } else {
                  TbsLog.i("TbsDownload", "in response responseCode is " + var8);
                  if (var8 == 0) {
                     var5.mSyncMap.put("tbs_responsecode", var8);
                     var5.mSyncMap.put("tbs_needdownload", false);
                     if (var2) {
                        var5.mSyncMap.put("tbs_download_interrupt_code_reason", -111);
                     } else {
                        var5.mSyncMap.put("tbs_download_interrupt_code_reason", -211);
                        var5.setDownloadInterruptCode(-211);
                     }

                     var5.commit();
                     if (!TbsShareManager.isThirdPartyApp(c)) {
                        startDecoupleCoreIfNeeded();
                     }

                     TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] return #5,responseCode=0");
                     return false;
                  } else {
                     int var55 = TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version", 0);
                     if (var55 > var11) {
                        g.c();
                        TbsInstaller.a().cleanStatusAndTmpDir(c);
                     }

                     boolean var38 = false;
                     if (!TbsShareManager.isThirdPartyApp(c)) {
                        int var39 = TbsInstaller.a().getTbsVersion(c, 0);
                        if (var39 >= var11) {
                           var38 = true;
                        }

                        TbsLog.i("TbsDownload", "tmpCoreVersion is " + var39 + " tbsDownloadVersion is" + var11);
                     }

                     if ((var1 >= var11 || TextUtils.isEmpty(var9) || var38) && var36 != 1) {
                        var5.mSyncMap.put("tbs_needdownload", false);
                        if (var2) {
                           if (TextUtils.isEmpty(var9)) {
                              var5.mSyncMap.put("tbs_download_interrupt_code_reason", -124);
                           } else if (var11 <= 0) {
                              var5.mSyncMap.put("tbs_download_interrupt_code_reason", -125);
                           } else if (var1 >= var11) {
                              var5.mSyncMap.put("tbs_download_interrupt_code_reason", -127);
                           } else {
                              var5.mSyncMap.put("tbs_download_interrupt_code_reason", -112);
                           }
                        } else {
                           short var57 = -212;
                           if (TextUtils.isEmpty(var9)) {
                              var57 = -217;
                           } else if (var11 <= 0) {
                              var57 = -218;
                           } else if (var1 >= var11) {
                              var57 = -219;
                           }

                           var5.mSyncMap.put("tbs_download_interrupt_code_reason", Integer.valueOf(var57));
                           var5.setDownloadInterruptCode(var57);
                        }

                        var5.commit();
                        TbsLog.i("TbsDownload", "version error or downloadUrl empty ,return ahead tbsLocalVersion=" + var1 + " tbsDownloadVersion=" + var11 + " tbsLastDownloadVersion=" + var55 + " downloadUrl=" + var9);
                        return false;
                     } else {
                        if (!var9.equals(var5.mPreferences.getString("tbs_downloadurl", (String)null))) {
                           g.c();
                           var5.mSyncMap.put("tbs_download_failed_retrytimes", 0);
                           var5.mSyncMap.put("tbs_download_success_retrytimes", 0);
                        }

                        var5.mSyncMap.put("tbs_download_version", var11);
                        TbsLog.i("TbsDownload", "put KEY_TBS_DOWNLOAD_V is " + var11);
                        if (var11 > 0) {
                           if (var36 == 1) {
                              var5.mSyncMap.put("tbs_download_version_type", 1);
                           } else {
                              var5.mSyncMap.put("tbs_download_version_type", 0);
                           }

                           TbsLog.i("TbsDownload", "put KEY_TBS_DOWNLOAD_V_TYPE is " + var36);
                        }

                        var5.mSyncMap.put("tbs_downloadurl", var9);
                        var5.mSyncMap.put("tbs_downloadurl_list", var10);
                        var5.mSyncMap.put("tbs_responsecode", var8);
                        var5.mSyncMap.put("tbs_download_maxflow", var12);
                        var5.mSyncMap.put("tbs_download_min_free_space", var13);
                        var5.mSyncMap.put("tbs_download_success_max_retrytimes", var14);
                        var5.mSyncMap.put("tbs_download_failed_max_retrytimes", var15);
                        var5.mSyncMap.put("tbs_single_timeout", var16);
                        var5.mSyncMap.put("tbs_apkfilesize", var18);
                        var5.commit();
                        if (var51 != null) {
                           var5.mSyncMap.put("tbs_apk_md5", var51);
                        }

                        if (!var3 && var4 && TbsInstaller.a().b(c, var11)) {
                           if (var2) {
                              var5.mSyncMap.put("tbs_download_interrupt_code_reason", -113);
                           } else {
                              var5.mSyncMap.put("tbs_download_interrupt_code_reason", -213);
                              var5.setDownloadInterruptCode(-213);
                           }

                           var5.mSyncMap.put("tbs_needdownload", false);
                           TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] ##6 set needDownload=false");
                        } else {
                           TbsLogReport.TbsLogInfo var56;
                           if (!var3 && var4 && g.a(var3, var8 == 1 || var8 == 2)) {
                              var5.mSyncMap.put("tbs_needdownload", false);
                              var56 = TbsLogReport.getInstance(c).tbsLogInfo();
                              var56.setErrorCode(100);
                              var56.setFailDetail("use local backup apk in needDownload" + g.a);
                              if (a(c)) {
                                 TbsLogReport.getInstance(c).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD_DECOUPLE, var56);
                              } else {
                                 TbsLogReport.getInstance(c).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, var56);
                              }

                              TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] ##7 set needDownload=false");
                           } else if (TbsDownloadConfig.getInstance(c).mPreferences.getInt("tbs_download_version_type", 0) == 1 && g.verifyAndInstallDecoupleCoreFromBackup()) {
                              var5.mSyncMap.put("tbs_needdownload", false);
                              var56 = TbsLogReport.getInstance(c).tbsLogInfo();
                              var56.setErrorCode(100);
                              var56.setFailDetail("installDecoupleCoreFromBackup" + g.a);
                              if (a(c)) {
                                 TbsLogReport.getInstance(c).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD_DECOUPLE, var56);
                              } else {
                                 TbsLogReport.getInstance(c).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, var56);
                              }

                              TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] ##8 set needDownload=false");
                           } else {
                              if (!var2) {
                                 var5.setDownloadInterruptCode(-216);
                              }

                              var5.mSyncMap.put("tbs_needdownload", true);
                              TbsLog.i("TbsDownload", "[TbsDownloader.readResponse] ##9 set needDownload=true");
                           }
                        }

                        if (var6.optInt("stop_pre_oat", 0) == 1) {
                           var5.mSyncMap.put("tbs_stop_preoat", true);
                        }

                        var5.commit();
                        return true;
                     }
                  }
               }
            }
         }
      }
   }

   public static void setRetryIntervalInSeconds(Context var0, long var1) {
      if (var0 != null) {
         if (var0.getApplicationInfo().packageName.equals("com.tencent.qqlive")) {
            l = var1;
         }

         TbsLog.i("TbsDownload", "mRetryIntervalInSeconds is " + l);
      }
   }

   public static long getRetryIntervalInSeconds() {
      return l;
   }

   static String b(Context var0) {
      if (!TextUtils.isEmpty(b)) {
         return b;
      } else {
         Locale var1 = Locale.getDefault();
         StringBuffer var2 = new StringBuffer();
         String var3 = VERSION.RELEASE;
         String var4 = null;

         try {
            var4 = new String(var3.getBytes("UTF-8"), "ISO8859-1");
         } catch (Exception var10) {
            var4 = var3;
         }

         if (var4 == null) {
            var2.append("1.0");
         } else if (var4.length() > 0) {
            var2.append(var4);
         } else {
            var2.append("1.0");
         }

         var2.append("; ");
         String var5 = var1.getLanguage();
         String var6;
         if (var5 != null) {
            var2.append(var5.toLowerCase());
            var6 = var1.getCountry();
            if (var6 != null) {
               var2.append("-");
               var2.append(var6.toLowerCase());
            }
         } else {
            var2.append("en");
         }

         String var7;
         if ("REL".equals(VERSION.CODENAME)) {
            var6 = Build.MODEL;
            var7 = null;

            try {
               var7 = new String(var6.getBytes("UTF-8"), "ISO8859-1");
            } catch (Exception var9) {
               var7 = var6;
            }

            if (var7 == null) {
               var2.append("; ");
            } else if (var7.length() > 0) {
               var2.append("; ");
               var2.append(var7);
            }
         }

         var6 = Build.ID == null ? "" : Build.ID;
         var7 = var6.replaceAll("[一-龥]", "");
         if (var7 == null) {
            var2.append(" Build/");
            var2.append("00");
         } else if (var7.length() > 0) {
            var2.append(" Build/");
            var2.append(var7);
         }

         return b = String.format("Mozilla/5.0 (Linux; U; Android %s) AppleWebKit/533.1 (KHTML, like Gecko)Version/4.0 Mobile Safari/533.1", var2);
      }
   }

   private static String a(String var0) {
      return var0 == null ? "" : var0;
   }

   @TargetApi(11)
   static void c(Context var0) {
      TbsDownloadConfig.getInstance(var0).clear();
      TbsLogReport.getInstance(var0).clear();
      TbsDownload.c(var0);
      SharedPreferences var1 = null;
      Editor var2 = null;
      if (VERSION.SDK_INT >= 11) {
         var1 = var0.getSharedPreferences("tbs_extension_config", 4);
      } else {
         var1 = var0.getSharedPreferences("tbs_extension_config", 0);
      }

      var2 = var1.edit();
      var2.clear().commit();
      if (VERSION.SDK_INT >= 11) {
         var1 = var0.getSharedPreferences("tbs_preloadx5_check_cfg_file", 4);
      } else {
         var1 = var0.getSharedPreferences("tbs_preloadx5_check_cfg_file", 0);
      }

      var2 = var1.edit();
      var2.clear().commit();
   }

   private static boolean h() {
      TbsDownloadConfig var0 = TbsDownloadConfig.getInstance(c);
      if (var0.mPreferences.getInt("tbs_download_success_retrytimes", 0) >= var0.getDownloadSuccessMaxRetrytimes()) {
         TbsLog.i("TbsDownload", "[TbsDownloader.needStartDownload] out of success retrytimes", true);
         var0.setDownloadInterruptCode(-115);
         return false;
      } else if (var0.mPreferences.getInt("tbs_download_failed_retrytimes", 0) >= var0.getDownloadFailedMaxRetrytimes()) {
         TbsLog.i("TbsDownload", "[TbsDownloader.needStartDownload] out of failed retrytimes", true);
         var0.setDownloadInterruptCode(-116);
         return false;
      } else if (!FileHelper.hasEnoughFreeSpace(c)) {
         TbsLog.i("TbsDownload", "[TbsDownloader.needStartDownload] local rom freespace limit", true);
         var0.setDownloadInterruptCode(-117);
         return false;
      } else {
         long var1 = System.currentTimeMillis();
         long var3 = var0.mPreferences.getLong("tbs_downloadstarttime", 0L);
         if (var1 - var3 <= 86400000L) {
            long var5 = var0.mPreferences.getLong("tbs_downloadflow", 0L);
            TbsLog.i("TbsDownload", "[TbsDownloader.needStartDownload] downloadFlow=" + var5);
            if (var5 >= var0.getDownloadMaxflow()) {
               TbsLog.i("TbsDownload", "[TbsDownloader.needStartDownload] failed because you exceeded max flow!", true);
               var0.setDownloadInterruptCode(-120);
               return false;
            }
         }

         return true;
      }
   }

   protected static File a(int var0) {
      String[] var1 = TbsShareManager.getCoreProviderAppList();
      File var2 = null;
      String[] var3 = var1;
      int var4 = var1.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String var6 = var3[var5];
         if (!var6.equals(c.getApplicationInfo().packageName)) {
            var2 = new File(FileHelper.getBackUpDir(c, var6, 4, false), getOverSea(c) ? "x5.oversea.tbs.org" : getBackupFileName(false));
            if (var2 != null && var2.exists()) {
               if (ApkMd5Util.getApkVersion(c, var2) == var0) {
                  TbsLog.i("TbsDownload", "local tbs version fond,path = " + var2.getAbsolutePath());
                  break;
               }

               TbsLog.i("TbsDownload", "version is not match");
            } else {
               TbsLog.i("TbsDownload", "can not find local backup core file");
            }
         }
      }

      return var2;
   }

   protected static File b(int var0) {
      String[] var1 = TbsShareManager.getCoreProviderAppList();
      File var2 = null;
      String[] var3 = var1;
      int var4 = var1.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String var6 = var3[var5];
         var2 = new File(FileHelper.getBackUpDir(c, var6, 4, false), getOverSea(c) ? "x5.oversea.tbs.org" : getBackupFileName(false));
         if (var2 != null && var2.exists() && ApkMd5Util.getApkVersion(c, var2) == var0) {
            TbsLog.i("TbsDownload", "local tbs version fond,path = " + var2.getAbsolutePath());
            break;
         }

         var2 = new File(FileHelper.getBackUpDir(c, var6, 4, false), "x5.tbs.decouple");
         if (var2 != null && var2.exists() && ApkMd5Util.getApkVersion(c, var2) == var0) {
            TbsLog.i("TbsDownload", "local tbs version fond,path = " + var2.getAbsolutePath());
            break;
         }
      }

      return var2;
   }

   private static JSONArray a(boolean var0) {
      JSONArray var1 = new JSONArray();
      String[] var2 = TbsShareManager.getCoreProviderAppList();
      String[] var3 = var2;
      int var4 = var2.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String var6 = var3[var5];
         File var7 = null;
         if (var0) {
            var7 = new File(FileHelper.getBackUpDir(c, var6, 4, false), getOverSea(c) ? "x5.oversea.tbs.org" : getBackupFileName(false));
         } else {
            var7 = new File(FileHelper.getBackUpDir(c, var6, 4, false), "x5.tbs.decouple");
         }

         if (var7 != null && var7.exists()) {
            long var8 = (long) ApkMd5Util.getApkVersion(c, var7);
            if (var8 > 0L) {
               boolean var10 = false;

               for(int var11 = 0; var11 < var1.length(); ++var11) {
                  if ((long)var1.optInt(var11) == var8) {
                     var10 = true;
                     break;
                  }
               }

               if (!var10) {
                  var1.put(var8);
               }
            }
         }
      }

      return var1;
   }

   public static void pauseDownload() {
      TbsLog.i("TbsDownload", "called pauseDownload,downloader=" + g);
      if (g != null) {
         g.pauseDownload();
      }

   }

   public static void resumeDownload() {
      TbsLog.i("TbsDownload", "called resumeDownload,downloader=" + g);
      if (d != null) {
         d.removeMessages(109);
         d.sendEmptyMessage(109);
      }

   }

   public interface TbsDownloaderCallback {
      void onNeedDownloadFinish(boolean var1, int var2);
   }
}
