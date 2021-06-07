package com.knziha.polymer.preferences;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.Options;

public class SearchHistoryAndInputMethodSettings extends SettingsPanel {
	BrowserActivity a;
	public SearchHistoryAndInputMethodSettings(Context context, ViewGroup root, int bottomPaddding, Options opt) {
		super(context, root, bottomPaddding, opt);
		a = (BrowserActivity) context;
		CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) settingsLayout.getLayoutParams();
		params.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
		params.width = -1;
		params.height = -1;
		//params.topMargin = UIData.appbar.getHeight()-TargetTransY;
		//root.setForegroundGravity();
		params.setBehavior(new AppBarLayout.ScrollingViewBehavior(context, null));
	}
	
	private final static String[][] UITexts = new String[][]{
			{"搜索框", "立即显示输入法", "自动全选", "清空后显示输入法"}
			,{"搜索词/历史记录列表"
			, "自动展开搜索记录"
			, "清空文本后，展开搜索记录"
			, "展开列表时，收起输入法"
			, "滚动列表时，收起输入法"
			, "淡入透明背景色"
	}};
	
	private final static Object[][] UITags = new Object[][]{
			// 	 , getShowImeImm , getSelectAllOnFocus, getShowKeyIMEOnClean
			{null, makeInt(1, 24, true), makeInt(1, 23, true), makeInt(1, 57, true)}
			,{null
			, makeInt(1, 51, true) // getShowSearchHints
			, makeInt(1, 53, true) // getShowSearchHintsOnClear
			, makeInt(1, 55, true) // getHideKeyboardOnShowSearchHints
			, makeInt(1, 56, true) // getHideKeyboardOnScrollSearchHints
			, makeInt(1, 54, true) // getTransitListBG
	}};
	
	@Override
	protected void onAction(int flagIdx, int flagPos, boolean val) {
		if (flagIdx==1 && flagPos==23) {
			a.UIData.etSearch.setSelectAllOnFocus(opt.getSelectAllOnFocus());
		}
	}
	
	@Override
	protected void init() {
		super.UITexts = UITexts;
		super.UITags = UITags;
		super.listenToActions = true;
	}
}
	