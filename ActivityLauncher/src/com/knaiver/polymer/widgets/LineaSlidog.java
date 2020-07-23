package com.knaiver.polymer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LineaSlidog extends LinearLayout {
	private OnTouchListener toc;
	
	public LineaSlidog(Context context) {
		this(context, null);
	}
	public LineaSlidog(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public LineaSlidog(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean ret=false;
		if(getTag() instanceof Integer) {
			ret = super.dispatchTouchEvent(ev);
			setTag(null);
			return ret;
		}
		if(toc!=null) {
			ret = toc.onTouch(this, ev);
		}
		return ret||super.dispatchTouchEvent(ev);
	}
	
	@Override
	public void setOnTouchListener(OnTouchListener toc) {
		this.toc = toc;
	}
}
