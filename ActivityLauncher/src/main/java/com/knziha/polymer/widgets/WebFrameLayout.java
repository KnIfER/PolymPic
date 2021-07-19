package com.knziha.polymer.widgets;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.WebBackForwardList;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.knziha.polymer.Utils.IU;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.WebCompoundListener;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebViewHelper;
import com.knziha.polymer.browser.webkit.XWalkWebView;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.ActivityMainBinding;
import com.knziha.polymer.toolkits.Utils.BU;
import com.knziha.polymer.toolkits.Utils.ReusableByteOutputStream;
import com.knziha.polymer.webstorage.DomainInfo;
import com.knziha.polymer.webstorage.SubStringKey;
import com.knziha.polymer.webstorage.WebOptions;
import com.knziha.polymer.webstorage.WebStacks;
import com.knziha.polymer.webstorage.WebStacksSer;
import com.knziha.polymer.webstorage.WebStacksStd;

import org.xwalk.core.XWalkGetBitmapCallback;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_DOMAIN;
import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_TAB;
import static com.knziha.polymer.browser.webkit.WebViewHelper.LookForANobleSteedCorrespondingWithDrawnClasses;
import static com.knziha.polymer.browser.webkit.WebViewHelper.bAdvancedMenu;
import static com.knziha.polymer.browser.webkit.WebViewHelper.minW;
import static com.knziha.polymer.database.LexicalDBHelper.FIELD_CREATE_TIME;
import static com.knziha.polymer.database.LexicalDBHelper.FIELD_LAST_TIME;
import static com.knziha.polymer.database.LexicalDBHelper.FIELD_URL;
import static com.knziha.polymer.database.LexicalDBHelper.FIELD_URL_ID;
import static com.knziha.polymer.database.LexicalDBHelper.TABLE_ANNOTS;
import static com.knziha.polymer.database.LexicalDBHelper.TABLE_ANNOTS_TEXT;
import static com.knziha.polymer.webstorage.WebOptions.BackendSettings;
import static com.knziha.polymer.webstorage.WebOptions.ImmersiveSettings;
import static com.knziha.polymer.webstorage.WebOptions.LockSettings;
import static com.knziha.polymer.webstorage.WebOptions.PC_MODE_MASK;
import static com.knziha.polymer.webstorage.WebOptions.StorageSettings;
import static com.knziha.polymer.webstorage.WebOptions.TextSettings;
import static com.knziha.polymer.widgets.Utils.DummyBMRef;
import static com.knziha.polymer.widgets.Utils.getWindowManagerViews;
import static org.xwalk.core.Utils.Log;
import static org.xwalk.core.Utils.getLockedView;
import static org.xwalk.core.Utils.unlock;

public class WebFrameLayout extends FrameLayout implements NestedScrollingChild, MenuItem.OnMenuItemClickListener, DownloadListener {
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
	public boolean hasPrune;
	public List<Pair<Pattern, Pair<String, String>>> modifiers;
	
	public static String android_ua = "Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + "; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.4072.134 Mobile Safari/537.36";
	public static String default_ua = null;
	
	private final ViewConfiguration viewConfig;
	public boolean frcWrp;
	public boolean allowAppScheme = true;
	private int scaledTouchSlop;
	
	public int postTime;
	
	public boolean hideForTabView;
	public boolean forbidScrollWhenSelecting;
	public boolean forbidLoading;
	public boolean isloading=false;
	
	public long time;
	public int lastThumbScroll;
	
	public BrowserActivity.TabHolder holder;
	public UniversalWebviewInterface mWebView;
	public View implView = this;
	public int offsetTopAndBottom;
	public int legalPad;
	public int legalPart;
	/** (始终显示底栏，滑动隐藏顶栏之时，) 若用户要求响应式高度变化，则启用此。 */
	public boolean PadPartPadBar = false;  // 响应式。
	public boolean recover;
	public volatile int pendingBitCapRsn=-1;
	
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
	
	public final ArrayList<Object> focusFucker = new ArrayList<>();
	
	public WeakReference<Bitmap> bm = Utils.DummyBMRef;
	public Canvas canvas;
	
	public BrowserActivity activity;
	public boolean isWebHold;
	
	public boolean isIMScrollSupressed;
	
	public boolean handleLongPress;
	public boolean handlingLongPress;
	private OnLongClickListener longClickListener;
	
	@NonNull public DomainInfo domainInfo = DomainInfo.EmptyInfo;
	@NonNull public SubStringKey domain = SubStringKey.EmptyDomain;
	public static boolean bUseCookie = true;
	
	public boolean stackloaded;
	public static final WebStacks webStacksWriterStd = new WebStacksStd();
	public static final WebStacksSer webStacksWriterSer = new WebStacksSer();
	public WebStacks webStacksWriter = webStacksWriterSer;
	
	public float webScale=0;
	
	WebBackForwardList nav_stacks;
	int nav_stacks_idx;
	/** Use to mark that the back/forward is changed. */
	public boolean nav_stacks_dirty;
	public boolean new_page_loaded;
	public String searchTerm;
	
