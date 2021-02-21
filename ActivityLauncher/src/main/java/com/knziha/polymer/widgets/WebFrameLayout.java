package com.knziha.polymer.widgets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.view.MotionEventCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.polymer.AdvancedBrowserWebView;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.WebOptions;
import com.knziha.polymer.WebCompoundListener;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebViewHelper;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.ActivityMainBinding;
import com.knziha.polymer.toolkits.Utils.BU;
import com.knziha.polymer.toolkits.Utils.ReusableByteOutputStream;
import com.knziha.polymer.webstorage.WebStacks;
import com.knziha.polymer.webstorage.WebStacksSer;
import com.knziha.polymer.webstorage.WebStacksStd;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.knziha.polymer.Utils.WebOptions.BackendSettings;
import static com.knziha.polymer.Utils.WebOptions.StorageSettings;
import static com.knziha.polymer.browser.webkit.UniversalWebviewInterface.getLockedView;
import static com.knziha.polymer.widgets.Utils.DummyBMRef;
import static com.knziha.polymer.widgets.Utils.getWindowManagerViews;
import static com.knziha.polymer.browser.webkit.WebViewHelper.LookForANobleSteedCorrespondingWithDrawnClasses;
import static com.knziha.polymer.browser.webkit.WebViewHelper.bAdvancedMenu;
import static com.knziha.polymer.browser.webkit.WebViewHelper.minW;

public class WebFrameLayout extends FrameLayout implements NestedScrollingChild, MenuItem.OnMenuItemClickListener{
	/**网页加载完成时清理回退栈 see {@link BrowserActivity#2LuxuriouslyLoadUrl}*/
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
	
	public boolean forbidScrollWhenSelecting;
	public boolean forbidLoading;
	public boolean isloading=false;
	
	public long time;
	public int lastThumbScroll;
	
	public BrowserActivity.TabHolder holder;
	public UniversalWebviewInterface mWebView;
	public View implView;
	public int offsetTopAndBottom;
	public int legalPad;
	public int legalPart;
	/** (始终显示底栏，滑动隐藏顶栏之时，) 若用户要求响应式高度变化，则启用此。 */
	public boolean PadPartPadBar = false;  // 响应式。
	public boolean recover;
	
	private int mLastMotionY;
	private final int[] mScrollOffset = new int[2];
	private final int[] mScrollConsumed = new int[2];
	private int mNestedYOffset;
	private int mNestedYOffset1;
	private int mNestedYOffset2;
	private float lastRawY;
	private float OrgRawY;
	private boolean dragged;
	private long lastRawYTime;
	private NestedScrollingChildHelper mChildHelper;
	
	public AppBarLayout appBarLayout;
	
	public WeakReference<Bitmap> bm = Utils.DummyBMRef;
	public Canvas canvas;
	
	public BrowserActivity activity;
	public boolean isWebHold;
	
	public boolean isIMScrollSupressed;
	
	public Cursor domainInfoCursor;
	public DomainInfo domainInfo;
	
	public boolean stackloaded;
	public static final WebStacks webStacksWriterStd = new WebStacksStd();
	public static final WebStacksSer webStacksWriterSer = new WebStacksSer();
	public WebStacks webStacksWriter = webStacksWriterSer;
	
	public float webScale=0;
	
	WebBackForwardList nav_stacks;
	int nav_stacks_idx;
	/** Use to mark that the back/forward is changed. */
	public boolean nav_stacks_dirty;
	
	public ArrayList<String> PolymerBackList = new ArrayList<>();
	
	
	private WebCompoundListener listener;
	
	public boolean PageFinishedPosted;
	
	public String targetUa;
	
	public boolean HLED;
	public float lastX;
	public float lastY;
	public float orgX;
	public float orgY;
	
	public String transientTitle;
	
	private boolean bIsActionMenuShownNew;
	public boolean bIsActionMenuShown;
	
	public WebFrameLayout(@NonNull Context context, BrowserActivity.TabHolder holder) {
		super(context);
		this.holder = holder;
		mChildHelper = new NestedScrollingChildHelper(this);
		setNestedScrollingEnabled(true);
		if(context instanceof BrowserActivity) {
			activity = (BrowserActivity) context;
		}
		
		webScale=getResources().getDisplayMetrics().density;
	}
	
