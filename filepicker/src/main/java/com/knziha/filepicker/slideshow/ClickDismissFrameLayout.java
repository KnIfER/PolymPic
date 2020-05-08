package com.knziha.filepicker.slideshow;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.filepicker.utils.CMNF;

public class ClickDismissFrameLayout extends FrameLayout {
	private GestureDetector mGestureDetector;
	private OnClickListener mOnClickListener;

	public ClickDismissFrameLayout(@NonNull Context context) {
		super(context);
		init(context);
	}

	public ClickDismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ClickDismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	private void init(Context context) {


		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			public boolean onDoubleTap(MotionEvent e) {
				return false;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				return super.onDown(e);
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if(mOnClickListener!=null)
					mOnClickListener.onClick(ClickDismissFrameLayout.this);
				//CMNF.Log("onSingleTapConfirmed"); //太慢了点点
				return super.onSingleTapConfirmed(e);
			}
		});

	}

	@Override
	public void setOnClickListener(@Nullable OnClickListener l) {
		mOnClickListener=l;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		mGestureDetector.onTouchEvent(ev);
		return super.onInterceptTouchEvent(ev);
	}

}
