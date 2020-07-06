package com.KnaIvER.polymer.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.KnaIvER.polymer.Utils.CMN;

public class EditTextmy extends EditText {
	public EditTextmy(Context context) {
		super(context);
	}
	
	public EditTextmy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public EditTextmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
//	@Override
//	public void getLocationOnScreen(int[] outLocation) {
//		super.getLocationOnScreen(outLocation);
//		outLocation[1]+=getHeight();
//		CMN.Log("getLocationOnScreen 0", outLocation[0], outLocation[1]);
//
//	}
//
//	@Override
//	public void getLocationInWindow(int[] outLocation) {
//		super.getLocationInWindow(outLocation);
//		outLocation[1]+=getHeight();
//		CMN.Log("getLocationOnScreen 2", outLocation[0], outLocation[1]);
//	}
	
	
	@Override
	public int getLineBounds(int line, Rect bounds) {
		CMN.Log("getLineBounds");
		return super.getLineBounds(line, bounds);
	}
}
