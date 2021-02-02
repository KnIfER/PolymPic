/*
 * Copyright 2020 The 多聚浏览 Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.polymer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.WebOptions;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.toolkits.Utils.BU;
import com.knziha.polymer.webstorage.WebStacks;
import com.knziha.polymer.webstorage.WebStacksSer;
import com.knziha.polymer.webstorage.WebStacksStd;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.knziha.polymer.widgets.WebViewmy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.knziha.polymer.Utils.WebOptions.BackendSettings;
import static com.knziha.polymer.Utils.WebOptions.StorageSettings;

/** Advanced WebView For DuoJuLiuLanQi <br/>
 * 多聚浏览器 <br/>
 * Based On previous work on Plain-Dictionary*/
public class AdvancedBrowserWebView extends WebViewmy implements NestedScrollingChild {
	public WebFrameLayout layout;
	public AppBarLayout appBarLayout;
	/**网页加载完成时清理回退栈 see {@link BrowserActivity#LuxuriouslyLoadUrl}*/
	public boolean clearHistroyRequested;
	/**记录网页开始加载*/
	public boolean PageStarted;
	/**记录启动后的加载次数*/
	public int PageVersion;
	/**“加快加载”，五十步当百步。 see {@link WebCompoundListener.WebClient#onProgressChanged} */
	public int EnRipenPercent;
	/**网页规则，即插件。*/
	public List<WebCompoundListener.SiteRule> rules = Collections.synchronizedList(new ArrayList<>());
	public List<Object> prunes = Collections.synchronizedList(new ArrayList<>());
	public Cursor domainInfoCursor;
	public DomainInfo domainInfo;
	private float lastRawY;
	private float OrgRawY;
	private boolean dragged;
	private long lastRawYTime;
	
	WebBackForwardList nav_stacks;
	int nav_stacks_idx;
	
	/** Use to mark that the back/forward is changed. */
	public boolean nav_stacks_dirty;
	
	/** Use to fast preview title while going back/forward. */
	public String navigateHistory(BrowserActivity a, int delta) {
		if(nav_stacks==null)
		{
			nav_stacks = saveState(new Bundle());
			nav_stacks_idx = nav_stacks.getCurrentIndex();
		}
		WebHistoryItem bfItem;
		if(false) {
			bfItem = nav_stacks.getItemAtIndex(nav_stacks_idx);
			String currentUrl = holder.url;
			if(!bfItem.getUrl().equals(currentUrl)) {
				if(nav_stacks_idx>0&&nav_stacks.getItemAtIndex(nav_stacks_idx-1).getUrl().equals(currentUrl)) {
					nav_stacks_idx--;
				}
				else if(nav_stacks_idx<nav_stacks.getSize() - 1&&nav_stacks.getItemAtIndex(nav_stacks_idx+1).getUrl().equals(currentUrl)) {
					nav_stacks_idx++;
				}
				else {
					nav_stacks = saveState(new Bundle());
					nav_stacks_idx = nav_stacks.getCurrentIndex();
				}
			}
		}
		nav_stacks_idx = Math.max(0, Math.min(nav_stacks.getSize() - 1, nav_stacks_idx + delta));
		bfItem = nav_stacks.getItemAtIndex(nav_stacks_idx);
		listener.lastTitleSuppressTime = CMN.now();
		a.postRectifyWebTitle();
		return nav_stacks_idx+" - "+bfItem.getTitle();
	}
	
	/** Use to rectify the title since the android tech above is not very reliable. */
	public String rectifyWebStacks(String title) {
		if(nav_stacks!=null) {
			WebHistoryItem bfItem = nav_stacks.getItemAtIndex(nav_stacks_idx);
			String currentUrl = holder.url;
			if(!bfItem.getUrl().equals(currentUrl)||!TextUtils.equals(bfItem.getTitle(), holder.title)) {
				nav_stacks = saveState(new Bundle());
				nav_stacks_idx = nav_stacks.getCurrentIndex();
				bfItem = nav_stacks.getItemAtIndex(nav_stacks_idx);
			}
			return bfItem.getTitle();
		}
		return title;
	}
	
