package com.KnaIvER.polymer;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RotateDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.KnaIvER.polymer.Utils.BufferedReader;
import com.KnaIvER.polymer.Utils.CMN;
import com.KnaIvER.polymer.Utils.Options;
import com.KnaIvER.polymer.toolkits.MyX509TrustManager;
import com.KnaIvER.polymer.toolkits.Utils.BU;
import com.KnaIvER.polymer.toolkits.Utils.ReusableByteOutputStream;
import com.KnaIvER.polymer.webslideshow.CenterLinearLayoutManager;
import com.KnaIvER.polymer.webslideshow.RecyclerViewPager;
import com.KnaIvER.polymer.webslideshow.ViewUtils;
import com.KnaIvER.polymer.widgets.AdvancedNestScrollWebView;
import com.KnaIvER.polymer.widgets.PopupBackground;
import com.KnaIvER.polymer.webslideshow.ViewPagerTouchHelper;
import com.KnaIvER.polymer.widgets.SpacesItemDecoration;
import com.KnaIvER.polymer.widgets.Utils;
import com.KnaIvER.polymer.widgets.WebViewmy;
import com.KnaIvER.polymer.widgets.WindowHacker;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import static androidx.appcompat.app.GlobalOptions.realWidth;


public class BrowserActivity extends Toastable_Activity implements View.OnClickListener, View.OnLongClickListener {
	public final static String TitleSep="\n";
	public final static String FieldSep="|";
	public final static Pattern IllegalStorageNameFilter=Pattern.compile("[\r\n|]");
	RecyclerViewPager recyclerView;
	View viewpager_holder;
	ViewGroup webcoord;
	TextView indicator;
	AppBarLayout appbar;
	ViewGroup bottombar2;
	EditText etSearch;
	ViewGroup toolbar;
	private int adapter_idx;
	boolean focused = true;
	private HashMap<Long, AdvancedNestScrollWebView> id_tabel = new HashMap<>(1024);
	ArrayList<AdvancedNestScrollWebView> Pages = new ArrayList<>(1024);
	//private ArrayList<WebViewmy> WebPool = new ArrayList<>(1024);
	private ArrayList<TabHolder> TabHolders = new ArrayList<>(1024);
	private int padWidth;
	private int itemPad;
	private int _45_;
	
	FileOutputStream url_file_recorder;
	
	MyHandler mHandler;
	private AdvancedNestScrollWebView currentWebView;
	
