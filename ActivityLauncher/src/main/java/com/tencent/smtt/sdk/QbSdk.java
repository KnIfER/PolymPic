package com.tencent.smtt.sdk;

import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.knziha.polymer.browser.webkit.XPlusWebView;
import com.tencent.smtt.export.external.DexLoader;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.sdk.stat.MttLoader;
import com.tencent.smtt.utils.AppUtil;
import com.tencent.smtt.utils.FileProvider;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.ReflectionUtils;
import com.tencent.smtt.utils.TbsCheckUtils;
import com.tencent.smtt.utils.TbsLog;
import com.tencent.smtt.utils.TbsLogClient;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONObject;

@SuppressLint({"NewApi"})
public class QbSdk {
   public static final String SHARE_PREFERENCES_NAME = "tbs_file_open_dialog_config";
   public static final int VERSION = 1;
   public static final String SVNVERSION = "jnizz";
   public static boolean sIsVersionPrinted = false;
   public static final String PARAM_KEY_POSITIONID = "param_key_positionid";
   public static final String PARAM_KEY_FEATUREID = "param_key_featureid";
   public static final String PARAM_KEY_FUNCTIONID = "param_key_functionid";
   private static int o = 0;
   private static String p = "";
   private static Class<?> q = null;
   private static Object r = null;
   static boolean forcedSysByInner = false;
   static boolean forcedSysByOuter = false;
   static boolean c = true;
   private static boolean s = false;
   private static String[] t;
   private static String u = "NULL";
   private static String v = "UNKNOWN";
   public static final int EXTENSION_INIT_FAILURE = -99999;
   static String d;
   public static final int TBSMODE = 1;
   public static final int QBMODE = 2;
   static boolean e = false;
   static long f = 0L;
   static long g = 0L;
   static Object h = new Object();
   public static final String LOGIN_TYPE_KEY_PARTNER_ID = "ChannelID";
   public static final String LOGIN_TYPE_KEY_PARTNER_CALL_POS = "PosID";
   public static boolean isDefaultDialog = false;
   private static boolean w = false;
   static boolean i = true;
   static boolean j = true;
   static boolean k = false;
   private static int x = 0;
   private static int y = 170;
   public static final String TID_QQNumber_Prefix = "QQ:";
   private static String z = null;
   private static String A = null;
   static volatile boolean l;
   public static boolean mDisableUseHostBackupCore;
   private static boolean B;
   private static boolean C;
   private static TbsListener D;
   private static TbsListener E;
   private static boolean F;
   private static boolean G;
   static TbsListener m;
   public static final String FILERADER_MENUDATA = "menuData";
   public static final String KEY_SET_SENDREQUEST_AND_UPLOAD = "SET_SENDREQUEST_AND_UPLOAD";
   static Map<String, Object> n;

   public static boolean startQBToLoadurl(Context var0, String var1, int var2, WebView var3) {
      HashMap var4 = new HashMap();
      var4.put("ChannelID", var0.getApplicationInfo().processName);
      var4.put("PosID", Integer.toString(var2));
      if (var3 == null) {
         try {
            PackageInfo var5 = var0.getPackageManager().getPackageInfo(var0.getPackageName(), 0);
            String var6 = var5.packageName;
            if (var6 == "com.tencent.mm" || var6 == "com.tencent.mobileqq") {
               X5CoreEngine var7 = X5CoreEngine.getInstance();
               if (null != var7 && var7.isInCharge()) {
                  DexLoader var8 = var7.getWVWizardBase().getDexLoader();
                  Object var9 = var8.invokeStaticMethod("com.tencent.smtt.webkit.WebViewList", "getCurrentMainWebviewJustForQQandWechat", new Class[0]);
                  if (var9 != null) {
                     IX5WebViewBase var10 = (IX5WebViewBase)var9;
                     if (var10 != null) {
                        var3 = (WebView)var10.getView().getParent();
                     }
                  }
               }
            }
         } catch (Exception var11) {
         }
      }

      return MttLoader.loadUrl(var0, var1, var4, "QbSdk.startQBToLoadurl", var3) == 0;
   }

   public static boolean startQBForVideo(Context var0, String var1, int var2) {
      HashMap var3 = new HashMap();
      var3.put("ChannelID", var0.getApplicationInfo().processName);
      var3.put("PosID", Integer.toString(var2));
      return MttLoader.openVideoWithQb(var0, var1, var3);
   }

   public static boolean startQBForDoc(Context var0, String var1, int var2, int var3, String var4, Bundle var5) {
      HashMap var6 = new HashMap();
      var6.put("ChannelID", var0.getApplicationContext().getApplicationInfo().processName);
      var6.put("PosID", Integer.toString(var2));
      return MttLoader.openDocWithQb(var0, var1, var3, var4, var6, var5);
   }

   public static boolean getIsSysWebViewForcedByOuter() {
      return forcedSysByOuter;
   }

   @SuppressLint({"NewApi"})
   private static boolean init(Context var0, boolean var1) {
      TbsLog.initIfNeed(var0);
      if (!sIsVersionPrinted) {
         TbsLog.i("QbSdk", "svn revision: jnizz; SDK_VERSION_CODE: 43967; SDK_VERSION_NAME: 4.3.0.67");
         sIsVersionPrinted = true;
      }

      if (forcedSysByInner && !var1) {
         TbsLog.e("QbSdk", "QbSdk init: " + v, false);
         TbsCoreLoadStat.getInstance().a(var0, 414, new Throwable(v));
         return false;
      } else if (forcedSysByOuter) {
         TbsLog.e("QbSdk", "QbSdk init mIsSysWebViewForcedByOuter = true", true);
         TbsCoreLoadStat.getInstance().a(var0, 402, new Throwable(u));
         return false;
      } else {
         if (!C) {
            d(var0);
         }

         try {
            File var2 = TbsInstaller.a().getTbsCoreShareDir(var0);
            if (var2 == null) {
               TbsLog.e("QbSdk", "QbSdk init (false) optDir == null");
               TbsCoreLoadStat.getInstance().a(var0, 312, new Throwable("QbSdk.init (false) TbsCoreShareDir is null"));
               return false;
            } else {
               if (TbsShareManager.isThirdPartyApp(var0)) {
                  if (o != 0 && o != TbsShareManager.d(var0)) {
                     q = null;
                     r = null;
                     TbsLog.e("QbSdk", "QbSdk init (false) ERROR_UNMATCH_TBSCORE_VER_THIRDPARTY!");
                     TbsCoreLoadStat.getInstance().a(var0, 302, new Throwable("sTbsVersion: " + o + "; AvailableTbsCoreVersion: " + TbsShareManager.d(var0)));
                     return false;
                  }

                  o = TbsShareManager.d(var0);
               } else {
                  int var3 = 0;
                  if (o != 0) {
                     var3 = TbsInstaller.a().a(true, var0);
                     if (o != var3) {
                        q = null;
                        r = null;
                        TbsLog.e("QbSdk", "QbSdk init (false) not isThirdPartyApp tbsCoreInstalledVer=" + var3, true);
                        TbsLog.e("QbSdk", "QbSdk init (false) not isThirdPartyApp sTbsVersion=" + o, true);
                        TbsCoreLoadStat.getInstance().a(var0, 303, new Throwable("sTbsVersion: " + o + "; tbsCoreInstalledVer: " + var3));
                        return false;
                     }
                  }

                  o = var3;
               }

               if (TbsDownloader.a(var0, o)) {
                  TbsLog.i("QbSdk", "version " + o + " is in blacklist,can not load! return");
                  return false;
               } else if (q != null && r != null) {
                  return true;
               } else {
                  File var8 = null;
                  File var4;
                  if (TbsShareManager.isThirdPartyApp(var0)) {
                     if (!TbsShareManager.j(var0)) {
                        TbsCoreLoadStat.getInstance().a(var0, 304, new Throwable("isShareTbsCoreAvailable false!"));
                        return false;
                     }

                     var8 = new File(TbsShareManager.c(var0), "tbs_sdk_extension_dex.jar");
                  } else {
                     var4 = TbsInstaller.a().getTbsCoreShareDir(var0);
                     var8 = new File(var4, "tbs_sdk_extension_dex.jar");
                  }

                  if (!var8.exists()) {
                     try {
                        TbsLog.e("QbSdk", "QbSdk init (false) tbs_sdk_extension_dex.jar is not exist!");
                        int var11 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(var0);
                        File var10 = new File(var8.getParentFile(), "tbs_jars_fusion_dex.jar");
                        if (var10.exists()) {
                           if (var11 > 0) {
                              TbsCoreLoadStat.getInstance().a(var0, 4131, new Exception("tbs_sdk_extension_dex not exist(with fusion dex)!" + var11));
                           } else {
                              TbsCoreLoadStat.getInstance().a(var0, 4132, new Exception("tbs_sdk_extension_dex not exist(with fusion dex)!" + var11));
                           }
                        } else if (var11 > 0) {
                           TbsCoreLoadStat.getInstance().a(var0, 4121, new Exception("tbs_sdk_extension_dex not exist(without fusion dex)!" + var11));
                        } else {
                           TbsCoreLoadStat.getInstance().a(var0, 4122, new Exception("tbs_sdk_extension_dex not exist(without fusion dex)!" + var11));
                        }
                     } catch (Throwable var6) {
                        var6.printStackTrace();
                     }

                     return false;
                  } else {
                     var4 = null;
                     String var9;
                     if (TbsShareManager.getHostCorePathAppDefined() != null) {
                        var9 = TbsShareManager.getHostCorePathAppDefined();
                     } else {
                        var9 = var2.getAbsolutePath();
                     }

                     TbsLog.i("QbSdk", "QbSdk init optDirExtension #1 is " + var9);
                     TbsLog.i("QbSdk", "new DexLoader #1 dexFile is " + var8.getAbsolutePath());
                     X5CoreEngine.getInstance().tryTbsCoreLoadFileLock(var0);
                     TbsCheckUtils.a(var0);
                     DexLoader var5 = new DexLoader(var8.getParent(), var0, new String[]{var8.getAbsolutePath()}, var9, getSettings());
                     q = var5.loadClass("com.tencent.tbs.sdk.extension.TbsSDKExtension");
                     loadTBSSDKExtension(var0, var8.getParent());
                     ReflectionUtils.invokeInstance(r, "setClientVersion", new Class[]{Integer.TYPE}, 1);
                     return true;
                  }
               }
            }
         } catch (Throwable var7) {
            TbsLog.e("QbSdk", "QbSdk init Throwable: " + Log.getStackTraceString(var7));
            TbsCoreLoadStat.getInstance().a(var0, 306, var7);
            return false;
         }
      }
   }