	public ArrayList<String> PolymerBackList = new ArrayList<>();
	
	
	private WebCompoundListener listener;
	
	public boolean PageFinishedPosted;
	
	public String targetUa;
	
	public boolean HLED;
	public float lastX;
	public float lastY;
	public float orgX;
	public float orgY;
	
	public boolean lockX;
	public boolean lockY;
	public boolean bNeedPtsCnt;
	public int pts;
	public boolean pts_2_scaled;
	
	public String transientTitle;
	
	private boolean bIsActionMenuShownNew;
	public boolean bIsActionMenuShown;
	private long lastDownTm;
	private boolean lastDwnStill;
	
	public static int GlobalSettingsVersion;
	private boolean hideBothBars = true;
	private boolean hideNoBars = true;
	public boolean bNeedCheckUA;
	
	public int bNeedCheckTextZoom;
	public int bRecentNewDomainLnk;
	
	public WebFrameLayout(@NonNull Context context, BrowserActivity.TabHolder holder) {
		super(context);
		this.holder = holder;
		mChildHelper = new NestedScrollingChildHelper(this);
		setNestedScrollingEnabled(true);
		if(context instanceof BrowserActivity) {
			activity = (BrowserActivity) context;
		}
		
		viewConfig = ViewConfiguration.get(context);
		scaledTouchSlop = viewConfig.getScaledTouchSlop();
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
		new_page_loaded = true;
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
//			handleSimpleNestedScrolling(this, this, ev);
//			ev.recycle();
//		}

		return super.dispatchTouchEvent(event);
	}
	
	Runnable mLongPressed = new Runnable() {
		public void run() {
			CMN.Log("mLongPressed!!!");
			if (isWebHold) {
				longClickListener.onLongClick((View) mWebView);
			}
		}
	};
	
