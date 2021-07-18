package com.knziha.polymer.preferences;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.webstorage.BrowserAppPanel;
import com.knziha.polymer.widgets.DescriptiveImageView;
import com.knziha.polymer.widgets.ScrollViewTransparent;
import com.knziha.polymer.widgets.Utils;

public class MenuGrid extends BrowserAppPanel {
	BrowserActivity a;
	DisplayMetrics dm;
	
	TextPaint menu_grid_painter;
	private ScrollViewTransparent menu_grid;
	private boolean MenuClicked;
	private int lastWidth;
	private int lastHeight;
	
	public MenuGrid(BrowserActivity a) {
		super(a);
	}
	
	@Override
	protected void init(Context context, ViewGroup root) {
		a=(BrowserActivity) context;
		showPopOnAppbar = false;
		
		showInPopWindow = false;
		mBackgroundColor = 0x3E8F8F8F; // 0x3E8F8F8F
		dm = a.dm;
		
		menu_grid_painter = CMN.mTextPainter;
		
		int TargetTransY = -a.UIData.bottombar2.getHeight();
		int legalMenuTransY = (int) (GlobalOptions.density*55);
		
		// init_menu_layout
		// todo avoid
		a.UIData.bottombar2.setOnClickListener(new Utils.DummyOnClick());
		//settingsLayout = (ViewGroup) a.UIData.menuGrid.getViewStub().inflate();
		settingsLayout = (ViewGroup) a.inflater.inflate(R.layout.menu_grid, root, false);
		settingsLayout.setOnClickListener(this);
		
		menu_grid= settingsLayout.findViewById(R.id.menu_grid);
		ViewGroup svp = (ViewGroup) menu_grid.getChildAt(0);
		menu_grid.setTranslationY(TargetTransY + legalMenuTransY);
		for (int i = 0; i < 2; i++) {
			ViewGroup sv = (ViewGroup) svp.getChildAt(i);
			for (int j = 0; j < 5; j++) {
				DescriptiveImageView vv = (DescriptiveImageView) sv.getChildAt(j);
				vv.textPainter=menu_grid_painter;
				vv.setOnClickListener(this);
			}
		}
		
		refreshMenuGridSize(true);
	}
	
