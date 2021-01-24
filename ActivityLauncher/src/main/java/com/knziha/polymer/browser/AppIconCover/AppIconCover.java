package com.knziha.polymer.browser.AppIconCover;

import java.util.Objects;

public class AppIconCover {
	public final AppInfoBean path;
	
	public AppIconCover(AppInfoBean path) {
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
}