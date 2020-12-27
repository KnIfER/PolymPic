package com.knziha.polymer.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView.OnScrollChangedListener;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.knziha.polymer.WebCompoundListener.ensureMarkJS;
import static com.knziha.polymer.widgets.Utils.getWindowManagerViews;

public class WebViewmy extends WebView implements MenuItem.OnMenuItemClickListener {
	public static float minW;
	public BrowserActivity.TabHolder holder;
	public BrowserActivity context;
	public long time;
	public int lastCaptureVer;
	public int version;
	public int lastScroll;
	public boolean stackloaded;
	public File stackpath;
	private boolean invalidable = true;
	public boolean HLED;
	public float lastX;
	public float lastY;
	public float orgX;
	public float orgY;
	public static Integer ShareString_Id;
	public static Integer SelectString_Id;
	public static Integer CopyString_Id;
	private boolean bIsActionMenuShownNew;
	
	private static String[] SangeHuoQiangShou;
	private final static int[] DuiYinChengSanRen=new int[]{R.id.toolbar_action0, R.id.toolbar_action1, R.id.toolbar_action3, R.id.plaindict};
	private static boolean bAdvancedMenu=Build.VERSION.SDK_INT>=Build.VERSION_CODES.M;
	public boolean isWebHold;
	public boolean isIMScrollSupressed;
	
	public WebViewmy(Context context) {
		super(context, null, 0);
		this.context=(BrowserActivity) context;
	}

	public int getContentHeight(){
		return computeVerticalScrollRange();
	}
	
	public int getContentOffset(){
		return this.computeVerticalScrollOffset();
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		//CMN.Log(lastScroll, "onScrollChanged", l, t, oldl, oldt); //有的网页监听不到
		//version++;
		if(!Options.getAlwaysRefreshThumbnail() && Math.abs(lastScroll-t)>100){
			lastScroll=t;
		}
		if(mOnScrollChangeListener !=null)
			mOnScrollChangeListener.onScrollChange(this,l,t,oldl,oldt);
	}
	
	public void setOnScrollChangedListener(OnScrollChangedListener onSrollChangedListener) {
		mOnScrollChangeListener =onSrollChangedListener;
	}
	OnScrollChangedListener mOnScrollChangeListener;
	
	public void SafeScrollTo(int x, int y) {
		OnScrollChangedListener mScrollChanged = mOnScrollChangeListener;
		mOnScrollChangeListener =null;
		scrollTo(x, y);
		mOnScrollChangeListener =mScrollChanged;
	}
	
	public boolean isloading=false;
	@Override
	public void loadDataWithBaseURL(String baseUrl,String data,String mimeType,String encoding,String historyUrl) {
		super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
		//if(!baseUrl.equals("about:blank"))
		isloading=true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int ow, int oh) {
		super.onSizeChanged(w, h, ow, oh);
		//CMN.Log("onSizeChanged  ");
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//CMN.Log("onMeasure  ");
	}

	@Override
	public void loadUrl(String url) {
		super.loadUrl(url);
		//CMN.Log("loadUrl: "+url.equals("about:blank"));
		//if(!url.equals("about:blank"))
		isloading=true;
	}

	@Override
	protected void onCreateContextMenu(ContextMenu menu){
		//Toast.makeText(getContext(), "ONCCM", 0).show();
		super.onCreateContextMenu(menu);
	}

	public boolean bIsActionMenuShown;
	public AdvancedWebViewCallback webviewcallback;
	