	@Override
	public void setOnLongClickListener(@Nullable OnLongClickListener listener) {
		longClickListener = listener;
		boolean val = listener!=null;
		setLongClickable(val);
		handleLongPress = val;
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
			case MotionEvent.ACTION_DOWN:{
				if(bNeedPtsCnt) {
					pts_2_scaled = false;
					pts = 1;
				}
				if (handleLongPress) {
					postDelayed(mLongPressed, 500);
					handlingLongPress = true;
				}
				isIMScrollSupressed=false;
				isWebHold=true;
				long tm = event.getDownTime();
				//CMN.Log("Juli", orgX-lastX, orgY-lastY, orgX, lastX, orgY, lastY, viewConfig.getScaledTouchSlop());
				//CMN.Log("scaledTouchSlop", Juli(orgX-lastX, orgY-lastY), scaledTouchSlop, tm-lastDownTm);
				if(lastDwnStill && tm-lastDownTm<ViewConfiguration.getDoubleTapTimeout() && Juli(orgX-lastX, orgY-lastY)<250*250) {
					isIMScrollSupressed=true;
				}
				lastDwnStill = true;
				orgY = lastY;
				orgX = lastX;
				incrementVerIfAtNormalPage();
				lastDownTm = tm;
			} break;
			case MotionEvent.ACTION_MOVE:
				if(lastDwnStill && (Math.abs(lastY - orgY) > scaledTouchSlop|| Math.abs(lastX - orgX) > scaledTouchSlop)) {
					lastDwnStill = false;
				}
				if(handlingLongPress && (Math.abs(lastY - orgY) > scaledTouchSlop|| Math.abs(lastX - orgX) > scaledTouchSlop)) {
					//removeCallbacks(mLongPressed);
					handlingLongPress = false;
				}
				if (dealWithCM && bIsActionMenuShownNew && (Math.abs(lastY - orgY) > GlobalOptions.density * 5 || Math.abs(lastX - orgX) > GlobalOptions.density * 5)) {
					bIsActionMenuShownNew = false;
					for (ViewGroup vI : popupDecorVies) {
						//CMN.Log(vI.getChildAt(0));
						if (vI.getChildCount() == 1) {
							vI.setTag(vI.getChildAt(0));
							vI.removeAllViews();
						}
					}
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				if(bNeedPtsCnt) pts = event.getPointerCount();
				break;
			case MotionEvent.ACTION_POINTER_UP:
				if(bNeedPtsCnt) pts = event.getPointerCount()-1;
				break;
			case MotionEvent.ACTION_UP:
				if (handleLongPress) {
					removeCallbacks(mLongPressed);
					handlingLongPress = false;
				}
				isIMScrollSupressed=
				isWebHold=false;
				if(dealWithCM) {
					bIsActionMenuShownNew = bIsActionMenuShown;
					for (ViewGroup vI : popupDecorVies) {
						if (vI.getParent() != null) {
							if (vI.getChildCount() == 0 && vI.getTag() instanceof View) {
								vI.addView((View) vI.getTag());
							}
						}
					}
				}
				break;
		}
		if(isIMScrollSupressed||appBarLayout==null|| hideNoBars) {
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
				if(hideBothBars && !dragged && nestedParent.getTop()==0) {
					float dstFactor=0.45f;
					if(limitSpd&&(rawY-lastRawY)/(evTime-lastRawYTime)<1.15) {
						OrgRawY += rawY-lastRawY;
					} else // Math.abs  important
						if((dRawY)>=appBarLayout.getHeight()*dstFactor) {
							mLastMotionY = (int) (y+(dRawY-appBarLayout.getHeight()*dstFactor));
							//mLastMotionY = (int) (y-(event.getRawY() - (downRawY + appBarLayout.getHeight())));
							//mLastMotionY = (int) y;
							dragged=true;
						}
				}
				lastRawY = rawY;
				lastRawYTime = evTime;
				if(!dragged && hideBothBars) {
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
	
	public boolean selSuppressed = false;
	
	public void suppressSelection(boolean suppress) {
		if (suppress) {
			if (hasSelection()) {
				simulateScrollEffect();
				selSuppressed = true;
			}
		} else if (selSuppressed) {
			stopScrollEffect();
			selSuppressed = true;
		}
	}
	
	/** 模拟触摸，暂时关闭 contextmenu */
	public void simulateScrollEffect() {
		final long now = System.currentTimeMillis();
		final MotionEvent motion = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN,
				0.0f, 0.0f, 0);
		implView.dispatchTouchEvent(motion);
		WebCompoundListener.CustomViewHideTime = now;
		motion.setAction(MotionEvent.ACTION_MOVE);
		motion.setLocation(100, 0);
		implView.dispatchTouchEvent(motion);
		motion.recycle();
	}
	
	/** 恢复 contextmenu */
	public void stopScrollEffect() {
		final MotionEvent motion = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP,
				0.0f, 0.0f, 0);
		implView.dispatchTouchEvent(motion);
		motion.recycle();
	}
	
	private float Juli(float v, float v1) {
		return v*v+v1*v1;
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
	
	
	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		if (!WebOptions.getNoAlerts(getDelegateFlag(BackendSettings, false))) {
			listener.onDownloadStart(url, userAgent, contentDisposition, mimetype, contentLength);
		}
	}
	
	///////// AdvancedNestScrollWebView END /////////
	
	final static long StorageSettingsMask = (0x1<<3)|(0x1<<4)|(0x1<<5)|(0x1<<6);
	final static long BackendSettingsMask = (0x1<<11)|(0x1<<12)|(0x1<<8)|(0x1<<7);
	final static long ScrollSettingsMask = (0x1<<14)|(0x1<<15)|(0x1<<16)|(0x1<<17);
	final static long TextSettingsMask = (0x1<<18)|(0x1<<19)|(0x1<<20)|(0x1<<21)|(0x1<<22)|(0x1FFL<<23);
	final static long LockSettingsMask = (0x1L<<33)|(0x1L<<34)|(0x1L<<35)|(0x1L<<36);
	
	long SettingsStamp;
	public int mSettingsVersion;
	
	public void checkSettings(boolean forceCheckAll, boolean updateUa) {
		long flag = getDelegateFlag(StorageSettings, false);
		if (mSettingsVersion!=GlobalSettingsVersion || forceCheckAll) {
			if ((SettingsStamp&StorageSettingsMask)!=(flag&StorageSettingsMask)) {
				setStorageSettings();
			}
			if ((SettingsStamp&BackendSettingsMask)!=(getDelegateFlag(BackendSettings, false)&BackendSettingsMask)) {
				if (!bNeedCheckUA) {
					bNeedCheckUA = WebOptions.getPCMode(getDelegateFlag(BackendSettings, false))!=WebOptions.getPCMode(SettingsStamp);
				}
				if (updateUa && bNeedCheckUA) {
					updateUserAgentString();
				}
				setBackEndSettings();
			}
			if ((SettingsStamp&ScrollSettingsMask)!=(getDelegateFlag(ImmersiveSettings, false)&ScrollSettingsMask)) {
				if (activity!=null && activity.currentViewImpl==this) {
					activity.checkImmersiveMode();
				}
				setImmersiveScrollSettings();
			}
			if ((SettingsStamp&TextSettingsMask)!=(getDelegateFlag(TextSettings, false)&TextSettingsMask)) {
				setTextSettings(false, true);
			}
			if ((SettingsStamp&LockSettingsMask)!=(getDelegateFlag(LockSettings, false)&LockSettingsMask)) {
				setLockSettings();
			}
			CMN.Log("已应用设置变化……", mSettingsVersion, GlobalSettingsVersion, forceCheckAll, updateUa);
			mSettingsVersion = GlobalSettingsVersion;
		}
		if(WebOptions.getUseCookie(flag) != bUseCookie) { // checkUserDataStrategy
			CMN.Log("已应用Cookie变化……", WebOptions.getUseCookie(flag), bUseCookie);
			CookieManager.getInstance().setAcceptCookie(bUseCookie = WebOptions.getUseCookie(flag));
		}
	}
	
	public void setStorageSettings() {
		WebSettings settings = mWebView.getSettings();
		long flag=getDelegateFlag(StorageSettings, false)&StorageSettingsMask;
		settings.setDomStorageEnabled(WebOptions.getUseDomStore(flag));
		settings.setAppCacheEnabled(WebOptions.getUseDatabase(flag));
		settings.setDatabaseEnabled(WebOptions.getUseDatabase(flag));
		SettingsStamp &= ~StorageSettingsMask;
		SettingsStamp |= flag;
	}
	
	public void setBackEndSettings() {
		WebSettings settings = mWebView.getSettings();
		long flag=getDelegateFlag(BackendSettings, false)&BackendSettingsMask;
		settings.setBlockNetworkImage(WebOptions.getNoNetworkImage(flag));
		settings.setJavaScriptEnabled(WebOptions.getEnableJavaScript(flag));
//		settings.setUserAgentString(WebOptions.getPCMode(flag)
//				?"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36"
//				:null);
		CMN.Log("--- 设置了设置了 ---", WebOptions.getEnableJavaScript(flag), WebOptions.getNoNetworkImage(flag));
		SettingsStamp &= ~BackendSettingsMask;
		SettingsStamp |= flag;
	}
	
	public void setImmersiveScrollSettings() {
		long flag=getDelegateFlag(ImmersiveSettings, false)&ScrollSettingsMask;
		setNestedScrollingEnabled(WebOptions.getImmersiveScrollEnabled(flag));
		boolean b1=WebOptions.getImmersiveScroll_HideTopBar(flag);
		boolean b2=WebOptions.getImmersiveScroll_HideBottomBar(flag);
		hideBothBars = !(!b1&&b2);
		hideNoBars = !b1&&!b2;
		
		// 滑动隐藏底栏  滑动隐藏顶栏 0
		// 底栏不动  滑动隐藏顶栏 1
		// 底栏不动  顶栏不动 2
		// 滑动隐藏底栏  顶栏不动 3
//		int hideBarType = 2;
//		boolean PadPartPadBar = false;
//		syncBarType(hideBarType, PadPartPadBar, activity.UIData);
		
		SettingsStamp &= ~ScrollSettingsMask;
		SettingsStamp |= flag;
	}
	
	public void setTextSettings(boolean init, boolean setTextZoom) {
		long flag=getDelegateFlag(TextSettings, false)&TextSettingsMask;
		WebSettings settings = mWebView.getSettings();
		boolean b = WebOptions.getTextTurboEnabled(flag);
		init |= WebOptions.getTextTurboEnabled(SettingsStamp)!=b;
		if (init || WebOptions.getOverviewMode(SettingsStamp)!=WebOptions.getOverviewMode(flag))
		{
			boolean  b1 = !b || WebOptions.getOverviewMode(flag);
			settings.setLoadWithOverviewMode(b1);
			settings.setUseWideViewPort(b1);
			//CMN.Log("setTextSettings::", b1, WebOptions.getTextTurboEnabled(flag), WebOptions.getOverviewMode(flag));
		}
		if (setTextZoom && (init || WebOptions.getTextZoom(SettingsStamp)!=WebOptions.getTextZoom(flag)) )
		{
			if (activity.opt.getUpdateTextZoomOnPageSt()) {
				bNeedCheckTextZoom = WebOptions.getTextZoom(flag);
			} else {
				settings.setTextZoom(b?Math.min(500, Math.max(15, WebOptions.getTextZoom(flag))):110);
				CMN.Log("setTextZoom::", WebOptions.getTextZoom(flag));
			}
		}
		SettingsStamp &= ~TextSettingsMask;
		SettingsStamp |= flag;
	}
	
	public void setLockSettings() {
		long flag=getDelegateFlag(LockSettings, false)&LockSettingsMask;
		boolean lock = WebOptions.getLockEnabled(flag);
		lockX = lock && WebOptions.getLockX(flag);
		lockY = lock && WebOptions.getLockY(flag);
		bNeedPtsCnt = lockX || lockY;
		SettingsStamp &= ~LockSettingsMask;
		SettingsStamp |= flag;
	}
	
	// todo battery optimise
	public void setTextZoom() {
		int ts = WebOptions.getTextZoom(getDelegateFlag(TextSettings, false));
		WebSettings settings = mWebView.getSettings();
		CMN.Log("setTextZoom 1::", bRecentNewDomainLnk, ts=Math.min(500, Math.max(15, ts)), settings.getTextZoom());
		if (settings.getTextZoom()!=ts || bRecentNewDomainLnk>0 && activity.opt.getSetTextZoomAggressively())
		{
			if(bRecentNewDomainLnk>0 && activity.opt.getSetTextZoomAggressively()) {
				settings.setTextZoom(ts-1);
			}
			settings.setTextZoom(ts);
		}
		bNeedCheckTextZoom = 0;
	}
	
	public long getDelegateFlag(int section, boolean bWillChange) {
		switch(section) {
			case StorageSettings:
				if(domainInfo.getApplyOverride_group_storage()) return domainInfo.f1;
				if(holder.getApplyOverride_group_storage()) return holder.flag;
			break;
			case BackendSettings:
				if(domainInfo.getApplyOverride_group_client()) return domainInfo.f1;
				if(holder.getApplyOverride_group_client())     return holder.flag;
			break;
			case ImmersiveSettings:
				if(domainInfo.getApplyOverride_group_scroll()) return domainInfo.f1;
				if(holder.getApplyOverride_group_scroll())     return holder.flag;
			break;
			case TextSettings:
				if(domainInfo.getApplyOverride_group_text()) return domainInfo.f1;
				if(holder.getApplyOverride_group_text())     return holder.flag;
			break;
			case LockSettings:
				if(domainInfo.getApplyOverride_group_lock()) return domainInfo.f1;
				if(holder.getApplyOverride_group_lock())     return holder.flag;
			break;
		}
		if (bWillChange) {
			GlobalSettingsVersion ++;
		}
		return Options.ThirdFlag;
	}
	
	public int getDelegateFlagIndex(int section) {
		switch(section) {
			case StorageSettings:
				if(domainInfo.getApplyOverride_group_storage()) return WebViewSettingsSource_DOMAIN;
				if(holder.getApplyOverride_group_storage()) return WebViewSettingsSource_TAB;
			break;
			case BackendSettings:
				if(domainInfo.getApplyOverride_group_client()) return WebViewSettingsSource_DOMAIN;
				if(holder.getApplyOverride_group_client()) return WebViewSettingsSource_TAB;
			break;
			case ImmersiveSettings:
				if(domainInfo.getApplyOverride_group_scroll()) return WebViewSettingsSource_DOMAIN;
				if(holder.getApplyOverride_group_scroll()) return WebViewSettingsSource_TAB;
			break;
			case TextSettings:
				if(domainInfo.getApplyOverride_group_text()) return WebViewSettingsSource_DOMAIN;
				if(holder.getApplyOverride_group_text()) return WebViewSettingsSource_TAB;
			break;
			case LockSettings:
				if(domainInfo.getApplyOverride_group_lock()) return WebViewSettingsSource_DOMAIN;
				if(holder.getApplyOverride_group_lock()) return WebViewSettingsSource_TAB;
			break;
		}
		return 3;
	}
	
	public void setDelegateFlag(int section, long tmpFlag) {
		switch(section) {
			case StorageSettings:
				if(domainInfo.getApplyOverride_group_storage()) domainInfo.f1=tmpFlag;
				else if(holder.getApplyOverride_group_storage()) holder.flag=tmpFlag;
				else break;
				return;
			case BackendSettings:
				if(domainInfo.getApplyOverride_group_client()) domainInfo.f1=tmpFlag;
				else if(holder.getApplyOverride_group_client()) holder.flag=tmpFlag;
				else break;
				return;
			case ImmersiveSettings:
				if(domainInfo.getApplyOverride_group_scroll()) domainInfo.f1=tmpFlag;
				else if(holder.getApplyOverride_group_scroll()) holder.flag=tmpFlag;
				else break;
				return;
			case TextSettings:
				if(domainInfo.getApplyOverride_group_text()) domainInfo.f1=tmpFlag;
				else if(holder.getApplyOverride_group_text()) holder.flag=tmpFlag;
				else break;
				return;
			case LockSettings:
				if(domainInfo.getApplyOverride_group_lock()) domainInfo.f1=tmpFlag;
				else if(holder.getApplyOverride_group_lock()) holder.flag=tmpFlag;
				else break;
				return;
		}
		Options.ThirdFlag = tmpFlag;
	}
	
	/** @param hideBarType 滑动隐藏底栏  滑动隐藏顶栏 0
	 *   底栏不动  滑动隐藏顶栏 1
	 *   底栏不动  顶栏不动 2
	 *   滑动隐藏底栏  顶栏不动 3  */
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
		Utils.removeView(this);
		removeAllViews();
		if(mWebView!=null) mWebView.destroy();
	}
	
