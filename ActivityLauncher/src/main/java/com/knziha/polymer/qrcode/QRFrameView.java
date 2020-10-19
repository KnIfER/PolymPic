/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.polymer.qrcode;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points. 这是一个位于相机顶部的预览view,它增加了一个外部部分透明的取景框，以及激光扫描动画和结果组件
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class QRFrameView extends View {
	private static final long ANIMATION_DELAY = 25L;
	private final int SCAN_VELOCITY = 8;
	private static final int CURRENT_POINT_OPACITY = 0xA0;
	private static final int MAX_RESULT_POINTS = 20;
	private static final int POINT_SIZE = 6;
	public QRActivity a;
	
	private Rect framingRect;
	private final Paint paint;
	private final Paint pointpainter;
	private final Paint framepainter;
	private Bitmap resultBitmap;
	private final int maskColor; // 取景框外的背景颜色
	private final int resultPointColor; // 特征点的颜色
	private final List<ResultPoint> possibleResultPoints = new ArrayList<>(5);
	private final List<ResultPoint> lastPossibleResultPoints = new ArrayList<>(5);
	
	public int scanLineTop;

	Bitmap scanLight;
	private RectF laserRect = new RectF();
	private int lowerLaserLimit;
	
	public boolean drawLocations;
	
	public boolean drawLaser;
	
	private ResultPointCollector mResultPointCollector;
	
	private boolean animating=false;
	
	
	public QRFrameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		Resources resources = getResources();
		maskColor = resources.getColor(R.color.viewfinder_mask);
		resultPointColor = resources.getColor(R.color.possible_result_points);
		scanLight = BitmapFactory.decodeResource(resources, R.drawable.scan_light);
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(maskColor);
		paint.setAlpha(CURRENT_POINT_OPACITY);
		
		framepainter = new Paint(Paint.ANTI_ALIAS_FLAG);
		framepainter.setColor(Color.WHITE);
		framepainter.setStrokeWidth(5);
		framepainter.setStyle(Paint.Style.STROKE);
		
		pointpainter = new Paint(Paint.ANTI_ALIAS_FLAG);
		pointpainter.setAlpha(CURRENT_POINT_OPACITY);
		pointpainter.setColor(resultPointColor);
	}
	
	public void setFramingRect(Rect framingRect) {
		this.framingRect = framingRect;
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		Rect frame = framingRect; // 取景框
		//Rect previewFrame = cameraManager.getFramingRectInPreview();
		if (frame == null) return;
		
		int width = getWidth();
		int height = getHeight();
		
		// Draw the exterior (i.e. outside the framing rect) dimmed
		
		canvas.drawRect(0, 0, width, frame.top, paint);// Rect_1
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint); // Rect_2
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint); // Rect_3
		canvas.drawRect(0, frame.bottom + 1, width, height, paint); // Rect_4
		
		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			// 如果有二维码结果的Bitmap，在扫取景框内绘制不透明的result Bitmap
			canvas.drawBitmap(resultBitmap, null, frame, paint);
		} else {
			canvas.drawRect(frame, framepainter);
			
			long delay = ANIMATION_DELAY;
			
			if(drawLaser) {
				// laser blade hen hen hah hyi！
				lowerLaserLimit = frame.bottom-15;
				
				drawScanLight(canvas, frame);
				
				if(scanLineTop==lowerLaserLimit) {
					scanLineTop=frame.top;
					//delay=350L;
					delay=650L;
				}
			} else {
				delay = 160L;
			}
			
			if(drawLocations && delay<300) {
				// 绘制点云
				synchronized (possibleResultPoints) {
					if (!lastPossibleResultPoints.isEmpty()) {
						drawPointsCloud(frame, canvas, lastPossibleResultPoints, POINT_SIZE / 2.0f);
						lastPossibleResultPoints.clear();
					}
					if (!possibleResultPoints.isEmpty()) {
						drawPointsCloud(frame, canvas, possibleResultPoints, POINT_SIZE);
						lastPossibleResultPoints.addAll(possibleResultPoints);
						possibleResultPoints.clear();
					}
				}
			}
			
			if(animating&&(drawLocations||drawLaser)) {
				// Request another update at the animation interval
				// , but only repaint the laser line, not the entire viewfinder mask.
				postInvalidateDelayed(delay, frame.left - POINT_SIZE,
						frame.top - POINT_SIZE, frame.right + POINT_SIZE,
						frame.bottom + POINT_SIZE);
			}
		}
	}
	
	private void drawPointsCloud(Rect frame, Canvas canvas, List<ResultPoint> pointsCloud, float radius) {
		int frameHeightPlusLeft = frame.left+frame.height();
		for (ResultPoint point : pointsCloud) {
			float x = point.getX() * a.scale;
			float y = point.getY() * a.scale;
			if(a.isPortrait) {
				canvas.drawCircle(frameHeightPlusLeft-y, frame.top+x, radius, pointpainter);
			} else {
				canvas.drawCircle(frame.left+x, frame.top+y, radius, pointpainter);
			}
		}
	}
	
	private void drawScanLight(Canvas canvas, Rect frame) {
		//CMN.Log("drawScanLight", scanLineTop);
		if (scanLineTop == 0) {
			scanLineTop = frame.top;
		}
		
		if(animating) {
			scanLineTop += SCAN_VELOCITY;
		}
		
		if (scanLineTop >= lowerLaserLimit-5) {
			scanLineTop = lowerLaserLimit;
			return;
		}
		
		laserRect.set(frame.left, scanLineTop, frame.right, scanLineTop + 30);
		
		canvas.drawBitmap(scanLight, null, laserRect, paint);
	}
	
	private void drawFrameCorners(Canvas canvas, Rect frame) {
		framepainter.setStyle(Paint.Style.FILL);
		int corWidth = 15;
		int corLength = 45;
		// TL
		canvas.drawRect(frame.left - corWidth, frame.top, frame.left, frame.top
				+ corLength, framepainter);
		canvas.drawRect(frame.left - corWidth, frame.top - corWidth, frame.left
				+ corLength, frame.top, framepainter);
		// TR
		canvas.drawRect(frame.right, frame.top, frame.right + corWidth,
				frame.top + corLength, framepainter);
		canvas.drawRect(frame.right - corLength, frame.top - corWidth,
				frame.right + corWidth, frame.top, framepainter);
		// BL
		canvas.drawRect(frame.left - corWidth, frame.bottom - corLength,
				frame.left, frame.bottom, framepainter);
		canvas.drawRect(frame.left - corWidth, frame.bottom, frame.left
				+ corLength, frame.bottom + corWidth, framepainter);
		// BR
		canvas.drawRect(frame.right, frame.bottom - corLength, frame.right
				+ corWidth, frame.bottom, framepainter);
		canvas.drawRect(frame.right - corLength, frame.bottom, frame.right
				+ corWidth, frame.bottom + corWidth, framepainter);
		
		framepainter.setStyle(Paint.Style.STROKE);
	}
	
	public void setBitmap(Bitmap bitmap) {
		if (resultBitmap != null) {
			resultBitmap.recycle();
		}
		if(resultBitmap!=bitmap) {
			resultBitmap = bitmap;
			invalidate();
		}
	}
	
	public void addPossibleResultPoint(ResultPoint point) {
		synchronized (possibleResultPoints) {
			possibleResultPoints.add(point);
			int size = possibleResultPoints.size();
			if (size > MAX_RESULT_POINTS) {
				possibleResultPoints.subList(0, size - MAX_RESULT_POINTS / 2).clear();
			}
		}
	}
	
	public ResultPointCallback getPointsCollector() {
		if(mResultPointCollector==null) {
			mResultPointCollector=new ResultPointCollector(this);
		}
		return mResultPointCollector;
	}
	
	public void pause() {
		animating=false;
	}
	
	public void suspense() {
		animating=false;
		scanLineTop=framingRect.top+framingRect.height()/2;
		invalidate();
	}
	
	public void resume() {
		animating=true;
		if(drawLaser||drawLocations) {
			invalidate();
		}
	}
	
	static class ResultPointCollector implements ResultPointCallback {
		public final WeakReference<QRFrameView> view;
		
		public ResultPointCollector(QRFrameView view) {
			this.view = new WeakReference<>(view);
		}
		
		@Override
		public void foundPossibleResultPoint(ResultPoint point) {
			QRFrameView view=this.view.get();
			if(view!=null) {
				view.addPossibleResultPoint(point);
			}
		}
	}
}
