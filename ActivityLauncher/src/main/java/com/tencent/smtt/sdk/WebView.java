package com.tencent.smtt.sdk;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.net.http.SslCertificate;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Message;
import android.print.PrintDocumentAdapter;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.knziha.polymer.Utils.CMN;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebChromeClientExtension;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebSettingsExtension;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewClientExtension;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import com.tencent.smtt.export.external.extension.proxy.X5ProxyWebViewClientExtension;
import com.tencent.smtt.export.external.interfaces.ClientCertRequest;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.HttpAuthHandler;
import com.tencent.smtt.export.external.interfaces.IX5WebBackForwardList;
import com.tencent.smtt.export.external.interfaces.IX5WebHistoryItem;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.JsPromptResult;
import com.tencent.smtt.export.external.interfaces.JsResult;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.export.external.interfaces.WebResourceError;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.export.external.interfaces.WebResourceResponse;
import com.tencent.smtt.utils.DebugConfigUtil;
import com.tencent.smtt.utils.DebugtbsUtil;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.ReflectionUtils;
import com.tencent.smtt.utils.TbsLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings({ "unused"})
public abstract class WebView extends FrameLayout {
	private final String b;
	public static final String SCHEME_TEL = "tel:";
	public static final String SCHEME_MAILTO = "mailto:";
	public static final String SCHEME_GEO = "geo:0,0?q=";
	private static final Lock c = new ReentrantLock();
	private static OutputStream d = null;
	public static final int GETPVERROR = -1;
	
	
	public WebResourceRequest wrr;
	public WebResourceError wre;
	public HttpAuthHandler httpAuthHandler;
	public Bundle bundle;
	public WebResourceResponse wret;
	public ConsoleMessage consoleMessage;
	public GeolocationPermissionsCallback geolocationPermissionsCallback;
	public JsResult jsResult;
	public JsPromptResult jsPromptResult;
	
	
	
	
	
	private boolean PreferX5;
	protected IX5WebViewBase X5WebView;
	private WebSettings webSettings;
	private Context i;
	private static Context j = null;
	int a;
	private boolean k;
	public WebViewCallbackClient mWebViewCallbackClient;
	public static boolean mWebViewCreated = false;
	private static DebugConfigUtil l = null;
	private static Method m = null;
	private android.webkit.WebViewClient webViewClient;
	private android.webkit.WebChromeClient webChromeClient;
	private static String p = null;
	private final int q;
	private final int r;
	private final int s;
	public static boolean mSysWebviewCreated = false;
	private final String t;
	private final String u;
	private static Paint v = null;
	private static boolean w = true;
	public static int NIGHT_MODE_ALPHA = 153;
	public static final int NORMAL_MODE_ALPHA = 255;
	public static final int NIGHT_MODE_COLOR = -16777216;
	private Object x;
	private OnLongClickListener longClickListener;
	
	public WebView(Context var1, boolean var2) {
		super(var1);
		this.b = "WebView";
		this.PreferX5 = false;
		this.webSettings = null;
		this.i = null;
		this.a = 0;
		this.k = false;
		this.webViewClient = null;
		this.webChromeClient = null;
		this.q = 1;
		this.r = 2;
		this.s = 3;
		this.t = "javascript:document.getElementsByTagName('HEAD').item(0).removeChild(document.getElementById('QQBrowserSDKNightMode'));";
		this.u = "javascript:var style = document.createElement('style');style.type='text/css';style.id='QQBrowserSDKNightMode';style.innerHTML='html,body{background:none !important;background-color: #1d1e2a !important;}html *{background-color: #1d1e2a !important; color:#888888 !important;border-color:#3e4f61 !important;text-shadow:none !important;box-shadow:none !important;}a,a *{border-color:#4c5b99 !important; color:#2d69b3 !important;text-decoration:none !important;}a:visited,a:visited *{color:#a600a6 !important;}a:active,a:active *{color:#5588AA !important;}input,select,textarea,option,button{background-image:none !important;color:#AAAAAA !important;border-color:#4c5b99 !important;}form,div,button,span{background-color:#1d1e2a !important; border-color:#4c5b99 !important;}img{opacity:0.5}';document.getElementsByTagName('HEAD').item(0).appendChild(style);";
		this.x = null;
		this.longClickListener = null;
	}
	
	public WebView(Context var1) throws IOException {
		this(var1, (AttributeSet)null);
	}
	
	public WebView(Context var1, AttributeSet var2) throws IOException {
		this(var1, var2, 0);
	}
	
	public WebView(Context var1, AttributeSet var2, int var3) throws IOException {
		this(var1, var2, var3, false);
	}
	
	/** @deprecated */
	@Deprecated
	public WebView(Context var1, AttributeSet var2, int var3, boolean var4) throws IOException {
		this(var1, var2, var3, (Map)null, var4);
	}
	
