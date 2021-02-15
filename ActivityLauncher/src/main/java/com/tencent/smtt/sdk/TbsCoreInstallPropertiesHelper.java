package com.tencent.smtt.sdk;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class TbsCoreInstallPropertiesHelper {
   private static TbsCoreInstallPropertiesHelper a = null;
   private static Context b = null;

   private TbsCoreInstallPropertiesHelper() {
   }

   static TbsCoreInstallPropertiesHelper getInstance(Context context) {
      if (a == null) {
         Class var1 = TbsCoreInstallPropertiesHelper.class;
         synchronized(TbsCoreInstallPropertiesHelper.class) {
            if (a == null) {
               a = new TbsCoreInstallPropertiesHelper();
            }
         }
      }

      b = context.getApplicationContext();
      return a;
   }

   void setCopyCoreVerAndCopyStatus(int var1, int var2) {
      this.setIntProperty("copy_core_ver", var1);
      this.setIntProperty("copy_status", var2);
   }

   void setTpatchVerAndTpatchStatus(int var1, int var2) {
      this.setIntProperty("tpatch_ver", var1);
      this.setIntProperty("tpatch_status", var2);
   }

   File ensureTbsCorePrivate_tbscoreinstallF() {
      TbsInstaller.a();
      File var1 = TbsInstaller.getTbsCorePrivateDir(b);
      File var2 = new File(var1, "tbscoreinstall.txt");
      if (!var2.exists()) {
         try {
            var2.createNewFile();
         } catch (IOException var4) {
            var4.printStackTrace();
            return null;
         }
      }

      return var2;
   }

   private Properties readProperties() {
      FileInputStream fileInputStream = null;
      BufferedInputStream bufferedInputStream = null;
      Properties properties = null;

      try {
         File file = this.ensureTbsCorePrivate_tbscoreinstallF();
         properties = new Properties();
         if (file != null) {
            fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            properties.load(bufferedInputStream);
         }
	
		  return properties;
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (bufferedInputStream != null) {
            try {
               bufferedInputStream.close();
            } catch (IOException e) {
            
            }
         }

      }

      return properties;
   }

   int getInstallCoreVer() {
      return this.getIntProperty("install_core_ver");
   }

   int getInstallStatus() {
      return this.getIntProperty_DefNeg1("install_status");
   }

   void setDexoptRetryNum(int var1) {
      this.setIntProperty("dexopt_retry_num", var1);
   }

   void setUnzipRetryNum(int var1) {
      this.setIntProperty("unzip_retry_num", var1);
   }

   void setInstallApkPath(String var1) {
      this.setProperty("install_apk_path", var1);
   }

   void setInstallCoreVerAndInstallStatus(int var1, int var2) {
      this.setIntProperty("install_core_ver", var1);
      this.setIntProperty("install_status", var2);
   }

   void setIncrupdateStatus(int var1) {
      this.setIntProperty("incrupdate_status", var1);
   }

   int getIncrupdateStatus() {
      return this.getIntProperty_DefNeg1("incrupdate_status");
   }

   void setUnlzmaStatus(int var1) {
      this.setIntProperty("unlzma_status", var1);
   }

   int getIntProperty_DefNeg1(String var1) {
      Properties var2 = this.readProperties();
      return var2 != null && var2.getProperty(var1) != null ? Integer.parseInt(var2.getProperty(var1)) : -1;
   }

   void setProperty(String propName, String propVal) {
      FileOutputStream var3 = null;

      try {
         Properties var4 = this.readProperties();
         if (var4 != null) {
            var4.setProperty(propName, propVal);
            File var5 = this.ensureTbsCorePrivate_tbscoreinstallF();
            if (var5 != null) {
               var3 = new FileOutputStream(var5);
               var4.store(var3, "update " + propName + " and status!");
            }
         }
      } catch (Exception var14) {
         var14.printStackTrace();
      } finally {
         if (var3 != null) {
            try {
               var3.close();
            } catch (IOException var13) {
               var13.printStackTrace();
            }
         }

      }

   }

   void setIntProperty(String var1, int var2) {
      this.setProperty(var1, String.valueOf(var2));
   }

   int getIntProperty(String propName) {
      Properties properties = this.readProperties();
      return properties != null && properties.getProperty(propName) != null ? Integer.parseInt(properties.getProperty(propName)) : 0;
   }

   String getStringProperty(String var1) {
      Properties var2 = this.readProperties();
      return var2 != null && var2.getProperty(var1) != null ? var2.getProperty(var1) : null;
   }
}
