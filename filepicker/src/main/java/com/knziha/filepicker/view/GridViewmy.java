package com.knziha.filepicker.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.GridView;

public class GridViewmy extends GridView {
	public GridViewmy(Context context) {
		super(context);
	}

	public GridViewmy(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GridViewmy(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setSelectionFromTop(int position, int offset) {
		if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP || offset==0)
			super.setSelection(position);
		else
			super.setSelectionFromTop(position, offset);
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
	}
}
