package com.tencent.smtt.sdk;

import android.net.Uri;
import android.os.Build.VERSION;
import android.webkit.WebResourceRequest;

import com.tencent.smtt.utils.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

public class WebResourceRequestEx implements com.tencent.smtt.export.external.interfaces.WebResourceRequest {
	private String url;
	private boolean forMainFrame;
	private boolean redirect;
	private boolean hasGesture;
	private String method;
	private Map<String, String> header;
	
	public WebResourceRequestEx(String var1, boolean var2, boolean var3, boolean var4, String var5, Map<String, String> var6) {
		this.url = var1;
		this.forMainFrame = var2;
		this.redirect = var3;
		this.hasGesture = var4;
		this.method = var5;
		this.header = var6;
	}
	
	public WebResourceRequestEx(WebResourceRequest webResourceRequest) {
		if (VERSION.SDK_INT >= 24) {
			Object object = ReflectionUtils.a((Object)webResourceRequest, "isRedirect");
			if (object instanceof Boolean) {
				this.redirect = (Boolean)object;
			}
			object = ReflectionUtils.a((Object)webResourceRequest, "getUrl");
			if (object instanceof Uri) {
				this.url = object.toString();
			}
			object = ReflectionUtils.a((Object)webResourceRequest, "isForMainFrame");
			if (object instanceof Boolean) {
				this.forMainFrame = (Boolean)object;
			}
			object = ReflectionUtils.a((Object)webResourceRequest, "hasGesture");
			if (object instanceof Boolean) {
				this.hasGesture = (Boolean)object;
			}
			object = ReflectionUtils.a((Object)webResourceRequest, "getMethod");
			if (object instanceof String) {
				this.method = object.toString();
			}
			object = ReflectionUtils.a((Object)webResourceRequest, "getRequestHeaders");
			if (object instanceof HashMap) {
				this.header = (Map<String, String>) object;
			}
		}
		
	}
	
	
	public Uri getUrl() {
		return Uri.parse(this.url);
	}
	
	public boolean isForMainFrame() {
		return this.forMainFrame;
	}
	
	public boolean isRedirect() {
		return this.redirect;
	}
	
	public boolean hasGesture() {
		return this.hasGesture;
	}
	
	public String getMethod() {
		return this.method;
	}
	
	public Map<String, String> getRequestHeaders() {
		return this.header;
	}
}