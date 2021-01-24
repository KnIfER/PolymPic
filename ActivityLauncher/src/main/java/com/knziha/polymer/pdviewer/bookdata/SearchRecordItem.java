package com.knziha.polymer.pdviewer.bookdata;

import android.graphics.RectF;

/** Stores the highlight rects and start-end index
 *  	of one matching item on a page */
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