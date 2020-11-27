package com.knziha.polymer;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Utils.BufferedReader;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.LexicalDBHelper;
import com.knziha.polymer.Utils.MyReceiver;
import com.knziha.polymer.Utils.OptionProcessor;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.Utils.WebOptions;
import com.knziha.polymer.databinding.ActivityMainBinding;
import com.knziha.polymer.databinding.SearchHintsItemBinding;
import com.knziha.polymer.databinding.WebPageItemBinding;
import com.knziha.polymer.qrcode.QRActivity;
import com.knziha.polymer.toolkits.Utils.BU;
import com.knziha.polymer.toolkits.Utils.ReusableByteOutputStream;
import com.knziha.polymer.webslideshow.CenterLinearLayoutManager;
import com.knziha.polymer.webslideshow.RecyclerViewPager;
import com.knziha.polymer.webslideshow.ViewPagerTouchHelper;
import com.knziha.polymer.webslideshow.ViewUtils;
import com.knziha.polymer.widgets.AppIconsAdapter;
import com.knziha.polymer.widgets.DescriptiveImageView;
import com.knziha.polymer.widgets.DialogWithTag;
import com.knziha.polymer.widgets.PopupBackground;
import com.knziha.polymer.widgets.PrintPdfAgentActivity;
import com.knziha.polymer.widgets.SpacesItemDecoration;
import com.knziha.polymer.widgets.TwoColumnAdapter;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.knziha.polymer.widgets.WebViewmy;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.math.MathUtils;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.appcompat.app.GlobalOptions.realWidth;
import static com.knziha.polymer.Utils.Options.getLimitHints;
import static com.knziha.polymer.Utils.Options.getTransitSearchHints;
import static com.knziha.polymer.WeakReferenceHelper.topDomainNamesMap;
import static com.knziha.polymer.WebCompoundListener.CustomViewHideTime;
import static com.knziha.polymer.WebCompoundListener.PrintStartTime;
import static com.knziha.polymer.WebCompoundListener.httpPattern;
import static com.knziha.polymer.WebCompoundListener.requestPattern;
import static com.knziha.polymer.widgets.Utils.getViewItemByPath;
import static com.knziha.polymer.widgets.Utils.indexOf;
import static com.knziha.polymer.Utils.WebOptions.*;
import static com.knziha.polymer.widgets.Utils.setOnClickListenersOneDepth;

@SuppressWarnings({"rawtypes","ClickableViewAccessibility","IntegerDivisionInFloatingPointContext"})
public class BrowserActivity extends Toastable_Activity implements View.OnClickListener, View.OnLongClickListener {
	public final static String TitleSep="\n";
	public final static String FieldSep="|";
	public final static Pattern IllegalStorageNameFilter=Pattern.compile("[\r\n|]");
	RecyclerViewPager recyclerView;
	View viewpager_holder;
	View searchbartitle;
	TextView webtitle;
	
	private int adapter_idx;
	boolean focused = true;
	Map<Long, AdvancedBrowserWebView> id_table = Collections.synchronizedMap(new HashMap<>(1024));
	ArrayList<AdvancedBrowserWebView> Pages = new ArrayList<>(1024);
	//private ArrayList<WebViewmy> WebPool = new ArrayList<>(1024);
	private ArrayList<TabHolder> TabHolders = new ArrayList<>(1024);
	private int padWidth;
	private int itemPad;
	private int _45_;
	
	FileOutputStream url_file_recorder;
	
	MyHandler mHandler;
	
	public AdvancedBrowserWebView currentWebView;
	
	ArrayList<String> WebDicts = new  ArrayList<>(64);
	private RecyclerView.Adapter/*<ViewDataHolder<WebPageItemBinding>>*/ adaptermy;
	private RecyclerView.Adapter/*<ViewDataHolder<SearchHintsItemBinding>>*/ adaptermy2;
	boolean viewpager_holder_hasTag;
	private CenterLinearLayoutManager layoutManager;
	private static final WebResourceResponse emptyResponse = new WebResourceResponse("", "", null);
	private int mItemWidth;
	private int mItemHeight;
	private Runnable scrollHereRunnnable;
	private boolean bNeedReCalculateItemWidth;
	private ListPopupWindow searchEnginePopup;
	private PopupBackground layoutListener;
	private PopupWindow polypopup;
	private View polypopupview;
	private boolean polysearching;
	private ColorDrawable AppWhiteDrawable;
	private Drawable frame_web;
	private int mStatusBarH;
	private CoordinatorLayout.Behavior bottombarScrollBehaviour;
	private AppBarLayout.LayoutParams toolbatLP;
	private CoordinatorLayout.LayoutParams bottombarLP;
	private boolean anioutTopBotForTab=false;
	private CoordinatorLayout.Behavior scrollingBH;
	private boolean fixTopBar = false;
	private boolean fixBotBar = true;
	private View toolbarti;
	int printSX;
	int printSY;
	float printScale;
	private BrowserSlider mBrowserSlider;
	private boolean etSearch_scrolled;
	LexicalDBHelper historyCon;
	
	int HintConstituents =0;
	private Cursor ActiveUrlCursor=Utils.EmptyCursor;
	private Cursor ActiveSearchCursor=Utils.EmptyCursor;
	private boolean keyboard_hidden;
	
	ObjectAnimator progressProceed;
	ObjectAnimator progressTransient;
	Animator animatorTransient;
	Drawable progressbar_background;
	boolean supressingProgressListener;
	
	TextPaint menu_grid_painter;
	private ViewGroup menu_grid;
	long supressingNxtLux;
	public static boolean closing;
	
	WebCompoundListener mWebListener;
	private PullHintsRunnable pull_hints_runnable;
	private boolean search_hints_vising=true;
	public ActivityMainBinding UIData;
	private Resources mResource;
	private String lastUrlSet;
	private boolean goToBarcodeScanner;
	private final int RequsetUrlFromCamera=1101;
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		CMN.Log("onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
		mConfiguration.setTo(newConfig);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(recyclerView!=null && !DissmissingViewHolder) {
			calculateItemWidth(true);
		} else {
			bNeedReCalculateItemWidth = true;
		}
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		CMN.Log("onWindowFocusChanged", hasFocus);
		if(checkFlagsChanged()){
			opt.setFlags(null, 1);
			FFStamp=opt.FirstFlag();
			SFStamp=opt.SecondFlag();
			TFStamp=opt.ThirdFlag();
		}
	}
	
	@Override
	public void onBackPressed() {
		if(checkWebSelection()) {
		
		} else if(searchEnginePopup!=null && searchEnginePopup.isShowing()) {
			searchEnginePopup.dismiss();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CMN.Log("getting mid 0 ...", Thread.currentThread().getId());
		
		TestHelper.testAddWebDicts(WebDicts);
		
		Window win = getWindow();
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		boolean transit = Options.getTransitSplashScreen();
		if(!transit) setTheme(R.style.AppThemeRaw);
		
		UIData = DataBindingUtil.setContentView(this, R.layout.activity_main);
		
		root=UIData.root;
		
		mWebListener = new WebCompoundListener(this);
		
		if(transit){
			root.setAlpha(0);
			ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(root, "alpha", 0, 1);
			fadeInContents.setInterpolator(new AccelerateDecelerateInterpolator());
			fadeInContents.setDuration(350);
			fadeInContents.addListener(new Animator.AnimatorListener() {
				@Override public void onAnimationStart(Animator animation) { }
				@Override public void onAnimationEnd(Animator animation) {
					win.setBackgroundDrawable(null);
				}
				@Override public void onAnimationCancel(Animator animation) { }
				@Override public void onAnimationRepeat(Animator animation) { }
			});
			root.post(fadeInContents::start);
		}
		
		CMN.mResource = mResource = getResources();
		
		frame_web = mResource.getDrawable(R.drawable.frame_web);
		
		setOnClickListenersOneDepth(UIData.bottombar2, this, 1, null);
		setOnClickListenersOneDepth(UIData.toolbarContent, this, 1, null);
		
		UIData.browserWidget10.setOnLongClickListener(this);
		
		etSearch=UIData.etSearch;
		progressbar_background=UIData.progressbar.getBackground();
		webtitle=UIData.webtitle;
		toolbatLP=(AppBarLayout.LayoutParams) UIData.toolbar.getLayoutParams();
		bottombarLP=(CoordinatorLayout.LayoutParams) UIData.bottombar2.getLayoutParams();
		bottombarScrollBehaviour = bottombarLP.getBehavior();
		scrollingBH = new AppBarLayout.ScrollingViewBehavior(getBaseContext(), null);
		
		decideTopBotBH();
		progressProceed = ObjectAnimator.ofInt(progressbar_background,"level", 0, 0);
		progressProceed.setDuration(100);
		progressTransient = ObjectAnimator.ofFloat(UIData.progressbar, "alpha", 0, 0);
		AnimatorSet transientAnimator = new AnimatorSet();
		transientAnimator.playTogether(progressTransient, progressProceed);
		transientAnimator.setDuration(100);
		animatorTransient = transientAnimator;
		animatorTransient.addListener(new Animator.AnimatorListener() {
			@Override public void onAnimationStart(Animator animation) { }
			@Override public void onAnimationEnd(Animator animation) {
				progressbar_background.setLevel(0);
				UIData.progressbar.setVisibility(View.GONE);
				UIData.ivRefresh.setImageResource(R.drawable.ic_refresh_black_24dp);
			}
			@Override public void onAnimationCancel(Animator animation) {
				UIData.progressbar.setAlpha(1);
			}
			@Override public void onAnimationRepeat(Animator animation) { }
		});
		checkLog(savedInstanceState);
		CrashHandler.getInstance(this, opt).TurnOn();
		setStatusBarColor(getWindow());
		WebView.setWebContentsDebuggingEnabled(true);
		mHandler = new MyHandler(this);
		Utils.PadWindow(root);
	}
	
	private Drawable bottombar2Background() {
		return UIData.bottombar2.getBackground();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		focused=false;
		if(systemIntialized) {
			if (currentWebView != null) {
				currentWebView.saveIfNeeded();
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//PrintStartTime=0;
		focused=true;
	}
	
	@Override
	protected void further_loading(Bundle savedInstanceState) {
		historyCon = new LexicalDBHelper(this, opt);
		CheckGlideJournal();
		checkMargin(this);
		_45_ = (int) mResource.getDimension(R.dimen._45_);
		WebViewmy.minW = Math.min(512, Math.min(dm.widthPixels, dm.heightPixels));
		menu_grid_painter = DescriptiveImageView.createTextPainter();
		UIData.browserWidget10.textPainter = menu_grid_painter;
		
		final File def = new File(getExternalFilesDir(null),"sites_default.txt");
		/* !!!原配 */
		if(def.exists()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(def));
				String line;
				while((line=in.readLine())!=null) {
					boolean via = false;
					boolean set = false;
					TabHolder holder = new TabHolder();
					String[] args = line.split(TitleSep);
					//CMN.Log("processing...", args);
					try{
						if(args.length==2) {
							holder.url = args[0];
							args = args[1].split("\\|");
							int len = args.length;
							if(len>0) {
								if(args[len-1].equals("=")) {
									via=true;
									len--;
								}
							}
							if(len>0) {
								//CMN.Log("processing 1..", args);
								holder.id = validifyid(Math.abs(Long.parseLong(args[0])));
								set=true;
								if(len>1) {
									holder.flag = Long.parseLong(args[1]);
								}
								if(len>2) {
									holder.title = args[2];
								}
								if(len>3) {
									holder.page_search_term = args[3];
								}
							}
						}
					} catch (Exception e) {
						CMN.Log(e);
					}
					if(set) {
						TabHolders.add(holder);
						if(via) {
							adapter_idx=TabHolders.size()-1;
						}
					}
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* !!!原装 */
		if(TabHolders.size()==0) {
			TabHolder tab0=new TabHolder();
			tab0.url="https://www.bing.com";
			tab0.id=CMN.now();
			TabHolders.add(tab0);
		}
		int size = TabHolders.size();
		for (int i = 0; i < size; i++) {
			Pages.add(null);
		}
		CMN.Log("ini.初始化完毕", size);
		
		AttachWebAt(adapter_idx, false);
		//SetupPasteBin();
		
		webtitle.setOnClickListener(this);
		
		mBrowserSlider = new BrowserSlider();
		
		UIData.bottombar2.setOnTouchListener(mBrowserSlider);
		
		UIData.toolbarContent.setOnTouchListener(mBrowserSlider);
		
		etSearch.setSelectAllOnFocus(opt.getSelectAllOnFocus());
		
		etSearch.setOnScrollChangedListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
			etSearch_scrolled=true;
			mBrowserSlider.supressed=true;
		});
		
		if(false)
		etSearch.setOnFocusChangeListener((v, hasFocus) -> {
			boolean focused = v.hasFocus();
			webtitle_setVisibility(focused);
		});
		
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (currentWebView!=null && actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
				v.clearFocus();
				execBrowserGoTo(null);
			}
			return false;
		});
		
		//tc
		etSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				CMN.Log("afterTextChanged");
				if(webtitle.getVisibility()!=View.VISIBLE) {
					goToBarcodeScanner=s.length()==0;
					updateQRBtn();
				}
				if(search_bar_vis()) {
					pull_hints(false);
				}
			}
		});
		//tg
		//TestHelper.async(TestHelper::downloadlink);
		
