package com.knziha.polymer.pdviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.util.SparseArray;

import com.knziha.polymer.Utils.CMN;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PDocument {
	public final String path;
	public final ArrayList<PDocPage> mPDocPages;
	public static PdfiumCore pdfiumCore;
	public PdfDocument pdfDocument;
	public long width;
	public long height;
	private int _num_entries;
	private int _anchor_page;
	private HashMap<Integer, Bitmap> bTMP = new HashMap<>();
	
	class PDocPage {
		final int pageIdx;
		final Size size;
		long OffsetAlongScrollAxis;
		long pid;
		PDocPage(int pageIdx, Size size) {
			this.pageIdx = pageIdx;
			this.size = size;
		}
		public void open() {
			if(pid==0) {
				pid = pdfiumCore.openPage(pdfDocument, pageIdx);
			}
		}
	}
	public PDocument(Context c, String path) throws IOException {
		this.path = path;
		if(pdfiumCore==null) {
			pdfiumCore = new PdfiumCore(c);
		}
		File f = new File(path);
		ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
		pdfDocument = pdfiumCore.newDocument(pfd);
		_num_entries = pdfiumCore.getPageCount(pdfDocument);
		mPDocPages = new ArrayList<>(_num_entries+8);
		height=0;
		for (int i = 0; i < _num_entries; i++) {
			Size size = pdfiumCore.getPageSize(pdfDocument, i);
			PDocPage page = new PDocPage(i, size);
			mPDocPages.add(page);
			page.OffsetAlongScrollAxis=height;
			height+=size.getHeight();
			CMN.Log("mPDocPages", i, size.getWidth(), size.getHeight());
			width = Math.max(width, size.getWidth());
		}
		if(_num_entries>0) {
			PDocPage page = mPDocPages.get(_num_entries-1);
			height = page.OffsetAlongScrollAxis+page.size.getHeight();
		}
	}
	
	public Bitmap renderBitmap(int pageIdx, float scale) {
		if(bTMP.get(pageIdx)!=null) return bTMP.get(pageIdx);
		PDocPage page = mPDocPages.get(pageIdx);
		Size size = page.size;
		int w = (int) (size.getWidth()*scale);
		int h = (int) (size.getHeight()*scale);
		CMN.Log("renderBitmap", w, h);
		Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		page.open();
		pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageIdx, 0, 0, w, h ,false);
		//pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageIdx, 0, 256 , 256, 256 ,false);
		bTMP.put(pageIdx, bitmap);
		return bitmap;
	}
	
}