	/** Use to fast preview title while going back/forward. */
	public String navigateHistory(BrowserActivity a, int delta) {
		if(nav_stacks==null)
		{
			nav_stacks = mWebView.saveState(new Bundle());
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
					nav_stacks = mWebView.saveState(new Bundle());
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
				nav_stacks = mWebView.saveState(new Bundle());
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
	
	@Override
	public void setTranslationY(float translationY) {
		//CMN.Log("setTranslationY");
		super.setTranslationY(translationY);
	}
	
	@Override
	public void offsetTopAndBottom(int offset) {
		//CMN.Log("offsetTopAndBottom", offset);
		offsetTopAndBottom = offset;
		super.offsetTopAndBottom(offset);
		if(implView != null && !isWebHold) {
			implView.scrollBy(0, offset);
		}
		if(PadPartPadBar&&legalPad>0) {
			setPadding(0, 0, 0, legalPad-(legalPart-getTop()));
		}
	}
	
	@Override
	public void removeViews(int start, int count) {
		for (int i = start+count-1; i >= start; i--) {
			View ca = getChildAt(i);
			if(ca instanceof AdvancedBrowserWebView) {
				AdvancedBrowserWebView webview = ((AdvancedBrowserWebView) ca);
				webview.stopLoading();
				//webview.pauseWeb(); //若此处暂停，再次启动时KitKat无法加载网页。
				webview.removeAllViews();
				webview.destroy();
			}
			super.removeViewAt(i);
		}
	}
	
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		CMN.Log("111 startActionMode");
		return super.startActionMode(callback, type);
	}
	
	///////// AdvancedNestScrollWebView START /////////
	////////  Copyright (C) 2016 Tobias Rohloff ////////
	////////  Apache License, Version 2.0 ////////
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		//if(true) return true;
		
		final int action = MotionEventCompat.getActionMasked(event);
		
//		if(!isIMScrollSupressed){
//			MotionEvent ev = MotionEvent.obtain(event);
//			handleSimpleNestedScrolling(this, this, ev, action);
//			ev.recycle();
//		}
//
		return super.dispatchTouchEvent(event);
	}
	
