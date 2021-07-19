package com.knziha.polymer.webfeature;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;

public class NightMode extends BrowserAppPanel {
	BrowserActivity a;
	DisplayMetrics dm;
	
	public NightMode(BrowserActivity a) {
		super(a);
	}
	
	@Override
	public void init(Context context, ViewGroup root) {
		a = (BrowserActivity) context;
		showInPopWindow = true;
		showPopOnAppbar = true;
		
		mBackgroundColor = 0x3E8F8F8F; // 0x3E8F8F8F
		dm = a.dm;
		
		settingsLayout = (ViewGroup) a.inflater.inflate(R.layout.night_mode, a.root, false);
	}
}