	ArrayList<String> searchEngines = new  ArrayList<>(64);
	private RecyclerView.Adapter adaptermy;
	private boolean viewpager_holder_hasTag;
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
	private WeakReference[] WeakReferencePool = new WeakReference[WeakReferenceHelper.poolSize];
	private View widget10;
	private ColorDrawable AppWhiteDrawable;
	private Drawable frame_web;
	private int mStatusBarH;
	private CoordinatorLayout.Behavior bottombarScrollBehaviour;
	private AppBarLayout.LayoutParams toolbatLP;
	private boolean anioutTopBotForTab=false;
	private CoordinatorLayout.Behavior scrollingBH;
	private boolean fixTopBar = false;
	private boolean fixBotBar = true;
	private View toolbarti;
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		CMN.Log("onConfigurationChanged");
		super.onConfigurationChanged(newConfig);
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		if(recyclerView!=null && !DissmissingViewHolder) {
			calculateItemWidth(true);
		} else {
			bNeedReCalculateItemWidth = true;
		}
	}
	
	@Override
	public void onBackPressed() {
		if(searchEnginePopup!=null && searchEnginePopup.isShowing()) {
			searchEnginePopup.dismiss();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CMN.Log("getting mid 0 ...", Thread.currentThread().getId());
		
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		searchEngines.add("https://www.baidu.com/s?wd=%s");
		
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.activity_main);
		
		frame_web = getDrawable(R.drawable.frame_web);
		root=findViewById(R.id.root);
		findViewById(R.id.browser_widget7).setOnClickListener(this);
		findViewById(R.id.browser_widget8).setOnClickListener(this);
		findViewById(R.id.browser_widget9).setOnClickListener(this);
		widget10 = findViewById(R.id.browser_widget10);
		widget10.setOnClickListener(this);
		widget10.setOnLongClickListener(this);
		findViewById(R.id.browser_widget11).setOnClickListener(this);
		
		indicator=findViewById(R.id.indicator);
		etSearch=findViewById(R.id.etSearch);
		ivBack=findViewById(R.id.ivBack);
		ivDeleteText=findViewById(R.id.ivDeleteText);
		toolbar=findViewById(R.id.toolbar);
		toolbatLP=(AppBarLayout.LayoutParams) toolbar.getLayoutParams();
		appbar=findViewById(R.id.appbar);
		bottombar2=findViewById(R.id.bottombar2);
		bottombarScrollBehaviour = bottombarLP().getBehavior();
		webcoord=findViewById(R.id.webcoord);
		scrollingBH = new AppBarLayout.ScrollingViewBehavior(getBaseContext(), null);
		
		ivBack.setOnClickListener(this);
		ivDeleteText.setOnClickListener(this);
		
		decideTopBotBH();
		
		checkLog(savedInstanceState);
		CrashHandler.getInstance(this, opt).TurnOn();
		setStatusBarColor(getWindow());
		WebView.setWebContentsDebuggingEnabled(true);
		mHandler = new MyHandler(this);
	}
	
	private CoordinatorLayout.LayoutParams appbarLP() {
		return ((CoordinatorLayout.LayoutParams)appbar.getLayoutParams());
	}
	
	private CoordinatorLayout.LayoutParams bottombarLP() {
		return ((CoordinatorLayout.LayoutParams)bottombar2.getLayoutParams());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		focused=false;
		if(currentWebView!=null) {
			currentWebView.saveIfNeeded();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		focused=true;
	}
	
	static class ViewHolder extends RecyclerView.ViewHolder {
		int position;
		ImageView iv;
		View close;
		TextView title;
		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			iv = itemView.findViewById(R.id.iv);
			close = itemView.findViewById(R.id.close);
			title = itemView.findViewById(R.id.title);
			itemView.setTag(this);
			title.setTag(this);
		}
		
		@Override
		public String toString() {
			return ""+position;
		}
	}
	
	@Override
	protected void further_loading(Bundle savedInstanceState) {
		CheckGlideJournal();
		checkMargin(this);
		_45_ = (int) getResources().getDimension(R.dimen._45_);
		WebViewmy.minW = Math.min(512, Math.min(dm.widthPixels, dm.heightPixels));
		
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
			tab0.id=now();
			TabHolders.add(tab0);
		}
		int size = TabHolders.size();
		for (int i = 0; i < size; i++) {
			Pages.add(null);
		}
		CMN.Log("ini.初始化完毕", size);
		AttachWebAt(adapter_idx, false);
		//SetupPasteBin();
		
		etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (currentWebView!=null && actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
					v.clearFocus();
					String text = v.getText().toString();
					int _1stDot = text.indexOf(".");
					boolean hasNecessaryUrlPart = _1stDot>0&&text.contains(":");
					boolean isUrl = false;
					if(hasNecessaryUrlPart) {
						if(text.regionMatches(true, 0, "http", 0, 4)) {
							isUrl = true;
						} else {
							int idx = text.indexOf("/", _1stDot);
							if(idx<0) {
								idx = text.length();
							}
							if(idx>0) {
								int suffix = text.lastIndexOf(".", idx);
								if(suffix>0 && suffix<idx) {
									String topDomain = text.substring(suffix+1, idx);
									CMN.Log("topDomain", topDomain);
								}
							}
						}
					}
					currentWebView.loadUrl(text);
				}
				return false;
			}
		});
		
		systemIntialized=true;
	}
	
	String[] topDomains = new String[]{"com", ""};
	
	
	private long now() {
		return System.currentTimeMillis();
	}
	
	private long validifyid(long id) {
		if(!id_tabel.containsKey(id)) {
			return id;
		}
		int findCound=0;
		while(id_tabel.containsKey(id)) {
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
	
	private void init_tabs_layout() {
		RecyclerViewPager recyclerViewPager = (RecyclerViewPager) findViewById(R.id.viewpager);
		recyclerView = recyclerViewPager;
		recyclerViewPager.setHasFixedSize(true);
		AppWhiteDrawable = new ColorDrawable(AppWhite);
		itemPad = (int) (getResources().getDimension(R.dimen._35_)/5);
		calculateItemWidth(false);
		scrollHereRunnnable = () -> {
			int target = (mItemWidth+2*itemPad)*adapter_idx;
			//recyclerView.requestLayout();
			View C0 = recyclerView.getChildAt(0);
			if(C0!=null){
				int p=((ViewHolder)C0.getTag()).position;
				if(p<0) {
					target-= -C0.getLeft() + itemPad;
				} else {
					target-=(mItemWidth+2*itemPad)*p+padWidth+2*itemPad-C0.getLeft() + itemPad;
				}
			}
			//CMN.Log("首发", C0==null, target);
			recyclerView.scrollBy(target,0);
			layoutManager.targetPos = ViewUtils.getCenterXChildPosition(recyclerView);
		};
		recyclerViewPager.setFlingFactor(0.175f);
		recyclerViewPager.setTriggerOffset(0.125f);
		
		adaptermy = new RecyclerView.Adapter<ViewHolder>() {
			public int getItemCount() { return 2+Pages.size(); }
			@NonNull
			@Override
			public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				ViewHolder vh = new ViewHolder(getLayoutInflater().inflate(R.layout.web_page_item, parent, false));
				vh.itemView.setOnClickListener(ocl);
				vh.title.setOnClickListener(ocl);
				vh.close.setOnClickListener(ocl);
				CMN.Log("onCreateViewHolder");
				return vh;
			}
			
			@Override
			public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
				ImageView iv = vh.iv;
				position-=1;
				vh.position=position;
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					vh.itemView.setForeground(null);
				}
				//iv.setImageBitmap(Pages.get(position).getBitmap());
				iv.getLayoutParams().height=ViewGroup.LayoutParams.MATCH_PARENT;
				if(targetIsPage(position)) {
					Bitmap bitmap=null;
					WebViewmy mWebView = Pages.get(position);
					TabHolder holder = TabHolders.get(position);
					iv.setTag(R.id.home, false);
					vh.itemView.getLayoutParams().width=mItemWidth;
					vh.itemView.setVisibility(View.VISIBLE);
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
					vh.itemView.getLayoutParams().width=padWidth;
					vh.itemView.setVisibility(View.INVISIBLE);
					//vh.itemView.getLayoutParams().width=mItemWidth;
				}
			}
			
			public long getItemId(int position) { return position; }
			
			View.OnClickListener ocl = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v.getId()==R.id.close) {
						ViewHolder vh = (ViewHolder) ((ViewGroup)v.getParent().getParent()).getTag();
						int target = vh.position;
						closeTabAt(target, 0);
					} else {
						ViewHolder vh = (ViewHolder) v.getTag();
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
		
		ItemTouchHelper.Callback callback=new ViewPagerTouchHelper(new ViewPagerTouchHelper.ItemTouchHelperCallback<ViewHolder>() {
			@Override
			public void onItemSwipe(ViewHolder vh) {
				closeTabAt(vh.position, 1);
			}
			
			@Override
			public void onItemSlide(ViewHolder fromPosition, int toPosition) {
				CMN.Log("onMove");
			}
		});
		ItemTouchHelper itemTouchHelper=new ItemTouchHelper(callback);
		itemTouchHelper.attachToRecyclerView(recyclerViewPager);
		
		viewpager_holder=findViewById(R.id.viewpager_holder);
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
			id_tabel.remove(tab.id);
			AdvancedNestScrollWebView webview = Pages.remove(positon);
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
		Glide glide = Glide.glide;
		if(glide!=null) {
			CMN.Log("trimMemory");
			glide.trimMemory(level);
			if(focused) {
				glide.clearMemory();
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
		int pad = (root.getHeight() - mItemHeight - bottombar2.getHeight())/2;
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
		boolean post = false;
		if(viewpager_holder==null) {
			init_tabs_layout();
			post=true;
		} else if(bNeedReCalculateItemWidth && DissmissingViewHolder) {
			calculateItemWidth(false);
			bNeedReCalculateItemWidth = false;
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
			alpha1 = ObjectAnimator.ofInt(bottombar2.getBackground().mutate(), "alpha", 1, 0);
			alpha3 = ObjectAnimator.ofFloat(appbar, "alpha", 0, 1);
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
						appbar.setAlpha(1);
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
			animatorObj.setDuration(220);
			tabsAnimator = animatorObj;
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
			appbar.setExpanded(DissmissingViewHolder, true);
		}
		tabsAnimator.start();
	};
	private void startAnimation(boolean post) {
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
		boolean bNeedPost = false;
		float ttY=1, ttX=1, targetScale=mItemWidth;
		boolean bc=fromClick>=0;
		if(true||DissmissingViewHolder||bc) {
			int W = root.getWidth();
			if(W==0) W=dm.widthPixels;
			
			int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
			mStatusBarH = getResources().getDimensionPixelSize(resourceId);
			int H = root.getHeight()-mStatusBarH;
			if(H<=0) H=dm.heightPixels;
			
			targetScale /= W;
			
			ttX=(W-W*targetScale)/2;
			
			ttY=(appbar.getHeight()+bottombar2.getHeight())/2-appbar.getTop();
			
			//ttY=((H/*-bottombar2.getHeight()*/)-(H-appbar.getHeight())*targetScale)/2-appbar.getHeight()-appbar.getTop();
			
			float titleH = getResources().getDimension(R.dimen._35_);
			
			ttY=(currentWebView.layout.getHeight()*(1-targetScale))/2+(+titleH-viewpager_holder.getPaddingBottom())/2-appbar.getHeight()-appbar.getTop();
			
			ttY=-((currentWebView.getHeight()*targetScale)/2-appbar.getHeight()/2)+(root.getHeight()+titleH-viewpager_holder.getPaddingBottom())/2;
			
			ttY=(H+titleH-viewpager_holder.getPaddingBottom()-currentWebView.getHeight()*targetScale)/2-10;
			
			if(!anioutTopBotForTab) {
				ttY+=-appbar.getHeight()-appbar.getTop();
			}
			
			CMN.Log(H, mStatusBarH, bottombar2.getHeight(), "appbar.h="+appbar.getHeight(), "appbar.top="+appbar.getTop(), "titleH="+titleH, "bottom.h="+appbar.getTop(), "idealItemHeight2="+currentWebView.getHeight()*targetScale);
			
			
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
				added = TabHolders.get(targetPos)!=null;
				added = AttachWebAt(targetPos, true);
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
		if(!DissmissingViewHolder){// 放
			alpha1.setIntValues(bottombar2.getBackground().getAlpha(), 255);
			appbar.setVisibility(View.VISIBLE);
			alpha3.setFloatValues(appbar.getAlpha(), 1);
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
			alpha1.setIntValues(bottombar2.getBackground().getAlpha(), 128);
			alpha3.setFloatValues(appbar.getAlpha(), 0);
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
		if(fromClick==-1) {
			b = v.getBackground();
			if(b!=null) {
				b.jumpToCurrentState();
			}
			v.setBackground(null);
		}
		startAnimation(b!=null&&false||bNeedPost);
	}
	
	
	private boolean AttachWebAt(int i, boolean pseudoAdd) {
		int size = TabHolders.size()-1; // sanity check
		if(size<0) return pseudoAdd;
		if(i<0) i=0;
		if(i>size) i=size;
		AdvancedNestScrollWebView mWebView = Pages.get(i);
		if(mWebView==null) {
			TabHolder holder = TabHolders.get(i);
			mWebView = id_tabel.get(holder.id);
			if(mWebView==null) {
				Utils.sendog(opt);
				mWebView = new AdvancedNestScrollWebView(getBaseContext());
				Utils.sencat(opt, ViewConfigDefault);
				
				mWebView.setBackgroundColor(Color.WHITE);
				FrameLayout layout = new FrameLayout(getBaseContext());
				layout.addView(mWebView, new FrameLayout.LayoutParams(-1, -1));
				mWebView.layout = layout;
				layout.setBackgroundColor(Color.TRANSPARENT);
				
				mWebView.holder = holder;

//				mWebView.getSettings().setAppCacheEnabled(false);
//				mWebView.getSettings().setDatabaseEnabled(false);
//				mWebView.getSettings().setDomStorageEnabled(false);
				
				
				mWebView.setFocusable(true);
				mWebView.setFocusableInTouchMode(true);
				id_tabel.put(holder.id, mWebView);
				//mWebView.setNestedScrollingEnabled(false);
			}
			Pages.set(i, mWebView);
		}
		if(mWebView.loadIfNeeded()){
			viewpager_holder_hasTag = true;
		}
		mWebView.setNestedScrollingEnabled(true);
		if(mWebView.getUrl().contains("p_2")) {
			mWebView.setNestedScrollingEnabled(false);
		}
		mWebView.layout.setVisibility(View.VISIBLE);
		mWebView.layout.setScaleX(1);
		mWebView.layout.setScaleY(1);
		mWebView.setWebViewClient(mWebClient);
		mWebView.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				CMN.Log("DOWNLOAD:::", url, contentDisposition, mimetype, contentLength);
				showDownloadDialog(url);
			}
		});
		while (webcoord.getChildAt(1) instanceof FrameLayout){
			webcoord.removeViewAt(1);
		}
		boolean add = true;
		View weblayout = mWebView.layout;
		ViewGroup svp = (ViewGroup) weblayout.getParent();
		if(svp!=null) { // sanity check
			if(add = svp!=webcoord) {
				svp.removeView(weblayout);
			}
		}
		if(add) {
			webcoord.addView(weblayout, 1);
		}
		currentWebView=mWebView;
		decideWebviewPadding();
		adapter_idx=i;
		etSearch.setText(mWebView.holder.url);
		indicator.setText(""+i);
		return add;
	}
	
	private void decideTopBotBH() {
		bottombarLP().setBehavior(fixBotBar?null:bottombarScrollBehaviour);
		ViewGroup svp = (ViewGroup) toolbar.getParent();
		if(svp==webcoord ^ fixTopBar) {
			svp.removeView(toolbar);
			(fixTopBar?webcoord:appbar).addView(toolbar, fixTopBar?1:0);
			if(fixTopBar) {
				toolbarti = new View(this);
				toolbarti.setLayoutParams(toolbatLP);
				appbar.addView(toolbarti);
			} else {
				toolbar.setLayoutParams(toolbatLP);
				appbar.removeView(toolbarti);
			}
		}
	}
	
	private void decideWebviewPadding() {
		View weblayout = currentWebView.layout;
		CoordinatorLayout.LayoutParams lp = ((CoordinatorLayout.LayoutParams) weblayout.getLayoutParams());
		ViewGroup.MarginLayoutParams lpw = ((ViewGroup.MarginLayoutParams) currentWebView.getLayoutParams());
		lp.height= ViewGroup.LayoutParams.MATCH_PARENT;
		
		int appbar_height = (int) getResources().getDimension(R.dimen._45_);
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
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.ivBack:{ // 搜索引擎弹窗 //searpop
				int polypopupW = (int) (_45_*1.5);
				int searchEnginePopupW = (int) (etSearch.getWidth()*0.85);
				searchEnginePopupW = Math.min(searchEnginePopupW, (int)Math.max(realWidth, 550*GlobalOptions.density));
				if(searchEnginePopup==null) {
					layoutListener = findViewById(R.id.layoutListener);
					searchEnginePopup = new ListPopupWindow(BrowserActivity.this);
					searchEnginePopup.setAdapter(new ArrayAdapter<>(BrowserActivity.this,
							R.layout.abc_list_menu_item_layout, R.id.title, searchEngines));
					searchEnginePopup.setAnchorView(findViewById(R.id.popline));
					//searchEnginePopup.setOverlapAnchor(true); //21 禁开
					searchEnginePopup.setDropDownAlwaysVisible(true);
					searchEnginePopup.setOnDismissListener(() -> {
						layoutListener.setVisibility(View.GONE);
						layoutListener.popup=null;
						polypopup.dismiss();
					});
					//searchEnginePopup.mPopup.setEnterTransition(null);
					
					polypopupview = LayoutInflater.from(this).inflate(R.layout.polymer, null);
					polypopupview.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							polysearching=!polysearching;
							polypopupview.setAlpha(polysearching?1:0.25f);
							showT(polysearching?"聚合搜索":"已关闭");
							//m_currentToast.setGravity(Gravity.TOP, 0, appbar.getHeight());
						}
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
					int iconWidth = ivBack.getWidth();
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
					polypopup.showAsDropDown(ivBack,iconWidth+((etSearch_getWidth+searchEnginePopupW+iconWidth)-(polypopupW))/2, -polypopupW/2);
				}
				//searchEnginePopup.show();
			} break;
			case R.id.ivDeleteText:
				final Dialog dlg = new Dialog(this);
				dlg.setCanceledOnTouchOutside(true);
				
				dlg.show();
				
				Window window = dlg.getWindow();
				window.setWindowAnimations(R.style.dialog);
				FrameLayout popuproot = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.main_menu, null);
				
				window.setContentView(popuproot);
				window.setBackgroundDrawable(null);
				//设置alterdialog全屏
				WindowManager.LayoutParams lp = window.getAttributes();
				lp.height = -1;
				lp.width = -1;
				window.setDimAmount(0);
				window.setAttributes(lp);
				
				setStatusBarColor(window);
				
				int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
				int height = getResources().getDimensionPixelSize(resourceId);
				popuproot.setPadding(0,height,0,0);
				popuproot.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
				View.OnClickListener clicker = new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean dissmiss=true;
						switch (v.getId()) {
							default:
								dissmiss=false;
								break;
							case R.id.dismiss:
								break;
							case R.id.copy:
								widget10.setTag(currentWebView);
							case R.id.new_folder:
								onLongClick(widget10);
								break;
							case R.id.reinit:
								CookieManager.getInstance().setAcceptCookie(false);
								break;
							case R.id.refresh:
								currentWebView.reload();
								break;
							case R.id.print:
								String name = currentWebView.getTitle()+".pdf";
								PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
								printManager.print("Print", currentWebView.createPrintDocumentAdapter(name)
										, new PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_COLOR).build());
								break;
							case R.id.offline:
								currentWebView.saveWebArchive(new File(getExternalFilesDir(null), currentWebView.getTitle()+".mht").getPath(), false, new ValueCallback<String>() {
									@Override
									public void onReceiveValue(String value) {
										showT(value);
									}
								});
								break;
						}
						if(dissmiss) {
							dlg.dismiss();
						}
					}
				};
				setOnClickListenersOneDepth((ViewGroup) popuproot.getChildAt(1), clicker, true);
				popuproot.getChildAt(0).setOnClickListener(clicker);
				
				//popuproot.setBackgroundColor(Color.GREEN);
				RotateDrawable main_progress_bar_d = (RotateDrawable) ((LayerDrawable)popuproot.findViewById(R.id.main_progress_bar).getBackground()).findDrawableByLayerId(android.R.id.progress);
				//main_progress_bar_d.setLevel((int) (.5 * 10000));
				ObjectAnimator animator = ObjectAnimator.ofInt(main_progress_bar_d, "level", 0,1);
				animator.setIntValues(0, 10000);
				animator.setDuration(1080);
				animator.start();
				CMN.Log("对话 对话");
				break;
			case R.id.browser_widget7:
				if(currentWebView.canGoBack()) {
					currentWebView.goBack();
					
				}
				break;
			case R.id.browser_widget8:
