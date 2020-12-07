package com.knziha.polymer.pdviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.pdviewer.bookdata.SearchRecordItem;
import com.shockwave.pdfium.SearchRecord;

import java.util.ArrayList;

/** A View to paint PDF selections, [magnifier] and search highlights */
public class PDocSelection extends View {
	public boolean supressRecalcInval;
	PDocView pDocView;
	float drawableWidth=128;
	float drawableHeight=128;
	float drawableDeltaW = drawableWidth / 4;
	Paint rectPaint;
	Paint rectFramePaint;
	Paint rectHighlightPaint;
	/** Small Canvas for magnifier.
	 * {@link Canvas#clipPath ClipPath} fails if the canvas it too high. ( will never happen in this project. )
	 * see <a href="https://issuetracker.google.com/issues/132402784">issuetracker</a>) */
	Canvas cc;
	Bitmap PageCache;
	BitmapDrawable PageCacheDrawable;
	
	Path magClipper;
	RectF magClipperR;
	float magFactor=1.5f;
	int magW=560;
	int magH=280;
	/** output image*/
	Drawable frameDrawable;
	private float framew;
	private final PointF vCursorPos = new PointF();
	private final RectF tmpPosRct = new RectF();
	
	int selPageSt;
	int selPageEd;
	int selStart;
	int selEnd;
	private float magdraw_stoff;
	private float magdraw_edoff;
	private float magdraw_stoffX;
	private float magdraw_edoffX;
	
	public PDocPageResultsProvider searchCtx;
	
	public PDocSelection(Context context) {
		super(context);
		init();
	}
	
