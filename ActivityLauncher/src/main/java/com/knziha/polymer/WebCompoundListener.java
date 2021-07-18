package com.knziha.polymer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView.OnScrollChangedListener;

import com.knziha.filepicker.utils.ExtensionHelper;
import com.knziha.polymer.Utils.AutoCloseNetStream;
import com.knziha.polymer.Utils.BufferedReader;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.IISTri;
import com.knziha.polymer.Utils.RgxPlc;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebResourceResponseCompat;
import com.knziha.polymer.browser.webkit.WebViewHelper;
import com.knziha.polymer.toolkits.MyX509TrustManager;
import com.knziha.polymer.webstorage.SubStringKey;
import com.knziha.polymer.webstorage.WebOptions;
import com.knziha.polymer.widgets.AppToastManager;
import com.knziha.polymer.widgets.DialogVanishing;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import okhttp3.Cache;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.knziha.polymer.HttpRequestUtil.DO_NOT_VERIFY;
import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_DOMAIN;
import static com.knziha.polymer.webstorage.WebOptions.BackendSettings;
import static com.knziha.polymer.webstorage.WebOptions.TextSettings;
import static org.xwalk.core.Utils.getTag;
import static org.xwalk.core.Utils.unlock;

/** WebView Compound Listener ：两大网页客户端监听器及Javascript桥，全局一个实例。 */
public class WebCompoundListener extends WebViewClient implements DownloadListener, OnScrollChangedListener {
	public final static Pattern requestPattern = Pattern.compile("\\?.*$");
	public final static Pattern httpPattern = Pattern.compile("(https?://.*)", Pattern.CASE_INSENSITIVE);
	public final static Pattern timePattern = Pattern.compile("t=([0-9]{8,15})", Pattern.CASE_INSENSITIVE);
	
	static final WebResourceResponse emptyResponse = new WebResourceResponse("", "", null);
	public static boolean layoutScrollDisabled;
	public boolean bShowCustomView;
	public static long CustomViewHideTime;
	public WebChromeClient.CustomViewCallback mCustomViewCallback;
	public static long PrintStartTime;
	public boolean upsended;
	private int mOldOri;
	
	BrowserActivity a;
	
	Object k3client;
	public static int _req_fvw;
	public static int _req_fvh;
	protected boolean isLowEnd = true;
	
	/** 以荆轲之鞘纳山川之峰脊破阻业之障 */
	Map<SubStringKey, String> jinkeSheaths = new ConcurrentHashMap<>();
	Map<HostKey, ArrayList<SiteRule>> SiteConfigs = Collections.synchronizedMap(new HashMap<>());
	ArrayList<Pair<Pattern, SiteRule>> SiteConfigsByPattern = new ArrayList<>();
	
	
	AppToastManager appToastMngr;
	public long lastTitleSuppressTime;
	
	public boolean dismissAppToast() {
		if(appToastMngr!=null&&appToastMngr.visible()) {
			appToastMngr.hide();
			return true;
		}
		return false;
	}
	
	public static class SiteRule {
		long id;
		String quantifier;
		String JS = null;
		Pattern gstHandler = null;
		String gstHandlerJS = null;
		int EnRipenPercent = -1;
		Object[] pruneRules;
		boolean forbidScrollWhenSelecting;
		boolean pauseJs;
		int ts;
		Pair<Pattern, Pair<String, String>>[] modifiers;
	}
	
	static class HostKey {
		int hashCode;
		int start;
		int end=-1;
		String host;
		public HostKey() { }
		
		HostKey(String url){
			host=url;
			end = url.length();
			hashCode = strHash(url, start, end);
		}
		
		public void set(String url){
			if(url==null) {
				url=StringUtils.EMPTY;
				start=end=hashCode=0;
			}
			host=url;
			start = url.indexOf("://")+3;
			end = url.indexOf("/", start);
			hashCode = strHash(url, start, end);
		}
		
		@Override
		public boolean equals(Object o) {
			return this==o||(o instanceof HostKey&&((HostKey)o).strEq(this));
		}
		
		private boolean strEq(HostKey other) {
			return host.regionMatches(start, other.host, other.start, end-start);
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
		public static int strHash(String toHash, int start, int end) {
			int h=0;
			for (int i = start; i < end; i++) {
				h = 31 * h + Character.toLowerCase(toHash.charAt(i));
			}
			return h;
		}
	}
	
	interface UrlResourceHandler{
		WebResourceResponse handleUrl(String url);
	}
	
	LinkedList<RgxPlc> DNSIntelligence = new LinkedList<>();
	
	/**
	 window.setTimeoutPlus = window.setTimeout;
	 window.setTimeout = function(e,t){
		 var es = e+"";
		 if(es.indexOf("tb-modal-open")>0||es.indexOf("href=i")>0||es.indexOf("clearTimeout(z)")>0) {
			 console.log("es="+es);
			 return 0;
		 } else {
		 	return window.setTimeoutPlus(e,t);
		 }
	 };
	 document.addEventListener("click", function(e) {
		 var p = e.srcElement.parentNode;
		 if(p.id==="tiebaCustomPassLogin") {
			 p.parentNode.removeChild(p);
		 }
	 });
	 */
	@Multiline(trim=true, compile=true)
	final static String tieba_turbo = "";
	