	@Override
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss) {
		boolean ret = super.toggle(root, parentToDismiss);
		menu_grid.focusable=ret;
		if (lastWidth!=a.root.getWidth() || lastHeight!=a.root.getHeight()) {
			refreshMenuGridSize(true);
		}
		return ret;
	}
	
	@Override
	protected void decorateInterceptorListener(boolean install) {
		View[] sameView = new View[]{a.UIData.browserWidget7, a.UIData.browserWidget8};
		for (View vue:sameView) {
			if (true) {
				vue.animate().
						alpha(install?0:1)
						.setListener(install?new AnimatorListenerAdapter() {
							@Override
							public void onAnimationEnd(Animator animation) {
								vue.setVisibility(View.INVISIBLE);
							}
						}:null)
						.setDuration(90);
				if (!install) {
					vue.setVisibility(View.VISIBLE);
				}
			} else {
				vue.setAlpha(1);
				vue.setVisibility(install?View.INVISIBLE:View.VISIBLE);
			}
		}
		a.UIData.browserWidget9.setImageResource(install?R.drawable.ic_exit_to_app:R.drawable.ic_home_black_24dp);
	}
	
	void test(){
		//appbar.setExpanded(false, true);
		float alpha = 0;
//		if(bIsShowing) { // 隐藏
//			TargetTransY = TargetTransY + legalMenuTransY;
//			bIsShowing=false;
//			if (!opt.getTransitListBG()) {
//				((View)menu_grid.getParent()).findViewById(R.id.menu_grid_shadow).setVisibility(View.GONE);
//			}
//		}
//		else { // 显示
//			alpha=1;
//			bIsShowing=true;
//			refreshMenuGridSize();
//			menu_grid.setVisibility(View.VISIBLE);
//			((View)menu_grid.getParent()).findViewById(R.id.menu_grid_shadow).setVisibility(View.VISIBLE);
//			//UIData.bottombar.getLayoutParams().height = UIData.mainMenuLst.getHeight()+UIData.bottombar2.getHeight();
//		}
//		// todo menu_grid.focusable=bIsShowing;
//		//UIData.browserWidget11.jumpDrawablesToCurrentState();
//		if (animate) {
//			menu_grid
//				.animate()
//				.translationY(TargetTransY)
//				.alpha(alpha)
//				.setDuration(180)
//				//.setInterpolator(linearInterpolator)
//				.setListener(bIsShowing?null:menu_grid_animlis)
//				.start()
//			;
//			if (opt.getTransitListBG()) {
//				((View)menu_grid.getParent()).findViewById(R.id.menu_grid_shadow)
//						.animate()
//						.alpha(alpha)
//						.setDuration(180)
//						.start();
//			}
//		}
//		else {
//			a.currentViewImpl.suppressSelection(bIsShowing);
//			menu_grid.setTranslationY(TargetTransY);
//			menu_grid.setAlpha(alpha);
//			if (!bIsShowing) {
//				menu_grid_animlis.onAnimationEnd(null);
//			}
//			((View)menu_grid.getParent()).findViewById(R.id.menu_grid_shadow).setAlpha(0);
//			((View)menu_grid.getParent()).findViewById(R.id.menu_grid_shadow).setVisibility(View.GONE);
//		}
	}
	
	@Override
	public void onClick(View v) {
		if(MenuClicked) {
			return;
		}
		//MenuClicked = true;
		switch (v.getId()) {
			case R.id.browser_widget11: {
				a.mInterceptorListenerHandled=true;
				dismiss();
			} break;
			case R.id.browser_widget9: {
				a.mInterceptorListenerHandled=true;
				a.moveTaskToBack(false);
				hide();
			} break;
			/* 历史 */
			case R.id.root: {
				dismiss();
			} break;
			case R.id.menu_icon3: {
				a.showHistory();
			} break;
			/* 下载 */
			case R.id.menu_icon4: {
				a.showDownloads();
			} break;
			/* 分享 */
			case R.id.menu_icon5: {
				a.shareUrlOrText(null, null);
			} break;
			case R.id.menu_icon9: {
				a.showBrowserSettings();
			} return;
			case R.id.menu_icon7: {
				a.showWebAnnots();
			} return;
			case R.id.menu_icon8: {
				a.showNightMode();
			} return;
			case R.id.menu_icon10: {
			} break;
		}
		//v.postDelayed(() -> toggleMenuGrid(true), 250);
	}
	
	public void refreshMenuGridSize(boolean init) {
		CMN.Log("refreshMenuGridSize？？？", dm.widthPixels, this, bIsShowing||init);
		if (bIsShowing||init) {
			lastWidth=a.root.getWidth();
			lastHeight=a.root.getHeight();
			int w = lastWidth;
			if(w>lastHeight) {
				w -= GlobalOptions.density*18;
			}
			int maxWidth = Math.min(w, (int) (GlobalOptions.density*560));
			if(Utils.actualLandscapeMode(a)) {
				maxWidth = Math.min(lastHeight, maxWidth);
			}
			
			menu_grid.setBackgroundResource(R.drawable.frame_top_rounded);
			ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) menu_grid.getLayoutParams();
			int maxHeight = lastHeight;
			CMN.Log("refreshMenuGridSize……", maxWidth, maxHeight);
			layoutParams.width=maxWidth;
			layoutParams.height=GlobalOptions.density*200>maxHeight?maxHeight:-2;
			if(GlobalOptions.isLarge) {
				layoutParams.setMarginEnd((int) (15*GlobalOptions.density));
				int pad = (int) (GlobalOptions.density*8);
				int HPad = (int) (pad*2.25);
				menu_grid.setPadding(HPad, pad/2, HPad, pad*2);
			}
			menu_grid.requestLayout();
		}
	}
}
