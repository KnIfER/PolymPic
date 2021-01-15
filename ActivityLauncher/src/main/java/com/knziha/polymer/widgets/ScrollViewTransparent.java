package com.knziha.polymer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import androidx.annotation.Nullable;

public class ScrollViewTransparent extends ScrollView {
	public boolean focusable;
	public ScrollViewTransparent(Context context) {
		super(context);
	}
	
	public ScrollViewTransparent(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ScrollViewTransparent(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(focusable) {
			super.dispatchTouchEvent(ev);
			return true;
		}
		return false;
	}
}
