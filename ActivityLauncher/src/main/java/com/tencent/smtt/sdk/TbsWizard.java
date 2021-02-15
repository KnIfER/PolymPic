package com.tencent.smtt.sdk;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.tencent.smtt.export.external.DexLoader;
import com.tencent.smtt.export.external.libwebp;
import com.tencent.smtt.utils.TbsCheckUtils;
import com.tencent.smtt.utils.TbsLog;
import java.util.Map;

class TbsWizard {
   private Context a = null;
   private Context b = null;
   private String c = null;
   private String[] d = null;
   private DexLoader dexLoader = null;
   private String f = "TbsDexOpt";
   private String g = null;

   public TbsWizard(Context var1, Context var2, String var3, String var4, String[] var5, String var6) throws Exception {
      TbsLog.i("TbsWizard", "construction start...");
      if (var1 != null && (var2 != null || TbsShareManager.getHostCorePathAppDefined() != null) && !TextUtils.isEmpty(var3) && var5 != null && var5.length != 0) {
         this.a = var1.getApplicationContext();
         if (var2.getApplicationContext() != null) {
            this.b = var2.getApplicationContext();
         } else {
            this.b = var2;
         }

         this.c = var3;
         this.d = var5;
         this.f = var4;

         for(int var7 = 0; var7 < this.d.length; ++var7) {
            TbsLog.i("TbsWizard", "#2 mDexFileList[" + var7 + "]: " + this.d[var7]);
         }

         TbsLog.i("TbsWizard", "new DexLoader #2 libraryPath is " + var6 + " mCallerAppContext is " + this.a + " dexOutPutDir is " + var4);
         this.dexLoader = new DexLoader(var6, this.a, this.d, var4, QbSdk.n);
         long var14 = System.currentTimeMillis();
         this.a(var1);
         libwebp.loadWepLibraryIfNeed(var2, this.c);
         if ("com.nd.android.pandahome2".equals(this.a.getApplicationInfo().packageName)) {
            this.dexLoader.invokeStaticMethod("com.tencent.tbs.common.beacon.X5CoreBeaconUploader", "getInstance", new Class[]{Context.class}, this.a);
         }

         if (QbSdk.n != null) {
            boolean var9 = false;

            try {
               var9 = TbsPVConfig.getInstance(this.a).getTbsCoreSandboxModeEnable();
            } catch (Throwable var13) {
            }

            boolean var10 = false;

            try {
               Object var11 = QbSdk.n.get("use_sandbox");
               if ("true".equals(String.valueOf(var11))) {
                  var10 = true;
               }
            } catch (Throwable var12) {
               var12.printStackTrace();
            }

            QbSdk.n.put("use_sandbox", var9 && var10);
            this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "initTbsSettings", new Class[]{Map.class}, QbSdk.n);
         }

         int var15 = this.initTesRuntimeEnvironment(var1);
         if (var15 < 0) {
            throw new Exception("TbsWizard init error: " + var15 + "; msg: " + this.g);
         } else {
            TbsLog.i("TbsWizard", "construction end...");
         }
      } else {
         throw new Exception("TbsWizard paramter error:-1callerContext:" + var1 + "hostcontext" + var2 + "isEmpty" + TextUtils.isEmpty(var3) + "dexfileList" + var5);
      }
   }

   void a(Context var1) {
      boolean var2 = true;
      Map var3 = QbSdk.n;
      if (var3 != null) {
         Object var4 = var3.get("check_tbs_validity");
         if (var4 instanceof Boolean) {
            var2 = (Boolean)var4;
         }
      }

      if (var2) {
         TbsCheckUtils.checkTbsValidity(var1);
      }

   }

   public void initTbsSettings(Context var1, Context var2, String var3, String var4, String[] var5, String var6) throws Exception {
      this.a = var1.getApplicationContext();
      if (this.b.getApplicationContext() != null) {
         this.b = this.b.getApplicationContext();
      }

      this.c = var3;
      this.d = var5;
      this.f = var4;
      libwebp.loadWepLibraryIfNeed(var2, this.c);
      if (QbSdk.n != null) {
         this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "initTbsSettings", new Class[]{Map.class}, QbSdk.n);
      }

      int var7 = this.initTesRuntimeEnvironment(var1);
      if (var7 < 0) {
         throw new Exception("continueInit init error: " + var7 + "; msg: " + this.g);
      }
   }

   private int initTesRuntimeEnvironment(Context var1) {
      boolean var2 = false;
      Object var3;
      if (this.b == null && TbsShareManager.getHostCorePathAppDefined() != null) {
         var3 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "initTesRuntimeEnvironment", new Class[]{Context.class, Context.class, DexLoader.class, String.class, String.class, String.class, Integer.TYPE, String.class, String.class}, var1, this.b, this.dexLoader, this.c, this.f, "4.3.0.67", 43967, QbSdk.a(), TbsShareManager.getHostCorePathAppDefined());
      } else {
         TbsLog.i("TbsWizard", "initTesRuntimeEnvironment callerContext is " + var1 + " mHostContext is " + this.b + " mDexLoader is " + this.dexLoader + " mtbsInstallLocation is " + this.c + " mDexOptPath is " + this.f);
         var3 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "initTesRuntimeEnvironment", new Class[]{Context.class, Context.class, DexLoader.class, String.class, String.class, String.class, Integer.TYPE, String.class}, var1, this.b, this.dexLoader, this.c, this.f, "4.3.0.67", 43967, QbSdk.a());
      }

      if (var3 == null) {
         this.setTesSdkVersionName();
         this.setVersion();
         var3 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "initTesRuntimeEnvironment", new Class[]{Context.class, Context.class, DexLoader.class, String.class, String.class}, var1, this.b, this.dexLoader, this.c, this.f);
      }

      int var6;
      if (var3 == null) {
         var6 = -3;
      } else if (var3 instanceof Integer) {
         var6 = (Integer)var3;
      } else if (var3 instanceof Throwable) {
         TbsCoreLoadStat.getInstance().a(this.a, 328, (Throwable)var3);
         var6 = -5;
      } else {
         var6 = -4;
      }

      if (var6 < 0) {
         Object var4 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "getLoadFailureDetails", new Class[0]);
         if (var4 instanceof Throwable) {
            Throwable var5 = (Throwable)var4;
            this.g = "#" + var5.getMessage() + "; cause: " + var5.getCause() + "; th: " + var5;
         }

         if (var4 instanceof String) {
            this.g = (String)var4;
         }
      } else {
         this.g = null;
      }

      return var6;
   }

   private void setTesSdkVersionName() {
      this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "setTesSdkVersionName", new Class[]{String.class}, "4.3.0.67");
   }

   private void setVersion() {
      this.dexLoader.setStaticField("com.tencent.tbs.tbsshell.TBSShell", "VERSION", 43967);
   }

   public String getCrashExtraInfo() {
      String var1 = null;
      Object var2 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "invokeStaticMethod", new Class[]{Boolean.TYPE, String.class, String.class, Class[].class, Object[].class}, true, "com.tencent.smtt.util.CrashTracker", "getCrashExtraInfo", null, new Object[0]);
      if (var2 == null) {
         var2 = this.dexLoader.invokeStaticMethod("com.tencent.smtt.util.CrashTracker", "getCrashExtraInfo", (Class[])null);
      }

      if (var2 != null) {
         var1 = String.valueOf(var2);
         var1 = var1 + " ReaderPackName=" + TbsReaderView.gReaderPackName + " ReaderPackVersion=" + TbsReaderView.gReaderPackVersion;
      }

      return var1 == null ? "X5 core get nothing..." : var1;
   }

   public boolean installLocalQbApk(Context var1, String var2, String var3, Bundle var4) {
      Object var5 = this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "installLocalQbApk", new Class[]{Context.class, String.class, String.class, Bundle.class}, var1, var2, var3, var4);
      return null == var5 ? false : (Boolean)var5;
   }

   public DexLoader getDexLoader() {
      return this.dexLoader;
   }
}
