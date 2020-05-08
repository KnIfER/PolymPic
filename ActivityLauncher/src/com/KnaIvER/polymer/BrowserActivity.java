package com.KnaIvER.polymer;


import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.KnaIvER.polymer.Utils.CMN;
import com.KnaIvER.polymer.Utils.Options;
import com.KnaIvER.polymer.toolkits.MyX509TrustManager;
import com.KnaIvER.polymer.toolkits.Utils.BU;
import com.KnaIvER.polymer.toolkits.Utils.ReusableByteOutputStream;
import com.KnaIvER.polymer.webslideshow.WebPic;
import com.KnaIvER.polymer.widgets.AdvancedNestScrollWebView;
import com.KnaIvER.polymer.widgets.SpacesItemDecoration;
import com.KnaIvER.polymer.widgets.WebViewmy;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;

import java.io.BufferedReader;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;


public class BrowserActivity extends Toastable_Activity implements View.OnClickListener, View.OnLongClickListener {
	public static String TitleSep="\\|tTt\\|";
	RecyclerView recyclerView;
	View viewpager_holder;
	ViewGroup webcoord;
	TextView indicator;
	ViewGroup appbar;
	EditText etSearch;
	ViewGroup toolbar;
	ArrayList<WebViewmy> Pages = new ArrayList<>();
	private int adapter_idx;
	private WebViewmy[] WebPool;
	int PoolVagranter=0;
	private int itemWidth;
	private int padWidth;
	private int itemPad;
	private int minW;
	private int _45_;

	FileOutputStream url_file_recorder;

	MyHandler mHandler;
	private WebViewmy currentWebView;

	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		recyclerView.getLayoutParams().height=getResources().getDisplayMetrics().heightPixels/2+_45_;
		recyclerView.getAdapter().notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		root=findViewById(R.id.root);
		findViewById(R.id.browser_widget7).setOnClickListener(this);
		findViewById(R.id.browser_widget8).setOnClickListener(this);
		findViewById(R.id.browser_widget9).setOnClickListener(this);
		findViewById(R.id.browser_widget10).setOnClickListener(this);
		findViewById(R.id.browser_widget10).setOnLongClickListener(this);
		findViewById(R.id.browser_widget11).setOnClickListener(this);

		indicator=findViewById(R.id.indicator);
		etSearch=findViewById(R.id.etSearch);
		toolbar=findViewById(R.id.toolbar);
		appbar=findViewById(R.id.appbar);
		webcoord=findViewById(R.id.webcoord);
		viewpager_holder=findViewById(R.id.viewpager_holder);
		recyclerView =findViewById(R.id.viewpager);

