package com.knziha.polymer.browser.AppIconCover;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;

public class AppIconCoverSignature implements Key {
	public final AppIconCover cover;

	public AppIconCoverSignature(AppIconCover cover) {
		this.cover = cover;
	}

	@Override
	public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
	}
	
	@Override
	public boolean equals(Object o) {
		//int i=1/0;
		if (this == o) return true;
		if (o instanceof AppIconCoverSignature) {
			AppIconCoverSignature that = (AppIconCoverSignature) o;
			return cover.equals(that.cover);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return cover.hashCode();
	}
}