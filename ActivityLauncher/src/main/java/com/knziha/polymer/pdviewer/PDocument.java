package com.knziha.polymer.pdviewer;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.SparseArray;

import com.knziha.filepicker.utils.FU;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.pdviewer.bookdata.PDocBookInfo;
import com.knziha.polymer.pdviewer.bookdata.SearchRecordItem;
import com.knziha.polymer.text.BreakIteratorHelper;
import com.knziha.polymer.widgets.Utils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.SearchRecord;
import com.shockwave.pdfium.bookmarks.BookMarkEntry;
import com.shockwave.pdfium.bookmarks.BookMarkNode;
import com.shockwave.pdfium.util.Size;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.knziha.polymer.pdviewer.PDocView.books;
import static com.shockwave.pdfium.PdfiumCore.LargeFileTheta;

public class PDocument {
	public final static AtomicBoolean initFlag = new AtomicBoolean(true);
	public final Uri path;
	public final AtomicInteger referenceCount;
	final DisplayMetrics dm;
	public /*final*/ PDocPage[] mPDocPages;
	public HashSet<Integer> mPageOpenedRecord = new HashSet<>();
	public static PdfiumCore pdfiumCore;
	public PdfDocument pdfDocument;
	public int maxPageWidth;
	public int maxPageHeight;
	public int gap=15;
	private long LengthAlongScrollAxis;
	public int _num_entries;
	public boolean isDirty;
	private boolean isHorizontalView=false;
	public int aid;
	private int _anchor_page;
	
	public float ThumbsHiResFactor=0.4f;
	public float ThumbsLoResFactor=0.1f;
	
	public final static int SavingScheme_SaveOnClose=0;
	public final static int SavingScheme_AlwaysSaveOnPause=1;
	public final static int SavingScheme_NotSaving=2;
	public static int SavingScheme=SavingScheme_AlwaysSaveOnPause;
	public BookMarkNode bmRoot;
	public int bmCount;
	public boolean isClosed;
	PDocBookInfo bookInfo;
	
	/** 策略：小于500MB统一加载到内存，允许保存。大于500MB在关闭文件时询问是否保存。 */
	public void saveDocAsCopy(Context a, Uri url, boolean incremental, boolean reload) {
		if(isDirty) {
			if(url==null) {
				url=path;
			}
			boolean abort = reload && !pdfiumCore.nativeHasReadBuf(pdfDocument.mNativeDocPtr);
			if(abort) {
				return;
			}
			Uri urlWriter = url;
			//File path = new File(url);
			//incremental = true; //debug inc
			//urlWriter = Uri.fromFile(new File(new File(urlWriter.getPath()).getParent(), "tmp2.pdf")); //debug save
			//try (ParcelFileDescriptor fd = ParcelFileDescriptor.open(path, ParcelFileDescriptor.MODE_WRITE_ONLY|ParcelFileDescriptor.MODE_CREATE)) {
			ContentResolver contentResolver = a.getContentResolver();
			Uri tmp = FU.buildContentUrl(a, url.getPath());
			if(tmp!=null) {
				urlWriter = tmp;
				CMN.Log("urlWriter", urlWriter);
				//urlWriter = Uri.parse("content://com.android.externalstorage.documents/tree/2486-F9E1%3A/document/2486-F9E1%3ADownload%2F文件夹%2F文件夹%2Fcheat.pdf");
			}
			try (ParcelFileDescriptor fd = contentResolver.openFileDescriptor(urlWriter, "rw")) {
				CMN.rt();
				//pdfDocument.closeFile();
				pdfiumCore.SaveAsCopy(pdfDocument.mNativeDocPtr, fd.getFd(), incremental);
				if(abort) {
					close();
					//pdfDocument = pdfiumCore.newDocument(ParcelFileDescriptor.open(path, ParcelFileDescriptor.MODE_READ_ONLY));
					pdfDocument = pdfiumCore.newDocument(contentResolver.openFileDescriptor(url, "r"));
					isClosed = false;
				}
				isDirty=false;
				CMN.pt("PDF 保存耗时：", url);
			} catch (Exception e) {
				if(reload && String.valueOf(e.getMessage()).contains("Permission")) {
					Utils.blameAndroidIfNeeded(a);
				}
				CMN.Log(e);
			}
		}
	}
	
