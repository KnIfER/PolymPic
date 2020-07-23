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

package com.knaiver.polymer;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;

import com.knaiver.polymer.Utils.CMN;
import com.knaiver.polymer.toolkits.Utils.BU;
import com.knaiver.polymer.widgets.WebViewmy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static androidx.appcompat.widget.AppCompatEditText.TextFucker;

/** Advanced WebView For DuoJuLiuLanQi <br/>
 * 多聚浏览器 <br/>
 * Based On previous work on Plain-Dictionary*/
public class AdvancedBrowserWebView extends WebViewmy implements NestedScrollingChild {
	public ViewGroup layout;
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
	
	private int mLastMotionY;

	private final int[] mScrollOffset = new int[2];
	private final int[] mScrollConsumed = new int[2];

	private int mNestedYOffset;
	
	public ArrayList<String> PolymerBackList = new ArrayList<>();

	private NestedScrollingChildHelper mChildHelper;
	
	private WebCompoundListener listener;
	
	public boolean PageFinishedPosted;
	
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
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		//settings.setSupportZoom(support);
		
		settings.setAllowUniversalAccessFromFileURLs(true);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		
		//settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		//setLayerType(View.LAYER_TYPE_HARDWARE, null);
		webScale=getResources().getDisplayMetrics().density;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			setTextClassifier(TextFucker);
		}
	}
	
	///////// AdvancedNestScrollWebView START /////////
	////////  Copyright (C) 2016 Tobias Rohloff ////////
	////////  Apache License, Version 2.0 ////////
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		MotionEvent trackedEvent = MotionEvent.obtain(event);

		final int action = MotionEventCompat.getActionMasked(event);

		if (action == MotionEvent.ACTION_DOWN) {
			mNestedYOffset = 0;
		}

		int y = (int) event.getY();

		event.offsetLocation(0, mNestedYOffset);

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mLastMotionY = y;
				startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
				break;
			case MotionEvent.ACTION_MOVE:
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
				}
				trackedEvent.recycle();
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				stopNestedScroll();
				break;
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
		return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
	}

	@Override
	public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
		return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
	}

	@Override
	public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
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
		if(holder.url!=null && !holder.url.equals(getTag())){
			CMN.Log("loadIfNeeded");
			if(stackpath==null) {
				stackpath = new File(getContext().getExternalCacheDir(), "webstack"+holder.id);
				CMN.Log("stackpath", stackpath.exists());
				if(stackpath.exists()) {
					Parcel parcel = Parcel.obtain();
					byte[] data = BU.fileToByteArr(stackpath);
					parcel.unmarshall(data, 0, data.length);
					parcel.setDataPosition(0);
					Bundle bundle = new Bundle();
					bundle.readFromParcel(parcel);
					parcel.recycle();
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
					} else {
						WebBackForwardList stacks = restoreState(bundle);
						if(stacks!=null && stacks.getSize()>0) {
							CMN.Log("复活……", stacks.getSize());
							return true;
						}
					}
				}
			}
			CMN.Log("再生……", holder.url);
			loadUrl(holder.url);
			return true;
		}
		return false;
	}
	
	/** 持久化保存网页前进/回退栈 */
	public void saveIfNeeded() { //WEBVIEW_CHROMIUM_STATE
		if((true/*version>1*/) && stackpath!=null) {
			Bundle bundle = new Bundle();
			
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
			} else {
				WebBackForwardList stacks = saveState(bundle);
				//WebBackForwardList stacks = saveState(bundle);
				if(stacks!=null&&stacks.getSize()>0) {
					NeedSave = true;
					CMN.Log("飞升……");
				} else {
					stackpath.delete();
				}
			}
			if(NeedSave) {
				Parcel parcel = Parcel.obtain();
				parcel.setDataPosition(0);
				bundle.writeToParcel(parcel, 0);
				byte[] bytes = parcel.marshall();
				parcel.recycle();
				BU.printFile(bytes, stackpath.getPath());
				if(preserve>=0) {
					PolymerBackList.subList(preserve, PolymerBackList.size()).clear();
				}
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
		pauseTimers();
		onPause();
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
	
	
}