//		StandardConfigDialog holder = buildStandardConfigDialog(BrowserActivity.this, null, R.string.global_vs_tabs);
//		holder.init_web_configs(true, 1);
//		holder.dlg.show();
		
		WebView.setWebContentsDebuggingEnabled(true);
		
		//TestHelper.insertMegaUrlDataToHistory(historyCon, 2500);
		
		systemIntialized=true;
	}
	
	private void updateQRBtn() {
		CMN.Log("updateQRBtn", goToBarcodeScanner);
		TextView QRBtn = UIData.browserWidget5;
		if(QRBtn.getTag()!=null^goToBarcodeScanner) {
			QRBtn.setText(goToBarcodeScanner?"扫码":"前往");
			
			QRBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
					mResource.getDrawable(goToBarcodeScanner?R.drawable.ic_baseline_qrcode:R.drawable.abc_ic_go_search_api_material)
					, null, null, null);
			QRBtn.setTag(goToBarcodeScanner?Utils.DummyTransX:null);
		}
	}
	
	private void execBrowserGoTo(String text) {
		if(text==null) {
			text = etSearch.getText().toString();
		}
		int _1stDot = text.indexOf(".");
		boolean hasNecessaryUrlPart = _1stDot>0;
		boolean isUrl = false;
		int len = text.length();
		if(hasNecessaryUrlPart) {
			int schemaIdx = text.indexOf(":");
			if(schemaIdx>0 && len>schemaIdx+1) {
				if(schemaIdx==4||schemaIdx==5) {
					isUrl=text.regionMatches(true, 0, "http", 0, 4);
				}
				if(!isUrl) {
					if(schemaIdx==4) {
						isUrl=text.regionMatches(true, 0, "file", 0, 4);
					} else if(schemaIdx==3) {
						isUrl=text.regionMatches(true, 0, "ftp", 0, 3);
					}
				}
			}
			if(!isUrl) {
				int idx = text.indexOf("/", _1stDot);
				if(idx<0) {
					idx = text.length();
				}
				if(idx>0) {
					int suffix = text.lastIndexOf(".", idx-1);
					if(suffix>0 && suffix<idx-1) {
						String topDomain = text.substring(suffix+1, idx).toLowerCase();
						isUrl = topDomainNamesMap.contains(topDomain);
						CMN.Log("topDomain", topDomain);
						if(!isUrl) { //ipv4
							suffix = topDomain.indexOf(":");
							if(suffix>0&&suffix<=3) {
								Pattern ipv4 = Pattern.compile("(([0-9]{1,3}\\.){3}[0-9]{1,3}):([0-9]?)(.*)");
								Matcher m = ipv4.matcher(text);
								if(m.matches()) {
									isUrl=true;
									if(m.group(1).length()==0) {
										text = m.replaceFirst("$1:80$3");
									}
								}
							}
						}
					}
				}
				if(isUrl) {
					text = "https:"+text;
				}
			}
		}
		//isUrl=true;
		if(!isUrl) {
			historyCon.insertSearchTerm(text);
			text = WebDicts.get(0).replace("%s", text);
		}
		CMN.Log("isUrl", isUrl, text, currentWebView.getOriginalUrl());
		if(currentWebView.holder.getLuxury() && !currentWebView.equalsUrl(text)) {
			LuxuriouslyLoadUrl(currentWebView, text);
		} else {
			currentWebView.loadUrl(text);
		}
		webtitle_setVisibility(false);
		etSearch_clearFocus();
	}
	
	private void webtitle_setVisibility(boolean invisible) {
		int targetVis = invisible ? View.INVISIBLE : View.VISIBLE;
		CMN.Log("webtitle_setVisibility", invisible);
		if(targetVis!=webtitle.getVisibility()) {
			if(invisible) {
				String urlNow=currentWebView.getUrl();
				if(!StringUtils.equals(urlNow, lastUrlSet)) {
					CMN.Log("设置了 设置了");
					etSearch.setText(lastUrlSet=currentWebView.getUrl());
				}
			}
			webtitle.setVisibility(targetVis);
			if(getTransitSearchHints()) {
				UIData.searchHints.setVisibility(View.INVISIBLE);
			}
			targetVis = invisible?View.VISIBLE:View.INVISIBLE;
			etSearch.setVisibility(targetVis);
			UIData.searchbar.setVisibility(targetVis);
			//todo search history fragment
			if(invisible) {
				keyboard_hidden=false;
				init_searint_layout();
				pull_hints(true);
			}
			shrinkToolbarWidgets(invisible);
		}
	}
	
	private void shrinkToolbarWidgets(boolean shrinkWidth) {
		ViewGroup svp = UIData.toolbarContent;
		for (int i = 0; i < svp.getChildCount(); i++) {
			View ca = svp.getChildAt(i);
			if(ca instanceof ImageView) {
				ViewGroup.LayoutParams lp = ca.getLayoutParams();
				int pad = ca.getPaddingTop();
				int padpad = (int) (dm.density*5);
				lp.width=shrinkWidth?(int) (_45_-2*pad+padpad):_45_;
				padpad = shrinkWidth?padpad/2:pad;
				ca.setPadding(padpad, pad, padpad, pad);
			}
		}
	}
	
	class BrowserSlider implements View.OnTouchListener {
		private boolean dragging;
		private boolean sliding;
		private boolean selecting;
		private float orgX;
		private float orgY;
		private float lastX;
		private float lastY;
		private View webla;
		private int adapter_to;
		ObjectAnimator resitu = ObjectAnimator.ofFloat(currentWebView.layout, "translationX", 0, 0);
		ObjectAnimator resitu1 = ObjectAnimator.ofFloat(currentWebView.layout, "translationX", 0, 0);
		ObjectAnimator resitu2 = ObjectAnimator.ofFloat(Utils.DummyTransX, "translationX", 0, 0);
		AnimatorSet animator;
		private float onFingDir;
		private boolean isPaused=true;
		private float simovefactor=12;
		private boolean supressed;
		private boolean should_check_supressed;
		private float animated_delta;
		private float ss;
		private float se;
		private float sd;
		
		BrowserSlider(){
			animator = new AnimatorSet();
			animator.addListener(new Animator.AnimatorListener() {
				@Override public void onAnimationStart(Animator animation) {
					int check = adapter_idx+adapter_to;
					if(check<0||check>Pages.size()) {
						TabHolder wv = TabHolders.get(check);
						webtitle.setText(wv.title);
					}
					isPaused=false;
				}
				@Override public void onAnimationEnd(Animator animation) {
					webla=null;
					isPaused=true;
					AttachWebAt(adapter_idx+adapter_to, adapter_to==0&&animated_delta<dm.density*90);
				}
				@Override
				public void onAnimationCancel(Animator animation) {}
				@Override
				public void onAnimationRepeat(Animator animation) {}
			});
			animator.setDuration(150);
			animator.playTogether(resitu, resitu1, resitu2);
		}
		
		GestureDetector mDectector = new GestureDetector(BrowserActivity.this, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
								   float velocityX, float velocityY) {
				if(Math.abs(velocityX)*1.2f>Math.abs(velocityY)) {
					onFingDir = velocityX;
				}
				return false;
			}
		});
	
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int evt = event.getActionMasked();
			switch (evt) {
				case MotionEvent.ACTION_DOWN:
					//CMN.Log("ACTION_DOWN");
					//toolbar.suppressLayout(true);
					animated_delta=0;
					etSearch_scrolled=false;
					should_check_supressed=
					supressed=false;
					orgX = event.getX();
					orgY = event.getX();
					if(v==searchbartitle) {
						supressed=true;
					} else if(v!=UIData.bottombar2) {
						if(webtitle.getVisibility()!=View.VISIBLE) {
							FrameLayout etSearchp = UIData.etSearchp;
							if(orgX>=etSearchp.getLeft()&&orgX<=etSearchp.getRight()) {
								if(!(supressed=getCurrentFocus()==etSearch)) {
									should_check_supressed=true;
								}
							}
						}
					}
				break;
				case MotionEvent.ACTION_MOVE: {
					if(should_check_supressed&&!supressed) {
						supressed = etSearch.hasSelection();
					}
					if(supressed) {
						if(v==searchbartitle) {
							float lastX = event.getX();
							lastY = event.getX();
							float delta = lastX-orgX;
							float abs = Math.abs(delta);
							if(!selecting && abs>100) {
								ss = etSearch.getSelectionStart();
								se = etSearch.getSelectionEnd();
								if(ss<0) {
									ss=0;
								}
								if(se<0) {
									se=ss;
								}
								sd=0;
								selecting=true;
								preventDefault(v, event);
							}
							if(selecting) {
								//delta=lastX-this.lastX;
								int len = etSearch.getText().length();
								float factor = MathUtils.lerp(0.5f, 3.5f, (abs-100)/218/dm.density);
								//MN.Log("abs, factor", abs, abs/2000/dm.density, factor);
								sd+=Math.signum(delta)*factor;
								if(etSearch.hasSelection()) {
									float val = Math.max(0, Math.min((se+sd), len));
									if(val==0||val==len) {
										sd=val-se;
									}
									etSearch.setSelection((int)ss, (int) val);
								} else {
									float val = Math.max(0,Math.min( (ss+sd), len));
									if(val==0||val==len) {
										sd=val-ss;
									}
									etSearch.setSelection((int)val);
								}
							}
							this.lastX=lastX;
							return selecting;
						}
						return false;
					}
					float lastX = event.getX();
					lastY = event.getX();
					float delta = Math.abs(lastX-orgX);
					if(DissmissingViewHolder) {
						//CMN.Log("ACTION_MOVE");
						if(!dragging && delta>100) {
							preventDefault(v, event);
							mDectector.onTouchEvent(event);
							dragging =true;
						}
						if(dragging) {
							animated_delta = Math.max(animated_delta, delta);
							mDectector.onTouchEvent(event);
							isPaused=true;
							animator.pause();
							//CMN.Log("draggging", getCurrentFocus());
							View layout = currentWebView.layout;
							float newTX = layout.getTranslationX()+lastX-this.lastX;
							layout.setTranslationX(newTX);
							if(search_bar_vis()) {
								UIData.searchbar.setTranslationX(newTX);
							}
							//CMN.Log("移动。。。",delta);
							//checkCompanion
							int attachIdx;
							if(newTX>0) {
								attachIdx=adapter_idx-1;
							} else {
								attachIdx=adapter_idx+1;
							}
							webla=null;
							if(attachIdx>=0&&attachIdx<Pages.size()) {
								AttachWebAt(attachIdx, true);
								AdvancedBrowserWebView attachLayout = Pages.get(attachIdx);
								if(attachLayout!=null) {
									webla = attachLayout.layout;
									webla.setBackgroundColor(Color.WHITE);
									webla.setTranslationX(newTX+root.getWidth()*(newTX>0?-1:1));
									webla.setTranslationY(layout.getTranslationY());
								}
							}
						}
					} else {
						if(!sliding && delta>100) {
							preventDefault(v, event);
							sliding =true;
							simovefactor=5;
							event.setLocation(lastX*simovefactor, 0);
							event.setAction(MotionEvent.ACTION_DOWN);
							recyclerView.dispatchTouchEvent(event);
							event.setAction(MotionEvent.ACTION_MOVE);
						}
						if(sliding) {
							//CMN.Log("simulated_move....");
							event.setLocation(lastX*simovefactor, 0);
							recyclerView.dispatchTouchEvent(event);
						}
					}
					this.lastX=lastX;
				} break;
				case MotionEvent.ACTION_UP:
					if(sliding) {
						sliding=false;
						event.setLocation(lastX*simovefactor, 0);
						recyclerView.dispatchTouchEvent(event);
					} else if(dragging) {
						dragging=false;
						View layout = currentWebView.layout;
						float tx = layout.getTranslationX();
						int W = root.getWidth();
						adapter_to=0;
						mDectector.onTouchEvent(event);
						int flingTheta = 10;
						float flingMoveTheta = dm.density*62;
						if(tx>0 && (onFingDir>flingTheta&&tx>flingMoveTheta||tx>=W/2)) {
							adapter_to=-1;
						} else if(tx<0 && (onFingDir<-flingTheta&&tx<-flingMoveTheta||tx<=-W/2)) {
							adapter_to=1;
						}
						int check = adapter_idx + adapter_to;
						if(check<0||check>Pages.size()) {
							adapter_to=0;
						}
						isPaused=true;
						animator.pause();
						resitu.setTarget(layout);
						resitu.setFloatValues(tx, -adapter_to*W);
						//animated_delta = Math.abs(tx+adapter_to*W);
						if(webla!=null) {
							resitu1.setTarget(webla);
							resitu1.setFloatValues(webla.getTranslationX(), adapter_to==0?W*(tx>0?-1:1):0);
						} else {
							resitu1.setTarget(Utils.DummyTransX);
						}
						if(webtitle.getVisibility()!=View.VISIBLE) {
							resitu2.setTarget(UIData.searchbar);
							resitu2.setFloatValues(UIData.searchbar.getTranslationX(), -adapter_to*W);
						} else {
							resitu2.setTarget(Utils.DummyTransX);
						}
						animator.start();
					}
					onFingDir = 0;
					selecting = false;
					webla = null;
				break;
			}
			return dragging;
		}
		
		private void preventDefault(View v, MotionEvent event) {
			v.setTag(MotionEvent.ACTION_UP);
			event.setAction(MotionEvent.ACTION_UP);
			v.dispatchTouchEvent(event);
		}
		
		void pause() {
			if(!isPaused) {
				//CMN.Log("rectify....");
				isPaused=true;
				animator.pause();
				webla = null;
				AttachWebAt(adapter_idx, false);
			}
		}
		
		public boolean active() {
			return dragging||sliding||selecting||!mBrowserSlider.isPaused;
		}
	}
	
	private long validifyid(long id) {
		if(!id_table.containsKey(id)) {
			return id;
		}
		int findCound=0;
		while(id_table.containsKey(id)) {
			findCound++;
			if(findCound==1900) {
				id=0;
				continue;
			} else if(findCound>=2000) {
				throw new IllegalArgumentException("");
			}
			id=Math.abs(id+2000);
		}
		return id;
	}
	
	private void pull_hints(boolean show_terms_only) {
		UIData.searchHints.scrollToPosition(0);
		if(pull_hints_runnable!=null) {
			pull_hints_runnable.interrupt();
		}
		pull_hints_runnable = new PullHintsRunnable(show_terms_only);
		pull_hints_runnable.start();
	}
	
	class PullHintsRunnable implements Runnable {
		private final boolean show_terms_only;
		CancellationSignal stopSign = new CancellationSignal();
		Thread t = new Thread(this);
		
		public PullHintsRunnable(boolean show_terms_only) {
			this.show_terms_only = show_terms_only;
		}
		
		public void interrupt() {
			t.interrupt();
			stopSign.cancel();
		}
		public void start() {
			t.start();
		}
		@AnyThread
		@Override
		public void run() {
			CMN.Log("pull_hints", HintConstituents);
			String text = show_terms_only?null:etSearch.getText().toString();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
			if(stopSign.isCanceled()) {
				return;
			}
			int HintsLimitation = opt.getHintsLimitation();
			boolean LimitHints = getLimitHints();
			if(!LimitHints) {
				HintsLimitation=Integer.MAX_VALUE;
			}
			int New_HintConstituents=0;
			Cursor newUrlCursor = null;
			Cursor newSearchCursor = null;
			try {
				CMN.rt();
				newUrlCursor = historyCon.queryUrl(text, HintsLimitation, stopSign);
				New_HintConstituents += newUrlCursor.getCount();
				CMN.pt(New_HintConstituents+"个历史查询时间:");
			} catch (Exception e) {
				CMN.Log(e);
				try {
					if(newUrlCursor!=null) newUrlCursor.close();
				} catch (Exception ignored) { }
				return;
			}
			if(LimitHints) HintsLimitation -= New_HintConstituents;
			try {
				newSearchCursor = historyCon.querySearchTerms(text, text==null?128:5, stopSign);
				New_HintConstituents += newSearchCursor.getCount();
			} catch (Exception e) {
				CMN.Log(e);
				try {
					if(newSearchCursor!=null) newSearchCursor.close();
				} catch (Exception ignored) { }
				return;
			}
			
			ActiveUrlCursor=newUrlCursor;
			ActiveSearchCursor=newSearchCursor;
			historyCon.updateLastSearchTerm(text);
			if(show_terms_only&&New_HintConstituents>=3) {
				root.post(hintsPulledRunnable);
			} else {
				root.post(hintsUpdatedRunnable);
			}
		}
	};
	
	Runnable hintsPulledRunnable = new Runnable() {
		@Override
		public void run() {
			hintsUpdatedRunnable.run();
			RecyclerView animating_search_hints=UIData.searchHints;
			if(animating_search_hints.getTag()==null) {
				LayoutAnimationController layoutAnimation = AnimationUtils
						.loadLayoutAnimation(BrowserActivity.this, R.anim.layout_animation_fall_down);
				animating_search_hints.setLayoutAnimation(layoutAnimation);
				animating_search_hints.setTag(0);
			}
			animating_search_hints.scheduleLayoutAnimation();
			if(getTransitSearchHints()&&search_hints_vising&&HintConstituents>=3) {
				animating_search_hints.postDelayed(BrowserActivity.this::search_hints_vis, 450);
				search_hints_vising=false;
			} else {
				search_hints_vis();
			}
		}
	};
	
	Runnable hintsUpdatedRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				HintConstituents=ActiveUrlCursor.getCount()+ActiveSearchCursor.getCount();
			} catch (Exception ignored) {
				HintConstituents = 0;
			}
			if(adaptermy2!=null) {
				adaptermy2.notifyDataSetChanged();
			}
			search_hints_vis();
			historyCon.closeCursors();
		}
	};
	
	private void search_hints_vis() {
		UIData.searchHints.setVisibility(View.VISIBLE);
	}
	
	private boolean search_bar_vis() {
		return UIData.searchbar.getVisibility()==View.VISIBLE;
	}
	
	static class ViewDataHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder{
		T data;
		int position;
		ViewDataHolder(T data){
			super(data.getRoot());
			itemView.setTag(this);
			this.data = data;
		}
	}
	
	private void init_searint_layout() {
		LinearLayout init_searchbar = UIData.searchbar;
		if(adaptermy2==null) {
			adaptermy2 = new RecyclerView.Adapter<ViewDataHolder<SearchHintsItemBinding>>() {
				public int getItemCount() { return HintConstituents+2; }
				@NonNull
				@Override
				public ViewDataHolder<SearchHintsItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
					SearchHintsItemBinding vh = SearchHintsItemBinding.inflate(getLayoutInflater(), parent, false);
					CMN.Log("onCreateViewHolder", viewType, CMN.now());
					return new ViewDataHolder<>(vh);
				}
				
				@Override
				public void onBindViewHolder(@NonNull ViewDataHolder<SearchHintsItemBinding> viewHolder, int position) {
					SearchHintsItemBinding vh = viewHolder.data;
					viewHolder.itemView.setVisibility(View.VISIBLE);
					if(false) {
						vh.subtitle.setVisibility(View.GONE);
						return;
					}
					try {
						int terms_count = ActiveSearchCursor.getCount();
						if(position<terms_count) {
							if(ActiveSearchCursor==historyCon.EmptySearchCursor) {
								position = terms_count-position-1;
							}
							ActiveSearchCursor.moveToPosition(position);
							vh.title.setText(ActiveSearchCursor.getString(0));
							vh.subtitle.setVisibility(View.GONE);
						} else {
							position-=terms_count;
							terms_count = ActiveUrlCursor.getCount();
							if(position<terms_count) {
								ActiveUrlCursor.moveToPosition(position);
								vh.title.setText(ActiveUrlCursor.getString(1));
								vh.subtitle.setText(ActiveUrlCursor.getString(0));
								vh.subtitle.setVisibility(View.VISIBLE);
							} else {
								viewHolder.itemView.setVisibility(View.GONE);
							}
						}
					} catch (Exception e) {
						vh.title.setText("Error!");
					}
				}
				
				public long getItemId(int position) { return position; }
				
			};
			adaptermy2.setHasStableIds(true);
			
			searchbartitle=init_searchbar.getChildAt(1);
			searchbartitle.setOnTouchListener(mBrowserSlider);
			init_searchbar.setOnClickListener(this);
			
			setOnClickListenersOneDepth((ViewGroup) searchbartitle, this, 1, null);
			
			RecyclerView init_search_hints = UIData.searchHints;
			init_search_hints.setHasFixedSize(true);
			
			LinearLayoutManager layoutManager = new LinearLayoutManager(this);
			layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			layoutManager.setInitialPrefetchItemCount(8);
			init_search_hints.setLayoutManager(layoutManager);
			init_search_hints.setRecycledViewPool(Utils.MaxRecyclerPool(GlobalOptions.isLarge?35:25));
			
			init_search_hints.setAdapter(adaptermy2);
			
			init_search_hints.setNestedScrollingEnabled(false);
			
			init_search_hints.setOnScrollChangedListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
				if(!keyboard_hidden&&init_search_hints.getScrollState()==RecyclerView.SCROLL_STATE_DRAGGING) {
					imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
					keyboard_hidden=true;
				}
			});
		}
		HintConstituents = 0;
		init_searchbar.setTranslationX(0);
	}
	
	private void init_tabs_layout() {
		viewpager_holder = UIData.viewpagerHolder.getViewStub().inflate();
		RecyclerViewPager recyclerViewPager = viewpager_holder.findViewById(R.id.viewpager);
		recyclerView = recyclerViewPager;
		recyclerViewPager.setHasFixedSize(true);
		AppWhiteDrawable = new ColorDrawable(AppWhite);
		itemPad = (int) (mResource.getDimension(R.dimen._35_)/5);
		calculateItemWidth(false);
		scrollHereRunnnable = () -> {
			int target = (mItemWidth+2*itemPad)*adapter_idx;
			//recyclerView.requestLayout();
			View C0 = recyclerViewPager.getChildAt(0);
			if(C0!=null){
				int p=((ViewDataHolder)C0.getTag()).position;
				if(p<0) {
					target-= -C0.getLeft() + itemPad;
				} else {
					target-=(mItemWidth+2*itemPad)*p+padWidth+2*itemPad-C0.getLeft() + itemPad;
				}
			}
			//CMN.Log("首发", C0==null, target);
			recyclerViewPager.scrollBy(target,0);
			layoutManager.targetPos = ViewUtils.getCenterXChildPosition(recyclerViewPager);
		};
		recyclerViewPager.setFlingFactor(0.175f);
		recyclerViewPager.setTriggerOffset(0.125f);
		
		adaptermy = new RecyclerView.Adapter<ViewDataHolder<WebPageItemBinding>>() {
			public int getItemCount() { return 2+Pages.size(); }
			@NonNull
			@Override
			public ViewDataHolder<WebPageItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				ViewDataHolder<WebPageItemBinding> vh = new ViewDataHolder<>(WebPageItemBinding.inflate(getLayoutInflater(), parent, false));
				vh.itemView.setOnClickListener(ocl);
				vh.data.title.setOnClickListener(ocl);
				vh.data.close.setOnClickListener(ocl);
				//CMN.Log("onCreateViewHolder");
				return vh;
			}
			
			@Override
			public void onBindViewHolder(@NonNull ViewDataHolder<WebPageItemBinding> viewHolder, int position) {
				WebPageItemBinding vh = viewHolder.data;
				ImageView iv = vh.iv;
				position-=1;
				viewHolder.position=position;
				View itemView = viewHolder.itemView;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					itemView.setForeground(null);
				}
				//iv.setImageBitmap(Pages.get(position).getBitmap());
				iv.getLayoutParams().height=ViewGroup.LayoutParams.MATCH_PARENT;
				if(targetIsPage(position)) {
					Bitmap bitmap=null;
					WebViewmy mWebView = Pages.get(position);
					TabHolder holder = TabHolders.get(position);
					iv.setTag(R.id.home, false);
					itemView.getLayoutParams().width=mItemWidth;
					itemView.setVisibility(View.VISIBLE);
					String title=holder.title;
					if(title==null||title.equals("")) title=holder.url;
					vh.title.setText(title);
					if(mWebView!=null) {
						bitmap = mWebView.bitmap;
					}
					vh.iv.setImageBitmap(bitmap);
					CMN.Log("setImageBitmap", bitmap);
				}
				else {
					iv.setTag(R.id.home, null);
					//iv.setImageBitmap(null);
					itemView.getLayoutParams().width=padWidth;
					itemView.setVisibility(View.INVISIBLE);
					//vh.itemView.getLayoutParams().width=mItemWidth;
				}
			}
			
			public long getItemId(int position) { return position; }
			
			View.OnClickListener ocl = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v.getId()==R.id.close) {
						ViewDataHolder vh = (ViewDataHolder) ((ViewGroup)v.getParent().getParent()).getTag();
						int target = vh.position;
						closeTabAt(target, 0);
					} else {
						ViewDataHolder vh = (ViewDataHolder) v.getTag();
						int target = vh.position;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
							if(target!=adapter_idx) {
								vh.itemView.setForeground(frame_web);
							}
						}
						
						CMN.Log("onClick", target);
						if(targetIsPage(target)) {
							//AttachWebAt(target);
							//currentWebView.setVisibility(View.INVISIBLE);
							DissmissingViewHolder=false;
							toggleTabView(target, v);
						}
					}
				}
			};
			
		};
		adaptermy.setHasStableIds(true);
		recyclerViewPager.setAdapter(adaptermy);
		layoutManager = new CenterLinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		recyclerViewPager.setLayoutManager(layoutManager);
		recyclerViewPager.addItemDecoration(new SpacesItemDecoration(itemPad));
		
		ItemTouchHelper.Callback callback=new ViewPagerTouchHelper(new ViewPagerTouchHelper.ItemTouchHelperCallback<ViewDataHolder>() {
			@Override
			public void onItemSwipe(ViewDataHolder vh) {
				closeTabAt(vh.position, 1);
			}
			
			@Override
			public void onItemSlide(ViewDataHolder fromPosition, int toPosition) {
				CMN.Log("onMove");
			}
		});
		ItemTouchHelper itemTouchHelper=new ItemTouchHelper(callback);
		itemTouchHelper.attachToRecyclerView(recyclerViewPager);
	}
	
	int MoveRangePositon;
	Runnable MoveRangeIvalidator = new Runnable() {
		@Override
		public void run() {
			adaptermy.notifyItemRangeChanged(MoveRangePositon, Pages.size() - MoveRangePositon);
		}
	};
	
	private void closeTabAt(int positon, int source) {
		if(positon>=0 && positon<TabHolders.size()) {
			TabHolder tab = TabHolders.remove(positon);
			id_table.remove(tab.id);
			AdvancedBrowserWebView webview = Pages.remove(positon);
			if (webview != null) {
				webview.destroy();
			}
			//todo delete stuffs but keep a stack record for re-coverage
			adapter_idx = Math.min(adapter_idx, Pages.size());
			if (adaptermy != null) {
				//todo add fast un-close button
				positon++;
				adaptermy.notifyItemRemoved(positon);
				MoveRangePositon = positon;
				viewpager_holder.removeCallbacks(MoveRangeIvalidator);
				if (source == 0) {
					viewpager_holder.postDelayed(MoveRangeIvalidator, 180);
				} else {
					viewpager_holder.post(MoveRangeIvalidator);
				}
			}
		}
	}
	
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		//todo fix glide trim memo
//		Glide glide = Glide.glide;
//		if(glide!=null) {
//			glide.trimMemory(level);
//			if(focused) {
//				glide.clearMemory();
//			}
//		}
		if(level>TRIM_MEMORY_MODERATE || level<TRIM_MEMORY_UI_HIDDEN) {
			CMN.Log("trimMemory", level, TRIM_MEMORY_MODERATE, TRIM_MEMORY_UI_HIDDEN);
			clearReferences(false);
		}
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		CMN.Log("trimMemory_onLowMemory");
		clearReferences(!focused);
	}
	
	public void clearReferences(boolean force) {
		for (int i = 0; i < WeakReferencePool.length; i++) {
			WeakReference wI = WeakReferencePool[i];
			if(wI!=null) {
				Object obj = wI.get();
				if(obj instanceof Dialog) {
					Dialog dlg = ((Dialog) obj);
					if(Utils.DGShowing(dlg)) {
						if(force) {
							dlg.dismiss();
						} else {
							continue;
						}
					}
				}
				wI.clear();
				WeakReferencePool[i]=null;
			}
		}
	}
	
	private void calculateItemWidth(boolean postMeasure) {
		if(dm.widthPixels<=GlobalOptions.realWidth) { // portrait
			mItemWidth = (int) (GlobalOptions.realWidth * 0.65);
			int idealItemHeight = (int) (((float) mItemWidth) / dm.widthPixels * dm.heightPixels);
			mItemHeight = Math.min(idealItemHeight, dm.heightPixels);
			CMN.Log("idealItemHeight", idealItemHeight, mItemHeight, idealItemHeight-mItemHeight);
		} else { // landscape
			mItemWidth = dm.widthPixels/4;
			mItemHeight = dm.heightPixels/2;
		}
		
		recyclerView.mItemWidth = mItemWidth;
		
		if(postMeasure) {
			root.postDelayed(() -> {
				padViewpager();
				recyclerView.smoothScrollToPosition(layoutManager.targetPos);
			}, 350);
		} else {
			padViewpager();
		}
		
		if(adaptermy!=null) {
			adaptermy.notifyDataSetChanged();
		}
	}
	
	void padViewpager() {
		int pad = (root.getHeight() - mItemHeight - UIData.bottombar2.getHeight())/2;
		recyclerView.setPadding(0, pad, 0, pad);
		padWidth = (root.getWidth()-mItemWidth)/2-3*itemPad;
		if(padWidth<0) padWidth=0;
	}
	
	private boolean targetIsPage(int target) {
		return target>=0 && target<Pages.size();
	}
	
	/** 列表化/窗口化标签管理 */
	private void toggleTabView(int fromClick, View v) {
		//todo 仿效 opera 浏览后直接显示 webview 难度 level up ⭐⭐⭐⭐⭐
		if(webtitle.getVisibility()!=View.VISIBLE) {
			webtitle_setVisibility(false);
		}
		if(DissmissingViewHolder) {
			webtitle.setText("标签页管理");
		} else {
			webtitle.setText(currentWebView.getTitle());
		}
		if(layoutManager.targetPos>Pages.size()) {
			layoutManager.targetPos=Pages.size();
		}
		boolean AnimateTabsManager = true;
		if(AnimateTabsManager && tabsAnimator==null) {
			scaleX = PropertyValuesHolder.ofFloat("scaleX", 0, 1);
			scaleY = PropertyValuesHolder.ofFloat("scaleY", 0, 1);
			translationX = PropertyValuesHolder.ofFloat("translationX", 0, 1);
			translationY = PropertyValuesHolder.ofFloat("translationY", 0, 1);
			alpha2 = PropertyValuesHolder.ofFloat("alpha", 0, 1);
			alpha = new ObjectAnimator();
			alpha.setPropertyName("alpha");
			alpha.setTarget(viewpager_holder);
			alpha1 = ObjectAnimator.ofInt(bottombar2Background().mutate(), "alpha", 1, 0);
			alpha3 = ObjectAnimator.ofFloat(UIData.appbar, "alpha", 0, 1);
			animatorObj = ObjectAnimator.ofPropertyValuesHolder(currentWebView.layout, scaleX, scaleY, translationX, translationY/*, alpha*/, alpha2/*, alpha3*/);
			//webAnimators = new ObjectAnimator[]{scaleX, scaleY, translationX, translationY, alpha1, alpha2};
			//if(false)
			animatorObj.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					//alpha1.setFloatValues(1f, 0f);
					//alpha1.setDuration(180);
					//alpha1.start();
					if(DissmissingViewHolder) {
						viewpager_holder.setVisibility(View.GONE);
						UIData.appbar.setAlpha(1);
					} else {
						currentWebView.layout.setVisibility(View.INVISIBLE);
						//etSearch.setText("标签模式");
					}
					currentWebView.resumeTimers();
					currentWebView.onResume();
					//v.setBackgroundResource(R.drawable.surrtrip1);
					//currentWebView.onResume();
				}
			});
			//animatorSet.playTogether(scaleX, scaleY, translationX, translationY/*, alpha*/, alpha2/*, alpha3*/);
			animatorSet = new AnimatorSet();
			animatorSet.playTogether(animatorObj, alpha1);
			animatorObj.setTarget(currentWebView.layout);
			animatorObj.setDuration(350);
			tabsAnimator = animatorObj;
		} else {
			animatorObj.setDuration(220);
		}
		if(AnimateTabsManager) {
			toggleInternalTabViewAnima(fromClick, v);
		} else {
			toggleInternalTabView(fromClick);
		}
	}
	
	private void onLeaveCurrentTab(int reason) {
		currentWebView.pauseTimers();
		currentWebView.onPause();
		currentWebView.recaptureBitmap();
	}
	
	private void toggleInternalTabView(int fromClick) {
		if(viewpager_holder.getVisibility()==View.VISIBLE){
			viewpager_holder.setVisibility(View.GONE);
			//todo 直接点击 而不是取消
			int targetPos = layoutManager.targetPos;
			if(targetPos!=adapter_idx) {
				AttachWebAt(layoutManager.targetPos-1, false);
			} else {
				currentWebView.layout.setVisibility(View.VISIBLE);
			}
		} else {
			viewpager_holder.setVisibility(View.VISIBLE);
			currentWebView.layout.setVisibility(View.INVISIBLE);
			if(recyclerView.getTag(R.id.home)==null) {
				//recyclerView.scrollToPosition(adapter_idx + 1);
				recyclerView.setTag(R.id.home, false);
			}
			recyclerView.post(scrollHereRunnnable);
			recyclerView.postDelayed(scrollHereRunnnable, 350);
			if(viewpager_holder_hasTag){
				adaptermy.notifyDataSetChanged();
				viewpager_holder_hasTag=false;
			}
			else {
				boolean b1=Options.getAlwaysRefreshThumbnail();
				if(b1){
					//currentWebView.time = System.currentTimeMillis();
				}
				//adaptermy.notifyDataSetChanged();
				//recyclerView.setItemAnimator(null);
				//
				//((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
				adaptermy.notifyItemChanged(adapter_idx+1, false);
				//adaptermy.onBindViewHolder((RecyclerView.ViewHolder) recyclerView.getChildAt(0).getTag(), layoutManager.findFirstVisibleItemPosition());
				
			}
		}
	}
	
	/** cheap animated version */
	Animator tabsAnimator;
	AnimatorSet animatorSet;
	ValueAnimator animatorObj;
	PropertyValuesHolder scaleX;
	PropertyValuesHolder scaleY;
	PropertyValuesHolder translationX;
	PropertyValuesHolder translationY;
	ObjectAnimator alpha;
	ObjectAnimator alpha1;
	PropertyValuesHolder alpha2;
	ObjectAnimator alpha3;
	boolean DissmissingViewHolder=true;
	Runnable startAnimationRunnable = () -> {
		if(anioutTopBotForTab) {
			UIData.appbar.setExpanded(DissmissingViewHolder, true);
		}
		tabsAnimator.start();
	};
	private void startAnimation(boolean post) {
		CMN.Log("动画咯");
		if(post) {
			root.removeCallbacks(startAnimationRunnable);
			root.postDelayed(startAnimationRunnable, 180);
		} else {
			startAnimationRunnable.run();
		}
	}
	private void toggleInternalTabViewAnima(int fromClick, View v) {
		View layout = currentWebView.layout;
		tabsAnimator.pause();
		mBrowserSlider.pause();
		boolean bNeedPost = false;
		float ttY=1, ttX=1, targetScale=mItemWidth;
		boolean bc=fromClick>=0;
		View appbar=UIData.appbar;
		if(true||DissmissingViewHolder||bc) {
			int W = root.getWidth();
			if(W==0) W=dm.widthPixels;
			
			int resourceId = mResource.getIdentifier("status_bar_height", "dimen", "android");
			mStatusBarH = mResource.getDimensionPixelSize(resourceId);
			int H = root.getHeight()-mStatusBarH;
			if(H<=0) H=dm.heightPixels;
			
			targetScale /= W;
			
			ttX=(W-W*targetScale)/2;
			
			ttY=(appbar.getHeight()+UIData.bottombar2.getHeight())/2-appbar.getTop();
			
			//ttY=((H/*-bottombar2.getHeight()*/)-(H-appbar.getHeight())*targetScale)/2-appbar.getHeight()-appbar.getTop();
			
			float titleH = mResource.getDimension(R.dimen._35_);
			
			//ttY=(currentWebView.layout.getHeight()*(1-targetScale))/2+(+titleH-viewpager_holder.getPaddingBottom())/2-appbar.getHeight()-appbar.getTop();
			
			//ttY=-((currentWebView.getHeight()*targetScale)/2-appbar.getHeight()/2)+(root.getHeight()+titleH-viewpager_holder.getPaddingBottom())/2;
			
			ttY=(H+titleH-viewpager_holder.getPaddingBottom()-currentWebView.getHeight()*targetScale)/2-10;
			
			if(!anioutTopBotForTab) {
				ttY+=-appbar.getHeight()-appbar.getTop();
			}
			
			CMN.Log(H, mStatusBarH, UIData.bottombar2.getHeight(), "appbar.h="+appbar.getHeight(), "appbar.top="+appbar.getTop(), "titleH="+titleH, "bottom.h="+appbar.getTop(), "idealItemHeight2="+currentWebView.getHeight()*targetScale);
			
			
			//ttY=(currentWebView.getHeight()*(1-targetScale))/2;
			
			//CMN.Log("ttX", currentWebView.getTranslationY(), currentWebView.getTop(), appbar.getTop(), appbar.getHeight());
		}
		
		boolean added = false;
		
		if(!DissmissingViewHolder) {
			int targetPos = layoutManager.targetPos-1;
			if(targetPos<0) targetPos=0;
			int bcc=0;
			if(bc) {
				bcc = fromClick-targetPos;
				ttX += (mItemWidth+2*itemPad)*bcc;
				targetPos = fromClick;
			}
			if(targetPos!=adapter_idx) {
				//added = TabHolders.get(targetPos)!=null;
				added = AttachWebAt(targetPos, false);
				layout = currentWebView.layout;
				layout.setScaleX(targetScale);
				layout.setScaleY(targetScale);
				layout.setTranslationY(ttY);
				layout.setTranslationX(ttX);
			} else if(bcc!=0) {
				layout.setTranslationX(ttX);
			}
		}
		animatorObj.setTarget(layout);
		layout.setPivotX(0);
		layout.setPivotY(0);
		alpha2.setFloatValues(1, 1);
		int bottombar2_alpha = bottombar2Background().getAlpha();
		float appbar_alpha = appbar.getAlpha();
		if(!DissmissingViewHolder){// 放
			alpha1.setIntValues(bottombar2_alpha, 255);
			alpha3.setFloatValues(appbar_alpha, 1);
			if(added/* || bc*/) {
				bNeedPost = true;
				currentWebView.layout.setAlpha(0);
				alpha2.setFloatValues(0, 1);
			}
			
			DissmissingViewHolder=true;
			layout.setVisibility(View.VISIBLE);
			//layout.setAlpha(1);
			
			scaleX.setFloatValues(bc?targetScale:layout.getScaleX(), 1f);
			scaleY.setFloatValues(bc?targetScale:layout.getScaleY(), 1f);
			translationX.setFloatValues(bc?ttX:layout.getTranslationX(), 0);
			translationY.setFloatValues(bc?ttY:layout.getTranslationY(), 0);
			
			alpha.setFloatValues(viewpager_holder.getAlpha(), 0);
			//animatorObj.setInterpolator(new AccelerateInterpolator(.5f));
			
			//alpha2.setInterpolator(null);
			alpha3.setInterpolator(null);
			//alpha2.setDuration(10);
		}
		else {  // 收
			alpha1.setIntValues(bottombar2_alpha, 128);
			alpha3.setFloatValues(appbar_alpha, 0);
			DissmissingViewHolder=false;
			boolean b1=Options.getAlwaysRefreshThumbnail();
			if(b1){
				currentWebView.time = System.currentTimeMillis();
			}
			adaptermy.notifyItemChanged(adapter_idx+1, false);
			float startAlpha = 0;
			if(layout.getScaleX()==1) {
				//viewpager_holder.setAlpha(0f);
			} else {
				startAlpha = viewpager_holder.getAlpha();
			}
			viewpager_holder.setVisibility(View.VISIBLE);
			recyclerView.post(scrollHereRunnnable);
			//recyclerView.postDelayed(scrollHereRunnnable, 350);
			
			scaleX.setFloatValues(layout.getScaleX(), targetScale);
			scaleY.setFloatValues(layout.getScaleY(), targetScale);
			translationX.setFloatValues(layout.getTranslationX(), ttX);
			translationY.setFloatValues(layout.getTranslationY(), ttY);
			alpha.setFloatValues(startAlpha, 1f);
			animatorObj.setInterpolator(null);
		}
		Drawable b = null;
//		if(fromClick==-1) {
//			b = v.getBackground();
//			if(b!=null) {
//				b.jumpToCurrentState();
//			}
//			v.setBackground(null);
//		}
		//startAnimation(b!=null&&false||bNeedPost);
		startAnimation(false);
	}
	
	
	private boolean AttachWebAt(int i, boolean pseudoAdd) {
		int size = TabHolders.size()-1; // sanity check
		if(size<0) return pseudoAdd;
		if(i<0) i=0;
		if(i>size) i=size;
		AdvancedBrowserWebView mWebView = Pages.get(i);
		if(mWebView==null) {
			TabHolder holder = TabHolders.get(i);
			mWebView = id_table.get(holder.id);
			if(mWebView==null) {
				mWebView = new_AdvancedNestScrollWebView(holder, null);
				id_table.put(holder.id, mWebView);
				//mWebView.setNestedScrollingEnabled(false);
			}
			Pages.set(i, mWebView);
		}
		if(/*!pseudoAdd && */mWebView.loadIfNeeded()) {
			viewpager_holder_hasTag = true;
		}
		mWebView.setNestedScrollingEnabled(true);
		String url = mWebView.getUrl();
		if(url!=null && url.contains("p_2")) {
			mWebView.setNestedScrollingEnabled(false);
		}
		View weblayout = mWebView.layout;
		weblayout.setVisibility(View.VISIBLE);
		weblayout.setScaleX(1);
		weblayout.setScaleY(1);
		boolean add = true;
		if(!pseudoAdd) {
			int ci=1;
			View ca;
			while ((ca=UIData.webcoord.getChildAt(ci))instanceof WebFrameLayout){
				if(ca==weblayout) {
					ci++;
				} else {
					UIData.webcoord.removeViewAt(ci);
				}
			}
			mWebView.setStorageSettings();
			mWebView.setStorageSettings();
		}
		ViewGroup svp = (ViewGroup) weblayout.getParent();
		if(svp!=null) { // sanity check
			if(add=svp!=UIData.webcoord) {
				svp.removeView(weblayout);
			}
		}
		if(add) {
			UIData.webcoord.addView(weblayout, 1);
		}
		decideWebviewPadding(mWebView);
		if(pseudoAdd) {
			return false;
		}
		webtitle_setVisibility(false);
		etSearch_clearFocus();
		webtitle.setText(mWebView.holder.title);
		currentWebView=mWebView;
		if(adapter_idx!=i || add) {
			adapter_idx=i;
			etSearch.setText(mWebView.holder.url);
		}
		UIData.browserWidget10.setText(Integer.toString(i));
		return add;
	}
	
	private void etSearch_clearFocus() {
		etSearch.clearFocus();
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
	}
	
	private void decideTopBotBH() {
		bottombarLP.setBehavior(fixBotBar?null:bottombarScrollBehaviour);
		ViewGroup svp = (ViewGroup) UIData.toolbar.getParent();
		if(svp==UIData.webcoord ^ fixTopBar) {
			svp.removeView(UIData.toolbar);
			(fixTopBar?UIData.webcoord:UIData.appbar).addView(UIData.toolbar, fixTopBar?1:0);
			if(fixTopBar) {
				toolbarti = new View(this);
				toolbarti.setLayoutParams(toolbatLP);
				UIData.appbar.addView(toolbarti);
			} else {
				UIData.toolbar.setLayoutParams(toolbatLP);
				UIData.appbar.removeView(toolbarti);
			}
		}
	}
	
	private void decideWebviewPadding(AdvancedBrowserWebView mWebView) {
		View weblayout = mWebView.layout;
		CoordinatorLayout.LayoutParams lp = ((CoordinatorLayout.LayoutParams) weblayout.getLayoutParams());
		ViewGroup.MarginLayoutParams lpw = ((ViewGroup.MarginLayoutParams) mWebView.getLayoutParams());
		lp.height= ViewGroup.LayoutParams.MATCH_PARENT;
		
		int appbar_height = (int) mResource.getDimension(R.dimen._45_);
		int pt = 0;
		int pb = 0;
		if(fixTopBar) {
			//lpw.topMargin=bottombar_height;
			lp.setBehavior(null);
			pt = appbar_height;
		} else {
			lp.setBehavior(scrollingBH);
		}
		if(fixBotBar) {
			pb = appbar_height;
		}
		
		weblayout.setPadding(0,pt,0,pb);
	}
	
	//click
	@RequiresApi(api = Build.VERSION_CODES.N_MR1)
	@SuppressLint("NonConstantResourceId") // no no no you don't want that. @Google
	@Override
	public void onClick(View v) {
		if(mBrowserSlider.active()) {
			return;
		}
		switch (v.getId()){
			/* 分享 */
			case R.id.menu_icon6: {
				shareUrlOrText();
			} break;
			case R.id.menu_icon10: {
				AddPDFViewerShortCut(getApplicationContext());
				
//				Intent intent = new Intent("colordict.intent.action.SEARCH");
//				intent.putExtra("EXTRA_QUERY", "happy");
//				startActivity(intent);
				//intent.addCategory(Intent.CATEGORY_BROWSABLE);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				
				
				//intent.setFlags(0x10000000);
				
				
				
			} break;
			case R.id.ivBack:{ // 搜索引擎弹窗 //searpop
				int polypopupW = (int) (_45_*1.5);
				int searchEnginePopupW = (int) (etSearch.getWidth()*0.85);
				searchEnginePopupW = Math.min(searchEnginePopupW, (int)Math.max(realWidth, 550*GlobalOptions.density));
				if(searchEnginePopup==null) {
					layoutListener = findViewById(R.id.layoutListener);
					searchEnginePopup = new ListPopupWindow(BrowserActivity.this);
					searchEnginePopup.setAdapter(new ArrayAdapter<>(BrowserActivity.this,
							R.layout.abc_list_menu_item_layout, R.id.title, WebDicts));
					searchEnginePopup.setAnchorView(findViewById(R.id.popline));
					//searchEnginePopup.setOverlapAnchor(true); //21 禁开
					searchEnginePopup.setDropDownAlwaysVisible(true);
					searchEnginePopup.setOnDismissListener(() -> {
						layoutListener.setVisibility(View.GONE);
						layoutListener.popup=null;
						polypopup.dismiss();
					});
					//searchEnginePopup.mPopup.setEnterTransition(null);
					
					polypopupview = LayoutInflater.from(this).inflate(R.layout.polymer, root, false);
					polypopupview.setOnClickListener(v1 -> {
						polysearching=!polysearching;
						polypopupview.setAlpha(polysearching?1:0.25f);
						showT(polysearching?"聚合搜索":"已关闭");
						//m_currentToast.setGravity(Gravity.TOP, 0, appbar.getHeight());
					});
					polypopupview.setAlpha(polysearching?1:0.25f);
					polypopup = new PopupWindow(polypopupview, polypopupW, polypopupW, false);
					polypopup.setOutsideTouchable(false);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						polypopup.setEnterTransition(null);
						polypopup.setExitTransition(null);
					}
				}
				//searchEnginePopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
				if(searchEnginePopup.isShowing()) {
					searchEnginePopup.dismiss();
				} else {
					int iconWidth = UIData.ivBack.getWidth();
					int etSearch_getWidth = root.getWidth()-iconWidth*2;
					searchEnginePopup.Selection=5;
					layoutListener.popup = searchEnginePopup;
					layoutListener.setVisibility(View.VISIBLE);
					//layoutListener.supressNextUpdate=true;
					searchEnginePopup.setWidth(searchEnginePopupW);
					searchEnginePopup.setHeight(-2);
					searchEnginePopup.show();
					//searchEnginePopup.mDropDownList.getLayoutParams().height=-2;
					//searchEnginePopup.mDropDownList.requestLayout();
					//((ViewGroup)searchEnginePopup.mDropDownList.getParent()).getLayoutParams().height=-2;
					//((ViewGroup)searchEnginePopup.mDropDownList.getParent()).requestLayout();
					//polypopup.showAtLocation(ivBack, Gravity.RIGHT|Gravity.TOP,10, appbar.getHeight());
					polypopup.showAsDropDown(UIData.ivBack,iconWidth+((etSearch_getWidth+searchEnginePopupW+iconWidth)-(polypopupW))/2, -polypopupW/2);
				}
				//searchEnginePopup.show();
			} break;
			case R.id.webtitle: {
				CharSequence text = etSearch.getText();
				boolean t0 = text.length() == 0;
				if(t0) {
					etSearch.setText(currentWebView.getUrl());
				}
				goToBarcodeScanner=t0||StringUtils.equals(text, currentWebView.getUrl());
				updateQRBtn();
				webtitle_setVisibility(true);
				if(opt.getSelectAllOnFocus()||opt.getShowImeImm()) {
					v.post(() -> { //upEvt
						etSearch.requestFocus();
						imm.showSoftInput(etSearch, 0);
					});
				}
			} break;
			case R.id.ivRefresh:
				AdvancedBrowserWebView mWebView = currentWebView;
				if(UIData.progressbar.getVisibility()==View.VISIBLE) {
					UIData.progressbar.setVisibility(View.GONE);
					supressingProgressListener = true;
					mWebView.stopLoading();
					progressProceed.cancel();
					animatorTransient.cancel();
					UIData.ivRefresh.setImageResource(R.drawable.ic_refresh_black_24dp);
					root.postDelayed(() -> supressingProgressListener = false, 100);
				} else {
					CMN.Log("刷新……");
					progressbar_background.setLevel(0);
					UIData.progressbar.setAlpha(1);
					mWebView.reload();
					UIData.ivRefresh.setImageResource(R.drawable.ic_close_white_24dp);
					supressingProgressListener = false;
				}
			break;
			case R.id.ivOverflow:
				int id = WeakReferenceHelper.top_menu;
				DialogWithTag top_menu = (DialogWithTag) getReferencedObject(id);
				if(top_menu==null) {
					final DialogWithTag dlg = new DialogWithTag(this, R.style.AppBaseTheme);
					dlg.setCanceledOnTouchOutside(true);
					dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
					FrameLayout popuproot = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.main_menu, null);
					
					Window win = dlg.getWindow();
					if(win!=null) {
						win.setWindowAnimations(R.style.dialog_animation);
						
						win.setContentView(popuproot);
						win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

						WindowManager.LayoutParams lp = win.getAttributes();
						lp.height = -1;
						lp.width = -1;
						win.setDimAmount(0);
						
						win.setAttributes(lp);
						
						setStatusBarColor(win);
					}
					
					int resourceId = mResource.getIdentifier("status_bar_height", "dimen", "android");
					int height = mResource.getDimensionPixelSize(resourceId);
					popuproot.setPadding(0,height,0,0);
					popuproot.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
					
					TopMenuClickListener TopMenuClicker = new TopMenuClickListener(dlg, popuproot);
					
					setOnClickListenersOneDepth(popuproot, TopMenuClicker, 999, null);
					View textItem = getViewItemByPath(popuproot, 1, 0, 4);
					TopMenuClicker.vals=new Object[] {
						getViewItemByPath(popuproot, 1, 0, 3, 0),
						getViewItemByPath(textItem, 0),
						getViewItemByPath(textItem, 1),
						getViewItemByPath(popuproot, 2, 0),
					};
					TopMenuClicker.pcmodeicontv=(TextView) getViewItemByPath(popuproot, 1, 0, 6);
					dlg.tag=TopMenuClicker;
					
					CMN.Log("对话 对话");
					putReferencedObject(id, top_menu=dlg);
					
					dlg.setOnFocusChangedListener((v12, hasFocus) -> {
						if(hasFocus&&topMenuRequestedInvalidate!=0) {
							TopMenuClicker.decorateAll();
							if((topMenuRequestedInvalidate&(1<<StorageSettings))!=0) {
								currentWebView.setStorageSettings();
							}
							if((topMenuRequestedInvalidate&(1<<BackendSettings))!=0) {
								currentWebView.setBackEndSettings();
							}
						}
					});
				}
				top_menu.show();
				break;
			case R.id.browser_widget7:
				AdvancedBrowserWebView wv = currentWebView;
				boolean backable = wv.canGoBack();
				CMN.Log("backable::", backable, wv.getThisIdx(), wv.getUrl());
				if(backable) {
					wv.goBack();
				} else if(wv.holder.getLuxury()) {
					boolean added = false;
					int idx = wv.getThisIdx();
					if(idx>0) {
						wv.setVisibility(View.GONE);
						wv.pauseWeb();
						currentWebView = (AdvancedBrowserWebView) wv.layout.getChildAt(idx-1);
						currentWebView.setVisibility(View.VISIBLE);
						currentWebView.resumeWeb();
						added = true;
					} else {
						int size = wv.PolymerBackList.size();
						if(size>0) {
							wv.setVisibility(View.GONE);
							wv.pauseWeb();
							String backUrl = wv.PolymerBackList.remove(size - 1);
							CMN.Log("--backUrl::", backUrl);
							currentWebView = new_AdvancedNestScrollWebView(wv.holder, wv);
							currentWebView.loadUrl(backUrl);
							currentWebView.resumeWeb();
							added=true;
						}
					}
					if(added) {
						v.jumpDrawablesToCurrentState();
						setUrlAndTitle();
						id_table.put(currentWebView.holder.id, currentWebView);
						UIData.progressbar.setVisibility(View.GONE);
					}
				}
				CMN.Log("backed::", wv.getUrl());
				break;
			case R.id.browser_widget8:
				wv = currentWebView;
				boolean added = false;
				boolean forwadable = wv.canGoForward();
				CMN.Log("forwadable::", forwadable, wv.getThisIdx(), wv.getUrl());
				if(forwadable) {
					wv.goForward();
				} else  if(wv.holder.getLuxury()) {
					int cc = wv.layout.getChildCount();
					int idx = wv.getThisIdx();
					if(idx<cc-1) {
						if(!forwadable||wv.isAtLastStackMinusOne()) {
							wv.pauseWeb();
							currentWebView = (AdvancedBrowserWebView) wv.layout.getChildAt(idx+1);
							currentWebView.setVisibility(View.VISIBLE);
							currentWebView.resumeWeb();
							added = true;
						}
					}
					if(added) {
						v.jumpDrawablesToCurrentState();
						setUrlAndTitle();
						id_table.put(currentWebView.holder.id, currentWebView);
						UIData.progressbar.setVisibility(View.GONE);
					}
				}
