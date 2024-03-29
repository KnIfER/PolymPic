package com.knziha.polymer;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.RelativeSizeSpan;
import android.view.ActionMode;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.math.MathUtils;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.RgxPlc;
import com.knziha.polymer.browser.BlockingDlgs;
import com.knziha.polymer.browser.DownloadHandlerStd;
import com.knziha.polymer.browser.DownloadUIHandler;
import com.knziha.polymer.browser.webkit.BitmapWaiter;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebViewHelper;
import com.knziha.polymer.browser.webkit.XPlusWebView;
import com.knziha.polymer.browser.webkit.XWalkWebView;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.ActivityMainBinding;
import com.knziha.polymer.databinding.SearchHintsItemBinding;
import com.knziha.polymer.preferences.MenuGrid;
import com.knziha.polymer.preferences.SettingsPanel;
import com.knziha.polymer.qrcode.QRActivity;
import com.knziha.polymer.qrcode.QRGenerator;
import com.knziha.polymer.toolkits.Utils.BU;
import com.knziha.polymer.toolkits.Utils.ReusableByteOutputStream;
import com.knziha.polymer.webfeature.NavigationManager;
import com.knziha.polymer.webfeature.NightMode;
import com.knziha.polymer.webfeature.QuickBrowserSettingsPanel;
import com.knziha.polymer.webfeature.WebDictsManager;
import com.knziha.polymer.webfeature.SearchHistoryAndInputMethodSettings;
import com.knziha.polymer.webfeature.WebAnnotationPanel;
import com.knziha.polymer.webslideshow.TabViewAdapter;
import com.knziha.polymer.webslideshow.ViewUtils;
import com.knziha.polymer.webslideshow.WebPic.WebPic;
import com.knziha.polymer.webstorage.BrowserDownloads;
import com.knziha.polymer.webstorage.BrowserHistory;
import com.knziha.polymer.webstorage.DomainInfo;
import com.knziha.polymer.webstorage.SardineCloud;
import com.knziha.polymer.webstorage.WebOptions;
import com.knziha.polymer.webstorage.WebStacksSer;
import com.knziha.polymer.webstorage.WebViewSettingsDialog;
import com.knziha.polymer.widgets.AppIconsAdapter;
import com.knziha.polymer.widgets.BottomBarBehavior;
import com.knziha.polymer.widgets.DescriptiveImageView;
import com.knziha.polymer.widgets.DialogWithTag;
import com.knziha.polymer.widgets.EditFieldHandler;
import com.knziha.polymer.widgets.MergedWebOptWidget;
import com.knziha.polymer.widgets.PDFPrintManager;
import com.knziha.polymer.widgets.PopupMenuHelper;
import com.knziha.polymer.widgets.TextMenuView;
import com.knziha.polymer.widgets.TwoColumnAdapter;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WaveView;
import com.knziha.polymer.widgets.WebFrameLayout;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_DOMAIN;
import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_TAB;
import static com.knziha.polymer.Utils.Options.getLimitHints;
import static com.knziha.polymer.Utils.Options.getTransitSearchHints;
import static com.knziha.polymer.WeakReferenceHelper.topDomainNamesMap;
import static com.knziha.polymer.WebCompoundListener.CustomViewHideTime;
import static com.knziha.polymer.WebCompoundListener.PrintStartTime;
import static com.knziha.polymer.WebCompoundListener.httpPattern;
import static com.knziha.polymer.WebCompoundListener.requestPattern;
import static com.knziha.polymer.browser.webkit.WebViewHelper.bAdvancedMenu;
import static com.knziha.polymer.webslideshow.ViewUtils.ViewDataHolder;
import static com.knziha.polymer.webstorage.WebOptions.BackendSettings;
import static com.knziha.polymer.webstorage.WebOptions.ImmersiveSettings;
import static com.knziha.polymer.webstorage.WebOptions.LockSettings;
import static com.knziha.polymer.webstorage.WebOptions.StorageSettings;
import static com.knziha.polymer.webstorage.WebOptions.TextSettings;
import static com.knziha.polymer.webstorage.WebOptions.WebTypes;
import static com.knziha.polymer.webstorage.WebOptions.WebTypes.WEBTYPE_INTEL;
import static com.knziha.polymer.webstorage.WebOptions.WebTypes.WEBTYPE_SYSTEM;
import static com.knziha.polymer.webstorage.WebOptions.WebTypes.WEBTYPE_TENCENT;
import static com.knziha.polymer.widgets.Utils.DummyTransX;
import static com.knziha.polymer.widgets.Utils.EmptyCursor;
import static com.knziha.polymer.widgets.Utils.RequestPDFFile;
import static com.knziha.polymer.widgets.Utils.RequsetFileFromFilePicker;
import static com.knziha.polymer.widgets.Utils.RequsetUrlFromCamera;
import static com.knziha.polymer.widgets.Utils.RequsetUrlFromStorage;
import static com.knziha.polymer.widgets.Utils.getViewItemByPath;
import static com.knziha.polymer.widgets.Utils.isKeyboardShown;
import static com.knziha.polymer.widgets.Utils.postInvalidateLayout;
import static com.knziha.polymer.widgets.Utils.setOnClickListenersOneDepth;

@SuppressWarnings({"rawtypes","ClickableViewAccessibility"
		,"IntegerDivisionInFloatingPointContext"
		,"NonConstantResourceId"
})
public class BrowserActivity extends Toastable_Activity implements View.OnClickListener, View.OnLongClickListener, BitmapWaiter, SettingsPanel.FlagAdapter {
	public final static String TitleSep="\n";
	public final static String FieldSep="|";
	public final static Pattern IllegalStorageNameFilter=Pattern.compile("[\r\n|]");
	public ValueCallback<Uri[]> filePathCallback;
	public boolean mInterceptorListenerHandled;
	public View.OnClickListener mInterceptorListener;
	View searchbartitle;
	public TextView webtitle;
	private boolean etSearch_scrolled;
	
	public boolean tabsManagerIsDirty;
	public ImageView imageViewCover;
	ImageView imageViewCover1;
	
	public int adapter_idx;
	boolean focused = true;
	public Map<Long, WebFrameLayout> id_table = Collections.synchronizedMap(new HashMap<>(1024));
	//private ArrayList<WebViewmy> WebPool = new ArrayList<>(1024);
	public ArrayList<TabHolder> TabHolders = new ArrayList<>(1024);
	public ArrayList<TabHolder> closedTabs = new ArrayList<>(1024);
	public ArrayList<TabHolder> activeClosedTabs = new ArrayList<>(1024);
	HashMap<Long, TabHolder> allTabs;
	public HashSet<WebFrameLayout> checkWebViewDirtyMap = new HashSet<>();
	boolean tabsRead = false;
	
	FileOutputStream url_file_recorder;
	
	MyHandler mHandler;
	
	private WebTypes webType=WEBTYPE_SYSTEM;
	
	private UniversalWebviewInterface wvPreInitInstance;
	
	public WebFrameLayout currentViewImpl;
	public TabHolder currentWebHolder;
	public UniversalWebviewInterface currentWebView;
	
	public Object mXWalkDelegate;
	
	public String currentWebDictUrl;
	public WebDictsManager webDictsManager = new WebDictsManager(this);
	
	public TabViewAdapter tabViewAdapter;
	private RecyclerView.Adapter/*<ViewDataHolder<SearchHintsItemBinding>>*/ adaptermy2;
	
	private CoordinatorLayout.Behavior bottombarScrollBehaviour;
	private CoordinatorLayout.Behavior bottombarHideBehaviour;
	private AppBarLayout.LayoutParams toolbatLP;
	private CoordinatorLayout.LayoutParams bottombarLP;
	public boolean anioutTopBotForTab=false;
	private CoordinatorLayout.Behavior scrollingBH;
	private boolean fixAll = false;
	private boolean fixTopBar = false;
	private boolean fixBotBar = false;
	private View toolbarti;
	int printSX;
	int printSY;
	float printScale;
	public BrowserSlider mBrowserSlider;
	public LexicalDBHelper historyCon;
	
//	private Cursor ActiveUrlCursor=Utils.EmptyCursor;
//	private Cursor ActiveSearchCursor=Utils.EmptyCursor;
	private int ActiveSearchCount =0;
	private ArrayList<Cursor> ActiveSearchCursors=new ArrayList<>();
	private boolean keyboard_hidden;
	
	ObjectAnimator progressProceed;
	ObjectAnimator progressTransient;
	Animator animatorTransient;
	Drawable progressbar_background;
	boolean supressingProgressListener;
	
	long supressingNxtLux;
	public static boolean closing;
	
	WebCompoundListener mWebListener;
	private PullHintsRunnable pull_hints_runnable;
	private boolean search_hints_vising=true;
	public ActivityMainBinding UIData;
	private String lastUrlSet;
	private boolean goToBarcodeScanner;
	public boolean tabsDirty;
	
	public RequestBuilder<Drawable> glide;
	
	private DownloadUIHandler downloadDlgHandler;
	
	private Paint paint = new Paint();
	private boolean bottombarHidden;
	private int softMode;
	public final int softModeHold = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
	public final int softModeResize = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
	private SardineCloud syncHandler;
	private WaveView waveView;
	private boolean IsShowingSearchHistory;
	private boolean IsReqCheckingNxtResume;
	
	public int _45_;
	
	public SettingsPanel settingsPanel;
	public PopupWindow   settingsPopup;
	public boolean settingsBelowAppbar;
	private NavigationManager navManager;
	private long lastOpenedTab;
	private long LastAutoNewTabTime;
	
	private View actionBarRoot;
	
	public boolean 黑狗贱畜; // 败坏秧苗
	public boolean 此校领导;
	public boolean 假义伪道; // 犬儒狗生！
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		CMN.Log("onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
		mResource = getResources();
		mStatusBarH = Utils.getStatusBarHeight(mResource);
		mConfiguration.setTo(newConfig);
		Display display = getWindowManager().getDefaultDisplay();
		display.getMetrics(dm);
		GlobalOptions.rotation = display.getRotation();
		tabViewAdapter.onConfigurationChanged();
		if(!GlobalOptions.isLarge) {
			// 压扁你，底部工具栏！
			float targetH = 45;
			if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
				targetH = 40;
				if(dm.heightPixels<450*GlobalOptions.density) {
					targetH = 38;
				}
			}
			targetH*=GlobalOptions.density;
			UIData.bottombar2.getLayoutParams().height = (int) (targetH);
			decideWebviewPadding(currentViewImpl);
		}
		if(settingsPanel!=null) {
			root.postDelayed(postOnConfigurationChanged, 200);
		}
	}
	
	Runnable postOnConfigurationChanged = new Runnable() {
		@Override
		public void run() {
			if(settingsPopup!=null) {
				//settingsPopup.dismiss();
				embedPopInCoordinatorLayout(settingsPopup, settingsBelowAppbar);
			} else if(settingsPanel!=null ){
				int pad = UIData.bottombar2.getHeight();
				if (UIData.appbar.getTop()>=0) {
					pad+=UIData.appbar.getHeight();
				}
				settingsPanel.setInnerBottomPadding(pad);
			}
			MenuGrid menuGrid = (MenuGrid) getReferencedObject(WeakReferenceHelper.menu_grid);
			if(menuGrid!=null) {
				menuGrid.refreshMenuGridSize(false);
			}
		}
	};
	
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
		if (hasFocus) {
			WebFrameLayout layout = this.currentViewImpl;
			if (layout!=null && layout.selSuppressed && layout.hasSelection()) {
				layout.suppressSelection(true);
			}
			if(resumer!=null) {
				resumer.resume();
				resumer = null;
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		if(!systemIntialized) {
			super.onBackPressed();
			return;
		}
		if(webDictsManager.isVisible()) {
			UIData.layoutListener.onTouchEvent(null);
		} else if(settingsPanel!=null) {
			hideSettingsPanel();
		} else if(navManager !=null && navManager.dismiss(true)) {
			// intentionally empty.
		} else if(webtitle.getVisibility()!=View.VISIBLE) {
			webtitle_setVisibility(false);
			etSearch_clearFocus();
		} else if(checkWebSelection()) {
			// intentionally empty.
		} else if(mWebListener.dismissAppToast()) {
			// intentionally empty.
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CMN.Log("getting mid 0 ...", Thread.currentThread().getId(), Build.VERSION.SDK_INT);
		
		
		Window win = getWindow();
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		//win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setSoftInputMode(softModeResize);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if(Utils.littleCake) {
			requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
			supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		}
		
		//win.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		
		boolean transit = opt.getTransitSplashScreen();
		//transit = false;
		if(!transit) setTheme(R.style.AppThemeRaw);
		
		UIData = DataBindingUtil.setContentView(this, R.layout.activity_main);
		
		root=UIData.root;

		webType = WebTypes.values()[opt.getWebType()];
		
		mWebListener = Utils.version>=21?new WebCompoundListener(this):new WebCompatListener(this);
		//mWebListener = new WebCompoundListener(this);
		
		imageViewCover = new ImageView(this);
		imageViewCover1 = new ImageView(this);
		
		final RequestOptions glide_options = new RequestOptions()
				.format(DecodeFormat.PREFER_ARGB_8888)//DecodeFormat.PREFER_ARGB_8888
				.skipMemoryCache(false)
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				//.onlyRetrieveFromCache(true)
				.fitCenter()
				.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
				;
		glide = Glide.with(this)
				.load(new WebPic(0, 0, id_table))
				.apply(glide_options);
		
		if(transit) {
			root.setAlpha(0);
			ObjectAnimator fadeInContents = ObjectAnimator.ofFloat(root, "alpha", 0, 1);
			fadeInContents.setInterpolator(new AccelerateDecelerateInterpolator());
			fadeInContents.setDuration(350);
			if (Utils.version>=23) { // 较旧版本的安卓，设置背景会有闪烁。
				fadeInContents.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						win.setBackgroundDrawable(new ColorDrawable(0));
					}
				});
			}
			root.post(fadeInContents::start);
		}
		
		CMN.mResource = mResource; // todo remove debug
		
		setOnClickListenersOneDepth(UIData.bottombar2, this, 1, null);
		setOnClickListenersOneDepth(UIData.toolbarContent, this, 1, null);
		
		UIData.browserWidget10.setOnLongClickListener(this);
		etSearch=UIData.etSearch;
		if(Utils.littleCake) {
			actionBarRoot = Utils.getNthParentNullable(root, 2);
			if (actionBarRoot instanceof ViewGroup && actionBarRoot.getId()==R.id.action_bar_root) {
				etSearch.setCustomSelectionActionModeCallback(new ActionMode.Callback() {
					View actionModeBar;
					Runnable restoreAbility = () -> {
						if (actionModeBar!=null) {
							Utils.addViewToParent(actionModeBar , (ViewGroup) actionBarRoot, 1);
						}
					};
					@Override public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }
					@Override public void onDestroyActionMode(ActionMode mode) {
						actionBarRoot.postDelayed(restoreAbility, 800); // 因为动画所以延迟调用 | wait for animation to end
					}
					@Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						actionBarRoot.removeCallbacks(restoreAbility);
						actionModeBar = actionBarRoot.findViewById(R.id.action_mode_bar);
						if (actionModeBar!=null) {
							Utils.removeView(actionModeBar);
						}
						return true;
					}
					@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
						return false;
					}
				});
			}
		}
		
		progressbar_background=UIData.progressbar.getBackground();
		webtitle=UIData.webtitle;
		toolbatLP=(AppBarLayout.LayoutParams) UIData.toolbar.getLayoutParams();
		bottombarLP=(CoordinatorLayout.LayoutParams) UIData.bottombar2.getLayoutParams();
		bottombarScrollBehaviour = bottombarLP.getBehavior();
		bottombarHideBehaviour = new BottomBarBehavior(getBaseContext(), null);
		scrollingBH = new AppBarLayout.ScrollingViewBehavior(getBaseContext(), null);
		
		progressProceed = ObjectAnimator.ofInt(progressbar_background,"level", 0, 0);
		progressProceed.setDuration(100);
		progressTransient = ObjectAnimator.ofFloat(UIData.progressbar, "alpha", 0, 0);
		AnimatorSet transientAnimator = new AnimatorSet();
		transientAnimator.playTogether(progressTransient, progressProceed);
		transientAnimator.setDuration(100);
		animatorTransient = transientAnimator;
		animatorTransient.addListener(new AnimatorListenerAdapter() {
			@Override public void onAnimationEnd(Animator animation) {
				progressbar_background.setLevel(0);
				UIData.progressbar.setVisibility(View.GONE);
				if (TestHelper.showSearchTabs || !tabViewAdapter.isVisible()) {
					UIData.ivRefresh.setImageResource(R.drawable.ic_refresh_white_24dp);
				}
			}
			@Override public void onAnimationCancel(Animator animation) {
				UIData.progressbar.setAlpha(1);
			}
		});
		checkLog(savedInstanceState);
		CrashHandler.getInstance(this, opt).TurnOn();
		setStatusBarColor(getWindow());
		mHandler = new MyHandler(this);
		CMN.browserTaskId = getTaskId();
		Utils.PadWindow(root);
		if (opt.getLockScreenOn()) acquireWakeLock();
	}
	
	public Drawable bottombar2Background() {
		return UIData.bottombar2.getBackground();
	}
	
	/** 吾何人哉？幻界之主，九方之神人也 */
	@Override
	protected void onPause() {
		super.onPause();
		focused=false;
		if(systemIntialized) {
			if(checkWebViewDirtyMap.size()>0) {
				for(WebFrameLayout wfl:checkWebViewDirtyMap) {
					wfl.saveIfNeeded();
				}
				checkWebViewDirtyMap.clear();
			}
			if (currentViewImpl != null) {
				currentViewImpl.saveIfNeeded();
			}
			if(opt.checkTabsOnPause()) {
				checkTabs();
			}
			if(false&&mWebListener!=null) mWebListener.parseJinKe();
		}
	}
	
	private void checkTabs() {
		if(tabsDirty) {
			StringBuilder stringBuilder = new StringBuilder();
			for(TabHolder tabHolder:TabHolders) {
				stringBuilder.append(tabHolder.id).append(" ");
			}
			CMN.Log("tabs::", stringBuilder);
			opt.putOpenedTabs(stringBuilder.toString(), currentWebHolder.id);
			tabsDirty = false;
		} else if(currentWebHolder.id!= lastOpenedTab) {
			opt.putLastOpenedTabID(lastOpenedTab=currentWebHolder.id);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//PrintStartTime=0;
		focused=true;
		CMN.Log("onResume");
		if (IsReqCheckingNxtResume) {
			checkResume();
		}
	}
	
	protected boolean getShouldKeepScreenOn() {
		return opt.getLockScreenOn();
	}
	
	private void checkResume() {
//		modThreeBtnForMenuGrid(menuGrid!=null, true);
		IsReqCheckingNxtResume = false;
	}
	@Override
	public void further_loading(Bundle savedInstanceState) {
		if(false) {
			//Utils.removeView(UIData.webcoord);
			//Utils.removeView(UIData.root);
			return;
		}
		if(historyCon==null) {
			if(TestHelper.debuggingWebType!=null) {
				webType=TestHelper.debuggingWebType;
			}
			try {
				historyCon = LexicalDBHelper.connectInstance(this);
			} catch (SQLiteFullException e) {
				BlockingDlgs.blameNoSpace(this, savedInstanceState);
				return;
			}
			if(webType==WEBTYPE_INTEL && BlockingDlgs.initXWalk(this, savedInstanceState)) {
				return;
			}
		}
		if(true && Utils.bigCake) { // disableWebViewFastRegionRendereing
			//WebView.enableSlowWholeDocumentDraw();
		}
		try {
			wvPreInitInstance = initWebViewImpl(null, null, false);
			//CMN.Log(wvPreInitInstance);
		} catch (Exception e) {
			CMN.Log(e);
			BlockingDlgs.blameNoWebView(this, savedInstanceState);
			return;
		}
		CheckGlideJournal();
		checkMargin(this);
		_45_ = (int) mResource.getDimension(R.dimen._45_);
		WebViewHelper.minW = Math.min(512, Math.min(dm.widthPixels, dm.heightPixels));
		CMN.mTextPainter = DescriptiveImageView.createTextPainter();
		UIData.browserWidget10.textPainter = CMN.mTextPainter;

		tabViewAdapter = new TabViewAdapter(this);
		
		if(allTabs==null) {
			Cursor tabsCursor = historyCon.queryTabs();
			allTabs = new HashMap<>(tabsCursor.getCount(), 1);
			while (tabsCursor.moveToNext()) {
				TabHolder holder = new TabHolder();
				holder.id = tabsCursor.getLong(0);
				holder.title = tabsCursor.getString(1);
				holder.url = tabsCursor.getString(2);
				holder.page_search_term = tabsCursor.getString(3);
				holder.flag = tabsCursor.getLong(4);
				holder.rank = tabsCursor.getLong(5);
				allTabs.put(holder.id, holder);
			}
			tabsCursor.close();
			tabsRead = true;
		}
		
		lastOpenedTab = opt.getLastOpenedTabID();
		String tabs = opt.getOpenedTabs();
		adapter_idx = 0;
		CMN.Log("tabs::", tabs);
		if(TextUtils.isEmpty(tabs)) {
			TabHolders.addAll(allTabs.values());
			tabsDirty = true;
		} else {
			String[] tabsArr = tabs.split(" ");
			for(String tabsI:tabsArr) {
				try {
					long id = Long.parseLong(tabsI);
					if(id==lastOpenedTab) {
						adapter_idx=TabHolders.size();
					}
					TabHolder tabI = allTabs.get(id);
					if(tabI!=null) {
						tabI.setClosed(false);
						TabHolders.add(tabI);
					}
				} catch (Exception ignored) { }
			}
		}
		// todo build closed tabs' stack
		//closedTabs = new ArrayList<>(Arrays.asList(allTabs.values().toArray(new TabHolder[]{})));
		
		checkCurrentTab(true, adapter_idx);
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
				execBrowserGoTo(null, true);
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
				//CMN.Log("afterTextChanged");
				if(webtitle.getVisibility()!=View.VISIBLE) {
					goToBarcodeScanner=s.length()==0;
					updateQRBtn();
				}
				if(search_bar_vis()) {
					if (s.length()==0) {
						if (opt.getShowSearchHintsOnClear()) {
							if(ActiveSearchCursors.size()>=2 && ActiveSearchCursors.get(1)!=EmptyCursor) {
								UIData.showSearchHistoryDropdown.setVisibility(View.GONE);
								pull_hints(false);
							}
						} else {
							showSearchHistoryDropdown();
						}
					} else {
						pull_hints(true);
					}
				}
			}
		});
		//tg
		
		//TestHelper.injectTestListener(this);
		
		opt.setUpdateUAOnPageSt(Build.VERSION.SDK_INT>=28);
		//opt.setUpdateUAOnPageSt(false);
		//showT("低端设备无法积极地更新UA::"+opt.getUpdateUALowEnd());
		
		//TestHelper.simplePagingTest(this);
		
		
