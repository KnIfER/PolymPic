package com.knziha.polymer.pdviewer.pagecover;

import android.content.ContentResolver;
import android.util.DisplayMetrics;

import java.util.Objects;

public class PageCover {
	final ContentResolver contentResolver;
	public final String path;
	final int rowID;
	final DisplayMetrics dm;
	
	public PageCover(ContentResolver contentResolver, String path, int rowID, DisplayMetrics dm) {
		this.contentResolver = contentResolver;
		this.path = path;
		this.rowID = rowID;
		this.dm = dm;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof PageCover) {
			PageCover that = (PageCover) o;
			return Objects.equals(path, that.path);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(path);
	}
}