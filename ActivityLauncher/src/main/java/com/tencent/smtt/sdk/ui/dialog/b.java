package com.tencent.smtt.sdk.ui.dialog;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import java.io.BufferedInputStream;

public class b {
   private Context a;
   private ResolveInfo b;
   private Drawable c;
   private String d = "";
   private String e = "";
   private String f;
   private boolean g = false;
   private boolean h = false;
   private String i = "";

   public void a(Drawable var1) {
      this.c = var1;
   }

   public Drawable a() {
      if (this.c != null) {
         return this.c;
      } else {
         Drawable var1 = null;
         var1 = a(this.a, this.d());
         if (var1 == null) {
            if (this.b != null) {
               var1 = this.b.loadIcon(this.a.getPackageManager());
            } else {
               var1 = this.c;
            }
         }

         return var1;
      }
   }

   public String b() {
      return this.b != null ? this.b.loadLabel(this.a.getPackageManager()).toString() : this.d;
   }

   public ResolveInfo c() {
      return this.b;
   }

   b(Context var1, ResolveInfo var2) {
      this.a = var1.getApplicationContext();
      this.b = var2;
      this.c = null;
      this.d = null;
      this.f = null;
   }

   b(Context var1, Drawable var2, String var3, String var4, String var5) {
      this.a = var1.getApplicationContext();
      this.b = null;
      this.c = var2;
      this.d = var3;
      this.f = var4;
      this.h = true;
      this.e = var5;
   }

   b(Context var1, int var2, String var3, String var4) {
      Drawable var5 = null;
      if (var2 != -1) {
         try {
            var5 = var1.getResources().getDrawable(var2);
         } catch (Exception var7) {
         }
      }

      if (var5 == null) {
         var5 = com.tencent.smtt.sdk.ui.dialog.e.a("application_icon");
      }

      this.a = var1.getApplicationContext();
      this.b = null;
      this.f = null;
      this.c = var5;
      this.d = var4;
      this.g = true;
      this.i = var3;
   }

   public String d() {
      if (this.b != null) {
         return this.b.activityInfo.packageName;
      } else {
         return this.f == null ? "" : this.f;
      }
   }

   public void a(ResolveInfo var1) {
      this.b = var1;
   }

   public static Drawable a(Context var0, String var1) {
      if ("com.tencent.mtt".equals(var1)) {
         try {
            return com.tencent.smtt.sdk.ui.dialog.e.a("application_icon");
         } catch (Throwable var10) {
            Log.e("error", "getApkIcon Error:" + Log.getStackTraceString(var10));
            return null;
         }
      } else {
         PackageManager var2 = var0.getApplicationContext().getPackageManager();
         Drawable var3 = null;
         ApplicationInfo var4 = null;

         try {
            var4 = var2.getApplicationInfo(var1, 128);
            if (null == var4) {
               return null;
            }

            Resources var5 = var2.getResourcesForApplication(var4);
            TypedValue var6 = new TypedValue();
            var5.getValue(var4.icon, var6, true);
            String var7 = var6.string.toString();
            BufferedInputStream var8 = null;

            try {
               AssetFileDescriptor var9 = var5.getAssets().openNonAssetFd(var6.assetCookie, var7);
               var8 = new BufferedInputStream(var9.createInputStream());
               var3 = Drawable.createFromResourceStream(var5, var6, var8, (String)null);
            } catch (Exception var11) {
            }
         } catch (Exception var12) {
            Log.e("sdk", "e = " + var12);
         }

         return var3;
      }
   }

   public boolean e() {
      return this.g;
   }

   public boolean f() {
      return this.h;
   }

   public String g() {
      return this.i;
   }

   public String h() {
      return this.e;
   }

   public void a(String var1) {
      this.e = var1;
   }

   public void a(boolean var1) {
      this.h = var1;
   }
}
