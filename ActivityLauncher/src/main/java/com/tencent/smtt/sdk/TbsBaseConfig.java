package com.tencent.smtt.sdk;

import android.content.Context;
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

public abstract class TbsBaseConfig {
   Map<String, String> a;
   public static final String TAG = "TbsBaseConfig";
   private Context b;

   public abstract String getConfigFileName();

   public void init(Context var1) {
      this.a = new HashMap();
      this.b = var1.getApplicationContext();
      if (this.b == null) {
         this.b = var1;
      }

      this.refreshSyncMap(var1);
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

   public void clear() {
      this.a.clear();
      this.commit();
   }

   public synchronized void refreshSyncMap(Context var1) {
      FileInputStream var2 = null;
      BufferedInputStream var3 = null;

      try {
         File var4 = a(this.b, this.getConfigFileName());
         if (var4 != null) {
            this.a.clear();
            var2 = new FileInputStream(var4);
            var3 = new BufferedInputStream(var2);
            Properties var5 = new Properties();
            var5.load(var3);
            Iterator var6 = var5.stringPropertyNames().iterator();

            while(var6.hasNext()) {
               String var7 = (String)var6.next();
               this.a.put(var7, var5.getProperty(var7));
            }

            return;
         }
      } catch (Throwable var17) {
         var17.printStackTrace();
         return;
      } finally {
         try {
            if (var3 != null) {
               var3.close();
            }
         } catch (Exception var16) {
            var16.printStackTrace();
         }

      }

   }

   public synchronized void writeTbsDownloadInfo() {
      TbsLog.i("TbsBaseConfig", "writeTbsDownloadInfo #1");
      FileInputStream var1 = null;
      FileOutputStream var2 = null;
      BufferedInputStream var3 = null;
      BufferedOutputStream var4 = null;

      try {
         File var5 = a(this.b, this.getConfigFileName());
         if (var5 == null) {
            return;
         }

         var1 = new FileInputStream(var5);
         var3 = new BufferedInputStream(var1);
         Properties var6 = new Properties();
         var6.load(var3);
         var6.clear();
         Set var7 = this.a.keySet();
         Iterator var8 = var7.iterator();

         while(var8.hasNext()) {
            String var9 = (String)var8.next();
            Object var10 = this.a.get(var9);
            var6.setProperty(var9, "" + var10);
            TbsLog.i("TbsBaseConfig", "writeTbsDownloadInfo key is " + var9 + " value is " + var10);
         }

         this.a.clear();
         var2 = new FileOutputStream(var5);
         var4 = new BufferedOutputStream(var2);
         var6.store(var4, (String)null);
      } catch (Throwable var25) {
         var25.printStackTrace();
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
}
