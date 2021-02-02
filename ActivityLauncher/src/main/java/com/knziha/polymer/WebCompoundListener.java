package com.knziha.polymer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView.OnScrollChangedListener;

import com.knziha.polymer.Utils.BufferedReader;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.IISTri;
import com.knziha.polymer.Utils.RgxPlc;
import com.knziha.polymer.toolkits.MyX509TrustManager;
import com.knziha.polymer.toolkits.Utils.BU;
import com.knziha.polymer.toolkits.Utils.ReusableByteOutputStream;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.knziha.polymer.widgets.WebViewmy;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import static com.knziha.polymer.BrowserActivity.TitleSep;
import static com.knziha.polymer.Utils.WebOptions.BackendSettings;

/** WebView Compound Listener ：两大网页客户端监听器及Javascript桥，全局一个实例。 */
public class WebCompoundListener extends WebViewClient implements DownloadListener, OnScrollChangedListener {
	public final static Pattern requestPattern = Pattern.compile("\\?.*$");
	public final static Pattern httpPattern = Pattern.compile("(https?://.*)", Pattern.CASE_INSENSITIVE);
	public final static Pattern timePattern = Pattern.compile("t=([0-9]{8,15})", Pattern.CASE_INSENSITIVE);
	
	static final WebResourceResponse emptyResponse = new WebResourceResponse("", "", null);
	public static boolean layoutScrollDisabled;
	public boolean bShowCustomView;
	public static long CustomViewHideTime;
	public static long PrintStartTime;
	public boolean upsended;
	private int mOldOri;
	
	BrowserActivity a;
	
	Object k3client;
	
	static class SubStringKey {
		final int st;
		final int ed;
		final int hash;
		final String text;
		final int len;
		
		static SubStringKey fast_hostKey(String text) {
			return new SubStringKey(text, 0, text.length());
		}
		
		static SubStringKey new_hostKey(String text) {
			int st=0, ed= text.length();
			int idx=text.indexOf("://");
			if(idx>0) {
				st=idx+3;
			}
			idx=text.indexOf("/", st);
			if(idx>0) {
				ed=idx;
			}
			return new SubStringKey(text, st, ed);
		}
		
		SubStringKey(String text, int st, int ed) {
			this.st = st;
			this.ed = ed;
			this.text = text;
			this.hash = Utils.hashCode(text, st, ed);
			this.len = ed-st;
		}
		
		@Override
		public boolean equals(Object o) {
			if (!(o instanceof SubStringKey)) return false;
			SubStringKey that = (SubStringKey) o;
			return this==that || len==that.len && text.regionMatches(st, that.text, that.st, len);
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
		
		@NonNull @Override
		public String toString() {
			return text.substring(st, ed);
		}
	}
	
	/** 以荆轲之鞘纳山川之峰脊破阻业之障 */
	HashMap<SubStringKey, String> jinkeSheaths = new HashMap<>();
	
	Map<HostKey, ArrayList<SiteRule>> SiteConfigs = Collections.synchronizedMap(new HashMap<>());
	ArrayList<Pair<Pattern, SiteRule>> SiteConfigsByPattern = new ArrayList<>();
	private View appToast;
	public long lastTitleSuppressTime;
	
	public boolean dismissAppToast() {
		if(appToast!=null&&appToast.getVisibility()==View.VISIBLE) {
			appToast.setVisibility(View.GONE);
			return true;
		}
		return false;
	}
	
	public static class SiteRule {
		long id;
		String quantifier;
		String JS = null;
		int EnRipenPercent = -1;
		Object[] pruneRules;
		boolean forbidScrollWhenSelecting;
		boolean pauseJs;
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
	
