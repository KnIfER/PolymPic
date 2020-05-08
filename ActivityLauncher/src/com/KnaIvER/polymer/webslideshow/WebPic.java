package com.KnaIvER.polymer.webslideshow;

import android.graphics.Bitmap;

import com.KnaIvER.polymer.widgets.WebViewmy;

public class WebPic {
	final String path;
	private final WebViewmy webView;
	public long time;
	int Page;
	public final static int MaxBitmapRam=45*1024*1024;

	public WebPic(WebViewmy webView) {
		this.webView = webView;
		this.path = "_"+webView.SelfIdx;
		this.time = webView.getTag()==null?-1:webView.time;
	}

	public Bitmap createBitMap() {
		return webView.getBitmap();
	}
}