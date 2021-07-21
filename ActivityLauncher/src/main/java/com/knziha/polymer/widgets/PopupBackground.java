package com.knziha.polymer.widgets;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ListPopupWindow;

import java.util.ArrayList;

public class PopupBackground extends ViewGroup {
	public final ArrayList<ListPopupWindow> popups = new ArrayList<>();
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
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	
	}
	
	private void checkPopup() {
		//CMN.Log("监听 监听");
		if(popups.size()>0) {
			if(supressNextUpdate) {
				supressNextUpdate=false;
			} else {
				//CMN.Log("更新 更新");
				for (int i = popups.size()-1; i >=0 ; i--) {
					if (popups.get(i).isShowing()) popups.get(i).show();
				}
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
		if (event==null || event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			int i = popups.size()-1;
			if (i>=0) {
				popups.remove(i).dismiss();
			}
		}
		return true;
	}
}
