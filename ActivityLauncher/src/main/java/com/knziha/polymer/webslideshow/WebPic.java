package com.knziha.polymer.webslideshow;

import android.graphics.Bitmap;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.widgets.WebViewmy;

public class WebPic {
	final String path;
	public final Object webView;
	public long time;
	long PageId;
	boolean alwaysFromFile = true;
	public final static int MaxBitmapRam=45*1024*1024;

	public WebPic(Object model) {
		BrowserActivity.TabHolder holder;
		webView = model;
		if(model instanceof WebViewmy) {
			WebViewmy webView = (WebViewmy) model;
			holder = ((WebViewmy) model).holder;
			this.time = webView.getTag()==null?-1:webView.time;
			if(webView.getParent()!=null) {
				alwaysFromFile = false;
			}
		} else {
			holder = (BrowserActivity.TabHolder) model;
			this.time = 0;
		}
		PageId = holder.id;
		this.path = holder.id+".webshot.png";
		//CMN.Log("PageId PageId", PageId, this.time);
	}

	public Bitmap createBitMap() {
		if(alwaysFromFile) {
			//return null;
		}
		return webView instanceof WebViewmy?((WebViewmy)webView).getBitmap():null;
	}
	
//	@Override
//	public boolean equals(Object o) {
//		if (this == o) return true;
//		if (o == null || getClass() != o.getClass()) return false;
//		WebPic webPic = (WebPic) o;
//		//boolean alwaysFromFile = this.alwaysFromFile&&webPic.alwaysFromFile;
//		return PageId == webPic.PageId && (time==webPic.time||alwaysFromFile);
//	}
}