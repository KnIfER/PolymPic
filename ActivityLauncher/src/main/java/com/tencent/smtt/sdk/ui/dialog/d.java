package com.tencent.smtt.sdk.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.MeasureSpec;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.stat.MttLoader;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.TbsLog;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class d extends Dialog {
   private ListView g;
   private Button h;
   private Button i;
   static WeakReference<ValueCallback<String>> a;
   protected List<b> b;
   private final String j = "TBSActivityPicker";
   protected final String c = "extraMenu";
   protected final String d = "name";
   protected final String e = "resource_id";
   protected final String f = "value";
   private String k;
   private a l;
   private String m = "*/*";
   private String n;
   private Intent o;
   private SharedPreferences p = null;
   private int q = 0;
   private int r = 0;
   private FrameLayout s;
   private LinearLayout t;

   public d(Context var1, String var2, Intent var3, Bundle var4, ValueCallback<String> var5, String var6, String var7) {
      super(var1, 16973835);
      this.n = var7;
      PackageManager var8 = var1.getPackageManager();
      List var9 = var8.queryIntentActivities(var3, 65536);
      TbsLog.i("TBSActivityPicker", "acts.size(): " + var9.size());
      Bundle var10 = var4 != null ? var4.getBundle("extraMenu") : null;
      if (var10 != null) {
         this.b = new ArrayList();
         Iterator var11 = var10.keySet().iterator();

         while(var11.hasNext()) {
            String var12 = (String)var11.next();
            Bundle var13 = var10.getBundle(var12);
            if (var13 != null) {
               String var14 = var13.getString("name", (String)null);
               int var15 = var13.getInt("resource_id", -1);
               String var16 = var13.getString("value", (String)null);
               if (var14 != null && var15 != -1 && var16 != null) {
                  Context var17 = this.getContext();
                  this.b.add(new b(var17, var15, var14, var16));
               }
            }
         }
      } else {
         TbsLog.i("TBSActivityPicker", "no extra menu info in bundle");
      }

      if ((var9 == null || var9.size() == 0) && (this.b == null || this.b.isEmpty()) && MttLoader.isBrowserInstalled(var1)) {
         TbsLog.i("TBSActivityPicker", "no action has been found with Intent:" + var3.toString());
         QbSdk.isDefaultDialog = true;
      }

      if ("com.tencent.rtxlite".equalsIgnoreCase(var1.getApplicationContext().getPackageName()) && (var9 == null || var9.size() == 0) && (this.b == null || this.b.isEmpty())) {
         TbsLog.i("TBSActivityPicker", "package name equal to `com.tencent.rtxlite` but no action has been found with Intent:" + var3.toString());
         QbSdk.isDefaultDialog = true;
      }

      this.k = var2;
      this.o = var3;
      a = new WeakReference(var5);
      this.p = var1.getSharedPreferences("tbs_file_open_dialog_config", 0);
      if (!TextUtils.isEmpty(var6)) {
         this.m = var6;
      }

      TbsLog.i("TBSActivityPicker", "Intent:" + this.m + " MineType:" + this.m);
   }

   public void a(String var1) {
      TbsLog.i("TBSActivityPicker", "setTBSPickedDefaultBrowser:" + var1);
      if (this.p != null) {
         if (TextUtils.isEmpty(var1)) {
            TbsLog.i("TBSActivityPicker", "paramString empty, remove: key_tbs_picked_default_browser_" + this.m);
            this.p.edit().remove("key_tbs_picked_default_browser_" + this.m).commit();
         } else {
            TbsLog.i("TBSActivityPicker", "paramString not empty, set: key_tbs_picked_default_browser_" + this.m + "=" + var1);
            this.p.edit().putString("key_tbs_picked_default_browser_" + this.m, var1).commit();
         }
      }

   }

   public String a() {
      if (this.p != null) {
         TbsLog.i("TBSActivityPicker", "getTBSPickedDefaultBrowser: " + this.p.getString("key_tbs_picked_default_browser_" + this.m, (String)null));
         return this.p.getString("key_tbs_picked_default_browser_" + this.m, (String)null);
      } else {
         return null;
      }
   }

   public void b() {
      Window var1 = this.getWindow();
      if (var1 != null) {
         var1.setBackgroundDrawable(new ColorDrawable(0));
         var1.setGravity(80);
         var1.setLayout(-1, -2);
         var1.getDecorView().setPadding(0, 0, 0, 0);
         LayoutParams var2 = var1.getAttributes();
         var2.width = -1;
         var2.horizontalMargin = 0.0F;
         var2.dimAmount = 0.5F;
         var1.setAttributes(var2);
      }

      this.setContentView(this.a(this.getContext()));
      this.d();
      this.h.setOnClickListener(new View.OnClickListener() {
         public void onClick(View var1) {
            b var2 = d.this.l.a();
            ResolveInfo var3 = d.this.l.a(var2);
            d.this.b("userClickAlwaysEvent:");
            if (var2 != null) {
               if (!var2.e()) {
                  if (var3 == null) {
                     d.this.a(var2);
                     d.this.dismiss();
                     return;
                  }

                  Intent var4 = d.this.o;
                  Context var5 = d.this.getContext();
                  String var6 = var3.activityInfo.packageName;
                  var4.setPackage(var6);
                  if ("com.tencent.mtt".equals(var6)) {
                     var4.putExtra("ChannelID", var5.getApplicationContext().getPackageName());
                     var4.putExtra("PosID", "4");
                  }

                  if (var5 != null && var5.getApplicationInfo().targetSdkVersion >= 24 && VERSION.SDK_INT >= 24) {
                     var4.addFlags(1);
                  }

                  if (!TextUtils.isEmpty(d.this.n)) {
                     var4.putExtra("big_brother_source_key", d.this.n);
                  }

                  try {
                     var5.startActivity(var4);
                  } catch (Exception var8) {
                     var8.printStackTrace();
                  }

                  if (com.tencent.smtt.sdk.ui.dialog.d.a.get() != null) {
                     ((ValueCallback)com.tencent.smtt.sdk.ui.dialog.d.a.get()).onReceiveValue("always");
                  }

                  d.this.a(var6);
               } else {
                  String var9 = var2.g();
                  if (com.tencent.smtt.sdk.ui.dialog.d.a.get() != null) {
                     ((ValueCallback)com.tencent.smtt.sdk.ui.dialog.d.a.get()).onReceiveValue("extraMenuEvent:" + var9);
                  }

                  d.this.a("extraMenuEvent:" + var9);
               }

               d.this.dismiss();
            }
         }
      });
      this.i.setOnClickListener(new View.OnClickListener() {
         public void onClick(View var1) {
            b var2 = d.this.l.a();
            ResolveInfo var3 = d.this.l.a(var2);
            d.this.b("userClickOnceEvent:");
            d.this.a("");
            if (var2 != null) {
               if (!var2.e()) {
                  if (var3 == null) {
                     d.this.a(var2);
                     d.this.dismiss();
                     return;
                  }

                  Intent var4 = d.this.o;
                  Context var5 = d.this.getContext();
                  String var6 = var3.activityInfo.packageName;
                  var4.setPackage(var6);
                  if ("com.tencent.mtt".equals(var6)) {
                     var4.putExtra("ChannelID", var5.getApplicationContext().getPackageName());
                     var4.putExtra("PosID", "4");
                  }

                  if (var5.getApplicationInfo().targetSdkVersion >= 24 && VERSION.SDK_INT >= 24) {
                     var4.addFlags(1);
                  }

                  if (!TextUtils.isEmpty(d.this.n)) {
                     var4.putExtra("big_brother_source_key", d.this.n);
                  }

                  try {
                     var5.startActivity(var4);
                  } catch (Exception var8) {
                     var8.printStackTrace();
                  }

                  if (com.tencent.smtt.sdk.ui.dialog.d.a.get() != null) {
                     ((ValueCallback)com.tencent.smtt.sdk.ui.dialog.d.a.get()).onReceiveValue("once");
                  }
               } else if (d.this.c() && com.tencent.smtt.sdk.ui.dialog.d.a.get() != null) {
                  ((ValueCallback)com.tencent.smtt.sdk.ui.dialog.d.a.get()).onReceiveValue("extraMenuEvent:" + var2.g());
               }

               d.this.dismiss();
            }
         }
      });
   }

   private boolean c() {
      return "com.tencent.mobileqq".equals(this.getContext().getApplicationContext().getPackageName());
   }

   private void a(b var1) {
      if (var1.f()) {
         if (this.c() && a.get() != null) {
            ((ValueCallback)a.get()).onReceiveValue("https://mdc.html5.qq.com/d/directdown.jsp?channel_id=11047");
         } else {
            Intent var2 = new Intent("android.intent.action.VIEW", Uri.parse("https://mdc.html5.qq.com/d/directdown.jsp?channel_id=11041"));
            var2.addFlags(268435456);
            this.getContext().startActivity(var2);
         }
      }

   }

   private void b(String var1) {
      if (this.l != null && this.c()) {
         b var2 = this.l.a();
         ResolveInfo var3 = this.l.a(var2);
         if (a.get() != null) {
            if (var2 != null && var3 != null && var3.activityInfo != null && var3.activityInfo.packageName != null) {
               ((ValueCallback)a.get()).onReceiveValue(var1 + var3.activityInfo.packageName);
            } else if (var2 != null) {
               if (var2.e()) {
                  ((ValueCallback)a.get()).onReceiveValue(var1 + var2.g());
               } else if (var2.f()) {
                  ((ValueCallback)a.get()).onReceiveValue(var1 + var2.d());
               }
            } else {
               ((ValueCallback)a.get()).onReceiveValue(var1 + "other");
            }
         }

      }
   }

   private View a(Context var1) {
      this.s = new FrameLayout(var1);
      this.t = new LinearLayout(var1);
      android.widget.FrameLayout.LayoutParams var2 = new android.widget.FrameLayout.LayoutParams(-1, Double.valueOf((double)(0.5F * (float)com.tencent.smtt.sdk.ui.dialog.c.a(var1))).intValue());
      var2.gravity = 17;
      this.t.setLayoutParams(var2);
      this.t.setOrientation(1);
      this.r = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 72.0F);
      com.tencent.smtt.sdk.ui.dialog.widget.a var3 = new com.tencent.smtt.sdk.ui.dialog.widget.a(var1, (float)com.tencent.smtt.sdk.ui.dialog.c.a(var1, 12.0F), (float)com.tencent.smtt.sdk.ui.dialog.c.b(var1, 35.0F), (float)com.tencent.smtt.sdk.ui.dialog.c.b(var1, 15.0F));
      android.widget.LinearLayout.LayoutParams var4 = new android.widget.LinearLayout.LayoutParams(-1, this.r);
      var3.setLayoutParams(var4);
      var3.setOnClickListener(new View.OnClickListener() {
         public void onClick(View var1) {
            d.this.dismiss();
         }
      });
      this.t.addView(var3);
      this.g = new ListView(var1);
      this.g.setOverScrollMode(2);
      this.g.setVerticalScrollBarEnabled(false);
      this.g.setBackgroundColor(-1);
      android.widget.LinearLayout.LayoutParams var6 = new android.widget.LinearLayout.LayoutParams(-1, -1);
      var6.weight = 1.0F;
      var6.gravity = 16;
      this.g.setLayoutParams(var6);
      this.g.setDividerHeight(0);
      this.t.addView(this.g);
      LinearLayout var7 = new LinearLayout(var1);
      this.q = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 150.0F);
      var4 = new android.widget.LinearLayout.LayoutParams(-1, this.q);
      var4.weight = 0.0F;
      var7.setLayoutParams(var4);
      var7.setOrientation(0);
      var7.setBackgroundColor(-1);
      var7.setContentDescription("x5_tbs_activity_picker_btn_container");
      this.h = new com.tencent.smtt.sdk.ui.dialog.widget.b(var1, com.tencent.smtt.sdk.ui.dialog.c.a(var1, 12.0F), Color.parseColor("#EBEDF5"));
      android.widget.LinearLayout.LayoutParams var5 = new android.widget.LinearLayout.LayoutParams(-1, com.tencent.smtt.sdk.ui.dialog.c.a(var1, 90.0F));
      var5.weight = 1.0F;
      var5.topMargin = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 30.0F);
      var5.bottomMargin = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 30.0F);
      var5.leftMargin = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 32.0F);
      var5.rightMargin = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 8.0F);
      this.h.setLayoutParams(var5);
      this.h.setText(com.tencent.smtt.sdk.ui.dialog.e.b("x5_tbs_wechat_activity_picker_label_always"));
      this.h.setTextColor(Color.rgb(29, 29, 29));
      this.h.setTextSize(0, (float)com.tencent.smtt.sdk.ui.dialog.c.a(var1, 34.0F));
      var7.addView(this.h);
      this.i = new com.tencent.smtt.sdk.ui.dialog.widget.b(var1, com.tencent.smtt.sdk.ui.dialog.c.a(var1, 12.0F), Color.parseColor("#00CAFC"));
      var5 = new android.widget.LinearLayout.LayoutParams(-1, com.tencent.smtt.sdk.ui.dialog.c.a(var1, 90.0F));
      var5.weight = 1.0F;
      var5.topMargin = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 30.0F);
      var5.bottomMargin = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 30.0F);
      var5.leftMargin = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 8.0F);
      var5.rightMargin = com.tencent.smtt.sdk.ui.dialog.c.a(var1, 32.0F);
      this.i.setLayoutParams(var5);
      this.i.setText(com.tencent.smtt.sdk.ui.dialog.e.b("x5_tbs_wechat_activity_picker_label_once"));
      this.i.setTextColor(Color.rgb(255, 255, 255));
      this.i.setTextSize(0, (float)com.tencent.smtt.sdk.ui.dialog.c.a(var1, 34.0F));
      var7.addView(this.i);
      this.t.addView(var7);
      this.s.addView(this.t);
      return this.s;
   }

   private Drawable c(String var1) {
      Context var2 = this.getContext();
      if (TextUtils.isEmpty(var1)) {
         return null;
      } else {
         File var3 = new File(var2.getFilesDir(), var1);
         if (!FileHelper.fileWritten(var3)) {
            return null;
         } else {
            BitmapDrawable var4 = null;

            try {
               TbsLog.i("TBSActivityPicker", "load icon from: " + var3.getAbsolutePath());
               Bitmap var5 = BitmapFactory.decodeFile(var3.getAbsolutePath());
               var4 = new BitmapDrawable(var5);
            } catch (Exception var6) {
               var6.printStackTrace();
            }

            return var4;
         }
      }
   }

   private void d() {
      b var1 = null;
      if (this.l != null) {
         var1 = this.l.a();
      }

      Drawable var2 = null;
      String var3 = null;
      String var4 = null;
      if (this.p != null) {
         var2 = this.c(this.p.getString("key_tbs_recommend_icon_url", (String)null));
         String var5 = this.p.getString("key_tbs_recommend_label", (String)null);
         String var6 = this.p.getString("key_tbs_recommend_description", (String)null);
         if (!TextUtils.isEmpty(var5)) {
            var3 = var5;
         }

         if (!TextUtils.isEmpty(var6)) {
            var4 = var6;
         }
      }

      if (var2 == null) {
         var2 = com.tencent.smtt.sdk.ui.dialog.e.a("application_icon");
      }

      if (var3 == null) {
         var3 = "QQ浏览器";
      }

      if (var4 == null) {
         var4 = com.tencent.smtt.sdk.ui.dialog.e.b("x5_tbs_wechat_activity_picker_label_recommend");
      }

      b var7 = new b(this.getContext(), var2, var3, "com.tencent.mtt", var4);
      this.l = new a(this.getContext(), this.o, var7, this.b, var1, this, this.g);
      this.g.setAdapter(this.l);
      this.e();
   }

   private void e() {
      ListAdapter var1 = this.g.getAdapter();
      if (var1 != null) {
         int var2 = 0;

         for(int var3 = 0; var3 < var1.getCount(); ++var3) {
            View var4 = var1.getView(var3, (View)null, this.g);
            var4.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            var2 += var4.getMeasuredHeight();
         }

         double var7 = (double)(this.r + var2 + this.q);
         android.view.ViewGroup.LayoutParams var5 = this.t.getLayoutParams();
         int var6 = com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext());
         var5.height = Double.valueOf(Math.max(Math.min(var7, (double)(0.9F * (float)var6)), (double)(0.5F * (float)var6))).intValue();
         this.t.setLayoutParams(var5);
      }
   }

   void a(boolean var1) {
      if (this.i != null) {
         this.i.setEnabled(var1);
      }

      if (this.h != null) {
         this.h.setEnabled(var1);
      }

      this.b("userMenuClickEvent:");
   }

   public void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.requestWindowFeature(1);
      this.b();
   }
}
