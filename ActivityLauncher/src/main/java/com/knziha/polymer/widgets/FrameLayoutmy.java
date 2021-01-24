package com.knziha.polymer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.knziha.polymer.Utils.CMN;

public class FrameLayoutmy extends FrameLayout {
	public FrameLayoutmy(Context context) {
		this(context, null);
	}
	public FrameLayoutmy(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public FrameLayoutmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setTranslationY(float translationY) {
		//CMN.Log("setTranslationY");
		super.setTranslationY(translationY);
	}
	@Override
	public void setTranslationX(float translationY) {
		CMN.Log("setTranslationX");
		super.setTranslationY(translationY);
	}
}