	//@SuppressLint("NewApi")
	public void handleSimpleNestedScrolling(View nestedParent, View scrollingView, MotionEvent event) {
		//		int layoutTop = nestedParent.getTop();
//			int offset = offsetTopAndBottom;
//			if(offset!=0) {
//				int top = nestedParent.getTop();
//				offset = Math.max(-top, Math.min(appBarLayout.getHeight()-top, offset));
//				offsetTopAndBottom=0;
//				mNestedYOffset2+=offset;
//				//event.offsetLocation(0, -offset);
//			}
//			event.offsetLocation(0, -mNestedYOffset2);
		int action = event.getActionMasked();
		lastY = event.getY();
		lastX = event.getX();
		boolean dealWithCM = webviewcallback!=null && bIsActionMenuShown;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				isIMScrollSupressed=false;
				isWebHold=true;
				orgY = lastY;
				orgX = lastX;
				incrementVerIfAtNormalPage();
				break;
			case MotionEvent.ACTION_MOVE:
				if (dealWithCM && bIsActionMenuShownNew && (Math.abs(lastY - orgY) > GlobalOptions.density * 5 || Math.abs(lastX - orgX) > GlobalOptions.density * 5)) {
					bIsActionMenuShownNew = false;
					for (ViewGroup vI : webviewcallback.popupDecorVies) {
						//CMN.Log(vI.getChildAt(0));
						if (vI.getChildCount() == 1) {
							vI.setTag(vI.getChildAt(0));
							vI.removeAllViews();
						}
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				isIMScrollSupressed=
						isWebHold=false;
				if(dealWithCM) {
					bIsActionMenuShownNew = bIsActionMenuShown;
					for (ViewGroup vI : webviewcallback.popupDecorVies) {
						if (vI.getParent() != null) {
							if (vI.getChildCount() == 0 && vI.getTag() instanceof View) {
								vI.addView((View) vI.getTag());
							}
						}
					}
				}
				break;
		}
		if(isIMScrollSupressed) {
			return;
		}
		NestedScrollingChild scrollingChild = (NestedScrollingChild) scrollingView;
		MotionEvent trackedEvent = null;
		if (action == MotionEvent.ACTION_DOWN) {
			mNestedYOffset = 0;
			mNestedYOffset1 = 0;
			mNestedYOffset2 = 0;
		} else if(action == MotionEvent.ACTION_MOVE) {
			trackedEvent = MotionEvent.obtain(event);
		}
		//event = MotionEvent.obtain(event);
		int y = (int) event.getY();
		
		event.offsetLocation(0, mNestedYOffset);//		event.offsetLocation(0, mNestedYOffset1);
		
		boolean limitSpd=false;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mLastMotionY = y;
				scrollingChild.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
				offsetTopAndBottom=0;
				lastRawY = OrgRawY = event.getRawY();
				lastRawYTime = limitSpd?event.getEventTime():0;
				dragged = nestedParent.getTop()!=0;
				//dragged = false;
				break;
			case MotionEvent.ACTION_MOVE:
				float rawY = event.getRawY();
				float dRawY = rawY - OrgRawY;
				long evTime = limitSpd?event.getEventTime():0;
				if(nestedParent.getTop()==0&&!dragged) {
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
				
				if (scrollingChild.dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
					deltaY -= mScrollConsumed[1];
					trackedEvent.offsetLocation(0, mScrollOffset[1]);
					mNestedYOffset += mScrollOffset[1];
				}
				mLastMotionY = y - mScrollOffset[1];
				
				int oldY = scrollingView.getScrollY();
				int newScrollY = Math.max(0, oldY + deltaY);
				int dyConsumed = newScrollY - oldY;
				int dyUnconsumed = deltaY - dyConsumed;
				
				if (scrollingChild.dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, mScrollOffset)) {
					mLastMotionY -= mScrollOffset[1];
					trackedEvent.offsetLocation(0, mScrollOffset[1]);
					mNestedYOffset += mScrollOffset[1];
					mNestedYOffset1-=dyConsumed;
				}
				trackedEvent.recycle();
				if(nestedParent.getTop()==0&&dRawY<0) {
					dragged=false;
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				scrollingChild.stopNestedScroll();
				break;
		}
		
		//event.offsetLocation(0, -offset);
		//event.offsetLocation(0, -mNestedYOffset1);
//		if(layoutTop!=0)
//			CMN.Log("top changed::",layoutTop);
//			event.offsetLocation(0, -layoutTop);
		
		//event.recycle();
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
	public void setStorageSettings() {
		WebSettings settings = mWebView.getSettings();
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
		WebSettings settings = mWebView.getSettings();
		WebOptions delegate=getDelegate(BackendSettings);
		settings.setBlockNetworkImage(delegate.getForbitNetworkImage());
		settings.setJavaScriptEnabled(delegate.getEnableJavaScript());
		settings.setUserAgentString(delegate.getPCMode()
				?"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
				:null);
		CMN.Log("设置了设置了", delegate.getEnableJavaScript());
	}
	
	public WebOptions getDelegate(int section) {
		switch(section) {
			case StorageSettings:
				if(holder.getApplyOverride_group_storage())
					break;
			case BackendSettings:
				if(holder.getApplyOverride_group_client())
					break;
			default:
				return activity.opt;
		}
		return holder;
	}
	
	/** @param hideBarType 滑动隐藏底栏  滑动隐藏顶栏 0
	 *   底栏不动  滑动隐藏顶栏 1
	 *   底栏不动  顶栏不动 2  */
	public void syncBarType(int hideBarType, boolean padPartPadBar, ActivityMainBinding UIData) {
		if(hideBarType==2) { // 两者皆不动
			PadPartPadBar = false;
			legalPart = UIData.toolbar.getHeight();
			legalPad = UIData.bottombar2.getHeight()+legalPart;
		}
		if(hideBarType==1) { // 底不动
			if(PadPartPadBar = padPartPadBar) {
				legalPart = UIData.toolbar.getHeight();
			} else {
				legalPart = 0;
			}
			legalPad = UIData.bottombar2.getHeight()+legalPart;
		}
		if(hideBarType==0) { // 顶动底动
			legalPad = legalPart = 0;
			PadPartPadBar = false;
		}
		if(legalPad!=getPaddingBottom()) {
			setPadding(0, 0, 0, legalPad);
		}
	}
	
	public void destroy() {
		removeViews(0, getChildCount());
		if(mWebView!=null) {
			mWebView.destroy();
		}
		if(getParent()!=null) {
			((ViewGroup)getParent()).removeView(this);
		}
	}
	
	public Bitmap getBitmap() {
		return bm.get();
	}
	
	public void resetBitmap() {
		bm.clear();
	}
	
	/** 从磁盘加载网页前进/回退栈 */
	public boolean lazyLoad() {
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
			mWebView.loadUrl(holder.url);
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
		if(data.length>8 && BU.getInt(data, 4)==0x4C444E42) {
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
				mWebView.loadUrl(PolymerBackList.get(size));
				PolymerBackList.remove(size);
				return true;
			}
		}
		else {
			CMN.Log("大风起兮……");
			WebBackForwardList stacks = mWebView.restoreState(bundle);
			CMN.Log("云飞扬……", stacks);
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
		if(holder.version>1 && holder.lastSaveVer<holder.version && stackloaded) {
			holder.lastSaveVer = holder.version;
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
				int cc = getChildCount();
				preserve = PolymerBackList.size();
				if(cc>0) {
					int idx = getThisIdx();
					for (int i = 0; i <= idx; i++) {
						AdvancedBrowserWebView wv = (AdvancedBrowserWebView) getChildAt(i);
						PolymerBackList.add(wv.getUrl());
					}
				}
				CMN.Log("saving...", PolymerBackList);
				bundle.putStringArrayList("PBL", PolymerBackList);
				//PolymerBackList.subList(preserve, PolymerBackList.size()).clear();
				NeedSave = true;
			}
			else {
				WebBackForwardList stacks = mWebView.saveState(bundle);
				//WebBackForwardList stacks = saveState(bundle);
				if(stacks!=null&&stacks.getSize()>0) {
					NeedSave = true;
					CMN.Log("飞升……");
				}
			}
			if(NeedSave) {
				byte[] data = webStacksWriterSer.bakeData(bundle);
				//BU.printFile(data, "/storage/emulated/0/myFolder/w_"+Utils.version);
				//BU.printFile(webStacksWriterStd.bakeData(bundle), "/storage/emulated/0/myFolder/w");
				ContentValues values = new ContentValues();
				values.put("webstack", data);
				if(nav_stacks_dirty) {
					nav_stacks_dirty = false;
					values.put("last_visit_time", holder.last_visit_time=CMN.now());
					CMN.Log("入库入库");
				}
				LexicalDBHelper.getInstancedDb().update("webtabs", values, "id=?", new String[]{""+holder.id});
				if(preserve>=0) {
					PolymerBackList.subList(preserve, PolymerBackList.size()).clear();
				}
				// "w:" test
//				WebStacksSer wss = new WebStacksSer();
//				data = wss.bakeData(bundle);
				//BU.printFile(data, "/storage/emulated/0/myFolder/w1");
//				CMN.Log("data.length", data.length);
//				BU.printBytes(data, 0, Math.min(data.length, 100));
//				bundle.clear();
//				wss.retrieveData(bundle, data);
			}
		}
	}
	
	
	/** Recapture Thumnails as a bitmap. There's no point in doing this asynchronously.
	 * 		draw(canvas) will block the UI even when it's called in another thread. */
	public void recaptureBitmap() {
		CMN.Log("recaptureBitmap...", holder.id, holder.version);
		holder.lastCaptureVer = holder.version;
		int w = implView.getWidth();
		int h = implView.getHeight();
		if(w>0) {
			float factor = 1;
			if(w>minW) {
				factor = minW/w;
			}
			int targetW = (int)(w*factor);
			int targetH = (int)(h*factor);
			int needRam = targetW*targetH*2;
			Bitmap bmItem = bm.get();
			boolean reset = bmItem==null||!bmItem.isMutable()||bmItem.getAllocationByteCount()<needRam;
			if(reset) {
				if(bmItem!=null) {
					//CMN.Log("bmItem reset");
					if(bmItem.isMutable()) //todo recycle all. ( this fixs the bug below )
						// todo currently there's a  bug when you click on the tabs manager btn before the page finished on a second launch of this app then switch to another tab and switch back and quickly show the tabs manager again that displays a blank image erroneously.
						bmItem.recycle();
				}
				bmItem = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.RGB_565);
			} else if(bmItem.getWidth()!=targetW||bmItem.getHeight()!=targetH) {
				bmItem.reconfigure(targetW, targetH, Bitmap.Config.RGB_565);
			}
			bmItem.eraseColor(Color.WHITE);
			if(canvas==null) {
				canvas = new Canvas(bmItem);
			} else if(reset) {
				canvas.setBitmap(bmItem);
			}
			canvas.setMatrix(Utils.IDENTITYXIRTAM);
			canvas.scale(factor, factor);
			canvas.translate(-implView.getScrollX(), -implView.getScrollY());
			long st = System.currentTimeMillis();
			implView.draw(canvas);
			CMN.Log("绘制时间：", System.currentTimeMillis()-st);
			bm = new WeakReference<>(bmItem);
			CMN.Log("复制时间：", System.currentTimeMillis()-st);
		}
	}
	