   public static void loadTBSSDKExtension(Context var0, String var1) {
      if (r == null) {
         Class var2 = QbSdk.class;
         synchronized(QbSdk.class) {
            if (r == null) {
               if (q == null) {
                  TbsLog.i("QbSdk", "QbSdk loadTBSSDKExtension sExtensionClass is null");
               }

               try {
                  Constructor var3 = null;

                  boolean var4;
                  try {
                     var3 = q.getConstructor(Context.class, Context.class, String.class, String.class, String.class);
                     var4 = true;
                  } catch (Throwable var8) {
                     var4 = false;
                  }

                  if (TbsShareManager.isThirdPartyApp(var0)) {
                     Context var5 = TbsShareManager.e(var0);
                     if (var5 == null && TbsShareManager.getHostCorePathAppDefined() == null) {
                        TbsLogReport.getInstance(var0.getApplicationContext()).setLoadErrorCode(227, (String)"host context is null!");
                        return;
                     }

                     if (var0.getApplicationContext() != null) {
                        var0 = var0.getApplicationContext();
                     }

                     if (!var4) {
                        if (var5 == null) {
                           var3 = q.getConstructor(Context.class, Context.class, String.class);
                           r = var3.newInstance(var0, var5, TbsShareManager.getHostCorePathAppDefined(), var1, null);
                        } else {
                           var3 = q.getConstructor(Context.class, Context.class);
                           r = var3.newInstance(var0, var5);
                        }
                     } else {
                        Object var6 = null;
                        r = var3.newInstance(var0, var5, TbsShareManager.getHostCorePathAppDefined(), var1, var6);
                     }
                  } else if (!var4) {
                     var3 = q.getConstructor(Context.class, Context.class);
                     if (var0.getApplicationContext() != null) {
                        var0 = var0.getApplicationContext();
                     }

                     r = var3.newInstance(var0, var0);
                  } else {
                     String var11 = null;
                     if ("com.tencent.mm".equals(getCurrentProcessName(var0)) && !WebView.mWebViewCreated) {
                        var11 = "notLoadSo";
                     }

                     if (var0.getApplicationContext() != null) {
                        var0 = var0.getApplicationContext();
                     }

                     r = var3.newInstance(var0, var0, null, var1, var11);
                  }
               } catch (Throwable var9) {
                  TbsLog.e("QbSdk", "throwable" + Log.getStackTraceString(var9));
               }

            }
         }
      }
   }

   public static void initForinitAndNotLoadSo(Context var0) {
      if (q == null) {
         File var1 = TbsInstaller.a().getTbsCoreShareDir(var0);
         if (var1 == null) {
            Log.e("QbSdk", "QbSdk initForinitAndNotLoadSo optDir == null");
            return;
         }

         File var2 = new File(var1, "tbs_sdk_extension_dex.jar");
         if (!var2.exists()) {
            Log.e("QbSdk", "QbSdk initForinitAndNotLoadSo dexFile.exists()=false");
            return;
         }

         String var3 = var1.getAbsolutePath();
         X5CoreEngine.getInstance().tryTbsCoreLoadFileLock(var0);
         TbsCheckUtils.a(var0);
         DexLoader var4 = new DexLoader(var2.getParent(), var0, new String[]{var2.getAbsolutePath()}, var3, getSettings());
         q = var4.loadClass("com.tencent.tbs.sdk.extension.TbsSDKExtension");
      }

   }

   public static boolean canLoadX5FirstTimeThirdApp(Context var0) {
      try {
//         if (var0.getApplicationInfo().packageName.contains("com.moji.mjweather") && android.os.Build.VERSION.SDK_INT == 19) {
//            return true;
//         } else
		{
            if (q == null) {
               File var1 = TbsInstaller.a().getTbsCoreShareDir(var0);
               if (var1 == null) {
                  TbsLog.e("QbSdk", "QbSdk canLoadX5FirstTimeThirdApp (false) optDir == null");
                  return false;
               }

               File var2 = new File(TbsShareManager.c(var0), "tbs_sdk_extension_dex.jar");
               if (!var2.exists()) {
                  TbsLog.e("QbSdk", "QbSdk canLoadX5FirstTimeThirdApp (false) dexFile.exists()=false", true);
                  return false;
               }

               String var3 = null;
               if (TbsShareManager.getHostCorePathAppDefined() != null) {
                  var3 = TbsShareManager.getHostCorePathAppDefined();
               } else {
                  var3 = var1.getAbsolutePath();
               }

               TbsLog.i("QbSdk", "QbSdk init optDirExtension #2 is " + var3);
               TbsLog.i("QbSdk", "new DexLoader #2 dexFile is " + var2.getAbsolutePath());
               X5CoreEngine.getInstance().tryTbsCoreLoadFileLock(var0);
               TbsCheckUtils.a(var0);
               DexLoader var4 = new DexLoader(var2.getParent(), var0, new String[]{var2.getAbsolutePath()}, var3, getSettings());
               q = var4.loadClass("com.tencent.tbs.sdk.extension.TbsSDKExtension");
               if (r == null) {
                  Context var5 = TbsShareManager.e(var0);
                  if (var5 == null && TbsShareManager.getHostCorePathAppDefined() == null) {
                     TbsLogReport.getInstance(var0.getApplicationContext()).setLoadErrorCode(227, (String)"host context is null!");
                     return false;
                  }

                  loadTBSSDKExtension(var0, var2.getParent());
               }
            }

            Object var7 = ReflectionUtils.getDeclaredMethod(r, "canLoadX5CoreForThirdApp", new Class[0]);
            return var7 != null && var7 instanceof Boolean ? (Boolean)var7 : false;
         }
      } catch (Throwable var6) {
         TbsLog.e("QbSdk", "canLoadX5FirstTimeThirdApp sys WebView: " + Log.getStackTraceString(var6));
         return false;
      }
   }

   static boolean a(Context var0) {
      try {
         if (q != null) {
            return true;
         } else {
            File var1 = TbsInstaller.a().getTbsCoreShareDir(var0);
            if (var1 == null) {
               TbsLog.e("QbSdk", "QbSdk initExtension (false) optDir == null");
               return false;
            } else {
               File var2 = new File(var1, "tbs_sdk_extension_dex.jar");
               if (!var2.exists()) {
                  TbsLog.e("QbSdk", "QbSdk initExtension (false) dexFile.exists()=false", true);
                  return false;
               } else {
                  TbsLog.i("QbSdk", "new DexLoader #3 dexFile is " + var2.getAbsolutePath());
                  X5CoreEngine.getInstance().tryTbsCoreLoadFileLock(var0);
                  TbsCheckUtils.a(var0);
                  DexLoader var3 = new DexLoader(var2.getParent(), var0, new String[]{var2.getAbsolutePath()}, var1.getAbsolutePath(), getSettings());
                  q = var3.loadClass("com.tencent.tbs.sdk.extension.TbsSDKExtension");
                  loadTBSSDKExtension(var0, var2.getParent());
                  return true;
               }
            }
         }
      } catch (Throwable var4) {
         TbsLog.e("QbSdk", "initExtension sys WebView: " + Log.getStackTraceString(var4));
         return false;
      }
   }

   private static boolean c(Context var0) {
      try {
         if (q != null) {
            return true;
         } else {
            File var1 = TbsInstaller.a().getTbsCoreShareDir(var0);
            if (var1 == null) {
               TbsLog.e("QbSdk", "QbSdk initForX5DisableConfig (false) optDir == null");
               return false;
            } else {
               File var2 = null;
               File var3;
               if (TbsShareManager.isThirdPartyApp(var0)) {
                  if (!TbsShareManager.j(var0)) {
                     TbsCoreLoadStat.getInstance().a(var0, 304);
                     return false;
                  }

                  var2 = new File(TbsShareManager.c(var0), "tbs_sdk_extension_dex.jar");
               } else {
                  var3 = TbsInstaller.a().getTbsCoreShareDir(var0);
                  var2 = new File(var3, "tbs_sdk_extension_dex.jar");
               }

               if (!var2.exists()) {
                  TbsCoreLoadStat.getInstance().a(var0, 406, new Exception("initForX5DisableConfig failure -- tbs_sdk_extension_dex.jar is not exist!"));
                  return false;
               } else {
                  var3 = null;
                  String var6;
                  if (TbsShareManager.getHostCorePathAppDefined() != null) {
                     var6 = TbsShareManager.getHostCorePathAppDefined();
                  } else {
                     var6 = var1.getAbsolutePath();
                  }

                  TbsLog.i("QbSdk", "QbSdk init optDirExtension #3 is " + var6);
                  TbsLog.i("QbSdk", "new DexLoader #4 dexFile is " + var2.getAbsolutePath());
                  X5CoreEngine.getInstance().tryTbsCoreLoadFileLock(var0);
                  TbsCheckUtils.a(var0);
                  DexLoader var4 = new DexLoader(var2.getParent(), var0, new String[]{var2.getAbsolutePath()}, var6, getSettings());
                  q = var4.loadClass("com.tencent.tbs.sdk.extension.TbsSDKExtension");
                  loadTBSSDKExtension(var0, var2.getParent());
                  ReflectionUtils.invokeInstance(r, "setClientVersion", new Class[]{Integer.TYPE}, 1);
                  return true;
               }
            }
         }
      } catch (Throwable var5) {
         TbsLog.e("QbSdk", "initForX5DisableConfig sys WebView: " + Log.getStackTraceString(var5));
         return false;
      }
   }

   public static void setOnlyDownload(boolean var0) {
      k = var0;
   }

   public static boolean getOnlyDownload() {
      return k;
   }

