package com.tencent.smtt.sdk.ui.dialog;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class c {
   private static float a = -1.0F;
   private static int b = -1;
   private static int c = -1;

   private static void b(Context var0) {
      if (a < 0.0F) {
         WindowManager var1 = (WindowManager)var0.getSystemService("window");
         DisplayMetrics var2 = new DisplayMetrics();
         var1.getDefaultDisplay().getMetrics(var2);
         a = var2.density;
         b = var2.heightPixels;
      }

   }

   public static int a(Context var0, float var1) {
      float var2 = 320.0F;
      return b(var0, var1 * 160.0F / var2);
   }

   public static int b(Context var0, float var1) {
      if (a == -1.0F) {
         b(var0);
      }

      return (int)(var1 * a + 0.5F);
   }

   public static int a(Context var0) {
      if (b == -1) {
         b(var0);
      }

      return b;
   }
}