	public Bitmap saveBitmap() {
		Bitmap bmItem = bm.get();
		if(bmItem==null) {
			return null;
		}
		long st = System.currentTimeMillis();
		ReusableByteOutputStream bos1 = WebViewHelper.bos1;
		if(bos1==null) {
			return null;
		}
		bos1.reset();
		bos1.ensureCapacity((int) (bmItem.getAllocationByteCount()*0.5));
		bmItem.compress(Bitmap.CompressFormat.JPEG, 95, bos1);
		long tabID_Fetcher = holder.id;
		CMN.Log(tabID_Fetcher, "压缩时间：", System.currentTimeMillis()-st);
		byte[] data = bos1.toByteArray();
		ContentValues values = new ContentValues();
		values.put("title", holder.title);
		values.put("url", holder.url);
		values.put("thumbnail", data);
		LexicalDBHelper.getInstancedDb().update("webtabs", values, "id=?", new String[]{String.valueOf(tabID_Fetcher)});
		CMN.Log(tabID_Fetcher, "入表时间：", System.currentTimeMillis()-st, data.length, (int) (bmItem.getAllocationByteCount()*0.50), bos1.data().length);
		bos1.close();
		return bmItem;
	}
	
	/** source 1=animation */
	public void onViewAttached(int source) {
		if(source!=1) {
			setStorageSettings();
		}
		if(holder.paused) {
			mWebView.resumeTimers();
			mWebView.onResume();
			holder.paused=false;
		}
	}
	