	@TargetApi(11)
	public WebView(Context paramContext, AttributeSet var2, int var3, Map<String, Object> var4, boolean var5) throws IOException {
		super(paramContext, var2, var3);
		this.b = "WebView";
		this.PreferX5 = false;
		this.webSettings = null;
		this.i = null;
		this.a = 0;
		this.k = false;
		this.webViewClient = null;
		this.webChromeClient = null;
		this.q = 1;
		this.r = 2;
		this.s = 3;
		this.t = "javascript:document.getElementsByTagName('HEAD').item(0).removeChild(document.getElementById('QQBrowserSDKNightMode'));";
		this.u = "javascript:var style = document.createElement('style');style.type='text/css';style.id='QQBrowserSDKNightMode';style.innerHTML='html,body{background:none !important;background-color: #1d1e2a !important;}html *{background-color: #1d1e2a !important; color:#888888 !important;border-color:#3e4f61 !important;text-shadow:none !important;box-shadow:none !important;}a,a *{border-color:#4c5b99 !important; color:#2d69b3 !important;text-decoration:none !important;}a:visited,a:visited *{color:#a600a6 !important;}a:active,a:active *{color:#5588AA !important;}input,select,textarea,option,button{background-image:none !important;color:#AAAAAA !important;border-color:#4c5b99 !important;}form,div,button,span{background-color:#1d1e2a !important; border-color:#4c5b99 !important;}img{opacity:0.5}';document.getElementsByTagName('HEAD').item(0).appendChild(style);";
		this.x = null;
		this.longClickListener = null;
		mWebViewCreated = true;
		if (TbsShareManager.isThirdPartyApp(paramContext)) {
			TbsLog.setWriteLogJIT(true);
		} else {
			TbsLog.setWriteLogJIT(false);
		}
		
		TbsLog.initIfNeed(paramContext);
		if (paramContext == null) {
			throw new IllegalArgumentException("Invalid context argument");
		} else {
			if (l == null) {
				l = DebugConfigUtil.getInstance(paramContext);
			}

//		if (l.forceUseSystemWebview) {
//		   TbsLog.e("WebView", "sys WebView: debug.conf force syswebview", true);
//		   QbSdk.forceSysWebViewInner(paramContext, "debug.conf force syswebview!");
//		}
			
			this.c(paramContext);
			this.i = paramContext;
			if (paramContext != null) {
				j = paramContext.getApplicationContext();
			}
			
			//if (this.PreferX5 && !QbSdk.forcedSysByInner) {
			this.X5WebView = X5CoreEngine.getInstance().wizard(true).createSDKWebview(paramContext);
			if (this.X5WebView == null || this.X5WebView.getView() == null) {
				throw new IOException("X5 create error!!!");
			}
			
			TbsLog.i("WebView", "X5 WebView Created Success!!");
			//((Toastable_Activity)getContext()).showT("X5 WebView Created Success!!");
			this.X5WebView.getView().setFocusableInTouchMode(true);
			this.a(var2);
			this.addView(this.X5WebView.getView(), new LayoutParams(-1, -1));
			this.X5WebView.setDownloadListener(new DownloadListenerWrap(this, (DownloadListener)null, this.PreferX5));
			this.X5WebView.getX5WebViewExtension().setWebViewClientExtension(new X5ProxyWebViewClientExtension(X5CoreEngine.getInstance().wizard(true).createDefaultX5WebChromeClientExtension()) {
				public void invalidate() {
				}
				
				public void onScrollChanged(int var1, int var2, int var3, int var4) {
					super.onScrollChanged(var1, var2, var3, var4);
					WebView.this.onScrollChanged(var3, var4, var1, var2);
				}
			});
			//}
			
			try {
				if (VERSION.SDK_INT >= 11) {
					this.removeJavascriptInterface("searchBoxJavaBridge_");
					this.removeJavascriptInterface("accessibility");
					this.removeJavascriptInterface("accessibilityTraversal");
				}
			} catch (Throwable var12) {
				var12.printStackTrace();
			}
			
			if (("com.tencent.mobileqq".equals(this.i.getApplicationInfo().packageName)
					|| "com.tencent.mm".equals(this.i.getApplicationInfo().packageName))
					&& SDKEngine.getInstance(true).h() && VERSION.SDK_INT >= 11) {
				this.setLayerType(LAYER_TYPE_SOFTWARE, (Paint)null);
			}
			
			if (this.X5WebView != null) {
				TbsLog.writeLogToDisk();
				if (!TbsShareManager.isThirdPartyApp(paramContext)) {
					int var6 = TbsDownloadConfig.getInstance(paramContext).mPreferences.getInt("tbs_decouplecoreversion", 0);
					if (var6 > 0 && var6 != TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(paramContext) && var6 == TbsInstaller.a().getTbsCoreInstalledVerInNolock(paramContext)) {
						TbsInstaller.a().coreShareCopyToDecouple(paramContext);
					} else {
						TbsLog.i("WebView", "webview construction #1 deCoupleCoreVersion is " + var6 + " getTbsCoreShareDecoupleCoreVersion is " + TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(paramContext) + " getTbsCoreInstalledVerInNolock is " + TbsInstaller.a().getTbsCoreInstalledVerInNolock(paramContext));
					}
				}
			}
			
			QbSdk.continueLoadSo(paramContext);
		}
	}
	
	public PrintDocumentAdapter createPrintDocumentAdapter(String var1) {
		try {
			Object ret = this.X5WebView.createPrintDocumentAdapter(var1);
			if(ret instanceof PrintDocumentAdapter)
				return (PrintDocumentAdapter) ret;
			else
				throw new Exception();
		} catch (Throwable e) {
			CMN.Log(e);
		}
		return null;
	}
	
	public int computeHorizontalScrollOffset() {
		try {
			Method var1;
			Object var2;
			var1 = (Method) ReflectionUtils.a(this.X5WebView.getView(), "computeHorizontalScrollOffset");
			var1.setAccessible(true);
			var2 = var1.invoke(this.X5WebView.getView());
			return (Integer)var2;
		} catch (Exception var3) {
			var3.printStackTrace();
			return -1;
		}
	}
	
	public int computeVerticalScrollOffset() {
		try {
			Method var1;
			Object var2;
			var1 = (Method) ReflectionUtils.a(this.X5WebView.getView(), "computeVerticalScrollOffset");
			var1.setAccessible(true);
			var2 = var1.invoke(this.X5WebView.getView());
			return (Integer)var2;
		} catch (Exception var3) {
			var3.printStackTrace();
			return -1;
		}
	}
	
	public int computeVerticalScrollExtent() {
		try {
			Method var1;
			Object var2;
			var1 = (Method) ReflectionUtils.a(this.X5WebView.getView(), "computeVerticalScrollExtent");
			var1.setAccessible(true);
			var2 = var1.invoke(this.X5WebView.getView());
			return (Integer)var2;
		} catch (Exception var3) {
			var3.printStackTrace();
			return -1;
		}
	}
	
	public int computeHorizontalScrollRange() {
		try {
			Object var4 = ReflectionUtils.a((Object)this.X5WebView.getView(), "computeHorizontalScrollRange", new Class[0]);
			return (Integer)var4;
		} catch (Exception var3) {
			var3.printStackTrace();
			return -1;
		}
	}
	
	public int computeHorizontalScrollExtent() {
		try {
			Method var1;
			Object var2;
			var1 = (Method) ReflectionUtils.a(this.X5WebView.getView(), "computeHorizontalScrollExtent");
			var1.setAccessible(true);
			var2 = var1.invoke(this.X5WebView.getView());
			return (Integer)var2;
		} catch (Exception var3) {
			var3.printStackTrace();
			return -1;
		}
	}
	
	public int computeVerticalScrollRange() {
		try {
			Object var4 = ReflectionUtils.a((Object)this.X5WebView.getView(), "computeVerticalScrollRange", new Class[0]);
			return (Integer)var4;
		} catch (Exception var3) {
			var3.printStackTrace();
			return -1;
		}
	}
	
	private boolean b(Context var1) {
		try {
			String var2 = var1.getPackageName();
			if (var2.indexOf("com.tencent.mobileqq") >= 0) {
				return true;
			}
		} catch (Throwable var3) {
			var3.printStackTrace();
		}
		
		return false;
	}
	
	@TargetApi(11)
	protected void onSizeChanged(int var1, int var2, int var3, int var4) {
		super.onSizeChanged(var1, var2, var3, var4);
		if (VERSION.SDK_INT >= 21 && this.b(this.i) && this.isHardwareAccelerated() && var1 > 0 && var2 > 0 && this.getLayerType() != LAYER_TYPE_HARDWARE) {
		}
		
	}
	
	private void c(Context var1) {
		if (QbSdk.i && TbsShareManager.isThirdPartyApp(var1)) {
			TbsExtensionFunctionManager.getInstance().initTbsBuglyIfNeed(var1);
		}
		
		X5CoreEngine var2 = X5CoreEngine.getInstance();
		var2.init(var1);
		this.PreferX5 = var2.isInCharge();
	}
	
	public void setScrollBarStyle(int var1) {
		this.X5WebView.getView().setScrollBarStyle(var1);
		
	}
	
	public void setHorizontalScrollbarOverlay(boolean var1) {
		this.X5WebView.setHorizontalScrollbarOverlay(var1);
		
	}
	
	public void setVerticalScrollbarOverlay(boolean var1) {
		this.X5WebView.setVerticalScrollbarOverlay(var1);
		
	}
	
	public boolean overlayHorizontalScrollbar() {
		return this.X5WebView.overlayHorizontalScrollbar();
	}
	
	public boolean overlayVerticalScrollbar() {
		return this.X5WebView.overlayVerticalScrollbar();
	}
	
