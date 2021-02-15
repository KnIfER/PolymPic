package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class TbsPVConfig extends TbsBaseConfig {
   private static TbsPVConfig b;
   public SharedPreferences mPreferences;

   private TbsPVConfig() {
   }

   public String getConfigFileName() {
      return "tbs_pv_config";
   }

   public static synchronized TbsPVConfig getInstance(Context var0) {
      if (b == null) {
         b = new TbsPVConfig();
         b.init(var0);
      }

      return b;
   }

   public static synchronized void releaseInstance() {
      b = null;
   }

   public synchronized int getLocalCoreVersionMoreTimes() {
      int var1 = 0;

      try {
         String var2 = (String)this.a.get("get_localcoreversion_moretimes");
         if (!TextUtils.isEmpty(var2)) {
            var1 = Integer.parseInt(var2);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return var1;
   }

   public synchronized String getSyncMapValue(String var1) {
      return (String)this.a.get(var1);
   }

   public synchronized int getEmergentCoreVersion() {
      int var1 = 0;

      try {
         String var2 = (String)this.a.get("emergent_core_version");
         if (!TextUtils.isEmpty(var2)) {
            var1 = Integer.parseInt(var2);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return var1;
   }

   public synchronized int getReadApk() {
      int var1 = 0;

      try {
         String var2 = (String)this.a.get("read_apk");
         if (!TextUtils.isEmpty(var2)) {
            var1 = Integer.parseInt(var2);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return var1;
   }

   public synchronized int getDisabledCoreVersion() {
      int var1 = 0;

      try {
         String var2 = (String)this.a.get("disabled_core_version");
         if (!TextUtils.isEmpty(var2)) {
            var1 = Integer.parseInt(var2);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

      return var1;
   }

   public synchronized boolean isEnableNoCoreGray() {
      try {
         String var1 = (String)this.a.get("enable_no_share_gray");
         if (!TextUtils.isEmpty(var1) && var1.equals("true")) {
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return false;
   }

   public synchronized boolean getTbsCoreSandboxModeEnable() {
      try {
         String var1 = (String)this.a.get("tbs_core_sandbox_mode_enable");
         if ("true".equals(var1)) {
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return false;
   }

   public synchronized boolean isDisableHostBackupCore() {
      try {
         String var1 = (String)this.a.get("disable_host_backup");
         if (!TextUtils.isEmpty(var1) && var1.equals("true")) {
            return true;
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }

      return false;
   }

   public synchronized void putData(String var1, String var2) {
      this.a.put(var1, var2);
   }

   public interface TbsPVConfigKey {
      String KEY_EMERGENT_CORE_VERSION = "emergent_core_version";
      String KEY_DISABLED_CORE_VERSION = "disabled_core_version";
      String KEY_ENABLE_NO_SHARE_GRAY = "enable_no_share_gray";
      String KEY_IS_DISABLE_HOST_BACKUP_CORE = "disable_host_backup";
      String KEY_READ_APK = "read_apk";
      String KEY_GET_LOCALCOREVERSION_MORETIMES = "get_localcoreversion_moretimes";
      String KEY_TBS_CORE_SANDBOX_MODE_ENABLE = "tbs_core_sandbox_mode_enable";
   }
}
