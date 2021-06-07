package com.knziha.polymer.preferences;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.IU;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.Utils;

import java.util.ArrayList;

public abstract class SettingsPanel extends AnimatorListenerAdapter implements View.OnClickListener {
	final ViewGroup settingsLayout;
	boolean bIsShowing;
	final int bottomPaddding;
	final Options opt;
	protected String[][] UITexts;
	protected Object[][] UITags;
	protected boolean shouldRemoveAfterDismiss = true;
	protected boolean listenToActions = false;
	
	protected static int makeInt(int flagIdx, int flagPos, boolean reverse) {
		int ret=flagIdx&0x7;
		if (reverse) {
			ret|=0x8;
		}
		ret|=flagPos<<5;
		return ret;
	}
	
	protected boolean getBooleanInFlag(int storageInt) {
		int flagIdx=storageInt&0x7;
		if(flagIdx==0) {
			return false;
		}
		int flagPos=storageInt>>5;
		boolean reverse = (storageInt&0x8)!=0;
		return opt.EvalBooleanForFlag(null, flagIdx, flagPos, reverse);
	}
	
	protected void setBooleanInFlag(int storageInt, boolean val) {
		int flagIdx=storageInt&0x7;
		int flagPos=storageInt>>5;
		boolean reverse = (storageInt&0x8)!=0;
		if(flagIdx!=0) {
			opt.PutBooleanForFlag(null, flagIdx, flagPos, val, reverse);
		}
		if (listenToActions) {
			onAction(flagIdx, flagPos, val);
		}
	}
	
	protected void onAction(int flagIdx, int flagPos, boolean val) { }
	
	public SettingsPanel(Context context, ViewGroup root, int bottomPaddding, Options opt) {
		this.bottomPaddding = bottomPaddding;
		this.opt = opt;
		init();
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
			if(group[0]!=null) {
				TextView groupTitle = new TextView(context, null, 0);
				//groupTitle.setTextAppearance(android.R.attr.textAppearanceLarge);
				groupTitle.setText(group[0]);
				groupTitle.setPadding((int) (2*density), (int) (8*density), 0, (int) (8*density));
				lv.addView(groupTitle, lp);
			}
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
		
		Utils.addViewToParent(settingsLayout, root);
	}
	
	protected abstract void init();
	
	public void toggle(ViewGroup root) {
		settingsLayout.setBackgroundColor(0xefffffff);
		float targetAlpha = 1;
		float targetTrans = 0;
		if (bIsShowing=!bIsShowing) {
			Utils.addViewToParent(settingsLayout, root, -1);
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
				.setListener(bIsShowing?null:this)
				//.start()
		;
	}
	
	@Override
	public void onAnimationEnd(Animator animation) {
		if (!bIsShowing) {
			settingsLayout.setVisibility(View.GONE);
			if (shouldRemoveAfterDismiss) {
				Utils.removeIfParentBeOrNotBe(settingsLayout, null, false);
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
				setBooleanInFlag(storageInt, button.isChecked());
				if((storageInt&0x7)==0) {
					button.setChecked(false);
				}
				//((Toastable_Activity)v.getContext()).showT(
				//		"v::"+button.isChecked()+"::"+getBooleanInFlag(storageInt)+opt.getHideKeyboardOnScrollSearchHints());
				
			}
		}
	}
	
	public boolean isVisible() {
		return bIsShowing;
	}
	
	public void dismiss() {
		if(bIsShowing) {
			toggle(null);
		}
	}
	
	public void hide() {
		if(bIsShowing) {
			bIsShowing=false;
			settingsLayout.setAlpha(0);
			settingsLayout.setTranslationY(bottomPaddding);
			onAnimationEnd(null);
		}
	}
}
	