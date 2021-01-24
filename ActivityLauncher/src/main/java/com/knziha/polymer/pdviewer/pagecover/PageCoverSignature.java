package com.knziha.polymer.pdviewer.pagecover;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Key;

import java.security.MessageDigest;
import java.util.Objects;

public class PageCoverSignature implements Key {
	public final String path;

	public PageCoverSignature(String path) {
		this.path = path;
	}

	@Override
	public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
	}
	
	@Override
	public boolean equals(Object o) {
		//int i=1/0;
		if (this == o) return true;
		if (o instanceof PageCoverSignature) {
			PageCoverSignature that = (PageCoverSignature) o;
			return Objects.equals(path, that.path);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(path);
	}
}