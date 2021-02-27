package com.knziha.polymer.browser;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.OnScrollChangedListener;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.XWalkWebView;

import org.apache.commons.lang3.StringUtils;

import static org.xwalk.core.Utils.Log;
import static org.xwalk.core.Utils.getLockedView;
import static org.xwalk.core.Utils.getTag;
import static org.xwalk.core.Utils.unlock;

/** WebView Compound Listener ：两大网页客户端监听器及Javascript桥，全局一个实例。 */
public class WebBrowseListener extends WebViewClient implements DownloadListener, OnScrollChangedListener {
	BrowseActivity a;
	private boolean pageStarted;
	View webview_Player;
	UniversalWebviewInterface webviewImpl;
	
	
	@SuppressLint("HandlerLeak")
	Handler EnhanceActivity = new Handler(){
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			if(msg.what==10) {
				clearWebview();
			}
		}
	};
	
	Runnable clearWebRunnable = new Runnable() {
		@Override
		public void run() {
			CMN.Log("clearing Webview …… ");
			webviewImpl.clearView();
			//webview_Player.loadData("http://2123", "text/plain", "utf8");
			webviewImpl.loadUrl("about:blank");
			webviewImpl.clearHistory();
		}
	};
	
	public void clearWebview() {
		CMN.Log("clearWebview");
		webview_Player.setTag(null);
		if(Looper.myLooper()!=Looper.getMainLooper()) {
			webview_Player.removeCallbacks(clearWebRunnable);
			webview_Player.post(clearWebRunnable);
		} else {
			clearWebRunnable.run();
		}
	}
	
	Runnable stopWebRunnable = new Runnable() {
		@Override
		public void run() {
			webviewImpl.stopLoading();
		}
	};
	
	public void stopWebview() {
		webview_Player.setTag(null);
		if(Looper.myLooper()!=Looper.getMainLooper()) {
			webview_Player.post(stopWebRunnable);
		} else {
			stopWebRunnable.run();
		}
	}
	
	public WebBrowseListener(BrowseActivity activity, UniversalWebviewInterface webviewImpl) {
		a = activity;
		this.webviewImpl = webviewImpl;
		this.webview_Player = (View) webviewImpl;
		webviewImpl.setWebChromeClient(this.mWebClient);
		webviewImpl.setWebViewClient(this);
		
		webviewImpl.addJavascriptInterface(this, "wvply");
		
		final WebSettings settings = webviewImpl.getSettings();
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setDefaultTextEncodingName("UTF-8");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		
		settings.setNeedInitialFocus(false);
		//settings.setDefaultFontSize(40);
		//settings.setTextZoom(100);
		//setInitialScale(25);
		
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setMediaPlaybackRequiresUserGesture(false);
		
		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
		
		//settings.setUseWideViewPort(true);//设定支持viewport
		//settings.setLoadWithOverviewMode(true);
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		//settings.setSupportZoom(support);
		settings.setAllowUniversalAccessFromFileURLs(true);
		
		settings.setUseWideViewPort(true);
		
		webviewImpl.loadUrl("about:blank");

//		settings.setAppCacheMaxSize(Long.MAX_VALUE);
//		settings.setAppCachePath(this.getDir("appcache", 0).getPath());
//		settings.setDatabasePath(this.getDir("databases", 0).getPath());
		settings.setGeolocationEnabled(false);
		// settings.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
		settings.setPluginState(com.tencent.smtt.sdk.WebSettings.PluginState.ON_DEMAND);
		// settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
		// settings.setPreFectch(true);
		//CookieSyncManager.createInstance(this);
		//CookieSyncManager.getInstance().sync();
	}
	
	public WebChromeClient mWebClient = new WebClient();
	
	public void loadUrl(String url, DownloadTask task) {
		//webview_Player.stopLoading();
		String ua = task.ua;
		CMN.Log("ua!!!", ua);
		if(TextUtils.isEmpty(ua)) {
			ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36";
		} else if(ua.startsWith("ph")){
			ua = "Mozilla/5.0 (Linux; Android 6.0.1; PAD A57) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Mobile Safari/537.36 OPR/58.2.2878.53403";
		}
		if(!StringUtils.equals(webviewImpl.getSettings().getUserAgentString()
				, ua)) {
			webviewImpl.getSettings().setUserAgentString(ua);
		}
		task.shotFn = null;
		if(url==null) url = "https://www.baidu.com";
		int idx = url.indexOf("\n");
		if(idx>0) {
			if(url.charAt(idx-1)=='\r') {
				idx--;
			}
			url = url.substring(0, idx);
		}
		webviewImpl.loadUrl(url);
		webview_Player.setTag(task);
	}
	
	class WebClient extends WebChromeClient {
		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			CMN.Log("onCreateWindow", isDialog);
			return false;
		}
		
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {

		}
		
		@Override
		public void onHideCustomView() {

		}
		
		@Override
		public void onReceivedTitle(WebView webView, String title) {
			DownloadTask task = (DownloadTask) webview_Player.getTag();
			if(task!=null) {
				task.webTitle = title;
				if(TextUtils.isEmpty(task.title)) {
					a.updateTitleForRow(task.id, title);
				}
			}
		}
		
		/** 进度变化的回调接口。 进度大于98时强行通知加载完成。<br/>
		 * see {@link WebBrowseListener#onPageFinished}*/
		@Override
		public void onProgressChanged(WebView  view, int newProgress) {
			//lock.unlock();
			UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
			View mWebView = (View) webviewImpl;
			if(webviewImpl.getUrl()==null||webviewImpl.getUrl().startsWith("about:")||!pageStarted) return;
			CMN.Log("OPC::", newProgress);
			
			boolean premature=true;
			int lowerBound = 90;
			if(lowerBound<=10) {
				lowerBound=98;
			}
			if(premature) {
				lowerBound=Math.min(80, lowerBound);
			}
//			if(webviewImpl instanceof XWalkWebView) {
//				lowerBound=Math.min(65, lowerBound);
//			}
			if(newProgress>=lowerBound){
				CMN.Log("newProgress>=98", newProgress);
				mWebView.post(OnPageFinishedNotifier);
				//OnPageFinishedNotifier.run();
				if(premature) {
					//mWebView.stopLoading();
				}
			}
		}
	}
	
	@Override
	public void onLoadResource(WebView view, String url) {
		UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
		View mWebView = (View) webviewImpl;
		//CMN.Log("onLoadResource", view, url);
		DownloadTask task = (DownloadTask) mWebView.getTag();
		if(task!=null) {
			if(task.ext2!=null && task.ext2contains(url)) {
				onUrlExtracted(url, null);
			}
		}
	}
	
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		CMN.Log("onPageStarted……", url, Thread.currentThread().getId());
		if(url.startsWith("about:")) return;
		pageStarted = true;
	}
	
	@Override public void onPageFinished(WebView  view, String url) {
		UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
		View mWebView = (View) webviewImpl;
		String ordinalUrl=webviewImpl.getUrl();
		if(ordinalUrl!=null) {
			url = ordinalUrl;
		}
		if(url.startsWith("about:")) return;
		if(pageStarted) {
			//CMN.Log("OPF:::", mWebView.holder.url);
			DownloadTask task = (DownloadTask) mWebView.getTag();
			CMN.Log("OPF:::", url, webviewImpl.getTitle(), task);
			mWebView.removeCallbacks(OnPageFinishedNotifier);
			pageStarted=false;
			if(task!=null) {
				if(task.ext1!=null) {
					webviewImpl.evaluateJavascript(task.ext1, null);
				}
			}
		}
	}

	
	Runnable OnPageFinishedNotifier = new Runnable() {
		@Override
		public void run() {
			if(webviewImpl instanceof WebView) {
				onPageFinished((WebView) webviewImpl, webviewImpl.getUrl());
			} else {
				try {
					onPageFinished(getLockedView(webviewImpl, true), webviewImpl.getUrl());
				} catch (Exception e) {
					Log(e);
				}
				unlock();
			}
		}
	};
	
	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		handler.proceed();
	}
	
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{
		//CMN.Log("SOUL::", url, Thread.currentThread().getId());
		//view.loadUrl(url);
		return false;
	}
	
	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		//CMN.Log("DOWNLOAD:::", url, contentDisposition, mimetype, contentLength);
	}
	
	@Override
	public void onScrollChange(View v, int scrollX, int scrollY, int oldx, int oldy) {
	}
	
	@org.xwalk.core.JavascriptInterface
	@JavascriptInterface
	public void onUrlExtracted(String url) {
		onUrlExtracted(url, null);
	}
	
	@org.xwalk.core.JavascriptInterface
	@JavascriptInterface
	public void onUrlExtracted(String url, String title) {
		DownloadTask task = (DownloadTask) webview_Player.getTag();
		EnhanceActivity.sendEmptyMessage(10);
		a.onUrlExtracted(task, url, title);
	}
	
	@org.xwalk.core.JavascriptInterface
	@JavascriptInterface
	public void batRenWithPat(String path, String pattern, String replace) {
		a.batRenWithPat(path, pattern, replace);
	}
}
