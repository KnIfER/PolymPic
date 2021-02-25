package com.tencent.smtt.sdk;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.knziha.polymer.Utils.CMN;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.QuotaUpdater;
import com.tencent.smtt.export.external.proxy.X5ProxyWebChromeClient;

import static org.xwalk.core.Utils.getLockedView;
import static org.xwalk.core.Utils.unlock;

class X5WebChromeClient extends X5ProxyWebChromeClient {
	private WebView webView;
	private android.webkit.WebChromeClient webChromeClient;
	
	public X5WebChromeClient(IX5WebChromeClient var1, WebView var2, android.webkit.WebChromeClient var3) {
		super(var1);
		webView = var2;
		webChromeClient = var3;
	}
	
	public void getVisitedHistory(android.webkit.ValueCallback<String[]> var1) {
	}
	
	public void onExceededDatabaseQuota(String var1, String var2, long var3, long var5, long var7, QuotaUpdater var9) {
		webChromeClient.onExceededDatabaseQuota(var1, var2, var3, var5, var7, new X5WebChromeClient.a(var9));
	}
	
	public Bitmap getDefaultVideoPoster() {
		return webChromeClient.getDefaultVideoPoster();
	}
	
	public void onCloseWindow(IX5WebViewBase var1) {
		webView.a(var1);
		try {
			webChromeClient.onCloseWindow(getLockedView(webView, true));
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onConsoleMessage(String var1, int var2, String var3) {
	}
	
	public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
		//CMN.Log("onConsoleMessageA::", consoleMessage.message());
//		android.webkit.WebView wv = getLockedView(webView, );
//		webView.consoleMessage = consoleMessage;
		boolean ret = webChromeClient.onConsoleMessage(new android.webkit.ConsoleMessage(consoleMessage.message()
				, consoleMessage.sourceId()
				, consoleMessage.lineNumber()
				, android.webkit.ConsoleMessage.MessageLevel.valueOf(consoleMessage.messageLevel().name())));
//		unlock();
		return ret;
	}
	
	public boolean onCreateWindow(IX5WebViewBase var1, boolean var2, boolean var3, final Message var4) {
		final WebView.WebViewTransport var5 = webView.new WebViewTransport();
		Handler var6 = var4.getTarget();
		Message var7 = Message.obtain(var6, new Runnable() {
			public void run() {
				WebView var1 = var5.getWebView();
				if (var1 != null) {
					IX5WebViewBase.WebViewTransport var2 = (IX5WebViewBase.WebViewTransport)var4.obj;
					var2.setWebView(var1.c());
				}
				
				var4.sendToTarget();
			}
		});
		var7.obj = var5;
		boolean ret = false;
		try {
			ret = webChromeClient.onCreateWindow(getLockedView(webView, true), var2, var3, var7);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public void onGeolocationPermissionsHidePrompt() {
		webChromeClient.onGeolocationPermissionsHidePrompt();
	}
	
	public void onGeolocationPermissionsShowPrompt(String var1, GeolocationPermissionsCallback geolocationPermissionsCallback) {
		webView.geolocationPermissionsCallback = geolocationPermissionsCallback;
		webChromeClient.onGeolocationPermissionsShowPrompt(var1, null);
	}
	
	public void onHideCustomView() {
		webChromeClient.onHideCustomView();
	}
	
	public boolean onJsAlert(IX5WebViewBase var1, String var2, String var3, JsResult jsResult) {
		webView.a(var1);
		android.webkit.WebView wv = getLockedView(webView, false);
		webView.jsResult = jsResult;
		boolean ret = false;
		try {
			ret = webChromeClient.onJsAlert(wv, var2, var3, null);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public boolean onJsConfirm(IX5WebViewBase var1, String var2, String var3, JsResult jsResult) {
		webView.a(var1);
		android.webkit.WebView wv = getLockedView(webView, false);
		webView.jsResult = jsResult;
		
		boolean ret = false;
		try {
			ret = webChromeClient.onJsConfirm(wv, var2, var3, null);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public boolean onJsPrompt(IX5WebViewBase var1, String var2, String var3, String var4, JsPromptResult jsPromptResult) {
		webView.a(var1);
		
		android.webkit.WebView wv = getLockedView(webView, false);
		webView.jsPromptResult = jsPromptResult;
		
		boolean ret = false;
		try {
			ret = webChromeClient.onJsPrompt(wv, var2, var3, var4, null);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public boolean onJsBeforeUnload(IX5WebViewBase var1, String var2, String var3, JsResult jsResult) {
		webView.a(var1);
		android.webkit.WebView wv = getLockedView(webView, false);
		webView.jsResult = jsResult;
		
		boolean ret = false;
		try {
			ret = webChromeClient.onJsBeforeUnload(wv, var2, var3, null);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
		return ret;
	}
	
	public boolean onJsTimeout() {
		return webChromeClient.onJsTimeout();
	}
	
	public void onProgressChanged(IX5WebViewBase var1, int var2) {
		webView.a(var1);
		try {
			webChromeClient.onProgressChanged(getLockedView(webView, true), var2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		unlock();
	}
	
	public void onReachedMaxAppCacheSize(long var1, long var3, QuotaUpdater var5) {
		webChromeClient.onReachedMaxAppCacheSize(var1, var3, new X5WebChromeClient.a(var5));
	}
	
	public void onReceivedIcon(IX5WebViewBase var1, Bitmap var2) {
		webView.a(var1);
		try {
			webChromeClient.onReceivedIcon(getLockedView(webView, true), var2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		unlock();
	}
	
	public void onReceivedTouchIconUrl(IX5WebViewBase var1, String var2, boolean var3) {
		webView.a(var1);
		try {
			webChromeClient.onReceivedTouchIconUrl(getLockedView(webView, true), var2, var3);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onReceivedTitle(IX5WebViewBase var1, String var2) {
		webView.a(var1);
		try {
			webChromeClient.onReceivedTitle(getLockedView(webView, true), var2);
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onRequestFocus(IX5WebViewBase var1) {
		webView.a(var1);
		try {
			webChromeClient.onRequestFocus(getLockedView(webView, true));
		} catch (Exception e) {
			CMN.Log(e);
		}
		unlock();
	}
	
	public void onShowCustomView(View var1, IX5WebChromeClient.CustomViewCallback var2) {
		webChromeClient.onShowCustomView(var1, var2);
	}
	
	public void onShowCustomView(View var1, int var2, IX5WebChromeClient.CustomViewCallback var3) {
		webChromeClient.onShowCustomView(var1, var2, var3);
	}
	
	public void openFileChooser(final android.webkit.ValueCallback<Uri[]> var1, String var2, String var3, boolean var4) {
//      webChromeClient.openFileChooser(new ValueCallback<Uri>() {
//         public void a(Uri var1x) {
//            var1.onReceiveValue(new Uri[]{var1x});
//         }
//
//         // $FF: synthetic method
//         public void onReceiveValue(Uri var1x) {
//            a((Uri)var1x);
//         }
//      }, var2, var3);
		//hhh
	}
	
	public boolean onShowFileChooser(IX5WebViewBase var1, final android.webkit.ValueCallback<Uri[]> var2, final IX5WebChromeClient.FileChooserParams var3) {
//      ValueCallback var5 = new ValueCallback<Uri[]>() {
//         public void a(Uri[] var1) {
//            var2.onReceiveValue(var1);
//         }
//
//         // $FF: synthetic method
//         public void onReceiveValue(Uri[] var1) {
//            a((Uri[])var1);
//         }
//      };
//      WebChromeClient.FileChooserParams var6 = new WebChromeClient.FileChooserParams() {
//         public int getMode() {
//            return var3.getMode();
//         }
//
//         public String[] getAcceptTypes() {
//            return var3.getAcceptTypes();
//         }
//
//         public boolean isCaptureEnabled() {
//            return var3.isCaptureEnabled();
//         }
//
//         public CharSequence getTitle() {
//            return var3.getTitle();
//         }
//
//         public String getFilenameHint() {
//            return var3.getFilenameHint();
//         }
//
//         public Intent createIntent() {
//            return var3.createIntent();
//         }
//      };
//      webView.a(var1);
//      return webChromeClient.onShowFileChooser(webView, var5, var6);
		return false;
	}
	
	class a implements QuotaUpdater {
		QuotaUpdater a;
		
		a(QuotaUpdater var2) {
			a = var2;
		}
		
		public void updateQuota(long var1) {
			a.updateQuota(var1);
		}
	}
}