	public WebCompoundListener(BrowserActivity activity) {
		a = activity;
		//wg
		{
			HostKey host = new HostKey("m.guokr.com");
			SiteRule rule = new SiteRule();
			rule.JS = "var d=document;var f=d.getElementsByClassName;f.call(d,'styled__Button-sc-1ctyfcr-7 uhLmk')[0].click();f.call(d,'styled__ModalAction-sc-1ctyfcr-9 cAsBvg cancel')[0].click();";
			ArrayList<SiteRule> rules = new ArrayList<>();
			rules.add(rule);
			SiteConfigs.put(host, rules);
		}
		
		{
			Pattern p = Pattern.compile("^http://.{0,20}\\.cssn\\.cn/.");
			SiteRule rule = new SiteRule();
			rule.EnRipenPercent = 80;
			rule.JS = "polyme.craft('style', 'body,#f-main,.f-main-left-sire,.f-main-left,.wrap,.content_main,.mian_li,.mian{width:100% !important;min-width:0px !important;padding:0px !important;}')";
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}
		
		// zhihu
		{
			Pattern p = Pattern.compile("^https?://www.zhihu.com/tardis/sogou"); // /ans
			SiteRule rule = new SiteRule();
			//rule.EnRipenPercent = 80;
			rule.JS = "polyme.craft('style', '.App{height:unset!important;}');var items=document.getElementsByClassName('sgui-slide-down');if(items.length==1) items[0].click();" +
					"items=document.getElementsByClassName('AuthorInfo AnswerItem-authorInfo AnswerItem-authorInfo--related')[0];if(items)items.onclick=function(){window.location='zhihu://answer/'+/[0-9]+/.exec(window.location.href)[0]}";
			rule.forbidScrollWhenSelecting = true;
			//rule.pauseJs = true;
			DNSIntelligence.add(new RgxPlc(Pattern.compile("^https?://www.zhihu.com/question/.+/answer/([0-9]+).*")
				,"https://www.zhihu.com/tardis/sogou/ans/$1"));
			DNSIntelligence.add(new RgxPlc(Pattern.compile("^https?://www.zhihu.com/question/([0-9]+)$")
				,"https://www.zhihu.com/tardis/sogou/qus/$1"));
			rule.pruneRules = new Object[2]; // pIRl
			rule.pruneRules[0] = new IISTri(42, -402077131, "js", null);
			rule.pruneRules[1] = new IISTri(51, -1941194295, "ut", ".App{height:auto!important}");
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}
		
		// kexuemeiguoren
		{
			Pattern p = Pattern.compile("^https?://www.scientificamerican.com/");
			SiteRule rule = new SiteRule();
			rule.JS = "polyme.craft('style', '#onetrust-banner-sdk{display:none;}')";
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}
		
		// tieba
		{
			Pattern p = Pattern.compile("^https?://tieba.baidu.com/");
			SiteRule rule = new SiteRule();
			rule.JS = tieba_turbo;
			rule.gstHandler = Pattern.compile("^com\\.baidu\\.tieba");
			rule.gstHandlerJS = "var getUrlParameter = function (url, name) { \n" +
					"var reg = new RegExp(\"(^|&)\" + name + \"=([^&]*)(&|$)\"); \n" +
					"return unescape(url.match(reg)[2]);\n" +
					"}; \n" +
					"var tid = getUrlParameter(\"%url\", \"tid\");\n" +
					"var ret=1;\n" +
					"if(tid) {\n" +
					"var nu = \"https://tieba.baidu.com/p/\"+tid;\n" +
					"if(!window.location.href.startsWith(nu)) {\n" +
					"window.location.href=nu;\n" +
					"ret=0;\n" +
					"}\n" +
					"}";
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}
		
		// qqxw
		{
			Pattern p = Pattern.compile("^https?://xw\\.qq\\.com/");
			SiteRule rule = new SiteRule();
			rule.JS = "var getUrlParameter = function (url, name) { \n" +
					"var reg = new RegExp(\"(^|&)\" + name + \"=([^&]*)(&|$)\"); \n" +
					"var reg=url.match(reg);\n" +
					"return unescape(reg?reg[2]:0);\n" +
					"}; \n" +
					"window.addEventListener(\"touchstart\", function(e) {\n" +
					"console.log(e.srcElement, e.path, e);\n" +
					"for(var i=0,p;p=e.path[i],i<5&&p;i++) {\n" +
					"var id=p.dataset[\"bossExpo\"];\n" +
					"if(id) {\n" +
					"id=getUrlParameter(id, \"articleid\");\n" +
					"p=p.getElementsByTagName(\"A\")[0];\n" +
					"if(p.getAttribute(\"href\")===\"\")\n" +
					"p.href=\"/cmsid/\"+id;\n" +
					"break;\n" +
					"}\n" +
					"}\n" +
					"})";
			rule.modifiers = new Pair[]{new Pair<>(Pattern.compile("^https://mat1\\.gtimg\\.com/qqcdn/xw/_next/static/.*?/pages/article/.*?\\.js"), new Pair<>("fe=me[0]", "fe=false"))};
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}
		
		// jianshu
		{
			Pattern p = Pattern.compile("^https?://www\\.jianshu\\.com/");
			SiteRule rule = new SiteRule();
			rule.ts = 118; // 120
			rule.JS = "polyme.craft('style','.collapse-free-content{height:auto!important} .call-app-btn{visibility:hidden}')";
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}
		
		// zhuanlizhijia
		{
			Pattern p = Pattern.compile("^https?://web\\.archive\\.org/");
			SiteRule rule = new SiteRule();
			rule.ts = 100; // 120
			rule.pruneRules = new Object[6]; // pIRl
			rule.pruneRules[0] = new Pair<>(Pattern.compile("^h.*?/http://googleads"), null);
			rule.pruneRules[1] = new IISTri(29, 593828798, "analytics.js", null);
			rule.pruneRules[2] = new IISTri(29, 593828798, "donate.php", null);
			rule.pruneRules[3] = new IISTri(29, 593828798, "fonts", null);
			rule.pruneRules[4] = new Pair<>(Pattern.compile("^h.*?/http://nsclick\\.baidu\\.com"), null);
			rule.pruneRules[5] = new Pair<>(Pattern.compile("^h.*?/http://pos\\.baidu\\.com"), null);
			rule.JS = "polyme.craft('style', '.post p{font-size:28px;line-height:39px;color:#000}')";
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}

		// mluxun
		{
			Pattern p = Pattern.compile("^https?://m[a-z]{0,12}\\.zuopinj\\.com");
			SiteRule rule = new SiteRule();
			rule.JS = "var cc=0,tm;\n" +
					"function func(){\n" +
					"[].forEach.call(document.body.children, function(e) {\n" +
					"  var h=e.offsetHeight;\n" +
					"  if(h>100&&h<150)e.style.visibility=\"collapse\";\n" +
					"});\n" +
					"cc++;\n" +
					"if(cc>10) clearInterval(tm);\n" +
					"}\n" +
					"tm = setInterval(func, 250);\n" +
					"polyme.craft(\"style\", \".ad{display:none}\");";
			//rule.ts = 100; // 120
			//rule.pruneRules[2] = new Pair<>(Pattern.compile("^h.*?/http://nsclick\\.baidu\\.com"), null);
			//rule.pruneRules[2] = new Pair<>(Pattern.compile("^h.*?/http://nsclick\\.baidu\\.com"), null);
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}
		
		// bilibook
		{
			Pattern p = Pattern.compile("^https?://www\\.bilibili\\.com/read");
			SiteRule rule = new SiteRule();
			rule.JS = "polyme.craft(\"style\", \".read-article-box{max-height:unset!important}\");";
			SiteConfigsByPattern.add(new Pair<>(p, rule));
		}
		
		parseJinKe();
		//jinkeSheaths.put(SubStringKey.fast_hostKey("en.wiktionary.org"), "91.198.174.192");
	}
	
	public void parseJinKe() {
		try {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
		} catch (Exception e) {
			CMN.Log(e);
		}
		jinkeSheaths.clear();
		File f = new File(a.getExternalFilesDir(null), "hosts");
		if(f.isFile()) {
			System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
			try (BufferedReader bufferedReader = new BufferedReader(new FileReader(f))){
				String ln;
				Pattern p = Pattern.compile("(.*)[ ]+(.*)");
				while ((ln=bufferedReader.readLine())!=null) {
					ln = ln.trim();
					if(ln.startsWith("#")) continue;
					Matcher m = p.matcher(ln);
					if(m.find()) {
						jinkeSheaths.put(SubStringKey.fast_hostKey(m.group(2)), m.group(1));
						//CMN.Log("jinke...", SubStringKey.fast_hostKey(m.group(2)), m.group(1));
					}
				}
			} catch (IOException e) {
				CMN.Log(e);
			}
		}
	}
	
	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	}
	
	@Override
	public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
	}
	
