package com.knziha.polymer.preferences;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.IU;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.XYLinearLayout;

public class SettingsPanel extends AnimatorListenerAdapter implements View.OnClickListener {
	public ViewGroup settingsLayout;
	protected LinearLayout linearLayout;
	protected boolean bIsShowing;
	protected int bottomPadding;
	protected final Options opt;
	protected String[][] UITexts;
	protected int[][] UITags;
	protected int[][] UIDrawable;
	protected boolean isHorizontal;
	protected boolean shouldWrapInScrollView = true;
	protected boolean bShouldRemoveAfterDismiss = true;
	protected boolean hasDelegatePicker;
	protected boolean showInPopWindow;
	public PopupWindow pop;
	protected int mPaddingLeft=10;
	protected int mPaddingRight=10;
	protected int mPaddingTop=0;
	protected int mPaddingBottom=0;
	protected int mItemPaddingLeft=5;
	protected int mItemPaddingTop=8;
	protected int mItemPaddingBottom=8;
	protected int mBackgroundColor = 0xefffffff;
	protected int mViewAttachIdx = -1;
	protected SettingsPanel parent;
	private SettingsPanel parentToDismiss;
	
	public void setEmbedded(ActionListener actionListener){
		mPaddingLeft = 15;
		mPaddingTop = 2;
		mPaddingBottom = 15;
		shouldWrapInScrollView = false;
		bShouldRemoveAfterDismiss = false;
		mItemPaddingLeft = 8;
		mItemPaddingTop = 9;
		mItemPaddingBottom = 9;
		mActionListener = actionListener;
		hasDelegatePicker = true;
		if (actionListener instanceof SettingsPanel) {
			parent = (SettingsPanel) actionListener;
		}
	}
	
	public void setActionListener(ActionListener mActionListener) {
		this.mActionListener = mActionListener;
	}
	
	final static int MAX_FLAG_COUNT=7; // 当前支持7个标志位存储容器，其中第六第七为动态容器。
	
	final static int MAX_FLAG_POS_MASK=(1<<8)-1; // 长整型，位数 64，存储需要 7 位。
	
	protected final static int FLAG_IDX_SHIFT=16;
	
	final static int BIT_IS_REVERSE=1<<9;
	final static int BIT_IS_DYNAMIC=1<<10;
	protected final static int BIT_HAS_ICON=1<<11;
	
	// Flag-Idx  BITs   Flag-Pos
	// 索引   选项   偏移
	
	protected static int makeInt(int flagIdx, int flagPos, boolean reverse) {
		return makeInt(flagIdx, flagPos, reverse, false);
	}
	
	protected static int makeDynInt(int flagIdx, int flagPos, boolean reverse) {
		return makeInt(flagIdx, flagPos, reverse, true);
	}
	
	protected static int makeDynIcoInt(int flagIdx, int flagPos, boolean reverse) {
		return makeInt(flagIdx, flagPos, reverse, true)|BIT_HAS_ICON;
	}
	
	protected static int makeInt(int flagIdx, int flagPos, boolean reverse, boolean dynamic) {
		int ret=flagPos&MAX_FLAG_POS_MASK;
		if (reverse) {
			ret|=BIT_IS_REVERSE;
		}
		if (dynamic) {
			ret|=BIT_IS_DYNAMIC;
		}
		ret|=flagIdx<<FLAG_IDX_SHIFT;
		return ret;
	}
	
	public void setBottomPadding(int padding) {
		if (!showInPopWindow) {
			View v = settingsLayout.getChildAt(0);
			v.setPadding(0, 0, 0, padding);
		}
	}
	
	public interface ActionListener{
		void onAction(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, boolean dynamic, boolean val);
		void onPickingDelegate(SettingsPanel settingsPanel, int flagIdxSection, int flagPos, int lastX, int lastY);
	}
	protected ActionListener mActionListener;
	
	public interface FlagAdapter{
		/** 取得标志位 */
		long Flag(int flagIndex);
		/** 保存标志位 */
		void Flag(int flagIndex, long val);
		/** 计算动态容器索引 */
		int getDynamicFlagIndex(int flagIdx);
		/** 选择动态容器 */
		void pickDelegateForSection(int flagIdx, int pickIndex);
	}
	protected final FlagAdapter mFlagAdapter;
	