	public Bitmap getBitmap() {
		return bm.get();
	}
	
	public void resetBitmap() {
		bm.clear();
	}
	
	
	final static HashMap<SubStringKey, DomainInfo> domainInfoMap = new HashMap<>();

	final static HashMap<String, Note> noteMap = new HashMap<>();
	
	static class Note {
		final long id;
		final String url;
		String data;
		public int edit;
		Note(String url, long id, String data, int edit) {
			this.id = id;
			this.url = url;
			this.data = data;
			this.edit = edit;
		}
	}
	final static Note DummyNote = new Note("", -1, null, 0);
	private Note mNote = DummyNote;

	
//	public void saveNote() {
//		if (mNote!=DummyNote) {
//			saveNote(mNote.data, null);
//		}
//	}
	
	public void saveNote(String data, String text, int textId, int type, String cols) {
		String url = holder.url;
		Note note = mNote.url.equals(url)?mNote:null;
		SQLiteDatabase db = activity.historyCon.getDB();
		if(db.isOpen()) {
			long now = CMN.now();
			try {
				ContentValues values = new ContentValues();
				values.put(FIELD_URL, url);
				byte[] dataArr = data.getBytes();
				byte[] new_data = BU.zlib_compress(dataArr);
				CMN.Log("压缩前长度：："+dataArr.length, "压缩后长度：："+new_data.length);
				values.put("notes", new_data);
				if (holder.url_id!=-1) {
					values.put(FIELD_URL_ID, holder.url_id);
				}
				values.put(FIELD_LAST_TIME, now);
				if (note==null) {
					values.put(FIELD_CREATE_TIME, now);
					long id = db.insert(TABLE_ANNOTS, null, values);
					noteMap.put(url, mNote=/* 新的笔记 */new Note(url, id, data, 1));
				} else {
					note.data = data;
					values.put("edit", ++note.edit);
					db.update(TABLE_ANNOTS, values, "id=?", new String[]{Long.toString(note.id)});
				}
				CMN.pt("保存了……", holder.url, CMN.now()-now);
				if (text!=null && mNote!=null) {
					activity.historyCon.createWebAnnotationTextTable();
					values = new ContentValues();
					values.put("note_id", mNote.id);
					values.put("text_id", textId);
					values.put("tab_id", holder.id);
					values.put("text", text);
					values.put("type", type);
					values.put("cols", cols);
					values.put(FIELD_CREATE_TIME, now);
					db.insertWithOnConflict(TABLE_ANNOTS_TEXT, null, values, SQLiteDatabase.CONFLICT_REPLACE);
				}
			} catch (Exception e) { CMN.Log(e); }
		}
	}
	
