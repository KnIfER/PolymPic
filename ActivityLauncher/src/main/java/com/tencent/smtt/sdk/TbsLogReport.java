package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import com.tencent.smtt.utils.Apn;
import com.tencent.smtt.utils.AppUtil;
import com.tencent.smtt.utils.DESedeUtils;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.QUA2Util;
import com.tencent.smtt.utils.TbsCommonConfig;
import com.tencent.smtt.utils.TbsLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.json.JSONArray;

public class TbsLogReport {
   private static TbsLogReport a;
   private Handler b = null;
   private Context c;
   private boolean d = false;

   private TbsLogReport(Context var1) {
      this.c = var1.getApplicationContext();
      HandlerThread var2 = new HandlerThread("TbsLogReportThread");
      var2.start();
      this.b = new Handler(var2.getLooper()) {
         public void handleMessage(Message var1) {
            if (var1.what == 600) {
               TbsLogReport.TbsLogInfo var2 = null;
               if (var1.obj instanceof TbsLogReport.TbsLogInfo) {
                  try {
                     var2 = (TbsLogReport.TbsLogInfo)var1.obj;
                     int var3 = var1.arg1;
                     TbsLogReport.this.a(var3, var2);
                  } catch (Exception var4) {
                     var4.printStackTrace();
                  }
               }
            } else if (var1.what == 601) {
               TbsLogReport.this.b();
            }

         }
      };
   }

   public static TbsLogReport getInstance(Context var0) {
      if (a == null) {
         Class var1 = TbsLogReport.class;
         synchronized(TbsLogReport.class) {
            if (a == null) {
               a = new TbsLogReport(var0);
            }
         }
      }

      return a;
   }

   public TbsLogReport.TbsLogInfo tbsLogInfo() {
      return new TbsLogReport.TbsLogInfo();
   }

   public void setInstallErrorCode(int var1, String var2) {
      this.setInstallErrorCode(var1, var2, TbsLogReport.EventType.TYPE_INSTALL);
   }

   public void setInstallErrorCode(int var1, String var2, TbsLogReport.EventType var3) {
      if (var1 != 200 && var1 != 220 && var1 != 221) {
         TbsLog.i("TbsDownload", "error occured in installation, errorCode:" + var1, true);
      }

      TbsLogReport.TbsLogInfo var4 = this.tbsLogInfo();
      var4.setFailDetail(var2);
      this.a(var1, var4, var3);
   }

   private void a(int var1, TbsLogReport.TbsLogInfo var2, TbsLogReport.EventType var3) {
      var2.setErrorCode(var1);
      var2.setEventTime(System.currentTimeMillis());
      QbSdk.m.onInstallFinish(var1);
      this.eventReport(var3, var2);
   }

   public void setInstallErrorCode(int var1, Throwable var2) {
      TbsLogReport.TbsLogInfo var3 = this.tbsLogInfo();
      var3.setFailDetail(var2);
      this.a(var1, var3, TbsLogReport.EventType.TYPE_INSTALL);
   }

   public void setLoadErrorCode(int var1, String var2) {
      TbsLogReport.TbsLogInfo var3 = this.tbsLogInfo();
      var3.setErrorCode(var1);
      var3.setEventTime(System.currentTimeMillis());
      var3.setFailDetail(var2);
      this.eventReport(TbsLogReport.EventType.TYPE_LOAD, var3);
   }

   public void setLoadErrorCode(int var1, Throwable var2) {
      String var3 = "NULL";
      if (var2 != null) {
         String var4 = "msg: " + var2.getMessage() + "; err: " + var2 + "; cause: " + Log.getStackTraceString(var2.getCause());
         int var5 = var4.length();
         var3 = var5 > 1024 ? var4.substring(0, 1024) : var4;
      }

      this.setLoadErrorCode(var1, var3);
   }

   public void dailyReport() {
      this.b.sendEmptyMessage(601);
   }

