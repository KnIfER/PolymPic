package com.knziha.polymer.pdviewer;

public class PDFPageParms {
	int pageIdx;
	int offsetX;
	int offsetY;
	float scale;
	public PDFPageParms(int p, int x, int y, float s) {
		pageIdx=p;
		offsetX=x;
		offsetY=y;
		scale=s;
	}
}