package com.KnaIvER.polymer.webslideshow;

import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;
import com.knziha.filepicker.model.TimeAffordable;
import com.knziha.filepicker.utils.CMNF;

import java.security.MessageDigest;

public class WebPicSignature implements Key, TimeAffordable {
	private final String file;
	private long time;

	public WebPicSignature(WebPic pic) {
		this.file = pic.path;
		if(pic.webView instanceof WebView /*&& ((View)pic.webView).getParent()!=null*/) {
			time = pic.time;
		} else {
			time = 0;
		}
	}

	@NonNull
	@Override
	public String toString() {
		return file+" : WebPicSignature :"+time;
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest) {
		byte[] bs = file.getBytes();
		messageDigest.update(bs, 0, bs.length);
	}

	@Override
	public long AffordTime() {
		return time;
	}
	
	@Override
	public String AffordPath() {
		return file;
	}
}