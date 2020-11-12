package com.knziha.polymer.pdviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.GestureDetector;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.text.BreakIteratorHelper;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.util.Size;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PDocument {
	public final String path;
	final DisplayMetrics dm;
	public /*final*/ PDocPage[] mPDocPages;
	public static PdfiumCore pdfiumCore;
	public PdfDocument pdfDocument;
	public int maxPageWidth;
	public int maxPageHeight;
	public int gap=15;
	public long height;
	public int _num_entries;
	private int _anchor_page;
	
	public float ThumbsHiResFactor=0.4f;
	public float ThumbsLoResFactor=0.1f;
	
	
	class PDocPage {
		final int pageIdx;
		final Size size;
		public PDocView.Tile tile;
		public PDocView.Tile tileBk;
		public int startY = 0;
		public int startX = 0;
		public int maxX;
		public int maxY;
		long OffsetAlongScrollAxis;
		final AtomicLong pid=new AtomicLong();
		long tid;
		BreakIteratorHelper pageBreakIterator;
		String allText;
		
		SparseArray<PDocView.RegionTile> regionRecords = new SparseArray<>();
		
		PDocPage(int pageIdx, Size size) {
			this.pageIdx = pageIdx;
			this.size = size;
		}
		
		public void open() {
			if(pid.get()==0) {
				synchronized(pid) {
					if(pid.get()==0) {
						pid.set(pdfiumCore.openPage(pdfDocument, pageIdx));
					}
				}
			}
		}
		
		public void prepareText() {
			open();
			if(tid==0) {
				tid = pdfiumCore.openText(pid.get());
				allText = pdfiumCore.nativeGetText(tid);
				if(pageBreakIterator==null) {
					pageBreakIterator = new BreakIteratorHelper();
				}
				pageBreakIterator.setText(allText);
			}
		}
		
		public void sendTileToBackup() {
			if(tile!=null && !tile.HiRes) {
				if(tileBk!=null) tileBk.reset();
				tileBk = tile;
			}
		}
		
		public void recordLoading(int x, int y, PDocView.RegionTile regionTile) {
			regionTile.OffsetAlongScrollAxis=OffsetAlongScrollAxis;
			regionRecords.put(y*1024+x, regionTile);
		}
		
		public void clearLoading(int x, int y) {
			regionRecords.remove(y*1024+x);
		}
		
		public PDocView.RegionTile getRegionTileAt(int x, int y) {
			return regionRecords.get(y*1024+x);
		}
		
		@Override
		public String toString() {
			return "PDocPage{" +
					"pageIdx=" + pageIdx +
					'}';
		}
		
		public int getHorizontalOffset() {
			if(size.getWidth()!=maxPageWidth) {
				return (maxPageWidth-size.getWidth())/2;
			}
			return 0;
		}
		
		/** get A Word At Src Pos  */
		public String getWordAtPos(float posX, float posY) {
			prepareText();
			if(tid!=0) {
				//int charIdx = pdfiumCore.nativeGetCharIndexAtPos(tid, posX, posY, 10.0, 10.0);
				int charIdx = pdfiumCore.nativeGetCharIndexAtCoord(pid.get(), size.getWidth(), size.getHeight(), tid
						, posX, posY, 10.0, 10.0);
				String ret=null;
				
				if(charIdx>=0) {
					int ed=pageBreakIterator.following(charIdx);
					int st=pageBreakIterator.previous();
					try {
						ret = allText.substring(st, ed);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return ret;
			}
			return "";
		}
		
		public boolean selWordAtPos(PDocView view, float posX, float posY, float tolFactor) {
			prepareText();
			if(tid!=0) {
				int charIdx = pdfiumCore.nativeGetCharIndexAtCoord(pid.get(), size.getWidth(), size.getHeight(), tid
						, posX, posY, 10.0*tolFactor, 10.0*tolFactor);
				if(charIdx>=0) {
					int ed=pageBreakIterator.following(charIdx);
					int st=pageBreakIterator.previous();
					view.setSelectionAtPage(pageIdx, st, ed);
					return true;
				}
			}
			return false;
		}
		
		public int getCharIdxAtPos(PDocView view, float posX, float posY) {
			prepareText();
			if(tid!=0) {
				return pdfiumCore.nativeGetCharIndexAtCoord(pid.get(), size.getWidth(), size.getHeight(), tid
						, posX, posY, 100.0, 100.0);
			}
			return -1;
		}
		
		public long getLinkAtPos(float posX, float posY) {
			return pdfiumCore.nativeGetLinkAtCoord(pid.get(), size.getWidth(), size.getHeight(), posX, posY);
		}
		
		public String getLinkTarget(long lnkPtr) {
			return pdfiumCore.nativeGetLinkTarget(pdfDocument.mNativeDocPtr, lnkPtr);
		}
		
		public void getSelRects(ArrayList<RectF> rectPagePool, int selSt, int selEd) {
			//CMN.Log("getTextRects", selSt, selEd);
			rectPagePool.clear();
			prepareText();
			if(tid!=0) {
				if(selEd==-1) {
					selEd=allText.length();
				}
				CMN.Log("getTextRects", selSt, selEd, allText.length());
				if(selEd<selSt) {
					int tmp = selSt;
					selSt=selEd;
					selEd=tmp;
				}
				selEd -= selSt;
				if(selEd>0) {
					int rectCount = pdfiumCore.getTextRects(pid.get(), OffsetAlongScrollAxis, getHorizontalOffset(), size, rectPagePool, tid, selSt, selEd);
					//CMN.Log("getTextRects", selSt, selEd, rectCount, rectPagePool.toString());
					if(rectCount>=0 && rectPagePool.size()>rectCount) {
						rectPagePool.subList(rectCount, rectPagePool.size()).clear();
					}
				}
			}
		}
		
		public void getCharPos(RectF pos, int index) {
			pdfiumCore.nativeGetCharPos(pid.get(), (int)OffsetAlongScrollAxis, getHorizontalOffset()
					, size.getWidth(), size.getHeight(), pos, tid, index, true);
		}
		
//		public void getCharPosLoose(RectF pos, int index) {
//			pdfiumCore.nativeGetCharPos(pid.get(), (int)OffsetAlongScrollAxis, getHorizontalOffset(), size.getWidth(), size.getHeight(), pos, tid, index, true);
//		}
	}
	
	public PDocument(Context c, String path, DisplayMetrics dm, AtomicBoolean abort) throws IOException {
		this.path = path;
		this.dm = dm;
		if(pdfiumCore==null) {
			pdfiumCore = new PdfiumCore(c);
		}
		File f = new File(path);
		ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
		pdfDocument = pdfiumCore.newDocument(pfd);
		_num_entries = pdfiumCore.getPageCount(pdfDocument);
		mPDocPages = new PDocPage[_num_entries];
		height=0;
		for (int i = 0; i < _num_entries; i++) {
			Size size = pdfiumCore.getPageSize(pdfDocument, i);
			PDocPage page = new PDocPage(i, size);
			mPDocPages[i]=page;
			page.OffsetAlongScrollAxis=height;
			height+=size.getHeight()+gap;
			//if(i<10) CMN.Log("mPDocPages", i, size.getWidth(), size.getHeight());
			maxPageWidth = Math.max(maxPageWidth, size.getWidth());
			maxPageHeight = Math.max(maxPageHeight, size.getHeight());
			if(abort!=null&&abort.get()) {
				return;
			}
		}
		if(_num_entries>0) {
			PDocPage page = mPDocPages[_num_entries-1];
			height = page.OffsetAlongScrollAxis+page.size.getHeight();
		}
		float w=dm.widthPixels, h=dm.heightPixels;
		float v1=maxPageWidth*maxPageHeight, v2=w*h;
		if(v1<=v2) {
			ThumbsHiResFactor=1f;
			ThumbsLoResFactor=0.35f;
		} else {
			if(w>100) {
				w-=100;
			}
			if(h>100) {
				h-=100;
			}
			ThumbsHiResFactor=Math.min(Math.min(w/maxPageWidth, h/maxPageHeight), v2/v1);
			if(ThumbsHiResFactor<=0) {
				ThumbsHiResFactor=0.004f;
			}
			ThumbsLoResFactor=ThumbsHiResFactor/4;
		}
		CMN.Log("缩放比", ThumbsHiResFactor, ThumbsLoResFactor);
	}
	
	public Bitmap renderBitmap(PDocPage page, Bitmap bitmap, float scale) {
		Size size = page.size;
		int w = (int) (size.getWidth()*scale);
		int h = (int) (size.getHeight()*scale);
		//CMN.Log("renderBitmap", w, h, bitmap.getWidth(), bitmap.getHeight());
		bitmap.eraseColor(Color.WHITE);
		page.open();
		pdfiumCore.renderPageBitmap(pdfDocument, bitmap, page.pageIdx, 0, 0, w, h , true);
		return bitmap;
	}
	
	public Bitmap renderRegionBitmap(PDocPage page, Bitmap bitmap, int startX, int startY, int srcW, int srcH) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		bitmap.eraseColor(Color.WHITE);
		page.open();
		//CMN.Log("renderRegion", w, h, startX, startY, srcW, srcH);
		pdfiumCore.renderPageBitmap(pdfDocument, bitmap, page.pageIdx, startX, startY, srcW, srcH ,false);
		return bitmap;
	}
	
	public Bitmap drawTumbnail(Bitmap bitmap, int pageIdx, float scale) {
		PDocPage page = mPDocPages[pageIdx];
		Size size = page.size;
		int w = (int) (size.getWidth()*scale);
		int h = (int) (size.getHeight()*scale);
		page.open();
		CMN.Log("PageOpened::", CMN.now()-CMN.ststrt, w, h);
		Bitmap OneSmallStep = bitmap;
		boolean recreate=OneSmallStep.isRecycled();
		if(!recreate && (w!=OneSmallStep.getWidth()||h!=OneSmallStep.getHeight())) {
			if(OneSmallStep.getAllocationByteCount()>=w*h*4) {
				OneSmallStep.reconfigure(w, h, Bitmap.Config.ARGB_8888);
			} else recreate=true;
		}
		if(recreate) {
			OneSmallStep.recycle();
			OneSmallStep = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		}
		renderBitmap(page, OneSmallStep, scale);
		//pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageIdx, 0, 0, w, h ,false);
		return bitmap;
	}
	
}