		checkLog(savedInstanceState);
		CrashHandler.getInstance(this, opt).TurnOn();
		setStatusBarColor();
		WebView.setWebContentsDebuggingEnabled(true);
		mHandler = new MyHandler(this);
	}

	static class ViewHolder extends RecyclerView.ViewHolder{
		int position;
		ImageView iv;
		View close;
		TextView title;
		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			iv = itemView.findViewById(R.id.iv);
			iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			close = itemView.findViewById(R.id.close);
			title = itemView.findViewById(R.id.title);
		}
	}

	@Override
	protected void further_loading(Bundle savedInstanceState) {
		CheckGlideJournal();
		checkMargin(this);
		_45_ = (int) getResources().getDimension(R.dimen._45_);
		minW = Math.min(512, Math.min(dm.widthPixels, dm.heightPixels));
		recyclerView.setAdapter(new RecyclerView.Adapter() {
			@NonNull
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				ViewHolder vh = new ViewHolder(getLayoutInflater().inflate(R.layout.web_page_item, parent, false));
				vh.itemView.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
				vh.itemView.setOnClickListener(ocl);
				return vh;
			}

			View.OnClickListener ocl = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewHolder vh = (ViewHolder) v.getTag();
					int target = vh.position;
					if(targetIsPage(target)){
						recyclerView.smoothScrollToPosition(target);
						toggleTabView();
						AttachWebAt(target);
						View C0 = recyclerView.getChildAt(0);
						if(C0!=null){
							int p=((ViewHolder)C0.getTag()).position;
							target= itemWidth*adapter_idx
									-(p*itemWidth-(p<0?0:C0.getLeft()));
							recyclerView.smoothScrollBy(target,0);
						}
					}
				}
			};

			@Override
			public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
				ViewHolder vh = (ViewHolder) holder;
				ImageView iv = vh.iv;
				position-=1;
				vh.position=position;
				holder.itemView.setTag(holder);
				//iv.setImageBitmap(Pages.get(position).getBitmap());
				int mItemWidth = Math.min(getResources().getDisplayMetrics().heightPixels, getResources().getDisplayMetrics().widthPixels) / 2;
				if(mItemWidth!=itemWidth){
					invalidPagePadding(mItemWidth);
				}
				iv.getLayoutParams().height=ViewGroup.LayoutParams.MATCH_PARENT;
				if(targetIsPage(position)){
					WebViewmy mWebView = Pages.get(position);
					iv.setTag(R.id.home, false);
					vh.itemView.getLayoutParams().width=mItemWidth;
					vh.itemView.setVisibility(View.VISIBLE);
					String title=mWebView.title;
					if(title==null||title.equals("")) title=mWebView.url;
					vh.title.setText(title);
					Glide.with(getBaseContext())
							.asBitmap()
							.load(new WebPic(mWebView))
							.override(minW, Target.SIZE_ORIGINAL)
							//.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							.fitCenter()
							//.skipMemoryCache(true)
							//.diskCacheStrategy(DiskCacheStrategy.NONE)
							.listener(new RequestListener<Bitmap>() {
								@Override
								public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
									ImageView medium_thumbnail = ((ImageViewTarget<?>) target).getView();
									medium_thumbnail.setImageDrawable(getResources().getDrawable(R.drawable.sky_background));
									return true;
								}

								@Override
								public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
									ImageView medium_thumbnail = ((ImageViewTarget<?>) target).getView();
									if(medium_thumbnail.getTag(R.id.home)==null){//true ||
										return true;
									}
									return false;
								}
							})
							.into(iv);
				}
				else{
					iv.setTag(R.id.home, null);
					iv.setImageBitmap(null);
					vh.itemView.setVisibility(View.INVISIBLE);
					vh.itemView.getLayoutParams().width=padWidth;
				}
			}

			@Override
			public int getItemCount() {
				return 2+Pages.size();
			}
		});
		LinearLayoutManager layoutManager=new LinearLayoutManager(this);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		recyclerView.setLayoutManager(layoutManager);
		itemPad = (int) (getResources().getDimension(R.dimen._35_)/5);
		recyclerView.addItemDecoration(new SpacesItemDecoration(itemPad));
		recyclerView.getLayoutParams().height=getResources().getDisplayMetrics().heightPixels/2;
		etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (currentWebView!=null && actionId== EditorInfo.IME_ACTION_SEARCH||actionId==EditorInfo.IME_ACTION_UNSPECIFIED){
					v.clearFocus();
					currentWebView.loadUrl(v.getText().toString());
				}
				return false;
			}
		});
		root.removeView(viewpager_holder);
		root.post(() -> invalidPagePadding( Math.min(getResources().getDisplayMetrics().heightPixels, getResources().getDisplayMetrics().widthPixels) / 2));
		//LinearSnapHelper helper = new LinearSnapHelper();
		//helper.attachToRecyclerView(recyclerView);

		int  WebPoolSize = 128;
		WebPool = new WebViewmy[WebPoolSize];
		final File def = new File(getExternalFilesDir(null),"sites_default.txt");
		/* !!!原配 */
		if(def.exists()){
			try {
				BufferedReader in = new BufferedReader(new FileReader(def));
				String line;
				while((line=in.readLine())!=null){
					//CMN.Log("processing...", line);
					int end=line.length()-1;
					boolean via = line.endsWith("|=");
					if(via) end-=2;
					if(end>0){
						int idx = line.lastIndexOf("|", end);
						if(idx>0){
							try {
								int id = Integer.parseInt(line.substring(idx+1,end+1));
								//CMN.Log("occupying pool at...", id);
								end=idx-1;
								try {
									idx = line.lastIndexOf("|", end);
									long time = Long.parseLong(line.substring(idx+1,end+1));
									if(id>=0 && id<WebPoolSize && WebPool[id]==null){
										WebViewmy mWebView = new AdvancedNestScrollWebView(getBaseContext());
										int idx_title = line.lastIndexOf(TitleSep, idx);
										if(idx_title!=-1){
											mWebView.title=line.substring(idx_title+TitleSep.length(), idx);
											idx=idx_title;
										}
										mWebView.url=line.substring(0, idx);
										mWebView.time=time;
										mWebView.SelfIdx=id;
										WebPool[id]=mWebView;
										Pages.add(mWebView);
										if(via) adapter_idx=Pages.size()-1;
										//CMN.Log("occupied ...", id);
										//Todo summon the lost
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		/* !!!原装 */
		if(Pages.size()==0){
			WebViewmy mWebView = new AdvancedNestScrollWebView(getBaseContext());
			mWebView.url="https://www.bing.com";
			Pages.add(mWebView);
			WebPool[0]=mWebView;
		}
		CMN.Log("初始化完毕", Pages.size());
		AttachWebAt(adapter_idx);
		SetupPasteBin();
		systemIntialized=true;
	}

	private boolean targetIsPage(int target) {
		return target>=0 && target<Pages.size();
	}

	private void invalidPagePadding(int mItemWidth) {
		//CMN.Log("invalidPagePadding", root.getWidth());
		itemWidth=mItemWidth;
		padWidth = (root.getWidth()-itemWidth)/2;
		if(padWidth<0) padWidth=0;
	}

	/** 列表化/窗口化标签管理 */
	private void toggleTabView() {
		if(viewpager_holder.getParent()!=null){
			root.removeView(viewpager_holder);
		}else{
			root.addView(viewpager_holder);
			if(recyclerView.getTag(R.id.home)==null) {
				recyclerView.scrollToPosition(adapter_idx + 1);
				recyclerView.setTag(R.id.home, false);
			}
			recyclerView.post(() -> {
				int target = (itemWidth+2*itemPad) * adapter_idx-padWidth;
				View C0 = recyclerView.getChildAt(0);
				if(C0!=null){
					int p=((ViewHolder)C0.getTag()).position;
					if(p<0) p=0;
					target-=p*(itemWidth+2*itemPad)-C0.getLeft()-itemPad;
					//target-=recyclerView.getScrollY();
					recyclerView.scrollBy(target,0);
				}
			});
			if(viewpager_holder.getTag()!=null){
				recyclerView.getAdapter().notifyDataSetChanged();
				viewpager_holder.setTag(null);
			}else{
				boolean b1=Options.getAlwaysRefreshThumbnail();
				if(b1){
					currentWebView.time = System.currentTimeMillis();
				}
				recyclerView.getAdapter().notifyItemChanged(adapter_idx+1, b1?null:false);
			}
		}
	}

	private void AttachWebAt(int i) {
		WebViewmy mWebView = Pages.get(i);
		if(mWebView.loadIfNeeded()){
			viewpager_holder.setTag(false);
		}
		mWebView.setWebViewClient(mWebClient);
		while (webcoord.getChildAt(1) instanceof WebView){
			webcoord.removeView(webcoord.getChildAt(1));
		}
		if(mWebView.getParent()!=null)// sanity check
			((ViewGroup)webcoord.getParent()).removeView(mWebView);
		webcoord.addView(mWebView, 1);
		((CoordinatorLayout.LayoutParams)mWebView.getLayoutParams()).height= ViewGroup.LayoutParams.MATCH_PARENT;
		((CoordinatorLayout.LayoutParams)mWebView.getLayoutParams()).setBehavior(new AppBarLayout.ScrollingViewBehavior(getBaseContext(), null));
		currentWebView=mWebView;
		adapter_idx=i;
		etSearch.setText(mWebView.url);
		indicator.setText(""+i);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.browser_widget7:
				CMN.Log("how ?? ");
				AppBarLayout.ScrollingViewBehavior b = (AppBarLayout.ScrollingViewBehavior) ((CoordinatorLayout.LayoutParams)currentWebView.getLayoutParams()).getBehavior();
				//b.
			break;
			case R.id.browser_widget8:
			break;
			case R.id.browser_widget9:
			break;
			case R.id.browser_widget10:
				toggleTabView();
			break;
			case R.id.browser_widget11:
			break;
		}
	}

	@Override
	public boolean onLongClick(View v) {
		switch (v.getId()){
			// 新建标签页
			case R.id.browser_widget10:{
				int i = 0;
				for (; i < WebPool.length; i++) {
					int id=(i+PoolVagranter)%WebPool.length;
					if(WebPool[id]==null){
						WebViewmy mWebView = new AdvancedNestScrollWebView(getBaseContext());
						mWebView.url="https://www.baidu.com";
						mWebView.SelfIdx=id;
						WebPool[id]=mWebView;
						Pages.add(mWebView);
						AttachWebAt(Pages.size()-1);
						break;
					}
				}
				if(i==WebPool.length){
					showT("达到最大限制");
					break;
				}else{
					PoolVagranter=(i+1+PoolVagranter)%WebPool.length;
				}
				View target = v;
				int trans = target.getHeight() / 2;
				ObjectAnimator tv1TranslateY = ObjectAnimator.ofFloat(target, "translationY", 0, -trans, 0);
				tv1TranslateY.setDuration(400);
				tv1TranslateY.start();
			} break;
		}
		return true;
	}

	public final static Pattern requestPattern = Pattern.compile("\\?.*$");
	public final static Pattern httpPattern = Pattern.compile("(https?://.*)", Pattern.CASE_INSENSITIVE);
	public final static Pattern timePattern = Pattern.compile("t=([0-9]{8,15})", Pattern.CASE_INSENSITIVE);

	WebViewClient mWebClient=new WebViewClient() {
		@Override public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			view.setTag(url);
			WebViewmy mWebView=((WebViewmy)view);
			mWebView.url=url;
			mWebView.title=mWebView.getTitle().replaceAll(TitleSep,"|");
			mWebView.time=System.currentTimeMillis();
			mWebView.lastScroll=view.getScrollY();
			if(mWebView.getParent()!=null){
				etSearch.setHint(mWebView.title);
			}
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			handler.proceed();
		}

		public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			CMN.Log("shouldOverrideUrlLoading");
			view.loadUrl(url);
			return true;
		}

		@Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			CMN.Log("shouldOverrideUrlLoading2");
			return super.shouldOverrideUrlLoading(view, request);
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

		@Nullable
		@Override
		public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
			//if(true) return null;
			String url=request.getUrl().toString();
			WebViewmy mWebView=((WebViewmy)view);
			if(false){
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
							url_file_recorder.write((mWebView.url+TitleSep+path.getName()).getBytes());
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
			viewpager_holder.setTag(false);
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
		try {
			final File def = new File(getExternalFilesDir(null),"sites_default.txt");      //!!!原配
			BufferedWriter output2 = new BufferedWriter(new FileWriter(def,false));
			for (int i = 0; i < Pages.size(); i++) {
				WebViewmy mWebView=Pages.get(i);
				String title = mWebView.title;
				output2.write(mWebView.url);
				output2.write(TitleSep);
				output2.write(title!=null?mWebView.title:"");
				output2.write("|");
				output2.write(""+mWebView.time);
				output2.write("|");
				output2.write(""+mWebView.SelfIdx);
				if(i==adapter_idx)
					output2.write("|=");
				output2.write("\n");
			}
			output2.flush();
			output2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setStatusBarColor(){
		Window window = getWindow();
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
}
