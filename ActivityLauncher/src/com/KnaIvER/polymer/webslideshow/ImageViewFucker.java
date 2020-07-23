package com.knaiver.polymer.webslideshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knaiver.polymer.Utils.CMN;

public class ImageViewFucker extends ImageView {
	private Bitmap currentImageBitmap;
	
	public ImageViewFucker(Context context) {
		super(context);
	}
	
	public ImageViewFucker(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ImageViewFucker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public void setImageResource(int resId) {
		//super.setImageResource(resId);
		CMN.Log("setImageResource");
	}
	
	@Override
	public void setImageDrawable(@Nullable Drawable drawable) {
		if(drawable instanceof FuckGlideDrawable) {
			return;
		}
		if(drawable==null) {
			return;
		}
		//CMN.Log("setImageDrawable", getDrawable(), drawable, this);
		super.setImageDrawable(drawable);
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		if(bm==null) {
			return;
		}
		//CMN.Log("setImageBitmap", bm);
		super.setImageBitmap(bm);
	}
	
	public static FuckGlideDrawable FuckGlideDrawable;
	public static class FuckGlideDrawable extends Drawable {
		@Override public void draw(@NonNull Canvas canvas) { }
		@Override public void setAlpha(int alpha) {  }
		@Override public void setColorFilter(@Nullable ColorFilter colorFilter) {  }
		@Override public int getOpacity() { return PixelFormat.OPAQUE; }
	}
}
