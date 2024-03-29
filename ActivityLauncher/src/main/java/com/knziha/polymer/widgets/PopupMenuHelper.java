package com.knziha.polymer.widgets;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.R;

import java.util.ArrayList;

public class PopupMenuHelper implements View.OnClickListener, View.OnLongClickListener {
	public final PopupWindow mPopupWindow;
	public final FrameLayout popRoot;
	public final LinearLayout lv;
	public final Context context;
	public Drawable leftDrawable;
	private int[] texts;
	private PopupMenuListener listener;
	private final Runnable postDismissRunnable = this::dismiss;
	private boolean bRecycle = false;
	ArrayList<TextMenuView> tvArr = new ArrayList<>();
	public int tag;
	
	public interface PopupMenuListener{
		boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick);
	}

	public PopupMenuHelper(Context context, int[] texts, PopupMenuListener listener) {
		this.context = context;
		this.leftDrawable = context.getResources().getDrawable(R.drawable.ic_yes_blue);
		lv = new LinearLayout(context);
		lv.setOrientation(LinearLayout.VERTICAL);
		if (texts!=null) {
			initLayout(texts, listener);
		} else {
			bRecycle = true;
		}
		
		ScrollView sv = new ScrollView(context);
		sv.setBackgroundResource(R.drawable.frame_pop_menu);
		sv.setPadding(0, (int) (8*GlobalOptions.density), 0, (int) (10*GlobalOptions.density));
		sv.addView(lv);
		popRoot = new FrameLayout(context);
		int padding = (int) (18*GlobalOptions.density);
		popRoot.setPadding(padding, padding, padding, padding);
		popRoot.setClickable(true);
		popRoot.addView(sv);
		
		mPopupWindow = new PopupWindow(popRoot
				, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
		mPopupWindow.setBackgroundDrawable(new ColorDrawable());
		mPopupWindow.setAnimationStyle(R.style.pop_menu_animation);
	}
	
	public void initLayout(int[] texts, PopupMenuListener listener) {
		this.texts = texts;
		this.listener = listener;
		if (lv.getChildCount()>0) {
			lv.removeAllViews();
		}
		int padding = (int) (11* GlobalOptions.density);
		int padding1 = (int) (32.8*GlobalOptions.density);
		for (int menuPos = 0; menuPos < this.texts.length; menuPos++) {
			int resId = this.texts[menuPos];
			//context.getResources().getResourceTypeName()
			//CMN.Log(Integer.toHexString(resId), "resId");
			if(resId>=0x7f110000) {
				TextMenuView tv;
				if (bRecycle && menuPos<tvArr.size()) {
					tv = tvArr.get(menuPos);
					tv.setOnLongClickListener(null);
					tv.setActivated(false);
				} else {
					tv = new TextMenuView(context);
					tv.setPadding(padding1, padding, padding1, padding);
					tv.setGravity(Gravity.CENTER_VERTICAL);
					tv.setSingleLine(true);
					tv.setClickable(true);
					tv.setOnClickListener(this);
					tv.setBackground(Utils.getThemeDrawable(context, R.attr.listChoiceBackgroundIndicator));
					if (bRecycle) {
						tvArr.add(tv);
					}
				}
				tv.setText(resId);
				tv.setId(resId);
				tv.setTextColor(Color.BLACK);
				tv.leftDrawable = leftDrawable;
				lv.addView(tv);
			} else {
				try {
					View view = LayoutInflater.from(context).inflate(resId, lv, false);
					if (view.isClickable()) {
						view.setOnClickListener(this);
					}
					if (view.isLongClickable()) {
						view.setOnLongClickListener(this);
					}
					if (view instanceof ViewGroup) {
						Utils.setOnClickListenersOneDepth((ViewGroup) view, this, 999, null);
					}
					lv.addView(view);
				} catch (Exception ignored) { }
			}
		}
	}

	public void show(View anchorView, int x, int y) {
		int windowPos[] = calculatePopWindowPos(anchorView , popRoot, lv.getChildAt(0), x, y);
		mPopupWindow.showAtLocation(anchorView, Gravity.TOP | Gravity.START, windowPos[0], windowPos[1]);
	}
	
	public static int[] calculatePopWindowPos(View anchorView
			, View popView, View itemTestView
			, int anchorX, int anchroY) {
		//if (anchorView.getTag() instanceof View) anchorView = (View) anchorView.getTag();
		final int[] windowPos = new int[2];
		final int screenHeight = getScreenHeight(anchorView.getContext());
		final int screenWidth = getScreenWidth(anchorView.getContext());
		// 测量popView 弹出框
		popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		int padding = popView.getPaddingTop();
		int popHeight = popView.getMeasuredHeight()  - padding*2;
		int popWidth = popView.getMeasuredWidth()    - padding*2;
		int itemHeight = itemTestView==null?0:itemTestView.getHeight() + padding;
		final boolean showUp = screenHeight + itemHeight - anchroY < popHeight;
		final boolean showLeft = screenWidth - anchorX < popWidth;
		
		if (showUp) {
			windowPos[1] = anchroY - popHeight - padding;
		} else {
			windowPos[1] = anchroY - padding;
		}
		if (showLeft) {
			windowPos[0] = anchorX - popWidth - padding;
		} else {
			windowPos[0] = anchorX - padding;
		}
		return windowPos;
	}
	
	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}
	/**
	 * 获取屏幕宽度(px)
	 */
	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}
	
	@Override
	public void onClick(View v) {
		listener.onMenuItemClick(this, v, false);
	}
	
	@Override
	public boolean onLongClick(View v) {
		return listener.onMenuItemClick(this, v, true);
	}
	
	public void dismiss() {
		mPopupWindow.dismiss();
	}
	
	public void postDismiss(int delay) {
		popRoot.postDelayed(postDismissRunnable, delay);
	}
	
	public void modifyMenu(int pos, String text, boolean act) {
		View v = lv.getChildAt(pos);
		if (v!=null) {
			if (text!=null && v instanceof TextView) {
				((TextView) v).setText(text);
			}
			v.setActivated(act);
		}
	}
	
	public PopupMenuListener getListener() {
		return listener;
	}
}
