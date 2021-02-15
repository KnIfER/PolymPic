package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import com.tencent.smtt.utils.TbsLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class TbsDownloadUpload {
   private static TbsDownloadUpload b;
   Map<String, Object> a = new HashMap();
   public SharedPreferences mPreferences;
   private Context c;
   private int d;
   private int e;
   private int f;
   private int g;
   private int h;
   private int i;

   public TbsDownloadUpload(Context var1) {
      this.mPreferences = var1.getSharedPreferences("tbs_download_upload", 4);
      this.c = var1.getApplicationContext();
      if (this.c == null) {
         this.c = var1;
      }

   }

   public static synchronized TbsDownloadUpload getInstance(Context var0) {
      if (b == null) {
         b = new TbsDownloadUpload(var0);
      }

      return b;
   }

   public static synchronized TbsDownloadUpload getInstance() {
      return b;
   }

   public static synchronized void clear() {
      b = null;
   }

   public void clearUploadCode() {
      this.a.put("tbs_needdownload_code", 0);
      this.a.put("tbs_startdownload_code", 0);
      this.a.put("tbs_needdownload_return", 0);
      this.a.put("tbs_needdownload_sent", 0);
      this.a.put("tbs_startdownload_sent", 0);
      this.a.put("tbs_local_core_version", 0);
      this.writeTbsDownloadInfo();
   }

   public synchronized int getNeedDownloadCode() {
      return this.g == 1 ? 148 : this.d;
   }

   public synchronized int getLocalCoreVersion() {
      return this.i;
   }

   public synchronized int getStartDownloadCode() {
      return this.h == 1 ? 168 : this.e;
   }

   public synchronized int getNeedDownloadReturn() {
      return this.f;
   }

   public synchronized void readTbsDownloadInfo(Context var1) {
      FileInputStream var2 = null;
      BufferedInputStream var3 = null;

      try {
         File var4 = a(this.c, "download_upload");
         if (var4 != null) {
            var2 = new FileInputStream(var4);
            var3 = new BufferedInputStream(var2);
            Properties var5 = new Properties();
            var5.load(var3);
            String var6 = var5.getProperty("tbs_needdownload_code", "");
            if (!"".equals(var6)) {
               this.d = Math.max(Integer.parseInt(var6), 0);
            }

            var6 = var5.getProperty("tbs_startdownload_code", "");
            if (!"".equals(var6)) {
               this.e = Math.max(Integer.parseInt(var6), 0);
            }

            var6 = var5.getProperty("tbs_needdownload_return", "");
            if (!"".equals(var6)) {
               this.f = Math.max(Integer.parseInt(var6), 0);
            }

            var6 = var5.getProperty("tbs_needdownload_sent", "");
            if (!"".equals(var6)) {
               this.g = Math.max(Integer.parseInt(var6), 0);
            }

            var6 = var5.getProperty("tbs_startdownload_sent", "");
            if (!"".equals(var6)) {
               this.h = Math.max(Integer.parseInt(var6), 0);
            }

            var6 = var5.getProperty("tbs_local_core_version", "");
            if (!"".equals(var6)) {
               this.i = Math.max(Integer.parseInt(var6), 0);
            }

            return;
         }
      } catch (Throwable var16) {
         var16.printStackTrace();
         return;
      } finally {
         try {
            if (var3 != null) {
               var3.close();
            }
         } catch (Exception var15) {
            var15.printStackTrace();
         }

      }

   }

   private static File a(Context var0, String var1) {
      TbsInstaller.a();
      File var2 = TbsInstaller.getTbsCorePrivateDir(var0);
      if (var2 == null) {
         return null;
      } else {
         File var3 = new File(var2, var1);
         if (var3 != null && var3.exists()) {
            return var3;
         } else {
            try {
               var3.createNewFile();
               return var3;
            } catch (IOException var5) {
               var5.printStackTrace();
               return null;
            }
         }
      }
   }

   public synchronized void writeTbsDownloadInfo() {
      TbsLog.i("TbsDownloadUpload", "writeTbsDownloadInfo #1");
      FileInputStream var1 = null;
      FileOutputStream var2 = null;
      BufferedInputStream var3 = null;
      BufferedOutputStream var4 = null;

      try {
         File var5 = a(this.c, "download_upload");
         if (var5 != null) {
            var1 = new FileInputStream(var5);
            var3 = new BufferedInputStream(var1);
            Properties var6 = new Properties();
            var6.load(var3);
            Set var7 = this.a.keySet();
            Iterator var8 = var7.iterator();

            while(var8.hasNext()) {
               String var9 = (String)var8.next();
               Object var10 = this.a.get(var9);
               var6.setProperty(var9, "" + var10);
               TbsLog.i("TbsDownloadUpload", "writeTbsDownloadInfo key is " + var9 + " value is " + var10);
            }

            this.a.clear();
            var2 = new FileOutputStream(var5);
            var4 = new BufferedOutputStream(var2);
            var6.store(var4, (String)null);
            return;
         }
      } catch (Throwable var25) {
         var25.printStackTrace();
         return;
      } finally {
         try {
            if (var3 != null) {
               var3.close();
            }
         } catch (Exception var24) {
            var24.printStackTrace();
         }

         try {
            if (var4 != null) {
               var4.close();
            }
         } catch (Exception var23) {
            var23.printStackTrace();
         }

      }

   }

   public synchronized void commit() {
      this.writeTbsDownloadInfo();
   }

   public interface TbsUploadKey {
      String KEY_NEEDDOWNLOAD_CODE = "tbs_needdownload_code";
      String KEY_STARTDOWNLOAD_CODE = "tbs_startdownload_code";
      String KEY_NEEDDOWNLOAD_RETURN = "tbs_needdownload_return";
      String KEY_NEEDDOWNLOAD_SENT = "tbs_needdownload_sent";
      String KEY_STARTDOWNLOAD_SENT = "tbs_startdownload_sent";
      String KEY_LOCAL_CORE_VERSION = "tbs_local_core_version";
   }
}