//				fixBotBar = !fixBotBar;
//				decideTopBotBH();
//				decideWebviewPadding();
//				showT("fixBotBar : "+fixBotBar);
				break;
			case R.id.browser_widget9:
				showT("测试……");
				
//				currentWebView.evaluateJavascript("window._docAnnots", new ValueCallback<String>() {
//					@Override
//					public void onReceiveValue(String value) {
//						CMN.Log(value);
//					}
//				});

//				currentWebView.evaluateJavascript("window._PPMInst.RestoreAnnots()", new ValueCallback<String>() {
//					@Override
//					public void onReceiveValue(String value) {
//						CMN.Log(value);
//					}
//				});
				
				showDownloadDialog(null);
//				fixTopBar = !fixTopBar;
//				decideTopBotBH();
//				decideWebviewPadding();
//				showT("fixTopBar : "+fixTopBar);
			break;
			case R.id.browser_widget11:
				//appbar.setExpanded(false, true);
				if(menu_grid==null) {
					init_menu_layout();
				}
				if(menu_grid.getVisibility()==View.VISIBLE) {
					menu_grid.setVisibility(View.GONE);
				} else {
					menu_grid.setVisibility(View.VISIBLE);
				}
			break;
			case R.id.browser_widget10:
				onLeaveCurrentTab(0);
				boolean post = false;
				if(viewpager_holder==null) {
					init_tabs_layout();
					post=true;
				} else if(bNeedReCalculateItemWidth && DissmissingViewHolder) {
					calculateItemWidth(false);
					bNeedReCalculateItemWidth = false;
				}
				if(post) {
					root.postDelayed(() -> toggleTabView(-1, v), 350);
				} else {
					toggleTabView(-1, v);
				}
			break;
			case R.id.searchbar:
			case R.id.browser_widget1:{
				etSearch_clearFocus();
				webtitle_setVisibility(false);
			} break;
			case R.id.browser_widget2:{
				if(etSearch.getText().length()==0) {
					etSearch.setText(currentWebView.getUrl());
				} else {
					String text = etSearch.getText().toString();
					if(etSearch.hasSelection()) {
						final int selectionStart = etSearch.getSelectionStart();
						final int selectionEnd = etSearch.getSelectionEnd();
						text = text.substring(selectionStart, selectionEnd);
					}
					ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setPrimaryClip(ClipData.newPlainText("PLOD", text));
				}
			} break;
			case R.id.browser_widget3:{
				ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData primaryClip = clipboard.getPrimaryClip();
				if (primaryClip != null && primaryClip.getItemCount()>0) {
					etSearch.setText(primaryClip.getItemAt(0).getText());
				}
			} break;
			case R.id.browser_widget4:{
				etSearch.setText(null);
				boolean showKeyBoardOnClean=true;
				if(showKeyBoardOnClean) {
					imm.showSoftInput(etSearch, 0);
				}
			} break;
			case R.id.browser_widget5:{
				String url = etSearch.getText().toString().trim();
				if(goToBarcodeScanner||url.length()==0) { //二维码
					Intent intent = new Intent(this, QRActivity.class);
					startActivityForResult(intent, RequsetUrlFromCamera);
				} else {
					execBrowserGoTo(url);
				}
			} break;
		}
	}
	
	private void AddPDFViewerShortCut(Context context) {
		if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClassName("com.knziha.polymer", "com.knziha.polymer.PDocShortCutActivity");
			//intent.putExtra("ASD", "dsa");
			intent.putExtra("ASD", 123);
			
			intent.setData(Uri.fromFile(new File("123345")));
			
			ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(context, "knziha.pd2f")
					.setIcon(IconCompat.createWithResource(context, R.drawable.ic_pdoc_house))
					.setShortLabel("PDF Viewer")
					.setIntent(intent)
					.build();
			
			//当添加快捷方式的确认弹框弹出来时，将被回调
			PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, MyReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
			//ShortcutManagerCompat.requestPinShortcut(context, info, shortcutCallbackIntent.getIntentSender());
			ShortcutManagerCompat.requestPinShortcut(context, info, shortcutCallbackIntent.getIntentSender());
			//ShortcutManagerCompat.addDynamicShortcuts(context, Collections.singletonList(info));
			//ShortcutManagerCompat.requestPinShortcut(context, info, null);
		}
	}
	
	public int topMenuRequestedInvalidate;
	
	class TopMenuClickListener implements View.OnClickListener, View.OnLongClickListener, Runnable{
		Dialog dlg;
		FrameLayout popuproot;
		private ViewGroup popupdelay;
		private TextView popupdismiss;
		private TextView popuptitle;
		private TextView pcmodeicontv;
		Object[] vals;
		
		public TopMenuClickListener(DialogWithTag dlg, FrameLayout popuproot) {
			this.dlg=dlg;
			this.popuproot=popuproot;
		}
		
		public boolean onItemClick(View v, int id, boolean isLongClicked) {
			boolean dissmiss=!isLongClicked;
			switch (v.getId()) {
				default:
					dissmiss=false;
				break;
				case R.id.dismiss:
				break;
				case R.id.copy:
					UIData.browserWidget10.setTag(currentWebView);
				case R.id.new_folder:
					onLongClick(UIData.browserWidget10);
				break;
				case R.id.reinit:
					if(isLongClicked) {
						show_web_configs(StorageSettings);
					} else {
						//popuproot.setBackgroundColor(Color.GREEN);
						if(v.getTag()==null) {
							TickIntoAction(v, "标签页-崭新模式", "\n\t\t即将为标签页切换启崭新模式，此模式下禁用站点的本地存储。\n\n（长按按钮以关闭此延时提示）");
							dissmiss=false;
						} else {
							v.setTag(null);
							WebOptions delegate = currentWebView.getDelegate(StorageSettings);
							boolean val = delegate.toggleForbidLocalStorage();
							currentWebView.setStorageSettings();
							showT("已为<标签页>"+(val?"开启":"关闭")+"崭新模式！");
						}
					}
				break;
				case R.id.refresh:
					currentWebView.reload();
				break;
				case R.id.print:
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						CustomViewHideTime = PrintStartTime = System.currentTimeMillis()-3500;
						printSX = currentWebView.getScrollX();
						printSY = currentWebView.getScrollY();
						printScale = currentWebView.getScaleX();
						PrintPdfAgentActivity.printPDF(BrowserActivity.this, currentWebView);
					}
				break;
				case R.id.offline:
					currentWebView.saveWebArchive(new File(getExternalFilesDir(null), currentWebView.getTitle()+".mht").getPath(), false, new ValueCallback<String>() {
						@Override
						public void onReceiveValue(String value) {
							showT(value);
						}
					});
				break;
				/* 分享 */
				case R.id.menu_icon6: {
					shareUrlOrText();
				} break;
				/* 页内搜索 */
				case R.id.menu_icon8: {
				
				} break;
				/* PC模式 */
				case R.id.menu_icon9:
					if(isLongClicked) {
						show_web_configs(BackendSettings);
					} else {
						WebOptions delegate = currentWebView.getDelegate(BackendSettings);
						boolean val = delegate.togglePCMode();
						setUserAgentString(currentWebView, val);
						currentWebView.reload();
						decoratePCMode();
					}
				break;
				case R.id.main_progress_bar:
					pendingView=null;
					animator.cancel();
					popupdelay.setVisibility(View.GONE);
					dissmiss=false;
				break;
			}
			if(dissmiss) {
				//root.postDelayed(() -> dlg.dismiss(), 1180);
				root.post(this);
				//dlg.dismiss();
			}
			return true;
		}
		RotateDrawable main_progress_bar_d;
		ObjectAnimator animator;
		View pendingView;
		
		private void TickIntoAction(View v, String title, String hint) {
			if(main_progress_bar_d==null) {
				main_progress_bar_d = (RotateDrawable) ((LayerDrawable)popuproot.findViewById(R.id.main_progress_bar).getBackground()).findDrawableByLayerId(android.R.id.progress);
				animator = ObjectAnimator.ofInt(main_progress_bar_d, "level", 0,1);
				animator.addListener(new Animator.AnimatorListener() {
					@Override public void onAnimationStart(Animator animation) { }
					
					@Override public void onAnimationEnd(Animator animation) {
						if(pendingView!=null) {
							pendingView.setTag(StringUtils.EMPTY);
							pendingView.performClick();
							popupdelay.setVisibility(View.GONE);
						}
					}
					
					@Override public void onAnimationCancel(Animator animation) {  }
					
					@Override public void onAnimationRepeat(Animator animation) { }
				});
				popupdelay = (ViewGroup) popuproot.getChildAt(2);
				popupdismiss = (TextView) popupdelay.getChildAt(0);
				popuptitle = (TextView) popupdelay.getChildAt(2);
				popupdismiss.setEnabled(true);
				popuptitle.setOnClickListener(v12 -> {
					float f = animator.getCurrentPlayTime() / 1080.f;
					animator.setDuration(180);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
						animator.setCurrentFraction(f);
					}
				});
				popupdismiss.setOnTouchListener((v1, evt) -> {
					int action = evt.getActionMasked();
					if(action==MotionEvent.ACTION_DOWN) {
						animator.pause();
					} else if(action==MotionEvent.ACTION_UP){
						animator.resume();
					}
					return true;
				});
			}
			popuptitle.setText(title);
			popupdismiss.setText(hint);
			pendingView = v;
			animator.pause();
			animator.setDuration(1080);
			popupdelay.setVisibility(View.VISIBLE);
			//main_progress_bar_d.setLevel((int) (.5 * 10000));
			animator.setIntValues(0, 10000);
			animator.start();
		}
		
		@Override
		public void onClick(View v) {
			onItemClick(v, v.getId(), false);
		}
		
		@Override
		public boolean onLongClick(View v) {
			return onItemClick(v, v.getId(), true);
		}
		
		public void decoratePCMode() {
			LayerDrawable ld = (LayerDrawable) pcmodeicontv.getCompoundDrawables()[0];
			ld.getDrawable(1).setAlpha(currentWebView.getDelegate(BackendSettings).getPCMode()?255:0);
		}
		
		@Override
		public void run() {
			dlg.dismiss();
		}
		
		public void decorateAll() {
			((TextView)vals[0]).setTextColor(currentWebView.getDelegate( StorageSettings).getForbidLocalStorage()?0xff4F7FDF:AppBlack);
			
			TextView shareIndicator = (TextView)vals[1];
			boolean shareText=currentWebView.hasFocus()&&currentWebView.bIsActionMenuShown;
			shareIndicator.setText(shareText?"分享文本":"分享链接");
			
			decoratePCMode();
		}
		
		private void show_web_configs(int groupID) {
			StandardConfigDialog holder = new StandardConfigDialog();
			
			StandardConfigDialog.buildStandardConfigDialog(BrowserActivity.this, holder, null, R.string.global_vs_tabs);
			
			holder.init_web_configs(currentWebView.holder.getApplyRegion(groupID), groupID);
			
			topMenuRequestedInvalidate=0;
			
			holder.dlg.show();
		}
	}
	
	
	class StandardConfigDialog extends StandardConfigDialogBase
			implements OptionProcessor, View.OnClickListener {
		SpannableStringBuilder str_tab;
		ForegroundColorSpan YinYangSpan;
		int realm;
		int region;
		int regionBackUp=0;
		boolean globalOrTab;
		private String[] groupNym;
		//private String[] groupPrefix;
		private String[] groupApply;
		
		public void clear(){
			str_global.clear();
			str_tab.clear();
		}
		
		@Override
		public void processOptionChanged(ClickableSpan clickableSpan, View widget, int processId, int val) {
			switch (processId) {
				case 1:
					topMenuRequestedInvalidate|=1<<StorageSettings;
				break;
				case 2:
					topMenuRequestedInvalidate|=1<<BackendSettings;
				break;
				case 666:
					init_web_configs(globalOrTab, region==-1?regionBackUp:-1);
				break;
			}
		}
		
		public void init_web_configs(boolean globalOrTab, int grounpID) {
			realm=0;
			region=grounpID;
			if(region!=-1) {
				regionBackUp=region;
			}
			this.globalOrTab=globalOrTab;
			if(YinYangSpan==null) {
				str_tab=new SpannableStringBuilder();
				SpannableStringBuilder str_title = new SpannableStringBuilder(title.getText());
				title.setText(str_title, TextView.BufferType.SPANNABLE);
				YinYangSpan=new ForegroundColorSpan(0x8a8f8f8f);
				title.setOnClickListener(this);
			}
			Spannable text = (Spannable) title.getText();
			int idx = indexOf(text, '/', 0);
			text.setSpan(YinYangSpan, globalOrTab?0:(idx+1), globalOrTab?idx:text.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			
			final String[] Coef = mResource.getString(R.string.coef).split("_");
			SpannableStringBuilder ssb = str_tab;
			ssb.clear();
			ClickSpanBuilder builder = new ClickSpanBuilder(BrowserActivity.this, opt, this, tv, ssb, Coef);
			
			boolean buildSingle=grounpID!=-1;
			
			groupApply = mResource.getStringArray(R.array.GroupApply);
			
			int flagIndex=globalOrTab?-1:3;
			
			switch (grounpID) {
				default:
					//ssb.append("存储组");
				/* Group#存储 */
				case 0:
					if(!buildSingle) webSepGroupApply(builder, ssb, 0, 6, 1);
					builder.withTitle(mResource.getStringArray(R.array.BenDiCunChu))
						.init_clickspan_with_bits_at(0,  0, 0x1, 2, 1, flagIndex, 1)//总开关
						.init_clickspan_with_bits_at(1, 1, 0x1, 3, 1, flagIndex, 1)//Cookie
						.init_clickspan_with_bits_at(2, 1, 0x1, 4, 1, flagIndex, 1)//DataBase
						.init_clickspan_with_bits_at(3, 1, 0x1, 5, 1, flagIndex, 1);//Dom
					
					if(buildSingle) {
						ssb.append("\r\n");
						if(globalOrTab) {
							builder.init_clickspan_with_bits_at(4, 1, 0x1, 6, 1, flagIndex, 1);//Apply
						}
						break;
					} else {
						append_hr(ssb, globalOrTab);
					}
				/* Group#客户端 */
				case 1:
					if(!buildSingle) webSepGroupApply(builder, ssb, 1, 11, 1);
					builder.withTitle(mResource.getStringArray(R.array.KeHuDuan))
						.init_clickspan_with_bits_at(0,  0, 0x1, 7, 1, flagIndex, 2)
						.init_clickspan_with_bits_at(1, 0, 0x1, 12, 1, flagIndex, 2)
						.init_clickspan_with_bits_at(2, 0, 0x1, 9, 1, flagIndex, 2)
						.init_clickspan_with_bits_at(3, 0, 0x1, 10, 1, flagIndex, 2)
						.init_clickspan_with_bits_at(4, 0, 0x1, 13, 1, flagIndex, 2)
						.init_clickspan_with_bits_at(5, 1, 0x1, 8, 1, flagIndex, 2)
					;
					if(buildSingle) {
						ssb.append("\r\n");
						if(globalOrTab) {
							builder.init_clickspan_with_bits_at(6, 1, 0x1, 11, 1, flagIndex, 2);
						}
						break;
					} else {
						append_hr(ssb, globalOrTab);
					}
					
			}
			
			builder.withTitle(groupApply).withCoef(null)
					.init_clickspan_with_bits_at(buildSingle?1:2, 0, 0x1, 10, 1, flagIndex, 666);
			
			if(buildSingle) {
				ssb.delete(ssb.length()-2, ssb.length());
				
				if(globalOrTab) {
					ssb.delete(ssb.length()-2, ssb.length());
				}
			}
			
			tv.setText(ssb, TextView.BufferType.SPANNABLE);
		}
		
		private void webSepGroupApply(ClickSpanBuilder builder, SpannableStringBuilder ssb, int group, int pos, int shift) {
			if(groupNym==null) {
				groupNym = mResource.getStringArray(R.array.GroupName);
				//groupPrefix = mResource.getStringArray(R.array.GroupPrefix);
			}
			if(globalOrTab) {
				builder.withTitle(groupNym).withBraces(false)/*.withPrefix(groupPrefix[group])*/
						.init_clickspan_with_bits_at(group, shift, 0x1, pos, 1, -1, 0)
						.withPrefix(null)
						.withBraces(true)
				;
			} else {
				ssb/*.append(groupPrefix[group])*/.append(groupNym[group]).append("\r\n").append("\r\n");
			}
		}
		
		private void append_hr(SpannableStringBuilder ssb, boolean globalOrTab) {
			//ssb.append(Html.fromHtml("<HR>", Html.FROM_HTML_MODE_COMPACT));
			ssb.append("\r\n").append("\r\n");
		}
		
		@Override
		public void onClick(View v) {
			if(realm==0) {
				init_web_configs(!globalOrTab, region);
				v.getBackground().jumpToCurrentState();
			}
		}
	}
	
	public static class StandardConfigDialogBase {
		public AlertDialog dlg;
		public TextView tv;
		public TextView title;
		public SpannableStringBuilder str_global=new SpannableStringBuilder();
		
		public static class ClickSpanBuilder {
			BrowserActivity a;
			OptionProcessor optprs;
			TextView tv;
			SpannableStringBuilder text;
			String[] title;
			String[] coef;
			String[] tmpTitle;
			String[] tmpCoef;
			boolean tmpIsTitle;
			String prefix;
			int coefOff;
			boolean prenth=true;
			Options opt;
			
			public ClickSpanBuilder(BrowserActivity a1, Options opt1, OptionProcessor optprs1, TextView tv1, SpannableStringBuilder text1, String[] coef1) {
				a = a1;
				opt = opt1;
				optprs = optprs1;
				tv = tv1;
				text = text1;
				coef = coef1;
			}
			
			public ClickSpanBuilder init_clickspan_with_bits_at(int titleOff,
																int coefShift, long mask,
																int flagPosition, int flagMax, int flagIndex,
																int processId) {
				final String[] title = tmpTitle!=null?tmpTitle:this.title;
				final String[]  coef = tmpIsTitle?null:tmpCoef!=null?tmpCoef:this.coef;
				tmpTitle=tmpCoef=null;
				final boolean prenth = !tmpIsTitle && this.prenth;
				tmpIsTitle=false;
				int start = text.length();
				int now = start+ title[titleOff].length();
				text.append(prenth?"[":"{ ");
				if(prefix!=null) {
					text.append(prefix);
				}
				text.append(title[titleOff]);
				if(coef!=null){
					text.append(" :");
					long val = (opt.Flag(a, flagIndex)>>flagPosition)&mask;
					text.append(coef[coefOff+(int) ((val)+coefShift)%(flagMax+1)]);
				}
				text.append(prenth?"]":" }").append("\r\n");
				text.setSpan(new ClickableSpan() {
					@Override
					public void updateDrawState(@NonNull TextPaint paint) {
						if(prenth) {
							super.updateDrawState(paint);
						}
					}
					@Override
					public void onClick(@NonNull View widget) {
						if(coef==null){
							if(optprs!=null) {
								optprs.processOptionChanged(this, widget, processId , -1);
							}
							return;
						}
						long flag = opt.Flag(a, flagIndex);
						long val = (flag>>flagPosition)&mask;
						val=(val+1)%(flagMax+1);
						flag &= ~(mask << flagPosition);
						flag |= (val << flagPosition);
						opt.Flag(a, flagIndex, flag);
						int fixedRange = indexOf(text, ':', now);
						text.delete(fixedRange+1, prenth?indexOf(text, ']', fixedRange):(fixedRange+3));
						val=(val+coefShift)%(flagMax+1);
						text.insert(fixedRange+1,coef[(int) (coefOff+val)]);
						tv.setText(text);
						if(optprs!=null) {
							optprs.processOptionChanged(this, widget, processId, (int) val);
						}
					}},start,text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.append("\r\n");
				return this;
			}
			
			public ClickSpanBuilder withTitle(String[] dictOpt1) {
				title =dictOpt1;
				return this;
			}
			
			public ClickSpanBuilder withCoef(String[] coef1) {
				coef=coef1;
				return this;
			}
			
			public ClickSpanBuilder withPrefix(String prefix1) {
				prefix=prefix1;
				return this;
			}
			
			public ClickSpanBuilder withBraces(boolean prenth1) {
				prenth=prenth1;
				return this;
			}
			
			public ClickSpanBuilder withTmpTitleAndCoef(String[] title, String[] coef) {
				if(title!=null) {
					tmpTitle=title;
				}
				if(coef!=null) {
					tmpCoef = coef;
				}
				return this;
			}
			
			public ClickSpanBuilder withTmpIsTitle(boolean title) {
				tmpIsTitle=title;
				return this;
			}
		}
		
		public static void buildStandardConfigDialog(Context context, StandardConfigDialogBase resultHolder, View.OnClickListener onclick, int title_id, Object...title_args) {
			final View dv = LayoutInflater.from(context).inflate(R.layout.dialog_about,null);
			final TextView tv = dv.findViewById(R.id.resultN);
			TextView title = dv.findViewById(R.id.title);
			Resources mResource = context.getResources();
			if(title_id!=0) {
				if(title_args.length>0){
					title.setText(mResource.getString(title_id, title_args));
				} else {
					title.setText(title_id);
				}
			}
			title.setTextSize(GlobalOptions.isLarge?19f:18f);
			title.setTextColor(AppBlack);
			//title.getPaint().setFakeBoldText(true);
			
			int topad = (int) mResource.getDimension(R.dimen._18_);
			((ViewGroup)title.getParent()).setPadding(topad*3/5, topad/2, 0, 0);
			//((ViewGroup)title.getParent()).setClipToPadding(false);
			//((ViewGroup.MarginLayoutParams)title.getLayoutParams()).setMarginStart(-topad/4);
			
			Options.setAsLinkedTextView(tv);
			
			final AlertDialog configurableDialog =
					new AlertDialog.Builder(context,GlobalOptions.isDark?R.style.DialogStyle3Line:R.style.DialogStyle4Line)
							.setView(dv)
							.create();
			configurableDialog.setCanceledOnTouchOutside(true);
			
			dv.findViewById(R.id.cancel).setOnClickListener(v -> {
				if(onclick!=null) onclick.onClick(v);
				configurableDialog.dismiss();
			});
			
			resultHolder.dlg=configurableDialog;
			resultHolder.tv=tv;
			resultHolder.title=title;
		}
	}
	
	public void setUserAgentString(AdvancedBrowserWebView mWebView, boolean val) {
		mWebView.getSettings().setUserAgentString(val
				?"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
				:null);
	}
	
	private void init_menu_layout() {
		menu_grid=(ViewGroup) ((ViewStub)findViewById(R.id.menu_grid)).inflate();
		for (int i = 0; i < 2; i++) {
			ViewGroup sv = (ViewGroup) menu_grid.getChildAt(i);
			for (int j = 0; j < 5; j++) {
				DescriptiveImageView vv = (DescriptiveImageView) sv.getChildAt(j);
				vv.textPainter=menu_grid_painter;
				vv.setOnClickListener(this);
			}
		}
		menu_grid.setVisibility(View.GONE);
	}
	
	private void shareUrlOrText() {
		//CMN.Log("menu_icon6menu_icon6");
		//CMN.rt("分享链接……");
		int id = WeakReferenceHelper.share_dialog;
		BottomSheetDialog dlg = (BottomSheetDialog) getReferencedObject(id);
		if(dlg==null) {
			putReferencedObject(id, dlg=new AppIconsAdapter(this).shareDialog);
		}
		//CMN.pt("新建耗时：");
		AppIconsAdapter shareAdapter = (AppIconsAdapter) dlg.tag;
		shareAdapter.pullAvailableApps(this, currentWebView.getUrl(), null);
		//shareAdapter.pullAvailableApps(this, null, "happy");
		//CMN.pt("拉取耗时：");
	}
	
	private void setUrlAndTitle() {
		webtitle.setText(currentWebView.holder.url=currentWebView.getTitle());
		etSearch.setText(currentWebView.holder.title=currentWebView.getUrl());
	}
	
	public void fadeOutProgressbar() {
		if(UIData.progressbar.getAlpha()==1 && !supressingProgressListener) {
			progressTransient.pause();
			progressProceed.pause();
			progressTransient.setFloatValues(1, 0);
			int start = progressbar_background.getLevel();
			progressProceed.setIntValues(start, 10000);
			animatorTransient.setDuration(Math.max((10000-start)/10, 10));
			animatorTransient.start();
		}
	}
	
	public void showDownloadDialog(String url) {
		//showT(text);
		int id = WeakReferenceHelper.bottom_download_dialog;
		BottomSheetDialog bottomPlaylist = (BottomSheetDialog) getReferencedObject(id);
		if(bottomPlaylist==null) {
			CMN.Log("重建底部弹出");
			putReferencedObject(id, bottomPlaylist = new BottomSheetDialog(this));
			View downloadPanel = LayoutInflater.from(this).inflate(R.layout.download_bottom_sheet, null);
			
			Object[] values = new Object[]{R.id.dir_path, R.id.download, null};
			
			BottomSheetDialog final_bottomPlaylist = bottomPlaylist;
			View.OnClickListener clicker = v -> {
				switch (v.getId()) {
					case R.id.abort:
						final_bottomPlaylist.dismiss();
					break;
					case R.id.download:
						String URL = (String) values[2];
						showT("下载中…"+URL);
						DownloadManager.Request request = new DownloadManager.Request(Uri.parse("ftp://127.0.0.1:8088/xhjhsatz.txt"));
						//设置什么网络情况下可以下载
						request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
						//设置通知栏的标题
						request.setTitle("下载");
						//设置通知栏的message
						request.setDescription("今日头条正在下载.....");
						//设置漫游状态下是否可以下载
						request.setAllowedOverRoaming(false);
						//设置文件存放目录
						request.setDestinationInExternalFilesDir(BrowserActivity.this, Environment.DIRECTORY_DOWNLOADS,"123.txt");
						//获取系统服务
						DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
						//进行下载
						long ID = downloadManager.enqueue(request);
						
					break;
				}
			};
			setOnClickListenersOneDepth((ViewGroup) downloadPanel, clicker, 999, values);
			bottomPlaylist.setContentView(downloadPanel);
			bottomPlaylist.tag=values;
			Window win = bottomPlaylist.getWindow();
			if(win!=null) {
				win.setDimAmount(0.2f);
				win.findViewById(R.id.design_bottom_sheet).setBackground(null);
				View decor = win.getDecorView();
				
				decor.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
						-> v.postDelayed(() -> {
					int targetTranY = 0;
					if(Utils.isKeyboardShown(v)) {
						int resourceId = mResource.getIdentifier("status_bar_height", "dimen", "android");
						mStatusBarH = mResource.getDimensionPixelSize(resourceId);
						int max = Utils.rect.height() - ((View) values[0]).getHeight()-mStatusBarH;
						if(max>0) {
							targetTranY = Math.min(max, (int) (-50 * GlobalOptions.density));
						}
					}
					((ViewGroup)v).getChildAt(0).setTranslationY(targetTranY);
					Utils.TrimWindowWidth(win, dm);
					CMN.Log("addOnLayoutChangeListener", Utils.isKeyboardShown(v), dm.widthPixels);
				}, 0));
			}
			
			bottomPlaylist.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
			if(GlobalOptions.isDark) {
				downloadPanel.setBackgroundColor(Color.BLACK);
				((TextView) values[1]).setTextColor(Color.WHITE);
			}
		}
//		View v = (View) _bottomPlaylist.getWindow().getDecorView().getTag();
//		DisplayMetrics dm2 = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getRealMetrics(dm2);
//		v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * _bottomPlaylist.getBehavior().getHalfExpandedRatio() - getResources().getDimension(R.dimen._45_) * 1.75);
//		v.requestLayout();
		Utils.TrimWindowWidth(bottomPlaylist.getWindow(), dm);
		Object[] views = (Object[]) bottomPlaylist.tag;
		((TextView)views[0]).setText(url);
		views[2]=url;
		bottomPlaylist.show();
	}
	
	//longclick
	@Override
	public boolean onLongClick(View v) {
		if(mBrowserSlider.active()) {
			return false;
		}
		switch (v.getId()){
			// 新建标签页
			case R.id.browser_widget10:{
				try {
					long id = validifyid(supressingNxtLux=CMN.now());
					int newtabloc = adapter_idx+1;
					TabHolder holder = new TabHolder();
					TabHolders.add(newtabloc, holder);
					Pages.add(newtabloc, null);
					holder.url=getDefaultPage();
					holder.id=id;
					AttachWebAt(newtabloc, false);
				} catch (IllegalArgumentException e) {
					showT("Hello Future!");
					break;
				}
				View target = v;
				int trans = target.getHeight() / 2;
				ObjectAnimator tv1TranslateY = ObjectAnimator.ofFloat(target, "translationY", 0, -trans, 0);
				tv1TranslateY.setDuration(400);
				target.post(tv1TranslateY::start);
			} break;
		}
		return true;
	}
	
	private String getDefaultPage() {
		return "https://www.bing.com";
	}
	
	
	/** Luxuriously Load Url：使用新的WebView打开链接，受Via浏览器启发，用于“返回不重载”。<br/>
	 *  标签页(tab)处于奢侈模式时({@link TabHolder#getLuxury})，使用此法打开链接。WebView总数有限制，且一段时间内不得打开太多。<br/>
	 *  前进后退时复用或销毁WebView。复用时，需清理网页回退栈。{@link AdvancedBrowserWebView#clearHistroyRequested}*/
	void LuxuriouslyLoadUrl(AdvancedBrowserWebView mWebView, String url) {
		if(!mWebView.hasValidUrl()) {
			mWebView.loadUrl(url);
			mWebView.clearHistroyRequested=true;
			return;
		}
		mWebView.pauseWeb();
		int idx = mWebView.getThisIdx();
		int cc = mWebView.layout.getChildCount();
		if(idx<cc-1) {
			mWebView = (AdvancedBrowserWebView) mWebView.layout.getChildAt(idx+1);
			mWebView.setVisibility(View.VISIBLE);
			mWebView.resumeWeb();
			mWebView.clearHistroyRequested=true;
			mWebView.layout.removeViews(idx+2, cc-idx-2);
		} else {
			mWebView = new_AdvancedNestScrollWebView(null, mWebView);
		}
		mWebView.loadUrl(url);
		id_table.put(mWebView.holder.id, mWebView);
		currentWebView = mWebView;
	}
	
	private AdvancedBrowserWebView new_AdvancedNestScrollWebView(TabHolder holder, AdvancedBrowserWebView patWeb) {
		AdvancedBrowserWebView mWebView = null;
		ViewGroup layout = null;
		if(patWeb!=null) {
			layout = patWeb.layout;
			final int luxLimit = 25;
			int cc = layout.getChildCount();
			if (cc > luxLimit) {
				cc = holder==null?0:cc-1;
				mWebView = (AdvancedBrowserWebView) layout.getChildAt(cc);
				mWebView.clearHistroyRequested=true;
				layout.removeViewAt(cc);
			}
		}
		
		//Utils.sendog(opt);
		if(mWebView==null) {
			mWebView = new AdvancedBrowserWebView(this);
		}
		//Utils.sencat(opt, ViewConfigDefault);
		
		int addIdx = -1;
		if(patWeb!=null) {
			mWebView.stackpath = patWeb.stackpath;
			mWebView.PolymerBackList = patWeb.PolymerBackList;
			if(holder!=null) {
				addIdx=0;
			} else {
				holder = patWeb.holder;
			}
		} else {
			layout = new WebFrameLayout(getBaseContext());
			layout.setBackgroundColor(Color.WHITE);
		}
		mWebView.holder = holder;
		mWebView.layout = layout;
		layout.addView(mWebView, addIdx, new FrameLayout.LayoutParams(-1, -1));
		
		mWebView.setStorageSettings();
		mWebView.setBackEndSettings();
		
		mWebView.addJavascriptInterface(mWebListener, "chrome");
		mWebView.addJavascriptInterface(holder.TabID, "chrmtd");
		
		mWebView.setFocusable(true);
		mWebView.setFocusableInTouchMode(true);
		mWebView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
		mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		
		mWebView.setWebViewClient(mWebListener);
		mWebView.setDownloadListener(mWebListener);
		mWebView.setOnScrollChangedListener(mWebListener);

		return mWebView;
	}
	
	void JumpToUrl(String url) {
		showT("接收到目标，准备下载…");
		//AttachWebAt(adapter_idx);
		if(!url.startsWith("http")){
			Matcher m = httpPattern.matcher(url);
			if(m.find()){
				url=m.group(1);
			}
		}
		currentWebView.loadUrl(url);
		//currentWebView.onResume();
		//currentWebView.resumeTimers();
		//currentWebView.requestLayout();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(systemIntialized) {
			Glide.get(this).clearMemory();
			//if(false)
			try {
				final File def = new File(getExternalFilesDir(null),"sites_default.txt");      //!!!原配
				BufferedWriter output2 = new BufferedWriter(new FileWriter(def,false));
				for (int i = 0; i < TabHolders.size(); i++) {
					TabHolder holder=TabHolders.get(i);
					output2.write(filteredStorageUrl(holder.url));
					output2.write(TitleSep);
					output2.write(Long.toString(holder.id));
					output2.write(FieldSep);
					output2.write(Long.toString(holder.flag));
					output2.write(FieldSep);
					output2.write(filteredStorageName(holder.title));
					output2.write(FieldSep);
					output2.write(filteredStorageName(holder.page_search_term));
					if(i==adapter_idx) {
						output2.write("|=");
					}
					output2.write("\r");
				}
				output2.flush();
				output2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			closing=true;
			Collection<AdvancedBrowserWebView> values = id_table.values();
			for (AdvancedBrowserWebView wv:values) {
				wv.deconstruct();
			}
			id_table.clear();
			CMN.Log("关闭历史记录...");
			historyCon.close();
		}
	}
	
	private String filteredStorageUrl(String url) {
		if(url.contains(TitleSep)) {
			url = url.replace(TitleSep,"");
		}
		return url;
	}
	
	private String filteredStorageName(String fieldVal) {
		if(fieldVal!=null) {
			Matcher m = IllegalStorageNameFilter.matcher(fieldVal);
			if(m.find()) {
				fieldVal = m.replaceAll("_");
			}
		}
		if(TextUtils.isEmpty(fieldVal)) {
			fieldVal = " ";
		}
		return fieldVal;
	}
	
	private void setStatusBarColor(Window window){
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		if(Build.VERSION.SDK_INT>=21) {
			window.setStatusBarColor(Color.TRANSPARENT);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}
	
	public  ArrayList<String> mClipboard;
	ClipboardManager.OnPrimaryClipChangedListener ClipListener;
	private ListView ClipboardList;
	private String mPreviousCBContent;
	
	void SetupPasteBin() {
		ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		if(clipboardManager!=null){
			if(mClipboard==null){
				//pasteBin = mDrawerListView.findViewById(R.id.pastebin);
				//pasteBin.setOnClickListener(this);
				mClipboard=new ArrayList<>(12);
				//mClipboard.add("Happy");
			}
			if(true){//Options.getShowPasteBin()
				//pasteBin.setVisibility(View.VISIBLE);
				if(ClipListener==null){
					ClipListener = () -> {
						if(true)//opt.getPasteBinEnabled()
							try {
								ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
								ClipData pclip = cm.getPrimaryClip();
								ClipData.Item firstItem = pclip.getItemAt(0);
								String content = firstItem.getText().toString();
								//CMN.Log("剪贴板监听器:", content);
								if(System.currentTimeMillis()-lastClickTime<256 && content.equals(mPreviousCBContent))
									return;
								int i = 0;
								for (; i < mClipboard.size(); i++) {
									if(mClipboard.get(i).equals(content))
										break;
								}
								if(i==mClipboard.size()) {
									mClipboard.add(0, content);
								}else {
									mClipboard.add(0, mClipboard.remove(i));
								}
								boolean focused=hasWindowFocus();
								//boolean toFloat=Options.getPasteTarget()==3;
								JumpToUrl(content);
								mPreviousCBContent=content;
								lastClickTime=System.currentTimeMillis();
							} catch(Exception e) { CMN.Log("ClipListener:"+e); }
					};
				}
				//CMN.Log("clipboardManager.addPrimaryClipChangedListener");
				clipboardManager.removePrimaryClipChangedListener(ClipListener);
				clipboardManager.addPrimaryClipChangedListener(ClipListener);
			}else {
				//pasteBin.setVisibility(View.GONE);
				if(ClipListener!=null)
					clipboardManager.removePrimaryClipChangedListener(ClipListener);
			}
		}
	}
	
	private static class MyHandler extends BaseHandler{
		private final WeakReference<Toastable_Activity> activity;
		MyHandler(Toastable_Activity a) {
			this.activity = new WeakReference<>(a);
		}
		@Override
		public void handleMessage(@NonNull Message msg) {
			if(activity.get()==null) return;
			BrowserActivity a = ((BrowserActivity)activity.get());
			switch (msg.what) {
				case 101:
					a.showT(msg.obj);
					break;
				case 110:
					String url= (String) msg.obj;
					new Thread(new Runnable() {
						@Override
						public void run() {
							CMN.Log();
							try {
								//url=timePattern.matcher(url).replaceAll("t="+(System.currentTimeMillis()/1000));
								String fname = requestPattern.matcher(url).replaceAll("");
								fname = fname.substring(fname.lastIndexOf("/"));
								File path=new File("/sdcard/Download/", fname);
								Object obj;
								CMN.Log("shouldInterceptRequest ", url, "to", fname);
								if(!path.exists()){
									Exception e = null;
									try {
										obj="文件正在下载中…";
										//mHandler.sendMessage(msg);
										URL requestURL = new URL(url);
										String val;
										HttpURLConnection urlConnection = (HttpURLConnection) requestURL.openConnection();
										urlConnection.setRequestMethod("GET");
										////urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());
										urlConnection.setRequestProperty("Accept-Language", "zh-CN");
										urlConnection.setRequestProperty("Referer", url.toString());
										urlConnection.setConnectTimeout(60000);
										//urlConnection.setRequestProperty("Charset", "UTF-8");
										//urlConnection.setRequestProperty("Connection", "Keep-Alive");
										val=null;
										urlConnection.setRequestProperty("User-agent", (val!=null?val:"Mozilla/5.0 (Linux; Android 9; VTR-AL00 Build/HUAWEIVTR-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36"));
										urlConnection.connect();
										InputStream is = urlConnection.getInputStream();
										
										ReusableByteOutputStream bos = new ReusableByteOutputStream();
										bos.reset();
										byte[] buffer = new byte[4096];
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
										//if(a.url_file_recorder==null) a.url_file_recorder = new FileOutputStream("/sdcard/file-url-list.txt", true);
										//a.url_file_recorder.write((mWebView.url+TitleSep+path.getName()).getBytes());
										//a.url_file_recorder.flush();
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
								if(obj!=null){
									Message msg = new Message();
									msg.what=101;
									msg.obj=obj;
									a.mHandler.sendMessage(msg);
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
					break;
			}
		}
	}
	
	@SuppressWarnings("ALL")
	public static class TabHolder implements WebOptions {
		public String url;
		public String title;
		public String page_search_term;
		public long flag;
		public long id;
		public final TabIdentifier TabID = new TabIdentifier();
		
		@Multiline(flagPos=0, debug=0) public boolean getLuxury(){ flag=flag; throw new RuntimeException(); }
		
		@Multiline(flagPos=2) public boolean getForbidLocalStorage(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=2) public boolean toggleForbidLocalStorage(){ flag=flag; throw new IllegalArgumentException(); }
		@Multiline(flagPos=3, shift=1) public boolean getForbidCookie(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=4, shift=1) public boolean getForbidDom(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=5, shift=1) public boolean getForbidDatabase(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=6, shift=1) public boolean getApplyOverride_group_storage(){ flag=flag; throw new RuntimeException(); }
		
		@Multiline(flagPos=7) public boolean getPCMode(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=7) public boolean togglePCMode(){ flag=flag; throw new IllegalArgumentException(); }
		@Multiline(flagPos=8, shift=1) public boolean getEnableJavaScript(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=9) public boolean getMuteAlert(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=10) public boolean getMuteDownload(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=11, shift=1) public boolean getApplyOverride_group_client(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=12) public boolean getForbitNetworkImage(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=13) public boolean getPremature(){ flag=flag; throw new RuntimeException(); }
		
		public boolean getApplyRegion(int groupID) {
			switch (groupID) {
				case StorageSettings: return getApplyOverride_group_storage();
				case BackendSettings: return getApplyOverride_group_client();
			}
			return false;
		}
		
		class TabIdentifier{
			@JavascriptInterface
			public long get() {
				return id;
			}
		}
	}
	
	class ItemOffsetDecoration extends RecyclerView.ItemDecoration {
		private int spacing = (int) (GlobalOptions.density*8);
		@Override
		public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
			super.getItemOffsets(outRect, view, parent, state);
			RecyclerView.Adapter adapter = parent.getAdapter();
			RecyclerView.ViewHolder vh = parent.findContainingViewHolder(view);
			int pos = vh.getBindingAdapterPosition();
			boolean isFirst = pos==0;
			boolean isLast = pos==adapter.getItemCount()-1;
			if(isFirst) {
				outRect.set(spacing, spacing, spacing, spacing / 2);
			} else if(isLast) {
				outRect.set(spacing, spacing / 2, spacing, spacing);
			} else {
				outRect.set(spacing, spacing / 2, spacing, spacing / 2);
			}
		}
	}
	
	public UniCoverClicker getUCC() {
		int id = WeakReferenceHelper.webview_panel_dialog;
		UniCoverClicker webviewPanel = (UniCoverClicker) getReferencedObject(id);
		if(webviewPanel==null) {
			putReferencedObject(id, webviewPanel=new UniCoverClicker());
		}
		return webviewPanel;
	}
	
	/**
	 ''+window.getSelection()
	 */
	@Multiline
	public static final String CollectWord=StringUtils.EMPTY;
	
	/**
	 var range=window.getSelection().getRangeAt(0);
	 var flmstd = document.getElementById('_PDict_Renderer');
	 if(!flmstd){
	 flmstd = document.createElement('div');
	 flmstd.id='_PDict_Renderer';
	 } else {
	 flmstd.innerHTML='';
	 }
	 flmstd.class='_PDict';
	 flmstd.appendChild(range.cloneContents());
	 flmstd.innerHTML;
	 */
	@Multiline
	public static final String CollectHtml=StringUtils.EMPTY;
	
	
	public final static String GoogleTranslate = "com.google.android.apps.translate";
	public final static String PlainDictionary = "com.knziha.plod.plaindict";
	public final static String AnkiDroid = "com.ichi2.anki";
	
	public final class UniCoverClicker implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener{
		AlertDialog d;
		String[] itemsB;
		String[] itemsD;
		String CurrentSelected="";
		boolean dismissing_ucc;
		TwoColumnAdapter twoColumnAda;
		RecyclerView twoColumnView;
		
		UniCoverClicker() {
			itemsB = mResource.getStringArray(R.array.dict_tweak_arr2);
			itemsD = mResource.getStringArray(R.array.text_tweak_arr);
		}
		
		public void show() {
			hideKeyboard();
			/* build_further_dialog() */
			String[] items = opt.getTextPanel()?itemsD:itemsB;
			if(d==null) {
				twoColumnView = new RecyclerView(BrowserActivity.this);
				twoColumnView.setClipToPadding(false);
				GridLayoutManager lman;
				twoColumnView.setLayoutManager(lman = new GridLayoutManager(BrowserActivity.this, 6));
				lman.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
					@Override
					public int getSpanSize(int position) {
						if (position>=10) return 2;
						return 3;
					}
				});
				twoColumnAda = new TwoColumnAdapter(items);
				twoColumnAda.setOnItemClickListener(this);
				twoColumnAda.setOnItemLongClickListener(this);
				twoColumnView.setAdapter(twoColumnAda);
				int pad = (int) (GlobalOptions.density*8);
				twoColumnView.setPadding(0, pad, 0, 0);
				twoColumnView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
				
				AlertDialog webviewPanel = new AlertDialog.Builder(BrowserActivity.this
						, GlobalOptions.isDark ? R.style.DialogStyle3Line
						: R.style.DialogStyle4Line)
						.setView(twoColumnView)
						.create();
				//webviewPanel.setTitle("文本选项");
				Window win = webviewPanel.getWindow();
				if(win!=null) {
					win.getAttributes().width = -2;
					win.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					win.setBackgroundDrawableResource(R.drawable.frame_dialog);
				}
				((ViewGroup)webviewPanel.findViewById(R.id.action_bar_root).getParent()).addOnLayoutChangeListener((v1, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
					double coord = 1.2*(bottom - top);
					if(right-left >= coord) {
						WindowManager.LayoutParams NaughtyDialogAttr = d.getWindow().getAttributes();
						NaughtyDialogAttr.width = (int) coord;
						d.getWindow().setAttributes(d.getWindow().getAttributes());
					}
				});
				d = webviewPanel;
			}
			d.show();
		}
		
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			return onItemClick(parent, view, position, id, true);
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			onItemClick(parent, view, position, id, false);
		}
		
		public boolean onItemClick(AdapterView<?> parent, @Nullable View view, int position, long id, boolean isLongClicked) {
			//CMN.Log("数据库 ", itemsA[0], getString(R.string.bmSub), itemsA[0].equals(getString(R.string.bmSub)));
			int dissmisstype=0;
			try {
				//if(isLongClicked) CMN.Log("长按开始……");
				if(opt.getTextPanel()) {
					position+=13;
				}
				AdvancedBrowserWebView mWebView = currentWebView;
				switch (position) {
					/* 全选 */
					case 0: {
						mWebView.evaluateJavascript("document.execCommand('selectAll')", null);
					} break;
					/* 标注颜色 */
					case 1: {
					} break;
					/* 高亮 */
					case 2: {
					} break;
					/* 清除高亮 */
					case 3: {
					} break;
					/* 下划线 */
					case 4: {
					} break;
					/* 清除下划线 */
					case 5: {
					} break;
					/* 翻译 */
					case 6:
					case 8:
					case 9:
					case 10:
					case 19:
					case 20:
					case 21:
					case 22: {
						handleVersatileShare(position);
					} break;
					/* 全文朗读 */
					case 13: {
					} break;
					/* 添加笔记 */
					case 14: {
					} break;
					/* 荧黄标亮 */
					case 15: {
					} break;
					/* 荧黄标亮 */
					case 16: {
					} break;
					/* 荧黄划线 */
					case 17: {
					} break;
					/* 荧红划线 */
					case 18: {
					} break;
					/* 切换 */
					case 12:
					case 25: {
						boolean val=!opt.getTextPanel();
						opt.setTextPanel(val);
						twoColumnAda.setItems(val?itemsD:itemsB);
					} break;
				}
				if(dissmisstype==1 || dissmisstype==2) {
					dismissing_ucc=true;
					d.dismiss();
				}
			} catch (Exception e){
				CMN.Log(e);
			}
			
			return false;
		}
		
		public boolean detached() {
			return d==null||Utils.isWindowDetached(d.getWindow());
		}
	}
	
	public void handleVersatileShare(int finalPosition) {
		currentWebView.evaluateJavascript(finalPosition==22?CollectHtml:CollectWord, value -> {
			if(value.charAt(0)=='"'&&value.length()>2) {
				value = value.substring(1, value.length()-1);
			}
			value = StringEscapeUtils.unescapeJava(value);
			if(finalPosition%10==0) {
			
			} else {
				boolean processText = finalPosition==19||finalPosition==21;
				String Action=processText?Intent.ACTION_PROCESS_TEXT:Intent.ACTION_SEND;
				String Extra=processText?Intent.EXTRA_PROCESS_TEXT:Intent.EXTRA_TEXT;
				Intent launcher = new Intent(Action);
				launcher.setType("text/plain");
				int errorMsg=0;
				if(finalPosition==6||finalPosition ==19) {
					launcher.setPackage(GoogleTranslate);
					launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					launcher.putExtra(Extra, value);
					errorMsg = R.string.gt_no_inst;
				} else if(finalPosition==8||finalPosition==21) {
					launcher.setPackage(PlainDictionary);
					launcher.putExtra(Extra, value);
					errorMsg = R.string.pd_no_inst;
				} else {
					launcher.setPackage(AnkiDroid);
					launcher.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					launcher.putExtra(Extra, value);
					errorMsg = R.string.ak_no_inst;
				}
				try {
					startActivity(launcher);
				} catch (Exception e) {
					showT(errorMsg);
				}
			}
		});
	}
	
	void hideKeyboard() {
		View ht = getCurrentFocus();
		if(ht==null) ht = etSearch;
		imm.hideSoftInputFromWindow(ht.getWindowToken(),0);
	}
	
	@Override @SuppressWarnings("ALL")
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		CMN.Log("onActivityResult", requestCode, resultCode, data);
		if(RequsetUrlFromCamera==requestCode) {
			String text = data.getStringExtra(Intent.EXTRA_TEXT);
			if(text!=null) {
				webtitle_setVisibility(true);
				execBrowserGoTo(text);
			}
			//etSearch.setText(data.getStringExtra(Intent.EXTRA_TEXT));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onActionModeStarted(ActionMode mode) {
		View v = getCurrentFocus();
		CMN.Log("-->onActionModeStarted", v);
		Menu menu;
		if (v==currentWebView && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
			AdvancedBrowserWebView wv = currentWebView;
			mode.setTitle(null);
			mode.setSubtitle(null);
			
			menu = mode.getMenu();
			
			String websearch = null;
			int websearch_id = Resources.getSystem().getIdentifier("websearch", "string", "android");
			if (websearch_id != 0)
				websearch = mResource.getString(websearch_id);
			
			String copy = mResource.getString(android.R.string.copy);
			int findCount = 2;
			MenuItem item1 = null;
			MenuItem item2 = null;
			MenuItem item;
			for (int i = 0; i < menu.size(); i++) {
				item = menu.getItem(i);
				String title = item.getTitle().toString();
				if (title.equals(copy)) {
					item1 = item;
					findCount--;
				} else if (title.equalsIgnoreCase(websearch)) {
					item2 = item;
					findCount--;
				}
				if (findCount == 0) break;
			}
			menu.clear();
			int ToolsOrder = 0;
			int af = MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT;
			if (item1 != null) {
				item = menu.add(0, item1.getItemId(), ++ToolsOrder, item1.getTitle()).setOnMenuItemClickListener(wv)
						.setIcon(item1.getIcon())
						.setShowAsActionFlags(af);
			}
			if (item2 != null) {
				item = menu.add(0, item2.getItemId(), ++ToolsOrder, item2.getTitle()).setOnMenuItemClickListener(wv)
						.setIcon(item2.getIcon())
						.setShowAsActionFlags(af);
			}
			Drawable hld = mResource.getDrawable(R.drawable.round_corner_dot);
			hld.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
			menu.add(0, R.id.toolbar_action0, ++ToolsOrder, "高亮").setOnMenuItemClickListener(wv).setShowAsActionFlags(af).setIcon(hld);
			menu.add(0, R.id.toolbar_action1, ++ToolsOrder, R.string.tools).setOnMenuItemClickListener(wv).setShowAsActionFlags(af).setIcon(R.drawable.ic_tune_black_24dp);
			menu.add(0, R.id.toolbar_action3, ++ToolsOrder, "TTS").setOnMenuItemClickListener(wv).setShowAsActionFlags(af).setIcon(R.drawable.voice_ic_big);
		}
		
		super.onActionModeStarted(mode);
	}
	
	public boolean checkWebSelection() {
		if(getCurrentFocus() == currentWebView){
			if(currentWebView.bIsActionMenuShown){
				currentWebView.clearFocus();
				return true;
			}
		}
		return false;
	}
}