	public boolean requestChildRectangleOnScreen(View var1, Rect var2, boolean var3) {
		View var4 = this.X5WebView.getView();
		return var4 instanceof ViewGroup ? ((ViewGroup)var4).requestChildRectangleOnScreen(var1 == this ? var4 : var1, var2, var3) : false;
	}
	
	public int getWebScrollX() {
		return this.X5WebView.getView().getScrollX() ;
	}
	
	public int getWebScrollY() {
		return this.X5WebView.getView().getScrollY();
	}
	
	public int getVisibleTitleHeight() {
		return this.X5WebView.getVisibleTitleHeight();
	}
	
	public SslCertificate getCertificate() {
		return this.X5WebView.getCertificate();
	}
	
	/** @deprecated */
	@Deprecated
	public void setCertificate(SslCertificate var1) {
		this.X5WebView.setCertificate(var1);
	}
	
	/** @deprecated */
	// safe???
	@Deprecated
	public void savePassword(String var1, String var2, String var3) {
		this.X5WebView.savePassword(var1, var2, var3);
		
	}
	
	public void setHttpAuthUsernamePassword(String var1, String var2, String var3, String var4) {
		this.X5WebView.setHttpAuthUsernamePassword(var1, var2, var3, var4);
	}
	
	public String[] getHttpAuthUsernamePassword(String var1, String var2) {
		return this.X5WebView.getHttpAuthUsernamePassword(var1, var2);
	}
	
	public void tbsWebviewDestroy(boolean var1) {
		String var2;
		if (!this.k && this.a != 0) {
			this.k = true;
			var2 = "";
			String var3 = "";
			String var4 = "";
			if (this.PreferX5) {
				Bundle var5 = this.X5WebView.getX5WebViewExtension().getSdkQBStatisticsInfo();
				if (var5 != null) {
					var2 = var5.getString("guid");
					var3 = var5.getString("qua2");
					var4 = var5.getString("lc");
				}
			}
			
			if ("com.qzone".equals(this.i.getApplicationInfo().packageName)) {
				int var21 = this.e(this.i);
				this.a = var21 == -1 ? this.a : var21;
				this.f(this.i);
			}
			
			boolean var22 = false;
			
			try {
				var22 = this.X5WebView.getX5WebViewExtension().isX5CoreSandboxMode();
			} catch (Throwable var16) {
				TbsLog.w("tbsWebviewDestroy", "exception: " + var16);
			}
			
			com.tencent.smtt.sdk.stat.b.a(this.i, var2, var3, var4, this.a, this.PreferX5, this.h(), var22);
			this.a = 0;
			this.k = false;
		}
		
		if (var1) {
			this.X5WebView.destroy();
		}
		
		TbsLog.i("WebView", "X5 GUID = " + QbSdk.b());
	}
	
	public void destroy() {
		try {
			if ("com.xunmeng.pinduoduo".equals(this.i.getApplicationInfo().packageName)) {
				(new Thread("WebviewDestroy") {
					public void run() {
						WebView.this.tbsWebviewDestroy(false);
					}
				}).start();
				this.X5WebView.destroy();
			} else {
				this.tbsWebviewDestroy(true);
			}
		} catch (Throwable var2) {
			this.tbsWebviewDestroy(true);
		}
		
	}
	
	private long h() {
		long var1 = 0L;
		synchronized(QbSdk.h) {
			if (QbSdk.e) {
				QbSdk.g += System.currentTimeMillis() - QbSdk.f;
				TbsLog.d("sdkreport", "pv report, WebView.getWifiConnectedTime QbSdk.sWifiConnectedTime=" + QbSdk.g);
			}
			
			var1 = QbSdk.g / 1000L;
			QbSdk.g = 0L;
			QbSdk.f = System.currentTimeMillis();
			return var1;
		}
	}
	
	/** @deprecated */
	@Deprecated
	public static void enablePlatformNotifications() {
		if (!X5CoreEngine.getInstance().isInCharge()) {
			ReflectionUtils.a("android.webkit.WebView", "enablePlatformNotifications");
		}
		
	}
	
	/** @deprecated */
	@Deprecated
	public static void disablePlatformNotifications() {
		if (!X5CoreEngine.getInstance().isInCharge()) {
			ReflectionUtils.a("android.webkit.WebView", "disablePlatformNotifications");
		}
		
	}
	
	public void setNetworkAvailable(boolean var1) {
		this.X5WebView.setNetworkAvailable(var1);
		
	}
	
	public android.webkit.WebBackForwardList saveState(Bundle var1) {
		return new BackForwardList(this.X5WebView.saveState(var1));
	}
	
	/** @deprecated */
	@Deprecated
	public boolean savePicture(Bundle var1, File var2) {
		return this.X5WebView.savePicture(var1, var2);
	}
	
	/** @deprecated */
	@Deprecated
	public boolean restorePicture(Bundle var1, File var2) {
		return this.X5WebView.restorePicture(var1, var2);
	}
	
	public android.webkit.WebBackForwardList restoreState(Bundle var1) {
		return new BackForwardList(this.X5WebView.restoreState(var1));
	}
	
	public JSONObject reportInitPerformance(long var1, int var3, long var4, long var6) {
		JSONObject var8 = new JSONObject();
		
		try {
			var8.put("IS_X5", this.PreferX5);
		} catch (JSONException var10) {
			var10.printStackTrace();
		}
		
		return var8;
	}
	
	@TargetApi(8)
	public void loadUrl(String var1, Map<String, String> var2) {
		if (var1 != null && !this.showDebugView(var1)) {
			this.X5WebView.loadUrl(var1, var2);
		}
	}
	
	public void loadUrl(String var1) {
		if (var1 != null && !this.showDebugView(var1)) {
			this.X5WebView.loadUrl(var1);
		}
	}
	
	@SuppressLint({"NewApi"})
	public boolean showDebugView(String url) {
		if (url.startsWith("https://debugtbs.qq.com")) {
			this.getView().setVisibility(INVISIBLE);
			DebugtbsUtil var3 = DebugtbsUtil.getInstance(this.i);
			var3.showPluginView(url, this, this.i, TbsHandlerThread.getInstance().getLooper());
			return true;
		} else if (url.startsWith("https://debugx5.qq.com")) {
			return false;
		} else {
			return false;
		}
	}
	
	@TargetApi(5)
	public void postUrl(String var1, byte[] var2) {
		this.X5WebView.postUrl(var1, var2);
	}
	
	public void loadData(String var1, String var2, String var3) {
		this.X5WebView.loadData(var1, var2, var3);
	}
	
	public void loadDataWithBaseURL(String var1, String var2, String var3, String var4, String var5) {
		this.X5WebView.loadDataWithBaseURL(var1, var2, var3, var4, var5);
	}
	
	@TargetApi(11)
	public void saveWebArchive(String var1) {
		this.X5WebView.saveWebArchive(var1);
	}
	
	@TargetApi(11)
	public void saveWebArchive(String var1, boolean var2, android.webkit.ValueCallback<String> var3) {
		this.X5WebView.saveWebArchive(var1, var2, var3);
	}
	
	public void stopLoading() {
		this.X5WebView.stopLoading();
	}
	
