package com.knziha.polymer.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.knziha.polymer.AdvancedBrowserWebView;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.databinding.ActivityMainBinding;

public class WebFrameLayout extends FrameLayout {
	public BrowserActivity.TabHolder holder;
	public AdvancedBrowserWebView mWebView;
	public View implView;
	public int offsetTopAndBottom;
	public int legalPad;
	public int legalPart;
	/** (始终显示底栏，滑动隐藏顶栏之时，) 若用户要求响应式高度变化，则启用此。 */
	public boolean PadPartPadBar = false;  // 响应式。
	public boolean recover;
	
	public WebFrameLayout(@NonNull Context context, BrowserActivity.TabHolder holder) {
		super(context);
		this.holder = holder;
	}
	
	
	@Override
	public void setTranslationY(float translationY) {
		//CMN.Log("setTranslationY");
		super.setTranslationY(translationY);
	}
	
	@Override
	public void offsetTopAndBottom(int offset) {
		//CMN.Log("offsetTopAndBottom", offset);
		offsetTopAndBottom = offset;
		super.offsetTopAndBottom(offset);
		if(mWebView != null && !mWebView.isWebHold) {
			mWebView.scrollBy(0, offset);
		}
		if(PadPartPadBar&&legalPad>0) {
			setPadding(0, 0, 0, legalPad-(legalPart-getTop()));
		}
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
	
	
	/** @param hideBarType 滑动隐藏底栏  滑动隐藏顶栏 0
	 *   底栏不动  滑动隐藏顶栏 1
	 *   底栏不动  顶栏不动 2  */
	public void syncBarType(int hideBarType, boolean padPartPadBar, ActivityMainBinding UIData) {
		if(hideBarType==2) { // 两者皆不动
			PadPartPadBar = false;
			legalPart = UIData.toolbar.getHeight();
			legalPad = UIData.bottombar2.getHeight()+legalPart;
		}
		if(hideBarType==1) { // 底不动
			if(PadPartPadBar = padPartPadBar) {
				legalPart = UIData.toolbar.getHeight();
			} else {
				legalPart = 0;
			}
			legalPad = UIData.bottombar2.getHeight()+legalPart;
		}
		if(hideBarType==0) { // 顶动底动
			legalPad = legalPart = 0;
			PadPartPadBar = false;
		}
		if(legalPad!=getPaddingBottom()) {
			setPadding(0, 0, 0, legalPad);
		}
	}
	
	public void destroy() {
		if(mWebView!=null) {
			mWebView.destroy();
		}
	}
	
	public void deconstruct() {
		mWebView.deconstruct();
	}
	
	public Bitmap getBitmap() {
		return mWebView.bm.get();
	}
	
	public boolean lazyLoad() {
		return mWebView.loadIfNeeded();
	}
	
	public void saveIfNeeded() {
		mWebView.saveIfNeeded();
	}
	
	public void recaptureBitmap() {
		mWebView.recaptureBitmap();
	}
	
	public Bitmap saveBitmap() {
		return mWebView.saveBitmap();
	}
	
	/** source 1=animation */
	public void onViewAttached(int source) {
		if(source!=1) {
			mWebView.setStorageSettings();
		}
		if(holder.paused) {
			mWebView.resumeTimers();
			mWebView.onResume();
			mWebView.holder.paused=false;
		}
	}
}