	public long preQueryNote(String url) {
		if (!mNote.url.equals(url)) {
			mNote = noteMap.get(url);
			if (mNote==null) {
				mNote = DummyNote;
			}
		}
		return mNote.id;
	}
	
	public Cursor queryNoteInDB(String url_key) {
		final String sql = "select id,notes,edit from annots where url=?";
		return activity.historyCon.getDB().rawQuery(sql, new String[]{url_key});
	}
	
	public String queryNote(String url) {
		CMN.rt();
		if (!mNote.url.equals(url)) {
			mNote = DummyNote;
			try (Cursor c = queryNoteInDB(url)) {
				Note note = null;
				if(c.getCount()>0) {
					c.moveToFirst();
					String text = null;
					// legacy support for un zipped format. ( deprecating )
					try {
						text = c.getString(1);
						if(!(text.length()>2 && text.charAt(2)=='/' && IU.parsint(text.substring(1,2), -1)>=0)) {
							text = null;
						}
					} catch (Exception e) { CMN.Log(e); }
					if(text == null) text = new String(BU.zlib_decompress(c.getBlob(1), 0));
					note=/* 读取笔记 */new Note(url, c.getLong(0), text, c.getInt(2));
				}
				if (note!=null) {
					noteMap.put(url, mNote = note);
				}
				CMN.pt("搜索完毕...", mNote.data);
			} catch (Exception e) { CMN.Log(e); }
		}
		return mNote.data;
	}
	