	public void close() {
		isClosed = true;
		for (int i:mPageOpenedRecord) {
			mPDocPages[i].close();
		}
		mPageOpenedRecord.clear();
		pdfiumCore.closeDocument(pdfDocument);
	}
	
	public void tryClose(int taskId) {
		if(referenceCount.decrementAndGet()==0) {
			close();
			books.remove(Utils.getRunTimePath(path));
		}
	}
	
	public void setHorizontalView(boolean horizontalView) {
		isHorizontalView = horizontalView;
	}
	
	public boolean isHorizontalView() {
		return isHorizontalView;
	}
	
	public long getHeight() {
		return isHorizontalView?maxPageHeight:LengthAlongScrollAxis;
	}
	
	public long getWidth() {
		return isHorizontalView?LengthAlongScrollAxis:maxPageWidth;
	}
	
	public void prepareBookMarks() {
		if(bmRoot==null) {
			try {
				bmRoot = new BookMarkNode(new BookMarkEntry("root", 0));
				bmCount = pdfiumCore.BuildBookMarkTree(pdfDocument.mNativeDocPtr, bmRoot);
				//CMN.Log(bmRoot);
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
	}
	
	public void findAll(String key, int flag, ArrayList<SearchRecord> arr) {
		pdfiumCore.nativeFindAll(pdfDocument.mNativeDocPtr, _num_entries, key, flag, arr);
	}
	
	public SearchRecord findPage(String key, int pageIdx, int flag) {
		synchronized (PdfiumCore.searchLock)
		{
			return pdfiumCore.nativeFindPage(pdfDocument.mNativeDocPtr, key, pageIdx, flag);
		}
	}
	
	public SearchRecord findPageCached(String key, int pageIdx, int flag) {
		PDocPage page = mPDocPages[pageIdx];
		synchronized (page.pid)
		{
			boolean shouldClose = page.loadText();
			int foundIdx = pdfiumCore.nativeFindTextPage(page.tid, key, flag);
			SearchRecord ret = foundIdx==-1?null:new SearchRecord(pageIdx, foundIdx);
			if(shouldClose) {
				page.close();
			}
			return ret;
		}
	}
	
	public void setBookInfo(PDocBookInfo bookInfo) {
		if(bookInfo!=null) {
			bookInfo.url = path;
			this.bookInfo = bookInfo;
		}
	}
	
	public boolean isBookInfoDirty() {
		return bookInfo!=null && bookInfo.isDirty;
	}
	
	public class PDocPage {
		final int pageIdx;
		public final Size size;
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
		
		public boolean open() {
			if(pid.get()==0) {
				synchronized(pid) {
					if(pid.get()==0) {
						//CMN.rt();
						pid.set(pdfiumCore.openPage(pdfDocument, pageIdx));
						mPageOpenedRecord.add(pageIdx);
						//CMN.pt("页面打开时间：");
						return true;
					}
				}
			}
			return false;
		}
		
		public void close() {
			if(pid.get()!=0) {
				synchronized(pid) {
					pdfiumCore.closePageAndText(pid.get(), tid);
					//mPageOpenedRecord.remove(pageIdx);
					pid.set(0);
					tid = 0;
				}
			}
		}
		
		public boolean loadText() {
			synchronized (pid) {
				boolean shouldClose = open();
				if (tid == 0) {
					tid = pdfiumCore.openText(pid.get());
				}
				return shouldClose;
			}
		}
		
		public void prepareText() {
			loadText();
			if(allText==null) {
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
		
		public int getLateralOffset() {
			//if(size.getWidth()!=maxPageWidth) {
			//	return (maxPageWidth-size.getWidth())/2;
			//}
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
					CMN.Log("st, ed", st, ed);
					if(st+1>=ed) {
						//return false;
					}
					view.setSelectionAtPage(pageIdx, st, ed);
					//CMN.Log("selWordAtPos", charIdx, allText.substring(st, ed+1)+"...");
					return true;
				}
			}
			return false;
		}
		
		/** Get the char index at a page position
		 * @param posX position X in the page coordinate<br/>
		 * @param posY position Y in the page coordinate<br/>
		 * */
		public int getCharIdxAtPos(float posX, float posY) {
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
					int rectCount = pdfiumCore.getTextRects(pid.get()
							, isHorizontalView?getLateralOffset():(int)OffsetAlongScrollAxis
							, isHorizontalView?(int)OffsetAlongScrollAxis:getLateralOffset()
							, size, rectPagePool, tid, selSt, selEd);
					//CMN.Log("getTextRects", selSt, selEd, rectCount, rectPagePool.toString());
					if(rectCount>=0 && rectPagePool.size()>rectCount) {
						rectPagePool.subList(rectCount, rectPagePool.size()).clear();
					}
				}
			}
		}
		
		public void getCharPos(RectF pos, int index) {
			pdfiumCore.nativeGetCharPos(pid.get()
					, isHorizontalView?getLateralOffset():(int)OffsetAlongScrollAxis
					, isHorizontalView?(int)OffsetAlongScrollAxis:getLateralOffset()
					, size.getWidth(), size.getHeight(), pos, tid, index, true);
		}
		
		public void getCharLoosePos(RectF pos, int index) {
			pdfiumCore.nativeGetMixedLooseCharPos(pid.get()
					, isHorizontalView?getLateralOffset():(int)OffsetAlongScrollAxis
					, isHorizontalView?(int)OffsetAlongScrollAxis:getLateralOffset()
					, size.getWidth(), size.getHeight(), pos, tid, index, true);
		}
		
		ArrayList<AnnotShape> mAnnotRects;
		
		public AnnotShape selAnnotAtPos(PDocView view, float posX, float posY) {
			int annotCount;
			AnnotShape ret = null;
			ArrayList<AnnotShape> selBucket = view.mAnnotBucket;
			selBucket.clear();
			if(mAnnotRects==null) {
				annotCount = pdfiumCore.nativeCountAnnot(pid.get());
				mAnnotRects = new ArrayList<>((int)(annotCount*1.2));
				for (int i = 0; i < annotCount; i++) {
					mAnnotRects.add(new AnnotShape(pdfiumCore.nativeGetAnnotRect(pid.get(), i, size.getWidth(), size.getHeight()), i));
				}
			} else {
				annotCount = mAnnotRects.size();
			}
			for (int i = 0; i < annotCount; i++) {
				AnnotShape aI = mAnnotRects.get(i);
				RectF rect = aI.box;
				if(posX>=rect.left&&posX<rect.right&&posY>=rect.top&&posY<rect.bottom) {
					selBucket.add(aI);
				}
			}
			annotCount = selBucket.size();
			
			//CMN.Log("selBucket 1", selBucket.size());
			
			if(annotCount>1) {
				PointF p = new PointF(posX, posY);
				for (int i = annotCount-1; i >= 0; i--) {
					AnnotShape aI = selBucket.get(i);
					fetchAnnotAttachPoints(aI);
					boolean remove=true;
					for (int j = 0; j < aI.attachPts.length; j++) {
						QuadShape qI = aI.attachPts[j];
						if(IsPointInMatrix(p, qI.p1, qI.p2, qI.p4, qI.p3)) {
							remove=false;
							break;
						}
					}
					if(remove) {
						selBucket.remove(i);
					}
				}
			}
			
			//CMN.Log("selBucket", selBucket.size());
			
			annotCount = selBucket.size();
			if(annotCount>0) {
				if(annotCount>1) {
					float sizeMin = Integer.MAX_VALUE;
					for (int i = 0; i < annotCount; i++) {
						AnnotShape aI = selBucket.get(i);
						float sz = aI.box.height() * aI.box.width();
						if(sz<=sizeMin) {
							sizeMin=sz;
							ret = aI;
						}
					}
				} else {
					ret = selBucket.get(annotCount-1);
				}
			}
			
			if(ret!=null) {
				view.setAnnotSelection(this, ret);
			}
			
			return ret;
		}
		
		void fetchAnnotAttachPoints(AnnotShape aI) {
			if(aI.attachPts==null) {
				long annotPtr = pdfiumCore.nativeOpenAnnot(pid.get(), aI.index);
				int attachPtSz = pdfiumCore.nativeCountAttachmentPoints(annotPtr);
				aI.attachPts = new QuadShape[attachPtSz];
				int width=size.getWidth(), height=size.getHeight();
				for (int j = 0; j < attachPtSz; j++) {
					QuadShape qI = aI.attachPts[j] = new QuadShape();
					pdfiumCore.nativeGetAttachmentPoints(pid.get(), annotPtr, j, width, height, qI.p1, qI.p2, qI.p3, qI.p4);
				}
				pdfiumCore.nativeCloseAnnot(annotPtr);
			}
		}
		
		/** Create a highlight annotation on this page. */
		public void createHighlight(PDocView pDocView, int colorInt, int selSt, int selEd, RectF box, ArrayList<RectF> selLineRects) {
			open();
			long antTmp = pdfiumCore.nativeCreateAnnot(pid.get(), 9);
			if(antTmp!=0) {
				// 5CMN.Log("nativeCreateAnnot", antTmp);
				int offset1, offset2;
				if(isHorizontalView) {
					offset1 = getLateralOffset();
					offset2 = (int) OffsetAlongScrollAxis;
				} else {
					offset1 = (int) OffsetAlongScrollAxis;
					offset2 = getLateralOffset();
				}
				double width = size.getWidth(), height = size.getHeight();
				pdfiumCore.nativeSetAnnotColor(antTmp, colorInt>>16&0xff, colorInt>>8&0xff, colorInt&0xff, colorInt>>24&0xff);
				pdfiumCore.nativeSetAnnotRect(pid.get(), antTmp, box.left-offset2, box.top-offset1, box.right-offset2, box.bottom-offset1, width, height);
				//pdfiumCore.nativeAppendAnnotPoints(pid.get(), antTmp, box.left-offset2, box.top-offset1, box.right-offset2, box.bottom-offset1, width, height);
				//pdfiumCore.nativeAppendAnnotPoints(pid.get(), antTmp, 0, size.getHeight()-100, 100, size.getHeight(), width, height);
				for (RectF rI:selLineRects) {
					pdfiumCore.nativeAppendAnnotPoints(pid.get(), antTmp, rI.left-offset2, rI.top-offset1, rI.right-offset2, rI.bottom-offset1, width, height);
				}
				pdfiumCore.nativeCloseAnnot(antTmp);
				
				if(true) {
					int selStart = Math.min(selSt, selEd);
					int selEnd = selStart+Math.abs(selEd-selSt);
					String text = null;
					if(true) {
						prepareText();
						text = allText.substring(selStart, selEnd);
					}
					bookInfo.appendAnnotRecord(pDocView.getCurrentPageParmsOnScreen(), colorInt, selStart, selEnd, text, getWritable()&&!pDocView.hasNoPermission);
				}
				
				// todo add the new newly created annot directly to the list
				mAnnotRects=null;
			}
		}
		
		public void getAllMatchOnPage(SearchRecord record, PDocPageResultsProvider searchCtx) {
			prepareText();
			if(record.data==null)
			{
				//CMN.rt();
				ArrayList<SearchRecordItem> data = new ArrayList<>();
				record.data = data;
				long keyStr = searchCtx.getKeyStr();
				if(keyStr!=0) {
					long searchHandle = pdfiumCore.nativeFindTextPageStart(tid, keyStr, searchCtx.flag, record.findStart);
					if(searchHandle!=0) {
						while(pdfiumCore.nativeFindTextPageNext(searchHandle)) {
							int st = pdfiumCore.nativeGetFindIdx(searchHandle);
							int ed = pdfiumCore.nativeGetFindLength(searchHandle);
							getRectsForRecordItem(data, st, ed);
						}
						pdfiumCore.nativeFindTextPageEnd(searchHandle);
					}
				}
				//CMN.pt("getAllSearchedHighlightRects：");
			}
		}
		
		private void getRectsForRecordItem(ArrayList<SearchRecordItem> data, int st, int ed) {
			if(st>=0&&ed>0) {
				int  rectCount = pdfiumCore.nativeCountRects(tid, st, ed);
				if(rectCount>0) {
					RectF[] rects = new RectF[rectCount];
					for (int i = 0; i < rectCount; i++) {
						RectF rI = new RectF();
						pdfiumCore.nativeGetRect(pid.get()
								, isHorizontalView?getLateralOffset():(int)OffsetAlongScrollAxis
								, isHorizontalView?(int)OffsetAlongScrollAxis:getLateralOffset()
								, size.getWidth(), size.getHeight()
								,tid, rI, i);
						rects[i]=rI;
					}
					rects = mergeLineRects(Arrays.asList(rects), null).toArray(new RectF[0]);
					data.add(new SearchRecordItem(st, ed, rects));
				}
			}
		}
		
		public ArrayList<RectF> mergeLineRects(List<RectF> selRects, RectF box) {
			RectF tmp = new RectF();
			ArrayList<RectF> selLineRects = new ArrayList<>(selRects.size());
			RectF currentLineRect=null;
			for(RectF rI:selRects) {
				//CMN.Log("RectF rI:selRects", rI);
				if(currentLineRect!=null&&Math.abs((currentLineRect.top+currentLineRect.bottom)-(rI.top+rI.bottom))<currentLineRect.bottom-currentLineRect.top) {
					currentLineRect.left = Math.min(currentLineRect.left, rI.left);
					currentLineRect.right = Math.max(currentLineRect.right, rI.right);
					currentLineRect.top = Math.min(currentLineRect.top, rI.top);
					currentLineRect.bottom = Math.max(currentLineRect.bottom, rI.bottom);
				} else {
					currentLineRect=new RectF();
					currentLineRect.set(rI);
					selLineRects.add(currentLineRect);
					int cid = getCharIdxAtPos(rI.left + 1, rI.top + rI.height() / 2);
					if(cid>0) {
						getCharLoosePos(tmp, cid);
						currentLineRect.left = Math.min(currentLineRect.left, tmp.left);
						currentLineRect.right = Math.max(currentLineRect.right, tmp.right);
						currentLineRect.top = Math.min(currentLineRect.top, tmp.top);
						currentLineRect.bottom = Math.max(currentLineRect.bottom, tmp.bottom);
					}
				}
				if(box!=null) {
					box.left = Math.min(box.left, currentLineRect.left);
					box.right = Math.max(box.right, currentLineRect.right);
					box.top = Math.min(box.top, currentLineRect.top);
					box.bottom = Math.max(box.bottom, currentLineRect.bottom);
				}
			}
			return selLineRects;
		}
	}
	
