package com.tencent.smtt.sdk.stat;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.text.TextUtils;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.utils.FileProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MttLoader {
   public static final String QQBROWSER_DOWNLOAD_URL = "https://mdc.html5.qq.com/mh?channel_id=50079&u=";
   public static final int RESULT_OK = 0;
   public static final int RESULT_UNKNOWN = 1;
   public static final int RESULT_INVALID_URL = 2;
   public static final int RESULT_INVALID_CONTEXT = 3;
   public static final int RESULT_NOT_INSTALL_QQBROWSER = 4;
   public static final int RESULT_QQBROWSER_LOW = 5;
   public static final String MTT_ACTION = "com.tencent.QQBrowser.action.VIEW";
   public static final String MTT_ACTION_SP = "com.tencent.QQBrowser.action.VIEWSP";
   public static final String PID_MOBILE_QQ = "50079";
   public static final String PID_QQPIM = "50190";
   public static final String PID_ARTICLE_NEWS = "21272";
   public static final String PID_WECHAT = "10318";
   public static final String PID_QZONE = "10494";
   public static final String QQBROWSER_SCHEME = "mttbrowser://url=";
   public static final String QQBROWSER_PARAMS_PD = ",product=";
   public static final String QQBROWSER_PARAMS_VERSION = ",version=";
   public static final String QQBROWSER_PARAMS_PACKAGENAME = ",packagename=";
   public static final String QQBROWSER_PARAMS_FROME = ",from=";
   public static final String QQBROWSER_DIRECT_DOWNLOAD_URL = "https://mdc.html5.qq.com/d/directdown.jsp?channel_id=50079";
   /** @deprecated */
   @Deprecated
   public static final String KEY_APP_NAME = "KEY_APPNAME";
   /** @deprecated */
   @Deprecated
   public static final String KEY_PACKAGE = "KEY_PKG";
   /** @deprecated */
   @Deprecated
   public static final String KEY_ACTIVITY_NAME = "KEY_ACT";
   public static final String KEY_PID = "KEY_PID";
   public static final String KEY_EUSESTAT = "KEY_EUSESTAT";
   public static final String CHANNEL_ID = "ChannelID";
   public static final String POS_ID = "PosID";
   public static final String STAT_KEY = "StatKey";
   public static final String ENTRY_ID = "entryId";

   public static String getValidQBUrl(Context var0, String var1) {
      String var2 = var1.toLowerCase();
      if (var2.startsWith("qb://")) {
         boolean var3 = false;
         MttLoader.BrowserInfo var4 = getBrowserInfo(var0);
         if (var4.browserType == -1) {
            var3 = true;
         } else if (var4.browserType == 2 && var4.ver < 33) {
            var3 = true;
         }

         if (var3) {
            return getDownloadUrlWithQb(var1);
         }
      }

      return var1;
   }

   public static String getDownloadUrlWithQb(String var0) {
      try {
         return "https://mdc.html5.qq.com/mh?channel_id=50079&u=" + URLEncoder.encode(var0, "UTF-8");
      } catch (UnsupportedEncodingException var2) {
         var2.printStackTrace();
         return "https://mdc.html5.qq.com/mh?channel_id=50079&u=";
      }
   }

   public static boolean isSupportQBScheme(Context var0) {
      MttLoader.BrowserInfo var1 = getBrowserInfo(var0);
      if (var1.browserType == -1) {
         return false;
      } else {
         return var1.browserType != 2 || var1.ver >= 42;
      }
   }

   public static boolean openDocWithQb(Context var0, String var1, int var2, String var3, HashMap<String, String> var4) {
      return openDocWithQb(var0, var1, var2, var3, var4, (Bundle)null);
   }

   public static boolean openDocWithQb(Context var0, String var1, int var2, String var3, HashMap<String, String> var4, Bundle var5) {
      return openDocWithQb(var0, var1, var2, var3, "", var4, (Bundle)null);
   }

   public static boolean openDocWithQb(Context var0, String var1, int var2, String var3, String var4, HashMap<String, String> var5, Bundle var6) {
      try {
         Intent var7 = new Intent("com.tencent.QQBrowser.action.sdk.document");
         if (var5 != null) {
            Set var8 = var5.keySet();
            if (var8 != null) {
               Iterator var9 = var8.iterator();

               while(var9.hasNext()) {
                  String var10 = (String)var9.next();
                  String var11 = (String)var5.get(var10);
                  if (!TextUtils.isEmpty(var11)) {
                     var7.putExtra(var10, var11);
                  }
               }
            }
         }

         new File(var1);
         var7.putExtra("key_reader_sdk_id", 3);
         var7.putExtra("key_reader_sdk_type", var2);
         if (!TextUtils.isEmpty(var4)) {
            var7.putExtra("big_brother_source_key", var4);
         }

         if (var2 == 0) {
            var7.putExtra("key_reader_sdk_path", var1);
         } else if (var2 == 1) {
            var7.putExtra("key_reader_sdk_url", var1);
         }

         var7.putExtra("key_reader_sdk_format", var3);
         if (var0 != null && var0.getApplicationInfo().targetSdkVersion >= 24 && VERSION.SDK_INT >= 24) {
            var7.addFlags(1);
         }

         var7.addFlags(268435456);
         Uri var13 = a(var0, var1);
         if (var13 == null) {
            return false;
         } else {
            var7.setDataAndType(var13, "mtt/" + var3);
            var7.putExtra("loginType", a(var0.getApplicationContext()));
            if (var6 != null) {
               var7.putExtra("key_reader_sdk_extrals", var6);
            }

            var0.startActivity(var7);
            return true;
         }
      } catch (Exception var12) {
         var12.printStackTrace();
         return false;
      }
   }

   private static Uri a(Context var0, String var1) {
      return FileProvider.a(var0, var1);
   }

   public static boolean openVideoWithQb(Context var0, String var1, HashMap<String, String> var2) {
      Uri var3 = Uri.parse(var1);
      Intent var4 = new Intent("android.intent.action.VIEW");
      var4.setFlags(268435456);
      var4.setDataAndType(var3, "video/*");
      if (var2 != null) {
         Set var5 = var2.keySet();
         if (var5 != null) {
            Iterator var6 = var5.iterator();

            while(var6.hasNext()) {
               String var7 = (String)var6.next();
               String var8 = (String)var2.get(var7);
               if (!TextUtils.isEmpty(var8)) {
                  var4.putExtra(var7, var8);
               }
            }
         }
      }

      boolean var11 = false;

      try {
         var4.putExtra("loginType", a(var0));
         var4.setComponent(new ComponentName("com.tencent.mtt", "com.tencent.mtt.browser.video.H5VideoThrdcallActivity"));
         var0.startActivity(var4);
         var11 = true;
      } catch (Throwable var10) {
      }

      if (!var11) {
         try {
            var4.setComponent((ComponentName)null);
            var0.startActivity(var4);
         } catch (Throwable var9) {
            var9.printStackTrace();
            return false;
         }
      }

      return true;
   }

   public static int loadUrl(Context var0, String var1, HashMap<String, String> var2, String var3, WebView var4) {
      StringBuilder var5 = new StringBuilder();
      boolean var6 = false;

      try {
         PackageInfo var7 = null;
         PackageManager var8 = var0.getPackageManager();
         if (var8 != null) {
            var7 = var8.getPackageInfo("com.tencent.mtt", 0);
            if (var7 != null && var7.versionCode > 601000) {
               var6 = true;
            }
         }
      } catch (Throwable var10) {
      }

      String var11;
      try {
         var11 = URLEncoder.encode(var1, "UTF-8");
         var1 = var6 ? var11 : var1;
      } catch (Exception var9) {
         var6 = false;
      }

      var11 = var6 ? ",encoded=1" : "";
      var5.append("mttbrowser://url=").append(var1).append(",product=").append("TBS").append(",packagename=").append(var0.getPackageName()).append(",from=").append(var3).append(",version=").append("4.3.0.67").append(var11);
      return loadUrl(var0, var5.toString(), var2, var4);
   }

   public static int loadUrl(Context var0, String var1, HashMap<String, String> var2, WebView var3) {
      if (var0 == null) {
         return 3;
      } else {
         if (!a(var1)) {
            var1 = "http://" + var1;
         }

         Uri var4 = null;

         try {
            var4 = Uri.parse(var1);
            if (var4 == null) {
               return 2;
            }
         } catch (Exception var12) {
            return 2;
         }

         MttLoader.BrowserInfo var5 = getBrowserInfo(var0);
         if (var5.browserType == -1) {
            return 4;
         } else if (var5.browserType == 2 && var5.ver < 33) {
            return 5;
         } else {
            Intent var6 = new Intent("android.intent.action.VIEW");
            MttLoader.a var7;
            if (var5.browserType == 2) {
               if (var5.ver >= 33 && var5.ver <= 39) {
                  var6.setClassName("com.tencent.mtt", "com.tencent.mtt.MainActivity");
               } else if (var5.ver >= 40 && var5.ver <= 45) {
                  var6.setClassName("com.tencent.mtt", "com.tencent.mtt.SplashActivity");
               } else if (var5.ver >= 46) {
                  var6 = new Intent("com.tencent.QQBrowser.action.VIEW");
                  var7 = a(var0, var4);
                  if (var7 != null && !TextUtils.isEmpty(var7.a)) {
                     var6.setClassName(var7.b, var7.a);
                  }
               }
            } else if (var5.browserType == 1) {
               if (var5.ver == 1) {
                  var6.setClassName("com.tencent.qbx5", "com.tencent.qbx5.MainActivity");
               } else if (var5.ver == 2) {
                  var6.setClassName("com.tencent.qbx5", "com.tencent.qbx5.SplashActivity");
               }
            } else if (var5.browserType == 0) {
               if (var5.ver >= 4 && var5.ver <= 6) {
                  var6.setClassName("com.tencent.qbx", "com.tencent.qbx.SplashActivity");
               } else if (var5.ver > 6) {
                  var6 = new Intent("com.tencent.QQBrowser.action.VIEW");
                  var7 = a(var0, var4);
                  if (var7 != null && !TextUtils.isEmpty(var7.a)) {
                     var6.setClassName(var7.b, var7.a);
                  }
               }
            } else {
               var6 = new Intent("com.tencent.QQBrowser.action.VIEW");
               var7 = a(var0, var4);
               if (var7 != null && !TextUtils.isEmpty(var7.a)) {
                  var6.setClassName(var7.b, var7.a);
               }
            }

            var6.setData(var4);
            if (var2 != null) {
               Set var13 = var2.keySet();
               if (var13 != null) {
                  Iterator var8 = var13.iterator();

                  while(var8.hasNext()) {
                     String var9 = (String)var8.next();
                     String var10 = (String)var2.get(var9);
                     if (!TextUtils.isEmpty(var10)) {
                        var6.putExtra(var9, var10);
                     }
                  }
               }
            }

            try {
               var6.putExtra("loginType", a(var0));
               var6.addFlags(268435456);
               if (var3 != null) {
                  Point var14 = new Point(var3.getScrollX(), var3.getScrollY());
                  var6.putExtra("AnchorPoint", var14);
                  Point var15 = new Point(var3.getContentWidth(), var3.getContentHeight());
                  var6.putExtra("ContentSize", var15);
               }

               var0.startActivity(var6);
               return 0;
            } catch (ActivityNotFoundException var11) {
               return 4;
            }
         }
      }
   }

   private static int a(Context var0) {
      byte var1 = 26;
      String var2 = var0.getApplicationInfo().processName;
      if (var2.equals("com.tencent.mobileqq")) {
         var1 = 13;
      } else if (var2.equals("com.qzone")) {
         var1 = 14;
      } else if (var2.equals("com.tencent.WBlog")) {
         var1 = 15;
      } else if (var2.equals("com.tencent.mm")) {
         var1 = 24;
      }

      return var1;
   }

   private static MttLoader.a a(Context var0, Uri var1) {
      Intent var2 = new Intent("com.tencent.QQBrowser.action.VIEW");
      var2.setData(var1);
      List var3 = var0.getPackageManager().queryIntentActivities(var2, 0);
      if (var3.size() <= 0) {
         return null;
      } else {
         MttLoader.a var4 = new MttLoader.a();
         Iterator var5 = var3.iterator();

         while(var5.hasNext()) {
            ResolveInfo var6 = (ResolveInfo)var5.next();
            String var7 = var6.activityInfo.packageName;
            if (var7.contains("com.tencent.mtt")) {
               var4.a = var6.activityInfo.name;
               var4.b = var6.activityInfo.packageName;
               return var4;
            }

            if (var7.contains("com.tencent.qbx")) {
               var4.a = var6.activityInfo.name;
               var4.b = var6.activityInfo.packageName;
            }
         }

         return var4;
      }
   }

   public static MttLoader.BrowserInfo getBrowserInfo(Context var0) {
      SharedPreferences var4 = var0.getApplicationContext().getSharedPreferences("x5_proxy_setting", 0);
      boolean var5 = var4.getBoolean("qb_install_status", false);
      MttLoader.BrowserInfo var6 = new MttLoader.BrowserInfo();
      if (var5) {
         return var6;
      } else {
         try {
            PackageManager var7 = var0.getPackageManager();
            PackageInfo var8 = null;

            try {
               var8 = var7.getPackageInfo("com.tencent.mtt", 0);
               var6.browserType = 2;
               var6.packageName = "com.tencent.mtt";
               var6.quahead = "ADRQB_";
               if (var8 != null && var8.versionCode > 420000) {
                  var6.ver = var8.versionCode;
                  var6.quahead = var6.quahead + var8.versionName.replaceAll("\\.", "");
                  var6.vn = var8.versionName.replaceAll("\\.", "");
                  return var6;
               }
            } catch (NameNotFoundException var19) {
            }

            try {
               var8 = var7.getPackageInfo("com.tencent.qbx", 0);
               var6.browserType = 0;
               var6.packageName = "com.tencent.qbx";
               var6.quahead = "ADRQBX_";
            } catch (NameNotFoundException var18) {
               try {
                  var8 = var7.getPackageInfo("com.tencent.qbx5", 0);
                  var6.browserType = 1;
                  var6.packageName = "com.tencent.qbx5";
                  var6.quahead = "ADRQBX5_";
               } catch (NameNotFoundException var17) {
                  try {
                     var8 = var7.getPackageInfo("com.tencent.mtt", 0);
                     var6.packageName = "com.tencent.mtt";
                     var6.browserType = 2;
                     var6.quahead = "ADRQB_";
                  } catch (NameNotFoundException var16) {
                     try {
                        var8 = var7.getPackageInfo("com.tencent.mtt.x86", 0);
                        var6.packageName = "com.tencent.mtt.x86";
                        var6.browserType = 2;
                        var6.quahead = "ADRQB_";
                     } catch (Exception var15) {
                        try {
                           MttLoader.a var13 = a(var0, Uri.parse("https://mdc.html5.qq.com/mh?channel_id=50079&u="));
                           if (var13 != null && !TextUtils.isEmpty(var13.b)) {
                              var8 = var7.getPackageInfo(var13.b, 0);
                              var6.packageName = var13.b;
                              var6.browserType = 2;
                              var6.quahead = "ADRQB_";
                           }
                        } catch (Exception var14) {
                        }
                     }
                  }
               }
            }

            if (var8 != null) {
               var6.ver = var8.versionCode;
               var6.quahead = var6.quahead + var8.versionName.replaceAll("\\.", "");
               var6.vn = var8.versionName.replaceAll("\\.", "");
            }
         } catch (Exception var20) {
         }

         return var6;
      }
   }

   private static boolean a(String var0) {
      if (var0 != null && var0.length() != 0) {
         String var1 = var0.trim();
         int var2 = var1.toLowerCase().indexOf("://");
         int var3 = var1.toLowerCase().indexOf(46);
         return var2 > 0 && var3 > 0 && var2 > var3 ? false : var1.toLowerCase().contains("://");
      } else {
         return false;
      }
   }

   public static boolean isBrowserInstalled(Context var0) {
      MttLoader.BrowserInfo var1 = getBrowserInfo(var0);
      return var1.browserType != -1;
   }

   public static boolean isBrowserInstalledEx(Context var0) {
      MttLoader.BrowserInfo var1 = getBrowserInfo(var0);
      boolean var2 = false;

      try {
         long var3 = Long.valueOf(var1.vn);
         if (var3 >= 6001500L) {
            var2 = true;
         }
      } catch (NumberFormatException var5) {
         var5.printStackTrace();
      }

      if (var1.ver >= 601500) {
         var2 = true;
      }

      return var2;
   }

   public static boolean isSupportingTbsTips(Context var0) {
      MttLoader.BrowserInfo var1 = getBrowserInfo(var0);
      return var1.browserType == 2 && var1.ver >= 580000;
   }

   public static boolean verifySignature(File var0) {
      JarFile var1 = null;
      InputStream var2 = null;

      boolean var6;
      try {
         var1 = new JarFile(var0);
         JarEntry var3 = var1.getJarEntry("AndroidManifest.xml");
         if (var3 == null) {
            boolean var29 = false;
            return var29;
         }

         byte[] var4 = new byte[8192];
         var2 = var1.getInputStream(var3);

         while(var2.read(var4, 0, var4.length) != -1) {
         }

         var2.close();
         Certificate[] var5 = var3.getCertificates();
         if (var5.length >= 1) {
            String var30 = a(var5[0]);
            if (var30 == null || !var30.equals("3082023f308201a8a00302010202044c46914a300d06092a864886f70d01010505003064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f301e170d3130303732313036313835305a170d3430303731333036313835305a3064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f30819f300d06092a864886f70d010101050003818d0030818902818100c209077044bd0d63ea00ede5b839914cabcc912a87f0f8b390877e0f7a2583f0d5933443c40431c35a4433bc4c965800141961adc44c9625b1d321385221fd097e5bdc2f44a1840d643ab59dc070cf6c4b4b4d98bed5cbb8046e0a7078ae134da107cdf2bfc9b440fe5cb2f7549b44b73202cc6f7c2c55b8cfb0d333a021f01f0203010001300d06092a864886f70d010105050003818100b007db9922774ef4ccfee81ba514a8d57c410257e7a2eba64bfa17c9e690da08106d32f637ac41fbc9f205176c71bde238c872c3ee2f8313502bee44c80288ea4ef377a6f2cdfe4d3653c145c4acfedbfbadea23b559d41980cc3cdd35d79a68240693739aabf5c5ed26148756cf88264226de394c8a24ac35b712b120d4d23a")) {
               return false;
            }

            boolean var7 = true;
            return var7;
         }

         var6 = false;
      } catch (Throwable var27) {
         return false;
      } finally {
         try {
            if (var2 != null) {
               var2.close();
            }
         } catch (IOException var26) {
         }

         try {
            if (var1 != null) {
               var1.close();
            }
         } catch (IOException var25) {
         }

      }

      return var6;
   }

   public static boolean isGreatBrowserVer(Context var0, long var1, long var3) {
      MttLoader.BrowserInfo var5 = getBrowserInfo(var0);
      boolean var6 = false;

      try {
         long var7 = Long.valueOf(var5.vn);
         if (var7 >= var1) {
            var6 = true;
         }
      } catch (NumberFormatException var9) {
         var9.printStackTrace();
      }

      if ((long)var5.ver >= var3) {
         var6 = true;
      }

      return var6;
   }

   private static String a(Certificate var0) throws CertificateEncodingException {
      byte[] var1 = var0.getEncoded();
      int var2 = var1.length;
      int var3 = var2 * 2;
      char[] var4 = new char[var3];

      for(int var5 = 0; var5 < var2; ++var5) {
         byte var6 = var1[var5];
         int var7 = var6 >> 4 & 15;
         var4[var5 * 2] = (char)(var7 >= 10 ? 97 + var7 - 10 : 48 + var7);
         var7 = var6 & 15;
         var4[var5 * 2 + 1] = (char)(var7 >= 10 ? 97 + var7 - 10 : 48 + var7);
      }

      return new String(var4);
   }

   private static class a {
      public String a;
      public String b;

      private a() {
         this.a = "";
         this.b = "";
      }

      // $FF: synthetic method
      a(Object var1) {
         this();
      }
   }

   public static class BrowserInfo {
      public int browserType = -1;
      public int ver = -1;
      public String quahead = "";
      public String vn = "0";
      public String packageName = null;
   }
}
