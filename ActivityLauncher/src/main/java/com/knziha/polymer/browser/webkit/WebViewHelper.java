package com.knziha.polymer.browser.webkit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.IU;
import com.knziha.polymer.toolkits.Utils.ReusableByteOutputStream;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;

import org.adrianwalker.multilinestring.Multiline;
import org.apache.commons.lang3.StringUtils;

public class WebViewHelper {
	private static WebViewHelper instance;
	
	public static WebViewHelper getInstance() {
		if(instance==null) {
			instance = new WebViewHelper();
		}
		return instance;
	}
	
	public static float minW;
	
	public final static ReusableByteOutputStream bos1 = new ReusableByteOutputStream();
	
	public int ShareString_Id;
	public int SelectString_Id;
	public int CopyString_Id;
	
	private String[] SangeHuoQiangShou;
	private final int[] DuiYinChengSanRen = new int[]{R.id.web_highlight, R.id.web_tools, R.id.web_tts, R.id.plaindict};
	public static boolean bAdvancedMenu = Build.VERSION.SDK_INT>=Build.VERSION_CODES.M;
	
	private void getSharedIds() {
		if(ShareString_Id==0) {
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
	private final String commonIcan = StringUtils.EMPTY;
 
	public StringBuilder HighlightBuilder = new StringBuilder();

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
	
	/**';
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
	
	/**if(script) {
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
	
	/**var w=window;function selectTouchtarget(e){
		var ret = selectTouchtarget_internal(e);
		if(ret<=0||e==1) {
			w._ttlck=!1;
			w._ttarget=null;
		}
		return ret;
	}
	function NoneSelectable(e){
		return getComputedStyle(e).userSelect=='none'
	}
	function selectTouchtarget_internal(e){
		w._ttlck=!0;
		var tt = w._ttarget;
		var t0 = tt;
		if(tt){
			var fc = 0;
			tt.userSelect='text';
			while(tt.tagName!="A"&&(w==0||tt.tagName!="IMG")) {
				tt = tt.parentElement;
				if(tt==null||++fc>=9)return -3;
				tt.userSelect='text';
			}
			if(NoneSelectable(tt)) {
				var sty = document.createElement("style");
				sty.innerHTML = "*{user-select:text !important}";
				document.head.appendChild(sty);
				if(NoneSelectable(tt)) {
					return -1;
				}
			}
			if(fc>0) {
				w._ttarget=tt;
			}
			if(e==0)tt._thref = tt.getAttribute("href");
			var sel = w.getSelection();
			var range = document.createRange();
			range.selectNodeContents(t0);
			sel.removeAllRanges();
			sel.addRange(range);
			var ret = sel.toString().length;
			if(e==0&&ret>0)tt.removeAttribute("href");
			return ret;
		}
		return -2;
	}
	 */
	@Multiline(trim=true)
	public final static String TouchTargetIncantation=StringUtils.EMPTY;
	
	public String getResoreHighLightIncantation(String jsonData) {
		HighlightBuilder.setLength(0);
		return HighlightBuilder.append(RestoreHighLightIncantation1)
				.append(jsonData)
				.append(RestoreHighLightIncantation2).toString();
	}
	
	public String getHighLightIncantation() {
		HighlightBuilder.setLength(0);
		HighlightBuilder.append(commonIcan);
		return HighlightBuilder.append(HighLightIncantation).toString();
	}
	public String getDeHighLightIncantation() {
		HighlightBuilder.setLength(0);
		HighlightBuilder.append(commonIcan);
		return HighlightBuilder.append(DeHighLightIncantation).toString();
	}
	
	
	public static Object getTypedTagInAncestors(View v, int depth, Class type) {
		int cc=0;
		if(type.isInstance(v.getTag())) {
			return v.getTag();
		}
		while(v.getParent() instanceof View&&cc<depth) {
			v = (View) v.getParent();
			if(type.isInstance(v.getTag())) {
				return v.getTag();
			}
			cc++;
		}
		return null;
	}
	
	/* 📕📕📕 微空间内爆术 📕📕📕 */
	public static View LookForANobleSteedCorrespondingWithDrawnClasses(View donkeySteed, int dynamicFrom, Class<?>...classes) {
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
	
	public void threeKingdomText(ViewGroup vg, View vI, View.OnLongClickListener onlongclick) {
		View vIStamp = vI;
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
					if(len==t.length()&& Utils.strEquals(text, t)) {
						CMN.Log("yes "+text+"!!!");
						vI.setId(DuiYinChengSanRen[j]);
						vI.setOnLongClickListener(onlongclick);
						cc++;
						break;
					}
				}
			}
		}
		if(cc>0) {
			vIStamp.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
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
	}
	
	public String extractTextResrouce(Context context, int id) {
		return context.getResources().getString(id);
	}
	
	public String getSelectText(Context context) {
		getSharedIds();
		return extractTextResrouce(context, SelectString_Id == 0 ? android.R.string.selectAll : SelectString_Id);
	}
	
	public String getShareText(Context context) {
		getSharedIds();
		return extractTextResrouce(context, ShareString_Id == 0 ? R.string.share : ShareString_Id);
	}
	
	public String getCopyText(Context context) {
		getSharedIds();
		return extractTextResrouce(context, CopyString_Id == 0 ? android.R.string.copy : CopyString_Id);
	}
	
	public void TweakWebviewContextMenu(Context context, Menu menu) {
		int gid=0;
		if(menu.size()>0) {
			/* remove artificial anti-intelligence */
			MenuItem item0 = menu.getItem(0);
			if(item0.getTitle().toString().startsWith("地") || item0.getTitle().toString().startsWith("Map"))
				menu.removeItem(item0.getItemId());
			if(menu.size()>0) gid=menu.getItem(0).getGroupId();
		}
		SangeHuoQiangShou = context.getResources().getStringArray(R.array.DingGeZhuGeLiang);
		CMN.Log(SangeHuoQiangShou, System.identityHashCode(SangeHuoQiangShou));
		int highlightColor= Color.YELLOW;
		String ColorCurse = String.format("%06X", highlightColor&0xFFFFFF);
		Spanned text = Html.fromHtml("<span style='background:#"+ColorCurse+"; color:#"+ColorCurse+";'>"+SangeHuoQiangShou[0]+"</span>");
		MenuItem MyMenu = menu.add(0, DuiYinChengSanRen[0], 0, text);
		
		String[] MenuItemsToReMove = new String[]{getShareText(context), getSelectText(context), context.getString(R.string.plain_dict)};
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
	
	
	
	public void SelectHtmlObject(Toastable_Activity a, WebFrameLayout layout, int source) {
		HighlightBuilder.setLength(0);
		HighlightBuilder.append(TouchTargetIncantation);
		/* Some sdk version need to manually bing up the selection handle. */
		final boolean fakePopHandles = Utils.version > 21 && Utils.version!=23;
		if(source==0&&!fakePopHandles) {
			source=1;
		}
		HighlightBuilder.append("selectTouchtarget(").append(source).append(")");
		layout.mWebView.evaluateJavascript(HighlightBuilder.toString(), new ValueCallback<String>() {
			@Override
			public void onReceiveValue(String value) {
				CMN.Log("selectTouchtarget", value);
				int len = IU.parsint(value, 0);
				if(len>0) {
					/* bring in action mode by a fake click on the programmatically  selected text. */
					if(fakePopHandles) {
						layout.forbidLoading=true;
						layout.mWebView.getSettings().setJavaScriptEnabled(false);
						layout.mWebView.getSettings().setJavaScriptEnabled(false);
						long time = CMN.now();
						MotionEvent evt = MotionEvent.obtain(time, time,MotionEvent.ACTION_DOWN, layout.lastX, layout.lastY, 0);
						layout.implView.dispatchTouchEvent(evt);
						evt.setAction(MotionEvent.ACTION_UP);
						layout.implView.dispatchTouchEvent(evt);
						evt.recycle();
						/* restore href attribute */
					}
				} else {
					a.showT("选择失败");
				}
				if(fakePopHandles) {
					layout.implView.postDelayed(() -> {
						layout.forbidLoading=false;
						layout.mWebView.getSettings().setJavaScriptEnabled(true);
						layout.mWebView.evaluateJavascript("window._ttlck=!1;var t=w._ttarget;if(t._thref)t.href=t._thref", null);
					}, 300);
				} else {
					layout.mWebView.evaluateJavascript("window._ttlck=!1;var t=w._ttarget;if(t._thref)t.href=t._thref", null);
				}
			}
		});
	}
}
