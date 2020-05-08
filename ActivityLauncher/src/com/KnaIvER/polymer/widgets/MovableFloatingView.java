package com.KnaIvER.polymer.widgets;

import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;

public class MovableFloatingView  {
	public View view;

	private WindowManager wm;
	private LayoutParams lp;
	private boolean added;
	private LinearLayout mask;

	public MovableFloatingView(View _view) {
		view = _view;
	}

	public void updateViewPosition() {
		lp.screenOrientation=ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		if (added) {
			wm.updateViewLayout(view, lp);
		} else {
			mask = new LinearLayout(view.getContext());
			mask.setBackgroundColor(0xeeffffff);
			wm.addView(mask, new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT,
					LayoutParams.TYPE_APPLICATION_OVERLAY,
					LayoutParams.FLAG_NOT_FOCUSABLE|
					LayoutParams.FLAG_NOT_TOUCHABLE|
					LayoutParams.FLAG_NOT_TOUCH_MODAL|
					LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|
					LayoutParams.FLAG_LAYOUT_IN_SCREEN,
					PixelFormat.TRANSLUCENT));
			wm.addView(view, lp);
			added = true;
		}
	}
	
	public void removeView() {
		if (added) {
			wm.removeView(view);
			wm.removeView(mask);
			added = false;
		}
	}

	public void init(WindowManager _wm, int height) {
		wm = _wm;
		lp = new LayoutParams(
				LayoutParams.MATCH_PARENT,
				height,
				LayoutParams.TYPE_APPLICATION_OVERLAY,
				LayoutParams.FLAG_NOT_TOUCH_MODAL|
				LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|
				LayoutParams.FLAG_FULLSCREEN|
				LayoutParams.FLAG_LAYOUT_IN_SCREEN
				,PixelFormat.RGB_565);

		view.setTag(lp);

		lp.gravity = Gravity.TOP | Gravity.START;
		lp.x = 0;
		lp.y = 0;
	}
}
