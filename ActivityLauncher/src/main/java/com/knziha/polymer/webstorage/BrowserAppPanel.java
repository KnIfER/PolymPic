package com.knziha.polymer.webstorage;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.CallSuper;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.preferences.SettingsPanel;
import com.knziha.polymer.widgets.Utils;

public class BrowserAppPanel extends SettingsPanel {
	protected BrowserActivity a;
	protected boolean bShouldInterceptClickListener = true;
	protected boolean showPopOnAppbar;
	
	public BrowserAppPanel(BrowserActivity a) {
		super(a, a.UIData.webcoord, a.UIData.bottombar2.getHeight()/2, a.opt, a);
		this.a = a;
		if (!showInPopWindow) {
			Utils.embedViewInCoordinatorLayout(settingsLayout, !showPopOnAppbar);
		}
	}
	
	@Override
	protected void showPop() {
		if (pop==null) {
			pop = new PopupWindow(a);
			pop.setContentView(settingsLayout);
		}
		//pop.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		a.embedPopInCoordinatorLayout(pop, !showPopOnAppbar);
	}
	
	@CallSuper
	@Override
	protected void onDismiss() {
		if(bShouldInterceptClickListener) {
			if (a.mInterceptorListener==this) {
				a.mInterceptorListener = null;
			}
			decorateInterceptorListener(false);
		}
	}
	
	protected void decorateInterceptorListener(boolean install) { }
	
	@Override
	public boolean toggle(ViewGroup root, SettingsPanel parentToDismiss) {
		if(!bIsShowing && bShouldInterceptClickListener) {
			a.mInterceptorListener = this;
			decorateInterceptorListener(true);
		}
		boolean ret = super.toggle(root, parentToDismiss);
		if (ret) {
			a.HideSelectionWidgets(true);
			a.settingsPanel = this;
			if (!showInPopWindow) {
				int pad = a.UIData.bottombar2.getHeight();
				if (a.UIData.appbar.getTop()>=0) {
					pad+=a.UIData.appbar.getHeight();
				}
				setBottomPadding(pad);
			}
		} else if(a.settingsPanel == this){
			a.hideSettingsPanel();
		}
		return ret;
	}
	
	@Override
	public void onAnimationEnd(Animator animation) {
		super.onAnimationEnd(animation);
		ValueAnimator va = (ValueAnimator) animation;
		if (!bIsShowing && (va==null || va.getAnimatedFraction()==1)) {
			boolean b1 = a.settingsPanel == this;
			if (b1) {
				a.hideSettingsPanel();
			}
			if (b1 || a.settingsPanel==null) {
				a.HideSelectionWidgets(false);
			}
		}
	}
}
