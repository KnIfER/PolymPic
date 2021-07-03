package com.knziha.polymer.preferences;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.knziha.polymer.webstorage.WebOptions;
import com.knziha.polymer.widgets.PopupMenuHelper;
import com.knziha.polymer.widgets.SwitchCompatBeautiful;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.knziha.polymer.widgets.XYLinearLayout;

import java.util.Locale;

import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_DOMAIN;
import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_TAB;
import static com.knziha.polymer.webstorage.WebOptions.BackendSettings;
import static com.knziha.polymer.webstorage.WebOptions.ImmersiveSettings;
import static com.knziha.polymer.webstorage.WebOptions.StorageSettings;
import static com.knziha.polymer.webstorage.WebOptions.TextSettings;
import static com.knziha.polymer.widgets.Utils.bandLevels;
import static com.knziha.polymer.widgets.Utils.mEqualizer;
import static com.knziha.polymer.widgets.Utils.mGlobalEqShift;

public class QuickBrowserSettingsPanel extends SettingsPanel implements SettingsPanel.ActionListener {
	private BrowserActivity a;
	private QuickSettingsPanelBinding UIData;
	private View sysVolEq;
	private LayoutTransition transition;
	private ViewGroup root;
	private Drawable[] drawables;
	private SettingsPanel PickingDelegateContext_settingsPanel;
	private int PickingDelegateContext_flagIdxSection;
	private int PickingDelegateContext_flagPos;
	private int mSettingsChanged;
	private HorizontalNumberPicker mTextZoomNumberPicker;
	private static int mScrollY;
	
	public QuickBrowserSettingsPanel(Context context, ViewGroup root, int bottomPaddding, Options opt) {
		super(context, root, bottomPaddding, opt, (BrowserActivity) context);
		mActionListener = this;
	}
	
	@Override
	protected void init(Context context, ViewGroup root) {
		a=(BrowserActivity) context;
		
		Resources mResource = a.mResource;
		drawables = new Drawable[]{
			mResource.getDrawable(R.drawable.ic_viewpager_carousel_1)
			, mResource.getDrawable(R.drawable.ic_domain_bk)
			, mResource.getDrawable(R.drawable.ic_polymer1)
		};
		for (Drawable drawable:drawables) {
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		}
		
		UIData = QuickSettingsPanelBinding.inflate(LayoutInflater.from(context), root, false);
		LinearLayout lv = linearLayout = UIData.root;
		
		transition = lv.getLayoutTransition();
		lv.setLayoutTransition(null);
		
		ScrollView sv = new ScrollView(context);
		
		root.postDelayed(() -> {
			lv.setLayoutTransition(transition);
		}, 450);
		
		sv.setLayoutParams(lv.getLayoutParams());
		sv.addView(lv, new ViewGroup.LayoutParams(-1, -2));
		sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		settingsLayout = sv;
		
		Utils.embedViewInCoordinatorLayout(settingsLayout);
		
		UIData.sysVolSwitch.setChecked(opt.getAdjustSystemVolume());
		
		ReadDelegatableSettings(0);
		
		this.root = UIData.root;
		Utils.setOnClickListenersOneDepth(UIData.root, this, 999, null);
		
		if (opt.getAdjustIMShown()) {
			UIData.immArrow.setRotation(90);
			initImmersiveSettingsPanel();
		}
		
		if (opt.getAdjustTextShown()) {
			UIData.textArrow.setRotation(90);
			initTextSettingsPanel();
		}
		
		if (opt.getAdjustFrequentSettingsShown()) {
			UIData.fsArrow.setRotation(90);
			initFrequentSettingsPanel();
		}
		
		Utils.setSystemEqualizer(opt, true);
		if (opt.getAdjustSystemVolumeShown()) {
			UIData.sysVolArrow.setRotation(90);
			initSysVolEqualizerPanel();
		}
		
		if(mScrollY>0) {
			root.post(() -> sv.scrollTo(0, mScrollY));
			//CMN.Log("重新滚动唷！", mScrollY);
		}
	}

