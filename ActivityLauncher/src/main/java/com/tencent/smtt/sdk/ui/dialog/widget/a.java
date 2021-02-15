package com.tencent.smtt.sdk.ui.dialog.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class a extends View {
   private int a;
   private int b;
   private Paint c;
   private Paint d;
   private Path e;
   private Path f;
   private RectF g;
   private float[] h;
   private float i;
   private float j;

   public a(Context var1, float var2, float var3, float var4) {
      super(var1, (AttributeSet)null, 0);
      this.a(var1, var2, var3, var4);
   }

   private void a(Context var1, float var2, float var3, float var4) {
      this.i = var3;
      this.j = var4;
      int var5 = Color.parseColor("#989DB4");
      float var6 = (float)a(var1, 6.0F);
      this.c = new Paint();
      this.d = new Paint();
      this.d.setColor(-1);
      this.d.setStyle(Style.FILL);
      this.d.setAntiAlias(true);
      this.c.setColor(var5);
      this.c.setStyle(Style.STROKE);
      this.c.setAntiAlias(true);
      this.c.setStrokeWidth(var6);
      this.c.setStrokeJoin(Join.ROUND);
      this.g = new RectF();
      this.h = new float[]{var2, var2, var2, var2, 0.0F, 0.0F, 0.0F, 0.0F};
   }

   protected void onSizeChanged(int var1, int var2, int var3, int var4) {
      super.onSizeChanged(var1, var2, var3, var4);
      this.a = var1;
      this.b = var2;
      this.g.left = 0.0F;
      this.g.top = 0.0F;
      this.g.right = (float)this.a;
      this.g.bottom = (float)this.b;
   }

   protected void onDraw(Canvas var1) {
      super.onDraw(var1);
      var1.translate(0.0F, 0.0F);
      var1.rotate(0.0F);
      if (this.f == null) {
         this.f = new Path();
      }

      this.f.reset();
      this.f.addRoundRect(this.g, this.h, Direction.CCW);
      this.f.close();
      var1.drawPath(this.f, this.d);
      var1.translate((float)this.a / 2.0F, (float)this.b / 2.0F + this.j / 2.0F);
      if (this.e == null) {
         this.e = new Path();
      }

      this.e.reset();
      this.e.moveTo(0.0F, 0.0F);
      this.e.lineTo(-this.i / 2.0F, -this.j / 2.0F);
      this.e.close();
      var1.drawPath(this.e, this.c);
      this.e.reset();
      this.e.moveTo(0.0F, 0.0F);
      this.e.lineTo(this.i / 2.0F, -this.j / 2.0F);
      this.e.close();
      var1.drawPath(this.e, this.c);
   }

   protected void onMeasure(int var1, int var2) {
      super.onMeasure(var1, var2);
      this.setMeasuredDimension(this.a(var1), this.a(var2));
   }

   private int a(int var1) {
      int var2 = 100;
      int var3 = MeasureSpec.getMode(var1);
      int var4 = MeasureSpec.getSize(var1);
      if (var3 == 1073741824) {
         var2 = var4;
      } else if (var3 == Integer.MIN_VALUE) {
         var2 = Math.min(var2, var4);
      }

      return var2;
   }

   public static int a(Context var0, float var1) {
      float var2 = var0.getResources().getDisplayMetrics().density;
      return (int)(var1 * var2 + 0.5F);
   }
}
