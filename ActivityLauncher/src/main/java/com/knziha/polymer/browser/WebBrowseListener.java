package com.knziha.polymer.browser;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
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

/** WebView Compound Listener ：两大网页客户端监听器及Javascript桥，全局一个实例。 */
public class WebBrowseListener extends WebViewClient implements DownloadListener, OnScrollChangedListener {
	BrowseActivity a;
	private boolean pageStarted;
	final WebView webview_Player;
	
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
			//webview_Player.loadData("http://2123", "text/plain", "utf8");
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
	
	Runnable stopWebRunnable = new Runnable() {
		@Override
		public void run() {
			webview_Player.stopLoading();
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
	
	public WebBrowseListener(BrowseActivity activity, WebView webview_Player) {
		a = activity;
		webview_Player.setWebChromeClient(this.mWebClient);
		webview_Player.setWebViewClient(this);
		
		this.webview_Player = webview_Player;
		webview_Player.addJavascriptInterface(this, "wvply");
		
		final WebSettings settings = webview_Player.getSettings();
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setDefaultTextEncodingName("UTF-8");
		
		settings.setUserAgentString(true
				?"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
				:null);
		
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
		WebView.setWebContentsDebuggingEnabled(true);
		webview_Player.loadUrl("about:blank");
	}
	
	public WebChromeClient mWebClient = new WebClient();
	
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
			DownloadTask task = (DownloadTask) webView.getTag();
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
		public void onProgressChanged(WebView view, int newProgress) {
			if(view.getUrl().startsWith("about:")||!pageStarted) return;
			CMN.Log("OPC::", newProgress);
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
		//CMN.Log("onLoadResource", view, url);
		DownloadTask task = (DownloadTask) view.getTag();
		if(task!=null) {
			if(task.ext2!=null && url.contains(task.ext2)) {
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
	
	@Override public void onPageFinished(WebView view, String url) {
		WebView mWebView=((WebView)view);
		String ordinalUrl=view.getUrl();
		if(ordinalUrl!=null) {
			url = ordinalUrl;
		}
		if(url.startsWith("about:")) return;
		if(pageStarted) {
			//CMN.Log("OPF:::", mWebView.holder.url);
			DownloadTask task = (DownloadTask) view.getTag();
			CMN.Log("OPF:::", url, view.getTitle(), task);
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
			onPageFinished(a.webview_Player, a.webview_Player.getUrl());
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
	
	@JavascriptInterface
	public void onUrlExtracted(String url) {
		onUrlExtracted(url, null);
	}
	
	@JavascriptInterface
	public void onUrlExtracted(String url, String title) {
		DownloadTask task = (DownloadTask) webview_Player.getTag();
		EnhanceActivity.sendEmptyMessage(10);
		a.onUrlExtracted(task, url, title);
	}
	
	@JavascriptInterface
	public void batRenWithPat(String path, String pattern, String replace) {
		a.batRenWithPat(path, pattern, replace);
	}
}
