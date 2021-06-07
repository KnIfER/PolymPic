package com.knziha.polymer.widgets;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.textclassifier.TextClassifier;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView.OnScrollChangedListener;

import static androidx.appcompat.widget.AppCompatEditText.UrlFucker;

public class EditTextmy extends EditText {
	public boolean bNeverBlink = false;
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
	
	
//	@Override
//	public int getLineBounds(int line, Rect bounds) {
//		CMN.Log("getLineBounds");
//		return super.getLineBounds(line, bounds);
//	}
	
	
	@Override
	public boolean postDelayed(Runnable action, long delayMillis) {
		if (bNeverBlink && action.getClass().getName().contains("Blink")) {
			return false;
		}
		return super.postDelayed(action, delayMillis);
	}
	
	public static TextPaint hackTp;
	@Override
	public TextPaint getPaint() {
		if(true){ //PDICMainAppOptions.getHackDisableMagnifier()
			if(EditTextmy.hackTp==null){
				EditTextmy.hackTp = new TextPaint();
				EditTextmy.hackTp.setTextSize(1000);
			};
			return EditTextmy.hackTp;
		}
		return super.getPaint();
	}
	
	
	/**
	 * Returns the {@link TextClassifier} used by this TextView.
	 * If no TextClassifier has been set, this TextView uses the default set by the
	 * {@link android.view.textclassifier.TextClassificationManager}.
	 */
	@Override
	@NonNull
	@RequiresApi(api = 26)
	public TextClassifier getTextClassifier() {
		// The null check is necessary because getTextClassifier is called when we are invoking
		// the super class's constructor.
		if(true && UrlFucker !=null) {
			return UrlFucker;
		}
		return super.getTextClassifier();
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		if(mOnScrollChangeListener !=null)
			mOnScrollChangeListener.onScrollChange(this,l,t,oldl,oldt);
	}
	
	public void setOnScrollChangedListener(OnScrollChangedListener onSrollChangedListener) {
		mOnScrollChangeListener=onSrollChangedListener;
	}
	OnScrollChangedListener mOnScrollChangeListener;
}