	LinkedList<RgxPlc> DNSIntelligence = new LinkedList<>();
	
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
			Pattern p = Pattern.compile("^https?://www.zhihu.com/tardis/sogou/ans");
			SiteRule rule = new SiteRule();
			//rule.EnRipenPercent = 80;
			rule.JS = "var items=document.getElementsByClassName('sgui-slide-down');if(items.length==1) items[0].click();" +
					"items=document.getElementsByClassName('AuthorInfo AnswerItem-authorInfo AnswerItem-authorInfo--related')[0];if(items)items.onclick=function(){window.location='zhihu://answer/'+/[0-9]+/.exec(window.location.href)[0]}";
			rule.forbidScrollWhenSelecting = true;
			//rule.pauseJs = true;
			DNSIntelligence.add(new RgxPlc(Pattern.compile("^https?://www.zhihu.com/question/.+/answer/([0-9]+).*")
				,"https://www.zhihu.com/tardis/sogou/ans/$1"));
			rule.pruneRules = new Object[2]; // pIRl
			rule.pruneRules[0] = new IISTri(42, -402077131, "js", null);
			rule.pruneRules[1] = new IISTri(51, -1941194295, "ut", ".App{height:auto!important}");
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
	
	@SuppressLint("SourceLockedOrientationActivity")
	public void fixVideoFullScreen() {
		if(bShowCustomView){
			mOldOri = a.mConfiguration.orientation;
			//int mode = opt.getFullScreenLandscapeMode();
			boolean bSwitch = true;
			if(bSwitch)
				a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			bShowCustomView =false;
		}
	}
	public WebChromeClient mWebClient = new WebClient();
	
	class WebClient extends WebChromeClient {
		private File filepickernow;
		Dialog d; View cv;
		
		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
			CMN.Log("onCreateWindow", isDialog);
			return false;
		}
		
		@Override
		public void onShowCustomView(View view, CustomViewCallback callback) {
			//CMN.Log("onShowCustomView", mdict._req_fvw, mdict._req_fvh);
			bShowCustomView = true;
			if(true)fixVideoFullScreen();
			if(d ==null){
				d = new Dialog(a);
				d.setCancelable(false);
				d.setCanceledOnTouchOutside(false);
			}
			
			d.show();
			
			Window window = d.getWindow();
			window.setDimAmount(1);
			WindowManager.LayoutParams layoutParams = window.getAttributes();
			layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.horizontalMargin = 0;
			layoutParams.verticalMargin = 0;
			window.setAttributes(layoutParams);
			
			window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			window.getDecorView().setBackground(null);
			window.getDecorView().setPadding(0,0,0,0);
			if(view!=null) d.setContentView(cv=view);
		}
		
		@Override
		public void onHideCustomView() {
			bShowCustomView = false;
			//if (mOldOri==Configuration.ORIENTATION_PORTRAIT&&mConfiguration.orientation==Configuration.ORIENTATION_LANDSCAPE) {
			a.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			//}
			if(d !=null){
				d.hide();
			}
			if(cv!=null && cv.getParent()!=null){
				((ViewGroup)cv.getParent()).removeView(cv);
				cv=null;
			}
			CustomViewHideTime = System.currentTimeMillis();
		}
		
		@Override
		public void onReceivedTitle(WebView view, String title) {
			AdvancedBrowserWebView mWebView = (AdvancedBrowserWebView) view;
			mWebView.transientTitle = title;
			mWebView.holder.title = title;
			if(mWebView==a.currentWebView) {
				if(lastTitleSuppressTime !=0) {
					if(CMN.now()-lastTitleSuppressTime<400) {
						return;
					}
					lastTitleSuppressTime =0;
					a.root.removeCallbacks(a.postRectifyWebTitleRunnable);
					title = mWebView.rectifyWebStacks(title);
				}
				a.webtitle.setText(title);
			}
		}
		
