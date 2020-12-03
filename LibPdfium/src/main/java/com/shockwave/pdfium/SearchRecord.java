package com.shockwave.pdfium;

public class SearchRecord {
	public final int pageIdx;
	public final int findStart;
	public SearchRecord(int pageIdx, int findStart) {
		this.pageIdx = pageIdx;
		this.findStart = findStart;
	}
}
