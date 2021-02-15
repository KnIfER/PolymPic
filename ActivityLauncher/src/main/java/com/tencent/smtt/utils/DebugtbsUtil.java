package com.tencent.smtt.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import com.tencent.smtt.sdk.WebView;
import dalvik.system.DexClassLoader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class DebugtbsUtil {
   public String a = "";
   private static DexClassLoader b = null;
   private static Looper c = null;
   private static DebugtbsUtil instance = null;

   private DebugtbsUtil(Context var1) {
      this.a = var1.getDir("debugtbs", 0).getAbsolutePath() + File.separator + "plugin";
   }

   public static DebugtbsUtil getInstance(Context var0) {
      if (instance == null) {
         instance = new DebugtbsUtil(var0);
      }

      return instance;
   }

   public void a(final String var1, final WebView var2, final Context var3) {
      final RelativeLayout var4 = new RelativeLayout(var3);
      final TextView var5 = new TextView(var3);
      String var6 = "加载中，请稍后...";
      LayoutParams var7 = new LayoutParams(-2, -2);
      var7.addRule(13);
      var5.setText(var6);
      var4.addView(var5, var7);
      var2.addView(var4, new android.widget.FrameLayout.LayoutParams(-1, -1));
      String var8 = this.a + File.separator + "DebugPlugin.tbs";
      File var9 = new File(var8);
      FileHelper.delete(var9);
      a(var8, new DebugtbsUtil.a() {
         public void a() {
            var2.post(new Runnable() {
               public void run() {
                  Toast.makeText(var3, "下载成功", 0).show();
                  var4.setVisibility(4);
                  DebugtbsUtil.this.showPluginView(var1, var2, var3, DebugtbsUtil.c);
               }
            });
         }

         public void a(final int var1x) {
            var2.post(new Runnable() {
               public void run() {
                  var5.setText("已下载" + var1x + "%");
               }
            });
         }

         public void a(Throwable var1x) {
            var2.post(new Runnable() {
               public void run() {
                  Toast.makeText(var3, "下载失败，请检查网络", 0).show();
               }
            });
         }
      });
   }

   @SuppressLint({"NewApi"})
   public static void a(final String var0, final DebugtbsUtil.a var1) {
      (new Thread() {
         public void run() {
            InputStream var1x = null;
            FileOutputStream var2 = null;

            try {
               boolean var3 = false;
               boolean var4 = false;
               int var5 = 0;
               URL var6 = new URL("https://soft.tbs.imtt.qq.com/17421/tbs_res_imtt_tbs_DebugPlugin_DebugPlugin.tbs");
               HttpURLConnection var7 = (HttpURLConnection)var6.openConnection();
               int var25 = var7.getContentLength();
               var7.setConnectTimeout(5000);
               var7.connect();
               var1x = var7.getInputStream();
               File var8 = new File(var0);
               var2 = FileHelper.getOutputStream(var8);
               byte[] var9 = new byte[8192];
               boolean var10 = true;

               int var27;
               while((var27 = var1x.read(var9)) > 0) {
                  var5 += var27;
                  var2.write(var9, 0, var27);
                  int var26 = var5 * 100 / var25;
                  var1.a(var26);
               }

               var1.a();
            } catch (Exception var23) {
               var23.printStackTrace();
               var1.a(var23);
            } finally {
               try {
                  var1x.close();
               } catch (Exception var22) {
                  var22.printStackTrace();
               }

               try {
                  var2.close();
               } catch (Exception var21) {
                  var21.printStackTrace();
               }

            }

         }
      }).start();
   }

   @SuppressLint({"NewApi"})
   public void showPluginView(String var1, WebView var2, Context var3, Looper var4) {
      TbsLog.i("debugtbs", "showPluginView -- url: " + var1 + "; webview: " + var2 + "; context: " + var3);
      String var5 = this.a + File.separator + "DebugPlugin.tbs";
      String var6 = this.a + File.separator + "DebugPlugin.apk";
      File var7 = new File(var5);
      File var8 = new File(var6);
      c = var4;
      if (var7.exists()) {
         var8.delete();
         var7.renameTo(var8);
      }

      if (!var8.exists()) {
         TbsLog.i("debugtbs", "showPluginView - going to download plugin...");
         this.a(var1, var2, var3);
      } else {
         try {
            String var9 = "";
            var9 = AppUtil.a(var3, true, new File(var6));
            if (!"308203773082025fa003020102020448bb959d300d06092a864886f70d01010b0500306b310b300906035504061302636e31123010060355040813094775616e67646f6e673111300f060355040713085368656e7a68656e3110300e060355040a130754656e63656e74310c300a060355040b13034d4947311530130603550403130c4d696e676875204875616e673020170d3136303532313039353730335a180f32303731303232323039353730335a306b310b300906035504061302636e31123010060355040813094775616e67646f6e673111300f060355040713085368656e7a68656e3110300e060355040a130754656e63656e74310c300a060355040b13034d4947311530130603550403130c4d696e676875204875616e6730820122300d06092a864886f70d01010105000382010f003082010a02820101008c58deabefe95f699c6322f9a75620873b490d26520c7267eb8382a91da625a5876b2bd617116eb40b371c4f88c988c1ba73052caaa9964873c94b7755c3429fca47a6677229fb2e72908d3b17df82f1ebe70447b94c1e5b0a763dad8948312180322657325306f01e423e0409ef3a59e5c0e0b9c765a2322699a2dec2d4dbe58ec15f41752516192169d9596169f5bf08eaf8aab9893240ad679e82fc92b97d2ae98b28021dc5a752f0a69437ea603c541e6753cea52dbc8e8043fe21fd5da46066c92e0714905dfad3116f35aca52b13871c57481459aa4ca255a6482ba972bd17af90d0d2c21a57ef65376bbd4ce7078e6047060640669f3867fdc69fbb750203010001a321301f301d0603551d0e0416041450fb9b6362e829797b1b29ca55e6d5e082e93ff3300d06092a864886f70d01010b050003820101004952ffbfba7c00ee9b84f44b05ec62bc2400dc769fb2e83f80395e3fbb54e44d56e16527413d144f42bf8f21fa443bc42a7a732de9d5124df906c6d728e75ca94eefc918080876bd3ce6cb5f7f2d9cc8d8e708033afc1295c7f347fb2d2098be2e4a79220e9552171d5b5f8f59cff4c6478cc41dce24cbe942305757488d37659d3265838ee54ebe44fccbd1bec93d809f950034f5ef292f20179554d22f5856c03b4d44997fcb9b3579e16a49218fce0e2e6bfe1fd4aa0ab39f548344c244c171c203baff1a730883aaf4506b6865a45c3c9aba40c6326d4152b6ce09cc058864bec1d6422e83dad9496b83fb252b4bfb30d3a6badf996099793e11f9af618d".equals(var9)) {
               TbsLog.e("debugtbs", "verifyPlugin apk: " + var6 + " signature failed - sig: " + var9);
               Toast.makeText(var3, "插件校验失败，请重试", 0).show();
               var7.delete();
               var8.delete();
               return;
            }

            String var10 = this.a + File.separator + "opt";
            File var11 = new File(var10);
            if (!var11.exists()) {
               var11.mkdirs();
            }

            if (b == null) {
               b = new DexClassLoader(var6, var10, (String)null, var3.getClassLoader());
            }

            HashMap var12 = new HashMap();
            var12.put("url", var1);
            var12.put("tbs_version", "" + WebView.getTbsSDKVersion(var3));
            var12.put("tbs_core_version", "" + WebView.getTbsCoreVersion(var3));
            if (c != null) {
               var12.put("looper", var4);
            }

            Class var13 = b.loadClass("com.tencent.tbs.debug.plugin.DebugView");
            Object var14 = var13.getConstructor(Context.class, Map.class).newInstance(var3, var12);
            if (var14 instanceof FrameLayout) {
               FrameLayout var15 = (FrameLayout)var14;
               android.widget.FrameLayout.LayoutParams var16 = new android.widget.FrameLayout.LayoutParams(-1, -1);
               var2.addView(var15, var16);
               TbsLog.i("debugtbs", "show " + var15 + " successful in " + var2);
            } else {
               TbsLog.e("debugtbs", "get debugview failure: " + var14);
            }
         } catch (Exception var17) {
            FileHelper.delete(var8);
            var17.printStackTrace();
         }

      }
   }

   public interface a {
      void a();

      void a(Throwable var1);

      void a(int var1);
   }
}