		/** 进度变化的回调接口。 进度大于98时强行通知加载完成。<br/>
		 * see {@link WebCompoundListener#onPageFinished}*/
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			CMN.Log("OPC::", newProgress, Thread.currentThread().getId());
			AdvancedBrowserWebView mWebView = (AdvancedBrowserWebView) view;
			if(mWebView==a.currentWebView && mWebView.PageStarted) {
				a.tabsManagerIsDirty =true;
				mWebView.isloading=true;
				boolean premature=mWebView.getDelegate(BackendSettings).getPremature();
				int lowerBound = mWebView.EnRipenPercent;
				if(lowerBound<=10) {
					lowerBound=98;
				}
//				if(premature) {
//					lowerBound=Math.min(80, lowerBound);
//				}
				if(newProgress>=lowerBound){
					CMN.Log("newProgress>=98", newProgress, premature, lowerBound);
					a.fadeOutProgressbar();
					if(mWebView.PageStarted) {
						mWebView.postFinished();
					}
					if(premature) {
						mWebView.stopLoading();
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
		if(PrintStartTime>0 && System.currentTimeMillis()- PrintStartTime <5350){
			if(oldScale!=newScale) {
				CMN.Log("re_scale...");
				view.setScaleX(a.printScale);
				view.setScaleY(a.printScale);
			}
			return;
		}
		WebViewmy mWebView = ((WebViewmy)view);
		mWebView.webScale=newScale;
	}
	
	@Override
	public void onLoadResource(WebView view, String url) {
		super.onLoadResource(view, url);
		WebViewmy mWebView=((WebViewmy)view);
		//CMN.Log("onLoadResource", url);
//			if(url.contains(".mp4")){
//				Message msg = new Message();
//				msg.what=110;
//				msg.obj=url;
//				mHandler.sendMessage(msg);
//			}
	}
	
	HostKey startHostMatcher = new HostKey();
	
	@Override
	public void onPageStarted(WebView view, String url, Bitmap favicon) {
		CMN.Log("onPageStarted……", url, view.getUrl(), Thread.currentThread().getId());
		AdvancedBrowserWebView mWebView = (AdvancedBrowserWebView) view;
		if(!a.opt.getUpdateUALowEnd()) {
			a.updateUserAgentString(mWebView, url);
		}
		
		// 滑动隐藏底栏  滑动隐藏顶栏 0
		// 底栏不动  滑动隐藏顶栏 1
		// 底栏不动  顶栏不动 2
		int hideBarType = 1;
		boolean PadPartPadBar = false;
		mWebView.layout.syncBarType(hideBarType, PadPartPadBar, a.UIData);
		
		//((ViewGroup.MarginLayoutParams)mWebView.layout.getLayoutParams()).bottomMargin=2*a.UIData.bottombar2.getHeight();
		mWebView.transientTitle=null;
		mWebView.holder.url=url;
		startHostMatcher.set(url);
		ArrayList<SiteRule> sm = SiteConfigs.get(startHostMatcher);
		int EnRipenPercent=Integer.MAX_VALUE;
		List<Object> prunes = mWebView.prunes;
		List<SiteRule> holder = mWebView.rules;
		prunes.clear();
		holder.clear();
		mWebView.forbidScrollWhenSelecting = false;
		boolean shutdownJs = false;
		for (Pair<Pattern, SiteRule> siteRulePair:SiteConfigsByPattern) {
			if(siteRulePair.first.matcher(url).find()) {
				SiteRule rI = siteRulePair.second;
				holder.add(rI);
				if(rI.pruneRules!=null) {
					prunes.addAll(Arrays.asList(rI.pruneRules));
				}
				mWebView.forbidScrollWhenSelecting |= rI.forbidScrollWhenSelecting;
				shutdownJs |= rI.pauseJs;
				if(rI.EnRipenPercent>10) {
					EnRipenPercent=Math.min(EnRipenPercent, rI.EnRipenPercent);
				}
			}
		}
		if(shutdownJs) {
			mWebView.shutdownJS();
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
		mWebView.EnRipenPercent=EnRipenPercent;
		mWebView.PageStarted=true;
		mWebView.PageVersion++;
		mWebView.PageFinishedPosted=false;
		if(mWebView==a.currentWebView) {
			a.updateProgressUI();
		}
	}
	
	
	
	/**
		if (!window.PPTurboKit) {
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
			var items = h.getElementsByTagName('meta');
			var len = items.length;
			var tag = 'viewport';
			var content = 'width=device-width,minimum-scale=1,maximum-scale=5.0,user-scalable=yes';
			var add = 0;
	 		if(add)
			for (var i = 0; i < len; i++) {
				var iI = items[i];
				if (iI.name === tag) {
					if (iI.content.lastIndexOf('no') > 0) {
						iI.content = content;
					}
					add = 0;
					break;
				}
			}
			if (add) {
				var item = d.createElement('meta');
				item.name = tag;
				item.content = content;
				h.appendChild(item);
			}
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
	 			ele
			};
	 		w._docAnnots="";
	 		w._docAnnott="";
		}
	 	polyme.logm('1','2','3');
	 */
	@Multiline(trim=true, compile=true)
	public final static String PRI = "Primary Rule Insersion";
	
	public static byte[] markjsBytesArr = null;
	
	@Multiline(file="ActivityLauncher\\src\\main\\assets\\mark.js")
	public static int markjsBytesLen = 0;
	
	/** 加载完成的回调接口。<br/>
	 * 注入基础规则。<br/>
	 * 注入网页规则。<br/>
	 * 处理搜索和笔记的高亮。<br/>
	 * 处理历史记录。<br/>
	 * see {@link WebClient#onProgressChanged}<br/>*/
	@Override public void onPageFinished(WebView view, String url) {
		AdvancedBrowserWebView mWebView=((AdvancedBrowserWebView)view);
		if(mWebView.PageStarted) {
			String ordinalUrl=view.getUrl();
			if(ordinalUrl!=null) {
				url = ordinalUrl;
			}
			mWebView.holder.url = url;
			CMN.Log("OPF:::", url, view.getTitle(), Thread.currentThread().getId());
			//CMN.Log("OPF:::", mWebView.holder.url);
			view.setTag(url);
			mWebView.removePostFinished();
			mWebView.PageStarted=false;
			mWebView.holder.url=url;
			String title = mWebView.getTitle();
			mWebView.transientTitle=title;
			mWebView.holder.title=title;
			mWebView.time=System.currentTimeMillis();
			mWebView.incrementVerIfAtNormalPage();
			mWebView.lastScroll=view.getScrollY();
			
//			boolean bEnableJavaScript = mWebView.getSettings().getJavaScriptEnabled();
//
//			if(!bEnableJavaScript)
//				mWebView.getSettings().setJavaScriptEnabled(true);
			
			//mWebView.postReviveJS(0);
			mWebView.reviveJS();
			
			/* 原天道契 */
			mWebView.evaluateJavascript(PRI, null);
			
			if(mWebView.rules.size()>0) {
				for (SiteRule rI:mWebView.rules) {
					if(rI.JS!=null/* && rI.host.equals(finishHostMatcher)*/) {
						mWebView.evaluateJavascript(rI.JS, null);
					}
				}
			}
			//mWebView.evaluateJavascript("polyme.craft('style', 'body{padding-bottom:450px !important;}')", null);
			
//			if(!bEnableJavaScript) {
//				mWebView.postDelayed(() -> mWebView.getSettings().setJavaScriptEnabled(false), 1350);
//			}
			
			//todo 高亮
			boolean MarkJsRequired=true;
			if(MarkJsRequired&&WebCompoundListener.markjsBytesArr==null) {
				ensureMarkJS(a);
			}
			//todo 笔记，先复原笔记，后上高亮，但是因为笔记最后会用到高亮的功能，所以加载笔记的同时加载高亮支持。
			CMN.rt();
			Cursor c = a.historyCon.queryNote(url);
			CMN.pt("搜索完毕...", c.getCount(), url);
			//if(false)
			if(c.getCount()>0) {
				c.moveToFirst();
				CMN.Log("搜索完毕...", c.getString(0));
				//"0/0/2/0/1/5/0/4/0/1/2/2:63,0/0/2/0/1/5/0/4/0/1/2/2:65|0\\n0/0/2/0/1/5/0/4/0/1/2/2:24,0/0/2/0/1/5/0/4/0/1/2/2:26|0\\n0/0/2/0/1/5/0/4/0/1/2/2:15,0/0/2/0/1/5/0/4/0/1/2/2:17|0\\n"
				mWebView.evaluateJavascript(WebViewmy.getResoreHighLightIncantation(c.getString(0)), new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						CMN.Log("asd", value);
					}
				});
			}
			
			if(false)
				mWebView.evaluateJavascript("polyme.highlight('aut')", new ValueCallback<String>() {
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
			if(mWebView.clearHistroyRequested) {
				mWebView.clearHistory();
				mWebView.clearHistroyRequested=false;
			}
			CMN.rt();
			a.historyCon.insertUpdateBrowserUrl(url, title);
			CMN.pt("历史插入时间：");
			if(mWebView.holder.getLuxury()) {
				int idx = mWebView.getThisIdx();
				if(idx>0) {
					mWebView.layout.getChildAt(idx-1).setVisibility(View.GONE);
				}
			}
		}
	}
	
	public static void ensureMarkJS(Context context) {
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
	
	@Override
	public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		handler.proceed();
	}
	
	public boolean shouldOverrideUrlLoading(WebView view, String url)
	{
		CMN.Log("SOUL::", url, Thread.currentThread().getId());
		AdvancedBrowserWebView mWebView = (AdvancedBrowserWebView) view;
		if(mWebView.forbidLoading) {
			return true;
		}
		if(!url.regionMatches(false, 0, "http", 0, 4)
				&&!url.regionMatches(false, 0, "https", 0, 5)) {
			//if(a.webtitle.getVisibility()== View.VISIBLE) a.etSearch.setText(url);
			if(true) {
				int idx = url.indexOf(":");
				String appScheme = url;
				if(idx>0) {
					appScheme = Utils.getSubStrWord(appScheme, 0, idx);
				}
				if(appToast==null) {
					appToast = a.UIData.appToast.getViewStub().inflate();
					TextView tv = appToast.findViewById(R.id.confirm_button);
					tv.setOnClickListener(v -> {
						String appUrl = (String) appToast.getTag();
						if(appUrl!=null)
						try {
							a.startActivity(new Intent(Intent.ACTION_VIEW
									, Uri.parse(appUrl)));
							appToast.setTag(null);
						} catch (Exception e) {
							CMN.Log(e);
							a.showT("跳转失败…");
						}
					});
				}
				appToast.setVisibility(View.VISIBLE);
				appToast.setTag(url);
				TextView tv = appToast.findViewById(R.id.text1);
				tv.setText(appScheme+" 请求打开外部APP");
				float targetY = 10 * GlobalOptions.density;
				Runnable runnable = (Runnable) tv.getTag();
				if(runnable==null) {
					AnimatorListenerAdapter animaLis = new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							appToast.setVisibility(View.GONE);
						}
					};
					tv.setTag(runnable = () -> appToast.animate()
							.alpha(0)
							.translationY(targetY)
							.setListener(animaLis));
				}
				appToast.removeCallbacks(runnable);
				appToast.setAlpha(0);
				appToast.setTranslationY(targetY);
				appToast.animate()
						.alpha(1)
						.translationY(0)
						.setListener(null)
				;
				appToast.postDelayed(runnable, 2350+180);
			}
			return true;
		}
		if(mWebView==a.currentWebView && mWebView.holder.getLuxury()) {
			long now = CMN.now();
			if(now>0&&now-a.supressingNxtLux<350) {
				CMN.Log("supressingNxtLux", url);
				mWebView.setNavStacksDirty();
				return false;
			}
			a.supressingNxtLux = now;
			if(!mWebView.hasValidUrl()) {
				mWebView.clearHistroyRequested=true;
			} else {
				a.LuxuriouslyLoadUrl(mWebView, url);
				return true;
			}
		}
		
		//view.loadUrl(url);
		mWebView.setNavStacksDirty();
		return false;
	}
	
	@Override
	public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
		AdvancedBrowserWebView mWebView = (AdvancedBrowserWebView) view;
		//CMN.Log("SIR::", url, url.length(), Thread.currentThread().getId());
		//CMN.Log(url.startsWith("https://mark.js"), markjsBytesArr!=null);
		if(mWebView.prunes.size()>0) {
			for(Object pI:mWebView.prunes) {
				if(pI instanceof IISTri) {
					IISTri tri = (IISTri) pI;
					int len = url.length();
					if(len>=tri.i1&&url.regionMatches(tri.i1-(len=tri.str1.length()), tri.str1, 0, len)&&Utils.hashCode(url, 0, tri.i1)==tri.i2) {
						CMN.Log("pIRl", url);
						if(tri.plc==null) {
							return emptyResponse;
						} else {
							return new WebResourceResponse("text/css","utf8", new ByteArrayInputStream(tri.plc.getBytes()));
						}
					}
				}
			}
		}
		if(url.startsWith("polyme://")) {
			CMN.Log("polyme://", url);
			try {
				if(url.contains(".js")) {
					return new WebResourceResponse("text/javascript","utf8", a.getAssets().open("3"));
				} else {
					return new WebResourceResponse("text/html","utf8", a.getAssets().open("2"));
				}
			} catch (IOException e) {
				CMN.Log(e);
			}
		}
		
		
		//CMN.Log("request.getUrl().getScheme()", request.getUrl().getScheme());
		if(url.length()<=20) {
			if(url.startsWith("https://mark.js")&&markjsBytesArr!=null) {
				//CMN.Log("加载中", new String(markjsBytesArr, 0, 200));
				WebResourceResponse ret = new WebResourceResponse("text/javascript", "utf8", new ByteArrayInputStream(markjsBytesArr));
				return ret;
			}
		}
		
		if(true) return null;
		if(false) {
			if(url.contains("?mid=")){
//				Map<String, String> keyset = hea;
//				for (String key:keyset.keySet()) {
//					CMN.Log("keyset : ", key, " :: ",keyset.get(key));
//				}
			
			}
			return null;
		}
		if(url.contains(".mp4")){
			if(false){
//				Map<String, String> keyset = request.getRequestHeaders();
//				for (String key:keyset.keySet()) {
//					CMN.Log("keyset : ", key, " :: ",keyset.get(key));
//				}
				CMN.Log();
				try {
					SSLContext sslcontext = SSLContext.getInstance("TLS");
					sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());
					HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
					boolean succ=false;
					//url=timePattern.matcher(url).replaceAll("t="+(System.currentTimeMillis()/1000));
					String fname = requestPattern.matcher(url).replaceAll("");
					fname = fname.substring(fname.lastIndexOf("/"));
					File path=new File("/sdcard/Download/", fname);
					Object obj;
					CMN.Log("shouldInterceptRequest ", url, "to", fname);
					WebResourceResponse response = null;
					if(!path.exists()){
						Exception e = null;
						try {
							Message msg = new Message();
							msg.what=101;
							msg.obj="文件正在下载中…";
							//mHandler.sendMessage(msg);
							URL requestURL = new URL(url);
							
							HttpRequestUtil.HeadRequestResponse headRequestResponse = HttpRequestUtil.performHeadRequest(url);
							Map<String, List<String>> headerMap = headRequestResponse.getHeaderMap();
							if (headerMap == null || !headerMap.containsKey("Content-Length") || headerMap.get("Content-Length").size()==0) {
								//检测失败，未找到Content-Type
								Log.d("DownloadManager", "fail 未找到Content-Length taskUrl=" + url);
							}
							long size = 0;
							try {
								size = Long.parseLong(headerMap.get("Content-Length").get(0));
							}catch (NumberFormatException e1){
								e1.printStackTrace();
								Log.d("DownloadManager", "NumberFormatException", e);
							}
							
							url = headRequestResponse.getRealUrl();
							
							HttpRequestUtil.save2File(HttpRequestUtil.sendGetRequest(url), path.getAbsolutePath());
							
							//if(url_file_recorder==null) url_file_recorder = new FileOutputStream("/sdcard/file-url-list.txt", true);
							//url_file_recorder.write((mWebView.url+TitleSep+path.getName()).getBytes());
							//url_file_recorder.flush();
							response = null;//new WebResourceResponse("*/*","UTF-8",new ByteArrayInputStream(bos.getBytes(), 0, bos.getCount()));
						} catch (Exception ex) {
							e=ex;
							ex.printStackTrace();
						}
						if(path.exists())
							obj="已下载！"+(e==null?"":e.toString());
						else
							obj="下载出错"+e;
					}
					else{
						obj="已存在，跳过";
					}
					Message msg = new Message();
					msg.what=101;
					msg.obj=obj;
					//todo a.mHandler.sendMessage(msg);
					if(response!=null)
						return response;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			Map<String, String> keyset = null;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				//keyset = request.getRequestHeaders();
			} else {
				keyset = new HashMap<>();
			}
			for (String key:keyset.keySet()) {
				CMN.Log("keyset : ", key, " :: ",keyset.get(key));
			}
			CMN.Log();
			try {
				SSLContext sslcontext = SSLContext.getInstance("TLS");
				sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
				boolean succ=false;
				//url=timePattern.matcher(url).replaceAll("t="+(System.currentTimeMillis()/1000));
				String fname = requestPattern.matcher(url).replaceAll("");
				fname = fname.substring(fname.lastIndexOf("/"));
				File path=new File("/sdcard/Download/", fname);
				Object obj;
				CMN.Log("shouldInterceptRequest ", url, "to", fname);
				WebResourceResponse response = null;
				if(!path.exists()){
					Exception e = null;
					try {
						Message msg = new Message();
						msg.what=101;
						msg.obj="文件正在下载中…";
						//mHandler.sendMessage(msg);
						URL requestURL = new URL(url);
						String val;
						HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
						val=keyset.get("chrome-proxy");
						if(val!=null)urlConnection.setRequestProperty("chrome-proxy", val);
						val=keyset.get("Accept-Encoding");
						if(val!=null)urlConnection.setRequestProperty("Accept-Encoding", val);
						val=keyset.get("Range");
						if(val!=null)urlConnection.setRequestProperty("Range", val);
						val=keyset.get("Accept");
						if(val!=null)urlConnection.setRequestProperty("Accept", val);
						urlConnection.setRequestMethod("GET");
						if(urlConnection instanceof HttpsURLConnection){
							HttpsURLConnection urlsConnection = (HttpsURLConnection) urlConnection;
							urlsConnection.setHostnameVerifier((hostname, session) -> true);
						}
						
						
						////urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());
						//urlConnection.setRequestProperty("Accept-Language", "zh-CN");
						//urlConnection.setRequestProperty("Referer", url.toString());
						urlConnection.setConnectTimeout(60000);
						//urlConnection.setRequestProperty("Charset", "UTF-8");
						//urlConnection.setRequestProperty("Connection", "Keep-Alive");
						val=keyset.get("User-Agent");
						urlConnection.setRequestProperty("User-agent", (val!=null?val:"Mozilla/5.0 (Linux; Android 9; VTR-AL00 Build/HUAWEIVTR-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36")+System.currentTimeMillis());
						urlConnection.connect();
						InputStream is = urlConnection.getInputStream();
						
						ReusableByteOutputStream bos = new ReusableByteOutputStream();
						bos.reset();
						byte[] buffer = new byte[1024*10];
						int len;
						try {
							while ((len = is.read(buffer)) > 0) {
								bos.write(buffer, 0, len);
							}
						} catch (Exception ex) {
							e=ex;
							CMN.Log("cccrrr", url);
						}
						urlConnection.disconnect();
						is.close();
						BU.printFile(bos.getBytes(), 0, bos.size(), path.getAbsolutePath());
						if(a.url_file_recorder==null) a.url_file_recorder = new FileOutputStream("/sdcard/file-url-list.txt", true);
						a.url_file_recorder.write((mWebView.holder.url+TitleSep+path.getName()).getBytes());
						a.url_file_recorder.flush();
						response = new WebResourceResponse("*/*","UTF-8",new ByteArrayInputStream(bos.getBytes(), 0, bos.getCount()));
					} catch (Exception ex) {
						e=ex;
						ex.printStackTrace();
					}
					if(path.exists())
						obj="已下载！"+(e==null?"":e.toString());
					else
						obj="下载出错"+e;
				}
				else{
					obj="已存在，跳过";
				}
				Message msg = new Message();
				msg.what=101;
				msg.obj=obj;
				//todo a.mHandler.sendMessage(msg);
				if(response!=null)
					return response;
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		CMN.Log("DOWNLOAD:::", url, contentDisposition, mimetype, contentLength);
		if(appToast!=null && appToast.getVisibility()==View.VISIBLE) {
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
	public void onScrollChange(View v, int scrollX, int scrollY, int oldx, int oldy) {
		AdvancedBrowserWebView webview = (AdvancedBrowserWebView) v;
		if(PrintStartTime>0 && System.currentTimeMillis()-CustomViewHideTime<5350){
			CMN.Log("re_scroll...", a.focused);
			webview.SafeScrollTo(a.printSX, a.printSY);
			webview.requestLayout();
			return;
		}
		boolean scrollforbid;
		//CMN.Log("onScrollChange", scrollY-oldy);
		if(webview.forbidScrollWhenSelecting && webview.bIsActionMenuShown && oldy-scrollY>200
			|| CustomViewHideTime>0 && System.currentTimeMillis()-CustomViewHideTime<350){
			CMN.Log("re_scroll...");
			webview.SafeScrollTo(oldx, oldy);
			return;
		}
	}
	
	@JavascriptInterface
	public void SaveAnnots(long tabID, String annots, String texts) {
		CMN.Log("SaveAnnots", tabID, annots, texts, a.historyCon.isOpen());
		//if(true) return;
		AdvancedBrowserWebView mWebView = a.getWebViewFromID(tabID);
		if(mWebView!=null && mWebView.HLED) {
			mWebView.HLED=false;
			//a.root.removeCallbacks()
			long ret=-1;
			if(a.historyCon.isOpen()) {
				CMN.rt("保存中……");
				try {
					ret = a.historyCon.insertUpdateNote(mWebView.holder.url, annots, texts);
				} catch (Exception e) {
					CMN.Log(e);
				}
				CMN.pt("保存了……", ret, mWebView.holder.url);
			}
		}
	}
	
	@JavascriptInterface
	public void log(String msg) {
		CMN.Log(msg);
		
	}
	
	@JavascriptInterface
	public void logm(String[] msg) {
		CMN.Log("logm", msg.length, msg);
		CMN.Log("logm--", System.identityHashCode(msg), System.identityHashCode(msg[0]));
		
	}
	
	@JavascriptInterface
	public void sendup(long id) {
		AdvancedBrowserWebView mWebView = a.getWebViewFromID(id);
		if(mWebView==a.currentWebView) {
			//if(false)
			//if(!mWebView.bIsActionMenuShown)
			upsended = true;
			mWebView.postDelayed(new Runnable() {
				@Override
				public void run() {
					long time = CMN.now();
					MotionEvent evt = MotionEvent.obtain(time, time,MotionEvent.ACTION_DOWN, mWebView.lastX, mWebView.lastY, 0);
					mWebView.dispatchTouchEvent(evt);
					evt.setAction(MotionEvent.ACTION_UP);
					mWebView.dispatchTouchEvent(evt);
					evt.recycle();
				}
			}, Utils.version>=29?0:150);
		}
	}
}