	private boolean getWritable() {
		return true;
	}
	
	// https://www.cnblogs.com/fangsmile/p/9306510.html
	// 计算 |p1 p2| X |p1 p|
	float GetCross(PointF p, PointF p1, PointF p2) {
		return (p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y);
	}
	//判断点p是否在p1p2p3p4的正方形内
	boolean IsPointInMatrix(PointF p, PointF p1, PointF p2, PointF p3, PointF p4) {
		return GetCross(p1, p2, p) * GetCross(p3, p4, p) >= 0 && GetCross(p2, p3, p) * GetCross(p4, p1, p) >= 0;
	}
	
	public PDocument(ContentResolver contentResolver, Uri path, DisplayMetrics dm, AtomicBoolean abort) throws IOException {
		this.path = path;
		this.referenceCount = new AtomicInteger(1);
		this.dm = dm;
		if(pdfiumCore==null) {
			pdfiumCore = new PdfiumCore();
		}
		//File f = new File(path);
		//ParcelFileDescriptor pfd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY);
		ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(path, "r");
		//CMN.Log("ParcelFileDescriptor", pfd.getFd());
		pdfDocument = pdfiumCore.newDocument(pfd, abort==initFlag?0:LargeFileTheta);
		_num_entries = pdfiumCore.getPageCount(pdfDocument);
		mPDocPages = new PDocPage[_num_entries];
		LengthAlongScrollAxis=0;
		recalcPages(false, abort);
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
		//ThumbsLoResFactor=0.01f;
		CMN.Log("缩放比", ThumbsHiResFactor, ThumbsLoResFactor);
	}
	