	/** 装饰动态索引 */
	protected Drawable getIconForDynamicFlagBySection(int section){
		if (parent!=null) {
			return parent.getIconForDynamicFlagBySection(section);
		}
		return null;
	};
	
	/** 取得动态容器 */
	public long getDynamicFlag(int section) {
		return mFlagAdapter.Flag(mFlagAdapter.getDynamicFlagIndex(section));
	}
	
	/** 保存动态容器 */
	public void putDynamicFlag(int scrollSettings, long flag) {
		mFlagAdapter.Flag(mFlagAdapter.getDynamicFlagIndex(scrollSettings), flag);
	}
	
	protected boolean getBooleanInFlag(int storageInt) {
		//if (storageInt==0||storageInt==Integer.MAX_VALUE) return false;
		int flagIdx=storageInt>>FLAG_IDX_SHIFT;
		if(flagIdx==0) {
			return false;
		}
		int flagPos=storageInt&MAX_FLAG_POS_MASK;
		boolean reverse = (storageInt&BIT_IS_REVERSE)!=0;
		boolean dynamic = (storageInt&BIT_IS_DYNAMIC)!=0;
		if (dynamic) {
			flagIdx = mFlagAdapter.getDynamicFlagIndex(flagIdx);
		}
		return EvalBooleanForFlag(flagIdx, flagPos, reverse);
	}
	
	protected void setBooleanInFlag(int storageInt, boolean val) {
		int flagIdx=storageInt>>FLAG_IDX_SHIFT;
		int flagPos=storageInt&MAX_FLAG_POS_MASK;
		CMN.Log("setBooleanInFlag", flagIdx, val);
		boolean reverse = (storageInt&BIT_IS_REVERSE)!=0;
		boolean dynamic = (storageInt&BIT_IS_DYNAMIC)!=0;
		if(flagIdx!=0) {
			PutBooleanForFlag(dynamic?mFlagAdapter.getDynamicFlagIndex(flagIdx):flagIdx, flagPos, val, reverse);
		}
		onAction(flagIdx, flagPos, dynamic, val);
	}
	
	public boolean EvalBooleanForFlag(int flagIndex, int flagPos, boolean reverse) {
		long flag = mFlagAdapter.Flag(flagIndex);
		return reverse ^ (((flag>>flagPos)&0x1)!=0);
	}
	
	public void PutBooleanForFlag(int flagIndex, int flagPos, boolean value, boolean reverse) {
		long flag = mFlagAdapter.Flag(flagIndex);
		long mask = 1l<<flagPos;
		if(value ^ reverse) {
			flag |= mask;
		} else {
			flag &= ~mask;
		}
		mFlagAdapter.Flag(flagIndex, flag);
	}
	
	protected void onAction(int flagIdx, int flagPos, boolean dynamic, boolean val) {
		if (mActionListener!=null) {
			mActionListener.onAction(this, flagIdx, flagPos, dynamic, val);
		}
	}
	
	public SettingsPanel(@NonNull FlagAdapter mFlagAdapter, @NonNull Options opt, String[][] UITexts, int[][] UITags, int[][] drawable) {
		this.bottomPadding = 0;
		this.opt = opt;
		this.mFlagAdapter = mFlagAdapter;
		this.UITexts = UITexts;
		this.UITags = UITags;
		this.UIDrawable = drawable;
	}
	
	public SettingsPanel(@NonNull Context context, @NonNull ViewGroup root, int bottomPadding, @NonNull Options opt, @NonNull FlagAdapter mFlagAdapter) {
		this.bottomPadding = bottomPadding;
		this.opt = opt;
		this.mFlagAdapter = mFlagAdapter;
		init(context, root);
		ViewGroup sl = this.settingsLayout;
		if (bottomPadding>0) {
			sl.setAlpha(0);
			if(isHorizontal) {
				sl.setTranslationX(bottomPadding);
			} else {
				sl.setTranslationY(bottomPadding);
			}
			sl.setBackgroundColor(mBackgroundColor);
		} else {
			sl.setBackgroundColor(mBackgroundColor);
		}
		if(root!=null && !showInPopWindow) {
			Utils.addViewToParent(sl, root);
		}
	}
	