   static boolean b(Context var0) {
      if (var0 == null) {
         return false;
      } else {
         try {
            if (var0.getApplicationInfo().packageName.contains("com.tencent.portfolio")) {
               TbsLog.i("QbSdk", "clearPluginConfigFile #1");
               TbsDownloadConfig var1 = TbsDownloadConfig.getInstance(var0);
               String var2 = var1.mPreferences.getString("app_versionname", (String)null);
               PackageInfo var3 = var0.getPackageManager().getPackageInfo("com.tencent.portfolio", 0);
               String var4 = var3.versionName;
               TbsLog.i("QbSdk", "clearPluginConfigFile oldAppVersionName is " + var2 + " newAppVersionName is " + var4);
               if (var2 != null && !var2.contains(var4)) {
                  SharedPreferences var5 = var0.getSharedPreferences("plugin_setting", 0);
                  if (var5 != null) {
                     Editor var6 = var5.edit();
                     var6.clear();
                     var6.commit();
                     TbsLog.i("QbSdk", "clearPluginConfigFile done");
                  }
               }
            }

            return true;
         } catch (Throwable var7) {
            TbsLog.i("QbSdk", "clearPluginConfigFile error is " + var7.getMessage());
            return false;
         }
      }
   }

   static Bundle a(Context var0, Bundle var1) throws Exception {
      if (!a(var0)) {
         TbsLogReport.getInstance(var0).setInstallErrorCode(216, (String)"initForPatch return false!");
         return null;
      } else {
         Object var2 = ReflectionUtils.invokeInstance(r, "incrUpdate", new Class[]{Context.class, Bundle.class}, var0, var1);
         if (var2 != null) {
            return (Bundle)var2;
         } else {
            TbsLogReport.getInstance(var0).setInstallErrorCode(216, (String)"incrUpdate return null!");
            return null;
         }
      }
   }

   static boolean a(Context var0, int var1) {
      return a(var0, var1, 20000);
   }

