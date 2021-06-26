package com.knziha.polymer.webstorage;

import androidx.annotation.NonNull;

import com.knziha.polymer.widgets.Utils;

public class SubStringKey {
	public final static SubStringKey EmptyDomain = new SubStringKey("",0,0);
	final int st;
	final int ed;
	final int hash;
	final String text;
	final int len;
	
	public static SubStringKey fast_hostKey(String text) {
		return new SubStringKey(text, 0, text.length());
	}
	
	public  static SubStringKey new_hostKey(String text) {
		int st=0, ed=text.length();
		int idx=text.indexOf("://");
		if(idx>0) {
			st=idx+3;
		}
		idx=text.indexOf("/", st);
		if(idx>0) {
			ed=idx;
		}
		return new SubStringKey(text, st, ed);
	}
	
	public boolean matches(String url) {
		int idx=url.indexOf("://")+3;
		if (idx>3) {
			int dLen = url.length()-(idx+len+1);
			if (dLen>=0) {
				return (dLen==0||url.charAt(idx+len)=='/') && url.regionMatches(idx, text, st, len);
			}
		}
		return false;
	}
	
	SubStringKey(String text, int st, int ed) {
		this.st = st;
		this.ed = ed;
		this.text = text;
		this.len = ed-st;
		this.hash = Utils.hashCode(text, st, ed);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SubStringKey)) return false;
		SubStringKey that = (SubStringKey) o;
		return this==that || len==that.len && text.regionMatches(st, that.text, that.st, len);
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@NonNull
	@Override
	public String toString() {
		if (st==0 && ed==text.length()) {
			return text;
		}
		return text.substring(st, ed);
	}
}