	public String getTitle() {
		return mWebView.getTitle();
	}
	
	public int getImplHeight() {
		return implView.getHeight();
	}
	
	public void stopLoading() {
		mWebView.stopLoading();
	}
	
	public void setBMRef(WeakReference<Bitmap> tmpBmRef) {
		if(bm==DummyBMRef && tmpBmRef.get()!=null) {
			bm = tmpBmRef;
		}
	}
	
	public void onSalvaged() {
		stackloaded=false;
	}
	
	public void loadUrl(String url) {
		mWebView.loadUrl(url);
	}
	
	public void setImplementation(UniversalWebviewInterface view) {
		implView = (View) view;
		mWebView = view;
		mChildHelper.setCurrentView(mWebView.getView());
	}
	
	public void copyText(String selectionText) {
		activity.copyText(selectionText);
		activity.showT("已复制");
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
	
	public int getThisIdx() {
		int cc = getChildCount();
		if(cc==1) return 0;
		for (int i = 0; i < cc; i++) {
			if(getChildAt(i)==implView) {
				return i;
			}
		}
		return 0;
	}
	
	public boolean isAtLastStackMinusOne() {
		WebBackForwardList stack = mWebView.copyBackForwardList();
		if(stack.getCurrentIndex()==stack.getSize()-2) {
			return true;
		}
		return false;
	}
	
	public void pauseWeb() {
		CMN.Log("pauseWeb");
		//mWebView.stopLoading();
		//mWebView.pauseTimers();
		//mWebView.onPause();
	}
	
	public void resumeWeb() {
		mWebView.resumeTimers();
		mWebView.onResume();
	}
	
	public boolean hasValidUrl(){
		return isUrlValid(mWebView.getUrl());
	}
	
	private boolean isUrlValid(String url) {
		return url!=null&&!url.equals("about:blank");
	}
	
	public boolean equalsUrl(String text) {
		return text.equals(mWebView.getUrl())||text.equals(mWebView.getOriginalUrl());
	}
	
	Runnable OnPageFinishedNotifier = new Runnable() {
		@Override
		public void run() {
			listener.onPageFinished(getLockedView(mWebView), mWebView.getUrl());
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
		mWebView.setWebViewClient(listener);
		mWebView.setWebChromeClient(listener.mWebClient);
	}
	
	Runnable reviveJSRunnable = () -> mWebView.getSettings().setJavaScriptEnabled(true);
	
	public void shutdownJS() {
		removeCallbacks(reviveJSRunnable);
		mWebView.getSettings().setJavaScriptEnabled(false);
	}
	
	public void reviveJS() {
		mWebView.getSettings().setJavaScriptEnabled(true);
	}
	
	public void postReviveJS(long timeMs) {
		removeCallbacks(reviveJSRunnable);
		postDelayed(reviveJSRunnable, timeMs);
	}
	
	public void incrementVerIfAtNormalPage() {
		if(!"网页无法打开".equals(getTitle())) {
			holder.version++;
		}
	}
	
	private boolean onMenuItemClick(ActionMode mode, MenuItem item) {
		//CMN.Log("onMenuItemClick", item.getClass(), item.getTitle(), item.getItemId(), android.R.id.copy);
		//CMN.Log("onActionItemClicked");
		int id = item.getItemId();
		switch(id) {
			case R.id.plaindict:{
				activity.handleVersatileShare(21);
			} return true;
			case R.id.web_highlight:{
				HLED=true;
				listener.ensureMarkJS(activity);
				mWebView.evaluateJavascript(WebViewHelper.getInstance().getHighLightIncantation(),new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						CMN.Log(value);
						
						invalidate();
					}});
			} return true;
			case R.id.web_tools:{//工具复用，我真厉害啊啊啊啊！
				//evaluateJavascript("document.execCommand('selectAll'); console.log('dsadsa')",null);
				//From normal, from history, from peruse view, [from popup window]
				/**
				 * 切换段落选项
				 * 全选   | 选择标注颜色
				 * 高亮   | 清除高亮
				 * 下划线 | 清除下划线
				 * 翻译 | 分享…
				 * 平典 | ANKI
				 */
				/**
				 * 切换段落选项
				 * 全文朗读   | 添加笔记
				 * 荧黄高亮   | 荧红高亮
				 * 荧黄划线 | 荧红划线
				 * 翻译(浮动搜索) | 分享…
				 * 平典(浮动搜索) | ANKI (HTML)
				 */
				activity.getUCC().show();
			} return false;
			case R.id.web_tts:{//TTS
				mWebView.evaluateJavascript("if(window.app)app.ReadText(''+window.getSelection())",null);
			} return false;
		}
		if (mode!=null && bAdvancedMenu) {
			boolean ret = webviewcallback.callback.onActionItemClicked(mode, item);
			if(id == 50856071 || id == android.R.id.copy || WebViewHelper.getInstance().getCopyText(getContext()).equals(item.getTitle())){
				clearFocus();
				ret=true;
			}
			return ret;
		}
		return false;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return onMenuItemClick(null, item);
	}
	