//				if(currentWebView.canGoForward()) {
//					currentWebView.goForward();
//				}
				fixBotBar = !fixBotBar;
				decideTopBotBH();
				decideWebviewPadding();
				showT("fixBotBar : "+fixBotBar);
				break;
			case R.id.browser_widget9:
				//showDownloadDialog(null);
				fixTopBar = !fixTopBar;
				decideTopBotBH();
				decideWebviewPadding();
				showT("fixTopBar : "+fixTopBar);
			break;
			case R.id.browser_widget11:
				appbar.setExpanded(false, true);
			break;
			case R.id.browser_widget10:
				onLeaveCurrentTab(0);
				toggleTabView(-1, v);
			break;
		}
	}
	
	public void setOnClickListenersOneDepth(ViewGroup vg, View.OnClickListener clicker, boolean digin) {
		int cc = vg.getChildCount();
		View ca;
		for (int i = 0; i < cc; i++) {
			ca = vg.getChildAt(i);
			if(ca instanceof ViewGroup) {
				if(digin) {
					setOnClickListenersOneDepth((ViewGroup) ca, clicker, false);
				}
			} else {
				ca.setOnClickListener(clicker);
			}
		}
	}
	
	public void showDownloadDialog(String url) {
		//showT(text);
		int id = WeakReferenceHelper.bottom_download_dialog;
		BottomSheetDialog bottomPlaylist = (BottomSheetDialog) getReferencedObject(id);
		if(bottomPlaylist==null) {
			CMN.Log("重建底部弹出");
			putReferencedObject(id, bottomPlaylist = new BottomSheetDialog(this));
			View ll = LayoutInflater.from(this).inflate(R.layout.download_bottom_sheet, null);
			
			BottomSheetDialog final_bottomPlaylist = bottomPlaylist;
			View.OnClickListener clicker = v -> {
				switch (v.getId()) {
					case R.id.cancel:
						final_bottomPlaylist.dismiss();
						break;
				}
			};
			AppCompatEditText dir_path = ll.findViewById(R.id.dir_path);
			ImageView logo = ll.findViewById(R.id.logo);
			TextView download = ll.findViewById(R.id.download);
			ll.findViewById(R.id.cancel).setOnClickListener(clicker);
			//ll.findViewById(R.id.confirm).setOnClickListener(clicker);
			ll.findViewById(R.id.new_folder).setOnClickListener(clicker);
			bottomPlaylist.setContentView(ll);
			Window win = bottomPlaylist.getWindow();
			win.setDimAmount(0.2f);
			win.findViewById(R.id.design_bottom_sheet).setBackground(null);
			logo.setOnClickListener(v -> {
				if(win.getCurrentFocus()==dir_path) {
					dir_path.onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(0,0,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BACK,0,0,0,0,0,0));
				}
			});
			//fix_full_screen(bottomPlaylist.getWindow().getDecorView());
			
			//_bottomPlaylist.getWindow().getDecorView().setTag(lv);
			
			bottomPlaylist.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);// 展开
			if(GlobalOptions.isDark){
				ll.setBackgroundColor(Color.BLACK);
				((TextView)ll.findViewById(R.id.download)).setTextColor(Color.WHITE);
				download.setTextColor(Color.WHITE);
			}
			View decor = win.getDecorView();
			Object[] values = new Object[]{dir_path, null};
			decor.setTag(values);
			download.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String url = (String) values[1];
					showT("下载中…"+url);
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
					long id = downloadManager.enqueue(request);
					
				}
			});
			//if(false)
			decor.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
					-> v.postDelayed(() -> {
				((ViewGroup)v).getChildAt(0).setTranslationY(Utils.isKeyboardShown(v)?(int) (-50*GlobalOptions.density):0);
				CMN.Log("addOnLayoutChangeListener", Utils.isKeyboardShown(v));
			}, 0));
		}