	public static void setWebContentsDebuggingEnabled(boolean var0) {
		X5CoreEngine var1 = X5CoreEngine.getInstance();
		if (null != var1 && var1.isInCharge()) {
			var1.getWVWizardBase().webview_setWebContentsDebuggingEnabled(var0);
		} else if (VERSION.SDK_INT >= 19) {
			try {
				Class var2 = Class.forName("android.webkit.WebView");
				Class[] var3 = new Class[]{Boolean.TYPE};
				m = var2.getDeclaredMethod("setWebContentsDebuggingEnabled", var3);
				if (m != null) {
					m.setAccessible(true);
					m.invoke((Object)null, var0);
				}
			} catch (Exception var4) {
				TbsLog.e("QbSdk", "Exception:" + var4.getStackTrace());
				var4.printStackTrace();
			}
		}
	}
	
	public void reload() {
		this.X5WebView.reload();
	}
	
	public boolean canGoBack() {
		return this.X5WebView.canGoBack();
	}
	
	public void goBack() {
		this.X5WebView.goBack();
	}
	
	public boolean canGoForward() {
		return this.X5WebView.canGoForward();
	}
	
	public void goForward() {
		this.X5WebView.goForward();
	}
	
	public boolean canGoBackOrForward(int var1) {
		return this.X5WebView.canGoBackOrForward(var1);
	}
	
	public void goBackOrForward(int var1) {
		this.X5WebView.goBackOrForward(var1);
	}
	
	public boolean pageUp(boolean var1) {
		return  this.X5WebView.pageUp(var1, -1);
	}
	
	public boolean pageDown(boolean var1) {
		return this.X5WebView.pageDown(var1, -1);
	}
	
	/** @deprecated */
	@Deprecated
	public void clearView() {
		this.X5WebView.clearView();
	}
	
	/** @deprecated */
	@Deprecated
	public Picture capturePicture() {
		return this.X5WebView.capturePicture();
	}
	
	/** @deprecated */
	@Deprecated
	public float getScale() {
		return this.X5WebView.getScale();
	}
	
	public void setInitialScale(int var1) {
		this.X5WebView.setInitialScale(var1);
	}
	
	public void invokeZoomPicker() {
		this.X5WebView.invokeZoomPicker();
	}
	
	public WebView.HitTestResult getHitTestResult() {
		return new WebView.HitTestResult(this.X5WebView.getHitTestResult());
	}
	
	public IX5WebViewBase.HitTestResult getX5HitTestResult() {
		return this.X5WebView.getHitTestResult();
	}
	
	public void requestFocusNodeHref(Message var1) {
		this.X5WebView.requestFocusNodeHref(var1);
		
	}
	
	public void requestImageRef(Message var1) {
		this.X5WebView.requestImageRef(var1);
	}
	
	public String getUrl() {
		return this.X5WebView.getUrl();
	}
	
	@TargetApi(3)
	public String getOriginalUrl() {
		return this.X5WebView.getOriginalUrl();
	}
	
	public String getTitle() {
		return this.X5WebView.getTitle();
	}
	
	public Bitmap getFavicon() {
		return this.X5WebView.getFavicon();
	}
	
	public static PackageInfo getCurrentWebViewPackage() {
		if (!X5CoreEngine.getInstance().isInCharge()) {
			if (VERSION.SDK_INT < 26) {
				return null;
			} else {
				try {
					Object var0 = ReflectionUtils.a("android.webkit.WebView", "getCurrentWebViewPackage");
					return (PackageInfo)var0;
				} catch (Exception var1) {
					var1.printStackTrace();
					return null;
				}
			}
		} else {
			return null;
		}
	}
	
	public void setRendererPriorityPolicy(int var1, boolean var2) {
	}
	
	public int getRendererRequestedPriority() {
		return 0;
	}
	
	public boolean getRendererPriorityWaivedWhenNotVisible() {
		return false;
	}
	
	public android.webkit.WebChromeClient getWebChromeClient() {
		return this.webChromeClient;
	}
	
	public android.webkit.WebViewClient getWebViewClient() {
		return this.webViewClient;
	}
	
	public int getProgress() {
		return this.X5WebView.getProgress();
	}
	
	public int getContentHeight() {
		return this.X5WebView.getContentHeight();
	}
	
	public int getContentWidth() {
		return this.X5WebView.getContentWidth();
	}
	
	public void pauseTimers() {
		this.X5WebView.pauseTimers();
		
	}
	
	public void resumeTimers() {
		this.X5WebView.resumeTimers();
	}
	
	public void onPause() {
		this.X5WebView.onPause();
	}
	
	public void onResume() {
		this.X5WebView.onResume();
	}
	
	/** @deprecated */
	@Deprecated
	public void freeMemory() {
		this.X5WebView.freeMemory();
	}
	
	public void clearCache(boolean var1) {
		this.X5WebView.clearCache(var1);
	}
	
	public void clearFormData() {
		this.X5WebView.clearFormData();
	}
	
	public void clearHistory() {
		this.X5WebView.clearHistory();
	}
	
	public void clearSslPreferences() {
		this.X5WebView.clearSslPreferences();
	}
	
	public android.webkit.WebBackForwardList copyBackForwardList() {
		return new BackForwardList(this.X5WebView.copyBackForwardList());
	}
	
	public static class HistoryItem extends android.webkit.WebHistoryItem{
		final IX5WebHistoryItem a;
		
		public HistoryItem(IX5WebHistoryItem a) {
			this.a = a;
		}
		
		@Override
		public String getUrl() {
			return a.getUrl();
		}
		
		@Override
		public String getOriginalUrl() {
			return a.getOriginalUrl();
		}
		
		@Override
		public String getTitle() {
			return a.getTitle();
		}
		
		@Nullable
		@Override
		public Bitmap getFavicon() {
			return a.getFavicon();
		}
		
		@Override
		protected WebHistoryItem clone() {
			return new HistoryItem(a);
		}
	}
	
	public static class BackForwardList extends android.webkit.WebBackForwardList{
		final IX5WebBackForwardList a;
		
		BackForwardList(IX5WebBackForwardList a) {
			this.a = a;
		}
		
		@Nullable
		@Override
		public WebHistoryItem getCurrentItem() {
			return new HistoryItem(a.getCurrentItem());
		}
		
		@Override
		public int getCurrentIndex() {
			return a.getCurrentIndex();
		}
		
		@Override
		public WebHistoryItem getItemAtIndex(int index) {
			return new HistoryItem(a.getItemAtIndex(index));
		}
		
		@Override
		public int getSize() {
			return a.getSize();
		}
		
		@Override
		protected WebBackForwardList clone() {
			return new BackForwardList(a);
		}
	}
	
	@TargetApi(16)
	public void setFindListener(final IX5WebViewBase.FindListener var1) {
		this.X5WebView.setFindListener(var1);
	}
	
	@TargetApi(3)
	public void findNext(boolean var1) {
		this.X5WebView.findNext(var1);
	}
	
	/** @deprecated */
	@Deprecated
	public int findAll(String var1) {
		return this.X5WebView.findAll(var1);
	}
	
	/** @deprecated */
	@Deprecated
	public static String findAddress(String var0) {
		return !X5CoreEngine.getInstance().isInCharge() ? android.webkit.WebView.findAddress(var0) : null;
	}
	
	@TargetApi(16)
	public void findAllAsync(String var1) {
		this.X5WebView.findAllAsync(var1);
		
	}
	
	public boolean showFindDialog(String var1, boolean var2) {
		return false;
	}
	
	@TargetApi(3)
	public void clearMatches() {
		this.X5WebView.clearMatches();
		
	}
	
	public void documentHasImages(Message var1) {
		this.X5WebView.documentHasImages(var1);
	}
	
