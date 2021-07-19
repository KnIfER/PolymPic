package com.knziha.polymer.widgets;

import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.browser.webkit.WebViewHelper;
import com.knziha.polymer.preferences.HorizontalSettingsPanel;
import com.knziha.polymer.preferences.SettingsPanel;

import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_DOMAIN;
import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_SYSTEM;
import static com.knziha.polymer.Utils.Options.WebViewSettingsSource_TAB;

public class MergedWebOptWidget extends HorizontalSettingsPanel {
	int mFlagPos;
	boolean reversed;
	public MergedWebOptWidget(BrowserActivity a, ViewGroup root, int flagPos, boolean reversed) {
		super(a, root, 0, (int) (GlobalOptions.density*30)
				, a, a.opt
				, null
				,  new int[][]{new int[]{0
						, makeInt(3, flagPos, false)
						, makeInt(WebViewSettingsSource_TAB, flagPos, reversed)
						, makeInt(WebViewSettingsSource_DOMAIN, flagPos, reversed)
				}}, new int[][]{new int[]{0
						, R.drawable.ic_polymer_blue
						, R.drawable.ic_viewpager_carousel_1
						, R.drawable.ic_domain_bk
				}});
		this.mFlagPos = flagPos;
		this.reversed = reversed;
		this.mViewAttachIdx = Integer.MAX_VALUE;
	}
	
	@Override
	protected void onAction(int flagIdx, int flagPos, boolean dynamic, boolean val) {
		super.onAction(flagIdx, flagPos, dynamic, val);
		View menuView = WebViewHelper.LookForANobleSteedCorrespondingWithDrawnClasses(root, 0, ViewGroup.class, TextMenuView.class);
		//CMN.Log("onAction...", EvalBooleanForMergedFlag(), menuView);
		if(menuView!=null) {
			menuView.setActivated(EvalBooleanForMergedFlag());
		}
	}
	
	@Override
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss) {
		boolean ret = super.toggle(root, parentToDismiss);
		if (ret) {
			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) settingsLayout.getLayoutParams();
			int pad = (int) (GlobalOptions.density*2);
			lp.width = root.getMeasuredWidth() - pad*2;
			lp.leftMargin = lp.rightMargin = pad;
		}
		return ret;
	}
	
	private boolean EvalBooleanForMergedFlag() {
		long flag = mFlagAdapter.Flag(WebViewSettingsSource_TAB)|mFlagAdapter.Flag(WebViewSettingsSource_DOMAIN)|mFlagAdapter.Flag(WebViewSettingsSource_SYSTEM);
		return reversed ^ (((flag>>mFlagPos)&0x1)!=0);
	}
}