   static boolean a(Context var0, int var1, int var2) {
      if (n != null && n.containsKey("SET_SENDREQUEST_AND_UPLOAD") && n.get("SET_SENDREQUEST_AND_UPLOAD").equals("false")) {
         TbsLog.i("QbSdk", "[QbSdk.isX5Disabled] -- SET_SENDREQUEST_AND_UPLOAD is false");
         return true;
      } else {
         TbsInstaller.a().installTbsCoreIfNeeded(var0, SDKEngine.a == 0);
         if (!c(var0)) {
            return true;
         } else {
            Object var3 = ReflectionUtils.invokeInstance(r, "isX5Disabled", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE}, var1, 43967, var2);
            if (var3 != null) {
               return (Boolean)var3;
            } else {
               var3 = ReflectionUtils.invokeInstance(r, "isX5Disabled", new Class[]{Integer.TYPE, Integer.TYPE}, var1, 43967);
               return var3 != null ? (Boolean)var3 : true;
            }
         }
      }
   }

   public static boolean canLoadX5(Context var0) {
      return init(var0, false, false);
   }

   public static boolean canOpenWebPlus(Context var0) {
      if (x == 0) {
         x = MemInfoHelper.getTotalMemory();
      }

      TbsLog.i("QbSdk", "canOpenWebPlus - totalRAM: " + x);
      if (android.os.Build.VERSION.SDK_INT >= 7 && x >= y) {
         if (var0 == null) {
            return false;
         } else {
            boolean var1 = false;
            FileInputStream var2 = null;
            BufferedInputStream var3 = null;

            int var8;
            int var9;
            int var43;
            try {
               File var4 = new File(TbsInstaller.a().getTbsCoreShareDir(var0), "tbs.conf");
               var2 = new FileInputStream(var4);
               var3 = new BufferedInputStream(var2);
               Properties var45 = new Properties();
               var45.load(var3);
               String var6 = var45.getProperty("android_sdk_max_supported");
               String var7 = var45.getProperty("android_sdk_min_supported");
               var8 = Integer.parseInt(var6);
               var9 = Integer.parseInt(var7);
               int var10 = Integer.parseInt(android.os.Build.VERSION.SDK);
               if (var10 > var8 || var10 < var9) {
                  TbsLog.i("QbSdk", "canOpenWebPlus - sdkVersion: " + var10);
                  boolean var11 = false;
                  return var11;
               }

               var43 = Integer.parseInt(var45.getProperty("tbs_core_version"));
            } catch (Throwable var41) {
               var41.printStackTrace();
               TbsLog.i("QbSdk", "canOpenWebPlus - canLoadX5 Exception");
               boolean var5 = false;
               return var5;
            } finally {
               try {
                  if (var3 != null) {
                     var3.close();
                  }
               } catch (Exception var38) {
               }

            }

            boolean var44 = false;
            FileInputStream var46 = null;

            try {
               File var47 = new File(TbsInstaller.getTbsCorePrivateDir(var0), "tbs_extension.conf");
               var46 = new FileInputStream(var47);
               Properties var48 = new Properties();
               var48.load(var46);
               var8 = Integer.parseInt(var48.getProperty("tbs_local_version"));
               var9 = Integer.parseInt(var48.getProperty("app_versioncode_for_switch"));
               if (var43 != 88888888 && var8 != 88888888) {
                  if (var43 > var8) {
                     var44 = false;
                  } else if (var43 == var8) {
                     if (var9 > 0 && var9 != AppUtil.getVersionCode(var0)) {
                        var44 = false;
                     } else {
                        var44 = Boolean.parseBoolean(var48.getProperty("x5_disabled")) && !TbsDownloadConfig.getInstance(var0.getApplicationContext()).mPreferences.getBoolean("switch_backupcore_enable", false);
                     }
                  }
               } else {
                  var44 = false;
               }
            } catch (Throwable var39) {
               var44 = true;
               TbsLog.i("QbSdk", "canOpenWebPlus - isX5Disabled Exception");
            } finally {
               try {
                  if (var46 != null) {
                     var46.close();
                  }
               } catch (Exception var37) {
               }

            }

            return !var44;
         }
      } else {
         return false;
      }
   }

   public static boolean isX5DisabledSync(Context var0) {
      int var1 = TbsCoreInstallPropertiesHelper.getInstance(var0).getInstallStatus();
      boolean var2 = var1 == 2;
      if (var2) {
         return false;
      } else if (!c(var0)) {
         return true;
      } else {
         int var3 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(var0);
         Object var4 = ReflectionUtils.invokeInstance(r, "isX5DisabledSync", new Class[]{Integer.TYPE, Integer.TYPE}, var3, 43967);
         return var4 != null ? (Boolean)var4 : true;
      }
   }

   public static void setTbsLogClient(TbsLogClient var0) {
      TbsLog.setTbsLogClient(var0);
   }

   public static boolean installLocalQbApk(Context var0, String var1, String var2, Bundle var3) {
      SDKEngine var4 = SDKEngine.getInstance(true);
      var4.init(var0, false, false);
      return null != var4 && var4.isInitialized() ? var4.a().installLocalQbApk(var0, var1, var2, var3) : false;
   }

   public static boolean canUseVideoFeatrue(Context var0, int var1) {
      Object var2 = ReflectionUtils.invokeInstance(r, "canUseVideoFeatrue", new Class[]{Integer.TYPE}, var1);
      return var2 != null && var2 instanceof Boolean ? (Boolean)var2 : false;
   }

   public static boolean canLoadVideo(Context var0) {
      Object var1 = ReflectionUtils.invokeInstance(r, "canLoadVideo", new Class[]{Integer.TYPE}, 0);
      if (var1 != null) {
         boolean var2 = (Boolean)var1;
         if (!var2) {
            TbsCoreLoadStat.getInstance().a(var0, 313);
         }
      } else {
         TbsCoreLoadStat.getInstance().a(var0, 314);
      }

      return var1 == null ? false : (Boolean)var1;
   }

   static boolean init(Context var0, boolean var1, boolean var2) {
      boolean var3 = false;
      int var4 = TbsPVConfig.getInstance(var0).getDisabledCoreVersion();
      if (var4 != 0 && var4 == TbsInstaller.a().getTbsCoreInstalledVerInNolock(var0)) {
         TbsLog.e("QbSdk", "force use sys by remote switch");
         return var3;
      } else if (TbsShareManager.isThirdPartyApp(var0) && !TbsShareManager.isShareTbsCoreAvailableInner(var0)) {
         TbsCoreLoadStat.getInstance().a(var0, 302);
         return var3;
      } else if (!init(var0, var1)) {
         TbsLog.e("QbSdk", "QbSdk.init failure!");
         return var3;
      } else {
         Object var5 = ReflectionUtils.invokeInstance(r, "canLoadX5Core", new Class[]{Integer.TYPE}, 43967);
         if (var5 != null) {
            if (var5 instanceof String && ((String)var5).equalsIgnoreCase("AuthenticationFail")) {
               return false;
            }

            if (!(var5 instanceof Bundle)) {
               var3 = false;
               TbsCoreLoadStat.getInstance().a(var0, 330, new Throwable("" + var5));
               TbsLog.e("loaderror", "ret not instance of bundle");
               return var3;
            }

            Bundle var6 = (Bundle)var5;
            if (var6.isEmpty()) {
               TbsCoreLoadStat.getInstance().a(var0, 331, new Throwable("" + var5));
               TbsLog.e("loaderror", "empty bundle");
               return false;
            }

            int var7 = -1;

            try {
               var7 = var6.getInt("result_code", -1);
            } catch (Exception var15) {
               TbsLog.e("QbSdk", "bundle.getInt(KEY_RESULT_CODE) error : " + var15.toString());
            }

            var3 = var7 == 0;
            if (TbsShareManager.isThirdPartyApp(var0)) {
               SDKEngine.a(TbsShareManager.d(var0));
               p = String.valueOf(TbsShareManager.d(var0));
               if (p.length() == 5) {
                  p = "0" + p;
               }

               if (p.length() != 6) {
                  p = "";
               }
            } else {
               try {
				   p = var6.getString("tbs_core_version", "0");
			   } catch (Exception var14) {
                  p = "0";
               }

               try {
                  o = Integer.parseInt(p);
               } catch (NumberFormatException var13) {
                  o = 0;
               }

               SDKEngine.a(o);
               if (o == 0) {
                  TbsCoreLoadStat.getInstance().a(var0, 307, new Throwable("sTbsVersion is 0"));
                  return false;
               }

               boolean var8 = o > 0 && o <= 25442 || o == 25472;
               if (var8) {
                  TbsLog.e("TbsDownload", "is_obsolete --> delete old core:" + o);
                  File var9 = TbsInstaller.a().getTbsCoreShareDir(var0);
                  FileHelper.delete(var9);
                  TbsCoreLoadStat.getInstance().a(var0, 307, new Throwable("is_obsolete --> delete old core:" + o));
                  return false;
               }
            }

            try {
               t = var6.getStringArray("tbs_jarfiles");
            } catch (Throwable var12) {
               TbsCoreLoadStat.getInstance().a(var0, 329, var12);
               return false;
            }

            if (!(t instanceof String[])) {
               TbsCoreLoadStat.getInstance().a(var0, 307, new Throwable("sJarFiles not instanceof String[]: " + t));
               return false;
            }

            try {
               d = var6.getString("tbs_librarypath");
            } catch (Exception var11) {
               return false;
            }

            Object var18 = null;
            if (var7 != 0) {
               try {
                  var18 = ReflectionUtils.getDeclaredMethod(r, "getErrorCodeForLogReport", new Class[0]);
               } catch (Exception var10) {
                  var10.printStackTrace();
               }
            }

            switch(var7) {
            case -2:
               if (var18 instanceof Integer) {
                  TbsCoreLoadStat.getInstance().a(var0, (Integer)var18, new Throwable("detail: " + var18));
               } else {
                  TbsCoreLoadStat.getInstance().a(var0, 404, new Throwable("detail: " + var18));
               }
               break;
            case -1:
               if (var18 instanceof Integer) {
                  TbsCoreLoadStat.getInstance().a(var0, (Integer)var18, new Throwable("detail: " + var18));
               } else {
                  TbsCoreLoadStat.getInstance().a(var0, 307, new Throwable("detail: " + var18));
               }
            case 0:
               break;
            default:
               TbsCoreLoadStat.getInstance().a(var0, 415, new Throwable("detail: " + var18 + "errcode" + var7));
            }
         } else {
            var5 = ReflectionUtils.invokeInstance(r, "canLoadX5", new Class[]{Integer.TYPE}, MemInfoHelper.getTotalMemory());
            if (var5 != null) {
               if (var5 instanceof String && ((String)var5).equalsIgnoreCase("AuthenticationFail")) {
                  return false;
               }

               if (var5 instanceof Boolean) {
                  o = SDKEngine.d();
                  boolean var16 = a(var0, SDKEngine.d());
                  boolean var17 = (Boolean)var5 && !var16;
                  if (!var17) {
                     TbsLog.e("loaderror", "318");
                     TbsLog.w("loaderror", "isX5Disable:" + var16);
                     TbsLog.w("loaderror", "(Boolean) ret:" + (Boolean)var5);
                  }

                  return var17;
               }
            } else {
               TbsCoreLoadStat.getInstance().a(var0, 308);
            }
         }

         if (!var3) {
            TbsLog.e("loaderror", "319");
         }

         return var3;
      }
   }

   public static boolean canOpenMimeFileType(Context var0, String var1) {
      return !init(var0, false) ? false : false;
   }

   public static void setCurrentID(String var0) {
      if (var0 != null) {
         if (var0.startsWith("QQ:")) {
            String var1 = "0000000000000000";
            String var2 = var0.substring("QQ:".length());
            z = var1.substring(var2.length()) + var2;
         }

      }
   }

   public static String getTID() {
      return z;
   }

   public static String getTbsResourcesPath(Context var0) {
      return TbsShareManager.g(var0);
   }

   public static void setQQBuildNumber(String var0) {
      A = var0;
   }

   public static String getQQBuildNumber() {
      return A;
   }

   static synchronized void forceSysWebViewInner(Context context, String str) {
      if (!forcedSysByInner) {
         forcedSysByInner = true;
         v = "forceSysWebViewInner: " + str;
         TbsLog.e("QbSdk", "QbSdk.SysWebViewForcedInner..." + v);
         TbsCoreLoadStat.getInstance().a(context, 401, new Throwable(v));
      }
   }

   public static void forceSysWebView() {
      forcedSysByOuter = true;
      u = "SysWebViewForcedByOuter: " + Log.getStackTraceString(new Throwable());
      TbsLog.e("QbSdk", "sys WebView: SysWebViewForcedByOuter");
   }

   public static void unForceSysWebView() {
      forcedSysByOuter = false;
      TbsLog.e("QbSdk", "sys WebView: unForceSysWebView called");
   }

   public static void canOpenFile(final Context var0, final String var1, final ValueCallback<Boolean> var2) {
      (new Thread() {
         public void run() {
            X5CoreEngine var1x = X5CoreEngine.getInstance();
            var1x.init(var0);
            boolean var2x = false;
            if (var1x.isInCharge()) {
               var2x = var1x.getWVWizardBase().canOpenFile(var0, var1);
            }
	
			 boolean finalVar2x = var2x;
			 (new Handler(Looper.getMainLooper())).post(new Runnable() {
               public void run() {
                  var2.onReceiveValue(finalVar2x);
               }
            });
         }
      }).start();
   }

   public static void closeFileReader(Context var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      var1.init(var0);
      if (var1.isInCharge()) {
         var1.getWVWizardBase().closeFileReader();
      }

   }

   /** @deprecated */
   public static synchronized void preInit(Context var0) {
      preInit(var0, (QbSdk.PreInitCallback)null);
   }

   public static void setDisableUseHostBackupCoreBySwitch(boolean var0) {
      mDisableUseHostBackupCore = var0;
      TbsLog.i("QbSdk", "setDisableUseHostBackupCoreBySwitch -- mDisableUseHostBackupCore is " + mDisableUseHostBackupCore);
   }

   public static void setDisableUnpreinitBySwitch(boolean var0) {
      B = var0;
      TbsLog.i("QbSdk", "setDisableUnpreinitBySwitch -- mDisableUnpreinitBySwitch is " + B);
   }

   public static synchronized boolean unPreInit(Context var0) {
      return true;
   }

   public static String getCurrentProcessName(Context var0) {
      FileInputStream var1 = null;

      try {
         String var2 = "/proc/self/cmdline";
         var1 = new FileInputStream(var2);
         byte[] var3 = new byte[256];

         int var4;
         int var5;
         for(var4 = 0; (var5 = var1.read()) > 0 && var4 < var3.length; var3[var4++] = (byte)var5) {
         }

         if (var4 > 0) {
            String var6 = new String(var3, 0, var4, "UTF-8");
            String var7 = var6;
            return var7;
         }
      } catch (Throwable var18) {
         var18.printStackTrace();
      } finally {
         if (var1 != null) {
            try {
               var1.close();
            } catch (IOException var17) {
               var17.printStackTrace();
            }
         }

      }

      return null;
   }

   public static synchronized void preInit(final Context var0, final QbSdk.PreInitCallback var1) {
      TbsLog.initIfNeed(var0);
      TbsLog.i("QbSdk", "preInit -- processName: " + getCurrentProcessName(var0));
      TbsLog.i("QbSdk", "preInit -- stack: " + Log.getStackTraceString(new Throwable("#")));
      l = forcedSysByInner;
      if (!s) {
         final Handler var2 = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message var1x) {
               switch(var1x.what) {
               case 1:
                  TbsExtensionFunctionManager var2 = TbsExtensionFunctionManager.getInstance();
                  QbSdk.B = var2.canUseFunction(var0, "disable_unpreinit.txt");
                  if (QbSdk.j) {
                     WebViewWizardBase var3 = X5CoreEngine.getInstance().getWVWizardBase();
                     if (var3 != null) {
                        var3.createSDKWebview(var0);
                     }
                  }

                  if (var1 != null) {
                     var1.onViewInitFinished(true);
                  }
                  break;
               case 2:
                  if (var1 != null) {
                     var1.onViewInitFinished(false);
                  }
                  break;
               case 3:
                  if (var1 != null) {
                     var1.onCoreInitFinished();
                  }
               }

            }
         };
         Thread var3 = new Thread() {
            public void run() {
               int var1 = TbsInstaller.a().a(true, var0);
               TbsDownloader.setAppContext(var0);
               TbsLog.i("QbSdk", "QbSdk preinit ver is " + var1);
               if (var1 == 0) {
                  TbsInstaller.a().installTbsCoreIfNeeded(var0, true);
               }

               TbsLog.i("QbSdk", "preInit -- prepare initAndLoadSo");
               SDKEngine var2x = SDKEngine.getInstance(true);
               var2x.init(var0, false, false);
               X5CoreEngine var3 = X5CoreEngine.getInstance();
               var3.init(var0);
               boolean var4 = var3.isInCharge();
               var2.sendEmptyMessage(3);
               if (!var4) {
                  var2.sendEmptyMessage(2);
               } else {
                  var2.sendEmptyMessage(1);
               }

            }
         };
         var3.setName("tbs_preinit");
         var3.setPriority(10);
         var3.start();
         s = true;
      }

   }

   public static void setUploadCode(Context var0, int var1) {
      TbsDownloadUpload var2;
      if (var1 >= 130 && var1 <= 139) {
         var2 = TbsDownloadUpload.getInstance(var0);
         var2.a.put("tbs_needdownload_code", var1);
         var2.commit();
      } else if (var1 >= 150 && var1 <= 159) {
         var2 = TbsDownloadUpload.getInstance(var0);
         var2.a.put("tbs_startdownload_code", var1);
         var2.commit();
      }

   }

   public static void checkTbsValidity(Context var0) {
      if (var0 != null) {
         if (!TbsCheckUtils.checkTbsValidity(var0)) {
            TbsLog.e("QbSdk", "sys WebView: SysWebViewForcedBy checkTbsValidity");
            TbsCoreLoadStat.getInstance().a(var0, 419);
            forceSysWebView();
         }

      }
   }

   public static void disableAutoCreateX5Webview() {
      j = false;
   }

   public static boolean isTbsCoreInited() {
      SDKEngine var0 = SDKEngine.getInstance(false);
      return var0 != null && var0.g();
   }

   public static void initX5Environment(final Context var0, final QbSdk.PreInitCallback var1) {
      TbsLog.initIfNeed(var0);
      if (var0 == null) {
         TbsLog.e("QbSdk", "initX5Environment,context=null");
      } else {
         b(var0);
         E = new TbsListener() {
            public void onDownloadFinish(int var1x) {
            }

            public void onInstallFinish(int var1x) {
               QbSdk.preInit(var0, var1);
            }

            public void onDownloadProgress(int var1x) {
            }
         };
         if (TbsShareManager.isThirdPartyApp(var0)) {
            TbsInstaller.a().installTbsCoreIfNeeded(var0, SDKEngine.a == 0);
         }

         TbsDownloader.needDownload(var0, false, false, true, new TbsDownloader.TbsDownloaderCallback() {
            public void onNeedDownloadFinish(boolean var1x, int var2) {
               if (TbsShareManager.findCoreForThirdPartyApp(var0) == 0 && !TbsShareManager.getCoreDisabled()) {
                  TbsShareManager.forceToLoadX5ForThirdApp(var0, false);
               }

               if (QbSdk.i && TbsShareManager.isThirdPartyApp(var0)) {
                  TbsExtensionFunctionManager.getInstance().initTbsBuglyIfNeed(var0);
               }

               QbSdk.preInit(var0, var1);
            }
         });
      }
   }

   private static void d(Context var0) {
      C = true;
      byte var1 = -1;
      int var2 = -1;
      boolean var3 = true;
      SharedPreferences var4 = null;
      int var5 = -1;
      int var6 = -1;

      int var13;
      Editor var10000;
      try {
		  var4 = var0.getSharedPreferences("tbs_preloadx5_check_cfg_file", 4);
	
		  var13 = var4.getInt("tbs_preload_x5_recorder", -1);
         boolean var12;
         if (var13 >= 0) {
            ++var13;
            var5 = var13;
            if (var13 > 4) {
               var12 = true;
               return;
            }
         }

         var2 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(var0);
         if (var2 <= 0) {
            var12 = true;
            return;
         }

         if (var13 <= 4) {
            var4.edit().putInt("tbs_preload_x5_recorder", var13).commit();
         }

         var13 = var4.getInt("tbs_preload_x5_counter", -1);
         if (var13 >= 0) {
            var10000 = var4.edit();
            ++var13;
            var10000.putInt("tbs_preload_x5_counter", var13).commit();
            var6 = var13;
         }
      } catch (Throwable var11) {
         TbsLog.e("QbSdk", "tbs_preload_x5_counter Inc exception:" + Log.getStackTraceString(var11));
      }

      if (var6 > 3) {
         try {
            var13 = var4.getInt("tbs_preload_x5_version", -1);
            Editor var7 = var4.edit();
            if (var13 == var2) {
               FileHelper.delete(TbsInstaller.a().getTbsCoreShareDir(var0), false);
               File var8 = TbsCoreInstallPropertiesHelper.getInstance(var0).ensureTbsCorePrivate_tbscoreinstallF();
               if (var8 != null) {
                  FileHelper.delete(var8, false);
               }

               TbsLog.e("QbSdk", "QbSdk - preload_x5_check: tbs core " + var2 + " is deleted!");
            } else {
               TbsLog.e("QbSdk", "QbSdk - preload_x5_check -- reset exception core_ver:" + var2 + "; value:" + var13);
            }

            var7.putInt("tbs_precheck_disable_version", var13);
            var7.commit();
         } catch (Throwable var9) {
            TbsLog.e("QbSdk", "tbs_preload_x5_counter disable version exception:" + Log.getStackTraceString(var9));
         }

      } else {
         if (var5 > 0 && var5 <= 3) {
            TbsLog.i("QbSdk", "QbSdk - preload_x5_check -- before creation!");
            X5CoreEngine.getInstance().init(var0);
            TbsLog.i("QbSdk", "QbSdk - preload_x5_check -- after creation!");
            var1 = 0;
         }

         try {
            var13 = var4.getInt("tbs_preload_x5_counter", -1);
            if (var13 > 0) {
               var10000 = var4.edit();
               --var13;
               var10000.putInt("tbs_preload_x5_counter", var13).commit();
            }
         } catch (Throwable var10) {
            TbsLog.e("QbSdk", "tbs_preload_x5_counter Dec exception:" + Log.getStackTraceString(var10));
         }

         TbsLog.i("QbSdk", "QbSdk -- preload_x5_check result:" + var1);
      }
   }

   public static int getTbsSdkVersion() {
      return 43967;
   }

   public static int getTbsVersion(Context var0) {
      if (TbsShareManager.isThirdPartyApp(var0)) {
         return TbsShareManager.a(var0, false);
      } else {
         int var1 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(var0);
         return var1;
      }
   }

   public static int getTbsVersionForCrash(Context var0) {
      if (TbsShareManager.isThirdPartyApp(var0)) {
         return TbsShareManager.a(var0, false);
      } else {
         int var1 = TbsInstaller.a().j(var0);
         if (var1 == 0 && TbsCoreInstallPropertiesHelper.getInstance(var0).getInstallStatus() == 3) {
            reset(var0);
         }

         return var1;
      }
   }

   public static void continueLoadSo(Context var0) {
      //if ("com.tencent.mm".equals(getCurrentProcessName(var0)) && WebView.mWebViewCreated)
      {
         ReflectionUtils.getDeclaredMethod(r, "continueLoadSo", new Class[0]);
      }
   }

   public static boolean getJarFilesAndLibraryPath(Context var0) {
      if (r == null) {
         TbsLog.i("QbSdk", "getJarFilesAndLibraryPath sExtensionObj is null");
         return false;
      } else {
         Object var1 = ReflectionUtils.invokeInstance(r, "canLoadX5CoreAndNotLoadSo", new Class[]{Integer.TYPE}, 43967);
         Bundle var2 = (Bundle)var1;
         if (var2 == null) {
            TbsLog.i("QbSdk", "getJarFilesAndLibraryPath bundle is null and coreverison is " + TbsInstaller.a().a(true, var0));
            return false;
         } else {
            t = var2.getStringArray("tbs_jarfiles");
            d = var2.getString("tbs_librarypath");
            return true;
         }
      }
   }

   public static String[] getDexLoaderFileList(Context var0, Context var1, String var2) {
      if (!(t instanceof String[])) {
         Object var6 = ReflectionUtils.invokeInstance(r, "getJarFiles", new Class[]{Context.class, Context.class, String.class}, var0, var1, var2);
         return (String[])((String[])(var6 instanceof String[] ? var6 : new String[]{""}));
      } else {
         int var3 = t.length;
         String[] var4 = new String[var3];

         for(int var5 = 0; var5 < var3; ++var5) {
            var4[var5] = var2 + t[var5];
         }

         return var4;
      }
   }

   public static boolean useSoftWare() {
      if (r == null) {
         return false;
      } else {
         Object var0 = ReflectionUtils.getDeclaredMethod(r, "useSoftWare", new Class[0]);
         if (var0 == null) {
            var0 = ReflectionUtils.invokeInstance(r, "useSoftWare", new Class[]{Integer.TYPE}, MemInfoHelper.getTotalMemory());
         }

         return var0 == null ? false : (Boolean)var0;
      }
   }

   public static void setTbsListener(TbsListener var0) {
      D = var0;
   }

   public static void setDownloadWithoutWifi(boolean var0) {
      F = var0;
   }

   public static boolean getDownloadWithoutWifi() {
      return F;
   }

   public static long getApkFileSize(Context var0) {
      return var0 != null ? TbsDownloadConfig.getInstance(var0.getApplicationContext()).mPreferences.getLong("tbs_apkfilesize", 0L) : 0L;
   }

   public static void setTBSInstallingStatus(boolean var0) {
      G = var0;
   }

   public static boolean getTBSInstalling() {
      return G;
   }

   static String a() {
      return p;
   }

   public static void reset(Context var0) {
      reset(var0, false);
   }

   public static void reset(Context var0, boolean var1) {
      TbsLog.e("QbSdk", "QbSdk reset!", true);

      try {
         TbsDownloader.stopDownload();
         boolean var2 = false;
         if (var1 && !TbsShareManager.isThirdPartyApp(var0)) {
            int var3 = TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var0);
            int var4 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(var0);
            if (var3 > 43300 && var3 != var4) {
               var2 = true;
            }
         }

         TbsDownloader.c(var0);
         File var8 = getTbsFolderDir(var0);
         FileHelper.delete(var8, false, "core_share_decouple");
         TbsLog.i("QbSdk", "delete downloaded apk success", true);
         TbsInstaller.a.set(0);
         File var9 = new File(var0.getFilesDir(), "bugly_switch.txt");
         if (var9 != null && var9.exists()) {
            var9.delete();
         }

         if (var2) {
            File var5 = TbsInstaller.a().getTbsCoreShareDecoupleDir(var0);
            File var6 = TbsInstaller.a().getCoreDir(var0, 0);
            FileHelper.forceTransferFile(var5, var6);
            TbsInstaller.a().b(var0);
         }
      } catch (Throwable var7) {
         TbsLog.e("QbSdk", "QbSdk reset exception:" + Log.getStackTraceString(var7));
      }

   }

   public static void resetDecoupleCore(Context var0) {
      TbsLog.e("QbSdk", "QbSdk resetDecoupleCore!", true);

      try {
         File var1 = TbsInstaller.a().getTbsCoreShareDecoupleDir(var0);
         FileHelper.delete(var1);
      } catch (Throwable var2) {
         TbsLog.e("QbSdk", "QbSdk resetDecoupleCore exception:" + Log.getStackTraceString(var2));
      }

   }

   public static void clear(Context var0) {
   }

   public static void clearAllWebViewCache(Context var0, boolean var1) {
      TbsLog.i("QbSdk", "clearAllWebViewCache(" + var0 + ", " + var1 + ")");
      boolean var2 = false;

      WebView var3;
      try {
         var3 = new XPlusWebView(var0);
         if (var3.getWebViewClientExtension() != null) {
            var2 = true;
            X5CoreEngine var4 = X5CoreEngine.getInstance();
            if (null != var4 && var4.isInCharge()) {
               var4.getWVWizardBase().clearAllCache(var0, var1);
            }
         }

         var3 = null;
      } catch (Throwable var6) {
         TbsLog.e("QbSdk", "clearAllWebViewCache exception 2 -- " + Log.getStackTraceString(var6));
      }

      if (var2) {
         TbsLog.i("QbSdk", "is_in_x5_mode --> no need to clear system webview!");
      } else {
         try {
            android.webkit.WebView var7 = new android.webkit.WebView(var0);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
               var7.removeJavascriptInterface("searchBoxJavaBridge_");
               var7.removeJavascriptInterface("accessibility");
               var7.removeJavascriptInterface("accessibilityTraversal");
            }

            var7.clearCache(true);
            if (var1) {
               android.webkit.CookieSyncManager.createInstance(var0);
               android.webkit.CookieManager.getInstance().removeAllCookie();
            }

            android.webkit.WebViewDatabase.getInstance(var0).clearUsernamePassword();
            android.webkit.WebViewDatabase.getInstance(var0).clearHttpAuthUsernamePassword();
            android.webkit.WebViewDatabase.getInstance(var0).clearFormData();
            android.webkit.WebStorage.getInstance().deleteAllData();
            android.webkit.WebIconDatabase.getInstance().removeAllIcons();
            var3 = null;
         } catch (Throwable var5) {
            TbsLog.e("QbSdk", "clearAllWebViewCache exception 1 -- " + Log.getStackTraceString(var5));
         }

      }
   }

   public static int startMiniQBToLoadUrl(Context var0, String var1, HashMap<String, String> var2, android.webkit.ValueCallback<String> var3) {
      TbsCoreLoadStat.getInstance().a(var0, 501);
      if (var0 == null) {
         return -100;
      } else {
         X5CoreEngine var4 = X5CoreEngine.getInstance();
         var4.init(var0);
         if (var4.isInCharge()) {
            if (var0 != null && var0.getApplicationInfo().packageName.equals("com.nd.android.pandahome2") && getTbsVersion(var0) < 25487) {
               return -101;
            } else {
               int var5 = var4.getWVWizardBase().startMiniQB(var0, var1, var2, (String)null, var3);
               if (var5 == 0) {
                  TbsCoreLoadStat.getInstance().a(var0, 503);
               } else {
                  TbsLogReport.getInstance(var0).setLoadErrorCode(504, (String)("" + var5));
               }

               Log.e("QbSdk", "startMiniQBToLoadUrl  ret = " + var5);
               return var5;
            }
         } else {
            TbsCoreLoadStat.getInstance().a(var0, 502);
            Log.e("QbSdk", "startMiniQBToLoadUrl  ret = -102");
            return -102;
         }
      }
   }

   public static boolean startQbOrMiniQBToLoadUrl(Context var0, String var1, HashMap<String, String> var2, ValueCallback<String> var3) {
      if (var0 == null) {
         return false;
      } else {
         X5CoreEngine var4 = X5CoreEngine.getInstance();
         var4.init(var0);
         String var7 = "QbSdk.startMiniQBToLoadUrl";
         if (var2 != null && "5".equals(var2.get("PosID")) && var4.isInCharge()) {
            Bundle var8 = null;
            DexLoader var9 = var4.getWVWizardBase().getDexLoader();
            Object var10 = var9.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "getAdWebViewInfoFromX5Core", new Class[0]);
            var8 = (Bundle)var10;
            if (var8 != null) {
            }
         }

         int var11 = MttLoader.loadUrl(var0, var1, var2, var7, (WebView)null);
         if (0 != var11) {
            if (var4.isInCharge()) {
               if (var0 != null && var0.getApplicationInfo().packageName.equals("com.nd.android.pandahome2") && getTbsVersion(var0) < 25487) {
                  return false;
               }

               if (var4.getWVWizardBase().startMiniQB(var0, var1, var2, (String)null, var3) == 0) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      }
   }

   public static boolean startQBWithBrowserlist(Context var0, String var1, int var2) {
      boolean var3 = startQBToLoadurl(var0, var1, var2, (WebView)null);
      if (!var3) {
         openBrowserList(var0, var1, (ValueCallback)null);
      }

      return var3;
   }

   public static int openFileReader(Context var0, String var1, HashMap<String, String> var2, ValueCallback<String> var3) {
      TbsCoreLoadStat.getInstance().a(var0, 505);
      if (!checkContentProviderPrivilage(var0)) {
         return -5;
      } else if (var1 != null) {
         String var4 = var1.substring(var1.lastIndexOf(".") + 1, var1.length());
         if (var4 != null) {
            var4 = var4.toLowerCase();
         }

         if ("apk".equalsIgnoreCase(var4)) {
            Intent var5 = new Intent("android.intent.action.VIEW");
            if (var0 != null && var0.getApplicationInfo().targetSdkVersion >= 24 && android.os.Build.VERSION.SDK_INT >= 24) {
               var5.addFlags(1);
            }

            Uri var6 = FileProvider.a(var0, var1);
            if (var6 == null) {
               var3.onReceiveValue("uri failed");
               return -6;
            } else {
               var5.setDataAndType(var6, "application/vnd.android.package-archive");
               var0.startActivity(var5);
               TbsCoreLoadStat.getInstance().a(var0, 506);
               Log.e("QbSdk", "open openFileReader ret = 4");
               return 4;
            }
         } else {
            if (MttLoader.isBrowserInstalled(var0)) {
               if (!a(var0, var1, var4)) {
                  Log.e("QbSdk", "openFileReader open in QB isQBSupport: false  ret = 3");
                  openFileReaderListWithQBDownload(var0, var1, var3);
                  TbsCoreLoadStat.getInstance().a(var0, 507);
                  return 3;
               }

               if (startQBForDoc(var0, var1, 4, 0, var4, a(var0, (Map)var2))) {
                  if (var3 != null) {
                     var3.onReceiveValue("open QB");
                  }

                  TbsCoreLoadStat.getInstance().a(var0, 507);
                  Log.e("QbSdk", "open openFileReader open QB ret = 1");
                  return 1;
               }

               Log.d("QbSdk", "openFileReader startQBForDoc return false");
            } else {
               Log.d("QbSdk", "openFileReader QQ browser not installed");
            }

            if (var2 == null) {
               var2 = new HashMap();
            }

            var2.put("local", "true");
            TbsLog.setWriteLogJIT(true);
            int var7 = startMiniQBToLoadUrl(var0, var1, var2, var3);
            if (var7 != 0) {
               openFileReaderListWithQBDownload(var0, var1, var3);
               TbsLogReport.getInstance(var0).setLoadErrorCode(511, (String)("" + var7));
               return 3;
            } else {
               TbsCoreLoadStat.getInstance().a(var0, 510);
               return 2;
            }
         }
      } else {
         if (var3 != null) {
            var3.onReceiveValue("filepath error");
         }

         TbsCoreLoadStat.getInstance().a(var0, 509);
         Log.e("QbSdk", "open openFileReader filepath error ret -1");
         return -1;
      }
   }

   public static boolean checkContentProviderPrivilage(Context var0) {
      if (var0 != null && var0.getApplicationInfo().targetSdkVersion >= 24 && android.os.Build.VERSION.SDK_INT >= 24 && !"com.tencent.mobileqq".equals(var0.getApplicationInfo().packageName)) {
         try {
            String var1 = "";
            ComponentName var2 = new ComponentName(var0.getPackageName(), "android.support.v4.content.FileProvider");
            ProviderInfo var3 = var0.getPackageManager().getProviderInfo(var2, 0);
            var1 = var3.authority;
            if (!TextUtils.isEmpty(var1)) {
               return true;
            }
         } catch (Exception var4) {
            var4.printStackTrace();
         }

         ProviderInfo var5 = var0.getPackageManager().resolveContentProvider(var0.getApplicationInfo().packageName + ".provider", 128);
         if (var5 == null) {
            Log.e("QbSdk", "Must declare com.tencent.smtt.utils.FileProvider in AndroidManifest above Android 7.0,please view document in x5.tencent.com");
         }

         return var5 != null;
      } else {
         return true;
      }
   }

   private static boolean a(Context var0, String var1, String var2) {
      return isSuportOpenFile(var2, 2);
   }

   private static Bundle a(Context var0, Map<String, String> var1) {
      try {
         if (var1 == null) {
            return null;
         } else {
            Bundle var2 = new Bundle();
            var2.putString("style", var1.get("style") == null ? "0" : (String)var1.get("style"));

            try {
               int var3 = Color.parseColor((String)var1.get("topBarBgColor"));
               var2.putInt("topBarBgColor", var3);
            } catch (Exception var12) {
            }

            if (var1 != null && var1.containsKey("menuData")) {
               String var14 = (String)var1.get("menuData");
               JSONObject var4 = null;
               var4 = new JSONObject(var14);
               JSONArray var5 = var4.getJSONArray("menuItems");
               if (var5 != null) {
                  ArrayList var6 = new ArrayList();

                  for(int var7 = 0; var7 < var5.length() && var7 < 5; ++var7) {
                     try {
                        JSONObject var8 = (JSONObject)var5.get(var7);
                        int var9 = var8.getInt("iconResId");
                        Bitmap var10 = BitmapFactory.decodeResource(var0.getResources(), var9);
                        var6.add(var7, var10);
                        var8.put("iconResId", var7);
                     } catch (Exception var11) {
                     }
                  }

                  var2.putParcelableArrayList("resArray", var6);
               }

               var2.putString("menuData", var4.toString());
            }

            return var2;
         }
      } catch (Exception var13) {
         var13.printStackTrace();
         return null;
      }
   }

   public static String getMiniQBVersion(Context var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      var1.init(var0);
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().getMiniQBVersion() : null;
   }

   public static boolean createMiniQBShortCut(Context var0, String var1, String var2, Drawable var3) {
      if (var0 == null) {
         return false;
      } else if (TbsDownloader.getOverSea(var0)) {
         return false;
      } else if (isMiniQBShortCutExist(var0, var1, var2)) {
         return false;
      } else {
         X5CoreEngine var4 = X5CoreEngine.getInstance();
         if (null != var4 && var4.isInCharge()) {
            Bitmap var5 = null;
            if (var3 instanceof BitmapDrawable) {
               var5 = ((BitmapDrawable)var3).getBitmap();
            }

            DexLoader var6 = var4.getWVWizardBase().getDexLoader();
            TbsLog.e("QbSdk", "qbsdk createMiniQBShortCut");
            Object var7 = var6.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "createMiniQBShortCut", new Class[]{Context.class, String.class, String.class, Bitmap.class}, var0, var1, var2, var5);
            TbsLog.e("QbSdk", "qbsdk after createMiniQBShortCut ret: " + var7);
            return null != var7;
         } else {
            return false;
         }
      }
   }

   public static boolean isMiniQBShortCutExist(Context var0, String var1, String var2) {
      if (var0 == null) {
         return false;
      } else if (TbsDownloader.getOverSea(var0)) {
         return false;
      } else {
         X5CoreEngine var3 = X5CoreEngine.getInstance();
         if (null != var3 && var3.isInCharge()) {
            DexLoader var4 = var3.getWVWizardBase().getDexLoader();
            Object var5 = var4.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "isMiniQBShortCutExist", new Class[]{Context.class, String.class}, var0, var1);
            if (null != var5) {
               Boolean var6 = false;
               if (var5 instanceof Boolean) {
                  var6 = (Boolean)var5;
               }

               return var6;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public static boolean deleteMiniQBShortCut(Context var0, String var1, String var2) {
      if (var0 == null) {
         return false;
      } else if (TbsDownloader.getOverSea(var0)) {
         return false;
      } else {
         X5CoreEngine var3 = X5CoreEngine.getInstance();
         if (null != var3 && var3.isInCharge()) {
            DexLoader var4 = var3.getWVWizardBase().getDexLoader();
            Object var5 = var4.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "deleteMiniQBShortCut", new Class[]{Context.class, String.class, String.class}, var0, var1, var2);
            return null != var5;
         } else {
            return false;
         }
      }
   }

   public static boolean intentDispatch(WebView var0, Intent var1, String var2, String var3) {
      if (var0 == null) {
         return false;
      } else {
         if (var2.startsWith("mttbrowser://miniqb/ch=icon?")) {
            Context var4 = var0.getContext();
            int var5 = var2.indexOf("url=");
            String var6 = null;
            if (var5 > 0) {
               var6 = var2.substring(var5 + 4);
            }

            HashMap var7 = new HashMap();
            String var8 = "unknown";

            String var9;
            try {
               var9 = var4.getApplicationInfo().packageName;
               var8 = var9;
            } catch (Exception var13) {
               var13.printStackTrace();
            }

            var9 = "14004";
            var7.put("ChannelID", var8);
            var7.put("PosID", var9);
            String var10 = var6;
            if ("miniqb://home".equals(var6)) {
               var10 = "qb://navicard/addCard?cardId=168&cardName=168";
            }

            int var11 = MttLoader.loadUrl(var4, var10, var7, "QbSdk.startMiniQBToLoadUrl", (WebView)null);
            if (0 != var11) {
               X5CoreEngine var12 = X5CoreEngine.getInstance();
               if (null != var12 && var12.isInCharge() && var12.getWVWizardBase().startMiniQB(var4, var6, (Map)null, var3, (android.webkit.ValueCallback)null) == 0) {
                  return true;
               }

               var0.loadUrl(var6);
            }
         } else {
            var0.loadUrl(var2);
         }

         return false;
      }
   }

   public static void openFileReaderListWithQBDownload(Context var0, String var1, ValueCallback<String> var2) {
      openFileReaderListWithQBDownload(var0, var1, (Bundle)null, var2);
   }

   public static void openFileReaderListWithQBDownload(Context var0, String var1, Bundle var2, final ValueCallback<String> var3) {
      if (var0 != null && !var0.getApplicationInfo().packageName.equals("com.tencent.qim") && !var0.getApplicationInfo().packageName.equals("com.tencent.androidqqmail")) {
         String var4 = "";
         if (var2 != null) {
            var4 = var2.getString("ChannelId");
         }

         String var5 = "选择其它应用打开";
         Intent var6 = new Intent("android.intent.action.VIEW");
         var6.addCategory("android.intent.category.DEFAULT");
         String var7 = com.tencent.smtt.sdk.ui.dialog.e.e(var1);
         if (var0 != null && var0.getApplicationInfo().targetSdkVersion >= 24 && android.os.Build.VERSION.SDK_INT >= 24) {
            var6.addFlags(1);
         }

         Uri var8 = FileProvider.a(var0, var1);
         if (var8 == null) {
            TbsLog.i("QbSdk", "openFileReaderListWithQBDownload,uri failed");
            var3.onReceiveValue("uri failed");
         } else {
            TbsLog.i("QbSdk", "openFileReaderListWithQBDownload,fileUri:" + var1 + ",mimeType:" + var7);
            var6.setDataAndType(var8, var7);
            isDefaultDialog = false;
            com.tencent.smtt.sdk.ui.dialog.d var9 = new com.tencent.smtt.sdk.ui.dialog.d(var0, var5, var6, var2, var3, var7, var4);
            String var10 = var9.a();
            TbsLog.i("QbSdk", "openFileReaderListWithQBDownload,defaultBrowser:" + var10);
            if (var10 != null && !TextUtils.isEmpty(var10) && var10.startsWith("extraMenuEvent:")) {
               TbsLog.i("QbSdk", "openFileReaderListWithQBDownload, is default extra menu action");
               var3.onReceiveValue(var10);
            } else {
               if (var10 != null && !TextUtils.isEmpty(var10) && checkApkExist(var0, var10)) {
                  TbsLog.i("QbSdk", "openFileReaderListWithQBDownload, is default normal menu action");
                  if ("com.tencent.mtt".equals(var10)) {
                     var6.putExtra("ChannelID", var0.getApplicationContext().getPackageName());
                     var6.putExtra("PosID", "4");
                  }

                  if (!TextUtils.isEmpty(var4)) {
                     var6.putExtra("big_brother_source_key", var4);
                  }

                  var6.setPackage(var10);
                  var0.startActivity(var6);
                  if (var3 != null) {
                     var3.onReceiveValue("default browser:" + var10);
                  }
               } else {
                  if ("com.tencent.rtxlite".equalsIgnoreCase(var0.getApplicationContext().getPackageName()) && isDefaultDialog) {
                     (new Builder(var0)).setTitle("提示").setMessage("此文件无法打开").setPositiveButton("确定", new OnClickListener() {
                        public void onClick(DialogInterface var1, int var2) {
                        }
                     }).show();
                     return;
                  }

                  if (isDefaultDialog) {
                     TbsLog.i("QbSdk", "isDefaultDialog=true");
                     if (var3 != null) {
                        TbsLog.i("QbSdk", "isDefaultDialog=true, can not open");
                        var3.onReceiveValue("can not open");
                     }
                  } else {
                     try {
                        TbsLog.i("QbSdk", "isDefaultDialog=false,try to open dialog");
                        var9.show();
                        var9.setOnDismissListener(new OnDismissListener() {
                           public void onDismiss(DialogInterface var1) {
                              if (var3 != null) {
                                 var3.onReceiveValue("TbsReaderDialogClosed");
                              }

                           }
                        });
                     } catch (Exception var12) {
                        var12.printStackTrace();
                        TbsLog.i("QbSdk", "isDefaultDialog=false,try to open dialog, but failed");
                        var3.onReceiveValue("TbsReaderDialogClosed");
                     }
                  }

                  TbsLog.i("QbSdk", "unexpected return, dialogBuilder not show!");
               }

            }
         }
      }
   }

   public static boolean checkApkExist(Context var0, String var1) {
      if (var1 != null && !"".equals(var1)) {
         try {
            ApplicationInfo var2 = var0.getPackageManager().getApplicationInfo(var1, 8192);
            return true;
         } catch (NameNotFoundException var3) {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isSuportOpenFile(String var0, int var1) {
      if (TextUtils.isEmpty(var0)) {
         return false;
      } else {
         String[] var2 = new String[]{"rar", "zip", "tar", "bz2", "gz", "7z", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "pdf", "epub", "chm", "html", "htm", "xml", "mht", "url", "ini", "log", "bat", "php", "js", "lrc", "jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", "mp3", "m4a", "aac", "amr", "wav", "ogg", "mid", "ra", "wma", "mpga", "ape", "flac", "RTSP", "RTP", "SDP", "RTMP", "mp4", "flv", "avi", "3gp", "3gpp", "webm", "ts", "ogv", "m3u8", "asf", "wmv", "rmvb", "rm", "f4v", "dat", "mov", "mpg", "mkv", "mpeg", "mpeg1", "mpeg2", "xvid", "dvd", "vcd", "vob", "divx"};
         String[] var3 = new String[]{"doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "pdf", "epub"};
         switch(var1) {
         case 1:
            List var5 = Arrays.asList(var3);
            return var5.contains(var0.toLowerCase());
         case 2:
            List var4 = Arrays.asList(var2);
            return var4.contains(var0.toLowerCase());
         default:
            return false;
         }
      }
   }

   public static void openBrowserList(Context var0, String var1, ValueCallback<String> var2) {
      openBrowserList(var0, var1, (Bundle)null, var2);
   }

   public static void openBrowserList(Context var0, String var1, Bundle var2, final ValueCallback<String> var3) {
      if (var0 != null) {
         String var4 = "";
         if (var2 != null) {
            var4 = var2.getString("ChannelId");
         }

         String var5 = "选择其它应用打开";
         Intent var6 = new Intent("android.intent.action.VIEW");
         var6.addCategory("android.intent.category.DEFAULT");
         var6.setData(Uri.parse(var1));
         String var7 = com.tencent.smtt.sdk.ui.dialog.e.e(var1);
         isDefaultDialog = false;
         com.tencent.smtt.sdk.ui.dialog.d var8 = new com.tencent.smtt.sdk.ui.dialog.d(var0, var5, var6, var2, var3, var7, var4);
         String var9 = var8.a();
         if (var9 != null && !TextUtils.isEmpty(var9)) {
            if ("com.tencent.mtt".equals(var9)) {
               var6.putExtra("ChannelID", var0.getApplicationContext().getPackageName());
               var6.putExtra("PosID", "4");
            }

            var6.setPackage(var9);
            var6.putExtra("big_brother_source_key", var4);
            var0.startActivity(var6);
            if (var3 != null) {
               var3.onReceiveValue("default browser:" + var9);
            }
         } else if (isDefaultDialog) {
            (new Builder(var0)).setTitle("提示").setMessage("此文件无法打开").setPositiveButton("确定", new OnClickListener() {
               public void onClick(DialogInterface var1, int var2) {
               }
            }).show();
            if (var3 != null) {
               var3.onReceiveValue("can not open");
            }
         } else {
            var8.show();
            var8.setOnDismissListener(new OnDismissListener() {
               public void onDismiss(DialogInterface var1) {
                  if (var3 != null) {
                     var3.onReceiveValue("TbsReaderDialogClosed");
                  }

               }
            });
         }

      }
   }

   public static void initTbsSettings(Map<String, Object> var0) {
      if (n == null) {
         n = var0;
      } else {
         try {
            n.putAll(var0);
         } catch (Exception var2) {
            var2.printStackTrace();
         }
      }

   }

   public static Map<String, Object> getSettings() {
      return n;
   }

   static Object a(Context var0, String var1, Bundle var2) {
      if (!a(var0)) {
         return -99999;
      } else {
         Object var3 = ReflectionUtils.invokeInstance(r, "miscCall", new Class[]{String.class, Bundle.class}, var1, var2);
         return var3 != null ? var3 : null;
      }
   }

   static void a(Context var0, Integer var1, Map<Integer, String> var2) {
      if (a(var0)) {
         Class[] var3 = new Class[]{Integer.class, Map.class};
         Object[] var4 = new Object[]{var1, var2};
         ReflectionUtils.invokeInstance(r, "dispatchEmergencyCommand", var3, var4);
      }
   }

   protected static String b() {
      X5CoreEngine var0 = X5CoreEngine.getInstance();
      if (null != var0 && var0.isInCharge()) {
         DexLoader var1 = var0.getWVWizardBase().getDexLoader();
         Object var2 = var1.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "getGUID", new Class[0]);
         if (null != var2 && var2 instanceof String) {
            return (String)var2;
         }
      }

      return null;
   }

   public static void fileInfoDetect(Context var0, String var1, android.webkit.ValueCallback<String> var2) {
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      if (null != var3 && var3.isInCharge()) {
         try {
            DexLoader var4 = var3.getWVWizardBase().getDexLoader();
            var4.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "fileInfoDetect", new Class[]{Context.class, String.class, android.webkit.ValueCallback.class}, var0, var1, var2);
         } catch (Throwable var6) {
            var6.printStackTrace();
         }
      }

   }

   public static void disAllowThirdAppDownload() {
      c = false;
   }

   public static void initBuglyAsync(boolean var0) {
      i = var0;
   }

   public static void setNeedInitX5FirstTime(boolean var0) {
      w = var0;
   }

   public static boolean isNeedInitX5FirstTime() {
      return w;
   }

   public static int getTmpDirTbsVersion(Context var0) {
      if (TbsCoreInstallPropertiesHelper.getInstance(var0).getInstallStatus() == 2) {
         return TbsInstaller.a().getTbsVersion(var0, 0);
      } else {
         return TbsCoreInstallPropertiesHelper.getInstance(var0).getIntProperty_DefNeg1("copy_status") == 1 ? TbsInstaller.a().getTbsVersion(var0, 1) : 0;
      }
   }

   public static File getTbsFolderDir(Context var0) {
      if (var0 == null) {
         return null;
      } else {
         try {
            if (AppUtil.d()) {
               return var0.getDir("tbs_64", 0);
            }
         } catch (Exception var2) {
            var2.printStackTrace();
         }

         return var0.getDir("tbs", 0);
      }
   }

   public static boolean isInDefaultBrowser(Context var0, String var1) {
      SharedPreferences var2 = var0.getSharedPreferences("tbs_file_open_dialog_config", 0);
      String var3 = com.tencent.smtt.sdk.ui.dialog.e.e(var1);
      if (TextUtils.isEmpty(var3)) {
         var3 = "*/*";
      }

      return var2.contains("key_tbs_picked_default_browser_" + var3);
   }

   public static void clearDefaultBrowser(Context var0, String var1) {
      SharedPreferences var2 = var0.getSharedPreferences("tbs_file_open_dialog_config", 0);
      String var3 = com.tencent.smtt.sdk.ui.dialog.e.e(var1);
      if (TextUtils.isEmpty(var3)) {
         var3 = "*/*";
      }

      var2.edit().remove("key_tbs_picked_default_browser_" + var3).commit();
   }

   public static void clearAllDefaultBrowser(Context var0) {
      SharedPreferences var1 = var0.getSharedPreferences("tbs_file_open_dialog_config", 0);
      var1.edit().clear().commit();
   }

   public static int openFileWithQB(Context var0, String var1, String var2) {
      TbsLog.i("QbSdk", "open openFileReader startMiniQBToLoadUrl filepath = " + var1);
      if (!checkContentProviderPrivilage(var0)) {
         return -1;
      } else if (var1 != null) {
         String var3 = var1.substring(var1.lastIndexOf(".") + 1);
         var3 = var3.toLowerCase();
         if (MttLoader.isBrowserInstalled(var0)) {
            if (!a(var0, var1, var3)) {
               TbsLog.i("QbSdk", "openFileReader open in QB isQBSupport: false");
               return -2;
            } else {
               HashMap var4 = new HashMap();
               var4.put("ChannelID", var0.getApplicationContext().getApplicationInfo().processName);
               var4.put("PosID", Integer.toString(4));
               if (MttLoader.openDocWithQb(var0, var1, 0, var3, var2, var4, (Bundle)null)) {
                  return 0;
               } else {
                  TbsLog.i("QbSdk", "openFileReader startQBForDoc return false");
                  return -3;
               }
            }
         } else {
            TbsLog.i("QbSdk", "openFileReader QQ browser not installed");
            return -4;
         }
      } else {
         TbsLog.i("QbSdk", "open openFileReader filepath error");
         return -5;
      }
   }

   public static void openNetLog(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();

         try {
            var2.invokeStaticMethod("com.tencent.smtt.livelog.NetLogManager", "openNetLog", new Class[]{String.class}, var0);
         } catch (Exception var4) {
            return;
         }
      }

   }

   public static String closeNetLogAndSavaToLocal() {
      X5CoreEngine var0 = X5CoreEngine.getInstance();
      if (null != var0 && var0.isInCharge()) {
         DexLoader var1 = var0.getWVWizardBase().getDexLoader();

         try {
            Object var2 = var1.invokeStaticMethod("com.tencent.smtt.livelog.NetLogManager", "closeNetLogAndSavaToLocal", new Class[0]);
            if (null != var2 && var2 instanceof String) {
               return (String)var2;
            }
         } catch (Exception var3) {
         }

         return "";
      } else {
         return "";
      }
   }

   public static void uploadNetLog(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();

         try {
            var2.invokeStaticMethod("com.tencent.smtt.livelog.NetLogManager", "uploadNetLog", new Class[]{String.class}, var0);
         } catch (Exception var4) {
            return;
         }
      }

   }

   public static void setNetLogEncryptionKey(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();

         try {
            var2.invokeStaticMethod("com.tencent.smtt.livelog.NetLogManager", "setNetLogEncryptionKey", new Class[]{String.class}, var0);
         } catch (Exception var4) {
            return;
         }
      }

   }

   public static void setNewDnsHostList(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();

         try {
            var2.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "setNewDnsHostList", new Class[0], var0);
         } catch (Exception var4) {
            return;
         }
      }

   }

   static {
      l = forcedSysByInner;
      mDisableUseHostBackupCore = false;
      B = false;
      C = true;
      D = null;
      E = null;
      F = false;
      G = false;
      m = new TbsListener() {
         public void onDownloadFinish(int var1) {
            if (TbsDownloader.needDownloadDecoupleCore()) {
               TbsLog.i("QbSdk", "onDownloadFinish needDownloadDecoupleCore is true", true);
               TbsDownloader.downloading = true;
            } else {
               TbsLog.i("QbSdk", "onDownloadFinish needDownloadDecoupleCore is false", true);
               TbsDownloader.downloading = false;
               boolean var2 = false;
               if (var1 == 100) {
                  var2 = true;
               }

               if (QbSdk.D != null) {
                  QbSdk.D.onDownloadFinish(var1);
               }

               if (QbSdk.E != null) {
                  QbSdk.E.onDownloadFinish(var1);
               }
            }

         }

         public void onInstallFinish(int var1) {
            boolean var2 = false;
            if (var1 == 200 || var1 == 220) {
               var2 = true;
            }

            QbSdk.setTBSInstallingStatus(false);
            TbsDownloader.downloading = false;
            if (TbsDownloader.startDecoupleCoreIfNeeded()) {
               TbsDownloader.downloading = true;
            } else {
               TbsDownloader.downloading = false;
            }

            if (QbSdk.D != null) {
               QbSdk.D.onInstallFinish(var1);
            }

            if (QbSdk.E != null) {
               QbSdk.E.onInstallFinish(var1);
            }

         }

         public void onDownloadProgress(int var1) {
            if (QbSdk.E != null) {
               QbSdk.E.onDownloadProgress(var1);
            }

            if (QbSdk.D != null) {
               QbSdk.D.onDownloadProgress(var1);
            }

         }
      };
      n = null;
   }

   public interface PreInitCallback {
      void onCoreInitFinished();

      void onViewInitFinished(boolean var1);
   }
}