   public void eventReport(TbsLogReport.EventType var1, TbsLogReport.TbsLogInfo var2) {
      try {
         TbsLogReport.TbsLogInfo var3 = (TbsLogReport.TbsLogInfo)var2.clone();
         Message var4 = this.b.obtainMessage();
         var4.what = 600;
         var4.arg1 = var1.a;
         var4.obj = var3;
         this.b.sendMessage(var4);
      } catch (Throwable var5) {
         TbsLog.w("upload", "[TbsLogReport.eventReport] error, message=" + var5.getMessage());
      }

   }

   private void a(int var1, TbsLogReport.TbsLogInfo var2) {
      StringBuilder var3 = new StringBuilder();
      var3.append(this.a(var1));
      var3.append(this.a(AppUtil.f(this.c)));
      var3.append(this.a(QUA2Util.takeSnapshot(this.c)));
      var3.append(this.a(TbsInstaller.a().getTbsCoreInstalledVerInNolock(this.c)));
      String var4 = Build.MODEL;
      String var5 = null;

      try {
         var5 = new String(var4.getBytes("UTF-8"), "ISO8859-1");
      } catch (Exception var16) {
         var5 = var4;
      }

      var3.append(this.a(var5));
      String var6 = this.c.getPackageName();
      var3.append(this.a(var6));
      if ("com.tencent.mm".equals(var6)) {
         var3.append(this.a(AppUtil.getMetaHex(this.c, "com.tencent.mm.BuildInfo.CLIENT_VERSION")));
      } else {
         var3.append(this.a(AppUtil.getVersionCode(this.c)));
      }

      var3.append(this.a(this.a(var2.b)));
      var3.append(this.a(var2.c));
      var3.append(this.a(var2.d));
      var3.append(this.a(var2.e));
      var3.append(this.a(var2.f));
      var3.append(this.a(var2.g));
      var3.append(this.a(var2.h));
      var3.append(this.a(var2.i));
      var3.append(this.a(var2.j));
      var3.append(this.a(var2.k));
      var3.append(this.b(var2.q));
      var3.append(this.b(var2.l));
      var3.append(this.b(var2.m));
      var3.append(this.a(var2.n));
      var3.append(this.a(var2.a));
      var3.append(this.a(var2.o));
      var3.append(this.a(var2.p));
      var3.append(this.a(TbsDownloadConfig.getInstance(this.c).mPreferences.getInt("tbs_download_version", 0)));
      var3.append(this.a(AppUtil.i(this.c)));
      String var7 = "4.3.0.67_43967";
      var3.append(this.a(var7));
      boolean var8 = false;
      var3.append(var8);
      SharedPreferences var10 = this.d();
      JSONArray var11 = this.a();
      JSONArray var12 = new JSONArray();
      if (var12.length() >= 5) {
         for(int var13 = 4; var13 >= 1; --var13) {
            try {
               var12.put(var11.get(var12.length() - var13));
            } catch (Exception var15) {
               TbsLog.e("upload", "JSONArray transform error!");
            }
         }
      } else {
         var12 = var11;
      }

      var12.put(var3.toString());
      Editor var17 = var10.edit();
      var17.putString("tbs_download_upload", var12.toString());
      var17.commit();
      if (this.d || var1 != TbsLogReport.EventType.TYPE_LOAD.a) {
         this.b();
      }

   }

   private JSONArray a() {
      String var1 = this.d().getString("tbs_download_upload", (String)null);
      JSONArray var2 = null;
      if (var1 == null) {
         var2 = new JSONArray();
      } else {
         try {
            var2 = new JSONArray(var1);
            if (var2.length() > 5) {
               JSONArray var3 = new JSONArray();
               int var4 = var2.length() - 1;
               if (var4 > var2.length() - 5) {
                  var3.put(var2.get(var4));
                  return var3;
               }
            }
         } catch (Exception var5) {
            var2 = new JSONArray();
         }
      }

      return var2;
   }