	public PDocSelection(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public PDocSelection(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		rectPaint = new Paint();
		rectPaint.setColor(0x66109afe);
		//rectPaint.setColor(0xffffff00);
		rectHighlightPaint = new Paint();
		rectHighlightPaint.setColor(0xffffff00);
		rectPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
		rectHighlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
		rectFramePaint = new Paint();
		rectFramePaint.setColor(0xccc7ab21);
		rectFramePaint.setStyle(Paint.Style.STROKE);
		rectFramePaint.setStrokeWidth(0.5f);
	}
	
	
	private void initMagnifier() {
		//setLayerType(LAYER_TYPE_NONE,null);
		cc = new Canvas(PageCache= Bitmap.createBitmap(magW,magH,Bitmap.Config.ARGB_8888));
		PageCacheDrawable = new BitmapDrawable(getResources(), PageCache);
		frameDrawable = getResources().getDrawable(R.drawable.frame);
		framew=getResources().getDimension(R.dimen.framew);
		magClipper = new Path();
		magClipperR = new RectF(PageCacheDrawable.getBounds());
		magClipper.reset();
		magClipperR.set(0,0,magW,magH);
		magClipper.addRoundRect(magClipperR,framew+5,framew+5,Path.Direction.CW);
	}
	
	int rectPoolSize=0;
	
	ArrayList<ArrayList<RectF>> rectPool = new ArrayList<>();
	
	ArrayList<RectF> magSelBucket = new ArrayList<>();
	
	public void resetSel() {
		CMN.Log("resetSel", pDocView.selPageSt, pDocView.selPageEd, pDocView.selStart, pDocView.selEnd);
		if(pDocView!=null&&pDocView.pdoc!=null&&pDocView.hasSelection) {
			boolean b1=selPageEd<selPageSt;
			if(b1) {
				selPageEd = pDocView.selPageSt;
				selPageSt = pDocView.selPageEd;
			} else {
				selPageEd = pDocView.selPageEd;
				selPageSt = pDocView.selPageSt;
			}
			if(b1||selPageEd==selPageSt&&selEnd<selStart) {
				selStart = pDocView.selEnd;
				selEnd = pDocView.selStart;
			} else {
				selStart = pDocView.selStart;
				selEnd = pDocView.selEnd;
			}
			int pageCount = selPageEd-selPageSt;
			int sz = rectPool.size();
			ArrayList<RectF> rectPagePool;
			for (int i = 0; i <= pageCount; i++) {
				if(i>=sz) {
					rectPool.add(rectPagePool=new ArrayList<>());
				} else {
					rectPagePool = rectPool.get(i);
				}
				int selSt = i==0?selStart:0;
				int selEd = i==pageCount?selEnd:-1;
				PDocument.PDocPage page = pDocView.pdoc.mPDocPages[selPageSt + i];
				page.getSelRects(rectPagePool, selSt, selEd);//+10
			}
			recalcHandles();
			rectPoolSize = pageCount+1;
		} else {
			rectPoolSize = 0;
		}
		if(!supressRecalcInval) {
			invalidate();
		}
	}
	
	public void recalcHandles() {
		PDocument.PDocPage page = pDocView.pdoc.mPDocPages[pDocView.selPageSt];
		page.prepareText();
		int st=pDocView.selStart;
		int ed=pDocView.selEnd;
		int dir=pDocView.selPageEd-pDocView.selPageSt;
		dir=(int) Math.signum(dir==0?ed-st:dir);
		if(dir!=0) {
			String atext = page.allText;
			int len=atext.length();
			if(st>=0&&st<len) {
				char c;
				while(((c=atext.charAt(st))=='\r'||c=='\n')&&st+dir>=0&&st+dir<len) {
					st+=dir;
				}
			}
			page.getCharPos(pDocView.handleLeftPos, st);
			pDocView.lineHeightLeft = pDocView.handleLeftPos.height()/2;
			page.getCharLoosePos(pDocView.handleLeftPos, st);
			
			page = pDocView.pdoc.mPDocPages[pDocView.selPageEd];
			page.prepareText();
			atext = page.allText;
			len = atext.length();
			int delta=-1;
			if(ed>=0&&ed<len) {
				char c;  dir*=-1;
				while(((c=atext.charAt(ed))=='\r'||c=='\n')&&ed+dir>=0&&ed+dir<len) {
					delta=0;
					ed+=dir;
				}
			}
			//CMN.Log("getCharPos", page.allText.substring(ed+delta, ed+delta+1));
			page.getCharPos(pDocView.handleRightPos, ed+delta);
			pDocView.lineHeightRight = pDocView.handleRightPos.height()/2;
			page.getCharLoosePos(pDocView.handleRightPos, ed+delta);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(pDocView==null) {
			return;
		}
		RectF VR = tmpPosRct;
		Matrix matrix = pDocView.matrix;
		if(searchCtx!=null) {
			// 绘制搜索高亮
			for (int i = 0; i < pDocView.logiLayoutSz; i++) {
				int pageIdx = pDocView.logiLayoutSt+i;
				SearchRecord record = searchCtx.getRecordForActualPage(pageIdx);
				if(record!=null) {
					PDocument.PDocPage page = pDocView.pdoc.mPDocPages[pageIdx];
					page.getAllMatchOnPage(record, searchCtx);
					ArrayList<SearchRecordItem> data = (ArrayList<SearchRecordItem>) record.data;
					for (int j = 0, len=data.size(); j < len; j++) {
						RectF[] rects = data.get(j).rects;
						if(rects!=null) {
							for(RectF rI:rects) {
								pDocView.sourceToViewRectFF(rI, VR);
								matrix.reset();
								int bmWidth = (int) rI.width();
								int bmHeight = (int) rI.height();
								pDocView.setMatrixArray(pDocView.srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
								pDocView.setMatrixArray(pDocView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
								
								matrix.setPolyToPoly(pDocView.srcArray, 0, pDocView.dstArray, 0, 4);
								matrix.postRotate(0, pDocView.getScreenWidth(), pDocView.getSHeight());
								
								canvas.save();
								canvas.concat(matrix);
								VR.set(0, 0, bmWidth, bmHeight);
								canvas.drawRect(VR, rectHighlightPaint);
								canvas.restore();
							}
						}
					}
				}
			}
		}
		if(pDocView.hasSelection) {
			pDocView.sourceToViewRectFF(pDocView.handleLeftPos, VR);
			float left = VR.left+drawableDeltaW;
			pDocView.handleLeft.setBounds((int)(left-drawableWidth), (int)VR.bottom, (int)left, (int)(VR.bottom+drawableHeight));
			pDocView.handleLeft.draw(canvas);
			//canvas.drawRect(pDocView.handleLeft.getBounds(), rectPaint);
			
			pDocView.sourceToViewRectFF(pDocView.handleRightPos, VR);
			left = VR.right-drawableDeltaW;
			pDocView.handleRight.setBounds((int)left, (int)VR.bottom, (int)(left+drawableWidth), (int)(VR.bottom+drawableHeight));
			pDocView.handleRight.draw(canvas);
			
			//canvas.drawRect(pDocView.handleRight.getBounds(), rectPaint);
			pDocView.sourceToViewCoord(pDocView.sCursorPos, vCursorPos);
			
			boolean drawMagnifier = true;
			drawMagnifier = pDocView.draggingHandle != null;
			
			if(drawMagnifier) {
				magSelBucket.clear();
				float magdrawW = magW/pDocView.scale/2;
				float magdrawH = magW/pDocView.scale/2;
				magdraw_stoffX = pDocView.sCursorPos.x-magdrawW;
				magdraw_edoffX = pDocView.sCursorPos.x+magdrawW;
				magdraw_stoff = pDocView.sCursorPos.y-magdrawH;
				magdraw_edoff = pDocView.sCursorPos.y+magdrawH;
			}
			
			boolean horizon = pDocView.pdoc.isHorizontalView();
			// 绘制选区，是一页页分开存储的
			for (int i = 0; i < rectPoolSize; i++) {
				PDocument.PDocPage page = pDocView.pdoc.mPDocPages[selPageSt + i];
				if(!horizon&&!(page.OffsetAlongScrollAxis>=pDocView.draw_edoff||page.OffsetAlongScrollAxis+page.size.getHeight()<=pDocView.draw_stoff)
					||horizon&&!(page.OffsetAlongScrollAxis>=pDocView.draw_edoffX||page.OffsetAlongScrollAxis+page.size.getWidth()<=pDocView.draw_stoffX)) {
					ArrayList<RectF> rectPage = rectPool.get(i);
					for(RectF rI:rectPage) {
						if(!(rI.top>=pDocView.draw_edoff || rI.bottom<pDocView.draw_stoff
								|| rI.left>=pDocView.draw_edoffX || rI.right<pDocView.draw_stoffX)) {
							pDocView.sourceToViewRectFF(rI, VR);
							matrix.reset();
							int bmWidth = (int) rI.width();
							int bmHeight = (int) rI.height();
							pDocView.setMatrixArray(pDocView.srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
							pDocView.setMatrixArray(pDocView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
							
							matrix.setPolyToPoly(pDocView.srcArray, 0, pDocView.dstArray, 0, 4);
							matrix.postRotate(0, pDocView.getScreenWidth(), pDocView.getSHeight());
							
							//matrix.mapR
							canvas.save();
							canvas.concat(matrix);
							VR.set(0, 0, bmWidth, bmHeight);
							canvas.drawRect(VR, rectPaint);
							canvas.restore();
							
							if(drawMagnifier && !(rI.top>=magdraw_edoff || rI.bottom<magdraw_stoff
									|| rI.left>=magdraw_edoffX || rI.right<magdraw_stoffX)) {
								magSelBucket.add(rI);
							}
						}
					}
				}
			}
			
			// 绘制放大镜
			if(drawMagnifier) {
			//if(pDocView.draggingHandle!=null) {
				if(PageCache==null)
					initMagnifier();
				
				cc.save();
				cc.clipPath(magClipper);//为了圆角
				
				cc.drawColor(pDocView.BackGroundColor);
				
				// 放大镜中的内容
				for (PDocView.RegionTile rgnTile:pDocView.drawingBucket) {
					if(!(rgnTile.sRect.top>=pDocView.draw_edoff || rgnTile.sRect.bottom<pDocView.draw_stoff
							|| rgnTile.sRect.left>=pDocView.draw_edoffX || rgnTile.sRect.right<pDocView.draw_stoffX)) {
						Bitmap bm = rgnTile.bitmap;
						sourceToMagViewRectFF(rgnTile.sRect, VR);
						matrix.reset();
						int d=0;
						int bmWidth = bm.getWidth()+d;
						int bmHeight = bm.getHeight()+d;
						
						pDocView.setMatrixArray(pDocView.srcArray, 0, 0,bmWidth , 0, bmWidth, bmHeight, 0, bmHeight);
						pDocView.setMatrixArray(pDocView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
						
						matrix.setPolyToPoly(pDocView.srcArray, 0, pDocView.dstArray, 0, 4);
						//matrix.postRotate(0, getScreenWidth(), getSHeight());
						
						cc.drawBitmap(bm, matrix, pDocView.bitmapPaint);
					}
				}
				
				// 放大镜中的选区
				for (RectF rI:magSelBucket) {
					sourceToMagViewRectFF(rI, VR);
					matrix.reset();
					int bmWidth = (int) rI.width();
					int bmHeight = (int) rI.height();
					pDocView.setMatrixArray(pDocView.srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
					pDocView.setMatrixArray(pDocView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
					
					matrix.setPolyToPoly(pDocView.srcArray, 0, pDocView.dstArray, 0, 4);
					matrix.postRotate(0, pDocView.getScreenWidth(), pDocView.getSHeight());
					
					//matrix.mapR
					cc.save();
					cc.concat(matrix);
					VR.set(0, 0, bmWidth, bmHeight);
					cc.drawRect(VR, rectPaint);
					cc.restore();
				}
				
				// 放大镜中的控点
				sourceToMagViewRectFF(pDocView.handleLeftPos, VR);
				left = VR.left+drawableDeltaW;
				pDocView.handleLeft.setBounds((int)(left-drawableWidth), (int)VR.bottom, (int)left, (int)(VR.bottom+drawableHeight));
				pDocView.handleLeft.draw(cc);
				
				sourceToMagViewRectFF(pDocView.handleRightPos, VR);
				left = VR.right-drawableDeltaW;
				pDocView.handleRight.setBounds((int)left, (int)VR.bottom, (int)(left+drawableWidth), (int)(VR.bottom+drawableHeight));
				pDocView.handleRight.draw(cc);
				
				
				int magX = (int) (pDocView.lastX-magW/2);
				int magY = (int) (pDocView.lastY-magH-200-drawableHeight-pDocView.lineHeight);
				
				PageCacheDrawable.setBounds(magX, magY, magX+magW, magY+magH);
				frameDrawable.setBounds(PageCacheDrawable.getBounds());
				
				PageCacheDrawable.draw(canvas);
				frameDrawable.draw(canvas);
				
				cc.restore();
			}
			
		}
		else if(pDocView.hasAnnotSelction) {
			// 绘制高亮选择框
			RectF rI=pDocView.annotSelRect;
			
			pDocView.sourceToViewRectFF(rI, VR);
			matrix.reset();
			int bmWidth = (int) rI.width();
			int bmHeight = (int) rI.height();
			pDocView.setMatrixArray(pDocView.srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
			pDocView.setMatrixArray(pDocView.dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
			
			matrix.setPolyToPoly(pDocView.srcArray, 0, pDocView.dstArray, 0, 4);
			matrix.postRotate(0, pDocView.getScreenWidth(), pDocView.getSHeight());
			
			//matrix.mapR
			canvas.save();
			canvas.concat(matrix);
			VR.set(0, 0, bmWidth, bmHeight);
			canvas.drawRect(VR, rectFramePaint);
			canvas.restore();
			
			//CMN.Log("绘制了高亮");
		}
	}
	
	void sourceToMagViewRectFF(@NonNull RectF sRect, @NonNull RectF vTarget) {
		float scaleFactor=1.5f;
		float scale = pDocView.scale*scaleFactor;
		float vx = (pDocView.vTranslate.x-vCursorPos.x)*scaleFactor+magW*0.5f;
		float vy = (pDocView.vTranslate.y-vCursorPos.y)*scaleFactor+magH*0.5f;
		
		vTarget.set(
				sRect.left * scale + vx,
				sRect.top * scale + vy,
				sRect.right * scale + vx,
				sRect.bottom * scale + vy
		);
	}
}