	public void refresh() {
		CMN.Log("刷新全部数据...");
		ReadDelegatableSettings(0);
		if (immersiveSettings!=null) {
			immersiveSettings.refresh();
		}
		if (textSettings!=null) {
			textSettings.refresh();
			setTextZoomNumber();
		}
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
				if (immersiveSettings==null) {
					initImmersiveSettingsPanel();
				}
				immersiveSettings.settingsLayout.setVisibility(show?View.VISIBLE:View.GONE);
			}  break;
			case R.id.imm_rlm: {
				onPickingDelegate(immersiveSettings, ImmersiveSettings, 0, 0, 0);
			}  break;
			case R.id.fs: {
				boolean show = opt.toggleAdjustFrequentSettingsShown();
				UIData.fsArrow.animate().rotation(show?90:0);
				if (frequentSettings==null) {
					initFrequentSettingsPanel();
				}
				frequentSettings.settingsLayout.setVisibility(show?View.VISIBLE:View.GONE);
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
				if (textSettings==null) {
					initTextSettingsPanel();
				}
				textSettings.settingsLayout.setVisibility(show?View.VISIBLE:View.GONE);
			}  break;
			case R.id.text_rlm: {
				onPickingDelegate(textSettings, TextSettings, 0, 0, 0);
			}  break;
			case R.id.sys_vol:{
				boolean show = opt.toggleAdjustSystemVolumeShown();
				UIData.sysVolArrow.animate().rotation(show?90:0);
				if (sysVolEq==null) {
					initSysVolEqualizerPanel();
				}
				sysVolEq.setVisibility(show?View.VISIBLE:View.GONE);
			} break;
			case R.id.sys_vol_switch:
				opt.setAdjustSystemVolume(checked);
				Utils.setSystemEqualizer(opt, false);
			break;
			default:
				super.onClick(v);
		}
	}
	
	@Override
	public void onAction(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, boolean dynamic, boolean val) {
		CMN.Log("onAction", flagIdxSection, flagPos, dynamic, val);
		if (flagIdxSection!=0) {
			if (dynamic) {
				if (mFlagAdapter.getDynamicFlagIndex(flagIdxSection)<6) {
					WebFrameLayout.GlobalSettingsVersion ++;
				}
				if (flagIdxSection==TextSettings) {
					ResetTextSettings(null, 0);
				} else if (flagIdxSection==ImmersiveSettings) {
					a.ResetIMSettings();
				} else {
					mSettingsChanged|=flagIdxSection;
				}
			}
		}
		if (flagIdxSection==0) {
			switch (flagPos) {
				// 缩放值预设
				case 0: {
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
		popupMenu.show(settingsLayout, vLocationOnScreen[0]+(int) (60*GlobalOptions.density), vLocationOnScreen[1]+(int) (xy.lastY-settingsLayout.getScrollY()));
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
	
	private void initTextSettingsPanel() {
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
		Utils.addViewToParent(textSettings.settingsLayout, root, UIData.textPanel);
	}
	
	private void initImmersiveSettingsPanel() {
		immersiveSettings = new SettingsPanel(a, opt
				, new String[][]{new String[]{null, "滑动隐藏顶栏", "滑动隐藏底栏"}}
				, new int[][]{new int[]{Integer.MAX_VALUE
				, makeDynInt(ImmersiveSettings, 16, true)
				, makeDynInt(ImmersiveSettings, 17, false)}});
		immersiveSettings.setEmbedded(this);
		immersiveSettings.init(a, root);
		Utils.addViewToParent(immersiveSettings.settingsLayout, root, UIData.immPanel);
	}
	
	private void initFrequentSettingsPanel() {
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
		Utils.addViewToParent(frequentSettings.settingsLayout, root, UIData.fsPannel);
	}
	
	boolean eq_panel_tracked;
	
	private void initSysVolEqualizerPanel() {
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
