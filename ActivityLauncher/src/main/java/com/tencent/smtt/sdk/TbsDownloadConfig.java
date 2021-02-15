package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TbsDownloadConfig {
   public static final int ERROR_REPORTED = 0;
   public static final int ERROR_NONE = 1;
   public static final int ERROR_DOWNLOAD = 2;
   public static final int ERROR_VERIFY = 3;
   public static final int ERROR_UNZIP = 4;
   public static final int ERROR_INSTALL = 5;
   public static final int ERROR_LOAD = 6;
   public static final int CMD_ID_FILE_UPLOAD = 100;
   public static final int CMD_ID_DOWNLOAD_FILE = 101;
   public static final long DEFAULT_RETRY_INTERVAL_SEC = 86400L;
   private static TbsDownloadConfig a;
   public Map<String, Object> mSyncMap = new HashMap();
   public SharedPreferences mPreferences;
   private Context b;

   private TbsDownloadConfig(Context var1) {
      this.mPreferences = var1.getSharedPreferences("tbs_download_config", 4);
      this.b = var1.getApplicationContext();
      if (this.b == null) {
         this.b = var1;
      }

   }

   public static synchronized TbsDownloadConfig getInstance(Context var0) {
      if (a == null) {
         a = new TbsDownloadConfig(var0);
      }

      return a;
   }

   public static synchronized TbsDownloadConfig getInstance() {
      return a;
   }

   public synchronized long getDownloadMaxflow() {
      int var1 = this.mPreferences.getInt("tbs_download_maxflow", 0);
      var1 = var1 == 0 ? 20 : var1;
      return (long)(var1 * 1024) * 1024L;
   }

   public synchronized long getRetryInterval() {
      if (TbsDownloader.getRetryIntervalInSeconds() >= 0L) {
         return TbsDownloader.getRetryIntervalInSeconds();
      } else {
         long var1 = this.mPreferences.getLong("retry_interval", 86400L);
         return var1;
      }
   }

   public synchronized long getDownloadMinFreeSpace() {
      int var1 = this.mPreferences.getInt("tbs_download_min_free_space", 0);
      var1 = var1 == 0 ? 0 : var1;
      return (long)(var1 * 1024) * 1024L;
   }

   public synchronized int getDownloadSuccessMaxRetrytimes() {
      int var1 = this.mPreferences.getInt("tbs_download_success_max_retrytimes", 0);
      return var1 == 0 ? 3 : var1;
   }

   public synchronized int getDownloadFailedMaxRetrytimes() {
      int var1 = this.mPreferences.getInt("tbs_download_failed_max_retrytimes", 0);
      return var1 == 0 ? 100 : var1;
   }

   public synchronized void setDownloadInterruptCode(int var1) {
      try {
         Editor var2 = this.mPreferences.edit();
         var2.putInt("tbs_download_interrupt_code", var1);
         var2.putLong("tbs_download_interrupt_time", System.currentTimeMillis());
         var2.commit();
      } catch (Exception var3) {
      }

   }

   public synchronized void setTbsCoreLoadRenameFileLockEnable(boolean var1) {
      try {
         Editor var2 = this.mPreferences.edit();
         var2.putBoolean("tbs_core_load_rename_file_lock_enable", var1);
         var2.commit();
      } catch (Exception var3) {
      }

   }

   public synchronized void setTbsCoreLoadRenameFileLockWaitEnable(boolean var1) {
      try {
         Editor var2 = this.mPreferences.edit();
         var2.putBoolean("tbs_core_load_rename_file_lock_wait_enable", var1);
         var2.commit();
      } catch (Exception var3) {
      }

   }

   public synchronized boolean getTbsCoreLoadRenameFileLockEnable() {
      boolean var1 = true;

      try {
         var1 = this.mPreferences.getBoolean("tbs_core_load_rename_file_lock_enable", true);
      } catch (Exception var3) {
      }

      return var1;
   }

   public synchronized boolean getTbsCoreLoadRenameFileLockWaitEnable() {
      boolean var1 = true;

      try {
         var1 = this.mPreferences.getBoolean("tbs_core_load_rename_file_lock_wait_enable", true);
      } catch (Exception var3) {
      }

      return var1;
   }

   public synchronized void uploadDownloadInterruptCodeIfNeeded(Context var1) {
      try {
         if (var1 != null && "com.tencent.mm".equals(var1.getApplicationContext().getApplicationInfo().packageName)) {
            boolean var2 = true;
            boolean var3 = true;
            boolean var4 = this.mPreferences.contains("tbs_download_interrupt_code");
            int var8;
            if (!var4) {
               try {
                  if (!(new File(new File(this.b.getFilesDir(), "shared_prefs"), "tbs_download_config")).exists()) {
                     var8 = -97;
                  } else if (!this.mPreferences.contains("tbs_needdownload")) {
                     var8 = -96;
                  } else {
                     var8 = -101;
                  }
               } catch (Throwable var6) {
                  var8 = -95;
               }
            } else {
               var8 = this.mPreferences.getInt("tbs_download_interrupt_code", -99);
               if ((var8 > -206 || var8 < -219) && (var8 > -302 || var8 < -316) && (var8 > -318 || var8 < -322)) {
                  var3 = false;
               }
            }

            if (var3) {
               TbsLogReport.TbsLogInfo var5 = TbsLogReport.getInstance(var1).tbsLogInfo();
               var5.setErrorCode(128);
               var5.setFailDetail(" " + var8);
               TbsLogReport.getInstance(var1).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, var5);
            }
         }
      } catch (Throwable var7) {
      }

   }

   public synchronized int getDownloadInterruptCode() {
      boolean var1 = true;
      boolean var2 = this.mPreferences.contains("tbs_download_interrupt_code");
      int var5;
      if (!var2) {
         try {
            if (!(new File(new File(this.b.getFilesDir(), "shared_prefs"), "tbs_download_config")).exists()) {
               var5 = -97;
            } else if (!this.mPreferences.contains("tbs_needdownload")) {
               var5 = -96;
            } else {
               var5 = -101;
            }
         } catch (Throwable var4) {
            var5 = -95;
         }
      } else {
         var5 = this.mPreferences.getInt("tbs_download_interrupt_code", -99);
         if (var5 == -119 || var5 == -121) {
            var5 = this.mPreferences.getInt("tbs_download_interrupt_code_reason", -119);
         }

         if (System.currentTimeMillis() - this.mPreferences.getLong("tbs_download_interrupt_time", 0L) > 86400000L) {
            var5 -= 98000;
         }
      }

      if (this.b != null && "com.tencent.mobileqq".equals(this.b.getApplicationInfo().packageName) && !"CN".equals(Locale.getDefault().getCountry())) {
         return -320;
      } else {
         int var3 = this.mPreferences.getInt("tbs_install_interrupt_code", -1);
         return var5 * 1000 + var3;
      }
   }

   public synchronized long getDownloadSingleTimeout() {
      long var1 = this.mPreferences.getLong("tbs_single_timeout", 0L);
      return var1 == 0L ? 1200000L : var1;
   }

   public synchronized boolean isOverSea() {
      return this.mPreferences.getBoolean("is_oversea", false);
   }

   public synchronized void commit() {
      try {
         Editor var1 = this.mPreferences.edit();
         Set var2 = this.mSyncMap.keySet();
         Iterator var3 = var2.iterator();

         while(var3.hasNext()) {
            String var4 = (String)var3.next();
            Object var5 = this.mSyncMap.get(var4);
            if (var5 instanceof String) {
               var1.putString(var4, (String)var5);
            } else if (var5 instanceof Boolean) {
               var1.putBoolean(var4, (Boolean)var5);
            } else if (var5 instanceof Long) {
               var1.putLong(var4, (Long)var5);
            } else if (var5 instanceof Integer) {
               var1.putInt(var4, (Integer)var5);
            } else if (var5 instanceof Float) {
               var1.putFloat(var4, (Float)var5);
            }
         }

         var1.commit();
         this.mSyncMap.clear();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }

   public void clear() {
      try {
         this.mSyncMap.clear();
         Editor var1 = this.mPreferences.edit();
         var1.clear();
         var1.commit();
      } catch (Exception var2) {
      }

   }

   public synchronized void setInstallInterruptCode(int var1) {
      Editor var2 = this.mPreferences.edit();
      var2.putInt("tbs_install_interrupt_code", var1);
      var2.commit();
   }

   public interface TbsConfigKey {
      String KEY_LAST_CHECK = "last_check";
      String KEY_LAST_REQUEST_SUCCESS = "last_request_success";
      String KEY_REQUEST_FAIL = "request_fail";
      String KEY_COUNT_REQUEST_FAIL_IN_24HOURS = "count_request_fail_in_24hours";
      String KEY_LAST_DOWNLOAD_DECOUPLE_CORE = "last_download_decouple_core";
      String KEY_TBS_DOWNLOAD_V = "tbs_download_version";
      String KEY_TBS_DOWNLOAD_V_TYPE = "tbs_download_version_type";
      String KEY_NEEDDOWNLOAD = "tbs_needdownload";
      String KEY_FULL_PACKAGE = "request_full_package";
      String KEY_TBSDOWNLOADURL = "tbs_downloadurl";
      String KEY_DOWNLOADURL_LIST = "tbs_downloadurl_list";
      String KEY_TBSAPKFILESIZE = "tbs_apkfilesize";
      String KEY_TBSAPK_MD5 = "tbs_apk_md5";
      String KEY_RESPONSECODE = "tbs_responsecode";
      String KEY_DECOUPLECOREVERSION = "tbs_decouplecoreversion";
      String KEY_DOWNLOADDECOUPLECORE = "tbs_downloaddecouplecore";
      String KEY_APP_VERSIONNAME = "app_versionname";
      String KEY_APP_VERSIONCODE = "app_versioncode";
      String KEY_APP_METADATA = "app_metadata";
      String KEY_APP_VERSIONCODE_FOR_SWITCH = "app_versioncode_for_switch";
      String KEY_DOWNLOAD_MAXFLOW = "tbs_download_maxflow";
      String KEY_DOWNLOAD_SUCCESS_MAX_RETRYTIMES = "tbs_download_success_max_retrytimes";
      String KEY_DOWNLOAD_SUCCESS_RETRYTIMES = "tbs_download_success_retrytimes";
      String KEY_DOWNLOAD_FAILED_MAX_RETRYTIMES = "tbs_download_failed_max_retrytimes";
      String KEY_DOWNLOAD_FAILED_RETRYTIMES = "tbs_download_failed_retrytimes";
      String KEY_DOWNLOAD_MIN_FREE_SPACE = "tbs_download_min_free_space";
      String KEY_DOWNLOAD_SINGLE_TIMEOUT = "tbs_single_timeout";
      String KEY_TBSDOWNLOAD_STARTTIME = "tbs_downloadstarttime";
      String KEY_TBSDOWNLOAD_FLOW = "tbs_downloadflow";
      String KEY_DEVICE_CPUABI = "device_cpuabi";
      String KEY_IS_OVERSEA = "is_oversea";
      String KEY_RETRY_INTERVAL = "retry_interval";
      String KEY_DESkEY_TOKEN = "tbs_deskey_token";
      String KEY_DOWNLOAD_INTERRUPT_CODE = "tbs_download_interrupt_code";
      String KEY_DOWNLOAD_INTERRUPT_CODE_REASON = "tbs_download_interrupt_code_reason";
      String KEY_INSTALL_INTERRUPT_CODE = "tbs_install_interrupt_code";
      String KEY_DOWNLOAD_INTERRUPT_TIME = "tbs_download_interrupt_time";
      String KEY_LAST_THIRDAPP_SENDREQUEST_COREVERSION = "last_thirdapp_sendrequest_coreversion";
      String KEY_USE_BACKUP_VERSION = "use_backup_version";
      String KEY_SWITCH_BACKUPCORE_ENABLE = "switch_backupcore_enable";
      String KEY_BACKUPCORE_DELFILELIST = "backupcore_delfilelist";
      String KEY_STOP_PRE_OAT = "tbs_stop_preoat";
      String KEY_GUID = "tbs_guid";
      String KEY_USE_BUGLY = "tbs_use_bugly";
      String KEY_TBS_CORE_LOAD_RENAME_FILE_LOCK_ENABLE = "tbs_core_load_rename_file_lock_enable";
      String KEY_TBS_CORE_LOAD_RENAME_FILE_LOCK_WAIT_ENABLE = "tbs_core_load_rename_file_lock_wait_enable";
   }
}