//		View v = (View) _bottomPlaylist.getWindow().getDecorView().getTag();
//		DisplayMetrics dm2 = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getRealMetrics(dm2);
//		v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * _bottomPlaylist.getBehavior().getHalfExpandedRatio() - getResources().getDimension(R.dimen._45_) * 1.75);
//		v.requestLayout();
		Object[] views = (Object[]) bottomPlaylist.getWindow().getDecorView().getTag();
		((TextView)views[0]).setText(url);
		views[1]=url;
		bottomPlaylist.show();
		
		
	}
	
	private Object getReferencedObject(int id) {
		if(WeakReferencePool[id] == null) {
			return null;
		}
		return WeakReferencePool[id].get();
	}
	
	private void putReferencedObject(int id, Object object) {
		WeakReferencePool[id] = new WeakReference(object);
	}
	
	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()){
			// 新建标签页
			case R.id.browser_widget10:{
				try {
					long id = validifyid(now());
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
	
	public final static Pattern requestPattern = Pattern.compile("\\?.*$");
	public final static Pattern httpPattern = Pattern.compile("(https?://.*)", Pattern.CASE_INSENSITIVE);
	public final static Pattern timePattern = Pattern.compile("t=([0-9]{8,15})", Pattern.CASE_INSENSITIVE);
	
	/**var items = document.head.getElementsByTagName('meta');
	 var len=items.length;
	 var tag = 'viewport';
	 var content = 'width=device-width,minimum-scale=1.0,maximum-scale=5.0,user-scalable=yes';
	 var add=1;
	 for(var i=0;i<len;i++) {
		 var iI = items[i];
		 if(iI.name===tag) {
			 iI.content=content;
			 add=0;
		 }
	 }
	 if(add){
		 var item = document.createElement('meta');
		 item.name = tag;
		 item.content = content;
		 document.head.appendChild(item);
	 }
	 */
	@Multiline
	final static String forceZoom = StringUtils.EMPTY;
	
	WebViewClient mWebClient=new WebViewClient() {
		@Override public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			CMN.Log("OPF:::", url);
			view.setTag(url);
			AdvancedNestScrollWebView mWebView=((AdvancedNestScrollWebView)view);
			mWebView.holder.url=url;
			mWebView.holder.title=mWebView.getTitle();
			mWebView.time=System.currentTimeMillis();
			mWebView.lastScroll=view.getScrollY();
			//mWebView.evaluateJavascript(forceZoom, null);
			if(mWebView.layout.getParent()!=null){
				etSearch.setHint(mWebView.holder.title);
			}
		}
		
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}
		
		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
//			int start = 0;
//			int len = url.length();
//			while(start<len && url.charAt(start) <= ' ') {
//				start++;
//			}
//			if(start<len) {
//				len = url.indexOf("://", start) - start;
//				switch (len) {
//					case 3:
//						if(url.regionMatches(true, start, "ftp", 0, 5))
//					break;
//					case 4:
//						if(url.regionMatches(true, start, "http", 0, 4)||url.regionMatches(true, start, "file", 0, 4))
//					break;
//					case 5:
//						if(url.regionMatches(true, start, "https", 0, 5))
//					break;
//				}
//			}
			
			((WebViewmy)view).loaded=true;
			CMN.Log("shouldOverrideUrl --->", url);
			//view.loadUrl(url);
			return false;
		}
		
		
		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);
			WebViewmy mWebView=((WebViewmy)view);