	private void recalcPages(boolean reinit, AtomicBoolean abort) {
		for (int i = 0; i < _num_entries; i++) {
			PDocPage page = reinit&&mPDocPages[i]!=null?mPDocPages[i]:(mPDocPages[i]=new PDocPage(i, pdfiumCore.getPageSize(pdfDocument, i)));
			Size size = page.size;
			page.OffsetAlongScrollAxis=LengthAlongScrollAxis;
			LengthAlongScrollAxis+=(isHorizontalView?size.getWidth():size.getHeight())+gap;
			//if(i<10) CMN.Log("mPDocPages", i, size.getWidth(), size.getHeight());
			maxPageWidth = Math.max(maxPageWidth, size.getWidth());
			maxPageHeight = Math.max(maxPageHeight, size.getHeight());
			if(abort!=null&&abort.get()) {
				return;
			}
		}
		if(_num_entries>0) {
			PDocPage page = mPDocPages[_num_entries-1];
			Size size = page.size;
			LengthAlongScrollAxis=page.OffsetAlongScrollAxis+(isHorizontalView?size.getWidth():size.getHeight());
		}
	}
	
	public Bitmap renderBitmap(PDocPage page, Bitmap bitmap, float scale) {
		Size size = page.size;
		int w = (int) (size.getWidth()*scale);
		int h = (int) (size.getHeight()*scale);
		//CMN.Log("renderBitmap", w, h, bitmap.getWidth(), bitmap.getHeight());
		bitmap.eraseColor(Color.WHITE);
		page.open();
		pdfiumCore.renderPageBitmap(pdfDocument, bitmap, page.pageIdx, page.pid.get(), 0, 0, w, h , true);
		return bitmap;
	}
	
