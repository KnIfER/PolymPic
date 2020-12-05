package com.knziha.polymer.pdviewer.searchdata;

import android.graphics.RectF;

/** stores an searched highlight result of a page */
public class SearchRecordItem{
	public final int st;
	public final int ed;
	public final RectF[] rects;
	public SearchRecordItem(int st, int ed, RectF[] rects) {
		this.st = st;
		this.ed = ed;
		this.rects = rects;
	}
}