//		Intent intent = new Intent(Intent.ACTION_MAIN)
//				.setClassName("com.knziha.polymer", "com.knziha.polymer.PDocShortCutActivity")
//				.putExtra("main", true)
//				.setData(Uri.fromFile(new File("123345")));
//
//		ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(this, "knziha.pdf")
//				.setIcon(IconCompat.createWithResource(this, R.mipmap.ic_pdoc_viewer))
//				.setShortLabel(mResource.getString(R.string.pdf_viewer))
//				.setIntent(intent)
//				.build();
		
		//ShortcutManagerCompat.addDynamicShortcuts(this, Collections.singletonList(shortcutInfo));
		
		

		//TestHelper.notifyStart(this);
		root.postDelayed(new Runnable() {
			@Override
			public void run() {
//				toggleMenuGrid(false);

//				menu_grid.findViewById(R.id.menu_icon7).performClick();
//				UIData.browserWidget10.performClick();
//				root.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						//UIData.ivRefresh.performClick();
//					}
//				}, 350);
			}
		}, 250);
		
		if (opt.getAdjustSystemVolume()) {
			Utils.setSystemEqualizer(opt, false);
		}
		
		// 性能测试 - 多网页同时加载
//		new Runnable() {
//			@Override
//			public void run() {
//				AttachWebAt(adapter_idx+1, 0);
//				root.postDelayed(this, 900);
//			}
//		}.run();
		
		//wakeUp();
		//CMN.Log("device space is ::",  getExternalFilesDir(null).getFreeSpace());
		
		//showDownloads();
		
		//TestHelper.testWebArchive().start();
		

//		DialogProperties properties = new DialogProperties();
//		properties.selection_mode = DialogConfigs.SINGLE_MODE;
//		properties.selection_type = DialogConfigs.FILE_SELECT;
//		properties.root = new File("/");
//		properties.error_dir = Environment.getExternalStorageDirectory();
//		properties.offset = new File("/data/data/com.knziha.polymer/");
//		properties.opt_dir=new File(getExternalFilesDir(null), "favorite_dirs");
//		properties.opt_dir.mkdirs();
//		properties.extensions = new HashSet<>();
//		properties.extensions.add("*");
//		properties.extensions.add(".pdf");
//		properties.title_id = R.string.pdf_internal_open;
//		properties.isDark = AppWhite==Color.BLACK;
//		FilePickerDialog dialog = new FilePickerDialog(this, properties);
//		dialog.show();
//		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		
		
		//CMN.Log("webview package is ::", AdvancedBrowserWebView.getCurrentWebViewPackage().packageName+":"+AdvancedBrowserWebView.getCurrentWebViewPackage().versionName);
		
//		TestHelper.createWVBSC(this, 0);
//		TestHelper.createWVBSC(this, 1);
//		TestHelper.createWVBSC(this, 2);
		
		//TestHelper.createXWalkSC(this);
		
		//TestHelper.createWVBSC(this, true);
		//AdvancedBrowserWebView.enableSlowWholeDocumentDraw();
		//TestHelper.async(TestHelper::downloadlink);
		//TestHelper.savePngBitmap(this, R.drawable.polymer, 150, 150, "/sdcard/150.png");

		//		StandardConfigDialog holder = buildStandardConfigDialog(BrowserActivity.this, null, R.string.global_vs_tabs);
