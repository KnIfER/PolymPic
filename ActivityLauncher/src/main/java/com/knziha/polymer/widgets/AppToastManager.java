package com.knziha.polymer.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;

public class AppToastManager {
	private final View appToast;
	private final BrowserActivity a;
	private String appUrl;
	TextView msgBtn;
	TextView yesBtn;
	
	Dialog d;
	FrameLayout fl;
	ViewGroup.LayoutParams lp;
	
	public AppToastManager(BrowserActivity a) {
		this.appToast = a.UIData.appToast.getViewStub().inflate();
		msgBtn = appToast.findViewById(R.id.text1);
		yesBtn = appToast.findViewById(R.id.confirm_button);
		this.a = a;
		yesBtn.setOnClickListener(v -> {
			if(appUrl!=null) {
				if(appUrl.equals("history")) {
					a.showHistory();
				} else if(appUrl.equals("downloads")) {
					a.showDownloads();
				} else {
					try {
						this.a.startActivity(new Intent(Intent.ACTION_VIEW
								, Uri.parse(appUrl)));
						appToast.setTag(null);
					} catch (Exception e) {
						CMN.Log(e);
						this.a.showT("跳转失败…");
					}
				}
			}
		});
	}
	
	AnimatorListenerAdapter animateOutListener = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationEnd(Animator animation) {
			appToast.setVisibility(View.GONE);
			if(appToast.getParent()!=a.root) {
				d.dismiss();
			}
		}
	};
	
	Runnable animateOutRunnable;
	
	public void showT(String message, String yesText, String url, float dimAmount) {
		msgBtn.setText(message);
		yesBtn.setText(yesText);
		appUrl = url;
		appToast.setVisibility(View.VISIBLE);
		
		float targetY = 10 * GlobalOptions.density;
		
		if(animateOutRunnable==null) {
			animateOutRunnable = () -> appToast.animate()
					.alpha(0)
					.translationY(targetY)
					.setListener(animateOutListener);
		}
		a.root.removeCallbacks(animateOutRunnable);
		
		if(dimAmount>=0) {
			if(d ==null){
				d = new Dialog(a);
			}
			
			d.show();
			
			Window window = d.getWindow();
			window.setDimAmount(dimAmount);
			WindowManager.LayoutParams layoutParams = window.getAttributes();
			layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
			layoutParams.horizontalMargin = 0;
			layoutParams.verticalMargin = 0;
			window.setAttributes(layoutParams);
			
			//window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			window.getDecorView().setBackground(null);
			window.getDecorView().setPadding(0,0,0,0);
			
			if(fl==null) {
				fl = new FrameLayout(a);
				fl.setOnClickListener(v -> {
					appToast.animate().setListener(null).cancel();
					d.dismiss();
				});
				lp = appToast.getLayoutParams();
				d.setContentView(fl);
			}
			Utils.addViewToParent(appToast, fl);
		}
		else {
			Utils.addViewToParent(appToast, a.root);
		}
		
		appToast.setAlpha(0);
		appToast.setTranslationY(targetY);
		appToast.animate().setListener(null)
				.alpha(1)
				.translationY(0)
		;
		a.root.postDelayed(animateOutRunnable, 2350+180);
	}
	
	
	public boolean visible() {
		return appToast.getVisibility()==View.VISIBLE;
	}
	
	public void hide() {
		appToast.setVisibility(View.GONE);
	}
}
