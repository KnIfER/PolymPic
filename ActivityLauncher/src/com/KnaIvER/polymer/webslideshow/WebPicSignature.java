package com.KnaIvER.polymer.webslideshow;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;
import com.knziha.filepicker.model.TimeAffordable;
import com.knziha.filepicker.utils.CMNF;

import java.security.MessageDigest;

public class WebPicSignature implements Key, TimeAffordable {
	private final String file;
	private final long time;

	public WebPicSignature(WebPic pic) {
		this.file = pic.path;
		time = pic.time;
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