//	public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {
//		CMN.Log("onReceivedClientCertRequest 1", request);
//		if (request!=null&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			request.cancel();
//		}
//	}

	public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
		//CMN.Log("onReceivedHttpAuthRequest 1", host, realm);
		//a.root.post(() -> a.showT("onReceivedHttpAuthRequest"));
		if(view instanceof UniversalWebviewInterface) {
			handler.cancel();
		}
	}
	
	@SuppressLint("SourceLockedOrientationActivity")
	public void fixVideoFullScreen(boolean landscape) {
		if(bShowCustomView){
			mOldOri = a.mConfiguration.orientation;
			//int mode = opt.getFullScreenLandscapeMode();
			boolean bSwitch = true;
			if(bSwitch)
				a.setRequestedOrientation(landscape?ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE:ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			bShowCustomView =false;
		}
	}
	public WebChromeClient mWebClient = new WebClient();
	
	public class WebClient extends WebChromeClient {
		private File filepickernow;
		Dialog d; View cv;
		
		@Override
		public void onConsoleMessage(String message, int lineNumber, String sourceID) {
			//CMN.Log("onConsoleMessage::", message, lineNumber, sourceID);
		}
		
//		@Override
//		public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
//			CMN.Log("onConsoleMessage::", consoleMessage);
//			return super.onConsoleMessage(consoleMessage);
//		}
		
		@Override
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
			View mWebView = (View) (view instanceof UniversalWebviewInterface?view:getTag());
			WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
			if (WebOptions.getNoAlerts(layout.getDelegateFlag(BackendSettings, false))) {
				return true;
			}
			return false;
		}
		
		@Override
		public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
			View mWebView = (View) (view instanceof UniversalWebviewInterface?view:getTag());
			WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
			if (WebOptions.getNoAlerts(layout.getDelegateFlag(BackendSettings, false))) {
				return true;
			}
			return super.onJsPrompt(view, url, message, defaultValue, result);
		}
		
		@Override
		public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
			View mWebView = (View) (view instanceof UniversalWebviewInterface?view:getTag());
			WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
			if (WebOptions.getNoAlerts(layout.getDelegateFlag(BackendSettings, false))) {
				return true;
			}
			return super.onJsConfirm(view, url, message, result);
		}
		
		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			CMN.Log("onCreateWindow", isDialog);
			return false;
		}
		
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			//CMN.Log("onShowCustomView", _req_fvw, _req_fvh);
			bShowCustomView = true;
			a.root.removeCallbacks(dismissRunnable);
			//if(true)fixVideoFullScreen(true);
			
			if(d==null){
				d = new DialogVanishing(a);
				d.setCancelable(false);
				d.setCanceledOnTouchOutside(false);
				d.show();
			}
			
			Window window = d.getWindow();
			
			hideCustomView();
			mCustomViewCallback = callback;
			d.show();
			
			View vToAnimate = window.getDecorView();
			vToAnimate.setAlpha(0);
			
			window.setDimAmount(0);
			WindowManager.LayoutParams layoutParams = window.getAttributes();
			layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.horizontalMargin = 0;
			layoutParams.verticalMargin = 0;
			window.setAttributes(layoutParams);
			
			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			//window.getDecorView().setBackground(null);
			window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
			window.getDecorView().setPadding(0,0,0,0);
			d.setContentView(cv=view);
			vToAnimate.animate()
					//.setDuration(1000)
					.alpha(1);
		}
		
		Runnable dismissRunnable = () -> {
			d.dismiss();
			if(cv!=null){
				Utils.removeIfParentBeOrNotBe(cv, null, false);
				cv=null;
			}
		};
		
		@Override
		public void onHideCustomView() {
			//CMN.Log("onHideCustomView");
			bShowCustomView = false;
			//if (mOldOri==Configuration.ORIENTATION_PORTRAIT&&mConfiguration.orientation==Configuration.ORIENTATION_LANDSCAPE) {
			a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			//}
			a.root.removeCallbacks(dismissRunnable);
			a.root.postDelayed(dismissRunnable, 120);
			CustomViewHideTime = System.currentTimeMillis();
			hideCustomView();
		}
		
		private void hideCustomView() {
			if (mCustomViewCallback!=null) {
				mCustomViewCallback.onCustomViewHidden();
				mCustomViewCallback = null;
			}
		}
		
		@Override
		public void onReceivedTitle(WebView view, String title) {
			UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
			View mWebView = (View) webviewImpl;
			WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
			if(layout==null||layout.implView!=mWebView) {
				return;
			}
			layout.transientTitle = title;
			layout.holder.title = title;
			if(mWebView==a.currentWebView) {
				if(lastTitleSuppressTime !=0) {
					if(CMN.now()-lastTitleSuppressTime<400) {
						return;
					}
					lastTitleSuppressTime =0;
					a.root.removeCallbacks(a.postRectifyWebTitleRunnable);
					title = layout.rectifyWebStacks(title);
				}
				a.webtitle.setText(title);
			}
		}
		
		@Override
		public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
			//CMN.Log("onShowFileChooser", fileChooserParams.getAcceptTypes());
			if (a.filePathCallback==null) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				String types = null;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					String[] accepts = fileChooserParams.getAcceptTypes();
					if (accepts != null && accepts.length > 0) {
						types = "";
						String type;
						for (int i = 0; i < accepts.length; i++) {
							type =  ExtensionHelper.InferMimeTypeForName(accepts[i]);
							if (type!=null) {
								types += type;
								if (true) break;
								if (i<accepts.length-1) {
									types += ";";
								}
							}
						}
						
					}
					CMN.Log("types::", types, accepts);
				}
				if (TextUtils.isEmpty(types)) {
					types = "*/*";
				}
				intent.setType(types);
				a.filePathCallback = filePathCallback;
				try {
					a.startActivityForResult(Intent.createChooser(intent, "File Chooser"), Utils.RequsetFileFromFilePicker);
				} catch (Exception e) {
					a.filePathCallback = null;
					CMN.Log(e);
					return false;
				}
			}
			return true;
		}
		
		
		/** 进度变化的回调接口。 进度大于98时强行通知加载完成。<br/>
		 * see {@link WebCompoundListener#onPageFinished}*/
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			//if(true) return;
			UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
			View mWebView = (View) webviewImpl;
			WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
			if(layout==null||layout.implView!=mWebView) {
				return;
			}
			//CMN.Log("OPC::", newProgress, Thread.currentThread().getId(), webviewImpl.getUrl());
			if (!layout.PageStarted && layout.holder.version>1) {
//				String url = webviewImpl.getUrl();
//				if (!TextUtils.equals(url, layout.holder.url)) {
//					CMN.Log("调用OPS……", url, layout.holder.url);
//					onPageStarted(view, url, null);
//				}
//				layout.postTime = 350;
			}
			if(mWebView==a.currentWebView && layout.PageStarted) {
				a.tabsManagerIsDirty =true;
				layout.isloading=true;
				boolean premature=layout.getPremature();
				int lowerBound = layout.EnRipenPercent;
				if(lowerBound<=10) {
					lowerBound=98;
				}
				//lowerBound = 1000;
//				if(premature) {
//					lowerBound=Math.min(80, lowerBound);
//				}
				if(newProgress>=lowerBound) {
					CMN.Log("newProgress>=98", newProgress, premature, lowerBound);
					a.fadeOutProgressbar();
					if(layout.PageStarted) {
						layout.postFinished();
					}
					if(premature) {
						layout.stopLoading();
					}
				} else if(!a.supressingProgressListener){
					int start = a.progressbar_background.getLevel();
					int end = newProgress*100;
					if(end<start) end=start+10;
					if(end<2500) end=2500;
					View animating_progressbar = a.UIData.progressbar;
					if(animating_progressbar.getVisibility()!=View.VISIBLE) {
						animating_progressbar.setVisibility(View.VISIBLE);
					}
					animating_progressbar.setAlpha(1);
					//a.progressProceed.pause();
					a.progressProceed.setIntValues(start, end);
					a.progressProceed.setDuration((end-start)/10);
					a.progressProceed.start();
				}
			}
		}
	};
	
	public void  onScaleChanged(WebView view, float oldScale,float newScale)
	{
		//CMN.Log("onScaleChanged", oldScale, newScale);
		if(PrintStartTime>0 && System.currentTimeMillis()- PrintStartTime <5350){
			if(oldScale!=newScale) {
				CMN.Log("re_scale...");
				view.setScaleX(a.printScale);
				view.setScaleY(a.printScale);
			}
			return;
		}
		WebFrameLayout layout = view instanceof AdvancedBrowserWebView?((AdvancedBrowserWebView) view).layout:(WebFrameLayout) ((View)getTag()).getParent();
		layout.webScale = newScale;
		if (layout.frcWrp) {
			layout.mWebView.evaluateJavascript(WrappedOnResize+(layout.getWidth()/newScale)+")", null);
		}
		if (layout.bNeedPtsCnt && !layout.pts_2_scaled) {
			layout.pts_2_scaled = true;
		}
	}
	
