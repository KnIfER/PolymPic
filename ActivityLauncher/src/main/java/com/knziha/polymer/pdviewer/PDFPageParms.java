package com.knziha.polymer.pdviewer;

import androidx.annotation.NonNull;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.JSONObjectWrap;

public class PDFPageParms {
	public int pageIdx;
	int offsetX;
	int offsetY;
	public float scale;
	public PDFPageParms(String val) {
		if(val!=null) {
			try {
				//CMN.Log("parsing...", val);
				JSONObjectWrap obj = new JSONObjectWrap(val);
				set(obj.getInt("p"), obj.getInt("x"), obj.getInt("y"), obj.getFloat("s"));
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
	}
	public PDFPageParms(int p, int x, int y, float s) {
		set(p, x, y, s);
	}
	public void set(int p, int x, int y, float s) {
		pageIdx=p;
		offsetX=x;
		offsetY=y;
		scale=s;
	}
	
	public boolean setIfNotEqual(int p, int x, int y, float s) {
		if(this.pageIdx!=p||this.offsetX!=x||this.offsetY!=y||this.scale!=s) {
			set(p, x, y, s);
			return true;
		}
		return false;
	}
	
	@NonNull
	@Override
	public String toString() {
		return String.format("{p:%d,x:%d,y:%d,s:%.3f}", pageIdx, offsetX, offsetY, scale);
	}
}