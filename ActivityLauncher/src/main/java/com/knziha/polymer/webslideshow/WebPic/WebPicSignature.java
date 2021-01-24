package com.knziha.polymer.webslideshow.WebPic;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;
import java.util.Objects;

public class WebPicSignature implements Key {
	private final long tabID;

	public WebPicSignature(WebPic pic) {
		this.tabID = pic.tabID;
	}

	@Override
	public void updateDiskCacheKey(MessageDigest messageDigest) {
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WebPicSignature that = (WebPicSignature) o;
		return tabID == that.tabID;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(tabID);
	}
}