package com.tencent.smtt.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.text.TextUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

public class TbsCommonConfig {
   private Context a = null;
   private File b = null;
   private static TbsCommonConfig instance = null;
   private String pvPostUrl = "https://log.tbs.qq.com/ajax?c=pu&v=2&k=";
   private String pvPostUrlTk = "https://log.tbs.qq.com/ajax?c=pu&tk=";
   private String tbsDownloadStatPostUrl = "https://log.tbs.qq.com/ajax?c=dl&k=";
   private String tbsDownloaderPostUrl = "https://cfg.imtt.qq.com/tbs?v=2&mk=";
   private String tbsLogPostUrl = "https://log.tbs.qq.com/ajax?c=ul&v=2&k=";
   private String tipsUrl = "https://mqqad.html5.qq.com/adjs";
   private String tbsCmdPostUrl = "https://log.tbs.qq.com/ajax?c=ucfu&k=";
   private String tbsEmergencyPostUrl = "https://tbsrecovery.imtt.qq.com/getconfig";

   public static synchronized TbsCommonConfig getInstance(Context context) {
      if (instance == null) {
         instance = new TbsCommonConfig(context);
      }

      return instance;
   }

   public static synchronized TbsCommonConfig getInstance() {
      return instance;
   }

   @TargetApi(11)
   private TbsCommonConfig(Context context) {
      TbsLog.w("TbsCommonConfig", "TbsCommonConfig constructing...");
      this.a = context.getApplicationContext();
      this.readProperties();
   }

   private synchronized void readProperties() {
      BufferedInputStream bufferedInputStream = null;

      try {
         File var2 = this.i();
         if (var2 == null) {
            TbsLog.e("TbsCommonConfig", "Config file is null, default values will be applied");
            return;
         }

         FileInputStream fileInputStream = new FileInputStream(var2);
         bufferedInputStream = new BufferedInputStream(fileInputStream);
         Properties properties = new Properties();
         properties.load(bufferedInputStream);
         String value = properties.getProperty("pv_post_url", "");
         if (!"".equals(value)) {
            this.pvPostUrl = value;
         }

         value = properties.getProperty("tbs_download_stat_post_url", "");
         if (!"".equals(value)) {
            this.tbsDownloadStatPostUrl = value;
         }

         value = properties.getProperty("tbs_downloader_post_url", "");
         if (!"".equals(value)) {
            this.tbsDownloaderPostUrl = value;
         }

         value = properties.getProperty("tbs_log_post_url", "");
         if (!"".equals(value)) {
            this.tbsLogPostUrl = value;
         }

         value = properties.getProperty("tips_url", "");
         if (!"".equals(value)) {
            this.tipsUrl = value;
         }

         value = properties.getProperty("tbs_cmd_post_url", "");
         if (!"".equals(value)) {
            this.tbsCmdPostUrl = value;
         }

         value = properties.getProperty("tbs_emergency_post_url", "");
         if (!"".equals(value)) {
            this.tbsEmergencyPostUrl = value;
         }

         value = properties.getProperty("pv_post_url_tk", "");
         if (!"".equals(value)) {
            this.pvPostUrlTk = value;
         }
      } catch (Throwable throwable) {
         StringWriter stringWriter = new StringWriter();
         throwable.printStackTrace(new PrintWriter(stringWriter));
         TbsLog.e("TbsCommonConfig", "exceptions occurred1:" + stringWriter.toString());
      } finally {
         if (bufferedInputStream != null) {
            try {
               bufferedInputStream.close();
            } catch (IOException var14) {
               var14.printStackTrace();
            }
         }

      }

   }

   private File i() {
      File ret = null;

      try {
         if (this.b == null) {
            String packageName = this.a.getApplicationContext().getApplicationInfo().packageName;
            if (!TextUtils.isEmpty(packageName)) {
               boolean var7 = this.a.getPackageManager().checkPermission("android.permission.READ_EXTERNAL_STORAGE", packageName) == 0;
               boolean var4 = this.a.getPackageManager().checkPermission("android.permission.WRITE_EXTERNAL_STORAGE", packageName) == 0;
               if (!var7 && !var4) {
                  this.b = new File(FileHelper.getBackUpDir(this.a, 8));
               } else {
                  TbsLog.i("TbsCommonConfig", "no permission,use sdcard default folder");
                  this.b = new File(FileHelper.getBackUpDir(this.a, 5));
               }
            } else {
               this.b = new File(FileHelper.getBackUpDir(this.a, 8));
            }

            if (this.b == null || !this.b.isDirectory()) {
               return null;
            }
         }

         File file = new File(this.b, "tbsnet.conf");
         if (!file.exists()) {
            TbsLog.e("TbsCommonConfig", "Get file(" + file.getCanonicalPath() + ") failed!");
            return ret;
         }

         ret = file;
         TbsLog.w("TbsCommonConfig", "pathc:" + file.getCanonicalPath());
      } catch (Throwable var5) {
         StringWriter var3 = new StringWriter();
         var5.printStackTrace(new PrintWriter(var3));
         TbsLog.e("TbsCommonConfig", "exceptions occurred2:" + var3.toString());
      }

      return ret;
   }

   public String b() {
      return this.pvPostUrl;
   }

   public String c() {
      return this.tbsDownloadStatPostUrl;
   }

   public String d() {
      return this.tbsDownloaderPostUrl;
   }

   public String e() {
      return this.tbsLogPostUrl;
   }

   public String f() {
      return this.pvPostUrlTk;
   }

   public String getTbsEmergencyPostUrl() {
      return this.tbsEmergencyPostUrl;
   }
}
