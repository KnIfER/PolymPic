package com.knziha.polymer.webslideshow.WebPic;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.ResourceKeyOutdating;
import com.bumptech.glide.load.model.Model;
import com.knziha.polymer.AdvancedBrowserWebView;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** A model to save and load webview's thumbnail. */
public class WebPic implements ResourceKeyOutdating, Model {
	public final static ConcurrentHashMap<Long, Integer> versionMap = new ConcurrentHashMap<>();
	public final long tabID;
	public int version;
	public final Map<Long, AdvancedBrowserWebView> id_table;
	
	public WebPic(long tabID, int version, Map<Long, AdvancedBrowserWebView> id_table) {
		this.tabID = tabID;
		this.version = version;
		this.id_table = id_table;
		Integer ver = versionMap.get(tabID);
		if(version>0&&(ver==null||version>ver)) {
			versionMap.put(tabID, version);
		}
		//CMN.Log(tabID, "WebPic__", ver, version);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		WebPic webPic = (WebPic) o;
		//CMN.Log("equals__", this, o);
		return tabID == webPic.tabID;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(tabID);
	}
	
	@Override
	public boolean isOutDated() {
		Integer ver = versionMap.get(tabID);
		//CMN.Log(tabID, "isOutDated__", ver!=null&&ver>version, version, ver);
		return ver!=null&&ver>version;
	}
	
	@Override
	public boolean isEquivalentTo(@Nullable Object other) {
		return equals(other)&&!((WebPic)other).isOutDated();
	}
	
	@Override
	public String toString() {
		return "WebPic{" +
				"tabID=" + tabID +
				", version=" + version +
				'}';
	}
}