	/** Recapture Thumnails as a bitmap. There's no point in doing this asynchronously.
	 * 		draw(canvas) will block the UI even it's called in another thread. */
	public void recaptureBitmap() {
		lastCaptureVer = version;
		int w = getWidth();
		int h = getHeight();
		if(w>0) {
			float factor = 1;
			if(w>minW) {
				factor = minW/w;
			}
			int targetW = (int)(w*factor);
			int targetH = (int)(h*factor);
			int needRam = targetW*targetH*2;
			boolean reset = bitmap.getAllocationByteCount()<needRam;
			if(reset) {
				bitmap.recycle();
				bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.RGB_565);
			} else if(bitmap.getWidth()!=targetW||bitmap.getHeight()!=targetH) {
				bitmap.reconfigure(targetW, targetH, Bitmap.Config.RGB_565);
			}
			if(canvas==null) {
				canvas = new Canvas(bitmap);
			} else if(reset) {
				canvas.setBitmap(bitmap);
			}
			canvas.setMatrix(Utils.IDENTITYXIRTAM);
			canvas.scale(factor, factor);
			canvas.translate(-getScrollX(), -getScrollY());
			long st = System.currentTimeMillis();
			draw(canvas);
			CMN.Log("绘制时间：", System.currentTimeMillis()-st);
			Bitmap bmItem = bm.get();
			if(bmItem!=null) {
				bmItem.recycle();
			}
			// now copy the result to an unique bitmap cache.
			bm = new WeakReference<>(bitmap.copy(Bitmap.Config.RGB_565, false));
			CMN.Log("复制时间：", System.currentTimeMillis()-st);
		}
	}
	
	public static Bitmap bitmap = Bitmap.createBitmap(1,1,Bitmap.Config.RGB_565);
	
	public WeakReference<Bitmap> bm = Utils.DummyBMRef;
	public Canvas canvas;
	static Bitmap b = Bitmap.createBitmap(1,1,Bitmap.Config.RGB_565);
	static long bindId;
	static Canvas c = new Canvas(b);
	@SuppressLint("WrongCall")
	public Bitmap getBitmap() {
//		if(bindId==holder.id) {
//			return b;
//		}
		long id = Thread.currentThread().getId();
		CMN.Log("getting bitmap from webview...", id);
		
		int w = getWidth();
		
		int h = getHeight();
		
		if(w>0) {
			float factor = 1;
			if(w>minW) {
				factor = minW/w;
			}
			//CMN.Log("getting scale factor", factor);
			Bitmap.Config config = Bitmap.Config.RGB_565;
			int targetW = (int)(w*factor);
			int targetH = (int)(h*factor);
			if(bindId!=holder.id) {
				CMN.Log("new bitmap");
				b = Bitmap.createBitmap(targetW, targetH, config);
				c.setBitmap(b);
				bindId=holder.id;
			}
			if(b.getWidth()!=targetW||b.getHeight()!=targetH) {
				//b.recycle();
				b = Bitmap.createBitmap(targetW, targetH, config);
				c.setBitmap(b);
			}
			c.setMatrix(Utils.IDENTITYXIRTAM);
			c.scale(factor, factor);
			//c.translate(-getScrollX(), -getScrollY());
			
			//c.setDensity(10000);
			//PaintFlagsDrawFilter df = new PaintFlagsDrawFilter(0, 0);
			//c.setDrawFilter(df);
			long st = System.currentTimeMillis();
//			Picture p = capturePicture();
			//CMN.Log("capture时间：", System.currentTimeMillis()-st);
			//drawBitmap(c);
			//onDraw(c);
			CMN.Log("绘制时间：", System.currentTimeMillis()-st);
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//			}
			return b;
		}
		return null;
	}
	
	public void incrementVerIfAtNormalPage() {
		if(!"网页无法打开".equals(getTitle())) {
			version++;
		}
	}
	
	@SuppressLint("NewApi")
	private class AdvancedWebViewCallback extends ActionMode.Callback2 {
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
		
		/* 📕📕📕 微空间内爆术 📕📕📕 */
		public View LookForANobleSteedCorrespondingWithDrawnClasses(View donkeySteed, int dynamicFrom, Class<?>...classes) {
			if(classes[0].isInstance(donkeySteed)) {
				ViewGroup vg;
				for (int i = 1;i < classes.length; i++) {
					vg=(ViewGroup) donkeySteed;
					donkeySteed = vg.getChildAt(0);
					if(i>=dynamicFrom) {
						int j=0;
						int cc=vg.getChildCount();
						while(!classes[i].isInstance(donkeySteed)&&++j<cc) {
							donkeySteed = vg.getChildAt(j);
						}
					}
					if(!classes[i].isInstance(donkeySteed)) {
						return null;
					}
				}
				return donkeySteed;
			}
			return null;
		}
		
		OnLongClickListener MenuLongClicker = v -> {
			switch (v.getId()) {
				case R.id.toolbar_action0:
					evaluateJavascript("if(window.app)app.setTTS()",null);
					break;
				case R.id.toolbar_action1:
					//evaluateJavascript(getUnderlineIncantation().toString(),null);
					break;
				case R.id.toolbar_action3:
					break;
			}
			return true;
		};
		
		Runnable explodeMenuRunnable = () -> {
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
					View vIStamp=vI;
					int cc=0;
					for (int i = 0; i < vg.getChildCount(); i++) {
						vI = vg.getChildAt(i);
						TextView tv = (TextView) LookForANobleSteedCorrespondingWithDrawnClasses(vI, 1, LinearLayout.class, TextView.class);
						if(tv!=null) {
							CharSequence text = tv.getText();
							CMN.Log(text.getClass());
							int len=text.length();
							for (int j = 0; j < 3; j++) {
								String t=SangeHuoQiangShou[j];
								if(len==t.length()&&Utils.strEquals(text, t)) {
									CMN.Log("yes "+text+"!!!");
									vI.setId(DuiYinChengSanRen[j]);
									vI.setOnLongClickListener(MenuLongClicker);
									cc++;
									break;
								}
							}
						}
					}
					if(cc>0) {
						vIStamp.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
							@Override
							public void onViewAttachedToWindow(View v) {
								CMN.Log("onViewAttachedToWindow");
							}
							
							@Override
							public void onViewDetachedFromWindow(View v) {
								CMN.Log("onViewDetachedFromWindow");
							}
						});
					}
					break;
				}
			}
			//CMN.Log("阿西吧", popupDecorVies);
		};
		
		public String getSelectText() {
			getSharedIds();
			return getResources().getString(SelectString_Id!=0?SelectString_Id:android.R.string.selectAll);
		}
		
		public String getShareText() {
			getSharedIds();
			return getResources().getString(ShareString_Id!=0?ShareString_Id:R.string.share);
		}
		
		public String getCopyText() {
			getSharedIds();
			return getResources().getString(CopyString_Id!=0?CopyString_Id:android.R.string.copy);
		}
		
		public void TweakWebviewContextMenu(Menu menu) {
			int gid=0;
			if(menu.size()>0) {
				/* remove artificial anti-intelligence */
				MenuItem item0 = menu.getItem(0);
				if(item0.getTitle().toString().startsWith("地") || item0.getTitle().toString().startsWith("Map"))
					menu.removeItem(item0.getItemId());
				if(menu.size()>0) gid=menu.getItem(0).getGroupId();
			}
			SangeHuoQiangShou = getResources().getStringArray(R.array.DingGeZhuGeLiang);
			CMN.Log(SangeHuoQiangShou, System.identityHashCode(SangeHuoQiangShou));
			int highlightColor=Color.YELLOW;
			String ColorCurse = String.format("%06X", highlightColor&0xFFFFFF);
			Spanned text = Html.fromHtml("<span style='background:#"+ColorCurse+"; color:#"+ColorCurse+";'>"+SangeHuoQiangShou[0]+"</span>");
			MenuItem MyMenu = menu.add(0, DuiYinChengSanRen[0], 0, text);
			
			String[] MenuItemsToReMove = new String[]{getShareText(), getSelectText(), getContext().getString(R.string.plain_dict)};
			int removeLen = 3;
			
			//CMN.Log("SelectAllText", MenuItemsToReMove[0], CMN.id(MenuItemsToReMove[0]));
			int findCount=3;
			int ToolsOrder=0;
			MenuItem PlainDictItem=null;
			//if(false)
			for(int i=0;i<menu.size();i++) {
				CharSequence title = menu.getItem(i).getTitle();
				CMN.Log(i, title);
				int tLen=title.length();
				for (int j = 0; j < removeLen; j++) {
					if(tLen==MenuItemsToReMove[j].length()&&Utils.strEquals(title, MenuItemsToReMove[j])) {
						if(j==1) ToolsOrder=menu.getItem(i).getOrder();
						if(j==2) {
							PlainDictItem = menu.getItem(i);
						}
						menu.removeItem(menu.getItem(i).getItemId());//移除分享、全选
						i--;
						findCount--;
						break;
					}
				}
				if(findCount==0) break;
			}
			
			menu.add(0,DuiYinChengSanRen[1],++ToolsOrder, SangeHuoQiangShou[1]);
			
			if(PlainDictItem!=null) {
				//CMN.Log("找到了找到了");
				menu.add(0,DuiYinChengSanRen[3],++ToolsOrder,PlainDictItem.getTitle());
				menu.add(PlainDictItem.getGroupId(),DuiYinChengSanRen[2],PlainDictItem.getOrder(),SangeHuoQiangShou[2]);
			} else {
				menu.add(0,DuiYinChengSanRen[2],++ToolsOrder,SangeHuoQiangShou[2]);
			}
			
		}
	}
	
	private boolean onMenuItemClick(ActionMode mode, MenuItem item) {
		//CMN.Log("onMenuItemClick", item.getClass(), item.getTitle(), item.getItemId(), android.R.id.copy);
		//CMN.Log("onActionItemClicked");
		int id = item.getItemId();
		switch(id) {
			case R.id.plaindict:{
				context.handleVersatileShare(21);
			} return true;
			case R.id.toolbar_action0:{
				HLED=true;
				evaluateJavascript(getHighLightIncantation(),new ValueCallback<String>() {
					@Override
					public void onReceiveValue(String value) {
						CMN.Log(value);
						
						invalidate();
					}});
			} return true;
			case R.id.toolbar_action1:{//工具复用，我真厉害啊啊啊啊！
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
				context.getUCC().show();
			} return false;
			case R.id.toolbar_action3:{//TTS
				evaluateJavascript("if(window.app)app.ReadText(''+window.getSelection())",null);
			} return false;
		}
		if (mode!=null && bAdvancedMenu) {
			boolean ret = webviewcallback.callback.onActionItemClicked(mode, item);
			if(id == 50856071 || id == android.R.id.copy || webviewcallback.getCopyText().equals(item.getTitle())){
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
	
	//Viva Marshmallow!
	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		CMN.Log("startActionMode…");
		isIMScrollSupressed = isWebHold;
		if(bAdvancedMenu) {
			if (webviewcallback == null) webviewcallback = new AdvancedWebViewCallback();
			
			ActionMode mode = super.startActionMode(webviewcallback.wrap(callback), type);
			
			//if(true) return mode;
			
			webviewcallback.TweakWebviewContextMenu(mode.getMenu());
			
			postDelayed(webviewcallback.explodeMenuRunnable, 350);
			
			return mode;
		}
		return super.startActionMode(callback, type);
	}
	
	private static void getSharedIds() {
		if(ShareString_Id==null) {
			Resources res = Resources.getSystem();
			CopyString_Id=res.getIdentifier("copy","string", "android");
			ShareString_Id=res.getIdentifier("share","string", "android");
			SelectString_Id=res.getIdentifier("selectAll","string", "android");
		}
	}
	
	/**
	 var w = window;
	 var script=undefined;
	 if(!w._PPMInst) {
	 	w._docAnnots = '';
		script = document.createElement('script');
		script.type = 'text/javascript';
		script.src = 'https://mark.js';
	 }
	 */
   @Multiline(trim=true)
	private final static String commonIcan = StringUtils.EMPTY;
 
	private static StringBuilder HighlightBuilder = new StringBuilder();

	/**
	 if(script) {
		 script.onload=function(){
	 		w._PPMInst.HighlightSelection();
			delete script.onload;
	 	};
	 	document.head.appendChild(script);
	 	'delay'
	 } else {
	 	w._PPMInst.HighlightSelection();
	 }
	 */
	@Multiline(trim=true)
	private final static String HighLightIncantation="ASDASDASD";
	
	 /**
	 window._docAnnots = '
	 */
	@Multiline(trim=true)
	private final static String RestoreHighLightIncantation1=StringUtils.EMPTY;
	
	/**
	 ';
	 var script = document.createElement('script');
	 script.type = 'text/javascript';
	 script.src = 'https://mark.js';
	 script.onload=function(){
		window._PPMInst.RestoreAnnots();
		delete script.onload;
	 };
	 document.head.appendChild(script);
	 */
	@Multiline(trim=false)
	private final static String RestoreHighLightIncantation2=StringUtils.EMPTY;
	
	/**
	 if(script) {
		 script.onload=function(){
	 		DeHighlightSelection();
			//delete script.onload;
	 	};
	 	document.head.appendChild(script);
	 } else {
	 	DeHighlightSelection();
	 }
	 */
	@Multiline(trim=true)
	private final static  String DeHighLightIncantation="DEHI";
	
	public static String getResoreHighLightIncantation(String jsonData) {
		HighlightBuilder.setLength(0);
		return HighlightBuilder.append(RestoreHighLightIncantation1)
				.append(jsonData)
				.append(RestoreHighLightIncantation2).toString();
	}
	
	public float webScale=0;
	public String getHighLightIncantation() {
		ensureMarkJS(context);
		HighlightBuilder.setLength(0);
		HighlightBuilder.append(commonIcan);
		return HighlightBuilder.append(HighLightIncantation).toString();
	}
	public String getDeHighLightIncantation() {
		ensureMarkJS(context);
		HighlightBuilder.setLength(0);
		HighlightBuilder.append(commonIcan);
		return HighlightBuilder.append(DeHighLightIncantation).toString();
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
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
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean postDelayed(Runnable action, long delayMillis) {
		CMN.Log("postDelayed", action, delayMillis);
		if(!isWebHold&&action.getClass().getName().contains("FloatingActionMode")) {
			CMN.Log("contextMenu_boost");
//			action.run();
//			return true;
		}
		return super.postDelayed(action, delayMillis);
	}
}