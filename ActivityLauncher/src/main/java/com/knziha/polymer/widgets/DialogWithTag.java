package com.knziha.polymer.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.knziha.polymer.R;

public class DialogWithTag extends Dialog {
	public Object tag;
	public DialogWithTag(@NonNull Context context, int themeResId) {
		super(context, themeResId);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(FocusChangeListener!=null) {
			FocusChangeListener.onFocusChange(null, hasFocus);
		}
	}
	
	View.OnFocusChangeListener FocusChangeListener;
	
	public void setOnFocusChangedListener(View.OnFocusChangeListener onFocusChangeListener) {
		FocusChangeListener=onFocusChangeListener;
	}
}
