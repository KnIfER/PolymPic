package com.tencent.smtt.sdk.ui.dialog.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.drawable.Drawable;

public class c extends Drawable {
   private float a;
   private float b;
   private float c;
   private float d;
   private Path e;
   private Paint f;
   private RectF g;

   public c(int var1, float var2, float var3, float var4, float var5) {
      this.a = var2;
      this.b = var3;
      this.d = var4;
      this.c = var5;
      this.f = new Paint();
      this.f.setStyle(Style.FILL);
      this.f.setAntiAlias(true);
      this.f.setColor(var1);
      this.g = new RectF();
   }

   public void draw(Canvas var1) {
      if (this.e == null) {
         this.e = new Path();
      }

      this.e.reset();
      this.e.addRoundRect(this.g, new float[]{this.a, this.a, this.b, this.b, this.d, this.d, this.c, this.c}, Direction.CCW);
      this.e.close();
      var1.drawPath(this.e, this.f);
   }

   public void setAlpha(int var1) {
      this.f.setAlpha(var1);
      this.invalidateSelf();
   }

   public void setColorFilter(ColorFilter var1) {
      this.f.setColorFilter(var1);
      this.invalidateSelf();
   }

   public int getOpacity() {
      return -3;
   }

   public void a(int var1, int var2) {
      this.g.left = 0.0F;
      this.g.top = 0.0F;
      this.g.right = (float)var1;
      this.g.bottom = (float)var2;
   }
}
