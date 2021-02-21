package com.tencent.smtt.sdk;

import android.content.Context;
import android.util.Log;
import com.tencent.smtt.export.external.DexLoader;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.TbsLog;
import java.nio.channels.FileLock;

class X5CoreEngine {
   private static X5CoreEngine instance;
   private WebViewWizardBase webViewWizardBase;
   private boolean X5SafeAndSound;
   private boolean initialized;
   private static FileLock e = null;

   private X5CoreEngine() {
   }

   public static X5CoreEngine getInstance() {
      if (instance == null) {
         Class var0 = X5CoreEngine.class;
         synchronized(X5CoreEngine.class) {
            if (instance == null) {
               instance = new X5CoreEngine();
            }
         }
      }

      return instance;
   }

   public boolean isInCharge() {
      return !QbSdk.forcedSysByInner && this.X5SafeAndSound;
   }

   public WebViewWizardBase wizard(boolean var1) {
      return var1 ? this.webViewWizardBase : this.getWVWizardBase();
   }

   public WebViewWizardBase getWVWizardBase() {
      return QbSdk.forcedSysByInner ? null : this.webViewWizardBase;
   }

   public synchronized void init(Context var1) {
      TbsLog.i("X5CoreEngine", "init #1");
      SDKEngine sdkEng = SDKEngine.getInstance(true);
      sdkEng.init(var1, false, false);
      StringBuilder sb = new StringBuilder();
      Throwable e = null;
      TbsWizard var5 = sdkEng.a();
      if (sdkEng.isInitialized() && var5 != null) {
         if (!this.initialized) {
            this.webViewWizardBase = new WebViewWizardBase(var5.getDexLoader());

            try {
               this.X5SafeAndSound = this.webViewWizardBase.canUseX5();
               if (!this.X5SafeAndSound) {
                  sb.append("can not use X5 by x5corewizard return false");
               }
            } catch (NoSuchMethodException var10) {
               this.X5SafeAndSound = true;
            } catch (Throwable var11) {
               this.X5SafeAndSound = false;
               e = var11;
               sb.append("can not use x5 by throwable " + Log.getStackTraceString(var11));
            }

            if (this.X5SafeAndSound) {
               CookieManager.getInstance().a(var1, true, true);
               CookieManager.getInstance().a();
            }
         }
      } else {
         this.X5SafeAndSound = false;
         sb.append("can not use X5 by !tbs available");
      }

      TbsLog.i("X5CoreEngine", "init  mCanUseX5 is " + this.X5SafeAndSound);
      if (!this.X5SafeAndSound) {
         TbsLog.e("X5CoreEngine", "mCanUseX5 is false --> report");
         if (sdkEng.isInitialized() && var5 != null && e == null) {
            try {
               DexLoader var6 = var5.getDexLoader();
               Object var7 = null;
               if (var6 != null) {
                  var7 = var6.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "getLoadFailureDetails", new Class[0]);
               }

               if (var7 instanceof Throwable) {
                  Throwable var8 = (Throwable)var7;
                  sb.append("#" + var8.getMessage() + "; cause: " + var8.getCause() + "; th: " + var8);
               }

               if (var7 instanceof String) {
                  sb.append("failure detail:" + var7);
               }
            } catch (Throwable var9) {
               var9.printStackTrace();
            }

            if (sb != null && sb.toString().contains("isPreloadX5Disabled:-10000")) {
               TbsCoreLoadStat.getInstance().a(var1, 408, new Throwable("X5CoreEngine::init, mCanUseX5=false, available true, details: " + sb.toString()));
            } else {
               TbsCoreLoadStat.getInstance().a(var1, 407, new Throwable("X5CoreEngine::init, mCanUseX5=false, available true, details: " + sb.toString()));
            }
         } else if (sdkEng.isInitialized()) {
            TbsCoreLoadStat.getInstance().a(var1, 409, new Throwable("mCanUseX5=false, available true, reason: " + e));
         } else {
            TbsCoreLoadStat.getInstance().a(var1, 410, new Throwable("mCanUseX5=false, available false, reason: " + e));
         }
      } else {
         TbsLog.i("X5CoreEngine", "init  sTbsCoreLoadFileLock is " + X5CoreEngine.e);
         if (X5CoreEngine.e == null) {
            this.tryTbsCoreLoadFileLock(var1);
         }
      }

      this.initialized = true;
   }

   boolean getInitialized() {
      return this.initialized;
   }

   public FileLock tryTbsCoreLoadFileLock(Context var1) {
      TbsLog.i("X5CoreEngine", "tryTbsCoreLoadFileLock ##");
      if (e != null) {
         return e;
      } else {
         Class x5CoreEngineClass = X5CoreEngine.class;
         synchronized(X5CoreEngine.class) {
            if (e == null) {
               e = FileHelper.getTbsCoreLoadFileLock(var1);
               if (e == null) {
                  TbsLog.i("X5CoreEngine", "init -- sTbsCoreLoadFileLock failed!");
               } else {
                  TbsLog.i("X5CoreEngine", "init -- sTbsCoreLoadFileLock succeeded: " + e);
               }
            }
         }

         return e;
      }
   }
}