	/** Reset the flag. */
	public void setNavStacksDirty() {
		nav_stacks_dirty = true;
		if(nav_stacks!=null) {
			nav_stacks = null;
		}
	}
	
	public static class DomainInfo {
		public long rowID;
		public long f1;
		public Bitmap thumbnail;
		DomainInfo(long rowID, long f1) {
			this.rowID = rowID;
			this.f1 = f1;
		}
	}
	public String domain;
	
	private int mLastMotionY;

	private final int[] mScrollOffset = new int[2];
	private final int[] mScrollConsumed = new int[2];

	private int mNestedYOffset;
	private int mNestedYOffset1;
	private int mNestedYOffset2;
	
	public ArrayList<String> PolymerBackList = new ArrayList<>();

	private NestedScrollingChildHelper mChildHelper;
	
	private WebCompoundListener listener;
	
	public boolean PageFinishedPosted;
	
	public static final WebStacks webStacksWriterStd = new WebStacksStd();
	
	public static final WebStacksSer webStacksWriterSer = new WebStacksSer();
	
	public WebStacks webStacksWriter = webStacksWriterSer;
	
	public AdvancedBrowserWebView(Context context) {
		super(context);
		//super(context, null, android.R.attr.webViewStyle);
		mChildHelper = new NestedScrollingChildHelper(this);
		setNestedScrollingEnabled(true);
		
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		//setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		
		final WebSettings settings = getSettings();
		
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		//	settings.setSafeBrowsingEnabled(false);
		//}
		
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setDefaultTextEncodingName("UTF-8");
		
		settings.setNeedInitialFocus(false);
		//settings.setDefaultFontSize(40);
		//settings.setTextZoom(100);
		//setInitialScale(25);
		
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(false);
		settings.setMediaPlaybackRequiresUserGesture(false);
		
		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
		
		settings.setAllowFileAccess(true);
		
		//settings.setUseWideViewPort(true);//设定支持viewport
		//settings.setLoadWithOverviewMode(true);
		//settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		//settings.setSupportZoom(support);
		
		settings.setAllowUniversalAccessFromFileURLs(true);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		
		webScale=getResources().getDisplayMetrics().density;
		
		settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		//setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}
	
	///////// AdvancedNestScrollWebView START /////////
	////////  Copyright (C) 2016 Tobias Rohloff ////////
	////////  Apache License, Version 2.0 ////////
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//if(true) return true;
		MotionEvent trackedEvent = MotionEvent.obtain(event);

		final int action = MotionEventCompat.getActionMasked(event);

		if (action == MotionEvent.ACTION_DOWN) {
			mNestedYOffset = 0;
			mNestedYOffset1 = 0;
			mNestedYOffset2 = 0;
		}

		int y = (int) event.getY();
		event.offsetLocation(0, mNestedYOffset);
//		event.offsetLocation(0, mNestedYOffset1);
		if(!isIMScrollSupressed){
//		int layoutTop = layout.getTop();
//			int offset = layout.offsetTopAndBottom;
//			if(offset!=0) {
//				int top = layout.getTop();
//				offset = Math.max(-top, Math.min(appBarLayout.getHeight()-top, offset));
//				layout.offsetTopAndBottom=0;
//				mNestedYOffset2+=offset;
//				//event.offsetLocation(0, -offset);
//			}
//			event.offsetLocation(0, -mNestedYOffset2);
		boolean limitSpd=false;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mLastMotionY = y;
				startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
				layout.offsetTopAndBottom=0;
				lastRawY = OrgRawY = event.getRawY();
				lastRawYTime = limitSpd?event.getEventTime():0;
				dragged = layout.getTop()!=0;
				//dragged = false;
				break;
			case MotionEvent.ACTION_MOVE:
				float rawY = event.getRawY();
				float dRawY = rawY - OrgRawY;
				long evTime = limitSpd?event.getEventTime():0;
				if(layout.getTop()==0&&!dragged) {
					float dstFactor=0.45f;
					if(limitSpd&&(rawY-lastRawY)/(evTime-lastRawYTime)<1.15) {
						OrgRawY += rawY-lastRawY;
					} else // Math.abs
					if((dRawY)>=appBarLayout.getHeight()*dstFactor) {
						mLastMotionY = (int) (y+(dRawY-appBarLayout.getHeight()*dstFactor));
						//mLastMotionY = (int) (y-(event.getRawY() - (downRawY + appBarLayout.getHeight())));
						//mLastMotionY = y;
						dragged=true;
					}
				}
				lastRawY = rawY;
				lastRawYTime = evTime;
				if(!dragged) {
					break;
				}
				int deltaY = mLastMotionY - y;

				if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
					deltaY -= mScrollConsumed[1];
					trackedEvent.offsetLocation(0, mScrollOffset[1]);
					mNestedYOffset += mScrollOffset[1];
				}
				mLastMotionY = y - mScrollOffset[1];

				int oldY = getScrollY();
				int newScrollY = Math.max(0, oldY + deltaY);
				int dyConsumed = newScrollY - oldY;
				int dyUnconsumed = deltaY - dyConsumed;
				
				if (dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, mScrollOffset)) {
					mLastMotionY -= mScrollOffset[1];
					trackedEvent.offsetLocation(0, mScrollOffset[1]);
					mNestedYOffset += mScrollOffset[1];
					mNestedYOffset1-=dyConsumed;
				}
				trackedEvent.recycle();
				if(layout.getTop()==0&&dRawY<0) {
					dragged=false;
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				stopNestedScroll();
				break;
		}
		
