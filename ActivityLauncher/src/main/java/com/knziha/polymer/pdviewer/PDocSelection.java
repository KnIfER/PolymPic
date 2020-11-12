package com.knziha.polymer.pdviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;

import java.util.ArrayList;

/** A View to paint PDF selections */
public class PDocSelection extends View {
	public boolean supressRecalcInval;
	PDocView pDocView;
	float drawableWidth=128;
	float drawableHeight=128;
	float drawableDeltaW = drawableWidth / 4;
	Paint rectPaint;
	/** Small Canvas for magnifier.
	 * {@link Canvas#clipPath ClipPath} fails if the canvas it too high.
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
		if(pDocView!=null&&pDocView.pdoc!=null&&pDocView.hasSelction) {
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
		int dir=(int) Math.signum(ed-st);
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
			page.getCharPos(pDocView.handleRightPos, ed+delta);
			pDocView.lineHeightRight = pDocView.handleRightPos.height()/2;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(pDocView!=null && pDocView.hasSelction) {
			RectF VR = tmpPosRct;
			Matrix matrix = pDocView.matrix;
			
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
			// 绘制选区
			for (int i = 0; i < rectPoolSize; i++) {
				PDocument.PDocPage page = pDocView.pdoc.mPDocPages[selPageSt + i];
				if(!(page.OffsetAlongScrollAxis>=pDocView.draw_edoff||page.OffsetAlongScrollAxis+page.size.getHeight()<=pDocView.draw_stoff)) {
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
				
				int magX = (int) (pDocView.lastX-magW/2);
				int magY = (int) (pDocView.lastY-magH-200-drawableHeight-pDocView.lineHeight);
				
				PageCacheDrawable.setBounds(magX, magY, magX+magW, magY+magH);
				frameDrawable.setBounds(PageCacheDrawable.getBounds());
				
				PageCacheDrawable.draw(canvas);
				frameDrawable.draw(canvas);
				
				cc.restore();
			}
			
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
