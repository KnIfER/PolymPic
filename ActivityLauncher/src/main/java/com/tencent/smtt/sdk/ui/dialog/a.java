package com.tencent.smtt.sdk.ui.dialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView.ScaleType;
import com.tencent.smtt.sdk.stat.MttLoader;
import com.tencent.smtt.sdk.ui.dialog.widget.RoundImageView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class a extends ArrayAdapter<b> implements OnClickListener, ListAdapter {
   private ArrayList<b> a;
   private b b = null;
   private Intent c;
   private WeakReference<ListView> d;
   private WeakReference<d> e;
   private b f;
   private b g;
   private List<b> h;
   private Handler i;
   private String[] j;

   public b a() {
      return this.b;
   }

   void a(ListView var1) {
      this.d = new WeakReference(var1);
   }

   void a(d var1) {
      this.e = new WeakReference(var1);
   }

   public a(Context var1, Intent var2, b var3, List<b> var4, b var5, d var6, ListView var7) {
      super(var1, 0);
      this.a(var6);
      this.a(var7);
      this.g = var3;
      this.c = var2;
      if (!"com.tencent.rtxlite".equalsIgnoreCase(var1.getApplicationContext().getPackageName()) && !MttLoader.isBrowserInstalled(var1)) {
         this.f = this.g;
      } else {
         this.f = null;
      }

      this.h = var4;
      this.i = new Handler() {
         public void handleMessage(Message var1) {
            switch(var1.what) {
            case 1:
               a.this.b();
            default:
            }
         }
      };
      this.j = new String[2];
      this.j[0] = com.tencent.smtt.sdk.ui.dialog.e.b("x5_tbs_activity_picker_recommend_to_trim");
      this.j[1] = com.tencent.smtt.sdk.ui.dialog.e.b("x5_tbs_activity_picker_recommend_with_chinese_brace_to_trim");
      this.a(var1, var5);
   }

   void a(Context var1, b var2) {
      this.a = new ArrayList();
      if (this.h != null && this.h.size() != 0) {
         this.a.addAll(this.h);
      }

      PackageManager var3 = var1.getPackageManager();
      List var4 = var3.queryIntentActivities(this.c, 65536);
      boolean var5 = false;
      boolean var6 = false;
      Iterator var7 = var4.iterator();

      while(true) {
         ResolveInfo var8;
         Drawable var9;
         do {
            if (!var7.hasNext()) {
               if (!var5 && this.f != null) {
                  this.a.add(0, this.f);
               }

               if (!var6 && this.a.size() > 0) {
                  this.b(var1, (b)this.a.get(0));
               }

               return;
            }

            var8 = (ResolveInfo)var7.next();
            var9 = com.tencent.smtt.sdk.ui.dialog.b.a(var1, var8.activityInfo.packageName);
            if (var9 != null) {
               break;
            }

            var9 = var8.loadIcon(var1.getPackageManager());
         } while(var9 == null);

         b var10 = new b(var1, var8);
         if (this.f != null && this.f.d().equals(var8.activityInfo.packageName)) {
            var10.a(this.f.f());
            var10.a(this.f.h());
            var10.a(this.f.a());
            this.a.add(0, var10);
            var5 = true;
         } else if ("com.tencent.mtt".equals(var8.activityInfo.packageName)) {
            if (this.g != null) {
               var10.a(this.g.f());
               var10.a(this.g.h());
               var10.a(this.g.a());
            }

            this.a.add(0, var10);
         } else {
            this.a.add(var10);
         }

         if (!var6 && var2 != null && var10.d().equals(var2.d())) {
            this.b(var1, var10);
            var6 = true;
         }
      }
   }

   public b a(int var1) {
      return var1 >= 0 && var1 < this.a.size() ? (b)this.a.get(var1) : null;
   }

   public int getCount() {
      return this.a.size();
   }

   public View getView(int var1, View var2, ViewGroup var3) {
      b var4 = this.a(var1);
      if (var4 == null) {
         return null;
      } else {
         View var5 = null;
         if (var2 != null) {
            var5 = var2;
         } else {
            var5 = this.a(this.getContext());
         }

         this.a(var4, var5);
         return var5;
      }
   }

   private View a(Context var1) {
      LinearLayout var2 = new LinearLayout(var1);
      var2.setLayoutParams(new LayoutParams(-1, -2));
      var2.setOrientation(1);
      StateListDrawable var3 = new StateListDrawable();
      var3.addState(new int[]{16842919}, new ColorDrawable(Color.argb(41, 0, 0, 0)));
      var3.addState(new int[]{-16842919}, new ColorDrawable(0));
      var2.setBackgroundDrawable(var3);
      RelativeLayout var7 = new RelativeLayout(var1);
      var7.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 144.0F)));
      RoundImageView var4 = new RoundImageView(var1);
      var4.setScaleType(ScaleType.FIT_CENTER);
      android.widget.RelativeLayout.LayoutParams var5 = new android.widget.RelativeLayout.LayoutParams(com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 96.0F), com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 96.0F));
      var5.addRule(9);
      var5.addRule(15);
      var5.setMargins(com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 32.0F), com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 24.0F), com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 24.0F), com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 24.0F));
      var4.setLayoutParams(var5);
      var4.setId(101);
      var7.addView(var4);
      LinearLayout var8 = new LinearLayout(var1);
      var5 = new android.widget.RelativeLayout.LayoutParams(-2, -2);
      var5.addRule(15);
      var5.addRule(1, 101);
      var8.setLayoutParams(var5);
      var8.setOrientation(1);
      TextView var6 = new TextView(var1);
      var6.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-2, -2));
      var6.setMaxLines(1);
      var6.setTextColor(Color.rgb(29, 29, 29));
      var6.setTextSize(1, 17.0F);
      var6.setId(102);
      var8.addView(var6);
      var6 = new TextView(var1);
      var6.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-2, -2));
      var6.setText(com.tencent.smtt.sdk.ui.dialog.e.b("x5_tbs_wechat_activity_picker_label_recommend"));
      var6.setTextColor(Color.parseColor("#00CAFC"));
      var6.setTextSize(1, 14.0F);
      var6.setId(103);
      var8.addView(var6);
      var7.addView(var8);
      ImageView var9 = new ImageView(var1);
      var5 = new android.widget.RelativeLayout.LayoutParams(com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 48.0F), com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 48.0F));
      var5.addRule(11);
      var5.addRule(15);
      var5.setMargins(0, 0, com.tencent.smtt.sdk.ui.dialog.c.a(this.getContext(), 32.0F), 0);
      var9.setLayoutParams(var5);
      var9.setImageDrawable(com.tencent.smtt.sdk.ui.dialog.e.a("x5_tbs_activity_picker_check"));
      var9.setId(104);
      var7.addView(var9);
      var7.setId(105);
      var2.addView(var7);
      return var2;
   }

   public void b() {
      if (Looper.myLooper() != Looper.getMainLooper()) {
         this.i.obtainMessage(1).sendToTarget();
      } else {
         ListView var1 = (ListView)this.d.get();
         if (var1 != null) {
            View var2 = var1.findViewWithTag(this.f);
            if (var2 != null) {
               this.a(this.f, var2);
            }

         }
      }
   }

   public ResolveInfo a(b var1) {
      if (var1 != null && !TextUtils.isEmpty(var1.d())) {
         PackageManager var2 = this.getContext().getPackageManager();
         List var3 = var2.queryIntentActivities(this.c, 65536);
         Iterator var4 = var3.iterator();

         ResolveInfo var5;
         do {
            if (!var4.hasNext()) {
               return null;
            }

            var5 = (ResolveInfo)var4.next();
         } while(!var1.d().equals(var5.activityInfo.packageName));

         return var5;
      } else {
         return null;
      }
   }

   private void a(b var1, View var2) {
      if (var2 != null && var1 != null) {
         ImageView var3 = (ImageView)var2.findViewById(101);
         TextView var4 = (TextView)var2.findViewById(102);
         TextView var5 = (TextView)var2.findViewById(103);
         ImageView var6 = (ImageView)var2.findViewById(104);
         View var7 = var2.findViewById(105);
         View var8 = var2.findViewById(106);
         var3.setImageDrawable(var1.a());
         char var9 = 160;
         String var10 = var1.b().trim().replaceAll(var9 + "", "");
         String[] var11 = this.j;
         int var12 = var11.length;

         for(int var13 = 0; var13 < var12; ++var13) {
            String var14 = var11[var13];
            if (var14 != null && var14.length() > 0) {
               var10 = var10.replaceAll(var14, "");
            }
         }

         var4.setText(var10);
         if (var1.c() == null) {
            var1.a(this.a(var1));
         }

         var7.setOnClickListener(new OnClickListener() {
            public void onClick(View var1) {
               ViewParent var2 = var1.getParent();
               if (var2 instanceof View) {
                  View var3 = (View)var2;
                  if (var3.getTag() == a.this.f) {
                     a.this.onClick(var3);
                  }

               }
            }
         });
         if ("com.tencent.mtt".equals(var1.d())) {
            var5.setVisibility(0);
            var5.setText(var1.h());
         } else {
            var5.setVisibility(8);
         }

         var7.setClickable(false);
         var7.setEnabled(false);
         if (var1 == this.b) {
            var6.setVisibility(0);
            if (var8 != null) {
               var8.setVisibility(0);
            }
         } else {
            var6.setVisibility(8);
            if (var8 != null) {
               var8.setVisibility(8);
            }
         }

         var2.setTag(var1);
         var2.setOnClickListener(this);
      }
   }

   public void onClick(View var1) {
      Object var2 = var1.getTag();
      if (var2 != null && var2 instanceof b) {
         b var3 = (b)var2;
         if (var3 == this.b) {
            return;
         }

         ViewParent var4 = var1.getParent();
         View var5 = null;
         if (var4 instanceof View) {
            var5 = (View)var4;
         }

         b var6 = this.b;
         this.b(var1.getContext(), var3);
         View var7 = var5.findViewWithTag(var6);
         this.a(var6, var7);
         this.a(this.b, var1);
      }

   }

   private void b(Context var1, b var2) {
      this.b = var2;
      if (this.b != null) {
         if (!this.b.e() && !this.b.f()) {
            this.a(a(var1, this.b.d()));
         } else {
            this.a(true);
         }

      }
   }

   private void a(boolean var1) {
      if (this.e != null) {
         d var2 = (d)this.e.get();
         if (var2 != null) {
            var2.a(var1);
         }

      }
   }

   public static boolean a(Context var0, String var1) {
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

   // $FF: synthetic method
   public com.tencent.smtt.sdk.ui.dialog.b getItem(int var1) {
      return this.a(var1);
   }
}
