package com.knziha.polymer.preferences;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.IU;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.Utils;

import java.util.ArrayList;

public class SearchHistoryAndInputMethodSettings extends AnimatorListenerAdapter implements View.OnClickListener {
	final ViewGroup settingsLayout;
	boolean bIsShowing;
	final int bottomPaddding;
	final Options opt;
	final static String[][] UITexts = new String[][]{
			{"搜索框", "立即显示输入法", "自动全选", "清空后显示输入法"}
			,{"搜索词/历史记录列表"
			, "自动展开搜索记录"
			, "清空文本后，展开搜索记录"
			, "展开列表时，收起输入法"
			, "滚动列表时，收起输入法"
			, "淡入透明背景色"
	}};
	final static Object[][] UITags = new Object[][]{
			// 	 , getShowImeImm , getSelectAllOnFocus, getShowKeyIMEOnClean
			{null, makeInt(1, 24, true), makeInt(1, 23, true), makeInt(1, 57, true)}
			,{null
			, makeInt(1, 51, true) // getShowSearchHints
			, makeInt(1, 53, true) // getShowSearchHintsOnClear
			, makeInt(1, 55, true) // getHideKeyboardOnShowSearchHints
			, makeInt(1, 56, true) // getHideKeyboardOnScrollSearchHints
			, makeInt(1, 54, true) // getTransitListBG
	}};
	
	private static int makeInt(int flagIdx, int flagPos, boolean reverse) {
		int ret=flagIdx&0x7;
		if (reverse) {
			ret|=0x8;
		}
		ret|=flagPos<<5;
		return ret;
	}
	
	protected boolean getBooleanInFlag(int storageInt) {
		int flagIdx=storageInt&0x7;
		boolean reverse = (storageInt&0x8)!=0;
		int flagPos=storageInt>>5;
		return opt.EvalBooleanForFlag(null, flagIdx, flagPos, reverse);
	}
	
	protected void setBooleanInFlag(int storageInt, boolean val) {
		int flagIdx=storageInt&0x7;
		boolean reverse = (storageInt&0x8)!=0;
		int flagPos=storageInt>>5;
		opt.PutBooleanForFlag(null, flagIdx, flagPos, val, reverse);
	}
	
	public SearchHistoryAndInputMethodSettings(Context context, ViewGroup root, int bottomPaddding, Options opt) {
		this.bottomPaddding = bottomPaddding;
		this.opt = opt;
		//settingsLayout = getLayoutInflater().inflate(R.layout.test_settings, UIData.webcoord, false);
		
		LinearLayout lv = new LinearLayout(context);
		lv.setOrientation(LinearLayout.VERTICAL);
		float density = GlobalOptions.density;
		lv.setPadding((int) (10*density), 0, (int) (10*density), 0);
		ArrayList<View> views;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
		for (int i = 0; i < UITexts.length; i++) {
			String[] group = UITexts[i];
			Object[] tags_group = UITags[i];
			TextView groupTitle = new TextView(context, null, 0);
			//groupTitle.setTextAppearance(android.R.attr.textAppearanceLarge);
			groupTitle.setText(group[0]);
			groupTitle.setPadding((int) (2*density), (int) (8*density), 0, (int) (8*density));
			lv.addView(groupTitle, lp);
			for (int j = 1; j < group.length; j++) {
				RadioSwitchButton button = new RadioSwitchButton(context);
				button.setText(group[j]);
				button.setButtonDrawable(R.drawable.radio_selector);
				button.setPadding((int) (5*density), (int) (8*density), 0, (int) (8*density));
				button.setOnClickListener(this);
				if (tags_group[j]!=null) {
					int storageInt = IU.parsint(tags_group[j], 0);
					if(storageInt!=0) {
						button.setChecked(getBooleanInFlag(storageInt));
					}
					button.setTag(tags_group[j]);
				}
				lv.addView(button, lp);
			}
		}
		//View v = new View(context);
		//lv.addView(v);
		//v.getLayoutParams().width = -1;
		//v.getLayoutParams().height = (int) (bottomPaddding);
		
		ScrollView sv = new ScrollView(context);
		sv.addView(lv, lp);
		sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		
		sv.setBackgroundColor(0xefffffff);
		
		settingsLayout = sv;
		settingsLayout.setAlpha(0);
		settingsLayout.setTranslationY(bottomPaddding);
		
		root.addView(settingsLayout);
		
		CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) settingsLayout.getLayoutParams();
		
		params.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
		
		params.width = -1;
		params.height = -1;
		
		//params.topMargin = UIData.appbar.getHeight()-TargetTransY;
		
		//root.setForegroundGravity();
		params.setBehavior(new AppBarLayout.ScrollingViewBehavior(context, null));
	}
	
	public void toggle(ViewGroup root) {
		float targetAlpha = 1;
		float targetTrans = 0;
		if (bIsShowing=!bIsShowing) {
			Utils.addViewToParent(settingsLayout, root);
			settingsLayout.setVisibility(View.VISIBLE);
		} else {
			targetAlpha = 0;
			targetTrans = bottomPaddding;
		}
		settingsLayout
				.animate()
				.alpha(targetAlpha)
				.translationY(targetTrans)
				.setDuration(180)
				.setListener(this)
				.start()
		;
	}
	
	@Override
	public void onAnimationEnd(Animator animation) {
		if (!bIsShowing) {
			settingsLayout.setVisibility(View.GONE);
			Utils.removeIfParentBeOrNotBe(settingsLayout, null, false);
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v instanceof RadioSwitchButton) {
			RadioSwitchButton button = (RadioSwitchButton)v;
			//((Toastable_Activity)v.getContext()).showT("v::"+button.isChecked());
			
			int storageInt = IU.parsint(v.getTag(), 0);
			if(storageInt!=0) {
				setBooleanInFlag(storageInt, button.isChecked());
				
				//((Toastable_Activity)v.getContext()).showT(
				//		"v::"+button.isChecked()+"::"+getBooleanInFlag(storageInt)+opt.getHideKeyboardOnScrollSearchHints());
				
			}
			
			
		}
	}
	
	public boolean isVisible() {
		return bIsShowing;
	}
}
	