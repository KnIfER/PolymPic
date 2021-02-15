package com.tencent.smtt.sdk.ui.dialog.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import java.lang.ref.WeakReference;

public class RoundImageView extends ImageView {
   private Paint a;
   private Xfermode b;
   private Bitmap c;
   private float[] d;
   private RectF e;
   private int f;
   private WeakReference<Bitmap> g;
   private float h;
   private Path i;

   public RoundImageView(Context var1) {
      this(var1, (AttributeSet)null);
   }

   public RoundImageView(Context var1, AttributeSet var2) {
      super(var1, var2);
      this.b = new PorterDuffXfermode(Mode.DST_IN);
      this.f = Color.parseColor("#eaeaea");
      this.a = new Paint();
      this.a.setAntiAlias(true);
      this.i = new Path();
      this.d = new float[8];
      this.e = new RectF();
      this.h = (float)com.tencent.smtt.sdk.ui.dialog.c.a(var1, 16.46F);

      for(int var3 = 0; var3 < this.d.length; ++var3) {
         this.d[var3] = this.h;
      }

   }

   protected void onMeasure(int var1, int var2) {
      super.onMeasure(var1, var2);
   }

   protected void onSizeChanged(int var1, int var2, int var3, int var4) {
      super.onSizeChanged(var1, var2, var3, var4);
      this.e.set(0.5F, 0.5F, (float)var1 - 0.5F, (float)var2 - 0.5F);
   }

   private void a(Canvas var1, int var2, int var3, RectF var4, float[] var5) {
      this.a(var2, var3);
      this.i.addRoundRect(var4, var5, Direction.CCW);
      var1.drawPath(this.i, this.a);
   }

   private void a(int var1, int var2) {
      this.i.reset();
      this.a.setStrokeWidth((float)var1);
      this.a.setColor(var2);
      this.a.setStyle(Style.STROKE);
   }

   @SuppressLint({"DrawAllocation"})
   protected void onDraw(Canvas var1) {
      Bitmap var2 = this.g == null ? null : (Bitmap)this.g.get();
      if (var2 != null && !var2.isRecycled()) {
         this.a.setXfermode((Xfermode)null);
         var1.drawBitmap(var2, 0.0F, 0.0F, this.a);
      } else {
         Drawable var3 = this.getDrawable();
         if (null != var3) {
            int var4 = var3.getIntrinsicWidth();
            int var5 = var3.getIntrinsicHeight();
            var2 = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Config.ARGB_8888);
            float var6 = 1.0F;
            Canvas var7 = new Canvas(var2);
            var6 = Math.max((float)this.getWidth() * 1.0F / (float)var4, (float)this.getHeight() * 1.0F / (float)var5);
            var3.setBounds(0, 0, (int)(var6 * (float)var4), (int)(var6 * (float)var5));
            var3.draw(var7);
            if (this.c == null || this.c.isRecycled()) {
               this.c = this.a();
            }

            this.a.reset();
            this.a.setFilterBitmap(false);
            this.a.setXfermode(this.b);
            if (this.c != null) {
               var7.drawBitmap(this.c, 0.0F, 0.0F, this.a);
            }

            this.a.setXfermode((Xfermode)null);
            var1.drawBitmap(var2, 0.0F, 0.0F, (Paint)null);
            this.g = new WeakReference(var2);
         }
      }

      this.a(var1, 1, this.f, this.e, this.d);
   }

   private Bitmap a() {
      Bitmap var1 = null;

      try {
         var1 = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Config.ARGB_8888);
         Canvas var2 = new Canvas(var1);
         Paint var3 = new Paint(1);
         var3.setColor(-16777216);
         var2.drawRoundRect(new RectF(0.0F, 0.0F, (float)this.getWidth(), (float)this.getHeight()), this.h, this.h, var3);
      } catch (Throwable var4) {
         var4.printStackTrace();
      }

      return var1;
   }

   public void invalidate() {
      this.g = null;
      if (this.c != null) {
         this.c.recycle();
         this.c = null;
      }

      super.invalidate();
   }
}
