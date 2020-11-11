package com.knziha.polymer.pdviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.knziha.polymer.Utils.CMN;

import java.util.ArrayList;

/** A View to paint PDF selections */
public class PDocSelection extends View {
	PDocView pDocView;
	Paint rectPaint;
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
	
	int rectPoolSize=0;
	
	ArrayList<ArrayList<RectF>> rectPool = new ArrayList<>();
	
	public void resetSel() {
		if(pDocView!=null&&pDocView.pdoc!=null&&pDocView.hasSelction) {
			int pageCount = pDocView.selPageEd-pDocView.selPageSt;
			int sz = rectPool.size();
			ArrayList<RectF> rectPagePool;
			for (int i = 0; i <= pageCount; i++) {
				if(i>=sz) {
					rectPool.add(rectPagePool=new ArrayList<>());
				} else {
					rectPagePool = rectPool.get(i);
				}
				int selSt = i==0?pDocView.selStart:0;
				int selEd = i==pageCount?pDocView.selEnd:-1;
				PDocument.PDocPage page = pDocView.pdoc.mPDocPages[pDocView.selPageSt + i];
				page.getSelRects(rectPagePool, selSt, selEd);//+10
			}
			recalcHandles();
			rectPoolSize = pageCount+1;
		} else {
			rectPoolSize = 0;
		}
		invalidate();
	}
	
	public void recalcHandles() {
		PDocument.PDocPage page = pDocView.pdoc.mPDocPages[pDocView.selPageSt];
		page.getCharPos(pDocView.handleLeftPos, pDocView.selStart);
		page = pDocView.pdoc.mPDocPages[pDocView.selPageEd];
		page.getCharPos(pDocView.handleRightPos, pDocView.selEnd-1);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(pDocView!=null && pDocView.hasSelction) {
			Rect VR = pDocView.vRect;
			Matrix matrix = pDocView.matrix;
			
			RectF VRF = new RectF();
			
			float w=pDocView.handleLeft.getIntrinsicWidth();
			float deltaW = w / 4;
			w*=pDocView.drawableScale;
			float h=pDocView.handleLeft.getIntrinsicHeight()*pDocView.drawableScale;
			
			pDocView.sourceToViewRectFF(pDocView.handleLeftPos, VRF);
			float left = VRF.left+deltaW;
			pDocView.handleLeft.setBounds((int)(left-w), (int)VRF.bottom, (int)left, (int)(VRF.bottom+h));
			pDocView.handleLeft.draw(canvas);
			//canvas.drawRect(pDocView.handleLeft.getBounds(), rectPaint);
			
			pDocView.sourceToViewRectF(pDocView.handleRightPos, VR);
			left = VR.right-deltaW;
			pDocView.handleRight.setBounds((int)left, (int)VR.bottom, (int)(left+w), (int)(VR.bottom+h));
			pDocView.handleRight.draw(canvas);
			//canvas.drawRect(pDocView.handleRight.getBounds(), rectPaint);
			
			for (int i = 0; i < rectPoolSize; i++) {
				ArrayList<RectF> rectPage = rectPool.get(i);
				for(RectF rI:rectPage) {
					pDocView.sourceToViewRectF(rI, VR);
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
					canvas.drawRect(new RectF(0, 0, bmWidth, bmHeight), rectPaint);
					canvas.restore();
					
					
				}
			}
		}
	}
}
