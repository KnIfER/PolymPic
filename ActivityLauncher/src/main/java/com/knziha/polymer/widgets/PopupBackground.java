package com.knziha.polymer.widgets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;

public class PopupBackground extends View {
	public ListPopupWindow popup;
	public boolean supressNextUpdate;
	ViewTreeObserver.OnGlobalLayoutListener layoutListener;
	
	public PopupBackground(Context context) {
		this(context, null);
	}
	
	public PopupBackground(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PopupBackground(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q) {
			addOnLayoutChangeListener((vv, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> checkPopup());
		} else {
			layoutListener = this::checkPopup;
		}
	}
	
	private void checkPopup() {
		//CMN.Log("监听 监听");
		if(popup!=null && popup.isShowing()) {
			if(supressNextUpdate) {
				supressNextUpdate=false;
			} else {
				//CMN.Log("更新 更新");
				popup.show();
			}
		}
	}
	
	@Override
	public void setVisibility(int visibility) {
		super.setVisibility(visibility);
		if(layoutListener!=null) {
			if(visibility==View.VISIBLE) {
				getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
			} else {
				getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(popup!=null) {
			popup.dismiss();
			popup=null;
		}
		return true;
	}
}