	public void setWebViewClient(android.webkit.WebViewClient var1) {
		this.X5WebView.setWebViewClient(var1 == null ? null : new X5WebViewClient(X5CoreEngine.getInstance().wizard(true).createDefaultX5WebViewClient()
				, this
				, var1));
		this.webViewClient = var1;
	}
	
	public void setWebChromeClient(android.webkit.WebChromeClient webChromeClient) {
		this.X5WebView.setWebChromeClient(webChromeClient == null ? null : new X5WebChromeClient(
				X5CoreEngine.getInstance().wizard(true).createDefaultX5WebChromeClient()
				, this
				, webChromeClient));
		this.webChromeClient = webChromeClient;
	}
	
	
	public void setWebViewCallbackClient(WebViewCallbackClient var1) {
		this.mWebViewCallbackClient = var1;
		if (this.PreferX5 && this.getX5WebViewExtension() != null) {
			Bundle var2 = new Bundle();
			var2.putBoolean("flag", true);
			this.getX5WebViewExtension().invokeMiscMethod("setWebViewCallbackClientFlag", var2);
		}
	}
	
	public void customDiskCachePathEnabled(boolean var1, String var2) {
		if (this.PreferX5 && this.getX5WebViewExtension() != null) {
			Bundle var3 = new Bundle();
			var3.putBoolean("enabled", var1);
			var3.putString("path", var2);
			this.getX5WebViewExtension().invokeMiscMethod("customDiskCachePathEnabled", var3);
		}
	}
	
	public void setDownloadListener(final android.webkit.DownloadListener downloadListener) {
		this.X5WebView.setDownloadListener(new DownloadListenerWrap(this, downloadListener, this.PreferX5));
	}
	
	public void addJavascriptInterface(Object var1, String var2) {
		this.X5WebView.addJavascriptInterface(var1, var2);
	}
	
	@TargetApi(11)
	public void removeJavascriptInterface(String var1) {
		this.X5WebView.removeJavascriptInterface(var1);
	}
	
	public android.webkit.WebSettings getSettings() {
		if (this.webSettings != null) {
			return this.webSettings;
		} else {
			return this.webSettings = new WebSettings(this.X5WebView.getSettings());
		}
	}
	
	/** @deprecated */
	@Deprecated
	public static synchronized Object getPluginList() {
		return !X5CoreEngine.getInstance().isInCharge() ? ReflectionUtils.a("android.webkit.WebView", "getPluginList") : null;
	}
	
	/** @deprecated */
	@Deprecated
	public void refreshPlugins(boolean var1) {
		this.X5WebView.refreshPlugins(var1);
	}
	
	/** @deprecated */
	@Deprecated
	public void setMapTrackballToArrowKeys(boolean var1) {
		this.X5WebView.setMapTrackballToArrowKeys(var1);
	}
	
	public void flingScroll(int var1, int var2) {
		this.X5WebView.flingScroll(var1, var2);
	}
	
	/** @deprecated */
	@Deprecated
	public View getZoomControls() {
		return this.X5WebView.getZoomControls();
	}
	
	/** @deprecated */
	@Deprecated
	public boolean canZoomIn() {
		return this.X5WebView.canZoomIn();
	}
	
	public boolean isPrivateBrowsingEnabled() {
		return this.X5WebView.isPrivateBrowsingEnable();
	}
	
	/** @deprecated */
	@Deprecated
	public boolean canZoomOut() {
		return this.X5WebView.canZoomOut();
	}
	
	public boolean zoomIn() {
		return this.X5WebView.zoomIn();
	}
	
	public boolean zoomOut() {
		return this.X5WebView.zoomOut();
	}
	
	public void dumpViewHierarchyWithProperties(BufferedWriter var1, int var2) {
		this.X5WebView.dumpViewHierarchyWithProperties(var1, var2);
	}
	
	public View findHierarchyView(String var1, int var2) {
		return this.X5WebView.findHierarchyView(var1, var2);
	}
	
	public void computeScroll() {
		this.X5WebView.computeScroll();
	}
	
	public void setBackgroundColor(int var1) {
		this.X5WebView.setBackgroundColor(var1);
		super.setBackgroundColor(var1);
	}
	
	public View getView() {
		return this.X5WebView.getView();
	}
	
