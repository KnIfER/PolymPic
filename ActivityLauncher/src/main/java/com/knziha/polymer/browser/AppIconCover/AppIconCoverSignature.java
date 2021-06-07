package com.knziha.polymer.browser.AppIconCover;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;
import java.util.Objects;

public class AppIconCoverSignature implements Key {
	public final AppLoadableBean path;

	public AppIconCoverSignature(AppLoadableBean path) {
		this.path = path;
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
			return Objects.equals(path, that.path);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(path);
	}
}