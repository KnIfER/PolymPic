package com.tencent.smtt.sdk;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;

import androidx.annotation.RequiresApi;

import com.knziha.polymer.Utils.CMN;
import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.IX5WebViewClient;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorCompat;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.export.external.proxy.X5ProxyWebViewClient;
import com.tencent.smtt.utils.TbsLog;

import static com.tencent.smtt.export.external.interfaces.WebResourceResponse.new_WebResourceResponse;
import static org.xwalk.core.Utils.getLockedView;
import static org.xwalk.core.Utils.unlock;

class X5WebViewClient extends X5ProxyWebViewClient {
	private android.webkit.WebViewClient webViewClient;
	private WebView webView;
	
	public X5WebViewClient(IX5WebViewClient var1, WebView var2, android.webkit.WebViewClient var3) {
		super(var1);
		this.webView = var2;
		this.webViewClient = var3;
		//this.webViewClient.x5WebViewClient = this;
	}
	
	public void doUpdateVisitedHistory(IX5WebViewBase var1, String var2, boolean var3) {
		this.webView.a(var1);
		try {
			this.webViewClient.doUpdateVisitedHistory(getLockedView(webView, false), var2, var3);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onFormResubmission(IX5WebViewBase var1, Message var2, Message var3) {
		this.webView.a(var1);
		try {
			this.webViewClient.onFormResubmission(getLockedView(webView, true), var2, var3);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onLoadResource(IX5WebViewBase var1, String var2) {
		this.webView.a(var1);
		try {
			this.webViewClient.onLoadResource(getLockedView(webView, true), var2);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onPageFinished(IX5WebViewBase var1, int var2, int var3, String var4) {
		
		this.webView.a(var1);
		++this.webView.a;
		try {
			this.webViewClient.onPageFinished(getLockedView(webView, true), var4);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		if ("com.qzone".equals(var1.getView().getContext().getApplicationInfo().packageName)) {
			this.webView.a(var1.getView().getContext());
		}
		
		TbsLog.app_extra("SmttWebViewClient", var1.getView().getContext());
		
		try {
			super.onPageFinished(var1, var2, var3, var4);
		} catch (Exception var6) {
		}
		
		WebView.d();
		if (!TbsShareManager.mHasQueryed && this.webView.getContext() != null && TbsShareManager.isThirdPartyApp(this.webView.getContext())) {
			TbsShareManager.mHasQueryed = true;
			(new Thread(new Runnable() {
				public void run() {
					if (!TbsShareManager.forceLoadX5FromTBSDemo(X5WebViewClient.this.webView.getContext()) && TbsDownloader.needDownload(X5WebViewClient.this.webView.getContext(), false)) {
						TbsDownloader.startDownload(X5WebViewClient.this.webView.getContext());
					}
					
				}
			})).start();
		}
		
		if (this.webView.getContext() != null && !TbsLogReport.getInstance(this.webView.getContext()).getShouldUploadEventReport()) {
			TbsLogReport.getInstance(this.webView.getContext()).setShouldUploadEventReport(true);
			TbsLogReport.getInstance(this.webView.getContext()).dailyReport();
		}
		
	}
	
	public void onPageStarted(IX5WebViewBase var1, int var2, int var3, String var4, Bitmap var5) {
		this.webView.a(var1);
		try {
			this.webViewClient.onPageStarted(getLockedView(webView, true), var4, var5);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	public void onReceivedError(IX5WebViewBase var1, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
		this.webView.a(var1);
		android.webkit.WebView wv = getLockedView(webView, false);
		this.webView.wrr = webResourceRequest;
		this.webView.wre = webResourceError;
		try {
			this.webViewClient.onReceivedError(wv, null, null);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onReceivedError(IX5WebViewBase var1, int var2, String var3, String var4) {
		if (var2 < -15) {
			if (var2 != -17) {
				return;
			}
			
			var2 = -1;
		}
		
		this.webView.a(var1);
		try {
			this.webViewClient.onReceivedError(getLockedView(webView, true), var2, var3, var4);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onReceivedHttpError(IX5WebViewBase var1, WebResourceRequest webResourceRequest, WebResourceResponse webResourceResponse) {
		this.webView.a(var1);
		android.webkit.WebView wv = getLockedView(webView, false);
		this.webView.wrr = webResourceRequest;
		this.webView.wret = webResourceResponse;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			//hhh
			try {
				this.webViewClient.onReceivedHttpError(wv, null, null);
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		unlock();
	}
	
	//hx
	public void onReceivedHttpAuthRequest(IX5WebViewBase var1, HttpAuthHandler httpAuthHandler, String var3, String var4) {
		this.webView.a(var1);
		CMN.Log("onReceivedHttpAuthRequest", var3, var4);
		//httpAuthHandler.cancel();
	}
	
	//hx
	public void onReceivedClientCertRequest(IX5WebViewBase var1, ClientCertRequest clientCertRequest) {
		this.webView.a(var1);
		CMN.Log("onReceivedClientCertRequest", clientCertRequest.getHost());
		//clientCertRequest.cancel();
	}
	
	public void onReceivedSslError(IX5WebViewBase var1, SslErrorHandler sslErrorHandler, SslError sslError) {
		this.webView.a(var1);
		android.webkit.WebView wv = getLockedView(webView, true);
		try {
			this.webViewClient.onReceivedSslError(wv, null, new SslErrorCompat(sslError, sslErrorHandler));
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onScaleChanged(IX5WebViewBase var1, float var2, float var3) {
		this.webView.a(var1);
		try {
			this.webViewClient.onScaleChanged(getLockedView(webView, true), var2, var3);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onUnhandledKeyEvent(IX5WebViewBase var1, KeyEvent var2) {
		this.webView.a(var1);
		try {
			this.webViewClient.onUnhandledKeyEvent(getLockedView(webView, true), var2);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public boolean shouldOverrideKeyEvent(IX5WebViewBase var1, KeyEvent var2) {
		this.webView.a(var1);
		boolean ret = false;
		try {
			ret = this.webViewClient.shouldOverrideKeyEvent(getLockedView(webView, true), var2);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public boolean shouldOverrideUrlLoading(IX5WebViewBase var1, String url) {
		if (url != null && !this.webView.showDebugView(url)) {
			this.webView.a(var1);
			boolean ret = false;
			try {
				ret = this.webViewClient.shouldOverrideUrlLoading(getLockedView(webView, true), url);
			} catch (Exception e) {
				CMN.Log(e);
			}
			unlock();
			return ret;
		} else {
			return true;
		}
	}
	
	public void onTooManyRedirects(IX5WebViewBase var1, Message var2, Message var3) {
		this.webView.a(var1);
		try {
			this.webViewClient.onTooManyRedirects(getLockedView(webView, true), var2, var3);
		} catch (Exception e) {
			e.printStackTrace();
		}
		unlock();
	}
	
	public boolean shouldOverrideUrlLoading(IX5WebViewBase var1, WebResourceRequest webResourceRequest) {
		String url = null;
		if (webResourceRequest != null && webResourceRequest.getUrl() != null) {
			url = webResourceRequest.getUrl().toString();
		}
		
		if (url != null && !this.webView.showDebugView(url)) {
			this.webView.a(var1);
			android.webkit.WebView wv = getLockedView(webView, false);
			this.webView.wrr = webResourceRequest;
			boolean ret = false;
			try {
				ret = this.webViewClient.shouldOverrideUrlLoading(wv, url);
			} catch (Exception e) {
				CMN.Log(e);
			}
			unlock();
			return ret;
		} else {
			return true;
		}
	}
	
	public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, String url) {
		this.webView.a(var1);
		this.webView.wrr = null;
		WebResourceResponse ret = null;
		try {
			ret = new_WebResourceResponse(this.webViewClient.shouldInterceptRequest(getLockedView(webView, true), url));
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, WebResourceRequest webResourceRequest) {
		this.webView.a(var1);
		android.webkit.WebView wv = getLockedView(webView, false);
		this.webView.wrr = webResourceRequest;
		WebResourceResponse ret = null;
		try {
			ret = new_WebResourceResponse(this.webViewClient.shouldInterceptRequest(wv, (String)null));
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public WebResourceResponse shouldInterceptRequest(IX5WebViewBase var1, WebResourceRequest webResourceRequest, Bundle bundle) {
		this.webView.a(var1);
		android.webkit.WebView wv = getLockedView(webView, false);
		this.webView.wrr = webResourceRequest;
		this.webView.bundle = bundle;
		WebResourceResponse ret = null;
		try {
			ret = new_WebResourceResponse(this.webViewClient.shouldInterceptRequest(wv, (String)null));
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public void onReceivedLoginRequest(IX5WebViewBase var1, String var2, String var3, String var4) {
		this.webView.a(var1);
		try {
			this.webViewClient.onReceivedLoginRequest(getLockedView(webView, true), var2, var3, var4);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void a(WebView var1, String var2, Bitmap var3) {
		super.onPageStarted(this.webView.c(), 0, 0, var2, var3);
	}
	
	public void onDetectedBlankScreen(IX5WebViewBase var1, String var2, int var3) {
		this.webView.a(var1);
		//hhh
		// this.webViewClient.onDetectedBlankScreen(var2, var3);
	}
	
	public void onPageFinished(IX5WebViewBase var1, String var2) {
		this.onPageFinished(var1, 0, 0, var2);
	}
	
	public void onPageStarted(IX5WebViewBase var1, String var2, Bitmap var3) {
		this.onPageStarted(var1, 0, 0, var2, var3);
	}
	
	public void countPVContentCacheCallBack(String var1) {
		++this.webView.a;
	}
	
	public void onPageCommitVisible(IX5WebViewBase var1, String var2) {
		this.webView.a(var1);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			//hhh
			try {
				this.webViewClient.onPageCommitVisible(getLockedView(webView, true), var2);
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		unlock();
	}
}