//		holder.init_web_configs(true, 1);
//		holder.dlg.show();
		
		//TestHelper.insertMegaUrlDataToHistory(historyCon, 2500);
		
		//TestHelper.insertMegaAnnotsTextToHistory(historyCon, 1000);
		
		
		root.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
			int lastW, lastH;
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				if(softMode!=softModeHold) {
					boolean mKeyboardUp = isKeyboardShown(root);
					if (bottombarHidden != mKeyboardUp) {
						View bottombar2 = UIData.bottombar2;
						if (mKeyboardUp) {
							showT("键盘弹出...");
							bottombarHidden = true;
							bottombar2.setVisibility(View.INVISIBLE);
						} else {
							showT("键盘收起...");
							bottombarHidden = false;
							bottombar2.setVisibility(View.VISIBLE);
						}
						decideWebviewPadding(currentViewImpl); // 窗口大小变化
					}
				}
			}
		});
		
		systemIntialized=true;
	}
	
	public void checkCurrentTab(boolean init, int targetPos) {
		/* !!!原装 */
		if(TabHolders.size()==0) {
			String defaultUrl = getDefaultPage();
			TabHolder tab0=new TabHolder();
			long id = historyCon.insertNewTab(defaultUrl, CMN.now());
			CMN.Log("insertNewTab", id);
			if(id!=-1) {
				tab0.url=defaultUrl;
				tab0.id=id;
				tab0.setClosed(false);
				allTabs.put(tab0.id, tab0);
				TabHolders.add(tab0);
			}
			tabsDirty=true;
		}
		CMN.Log("ini.初始化完毕", TabHolders.size());
		if(currentViewImpl==null || targetPos!=adapter_idx) {
			int idx=targetPos;
			if(tabViewAdapter.layoutManager!=null) {
				// todo check
				if (targetPos==-1) targetPos = ViewUtils.getCenterXChildPositionV2(tabViewAdapter.recyclerView) - 1;
				idx = tabViewAdapter.layoutManager.targetPos = targetPos;
				idx = Math.max(0, Math.min(idx, TabHolders.size()-1));
			}
			AttachWebAt(idx, init?0:3);
		}
		checkImmersiveMode();
		decideWebviewPadding(currentViewImpl);
	}
	
	private void updateQRBtn() {
		//CMN.Log("updateQRBtn", goToBarcodeScanner);
		TextView QRBtn = UIData.browserWidget5;
		if(QRBtn.getTag()!=null^goToBarcodeScanner) {
			QRBtn.setText(goToBarcodeScanner?"扫码":"前往");
			
			QRBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
					mResource.getDrawable(goToBarcodeScanner?R.drawable.ic_baseline_qrcode:R.drawable.abc_ic_go_search_api_material)
					, null, null, null);
			QRBtn.setTag(goToBarcodeScanner? DummyTransX:null);
		}
	}
	
	public String execBrowserGoTo(String text, boolean exec) {
		tabViewAdapter.dismiss();
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
					}/* else if(schemaIdx==6) {
						isUrl=text.regionMatches(true, 0, "polyme", 0, 6);
					}*/
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
			currentViewImpl.searchTerm = text;
			historyCon.insertSearchTerm(text);
			text = currentWebDictUrl.replace("%s", text);
		} else {
			//currentViewImpl.searchTerm = null;
			LinkedList<RgxPlc> queryDNS = mWebListener.DNSIntelligence;
			if(queryDNS.size()>0) {
				for(RgxPlc dI:queryDNS) {
					Matcher m = dI.p.matcher(text);
					if(m.matches()) {
						text = m.replaceAll(dI.rep);
					}
				}
			}
		}
		CMN.Log("isUrl", isUrl, text, currentWebView.getOriginalUrl());
		if (exec) {
			if(currentViewImpl.holder.getLuxury() && !currentViewImpl.equalsUrl(text)) {
				//LuxuriouslyLoadUrl(currentViewImpl.mWebView, text);
			} else {
				loadUrl(currentViewImpl, text);
			}
			webtitle_setVisibility(false);
			etSearch_clearFocus();
			return null;
		}
		return text;
	}
	
	private void loadUrl(WebFrameLayout layout, String url) {
		if (layout.queryDomain(url, true)) layout.bRecentNewDomainLnk++;
		layout.new_page_loaded = false;
		layout.loadUrl(url);
	}
	
	private Runnable changeNavBottomBarIconsRunnable = () -> {
			UIData.browserWidget7.setImageResource(R.drawable.chevron_recess_ic_back);
			UIData.browserWidget8.setImageResource(R.drawable.chevron_forward_settings);
			UIData.browserWidget9.setImageResource(R.drawable.ic_home_baseline_keyboard_show_24);
	};
	
	public void webtitle_setVisibility(boolean invisible) {
		int targetVis = invisible ? View.INVISIBLE : View.VISIBLE;
		//CMN.Log("webtitle_setVisibility", invisible);
		if(targetVis!=webtitle.getVisibility()) {
			IsShowingSearchHistory = invisible;
			boolean keepSchTrm = false;
			if(invisible) {
				String urlNow=currentWebView.getUrl();
				if (currentViewImpl.searchTerm!=null) {
					if (currentViewImpl.new_page_loaded) {
						keepSchTrm = currentViewImpl.getTitle().startsWith(currentViewImpl.searchTerm);
					} else {
						keepSchTrm = true;
					}
					if (keepSchTrm) {
						if(!etSearch.getText().equals(currentViewImpl.searchTerm))
							etSearch.setText(currentViewImpl.searchTerm);
						goToBarcodeScanner = true;
					}
				}
				if(!keepSchTrm && !StringUtils.equals(urlNow, lastUrlSet)) {
					CMN.Log("设置了 设置了");
					etSearch.setText(lastUrlSet=currentWebView.getUrl());
				} else if(!keepSchTrm) {
					Editable oldText = etSearch.getText();
					keepSchTrm = !TextUtils.equals(oldText, urlNow) && (oldText.length()<4||!TextUtils.regionMatches(oldText, 0, "http", 0, 4));
				}
			}
			if (settingsPanel!=null) {
				UIData.browserWidget8.performClick();
			}
			webtitle.setVisibility(targetVis);
			if(getTransitSearchHints()) {
				UIData.searchHints.setVisibility(View.INVISIBLE);
			}
			targetVis = invisible?View.VISIBLE:View.INVISIBLE;
			etSearch.setVisibility(targetVis);
			UIData.searchbar.setVisibility(targetVis);
			//todo search history fragment
			CMN.Log("keepSchTrm="+keepSchTrm, currentViewImpl.getTitle(), currentViewImpl.searchTerm);
			if(invisible) {
				keyboard_hidden=false;
				init_searint_layout();
				if (opt.getShowSearchHints()) {
					UIData.showSearchHistoryDropdown.setVisibility(View.GONE);
					pull_hints(keepSchTrm);
				} else {
					showSearchHistoryDropdown();
				}
				if(opt.getShowImeImm()) {
					root.postDelayed(changeNavBottomBarIconsRunnable, 350);
				} else {
					changeNavBottomBarIconsRunnable.run();
				}
			} else {
				UIData.browserWidget7.setImageResource(R.drawable.chevron_recess);
				UIData.browserWidget8.setImageResource(R.drawable.chevron_forward);
				UIData.browserWidget9.setImageResource(R.drawable.ic_home_black_24dp);
			}
			shrinkToolbarWidgets(invisible);
			if (opt.getTransitListBG()) {
				UIData.showSearchHistoryDropdownBg
						.animate()
						.alpha(invisible?1:0)
						.setDuration(180)
						//.start()
				;
			}
		}
	}
	
	private void showSearchHistoryDropdown() {
		UIData.showSearchHistoryDropdown.setVisibility(View.VISIBLE);
		if (ActiveSearchCount>0) {
			ActiveSearchCount=0;
			adaptermy2.notifyDataSetChanged();
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
	
	public DownloadHandlerStd getDownloader() {
		if(downloadDlgHandler==null) {
			downloadDlgHandler = new DownloadUIHandler(this, historyCon);
		}
		return downloadDlgHandler.getDownloader();
	}
	
	public void updateProgressUI() {
		WebFrameLayout layout = currentViewImpl;
		if (layout!=null) {
			View starting_progressbar = UIData.progressbar;
			if(layout.PageStarted) {
				//if(mWebView.getProgress()<100) {
				starting_progressbar.setAlpha(1);
				starting_progressbar.setVisibility(View.VISIBLE);
				//progressbar_background.setLevel(Math.min(progressbar_background.getLevel(), 2500));
				progressbar_background.setLevel(layout.mWebView.getProgress()*100);
			} else {
				starting_progressbar.setVisibility(View.GONE);
			}
			if (TestHelper.showSearchTabs || !tabViewAdapter.isVisible()) {
				UIData.ivRefresh.setImageResource(layout.PageStarted?R.drawable.ic_close_white_24dp:R.drawable.ic_refresh_white_24dp);
			}
		}
	}
	
	// lectus synca
	Runnable selectSyncTabsRunnable = () -> {
		AlertDialog alert = buildSyncInterface();
		View[] items = (View[]) alert.tag;
		items[0].setEnabled(false);
		items[4].setVisibility(View.VISIBLE);
		((TextView)items[5]).setText("选择同步目标");
		ListView lv;
		boolean newbie=items[1]==null;
		if(newbie) {
			items[1] = lv = new ListView(alert.getContext());
			lv.setTag(syncHandler.mergedTabs);
			View.OnClickListener clicker = new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					int id = v.getId();
					if(id==R.id.type) {
						showT("TYPE");
					} else {
						ViewGroup vg;
						if(id==R.id.check1) {
							vg = (ViewGroup) v.getParent();
						} else {
							vg = (ViewGroup) v;
						}
						int position = (int) vg.getTag();
						SardineCloud.TabBean item = syncHandler.mergedTabs.get(position);
						item.selected=!item.selected;
						vg.getChildAt(0).setAlpha(item.selected?1:0.06f);
					}
				}
			};
			lv.setAdapter(new BaseAdapter() {
				@Override
				public int getCount() {
					return ((ArrayList<SardineCloud.TabBean>)lv.getTag()).size();
				}
				@Override
				public Object getItem(int position) {
					return null;
				}
				@Override
				public long getItemId(int position) {
					return position;
				}
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					if(convertView==null) {
						convertView = LayoutInflater.from(parent.getContext())
								.inflate(R.layout.sync_tabs_item, parent, false);
						setOnClickListenersOneDepth((ViewGroup) convertView, clicker, 1, null);
					}
					ViewGroup vh = (ViewGroup) convertView;
					vh.setTag(position);
					SardineCloud.TabBean item = ((ArrayList<SardineCloud.TabBean>)lv.getTag()).get(position);
					((TextView)vh.getChildAt(1)).setText(item.toString());
					int type = item.type;
					if(type==1) {
						type = R.drawable.sync_tabs_add;
					} else if(type==-1){
						type = R.drawable.sync_tabs_close;
					} else if(type==2){
						type = R.drawable.sync_tabs_changed;
					} else if(type==3){
						type = R.drawable.sync_tabs_lag;
					} else {
						type = 0;
					}
					((ImageView) vh.getChildAt(2)).setImageResource(type);
					vh.getChildAt(0).setAlpha(item.selected?1:0.06f);
					return convertView;
				}
			});
			((ViewGroup) items[2]).addView(lv);
			//lv.setPadding(0, 0, 0, (int) (GlobalOptions.density*10));
			//lv.setBackgroundColor(0xffffffff);
		} else {
			lv = (ListView) items[1];
			newbie=lv.getVisibility()!=View.VISIBLE;
		}
		if(newbie) {
			items[3].setVisibility(View.VISIBLE);
			lv.setVisibility(View.VISIBLE);
			lv.setTag(syncHandler.mergedTabs);
		}
		((BaseAdapter)lv.getAdapter()).notifyDataSetChanged();
	};
	
	private AlertDialog buildSyncInterface() {
		AlertDialog alert = (AlertDialog) getReferencedObject(WeakReferenceHelper.sync_interface);
		if(alert==null) {
			alert = new AlertDialog.Builder(this)
					.setView(R.layout.sync_interface)
					.create();
			alert.show();
			ViewGroup root = alert.findViewById(R.id.root);
			View[] items = new View[]{root.findViewById(R.id.wave), null, root, root.getChildAt(1), root.findViewById(R.id.start), root.findViewById(R.id.title)};
			
			View.OnClickListener clicker = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v==items[0]) {
					
					} else {
						if(syncHandler!=null) {
							if(((Integer)0).equals(v.getTag())) {
								initWaveProgressView(items);
								syncHandler.scheduleTask(BrowserActivity.this, SardineCloud.TaskType.uploadTabs);
							} else {
								initWaveProgressView(items);
								syncHandler.scheduleTask(BrowserActivity.this, SardineCloud.TaskType.syncTabs);
							}
						}
					}
				}
			};
			items[0].setOnClickListener(clicker);
			items[4].setOnClickListener(clicker);
			alert.tag = items;
			putReferencedObject(WeakReferenceHelper.sync_interface, alert);
		} else {
			alert.setCancelable(true);
			alert.setCanceledOnTouchOutside(true);
			alert.show();
		}
		return alert;
	}
	
	public void postSelectSyncTabs() {
		root.post(selectSyncTabsRunnable);
	}
	
	Runnable doneSyncTabsRunnable = () -> {
		AlertDialog alert = (AlertDialog) getReferencedObject(WeakReferenceHelper.sync_interface);
		if(alert!=null) {
			alert.dismiss();
		}
		showT("同步成功！");
		this.waveView=null;
		if(tabViewAdapter!=null && tabsManagerIsDirty) {
			tabViewAdapter.notifyDataSetChanged();
			tabsManagerIsDirty=false;
			currentViewImpl = null;
			currentWebView = null;
			// attach new tabs
			//if(currentViewImpl.destoryed())
		}
	};
	
	public void postDoneSyncing(SardineCloud.TaskType type) {
		root.removeCallbacks(doneSyncTabsRunnable);
		root.post(doneSyncTabsRunnable);
		if(type==SardineCloud.TaskType.syncTabs) {
			tabsManagerIsDirty = true;
			tabsDirty = true;
		}
	}
	
	public WebFrameLayout getWebViewFromID(long tabID) {
		return id_table.get(tabID);
	}
	
	public int setWebType(int type) {
		//if(!systemIntialized)
		{
			webType=WebTypes.values()[type];
		}
		return type;
	}
	
	public int getWebType() {
		return webType.ordinal();
	}
	
	public PopupMenuHelper getPopupMenu() {
		PopupMenuHelper ret = (PopupMenuHelper) getReferencedObject(WeakReferenceHelper.popup_menu);
		if (ret==null) {
			//CMN.Log("新建popup...");
			ret  = new PopupMenuHelper(this, null, null);
			putReferencedObject(WeakReferenceHelper.popup_menu, ret);
		}
		return ret;
	}
	
	public void fast_recalc_adapter_idx(int refPos) {
		TabHolder tab = currentWebHolder;
		int idx = -1;
		ArrayList<TabHolder> holders = TabHolders;
		if (refPos>=0 && refPos< holders.size()) {
			if (holders.get(refPos)==tab) idx = refPos;
			else if(refPos>0 && holders.get(refPos-1)==tab) idx = refPos-1;
			else if(refPos<holders.size()-1 && holders.get(refPos+1)==tab) idx = refPos+1;
		}
		if (idx==-1) {
			//CMN.Log("fast_recalc_adapter_idx::慢查", refPos, adapter_idx);
			idx = holders.indexOf(tab);
		} else {
			//CMN.Log("fast_recalc_adapter_idx::快找", refPos, adapter_idx, idx);
		}
		adapter_idx = idx;
	}
	
	final int[] ScreenOrientation = new int[]{
			ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
			,ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
			,ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
			,ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
			,ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
			,ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
			,ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
			,ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
			,ActivityInfo.SCREEN_ORIENTATION_LOCKED
	};
	
	public void setScreenOrientation(int idx) {
		//CMN.Log("setScreenOrientation::", ScreenOrientation[idx]);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		setRequestedOrientation(ScreenOrientation[idx]);
	}
	
	public void reload() {
		if (settingsPanel!=null) hideSettingsPanel();
		if (currentWebView!=null) currentWebView.reload();
	}
	
	public void hideSettingsPanel() {
		if (settingsPanel!=null) {
			settingsPanel.dismiss();
			settingsPanel = null;
		}
		if(settingsPopup!=null) settingsPopup = null;
	}
	
	public void embedPopInCoordinatorLayout(PopupWindow pop, boolean below) {
		settingsPopup = pop;
		settingsBelowAppbar = below;
		pop.setWidth(dm.widthPixels);
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		pop.setBackgroundDrawable(null);
		int[] vLocation = new int[2];
		UIData.webcoord.getLocationInWindow(vLocation);
		int topY = vLocation[1];
		int h = UIData.webcoord.getHeight() - UIData.bottombar2.getHeight();
		if (below && UIData.appbar.getTop()==0) {
			int h1 = UIData.appbar.getHeight();
			topY += h1;
			h -= h1;
		}
		pop.setWidth(-1);
		pop.setHeight(h);
		if (pop.isShowing()) {
			pop.update(0, topY, -1, h);
		} else {
			pop.showAtLocation(root, Gravity.TOP, 0, topY);
		}
	}
	
	/** 防止短时间内自动打开过多标签页。 | Limit auto-new-tab frequency to 1/350ms */
	public boolean checkAutoNewTabAllowed() {
		if (LastAutoNewTabTime>0) {
			if (CMN.now()-LastAutoNewTabTime<350) {
				return false;
			} else {
				LastAutoNewTabTime=0;
			}
		}
		return true;
	}
	
	public class BrowserSlider implements View.OnTouchListener {
		private boolean dragging;
		private boolean sliding;
		private boolean selecting;
		private float orgX;
		private float orgY;
		private float lastX;
		private float lastY;
		private View webla;
		private int adapter_to;
		ObjectAnimator resitu = ObjectAnimator.ofFloat(currentViewImpl, "translationX", 0, 0);
		ObjectAnimator resitu1 = ObjectAnimator.ofFloat(currentViewImpl, "translationX", 0, 0);
		ObjectAnimator resitu2 = ObjectAnimator.ofFloat(DummyTransX, "translationX", 0, 0);
		AnimatorSet animator;
		boolean forceNxtEukatch=false;
		Animator.AnimatorListener animatorLis=new Animator.AnimatorListener() {
			@Override public void onAnimationStart(Animator animation) {
				int check = adapter_idx+adapter_to;
				if(check<0||check>TabHolders.size()) {
					TabHolder wv = TabHolders.get(check); //gettt
					webtitle.setText(wv.title);
				}
				isPaused=false;
			}
			@Override public void onAnimationEnd(Animator animation) {
				webla=null;
				isPaused=true;
				AttachWebAt(adapter_idx+adapter_to, (!forceNxtEukatch&&adapter_to==0&&animated_delta<dm.density*90)?1:0);
			}
			@Override
			public void onAnimationCancel(Animator animation) {}
			@Override
			public void onAnimationRepeat(Animator animation) {}
		};
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
			animator.addListener(animatorLis);
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
			if(currentViewImpl==null) {
				return false;
			}
			switch (evt) {
				case MotionEvent.ACTION_DOWN:
					//CMN.Log("ACTION_DOWN");
					//toolbar.suppressLayout(true);
					if(animator.isRunning()) {
						forceNxtEukatch=true;
						animator.removeListener(animatorLis);
						animator.cancel();
						animator.addListener(animatorLis);
						CMN.Log("animator.isRunning()", adapter_to, webla);
						float finilen;
						if(adapter_to!=0 && webla!=null
								/*&& ((finilen=webla.getTranslationX() + webla.getWidth() / 2)>=0
								&&finilen<=root.getWidth())*/) {
							AttachWebAt(adapter_idx+adapter_to, 2);
							adapter_to=0;
						}
					} else {
						forceNxtEukatch=false;
					}
					animated_delta=0;
					etSearch_scrolled=false;
					should_check_supressed=
					supressed=false;
					lastX = orgX = event.getX();
					lastY = orgX = event.getX();
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
					webla = null;
					onFingDir = 0;
				break;
				case MotionEvent.ACTION_MOVE: {
					if(settingsPanel!=null) {
						return false;
					}
					if(should_check_supressed&&!supressed) {
						supressed = etSearch.hasSelection();
					}
					if(supressed) {
						if(v==searchbartitle) {
							float lastX = event.getX();
							lastY = event.getY();
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
					lastY = event.getY();
					float delta = Math.abs(lastX-orgX);
					if(!tabViewAdapter.isVisible()) {
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
							//animator.cancel();
							//CMN.Log("draggging", getCurrentFocus());
							View layout = currentViewImpl;
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
							if(attachIdx>=0&&attachIdx<TabHolders.size()) {
								AttachWebAt(attachIdx, 1);
								WebFrameLayout layoutAttach = TabHolders.get(attachIdx).viewImpl;
								View attachLayout = layoutAttach.implView;
								if(attachLayout!=null) {
									webla = layoutAttach;
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
							tabViewAdapter.recyclerView.dispatchTouchEvent(event);
							event.setAction(MotionEvent.ACTION_MOVE);
						}
						if(sliding) {
							//CMN.Log("simulated_move....");
							event.setLocation(lastX*simovefactor, 0);
							tabViewAdapter.recyclerView.dispatchTouchEvent(event);
						}
					}
					this.lastX=lastX;
				} break;
				case MotionEvent.ACTION_UP:
					if(sliding) {
						sliding=false;
						event.setLocation(lastX*simovefactor, 0);
						tabViewAdapter.recyclerView.dispatchTouchEvent(event);
					} else {
						View layout = currentViewImpl;
						float tx = layout.getTranslationX();
						if(dragging||tx!=0&&layout.getScaleX()==1) {
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
							if(check<0||check>TabHolders.size()) {
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
								resitu1.setTarget(DummyTransX);
							}
							if(webtitle.getVisibility()!=View.VISIBLE) {
								resitu2.setTarget(UIData.searchbar);
								resitu2.setFloatValues(UIData.searchbar.getTranslationX(), -adapter_to*W);
							} else {
								resitu2.setTarget(DummyTransX);
							}
							if(adapter_to!=0) {
								onLeaveCurrentTab(3);
							}
							animator.start();
						}
						if(v==UIData.toolbarContent&&search_bar_vis()) {
							//float delta = Math.abs(event.getX()-orgX);
							float delta = Math.abs(currentViewImpl.getTranslationX());
							if(delta>5*GlobalOptions.density) {
								hideKeyboard();
								//if(delta>30*GlobalOptions.density)
								{
									webtitle_setVisibility(false);
									etSearch_clearFocus();
								}
							}
						}
						dragging=false;
					}
					//webla=null;
					selecting = false;
				break;
			}
			return dragging;
		}
		
		private void preventDefault(View v, MotionEvent event) {
			v.setTag(MotionEvent.ACTION_UP);
			event.setAction(MotionEvent.ACTION_UP);
			v.dispatchTouchEvent(event);
		}
		
		public void pause() {
			if(!isPaused) {
				//CMN.Log("rectify....");
				isPaused=true;
				animator.pause();
				webla = null;
				AttachWebAt(adapter_idx, 0);
			}
		}
		
		public boolean moved() {
			return lastX-orgX>100||onFingDir!=0;
		}
		
		public boolean active() {
			return dragging||sliding||selecting||!mBrowserSlider.isPaused;
		}
	}
	
	/**  0=show combined 1=show sch history 2=search current term. */
	private void pull_hints(boolean searchTables) {
		UIData.searchHints.scrollToPosition(0);
		if(pull_hints_runnable!=null) {
			pull_hints_runnable.interrupt();
		}
		pull_hints_runnable = new PullHintsRunnable(searchTables);
		pull_hints_runnable.start();
	}
	
	class PullHintsRunnable implements Runnable {
		/** 0=show combined 1=show sch history 2=search current */
		private final boolean doSearch;
		CancellationSignal stopSignal = new CancellationSignal();
		CancellationSignal searchSignal;
		Thread t = new Thread(this);
		Handler handler;
		private String text;
		private ArrayList<Cursor> searchCursors=new ArrayList<>();
		public PullHintsRunnable(boolean doSearch) {
			this.doSearch = doSearch;
		}
		
		public void interrupt() {
			t.interrupt();
			stopSignal.cancel();
		}
		public void start() {
			text = doSearch?etSearch.getText().toString():null;
			t.start();
		}
		@AnyThread
		@Override
		public void run() {
			CMN.Log("pull_hints", ActiveSearchCount);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				return;
			}
			if(stopSignal.isCanceled()) {
				return;
			}
//			if (handler==null) {
//				handler = new Handler();
//			}
			int SearchLimitation = opt.getHintsLimitation();
			boolean LimitQueryResults = getLimitHints();
			if(!LimitQueryResults) {
				SearchLimitation=Integer.MAX_VALUE;
			}
			int searchCount=0;
			Cursor searhCursor = null;
			
			searchCursors.add(null);
			
			// 网址记录
			try {
				CMN.rt();
				searhCursor = historyCon.queryUrl(text, SearchLimitation, stopSignal);
				searchCount += searhCursor.getCount();
				CMN.pt(searchCount+"个历史查询时间:");
			} catch (Exception e) {
				CMN.Log(e);
				try {
					if(searhCursor!=null) searhCursor.close();
				} catch (Exception ignored) { }
				searhCursor = EmptyCursor;
				return;
			}
			searchCursors.add(searhCursor);
			
			searchCount=0;
			searhCursor = null;
			
			// 搜索记录
			if(LimitQueryResults) SearchLimitation -= searchCount;
			try {
				searhCursor = historyCon.querySearchTerms(text, doSearch?5:128, stopSignal);
				searchCount += searhCursor.getCount();
			} catch (Exception e) {
				CMN.Log(e);
				try {
					if(searhCursor!=null) searhCursor.close();
				} catch (Exception ignored) { }
				searhCursor = EmptyCursor;
				return;
			}
			searchCursors.set(0, searhCursor);
			
			ActiveSearchCursors = searchCursors;
			historyCon.updateLastSearchTerm(text);
			if(doSearch||searchCount<3) { //todo 000
				root.post(hintsUpdatedRunnable);
			} else {
				root.post(hintsPulledRunnable);
			}
		}
	};
	
	Runnable hintsPulledRunnable = new Runnable() {
		@Override
		public void run() {
			hintsUpdatedRunnable.run();
			RecyclerView animating_search_hints=UIData.searchHints;
			//if(animating_search_hints.getTag()==null)
			{
				LayoutAnimationController layoutAnimation = AnimationUtils
						.loadLayoutAnimation(BrowserActivity.this, R.anim.layout_animation_fall_down);
				animating_search_hints.setLayoutAnimation(layoutAnimation);
				animating_search_hints.setTag(0);
			}
			animating_search_hints.scheduleLayoutAnimation();
			if(getTransitSearchHints()&&search_hints_vising&& ActiveSearchCount >=3) {
				animating_search_hints.postDelayed(BrowserActivity.this::ShowUrlKeySearchResults, 450);
				search_hints_vising=false;
			} else {
				ShowUrlKeySearchResults();
			}
			//((LinearLayoutManager)UIData.searchHints.getLayoutManager()).scrollToPositionWithOffset(2,0);
			
		}
	};
	
	Runnable hintsUpdatedRunnable = new Runnable() {
		@Override
		public void run() {
			int searchCount=0;
			try {
				for(Cursor iter:ActiveSearchCursors) {
					searchCount+=iter.getCount();
				}
			} catch (Exception ignored) { }
			ActiveSearchCount = searchCount;
			if(adaptermy2!=null) {
				adaptermy2.notifyDataSetChanged();
			}
			//UIData.searchHints.getLayoutManager().scrollToPosition(2);
			//((LinearLayoutManager)UIData.searchHints.getLayoutManager()).scrollToPositionWithOffset(2,-10);
			ShowUrlKeySearchResults();
			historyCon.closeCursors();
		}
	};
	
	private void ShowUrlKeySearchResults() {
		UIData.searchHints.setVisibility(View.VISIBLE);
		if (!opt.getShowIdleSearchHints())
		{
			UIData.showSearchHistoryDropdown.setVisibility(View.GONE);
		}
	}
	
	private boolean search_bar_vis() {
		return UIData.searchbar.getVisibility()==View.VISIBLE;
	}
	
	private void init_searint_layout() {
		LinearLayout init_searchbar = UIData.searchbar;
		if(adaptermy2==null) {
			View.OnClickListener searchHistoryEventListener = new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					ViewDataHolder viewHolder = (ViewDataHolder) v.getTag();
					SearchHintsItemBinding vh = (SearchHintsItemBinding) viewHolder.data;
					//showT(""+viewHolder.getLayoutPosition());
					String text = (vh.subtitle.getVisibility()==View.VISIBLE
							?vh.subtitle// 点击历史url
							:vh.title // 点击搜索词
							).getText().toString();
					if(v.getId()==R.id.close) {
						etSearch.setText(text);
					} else {
						execBrowserGoTo(text, true);
					}
				}
			};
			adaptermy2 = new RecyclerView.Adapter<ViewDataHolder<SearchHintsItemBinding>>() {
				public int getItemCount() { return ActiveSearchCount +2; }
				@NonNull
				@Override
				public ViewDataHolder<SearchHintsItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
					SearchHintsItemBinding vh = SearchHintsItemBinding.inflate(getLayoutInflater(), parent, false);
					//CMN.Log("onCreateViewHolder", viewType, CMN.now());
					ViewDataHolder<SearchHintsItemBinding> ret = new ViewDataHolder<>(vh);
					vh.item.setOnClickListener(searchHistoryEventListener);
					vh.close.setOnClickListener(searchHistoryEventListener);
					vh.close.setTag(ret);
					return ret;
				}
				
				@Override
				public void onBindViewHolder(@NonNull ViewDataHolder<SearchHintsItemBinding> viewHolder, int position) {
					SearchHintsItemBinding vh = viewHolder.data;
					if(ActiveSearchCount ==0) {
						viewHolder.itemView.setVisibility(View.GONE);
						return;
					}
					viewHolder.itemView.setVisibility(View.VISIBLE);
					if(false) {
						vh.subtitle.setVisibility(View.GONE);
						return;
					}
					try {
						int terms_count = 0;
						if (position<ActiveSearchCount) {
							for (int i = 0, len=ActiveSearchCursors.size(); i < len; i++) {
								Cursor activeCursor = ActiveSearchCursors.get(i);
								int st=terms_count;
								terms_count += activeCursor.getCount();
								if (position<terms_count) {
									activeCursor.moveToPosition(position-st);
									if (i==0) {
										vh.title.setText(activeCursor.getString(0));
										vh.subtitle.setVisibility(View.GONE);
									} else {
										vh.title.setText(activeCursor.getString(1));
										vh.subtitle.setText(activeCursor.getString(0));
										vh.subtitle.setVisibility(View.VISIBLE);
									}
									break;
								}
							}
							viewHolder.itemView.setVisibility(View.VISIBLE);
						} else {
							viewHolder.itemView.setVisibility(View.GONE);
						}
					} catch (Exception e) {
						CMN.Log(e);
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
			// https://stackoverflow.com/questions/56379574/how-do-i-detect-overscroll-in-android-recyclerview
			LinearLayoutManager layoutManager = new LinearLayoutManager(this){
				@Override
				public int scrollVerticallyBy ( int dx, RecyclerView.Recycler recycler, RecyclerView.State state ) {
					int scrollRange = super.scrollVerticallyBy(dx, recycler, state);
					if(!keyboard_hidden && dx!=scrollRange && opt.getHideKeyboardOnScrollSearchHints()) {
						imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
						keyboard_hidden=true;
					}
					return scrollRange;
				}
			};
			layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
			layoutManager.setInitialPrefetchItemCount(8);
			init_search_hints.setLayoutManager(layoutManager);
			init_search_hints.setRecycledViewPool(Utils.MaxRecyclerPool(GlobalOptions.isLarge?35:25));
			
			init_search_hints.setAdapter(adaptermy2);
			
			init_search_hints.setNestedScrollingEnabled(false);
			
			init_search_hints.setOnScrollChangedListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
				if(!keyboard_hidden
					&& init_search_hints.getScrollState()==RecyclerView.SCROLL_STATE_DRAGGING
					&& opt.getHideKeyboardOnScrollSearchHints()
				) {
					imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
					keyboard_hidden=true;
				}
			});
			
			etSearch.setOnClickListener(this);
			
			if (opt.getTransitListBG()) {
				UIData.showSearchHistoryDropdownBg.setAlpha(0);
			}
		}
		ActiveSearchCount = 0;
		init_searchbar.setTranslationX(0);
	}
	
	public void closeTabAt(int positon, int source) {
		if(positon>=0 && positon<TabHolders.size()) {
			TabHolder tab = TabHolders.remove(positon);
			setTabClosed(tab);
			closedTabs.add(tab);
			if (tab!=currentWebHolder) {
				fast_recalc_adapter_idx(adapter_idx);
			} else {
				adapter_idx = -1; // todo jump list
			}
			//todo add fast un-close button
			tabViewAdapter.notifyItemRemovedAt(positon, source);
			tabsDirty = true;
		}
	}
	
	public void setTabClosed(TabHolder tab) {
		if(tab==currentWebHolder) {
			currentViewImpl=null;
			adapter_idx = -1;
			//Utils.removeView(imageViewCover);
		}
		if(opt.getDelayRemovingClosedTabs()) {
			activeClosedTabs.add(tab);
			// todo delay
			tab.freeze();
			if(tab.viewImpl!=null) {
				tab.viewImpl.resetBitmap();
			}
		} else {
			id_table.remove(tab.id);
			tab.close();
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
	
	public WebFrameLayout webFrameLayout;
	
	
	/** Leaving the current label for some reason.
	 * @param reason 0=to leave webview;
	 * 				 1=to show webview
	 * 				 2=to leave activity
	 * 				 3=to switch to another webview
	 * */
	public void onLeaveCurrentTab(int reason) {
//		if(false)
//		if(reason==0)
//		{
//			mWebView.pauseTimers();
//			mWebView.onPause();
//			mWebView.holder.paused=true;
//		}
		WebFrameLayout layout = this.currentViewImpl;
		if(layout ==null) return;
		TabHolder holder = layout.holder;
		//CMN.Log("onLeaveCurrentTab::", holder.lastCaptureVer, holder.version);
		if(reason!=1)
			if(holder.lastCaptureVer!=holder.version) {
				if(layout.recaptureBitmap()) {
					onBitmapCaptured(layout, reason);
				} else {
					layout.pendingBitCapRsn = reason;
				}
			}
		if(reason==3||reason==0) {
			//CMN.Log("onLeaveCurrentTab", mWebView.version);
			if(holder.version>1) {
				if(opt.saveTabsOnPause()) {
					checkWebViewDirtyMap.add(layout);
				} else {
					layout.saveIfNeeded();
				}
			}
			//mWebView.saveIfNeeded();
		}
	}
	
	public void onBitmapCaptured(WebFrameLayout view, int reason) {
		if(reason!=0) {
			if(reason!=2) {
				TabHolder holder = view.holder;
				glide.load(new WebPic(holder.id, holder.version, id_table))
						.priority(Priority.HIGH)
						.into(reason==3?imageViewCover1:imageViewCover);
			} else {
				view.saveBitmap(false);
			}
		}
	}
	
	/** 根据标签页ID（来自数据库）
	 * 	跳转至标签页。 | Jump Tab By It's ID */
	public boolean NavigateToTab(long tab_id) {
		TabHolder tab = allTabs.get(tab_id);
		if (tab!=null && !tab.closed) {
			int idx = TabHolders.indexOf(tab);
			if (idx>=0) {
				//AttachWebAt(idx, 0);
				//hideTabView();
				selectTab(idx);
				return true;
			}
		}
		return false;
	}
	
	private void hideTabView() {
		if(tabViewAdapter.isVisible()) {
			tabViewAdapter.hideTabView(false);
			WebFrameLayout layout = this.currentViewImpl;
			if (layout!=null) {
				layout.setScaleX(1);
				layout.setScaleY(1);
				layout.setTranslationX(0);
				layout.setTranslationY(0);
			}
		}
	}
	
	public boolean AttachWebAt(int i, int pseudoAdd) {
		CMN.Log("AttachWebAt", i, "pseudoAdd", pseudoAdd);
		hideSettingsPanel();
		int size = TabHolders.size()-1; // sanity check
		if(size<0) return pseudoAdd!=0;
		if(i<0) i=0;
		if(i>size) i=size;
		TabHolder tabIni = TabHolders.get(i);
		WebFrameLayout weblayout = tabIni.viewImpl;
		
		TabHolder holder = TabHolders.get(i);
		
		if(tabIni.viewImpl==null) {
			WebFrameLayout viewImpl = id_table.get(holder.id);
			if(viewImpl==null) {
				// todo various kinds of browser tabs.
				if(holder.type==0) {
					// type webview
					viewImpl = new WebFrameLayout(this, holder); //ttt
					//viewImpl.setBackgroundColor(Color.WHITE);
					viewImpl.setPadding(0, 0, 0, _45_);
					viewImpl.setPivotX(0);
					viewImpl.setPivotY(0);
					viewImpl.setImplementation(initWebViewImpl(viewImpl, null, true));
				} else {
					// ???
				}
				id_table.put(holder.id, viewImpl);
				//mWebView.setNestedScrollingEnabled(false);
			}
			tabIni.viewImpl = weblayout = viewImpl;
		} else {
			if(pseudoAdd==1 && weblayout!=null && weblayout.getParent()!=null
					&& weblayout.implView!=null && weblayout.implView.getScaleX()==1 && weblayout.implView.getVisibility()==View.VISIBLE) {
				return true;
			}
		}
		
		if(/*pseudoAdd==0 &&*/ weblayout.lazyLoad()) {
			weblayout.setStorageSettings();
			weblayout.setBackEndSettings(); // 初始化
			weblayout.setImmersiveScrollSettings();
			weblayout.setTextSettings(true, true);
			weblayout.setLockSettings();
			tabsManagerIsDirty = true;
		}
		
		if(pseudoAdd!=1) {
//			mWebView.setNestedScrollingEnabled(true);
//			String url = mWebView.getUrl();
//			if(url!=null && url.contains("p_2")) {
//				mWebView.setNestedScrollingEnabled(false);
//			}
			if(pseudoAdd==0&&weblayout.recover) {
				tabViewAdapter.uncoverTheTab(225);
				//mWebView.setVisibility(View.VISIBLE);
			}
		} else if(true){
			//mWebView.setVisibility(View.INVISIBLE);
			tabViewAdapter.coverupTheTab(weblayout, true);
			if(weblayout.getBitmap()==null) {
				glide.load(new WebPic(holder.id, holder.version, id_table))
						.into(imageViewCover);
			}
		}
		//weblayout.setAlpha(1);
		weblayout.setVisibility(View.VISIBLE);
		weblayout.setScaleX(1);
		weblayout.setScaleY(1);
		boolean attachView = pseudoAdd!=3 && pseudoAdd!=4;
		boolean add = true;
		if(attachView) {
			//if(pseudoAdd==0)
			{
				View ca;
				for (int j = 0; j < UIData.webcoord.getChildCount(); j++) {
					if((ca=UIData.webcoord.getChildAt(j)) instanceof WebFrameLayout
							&&ca!=weblayout
							&&(pseudoAdd==0||ca!=mBrowserSlider.webla&&ca!=currentViewImpl)
					) {
						try {
							CMN.Log("移除多余的你::", UIData.webcoord.getChildAt(j));
							if (!(ca.getLayoutParams() instanceof CoordinatorLayout.LayoutParams)) {
								ca.setLayoutParams(new CoordinatorLayout.LayoutParams(-1, -1));
								CMN.Log("不是的！！！");
							}
							UIData.webcoord.removeViewAt(j);
							j--;
						} catch (Exception e) {
							// todo fix java.lang.ClassCastException: com.google.android.material.appbar.AppBarLayout$LayoutParams cannot be cast to androidx.coordinatorlayout.widget.CoordinatorLayout$LayoutParams
							// 	 occurred when switch tab( immersive scroll enabled, hide bottom bar only ) to  tab( immersive scroll enabled, hide top bar only )
							CMN.Log(e);
						}
					}
				}
			}
			add = Utils.addViewToParent(weblayout, UIData.webcoord, 0);
			decideWebviewPadding(weblayout);  CMN.Log("Attach检查 padding...");
			weblayout.onViewAttached(0);
		}
		if(pseudoAdd==1 || pseudoAdd==4) {
			return false;
		}
		currentViewImpl = weblayout;
		currentWebHolder = weblayout.holder;
		currentWebView=weblayout.mWebView;
		webtitle.setText(holder.title);
		
		weblayout.hideForTabView = false;
		weblayout.checkSettings(false, true); // Attach检查
		checkImmersiveMode(); // Attach检查
		if (settingsPanel!=null) hideSettingsPanel();
		if (weblayout.focusFucker.size()>0) {
			for(Object o:weblayout.focusFucker) {
				if (o instanceof Dialog) {
					Dialog d = (Dialog) o;
					if (!d.isShowing()) {
						d.show();
						d.dismiss();
					}
				}
			}
			weblayout.focusFucker.clear();
		}
		
		webtitle_setVisibility(false);
		etSearch_clearFocus();
		//if(pseudoAdd==0)
		updateProgressUI();
		if(adapter_idx!=i || add) {
			adapter_idx = i;
			etSearch.setText(holder.url);
		}
		UIData.browserWidget10.setText(Integer.toString(i));
		return add;
	}
	
	private void etSearch_clearFocus() {
		etSearch.clearFocus();
		imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
		if(!tabViewAdapter.isVisible()) {
			root.removeCallbacks(postSetSoftResizeRunnable);
			root.postDelayed(postSetSoftResizeRunnable, 350);
		}
	}
	
	Runnable postSetSoftResizeRunnable = () -> setSoftInputMode(softModeResize);
	
	public void checkImmersiveMode() {
		long flag = currentViewImpl.getDelegateFlag(ImmersiveSettings, false);
		if (fixAll==WebOptions.getImmersiveScrollEnabled(flag)
				||fixTopBar==WebOptions.getImmersiveScroll_HideTopBar(flag)
				|| fixBotBar==WebOptions.getImmersiveScroll_HideBottomBar(flag)
		) {
			decideTopBotBehaviours();
		}
	}
	
	/** 根据沉浸式滚动设置顶栏和底栏的外观以及行为 */
	private void decideTopBotBehaviours() {
		if(currentViewImpl!=null) {
			long flag = currentViewImpl.getDelegateFlag(ImmersiveSettings, false);
			fixAll = !WebOptions.getImmersiveScrollEnabled(flag);
			fixTopBar = !WebOptions.getImmersiveScroll_HideTopBar(flag);
			fixBotBar = !WebOptions.getImmersiveScroll_HideBottomBar(flag);
			if (fixTopBar||fixBotBar) {
				ResetIMOffset();
			}
			if (fixBotBar) {
				bottombarLP.setBehavior(null);
			} else if (fixTopBar) {
				bottombarLP.setBehavior(bottombarHideBehaviour);
			} else {
				bottombarLP.setBehavior(bottombarScrollBehaviour);
			}
			ViewGroup svp = (ViewGroup) UIData.toolbar.getParent();
			if(svp==UIData.webcoord ^ fixTopBar) {
				Utils.addViewToParent(UIData.toolbar, fixTopBar?UIData.webcoord:UIData.appbar, fixTopBar?2:0);
				if(fixTopBar) {
					if(toolbarti==null) {
						toolbarti = new View(this);
						toolbarti.setLayoutParams(toolbatLP);
					}
					Utils.addViewToParent(toolbarti, UIData.appbar);
				} else {
					UIData.toolbar.setLayoutParams(toolbatLP);
					Utils.removeView(toolbarti);
				}
			}
		}
		//decideWebviewPadding(currentViewImpl);
	}
	
	/** 根据沉浸式滚动设置当前网页视图PADDING */
	private void decideWebviewPadding(WebFrameLayout weblayout) {
		if (weblayout==null) {
			return;
		}
		CoordinatorLayout.LayoutParams lp = ((CoordinatorLayout.LayoutParams) weblayout.getLayoutParams());
		if (lp==null) {
			weblayout.setLayoutParams(lp=new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		} else {
			lp.height= ViewGroup.LayoutParams.MATCH_PARENT;
		}
		
		int appbar_height = (int) mResource.getDimension(R.dimen._45_);
		int pt = 0;
		int pb = 0;
		
		long flag = weblayout.getDelegateFlag(ImmersiveSettings, false);
		boolean fixAll=!WebOptions.getImmersiveScrollEnabled(flag);
		boolean fixTopBar=!WebOptions.getImmersiveScroll_HideTopBar(flag);
		boolean fixBotBar=!WebOptions.getImmersiveScroll_HideBottomBar(flag);
		
		if(fixTopBar||fixAll) {
			//lpw.topMargin=bottombar_height;
			lp.setBehavior(null);
			pt = appbar_height;
		} else {
			lp.setBehavior(scrollingBH);
		}
		if((fixBotBar||fixAll) && !bottombarHidden) {
			pb = UIData.bottombar2.getLayoutParams().height;
			if (pb<=0) pb = UIData.bottombar2.getHeight();
			if (pb<=0) pb = appbar_height;
		}
		//CMN.Log("decideWebviewPadding::", bottombarHidden, pt, pb);
		weblayout.setPadding(0,pt,0,pb);
	}
	
	// click
	@RequiresApi(api = Build.VERSION_CODES.N_MR1)
	@SuppressLint("NonConstantResourceId") // no no no you don't want that.
	@Override
	public void onClick(View v) {
		if(!systemIntialized||mBrowserSlider.active()||mBrowserSlider.moved()) {
			return;
		}
		int vid = v.getId();
		if (mInterceptorListener !=null) {
			mInterceptorListenerHandled = false;
			mInterceptorListener.onClick(v);
			if (mInterceptorListenerHandled) {
				return;
			}
		}
		if (settingsPanel!=null) {
			hideSettingsPanel();
			if(vid==R.id.browser_widget7
					|| vid==R.id.browser_widget8
			) {
				return;
			}
		}
		if (navManager !=null && navManager.isVisible()) {
			if(vid!=R.id.browser_widget8) {
				navManager.dismiss(false);
				if(vid!=R.id.browser_widget10 && vid!=R.id.browser_widget11) {
					return;
				}
			}
		}
		switch (vid){
			case R.id.ivBack:{ // 搜索引擎弹窗 //searpop
				webDictsManager.toggle();
			} break;
			case R.id.search_engines_add: {
			
			} break;
			case R.id.webtitle: {
				CharSequence text = etSearch.getText();
				boolean t0 = text.length() == 0;
				ResetIMOffset();
				if(t0) {
					etSearch.setText(currentWebView.getUrl());
				}
				goToBarcodeScanner=t0||StringUtils.equals(text, currentWebView.getUrl());
				webtitle_setVisibility(true);
				updateQRBtn();
				root.removeCallbacks(postSetSoftResizeRunnable);
				setSoftInputMode(softModeHold);
				if(opt.getSelectAllOnFocus()||opt.getShowImeImm()) {
					v.post(() -> { //upEvt
						etSearch.requestFocus();
						if(opt.getShowImeImm()) {
							imm.showSoftInput(etSearch, 0);
						}
					});
				}
			} break;
			case R.id.ivSearchTabs: {
				tabViewAdapter.showSearchView(tabViewAdapter.isVisible());
			} break;
			case R.id.ivRefresh:
				WebFrameLayout layout = currentViewImpl;
				UniversalWebviewInterface mWebView = currentWebView;
				if(search_bar_vis()) {
					webtitle_setVisibility(false);
					etSearch_clearFocus();
				}
				if (!TestHelper.showSearchTabs && tabViewAdapter.isVisible()) {
					//UIData.searchHolder.showSearch();
					tabViewAdapter.showSearchView(true);
					break;
				}
				if(UIData.progressbar.getVisibility()==View.VISIBLE) {
					UIData.progressbar.setVisibility(View.GONE);
					supressingProgressListener = true;
					mWebView.stopLoading();
					progressProceed.cancel();
					animatorTransient.cancel();
					UIData.ivRefresh.setImageResource(R.drawable.ic_refresh_white_24dp);
					root.postDelayed(() -> supressingProgressListener = false, 100);
				} else {
					CMN.Log("刷新……");
					progressbar_background.setLevel(0);
					UIData.progressbar.setAlpha(1);
					layout.updateUserAgentString();
					mWebView.reload();
					UIData.ivRefresh.setImageResource(R.drawable.ic_close_white_24dp);
					supressingProgressListener = false;
				}
			break;
			case R.id.ivOverflow:
				if(search_bar_vis()) {
					webtitle_setVisibility(false);
					etSearch_clearFocus();
					break;
				}
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
						if(hasFocus&&topMenuRequestedInvalidate) {
							TopMenuClicker.decorateAll();
							currentViewImpl.checkSettings(true, true);  // 设置变化
						}
					});
					TopMenuClicker.decorateAll();
				}
				top_menu.show();
				break;
			case R.id.browser_widget7:
				if (IsShowingSearchHistory) {
					UIData.searchHints.performClick();
					break;
				}
				if (tabViewAdapter.isVisible()) {
					Integer arrayId = R.array.sync_tabs;
					// mResource.getString(R.string.sync_tabs)
					AlertDialog alert = getOptionListDialog(WeakReferenceHelper.opt_list_main, 0, arrayId, null);
					if(!arrayId.equals(alert.tag)) {
						ListView lv = alert.getListView();
						lv.setOnItemClickListener((parent, view, position, vid1) -> {
							switch (position) {
								case -1:
								break;
								/* 同步上传标签页 */
								case 0:
								/* 同步下载标签页 */
								case 1: {
									AlertDialog syncInterface = buildSyncInterface();
									View[] items = (View[]) syncInterface.tag;
									if(items[1]!=null) {
										items[1].setVisibility(View.GONE);
									}
									int tid;
									if(syncHandler==null) {
										syncHandler = new SardineCloud();
									}
									if(position==0) {
										tid = R.string.sync_dddot;
										items[0].setEnabled(false);
										items[4].setVisibility(View.VISIBLE);
									} else {
										tid = R.string.fetch_syn_info;
										items[4].setVisibility(View.GONE);
										initWaveProgressView(items);
										syncHandler.scheduleTask(this, SardineCloud.TaskType.pullTabs);
									}
									items[4].setTag(position);
									//postSelectSyncTabs();
									((TextView)items[5]).setText(tid);
								} break;
								/* 同步设置 */
								case 2:{
								
								} break;
							}
							//alert.dismiss();
						});
						alert.tag = arrayId;
					}
					break;
				}
				WebFrameLayout ly = currentViewImpl;
				UniversalWebviewInterface wv = currentWebView;
				boolean backable = wv.canGoBack();
				CMN.Log("backable::", backable, ly.getThisIdx(), wv.getUrl());
				ly.new_page_loaded = true;
				if(backable) {
					if(true) {
						webtitle.setText(ly.navigateHistory(this, -1));
					}
					if(true) {
						wv.stopLoading();
					}
					wv.goBack();
				}
				else if(ly.holder.getLuxury()) {
					boolean added = false;
					int idx = ly.getThisIdx();
					if(idx>0) {
						((View)wv).setVisibility(View.GONE);
						ly.pauseWeb();
						currentWebView = (UniversalWebviewInterface) ly.getChildAt(idx-1);
						currentViewImpl.implView.setVisibility(View.VISIBLE);
						ly.resumeWeb();
						added = true;
					} else {
						int size = ly.PolymerBackList.size();
						if(size>0) {
							((View)wv).setVisibility(View.GONE);
							ly.pauseWeb();
							String backUrl = ly.PolymerBackList.remove(size - 1);
							CMN.Log("--backUrl::", backUrl);
							currentWebView = initWebViewImpl(ly, wv, true);
							currentWebView.loadUrl(backUrl);
							ly.resumeWeb();
							added=true;
						}
					}
					if(added) {
						v.jumpDrawablesToCurrentState();
						setUrlAndTitle();
						//id_table.put(currentWebView.holder.id, currentViewImpl);
						UIData.progressbar.setVisibility(View.GONE);
					}
				}
				CMN.Log("backed::", wv.getUrl());
				break;
			case R.id.browser_widget8:
				if (IsShowingSearchHistory) {
					// 显示历史记录的快速设置界面
					int jd = WeakReferenceHelper.search_history_options;
					SearchHistoryAndInputMethodSettings shimObj
							= (SearchHistoryAndInputMethodSettings) getReferencedObject(jd);
					if (shimObj==null) {
						shimObj = new SearchHistoryAndInputMethodSettings(
								this
								, UIData.webcoord
								, UIData.bottombar2.getHeight()
								, opt
						);
						putReferencedObject(jd, shimObj);
					}
					shimObj.toggle(UIData.webcoord, null);
					settingsPanel = shimObj.isVisible()?shimObj:null;
					break;
				}
				if (tabViewAdapter.isVisible()) {
					break;
				}
				ly = currentViewImpl;
				wv = currentWebView;
				boolean added = false;
				boolean forwadable = wv.canGoForward();
				CMN.Log("forwadable::", forwadable, ly.getThisIdx(), wv.getUrl());
				ly.new_page_loaded = true;
				if(forwadable) {
					if(true) {
						webtitle.setText(ly.navigateHistory(this, 1));
					}
					if(true) {
						wv.stopLoading();
					}
					wv.goForward();
				}
				else if(ly.holder.getLuxury()) {
					int cc = ly.getChildCount();
					int idx = ly.getThisIdx();
					if(idx<cc-1) {
						if(!forwadable||ly.isAtLastStackMinusOne()) {
							ly.pauseWeb();
							currentWebView = (AdvancedBrowserWebView) ly.getChildAt(idx+1);
							currentViewImpl.implView.setVisibility(View.VISIBLE);
							ly.resumeWeb();
							added = true;
						}
					}
					if(added) {
						v.jumpDrawablesToCurrentState();
						setUrlAndTitle();
						//id_table.put(currentWebView.holder.id, currentViewImpl);
						UIData.progressbar.setVisibility(View.GONE);
					}
				}
//				fixBotBar = !fixBotBar;
//				decideTopBotBH();
//				decideWebviewPadding();
//				showT("fixBotBar : "+fixBotBar);
				break;
			case R.id.browser_widget9:
				if (IsShowingSearchHistory) {
					etSearch.requestFocus();
					imm.showSoftInput(etSearch, 0);
					break;
				}
				getNavManager().toggle(UIData.webcoord, null);
			break;
			case R.id.browser_widget10:
//				if (true) {
//					// test new tabs
//					//newTab(null, false, true, -1);
//					// test switch tabs
//					AttachWebAt(adapter_idx+1, 0);
//					break;
//				}
				tabViewAdapter.toggle(v);
			break;
			case R.id.browser_widget11:
				// todo MenuClicked=menuGrid!=null;
				mWebListener.dismissAppToast();
				showMenuGrid();
			break;
			case R.id.etSearch:{
				if (keyboard_hidden) {
					keyboard_hidden = false;
				}
			} break;
			case R.id.show_search_history_dropdown_bg:
			case R.id.search_hints:
			case R.id.browser_widget1:{
				onBackPressed();
			} break;
			case R.id.show_search_history_dropdown:{
				if (webtitle.getVisibility()!=View.VISIBLE)
				{
					pull_hints(true); //todo
					if (opt.getHideKeyboardOnShowSearchHints()) {
						if(!keyboard_hidden) {
							imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
							keyboard_hidden=true;
						}
					}
				}
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
					copyText(text);
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
				currentViewImpl.searchTerm = null;
				if(opt.getShowKeyIMEOnClean()) {
					imm.showSoftInput(etSearch, 0);
					keyboard_hidden = false;
				}
			} break;
			case R.id.browser_widget5:{
				String url = etSearch.getText().toString().trim();
				if(goToBarcodeScanner||url.length()==0) { //二维码
					Intent intent = new Intent(this, QRActivity.class);
					startActivityForResult(intent, RequsetUrlFromCamera);
				} else {
					if(url.equals("w:")) {
						File f = new File("/storage/emulated/0/w1");
						if(f.exists()) {
							try(FileInputStream in = new FileInputStream(f)) {
								byte[] data = new byte[(int) f.length()];
								in.read(data);
								Bundle bundle = new Bundle();
								new WebStacksSer().readData(bundle, data);
								currentWebView.restoreState(bundle);
							} catch (Exception e){ CMN.Log(e); }
						}
					} else if(url.startsWith("chrome:")) {
						currentWebView.loadUrl(url);
					} else {
						execBrowserGoTo(url, true);
					}
				}
			} break;
		}
	}
	
	public NavigationManager getNavManager() {
		if (navManager==null) {
			navManager = new NavigationManager(this);
		}
		return navManager;
	}
	
	public void copyText(String text) {
		ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText("PLOD", text));
	}
	
	private void initWaveProgressView(View[] items) {
		WaveView waveView = (WaveView) items[0];
		this.waveView = waveView;
		waveView.setMax(100);
		waveView.setProgress(0);
		waveView.setEnabled(true);
		mHandler.sendEmptyMessageDelayed(11576, 100);
	}
	
	Runnable postRectifyWebTitleRunnable = () -> {
		if(!tabViewAdapter.isVisible()) {
			String title = currentViewImpl.transientTitle;
			if(title!=null) {
				webtitle.setText(currentViewImpl.rectifyWebStacks(title));
			} else {
				postRectifyWebTitle();
			}
		}
	};
	public void postRectifyWebTitle() {
		root.removeCallbacks(postRectifyWebTitleRunnable);
		root.postDelayed(postRectifyWebTitleRunnable, 500);
	}
	
	public void HideSelectionWidgets(boolean hideSel) {
		WebFrameLayout layout = this.currentViewImpl;
		if (layout!=null) {
			layout.suppressSelection(hideSel);
		}
	}
	
	public void fixFocusHiddenSelectionWidgets(Object d) {
		WebFrameLayout layout = this.currentViewImpl;
		if (layout!=null && layout.hasSelection()) {
			layout.focusFucker.add(d);
		}
	}
	
	
	public void showMenuGrid() {
		int jd = WeakReferenceHelper.menu_grid;
		MenuGrid menuGrid = (MenuGrid) getReferencedObject(jd);
		if (menuGrid==null) {
			menuGrid = new MenuGrid(this);
			putReferencedObject(jd, menuGrid);
			CMN.Log("新建MenuGrid...");
		}
		if (!menuGrid.isVisible()) {
			menuGrid.toggle(UIData.webcoord, null);
		}
	}
	
	public void showWebAnnots() {
		int jd = WeakReferenceHelper.web_annots;
		WebAnnotationPanel wenAnnots
				= (WebAnnotationPanel) getReferencedObject(jd);
		if (wenAnnots==null||true) {
			wenAnnots = new WebAnnotationPanel(this);
			putReferencedObject(jd, wenAnnots);
			CMN.Log("重建WebAnnotationPanel...");
		}
		settingsPanel = wenAnnots;
		if(!wenAnnots.isVisible()) {
			wenAnnots.toggle(UIData.webcoord, (SettingsPanel) getReferencedObject(WeakReferenceHelper.menu_grid));
			int padding = 0;
			if (UIData.appbar.getTop()>=0) {
				padding += UIData.toolbar.getHeight();
			}
			if (UIData.bottombar2.getBottom()<=UIData.webcoord.getHeight()) {
				padding += UIData.bottombar2.getHeight();
			}
			wenAnnots.setInnerBottomPadding(padding);
		}
//		if (menuGrid!=null && vis) {
//			//modThreeBtnForMenuGrid(false, true);
////							settingsPanel.settingsLayout.postDelayed(new Runnable() {
////								@Override
////								public void run() {
////									toggleMenuGrid(false);
////								}
////							}, 220);
//			//modThreeBtnForMenuGrid(false, true);
//		}
	}
	
	public void showNightMode() {
		int jd = WeakReferenceHelper.web_annots;
		NightMode nightMode = new NightMode(this);
		settingsPanel = nightMode;
		if(!nightMode.isVisible()) {
			nightMode.toggle(UIData.webcoord, (SettingsPanel) getReferencedObject(WeakReferenceHelper.menu_grid));
			showT("尚未实现……");
		}
		settingsPopup = nightMode.pop;
	}
	
	public void showBrowserSettings() {
		int jd = WeakReferenceHelper.quick_settings;
		QuickBrowserSettingsPanel quickSettings
				= (QuickBrowserSettingsPanel) getReferencedObject(jd);
		if (quickSettings==null) {
			quickSettings = new QuickBrowserSettingsPanel(this);
			putReferencedObject(jd, quickSettings);
			CMN.Log("重建QuickBrowserSettingsPanel...");
		} else {
			quickSettings.refresh();
		}
		boolean vis = quickSettings.toggle(UIData.webcoord, (SettingsPanel) getReferencedObject(WeakReferenceHelper.menu_grid));
		if (vis) {
			int padding = 0;
			if (UIData.appbar.getTop()>=0) {
				padding += UIData.toolbar.getHeight();
			}
			if (UIData.bottombar2.getBottom()<=UIData.webcoord.getHeight()) {
				padding += UIData.bottombar2.getHeight();
			}
			quickSettings.setInnerBottomPadding(padding);
		}
		settingsPanel = vis?quickSettings:null;
//		if (menuGrid!=null && vis) {
//			toggleMenuGrid(true);
//		}
	}
	
