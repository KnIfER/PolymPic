package com.knziha.polymer.browser.AppIconCover;

import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.util.Objects;

public class AppIconCover {
	public final AppLoadableBean path;
	
	public AppIconCover(AppLoadableBean path) {
		this.path = path;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof AppIconCover) {
			AppIconCover that = (AppIconCover) o;
			return Objects.equals(path, that.path);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(path);
	}
	
	public Drawable load() throws IOException {
		return path.load();
	}
}