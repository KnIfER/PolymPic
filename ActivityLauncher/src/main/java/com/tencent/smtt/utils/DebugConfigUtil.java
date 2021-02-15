package com.tencent.smtt.utils;

import android.content.Context;
import com.tencent.smtt.sdk.QbSdk;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class DebugConfigUtil {
   private Context context = null;
   private File corePrivateDir_inTbsFolder = null;
   public boolean forceUseSystemWebview = false;
   private boolean systemWebviewForceUsed = false;
   private static DebugConfigUtil instance = null;
   private File debugConfigF = null;

   public static synchronized DebugConfigUtil getInstance(Context context) {
      if (instance == null) {
         instance = new DebugConfigUtil(context);
      }
      return instance;
   }

   private DebugConfigUtil(Context var1) {
      this.context = var1.getApplicationContext();
      this.readDebugConfigF();
   }

   public static synchronized DebugConfigUtil a() {
      return instance;
   }

   public synchronized void readDebugConfigF() {
      FileInputStream fileInputStream = null;
      BufferedInputStream bufferedInputStream = null;

      try {
         if (this.debugConfigF == null) {
            this.debugConfigF = this.getDebugConfigF();
         }

         if (this.debugConfigF == null) {
            return;
         }

         fileInputStream = new FileInputStream(this.debugConfigF);
         bufferedInputStream = new BufferedInputStream(fileInputStream);
         Properties properties = new Properties();
         properties.load(bufferedInputStream);
         String forceUseSystemWebview = properties.getProperty("setting_forceUseSystemWebview", "");
         if (!"".equals(forceUseSystemWebview)) {
            this.forceUseSystemWebview = Boolean.parseBoolean(forceUseSystemWebview);
         }
      } catch (Throwable throwable) {
         throwable.printStackTrace();
      } finally {
         try {
            if (bufferedInputStream != null) {
               bufferedInputStream.close();
            }
         } catch (Exception ignored) { }
      }
   }

   private File getDebugConfigF() {
      File ret = null;

      try {
         File file;
         if (this.corePrivateDir_inTbsFolder == null) {
            file = QbSdk.getTbsFolderDir(this.context);
            this.corePrivateDir_inTbsFolder = new File(file, "core_private");
            if (!this.corePrivateDir_inTbsFolder.isDirectory()) {
               return null;
            }
         }

         file = new File(this.corePrivateDir_inTbsFolder, "debug.conf");
         if (!file.exists()) {
            file.createNewFile();
         }

         ret = file;
      } catch (Throwable var3) {
         var3.printStackTrace();
      }

      return ret;
   }

   public void setSystemWebviewForceUsedResult(boolean result) {
      this.systemWebviewForceUsed = result;
      this.storeDebugConfigF();
   }

   public void storeDebugConfigF() {
      FileOutputStream fileOutputStream = null;
      FileInputStream fileInputStream = null;
      BufferedInputStream bufferedInputStream = null;
      BufferedOutputStream bufferedOutputStream = null;

      try {
         File debugConfigF = this.getDebugConfigF();
         if (debugConfigF == null) {
            return;
         }

         fileInputStream = new FileInputStream(debugConfigF);
         bufferedInputStream = new BufferedInputStream(fileInputStream);
         Properties properties = new Properties();
         properties.load(bufferedInputStream);
         properties.setProperty("setting_forceUseSystemWebview", Boolean.toString(this.forceUseSystemWebview));
         properties.setProperty("result_systemWebviewForceUsed", Boolean.toString(this.systemWebviewForceUsed));
         fileOutputStream = new FileOutputStream(debugConfigF);
         bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
         properties.store(bufferedOutputStream, (String)null);
      } catch (Throwable e) {
         e.printStackTrace();
      } finally {
         try {
            bufferedInputStream.close();
         } catch (Exception ignored) { }

         try {
            bufferedOutputStream.close();
         } catch (Exception ignored) { }

      }

   }
}
