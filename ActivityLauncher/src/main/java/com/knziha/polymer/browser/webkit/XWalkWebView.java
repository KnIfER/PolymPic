package com.knziha.polymer.browser.webkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.WebFrameLayout;

import org.xwalk.core.XWalkDownloadListener;
import org.xwalk.core.XWalkHitTestResult;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;
import org.xwalk.core.XWalkWebResourceRequest;

import java.util.Map;

import static org.xwalk.core.Utils.unlock;

public class XWalkWebView extends XWalkView implements UniversalWebviewInterface
{
	public BrowserActivity.TabHolder holder;
	public WebFrameLayout layout;
	
	XWalkResourceClient xWalkResourceClient;
	XWalkUIClient xWalkUIClient;
	
	static {
		//XWalkPreferences.setValue(XWalkPreferences.ANIMATABLE_XWALK_VIEW, true);
	}
	
	public XWalkWebView(@NonNull Context context) {
		super(context);
	}
	
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
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Override
	public Object getLastRequest() {
		XWalkWebResourceRequest wrr = this.wrr;
		unlock();
		if(wrr==null) {
			return null;
		}
		return new android.webkit.WebResourceRequest(){
			@Override
			public Uri getUrl() {
				return wrr.getUrl();
			}
			@Override
			public boolean isForMainFrame() {
				return wrr.isForMainFrame();
			}
			@Override
			public boolean isRedirect() {
				return false;
			}
			@Override
			public boolean hasGesture() {
				return wrr.hasGesture();
			}
			@Override
			public String getMethod() {
				return wrr.getMethod();
			}
			@Override
			public Map<String, String> getRequestHeaders() {
				return wrr.getRequestHeaders();
			}
		};
	}
	
	@Override
	public Map<String, String> getLastRequestHeaders() {
		XWalkWebResourceRequest wrr = this.wrr;
		unlock();
		if(wrr==null) {
			return null;
		}
		Map<String, String> ret = wrr.getRequestHeaders();
		ret.put("Method", wrr.getMethod());
		ret.put("Url", wrr.getUrl().toString());
		return ret;
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
	public void setDownloadListener(DownloadListener listener) {
		super.setDownloadListener(new XWalkDownloadListener(getContext(), listener));
	}
	
	@Override
	public int getHitType(Object ret) {
		return ((XWalkHitTestResult)ret).getType().ordinal();
	}
	
	@Override
	public String getHitExtra(Object ret) {
		return ((XWalkHitTestResult)ret).getExtra();
	}
	
	public int getContentHeight(){
		return computeVerticalScrollRange();
	}
	
	@Override
	public void setPictureListener(WebView.PictureListener pictureListener) {
		// no impl.
	}
	
	@Override
	public void drawToBitmap(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	public int getContentWidth(){
		return computeHorizontalScrollOffset();
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
	
	@Override
	public void clearHistory() {
		getNavigationHistory().clear();
	}
	
	@Override
	public void clearView() {
		//getNavigationHistory().clear();
	}
	
	private void initListeners() {
		if (xWalkResourceClient == null) {
			super.setResourceClient(xWalkResourceClient = new XWalkResourceClient(this, null, null));
		}
		if (xWalkUIClient == null) {
			super.setUIClient(xWalkUIClient = new XWalkUIClient(this, null, null));
		}
	}
	
	@Override
	public void setWebChromeClient(WebChromeClient mWebClient) {
		initListeners();
		xWalkResourceClient.syncAB(null, mWebClient);
		xWalkUIClient.syncAB(null, mWebClient);
	}
	
	@Override
	public void setWebViewClient(WebViewClient webBrowseListener) {
		initListeners();
		xWalkResourceClient.syncAB(webBrowseListener, null);
		xWalkUIClient.syncAB(webBrowseListener, null);
	}
	
	@Override
	public void onPause() {
	
	}
	
	@Override
	public int getProgress() {
		return 0; //www
	}
	
	@Override
	public void reload() {
		super.reload(0); //www
	}
	
	@Override
	public boolean canGoBack() {
		return super.getNavigationHistory().canGoBack();
	}
	
	@Override
	public void goBack() {
		super.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
	}
	
	@Override
	public boolean canGoForward() {
		return super.getNavigationHistory().canGoForward();
	}
	
	@Override
	public void goForward() {
		super.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.FORWARD, 1);
	}
	
	@Override
	public void saveWebArchive(String path, boolean b, ValueCallback<String> stringValueCallback) {
		// www
	}
	
	@Override
	public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
		CMN.Log("startActionModeForChild …");
		//return super.startActionModeForChild(originalView, callback, type);
		return startActionMode(callback, type);
	}
	
//	@Override
//	public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
//		CMN.Log("startActionModeForChild 1 …");
//		return super.startActionModeForChild(originalView, callback);
//	}
	

//	@Override
//	public ActionMode startActionMode(ActionMode.Callback callback) {
//		CMN.Log("startActionMode 0 …");
//		return super.startActionMode(callback);
//	}
	
	//Viva Marshmallow!
//	@Override
//	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
//		CMN.Log("startActionMode…");
//		layout.isIMScrollSupressed = layout.isWebHold;
//		if(bAdvancedMenu) {
//			WebFrameLayout.AdvancedWebViewCallback webviewcallback = layout.getWebViewActionModeCallback();
//
//			ActionMode mode = super.startActionMode(webviewcallback.wrap(callback), type);
//
//			//if(true) return mode;
//
//			WebViewHelper.getInstance().TweakWebviewContextMenu(getContext(), mode.getMenu());
//
//			postDelayed(webviewcallback.explodeMenuRunnable, 350);
//
//			return mode;
//		}
//		return super.startActionMode(callback, type);
//	}
	
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		return this.dummyActionMode();
	}
	
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback) {
		return this.dummyActionMode();
	}
	
	public ActionMode dummyActionMode() {
		return new ActionMode() {
			@Override public void setTitle(CharSequence title) {}
			@Override public void setTitle(int resId) {}
			@Override public void setSubtitle(CharSequence subtitle) {}
			@Override public void setSubtitle(int resId) {}
			@Override public void setCustomView(View view) {}
			@Override public void invalidate() {}
			@Override public void finish() {}
			@Override public Menu getMenu() { return null; }
			@Override public CharSequence getTitle() { return null; }
			@Override public CharSequence getSubtitle() { return null; }
			@Override public View getCustomView() { return null; }
			@Override public MenuInflater getMenuInflater() { return null; }
		};
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
		if(layout!=null&&!layout.isWebHold&&action.getClass().getName().contains("FloatingActionMode")) {
			CMN.Log("contextMenu_boost");
//			action.run();
//			return true;
		}
		return super.postDelayed(action, delayMillis);
	}
	
	@Override
	public void destroy() {
		//super.destroy();
		setWebChromeClient(null);
		setWebViewClient(null);
	}
	
	@Override
	public void onResume() {
	
	}
	
	public Object initPrintDocumentAdapter(String var1) {
		return null;
	}
	
	@Override
	public Picture capturePicture() {
		return null;
	}
	
	public void requestFocusNodeHref(Message var1) {
		// no impl.
	}
	
	public void requestImageRef(Message var1) {
		// no impl.
	}
	
	@Override
	public int getType() {
		return 2;
	}
}