			//event.offsetLocation(0, -offset);
			//event.offsetLocation(0, -mNestedYOffset1);
//		if(layoutTop!=0)
//			CMN.Log("top changed::",layoutTop);
//			event.offsetLocation(0, -layoutTop);
		}
		//if(OrgTop-getTop()==0)
			super.onTouchEvent(event);
		return true;
	}

	@Override
	public void setNestedScrollingEnabled(boolean enabled) {
		mChildHelper.setNestedScrollingEnabled(enabled);
	}

	@Override
	public boolean isNestedScrollingEnabled() {
		return mChildHelper.isNestedScrollingEnabled();
	}

	@Override
	public boolean startNestedScroll(int axes) {
		return mChildHelper.startNestedScroll(axes);
	}

	@Override
	public void stopNestedScroll() {
		mChildHelper.stopNestedScroll();
	}

	@Override
	public boolean hasNestedScrollingParent() {
		return mChildHelper.hasNestedScrollingParent();
	}

	@Override
	public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
		//CMN.Log("dispatchNestedScroll");
		return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
	}

	@Override
	public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
		return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
	}

	@Override
	public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
		//CMN.Log("dispatchNestedFling");
		return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
	}

	@Override
	public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
		return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
	}
	
	///////// AdvancedNestScrollWebView END /////////
	
	///////// AdvancedBrowserWebView Start /////////
	
	public int getThisIdx() {
		int cc = layout.getChildCount();
		if(cc==1) return 0;
		for (int i = 0; i < cc; i++) {
			if(layout.getChildAt(i)==this) {
				return i;
			}
		}
		return 0;
	}
	
	/** 从磁盘加载网页前进/回退栈 */
	public boolean loadIfNeeded() {
		CMN.Log("loadIfNeeded", holder.url);
		if(!stackloaded) {
			stackloaded = true;
			Cursor stackcursor = LexicalDBHelper.getInstancedDb()
					.rawQuery("select webstack from webtabs where id=? limit 1"
							, new String[]{""+holder.id});
			if(stackcursor.moveToFirst()
					&&parseBundleFromData(stackcursor.getBlob(0))) {
				return true;
			}
			CMN.Log("再生……", holder.url);
			loadUrl(holder.url);
			return true;
		}
//		if(holder.url!=null && !holder.url.equals(getTag())){ todo remove tag
//		}
		return false;
	}
	
	private boolean parseBundleFromData(byte[] data) {
		if(data==null) {
			return false;
		}
		try {
		WebStacks stateReader = webStacksWriterSer;
		if(data.length>8 && BU.getInt(data, 4)!=0x4C444E42) {
			stateReader = webStacksWriterStd;
		}
		Bundle bundle = new Bundle();
		stateReader.readData(bundle, data);
		if(holder.getLuxury()) {
			ArrayList<String> BackList = bundle.getStringArrayList("PBL");
			CMN.Log("PBL", BackList);
			if(BackList!=null) {
				PolymerBackList.clear();
				PolymerBackList.addAll(BackList);
			}
			for (int i = PolymerBackList.size()-1; i >= 0; i--) {
				if(!isUrlValid(PolymerBackList.get(i))) {
					PolymerBackList.remove(i);
				}
			}
			int size = PolymerBackList.size()-1;
			if(size>=0) {
				CMN.Log("retrieving...", PolymerBackList);
				loadUrl(PolymerBackList.get(size));
				PolymerBackList.remove(size);
				return true;
			}
		}
		else {
			WebBackForwardList stacks = restoreState(bundle);
			if(stacks!=null && stacks.getSize()>0) {
				CMN.Log("复活……", stacks.getSize());
				return true;
			}
		}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return false;
	}
	
	/** 持久化保存网页前进/回退栈 */
	public void saveIfNeeded() { //WEBVIEW_CHROMIUM_STATE
		if(holder.version>1 && lastSaveVer<holder.version && stackloaded) {
			lastSaveVer = holder.version;
			Bundle bundle = new Bundle();
			CMN.Log("saveIfNeeded", holder.getLuxury());
//			int stacksCount=0;
//			for (int i = 0; i < layout.getChildCount(); i++) {
//				WebBackForwardList stacks = ((AdvancedNestScrollWebView)layout.getChildAt(i)).saveState(bundle);
//				if(stacks!=null) {
//					stacksCount+=stacks.getSize();
//				}
//			}
			boolean NeedSave=false;
			int preserve = -1;
			if(holder.getLuxury()) {
				int cc = layout.getChildCount();
				preserve = PolymerBackList.size();
				if(cc>0) {
					int idx = getThisIdx();
					for (int i = 0; i <= idx; i++) {
						AdvancedBrowserWebView wv = (AdvancedBrowserWebView) layout.getChildAt(i);
						PolymerBackList.add(wv.getUrl());
					}
				}
				CMN.Log("saving...", PolymerBackList);
				bundle.putStringArrayList("PBL", PolymerBackList);
				//PolymerBackList.subList(preserve, PolymerBackList.size()).clear();
				NeedSave = true;
			}
			else {
				WebBackForwardList stacks = saveState(bundle);
				//WebBackForwardList stacks = saveState(bundle);
				if(stacks!=null&&stacks.getSize()>0) {
					NeedSave = true;
					CMN.Log("飞升……");
				}
			}
			if(NeedSave) {
				byte[] data = webStacksWriterSer.bakeData(bundle);
				//BU.printFile(data, "/storage/emulated/0/myFolder/w");
				//BU.printFile(webStacksWriterStd.bakeData(bundle), "/storage/emulated/0/myFolder/w");
				ContentValues values = new ContentValues();
				values.put("webstack", data);
				if(nav_stacks_dirty) {
					nav_stacks_dirty = false;
					values.put("last_visit_time", CMN.now());
					CMN.Log("入库入库");
				}
				LexicalDBHelper.getInstancedDb().update("webtabs", values, "id=?", new String[]{""+holder.id});
				if(preserve>=0) {
					PolymerBackList.subList(preserve, PolymerBackList.size()).clear();
				}
				// "w:" test
//				WebStacksSer wss = new WebStacksSer();
//				data = wss.bakeData(bundle);
//				BU.printFile(data, "/storage/emulated/0/myFolder/w1");
//				CMN.Log("data.length", data.length);
//				BU.printBytes(data, 0, Math.min(data.length, 100));
//				bundle.clear();
//				wss.retrieveData(bundle, data);
			}
		}
	}
	
	public boolean isAtLastStackMinusOne() {
		WebBackForwardList stack = copyBackForwardList();
		if(stack.getCurrentIndex()==stack.getSize()-2) {
			return true;
		}
		return false;
	}
	
	public void pauseWeb() {
		CMN.Log("pauseWeb");
		//stopLoading();
		//pauseTimers();
		//onPause();
	}
	
	public void resumeWeb() {
		resumeTimers();
		onResume();
	}
	
	public boolean hasValidUrl(){
		return isUrlValid(getUrl());
	}
	
	private boolean isUrlValid(String url) {
		return url!=null&&!url.equals("about:blank");
	}
	
	public void deconstruct() {
		layout.removeViews(0, layout.getChildCount());
	}
	
	public boolean equalsUrl(String text) {
		return text.equals(getUrl())||text.equals(getOriginalUrl());
	}
	
	Runnable OnPageFinishedNotifier = new Runnable() {
		@Override
		public void run() {
			listener.onPageFinished(AdvancedBrowserWebView.this, getUrl());
		}
	};
	
	/** 加快显示加载完成。 see {@link #EnRipenPercent} */
	public void postFinished() {
		if(!PageFinishedPosted) {
			PageFinishedPosted=true;
			post(OnPageFinishedNotifier);
			//postDelayed(OnPageFinishedNotifier, 350);
		}
	}
	
	public void removePostFinished() {
		removeCallbacks(OnPageFinishedNotifier);
	}
	
	public void setWebViewClient(WebCompoundListener listener) {
		this.listener = listener;
		super.setWebViewClient(listener);
		super.setWebChromeClient(listener.mWebClient);
	}
	
	
	public void setStorageSettings() {
		WebSettings settings = getSettings();
		WebOptions delegate=getDelegate(StorageSettings);
		boolean enabled=delegate.getForbidLocalStorage();
		settings.setDomStorageEnabled(enabled?!delegate.getForbidDom():true);
		settings.setAppCacheEnabled(enabled?!delegate.getForbidDatabase():true);
		settings.setDatabaseEnabled(enabled?!delegate.getForbidDatabase():true);
		settings.setDomStorageEnabled(true);
		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
	}
	
	public void setBackEndSettings() {
		WebSettings settings = getSettings();
		WebOptions delegate=getDelegate(BackendSettings);
		settings.setBlockNetworkImage(delegate.getForbitNetworkImage());
		settings.setJavaScriptEnabled(delegate.getEnableJavaScript());
		settings.setUserAgentString(delegate.getPCMode()
				?"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
				:null);
		CMN.Log("设置了设置了", delegate.getEnableJavaScript());
	}
	
	WebOptions getDelegate(int section) {
		switch(section) {
			case StorageSettings:
				if(holder.getApplyOverride_group_storage())
					break;
			case BackendSettings:
				if(holder.getApplyOverride_group_client())
					break;
			default:
				return context.opt;
		}
		return holder;
	}
	
	Runnable reviveJSRunnable = () -> getSettings().setJavaScriptEnabled(true);
	
	public void shutdownJS() {
		removeCallbacks(reviveJSRunnable);
		getSettings().setJavaScriptEnabled(false);
	}
	
	public void reviveJS() {
		getSettings().setJavaScriptEnabled(true);
	}
	
	public void postReviveJS(long timeMs) {
		removeCallbacks(reviveJSRunnable);
		postDelayed(reviveJSRunnable, timeMs);
	}
	
	public void setDomainCursor(String domainUrl, Cursor infoCursor) {
		if(domainInfoCursor!=null) {
			domainInfoCursor.close();
		}
		domain = domainUrl;
		if(infoCursor!=null&&!infoCursor.moveToFirst()) {
			infoCursor.close();
			domainInfoCursor = null;
			return;
		}
		if((domainInfoCursor = infoCursor)!=null) {
			domainInfo = new DomainInfo(infoCursor.getLong(0), infoCursor.getLong(3));
		}
	}
	
	public long getDomainFlag() {
		if(domainInfo!=null) {
			return domainInfo.f1;
		}
		return 0;
	}
	
	public void setDomainFlag(long val) {
		LexicalDBHelper.getInstance().updateDomainFlag(this, val);
	}
}