	public AdvancedWebViewCallback webviewcallback;
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	public AdvancedWebViewCallback getWebViewActionModeCallback() {
		if(webviewcallback==null) {
			webviewcallback = new AdvancedWebViewCallback();
		}
		return webviewcallback;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	public class AdvancedWebViewCallback extends ActionMode.Callback2 {
		ArrayList<ViewGroup> popupDecorVies = new ArrayList<>();
		ActionMode.Callback callback;
		public AdvancedWebViewCallback wrap(ActionMode.Callback callher) {
			callback=callher;
			return this;
		}
		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			CMN.Log("onCreateActionMode…");
			popupDecorVies.clear();
			List<View> views = getWindowManagerViews();
			/* 📕📕📕先天法则符箓📕📕📕 */
			for(View vI:views) {
				//CMN.recurseLogCascade(vI);
				if (vI instanceof FrameLayout) {
					if (vI.getClass().getName().contains("PopupDecorView")) {
						//CMN.Log("Xplode_0", vI.getAnimation());
						View dragDrawableEntity = ((FrameLayout) vI).getChildAt(0);
//						try {
//							Field f_Alpha = dragDrawableEntity.getClass().getDeclaredField("mAlpha");
//							f_Alpha.setAccessible(true);
//							f_Alpha.set(dragDrawableEntity, 1.0f);
//						} catch (Exception e) {
//							CMN.Log(e);
//						}
						//任尔东西
						if(Utils.version>28)
							try {
								Field f_Alpha = dragDrawableEntity.getClass().getDeclaredField("mDrawable");
								f_Alpha.setAccessible(true);
								f_Alpha.set(dragDrawableEntity, 1.0f);
							} catch (Exception e) {
								CMN.Log(e);
							}
						
						//李代桃僵
//						DescriptiveImageView d_h = new DescriptiveImageView(getContext());
//						d_h.setImageDrawable(getContext().getResources().getDrawable(R.drawable.abc_text_select_handle_left_mtrl_dark));
//						((FrameLayout) vI).removeAllViews();
//						((FrameLayout) vI).addView(d_h);
						popupDecorVies.add((ViewGroup) vI);
					}
				}
			}
			//CMN.Log("先天法执", popupDecorVies);
			return bIsActionMenuShownNew=bIsActionMenuShown=callback.onCreateActionMode(mode, menu);
		}
		
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return callback.onPrepareActionMode(mode, menu);
		}
		
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return onMenuItemClick(mode, item);
		}
		
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			bIsActionMenuShown=false;
			bIsActionMenuShownNew=false;
		}
		
		@Override
		public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
			if(callback instanceof ActionMode.Callback2) {
				((ActionMode.Callback2)callback).onGetContentRect(mode, view, outRect);
			} else {
				super.onGetContentRect(mode, view, outRect);
			}
		}
		
		OnLongClickListener MenuLongClicker = v -> {
			switch (v.getId()) {
				case R.id.web_highlight:
					mWebView.evaluateJavascript("if(window.app)app.setTTS()",null);
					break;
				case R.id.web_tools:
					//evaluateJavascript(getUnderlineIncantation().toString(),null);
					break;
				case R.id.web_tts:
					break;
			}
			return true;
		};
		
		public Runnable explodeMenuRunnable = () -> {
			List<View> views = getWindowManagerViews();
			ViewGroup vg;
			boolean addViews=popupDecorVies.size()==0;
			if(addViews) {
				bIsActionMenuShownNew=true;
			}
			for(View vI:views){
				//CMN.recurseLogCascade(vI);
				if (addViews && vI instanceof FrameLayout && vI.getClass().getName().contains("PopupDecorView")) {
					//CMN.Log("Xplode", vI.getAnimation());
					popupDecorVies.add((ViewGroup) vI);
				}
				/* 📕📕📕阿西吧折叠空间打开术第二式按图索骥大法📕📕📕 */
				vg = (ViewGroup) LookForANobleSteedCorrespondingWithDrawnClasses(vI, 4, FrameLayout.class, FrameLayout.class, LinearLayout.class, RelativeLayout.class, LinearLayout.class);
				if(vg!=null) {
					WebViewHelper.getInstance().threeKingdomText(vg, vI, MenuLongClicker);
					break;
				}
			}
			//CMN.Log("阿西吧", popupDecorVies);
		};
		
	}
}
