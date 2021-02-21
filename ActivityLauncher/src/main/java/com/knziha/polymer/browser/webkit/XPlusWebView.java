package com.knziha.polymer.browser.webkit;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewExtension;
import com.tencent.smtt.export.external.extension.proxy.ProxyWebViewClientExtension;
import com.tencent.smtt.export.external.interfaces.ISelectionInterface;
import com.tencent.smtt.export.external.interfaces.IX5WebViewBase;
import com.tencent.smtt.export.external.interfaces.WebResourceRequest;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.WebView;

import java.io.IOException;
import java.util.Map;

// X+
public class XPlusWebView extends WebView implements UniversalWebviewInterface {
	WebFrameLayout layout;
	
	ActionMode actionMode;
	View actionView;
	
	public XPlusWebView(Context context) throws IOException {
		super(wrapInitCtx(context));
		
		IX5WebViewExtension x5WebViewExtension = getX5WebViewExtension();
		if (x5WebViewExtension != null) {
			x5WebViewExtension.setWebViewClientExtension(new WebViewExtension());
			x5WebViewExtension.setSelectListener(new ISelectionInterfaceX());
		}
		
		setOnTouchListener((v, event) -> {
			//CMN.Log("onTouch");
			layout.handleSimpleNestedScrolling(layout, layout, event);
			return false;
		});
	}
	
	private static Context wrapInitCtx(Context context) {
		QbSdk.initX5Environment(context.getApplicationContext(),  null);
		return context;
	}
	
	private class WebViewExtension extends ProxyWebViewClientExtension {
		public void invalidate() {
		}
		public void onScrollChanged(int l, int t, int oldl, int oldt) {
			//super.onScrollChanged(l, t, oldl, oldt);
			//CMN.Log("onScrollChanged!!!");
			//CMN.Log(lastScroll, "onScrollChanged", l, t, oldl, oldt); //有的网页监听不到
			//version++;
			if(layout!=null&&!Options.getAlwaysRefreshThumbnail() && Math.abs(layout.lastThumbScroll-t)>100){
				layout.lastThumbScroll=t;
			}
			if(mOnScrollChangeListener!=null) {
				mOnScrollChangeListener.onScrollChange(XPlusWebView.this,l,t,oldl,oldt);
			}
		}
		@Override
		public boolean onShowLongClickPopupMenu() {
			// https://github.com/SjAndy88/X5-ActionMode
			// 腾讯X5内核实现长按弹出自定义菜单方法
			// 文章是屏蔽了长按弹框，通过js实现自定义弹框，但我们是通过原生实现
			postDelayed(() -> getX5WebViewExtension().enterSelectionMode(false), 30);
			return true;
		}
	}
	
	// x5相关api介绍
	// https://x5.tencent.com/docs/tbsapi.html
	private class ISelectionInterfaceX implements ISelectionInterface {
		@Override
		public void onSelectionChange(Rect rect, Rect rect1, int i, int i1, short i2) {
			System.out.println();
		}
		@Override
		public void onSelectionBegin(Rect rect, Rect rect1, int i, int i1, short i2) {
			System.out.println();
		}
		@Override
		public void onSelectionBeginFailed(int i, int i1) {
			System.out.println();
		}
		@Override
		public void onSelectionDone(Rect rect, boolean b) {
			System.out.println();
		}
		@Override
		public void hideSelectionView() {
			System.out.println();
			if (actionView != null) {
				removeViewInLayout(actionView);
				actionView = null;
			}
			if (actionMode != null) {
				actionMode.finish();
				actionMode = null;
			}
		}
		@Override
		public void onSelectCancel() {
			System.out.println();
		}
		@Override
		public void updateHelperWidget(Rect rect, Rect rect1) {
			System.out.println();
			post(() -> {
				Context context = getContext();
				
				ActionMode.Callback callback = new ActionMode.Callback() {
					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						// 引入新的menu
						mode.getMenuInflater().inflate(R.menu.webview_context_menu, menu);
						return true;
					}
					
					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						return false;
					}
					
					@Override
					public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
						IX5WebViewExtension webViewExtension = getX5WebViewExtension();
						String selectionText = "";
						if (webViewExtension != null) {
							selectionText = webViewExtension.getSelectionText();
						}
						boolean leaveSelection = true;
						switch (item.getItemId()) {
							case R.id.ctx_copy:
								layout.copyText(selectionText);
								break;
							default:
								leaveSelection = layout.onMenuItemClick(item);
							break;
						}
						boolean finalLeaveSelection = leaveSelection;
						postDelayed(() -> {
							if (finalLeaveSelection && webViewExtension != null) {
								webViewExtension.leaveSelectionMode();
							}
						}, 30);
						return true;
					}
					
					@Override
					public void onDestroyActionMode(ActionMode mode) {
						if(layout!=null) {
							layout.bIsActionMenuShown = false;
						}
					}
				};
				
