package com.tencent.smtt.sdk;

import android.content.Context;
import android.os.Build.VERSION;
import com.tencent.smtt.utils.TbsLog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

class SDKEngine {
   private TbsWizard c = null;
   private TbsWizard d = null;
   private static SDKEngine instance = null;
   private boolean initilized = false;
   private boolean g = false;
   private static int h = 0;
   static int a = 0;
   static boolean b = false;
   private static int i = 3;
   private File j = null;
   private static String k = null;

   private SDKEngine() {
   }

   public static SDKEngine getInstance(boolean instantiate) {
      if (instance == null && instantiate) {
         Class var1 = SDKEngine.class;
         synchronized(SDKEngine.class) {
            if (instance == null) {
               instance = new SDKEngine();
            }
         }
      }

      return instance;
   }

   public synchronized void init(Context context, boolean var2, boolean isPreIniting) {
      TbsLog.addLog(999, (String)null);
      TbsLog.initIfNeed(context);
      TbsLog.i("SDKEngine", "init -- context: " + context + ", isPreIniting: " + isPreIniting);
      ++a;
      TbsCoreLoadStat.getInstance().a();
      TbsInstaller.a().installTbsCoreIfNeeded(context, a == 1);
      TbsInstaller.a().k(context);
      TbsShareManager.forceToLoadX5ForThirdApp(context, true);
      boolean var4 = QbSdk.a(context, var2, isPreIniting);
      boolean var5 = VERSION.SDK_INT >= 7;
      boolean var6 = var4 && var5;
      if (var6) {
         long var7 = System.currentTimeMillis();
         var6 = TbsInstaller.a().isTBSCoreLegal(context, d());
         TbsLog.i("SDKEngine", "isTbsCoreLegal: " + var6 + "; cost: " + (System.currentTimeMillis() - var7));
      }

      if (var6) {
         if (this.initilized) {
            return;
         }

         try {
            File var13 = null;
            File var8 = null;
            Context var9 = null;
            boolean var10;
            if (TbsShareManager.isThirdPartyApp(context)) {
               TbsLog.addLog(995, (String)null);
               var10 = TbsShareManager.j(context);
               if (!var10) {
                  this.initilized = false;
                  QbSdk.forceSysWebViewInner(context, "SDKEngine::useSystemWebView by error_host_unavailable");
                  return;
               }

               var13 = new File(TbsShareManager.c(context));
               var8 = TbsInstaller.a().getTbsCoreShareDir(context);
               var9 = TbsShareManager.e(context);
               if (var8 == null) {
                  this.initilized = false;
                  QbSdk.forceSysWebViewInner(context, "SDKEngine::useSystemWebView by error_tbs_core_dexopt_dir null!");
                  return;
               }
            } else {
               TbsLog.addLog(996, (String)null);
               var13 = TbsInstaller.a().getTbsCoreShareDir(context);
               var8 = var13;
               var10 = h == 25436 || h == 25437;
               if (var10) {
                  var9 = context.getApplicationContext();
               } else {
                  var9 = context;
               }

               if (var13 == null) {
                  this.initilized = false;
                  QbSdk.forceSysWebViewInner(context, "SDKEngine::useSystemWebView by tbs_core_share_dir null!");
                  return;
               }
            }

            String[] var16 = QbSdk.getDexLoaderFileList(context, var9, var13.getAbsolutePath());

            for(int var11 = 0; var11 < var16.length; ++var11) {
            }

            String optDir = null;
            if (TbsShareManager.getHostCorePathAppDefined() != null) {
               optDir = TbsShareManager.getHostCorePathAppDefined();
            } else {
               optDir = var8.getAbsolutePath();
            }

            TbsLog.i("SDKEngine", "SDKEngine init optDir is " + optDir);
            if (this.d != null) {
               this.c = this.d;
               this.c.initTbsSettings(context, var9, var13.getAbsolutePath(), optDir, var16, QbSdk.d);
            } else {
               this.c = new TbsWizard(context, var9, var13.getAbsolutePath(), optDir, var16, QbSdk.d);
            }

            this.initilized = true;
         } catch (Throwable var12) {
            TbsLog.e("SDKEngine", "useSystemWebView by exception: " + var12);
            if (var12 == null) {
               TbsCoreLoadStat.getInstance().a(context, 326);
            } else {
               TbsCoreLoadStat.getInstance().a(context, 327, var12);
            }

            this.initilized = false;
            QbSdk.forceSysWebViewInner(context, "SDKEngine::useSystemWebView by exception: " + var12);
         }
      } else {
         String var14 = "can_load_x5=" + var4 + "; is_compatible=" + var5;
         TbsLog.e("SDKEngine", "SDKEngine.init canLoadTbs=false; failure: " + var14);
         if (!QbSdk.forcedSysByInner || !this.initilized) {
            this.initilized = false;
            TbsCoreLoadStat.getInstance().a(context, 405, new Throwable(var14));
         }
      }

      EmergencyManager emergencyManager = EmergencyManager.getInstance();
      if (this.c != null) {
         emergencyManager.setDexLoader(this.c.getDexLoader());
      }

      emergencyManager.init(context);
      this.j = TbsInstaller.getTbsCorePrivateDir(context);
      this.g = true;
   }

   public TbsWizard a() {
      return this.initilized ? this.c : null;
   }

   public boolean isInitialized() {
      return this.initilized;
   }

   TbsWizard c() {
      return this.c;
   }

   public static int d() {
      return h;
   }

   static void a(int var0) {
      h = var0;
   }

   public String e() {
      return this.c != null && !QbSdk.forcedSysByInner ? this.c.getCrashExtraInfo() : "system webview get nothing...";
   }

   boolean f() {
      if (b) {
         if (k == null) {
            return false;
         }

         int var1 = this.i();
         if (var1 == 0) {
            this.b(1);
         } else {
            if (var1 + 1 > i) {
               return false;
            }

            this.b(var1 + 1);
         }
      }

      return b;
   }

   boolean b(boolean var1) {
      b = var1;
      return var1;
   }

   boolean g() {
      return this.g;
   }

   void a(String var1) {
      k = var1;
   }

   private int i() {
      FileInputStream var1 = null;
      BufferedInputStream var2 = null;

      int var7;
      try {
         File var3 = new File(this.j, "count.prop");
         if (!var3.exists()) {
            byte var20 = 0;
            return var20;
         }

         var1 = new FileInputStream(var3);
         var2 = new BufferedInputStream(var1);
         Properties var4 = new Properties();
         var4.load(var2);
         String var5 = var4.getProperty(k, "1");
         int var6 = Integer.valueOf(var5);
         var7 = var6;
      } catch (Exception var18) {
         var18.printStackTrace();
         return 0;
      } finally {
         if (var2 != null) {
            try {
               var2.close();
            } catch (IOException var17) {
               var17.printStackTrace();
            }
         }

      }

      return var7;
   }

   private void b(int var1) {
      String var2 = String.valueOf(var1);
      Properties var3 = new Properties();
      var3.setProperty(k, var2);

      try {
         var3.store(new FileOutputStream(new File(this.j, "count.prop")), (String)null);
      } catch (FileNotFoundException var5) {
         var5.printStackTrace();
      } catch (IOException var6) {
         var6.printStackTrace();
      }

   }

   public boolean h() {
      return QbSdk.useSoftWare();
   }
}
