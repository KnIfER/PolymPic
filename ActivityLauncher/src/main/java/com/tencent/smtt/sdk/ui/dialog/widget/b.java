package com.tencent.smtt.sdk.ui.dialog.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.widget.Button;

public class b extends Button {
   private int a;
   private int b;
   private float c;
   private float d;
   private float e;
   private float f;
   private c g;
   private c h;
   private c i;

   public b(Context var1, int var2, int var3) {
      this(var1, (float)var2, (float)var2, (float)var2, (float)var2, var3);
   }

   public b(Context var1, float var2, float var3, float var4, float var5, int var6) {
      super(var1);
      this.g = null;
      this.h = null;
      this.i = null;
      this.c = var2;
      this.d = var3;
      this.e = var4;
      this.f = var5;
      this.a = var6;
      this.b = Color.parseColor("#D0D0D0");
      this.a();
   }

   protected void onLayout(boolean var1, int var2, int var3, int var4, int var5) {
      super.onLayout(var1, var2, var3, var4, var5);
      if (this.g != null) {
         this.g.a(var4 - var2, var5 - var3);
      }

      if (this.h != null) {
         this.h.a(var4 - var2, var5 - var3);
      }

      if (this.i != null) {
         this.i.a(var4 - var2, var5 - var3);
      }

   }

   public void a() {
      this.g = new c(this.a, this.c, this.d, this.e, this.f);
      this.g.a(this.getWidth(), this.getHeight());
      byte var1 = 80;
      int var2 = var1 << 24 | 16777215 & this.a;
      this.h = new c(var2, this.c, this.d, this.e, this.f);
      this.h.a(this.getWidth(), this.getHeight());
      this.i = new c(this.b, this.c, this.d, this.e, this.f);
      this.i.a(this.getWidth(), this.getHeight());
      StateListDrawable var3 = new StateListDrawable();
      var3.addState(new int[]{16842910, -16842919}, this.g);
      var3.addState(new int[]{16842910, 16842919}, this.h);
      var3.addState(new int[]{-16842910}, this.i);
      this.setBackgroundDrawable(var3);
   }
}