	protected void a() {
		if (!this.k && this.a != 0) {
			this.k = true;
			String guid = "";
			String qua2 = "";
			String lc = "";
			if (this.PreferX5) {
				Bundle bundle = this.X5WebView.getX5WebViewExtension().getSdkQBStatisticsInfo();
				if (bundle != null) {
					guid = bundle.getString("guid");
					qua2 = bundle.getString("qua2");
					lc = bundle.getString("lc");
				}
			}
			
			if ("com.qzone".equals(this.i.getApplicationInfo().packageName)) {
				int var7 = this.e(this.i);
				this.a = var7 == -1 ? this.a : var7;
				this.f(this.i);
			}
			
			boolean var8 = false;
			
			try {
				var8 = this.X5WebView.getX5WebViewExtension().isX5CoreSandboxMode();
			} catch (Throwable var6) {
				TbsLog.w("tbsOnDetachedFromWindow", "exception: " + var6);
			}
			
			com.tencent.smtt.sdk.stat.b.a(this.i, guid, qua2, lc, this.a, this.PreferX5, this.h(), var8);
			this.a = 0;
			this.k = false;
		}
		super.onDetachedFromWindow();
	}
	
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		try {
			if ("com.xunmeng.pinduoduo".equals(this.i.getApplicationInfo().packageName)) {
				(new Thread("onDetachedFromWindow") {
					public void run() {
						try {
							WebView.this.a();
						} catch (Exception var2) {
							var2.printStackTrace();
						}
						
					}
				}).start();
			} else {
				this.a();
			}
		} catch (Throwable var2) {
			this.a();
		}
	}
	
	protected void onVisibilityChanged(View var1, int var2) {
		if (this.i == null) {
			super.onVisibilityChanged(var1, var2);
		} else {
			if (p == null) {
				ApplicationInfo var3 = this.i.getApplicationInfo();
				p = var3.packageName;
			}
			
			if (p == null || !p.equals("com.tencent.mm") && !p.equals("com.tencent.mobileqq")) {
				if (var2 != 0 && !this.k && this.a != 0) {
					this.k = true;
					String var9 = "";
					String var4 = "";
					String var5 = "";
					if (this.PreferX5) {
						Bundle var6 = this.X5WebView.getX5WebViewExtension().getSdkQBStatisticsInfo();
						if (var6 != null) {
							var9 = var6.getString("guid");
							var4 = var6.getString("qua2");
							var5 = var6.getString("lc");
						}
					}
					
					if ("com.qzone".equals(this.i.getApplicationInfo().packageName)) {
						int var10 = this.e(this.i);
						this.a = var10 == -1 ? this.a : var10;
						this.f(this.i);
					}
					
					boolean var11 = false;
					
					try {
						var11 = this.X5WebView.getX5WebViewExtension().isX5CoreSandboxMode();
					} catch (Throwable var8) {
						TbsLog.w("onVisibilityChanged", "exception: " + var8);
					}
					
					com.tencent.smtt.sdk.stat.b.a(this.i, var9, var4, var5, this.a, this.PreferX5, this.h(), var11);
					this.a = 0;
					this.k = false;
				}
				
				super.onVisibilityChanged(var1, var2);
			} else {
				super.onVisibilityChanged(var1, var2);
			}
		}
	}
	
	public IX5WebViewExtension getX5WebViewExtension() {
		return !this.PreferX5 ? null : this.X5WebView.getX5WebViewExtension();
	}
	
	public IX5WebSettingsExtension getSettingsExtension() {
		return !this.PreferX5 ? null : this.X5WebView.getX5WebViewExtension().getSettingsExtension();
	}
	
	public void setWebViewClientExtension(IX5WebViewClientExtension var1) {
		if (this.PreferX5) {
			this.X5WebView.getX5WebViewExtension().setWebViewClientExtension(var1);
		}
	}
	
	public void setWebChromeClientExtension(IX5WebChromeClientExtension var1) {
		if (this.PreferX5) {
			this.X5WebView.getX5WebViewExtension().setWebChromeClientExtension(var1);
		}
	}
	
	public IX5WebChromeClientExtension getWebChromeClientExtension() {
		return !this.PreferX5 ? null : this.X5WebView.getX5WebViewExtension().getWebChromeClientExtension();
	}
	
	public IX5WebViewClientExtension getWebViewClientExtension() {
		return !this.PreferX5 ? null : this.X5WebView.getX5WebViewExtension().getWebViewClientExtension();
	}
	
	public void evaluateJavascript(String var1, android.webkit.ValueCallback<String> var2) {
		try {
			View var3 = this.X5WebView.getView();
			Method var4 = ReflectionUtils.a(var3, "evaluateJavascript", String.class, android.webkit.ValueCallback.class);
			var4.setAccessible(true);
			var4.invoke(this.X5WebView.getView(), var1, var2);
		} catch (Exception var7) {
			var7.printStackTrace();
			this.loadUrl(var1);
		}
	}
	
	public static int getTbsCoreVersion(Context var0) {
		return QbSdk.getTbsVersion(var0);
	}
	
	public static int getTbsSDKVersion(Context var0) {
		return 43967;
	}
	
	public boolean setVideoFullScreen(Context var1, boolean var2) {
		String var3 = var1.getApplicationInfo().processName;
		if (var3.contains("com.tencent.android.qqdownloader") && this.X5WebView != null) {
			Bundle var4 = new Bundle();
			if (var2) {
				var4.putInt("DefaultVideoScreen", 2);
			} else {
				var4.putInt("DefaultVideoScreen", 1);
			}
			
			this.X5WebView.getX5WebViewExtension().invokeMiscMethod("setVideoParams", var4);
			return true;
		} else {
			return false;
		}
	}
	
	void a(android.webkit.WebView var1) {
	}
	
	android.webkit.WebView b() {
		return null;
	}
	
	void a(IX5WebViewBase var1) {
		this.X5WebView = var1;
	}
	
	IX5WebViewBase c() {
		return this.X5WebView;
	}
	
	public void setOnTouchListener(OnTouchListener var1) {
		this.getView().setOnTouchListener(var1);
	}
	
	private void a(AttributeSet var1) {
		try {
			if (var1 != null) {
				int var2 = var1.getAttributeCount();
				for(int var3 = 0; var3 < var2; ++var3) {
					if (var1.getAttributeName(var3).equalsIgnoreCase("scrollbars")) {
						int[] var4 = this.getResources().getIntArray(16842974);
						int var5 = var1.getAttributeIntValue(var3, -1);
						if (var5 == var4[1]) {
							this.X5WebView.getView().setVerticalScrollBarEnabled(false);
							this.X5WebView.getView().setHorizontalScrollBarEnabled(false);
						} else if (var5 == var4[2]) {
							this.X5WebView.getView().setVerticalScrollBarEnabled(false);
						} else if (var5 == var4[3]) {
							this.X5WebView.getView().setHorizontalScrollBarEnabled(false);
						}
					}
				}
			}
		} catch (Exception var6) {
			var6.printStackTrace();
		}
		
	}
	
	private Context d(Context var1) {
		return VERSION.SDK_INT >= 21 && VERSION.SDK_INT <= 22 ? var1.createConfigurationContext(new Configuration()) : var1;
	}
	
	public void switchNightMode(boolean var1) {
		if (var1 != w) {
			w = var1;
			if (w) {
				TbsLog.e("QB_SDK", "deleteNightMode");
				this.loadUrl("javascript:document.getElementsByTagName('HEAD').item(0).removeChild(document.getElementById('QQBrowserSDKNightMode'));");
			} else {
				TbsLog.e("QB_SDK", "nightMode");
				this.loadUrl("javascript:var style = document.createElement('style');style.type='text/css';style.id='QQBrowserSDKNightMode';style.innerHTML='html,body{background:none !important;background-color: #1d1e2a !important;}html *{background-color: #1d1e2a !important; color:#888888 !important;border-color:#3e4f61 !important;text-shadow:none !important;box-shadow:none !important;}a,a *{border-color:#4c5b99 !important; color:#2d69b3 !important;text-decoration:none !important;}a:visited,a:visited *{color:#a600a6 !important;}a:active,a:active *{color:#5588AA !important;}input,select,textarea,option,button{background-image:none !important;color:#AAAAAA !important;border-color:#4c5b99 !important;}form,div,button,span{background-color:#1d1e2a !important; border-color:#4c5b99 !important;}img{opacity:0.5}';document.getElementsByTagName('HEAD').item(0).appendChild(style);");
			}
			
		}
	}
	
	public void switchToNightMode() {
		TbsLog.e("QB_SDK", "switchToNightMode 01");
		if (!w) {
			TbsLog.e("QB_SDK", "switchToNightMode");
			this.loadUrl("javascript:var style = document.createElement('style');style.type='text/css';style.id='QQBrowserSDKNightMode';style.innerHTML='html,body{background:none !important;background-color: #1d1e2a !important;}html *{background-color: #1d1e2a !important; color:#888888 !important;border-color:#3e4f61 !important;text-shadow:none !important;box-shadow:none !important;}a,a *{border-color:#4c5b99 !important; color:#2d69b3 !important;text-decoration:none !important;}a:visited,a:visited *{color:#a600a6 !important;}a:active,a:active *{color:#5588AA !important;}input,select,textarea,option,button{background-image:none !important;color:#AAAAAA !important;border-color:#4c5b99 !important;}form,div,button,span{background-color:#1d1e2a !important; border-color:#4c5b99 !important;}img{opacity:0.5}';document.getElementsByTagName('HEAD').item(0).appendChild(style);");
		}
	}
	
	public static synchronized void setSysDayOrNight(boolean var0) {
		if (var0 != w) {
			w = var0;
			if (v == null) {
				v = new Paint();
				v.setColor(-16777216);
			}
			
			if (!var0) {
				if (v.getAlpha() != NIGHT_MODE_ALPHA) {
					v.setAlpha(NIGHT_MODE_ALPHA);
				}
			} else if (v.getAlpha() != 255) {
				v.setAlpha(255);
			}
			
		}
	}
	
	public void setDayOrNight(boolean var1) {
		try {
			if (this.PreferX5) {
				this.getSettingsExtension().setDayOrNight(var1);
			}
			
			setSysDayOrNight(var1);
			this.getView().postInvalidate();
		} catch (Throwable var3) {
			var3.printStackTrace();
		}
	}
	
	public void setARModeEnable(boolean var1) {
		try {
			if (this.PreferX5) {
				this.getSettingsExtension().setARModeEnable(var1);
			}
		} catch (Throwable var3) {
			var3.printStackTrace();
		}
	}
	
	public boolean isDayMode() {
		return w;
	}
	
	public int getSysNightModeAlpha() {
		return NIGHT_MODE_ALPHA;
	}
	
	public void setSysNightModeAlpha(int var1) {
		NIGHT_MODE_ALPHA = var1;
	}
	
