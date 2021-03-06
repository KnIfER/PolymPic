package com.knziha.polymer.browser.webkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.WebFrameLayout;

import java.util.Map;

import static com.knziha.polymer.browser.webkit.WebViewHelper.bAdvancedMenu;

public class WebViewImplExt extends WebView implements UniversalWebviewInterface {
	public BrowserActivity.TabHolder holder;
	public WebFrameLayout layout;
	
	public WebViewImplExt(@NonNull Context context) {
		super(context);
	}
	
	@Override
	public void setLayoutParent(WebFrameLayout layout, boolean addView) {
		this.layout = layout;
		layout.setImplementation(this);
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
	
	@Override
	public View getView() {
		return this;
	}
	
	@Override
	public Object getLastRequest() {
		return null;
	}
	
	@Override
	public Map<String, String> getLastRequestHeaders() {
		return null;
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
	
	@Override
	public void drawToBitmap(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	public int getContentWidth(){
		return computeHorizontalScrollRange();
	}
	
	public int getContentOffset(){
		return this.computeVerticalScrollOffset();
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
		layout.handleSimpleNestedScrolling(layout, layout, event);
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean postDelayed(Runnable action, long delayMillis) {
		CMN.Log("postDelayed", action, delayMillis);
//		if(layout!=null&&!layout.isWebHold&&action.getClass().getName().contains("FloatingActionMode")) {
//			CMN.Log("contextMenu_boost");
////			action.run();
////			return true;
//		}
		return super.postDelayed(action, delayMillis);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		setWebChromeClient(null);
		setWebViewClient(null);
	}
	
	public Object initPrintDocumentAdapter(String var1) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			return super.createPrintDocumentAdapter(var1);
		} else {
			return null;
		}
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!layout.hideForTabView) {
			// WebView has displayed some content and is scrollable.
			//CMN.Log("onNewPicture!!!  v2 ", getVisibility()==View.VISIBLE, CMN.now(), getContentHeight(), getHeight());
		}
	}
	
	@Override
	public int getType() {
		return 0;
	}
}