   public void reportTbsLog() {
      if (QbSdk.n != null && QbSdk.n.containsKey("SET_SENDREQUEST_AND_UPLOAD") && QbSdk.n.get("SET_SENDREQUEST_AND_UPLOAD").equals("false")) {
         TbsLog.i("upload", "[TbsLogReport.reportTbsLog] -- SET_SENDREQUEST_AND_UPLOAD is false");
      } else if (Apn.getApnType(this.c) == 3) {
         String var1 = TbsLog.getTbsLogFilePath();
         if (var1 != null) {
            String var2 = DESedeUtils.getInstance().b();
            String var3 = AppUtil.f(this.c);
            String var4 = AppUtil.i(this.c);
            byte[] var5 = var3.getBytes();
            byte[] var6 = var4.getBytes();

            try {
               var5 = DESedeUtils.getInstance().a(var5);
               var6 = DESedeUtils.getInstance().a(var6);
            } catch (Exception var30) {
            }

            var3 = DESedeUtils.b(var5);
            var4 = DESedeUtils.b(var6);
            String var7 = TbsCommonConfig.getInstance(this.c).e() + var3 + "&aid=" + var4;
            HashMap var8 = new HashMap();
            var8.put("Content-Type", "application/octet-stream");
            var8.put("Charset", "UTF-8");
            var8.put("QUA2", QUA2Util.takeSnapshot(this.c));
            FileInputStream var9 = null;
            File var10 = null;
            ByteArrayOutputStream var11 = null;
            byte[] var12 = null;

            try {
               new File(FileHelper.a);
               TbsLogReport.a var14 = new TbsLogReport.a(var1, FileHelper.a + "/tbslog_temp.zip");
               var14.a();
               var10 = new File(FileHelper.a, "tbslog_temp.zip");
               var9 = new FileInputStream(var10);
               boolean var15 = false;
               byte[] var16 = new byte[8192];
               var11 = new ByteArrayOutputStream();

               int var33;
               while((var33 = var9.read(var16)) != -1) {
                  var11.write(var16, 0, var33);
               }

               var11.flush();
               var12 = DESedeUtils.getInstance().a(var11.toByteArray());
            } catch (Exception var31) {
               var31.printStackTrace();
            } finally {
               try {
                  if (var9 != null) {
                     var9.close();
                  }
               } catch (Exception var29) {
               }

               try {
                  if (var11 != null) {
                     var11.close();
                  }
               } catch (Exception var28) {
               }

               if (var10 != null) {
                  var10.delete();
               }

            }

            var7 = var7 + "&ek=" + var2;
            com.tencent.smtt.utils.f.a(var7, var8, var12, new com.tencent.smtt.utils.f.a() {
               public void a(int var1) {
                  TbsLog.i("TbsDownload", "[TbsApkDownloadStat.reportTbsLog] httpResponseCode=" + var1);
               }
            }, false);
         }
      }
   }

   private void b() {
      if (QbSdk.n != null && QbSdk.n.containsKey("SET_SENDREQUEST_AND_UPLOAD") && QbSdk.n.get("SET_SENDREQUEST_AND_UPLOAD").equals("false")) {
         TbsLog.i("upload", "[TbsLogReport.sendLogReportRequest] -- SET_SENDREQUEST_AND_UPLOAD is false");
      } else {
         TbsLog.i("TbsDownload", "[TbsApkDownloadStat.reportDownloadStat]");
         JSONArray var1 = this.a();
         if (var1 != null && var1.length() != 0) {
            TbsLog.i("TbsDownload", "[TbsApkDownloadStat.reportDownloadStat] jsonArray:" + var1);

            try {
               TbsCommonConfig var2 = TbsCommonConfig.getInstance(this.c);
               String var3 = var2.c();
               String var4 = com.tencent.smtt.utils.f.a(var3, var1.toString().getBytes("utf-8"), new com.tencent.smtt.utils.f.a() {
                  public void a(int var1) {
                     TbsLog.i("TbsDownload", "[TbsApkDownloadStat.reportDownloadStat] onHttpResponseCode:" + var1);
                     if (var1 < 300) {
                        TbsLogReport.this.c();
                     }

                  }
               }, true);
               TbsLog.i("TbsDownload", "[TbsApkDownloadStat.reportDownloadStat] response:" + var4 + " testcase: " + -1);
            } catch (Throwable var5) {
               var5.printStackTrace();
            }

         } else {
            TbsLog.i("TbsDownload", "[TbsApkDownloadStat.reportDownloadStat] no data");
         }
      }
   }