//	public boolean onLongClick(View var1) {
//		if (this.longClickListener != null) {
//			return !this.longClickListener.onLongClick(var1) ? this.a(var1) : true;
//		} else {
//			return this.a(var1);
//		}
//	}
	
	public void setOnLongClickListener(OnLongClickListener listener) {
//		View var2 = this.X5WebView.getView();
//
//		try {
//			if (this.x == null) {
//				Method var3 = (Method) ReflectionUtils.a(var2, "getListenerInfo");
//				var3.setAccessible(true);
//				Object var4 = var3.invoke(var2, (Object[])null);
//				Field var5 = var4.getClass().getDeclaredField("mOnLongClickListener");
//				var5.setAccessible(true);
//				this.x = var5.get(var4);
//			}
//		} catch (Throwable var6) {
//			return;
//		}
//
//		this.y = var1;
		this.getView().setOnLongClickListener(listener==null?null:v -> listener.onLongClick(WebView.this));
	}
	
	private int e(Context var1) {
		FileOutputStream var2 = FileHelper.getLockFile(var1, true, "tbslock.txt");
		FileLock var3 = null;
		if (var2 != null) {
			var3 = FileHelper.lockStream(var1, var2);
			if (var3 == null) {
				return -1;
			} else {
				boolean var4 = c.tryLock();
				if (var4) {
					FileInputStream var5 = null;
					
					try {
						File var6 = QbSdk.getTbsFolderDir(var1);
						File var25 = new File(var6 + File.separator + "core_private", "pv.db");
						if (var25 != null && var25.exists()) {
							Properties var26 = new Properties();
							var5 = new FileInputStream(var25);
							var26.load(var5);
							var5.close();
							String var9 = var26.getProperty("PV");
							if (var9 == null) {
								byte var27 = -1;
								return var27;
							} else {
								int var10 = Integer.parseInt(var9);
								int var11 = var10;
								return var11;
							}
						} else {
							byte var8 = -1;
							return var8;
						}
					} catch (Exception var23) {
						TbsLog.e("getTbsCorePV", "TbsInstaller--getTbsCorePV Exception=" + var23.toString());
						byte var7 = -1;
						return var7;
					} finally {
						if (var5 != null) {
							try {
								var5.close();
							} catch (IOException var22) {
								TbsLog.e("getTbsCorePV", "TbsInstaller--getTbsCorePV IOException=" + var22.toString());
							}
						}
						
						c.unlock();
						FileHelper.releaseFileLock(var3, var2);
					}
				} else {
					FileHelper.releaseFileLock(var3, var2);
					return -1;
				}
			}
		} else {
			return -1;
		}
	}
	
	void a(Context var1) {
		int var3 = this.e(var1);
		String var2;
		if (var3 != -1) {
			++var3;
			var2 = "PV=" + String.valueOf(var3);
		} else {
			var2 = "PV=1";
		}
		
		File var4 = QbSdk.getTbsFolderDir(var1);
		File var5 = new File(var4 + File.separator + "core_private", "pv.db");
		if (var5 != null) {
			try {
				try {
					var5.getParentFile().mkdirs();
					if (!var5.isFile() || !var5.exists()) {
						var5.createNewFile();
					}
					
					d = new FileOutputStream(var5, false);
					d.write(var2.getBytes());
				} finally {
					if (d != null) {
						d.flush();
					}
					
				}
			} catch (Throwable var10) {
			}
			
		}
	}
	
	private void f(Context var1) {
		try {
			File var2 = QbSdk.getTbsFolderDir(var1);
			File var3 = new File(var2 + File.separator + "core_private", "pv.db");
			if (var3 == null || !var3.exists()) {
				return;
			}
			
			var3.delete();
		} catch (Exception var4) {
			TbsLog.i("getTbsCorePV", "TbsInstaller--getTbsCorePV Exception=" + var4.toString());
		}
	}
	
	private boolean a(View var1) {
		if (this.i != null && getTbsCoreVersion(this.i) > 36200) {
			return false;
		} else {
			Object var2 = ReflectionUtils.invokeInstance(this.x, "onLongClick", new Class[]{View.class}, var1);
			return var2 != null ? (Boolean)var2 : false;
		}
	}
	
	public void addView(View var1) {
		View var2 = this.X5WebView.getView();
		
		try {
			Method var3 = ReflectionUtils.a(var2, "addView", View.class);
			var3.setAccessible(true);
			var3.invoke(var2, var1);
		} catch (Throwable var4) {
			return;
		}
	}
	
	public void removeView(View var1) {
		View var2 = this.X5WebView.getView();
		
		try {
			Method var3 = ReflectionUtils.a(var2, "removeView", View.class);
			var3.setAccessible(true);
			var3.invoke(var2, var1);
		} catch (Throwable var4) {
			return;
		}
	}
	
	public static String getCrashExtraMessage(Context var0) {
		if (var0 == null) {
			return "";
		} else {
			String var1 = "tbs_core_version:" + QbSdk.getTbsVersionForCrash(var0) + ";" + "tbs_sdk_version:" + '\uabbf' + ";";
			boolean var2 = false;
			if ("com.tencent.mm".equals(var0.getApplicationInfo().packageName)) {
				var2 = true;
				
				try {
					Class var3 = Class.forName("de.robv.android.xposed.XposedBridge");
				} catch (ClassNotFoundException var5) {
					var2 = false;
				} catch (Throwable var6) {
					var6.printStackTrace();
					var2 = false;
				}
			}
			
			if (var2) {
				return var1 + "isXposed=true;";
			} else {
				StringBuilder var7 = new StringBuilder();
				var7.append(SDKEngine.getInstance(true).e());
				var7.append("\n");
				var7.append(var1);
				if (!TbsShareManager.isThirdPartyApp(var0) && QbSdk.n != null && QbSdk.n.containsKey("weapp_id") && QbSdk.n.containsKey("weapp_name")) {
					String var4 = "weapp_id:" + QbSdk.n.get("weapp_id") + ";" + "weapp_name" + ":" + QbSdk.n.get("weapp_name") + ";";
					var7.append("\n");
					var7.append(var4);
				}
				
				return var7.length() > 8192 ? var7.substring(var7.length() - 8192) : var7.toString();
			}
		}
	}
	
	public static boolean getTbsNeedReboot() {
		d();
		boolean var0 = SDKEngine.getInstance(true).f();
		return var0;
	}
	
	static void d() {
		Runnable var0 = new Runnable() {
			public void run() {
				if (WebView.j == null) {
					TbsLog.d("TbsNeedReboot", "WebView.updateNeeeRebootStatus--mAppContext == null");
				} else {
					SDKEngine var1 = SDKEngine.getInstance(true);
					if (SDKEngine.b) {
						TbsLog.d("TbsNeedReboot", "WebView.updateNeeeRebootStatus--needReboot = true");
					} else {
						TbsCoreInstallPropertiesHelper var2 = TbsCoreInstallPropertiesHelper.getInstance(WebView.j);
						int var3 = var2.getInstallStatus();
						TbsLog.d("TbsNeedReboot", "WebView.updateNeeeRebootStatus--installStatus = " + var3);
						int var4;
						if (var3 == 2) {
							TbsLog.d("TbsNeedReboot", "WebView.updateNeeeRebootStatus--install setTbsNeedReboot true");
							var4 = var2.getInstallCoreVer();
							var1.a(String.valueOf(var4));
							var1.b(true);
						} else {
							var4 = var2.getIntProperty_DefNeg1("copy_status");
							TbsLog.d("TbsNeedReboot", "WebView.updateNeeeRebootStatus--copyStatus = " + var4);
							if (var4 == 1) {
								TbsLog.d("TbsNeedReboot", "WebView.updateNeeeRebootStatus--copy setTbsNeedReboot true");
								int var5 = var2.getIntProperty("copy_core_ver");
								var1.a(String.valueOf(var5));
								var1.b(true);
							} else {
								if (!X5CoreEngine.getInstance().isInCharge() && (var3 == 3 || var4 == 3)) {
									TbsLog.d("TbsNeedReboot", "WebView.updateNeeeRebootStatus--setTbsNeedReboot true");
									var1.a(String.valueOf(SDKEngine.d()));
									var1.b(true);
								}
								
							}
						}
					}
				}
			}
		};
		
		try {
			(new Thread(var0)).start();
		} catch (Throwable var2) {
			TbsLog.e("webview", "updateRebootStatus excpetion: " + var2);
		}
	}
	
	public void super_onScrollChanged(int var1, int var2, int var3, int var4) {
		View var5 = this.X5WebView.getView();
		
		try {
			ReflectionUtils.invokeInstance((Object)var5, "super_onScrollChanged", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE}, var1, var2, var3, var4);
		} catch (Throwable var7) {
			var7.printStackTrace();
		}
	}
	
	public boolean super_overScrollBy(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9) {
		View var10 = this.X5WebView.getView();
		
		try {
			Object var11 = ReflectionUtils.invokeInstance((Object)var10, "super_overScrollBy", new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE}, var1, var2, var3, var4, var5, var6, var7, var8, var9);
			return var11 == null ? false : (Boolean)var11;
		} catch (Throwable var12) {
			return false;
		}
	}
	
	public void super_onOverScrolled(int var1, int var2, boolean var3, boolean var4) {
		View var5 = this.X5WebView.getView();
		
		try {
			ReflectionUtils.invokeInstance((Object)var5, "super_onOverScrolled", new Class[]{Integer.TYPE, Integer.TYPE, Boolean.TYPE, Boolean.TYPE}, var1, var2, var3, var4);
		} catch (Throwable var7) {
			var7.printStackTrace();
		}
	}
	
	public boolean super_dispatchTouchEvent(MotionEvent var1) {
		View var2 = this.X5WebView.getView();
		
		try {
			Object var3 = ReflectionUtils.invokeInstance((Object)var2, "super_dispatchTouchEvent", new Class[]{MotionEvent.class}, var1);
			return var3 == null ? false : (Boolean)var3;
		} catch (Throwable var4) {
			return false;
		}
	}
	
	public boolean super_onInterceptTouchEvent(MotionEvent var1) {
		View var2 = this.X5WebView.getView();
		
		try {
			Object var3 = ReflectionUtils.invokeInstance((Object)var2, "super_onInterceptTouchEvent", new Class[]{MotionEvent.class}, var1);
			return var3 == null ? false : (Boolean)var3;
		} catch (Throwable var4) {
			return false;
		}
	}
	
	public boolean super_onTouchEvent(MotionEvent var1) {
		View var2 = this.X5WebView.getView();
		
		try {
			Object var3 = ReflectionUtils.invokeInstance((Object)var2, "super_onTouchEvent", new Class[]{MotionEvent.class}, var1);
			return var3 == null ? false : (Boolean)var3;
		} catch (Throwable var4) {
			return false;
		}
	}
	
	public void super_computeScroll() {
		View var1 = this.X5WebView.getView();
		
		try {
			ReflectionUtils.a((Object)var1, "super_computeScroll");
		} catch (Throwable var3) {
			var3.printStackTrace();
		}
	}
	
	public int getScrollBarDefaultDelayBeforeFade() {
		return this.getView() == null ? 0 : this.getView().getScrollBarDefaultDelayBeforeFade();
	}
	
	public int getScrollBarFadeDuration() {
		return this.getView() == null ? 0 : this.getView().getScrollBarFadeDuration();
	}
	
	public int getScrollBarSize() {
		return this.getView() == null ? 0 : this.getView().getScrollBarSize();
	}
	
	public int getScrollBarStyle() {
		return this.getView() == null ? 0 : this.getView().getScrollBarStyle();
	}
	
	public void setVisibility(int var1) {
		super.setVisibility(var1);
		if (this.getView() != null) {
			this.getView().setVisibility(var1);
		}
	}
	
	public static void setDataDirectorySuffix(String var0) {
		if (VERSION.SDK_INT >= 28) {
			try {
				Class var1 = Class.forName("android.webkit.WebView");
				ReflectionUtils.a(var1, "setDataDirectorySuffix", new Class[]{String.class}, var0);
			} catch (Exception var2) {
				var2.printStackTrace();
			}
		}
		
		HashMap var3 = new HashMap();
		var3.put("data_directory_suffix", var0);
		QbSdk.initTbsSettings(var3);
	}
	
