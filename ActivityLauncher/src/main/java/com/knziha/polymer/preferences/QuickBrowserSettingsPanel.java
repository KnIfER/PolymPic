package com.knziha.polymer.preferences;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.widget.HorizontalNumberPicker;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.databinding.QuickSettingsPanelBinding;
import com.knziha.polymer.equalizer.EqualizerGroup;
import com.knziha.polymer.equalizer.VerticalSeekBar;
import com.knziha.polymer.webstorage.DomainInfo;
import com.knziha.polymer.webstorage.WebOptions;
import com.knziha.polymer.widgets.PopupMenuHelper;
import com.knziha.polymer.widgets.SwitchCompatBeautiful;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.knziha.polymer.widgets.XYLinearLayout;

import org.adrianwalker.multilinestring.Multiline;

import java.util.Locale;

import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_DOMAIN;
import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_TAB;
import static com.knziha.polymer.webstorage.WebOptions.BackendSettings;
import static com.knziha.polymer.webstorage.WebOptions.ImmersiveSettings;
import static com.knziha.polymer.webstorage.WebOptions.LockSettings;
import static com.knziha.polymer.webstorage.WebOptions.StorageSettings;
import static com.knziha.polymer.webstorage.WebOptions.TextSettings;
import static com.knziha.polymer.widgets.Utils.bandLevels;
import static com.knziha.polymer.widgets.Utils.mEqualizer;
import static com.knziha.polymer.widgets.Utils.mGlobalEqShift;

public class QuickBrowserSettingsPanel extends SettingsPanel implements SettingsPanel.ActionListener {
	protected BrowserActivity a;
	protected QuickSettingsPanelBinding UIData;
	protected View sysVolEq;
	protected View webSiteInfo;
	protected View.OnClickListener webSiteInfoListener;
	protected LayoutTransition transition;
	protected ViewGroup root;
	protected Drawable[] drawables;
	protected SettingsPanel PickingDelegateContext_settingsPanel;
	protected int PickingDelegateContext_flagIdxSection;
	protected int PickingDelegateContext_flagPos;
	protected int mSettingsChanged;
	protected HorizontalNumberPicker mTextZoomNumberPicker;
	protected static int mScrollY;
	
	public QuickBrowserSettingsPanel(Context context, ViewGroup root, int bottomPaddding, Options opt) {
		super(context, root, bottomPaddding, opt, (BrowserActivity) context);
		mActionListener = this;
	}
	
	@Override
	protected void init(Context context, ViewGroup root) {
		a=(BrowserActivity) context;
		
		showInPopWindow = true;
		
		Resources mResource = a.mResource;
		drawables = new Drawable[]{
			mResource.getDrawable(R.drawable.ic_viewpager_carousel_1)
			, mResource.getDrawable(R.drawable.ic_domain_bk)
			, mResource.getDrawable(R.drawable.ic_polymer1)
		};
		for (Drawable drawable:drawables) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		}
		
		inflateUIData(context, root);
		LinearLayout lv = linearLayout = UIData.root;
		
		transition = lv.getLayoutTransition();
		lv.setLayoutTransition(null);
		
		ScrollView sv = new ScrollView(context);
		
		root.postDelayed(() -> lv.setLayoutTransition(transition), 450);
		
		sv.setLayoutParams(lv.getLayoutParams());
		sv.addView(lv, new ViewGroup.LayoutParams(-1, -2));
		sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		settingsLayout = sv;
		
		if (!showInPopWindow) {
			Utils.embedViewInCoordinatorLayout(settingsLayout);
		}
		
		UIData.sysVolSwitch.setChecked(opt.getAdjustSystemVolume());
		
		ReadDelegatableSettings(0);
		
		this.root = UIData.root;
		Utils.setOnClickListenersOneDepth(UIData.root, this, 999, null);
		
		if (opt.getAdjustIMShown()) {
			UIData.immArrow.setRotation(90);
			initImmersiveSettingsPanel();
		}
		
		
		if (opt.getAdjustFrequentSettingsShown()) {
			UIData.fsArrow.setRotation(90);
			initFrequentSettingsPanel();
		}
		
		if (opt.getAdjustTextShown()) {
			UIData.textArrow.setRotation(90);
			initTextSettingsPanel();
		}
		
		if (opt.getAdjustLockShown()) {
			UIData.lockArrow.setRotation(90);
			initLockPanel();
		}
		
		if (opt.getAdjustScnShown()) {
			UIData.scnArrow.setRotation(90);
			initScreenPanel();
		}
		
		if (opt.getAdjustStanzaShown()) {
			UIData.stanzaArrow.setRotation(90);
			initStanzaPanel();
		}
		
