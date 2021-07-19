package com.knziha.polymer.preferences;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.XYLinearLayout;

public class HorizontalSettingsPanel extends SettingsPanel implements View.OnTouchListener, View.OnClickListener {
	final GestureDetector mDetector;
	final float targetAlpha;
	protected final ViewGroup root;
	public HorizontalSettingsPanel(Context context, ViewGroup root, float targetAlpha, int targetTransX, @NonNull FlagAdapter mFlagAdapter, @NonNull Options opt, String[][] UITexts, int[][] UITags, int[][] drawable) {
		super(mFlagAdapter, opt, UITexts, UITags, drawable);
		isHorizontal = true;
		mPaddingLeft = mPaddingRight = 0;
		mBackgroundColor = Color.WHITE;
		mDetector = new GestureDetector(context
				, new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
								   float velocityX, float velocityY) {
				CMN.Log("onFling!!!", velocityX, Math.abs(velocityX)*1.2f>Math.abs(velocityY));
				if(Math.abs(velocityX)*1.2f>Math.abs(velocityY)) {
					if(velocityX!=0) {
						if (velocityX<0) {
							show();
							CMN.Log("show!!!");
						} else {
							dismiss();
						}
						return true;
					}
				}
				return false;
			}
		});
		this.root = root;
		this.targetAlpha = targetAlpha;
		this.bottomPadding = targetTransX;
	}
	
	public void show() {
		if (!bIsShowing) {
			toggle(root, null);
			if (root!=null) {
				Utils.preventDefaultTouchEvent(root, -100, -100);
			}
			refresh();
		}
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		//settingsLayout = getLayoutInflater().inflate(R.layout.test_settings, UIData.webcoord, false);
		if (settingsLayout!=null) {
			return;
		}
		LinearLayout lv = linearLayout = hasDelegatePicker?new XYLinearLayout(context):new LinearLayout(context);
		lv.setOrientation(LinearLayout.HORIZONTAL);
		float density = GlobalOptions.density;
		lv.setPadding((int) (mPaddingLeft*density), (int) (mPaddingTop*density), (int) (mPaddingRight*density), (int) (mPaddingBottom*density));
		//ArrayList<View> views;
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2);
		lp.weight = 1;
		int storageInt;
		for (int i = 0; i < UITags.length; i++) {
			String[] group=UITexts==null?null:UITexts[i];
			int[] tags_group = UITags[i];
			int[] icon_group=UIDrawable==null?null:UIDrawable[i];
			int itemsLength = tags_group.length;
			if(group!=null && group[0]!=null) { // 需显示标题
				TextView groupTitle = new TextView(context, null, 0);
				//groupTitle.setTextAppearance(android.R.attr.textAppearanceLarge);
				groupTitle.setText(group[0]);
				groupTitle.setPadding((int) (2*density), (int) (8*density), 0, (int) (8*density));
				lv.addView(groupTitle, lp);
			}
			for (int j = 1; j < itemsLength; j++) { // 建立子项
				RadioSwitchButton button = new RadioSwitchButton(context);
				if (group!=null) {
					button.setText(group[j]);
				}
				Drawable leftDrawable=null, rightDrawable=null;
				if (icon_group!=null) {
					leftDrawable = context.getResources().getDrawable(icon_group[j]);
					leftDrawable.setBounds(0,0,leftDrawable.getIntrinsicWidth(), leftDrawable.getIntrinsicHeight());
					button.setId(icon_group[j]);
				}
				button.setButtonDrawable(R.drawable.radio_selector);
				button.setPadding((int) (mItemPaddingLeft*density), (int) (mItemPaddingTop*density), 0, (int) (mItemPaddingBottom*density));
				button.setOnClickListener(this);
				button.setOnTouchListener(this);
				storageInt = tags_group[j];
				if (storageInt != Integer.MAX_VALUE) {
					if(storageInt!=0) {
						button.setChecked(getBooleanInFlag(storageInt));
					}
					button.setTag(storageInt);
				}
				if ((storageInt&BIT_HAS_ICON)!=0) {
					rightDrawable = getIconForDynamicFlagBySection(storageInt>>FLAG_IDX_SHIFT);
				}
				if (leftDrawable!=null || rightDrawable!=null) {
					button.setCompoundDrawables(leftDrawable, null, rightDrawable, null);
				}
				lv.addView(button, lp);
			}
		}
		if (false) {
			ScrollView sv = new ScrollView(context);
			sv.addView(lv, lp);
			sv.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
			settingsLayout = sv;
		} else {
			settingsLayout = lv;
		}
		
		settingsLayout.setAlpha(targetAlpha);
		settingsLayout.setTranslationX(bottomPadding);
		settingsLayout.setOnTouchListener(this);
		settingsLayout.setBackgroundColor(Color.WHITE);
		//View v = new View(context);
		//lv.addView(v);
		//v.getLayoutParams().width = -1;
		//v.getLayoutParams().height = (int) (bottomPaddding);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//CMN.Log("onTouch!!!");
		if(event.getActionMasked()== MotionEvent.ACTION_DOWN) {
//			float y = event.getY();
//			View ca=null;
//			int i=0,len=vg.getChildCount();
//			for(;i<len;i++) {
//				ca=vg.getChildAt(i);
//				if(ca.getBottom()>y) {
//					break;
//				}
//			}
		}
		return mDetector.onTouchEvent(event);
	}
}