//	private void modThreeBtnForMenuGrid(boolean menuGrid!=null, boolean animate) {
//		View[] sameView = new View[]{UIData.browserWidget7, UIData.browserWidget8};
//		for (View vue:sameView) {
//			if (animate) {
//				vue.animate().
//						alpha(menuGrid!=null?0:1)
//						.setListener(menuGrid!=null?new AnimatorListenerAdapter() {
//							@Override
//							public void onAnimationEnd(Animator animation) {
//								vue.setVisibility(View.INVISIBLE);
//							}
//						}:null)
//						.setDuration(90);
//				if (!menuGrid!=null) {
//					vue.setVisibility(View.VISIBLE);
//				}
//			} else {
//				vue.setAlpha(1);
//				vue.setVisibility(menuGrid!=null?View.INVISIBLE:View.VISIBLE);
//			}
//		}
//		UIData.browserWidget9.setImageResource(menuGrid!=null?R.drawable.ic_exit_to_app:R.drawable.ic_home_black_24dp);
//	}
	
	public void setSoftInputMode(int mode) {
		if(softMode!=mode) {
			softMode=mode;
			getWindow().setSoftInputMode(mode);
		}
	}
	
	public void AddPDFViewerShortCut() {
		//				Intent intent = new Intent("colordict.intent.action.SEARCH");
		//				intent.putExtra("EXTRA_QUERY", "happy");
		//				startActivity(intent);
		//intent.addCategory(Intent.CATEGORY_BROWSABLE);
		//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		//				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		//				intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		//intent.setFlags(0x10000000);
		
		Context context = getApplicationContext();
		boolean succ = false;
		try {
			//if (ShortcutManagerCompat.isRequestPinShortcutSupported(context))
			Intent intent = new Intent(Intent.ACTION_MAIN)
					.setClassName("com.knziha.polymer", "com.knziha.polymer.PDocShortCutActivity")
					.putExtra("main", true)
					.setData(Uri.fromFile(new File("123345")));
			ShortcutInfoCompat shortcutInfo = new ShortcutInfoCompat.Builder(context, "knziha.pdf")
					.setIcon(IconCompat.createWithResource(context, R.mipmap.ic_pdoc_viewer))
					.setShortLabel(mResource.getString(R.string.pdf_viewer))
					.setIntent(intent)
					.build();
			succ = ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null);
		} catch (Exception e) { }
		if (!succ) {
			showT("不支持或没有权限！");
		}
	}
	
	public boolean topMenuRequestedInvalidate;
	
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
//						if(v.getTag()==null) {
//							TickIntoAction(v, "标签页-崭新模式", "\n\t\t即将为标签页切换启崭新模式，此模式下禁用站点的本地存储。\n\n（长按按钮以关闭此延时提示）");
//							dissmiss=false;
//						} else {
//							v.setTag(null);
//							boolean val = WebOptions.toggleForbidLocalStorage(currentViewImpl.getDelegateFlag(StorageSettings, false));
//							currentViewImpl.setStorageSettings();
//							showT("已为<标签页>"+(val?"开启":"关闭")+"崭新模式！");
//						}
					}
				break;
				case R.id.refresh:
					currentWebView.reload();
				break;
				case R.id.print:
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						CustomViewHideTime = PrintStartTime = System.currentTimeMillis()-3500;
						printSX = currentViewImpl.implView.getScrollX();
						printSY = currentViewImpl.implView.getScrollY();
						printScale = currentViewImpl.implView.getScaleX();
						PDFPrintManager.printPDF(BrowserActivity.this, currentWebView, false);
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
					shareUrlOrText(null, null);
				} break;
				/* 页内搜索 */
				case R.id.menu_icon8: {
				
				} break;
				/* PC模式 */
				case R.id.menu_icon9:
					if(isLongClicked) {
						show_web_configs(BackendSettings);
					} else {
						WebOptions.tmpFlag = currentViewImpl.getDelegateFlag(BackendSettings, true);
						WebOptions.togglePCMode();
						currentViewImpl.setDelegateFlag(BackendSettings, WebOptions.tmpFlag);
						currentViewImpl.updateUserAgentString();
						currentWebView.reload();
						decoratePCMode();
					}
				break;
				case R.id.bookmark: {
				
				} break;
				case R.id.mainpage: {
					getNavManager().InsertNavNode(null, null);
				} break;
				case R.id.search: {
				
				} break;
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
			ld.getDrawable(1).setAlpha(currentViewImpl.getPCMode()?255:0);
		}
		
		@Override
		public void run() {
			dlg.dismiss();
		}
		
		public void decorateAll() {
			//((TextView)vals[0]).setTextColor(currentViewImpl.getForbidLocalStorage()?0xff4F7FDF:AppBlack);
			
			TextView shareIndicator = (TextView)vals[1];
			boolean shareText=currentViewImpl.implView.hasFocus()&&currentViewImpl.hasSelection();
			shareIndicator.setText(shareText?"分享文本":"分享链接");
			
			decoratePCMode();
		}
		
		private void show_web_configs(int groupID) {
			WebViewSettingsDialog holder = new WebViewSettingsDialog(BrowserActivity.this);
			
			WebViewSettingsDialog.buildStandardConfigDialog(BrowserActivity.this, holder, null, R.string.global_vs_tabs);
			
			holder.init_web_configs(currentViewImpl.getApplyTabRegion(groupID), groupID, currentViewImpl.getApplyDomainRegion(groupID));
		
			topMenuRequestedInvalidate=false;
		
			holder.dlg.show();
		}
	}
	
	public void shareUrlOrText(String url, String text) {
		//CMN.Log("menu_icon6menu_icon6");
		//CMN.rt("分享链接……");
		int id = WeakReferenceHelper.share_dialog;
		BottomSheetDialog dlg = (BottomSheetDialog) getReferencedObject(id);
		if(dlg==null) {
			putReferencedObject(id, dlg=new AppIconsAdapter(this).shareDialog);
		}
		//CMN.pt("新建耗时：");
		if (url==null) {
			url = currentWebView.getUrl();
		}
		AppIconsAdapter shareAdapter = (AppIconsAdapter) dlg.tag;
		shareAdapter.pullAvailableApps(this, url, text);
		//shareAdapter.pullAvailableApps(this, null, "happy");
		//CMN.pt("拉取耗时：");
	}
	
	private void setUrlAndTitle() {
		webtitle.setText(currentViewImpl.holder.url=currentWebView.getTitle());
		etSearch.setText(currentViewImpl.holder.title=currentWebView.getUrl());
	}
	
	public void fadeOutProgressbar() {
		if(UIData.progressbar.getAlpha()==1 && !supressingProgressListener) {
//			progressTransient.pause();
//			progressProceed.pause();
			progressTransient.setFloatValues(1, 0);
			int start = progressbar_background.getLevel();
			progressProceed.setIntValues(start, 10000);
			animatorTransient.setDuration(Math.max((10000-start)/10, 10));
			animatorTransient.start();
		}
	}
	
	
	public void showHistory() {
		int id = WeakReferenceHelper.history_list;
		BrowserHistory historyList = (BrowserHistory) getReferencedObject(id);
		if(historyList==null) {
			CMN.Log("重建历史列表");
			putReferencedObject(id, historyList = new BrowserHistory());
		}
		historyList.show(getSupportFragmentManager(), "history");
	}
	
	public void showDownloads() {
		int id = WeakReferenceHelper.dwnld_list;
		BrowserDownloads downloadList = (BrowserDownloads) getReferencedObject(id);
		if(downloadList==null) {
			CMN.Log("重建下载列表");
			putReferencedObject(id, downloadList = new BrowserDownloads());
		}
		downloadList.show(getSupportFragmentManager(), "downloads");
	}
	
	public void showDownloadDialog(String url, long contentLength, String mimetype) {
		if(downloadDlgHandler==null) {
			downloadDlgHandler = new DownloadUIHandler(this, historyCon);
		}
		downloadDlgHandler.showDownloadDialog(url, contentLength, mimetype);
	}
	
	
	//longclick
	@Override
	public boolean onLongClick(View v) {
		if(mBrowserSlider.active()) {
			return false;
		}
		switch (v.getId()) {
			// 扫码
			case R.id.browser_widget5:{
				//if(!goToBarcodeScanner) return false;
				startActivity(new Intent(this, QRGenerator.class).putExtra(Intent.EXTRA_TEXT, Utils.getTextInView(etSearch)));
				acquireWakeLock();
			} break;
			// 新建标签页
			case R.id.browser_widget10:{
				newTab(null, false, true, -1);
			} break;
		}
		return true;
	}
	
	public void ResetIMSettings() {
		if (getDynamicFlagIndex(ImmersiveSettings)==3) {
			WebFrameLayout.GlobalSettingsVersion ++;
		}
		if (settingsPopup!=null) {
			embedPopInCoordinatorLayout(settingsPopup, settingsBelowAppbar);
		}
		currentViewImpl.setImmersiveScrollSettings();
		checkImmersiveMode();
		decideWebviewPadding(currentViewImpl); // 设置变化检查
		
		//currentViewImpl.bm.clear();
		Utils.removeView(imageViewCover);
//		currentViewImpl.implView.invalidate();
//		currentViewImpl.implView.destroyDrawingCache();
//		currentViewImpl.implView.requestLayout();
//		currentViewImpl.implView.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				currentViewImpl.implView.setVisibility(View.GONE);
//				currentViewImpl.implView.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						currentViewImpl.implView.setVisibility(View.VISIBLE);
//					}
//				}, 350);
//			}
//		}, 350);
	}
	
	private void ResetIMOffset() {
		AppBarLayout barappla = (AppBarLayout) UIData.appbar;
		if(barappla.getTop()<0) {
			//CMN.Log("重置了");
			barappla.resetAppBarLayoutOffset();
			barappla.requestLayout();
		}
	}
	
	public void newTab(String url, boolean background, boolean show, int newloc) {
		if (!background) {
			if (navManager !=null && navManager.isVisible()) {
				navManager.dismiss(false);
			}
			ResetIMOffset();
		}
		supressingNxtLux=CMN.now();
		int newtabloc;
		if (newloc==-1) {
			newtabloc = adapter_idx+1;
			if (tabViewAdapter.isVisible()) {
				newtabloc = tabViewAdapter.layoutManager.targetPos;
			}
		} else {
			newtabloc = newloc;
		}
		newtabloc = Math.max(0, Math.min(TabHolders.size(), newtabloc));
		if (url==null) {
			url = getDefaultPage();
		}
		TabHolder holder=new TabHolder();
		long id = historyCon.insertNewTab(url, CMN.now());
		tabsDirty=true;
		//CMN.Log("insertNewTab", id);
		if(id!=-1) {
			holder.url=url;
			holder.id=id;
			TabHolders.add(newtabloc, holder);
			
			if(tabViewAdapter.isVisible()) {
				//adaptermy.notifyDataSetChanged();
				tabViewAdapter.notifyItemInserted(newtabloc);
				tabViewAdapter.notifyItemRangeChanged(newtabloc, TabHolders.size()-newtabloc+1);
				tabViewAdapter.hideTabView(true);
			} else {
				onLeaveCurrentTab(3);
				tabsManagerIsDirty = true;
			}
			
			AttachWebAt(newtabloc, show?0:4);
			
			View target = UIData.browserWidget10;
			int trans = target.getHeight() / 2;
			ObjectAnimator tv1TranslateY = ObjectAnimator.ofFloat(target, "translationY", 0, -trans, 0);
			tv1TranslateY.setDuration(400);
			target.post(tv1TranslateY::start);
			//CMN.Log("newTab", currentViewImpl);
		}
	}
	
	public void selectTab(int newtabloc) {
		if(tabViewAdapter.isVisible() && false) {
			//tabViewAdapter.hideTabView();
			tabViewAdapter.toggleTabView(newtabloc, null, 0);
			return;
		}
		onLeaveCurrentTab(3);
		boolean b1=newtabloc!=adapter_idx||currentViewImpl==null||currentViewImpl.getParent()==null;
		boolean added = AttachWebAt(newtabloc, 0);
		if(b1) {
			TabHolder tab = TabHolders.get(newtabloc);
			if(tab.version<=0 || true) {
				tabViewAdapter.coverupTheTab(currentViewImpl, added);
				if(currentViewImpl.getBitmap()==null) {
					glide.load(new WebPic(tab.id, tab.version, id_table))
							.into(imageViewCover);
				}
			}
		}
		if (currentViewImpl!=null) {
			currentViewImpl.hideForTabView = false;
			currentViewImpl.setTranslationX(0);
			currentViewImpl.setTranslationY(0);
		}
		if(b1) {
			tabViewAdapter.uncoverTheTab(350); // 225
		}
		if(tabViewAdapter.isVisible()) {
			hideTabView();
		}
	}
	
	private String getDefaultPage() {
		return "https://www.bing.com";
	}
	
	/** Luxuriously Load Url：使用新的WebView打开链接，受Via浏览器启发，用于“返回不重载”。<br/>
	 *  标签页(tab)处于奢侈模式时({@link TabHolder#getLuxury})，使用此法打开链接。WebView总数有限制，且一段时间内不得打开太多。<br/>
	 *  前进后退时复用或销毁WebView。复用时，需清理网页回退栈。{@link WebFrameLayout#clearHistroyRequested}*/
	void LuxuriouslyLoadUrl(AdvancedBrowserWebView mWebView, String url) {
//		if(!mWebView.hasValidUrl()) {
//			mWebView.loadUrl(url);
//			mWebView.clearHistroyRequested=true;
//			return;
//		}
//		mWebView.pauseWeb();
//		int idx = mWebView.getThisIdx();
//		int cc = mWebView.layout.getChildCount();
//		if(idx<cc-1) {
//			mWebView = (AdvancedBrowserWebView) mWebView.layout.getChildAt(idx+1);
//			mWebView.setVisibility(View.VISIBLE);
//			mWebView.resumeWeb();
//			mWebView.clearHistroyRequested=true;
//			mWebView.layout.removeViews(idx+2, cc-idx-2);
//		} else {
//			mWebView = new_AdvancedNestScrollWebView(null, webFrameLayout, mWebView);
//		}
//		mWebView.loadUrl(url);
//		//id_table.put(mWebView.holder.id, mWebView);
//		currentWebView = mWebView;
	}
	
	HtmlObjectHandler mHtmlObjectHandler = new HtmlObjectHandler();
	
	class HtmlObjectHandler extends WebViewClient implements View.OnLongClickListener, PopupMenuHelper.PopupMenuListener, Runnable {
		private PopupMenuHelper popupMenu;
		Object mHitResult;
		String mHitUrl;
		String mHitUrl1;
		boolean postPopup;
		int urlPreviewIdx;
		@Override
		public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
			boolean ret=true;
			boolean dismiss = !isLongClick;
			String url = mHitUrl;
			if(url!=null)
				switch (v.getId()) {
					case R.string.houtaidakai:{
						if (mHitUrl1!=null) url = mHitUrl1;
						newTab(url, true, false, -1);
					} break;
					case R.id.always_new_tab_morpt: {
						// ttt
						MergedWebOptWidget viewAnimator = (MergedWebOptWidget) v.getTag();
						ViewGroup vp = (ViewGroup) v.getParent();
						if (viewAnimator!=null) {
							viewAnimator.show();
						}
					} return true;
					case R.string.xinbiaoqianyedaikai:
						if (mHitUrl1!=null) url = mHitUrl1;
						if (WebOptions.getAlwaysOpenInNewTab(getMergedFlag()) && true /** 若已设置总是在新标签页打开，则点击此项变成在本标签页内打开。 | Open In This Tab Direcltly When Opt to Always Open In New Tab.*/) {
							LastAutoNewTabTime = CMN.now();
							currentWebView.loadUrl(url);
							break;
						}
					case R.string.dakaitupian: {
						newTab(url, false, true, -1);
					} break;
					case R.string.fuzhilianjie: {
						if (mHitUrl1!=null) url = mHitUrl1;
						TextToClipboard(url, 0);
					} break;
					case R.string.fuzhiwenben: {
						currentWebView.evaluateJavascript("window._ttarget?window._ttarget.innerText:''", new ValueCallback<String>() {
							@Override
							public void onReceiveValue(String value) {
								value = StringEscapeUtils.unescapeJava(value.substring(1,value.length()-1));
								TextToClipboard(value, 1);
							}
						});
					} break;
					case R.string.xuanzewenben:{
						WebViewHelper.getInstance().SelectHtmlObject(BrowserActivity.this, currentViewImpl, 0);
					} break;
					case R.string.share:{
						shareUrlOrText(url, null);
					} break;
					case R.string.xiazaitupian:{
						showDownloadDialog(url, 0, "image/*");
					} break;
					case R.string.shitusousuo:{
						String webPicDict = "https://pic.sogou.com/ris?query=%s&flag=1&drag=1";
						webPicDict.replace("%s", StringEscapeUtils.escapeJson(url));
						newTab(webPicDict, false, true, -1);
					} break;
				}
			if (dismiss) {
				popupMenuHelper.postDismiss(80);
			}
			return ret;
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// https://stackoverflow.com/questions/12168039#answer-12175558
			mHitUrl1 = url;
			return true;
		}
		
		@Override
		public boolean onLongClick(View v) {
			try {
			//CMN.Log("onLongClick getHitResultObject", v);
			UniversalWebviewInterface _mWebView = v instanceof UniversalWebviewInterface?(UniversalWebviewInterface)v:((WebFrameLayout)v).mWebView;
			Object result = _mWebView.getHitResultObject();
			//CMN.Log("getHitResultObject", result);
			if (null == result) return false;
			int type = _mWebView.getHitType(result);
			CMN.Log("getHitTestResult", type, _mWebView.getHitExtra(result));
			if (type==WebView.HitTestResult.UNKNOWN_TYPE) {
				return false;
			}
			mHitResult = result;
			mHitUrl = _mWebView.getHitExtra(result);
			popupMenu = getPopupMenu();
			int tag = popupMenu.tag;
			int[] texts = null;
			urlPreviewIdx = 0;
			int newTabIdx = 1;
			mHitUrl1 = null;
			removePost();
			switch (type) {
				default: return false;
				/* 长按图片链接 */
				case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: {
					if (currentWebView!=null) {
						postPopup = true;
						Message msg = mHandler.obtainMessage();
						msg.what = 2021;
						currentWebView.requestFocusNodeHref(msg);
						//currentWebView.setWebViewClient(this);
						//Utils.performClick(currentWebView.getView(), currentViewImpl.lastX, currentViewImpl.lastY);
					}
					//currentWebView.setWebViewClient(mWebListener);
					if (tag!=R.string.shitusousuo) {
						texts = new int[] {
							R.string.houtaidakai
							,R.layout.menu_open_in_new_tab
							,R.string.dakaitupian
							,R.string.xiazaitupian
							,R.string.shitusousuo
							,R.string.fuzhilianjie
							,R.layout.menu_share_preview
						};
						tag=R.string.shitusousuo;
					}
					urlPreviewIdx = 6;
				} break;
				/* 长按图片 */
				case WebView.HitTestResult.IMAGE_TYPE:{
					if (tag!=R.string.dakaitupian) {
						texts = new int[] {
							R.string.dakaitupian
							,R.string.xiazaitupian
							,R.string.shitusousuo
							,R.string.fuzhilianjie
							,R.layout.menu_share_preview
						};
						tag=R.string.dakaitupian;
					}
					newTabIdx = -1;
					urlPreviewIdx = 4;
				} break;
				/* 长按anchor */
				case WebView.HitTestResult.SRC_ANCHOR_TYPE:{
					if (tag!=R.string.fuzhilianjie) {
						texts = new int[] {
								R.string.houtaidakai
								,R.layout.menu_open_in_new_tab
								,R.string.fuzhilianjie
								,R.string.fuzhiwenben
								,R.string.xuanzewenben
								//,R.string.share
								,R.layout.menu_share_preview
						};
						tag=R.string.fuzhilianjie;
					}
					urlPreviewIdx = 5;
//					String url = _mWebView.getHitExtra(result);
//					Integer arrayId = R.array.config_links;
//					AlertDialog alert = getOptionListDialog(WeakReferenceHelper.opt_list_main, 0, arrayId, url);
//					if(!arrayId.equals(alert.tag)) {
//						ListView lv = alert.getListView();
//						lv.setOnItemClickListener((parent, view, position, id) -> {
//							switch (position) {
//								case -1:
//								break;
//								/* 复制链接 */
//								case 2:{
//								} break;
//								/* 复制链接文本 */
//								case 3:{
//								} break;
//								/* 选择链接文本 */
//								case 4:{
//								} break;
//							}
//							alert.dismiss();
//						});
//						alert.tag = arrayId;
//					}
				
				} break;
			}
			if (texts!=null) {
				popupMenu.initLayout(texts, this);
				popupMenu.tag=tag;
			}
			if (newTabIdx!=-1) {
				boolean alwaysInNewTab = WebOptions.getAlwaysOpenInNewTab(getMergedFlag());
				ViewGroup xinbiaoqianyedaikai = (ViewGroup) popupMenu.lv.getChildAt(newTabIdx);
				View tv = xinbiaoqianyedaikai.findViewById(R.string.xinbiaoqianyedaikai);
				View morpt = xinbiaoqianyedaikai.findViewById(R.id.always_new_tab_morpt);
				morpt.setVisibility((alwaysInNewTab||true)?View.VISIBLE:View.GONE);
				MergedWebOptWidget viewAnimator = (MergedWebOptWidget) morpt.getTag();
				ViewGroup vp = (ViewGroup) v.getParent();
				if (viewAnimator==null) {
					viewAnimator = new MergedWebOptWidget(BrowserActivity.this, xinbiaoqianyedaikai, 37, false);
					tv.setOnTouchListener(viewAnimator);
					morpt.setTag(viewAnimator);
				} else {
					viewAnimator.hide();
				}
				View.OnLongClickListener longClicker = new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						boolean alwaysInNewTab = !WebOptions.getAlwaysOpenInNewTab(getMergedFlag());
						if (alwaysInNewTab) {
							WebOptions.tmpFlag = currentViewImpl.domainInfo.f1;
							WebOptions.setAlwaysOpenInNewTab(true);
							currentViewImpl.domainInfo.f1 = WebOptions.tmpFlag;
						} else {
							WebOptions.tmpFlag = opt.ThirdFlag;
							WebOptions.setAlwaysOpenInNewTab(false);
							opt.ThirdFlag = WebOptions.tmpFlag;
							WebOptions.tmpFlag = currentViewImpl.domainInfo.f1;
							WebOptions.setAlwaysOpenInNewTab(false);
							currentViewImpl.domainInfo.f1 = WebOptions.tmpFlag;
							WebOptions.tmpFlag = currentWebHolder.flag;
							WebOptions.setAlwaysOpenInNewTab(false);
							currentWebHolder.flag = WebOptions.tmpFlag;
						}
						WebViewHelper.LookForANobleSteedCorrespondingWithDrawnClasses(Utils.getNthParentNonNull(v,1), 0, ViewGroup.class, TextMenuView.class).setActivated(alwaysInNewTab);
						return true;
					}
				};
				tv.setOnLongClickListener(longClicker);
				morpt.setOnLongClickListener(longClicker);
				tv.setActivated(alwaysInNewTab);
			}
			if (postPopup) {
				root.postDelayed(this, 80);
			} else {
				run();
			}
			} catch (Exception e){CMN.Log(e);}
			return true;
		}
		
		@Override
		public void run() {
			if (popupMenu!=null) {
				int[] vLocationOnScreen = new int[2];
				currentViewImpl.getLocationOnScreen(vLocationOnScreen);
				int x=(int)currentViewImpl.lastX;
				int y=(int)currentViewImpl.lastY;
				popupMenu.show(root, x+vLocationOnScreen[0], y+vLocationOnScreen[1]);
				Utils.preventDefaultTouchEvent(currentViewImpl, x, y);
				if (urlPreviewIdx!=0) {
					View v = Utils.getViewItemByPath(popupMenu.lv, urlPreviewIdx, 1);
					if (v!=null) {
						((TextView)v).setText(mHitUrl1==null?mHitUrl:mHitUrl1);
					}
				}
				popupMenu = null;
			}
			removePost();
		}
		
		private void removePost() {
			if (postPopup) {
				//showT(mHitUrl1);
				root.removeCallbacks(this);
				if (currentWebView!=null) {
					currentWebView.setWebViewClient(mWebListener);
				}
				postPopup = false;
			}
		}
	}
	
	public long getMergedFlag() {
		long ret = opt.ThirdFlag();
		WebFrameLayout layout = this.currentViewImpl;
		if (layout !=null) {
			ret |= layout.domainInfo.f1;
			ret |= layout.holder.flag;
		}
		return ret;
	}
	
	public void TextToClipboard(String text, int reason) {
		try {
			ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			if(cm!=null){
				cm.setPrimaryClip(ClipData.newPlainText(null, text));
				if (reason==0) {
					showT("已复制");
				} else {
					showT(text);
				}
			}
		} catch (Exception e) {
			showT(e+"");
		}
	}
	
	Field mObjectsF=null;
	
	/** 天下第一舍我其谁！ */
	public AlertDialog getOptionListDialog(int id
			, int titleId
			, Integer arrayId
			, String titleTail) {
		AlertDialog dialog = (AlertDialog) getReferencedObject(id);
		ListView lv=null;
		boolean bNeedReset = true;
		CharSequence title = "";
		if(dialog!=null) {
			lv = dialog.getListView();
			if(bNeedReset = !arrayId.equals(dialog.tag)) { //相异
				if(lv.getAdapter() instanceof ArrayAdapter) {
					try {
						if(mObjectsF==null) {
							mObjectsF = ArrayAdapter.class.getDeclaredField("mObjects");
							mObjectsF.setAccessible(true);
						}
						mObjectsF.set(lv.getAdapter(), Arrays.asList(mResource.getStringArray(arrayId)));
					} catch (Exception e) {
						//CMN.Log(e);
					}
				}
				lv.setTag(null);
				dialog.tag=null;
				if(mObjectsF==null) {
					dialog=null;
				}
			}
		}
		if(bNeedReset) {
			title = getString(titleId==0?R.string.empty__:titleId);
			if(titleTail!=null) {
				SpannableStringBuilder ssb = new SpannableStringBuilder(title);
				int start = ssb.length();
				ssb.append(titleTail);
				int end=ssb.length();
				ssb.setSpan(new RelativeSizeSpan(0.63f), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				ssb.setSpan(new ClickableSpan() {
					@Override
					public void onClick(@NonNull View v) {
						AlertDialog dialog = (AlertDialog) getReferencedObject(id);
						if(dialog!=null) {
							ListView lv = dialog.getListView();
							lv.getOnItemClickListener().onItemClick(lv, null, -1, -1);
						}
					}
				}, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				title = ssb;
			}
		}
		if(dialog==null) {
			CMN.Log("重建 alert……");
			dialog = new AlertDialog.Builder(this)
					.setSingleChoiceLayout(R.layout.singlechoice_plain)
					.setSingleChoiceItems(arrayId, 0, null)
					.setTitle(title).create();
			dialog.show();
			TextView titleView = dialog.findViewById(R.id.alertTitle);
			if(titleTail!=null) {
				titleView.setMovementMethod(LinkMovementMethod.getInstance());
			}
			if(titleId==0) {
				dialog.findViewById(R.id.topPanel).getLayoutParams().height=titleView.getHeight()/2;
			}
			titleView.setSingleLine(titleId==0);
			putReferencedObject(id, dialog);
		} else {
			if(bNeedReset) {
				dialog.setTitle(title);
			}
			dialog.show();
			// 有时复用后台调出的对话框导致列表项宽度不对，都变小，layout参数正常，width不匹配，则需要调用此。
			postInvalidateLayout(lv); // todo 检查必要性。
		}
		return dialog; // returns the universal dialog
	}
	
	private UniversalWebviewInterface initWebViewImpl(WebFrameLayout layout, UniversalWebviewInterface patWeb, boolean fallBack) {
		UniversalWebviewInterface mWebView = null;
//		if(layout==null&&patWeb!=null) {
//			layout = patWeb.layout;
//			final int luxLimit = 25;
//			int cc = layout.getChildCount();
//			if (cc > luxLimit) {
//				cc = holder==null?0:cc-1;
//				mWebView = (AdvancedBrowserWebView) layout.getChildAt(cc);
//				layout.clearHistroyRequested=true;
//				layout.removeViewAt(cc);
//			}
//		}
		//Utils.sendog(opt);
		View theView;
		if(wvPreInitInstance!=null) {
			if(wvPreInitInstance.getType()==webType.ordinal()) {
				mWebView = wvPreInitInstance;
			}
			wvPreInitInstance = null;
		}
		if(mWebView==null) {
			if(webType==WEBTYPE_TENCENT) {
				try {
					mWebView = new XPlusWebView(this);
				} catch (Exception e) {
					CMN.Log(e);
					if(fallBack) webType=WEBTYPE_SYSTEM;
				}
			} else if(webType==WEBTYPE_INTEL) {
				try {
					mWebView = new XWalkWebView(this);
				} catch (Exception e) {
					CMN.Log(e);
					if(fallBack) webType=WEBTYPE_SYSTEM;
				}
			}
			if(webType==WEBTYPE_SYSTEM) {
				//WebView.setWebContentsDebuggingEnabled(true);
				mWebView = new AdvancedBrowserWebView(this);
//				mWebView.setPictureListener(new WebView.PictureListener() {
//					@Override
//					public void onNewPicture(WebView webView, @Nullable Picture picture) {
//						CMN.Log("onNewPicture!!!");
//					}
//				});
			}
			theView = (View) mWebView;
			if(Utils.bigCake) {
				theView.setOnLongClickListener(mHtmlObjectHandler);
			}
		} else {
			theView = (View) mWebView;
		}
		if(layout==null) {
			return mWebView;
		}

		if(!Utils.bigCake)
		{
			layout.setOnLongClickListener(mHtmlObjectHandler);
		}
		
		TabHolder holder = layout.holder;
		
		//mWebView.clearCache(true);
		//CMN.Log("new mWebView::ua::", mWebView.getSettings().getUserAgentString());
		//Utils.sencat(opt, ViewConfigDefault);
		
		int addIdx = -1;
		if(patWeb!=null) {
//			if(holder!=null) {
//				addIdx=0;
//			} else {
//				holder = patWeb.holder;
//			}
		}
//		else {
//			layout = new WebFrameLayout(getBaseContext());
//			layout.setBackgroundColor(Color.WHITE);
//			layout.setPadding(0, 0, 0, _45_);
//		}
		mWebView.setLayoutParent(layout, false);
		
		layout.appBarLayout = UIData.appbar;
		layout.implView = theView;
		layout.mWebView = mWebView;
		layout.addView(theView, addIdx, new FrameLayout.LayoutParams(-1, -1));
		
		mWebView.addJavascriptInterface(mWebListener.bridge, "polyme");
		mWebView.addJavascriptInterface(holder.TabID, "chrmtd");
		
		layout.setWebViewClient(mWebListener);
		
		theView.setFocusable(true);
		theView.setFocusableInTouchMode(true);
		theView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
		theView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mWebView.setOnScrollChangedListener(mWebListener);
		theView.setBackgroundColor(Color.WHITE);
		
		
		//mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
		//mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE,null);

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
			onLeaveCurrentTab(2);
			Glide.get(this).clearMemory();
			closing=true;
			if(!opt.checkTabsOnPause()) {
				checkTabs();
			}
			Collection<WebFrameLayout> values = id_table.values();
			for (WebFrameLayout wfl:values) {
				wfl.destroy();
			}
			if (Utils.mEqualizer!=null) {
				Utils.mEqualizer.setEnabled(false);
				Utils.mEqualizer = null;
			}
			WebPic.versionMap.clear();
			id_table.clear();
			CMN.Log("关闭历史记录...");
			historyCon.close_for_browser();
			historyCon.try_close();
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
	
	public static void setStatusBarColor(Window window){
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
				case 2021:{
					String url = (String) msg.getData().get("url");
					if (url != null) {
						a.mHtmlObjectHandler.mHitUrl1 = url;
					}
				} break;
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
				case 11576: {
					removeMessages(11576);
					if(a.waveView!=null) {
						a.waveView.setMaxAndProgress(a.syncHandler.progressMax
							,a.syncHandler.progress);
						sendEmptyMessageDelayed(11576, 160);
					}
				} break;
			}
		}
	}
	
	@SuppressWarnings("ALL")
	public static class TabHolder {
		public String url;
		public String title;
		public long url_id;
		public long note_id;
		public String page_search_term;
		public long flag;
		public long rank;
		public long id;
		public final TabIdentifier TabID = new TabIdentifier();
		public boolean closed=true;
		public boolean paused;
		WebFrameLayout viewImpl;
		public long last_visit_time;
		int type;
		public int lastSaveVer;
		public int lastCaptureVer;
		public int version;
		
		@Multiline(flagPos=0, debug=0) public boolean getLuxury(){ flag=flag; throw new RuntimeException(); }
		
		@Multiline(flagPos=6) public boolean getApplyOverride_group_storage(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=6) public void setApplyOverride_group_storage(boolean val){ flag=flag; throw new RuntimeException(); }
		
		@Multiline(flagPos=11) public boolean getApplyOverride_group_client(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=11) public void setApplyOverride_group_client(boolean val){ flag=flag; throw new RuntimeException(); }
		
		@Multiline(flagPos=14) public boolean getApplyOverride_group_scroll(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=14) public void setApplyOverride_group_scroll(boolean val){ flag=flag; throw new RuntimeException(); }
		
		@Multiline(flagPos=21) public boolean getApplyOverride_group_text(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=21) public void setApplyOverride_group_text(boolean val){ flag=flag; throw new RuntimeException(); }
		
		@Multiline(flagPos=33) public boolean getApplyOverride_group_lock(){ flag=flag; throw new RuntimeException(); }
		@Multiline(flagPos=33) public void setApplyOverride_group_lock(boolean val){ flag=flag; throw new RuntimeException(); }
		
		public void close() {
			if (viewImpl != null) {
				viewImpl.stopLoading();
				viewImpl.destroy();
				viewImpl = null;
			}
		}
		
		public void freeze() {
			if (viewImpl != null) {
				viewImpl.pauseWeb();
			}
		}
		
		public void onSalvaged() {
			if (viewImpl != null) {
				viewImpl.onSalvaged();
			}
		}
		
		public void setClosed(boolean val) {
			closed = val;
		}
		
		class TabIdentifier{
			@org.xwalk.core.JavascriptInterface
			@JavascriptInterface
			public long get() {
				return id;
			}
		}
		
		@Override
		public String toString() {
			return "TabHolder{" +
					"url='" + url + '\'' +
					", title='" + title + '\'' +
					", id=" + id +
					", viewImpl=" + viewImpl +
					'}';
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
				UniversalWebviewInterface mWebView = currentWebView;
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
	
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==RequsetUrlFromStorage) {
			downloadDlgHandler.startDownload(false, true);
		}
	}
	
	@Override @SuppressWarnings("ALL")
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		CMN.Log("onActivityResult", requestCode, resultCode, data, data==null?"nullUri":data.getData());
		switch (requestCode){
			case RequsetUrlFromCamera: {
				String text = data==null?null:data.getStringExtra(Intent.EXTRA_TEXT);
				if(Utils.littleCat) {
					checkResumeQRText = true;
				}
				onQRGetText(text);
			} break;
			case RequsetUrlFromStorage: {
				Uri result = data.getData();
				if(result!=null) {
					Uri docUri = DocumentsContract.buildDocumentUriUsingTree(result
							, DocumentsContract.getTreeDocumentId(result));
					String realPath = Utils.getFullPathFromTreeUri(this, docUri);
					if(realPath!=null) {
						showT(realPath);
						if(downloadDlgHandler!=null) {
							downloadDlgHandler.downloadTargetDir = new File(realPath);
						}
					}
				}
			} break;
			case RequsetFileFromFilePicker: {
				if (filePathCallback!=null) {
					Uri[] value = new Uri[]{};
					if (data!=null && data.getData()!=null) {
						value = new Uri[]{data.getData()};
					}
					filePathCallback.onReceiveValue(value);
					filePathCallback = null;
				}
			} break;
			case RequestPDFFile: {
				if (resultCode==RESULT_OK && data!=null)
					startActivity(new Intent(this, PolyShareActivity.class)
						.setDataAndType(data.getData(), "application/pdf"));
			} break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	protected void onQRGetText(String text) {
		if(text!=null) {
			EditFieldHandler editFieldHandler = (EditFieldHandler) getReferencedObject(WeakReferenceHelper.edit_field);
			if(editFieldHandler!=null&&editFieldHandler.visible()) {
				editFieldHandler.setText(text);
			} else {
				webtitle_setVisibility(true);
				execBrowserGoTo(text, true);
			}
		}
	}
	
	@Override
	public void onActionModeStarted(ActionMode mode) {
		View v = getCurrentFocus();
		//CMN.Log("-->onActionModeStarted", v);
		if (!bAdvancedMenu && v!=null && (v==currentWebView || Utils.getNthParentNonNull(v, 2)==currentWebView)) {
			MenuItem.OnMenuItemClickListener listener = this.currentViewImpl;
			mode.setTitle(null);
			mode.setSubtitle(null);
			
			Menu menu = mode.getMenu();
			
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
				item = menu.add(0, item1.getItemId(), ++ToolsOrder, item1.getTitle()).setOnMenuItemClickListener(listener)
						.setIcon(item1.getIcon())
						.setShowAsActionFlags(af);
			}
			if (item2 != null) {
				item = menu.add(0, item2.getItemId(), ++ToolsOrder, item2.getTitle()).setOnMenuItemClickListener(listener)
						.setIcon(item2.getIcon())
						.setShowAsActionFlags(af);
			}
			Drawable hld = mResource.getDrawable(R.drawable.round_corner_dot);
			hld.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_IN);
			menu.add(0, R.id.web_highlight, ++ToolsOrder, "高亮").setOnMenuItemClickListener(listener).setShowAsActionFlags(af).setIcon(hld);
			menu.add(0, R.id.web_tools, ++ToolsOrder, R.string.tools).setOnMenuItemClickListener(listener).setShowAsActionFlags(af).setIcon(R.drawable.ic_tune_black_24dp);
			menu.add(0, R.id.web_tts, ++ToolsOrder, "TTS").setOnMenuItemClickListener(listener).setShowAsActionFlags(af).setIcon(R.drawable.voice_ic_big);
		}
		super.onActionModeStarted(mode);
	}
	
	public boolean checkWebSelection() {
		WebFrameLayout layout = this.currentViewImpl;
		if(layout !=null
				&& layout.hasSelection()
				&& getCurrentFocus() == layout.mWebView.getView()
		){
			layout.implView.clearFocus();
			layout.popupDecorVies.clear();
			if(mWebListener.upsended) {
				currentWebView.evaluateJavascript("igNNC=0", null);
				mWebListener.upsended=false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void showT(Object text) {
		if("downloads".equals(text)) {
			mWebListener.showT("下载完成", "查看", "downloads", true);
		} else if("deleted_refresh".equals(text)) {
			mWebListener.showT("已删除", "刷新", "reload", true);
		} else {
			super.showT(text);
		}
	}
	
	public void animateHomeIcon() {
		ImageView iv = UIData.browserWidget9;
		iv.setImageResource(R.drawable.avd_anim);
		((AnimatedVectorDrawable)iv.getDrawable()).start();
	}
	
	@Override
	public void startActivity(Intent intent) {
		CMN.Log("APP::startActivity::", intent);
		if (intent.getAction()==Intent.ACTION_WEB_SEARCH) {
			String text = intent.getStringExtra(SearchManager.QUERY);
			newTab(execBrowserGoTo(text, false), false, true, -1);
			return;
		}
		super.startActivity(intent);
	}
	
	public long Flag(int flagIndex) {
		switch (flagIndex){
			case WebViewSettingsSource_DOMAIN:
				return currentViewImpl.getDomainFlag();
			case WebViewSettingsSource_TAB:
				return currentViewImpl.holder.flag;
			default:
				return opt.Flag(flagIndex);
		}
	}
	
	public void Flag(int flagIndex, long val) {
		switch (flagIndex){
			case WebViewSettingsSource_DOMAIN:
				currentViewImpl.setDomainFlag(val);
				break;
			case WebViewSettingsSource_TAB:
				currentViewImpl.holder.flag=val;
				break;
			default:
				opt.Flag(flagIndex, val);
				break;
		}
	}
	
	@Override
	public int getDynamicFlagIndex(int flagIndex) {
		return currentViewImpl.getDelegateFlagIndex(flagIndex);
	}
	
	@Override
	public void pickDelegateForSection(int section, int pickIndex) {
		WebFrameLayout layout = currentViewImpl;
		TabHolder holder = layout.holder;
		DomainInfo domainInfo = layout.domainInfo;
		boolean b1=pickIndex!=0;
		boolean b2=pickIndex!=2;
		boolean b3=b1 && !b2;
		switch (section){
			case BackendSettings:
				if (b2) holder.setApplyOverride_group_client(b1);
				domainInfo.setApplyOverride_group_client(b3);
			break;
			case StorageSettings:
				if (b2) holder.setApplyOverride_group_storage(b1);
				domainInfo.setApplyOverride_group_storage(b3);
			break;
			case ImmersiveSettings:
				if (b2) holder.setApplyOverride_group_scroll(b1);
				domainInfo.setApplyOverride_group_scroll(b3);
			break;
			case TextSettings:
				if (b2) holder.setApplyOverride_group_text(b1);
				domainInfo.setApplyOverride_group_text(b3);
			break;
			case LockSettings:
				if (b2) holder.setApplyOverride_group_lock(b1);
				domainInfo.setApplyOverride_group_lock(b3);
			break;
		}
	}
}
