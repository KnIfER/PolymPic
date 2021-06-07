package com.knziha.polymer.preferences;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.NavigationHomeAdapter;

public class NavHomeEditorDialogSettings extends SettingsPanel {
	final NavigationHomeAdapter navigationHomeAdapter;
	public NavHomeEditorDialogSettings(Context context, ViewGroup root, int bottomPaddding, Options opt, NavigationHomeAdapter navigationHomeAdapter) {
		super(context, root, bottomPaddding, opt);
		this.navigationHomeAdapter = navigationHomeAdapter;
		syncHeights();
		settingsLayout.getChildAt(0).setPadding((int) (15*GlobalOptions.density), (int) (10*GlobalOptions.density), 0, 0);
		settingsLayout.setBackgroundColor(Color.WHITE);
	}
	
	private void syncHeights() {
		View vg = (View) settingsLayout.getParent();
		if (vg!=null) {
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) settingsLayout.getLayoutParams();
			params.width = -1;
			params.height = vg.getHeight()-bottomPaddding;
			//params.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
			//params.bottomMargin = bottomPaddding;
			//params.height = -1;
		}
	}
	
	@Override
	public void toggle(ViewGroup root) {
		super.toggle(root);
		if (bIsShowing) {
			syncHeights();
		}
	}
	
	private final static String[][] UITexts = new String[][]{
	{null, "始终显示多行文本框"
		, "添加新节点至末尾"
		, "使用当前导航节点"
		, "填充当前URL与标题"
	}};
	
	private final static Object[][] UITags = new Object[][]{
	{ null
		, makeInt(1, 58, false) // getAlwaysShowMultilineEditField
		, makeInt(1, 59, true) // getAppendNewNavNodeToEnd
		, makeInt(0, 0, true) // ApplyNavNode
		, makeInt(0, 1, true) // FillNavNode
	}};
	
	protected void onAction(int flagIdx, int flagPos, boolean val) {
		if(flagIdx==0) {
			if (flagPos==0) {
				navigationHomeAdapter.OnNavHomeEditorActions(0);
			}
			else {
				navigationHomeAdapter.OnNavHomeEditorActions(1);
			}
		} else if (flagPos==58) {
			navigationHomeAdapter.OnNavHomeEditorActions(2);
		}
	}
	
	@Override
	protected void init() {
		super.UITexts = UITexts;
		super.UITags = UITags;
		super.shouldRemoveAfterDismiss = false;
		super.listenToActions = true;
	}
}
	