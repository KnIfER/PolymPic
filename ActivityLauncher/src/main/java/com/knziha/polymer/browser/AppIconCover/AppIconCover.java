package com.knziha.polymer.browser.AppIconCover;

import android.graphics.drawable.Drawable;

import com.knziha.polymer.Utils.CMN;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Objects;

public class AppIconCover {
	private final int hash;
	private final AppLoadableBean strongRef;
	private final WeakReference<AppLoadableBean> path;
	
	public AppIconCover(AppLoadableBean path) {
		this(path, false);
	}
	
	public AppIconCover(AppLoadableBean path, boolean strong_ref) {
		this.hash = path.hashCode();
		this.strongRef = strong_ref?path:null;
		this.path = new WeakReference<>(path);
	}
	
	public AppLoadableBean getBeanInMemory() {
		return path.get();
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof AppIconCover) {
			AppIconCover that = (AppIconCover) o;
			return hash==that.hash && Objects.equals(path.get(), that.path.get());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	public Drawable load() throws IOException {
		AppLoadableBean loader = path.get();
		CMN.Log("load:: "+loader);
		if(loader==null) CMN.Log("Glide :: 加载异常，WeakReference 已被擦除！");
		return loader==null?null:loader.load();
	}
}