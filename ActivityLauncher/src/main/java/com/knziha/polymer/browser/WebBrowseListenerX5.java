package com.knziha.polymer.browser;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;

import androidx.annotation.NonNull;

import com.knziha.polymer.Utils.CMN;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

/** WebView Compound Listener ：两大网页客户端监听器及Javascript桥，全局一个实例。 */
public class WebBrowseListenerX5 extends WebViewClient {
	BrowseActivity a;
	private boolean pageStarted;
	final com.tencent.smtt.sdk.WebView webview_Player;
	
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
			webview_Player.clearView();
			webview_Player.loadData("http://2123", "text/plain", "utf8");
			webview_Player.loadUrl("about:blank");
			webview_Player.clearHistory();
		}
	};
	
	public void clearWebview() {
		CMN.Log("clearWebview");
		webview_Player.setTag(null);
		if(Looper.myLooper()!=Looper.getMainLooper()) {
			webview_Player.post(clearWebRunnable);
		} else {
			clearWebRunnable.run();
		}
	}
	
	public void stopWebview() {
		webview_Player.setTag(null);
		webview_Player.stopLoading();
	}
	
	public WebBrowseListenerX5(BrowseActivity activity, WebView x5_webview_Player) {
		a = activity;
		x5_webview_Player.setWebViewClient(this);
		x5_webview_Player.setWebChromeClient(this.mWebClient);
		
		this.webview_Player = x5_webview_Player;
		webview_Player.addJavascriptInterface(this, "wvply");
		
		com.tencent.smtt.sdk.WebSettings webSetting = x5_webview_Player.getSettings();
		webSetting.setAllowFileAccess(true);
		webSetting.setLayoutAlgorithm(com.tencent.smtt.sdk.WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
		webSetting.setSupportZoom(true);
		webSetting.setBuiltInZoomControls(true);
		webSetting.setUseWideViewPort(true);
		webSetting.setSupportMultipleWindows(false);
		// webSetting.setLoadWithOverviewMode(true);
		webSetting.setAppCacheEnabled(true);
		// webSetting.setDatabaseEnabled(true);
		webSetting.setDomStorageEnabled(true);
		webSetting.setJavaScriptEnabled(true);
		webSetting.setGeolocationEnabled(true);
		webSetting.setUserAgentString(true
				?"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
				:null);
//		webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
//		webSetting.setAppCachePath(this.getDir("appcache", 0).getPath());
//		webSetting.setDatabasePath(this.getDir("databases", 0).getPath());
		webSetting.setGeolocationEnabled(false);
		// webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
		webSetting.setPluginState(com.tencent.smtt.sdk.WebSettings.PluginState.ON_DEMAND);
		// webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
		// webSetting.setPreFectch(true);
		//CookieSyncManager.createInstance(this);
		//CookieSyncManager.getInstance().sync();
	}
	
	public WebChromeClient mWebClient = new WebClient();
	
	class WebClient extends WebChromeClient {
		@Override
		public void onHideCustomView() {

		}
		
		@Override
		public void onReceivedTitle(WebView webView, String title) {
			super.onReceivedTitle(webView, title);
			DownloadTask task = (DownloadTask) webView.getTag();
			if(task!=null && TextUtils.isEmpty(task.title)) {
				task.updateTitle(title);
			}
		}
		
		/** 进度变化的回调接口。 进度大于98时强行通知加载完成。<br/>
		 * see {@link WebBrowseListenerX5#onPageFinished}*/
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if(view.getUrl().startsWith("about:")) return;
			CMN.Log("OPC::", newProgress, Thread.currentThread().getId());
			WebView mWebView = (WebView) view;
			
			boolean premature=true;
			int lowerBound = 90;
			if(lowerBound<=10) {
				lowerBound=98;
			}
			if(premature) {
				lowerBound=Math.min(80, lowerBound);
			}
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
		super.onLoadResource(view, url);
		WebView mWebView=((WebView)view);
		DownloadTask task = (DownloadTask) view.getTag();
		if(task!=null) {
			if(task.ext2 !=null && url.contains(task.ext2)) {
				onUrlExtracted(url);
			}
		}
	}
	
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		CMN.Log("onPageStarted……", url, Thread.currentThread().getId());
		if(url.startsWith("about:")) return;
		pageStarted = true;
	}
	
	@Override public void onPageFinished(WebView view, String url) {
		WebView mWebView=((WebView)view);
		String ordinalUrl=view.getUrl();
		if(ordinalUrl!=null) {
			url = ordinalUrl;
		}
		if(url.startsWith("about:")) return;
		if(pageStarted) {
			CMN.Log("OPF:::", url, view.getTitle(), Thread.currentThread().getId());
			//CMN.Log("OPF:::", mWebView.holder.url);
			DownloadTask task = (DownloadTask) view.getTag();
			mWebView.removeCallbacks(OnPageFinishedNotifier);
			pageStarted=false;
			if(task!=null) {
				if(task.ext1!=null) {
					mWebView.evaluateJavascript(task.ext1, null);
				}
			}
		}
	}
	
	Runnable OnPageFinishedNotifier = new Runnable() {
		@Override
		public void run() {
			onPageFinished(a.x5_webview_Player, a.x5_webview_Player.getUrl());
		}
	};
	
	@Override
	public void onReceivedSslError(WebView webView, SslErrorHandler handler, com.tencent.smtt.export.external.interfaces.SslError sslError) {
		super.onReceivedSslError(webView, handler, sslError);
		handler.proceed();
	}
	
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{
		//CMN.Log("SOUL::", url, Thread.currentThread().getId());
		//view.loadUrl(url);
		return false;
	}
	
	@JavascriptInterface
	public void onUrlExtracted(String url) {
		DownloadTask task = (DownloadTask) webview_Player.getTag();
		CMN.Log("onUrlExtract", url, a.taskRunning(task.id));
		EnhanceActivity.sendEmptyMessage(10);
		a.onUrlExtracted(task, url);
	}
	
	@JavascriptInterface
	public void log(String items) {
		CMN.Log(items);
	}
}