//	public static class HitTestResult1 extends android.webkit.WebView.HitTestResult{
//
//	}
	
	public static class HitTestResult {
		public static final int UNKNOWN_TYPE = 0;
		/** @deprecated */
		@Deprecated
		public static final int ANCHOR_TYPE = 1;
		public static final int PHONE_TYPE = 2;
		public static final int GEO_TYPE = 3;
		public static final int EMAIL_TYPE = 4;
		public static final int IMAGE_TYPE = 5;
		/** @deprecated */
		@Deprecated
		public static final int IMAGE_ANCHOR_TYPE = 6;
		public static final int SRC_ANCHOR_TYPE = 7;
		public static final int SRC_IMAGE_ANCHOR_TYPE = 8;
		public static final int EDIT_TEXT_TYPE = 9;
		private IX5WebViewBase.HitTestResult a;
		private android.webkit.WebView.HitTestResult b = null;
		
		public HitTestResult() {
			this.a = null;
			this.b = null;
		}
		
		public HitTestResult(IX5WebViewBase.HitTestResult var1) {
			this.a = var1;
			this.b = null;
		}
		
		public HitTestResult(android.webkit.WebView.HitTestResult var1) {
			this.a = null;
			this.b = var1;
		}
		
		public int getType() {
			int var1 = 0;
			if (this.a != null) {
				var1 = this.a.getType();
			} else if (this.b != null) {
				var1 = this.b.getType();
			}
			
			return var1;
		}
		
		public String getExtra() {
			String var1 = "";
			if (this.a != null) {
				var1 = this.a.getExtra();
			} else if (this.b != null) {
				var1 = this.b.getExtra();
			}
			
			return var1;
		}
	}
	
	public class WebViewTransport {
		private WebView b;
		
		public synchronized void setWebView(WebView var1) {
			this.b = var1;
		}
		
		public synchronized WebView getWebView() {
			return this.b;
		}
	}
}