	protected void init(Context context, ViewGroup root) {
		//settingsLayout = getLayoutInflater().inflate(R.layout.test_settings, UIData.webcoord, false);
		if (settingsLayout!=null) {
			return;
		}
		LinearLayout lv = linearLayout = hasDelegatePicker?new XYLinearLayout(context):new LinearLayout(context);
		lv.setOrientation(LinearLayout.VERTICAL);
		float density = GlobalOptions.density;
		lv.setPadding((int) (mPaddingLeft*density), (int) (mPaddingTop*density), (int) (mPaddingRight*density), (int) (mPaddingBottom*density));
		//ArrayList<View> views;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
		int storageInt;
		for (int i = 0; i < UITexts.length; i++) {
			String[] group = UITexts[i];
			int[] tags_group = UITags[i];
			if(group[0]!=null) { // 需显示标题
				TextView groupTitle = new TextView(context, null, 0);
				//groupTitle.setTextAppearance(android.R.attr.textAppearanceLarge);
				groupTitle.setText(group[0]);
				groupTitle.setPadding((int) (2*density), (int) (8*density), 0, (int) (8*density));
				lv.addView(groupTitle, lp);
			}
			for (int j = 1; j < group.length; j++) { // 建立子项
				RadioSwitchButton button = new RadioSwitchButton(context);
				button.setText(group[j]);
				button.setButtonDrawable(R.drawable.radio_selector);
				button.setPadding((int) (mItemPaddingLeft*density), (int) (mItemPaddingTop*density), 0, (int) (mItemPaddingBottom*density));
				button.setOnClickListener(this);
				storageInt = tags_group[j];
				if (storageInt != Integer.MAX_VALUE) {
					if(storageInt!=0) {
						button.setChecked(getBooleanInFlag(storageInt));
					}
					button.setTag(storageInt);
				}
				if ((storageInt&BIT_HAS_ICON)!=0) {
					Drawable drawable = getIconForDynamicFlagBySection(storageInt>>FLAG_IDX_SHIFT);
					//CMN.Log("drawable::??", drawable);
					if (drawable!=null) {
						button.setCompoundDrawables(null, null, drawable, null);
					}
				}
				lv.addView(button, lp);
			}
		}
		//View v = new View(context);
		//lv.addView(v);
		//v.getLayoutParams().width = -1;
		//v.getLayoutParams().height = (int) (bottomPaddding);
		if (shouldWrapInScrollView) {
			ScrollView sv = new ScrollView(context);
			sv.addView(lv, lp);
			sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
			settingsLayout = sv;
		} else {
			settingsLayout = lv;
		}
	}
	
