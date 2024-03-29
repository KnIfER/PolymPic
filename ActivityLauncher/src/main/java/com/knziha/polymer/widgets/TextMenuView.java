package com.knziha.polymer.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.knziha.polymer.R;

public class TextMenuView extends TextView {
	public Drawable leftDrawable;
	public boolean activated;
	
	public TextMenuView(Context context) {
		super(context);
	}

	public TextMenuView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		leftDrawable = getResources().getDrawable(R.drawable.ic_yes_blue);
		leftDrawable.setBounds(0, 0, leftDrawable.getIntrinsicWidth(), leftDrawable.getIntrinsicHeight());
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (leftDrawable!=null && activated) {
			int mPaddingLeft = getPaddingLeft();
			int drawableSz = leftDrawable.getIntrinsicWidth();
			int left = (int) ((mPaddingLeft-drawableSz)*0.55);
			int top = (int) ((getMeasuredHeight()-drawableSz)*0.55);
			leftDrawable.setBounds(left, top, left+drawableSz, top+drawableSz);
			leftDrawable.draw(canvas);
		}
	}
	
	@Override
	public void setActivated(boolean activated) {
		if (this.activated!=activated) {
			this.activated = activated;
			invalidate();
		}
	}
	
	@Override
	public boolean isActivated() {
		return activated;
	}
}
