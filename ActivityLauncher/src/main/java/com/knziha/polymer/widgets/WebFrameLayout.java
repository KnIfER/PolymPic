package com.knziha.polymer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.polymer.AdvancedBrowserWebView;

public class WebFrameLayout extends FrameLayout {
	public WebFrameLayout(@NonNull Context context) {
		super(context);
	}
	
	public WebFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}
	
	public WebFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public void removeViews(int start, int count) {
		for (int i = start+count-1; i >= start; i--) {
			View ca = getChildAt(i);
			if(ca instanceof AdvancedBrowserWebView) {
				AdvancedBrowserWebView webview = ((AdvancedBrowserWebView) ca);
				webview.stopLoading();
				//webview.pauseWeb(); //若此处暂停，再次启动时KitKat无法加载网页。
				webview.removeAllViews();
				webview.destroy();
			}
			super.removeViewAt(i);
		}
	}
}