	public Bitmap renderRegionBitmap(PDocPage page, Bitmap bitmap, int startX, int startY, int srcW, int srcH) {
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		bitmap.eraseColor(Color.WHITE);
		page.open();
		//CMN.Log("renderRegion", w, h, startX, startY, srcW, srcH);
		pdfiumCore.renderPageBitmap(pdfDocument, bitmap, page.pageIdx, page.pid.get(), startX, startY, srcW, srcH, true);
		return bitmap;
	}
	
	public Bitmap drawTumbnail(Bitmap bitmap, int pageIdx, float scale) {
		PDocPage page = mPDocPages[pageIdx];
		Size size = page.size;
		int w = (int) (size.getWidth()*scale);
		int h = (int) (size.getHeight()*scale);
		page.open();
		//CMN.Log("PageOpened::", CMN.now()-CMN.ststrt, w, h);
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
	
	static class AnnotShape {
		int index;
		RectF box;
		QuadShape[] attachPts;
		
		public AnnotShape(RectF rect, int i) {
			box = rect;
			index=i;
		}
	}
	
	static class QuadShape {
		PointF p1=new PointF();
		PointF p2=new PointF();
		PointF p3=new PointF();
		PointF p4=new PointF();
	}
	
	@Override
	public String toString() {
		return "PDocument{" +
				"isClosed=" + isClosed +
				'}';
	}
	
	public void test() {
		ArrayList<Integer> arr = new ArrayList<>();
		int sz = 2000;
		
		CMN.rt();
		for (int i = 0; i < sz*2; i++) {
			pdfiumCore.nativeTestAdd(i);
		}
		CMN.pt("nativeTestLoopAdd_1：");
		
		CMN.rt();
		pdfiumCore.nativeTestLoopAdd(arr, sz);
		CMN.pt("nativeTestLoopAdd_0：");
	}
	public void test1() {
		CMN.rt();
		CMN.Log("nativeGetStringChars 1", pdfiumCore.nativeGetStringChars("happy"));
		CMN.Log("nativeGetStringChars 2", pdfiumCore.nativeGetStringChars("happy"));
		CMN.pt("nativeGetStringChars：");
	}
	public void test2() {
		int sz = 2000;
		
		
		CMN.rt();
		for (int i = 0; i < sz; i++) {
			RectF rect = new RectF();
			pdfiumCore.nativeTestCallSetFields(rect);
		}
		CMN.pt("nativeTestCallSetFields call：");
		
		
		CMN.rt();
		for (int i = 0; i < sz; i++) {
			RectF rect = new RectF();
			pdfiumCore.nativeTestSetFields(rect);
		}
		CMN.pt("nativeTestCallSetFields set：");
	}
}