//	@Override
//	public void onLoadResource(WebView view, String url) {
//		super.onLoadResource(view, url);
//		//CMN.Log("onLoadResource", url);
////			if(url.contains(".mp4")){
////				Message msg = new Message();
////				msg.what=110;
////				msg.obj=url;
////				mHandler.sendMessage(msg);
////			}
//	}
	
	HostKey startHostMatcher = new HostKey();
	
	public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail){
		showT("渲染进程崩溃！", "刷新", "reload", false);
		UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
//		View mWebView = (View) webviewImpl;
//		WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
//		webviewImpl.reload();
//		layout.holder.version++;
//		layout.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				layout.saveIfNeeded();
//			}
//		}, 1000);
		CMN.Log("渲染进程崩溃！", webviewImpl.getUrl());
		return true;
	}
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) { // OPS
		//if(true) return;
		UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
		CMN.Log("onPageStarted……", url, webviewImpl.getUrl(), Thread.currentThread().getId());
		View mWebView = (View) webviewImpl;
		WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
		if(layout==null||layout.implView!=mWebView) {
			return;
		}
		layout.queryDomain(url, false);
		
		if (layout.bNeedCheckUA && a.opt.getUpdateUAOnPageSt()) {
			// 在此处更新UA或导致页面往复重载以及冻结，慎之 | Dangerous to Update UA Here.
			layout.updateUserAgentString();
		}
		if (layout.bNeedCheckTextZoom>0 || a.opt.getSetTextZoomAggressively()) {
			// 若过早修改字体缩放，则加载前的页面也会收到影响。 | For Per-Website Font scale Updating.
			layout.setTextZoom();
		}
		
		//((ViewGroup.MarginLayoutParams)mWebView.layout.getLayoutParams()).bottomMargin=2*a.UIData.bottombar2.getHeight();
		layout.transientTitle=null;
		layout.holder.url=url;
		startHostMatcher.set(url);
		ArrayList<SiteRule> sm = SiteConfigs.get(startHostMatcher);
		int EnRipenPercent=Integer.MAX_VALUE;
		List<Object> prunes = layout.prunes;
		List<SiteRule> holder = layout.rules;
		prunes.clear();
		holder.clear();
		layout.forbidScrollWhenSelecting = false;
		boolean shutdownJs = false;
		layout.modifiers = null;
		int ts = 115;
		for (Pair<Pattern, SiteRule> siteRulePair:SiteConfigsByPattern) {
			if(siteRulePair.first.matcher(url).find()) {
				SiteRule rI = siteRulePair.second;
				holder.add(rI);
				if(rI.pruneRules!=null) {
					prunes.addAll(Arrays.asList(rI.pruneRules));
				}
				if (rI.ts>0) {
					ts = rI.ts;
				}
				layout.forbidScrollWhenSelecting |= rI.forbidScrollWhenSelecting;
				if (rI.modifiers!=null) {
					layout.addModifiers(rI.modifiers);
				}
				shutdownJs |= rI.pauseJs;
				if(rI.EnRipenPercent>10) {
					EnRipenPercent=Math.min(EnRipenPercent, rI.EnRipenPercent);
				}
			}
		}
		layout.hasPrune = prunes.size()>0;
		//CMN.Log("prunes", layout.hasPrune, prunes, url);
		
		if(shutdownJs) {
			layout.shutdownJS();
		}
		if(EnRipenPercent>100) {
			EnRipenPercent=-1;
		}
		if(sm!=null) {
			holder.addAll(sm);
			if(sm.size()>0) {
				EnRipenPercent=sm.get(0).EnRipenPercent;
			}
		}
		layout.EnRipenPercent=EnRipenPercent;
		layout.PageStarted=true;
		layout.PageVersion++;
		layout.PageFinishedPosted=false;
		if(mWebView==a.currentWebView) {
			a.updateProgressUI();
		}
	}
	
	
	
	/**if (!window.PPTurboKit) {
			//window.polyme = {};
			var w = window;
			w.PPTurboKit = 1;
			w.addEventListener('keydown', function(e) {
				var code = e.keyCode;
				if (code >= 37 && code <= 40) {
					if (document.activeElement.tagName === 'TEXTAREA') {
						var b = code == 37 || code == 39;
						e.stopPropagation();
						e.preventDefault();
						if (b) {
							var s = window.getSelection();
							s.modify('move', code == 37 ? 'backward': 'forward', 'character');
						}
					}
				}
			});
	 		w.igNNC=0;
			w.addEventListener('click',function(e){
	 			console.log('click::'+w._ttlck, e);
			  //getSelection().empty();
	          //if(w._ttlck) {} else
			  if(igNNC) {igNNC=0;} else
			  if(e.target.className=='PLOD_HL') {
				  var sel = getSelection();
				  sel.empty();
				  var range = new Range();
				  range.selectNode(e.target);
				  sel.addRange(range);
				  polyme.sendup(chrmtd.get());
				  igNNC=1;
			  } else return;
			  e.preventDefault();
			  //console.log(e.target)
			});
			w.addEventListener('load', function(e) {
				console.log('onload !!!');
			});
	 		w.addEventListener('touchstart', function(e){
				if(!w._ttlck && e.touches.length==1){
					w._ttarget = e.touches[0].target;
				}
			});
			var d = document;
			var h = d.head;
			var b = d.body;
			polyme.loadJs=function(url, callback) {
				var script = d.createElement('script');
				script.type = "text/javascript";
				if (typeof(callback) != "undefined") {
					script.onload = function() {
						callback();
					}
				}
				script.src = url;
				b.appendChild(script);
			};
			polyme.craft=function(tagN, H, _) {
	 			var ele = d.createElement(tagN);
				ele.innerHTML=H;
				_?b:h.appendChild(ele);
				return ele;
			};
			polyme.highlight=function(keyword) {
				var b1 = keyword == null;
				if (b1) keyword = '备 受 开 启';
				if (keyword == null || b1 && keyword.trim().length == 0) return;
				var w = window;
				if (!w._PPMInst) {
					polyme.loadJs('https://mark.js',
					function() {
						w._PPMInst.do_highlight(keyword);
					});
				} else {
					w._PPMInst.do_highlight(keyword);
				}
			};
	 		w._docAnnots="";
	 		w._docAnnott="";
			function wrappedFscrFunc(e) {
	 			//console.log('fatal fullscreen 111'+ document.fullscreenElement);
	 			if(document.fullscreen || true)
	 			{
					var vw=0,vh=0;
					var se = e.srcElement;
					var vi = se.tagName==="VIDEO"?se:(se.querySelectorAll("VIDEO")[0]);
					if(vi) {
						vw = vi.videoWidth;
						vh = vi.videoHeight;
					}
					if(vw==0) {
						vw = se.clientWidth;
						vh = se.clientHeight;
					}
					//console.log('fatal fullscreen!!! '+vw+","+vh);
					polyme.onRequestFView(vw, vh);
					return false;
	 			}
			}
	 		//console.log('fatal fullscreen???');
			w.addEventListener('fullscreenchange', wrappedFscrFunc);
			w.addEventListener('webkitfullscreenchange', wrappedFscrFunc);
			w.addEventListener('mozfullscreenchange', wrappedFscrFunc);
		}
	 	polyme.logm(['fatal logm test', '1','2','3']);
	 */
	@Multiline(trim=true, compile=true)
	public final static String PRI = "Primary Rule Insertion";
	
	/** var m = document.head.querySelectorAll('meta');
	 	window.hasVPM = false;
		for (var i = 0; i < m.length; i++) {
			var iI = m[i];
			if (iI.name === 'viewport') {
				if (iI.content.lastIndexOf('no') > 0) {
					iI.content = 'width=device-width,minimum-scale=1,maximum-scale=5.0,user-scalable=yes';
				}
	 			window.hasVPM = true;
				break;
			}
		}
	 */
	@Multiline(trim=true, compile=true)
	public final static String ForceResizable = "";
	
	/** var tmp = window.hasVPM;
		if(tmp===undefined) {
	 		tmp = false;
			var m = document.head.querySelectorAll('meta');
			for (var i = 0; i < m.length; i++) {
				var iI = m[i];
				if (iI.name === 'viewport') {
					tmp = true;
					break;
				}
			}
	 	} else {
	 	 	tmp=!tmp;
		}
	 	if(tmp) {
			window._frcWrp = 1;
		}
		window.metas = 0;
	 	tmp?1:0
	 */
	@Multiline(trim=true, compile=true)
	public final static String ForceWarpable = "";
	
	
	/** (function(wd){
			if(wd&&window._frcWrp) {
				wd = (wd-25)+"px";
				console.log("fatal WrappedOnResize!!! "+wd);
				function traverseDom(p) {
					var n = [];
					n.push(p);
					var cc = 1;
					for(var j=0;j<cc;j++) {
						p = n[j];
						if(p.className!=undefined && p.className.indexOf && p.className.indexOf('code')<0) { // p.tagName!='A' &&
							var m = p.childNodes, ln = m.length, pl = cc, i=0;
							//if(ln>p.childElementCount)
							{
								for(;i<ln;i++) {
									var e = m[i];
	 								if(e.id==='b_results' || e.tagName==='A' || e.tagName==='TABLE') {
										e.style.maxWidth = wd;
									} else if(e.nodeType==3) {
										var t=e.textContent;
										if (/\S/.test(t) && t.length>3) {
											cc = pl;
											p.style.maxWidth = wd;
											p.style.whiteSpace = "unset";
											console.log(p);
											break;
										}
									} else if(e.tagName!=undefined && e.tagName!="SCRIPT" && e.tagName!="STYLE"){
										n.push(e);
										cc++;
									}
								}
							}
						}
					}
				}
				[].forEach.call(window.frames, function(e){
				try{
				traverseDom(window.frames[0].document.body);
				} catch(e){}
				});
				traverseDom(document.body);
			}
	 	})(
	 */
	@Multiline(trim=true, compile=true)
	public final static String WrappedOnResize = "";
	
	public static byte[] markjsBytesArr = null;
	
	public static byte[] erudajsBytesArr = null;
	
	@Multiline(file="ActivityLauncher\\src\\main\\assets\\mark.js")
	public static int markjsBytesLen = 0;
	
	@Override
	public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
		UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
		View mWebView = (View) webviewImpl;
		WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
		if(layout==null||layout.implView!=mWebView) {
			return;
		}
		//CMN.Log("OPV:: ", url, isReload, webviewImpl.getUrl(), webviewImpl.getTitle());
		String title = layout.getTitle();
		layout.holder.url=url;
		layout.transientTitle=title;
		layout.holder.title=title;
		if (!isReload || a.opt.getOnReloadUpdateHistory()) {
			layout.holder.url_id = a.historyCon.insertUpdateBrowserUrl(layout.holder, layout.preQueryNote(url), !isReload||a.opt.getOnReloadIncreaseVisitCount());
		}
	}
	
	/** 加载完成的回调接口。<br/>
	 * 注入基础规则。<br/>
	 * 注入网页规则。<br/>
	 * 处理搜索和笔记的高亮。<br/>
	 * 处理历史记录。<br/>
	 * see {@link WebClient#onProgressChanged}<br/>*/
	@Override public void onPageFinished(WebView view, String url) {
		UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
		View mWebView = (View) webviewImpl;
		WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
		if(layout==null||layout.implView!=mWebView) {
			return;
		}
		//if(true) return;
		if(layout.PageStarted) {
//			if(!a.opt.getUpdateUALowEnd()) { // 在此处更新UA或导致页面往复重载，慎之
//				if(a.updateUserAgentString(layout, url)) {
//					layout.refresh();
//					return;
//				}
//			}
			String ordinalUrl=webviewImpl.getUrl();
			if(ordinalUrl!=null) {
				url = ordinalUrl;
			}
			layout.holder.url=url;
			CMN.Log("OPF:::", url, webviewImpl.getTitle(), Thread.currentThread().getId());
			//CMN.Log("OPF:::", mWebView.holder.url);
			mWebView.setTag(url);
			layout.removePostFinished();
			layout.PageStarted=false;
			
			
			layout.time=System.currentTimeMillis();
			layout.incrementVerIfAtNormalPage();
			layout.lastThumbScroll=mWebView.getScrollY();
			
			if(layout.bNeedCheckUA && a.opt.getUpdateUAOnPageFn()) {
				layout.updateUserAgentString();
			}
			
			//layout.setTextZoom();
			
//			boolean bEnableJavaScript = mWebView.getSettings().getJavaScriptEnabled();
//
//			if(!bEnableJavaScript)
//				mWebView.getSettings().setJavaScriptEnabled(true);
			
			//mWebView.postReviveJS(0);
			//layout.reviveJS();
			
			/* 原天道契 */
			webviewImpl.evaluateJavascript(PRI, null);
			
			//webviewImpl.evaluateJavascript("polyme.loadJs('//cdn.jsdelivr.net/npm/eruda',()=>{eruda.init();})", null);
			//ensureErudaJS(a);
			//webviewImpl.evaluateJavascript("polyme.loadJs('https://erdo.js',()=>{erdo.init();})", null);
			
			layout.frcWrp = false;
			long flag = layout.getDelegateFlag(TextSettings, false);
			if(WebOptions.getTextTurboEnabled(flag)) {
				if(WebOptions.getForcePageZoomable(flag)) {
					webviewImpl.evaluateJavascript(ForceResizable, null);
				}
				if(WebOptions.getForceTextWrap(flag)) {
					if(a.opt.getForceTextWrapForAllWebs() || layout.getDelegateFlagIndex(TextSettings)==WebViewSettingsSource_DOMAIN) {
						webviewImpl.evaluateJavascript("window._frcWrp=1", null);
						layout.frcWrp = true;
						SendTextWarp(layout);
					} else {
						webviewImpl.evaluateJavascript(ForceWarpable, value -> {
							if (layout.frcWrp = "1".equals(value)) {
								SendTextWarp(layout);
							}
						});
					}
				}
			}
			
			if(layout.rules.size()>0) {
				for (SiteRule rI:layout.rules) {
					if(rI.JS!=null/* && rI.host.equals(finishHostMatcher)*/) {
						webviewImpl.evaluateJavascript(rI.JS, null);
					}
				}
			}
			//mWebView.evaluateJavascript("polyme.craft('style', 'body{padding-bottom:450px !important;}')", null);
			
//			if(!bEnableJavaScript) {
//				mWebView.postDelayed(() -> mWebView.getSettings().setJavaScriptEnabled(false), 1350);
//			}
			
			String title = layout.getTitle();
			layout.transientTitle=title;
			layout.holder.url=url;
			layout.holder.title=title;
			//todo 高亮
			String note = layout.queryNote(url);
			//CMN.Log("note::", note);
			boolean MarkJsRequired = note!=null;
			//todo 笔记，先复原笔记，后上高亮，但是因为笔记最后会用到高亮的功能，所以加载笔记的同时加载高亮支持。
			if(MarkJsRequired&&markjsBytesArr==null) {
				ensureMarkJS(a);
			}
			if (note!=null) {
				//"0/0/2/0/1/5/0/4/0/1/2/2:63,0/0/2/0/1/5/0/4/0/1/2/2:65|0\\n0/0/2/0/1/5/0/4/0/1/2/2:24,0/0/2/0/1/5/0/4/0/1/2/2:26|0\\n0/0/2/0/1/5/0/4/0/1/2/2:15,0/0/2/0/1/5/0/4/0/1/2/2:17|0\\n"
				webviewImpl.evaluateJavascript(WebViewHelper.getInstance()
						.getResoreHighLightIncantation(note), new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						CMN.Log("asd", value);
					}
				});
			}
			
			if(false)
				webviewImpl.evaluateJavascript("polyme.highlight('aut')", new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						CMN.Log("asd", value);
					}
				});
			
			if(mWebView==a.currentWebView) {
				//a.webtitle.setText(title);
				if(a.webtitle.getVisibility()==View.VISIBLE) {
					a.etSearch.setText(url);
				}
				a.fadeOutProgressbar();
			}
			if(layout.clearHistroyRequested) {
				webviewImpl.clearHistory();
				layout.clearHistroyRequested=false;
			}
			if(layout.holder.getLuxury()) {
				int idx = layout.getThisIdx();
				if(idx>0) {
					layout.getChildAt(idx-1).setVisibility(View.GONE);
				}
			}
		}
	}
	
	private void SendTextWarp(WebFrameLayout layout) {
		layout.mWebView.evaluateJavascript(WrappedOnResize+(layout.getWidth()/layout.webScale)+")", null);
	}
	
	public void ensureMarkJS(Context context) {
		//WebCompoundListener.markjsBytesArr = new byte[WebCompoundListener.markjsBytesLen];
		if(markjsBytesArr==null) {
			try {
				InputStream input = context.getAssets().open("mark.js");
				markjsBytesLen = input.available();
				markjsBytesArr = new byte[WebCompoundListener.markjsBytesLen];
				input.read(WebCompoundListener.markjsBytesArr);
				input.close();
			} catch (IOException e) { CMN.Log(e);}
		}
	}
	
	public void ensureErudaJS(Context context) {
		//WebCompoundListener.markjsBytesArr = new byte[WebCompoundListener.markjsBytesLen];
		if(erudajsBytesArr==null) {
			try {
				InputStream input = context.getAssets().open("erdo.js");
				int markjsBytesLen = input.available();
				erudajsBytesArr = new byte[markjsBytesLen];
				input.read(erudajsBytesArr);
				input.close();
			} catch (IOException e) { CMN.Log(e);}
		}
	}
	
	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		if(handler!=null) {
			handler.proceed();
		} else {
			error.hasError(0x108895);
		}
	}
	
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{
		boolean b1 = view instanceof UniversalWebviewInterface;
		UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (b1?view:getTag());
		View mWebView = (View) webviewImpl;
		WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
		if(!b1) {
			unlock();
		}
		CMN.Log("SOUL::", url, Thread.currentThread().getId(), webviewImpl.getProgress());
		if(layout==null||layout.implView!=mWebView) {
			return false;
		}
		if(layout.forbidLoading) {
			return true;
		}
		if(WebOptions.getNoCORSJump(layout.getDelegateFlag(BackendSettings, false)) && !layout.domain.matches(url)) {
			return true;
		}
		if(!url.regionMatches(false, 0, "http", 0, 4)
				&&!url.regionMatches(false, 0, "https", 0, 5)) {
			//if(a.webtitle.getVisibility()== View.VISIBLE) a.etSearch.setText(url);
			for (int i = 0, len=layout.rules.size(); i < len; i++) {
				SiteRule rl = layout.rules.get(i);
				if(rl.gstHandler!=null && rl.gstHandler.matcher(url).find()) {
					webviewImpl.evaluateJavascript(rl.gstHandlerJS.replace("%url", url), value -> {
						if (!"0".equals(value)) {
							relayAppScheme(layout, url);
						}
					});
					return true;
				}
			}
			if(true) {
				relayAppScheme(layout, url);
			} else {
				return false;
			}
			return true;
		}
		if(mWebView==a.currentWebView) {
			if(WebOptions.getAlwaysOpenInNewTab(a.getMergedFlag()) && layout.holder.version>=1) {
				if(a.checkAutoNewTabAllowed()) {
					a.newTab(url, false, true, -1);
					return true;
				} else if(webviewImpl.getProgress()==100) {
					// when Auto-New-Tab is not Allowed due to frequency limitation
					// , dont intercept the url loading if its already in loading state
					// (progress!=100).
					return true;
				}
			}
			if (layout.holder.getLuxury()) {
				long now = CMN.now();
				if(now>0&&now-a.supressingNxtLux<350) {
					CMN.Log("supressingNxtLux", url);
					//layout.setNavStacksDirty();
					//return false;
				} else {
					a.supressingNxtLux = now;
					if(!layout.hasValidUrl()) {
						layout.clearHistroyRequested=true;
					} else {
						//a.LuxuriouslyLoadUrl(layout, url);
						return true;
					}
				}
			}
		}
//		if (layout.getDelegateFlagIndex(BackendSettings)==WebViewSettingsSource_DOMAIN && webviewImpl.getProgress()<100) {
//			CMN.Log("你不行你不行");
//			// 防止一侧设置PC模式后，www <-> m 反复重载
//			return true;
//		}
		// 此处设置UA，可能导致旧的页面重载而略过新的页面加载。
		if(layout.queryDomain(url, a.opt.getUpdateUAOnClkLnk())) layout.bRecentNewDomainLnk++;
		layout.setNavStacksDirty();
		//webviewImpl.loadUrl(url); return true;
		return false;
	}
	
	private void relayAppScheme(WebFrameLayout layout, String url) {
		if (layout!=null && layout.allowAppScheme && layout==a.currentViewImpl && layout.getVisibility()==View.VISIBLE) {
			int idx = url.indexOf(":");
			String appScheme = url;
			if(idx>0) {
				appScheme = Utils.getSubStrWord(appScheme, 0, idx);
			}
			showT(appScheme+" 请求打开外部APP", "继续", url, false);
		}
	}
	
	public void showT(String message, String yesText, String url, boolean forceShow) {
		if(appToastMngr==null) {
			appToastMngr = new AppToastManager(a);
		}
		appToastMngr.showT(message, yesText, url, forceShow, (a.hasWindowFocus()&&a.settingsPopup==null)?-1:0);
	}
	
	private OkHttpClient prepareKlient() {
		if (k3client!=null) return (OkHttpClient) k3client;
		int cacheSize = 10 * 1024 * 1024;
		Interceptor headerInterceptor = new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				Request request = chain.request();
				Response response = chain.proceed(request);
				Response response1 = response.newBuilder()
						.removeHeader("Pragma")
						.removeHeader("Cache-Control")
						//cache for 30 days
						.header("Cache-Control", "max-age=" + 3600 * 24 * 30)
						.build();
				return response1;
			}
		};
		OkHttpClient klient = new OkHttpClient.Builder()
				.connectTimeout(5, TimeUnit.SECONDS)
				.addNetworkInterceptor(headerInterceptor)
				//.protocols(Collections.singletonList(Protocol.HTTP_1_1))
				.cache(true ?
						new Cache(new File(a.getExternalCacheDir(), "k3cache")
								, cacheSize) : null) // 配置缓存
				.dns(new Dns() {
					@Override
					public List<InetAddress> lookup(String hostname) throws UnknownHostException {
						String addr = jinkeSheaths.get(SubStringKey.new_hostKey(hostname));
						CMN.Log("lookup...", hostname, addr, InetAddress.getByName(addr));
						if (addr != null) {
							return Collections.singletonList(InetAddress.getByName(addr));
						}
						//else return Collections.singletonList(InetAddress.getByName(hostname));
						return Dns.SYSTEM.lookup(hostname);
					}
				})
				//.readTimeout(5, TimeUnit.SECONDS)
				//.setCache(getCache())
				//.certificatePinner(getPinnedCerts())
				//.setSslSocketFactory(getSSL())
				.hostnameVerifier(DO_NOT_VERIFY)
				.build();
		return klient;
	}
	
	protected WebResourceResponse handlePrunes(WebFrameLayout layout, String url) {
		for(Object pI:layout.prunes) {
			if(pI instanceof IISTri) {
				IISTri tri = (IISTri) pI;
				int len = url.length();
				if(len>tri.i1&&url.regionMatches(tri.i1, tri.str1, 0, tri.str1.length())&&Utils.hashCode(url, 0, tri.i1)==tri.i2) {
					CMN.Log("pIRl", url);
					if(tri.plc==null) {
						return emptyResponse;
					} else {
						return new WebResourceResponse("text/css","utf8", new ByteArrayInputStream(tri.plc.getBytes()));
					}
				}
			}
			else if(pI instanceof Pair) {
				Pair p = (Pair) pI;
				Pattern P = (Pattern) p.first;
				if (P.matcher(url).find()) {
					Object obj = p.second;
					CMN.Log("pIRl", url, obj);
					if (obj==null || obj instanceof String) {
						String text = (String) obj;
						if(text==null) {
							return emptyResponse;
						} else {
							return new WebResourceResponse("text/css","utf8", new ByteArrayInputStream(text.getBytes()));
						}
					} else if (obj instanceof UrlResourceHandler) {
						UrlResourceHandler text = (UrlResourceHandler) obj;
						return text.handleUrl(url);
					}
				}
			}
		}
		return null;
	}
	
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	@Nullable
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		//CMN.Log("SIR::1::", view, url);
		if (view==null && url==null) {
			return shouldInterceptRequest(view, (WebResourceRequest)null);
		}
		return null;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
		View mWebView = (View) (view instanceof UniversalWebviewInterface?view:getTag());
		WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
		if (request==null) {
			request = (WebResourceRequest) ((UniversalWebviewInterface)mWebView).getLastRequest();
		}
		//if(true) return null;
		String url = request.getUrl().toString();
		//CMN.Log("SIR::", url);
		//CMN.Log("SIR::", headers);
		if(layout==null) {
			return null;
		}
		boolean lishanxizhe = false;
		
		if(layout.hasPrune) {
			WebResourceResponse ret = handlePrunes(layout, url);
			if (ret!=null) return ret;
		}
		
		String host = null;
		if(true) {
			String addr = jinkeSheaths.get(SubStringKey.new_hostKey(url));
			if(addr!=null) {
				try {
					URL oldUrl = new URL(url);
					host = oldUrl.getHost();
					if(false)
					url = url.replaceFirst(oldUrl.getHost(), addr);
					CMN.Log("秦王绕柱走", url);
					lishanxizhe = true;
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
		}
		List<Pair> moders = null;
		if (layout.modifiers!=null) {
			for (Pair p:layout.modifiers) {
				if(((Pattern)(p.first)).matcher(url).find()) {
					if (moders==null) moders = new ArrayList<>();
					moders.add(p);
				}
			}
		}
		//CMN.Log("修改了::??", url, moders!=null, layout.modifiers);
		if(lishanxizhe /*&& !(url.endsWith("google.com/")||url.endsWith("google.cn/"))*/
				|| moders!=null) {
			try {
				return getClientResponse(url, host, moders, request.getRequestHeaders(), request.getMethod());
			} catch (IOException e) {
				CMN.Log(url+"\n", e);
				return emptyResponse;
				//return null;
			}
		}
		//CMN.Log("request.getUrl().getScheme()", request.getUrl().getScheme());
		if(url.startsWith("https://mark.js")&&markjsBytesArr!=null) {
			CMN.Log("加载中", new String(markjsBytesArr, 0, 200));
			return new WebResourceResponse("text/javascript", "utf8", new ByteArrayInputStream(markjsBytesArr));
		}
		if(url.contains("erdo.js")) {
			CMN.Log("加载erdo中", new String(erudajsBytesArr, 0, 200));
			return new WebResourceResponse("text/javascript", "utf8", new ByteArrayInputStream(erudajsBytesArr));
		}
		return null;
	}
	
	protected WebResourceResponse getClientResponse(String url, String host, List<Pair> moders, Map<String, String> headers, String method) throws IOException {
		String acc = headers.get("Accept");
		//CMN.Log("SIR::Accept_", acc, acc.equals("*/*"));
		if(acc==null) {
			acc = "*/*;";
		}
		headers.put("Access-Control-Allow-Origin", "*");
		headers.put("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
		InputStream input;
		String MIME;
		CMN.rt("转构开始……", url);
		if(true || moders!=null) {
			OkHttpClient klient = (OkHttpClient) k3client;
			if(klient==null) {
				klient = prepareKlient();
			}
			Request.Builder k3request = new Request.Builder()
					.url(url)
					.header("Accept-Charset", "utf-8")
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept")
					;
			for(String kI:headers.keySet()) {
				k3request.header(kI, headers.get(kI));
			}
			//int maxSale = 60 * 60 * 24 * 28; // tolerate 4-weeks sale
//					if (!NetworkUtils.isConnected(a))
//					k3request.removeHeader("Pragma")
//							.cacheControl(new CacheControl.Builder()
//									.maxAge(0, TimeUnit.SECONDS)
//									.maxStale(365,TimeUnit.DAYS).build())
//							.header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale);
			if(host!=null) k3request.header("Host", host);
			k3request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36");
			Response k3response = klient.newCall(k3request.build()).execute();
			MIME = k3response.header("content-type");
			if (moders!=null) {
				String raw = k3response.body().string();
				for (Pair p:moders) {
					p = (Pair) p.second;
					raw = raw.replace((String)p.first, (String)p.second);
				}
				//CMN.Log("修改了::", url);
				input = new ByteArrayInputStream(raw.getBytes());
			} else {
				input = k3response.body().byteStream();
			}
		}
		else {
			HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
			//urlConnection.setRequestProperty("contentType", headers.get("contentType"));
			//urlConnection.setRequestProperty("Accept", headers.get("Accept"));
			if(false) {
				File httpCacheDir = new File(a.getExternalCacheDir(), "k1cache");
				int cacheSize = 10 * 1024 * 1024;
				try {
					HttpResponseCache.install(httpCacheDir, cacheSize);
				} catch (IOException e) {
					CMN.Log(e);
				}
			}
			
			if(urlConnection instanceof HttpsURLConnection) {
				((HttpsURLConnection)urlConnection).setHostnameVerifier(DO_NOT_VERIFY);
			}
			urlConnection.setRequestProperty("Accept-Charset", "utf-8");
			urlConnection.setRequestProperty("connection", "Keep-Alive");
			urlConnection.setRequestMethod(method);
			urlConnection.setConnectTimeout(3800);
			urlConnection.setUseCaches(true);
			urlConnection.setDefaultUseCaches(true);
			for(String kI:headers.keySet()) {
				urlConnection.setRequestProperty(kI, headers.get(kI));
			}
			if(host!=null) urlConnection.setRequestProperty("Host", host);
			urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36");
			//int maxSale = 60 * 60 * 24 * 28; // tolerate 4-weeks sale
			//if(NetworkUtils.isConnected(a))
			//urlConnection.setRequestProperty("Cache-Control", "max-age=" + maxSale);
			//else
			//urlConnection.setRequestProperty("Cache-Control", "public, only-if-cached, max-stale=" + maxSale);
			
			//urlConnection.setRequestProperty("User-Agent", a.android_ua);
			//urlConnection.setRequestProperty("Host", "translate.google.cn");
			//urlConnection.setRequestProperty("Origin", "https://translate.google.cn");
			urlConnection.connect();
			input = urlConnection.getInputStream();
			input = new AutoCloseNetStream(input, urlConnection);
			MIME = urlConnection.getHeaderField("content-type");
		}
		CMN.pt("转构完毕！！！", input.available(), MIME);
		if(TextUtils.isEmpty(MIME)) {
			MIME = acc;
		}
		int idx = MIME.indexOf(",");
		if(idx<0) {
			idx = MIME.indexOf(";");
		}
		if(idx>=0) {
			MIME = MIME.substring(0, idx);
		}
		WebResourceResponse webResourceResponse;
		if(Utils.bigCake) {
			webResourceResponse=new WebResourceResponse(MIME, "utf8", input);
			webResourceResponse.setResponseHeaders(headers);
		} else {
			webResourceResponse = new WebResourceResponseCompat(MIME, "utf8", input);
			((WebResourceResponseCompat)webResourceResponse).setResponseHeaders(headers);
		}
		//CMN.Log("百代春秋泽被万世");
		return webResourceResponse;
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		CMN.Log("DOWNLOAD:::", url, contentDisposition, mimetype, contentLength);
		if(appToastMngr!=null&&appToastMngr.visible()) {
			return;
		}
		a.showDownloadDialog(url, contentLength, mimetype);
		
//		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//		//a.startActivity(intent);
//
//		intent = new Intent(Intent.ACTION_VIEW);
//		intent.addCategory(Intent.CATEGORY_BROWSABLE);
//		intent.setData(Uri.parse(url));
//		a.startActivity(intent);
	}
	
	@Override
	public void onScrollChange(View view, int scrollX, int scrollY, int oldx, int oldy) {
		UniversalWebviewInterface webviewImpl = (UniversalWebviewInterface) (view instanceof UniversalWebviewInterface?view:getTag());
		View mWebView = (View) webviewImpl;
		WebFrameLayout layout = (WebFrameLayout) mWebView.getParent();
		if(layout==null||layout.implView!=mWebView) {
			return;
		}
		if(PrintStartTime>0 && System.currentTimeMillis()-CustomViewHideTime<5350) {
			//CMN.Log("re_scroll...", a.focused);
			webviewImpl.SafeScrollTo(a.printSX, a.printSY);
			mWebView.requestLayout();
			return;
		}
		//CMN.Log("onScrollChange", scrollY-oldy);
		if(layout.forbidScrollWhenSelecting && layout.bIsActionMenuShown && oldy-scrollY>200
			|| /*a.tabViewAdapter.isVisible()*/ layout.hideForTabView
			|| CustomViewHideTime>0 && System.currentTimeMillis()-CustomViewHideTime<350) {
			//CMN.Log("re_scroll...");
			webviewImpl.SafeScrollTo(oldx, oldy);
			return;
		}
		if(layout.lockX && oldx!=scrollX || layout.lockY && oldy!=scrollY) {
			CMN.Log("re_scroll...", layout.pts);
			if (layout.pts<2 /*!layout.pts_2_scaled*/ && !layout.isIMScrollSupressed) {
				webviewImpl.SafeScrollTo(layout.lockX?oldx:scrollX, layout.lockY?oldy:scrollY);
			}
			return;
		}
	}
	
	final Object bridge = new Object(){
		@org.xwalk.core.JavascriptInterface
		@JavascriptInterface
		public void onRequestFView(int w, int h) {
			CMN.Log("onRequestFView", w, h);
			_req_fvw=w;
			_req_fvh=h;
			fixVideoFullScreen(w>h);
		}
		
		@org.xwalk.core.JavascriptInterface
		@JavascriptInterface
		public void SaveAnnots(long tabID, String annots, String text, int textId, int type, String cols) {
			CMN.Log("SaveAnnots", tabID, annots, text, a.historyCon.isOpen());
			WebFrameLayout layout = a.getWebViewFromID(tabID);
			if(layout!=null && layout.HLED) {
				layout.HLED=false;
				layout.saveNote(annots, text, textId, type, cols);
			}
		}
		
		@org.xwalk.core.JavascriptInterface
		@JavascriptInterface
		public void log(String msg) {
			CMN.Log(msg);
			
		}
		
		@org.xwalk.core.JavascriptInterface
		@JavascriptInterface
		public void logm(String[] msg) {
			CMN.Log("logm", msg.length, msg);
			CMN.Log("logm--", System.identityHashCode(msg), System.identityHashCode(msg[0]));
		}
		
		@org.xwalk.core.JavascriptInterface
		@JavascriptInterface
		public void sendup(long id) {
			WebFrameLayout layout = a.getWebViewFromID(id);
			if(layout==a.currentViewImpl) {
				//if(false)
				//if(!mWebView.bIsActionMenuShown)
				upsended = true;
				View view = layout.implView;
				view.postDelayed(new Runnable() {
					@Override
					public void run() {
						long time = CMN.now();
						MotionEvent evt = MotionEvent.obtain(time, time,MotionEvent.ACTION_DOWN, layout.lastX, layout.lastY, 0);
						view.dispatchTouchEvent(evt);
						evt.setAction(MotionEvent.ACTION_UP);
						view.dispatchTouchEvent(evt);
						evt.recycle();
					}
				}, Utils.version>=29?0:150);
			}
		}
	};
}
