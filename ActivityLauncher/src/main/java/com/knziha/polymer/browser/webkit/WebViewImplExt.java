package com.knziha.polymer.browser.webkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.WebFrameLayout;

import static com.knziha.polymer.browser.webkit.WebViewHelper.bAdvancedMenu;

public class WebViewImplExt extends WebView implements UniversalWebviewInterface {
	public BrowserActivity.TabHolder holder;
	public BrowserActivity activity;
	public WebFrameLayout layout;
	
	public WebViewImplExt(@NonNull Context context) {
		super(context);
		this.activity =(BrowserActivity) context;
	}
	
	@Override
	public void setLayoutParent(WebFrameLayout layout, boolean addView) {
		this.layout = layout;
		this.holder = layout.holder;
		if(addView) {
			layout.addView(this);
		}
	}
	
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		//CMN.Log(lastScroll, "onScrollChanged", l, t, oldl, oldt); //有的网页监听不到
		//version++;
		if(layout!=null&&!Options.getAlwaysRefreshThumbnail() && Math.abs(layout.lastThumbScroll-t)>100){
			layout.lastThumbScroll=t;
		}
		if(mOnScrollChangeListener!=null)
			mOnScrollChangeListener.onScrollChange(this,l,t,oldl,oldt);
	}
	
	public void setOnScrollChangedListener(RecyclerView.OnScrollChangedListener onSrollChangedListener) {
		mOnScrollChangeListener =onSrollChangedListener;
	}
	RecyclerView.OnScrollChangedListener mOnScrollChangeListener;
	
	public void SafeScrollTo(int x, int y) {
		RecyclerView.OnScrollChangedListener mScrollChanged = mOnScrollChangeListener;
		mOnScrollChangeListener =null;
		scrollTo(x, y);
		mOnScrollChangeListener =mScrollChanged;
	}
	
	@Override
	public Object getHitResultObject() {
		return getHitTestResult();
	}
	
	@Override
	public int getHitType(Object ret) {
		return ((HitTestResult)ret).getType();
	}
	
	@Override
	public String getHitExtra(Object ret) {
		return ((HitTestResult)ret).getExtra();
	}
	
	public int getContentHeight(){
		return computeVerticalScrollRange();
	}
	
	public int getContentOffset(){
		return this.computeVerticalScrollOffset();
	}
	
	@Override
	public void loadDataWithBaseURL(String baseUrl,String data,String mimeType,String encoding,String historyUrl) {
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
		//if(!baseUrl.equals("about:blank"))
		layout.isloading=true;
	}
	
	
	@Override
	public void loadUrl(String url) {
		super.loadUrl(url);
		CMN.Log("loadUrl: "+url.equals("about:blank"));
		//if(!url.equals("about:blank"))
		layout.isloading=true;
	}
	
	@Nullable
	@Override
	public WebBackForwardList restoreState(@NonNull Bundle inState) {
		CMN.Log("restoreState: ");
		return super.restoreState(inState);
	}
	
	@SuppressLint("NewApi")
	//Viva Marshmallow!
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		CMN.Log("startActionMode…");
		layout.isIMScrollSupressed = layout.isWebHold;
		if(bAdvancedMenu) {
			WebFrameLayout.AdvancedWebViewCallback webviewcallback = layout.getWebViewActionModeCallback();
			
			ActionMode mode = super.startActionMode(webviewcallback.wrap(callback), type);
			
			//if(true) return mode;
			
			WebViewHelper.getInstance().TweakWebviewContextMenu(getContext(), mode.getMenu());
			
			postDelayed(webviewcallback.explodeMenuRunnable, 350);
			
			return mode;
		}
		return super.startActionMode(callback, type);
	}
	
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		layout.handleSimpleNestedScrolling(layout, this, event);
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean postDelayed(Runnable action, long delayMillis) {
		CMN.Log("postDelayed", action, delayMillis);
		if(!layout.isWebHold&&action.getClass().getName().contains("FloatingActionMode")) {
			CMN.Log("contextMenu_boost");
//			action.run();
//			return true;
		}
		return super.postDelayed(action, delayMillis);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		setWebChromeClient(null);
		setWebViewClient(null);
	}
}