   private void c() {
      Editor var1 = this.d().edit();
      var1.remove("tbs_download_upload");
      var1.commit();
   }

   private String a(long var1) {
      String var3 = null;

      try {
         SimpleDateFormat var4 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
         var3 = var4.format(new Date(var1));
      } catch (Exception var5) {
      }

      return var3;
   }

   private SharedPreferences d() {
      return this.c.getSharedPreferences("tbs_download_stat", 4);
   }

   private String a(String var1) {
      return (var1 == null ? "" : var1) + "|";
   }

   private String a(int var1) {
      return var1 + "|";
   }

   private String b(long var1) {
      return var1 + "|";
   }

   public void clear() {
      try {
         Editor var1 = this.d().edit();
         var1.clear();
         var1.commit();
      } catch (Exception var2) {
      }

   }

   public void setShouldUploadEventReport(boolean var1) {
      this.d = var1;
   }

   public boolean getShouldUploadEventReport() {
      return this.d;
   }

   private static class a {
      private final String a;
      private final String b;

      public a(String var1, String var2) {
         this.a = var1;
         this.b = var2;
      }

      public void a() {
         FileOutputStream var1 = null;
         ZipOutputStream var2 = null;

         try {
            var1 = new FileOutputStream(this.b);
            var2 = new ZipOutputStream(new BufferedOutputStream(var1));
            byte[] var3 = new byte[2048];
            String var4 = this.a;
            FileInputStream var5 = null;
            BufferedInputStream var6 = null;

            try {
               var5 = new FileInputStream(var4);
               var6 = new BufferedInputStream(var5, 2048);
               ZipEntry var7 = new ZipEntry(var4.substring(var4.lastIndexOf("/") + 1));
               var2.putNextEntry(var7);

               int var8;
               while((var8 = var6.read(var3, 0, 2048)) != -1) {
                  var2.write(var3, 0, var8);
               }

               var2.flush();
               var2.closeEntry();
            } catch (Exception var45) {
               var45.printStackTrace();
            } finally {
               if (var6 != null) {
                  try {
                     var6.close();
                  } catch (IOException var44) {
                     var44.printStackTrace();
                  }
               }

               if (var5 != null) {
                  try {
                     var5.close();
                  } catch (IOException var43) {
                     var43.printStackTrace();
                  }
               }

            }

            a(new File(this.b));
         } catch (Exception var47) {
            var47.printStackTrace();
         } finally {
            if (var2 != null) {
               try {
                  var2.close();
               } catch (IOException var42) {
                  var42.printStackTrace();
               }
            }

            if (var1 != null) {
               try {
                  var1.close();
               } catch (IOException var41) {
                  var41.printStackTrace();
               }
            }

         }

      }

      private static void a(File var0) throws IOException {
         RandomAccessFile var1 = null;

         try {
            var1 = new RandomAccessFile(var0, "rw");
            if (var1 != null) {
               int var2 = Integer.parseInt("00001000", 2);
               var1.seek(7L);
               int var3 = var1.read();
               if ((var3 & var2) > 0) {
                  var1.seek(7L);
                  var2 = ~var2 & 255;
                  var1.write(var3 & var2);
               }
            }
         } catch (Exception var12) {
            var12.printStackTrace();
         } finally {
            if (var1 != null) {
               try {
                  var1.close();
               } catch (IOException var11) {
                  var11.printStackTrace();
               }
            }

         }

      }
   }