		if (opt.getAdjustPdfShown()) {
			UIData.pdfArrow.setRotation(90);
			initPdfPanel();
		}
		
		Utils.setSystemEqualizer(opt, true);
		if (opt.getAdjustSystemVolumeShown()) {
			UIData.sysVolArrow.setRotation(90);
			initSysVolEqualizerPanel();
		}
		
		if (opt.getAdjustWebsiteInfoShown()) {
			UIData.infoArrow.setRotation(90);
			initInfoPanel();
		}
		if(mScrollY>0) {
			root.post(() -> sv.scrollTo(0, mScrollY));
			//CMN.Log("重新滚动唷！", mScrollY);
		}
	}
	
	protected void inflateUIData(Context context, ViewGroup root) {
		if (UIData==null) {
			UIData = QuickSettingsPanelBinding.inflate(LayoutInflater.from(context), root, false);
		}
	}
	
	public void refresh() {
		CMN.Log("刷新全部数据...");
		ReadDelegatableSettings(0);
		if (immersiveSettings!=null) immersiveSettings.refresh();
		if (frequentSettings!=null) frequentSettings.refresh();
		if (lockSettings!=null) lockSettings.refresh();
		if (textSettings!=null) {
			textSettings.refresh();
			setTextZoomNumber();
		}
		if (screenSettings!=null) screenSettings.refresh();
		if (webSiteInfoListener!=null) webSiteInfoListener.onClick(null);
	}
	
	private void setTextZoomNumber() {
		mTextZoomNumberPicker.SafeSetValue(WebOptions.getTextZoom(getDynamicFlag(TextSettings)));
	}
	
	private void ReadDelegatableSettings(int section) {
		switch (section) {
			default:
				if(section!=0) break;
			case ImmersiveSettings:
				UIData.immSwitch.setChecked(WebOptions.getImmersiveScrollEnabled(getDynamicFlag(ImmersiveSettings)));
				UIData.immRlm.setImageResource(getIconResForDynamicFlagBySection(ImmersiveSettings));
			if(section!=0) break;
			case TextSettings:
				UIData.textSwitch.setChecked(WebOptions.getTextTurboEnabled(getDynamicFlag(TextSettings)));
				UIData.textRlm.setImageResource(getIconResForDynamicFlagBySection(TextSettings));
			if(section!=0) break;
			case LockSettings:
				UIData.lockSwitch.setChecked(WebOptions.getLockEnabled(getDynamicFlag(LockSettings)));
				UIData.lockRlm.setImageResource(getIconResForDynamicFlagBySection(LockSettings));
			if(section!=0) break;
		}
	}
	
	@Override
	public void onClick(View v) {
		boolean checked = v instanceof SwitchCompatBeautiful && ((SwitchCompatBeautiful) v).isChecked();
		switch (v.getId()) {
			case R.id.imm_switch:
				WebOptions.tmpFlag = getDynamicFlag(ImmersiveSettings);
				WebOptions.setImmersiveScrollEnabled(checked);
				putDynamicFlag(ImmersiveSettings, WebOptions.tmpFlag);
				a.ResetIMSettings();
				break;
			case R.id.imm: {
				boolean show = opt.toggleAdjustIMShown();
				UIData.immArrow.animate().rotation(show?90:0);
				setPanelVis(initImmersiveSettingsPanel().settingsLayout, show);
			}  break;
			case R.id.imm_rlm: {
				onPickingDelegate(immersiveSettings, ImmersiveSettings, 0, 0, 0);
			}  break;
			case R.id.fs: {
				boolean show = opt.toggleAdjustFrequentSettingsShown();
				UIData.fsArrow.animate().rotation(show?90:0);
				setPanelVis(initFrequentSettingsPanel().settingsLayout, show);
			}  break;
			case R.id.text_switch:
				WebOptions.tmpFlag = getDynamicFlag(TextSettings);
				WebOptions.setTextTurboEnabled(checked);
				putDynamicFlag(TextSettings, WebOptions.tmpFlag);
				mSettingsChanged|=TextSettings;
				break;
			case R.id.text: {
				boolean show = opt.toggleAdjustTextShown();
				UIData.textArrow.animate().rotation(show?90:0);
				setPanelVis(initTextSettingsPanel().settingsLayout, show);
			}  break;
			case R.id.text_rlm: {
				onPickingDelegate(textSettings, TextSettings, 0, 0, 0);
			}  break;
			case R.id.lock_switch:
				WebOptions.tmpFlag = getDynamicFlag(TextSettings);
				WebOptions.setLockEnabled(checked);
				putDynamicFlag(LockSettings, WebOptions.tmpFlag);
				mSettingsChanged|=LockSettings;
				break;
			case R.id.lock: {
				boolean show = opt.toggleAdjustLockShown();
				UIData.lockArrow.animate().rotation(show?90:0);
				setPanelVis(initLockPanel().settingsLayout, show);
			}  break;
			case R.id.lock_rlm: {
				onPickingDelegate(lockSettings, LockSettings, 0, 0, 0);
			}  break;
			case R.id.scn: {
				boolean show = opt.toggleAdjustScnShown();
				UIData.scnArrow.animate().rotation(show?90:0);
				setPanelVis(initScreenPanel().settingsLayout, show);
			}  break;
			case R.id.stanza: {
				boolean show = opt.toggleAdjustStanzaShown();
				UIData.stanzaArrow.animate().rotation(show?90:0);
				setPanelVis(initStanzaPanel().settingsLayout, show);
			}  break;
			case R.id.pdf: {
				boolean show = opt.toggleAdjustPdfShown();
				UIData.pdfArrow.animate().rotation(show?90:0);
				setPanelVis(initPdfPanel().settingsLayout, show);
			}  break;
			case R.id.info:{
				boolean show = opt.toggleAdjustWebsiteInfoShown();
				UIData.infoArrow.animate().rotation(show?90:0);
				setPanelVis(initInfoPanel(), show);
			} break;
			case R.id.sys_vol:{
				boolean show = opt.toggleAdjustSystemVolumeShown();
				UIData.sysVolArrow.animate().rotation(show?90:0);
				setPanelVis(initSysVolEqualizerPanel(), show);
			} break;
			case R.id.sys_vol_switch:
				opt.setAdjustSystemVolume(checked);
				Utils.setSystemEqualizer(opt, false);
			break;
			default:
				super.onClick(v);
		}
	}
	
	protected void setPanelVis(View settingsLayout, boolean show) {
		settingsLayout.setVisibility(show?View.VISIBLE:View.GONE);
	}
	
	/**
	var sel = getSelection();
	var p = sel.getRangeAt(0).commonAncestorContainer;
	sel.empty();
	var range = new Range();
	range.selectNode(p);
	sel.addRange(range);
	// hate these naughty APIs.
	sel.modify('extend', 'forward', 'paragraphboundary');
	 */
	@Multiline(trim=true, compile=true)
	public final static String ExpandSelection = "";
	
	/**
	var sel = getSelection();
	 sel.modify('move', 'backward', 'paragraphboundary');
	 sel.modify('extend', 'forward', 'paragraphboundary');
	 */
	@Multiline(trim=true, compile=true)
	public final static String SelectParagraph = "";
	
	/**
	 getSelection()
	 */
	@Multiline(trim=true, compile=true)
	public final static String GetText = "";
	
	@Override
	public void onAction(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, boolean dynamic, boolean val) {
		CMN.Log("onAction", flagIdxSection, flagPos, dynamic, val);
		if (flagIdxSection!=0) {
			if (dynamic) {
				if (mFlagAdapter.getDynamicFlagIndex(flagIdxSection)<6) {
					WebFrameLayout.GlobalSettingsVersion ++;
				}
				if (flagIdxSection==LockSettings) {
					ResetLockSettings();
				} else if (flagIdxSection==TextSettings) {
					ResetTextSettings(null, 0);
				} else if (flagIdxSection==ImmersiveSettings) {
					a.ResetIMSettings();
				} else {
					mSettingsChanged|=flagIdxSection;
				}
			} else if(flagIdxSection==1) {
				if (flagPos==8) {
					if (val) {
						a.acquireWakeLock();
					} else {
						a.releaseWakeLock();
					}
				}
			}
		}
		if (flagIdxSection==NONE_SETTINGS_GROUP1) {
			ActionGp_1 var = ActionGp_1.values()[flagPos];
			//CMN.Log("NONE_SETTINGS_GROUP1::", var.name());
			switch (var) {
				// 缩放值预设
				case zoom: {
					PopupMenuHelper popupMenu = a.getPopupMenu();
					if (popupMenu.tag!=R.string.ts_100) {
						int[] texts = new int[] {
							R.string.ts_75
							,R.string.ts_90
							,R.string.ts_100
							,R.string.ts_110
							,R.string.ts_125
							,R.string.ts_150
						};
						popupMenu.leftDrawable = a.mResource.getDrawable(R.drawable.ic_yes);
						popupMenu.initLayout(texts, (popupMenuHelper, v, isLongClick) -> {
							int value = 110;
							switch (v.getId()) {
								case R.string.ts_75: {
									value = 75;
								} break;
								case R.string.ts_90: {
									value = 90;
								} break;
								case R.string.ts_100: {
									value = 100;
								} break;
								case R.string.ts_125: {
									value = 125;
								} break;
								case R.string.ts_150: {
									value = 150;
								} break;
							}
							ResetTextSettings(null, value);
							setTextZoomNumber();
							popupMenuHelper.postDismiss(80);
							return true;
						});
						popupMenu.tag=R.string.ts_100;
					}
					int[] vLocationOnScreen = new int[2];
					settingsLayout.getLocationOnScreen(vLocationOnScreen);
					XYLinearLayout xy = UIData.root;
					popupMenu.show(settingsLayout, vLocationOnScreen[0]+(int) (60*GlobalOptions.density), vLocationOnScreen[1]+(int) (xy.lastY-settingsLayout.getScrollY()));
				} break;
				// 切换屏幕方向
				case hengping1:
				case hengping2:
				case hengping3:
				case shuping1:
				case shuping2:
				case shuping3:
				case zhongli:
				case xitong:
				case lock: {
					a.setScreenOrientation(var.ordinal()-1);
				} break;
				// 段落工具
				case kuodaxuanze: {
					a.currentWebView.evaluateJavascript(ExpandSelection, null);
				} break;
				case xuanduanfanyi: {
					a.currentWebView.evaluateJavascript(SelectParagraph, value -> a.handleVersatileShare(19));
				} break;
				case fanyixuanze: {
					a.handleVersatileShare(19);
				} break;
				case quanxuan: {
					a.currentWebView.evaluateJavascript("document.execCommand('selectAll', false)", null);
				} break;
				// PDF 阅读
				case quanwenfanyi: {
					Utils.blinkView(settingsPanel.settingsLayout, false);
					a.showT("灵气不足，运转失败…");
				} break;
				case tianjiapdflnk: {
					a.AddPDFViewerShortCut();
				} break;
				case dakaipdf: {
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.setClassName("com.knziha.polymer", "com.knziha.polymer.PDocShortCutActivity");
					intent.putExtra("main", true);
					a.startActivity(intent);
				} break;
				case dakaipdfwenjian: {
					a.startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT)
						.addCategory(Intent.CATEGORY_OPENABLE)
						.setType("application/pdf")
						, Utils.RequestPDFFile);
				} break;
			}
		}
	}
	
	@Override
	public void onPickingDelegate(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, int lastX, int lastY) {
		PickingDelegateContext_settingsPanel = settingsPanel;
		PickingDelegateContext_flagIdxSection = flagIdxSection;
		PickingDelegateContext_flagPos = flagPos;
		PopupMenuHelper popupMenu = a.getPopupMenu();
		if (popupMenu.tag!=R.string.quanju) {
			int[] texts = new int[] {
					R.layout.apply_from
					,R.string.quanju
					,R.string.biaoqianye
					,R.string.wangzhan
					,R.string.genduo
			};
			popupMenu.leftDrawable = a.mResource.getDrawable(R.drawable.ic_yes);
			popupMenu.initLayout(texts, (popupMenuHelper, v, isLongClick) -> {
				int pickDelegateIndex = -1;
				switch (v.getId()) {
					case R.string.quanju:
						pickDelegateIndex=0;
					break;
					case R.string.biaoqianye:
						pickDelegateIndex=1;
					break;
					case R.string.wangzhan:
						pickDelegateIndex=2;
					break;
					case R.string.genduo:
					break;
				}
				if (pickDelegateIndex>=0) {
					SettingsPanel settingsPane1 = PickingDelegateContext_settingsPanel;
					int section = PickingDelegateContext_flagIdxSection;
					mFlagAdapter.pickDelegateForSection(section, pickDelegateIndex);
					if (settingsPane1!=null) {
						settingsPane1.refresh();
						if (settingsPane1==textSettings) {
							setTextZoomNumber();
						}
					}
					ReadDelegatableSettings(section);
					if (flagIdxSection==TextSettings) {
						ResetTextSettings(null, 0);
					} else if (section==ImmersiveSettings) {
						a.ResetIMSettings();
					} else {
						mSettingsChanged|= section;
					}
				}
				popupMenuHelper.postDismiss(80);
				return true;
			});
			popupMenu.tag=R.string.quanju;
		}
		int nowIdx = mFlagAdapter.getDynamicFlagIndex(flagIdxSection);
		nowIdx = nowIdx==WebViewSettingsSource_DOMAIN?3:nowIdx==WebViewSettingsSource_TAB?2:1;
		for (int i = 1; i < 4; i++) {
			popupMenu.lv.getChildAt(i).setActivated(i==nowIdx);
		}
		int[] vLocationOnScreen = new int[2];
		settingsLayout.getLocationOnScreen(vLocationOnScreen);
		XYLinearLayout xy = UIData.root;
		popupMenu.show(a.root, vLocationOnScreen[0]+(int) (60*GlobalOptions.density), vLocationOnScreen[1]+(int) (xy.lastY-settingsLayout.getScrollY()));
		//popupMenu.show(settingsLayout, vLocationOnScreen[0]+(int) (xy.lastX-15*GlobalOptions.density), vLocationOnScreen[1]+(int) xy.lastY);
	}
	
	private int getIconResForDynamicFlagBySection(int section) {
		int index = mFlagAdapter.getDynamicFlagIndex(section);
		if (index==WebViewSettingsSource_TAB) {
			return R.drawable.ic_viewpager_carousel_1;
		} else if (index==WebViewSettingsSource_DOMAIN) {
			return R.drawable.ic_domain_bk;
		}
		return R.drawable.ic_polymer1;
	}
	
	@Override
	public Drawable getIconForDynamicFlagBySection(int section) {
		int index = mFlagAdapter.getDynamicFlagIndex(section);
		if (index==WebViewSettingsSource_TAB) {
			return drawables[0];
		} else if (index==WebViewSettingsSource_DOMAIN) {
			return drawables[1];
		}
		return drawables[2];
	}
	
	
	////////////////// Activity Functions //////////////////
	public void ResetLockSettings() {
		WebFrameLayout layout = a.currentViewImpl;
		if (layout!=null) {
			layout.getDelegateFlag(TextSettings, true);
			layout.setLockSettings();
		}
	}
	
	public void ResetTextSettings(View view, int ts) {
		WebFrameLayout layout = a.currentViewImpl;
		long flag = layout.getDelegateFlag(TextSettings, false);
		layout.frcWrp = WebOptions.getTextTurboEnabled(flag) && WebOptions.getForceTextWrap(flag);
		layout.setTextSettings(false, view==null && ts==0);
		if (ts!=0) {
			WebOptions.tmpFlag = layout.getDelegateFlag(TextSettings, true);
			WebOptions.setTextZoom(ts);
			layout.setDelegateFlag(TextSettings, WebOptions.tmpFlag);
			if (view!=null) {
				view.removeCallbacks(UpdateTextZoom_RN);
				view.postDelayed(UpdateTextZoom_RN, 300);
			} else {
				layout.setTextZoom();
			}
		} else if(layout.bNeedCheckTextZoom>0) {
			layout.setTextZoom();
		}
	}
	
	Runnable UpdateTextZoom_RN = new Runnable() {
		@Override
		public void run() {
			WebFrameLayout layout = a.currentViewImpl;
			if (layout!=null) {
				layout.setTextZoom();
			}
		}
	};
	////////////////// Activity Functions End //////////////////
	
	
	SettingsPanel immersiveSettings;
	SettingsPanel frequentSettings;
	SettingsPanel textSettings;
	SettingsPanel lockSettings;
	SettingsPanel screenSettings;
	SettingsPanel stanzaSettings;
	SettingsPanel pdfSettings;
	
	public final static int NONE_SETTINGS_GROUP1=0;
	
	enum ActionGp_1 {
		zoom
		,hengping1
		,hengping2
		,hengping3
		,shuping1
		,shuping2
		,shuping3
		,zhongli
		,xitong
		,lock
		,kuodaxuanze
		,fanyixuanze
		,xuanduanfanyi
		,quanwenfanyi
		,quanxuan
		,dakaipdf
		,tianjiapdflnk
		,dakaipdfwenjian
	}
	
	private SettingsPanel initPdfPanel() {
		if (pdfSettings==null) {
			pdfSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "添加桌面快捷方式", "打开阅读器", "打开文件..."}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.tianjiapdflnk.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.dakaipdf.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.dakaipdfwenjian.ordinal(), true)
			}});
			pdfSettings.setEmbedded(this);
			pdfSettings.init(a, root);
			addPanelViewBelow(pdfSettings.settingsLayout, UIData.pdfPanel);
		}
		return pdfSettings;
	}
	
	private SettingsPanel initStanzaPanel() {
		if (stanzaSettings==null) {
			stanzaSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "扩大选择", "翻译选择", "选段翻译", "全文翻译", "全选"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.kuodaxuanze.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.fanyixuanze.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.xuanduanfanyi.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.quanwenfanyi.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.quanxuan.ordinal(), true)
			}});
			stanzaSettings.setEmbedded(this);
			stanzaSettings.init(a, root);
			addPanelViewBelow(stanzaSettings.settingsLayout, UIData.stanzaPanel);
		}
		return stanzaSettings;
	}
	
	/**
		// https://www.cnblogs.com/handsome-jm/p/11505191.html
		function clearAllCookie() {
			var keys = document.cookie.match(/[^ =;]+(?=\=)/g);
			if(keys) {
				for(var i = keys.length; i--;)
					document.cookie = keys[i] + '=0;domain='+document.domain+';expires=' + new Date(0).toUTCString()
			}
		}
		 function delCookie () {
		  var keys = document.cookie.match(/[^ =;]+(?==)/g);
		  if (keys) {
			for (var i = keys.length; i--;) {
			  document.cookie = keys[i] + '=0;path=/;expires=' + new Date(0).toUTCString(); // 清除当前域名下的,例如：m.ratingdog.cn
			  document.cookie = keys[i] + '=0;path=/;domain=' + document.domain + ';expires=' + new Date(0).toUTCString(); // 清除当前域名下的，例如 .m.ratingdog.cn
			  document.cookie = keys[i] + '=0;path=/;domain=bing.cn;expires=' + new Date(0).toUTCString(); // 清除一级域名下的或指定的，例如 .ratingdog.cn
			  document.cookie = keys[i] + '=;Max-Age=-1';
			  document.cookie = keys[i] + '=;domain=bing.cn;Max-Age=-1';
			}
		  }
		}
	 	clearAllCookie();
		delCookie();
	 	console.log('清除所有cookie函数'+document.cookie);
	 * */
	@Multiline
	final static String RemoveAllCookie = "";
	
	DomainInfo domainInfo;
	
	private View initInfoPanel() {
		if (webSiteInfo==null) {
			webSiteInfo = UIData.websiteInfo.getViewStub().inflate();
			SparseArray<View> viewMap = new SparseArray<>();
			View.OnClickListener listener = v -> {
				if(v==null||domainInfo==null) {
					domainInfo = a.currentViewImpl.domainInfo;
				}
				String domain = domainInfo.domainKey.toString();
				if(v==null) {
					Utils.setTextInView(viewMap.get(R.id.domainNameTv), domain);
					boolean hasConfig = domainInfo.domainID!=0;
					View domainOptTv = viewMap.get(R.id.domainOptTv);
					domainOptTv.setActivated(hasConfig);
					Utils.setTextInView(domainOptTv, hasConfig?"已保存网站设定":"未保存网站设定");
				}
				else switch (v.getId()) {
					case R.id.domainQuery:
						a.newTab("http://icp.chinaz.com/"+domain, false, true, -1);
					break;
					case R.id.domainRemove:
						domainInfo.remove();
						refresh();
						a.showT("deleted_refresh");
					break;
					case R.id.domainRemove1:
						a.currentWebView.evaluateJavascript(RemoveAllCookie, value ->
							a.currentWebView.evaluateJavascript("document.cookie" , value1 -> {
									deleteCookiesForMainDo(domain);
									//a.showT(value1);
									a.showT("deleted_refresh");
								}
							)
						);
					break;
					case R.id.domainCookieTv:
						a.showT("没啥管理");
					break;
					default:
						a.showT("别点我");
				}
			};
			Utils.setOnClickListenersOneDepth((ViewGroup) webSiteInfo, listener, viewMap, 999);
			addPanelViewBelow(webSiteInfo, UIData.infoPanel);
			listener.onClick(null);
			webSiteInfoListener = listener;
		}
		return webSiteInfo;
	}
	
	private static void deleteCookiesForMainDo(String domain) {
		try {
			CookieManager manager = CookieManager.getInstance();
			domain = domain.substring(domain.indexOf("."));
			String cookieGlob = manager.getCookie(domain);
			if (cookieGlob != null) {
				String[] cookies = cookieGlob.split(";");
				for (String cookieTuple : cookies) {
					String[] cookieParts = cookieTuple.split("=");
					manager.setCookie(domain, cookieParts[0] + "=; Max-Age=-1");
				}
			}
		} catch (Exception ignored) { }
	}
	
	
	private SettingsPanel initScreenPanel() {
		if (screenSettings==null) {
			final SettingsPanel screenSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "重力感应方向", "跟随系统方向", "锁定当前方向", "保持屏幕常亮"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.zhongli.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.xitong.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.lock.ordinal(), true)
					, makeInt(1, 8, false) // getLockScreenOn
			}});
			screenSettings.setEmbedded(this);
			screenSettings.init(a, root);
			
			final SettingsPanel hengping = new SettingsPanel(a, opt
					, new String[][]{new String[]{"切换横屏：", "重力感应", "正向", "反向"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping1.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping2.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.hengping3.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping1.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping2.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping3.ordinal(), true)
			}});
			final SettingsPanel shuping = new SettingsPanel(a, opt
					, new String[][]{new String[]{"切换竖屏：", "重力感应", "正向", "反向"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping1.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping2.ordinal(), true)
					, makeDynInt(NONE_SETTINGS_GROUP1, ActionGp_1.shuping3.ordinal(), true)
			}});
			hengping.setEmbedded(this);
			hengping.init(a, root);
			shuping.setEmbedded(this);
			shuping.init(a, root);
			LinearLayout ll = new LinearLayout(a);
			View sep = new View(a);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			Utils.addViewToParent(hengping.settingsLayout, ll);
			Utils.addViewToParent(sep, ll);
			Utils.addViewToParent(shuping.settingsLayout, ll);
			hengping.settingsLayout.setBackgroundResource(R.drawable.frame);
			shuping.settingsLayout.setBackgroundResource(R.drawable.frame);
			((LinearLayout.LayoutParams)hengping.settingsLayout.getLayoutParams()).weight = 1;
			((LinearLayout.LayoutParams)shuping.settingsLayout.getLayoutParams()).weight = 1;
			((LinearLayout.LayoutParams)sep.getLayoutParams()).width = (int) (GlobalOptions.density*15);
			int pad = (int) (GlobalOptions.density*5);
			ll.setPadding(pad, 0, pad, 0);
			Utils.addViewToParent(ll, screenSettings.settingsLayout, 0);
			
			addPanelViewBelow(screenSettings.settingsLayout, UIData.scnPanel);
			this.screenSettings = screenSettings;
		}
		return screenSettings;
	}
	
	private SettingsPanel initLockPanel() {
		if (lockSettings==null) {
			lockSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "锁定水平滚动", "锁定垂直滚动"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(LockSettings, 35, true) // getLockX
					, makeDynInt(LockSettings, 36, false) // getLockY
			}});
			lockSettings.setEmbedded(this);
			lockSettings.init(a, root);
			addPanelViewBelow(lockSettings.settingsLayout, UIData.lockPanel);
		}
		return lockSettings;
	}
	
	private SettingsPanel initTextSettingsPanel() {
		if (textSettings==null) {
			textSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "强制允许缩放", "强制文本换行", "启用概览模式"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(TextSettings, 19, true) // getForcePageZoomable
					, makeDynInt(TextSettings, 20, true) // getForceTextWrap
					, makeDynInt(TextSettings, 22, true) // getOverrideMode
			}});
			textSettings.setEmbedded(this);
			textSettings.init(a, root);
			View textscalePicker = UIData.textscalePicker.getViewStub().inflate();
			View ts_button = textscalePicker.findViewById(R.id.radio);
			ts_button.setOnClickListener(this);
			ts_button.setTag(makeInt(0, 0, true));
			HorizontalNumberPicker numberPicker = mTextZoomNumberPicker = textscalePicker.findViewById(R.id.numberpicker);
			//numberPicker.setMinValue(15);
			//numberPicker.setMaxValue(511);
			numberPicker.setStepMuliplier(5);
			numberPicker.setMinValue(15);
			numberPicker.setMaxValue(114);
			setTextZoomNumber();
			numberPicker.setTextColor(0xa82b43e1);
			numberPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
				//CMN.Log("SafeSetValue setOnValueChangedListener", numberPicker.getScrollState(), newVal);
				if (mFlagAdapter.getDynamicFlagIndex(TextSettings)<WebViewSettingsSource_DOMAIN) {
					WebFrameLayout.GlobalSettingsVersion++;
				}
				numberPicker.mSetValueStr = null;
				newVal = Math.max(15, Math.min(510, newVal));
				ResetTextSettings(picker, newVal);
			});
			Utils.addViewToParent(textscalePicker, textSettings.linearLayout);
			addPanelViewBelow(textSettings.settingsLayout, UIData.textPanel);
			Utils.addViewToParent(textscalePicker, textSettings.linearLayout);
		}
		return textSettings;
	}
	
	private SettingsPanel initImmersiveSettingsPanel() {
		if (immersiveSettings==null) {
			immersiveSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null, "滑动隐藏顶栏", "滑动隐藏底栏"}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynInt(ImmersiveSettings, 16, true)
					, makeDynInt(ImmersiveSettings, 17, false)}});
			immersiveSettings.setEmbedded(this);
			immersiveSettings.init(a, root);
			addPanelViewBelow(immersiveSettings.settingsLayout, UIData.immPanel);
		}
		return immersiveSettings;
	}
	
	protected void addPanelViewBelow(View settingsLayout, LinearLayout panelTitle) {
		Utils.addViewToParent(settingsLayout, root, panelTitle);
	}
	
	@Override
	protected void showPop() {
		if (pop==null) {
			pop = new PopupWindow(a);
			pop.setContentView(settingsLayout);
		}
		a.embedPopInCoordinatorLayout(pop);
	}
	
	private SettingsPanel initFrequentSettingsPanel() {
		if (frequentSettings==null) {
			frequentSettings = new SettingsPanel(a, opt
					, new String[][]{new String[]{null
					, "电脑模式"
					, "自定义标识符... "
					, "无图模式"
					, "启用JS"
					, "启用cookie"
					, "启用历史记录"
					}}
					, new int[][]{new int[]{Integer.MAX_VALUE
					, makeDynIcoInt(BackendSettings, 7, false) // getPCMode
					, makeInt(0, 0, false)
					, makeDynIcoInt(BackendSettings, 12, false) // getNoNetworkImage
					, makeDynIcoInt(BackendSettings, 8, true) // getEnableJavaScript
					, makeDynIcoInt(StorageSettings, 3, true) // getUseCookie
					, makeDynIcoInt(StorageSettings, 2, true) // getRecordHistory
			}});
			frequentSettings.setEmbedded(this);
			frequentSettings.init(a, root);
			addPanelViewBelow(frequentSettings.settingsLayout, UIData.fsPannel);
		}
		return frequentSettings;
	}
	
	boolean eq_panel_tracked;
	
	private View initSysVolEqualizerPanel() {
		if (sysVolEq==null) {
			sysVolEq = UIData.sysVolEq.getViewStub().inflate();
			
			SeekBar sysVolSeek = sysVolEq.findViewById(R.id.sys_vol_seek);
			sysVolSeek.setProgress(mGlobalEqShift);
			TextView sysVolPercent = sysVolEq.findViewById(R.id.sys_vol_percent);
			sysVolPercent.setText((int) (((mGlobalEqShift-5000)/10000.f)*100)+"%");
			
			int bandCnt = mEqualizer.getNumberOfBands();
			int min = mEqualizer.getBandLevelRange()[0];
			int max = mEqualizer.getBandLevelRange()[1];
			
			EqualizerGroup eqlv1 = sysVolEq.findViewById(R.id.equlizer_group);
			eqlv1.opt = opt;
			SeekBar.OnSeekBarChangeListener mSeekbarsListener = new SeekBar.OnSeekBarChangeListener() {
				@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					//CMN.Log("onProgressChanged", progress, fromUser);
					if (seekBar == sysVolSeek) {
						int percent = (int) (((progress - 5000) / 10000.f) * 100);
						sysVolPercent.setText(percent + "%");
						mGlobalEqShift = progress;
						Utils.setSystemEqualizer(opt, false);
					} else {
						int Amp = progress + eqlv1.baseLevel;
						int id = seekBar.getId();
						((VerticalSeekBar) eqlv1.getChildAt(id)).btmTags.set(0, String.format(Locale.UK, "%.1f", Amp * 1.f / 100) + "db");
						eqlv1.mEqualizerListener.setAmp(id, Amp);
					}
				}
				@Override public void onStartTrackingTouch(SeekBar seekBar) {
					eq_panel_tracked = true;
					if (seekBar == sysVolSeek) {
						if (!opt.getAdjustSystemVolume()) {
							UIData.sysVolSwitch.performClick();
						}
					}
				}
				@Override public void onStopTrackingTouch(SeekBar seekBar) { }
			};
			sysVolSeek.setOnSeekBarChangeListener(mSeekbarsListener);
			eqlv1.setEqualizerListeners(mSeekbarsListener, new EqualizerGroup.EqualizerListener() {
				@Override
				public float getBandFrequency(int Index) {
					return mEqualizer.getCenterFreq((short) Index)/1000;
				}
				@Override
				public int setAmp(int Index, int Amplitute) {
					bandLevels[Index] = Amplitute;
					float shiftGlobal = (mGlobalEqShift-5000)/10000.f*(max-min);
					int modified = (int) Math.max(min, Math.min(max, Amplitute + shiftGlobal));
					mEqualizer.setBandLevel((short)Index, (short)modified);
					return 0;
				}
				@Override
				public int[] getBandRange() {
					return new int[]{mEqualizer.getBandLevelRange()[0],mEqualizer.getBandLevelRange()[1]};
				}
				@Override
				public int getBandCount() {
					return bandCnt;
				}
				@Override
				public int getAmp(int i) {
					return bandLevels[i];
				}
			});
			sysVolSeek.setOnTouchListener((v, event) -> event.getActionMasked()==MotionEvent.ACTION_DOWN);
			eqlv1.inflate(LayoutInflater.from(UIData.root.getContext()), true, true);
			addPanelViewBelow(sysVolEq, UIData.sysVolPanel);
		}
		return sysVolEq;
	}
	
	@Override
	protected void onDismiss() {
		CMN.Log("onDismiss::", mSettingsChanged);
		if (eq_panel_tracked) {
			eq_panel_tracked = false;
			opt.putBandLevels(bandLevels, mGlobalEqShift);
		}
		if (mSettingsChanged!=0) {
			a.currentViewImpl.checkSettings(true, true);
			mSettingsChanged=0;
		}
		mScrollY = settingsLayout.getScrollY();
	}
}