				// 生成actionView，并通过它启动ActionMode，解决定位问题
				// actionView的大小和位置，大致和Selection相同，直接将其add到WebView中
				if (actionView != null) {
					removeViewInLayout(actionView);
				}
				
				actionView = new View(context);
				//actionView.setBackgroundColor(0x33FF00FF);// 方便调试
				int width = rect1.right - rect.left;
				int height = rect1.bottom - rect.top;
				LayoutParams lp = new LayoutParams(width <= 0 ? 10 : width, height <= 0 ? 10 : height);
				lp.leftMargin = rect.left;
				lp.topMargin = rect.top;
				addView(actionView, lp);
				
				// 需要延迟startActionMode，给布局actionView的时间
				actionView.post(() -> {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
						actionMode = actionView.startActionMode(callback, ActionMode.TYPE_FLOATING);
					} else {
						actionMode = actionView.startActionMode(callback);
					}
				});
				if(layout!=null) {
					layout.bIsActionMenuShown = true;
				}
			});
		}
		
		@Override
		public void setText(String s, boolean b) {
			System.out.println();
		}
		
		@Override
		public String getText() {
			System.out.println();
			return null;
		}
		
		@Override
		public void onRetrieveFingerSearchContextResponse(String s, String s1, int i) {
			System.out.println();
		}
	}
	
	
	
	@Override
	public void setLayoutParent(WebFrameLayout layout, boolean addView) {
		this.layout = layout;
		layout.setImplementation(this);
		if(addView) {
			layout.addView(this);
		}
	}
	
	public void setOnScrollChangedListener(RecyclerView.OnScrollChangedListener onSrollChangedListener) {
		mOnScrollChangeListener =onSrollChangedListener;
		//X5WebView.getView().setOnScrollChangeListener(mOnScrollChangeListener);
	}
	
	@Override
	public Map<String, String> getLastRequestHeaders() {
		WebResourceRequest wrr = this.wrr;
		CMN.tryUnLock();
		if(wrr==null) {
			return null;
		}
		Map<String, String> ret = wrr.getRequestHeaders();
		ret.put("Method", wrr.getMethod());
		ret.put("Url", wrr.getUrl().toString());
		return ret;
	}
	
	@Override
	public Object initPrintDocumentAdapter(String name) {
		return X5WebView.createPrintDocumentAdapter(name);
	}
	
	RecyclerView.OnScrollChangedListener mOnScrollChangeListener;
	
	@Override
	public void SafeScrollTo(int x, int y) {
	
	}
	
	@Override
	public void scrollBy(int x, int y) {
		getView().scrollBy(x, y);
	}
	
	@Override
	public Object getHitResultObject() {
		return getX5HitTestResult();
	}

//	static class xx extends android.webkit.WebView.HitTestResult{
//		xx(){
//			super();
//		}
//	}
	
	@Override
	public int getHitType(Object ret) {
		return ((IX5WebViewBase.HitTestResult)ret).getType();
	}
	
	@Override
	public String getHitExtra(Object ret) {
		return ((IX5WebViewBase.HitTestResult)ret).getExtra();
	}

//	@Override
//	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
//		CMN.Log("startActionMode???");
//		return super.startActionMode(callback, type);
//	}
//
//	@Override
//	public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback, int type) {
//		CMN.Log("startActionModeForChild???");
//		return super.startActionModeForChild(originalView, callback, type);
//	}
	
	
	@Override
	public void clearFocus() {
		//X5WebView.getView().clearFocus();
		if(actionMode!=null) {
			actionMode.finish();
		}
		evaluateJavascript("getSelection().collapseToStart()", null);
	}
}