   public static class TbsLogInfo implements Cloneable {
      private long b;
      private String c;
      private String d;
      private int e;
      private int f;
      private int g;
      private int h;
      private String i;
      private int j;
      private int k;
      private long l;
      private long m;
      private int n;
      int a;
      private String o;
      private String p;
      private long q;

      private TbsLogInfo() {
         this.resetArgs();
      }

      protected Object clone() {
         try {
            return super.clone();
         } catch (CloneNotSupportedException var2) {
            return this;
         }
      }

      public void resetArgs() {
         this.b = 0L;
         this.c = null;
         this.d = null;
         this.e = 0;
         this.f = 0;
         this.g = 0;
         this.h = 2;
         this.i = "unknown";
         this.j = 0;
         this.k = 2;
         this.l = 0L;
         this.m = 0L;
         this.n = 1;
         this.a = 0;
         this.o = null;
         this.p = null;
         this.q = 0L;
      }

      public void setEventTime(long var1) {
         this.b = var1;
      }

      public void setDownloadUrl(String var1) {
         if (this.c == null) {
            this.c = var1;
         } else {
            this.c = this.c + ";" + var1;
         }

      }

      public void setResolveIp(String var1) {
         this.d = var1;
      }

      public void setHttpCode(int var1) {
         this.e = var1;
      }

      public void setPatchUpdateFlag(int var1) {
         this.f = var1;
      }

      public void setDownloadCancel(int var1) {
         this.g = var1;
      }

      public void setUnpkgFlag(int var1) {
         this.h = var1;
      }

      public void setApn(String var1) {
         this.i = var1;
      }

      public void setNetworkType(int var1) {
         this.j = var1;
      }

      public void setDownFinalFlag(int var1) {
         this.k = var1;
      }

      public int getDownFinalFlag() {
         return this.k;
      }

      public void setPkgSize(long var1) {
         this.l = var1;
      }

      public void setDownConsumeTime(long var1) {
         this.m += var1;
      }

      public void setNetworkChange(int var1) {
         this.n = var1;
      }

      public void setErrorCode(int var1) {
         if (var1 != 100 && var1 != 110 && var1 != 120 && var1 != 111 && var1 < 400) {
            TbsLog.i("TbsDownload", "error occured, errorCode:" + var1, true);
         }

         if (var1 == 111) {
            TbsLog.i("TbsDownload", "you are not in wifi, downloading stoped", true);
         }

         this.a = var1;
      }

      public void setCheckErrorDetail(String var1) {
         this.setErrorCode(108);
         this.o = var1;
      }

      public void setFailDetail(String var1) {
         if (var1 != null) {
            int var2 = var1.length();
            this.p = var2 > 1024 ? var1.substring(0, 1024) : var1;
         }
      }

      public void setFailDetail(Throwable var1) {
         if (var1 == null) {
            this.p = "";
         } else {
            String var2 = Log.getStackTraceString(var1);
            int var3 = var2.length();
            this.p = var3 > 1024 ? var2.substring(0, 1024) : var2;
         }
      }

      public void setDownloadSize(long var1) {
         this.q += var1;
      }

      // $FF: synthetic method
      TbsLogInfo(Object var1) {
         this();
      }
   }

   public static enum EventType {
      TYPE_DOWNLOAD(0),
      TYPE_INSTALL(1),
      TYPE_LOAD(2),
      TYPE_DOWNLOAD_DECOUPLE(3),
      TYPE_INSTALL_DECOUPLE(4),
      TYPE_COOKIE_DB_SWITCH(5),
      TYPE_SDK_REPORT_INFO(6);

      int a;

      private EventType(int var3) {
         this.a = var3;
      }
   }
}
