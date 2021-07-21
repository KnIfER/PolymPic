package com.knziha.polymer.webfeature;

import android.text.TextUtils;

import java.util.ArrayList;

public class WebDict {
	WebDict parent;
	ArrayList<WebDict> children;
	public String url;
	public String name;
	public String activeChildName;
	public boolean isEditing;
	public WebDict(String url, String name) {
		this.url = url;
		this.name = name;
		if (TextUtils.isEmpty(this.name)) {
			this.name = "未命名";
		}
	}
	
	public ArrayList<WebDict> getChildren() {
		if (children==null) {
			children = new ArrayList<>();
		}
		return children;
	}
	
	public boolean hasMoreVariants() {
		return children!=null && children.size()>0;
	}
}