//			if(url.contains(".mp4")){
//				Message msg = new Message();
//				msg.what=110;
//				msg.obj=url;
//				mHandler.sendMessage(msg);
//			}
		}
		
		@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
		@Nullable
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
			//CMN.Log("shouldInterceptRequest", request.getUrl());
			WebViewmy mWebView=((WebViewmy)view);
			String url=request.getUrl().toString();
			
			if(url.startsWith("pp:")) {
				CMN.Log("pp://", url);
				try {
					if(url.contains(".js")) {
						return new WebResourceResponse("text/javascript","utf8", getAssets().open("3"));
					} else {
						return new WebResourceResponse("text/html","utf8", getAssets().open("2"));
					}
				} catch (IOException e) {
					CMN.Log(e);
				}
			}
			//CMN.Log("request.getUrl().getScheme()", request.getUrl().getScheme());
			if("mdbr".equals(request.getUrl().getScheme())) {
				return new WebResourceResponse("text/javascript","utf8", new ByteArrayInputStream(WebViewmy.commonIcanBytes));
			}
			if(true) return null;
			if(false) {
				if(request.getUrl().toString().contains("?mid=")){
					Map<String, String> keyset = request.getRequestHeaders();
					for (String key:keyset.keySet()) {
						CMN.Log("keyset : ", key, " :: ",keyset.get(key));
					}
					
				}
				return null;
			}
			if(url.contains(".mp4")){
				if(false){
					Map<String, String> keyset = request.getRequestHeaders();
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
						mHandler.sendMessage(msg);
						if(response!=null)
							return response;
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				Map<String, String> keyset = request.getRequestHeaders();
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
							if(url_file_recorder==null) url_file_recorder = new FileOutputStream("/sdcard/file-url-list.txt", true);
							url_file_recorder.write((mWebView.holder.url+TitleSep+path.getName()).getBytes());
							url_file_recorder.flush();
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
					mHandler.sendMessage(msg);
					if(response!=null)
						return response;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			return super.shouldInterceptRequest(view, url);
		}
	};
	
	WebChromeClient mChromeWebClient=new WebChromeClient(){
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			CMN.Log("onProgressChanged");
			viewpager_holder_hasTag=true;
			super.onProgressChanged(view, newProgress);
		}
	};
	
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
	
	public static class TabHolder {
		public String url;
		public String title;
		public String page_search_term;
		public long flag;
		public long id;
	}
}