	// 设置变化、SOUL、刷新、OPS、加载网页。
	// 搜索引擎列表，导航页，需要域数据库记录的图标。
	// 常规访问，updateua (loadurl、刷新、onpagestart之时) 需要域数据库记录的配置信息。
	public boolean queryDomain(String url, boolean updateUa) {
		if (!domain.matches(url))
		{
			SubStringKey domainKey = SubStringKey.new_hostKey(url);
			CMN.Log("queryDomain::", CMN.tid(), domainKey, url);
			SubStringKey last_domain = domainInfo.domainKey;
			long flag = domainInfo.f1;
			DomainInfo domainInfo = domainInfoMap.get(domainKey);
			if (domainInfo!=null) {
				setDomainInfo(domainInfo.domainKey, domainInfo);
			} else {
				String domain = domainKey.toString();
				domainKey = SubStringKey.fast_hostKey(domain);
				try(Cursor infoCursor = LexicalDBHelper.getInstancedDb()
						.rawQuery("select * from domains where url = ? ", new String[]{domain})){
					if (infoCursor.moveToFirst()) {
						domainInfo = new DomainInfo(domainKey, infoCursor.getLong(0), infoCursor.getLong(3));
					} else {
						domainInfo = new DomainInfo(domainKey, 0, 0);
					}
					domainInfoMap.put(domainKey, domainInfo);
					setDomainInfo(domainKey, domainInfo);
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
			if (this.domainInfo.f1!=flag) {
				if ((this.domainInfo.f1&PC_MODE_MASK)!=(flag&PC_MODE_MASK) && mWebView.getProgress()<100) {
					// 防止一侧设置PC模式后，www <-> m 反复重载 | avoid circular auto redirection for the same 'domain'
					if (last_domain.isSameTopDomain(this.domain)) {
						//CMN.Log("复制同顶级域名的用户设置：：");
						this.domainInfo.f1 &= ~PC_MODE_MASK;
						this.domainInfo.f1 |= flag&PC_MODE_MASK;
					}
				}
				checkSettings(true, updateUa); // 域名变化
			}
			if (activity!=null && activity.settingsPanel!=null) {
				activity.settingsPanel.refresh();
			}
			return true;
		}
		return false;
	}
	
	public boolean updateUserAgentString() {
		boolean pcMode = getPCMode();
		//pcMode = url!=null && url.contains("www.baidu");
		//pcMode = true;
		String target_ua;
		if(pcMode) {
			target_ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36";
			target_ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";
		} else {
			//CMN.Log("android_ua", android_ua);
			target_ua = android_ua;
			//target_ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";
			//targetUa = default_ua;
		}
		targetUa = target_ua;
		WebSettings settings = mWebView.getSettings();
		bNeedCheckUA = false;
		if(!TextUtils.equals(settings.getUserAgentString(), target_ua)) {
			CMN.Log("测试 ua 设置处……", CMN.tid(), target_ua);
			settings.setUserAgentString(target_ua);
			//mWebView.stopLoading();
			return true;
		}
		return false;
	}
	
	/** 从磁盘加载网页前进/回退栈 */
	public boolean lazyLoad() {
		CMN.Log("loadIfNeeded", holder.url);
		if(!stackloaded) {
			queryDomain(holder.url, false);
			updateUserAgentString();
			stackloaded = true;
			try(Cursor stackcursor = LexicalDBHelper.getInstancedDb()
					.rawQuery("select webstack from webtabs where id=? limit 1"
							, new String[]{""+holder.id})) {
				if(stackcursor.moveToFirst()
						&&parseBundleFromData(stackcursor.getBlob(0))) {
					return true;
				}
			} catch(Exception e) { CMN.Log(e); }
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
			//bundle.clear(); mWebView.saveState(bundle); stacks = mWebView.restoreState(bundle);
			CMN.Log("云飞扬……", stacks);
			if(stacks!=null && stacks.getSize()>0) {
				//mWebView.reload();
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
		domainInfo.checkDirty();
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
	public boolean recaptureBitmap() {
		if(implView instanceof XWalkWebView) {
			((XWalkWebView)implView).captureBitmapAsync(new XWalkGetBitmapCallback() {
				@Override
				public void onFinishGetBitmap(Bitmap bitmap, int var2) {
					doCaptureBitmap(bitmap);
				}
			});
			return false;
		}
		doCaptureBitmap(null);
		return true;
	}
	
	private void doCaptureBitmap(Bitmap bitmap) {
		holder.lastCaptureVer = holder.version;
		int w = implView.getWidth();
		int h = implView.getHeight();
		//w = getMeasuredWidth();
		//h = getMeasuredHeight()-getPaddingTop()-getPaddingBottom();
		CMN.Log("recaptureBitmap...", w, h, holder.id, holder.version);
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
			//reset |= bmItem==null||bmItem.getWidth()!=targetW||bmItem.getHeight()!=targetH;
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
			//canvas = new Canvas(bmItem);
			canvas.setMatrix(Utils.IDENTITYXIRTAM);
			long st = CMN.now();
			
			if(bitmap==null) {
				canvas.scale(factor, factor);
				canvas.translate(-implView.getScrollX(), -implView.getScrollY());
				mWebView.drawToBitmap(canvas);
				bm = new WeakReference<>(bmItem);
			} else {
				canvas.drawBitmap(bitmap, null, new RectF(0,0, bmItem.getWidth(), bmItem.getHeight()), null);
				bm = new WeakReference<>(bmItem);
				activity.onBitmapCaptured(WebFrameLayout.this, pendingBitCapRsn);
				pendingBitCapRsn = -1;
			}
			CMN.Log("绘制时间：", CMN.now()-st);
			
		}
	}
	
	public Bitmap saveBitmap(boolean shouldWait) {
		Bitmap bmItem = bm.get();
		if(bmItem==null) {
			return null;
		}
		long st = System.currentTimeMillis();
		ReusableByteOutputStream bos1 = WebViewHelper.bos1;
		if(bos1==null) {
			return null;
		}
		if(shouldWait) {
			int slpCnt = 5;
			while(pendingBitCapRsn>=0&&slpCnt-->0) {
				try {
					// CMN.Log("睡眠……"); // 睡你丫的
					Thread.sleep(50);
				} catch (InterruptedException ignored) { }
			}
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
		values.put("f1", holder.flag);
		values.put("thumbnail", data);
		LexicalDBHelper.getInstancedDb().update("webtabs", values, "id=?", new String[]{String.valueOf(tabID_Fetcher)});
		CMN.Log(tabID_Fetcher, "入表时间：", System.currentTimeMillis()-st, data.length, (int) (bmItem.getAllocationByteCount()*0.50), bos1.data().length);
		bos1.close();
		return bmItem;
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!hideForTabView) {
			// WebView has displayed some content and is scrollable.
			//CMN.Log("onNewPicture!!!  v2 ", CMN.now());
		}
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
	
	public int getViewHeight() {
		int ret = implView.getHeight();
		if(ret==0) {
			ret = activity.UIData.webcoord.getHeight()-getPaddingBottom()-getPaddingTop();
		}
		//CMN.Log("getViewHeight::", holder.title);
		//CMN.Log("getViewHeight::", activity.UIData.webcoord.getHeight(), getPaddingBottom(), getPaddingTop());
		return ret==0?activity.dm.heightPixels:ret;
	}
	
	public int getViewWidth() {
		int ret = implView.getWidth();
		if(ret==0) {
			ret = activity.UIData.webcoord.getWidth();
		}
		return ret==0?activity.dm.widthPixels:ret;
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
	
	public boolean hasSelection() {
		return bIsActionMenuShown || popupDecorVies.size()==2 && popupDecorVies.get(0).getParent()!=null;
	}
	
	public void addModifiers(Pair<Pattern, Pair<String, String>>[] modifiers) {
		if (modifiers!=null && modifiers.length>0) {
			if(this.modifiers==null) {
				this.modifiers = Collections.synchronizedList(new ArrayList<>());
			}
			this.modifiers.addAll(Arrays.asList(modifiers));
		}
	}
	
	public boolean getPCMode() {
		return WebOptions.getPCMode(getDelegateFlag(BackendSettings, false));
	}
	
	public boolean getUseCookie() {
		return !WebOptions.getUseCookie(getDelegateFlag(StorageSettings, false));
	}
	
	public boolean getPremature() {
		return WebOptions.getPremature(getDelegateFlag(BackendSettings, false));
	}
	
	
	public boolean getApplyTabRegion(int groupID) {
		switch (groupID) {
			case StorageSettings: return holder.getApplyOverride_group_storage();
			case BackendSettings: return holder.getApplyOverride_group_client();
		}
		return false;
	}
	
	public boolean getApplyDomainRegion(int groupID) {
		switch (groupID) {
			case StorageSettings: return domainInfo.getApplyOverride_group_storage();
			case BackendSettings: return domainInfo.getApplyOverride_group_client();
		}
		return false;
	}
	
	public void setDomainInfo(SubStringKey domainKey, DomainInfo info) {
		domainInfo.checkDirty();
		domain = domainKey;
		domainInfo = info;
	}
	
	public long getDomainFlag() {
		return domainInfo.f1;
	}
	
	public void setDomainFlag(long val) {
		domainInfo.updateFlag(val);
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
		mWebView.stopLoading();
		//mWebView.pauseTimers();
		//mWebView.onPause();
	}
	
	public void resumeWeb() {
		//mWebView.resumeTimers();
		//mWebView.onResume();
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
			if(mWebView instanceof WebView) {
				listener.onPageFinished((WebView) mWebView, mWebView.getUrl());
			} else {
				try {
					listener.onPageFinished(getLockedView(mWebView, true), mWebView.getUrl());
				} catch (Exception e) {
					Log(e);
				}
				unlock();
			}
		}
	};
	
	/** 加快显示加载完成。 see {@link #EnRipenPercent} */
	public void postFinished() {
		if(!PageFinishedPosted) {
			PageFinishedPosted=true;
			if (postTime>0) {
				postDelayed(OnPageFinishedNotifier, postTime);
				postTime = 0;
			} else {
				post(OnPageFinishedNotifier);
				//postDelayed(OnPageFinishedNotifier, 350);
			}
		}
	}
	
	public void removePostFinished() {
		removeCallbacks(OnPageFinishedNotifier);
	}
	
	public void setWebViewClient(WebCompoundListener listener) {
		this.listener = listener;
		mWebView.setWebViewClient(listener);
		mWebView.setWebChromeClient(listener.mWebClient);
		mWebView.setDownloadListener(this);
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
		CMN.Log("onMenuItemClick", item.getClass(), item.getTitle(), item.getItemId(), android.R.id.copy);
		//CMN.Log("onActionItemClicked");
		int id = item.getItemId();
		switch(id) {
			case R.id.plaindict:{
				activity.handleVersatileShare(21);
			} return true;
			case R.id.web_highlight: {
				CMN.Log("点击了高亮按钮！");
				HLED=true;
				listener.ensureMarkJS(activity);
				mWebView.evaluateJavascript(WebViewHelper.getInstance()
						.getHighLightIncantation(1, 0xFFFFAAAAL, null), null);
				mWebView.evaluateJavascript("console.log(123456);", null);
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
	
	public final ArrayList<ViewGroup> popupDecorVies = new ArrayList<>();
	
	public void refresh() {
		if(mWebView!=null) {
			mWebView.reload();
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	public class AdvancedWebViewCallback extends ActionMode.Callback2 {
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
			CMN.Log("onMenuItemClick MenuLongClicker!!!", v.getId());
			switch (v.getId()) {
				case R.id.web_highlight:
					HLED=true;
					listener.ensureMarkJS(activity);
					mWebView.evaluateJavascript(WebViewHelper.getInstance()
							.getHighLightIncantation(1, 0, null), null);
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
