package com.knziha.polymer.browser.AppIconCover;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.Objects;

public class AppInfoBean {
	public Intent intent;
	public ResolveInfo data;
	public PackageManager pm;
	//public Drawable icon;
	public String appName;
	public String pkgName;
	public String appLauncherClassName;

	public Drawable load() {
		if(appName==null) {
			appName = data.loadLabel(pm).toString();
		}
		return data.loadIcon(pm);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AppInfoBean that = (AppInfoBean) o;
		boolean ret = Objects.equals(pkgName, that.pkgName) &&
				Objects.equals(appLauncherClassName, that.appLauncherClassName);
		if(ret && appName==null ^ that.appName==null) {
			if(appName==null) {
				appName = that.appName;
			} else {
				that.appName = appName;
			}
		}
		return ret;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(pkgName, appLauncherClassName);
	}
}