	public void refresh() {
		if(linearLayout!=null)
		for (int i = 0, len=linearLayout.getChildCount(); i < len; i++) {
			View v = linearLayout.getChildAt(i);
			if (v instanceof RadioSwitchButton) {
				RadioSwitchButton button = (RadioSwitchButton)v;
				int storageInt = IU.parsint(v.getTag(), 0);
				if (storageInt!=Integer.MAX_VALUE) {
					button.setChecked(getBooleanInFlag(storageInt));
					if ((storageInt&BIT_HAS_ICON)!=0) {
						Drawable drawable = getIconForDynamicFlagBySection(storageInt>>FLAG_IDX_SHIFT);
						if (drawable!=null) {
							button.setCompoundDrawables(null, null, drawable, null);
						}
					}
				}
			}
		}
	}
	
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss) {
		if (settingsLayout==null && root!=null) {
			init(root.getContext(), root);
		}
		float targetAlpha = 1;
		float targetTrans = 0;
		if (bIsShowing=!bIsShowing) {
			if (showInPopWindow) {
				showPop();
			} else {
				Utils.addViewToParent(settingsLayout, root, mViewAttachIdx);
			}
			settingsLayout.setVisibility(View.VISIBLE);
		} else {
			targetAlpha = 0;
			targetTrans = bottomPadding;
		}
		ViewPropertyAnimator animator = settingsLayout.animate().alpha(targetAlpha);
		if (isHorizontal) {
			animator.translationX(targetTrans);
		} else {
			animator.translationY(targetTrans);
		}
		animator.setDuration(180)
				.setListener(this)
				//.start()
		;
		if (!bIsShowing) {
			onDismiss();
		} else if (parentToDismiss!=null) {
			this.parentToDismiss = parentToDismiss;
		}
		return bIsShowing;
	}
	
	protected void showPop() {
		throw new RuntimeException("Stub!");
	}
	
	protected void onDismiss() { }
	
	@Override
	public void onAnimationEnd(Animator animation) {
		ValueAnimator va = (ValueAnimator) animation;
		if (va==null || va.getAnimatedFraction()==1) {
			if (!bIsShowing) {
				CMN.Log("dismiss!!!");
				if (pop!=null) {
					pop.dismiss();
				} else {
					settingsLayout.setVisibility(View.GONE);
					if (bShouldRemoveAfterDismiss) {
						Utils.removeView(settingsLayout);
					}
				}
				if (settingsLayout.getAlpha()==0 && (isHorizontal?settingsLayout.getTranslationX():settingsLayout.getTranslationY())==0) {
					if (isHorizontal) {
						settingsLayout.setTranslationX(bottomPadding);
					} else {
						settingsLayout.setTranslationY(bottomPadding);
					}
				}
			} else {
				if (parentToDismiss!=null) {
					if ((mBackgroundColor&0xff000000)!=0xff000000) {
						parentToDismiss.fadeOut();
					} else {
						parentToDismiss.hide();
					}
					parentToDismiss = null;
				}
			}
		}
		
	}
	
	@Override
	public void onClick(View v) {
		if (v instanceof RadioSwitchButton) {
			RadioSwitchButton button = (RadioSwitchButton)v;
			//((Toastable_Activity)v.getContext()).showT("v::"+button.isChecked());
			int storageInt = IU.parsint(v.getTag(), 0);
			if(storageInt!=0) {
				Drawable d;
				if (linearLayout instanceof XYLinearLayout && (d=button.getCompoundDrawables()[2])!=null && (storageInt&BIT_IS_DYNAMIC)!=0) {
					XYLinearLayout xy = ((XYLinearLayout) linearLayout);
					if (xy.lastX>xy.getWidth()-v.getPaddingRight()-d.getIntrinsicWidth()-GlobalOptions.density*8) {
						button.setChecked(!button.isChecked());
						if (mActionListener!=null) {
							mActionListener.onPickingDelegate(this, storageInt>>FLAG_IDX_SHIFT, storageInt&MAX_FLAG_POS_MASK, (int)xy.lastX, (int)xy.lastY+settingsLayout.getTop());
						}
						//button.jumpDrawablesToCurrentState();
						return;
					}
				}
				setBooleanInFlag(storageInt, button.isChecked());
				if((storageInt>>FLAG_IDX_SHIFT)==0) {
					button.setChecked(false); // 容器索引为零，代表非选项
				}
				//((Toastable_Activity)v.getContext()).showT(
				//		"v::"+button.isChecked()+"::"+getBooleanInFlag(storageInt)+opt.getHideKeyboardOnScrollSearchHints());
				
			}/* else if (mActionListener!=null) {
				mActionListener.onAction(this, storageInt>>FLAG_IDX_SHIFT, storageInt&MAX_FLAG_POS_MASK, false, false);
			}*/
		}
	}
	
	public boolean isVisible() {
		return bIsShowing;
	}
	
	public void dismiss() {
		if(bIsShowing) {
			toggle(null, null);
		}
	}
	
	private void fadeOut() {
		if(bIsShowing) {
			bIsShowing=false;
			settingsLayout
				.animate()
				.alpha(0)
				.setDuration(300)
				.setListener(this);
			onDismiss();
		}
	}
	
	public void hide() {
		if(bIsShowing) {
			bIsShowing=false;
			settingsLayout.setAlpha(0);
			if (isHorizontal) {
				settingsLayout.setTranslationX(bottomPadding);
			} else {
				settingsLayout.setTranslationY(bottomPadding);
			}
			onAnimationEnd(null);
			onDismiss();
		}
	}
}
	