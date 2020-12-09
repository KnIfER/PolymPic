/*
Copyright 2020 KnIfER
Copyright 2013-2015 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.knziha.polymer.pdviewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.pdviewer.bookdata.BookOptions;
import com.knziha.polymer.pdviewer.bookdata.PDocBookInfo;
import com.knziha.polymer.slideshow.ImageViewState;
import com.knziha.polymer.slideshow.OverScroller;
import com.knziha.polymer.slideshow.decoder.ImageDecoder;
import com.knziha.polymer.slideshow.decoder.ImageRegionDecoder;
import com.knziha.polymer.webslideshow.RecyclerViewPager.OnPageChangedListener;
import com.knziha.polymer.webslideshow.RecyclerViewPagerAdapter;
import com.knziha.polymer.widgets.Utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/** Pro-level utilization of the PDFium project.
 * Inspired by https://github.com/davemorrissey/subsampling-scale-image-view
 *
 * 特性：v1.0 连续渲染页面，双击、缩放动画，单指双击缩放，平滑惯性滑动，弹性滚动限制
 * 		v2.0 选择、高亮文本
 * 		v3.0 保存
 *
 * 备注：或许应抽离布局相关代码，实现 LinearLayout、HorizontalLinearLayout、甚至 GridLayout布局，然时间、成本受限，就这样一股脑的吧。
 */
@SuppressWarnings({"unused", "IntegerDivisionInFloatingPointContext"})
public class PDocView extends View {
	private static final String TAG = PDocView.class.getSimpleName();
	final static Bitmap DummyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
	
	/** Attempt to use EXIF information on the image to rotate it. Works for external files only. */
	public static final int ORIENTATION_USE_EXIF = -1;
	/** Display the image file in its native orientation. */
	public static final int ORIENTATION_0 = 0;
	/** Rotate the image 90 degrees clockwise. */
	public static final int ORIENTATION_90 = 90;
	/** Rotate the image 180 degrees. */
	public static final int ORIENTATION_180 = 180;
	/** Rotate the image 270 degrees clockwise. */
	public static final int ORIENTATION_270 = 270;
	
	/** Quadratic ease out. Not recommended for scale animation, but good for panning. */
	public static final int EASE_OUT_QUAD = 1;
	/** Quadratic ease in and out. */
	public static final int EASE_IN_OUT_QUAD = 2;
	
	/** State change originated from animation. */
	public static final int ORIGIN_ANIM = 1;
	/** State change originated from touch gesture. */
	public static final int ORIGIN_TOUCH = 2;
	/** State change originated from a fling momentum anim. */
	public static final int ORIGIN_FLING = 3;
	/** State change originated from a double tap zoom anim. */
	public static final int ORIGIN_DOUBLE_TAP_ZOOM = 4;
	public Toastable_Activity a;
	public int BackGroundColor=Color.LTGRAY;
	
	// Uri of full size image
	private Uri uri;
	
	// Overlay tile boundaries and other info
	private boolean SSVD=false;//debug
	
	private boolean SSVDF=false;//debug
	
	private boolean SSVDF2=false;//debug
	
	// Image orientation setting
	private int orientation = ORIENTATION_0;
	
	// Max scale allowed (prevent infinite zoom)
	public float maxScale = 1.5F;
	
	// Min scale allowed (prevent infinite zoom)
	private float minScale = 0.1f;
	
	// overrides for the dimensions of the generated tiles
	public static final int TILE_SIZE_AUTO = Integer.MAX_VALUE;
	private int maxTileWidth = TILE_SIZE_AUTO;
	private int maxTileHeight = TILE_SIZE_AUTO;
	
	// An executor service for loading of images
	private Executor executor = AsyncTask.THREAD_POOL_EXECUTOR;
	
	// Whether tiles should be loaded while gestures and animations are still in progress
	private boolean eagerLoadingEnabled = true;
	
	// Gesture detection settings
	private boolean panEnabled = true;
	private boolean zoomEnabled = true;
	private boolean rotationEnabled = false;
	private boolean quickScaleEnabled = true;
	
	// Double tap zoom behaviour
	private float[] quickZoomLevels = new float[3];
	private float quickZoomLevelCount = 2;
	
	// Current scale and scale at start of zoom
	public float scale;
	private float scaleStart;
	
	// Rotation parameters
	private float rotation = 0;
	// Stored to avoid unnecessary calculation
	private double cos = 1;//Math.cos(0);
	private double sin = 0;//Math.sin(0);
	
	// Screen coordinate of top-left corner of source image
	public PointF vTranslate = new PointF();
	PointF vTranslateOrg = new PointF();
	private PointF vTranslateStart = new PointF();
	private PointF vTranslateBefore = new PointF();
	
	// Source coordinate to center on, used when new position is set externally before view is ready
	private float pendingScale;
	private PointF sPendingCenter;
	private PointF sRequestedCenter;
	
	private AnimationBuilder pendingAnimation;
	
	// Source image dimensions and orientation - dimensions relate to the unrotated image
	private long sWidth;
	private long sHeight;
	private int sOrientation;
	private Rect sRegion;
	private Rect pRegion;
	
	// Is two-finger zooming in progress
	private boolean isZooming;
	// Is one-finger panning in progress
	private boolean isPanning;
	private boolean isRotating;
	// Is quick-scale gesture in progress
	private boolean isQuickScaling;
	// Max touches used in current gesture
	private int maxTouchCount;
	
	// Fling detector
	private GestureDetector flingdetector;
	
	private boolean doubleTapDetected;
	private PointF doubleTapFocus = new PointF();
	// Debug values
	private final PointF vCenterStart = new PointF();
	private PointF sCenterStart = new PointF();
	private PointF vCenterStartNow = new PointF();
	private float vDistStart;
	private float lastAngle;
	
	// Current quickscale state
	private final float quickScaleThreshold;
	private float quickScaleLastDistance;
	private float quickScaleStart;
	private boolean quickScaleMoved;
	private PointF quickScaleVLastPoint;
	private PointF quickScaleSCenter;
	private PointF quickScaleVStart;
	private boolean startTouchWithAnimation;
	
	// Scale and center animation tracking
	private Anim anim;
	
	// Whether a ready notification has been sent to subclasses
	private boolean readySent;
	// Whether a base layer loaded notification has been sent to subclasses
	private boolean imageLoading = true;
	
	// Long click listener
	private OnLongClickListener onLongClickListener;
	
	// Long click handler
	private final Handler handler;
	private static final int MESSAGE_LONG_CLICK = 1;
	
	// Paint objects
	Paint bitmapPaint;
	private Paint debugTextPaint;
	private Paint debugLinePaint;
	private Paint tileBgPaint;
	
	// Volatile fields used to reduce object creation
	private ScaleTranslateRotate strTemp = new ScaleTranslateRotate(0, new PointF(0, 0), 0);
	final Matrix matrix = new Matrix();
	private RectF sRect;
	final float[] srcArray = new float[8];
	final float[] dstArray = new float[8];
	
	//The logical density of the display
	private final float density;
	
	// A global preference for bitmap format, available to decoder classes that respect it
	private static Bitmap.Config preferredBitmapConfig;
	public DisplayMetrics dm;
	public BookOptions opt;
	public boolean isProxy;
	public String ImgSrc =null;
	private Runnable mAnimationRunnable = this::handle_animation;
	
	public PDocument pdoc;
	float lastX;
	float lastY;
	float orgX;
	float orgY;
	private float view_pager_toguard_lastX;
	private float view_pager_toguard_lastY;
	boolean freefling=true;
	boolean freeAnimation=true;
	private boolean isFlinging;
	private TilesInitTask loadingTask;
	private boolean treatNxtUpAsSingle;
	
	public RecyclerViewPagerAdapter.PageScope pageScoper;
	
	private OnPageChangedListener mOnPageChangeListener;
	
	private int lastMiddlePage;
	private boolean hideContextMenu;
	private boolean showContextMenu;
	private boolean downFlinging;
	private boolean abortNextDoubleTapZoom;
	private MotionEvent wastedEvent;
	private static boolean stdFling = !Utils.littleCake;
	private GestureDetector.SimpleOnGestureListener flinglistener;
	public boolean hasNoPermission;
	private boolean ignoreNxtClick;
	
	public int getCurrentPageOnScreen() {
		return lastMiddlePage;
	}
	
	public PDFPageParms getCurrentPageParmsOnScreen() {
		int pageIdx = getCurrentPageOnScreen();
		PDocument.PDocPage page = pdoc.mPDocPages[pageIdx];
		int offsetX, offsetY;
		offsetX = (int) (vTranslate.x/scale - page.getLateralOffset());
		offsetY = (int) (-vTranslate.y/scale - page.OffsetAlongScrollAxis);
		return new PDFPageParms(pageIdx, offsetX, offsetY, scale);
	}
	
	public int getPageCount() {
		return pdoc==null?0:pdoc._num_entries;
	}
	
	public boolean tryClearSelection() {
		if(draggingHandle==null && hasSelection || hasAnnotSelction) {
			clearSelection();
			return true;
		}
		return false;
	}
	
	public void navigateTo(PDFPageParms pageParms, boolean invalid) {
		CMN.Log("navigateTo", pageParms);
		int pageIdx = Math.max(0, Math.min(pageParms.pageIdx, pdoc._num_entries));
		PDocument.PDocPage page = pdoc.mPDocPages[pageIdx];
		float newScale = pageParms.scale;
		if(newScale>0) {
			newScale = Math.min(Math.max(currentMinScale(), newScale), maxScale);
			scale = newScale;
		}
		vTranslate.set((page.getLateralOffset()+pageParms.offsetX)*scale, -(page.OffsetAlongScrollAxis+pageParms.offsetY)*scale);
		if(invalid) {
			refreshRequiredTiles(true);
		}
	}
	
	public int pages() {
		return pdoc==null?0:pdoc._num_entries;
	}
	
	public void setOnPageChangeListener(OnPageChangedListener onPageChangeListener) {
		mOnPageChangeListener = onPageChangeListener;
	}
	
	/** Navigate to certain page. Inspired by the ezpdfreader. */
	public void goToPageCentered(int position, boolean still) {
		if(pdoc!=null && position!=lastMiddlePage && position>=0 && position<pdoc._num_entries) {
			OnPageChangedListener tmpPCL = null;
			if(still) {
				tmpPCL = mOnPageChangeListener;
				mOnPageChangeListener = null;
			}
			boolean horizon = pdoc.isHorizontalView();
			float SO = horizon?stoffX:stoff;
			SO = SO-pdoc.mPDocPages[lastMiddlePage].OffsetAlongScrollAxis+pdoc.mPDocPages[position].OffsetAlongScrollAxis;
			SO*=-scale;
			if(horizon) {
				vTranslate.x = SO;
			} else {
				vTranslate.y = SO;
			}
			if(!refreshRequiredTiles(true)) {
				invalidate();
			}
			if(still) {
				mOnPageChangeListener = tmpPCL;
			}
			if(shouldDrawSelection()||selectionPaintView.searchCtx!=null) {
				redrawSel();
			}
		}
	}
	
	public interface ImageReadyListener {
		void ImageReady();
		PDocBookInfo onDocOpened(PDocView view, Uri url);
		void NewDocOpened();
		void saveBookInfo(PDocBookInfo bookInfo);
	}
	
	ImageReadyListener mImageReadyListener;
	
	ArrayList<PDocument.AnnotShape> mAnnotBucket = new ArrayList<>(8);
	
	private Runnable flingRunnable = new Runnable() {
		@Override
		public void run() {
			if(flingScroller.computeScrollOffset()) {
				int cfx = flingScroller.getCurrX();
				int cfy = flingScroller.getCurrY();
				//CMN.Log("fling...", cfx - mLastFlingX, cfy - mLastFlingY, flingScroller.getCurrVelocity());
				
				float x = cfx - mLastFlingX;
				float y = cfy - mLastFlingY;
				
				mLastFlingX = cfx;
				mLastFlingY = cfy;
				
				int flag;
				
				if(pdoc.isHorizontalView()) {
					vTranslate.x = vTranslate.x+(flingVx>0?x:-x);
					if(freefling)
						vTranslate.y = vTranslate.y+(flingVy>0?y:-y);
				} else {
					if(freefling)
						vTranslate.x = vTranslate.x+(flingVx>0?x:-x);// fingStartX + cfx-flingScroller.getStartX();
					vTranslate.y = vTranslate.y+(flingVy>0?y:-y);// fingStartY + cfy-flingScroller.getStartY();
				}
				
				//if(isProxy)
				if(freefling)
					handle_proxy_simul(scale, null, rotation);
				
				if(!isProxy && freefling){
					refreshRequiredTiles(true); // flingScroller.getCurrVelocity()<2500
					invalidate();
					//postInvalidate();
				}
				if(!stdFling) {
					post(this);
				}
			} else {
				isFlinging = false;
				if(freefling && shouldDrawSelection()) {
					relocateContextMenuView();
				}
			}
			//else scroll ended
		}
	};
	private float fingStartX;
	private float fingStartY;
	private float flingVx;
	private float flingVy;
	private boolean waitingNextTouchResume;
	private PointF quickScalesStart;
	private long lastDrawTime;
	public static ColorFilter sample_fileter = new ColorMatrixColorFilter(new float[]{
			0.310f, 0.000f, 0.690f, 0.000f, 0.139f,
			0.310f, 0.172f, 0.000f, 0.000f, 0.139f,
			0.448f, 0.000f, -0.052f, 0.000f, 0.139f,
			0.000f, 0.000f, 0.000f, 1.000f, 0.139f,
	});
	private float vtParms_sew;
	private float vtParms_seh;
	private float vtParms_se_delta;
	private float vtParms_se_SWidth;
	private int vtParms_dragDir;
	private int vtParms_dir;
	private boolean vtParms_b1;
	private boolean vtParms_b2;
	private boolean vtParms_b3;
	private double vtParms_cos = Math.cos(0);
	private double vtParms_sin = Math.sin(0);
	private boolean isDown;
	private long stoff;
	private float edoff;
	private long stoffX;
	private float edoffX;
	private RectF hrcTmp=new RectF();
	long draw_stoff;
	float draw_edoff;
	long draw_stoffX;
	float draw_edoffX;
	final Rect vRect = new Rect();
	int selPageSt=-1;
	int selPageEd;
	int selStart;
	int selEnd;
	boolean hasSelection;
	boolean hasAnnotSelction;
	
	PDocument.PDocPage annotSelPage;
	PDocument.AnnotShape annotSelection;
	RectF annotSelRect = new RectF();
	int annotSelIdx;
	
	float drawableScale=1.f;
	final RectF handleLeftPos=new RectF();
	final RectF handleRightPos=new RectF();
	Drawable handleLeft;
	Drawable handleRight;
	Drawable draggingHandle;
	private boolean startInDrag;
	float lineHeight;
	float lineHeightLeft;
	float lineHeightRight;
	PointF sCursorPosStart = new PointF();
	PointF sCursorPos = new PointF();
	private View contextView;
	private boolean bSupressingUpdatCtxMenu;
	private int selAnnotSt;
	private int selAnnotEd;
	
	public void dragHandle() {
		if(draggingHandle!=null) {
			lineHeight = draggingHandle==handleLeft?lineHeightLeft:lineHeightRight;
			//float posX = (lastX - vTranslate.x)/scale;
			//float posY = (lastY - vTranslate.y)/scale;
			float posX = sCursorPosStart.x+(lastX - orgX)/scale;
			float posY = sCursorPosStart.y+(lastY - orgY)/scale;
			sCursorPos.set(posX, posY);
			boolean isLeft = draggingHandle==handleLeft;
			int charIdx=-1; int pageIdx=-1;
			//if(false)
			boolean horizon = pdoc.isHorizontalView();
			for (int i = 0; i < logiLayoutSz; i++) {
				PDocument.PDocPage pageI = pdoc.mPDocPages[logiLayoutSt + i];
				if(!horizon&&pageI.OffsetAlongScrollAxis+pageI.size.getHeight()+pdoc.gap>posY
					||horizon&&pageI.OffsetAlongScrollAxis+pageI.size.getWidth()+pdoc.gap>posX) {
					pageIdx = logiLayoutSt+i;
					if(horizon) {
						posX -= pageI.OffsetAlongScrollAxis;
						posY -= pageI.getLateralOffset();
					} else {
						posY -= pageI.OffsetAlongScrollAxis;
						posX -= pageI.getLateralOffset();
					}
					charIdx = pageI.getCharIdxAtPos(posX, posY-lineHeight);
					break;
				}
			}
			
			selectionPaintView.supressRecalcInval=true;
			if(charIdx>=0) {
				if(isLeft){
					if(pageIdx!=selPageSt||charIdx!=selStart) {
						selPageSt=pageIdx;
						selStart=charIdx;
						selectionPaintView.resetSel();
					}
				} else {
					charIdx+=1;
					if(pageIdx!=selPageEd||charIdx!=selEnd) {
						selPageEd=pageIdx;
						selEnd=charIdx;
						selectionPaintView.resetSel();
					}
				}
			}
			redrawSel();
			selectionPaintView.supressRecalcInval=false;
		}
	}
	
	//构造
	public PDocView(Context context) {
		this(context, null);
	}
	
	public PDocView(Context context, AttributeSet attr) {
		super(context, attr);
		density = getResources().getDisplayMetrics().density;
		setGestureDetector(context);
		createPaints();
		this.handler = new Handler(new Handler.Callback() {
			public boolean handleMessage(Message message) {
				//CMN.Log("长按了");
				if(message.what == MESSAGE_LONG_CLICK) {
					float posX = (lastX - vTranslate.x)/scale;
					float posY = (lastY - vTranslate.y)/scale;
					PDocument.PDocPage pageI = getPageAtSrcPos(posX, posY);
					if(pageI!=null) {
						if(pdoc.isHorizontalView()) {
							posX -= pageI.OffsetAlongScrollAxis;
							posY -= pageI.getLateralOffset();
						} else {
							posY -= pageI.OffsetAlongScrollAxis;
							posX -= pageI.getLateralOffset();
						}
						if(pageI.selWordAtPos(PDocView.this, posX, posY, 5)) {
							draggingHandle = handleRight;
							sCursorPosStart.set(handleRightPos.right, handleRightPos.bottom);
						}
					}
					
					if (onLongClickListener != null) {
						//maxTouchCount = 0;
						PDocView.super.setOnLongClickListener(onLongClickListener);
						performLongClick();
						PDocView.super.setOnLongClickListener(null);
					}
				}
				return true;
			}
		});
		quickScaleThreshold = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
		
		handleLeft=getResources().getDrawable(R.drawable.abc_text_select_handle_left_mtrl_dark);
		handleRight=getResources().getDrawable(R.drawable.abc_text_select_handle_right_mtrl_dark);
		if(GlobalOptions.isLarge){
			drawableScale=1.5f;
		}
		ColorFilter colorFilter = new PorterDuffColorFilter(0xaa309afe, PorterDuff.Mode.SRC_IN);
		handleLeft.setColorFilter(colorFilter);
		handleRight.setColorFilter(colorFilter);
		handleLeft.setAlpha(200);
		handleRight.setAlpha(200);
	}
	
	
	/**
	 * Get the current preferred configuration for decoding bitmaps. {@link ImageDecoder} and {@link ImageRegionDecoder}
	 * instances can read this and use it when decoding images.
	 * @return the preferred bitmap configuration, or null if none has been set.
	 */
	public static Bitmap.Config getPreferredBitmapConfig() {
		return preferredBitmapConfig;
	}
	
	/**
	 * Set a global preferred bitmap config shared by all view instances and applied to new instances
	 * initialised after the call is made. This is a hint only; the bundled {@link ImageDecoder} and
	 * {@link ImageRegionDecoder} classes all respect this (except when they were constructed with
	 * an instance-specific config) but custom decoder classes will not.
	 * @param preferredBitmapConfig the bitmap configuration to be used by future instances of the view. Pass null to restore the default.
	 */
	public static void setPreferredBitmapConfig(Bitmap.Config preferredBitmapConfig) {
		PDocView.preferredBitmapConfig = preferredBitmapConfig;
	}
	
	/**
	 * Sets the image orientation. It's best to call this before setting the image file or asset, because it may waste
	 * loading of tiles. However, this can be freely called at any time.
	 * @param orientation orientation to be set. See ORIENTATION_ static fields for valid values.
	 */
	public final void setOrientation(int orientation) {
		if(orientation>=0&&orientation<=270 && orientation%90==0){
			this.orientation = orientation;
			//reset(false);
			invalidate();
			//requestLayout();
		}
	}
	
	public PDocSelection selectionPaintView;
	
	public void setSelectionAtPage(int pageIdx, int st, int ed) {
		selPageSt=pageIdx;
		selPageEd=pageIdx;
		selStart=st;
		selEnd=ed;
		hasSelection =true;
		selectionPaintView.resetSel();
	}
	
	public void setSelectionPaintView(PDocSelection sv) {
		selectionPaintView = sv;
		sv.pDocView = this;
		sv.resetSel();
		sv.drawableWidth = handleLeft.getIntrinsicWidth()*drawableScale;
		sv.drawableHeight = handleLeft.getIntrinsicHeight()*drawableScale;
		sv.drawableDeltaW = sv.drawableWidth / 4;
	}
	
	public boolean hasSelection() {
		return hasSelection;
	}
	
	public boolean shouldDrawSelection() {
		return hasSelection ||hasAnnotSelction;
	}
	
	public void clearSelection() {
		hasSelection =false;
		hasAnnotSelction=false;
		redrawSel();
		hideContextMenuView();
	}
	
	public void setAnnotSelection(PDocument.PDocPage page, PDocument.AnnotShape annot) {
		if(hasSelection) {
			clearSelection();
		}
		annotSelPage = page;
		annotSelIdx = annot.index;
		annotSelection = annot;
		RectF rect = annotSelRect;
		rect.set(annot.box);
		int offset1, offset2;
		if(pdoc.isHorizontalView()) {
			offset1 = page.getLateralOffset();
			offset2 = (int) page.OffsetAlongScrollAxis;
		} else {
			offset1 = (int) page.OffsetAlongScrollAxis;
			offset2 = page.getLateralOffset();
		}
		rect.left+=offset2;
		rect.right+=offset2;
		rect.top+=offset1;
		rect.bottom+=offset1;
		hasAnnotSelction = true;
		redrawSel();
	}
	
	private void redrawSel() {
		if(selectionPaintView!=null) {
			selectionPaintView.invalidate();
		}
	}
	
	public void setContextMenuView(View contextView) {
		this.contextView = contextView;
	}
	
	public String getSelection() {
		if(shouldDrawSelection() && selectionPaintView!=null) {
			try {
				if(hasSelection) {
					int pageStart = selectionPaintView.selPageSt;
					int pageCount = selectionPaintView.selPageEd-pageStart;
					if(pageCount==0) {
						PDocument.PDocPage page = pdoc.mPDocPages[pageStart];
						page.prepareText();
						return page.allText.substring(selStart, selEnd);
					}
					StringBuilder sb = new StringBuilder();
					int selCount=0;
					for (int i = 0; i <= pageCount; i++) {
						PDocument.PDocPage page = pdoc.mPDocPages[pageStart+i];
						page.prepareText();
						int len = page.allText.length();
						selCount+=i==0?len-selStart:i==pageCount?selEnd:len;
					}
					sb.ensureCapacity(selCount+64);
					for (int i = 0; i <= pageCount; i++) {
						PDocument.PDocPage page = pdoc.mPDocPages[pageStart+i];
						sb.append(page.allText.substring(i==0?selStart:0, i==pageCount?selEnd:page.allText.length()));
					}
					return sb.toString();
				} else {
					expandAnnotToTextSelection();
					if(annotSelectionValid()) {
						return annotSelPage.allText.substring(selAnnotSt, selAnnotEd);
					}
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
		return null;
	}
	
	public void highlightSelection() {
		if(hasSelection && selPageSt==selPageEd && selectionPaintView!=null) {
			ArrayList<RectF> selRects = selectionPaintView.rectPool.get(0);
			PDocument.PDocPage page = pdoc.mPDocPages[selPageSt];
			if(selRects.size()>0) { //sanity check
				RectF box = new RectF(selRects.get(0));
				ArrayList<RectF> selLineRects = page.mergeLineRects(selRects, box);
				page.createHighlight(this, 0xffffff00, selStart, selEnd, box, selLineRects);
				invalidateTiles(page, box);
				clearSelection();
				pdoc.isDirty=true;
			}
		}
	}
	
	private void invalidateTiles(PDocument.PDocPage page, RectF box) {
		if(page.tile!=null) {
			page.tile.reset();
			page.tileBk=page.tile;
		}
		for(RegionTile rtI:regionTiles) {
			if(rtI!=null && rtI.currentOwner!=null && isRectOverlap(rtI.sRect, box)) {
				rtI.reset();
				CMN.Log("rtI.reset()");
			}
		}
		refreshRequiredTiles(true);
		//postInvalidate();
	}
	
	boolean isRectOverlap(RectF rc1, RectF rc2)
	{
		return rc1.right > rc2.left &&
				rc2.right > rc1.left &&
				rc1.bottom > rc2.top &&
				rc2.bottom > rc1.top;
	}
	
	/** Enlarge or expand text selection. <br/>
	 *  If only a highlight annotation is selected, it will be converted to the corresponding text selection.  <br/>*/
	public void enlargeSelection(boolean baseStart) {
		if(hasAnnotSelction) {
			expandAnnotToTextSelection();
			//CMN.Log("enlargeSelection", charSt, charEd);
			if(annotSelectionValid()) {
				bSupressingUpdatCtxMenu=true;
				clearSelection();
				setSelectionAtPage(annotSelPage.pageIdx, selAnnotSt, selAnnotEd);
				bSupressingUpdatCtxMenu=false;
			}
		} else {
			if(!hasSelection && selPageSt!=-1) {
				hasSelection = true;
				selEnd = selStart+1;
			}
			if(hasSelection) {
				if(baseStart) {
					//@hide(9)
					selPageEd = selPageSt;
					PDocument.PDocPage pageSt = pdoc.mPDocPages[selPageSt];
					if(selStart+1<pageSt.allText.length()) {
						selStart++;
					}
					if(selStart+1<pageSt.allText.length()) {
						selStart++;
					}
					selEnd = selStart+1;
				}
				int d=selPageEd-selPageSt;
				boolean reversed=d<0||d==0&&selStart>selEnd;
				selStart = trimSelToMargin(pdoc.mPDocPages[selPageSt], selStart, reversed);
				selEnd = trimSelToMargin(pdoc.mPDocPages[selPageEd], selEnd, !reversed);
				selectionPaintView.resetSel();
				relocateContextMenuView();
			}
		}
	}
	
	private boolean annotSelectionValid() {
		return selAnnotSt >= 0 && selAnnotSt < selAnnotEd;
	}
	
	private void expandAnnotToTextSelection() {
		annotSelPage.fetchAnnotAttachPoints(annotSelection);
		annotSelPage.prepareText();
		selAnnotSt=annotSelPage.allText.length();
		selAnnotEd=0;
		int len=annotSelection.attachPts.length;
		if(len>0) {
			PointF p = new PointF();
			int charIdx;
			for (int i = 0; i < len; i++) {
				PDocument.QuadShape qI = annotSelection.attachPts[i];
				setCenterPoint(p, qI.p1, qI.p3);
				charIdx = annotSelPage.getCharIdxAtPos(p.x, p.y);
				if(charIdx>=0) {
					selAnnotSt = Math.min(selAnnotSt, charIdx);
				}
				setCenterPoint(p, qI.p2, qI.p4);
				charIdx = annotSelPage.getCharIdxAtPos(p.x, p.y);
				if(charIdx>=0) {
					selAnnotEd = Math.max(selAnnotEd, charIdx+1);
				}
			}
		} else {
			RectF rect = annotSelection.box;
			selAnnotSt = annotSelPage.getCharIdxAtPos(rect.left, rect.top);
			selAnnotEd = annotSelPage.getCharIdxAtPos(rect.right, rect.bottom);
		}
	}
	
	private int trimSelToMargin(PDocument.PDocPage page, int charIdx, boolean reversed) {
		page.prepareText();
		char charAt;
		int cc=0;
		boolean search_started=false;
		String atext = page.allText;
		if(reversed) {
			while(cc<1024&&charIdx<atext.length()) {
				charAt=atext.charAt(charIdx-1);
				if(charAt=='\r'||charAt=='\n') {
					if(!search_started) {
						charIdx++;
						continue;
					}
					int SST = charIdx + 1;
					while(SST-1>0&&isEmptyChar(charAt=page.allText.charAt(SST-1))) {
						SST--;
					}
					if(isParagraphSepChar(charAt)) {
						break;
					}
					//charIdx=SST;
					charIdx++;
					//search_started=false;
				} else {
					if(!search_started) {
						search_started=true;
					}
					charIdx++;
				}
				cc++;
			}
		} else {
			while(cc<1024&&charIdx>0) {
				charAt=page.allText.charAt(charIdx-1);
				if(charAt=='\r'||charAt=='\n') {
					if(!search_started) {
						charIdx--;
						continue;
					}
					int SST = charIdx - 1;
					while(SST-1>0&&isEmptyChar(charAt=page.allText.charAt(SST-1))) {
						SST--;
					}
					if(SST>=0&&isParagraphSepChar(charAt)) {
						break;
					}
					charIdx=SST;
					//charIdx--;
					//search_started=false;
				} else {
					if(!search_started) {
						search_started=true;
					}
					charIdx--;
				}
				cc++;
			}
		}
		return charIdx;
	}
	
	private boolean isEmptyChar(char charAt) {
		return charAt<=' ';
	}
	
	final static char[] paragraphSepChars = new char[]{'.', '。', '!', '！'/*, '\'', '”'*/, '?', '？', '”', '“', ':', '：', '\''};
	
	private boolean isParagraphSepChar(char charAt) {
		//CMN.Log("isParagraphSepChar : "+charAt, charAt=='\r', charAt=='\n', charAt==' ');
		for (char paragraphSepChar : paragraphSepChars) {
			if (paragraphSepChar == charAt) {
				return true;
			}
		}
		return false;
	}
	
	private void setCenterPoint(PointF p, PointF p1, PointF p2) {
		p.set((p1.x+p2.x)/2, (p1.y+p2.y)/2);
	}
	
	public void setImageReadyListener(ImageReadyListener imageReadyListener) {
		mImageReadyListener = imageReadyListener;
	}
	
	/** Stores all loaded files in a static map. */
	public final static ConcurrentHashMap<String, PDocument> books = new ConcurrentHashMap<>(12);
	
	/** Async loading of one pdf file. */
	private static class TilesInitTask implements Runnable {
		private final Uri url;
		private final AtomicBoolean abort = new AtomicBoolean();
		private final AtomicBoolean finished = new AtomicBoolean();
		private final WeakReference<PDocView> viewRef;
		private Thread t;
		private PDocument result;
		
		TilesInitTask(PDocView view, Uri url) {
			this.url = url;
			viewRef = new WeakReference<>(view);
		}
		
		@Override
		public void run() {
			final PDocView view = viewRef.get();
			if (view != null) {
				if(finished.get()) {
					view.setDocument(result);
					//result.isDirty=true; //debug save
					view.selPageSt=-1;
					if(!abort.get() && view.loadingTask==this) {
						view.onTileInited();
						view.loadingTask=null;
					}
				} else {
					try {
						String path = Utils.getRunTimePath(url);
						boolean responsibleForThisBook=false;
						PDocument doc = books.get(path);
						if(doc==null) {
							doc = new PDocument(view.getContext().getContentResolver(), url, view.dm, Looper.myLooper() == Looper.getMainLooper() ? null : abort);
							responsibleForThisBook=true;
						}
						if(!abort.get()) {
							finished.set(true);
							result = doc;
							if(responsibleForThisBook) {
								books.put(path, doc);
								if(view.mImageReadyListener!=null) {
									// pre-fetch the bookInfo so that it could be used.
									PDocBookInfo bookInfo = view.mImageReadyListener.onDocOpened(view, url);
									doc.setBookInfo(bookInfo);
								}
							} else {
								doc.referenceCount.incrementAndGet();
							}
							view.post(this);
						} else if(responsibleForThisBook) {
							doc.close();
						}
					} catch (Exception e) {
						CMN.Log(e);
					}
				}
			}
		}
		
		public void start() {
			if(t==null) {
				t=new Thread(this, "PDocInit");
				t.start();
			}
		}
		
		public void abort() {
			if(!finished.get()) {
				abort.set(true);
			}
		}
	}
	
	/**
	 * Reset all state before setting/changing image or setting new rotation.
	 */
	private void reset(boolean newImage) {
		debug("reset newImage=" + newImage);
		pendingScale = 0f;
		isZooming = false;
		isPanning = false;
		isQuickScaling = false;
		maxTouchCount = 0;
		vDistStart = 0;
		quickScaleLastDistance = 0f;
		quickScaleMoved = false;
		if (newImage) {
			uri = null;
			sWidth = 0;
			sHeight = 0;
			sOrientation = 0;
			readySent = false;
			imageLoading = true;
		}
	}

	public void refresh() {
		pendingScale = 0f;
		sPendingCenter = null;
		sRequestedCenter = null;
		isZooming = false;
		isPanning = false;
		isQuickScaling = false;
		maxTouchCount = 0;
		vDistStart = 0;
		quickScaleLastDistance = 0f;
		quickScaleMoved = false;
		quickScaleSCenter = null;
		quickScaleVLastPoint = null;
		quickScaleVStart = null;
		anim = null;
		sRect = null;
	}
	
	int mLastFlingX;
	int mLastFlingY;
	private long wastedClickTime;
	private boolean onFlingDetected;
	private int MAX_FLING_OVER_SCROLL = (int) (30*getContext().getResources().getDisplayMetrics().density);
	@SuppressWarnings("SuspiciousNameCombination")
	private void setGestureDetector(final Context context) {
		this.flingdetector = new GestureDetector(context, flinglistener=new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				CMN.Log("onFling");
				onFlingDetected =true;
				if(isQuickScaling||scale < minScale() || scale>maxScale || draggingHandle!=null){
					return true;
				}
				//if(false)
				if (panEnabled && (readySent||isProxy)
						//&& (Math.abs(e1.getX() - e2.getX()) > 50 || Math.abs(e1.getY() - e2.getY()) > 50)
						//&& (Math.abs(velocityX) > 500 || Math.abs(velocityY) > 500)
						&& !isZooming) {
					
					float vxDelta = vTranslate.x;
					float vyDelta = vTranslate.y;

					float scaleWidth = scale * sWidth;
					float scaleHeight = scale * sHeight;
					float screenWidth = getScreenWidth();
					float screenHeight = getScreenHeight();

					float widthDelta;
					float heightDelta;
					float minX,maxX,minY,maxY;
					
					if(vtParms_dir==1){
						widthDelta=screenWidth-scaleWidth;
						heightDelta=screenHeight-scaleHeight;
						minX = screenWidth-scaleWidth;
						maxX = 0;
						if(minX>0) minX=maxX=minX/2;
						
						minY = screenHeight-scaleHeight;
						maxY = 0;
						if(minY>0) minY=maxY=minY/2;
						CMN.Log("x限制·1", minX, maxX, minY, maxY);
					} else {
						widthDelta=screenWidth-scaleHeight;
						heightDelta=screenHeight-scaleWidth;
						minX = heightDelta;
						maxX = 0;
						if(minX>0) maxX=minX=minX/2;
						minX -= vtParms_se_delta*vtParms_dir;
						maxX -= vtParms_se_delta*vtParms_dir;
						
						minY = widthDelta;
						maxY = 0;
						if(minY>0) minY=maxY=minY/2;
						minY += vtParms_se_delta*vtParms_dir;
						maxY += vtParms_se_delta*vtParms_dir;
						CMN.Log("x限制·2", minX, maxX, minY, maxY);
					}
					
					int distanceX = 0;
					int distanceY = 0;
					
					float vX = (float) (velocityX * cos - velocityY * -sin);
					float vY = (float) (velocityX * -sin + velocityY * cos);
					if(vxDelta<=minX || vxDelta>=maxX){
						vX = 0;
					} else {
						distanceX = Math.abs((int) (vX>0?(maxX-vxDelta):(vxDelta-minX)));
					}
					if(vyDelta<=minY || vyDelta>=maxY){
						vY = 0;
					} else {
						distanceY = Math.abs((int) (vY>0?(maxY-vyDelta):(vyDelta-minY)));
					}
					if(pdoc.isHorizontalView()?vX!=0:vY!=0) {
						// Account for rotation
						boolean SameDir = Math.signum(vX) == Math.signum(flingVx) && Math.signum(vY) == Math.signum(flingVy);

						flingVx = vX;
						flingVy = vY;
						
						if(vtParms_dir==1) {
							distanceX = minX >= 0 ? 0 : (int) (vX > 0 ? Math.abs(vxDelta) : vxDelta - widthDelta);
							distanceY = minY >= 0 ? 0 : (int) (vY > 0 ? Math.abs(vyDelta) : vyDelta - heightDelta);
						}
						CMN.Log("fling 初始参数(vx, vy, distX, distY)", flingVx, flingVy, distanceX, distanceY);
						
						vX = Math.abs(vX);
						vY = Math.abs(vY);

						if (vX==0 || distanceX < 0) distanceX = 0;
						if (vY==0 || distanceY < 0) distanceY = 0;

						int overX = distanceX < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL;
						int overY = distanceY < MAX_FLING_OVER_SCROLL * 2 ? 0 : MAX_FLING_OVER_SCROLL;

						if (velocityX == 0) distanceX = 0;
						if (velocityY == 0) distanceY = 0;

						mLastFlingY = mLastFlingX = 0;
						CMN.Log("fling 最终参数(vx, vy, distX, distY)", flingVx, flingVy, distanceX, distanceY);
						
						flingScroller.fling(mLastFlingX, mLastFlingY, (int) vX, (int) vY,
								0, distanceX, 0, distanceY, overX, overY, SameDir);
						isFlinging = true;
						if(!stdFling) {
							post(flingRunnable);
						}
						return false;
					}
				}
				return super.onFling(e1, e2, velocityX, velocityY);
			}
			
			@Override
			public boolean onSingleTapConfirmed(MotionEvent e) {
				if(!ignoreNxtClick) {
					performClick();
					return true;
				}
				return false;
			}
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if(draggingHandle!=null) {
					ignoreNxtClick = true;
					return true;
				}
				//performClick();
				float posX = (lastX - vTranslate.x)/scale;
				float posY = (lastY - vTranslate.y)/scale;
				
				PDocument.PDocPage pageI = getPageAtSrcPos(posX, posY);
				
				if(pageI!=null) {
					if(pdoc.isHorizontalView()) {
						posX -= pageI.OffsetAlongScrollAxis;
						posY -= pageI.getLateralOffset();
					} else {
						posY -= pageI.OffsetAlongScrollAxis;
						posX -= pageI.getLateralOffset();
					}
					
					if(false) {
						long lnkPtr = pageI.getLinkAtPos(posX, posY);
						if(lnkPtr!=0) {
							String lnkTgt = pageI.getLinkTarget(lnkPtr);
							//a.showT("LINK::"+lnkTgt);
							ignoreNxtClick = true;
							return true;
						}
					}
					
					// disable single click selecting if last touch participate in quick zooming
					if(quickScaleMoved) {
						return true;
					}
					
					// disable single click selecting if was flinging
					if(downFlinging) {
						wastedClickTime = e.getDownTime();
						wastedEvent = e;
						ignoreNxtClick = true;
						return true;
					}
					
					boolean singleTapSel = true;
					if(opt.getSingleTapClearSel() && shouldDrawSelection()) {
						clearSelection();
						singleTapSel = false;
						wastedClickTime = e.getDownTime();
						wastedEvent = e;
						ignoreNxtClick = true;
					}
					
					if(true /*&& singleTapSel*/) {
						// select annotation
						PDocument.AnnotShape annot = pageI.selAnnotAtPos(PDocView.this, posX, posY);
						if(annot!=null) {
							//a.showT("annotIdx::"+annotIdx);
							doRelocateContextMenuView();
							ignoreNxtClick = true;
							return true;
						}
					}
					
					if(opt.getSingleTapSelWord() && singleTapSel) {
						// select text
						if(!pageI.selWordAtPos(PDocView.this, posX, posY, 1.5f)) {
							clearSelection();
						} else {
							doRelocateContextMenuView();
							ignoreNxtClick = true;
						}
						return true;
					}
				}
				
				return super.onSingleTapUp(e);
			}
			
			@Override
			public boolean onDoubleTapEvent(MotionEvent e) {
				// remove this to enable quickScaling after dismissed selection.
				//if(!abortNextDoubleTapZoom)
				if(treatNxtUpAsSingle && e.getActionMasked()==MotionEvent.ACTION_UP) {
					onSingleTapUp(e);
				}
				if(abortNextDoubleTapZoom||wastedClickTime!=0) {
					if(e.getDownTime()-wastedClickTime<350) {
						treatNxtUpAsSingle = true;
						abortNextDoubleTapZoom=true;
						//return true;
					} else {
						wastedClickTime = 0;
						treatNxtUpAsSingle = false;
						abortNextDoubleTapZoom=false;
					}
				}
				// remove this to enable quickScaling after dismissed selection.
				if(abortNextDoubleTapZoom) {
					if(e.getActionMasked()==MotionEvent.ACTION_DOWN)
					treatNxtUpAsSingle = true;
					setGestureDetector(getContext());
					return false;
				}
				if (zoomEnabled && (readySent||isProxy) && e.getActionMasked()==MotionEvent.ACTION_DOWN) {
					CMN.Log("kiam 双击");
					//setGestureDetector(context);
					doubleTapDetected=true;
					//doubleTapFocus.set(e.getX(), e.getY());
					doubleTapFocus.set(vCenterStart.x, vCenterStart.y);
					// Store quick scale params. This will become either a double tap zoom or a
					// quick scale depending on whether the user swipes.
					//vCenterStart = new PointF(e.getX(), e.getY());
					//vTranslateStart = new PointF(vTranslate.x, vTranslate.y);
					scaleStart = scale;
					isQuickScaling = true;
					isZooming = true;
					quickScaleLastDistance = -1F;
					quickScaleStart = scale;
					//
					quickScaleSCenter = viewToSourceCoord(vCenterStart);
					quickScaleVStart = new PointF(vCenterStart.x, vCenterStart.y);
					quickScaleVLastPoint = new PointF(quickScaleSCenter.x, quickScaleSCenter.y);
					quickScaleMoved = false;
					// We need to get events in onTouchEvent after this.
					return false;
				}
				return true;
			}
			
		});
		
		flingdetector.setIsLongpressEnabled(false);
	}
	
	private PDocument.PDocPage getPageAtSrcPos(float posX, float posY) {
		boolean horizon = pdoc.isHorizontalView();
		for (int i = 0; i < logiLayoutSz; i++) {
			PDocument.PDocPage pageI = pdoc.mPDocPages[logiLayoutSt + i];
			if(!horizon&&pageI.OffsetAlongScrollAxis+pageI.size.getHeight()+pdoc.gap>posY
				||horizon&&pageI.OffsetAlongScrollAxis+pageI.size.getWidth()+pdoc.gap>posX) {
				return pageI;
			}
		}
		return null;
	}
	
	/**
	 * On resize, preserve center and scale. Various behaviours are possible, override this method to use another.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		debug("onSizeChanged %dx%d -> %dx%d", oldw, oldh, w, h);
		PointF sCenter = getCenter();
		if (readySent && sCenter != null) {
			this.anim = null;
			this.pendingScale = scale;
			this.sPendingCenter = sCenter;
		}
	}
	
//	/**
//	 * Measures the width and height of the view, preserving the aspect ratio of the image displayed if wrap_content is
//	 * used. The image will scale within this box, not resizing the view as it is zoomed.
//	 */
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
//		int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
//		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
//		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
//		boolean resizeWidth = widthSpecMode != MeasureSpec.EXACTLY;
//		boolean resizeHeight = heightSpecMode != MeasureSpec.EXACTLY;
//		int width = parentWidth;
//		int height = parentHeight;
//		if (sWidth > 0 && sHeight > 0) {
//			if (resizeWidth && resizeHeight) {
//				width = sWidth();
//				height = sHeight();
//			} else if (resizeHeight) {
//				height = (int)((((double)sHeight()/(double)sWidth()) * width));
//			} else if (resizeWidth) {
//				width = (int)((((double)sWidth()/(double)sHeight()) * height));
//			}
//		}
//		width = Math.max(width, getSuggestedMinimumWidth());
//		height = Math.max(height, getSuggestedMinimumHeight());
//		setMeasuredDimension(width, height);
//	}
	
	/**
	 * Handle touch events. One finger pans, and two finger pinch and zoom plus panning.
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		//CMN.Log("onTouchEvent");
		// During non-interruptible anims, ignore all touch events
		if(pdoc==null) {
			return true;
		}
		int touch_type = event.getAction() & MotionEvent.ACTION_MASK;
		boolean isDown = touch_type==MotionEvent.ACTION_DOWN||touch_type==MotionEvent.ACTION_POINTER_DOWN;
		if(waitingNextTouchResume){
			if(!isDown){
				return true;
			}
			waitingNextTouchResume=false;
			//从善如流
			touch_partisheet.add(0);
		}
		
		if(isDown) {
			isQuickScaling=quickScaleMoved=false;
			if(touch_type==MotionEvent.ACTION_DOWN){
				vCenterStart.set(event.getX(), event.getY());
			}
			startTouchWithAnimation = anim != null;
		}
		
		
		// Abort if not ready
		// Detect flings, taps and double taps
		onFlingDetected =false;
		boolean flingEvent = flingdetector.onTouchEvent(event);
		//if(!(onflingdetected && scale <= minScale())){
		//	if (!isQuickScaling && flingEvent) {
		//		isZooming = false;
		//		isPanning = false;
		//		maxTouchCount = 0;
		//		return true;
		//	}
		//}
		
		if (anim != null){
			if(!anim.interruptible) {
				//requestDisallowInterceptTouchEvent(true);
				return true;
			} else {
				anim = null;
			}
		}
		
		// Store current values so we can send an event if they change
		float scaleBefore = scale;
		vTranslateBefore.set(vTranslate);
		float rotationBefore = rotation;
		
		boolean handled = onTouchEventInternal(event);
		//sendStateChanged(scaleBefore, vTranslateBefore, rotationBefore, ORIGIN_TOUCH);
		return handled||super.onTouchEvent(event);
	}
	
	HashSet<Integer> touch_partisheet = new HashSet<>();
	boolean is_classical_pan_shrinking=false;
	PointF tmpCenter = new PointF();
	private boolean onTouchEventInternal(@NonNull MotionEvent event) {
		int touchCount = event.getPointerCount();
		int touch_type = event.getAction() & MotionEvent.ACTION_MASK;
		int touch_id = event.getPointerId(event.getActionIndex());
		lastX = event.getX();
		lastY = event.getY();
		switch (touch_type) {
			case MotionEvent.ACTION_DOWN:{
				ignoreNxtClick=false;
				isDown = true;
				touch_partisheet.clear();
				isRotating = false;
				orgX=view_pager_toguard_lastX=lastX;
				orgY=view_pager_toguard_lastY=lastY;
				if(hasSelection) {
					if (handleLeft.getBounds().contains((int) orgX, (int) orgY)) {
						startInDrag = true;
						draggingHandle = handleLeft;
						sCursorPosStart.set(handleLeftPos.left, handleLeftPos.bottom);
					} else if (handleRight.getBounds().contains((int) orgX, (int) orgY)) {
						startInDrag = true;
						draggingHandle = handleRight;
						sCursorPosStart.set(handleRightPos.right, handleRightPos.bottom);
					}
				}
				if(shouldDrawSelection() && (hideContextMenu||draggingHandle!=null)) {
					hideContextMenuView();
				}
				downFlinging = isFlinging;
			}
			case MotionEvent.ACTION_POINTER_DOWN:{
				CMN.Log("ACTION_DOWN", touchCount);
				doubleTapDetected=false;
				flingScroller.abortAnimation();
				if(draggingHandle!=null) {
					return true;
				}
				if(touch_partisheet.size()==0 && touch_type==MotionEvent.ACTION_POINTER_DOWN) {
					break;
				}
				int touch_seat_count = 2-touch_partisheet.size();
				for(int i=0;i<Math.min(touch_seat_count, touchCount);i++) {
					if(touch_partisheet.contains(event.getPointerId(i))) {
						touch_seat_count++;
					}else
						touch_partisheet.add(event.getPointerId(i));
				}
				
				anim = null;
				requestDisallowInterceptTouchEvent(true);
				maxTouchCount = Math.max(maxTouchCount, touchCount);
				vTranslateStart.set(vTranslate.x, vTranslate.y);
				if (touchCount >= 2) {
					// Start pinch to zoom. Calculate distance between touch points and center point of the pinch.
					float distance = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
					scaleStart = scale;
					vDistStart = distance;
					vCenterStart.set((event.getX(0) + event.getX(1))/2, (event.getY(0) + event.getY(1))/2);
					viewToSourceCoord(vCenterStart, sCenterStart);
					
					if (!zoomEnabled && !rotationEnabled) {
						// Abort all gestures on second touch
						maxTouchCount = 0;
					}
					
					if (rotationEnabled) {
						lastAngle = (float) Math.atan2((event.getY(0) - event.getY(1)), (event.getX(0) - event.getX(1)));
					}
					
					// Cancel long click timer
					handler.removeMessages(MESSAGE_LONG_CLICK);
				}
				else if (!isQuickScaling) {
					// Start one-finger pan
					vCenterStart.set(event.getX(), event.getY());
					
					// Start long click timer
					handler.sendEmptyMessageDelayed(MESSAGE_LONG_CLICK, 600);
				}
			} return true;
			case MotionEvent.ACTION_MOVE:{
				//CMN.Log("ACTION_MOVE", touchCount);
				if(draggingHandle!=null) {
					dragHandle();
					return true;
				}
				if(!isDown){
					MotionEvent ev = MotionEvent.obtain(event);
					ev.setAction(MotionEvent.ACTION_DOWN);
					onTouchEventInternal(ev);
				}
				float preXCoord = vTranslate.x;
				float preYCoord = vTranslate.y;
				if(!touch_partisheet.contains(touch_id))
					return true;
				boolean consumed = false;
				float scaleStamp=scale,rotationStamp=rotation;
				PointF tranlationStamp = new PointF(vTranslate.x, vTranslate.y);
				PointF centerAt = new PointF(sWidth/2, sHeight/2);
				PointF result = new PointF();
				sourceToViewCoord(centerAt, result);
				
				if (maxTouchCount > 0) {
					if (touch_partisheet.size() >= 2) {
						//if(view_pager_toguard.isFakeDragging()) view_pager_toguard.endFakeDrag();
						// Calculate new distance between touch points, to scale and pan relative to start values.
						float vDistEnd = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
						float vCenterEndX = (event.getX(0) + event.getX(1))/2;
						float vCenterEndY = (event.getY(0) + event.getY(1))/2;
						
						if(rotationEnabled && !isQuickScaling){
							float angle = (float) Math.atan2((event.getY(0) - event.getY(1)), (event.getX(0) - event.getX(1)));
							
							if(!isRotating){
								//CMN.Log("isRotating?", Math.abs(angle - lastAngle)/Math.PI*180);
								if(/*!isDragging && */Math.abs(angle - lastAngle)/Math.PI*180>22){
									isRotating = true;
									lastAngle = angle;
								}
							} else {
								setRotationInternal(rotation + angle - lastAngle);
								lastAngle = angle;
								consumed = true;
								//if(view_to_guard!=null)view_to_guard.setRotation((float) (rotation/Math.PI*180));
							}
						}
						
						if (isPanning || distance(vCenterStart.x, vCenterEndX, vCenterStart.y, vCenterEndY) > 5 || Math.abs(vDistEnd - vDistStart) > 5) {
							isZooming = true;
							isPanning = true;
							consumed = true;
							
							double previousScale = scale;
							if (zoomEnabled) {
								scale = (vDistEnd / vDistStart) * scaleStart;//Math.min(maxScale, );
							}
							//android.util.Log.e("fatal","scale"+scale);
							
							if (panEnabled) {
								// Translate to place the source image coordinate that was at the center of the pinch at the start
								// at the center of the pinch now, to give simultaneous pan + zoom.
								//viewToSourceCoord(vCenterStart, sCenterStart);
								sourceToViewCoord(sCenterStart, vCenterStartNow);
								
								final float dx = (vCenterEndX - vCenterStartNow.x);
								final float dy = (vCenterEndY - vCenterStartNow.y);
								
								float dxR = (float) (dx * cos - dy * -sin);
								float dyR = (float) (dx * -sin + dy * cos);
								
								vTranslate.x += dxR;
								vTranslate.y += dyR;
								
								// TODO: Account for rotation
								boolean b1 = true || scale * sHeight() >= getScreenHeight();
								boolean b2 = true || scale * sWidth() >= getScreenWidth();
								boolean b3 = true || previousScale * sHeight() < getScreenHeight();
								boolean b4 = true || previousScale * sWidth() < getScreenWidth();
								if (true) {//(b3 && b1) || (b4 && b2)
									//fitToBounds(true,true);
//									vCenterStart.set(vCenterEndX, vCenterEndY);
//									vTranslateStart.set(vTranslate);
//									scaleStart = scale;
//									vDistStart = vDistEnd;
								}
								
							} else if (sRequestedCenter != null) {
								// With a center specified from code, zoom around that point.
//								vTranslate.x = (getScreenWidth()/2) - (scale * sRequestedCenter.x);
//								vTranslate.y = (getScreenHeight()/2) - (scale * sRequestedCenter.y);
							} else {
								// With no requested center, scale around the image center.
//								vTranslate.x = (getScreenWidth()/2) - (scale * (sWidth()/2));
//								vTranslate.y = (getScreenHeight()/2) - (scale * (sHeight()/2));
							}
							
							//fitToBounds(true,true);
							//fitCenter();
							refreshRequiredTiles(eagerLoadingEnabled);
						}
					}
					else if (isQuickScaling) {
						float rawY = event.getY();
						float rawDist = quickScaleVStart.y - rawY;
						float dist = rawDist;
						if(dist<0) dist=-dist;
						if(!quickScaleMoved){
							if(dist > quickScaleThreshold){
								dist -= quickScaleThreshold;
								quickScaleVStart.y += quickScaleThreshold*(rawDist<0?1:-1);
								quickScaleMoved = true;
								quickScaleLastDistance = 0;
								quickScalesStart = viewToSourceCoord(quickScaleVStart);
							}
						}
						
						if (quickScaleMoved) {
							float multiplier = 1+dist/quickScaleThreshold;
							
							if(rawY < quickScaleVStart.y)
								multiplier=1/multiplier;
							
							float previousScale = scale;
							
							scale = Math.max(0, quickScaleStart * multiplier);
							//CMN.Log("isQuickScaling",Math.abs(quickScaleLastDistance-dist), scale, dist);
							
							if (panEnabled) {
								vTranslate.x = vTranslateStart.x;
								vTranslate.y = vTranslateStart.y;

								PointF quickScaleVStartNow = sourceToViewCoord(quickScalesStart);
								float xd = quickScaleVStart.x - quickScaleVStartNow.x;
								float yd = quickScaleVStart.y - quickScaleVStartNow.y;

								float dx =  (float) (xd * cos - yd * -sin);
								float dy = (float) (xd * -sin + yd * cos);

								vTranslate.x = vTranslateStart.x + dx;
								vTranslate.y = vTranslateStart.y + dy;
								
								
								if ((previousScale * sHeight() < getScreenHeight() && scale * sHeight() >= getScreenHeight()) || (previousScale * sWidth() < getScreenWidth() && scale * sWidth() >= getScreenWidth())) {
								
								}
							} else if (sRequestedCenter != null) {
								// With a center specified from code, zoom around that point.
								vTranslate.x = (getScreenWidth()/2) - (scale * sRequestedCenter.x);
								vTranslate.y = (getScreenHeight()/2) - (scale * sRequestedCenter.y);
							} else {
								// With no requested center, scale around the image center.
								vTranslate.x = (getScreenWidth()/2) - (scale * (sWidth()/2));
								vTranslate.y = (getScreenHeight()/2) - (scale * (sHeight()/2));
							}
						}
						
						//nono fitToBounds(true,true);
						//fitToBounds(false,false);
						
						refreshRequiredTiles(Math.abs(quickScaleLastDistance-dist)<10);
						
						quickScaleLastDistance = dist;
						//refreshRequiredTiles(eagerLoadingEnabled);
						
						consumed = true;
					}
					else if (!isZooming) {
						// One finger pan - translate the image. We do this calculation even with pan disabled so click
						// and long click behaviour is preserved.
						
						float offset = 0;//density * 5;
						if (isPanning  || Math.abs(event.getY() - vCenterStart.y) > offset) {
							isPanning =
							consumed = true;
							
							float dxRaw = lastX - view_pager_toguard_lastX;
							float dyRaw = lastY - view_pager_toguard_lastY;
							// Using negative angle cos and sin
							float dxR = (float) (dxRaw * cos - dyRaw * -sin);
							float dyR = (float) (dxRaw * -sin + dyRaw * cos);
							
							vTranslate.x = vTranslate.x + dxR;
							vTranslate.y = vTranslate.y + dyR;
							//vTranslate.x = vTranslateStart.x + (event.getX() - vCenterStart.x);
							//vTranslate.y = vTranslateStart.y + (event.getY() - vCenterStart.y);
							
							refreshRequiredTiles(eagerLoadingEnabled);
						}
					}
				}
				
				if(!isRotating){
					vtParms_sew = getScreenWidth();
					vtParms_seh = getScreenExifHeight();
					vtParms_se_delta = (getScreenWidth() - getScreenExifWidth())/2;
					vtParms_se_SWidth = exifSWidth();
					
					double r = rotation % doublePI;
					vtParms_b1=false;vtParms_b2=false;vtParms_b3=false;
					if(!(vtParms_b1=r>halfPI*2.5 && r<halfPI*3.5))
						vtParms_b2=r>halfPI*0.5 && r<halfPI*1.5;
					vtParms_b3 = vtParms_b1 || vtParms_b2;
					vtParms_dir = vtParms_b3?-1:1;
					vtParms_dragDir = 1;
					if(vtParms_b2 || r>halfPI*1.5&&r<halfPI*2.5){
						vtParms_dragDir = -1;
					}
					vtParms_cos = cos;
					vtParms_sin = sin;
				}
				
				//if(false)
				if(!isQuickScaling) {
					float delta_parent = lastX - view_pager_toguard_lastX;
					PointF resultAfter = new PointF();
					sourceToViewCoord(centerAt, resultAfter);
					if(touchCount>1){
						//delta_parent = resultAfter.x - result.x;
					}
					
//					float afterXCoord = vtParms_b3?vTranslate.y:vTranslate.x;
					float scaleWidth = scale * vtParms_se_SWidth;
					float minX = vtParms_sew - scaleWidth;
					float maxX = 0;
					boolean bOverScrollable=false;
					if(minX>=0) {
						minX=minX/2;
						maxX=minX;
					}
					else {
						bOverScrollable = true;
						//minX= -se_delta*dir;
						//maxX = se_delta*dir;
					}
					minX += vtParms_se_delta*vtParms_dir;
					maxX += vtParms_se_delta*vtParms_dir;
					
//					float compensationL=0;//<--
//					float compensationR=0;//-->
//					if(afterXCoord<minX){
//						compensationL=minX-afterXCoord;
//						if(vtParms_b3) vTranslate.y=minX;
//						else vTranslate.x=minX;
//					}
//					if(afterXCoord>maxX){
//						compensationR=afterXCoord-maxX;
//						if(vtParms_b3) vTranslate.y=maxX;
//						else vTranslate.x=maxX;
//					}
//
//					float compensation = afterXCoord - (vtParms_b3?vTranslate.y:vTranslate.x);
					//CMN.Log("compensation?", compensation, compensationL, compensationR,"dragDir", vtParms_dragDir, "minX, maxX", minX, maxX, afterXCoord, "mLastMotionX"+view_pager_toguard.mLastMotionX, "mold", mold, " dragOnRight:"+dragOnRight, "se_delta:"+vtParms_se_delta);
					
					
					
					//CMN.Log("drag on right ::: ", Math.abs((vtParms_b3?vTranslate.y:vTranslate.x)-(vtParms_dragDir==1?minX:maxX))>=Math.abs((vtParms_b3?vTranslate.y:vTranslate.x)-(vtParms_dragDir==1?maxX:minX)));
					//if(false)
					if(!isRotating && touchCount==1 && !startTouchWithAnimation){
						float scaleHeight = scale * sHeight;
						scaleWidth = scale * sWidth;
						float screenHeight = getScreenHeight();
						
						if(vtParms_dir==1){
							float minY = screenHeight-scaleHeight;
							float maxY = 0;
							if(minY>0) minY=maxY=minY/2;
							
//							if(vTranslate.y<minY) vTranslate.y=minY;
//							else if(vTranslate.y>maxY) vTranslate.y=maxY;
						} else {
							minX = screenHeight-scaleWidth;
							maxX = 0;
							if(minX>0) maxX=minX=minX/2;
							minX -= vtParms_se_delta*vtParms_dir;
							maxX -= vtParms_se_delta*vtParms_dir;
							CMN.Log("minX, maxX", minX, maxX);
//							if(vTranslate.x<minX) vTranslate.x=minX;
//							else if(vTranslate.x>maxX) vTranslate.x=maxX;
						}
					}
					
				}
				
				view_pager_toguard_lastX=lastX;
				view_pager_toguard_lastY=lastY;
				
				handle_proxy_simul(scaleStamp,tranlationStamp,rotationStamp);
				if (consumed) {
					handler.removeMessages(MESSAGE_LONG_CLICK);
					invalidate();
					return true;
				}
			} break;
			case MotionEvent.ACTION_UP: {
				isDown = false;
				isZooming=false;
				if(shouldDrawSelection() && anim==null && !isFlinging) {
					postRelocateContextMenuView();
				}
				if(treatNxtUpAsSingle) {
					treatNxtUpAsSingle=false;
					if(!isPanning)
					flinglistener.onSingleTapUp(event);
				}
//				if(event!=wastedEvent) {
//					abortNextDoubleTapZoom=false;
//					wastedClickTime=0;
//				}
			}
			case MotionEvent.ACTION_POINTER_UP:{
				if(draggingHandle!=null) {
					if(touchCount<=1) {
						judgeClick();
					}
					break;
				}
				if(isZooming) {
					if(touch_partisheet.remove(touch_id)) {
						// convert double finger zoom to single finger move
						//vCenterStart.set(event.getX(), event.getY());
						if(touch_partisheet.size()>0) {
							int pid = touch_partisheet.iterator().next().intValue();
							//vTranslateStart.set(vTranslate.x, vTranslate.y);
							pid = touch_id>0?0:1;
							view_pager_toguard_lastX=event.getX(pid);
							view_pager_toguard_lastY=event.getY(pid);
							isPanning=true;
						}
						isZooming=false;
					}
					return true;
				}
				//final float vX = (float) (velocityX * cos - velocityY * -sin);
				//final float vY = (float) (velocityX * -sin + velocityY * cos);
				//touch_partisheet.remove(touch_id);
				//if(is_classical_pan_shrinking) {
				//	if(touch_partisheet.contains(touch_id))
				//		touch_partisheet.clear();
				//}else {
				//	touch_partisheet.remove(touch_id);
				//}
				
				touch_partisheet.clear();
				waitingNextTouchResume = true;
				
				//if(flingScroller.isFinished())
				if(!(doubleTapDetected && !quickScaleMoved)){
					float toRotation = SnapRotation(rotation);
					boolean shouldAnimate = toRotation!=rotation;
					float toScale = currentMinScale();
					
					tmpCenter.set(getScreenWidth()/2, getScreenHeight()/2);
					boolean horizon = pdoc.isHorizontalView();
					boolean resetScale=false;
					if(scale>toScale) {
						toScale=scale;
						if(scale>maxScale){
							toScale=maxScale;
							shouldAnimate = true;
						}
					} else if(scale<toScale){
						resetScale = shouldAnimate = true;
						if(horizon) {
							tmpCenter.set(getCenter().x, getSHeight() / 2);
						} else {
							tmpCenter.set(getSWidth() / 2, getCenter().y);
						}
						//tmpCenter.set(getCenter());
					}
					CMN.Log("toScale", toScale, shouldAnimate);
					strTemp.scale = scale;
					strTemp.vTranslate.set(vTranslate);
					strTemp.rotate = rotation;
					fitToBounds_internal2(true, strTemp);
					if(strTemp.vTranslate.x!=vTranslate.x || strTemp.vTranslate.y!=vTranslate.y){
						if (!resetScale) {
							centerToSourceCoord(tmpCenter, strTemp.vTranslate);
						}
						CMN.Log("strTemp.vTranslate.x!=vTranslate.x || strTemp.vTranslate.y!=vTranslate.y", strTemp.vTranslate.x, vTranslate.x, strTemp.vTranslate.y, vTranslate.y);
						shouldAnimate = true;
					} else if (!resetScale) {
						centerToSourceCoord(tmpCenter, vTranslate);
					}
					//if(false)
					if(shouldAnimate) {
						if(scale<toScale){
							if(horizon) {
								tmpCenter.x = Math.max(tmpCenter.x, getScreenWidth()/2/toScale);
							} else {
								tmpCenter.y = Math.max(tmpCenter.y, getScreenHeight()/2/toScale);
							}
							//tmpCenter.set(getCenter());
						}
						//Take after everything.
						new AnimationBuilder(toScale, tmpCenter, toRotation)
								.withEasing(EASE_OUT_QUAD).withPanLimited(false)
								.withOrigin(ORIGIN_ANIM)
								.withInterruptible(!quickScaleMoved)
								.withDuration(250)
								.start();
					}
				}
				
				handler.removeMessages(MESSAGE_LONG_CLICK);
				if (isQuickScaling) {
					isQuickScaling=false;
					if (!quickScaleMoved) {
						if(!abortNextDoubleTapZoom)
						doubleTapZoom(quickScaleSCenter/*, vCenterStart*/);
					}
				}
				
				if (maxTouchCount > 0 && (isZooming || isPanning) || touch_partisheet.size()==1 || quickScaleMoved) {
					if (isZooming && touchCount == 2 || touch_partisheet.size()==1) {
						// Convert from zoom to pan with remaining touch
//						isPanning = true;
//						vTranslateStart.set(vTranslate.x, vTranslate.y);
//						if(!is_classical_pan_shrinking) {
//							for(int i=0;i<touchCount;i++) {
//								if(touch_partisheet.contains(event.getPointerId(i))) {
//									vCenterStart.set(event.getX(i), event.getY(i));
//									break;
//								}
//							}
//						}
					}
					if (touchCount < 3) {
						// End zooming when only one touch point
						isZooming = false;
					}
					if (touchCount < 2) {
						// End panning when no touch points
						isPanning = false;
						isRotating = false;
						maxTouchCount = 0;
					}
					// Trigger load of tiles now required
					refreshRequiredTiles(true);
					return true;
				}
				if (touchCount == 1) {
					isZooming = false;
					isPanning = false;
					maxTouchCount = 0;
				}
			} return true;
		}
		return false;
	}
	
	
	public void showContextMenuView() {
		if(contextView!=null&&!bSupressingUpdatCtxMenu) {
			contextView.setVisibility(View.VISIBLE);
			showContextMenu = true;
		}
	}
	
	public void hideContextMenuView() {
		if(contextView!=null&&!bSupressingUpdatCtxMenu) {
			contextView.setVisibility(View.GONE);
			showContextMenu = false;
		}
	}
	
	Runnable relocateCMRunnable = this::relocateContextMenuView;
	
	public void postRelocateContextMenuView() {
		if(hideContextMenu) {
			removeCallbacks(relocateCMRunnable);
			postDelayed(relocateCMRunnable, 280);
		} else {
			doRelocateContextMenuView();
		}
	}
	
	public void relocateContextMenuView() {
		if(contextView!=null && !isDown && !bSupressingUpdatCtxMenu) {
			doRelocateContextMenuView();
		}
	}
	
	private void doRelocateContextMenuView() {
		if(hasSelection ||hasAnnotSelction) {
			int height = contextView.getMeasuredHeight();
			float top1, top2;
			float left1;
			float lh=0;
			if(hasSelection) {
				lh = Math.max(handleLeftPos.height(), handleRightPos.height());
				top1 = sourceToViewY(handleLeftPos.top);
				top2 = sourceToViewY(handleRightPos.top);
				if(top1>top2) {
					float tmp = top2;
					top2=top1;
					top1=tmp;
					//left1 = sourceToViewX(handleRightPos.centerX());
				} else {
					//left1 = sourceToViewX(handleLeftPos.centerX());
				}
				left1 = sourceToViewX((handleLeftPos.centerX()+handleRightPos.centerX())/2);
			}
			else {
				top1 = sourceToViewY(annotSelRect.top);
				top2 = sourceToViewY(annotSelRect.bottom);
				left1 = sourceToViewX(annotSelRect.centerX());
			}
			top1 += -height-lh*2*scale;
			top2 += selectionPaintView.drawableHeight + lh*scale;
			float top=top1;
			if(false)
			if(top<0) { // 没入顶端
				top = top2;
				if(top+height>getHeight()) { // 溢出底部
					float center = (top1 + top2) / 2;
					if(center>0&&center<getHeight()) {
						top = (getHeight()-height)/2;
					}
				}
			}
			if(true)
			if(false){
				top = Math.min(Math.max(0, top), getHeight()-height);
			}
			contextView.setTranslationY(top);
			
			contextView.setTranslationX(left1-contextView.getWidth()/2);
			contextView.setVisibility(View.VISIBLE);
			showContextMenu = true;
		}
	}
	
	public void judgeClick() {
		CMN.Log("judgeClick!!!");
		if(draggingHandle!=null){
			draggingHandle=null;
		}
		//System.gc();
	}
	
	double halfPI = Math.PI / 2;
	double doublePI = Math.PI * 2;
	private float SnapRotation(float rotation) {
		while(rotation>doublePI){
			rotation-=doublePI;
		}
		if(rotation>halfPI*3.5) rotation= (float) doublePI;
		else if(rotation>halfPI*2.5) rotation= (float) (halfPI*3);
		else if(rotation>halfPI*1.5) rotation= (float) (Math.PI);
		else if(rotation>halfPI*0.5) rotation= (float) (halfPI*1);
		else rotation= 0;
		return rotation;
	}
	
	private void handle_proxy_simul(float scaleStamp, PointF translationStamp, float rotationStamp) {
		if(shouldDrawSelection()||selectionPaintView.searchCtx!=null) {
			redrawSel();
		}
		if(showContextMenu && contextView!=null && !hideContextMenu) {
			doRelocateContextMenuView();
		}
//		if (view_to_guard != null) {
//			if(false) {
//				view_to_guard.setScaleType(ImageView.ScaleType.MATRIX);
//				Matrix mat = new Matrix();
//				view_to_guard.setImageMatrix(mat);
//				mat.reset();
//				mat.postScale(scale / getMinScale(), scale / getMinScale());
//				//mat.postRotate(getRequiredRotation());
//				mat.postTranslate(vTranslate.x, vTranslate.y);
//				mat.postRotate((float) Math.toDegrees(rotation), getScreenWidth() / 2, getScreenHeight() / 2);
//				//re-paint
//				view_to_guard.invalidate();
//			}else{
//				//Rotacio is hard to take after, yet I have figured it out!
//				if(rotation!=rotationStamp)
//					view_to_guard.setRotation((float) ((rotation)/Math.PI*180)-sOrientation);
//				if(scale!=scaleStamp){
//					view_to_guard.setScaleX(scale/getMinScale());
//					view_to_guard.setScaleY(scale/getMinScale());
//				}
//				if(scale!=scaleStamp || rotation!=rotationStamp || translationStamp==null || !translationStamp.equals(vTranslate)){
//					float deltaX = scale*sWidth/2 +vTranslate.x-getScreenWidth()*1.0f/2;
//					float deltaY = scale*sHeight/2+vTranslate.y-getScreenHeight()*1.0f/2;
//					PointF vTranslateDelta = new PointF();
//					vTranslateDelta.x = (float) (deltaX * cos + deltaY * -sin - deltaX);
//					vTranslateDelta.y = (float) (deltaX * sin + deltaY * cos - deltaY);
//					vTranslateOrg.x = getScreenWidth()*1.0f/2-scale*sWidth/2;
//					vTranslateOrg.y = getScreenHeight()*1.0f/2-scale*sHeight/2;
//					float targetTransX = vTranslate.x - vTranslateOrg.x + vTranslateDelta.x;
//					float targetTransY = vTranslate.y - vTranslateOrg.y + vTranslateDelta.y;
//					if(view_to_guard.getTranslationX()!=targetTransX)view_to_guard.setTranslationX(targetTransX);
//					if(view_to_guard.getTranslationY()!=targetTransY)view_to_guard.setTranslationY(targetTransY);
//				}
//			}
//		}
	}
	
	private void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		ViewParent parent = getParent();
		if (parent != null) {
			parent.requestDisallowInterceptTouchEvent(disallowIntercept);
		}
	}
	
	/** Compute Quick Double Tap Zoom Levels. see {@link #currentMinScale} */
	private void computeQuickZoomLevels() {
		int vPadding = getPaddingBottom() + getPaddingTop();
		int hPadding = getPaddingLeft() + getPaddingRight();
		long sw = sWidth;
		long sh = sHeight;
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5){
			sw = sHeight;
			sh = sWidth;
		}
		float scaleMin1;
		if(pdoc.isHorizontalView()) {
			scaleMin1 = (getScreenHeight() - vPadding) / (float) sh;
		} else {
			scaleMin1 = (getScreenWidth() - hPadding) / (float) sw;
		}
		float zoomInLevel = 2.5f;
		
		quickZoomLevels[0] = scaleMin1;
		scaleMin1 = Math.min(scaleMin1*zoomInLevel, maxScale);
		quickZoomLevels[1] = scaleMin1;
		quickZoomLevelCount = 2;
		if(scaleMin1<maxScale) {
			quickZoomLevelCount = 3;
			scaleMin1 = Math.min(scaleMin1*zoomInLevel, maxScale);
			quickZoomLevels[2] = scaleMin1;
		}
	}
	
	/**
	 * Double tap zoom handler triggered from gesture detector or on touch, depending on whether
	 * quick scale is enabled.
	 */
	private void doubleTapZoom(PointF sCenter) {
		float scaleMin = currentMinScale();
		float targetScale;
		computeQuickZoomLevels();
		float padding = 0.01f;
		float currentScale = scale;
		
		if(anim!=null){
			if(anim.origin == ORIGIN_DOUBLE_TAP_ZOOM){
				currentScale = anim.scaleEnd;
			}
			anim=null;
		}
		
		if(quickZoomLevelCount==2){
			targetScale = currentScale >= quickZoomLevels[1]?quickZoomLevels[0]:quickZoomLevels[1];
		} else {
			if(currentScale <= quickZoomLevels[1]-padding){
				targetScale = quickZoomLevels[1];
			} else if(currentScale <= quickZoomLevels[2]-padding){
				targetScale = quickZoomLevels[2];
			} else {
				targetScale = quickZoomLevels[0];
			}
		}
		
		new AnimationBuilder(targetScale, sCenter)
				.withInterruptible(false)
				.withDuration(250) //doubleTapZoomDuration
				.withOrigin(ORIGIN_DOUBLE_TAP_ZOOM)
				.start();
	}
	
	/** Count number of set bits */
	static int CountBits(int w)
	{
		int e = 0;
		while (w > 0)
		{
			w &= w - 1;
			e++;
		}
		return e;
	}
	
	/** n === 2^ret */
	static int log2(int n)
	{
		return CountBits(n-1);
	}
	
	/**
	 * Draw method should not be called until the view has dimensions so the first calls are used as triggers to calculate
	 * the scaling and tiling required. Once the view is setup, tiles are displayed as they are loaded.
	 */
	Tile[] twoStepHeaven = new Tile[2];
	int    toHeavenSteps = 2;
	
	int[] twoStepHeavenCode = new int[2];
	long[] logicLayout = new long[256];
	int logiLayoutSz = 0;
	int logiLayoutSt = 0;
	
	private int reduceDocOffset(float offset, int start, int end) {
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			return offset > pdoc.mPDocPages[start + len - 1].OffsetAlongScrollAxis
					? reduceDocOffset(offset,start+len,end)
					: reduceDocOffset(offset,start,start+len);
		} else {
			return start;
		}
	}
	
	int tickCheckLoRThumbsIter;
	int tickCheckHIRThumbsIter;
	int tickCheckHiResRgnsIter, tickCheckHiResRgnsItSt;
	Tile[] LowThumbs = new Tile[1024];
	int RTLIMIT=48*2;
	RegionTile[] regionTiles = new RegionTile[RTLIMIT];
	public Tile tickCheckLoRThumbnails() {
		for (; tickCheckLoRThumbsIter < 1024; tickCheckLoRThumbsIter++) {
			Tile ltI = LowThumbs[tickCheckLoRThumbsIter];
			if(ltI==null) {
				LowThumbs[tickCheckLoRThumbsIter]=ltI=new Tile();
				ltI.bitmap=DummyBitmap;
				return ltI;
			}
			int pageIdx = ltI.currentOwner == null ? -1 : ltI.currentOwner.pageIdx;
			if(pageIdx==-1 || (pageScoper==null||!pageScoper.pageInScope(pageIdx)) && ltI.resetIfOutside(this, 5)) {
				return ltI;
			}
		}
		return null;
	}
	
	public void resetLoRThumbnailTick() {
		tickCheckLoRThumbsIter = 0;
	}
	
	public Bitmap getLoRThumbnailForPageAt(int pageIdx) {
		if(pdoc!=null && pageIdx>=0 && pageIdx<pdoc.mPDocPages.length) {
			Tile tI = pdoc.mPDocPages[pageIdx].tile;
			if(tI!=null && !tI.taskToken.loading) {
				return tI.bitmap;
			}
		}
		return null;
	}
	
	public void requestLoRThumbnailForPageAt(int pageIdx) {
		if(pdoc!=null && pageIdx>=0 && pageIdx<pdoc.mPDocPages.length) {
			PDocument.PDocPage page = pdoc.mPDocPages[pageIdx];
			Tile tI = page.tile;
			if(tI==null) {
				tI = tickCheckLoRThumbnails();
				if(tI!=null) {
					tickCheckLoRThumbsIter++;
					TileLoadingTask task = acquireFreeTask();
					tI.assignToAsAlterThumbnail(task, page, pdoc.ThumbsLoResFactor);
					task.dequire();
					task.startIfNeeded();
				}
			}
		}
	}
	
	private Tile tickCheckHIRThumbnails() {
		for (; tickCheckHIRThumbsIter < toHeavenSteps; tickCheckHIRThumbsIter++) {
			Tile ltI = twoStepHeaven[tickCheckHIRThumbsIter];
			if(ltI==null) {
				twoStepHeaven[tickCheckHIRThumbsIter]=ltI=new Tile();
				ltI.bitmap=DummyBitmap;
				ltI.HiRes=true;
				return ltI;
			}
			if(ltI.currentOwner==null || ltI.resetIfOutside(this, 0)) {
				return ltI;
			}
		}
		return null;
	}
	
	private RegionTile tickCheckHiResRegions(float stride) {
		for (; tickCheckHiResRgnsIter < RTLIMIT; tickCheckHiResRgnsIter++) {
			int iter = (tickCheckHiResRgnsItSt+tickCheckHiResRgnsIter)%RTLIMIT;
			RegionTile ltI = regionTiles[iter];
			if(ltI==null) {
				regionTiles[iter]=ltI=new RegionTile();
				ltI.bitmap=Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
				return ltI;
			}
			if(ltI.currentOwner==null || ltI.resetIfOutside(this, stride)) {
				return ltI;
			}
		}
		return null;
	}
	
	private boolean refreshRequiredTiles(boolean load) {
		//CMN.Log("refreshRequiredTiles", pdoc);
		if (pdoc==null || pdoc.isClosed) { return false; }
		stoff = (long) (-vTranslate.y/scale);
		edoff = stoff+getScreenHeight()/scale;
		stoffX = (long) (-vTranslate.x/scale);
		edoffX = stoffX+getScreenWidth()/scale;
		//存疑
		logiLayoutSt = reduceDocOffset(pdoc.isHorizontalView()?stoffX:stoff, 0, pdoc.mPDocPages.length);
		if(pdoc.mPDocPages[logiLayoutSt].OffsetAlongScrollAxis> logiLayoutSt) {
			logiLayoutSt--;
		}
		logiLayoutSz=0;
		//CMN.Log("screen_scoped_src_is...", stoff, edoff);
		boolean horizon = pdoc.isHorizontalView();
		float EO = horizon ? edoffX : edoff;
		float MO = horizon ? (edoffX+stoffX)/2 : (edoff+stoff)/2;
		boolean seekMiddlePage = true;
		int pageMiddle = -1;
		while (logiLayoutSz < logicLayout.length
				&& logiLayoutSt+logiLayoutSz <pdoc.mPDocPages.length) {
			//CMN.Log("第几...", pdoc.mPDocPages[logiLayoutSt+logiLayoutSz].OffsetAlongScrollAxis);
			PDocument.PDocPage page = pdoc.mPDocPages[logiLayoutSt + logiLayoutSz];
			if(page.OffsetAlongScrollAxis<EO) {
				//stoff += (logiLayoutSz>0?pdoc.mPDocPages[logiLayoutSt+logiLayoutSz-1].size.getHeight()+0:0);
				logicLayout[logiLayoutSz] = pdoc.mPDocPages[logiLayoutSt+logiLayoutSz].OffsetAlongScrollAxis;
				if(seekMiddlePage && page.OffsetAlongScrollAxis+(horizon?pdoc.gap+page.size.getWidth():page.size.getHeight())>MO) {
					pageMiddle = logiLayoutSt+logiLayoutSz;
					seekMiddlePage = false;
				}
				logiLayoutSz++;
			} else {
				break;
			}
		}
		if(pageMiddle>=0 && pageMiddle!=lastMiddlePage) {
			if(mOnPageChangeListener!=null) {
				mOnPageChangeListener.OnPageChanged(lastMiddlePage, pageMiddle);
			}
			lastMiddlePage = pageMiddle;
		}
		TileLoadingTask task = null;
		if(logiLayoutSz>0) {
			PDocument.PDocPage page;
			boolean HiRes, NoTile;
			tickCheckLoRThumbsIter=0;
			tickCheckHIRThumbsIter=0;
			int thetaDel = 2;
			/* add thumbnails */
			//if(false)
			ThumbnailsAssignment:
			for (int i = -thetaDel; i < logiLayoutSz+thetaDel; i++) {
				if(logiLayoutSt + i<0||logiLayoutSt + i>=pdoc._num_entries)
					continue ;
				page = pdoc.mPDocPages[logiLayoutSt + i];
				HiRes = i>=0 && i<=toHeavenSteps &&  (i>0 || logiLayoutSz<=2
						|| !horizon&&vTranslate.y+(page.OffsetAlongScrollAxis+page.size.getHeight()/2)*scale>0 /*半入法眼*/
						|| horizon&&vTranslate.x+(page.OffsetAlongScrollAxis+page.size.getWidth()/2)*scale>0 /*半入法眼*/
						);
				//CMN.Log("HiRes", HiRes);
				HiRes = false;
				NoTile=page.tile==null;
				if(NoTile || HiRes && !page.tile.HiRes) {
					if(HiRes) {
						Tile tile = tickCheckHIRThumbnails();
						if(tile!=null) {
							if(task==null)task = acquireFreeTask();
							if(!NoTile) page.sendTileToBackup();
							tile.assignToAsThumbnail(task, page, pdoc.ThumbsHiResFactor);
							continue ThumbnailsAssignment;
						}
					}
					// assign normal thumbnails.
					//if(false)
					if(page.tile==null && tickCheckLoRThumbsIter<1024) {
						Tile tile = tickCheckLoRThumbnails();
						if(tile!=null) {
							tickCheckLoRThumbsIter++;
							if(task==null)task = acquireFreeTask();
							tile.assignToAsThumbnail(task, page, pdoc.ThumbsLoResFactor);
						}
					}
				}
			}
			int tastSize = task==null?0:task.list.size();
			//if(!((flingScroller.isFinished()||isDown) && !isZooming))
			//CMN.Log("refreshRequiredTiles", flingScroller.isFinished()||isDown, !isZooming);
			/* add regions */
			//if(false)
			if(scale>=minScale() && (!isFlinging||flingScroller.getCurrVelocity()<15000*5) && !isZooming) {
				tickCheckHiResRgnsIter=0;
				float stride = (256 / scale);
				
				refreshedBucket.clear();
				if(horizon) {
					for (int i = 0; i < logiLayoutSz; i++) {
						page = pdoc.mPDocPages[logiLayoutSt + i];
						int HO=page.getLateralOffset();
						long stoff=this.stoff-HO;
						float edoff=this.edoff-HO;
						long scroll_axis_top = page.OffsetAlongScrollAxis;
						int scroll_axis_top_delta = (int) (stoffX - scroll_axis_top);
						int startY = 0;
						int startX = 0;
						if (scroll_axis_top_delta > 0) {
							startX = (int) (scroll_axis_top_delta / stride);
						}
						if (stoff > 0) {
							startY = (int) (stoff / stride);
						}
						page.startX=startX;
						page.startY=startY;
						page.maxX = Math.min((int) Math.ceil(page.size.getWidth()*1.0f/stride), (int) Math.ceil((edoffX-scroll_axis_top)*1.0f/stride))-1;
						page.maxY = Math.min((int) Math.ceil(page.size.getHeight()*1.0f/stride), (int) Math.ceil(edoff*1.0f/stride))-1;
						//CMN.Log(page, "maxX, maxY", page.maxX, page.maxY, "ST::X, Y", page.startX, page.startY);
					}
					for (int i = 0; i < logiLayoutSz; i++) {
						page = pdoc.mPDocPages[logiLayoutSt + i];
						int sY= (int) page.startX;
						long top = page.OffsetAlongScrollAxis;
						float srcRgnTop=sY*stride;
						while(top+srcRgnTop<edoffX && sY<=page.maxX) { // 子块顶边不超出
							int sX = (int) page.startY;
							float srcRgnLeft=page.startY*stride;
							while(srcRgnLeft<edoff && sX<=page.maxY) {
								RegionTile rgnTile = page.getRegionTileAt(sY, sX);
								if(rgnTile==null)
								{
									//CMN.Log("attempting to place tile at::", sX, sY, "pageidx=", logiLayoutSt + i);
									rgnTile = tickCheckHiResRegions(stride);
									if(rgnTile!=null) {
										tickCheckHiResRgnsIter++;
										if(task==null)task = acquireFreeTask();
										rgnTile.assignToAsRegion(task, page, sY, sX, scale, srcRgnLeft, srcRgnTop, stride);
									} else {
										CMN.Log("------lucking tiles!!!", sY, sX);
									}
								}
								else if(rgnTile.scale!=scale) {
									//CMN.Log("attempting to restart tile at::", sX, sY, "pageidx=", logiLayoutSt + i);
									if(task==null)task = acquireFreeTask();
									rgnTile.restart(task, page, sY, sX, scale, srcRgnLeft, srcRgnTop, stride);
								}
								if(rgnTile!=null) {
									refreshedBucket.add(rgnTile);
								}
								//if(sX>2*256)break;
								srcRgnLeft+=stride;
								sX++;
							}
							//CMN.Log("startY", startY);
							sY++;
							srcRgnTop+=stride;
						}
					}
				} else {
					for (int i = 0; i < logiLayoutSz; i++) {
						page = pdoc.mPDocPages[logiLayoutSt + i];
						int HO=page.getLateralOffset();
						long stoffX=this.stoffX-HO;
						float edoffX=this.edoffX-HO;
						long top = page.OffsetAlongScrollAxis;
						int top_delta = (int) (stoff - top);
						int startY = 0;
						int startX = 0;
						if (top_delta > 0) {
							startY = (int) (top_delta / stride);
						}
						if (stoffX > 0) {
							startX = (int) (stoffX / stride);
						}
						page.startX=startX;
						page.startY=startY;
						page.maxX = Math.min((int) Math.ceil(page.size.getWidth()*1.0f/stride), (int) Math.ceil(edoffX*1.0f/stride))-1;
						page.maxY = Math.min((int) Math.ceil(page.size.getHeight()*1.0f/stride), (int) Math.ceil((edoff-top)*1.0f/stride))-1;
						//CMN.Log(page, "maxX, maxY", page.maxX, page.maxY, "ST::X, Y", page.startX, page.startY);
					}
					for (int i = 0; i < logiLayoutSz; i++) {
						page = pdoc.mPDocPages[logiLayoutSt + i];
						int sY= (int) page.startY;
						long top = page.OffsetAlongScrollAxis;
						float srcRgnTop=sY*stride;
						while(top+srcRgnTop<edoff && sY<=page.maxY) { // 子块顶边不超出
							int sX = (int) page.startX;
							float srcRgnLeft=page.startX*stride;
							while(srcRgnLeft<edoffX && sX<=page.maxX) {
								RegionTile rgnTile = page.getRegionTileAt(sX, sY);
								if(rgnTile==null)
								{
									//CMN.Log("attempting to place tile at::", sX, sY, "pageidx=", logiLayoutSt + i);
									rgnTile = tickCheckHiResRegions(stride);
									if(rgnTile!=null) {
										tickCheckHiResRgnsIter++;
										if(task==null)task = acquireFreeTask();
										rgnTile.assignToAsRegion(task, page, sX, sY, scale, srcRgnTop, srcRgnLeft, stride);
									} else {
										CMN.Log("------lucking tiles!!!", sX, sY);
									}
								}
								else if(rgnTile.scale!=scale) {
									//CMN.Log("attempting to restart tile at::", sX, sY, "pageidx=", logiLayoutSt + i);
									if(task==null)task = acquireFreeTask();
									rgnTile.restart(task, page, sX, sY, scale, srcRgnTop, srcRgnLeft, stride);
								}
								if(rgnTile!=null) {
									refreshedBucket.add(rgnTile);
								}
								//if(sX>2*256)break;
								srcRgnLeft+=stride;
								sX++;
							}
							//CMN.Log("startY", startY);
							sY++;
							srcRgnTop+=stride;
						}
					}
				}
			}
			//CMN.Log("这一轮更新", tickCheckHiResRgnsItSt, tickCheckHiResRgnsIter);
			tickCheckHiResRgnsItSt = (tickCheckHiResRgnsItSt+tickCheckHiResRgnsIter)%RTLIMIT;
			tastSize = task==null?0:task.list.size()-tastSize;
			//if(tastSize>0) CMN.Log("region_bitmap_assignment::", tastSize);
		}
		if(task!=null) {
			task.dequire();
			task.startIfNeeded();
			//if(tickCheckLoRThumbsIter>0) CMN.Log("bitmap_assignment::", tickCheckLoRThumbsIter);
			return true;
		}
		//CMN.Log("logicalLayout:: ", logiLayoutSt, logiLayoutSz, Arrays.toString(Arrays.copyOfRange(logicLayout, 0, logiLayoutSz)));
		return false;
		
	}
	
	void decodeThumbAt(Tile tile, float scale) {
		if(pdoc.isClosed) {
			return;
		}
		Bitmap OneSmallStep = tile.bitmap;
		PDocument.PDocPage page = tile.currentOwner;
		if(OneSmallStep==null||page==null) {
			return;
		}
		page.open();
		int HO = page.getLateralOffset();
		if(pdoc.isHorizontalView()) {
			tile.sRect.set((int)page.OffsetAlongScrollAxis, HO, (int)page.OffsetAlongScrollAxis+page.size.getWidth(), HO+page.size.getHeight());
		} else {
			tile.sRect.set(HO, (int)page.OffsetAlongScrollAxis, HO+page.size.getWidth(), (int)page.OffsetAlongScrollAxis+page.size.getHeight());
		}
		int w = (int) (page.size.getWidth()*scale);
		int h = (int) (page.size.getHeight()*scale);
		boolean recreate=OneSmallStep.isRecycled();
		if(!recreate && (w!=OneSmallStep.getWidth()||h!=OneSmallStep.getHeight())) {
			if(OneSmallStep.getAllocationByteCount()>=w*h*4) {
				OneSmallStep.reconfigure(w, h, Bitmap.Config.ARGB_8888);
			} else recreate=true;
		}
		if(recreate) {
			OneSmallStep.recycle();
			tile.bitmap = OneSmallStep = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		}
		pdoc.renderBitmap(page, OneSmallStep, scale);
		//CMN.Log("loaded!!!");
		//postInvalidate();
	}
	
	/** decode Region At */
	void decodeRegionAt(RegionTile tile, float scale) {
		//CMN.Log("decodeRegionAt");
		if(pdoc.isClosed) {
			return;
		}
		Bitmap OneSmallStep = tile.bitmap;
		PDocument.PDocPage page = tile.currentOwner;
		if(OneSmallStep==null||page==null) {
			return;
		}
		page.open();
		float w = 256; // the drawSize, in view space
		float h = 256; // the drawSize, in view space
		float x=tile.x*w, y=tile.y*h;
		int vPageW = (int) (page.size.getWidth()*scale);
		int vPageH = (int) (page.size.getHeight()*scale);
		if(x+w>vPageW) {
			w=vPageW-x;
		}
		if(y+h>vPageH) {
			h=vPageH-y;
		}
		if(w!=OneSmallStep.getWidth()||h!=OneSmallStep.getHeight()) {
			if(w<=0||h<=0) return;
			OneSmallStep.reconfigure((int)Math.ceil(w), (int)Math.ceil(h), Bitmap.Config.ARGB_8888);
		}
//		int src_top = (int) (tile.OffsetAlongScrollAxis+y/scale);
//		int src_left = (int) (x/scale);
		float src_top, src_left;
		if(pdoc.isHorizontalView()) {
			src_top = (page.getLateralOffset()+tile.srcRgnTop);
			src_left = (tile.srcRgnLeft+tile.OffsetAlongScrollAxis);
		} else {
			src_top = (tile.OffsetAlongScrollAxis+tile.srcRgnTop);
			src_left = (tile.srcRgnLeft+page.getLateralOffset());
		}
		w/=scale; // the drawSize, in src space
		h/=scale; // the drawSize, in src space
		tile.sRect.set(src_left, src_top, src_left+w, src_top+h);
		int srcW=(int) (page.size.getWidth()*scale);
		int srcH=(int) (page.size.getHeight()*scale);
		pdoc.renderRegionBitmap(page, OneSmallStep, (int) (-tile.srcRgnLeft*scale), (int) (-tile.srcRgnTop*scale), srcW, srcH);
//		pdoc.renderRegionBitmap(page, OneSmallStep, 0, 0, srcW, srcH);
		//CMN.Log("loaded rgn!!!", page.pageIdx, "lt:", src_left, src_top, "wh:", w, h, tile.srcRgnLeft, tile.srcRgnTop);
//		if(flingScroller.isFinished())
//			postInvalidate();
	}
	
	@Override
	public void computeScroll() {
		super.computeScroll();
//		if (anim != null) {
//			handle_animation();
//		} else
		if(stdFling && isFlinging) {
			flingRunnable.run();
		}
	}
	
	ArrayList<RegionTile> refreshedBucket = new ArrayList<>(128);
	ArrayList<RegionTile> drawingBucket = new ArrayList<>(128);
	ArrayList<RegionTile> zoomingBucket = new ArrayList<>(128);
	
	@SuppressLint("DefaultLocale")
	@Override
	protected void onDraw(Canvas canvas) {
		//CMN.Log("ondraw");
		super.onDraw(canvas);
		int drawCount=0;
		boolean horizon = pdoc!=null&&pdoc.isHorizontalView();
		//if(false)
		if(pdoc!=null) {
			if (anim != null) {
				handle_animation();
			}
			
			canvas.drawColor(BackGroundColor);
			//canvas.drawBitmap(bitmap, null, new RectF(0,vTranslate.y,100,100), bitmapPaint);
			//canvas.drawBitmap(bitmap, 0, vTranslate.y, bitmapPaint);
			
			// 绘制高清
			// 策略二
			draw_stoff = (long) (-vTranslate.y/scale);
			draw_edoff = stoff+getScreenHeight()/scale;
			draw_stoffX = (long) (-vTranslate.x/scale);
			draw_edoffX = stoffX+getScreenWidth()/scale;
			//collect
			boolean missingTile=false;
			drawingBucket.clear();
			for (int i = 0, len=refreshedBucket.size(); i < len; i++) {
				RegionTile rgnTile = refreshedBucket.get(i);
				if(rgnTile!=null && rgnTile.shouldDraw(this)) {
					if(!rgnTile.taskToken.loading) {
						drawingBucket.add(rgnTile);
						if(SSVDF) {
							hrcTmp.set(0,0,256,256);
							matrix.mapRect(hrcTmp);
							canvas.drawText(String.format("%d,%d", rgnTile.x, rgnTile.y), hrcTmp.left, hrcTmp.top+20, debugTextPaint);
						}
					} else if(!missingTile) {
						missingTile=true;
					}
				}
			}
			
			//if(missingTile||isZooming||isFlinging||anim!=null)
			//if(false)
			{
				// 绘制缩略图
				for (int i = 0; i < logiLayoutSz; i++) {
					PDocument.PDocPage page = pdoc.mPDocPages[logiLayoutSt + i];
					Tile tile = page.tile;
					if (tile != null) {
						Bitmap bm = tile.bitmap;
						if (bm.isRecycled() || tile.taskToken.loading) {
							bm = null;
							if (page.tileBk != null) {
								tile = page.tileBk;
								bm = tile.bitmap;
								if (bm.isRecycled() || tile.taskToken.loading) {
									bm = null;
								}
							}
						}
						if (bm == null) {
							continue;
						}
						
						Rect VR = tile.vRect;
						sourceToViewRectF(tile.sRect, VR);
						if (tileBgPaint != null) {
							canvas.drawRect(VR, tileBgPaint);
						}
						matrix.reset();
						int bmWidth = bm.getWidth();
						int bmHeight = bm.getHeight();
						setMatrixArray(srcArray, 0, 0, bmWidth, 0, bmWidth, bmHeight, 0, bmHeight);
						setMatrixArray(dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
						
						matrix.setPolyToPoly(srcArray, 0, dstArray, 0, 4);
						matrix.postRotate(0, getScreenWidth(), getSHeight());
						canvas.drawBitmap(bm, matrix, bitmapPaint);
					}
				}
			}
			
			//if(false)
			for (RegionTile rgnTile:drawingBucket) {
				Bitmap bm = rgnTile.bitmap;
				Rect VR = vRect;
				sourceToViewRectF(rgnTile.sRect, VR);
				matrix.reset();
				int d=0;
				int bmWidth = bm.getWidth()+d;
				int bmHeight = bm.getHeight()+d;
				
				setMatrixArray(srcArray, 0, 0,bmWidth , 0, bmWidth, bmHeight, 0, bmHeight);
				setMatrixArray(dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
				
				matrix.setPolyToPoly(srcArray, 0, dstArray, 0, 4);
				//matrix.postRotate(0, getScreenWidth(), getSHeight());
				canvas.drawBitmap(bm, matrix, bitmapPaint);
				
				drawCount++;
			}
			
			if(false)
			for (RegionTile rgnTile:regionTiles) {
				if(rgnTile!=null && !rgnTile.isOutSide(this)/* && rgnTile.isInSide(this)*/) {
					if(!rgnTile.taskToken.loading) {
						if(!isZooming && rgnTile.scale!=scale && rgnTile.scale!=1) {
							continue;
						}
						Bitmap bm = rgnTile.bitmap;
						Rect VR = rgnTile.vRect;
						sourceToViewRectF(rgnTile.sRect, VR);
						matrix.reset();
						int d=2;
						int bmWidth = bm.getWidth()+d;
						int bmHeight = bm.getHeight()+d;
						
						setMatrixArray(srcArray, 0, 0,bmWidth , 0, bmWidth, bmHeight, 0, bmHeight);
						setMatrixArray(dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
						
						matrix.setPolyToPoly(srcArray, 0, dstArray, 0, 4);
						
						//if(false)
//						if(rgnTile.scale==scale) {
//							hrcTmp.set(0,0,256,256);
//							matrix.mapRect(hrcTmp);
//							canvas.drawBitmap(bm, hrcTmp.left, hrcTmp.top, bitmapPaint);
//						} else {
							canvas.drawBitmap(bm, matrix, bitmapPaint);
//						}
						
						drawCount++;
						if(SSVDF) {
							matrix.mapRect(hrcTmp);
							canvas.drawText(String.format("%d,%d-[%d,%d]-%d", rgnTile.x, rgnTile.y, rgnTile.currentOwner.maxX,rgnTile.currentOwner.maxY, (rgnTile.currentOwner.maxX-rgnTile.currentOwner.startX+1)*(rgnTile.currentOwner.maxY-rgnTile.currentOwner.startY+1)), hrcTmp.left, hrcTmp.top+20, debugTextPaint);
						}
					}
					else if(false && rgnTile.bkRc!=null) {
						Bitmap bm = rgnTile.bitmap;
						Rect VR = rgnTile.vRect;
						sourceToViewRectF(rgnTile.bkRc, VR);
						matrix.reset();
						int d=0;
						int bmWidth = bm.getWidth()+d;
						int bmHeight = bm.getHeight()+d;
						
						setMatrixArray(srcArray, 0, 0,bmWidth , 0, bmWidth, bmHeight, 0, bmHeight);
						setMatrixArray(dstArray, VR.left, VR.top, VR.right, VR.top, VR.right, VR.bottom, VR.left, VR.bottom);
						
						matrix.setPolyToPoly(srcArray, 0, dstArray, 0, 4);
						
						canvas.drawBitmap(bm, matrix, bitmapPaint);
					}
				}
			}
			
			
			//CMN.Log("绘制了", vTranslate.y);
		}
		
		
		if(SSVD && pdoc!=null) {
			if(SSVDF) {
				for (int i = 0; i < logiLayoutSz; i++) {
					long srcLayOff = logicLayout[i];
					PDocument.PDocPage page = pdoc.mPDocPages[logiLayoutSt+i];
					int srcRectW = page.size.getWidth();
					int srcRectH = page.size.getHeight();
					float top, left;
					if(horizon) {
						top = vTranslate.y + page.getLateralOffset()*scale;
						left = vTranslate.x + srcLayOff * scale;
					} else {
						top = vTranslate.y + srcLayOff * scale;
						left = vTranslate.x+page.getLateralOffset()*scale;
					}
					canvas.drawRect(left, top, left+srcRectW*scale, top+srcRectH*scale, debugLinePaint);
				}
			}
			
			//debugTextPaint.setColor(0xff6666ff);
			int y=15;
			int x=px(5);
			canvas.drawText(String.format("Scale: %.4f (%.4f-%.4f) drawCount:%d", scale, minScale(), maxScale, drawCount), px(5), px(y), debugTextPaint);y+=15;
			canvas.drawText(String.format("Translate: [%.2f:%.2f] Speed: %d", vTranslate.x, vTranslate.y, (int)flingScroller.getCurrVelocity()), x, px(y), debugTextPaint);y+=15;
			
			PointF center = getCenter();
			canvas.drawText(String.format("Source center: %.2f:%.2f", center.x, center.y), px(5), px(y), debugTextPaint);y+=15;
			
			//canvas.drawText(String.format("[SampleSize:%d] [cc:%d] [scale:%.2f] [preview:%d]",0, 0, scale, maxImageSampleSize), x, px(y), debugTextPaint);y+=15;
			
			//if(imgsrc!=null)canvas.drawText("path = " + imgsrc.substring(imgsrc.lastIndexOf("/")+1), x, px(y), debugTextPaint);y+=15;
			
			canvas.drawText(String.format("size = [%dx%d] [%dx%d]", getScreenWidth(), getScreenHeight(), sWidth, sHeight), x, px(y), debugTextPaint);y+=15;
			
			
			debugLinePaint.setColor(Color.MAGENTA);
			
		}
	}
	
	private void handle_animation() {
		//CMN.Log("handle_animation");
		//if(true) return;
		if (anim != null && anim.vFocusStart != null) {
			if(isFlinging) {
				freeAnimation=false;
				freefling=false;
				flingRunnable.run();
				freefling=true;
			}
			// Store current values so we can send an event if they change
			float scaleBefore = scale;
			float rotationBefore = rotation;
			if (vTranslateBefore == null) { vTranslateBefore = new PointF(0, 0); }
			vTranslateBefore.set(vTranslate);
			
			long scaleElapsed = System.currentTimeMillis() - anim.time;
			boolean finished = scaleElapsed > anim.duration;
			scaleElapsed = Math.min(scaleElapsed, anim.duration);
			scale = ease(anim.easing, scaleElapsed, anim.scaleStart, anim.scaleEnd - anim.scaleStart, anim.duration);
			//Log.e("anim_scalanim", anim.scaleStart+","+(anim.scaleEnd - anim.scaleStart)+","+scale);
			
			// Apply required animation to the focal point
			float vFocusNowX = ease(anim.easing, scaleElapsed, anim.vFocusStart.x, anim.vFocusEnd.x - anim.vFocusStart.x, anim.duration);
			float vFocusNowY = ease(anim.easing, scaleElapsed, anim.vFocusStart.y, anim.vFocusEnd.y - anim.vFocusStart.y, anim.duration);
			
			if (rotationEnabled) {
				float target = ease(anim.easing, scaleElapsed, anim.rotationStart, anim.rotationEnd - anim.rotationStart, anim.duration);
				//Log.e("ani_rotanim", anim.rotationStart+","+(anim.rotationEnd - anim.rotationStart)+","+target);
				setRotationInternal(target);
			}
			
			// Find out where the focal point is at this scale and adjust its position to follow the animation path
			PointF animVCenterEnd = sourceToViewCoord(anim.sCenterEnd);
			final float dX = animVCenterEnd.x - vFocusNowX;
			final float dY = animVCenterEnd.y - vFocusNowY;
			if(pdoc.isHorizontalView()) {
				if(freeAnimation)
					vTranslate.x -= (dX * cos + dY * sin);
				vTranslate.y -= (-dX * sin + dY * cos);
			} else {
				vTranslate.x -= (dX * cos + dY * sin);
				if(freeAnimation)
					vTranslate.y -= (-dX * sin + dY * cos);
			}
			//vTranslate.x -= sourceToViewX(anim.sCenterEnd.x) - vFocusNowX;
			//vTranslate.y -= sourceToViewY(anim.sCenterEnd.y) - vFocusNowY;
			
			// For translate anims, showing the image non-centered is never allowed, for scaling anims it is during the animation.
			//nono fitToBounds(finished || (anim.scaleStart == anim.scaleEnd),false);
			//fitToBounds(false,false);
			
			//sendStateChanged(scaleBefore, vTranslateBefore, rotationBefore, anim.origin);
			
			if (finished) {
				refreshRequiredTiles(finished);
				//if (anim.listener != null) anim.listener.onComplete();
				if(shouldDrawSelection()) {
					relocateContextMenuView();
				}
				anim = null;
			}
			
			handle_proxy_simul(scaleBefore, vTranslateBefore, rotationBefore);
			if(!finished){
				if(isProxy) postSimulate();
				else invalidate();
			}
		}
	}
	
	private void postSimulate() {
		post(mAnimationRunnable);
	}
	
	/**
	 * Helper method for setting the values of a tile matrix array.
	 */
	void setMatrixArray(float[] array, float f0, float f1, float f2, float f3, float f4, float f5, float f6, float f7) {
		array[0] = f0;
		array[1] = f1;
		array[2] = f2;
		array[3] = f3;
		array[4] = f4;
		array[5] = f5;
		array[6] = f6;
		array[7] = f7;
	}
	
	/**
	 * Checks whether the base layer of tiles or full size bitmap is ready.
	 */
	private boolean isBaseLayerReady() {
		if(true) return true;
//		if (tileMap != null) {
//			boolean baseLayerReady = true;
//			for (Map.Entry<Integer, List<Tile>> tileMapEntry : tileMap.entrySet()) {
//				if (tileMapEntry.getKey() == fullImageSampleSize) {
//					for (Tile tile : tileMapEntry.getValue()) {
//						if (tile.loading || tile.bitmap == null) {
//							baseLayerReady = false;
//						}
//					}
//				}
//			}
//			return baseLayerReady;
//		}
		return false;
	}
	
	/**
	 * Check whether view and image dimensions are known and either a preview, full size image or
	 * base layer tiles are loaded. First time, send ready event to listener. The next draw will
	 * display an image.
	 */
	private boolean checkReady() {
		boolean ready = getScreenWidth() > 0 && getScreenHeight() > 0 && sWidth > 0 && sHeight > 0 && (isBaseLayerReady());
		if (!readySent && ready) {
			preDraw();
			readySent = true;
			//if (onImageEventListener != null) onImageEventListener.onReady();
			// Restart an animation that was waiting for the view to be ready
			if (pendingAnimation != null) {
				pendingAnimation.start();
				pendingAnimation = null;
			}
		}
		return ready;
	}
	
	/**
	 * Creates Paint objects once when first needed.
	 */
	private void createPaints() {
		if (bitmapPaint == null) {
			bitmapPaint = new Paint();
			bitmapPaint.setAntiAlias(false);
			bitmapPaint.setFilterBitmap(true);
			bitmapPaint.setDither(true);
			//bitmapPaint.setColorFilter(sample_fileter);
		}
		if (SSVD) {
			debugTextPaint = new Paint();
			debugTextPaint.setTextSize(px(12));
			debugTextPaint.setColor(Color.MAGENTA);
			debugTextPaint.setStyle(Style.FILL);
			debugLinePaint = new Paint();
			debugLinePaint.setColor(Color.MAGENTA);
			debugLinePaint.setStyle(Style.STROKE);
			debugLinePaint.setStrokeWidth(px(1));
		}
	}
	
	/**
	 * Loads the optimum tiles for display at the current scale and translate, so the screen can be filled with tiles
	 * that are at least as high resolution as the screen. Frees up bitmaps that are now off the screen.
	 * @param load Whether to load the new tiles needed. Use false while scrolling/panning for performance.
	 */
	
	/**
	 * Determine whether tile is visible.
	 */
	private boolean tileVisible(Tile tile, int pardon) {
		//if(true) return true;
		int sw = getScreenWidth()+pardon;
		int sh = getScreenHeight()+pardon;
		pardon=-pardon;
		if (this.rotation == 0f) {
			float sVisLeft = viewToSourceX(pardon),
					sVisRight = viewToSourceX(sw),
					sVisTop = viewToSourceY(pardon),
					sVisBottom = viewToSourceY(sh);
			return !(sVisLeft > tile.sRect.right || tile.sRect.left > sVisRight || sVisTop > tile.sRect.bottom || tile.sRect.top > sVisBottom);
		}
		
		PointF[] corners = new PointF[]{
				sourceToViewCoord(tile.sRect.left, tile.sRect.top),
				sourceToViewCoord(tile.sRect.right, tile.sRect.top),
				sourceToViewCoord(tile.sRect.right, tile.sRect.bottom),
				sourceToViewCoord(tile.sRect.left, tile.sRect.bottom),
		};
		
//		for (PointF pointF: corners) {
//			if (pointF == null) {
//				return false;
//			}
//		}
		final double rotation = this.rotation % (Math.PI * 2);
		
		if (rotation < Math.PI / 2) {
			return !(corners[0].y > sh || corners[1].x < pardon
					|| corners[2].y < pardon || corners[3].x > sw);
		} else if (rotation < Math.PI) {
			return !(corners[3].y > sh || corners[0].x < pardon
					|| corners[1].y < pardon || corners[2].x > sw);
		} else if (rotation < Math.PI * 3/2) {
			return !(corners[2].y > sh || corners[3].x < pardon
					|| corners[0].y < pardon || corners[1].x > sw);
		} else {
			return !(corners[1].y > sh || corners[2].x < pardon
					|| corners[3].y < pardon || corners[0].x > sw);
		}
		
	}
	
	/**
	 * Sets scale and translate ready for the next draw.
	 */
	private void preDraw() {
		//Log.e("fatal","preDraw");
		if (getScreenWidth() == 0 || getScreenHeight() == 0 || sWidth <= 0 || sHeight <= 0) {
			return;
		}
		
		// If waiting to translate to new center position, set translate now
		if (sPendingCenter != null && pendingScale > 0) {
			scale = pendingScale;
			CMN.Log("kiam preDraw scale="+scale+"  getWidth="+getScreenWidth()+"  getHeight="+getScreenHeight()+" width="+getWidth());
			vTranslate.x = (getScreenWidth()/2) - (scale * sPendingCenter.x);
			vTranslate.y = 0;//(getScreenHeight()/2) - (scale * sPendingCenter.y);
			sPendingCenter = null;
			pendingScale = 0;
			fitToBounds(true,true);
			refreshRequiredTiles(true);
		}
		
		// On first display of base image set up position, and in other cases make sure scale is correct.
		fitToBounds(false,false);
	}
	
	public void preDraw2(PointF _sPendingCenter, Float _pendingScale) {
		CMN.Log("fatal","preDraw2");
		
		if (sWidth <= 0 || sHeight <= 0) {
			return;
		}
		//Log.e("fatal poison", "_pendingScale "+_pendingScale);
		
		// If waiting to translate to new center position, set translate now
		if (_sPendingCenter != null && _pendingScale != null) {
			scale = _pendingScale;//getMinScale();
			//pendingScale = scale;
			//sPendingCenter = _sPendingCenter;
			Log.e("fatal","kiam preDraw2 scale="+scale+"  getWidth="+getScreenWidth()+"  getHeight="+getScreenHeight()+" width="+getWidth());
			if(pdoc.bookInfo!=null) {
				navigateTo(pdoc.bookInfo.parms, false);
			} else {
				if(pdoc.isHorizontalView()) {
					vTranslate.x = 0;
					vTranslate.y = (getScreenHeight()*1.0f/2) - (scale * _sPendingCenter.y);
				} else {
					vTranslate.x = (getScreenWidth()*1.0f/2) - (scale * _sPendingCenter.x);
					vTranslate.y = 0;//(getScreenHeight()*1.0f/2) - (scale * _sPendingCenter.y);
				}
			}
			vTranslateOrg.set(vTranslate);
			//Log.e("fatal poison", ""+getScreenWidth()+" x "+getScreenHeight());
			Log.e("fatal","preDraw2 fitToBounds1");
			fitToBounds(true,true);
			//refreshRequiredTiles(true);
			
			if(scale>0.8 && pdoc.maxPageWidth*pdoc.ThumbsHiResFactor<getScreenWidth()) {
				pdoc.ThumbsHiResFactor = Math.max(0.85f*getScreenWidth()*getScreenHeight()/(pdoc.maxPageWidth*pdoc.maxPageHeight), pdoc.ThumbsHiResFactor);
				pdoc.ThumbsHiResFactor = Math.min(2, pdoc.ThumbsHiResFactor);
			}
		}
		
		// On first display of base image set up position, and in other cases make sure scale is correct.
		fitToBounds(false,true);
	}
	
	static int RoundDown_pow2(int cap) {
		int n = cap - 1;
		n >>= 1;
		n |= n >> 1;
		n |= n >> 2;
		n |= n >> 4;
		n |= n >> 8;
		n |= n >> 16;
		return (n < 0) ? 1 : n+1;
	}
	
	
	/**
	 * Adjusts hypothetical future scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
	 * is set so one dimension fills the view and the image is centered on the other dimension. Used to calculate what the target of an
	 * animation should be.
	 * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
	 * @param sat The scale we want and the translation we're aiming for. The values are adjusted to be valid.
	 */
	private void fitToBounds_internal2(boolean center, ScaleTranslateRotate sat) {
		//if(true) return;
		// TODO: Rotation
		PointF vTranslate = sat.vTranslate;
		float scale = limitedScale(sat.scale);
		float scaleWidth = scale * sWidth;
		float scaleHeight = scale * sHeight;
		
		float sew = getScreenExifWidth();
		float seh = getScreenExifHeight();
		float se_delta = (getScreenWidth() - sew)/2;
		if (center) {
			//CMN.Log("minHeight=", getScreenExifWidth() - scaleWidth, se_delta);
			vTranslate.x = Math.max(vTranslate.x, sew + se_delta - scaleWidth);
			vTranslate.y = Math.max(vTranslate.y, seh - se_delta - scaleHeight);
		} else {
			vTranslate.x = Math.max(vTranslate.x, -scaleWidth);
			vTranslate.y = Math.max(vTranslate.y, -scaleHeight);
		}
		
		// Asymmetric padding adjustments
		float xPaddingRatio = getPaddingLeft() > 0 || getPaddingRight() > 0 ? getPaddingLeft()/(float)(getPaddingLeft() + getPaddingRight()) : 0.5f;
		float yPaddingRatio = getPaddingTop() > 0 || getPaddingBottom() > 0 ? getPaddingTop()/(float)(getPaddingTop() + getPaddingBottom()) : 0.5f;
		
		float maxTx;
		float maxTy;
		if (center) {
			maxTx = Math.max(se_delta, (getScreenWidth() - scaleWidth) * xPaddingRatio);
			maxTy = Math.max(-se_delta, (getScreenHeight() - scaleHeight) * yPaddingRatio);
		} else {
			maxTx = Math.max(0, getScreenWidth());
			maxTy = Math.max(0, getScreenHeight());
		}
		
		vTranslate.x = Math.min(vTranslate.x, maxTx);
		vTranslate.y = Math.min(vTranslate.y, maxTy);
		
		sat.scale = scale;
	}
	
	/**
	 * Adjusts hypothetical future scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
	 * is set so one dimension fills the view and the image is centered on the other dimension. Used to calculate what the target of an
	 * animation should be.
	 * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
	 * @param sat The scale we want and the translation we're aiming for. The values are adjusted to be valid.
	 */
	private void fitToBounds_internal(boolean center, ScaleTranslateRotate sat) {
		//if(true) return;
		if(true) {
			fitToBounds_internal2(center, sat);
			return;
		}
		// TODO: Rotation
		PointF vTranslate = sat.vTranslate;
		float scale = limitedScale(sat.scale);
		float scaleWidth = scale * sWidth();
		float scaleHeight = scale * sHeight();
		
		if (center) {
			vTranslate.x = Math.max(vTranslate.x, getScreenWidth() - scaleWidth);
			vTranslate.y = Math.max(vTranslate.y, getScreenHeight() - scaleHeight);
		} else {
			vTranslate.x = Math.max(vTranslate.x, -scaleWidth);
			vTranslate.y = Math.max(vTranslate.y, -scaleHeight);
		}
		
		// Asymmetric padding adjustments
		float xPaddingRatio = getPaddingLeft() > 0 || getPaddingRight() > 0 ? getPaddingLeft()/(float)(getPaddingLeft() + getPaddingRight()) : 0.5f;
		float yPaddingRatio = getPaddingTop() > 0 || getPaddingBottom() > 0 ? getPaddingTop()/(float)(getPaddingTop() + getPaddingBottom()) : 0.5f;
		
		float maxTx;
		float maxTy;
		if (center) {
			maxTx = Math.max(0, (getScreenWidth() - scaleWidth) * xPaddingRatio);
			maxTy = Math.max(0, (getScreenHeight() - scaleHeight) * yPaddingRatio);
		} else {
			maxTx = Math.max(0, getScreenWidth());
			maxTy = Math.max(0, getScreenHeight());
		}
		
		vTranslate.x = Math.min(vTranslate.x, maxTx);
		vTranslate.y = Math.min(vTranslate.y, maxTy);
		
		sat.scale = scale;
	}
	
	/**
	 * Adjusts current scale and translate values to keep scale within the allowed range and the image on screen. Minimum scale
	 * is set so one dimension fills the view and the image is centered on the other dimension.
	 * @param center Whether the image should be centered in the dimension it's too small to fill. While animating this can be false to avoid changes in direction as bounds are reached.
	 */
	private void fitToBounds(boolean center, boolean bFixScale) {
		//CMN.Log("fitToBounds ", bFixScale);
		boolean init = false;
//		if (vTranslate == null) {
//			init = true;
//			vTranslate = new PointF(0, 0);
//		}
		strTemp.scale = scale;
		strTemp.vTranslate.set(vTranslate);
		strTemp.rotate = rotation;
		fitToBounds_internal(center, strTemp);
		if(bFixScale)scale = strTemp.scale;
		vTranslate.set(strTemp.vTranslate);
		setRotationInternal(strTemp.rotate);
		if (init) {
			vTranslate.set(vTranslateForSCenter(sWidth()/2, sHeight()/2, scale));
		}
	}
	
	private void fitTmpToBounds(boolean center) {
		//CMN.Log("fitToBounds ", bFixScale);
		if (strTemp == null) {
			strTemp = new ScaleTranslateRotate(0, new PointF(0, 0), 0);
		}
		strTemp.scale = scale;
		strTemp.vTranslate.set(vTranslate);
		strTemp.rotate = rotation;
		fitToBounds_internal(center, strTemp);
	}
	
	private void fitCenter() {
	
	}
	
	@Deprecated
	public void setDocumentPath(String path) {
		setDocumentUri(Uri.fromFile(new File(path)));
	}
	
	public boolean setDocumentUri(Uri path) {
		if(path!=null && isDocTheSame(path)) {
			return false;
		}
		if(loadingTask!=null) {
			loadingTask.abort();
		}
		PDocument currentDoc = pdoc;
		if(currentDoc!=null) {
			checkDoc(getContext(), false, false);
			currentDoc.tryClose(a.getTaskId());
		}
		if(path!=null) {
			imageLoading = true;
			loadingTask = new TilesInitTask(this, path);
			loadingTask.start();
		} else {
			loadingTask = null;
		}
		return true;
	}
	
	public boolean isDocTheSame(Uri path) {
		return pdoc!=null && path.equals(pdoc.path);
	}
	
	public void setDocument(PDocument _pdoc) {
		pdoc = _pdoc;
		_pdoc.aid = a.getTaskId();
		removeCallbacks(mAnimationRunnable);
		long time = System.currentTimeMillis();
		sWidth = _pdoc.getWidth();
		sHeight = _pdoc.getHeight();
		//sOrientation = 0;
		ImgSrc = Utils.getRunTimePath(_pdoc.path);
		sOrientation = getExifOrientation(getContext(), ImgSrc);
		rotation = (float) Math.toRadians(sOrientation);
		CMN.Log("setProxy getExifOrientation", sOrientation, rotation, ImgSrc);
		
		isProxy=false;
		scale = currentMinScale();
		CMN.Log("proxy set min scale = "+scale);
		//minScale = getMinScale();
		maxScale = scale*10;
		
		preDraw2(new PointF(sWidth *1.0f/2, sHeight *1.0f/2), scale);
		
		if(mImageReadyListener!=null) {
			mImageReadyListener.NewDocOpened();
		}
//		ViewGroup.LayoutParams lp = view_to_paint.getLayoutParams();
//		lp.width=(int) (sWidth*scale);
//		lp.height=(int) (sHeight*scale);
	}
	
	/**
	 * Called by worker task when decoder is ready and image size and EXIF orientation is known.
	 */
	private void onTileInited() {
		// If actual dimensions don't match the declared size, reset everything.
		CMN.Log("onTilesInited sWidth, sHeight, sOrientation", sWidth, sHeight, orientation, scale);
		checkReady();
		refreshRequiredTiles(true);
		invalidate();
		requestLayout();
		isProxy = false;
	}
	
	/**
	 * Helper method for load tasks. Examines the EXIF info on the image file to determine the orientation.
	 * This will only work for external files, not assets, resources or other URIs.
	 */
	@AnyThread
	private int getExifOrientation(Context context, String sourceUri) {
		return ORIENTATION_0;
	}
	
	public static class TaskToken {
		final Tile tile;
		final float scale;
		final AtomicBoolean abort=new AtomicBoolean();
		final AtomicBoolean startedLoading=new AtomicBoolean();
		public boolean loading=true;
		public boolean isTaskForThisView = true;
		
		public TaskToken(Tile tile, float s) {
			this.tile = tile;
			this.scale = s;
		}
	}
	
	TileLoadingTask[] threadPool = new TileLoadingTask[8];
	int threadIter=0;
	
	public TileLoadingTask acquireFreeTask() {
		TileLoadingTask tI = null;
		boolean wildMan = false;
		int i = wildMan?0:++threadIter;
		for (int iter = 0; iter < 3; iter++) {
			i = (i+iter)%3;
			tI = threadPool[i];
			if(tI!=null) {
				tI.acquire();
				if(tI.isEnded()) {
					tI.dequire();
					threadPool[i]=null;
				} else {
					//CMN.Log("TileLoadingTask reusing…………");
					if(!wildMan)
					return tI;
				}
			}
			if(wildMan) continue;
			//CMN.Log("TileLoadingTask new task…………");
			threadPool[i] = tI = new TileLoadingTask(this);
			tI.acquire();
			return tI;
		}
		if(wildMan)
		for (int j = 0; j < 8; j++) {
			if(threadPool[j]==null) {
				threadPool[j] = tI = new TileLoadingTask(this);
				tI.acquire();
				//CMN.Log("New Thread!!!…………", i);
				break;
			}
		}
		return tI;
	}
	
	public static class TileLoadingTask implements Runnable {
		Thread t;
		private final WeakReference<PDocView> iv;
		private final AtomicBoolean ended=new AtomicBoolean();
		public final AtomicBoolean acquired=new AtomicBoolean();
		final List<TaskToken> list = Collections.synchronizedList(new ArrayList<>(32));
		int listSz;
		private boolean sleeping;
		
		public TileLoadingTask(PDocView piv) {
			this.iv = new WeakReference<>(piv);
		}
		@Override
		public void run() {
			if(ended.get()) {

				return;
			}
			PDocView piv = iv.get();
			try {
				while(true) {
				boolean seekingTaskForWho = true;
				boolean isTaskForThisView = false;
				while((listSz=list.size())>0 || acquired.get()) {
					if(listSz>0 && piv!=null) {
						TaskToken task = list.remove(listSz-1);
						if(!task.abort.get()) {
							Tile tile = task.tile;
							//CMN.Log("tile.taskToken==task", tile.taskToken==task, tile, listSz, list.size(), acquired.get(), tile.HiRes, tile.currentOwner);
							if(tile.taskToken==task) {
								if(tile instanceof RegionTile) {
									task.startedLoading.set(true);
									piv.decodeRegionAt((RegionTile)tile, task.scale);
								} else {
									piv.decodeThumbAt(tile, task.scale);
								}
								if(seekingTaskForWho) {
									if(isTaskForThisView |= task.isTaskForThisView) {
										seekingTaskForWho = false;
									}
								}
								if(piv.pageScoper!=null) {
									PDocument.PDocPage page = tile.currentOwner;
									if(page!=null && piv.pageScoper.pageInScope(page.pageIdx)) {
										piv.pageScoper.notifyItemChanged(piv, page.pageIdx);
									}
								}
								task.loading=false;
							}
						}
						//else CMN.Log("aborted");
					}
					else {
						try {
							sleeping=true;
							Thread.sleep(200);
						} catch (InterruptedException e) {
							sleeping=false;
						}
					}
				}
				if(piv.flingScroller.isFinished())
					piv.postInvalidate();
				if(piv.imageLoading && piv.mImageReadyListener!=null) {
					piv.mImageReadyListener.ImageReady();
					piv.imageLoading = false;
				}
				try {
					sleeping=true;
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					sleeping=false;
					continue;
				}
				break;
				}
			} catch (Exception e) {
				CMN.Log(e);
			}
			ended.set(true);
			if(piv!=null) {
				//piv.post(this);
			}
		}
		
		void addTask(TaskToken task) {
			list.add(task);
		}
		
		public boolean isEnded() {
			return ended.get();
		}
		
		public void acquire() {
			acquired.set(true);
		}
		
		public void dequire() {
			acquired.set(false);
		}
		
		public void startIfNeeded() {
			if(t==null&&list.size()>0) {
				t = new Thread(this, "PDocDcd");
				t.start();
			}
			if(sleeping) {
				t.interrupt();
			}
		}
	}
	
	public static class RegionTile extends Tile{
		long OffsetAlongScrollAxis;
		float scale;
		float srcRgnTop;
		float srcRgnLeft;
		float stride;
		/*0 1 2 3 4 5*/
		int x;
		/*0 1 2 3 4 5 6 7 8*/
		int y;
		RectF bkRc;
//		BackUpdata backUpdata;
//		class BackUpdata {
//			final float scale;
//			final int srcRgnTop;
//			final int srcRgnLeft;
//			final int stride;
//			BackUpdata(float scale, int srcRgnTop, int srcRgnLeft, int stride) {
//				this.scale = scale;
//				this.srcRgnTop = srcRgnTop;
//				this.srcRgnLeft = srcRgnLeft;
//				this.stride = stride;
//			}
//		}
//
//		@Override
//		public boolean resetIfOutside(PDocView pDocView, int thetaDel) {
//			long top=OffsetAlongScrollAxis+srcRgnTop;
//			int left=0+srcRgnLeft;
//			if(currentOwner!=null && (top>=pDocView.edoff || top+stride<pDocView.stoff
//				|| left>=pDocView.edoffX || left+stride<pDocView.stoffX)) {
//				CMN.Log("resetIfOutside", "[ "+pDocView.stoff+" ~ "+(top+pDocView.edoff)+" ]", "[ "+top+" ~ "+(top+256/scale)+" ]");
//				reset();
//				return true;
//			}
//			return false;
//		}
		
		public boolean resetIfOutside(PDocView pDocView, float stride) {
			long top, left;
			if(pDocView.pdoc.isHorizontalView()) {
				top= (long) (y*stride);
				left= (int) (0+x*stride+OffsetAlongScrollAxis);
			} else {
				top= (long) (OffsetAlongScrollAxis+y*stride);
				left= (int) (0+x*stride);
			}
//			if(currentOwner!=null && (currentOwner.pageIdx<pDocView.logiLayoutSt||currentOwner.pageIdx>pDocView.logiLayoutSt+pDocView.logiLayoutSz
//					||top>=pDocView.edoff||left>=pDocView.edoffX||top+stride<=pDocView.stoff||left+stride<=pDocView.stoffX)) {
			if(currentOwner!=null && (currentOwner.pageIdx<pDocView.logiLayoutSt||currentOwner.pageIdx>=pDocView.logiLayoutSt+pDocView.logiLayoutSz
					||x>currentOwner.maxX||y>currentOwner.maxY||x<currentOwner.startX||y<currentOwner.startY)) {
				reset();
				return true;
			}
			return false;
		}
		
		public boolean isOutSide(PDocView pDocView) {
			long top, left;
			if(pDocView.pdoc.isHorizontalView()) {
				top= (long) (currentOwner.getLateralOffset()+srcRgnTop);
				left= (int) (0+srcRgnLeft+OffsetAlongScrollAxis);
			} else {
				top= (long) (OffsetAlongScrollAxis+srcRgnTop);
				left= (int) (0+srcRgnLeft+currentOwner.getLateralOffset());
			}
			return (top>=pDocView.edoff || top+stride<pDocView.stoff
					|| left>=pDocView.edoffX || left+stride<pDocView.stoffX);
		}
		
		public boolean shouldDraw(PDocView pDocView) {
			long top, left;
			if(pDocView.pdoc.isHorizontalView()) {
				top= (long) (currentOwner.getLateralOffset()+srcRgnTop);
				left= (int) (0+srcRgnLeft+OffsetAlongScrollAxis);
			} else {
				top= (long) (OffsetAlongScrollAxis+srcRgnTop);
				left= (int) (0+srcRgnLeft+currentOwner.getLateralOffset());
			}
			return !(top>=pDocView.draw_edoff || top+stride<pDocView.draw_stoff
					|| left>=pDocView.draw_edoffX || left+stride<pDocView.draw_stoffX);
		}
		
		public boolean isInSide(PDocView pDocView) {
			long top, left;
			if(pDocView.pdoc.isHorizontalView()) {
				top= (long) (currentOwner.getLateralOffset()+srcRgnTop);
				left= (int) (0+srcRgnLeft+OffsetAlongScrollAxis);
			} else {
				top= (long) (OffsetAlongScrollAxis+srcRgnTop);
				left= (int) (0+srcRgnLeft+currentOwner.getLateralOffset());
			}
			return (top>=pDocView.stoff && top+stride<=pDocView.edoff
					&& left>=pDocView.stoffX && left+stride<pDocView.edoffX);
		}
		
		@Override
		public void reset() {
//			if(backUpdata!=null) {
//				backUpdata=null;
//			}
			bkRc=null;
			if(currentOwner!=null) {
				currentOwner.clearLoading(x, y);
				currentOwner = null;
			}
		}
		
		@Override
		public void assignToAsThumbnail(TileLoadingTask taskThread, PDocument.PDocPage page, float v) {
			throw new IllegalArgumentException();
		}
		
		public void assignToAsRegion(TileLoadingTask taskThread, PDocument.PDocPage page, int sX, int sY, float scale, float srcRgnTop, float srcRgnLeft, float stride) {
			reset();
			taskToken.abort.set(true);
			currentOwner=page;
			taskThread.addTask(taskToken=new TaskToken(this, scale));
			this.scale = scale;
			this.srcRgnTop = srcRgnTop;
			this.srcRgnLeft = srcRgnLeft;
			this.stride = stride;
			x = sX;
			y = sY;
			page.recordLoading(x, y, this);
		}
		
		public void restart(TileLoadingTask taskThread, PDocument.PDocPage page, int sX, int startY, float scale, float srcRgnTop, float srcRgnLeft, float stride) {
			if(!taskToken.loading&&!taskToken.abort.get()&&taskToken.startedLoading.get()) {
				bkRc=new RectF(sRect);
			}
			//bkRc=new RectF(sRect);
			taskToken.abort.set(true);
			currentOwner=page;
			taskThread.addTask(taskToken=new TaskToken(this, scale));
			this.scale = scale;
			this.srcRgnTop = srcRgnTop;
			this.srcRgnLeft = srcRgnLeft;
			this.stride = stride;
		}
	}
	
	public static class Tile {
		public ImageView iv;
		public int patchId;
		public boolean HiRes;
		protected RectF sRect = new RectF(0, 0, 0, 0);
		protected int sampleSize;
		public Bitmap bitmap;
		//private WeakReference<Bitmap> bitmapStore;
		protected boolean loading;
		protected boolean visible;
		PDocument.PDocPage currentOwner;
		TaskToken taskToken=new TaskToken(this, 1);
		
		// Volatile fields instantiated once then updated before use to reduce GC.
		protected Rect vRect = new Rect(0, 0, 0, 0);
		protected Rect fileSRect = new Rect(0, 0, 0, 0);
		
		public void clean() {
			//bitmapStore = new WeakReference<>(bitmap);
			bitmap.recycle();
			bitmap = null;
			if(iv!=null){
				iv.setImageBitmap(null);
				iv.setVisibility(View.GONE);
			}
		}
		
		public boolean missing() {
			return !visible||bitmap==null;
		}
		
		public boolean resetIfOutside(PDocView pDocView, int thetaDel) {
			if(currentOwner!=null && (currentOwner.pageIdx<pDocView.logiLayoutSt-thetaDel
					||currentOwner.pageIdx>=thetaDel+pDocView.logiLayoutSt+pDocView.logiLayoutSz)) {
				reset();
				return true;
			}
			return false;
		}
		
		public void reset() {
			if(currentOwner!=null) {
				if(currentOwner.tile==this)currentOwner.tile=null;
				if(currentOwner.tileBk==this)currentOwner.tileBk=null;
				currentOwner=null;
			}
		}
		
		public void assignToAsThumbnail(TileLoadingTask taskThread, PDocument.PDocPage page, float v) {
			taskToken.abort.set(true);
			currentOwner=page;
			taskThread.addTask(taskToken=new TaskToken(this, v));
			page.tile=this;
		}
		
		public void assignToAsAlterThumbnail(TileLoadingTask taskThread, PDocument.PDocPage page, float v) {
			taskToken.abort.set(true);
			currentOwner=page;
			taskToken=new TaskToken(this, v);
			taskToken.isTaskForThisView = false;
			taskThread.addTask(taskToken);
			page.tile=this;
		}
	}
	
	private static class Anim {
		private float scaleStart; // Scale at start of anim
		private float scaleEnd; // Scale at end of anim (target)
		private float rotationStart; // Rotation at start of anim
		private float rotationEnd; // Rotation at end o anim
		private PointF sCenterStart; // Source center point at start
		private PointF sCenterEnd; // Source center point at end, adjusted for pan limits
		private PointF sCenterEndRequested; // Source center point that was requested, without adjustment
		private PointF vFocusStart; // View point that was double tapped
		private PointF vFocusEnd; // Where the view focal point should be moved to during the anim
		private long duration = 500; // How long the anim takes
		private boolean interruptible = true; // Whether the anim can be interrupted by a touch
		private int easing = EASE_IN_OUT_QUAD; // Easing style
		private int origin = ORIGIN_ANIM; // Animation origin (API, double tap or fling)
		private long time = System.currentTimeMillis(); // Start time
	}
	
	private static class ScaleTranslateRotate {
		private ScaleTranslateRotate(float scale, PointF vTranslate, float rotate) {
			this.scale = scale;
			this.vTranslate = vTranslate;
			this.rotate = rotate;
		}
		private float scale;
		private PointF vTranslate;
		private float rotate;
	}
	
	/**
	 * Set scale, center and orientation from saved state.
	 */
	private void restoreState(ImageViewState state) {
		if (state != null) {
			//this.orientation = state.getOrientation();
			this.pendingScale = state.getScale();
			this.sPendingCenter = state.getCenter();
			setRotationInternal(0);
			invalidate();
		}
	}
	
	/**
	 * By default the View automatically calculates the optimal tile size. Set this to override this, and force an upper limit to the dimensions of the generated tiles. Passing {@link #TILE_SIZE_AUTO} will re-enable the default behaviour.
	 *
	 * @param maxPixels Maximum tile size X and Y in pixels.
	 */
	public void setMaxTileSize(int maxPixels) {
		this.maxTileWidth = maxPixels;
		this.maxTileHeight = maxPixels;
	}
	
	/**
	 * By default the View automatically calculates the optimal tile size. Set this to override this, and force an upper limit to the dimensions of the generated tiles. Passing {@link #TILE_SIZE_AUTO} will re-enable the default behaviour.
	 *
	 * @param maxPixelsX Maximum tile width.
	 * @param maxPixelsY Maximum tile height.
	 */
	public void setMaxTileSize(int maxPixelsX, int maxPixelsY) {
		this.maxTileWidth = maxPixelsX;
		this.maxTileHeight = maxPixelsY;
	}
	
	/**
	 * Use canvas max bitmap width and height instead of the default 2048, to avoid redundant tiling.
	 */
	@NonNull
	private Point getMaxBitmapDimensions(Canvas canvas) {
		return new Point(Math.min(canvas.getMaximumBitmapWidth(), maxTileWidth), Math.min(canvas.getMaximumBitmapHeight(), maxTileHeight));
	}
	
	/**
	 * Get source width taking rotation into account.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	private int sWidth() {
		return (int) sWidth;
	}
	
	@SuppressWarnings("SuspiciousNameCombination")
	private int exifWidth() {
		if (sOrientation == 90 || sOrientation == 270) {
			return (int) sHeight;
		} else {
			return (int) sWidth;
		}
	}
	
	@SuppressWarnings("SuspiciousNameCombination")
	private int exifSWidth() {
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5 ){
			return (int) sHeight;
		} else {
			return (int) sWidth;
		}
	}
	
	public int getScreenExifWidth() {
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5 ){
			//return (isProxy && dm!=null)?dm.heightPixels:getHeight();
			return getHeight();
		} else {
			//return (isProxy && dm!=null)?dm.widthPixels:getWidth();
			return getWidth();
		}
	}
	
	public int getScreenExifHeight() {
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5 ){
			//return (isProxy && dm!=null)?dm.widthPixels:getWidth();
			return getWidth();
		} else {
			//return (isProxy && dm!=null)?dm.heightPixels:getHeight();
			return getHeight();
		}
	}
	/**
	 * Get source height taking rotation into account.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	private int sHeight() {
		return (int) sHeight;
	}
	
	@SuppressWarnings("SuspiciousNameCombination")
	private int exifHeight() {
		if (sOrientation == 90 || sOrientation == 270) {
			return (int) sWidth;
		} else {
			return (int) sHeight;
		}
	}
	/**
	 * Converts source rectangle from tile, which treats the image file as if it were in the correct orientation already,
	 * to the rectangle of the image that needs to be loaded.
	 */
	@SuppressWarnings("SuspiciousNameCombination")
	@AnyThread
	private void fileSRect(Rect sRect, Rect target) {
		target.set(sRect);
	}
	
	/**
	 * Pythagoras distance between two points.
	 */
	private float distance(float x0, float x1, float y0, float y1) {
		float x = x0 - x1;
		float y = y0 - y1;
		return (float) Math.sqrt(x * x + y * y);
	}
	
	/**
	 * Releases all resources the view is using and resets the state, nulling any fields that use significant memory.
	 * After you have called this method, the view can be re-used by setting a new image. Settings are remembered
	 * but state (scale and center) is forgotten. You can restore these yourself if required.
	 */
	public void recycle() {
		// reset(true);
	}
	
	/**
	 * Convert screen to source x coordinate.
	 * NOTE: This operation corresponds to source coordinates before rotation is applied
	 */
	private float viewToSourceX(float vx) {
		return (vx - vTranslate.x)/scale;
	}
	
	private float viewToSourceX(float vx,float tx) {
		return (vx - tx)/scale;
	}
	
	private float viewToSourceX(float vx,float tx,float scale) {
		return (vx - tx)/scale;
	}
	
	private float viewToSourceX(float vx, PointF vTranslate) {
		return (vx - vTranslate.x)/scale;
	}
	
	private float viewToSourceY(float vy, PointF vTranslate) {
		return (vy - vTranslate.y)/scale;
	}
	
	private float viewToSourceY(float vy,float ty,float scale) {
		return (vy - ty)/scale;
	}
	/**
	 * Convert screen to source y coordinate.
	 * NOTE: This operation corresponds to source coordinates before rotation is applied
	 */
	private float viewToSourceY(float vy) {
		return (vy - vTranslate.y)/scale;
	}
	
	/**
	 * Converts a rectangle within the view to the corresponding rectangle from the source file, taking
	 * into account the current scale, translation, orientation and clipped region. This can be used
	 * to decode a bitmap from the source file.
	 *
	 * This method will only work when the image has fully initialised, after {@link #isReady()} returns
	 * true. It is not guaranteed to work with preloaded bitmaps.
	 *
	 * The result is written to the fRect argument. Re-use a single instance for efficiency.
	 * @param vRect rectangle representing the view area to interpret.
	 * @param fRect rectangle instance to which the result will be written. Re-use for efficiency.
	 */
	public void viewToFileRect(Rect vRect, Rect fRect) {
		if (!readySent) {
			return;
		}
		fRect.set(
				(int)viewToSourceX(vRect.left),
				(int)viewToSourceY(vRect.top),
				(int)viewToSourceX(vRect.right),
				(int)viewToSourceY(vRect.bottom));
		fileSRect(fRect, fRect);
		fRect.set(
				Math.max(0, fRect.left),
				Math.max(0, fRect.top),
				(int)Math.min(sWidth, fRect.right),
				(int)Math.min(sHeight, fRect.bottom)
		);
		if (sRegion != null) {
			fRect.offset(sRegion.left, sRegion.top);
		}
	}
	
	/**
	 * Find the area of the source file that is currently visible on screen, taking into account the
	 * current scale, translation, orientation and clipped region. This is a convenience method; see
	 * {@link #viewToFileRect(Rect, Rect)}.
	 * @param fRect rectangle instance to which the result will be written. Re-use for efficiency.
	 */
	public void visibleFileRect(Rect fRect) {
		if (!readySent) {
			return;
		}
		fRect.set(0, 0, getScreenWidth(), getScreenHeight());
		viewToFileRect(fRect, fRect);
	}
	
	/**
	 * Convert screen coordinate to source coordinate.
	 * @param vxy view X/Y coordinate.
	 * @return a coordinate representing the corresponding source coordinate.
	 */
	@Nullable
	public final PointF viewToSourceCoord(PointF vxy) {
		return viewToSourceCoord(vxy.x, vxy.y, new PointF());
	}
	
	/**
	 * Convert screen coordinate to source coordinate.
	 * @param vx view X coordinate.
	 * @param vy view Y coordinate.
	 * @return a coordinate representing the corresponding source coordinate.
	 */
	@Nullable
	public final PointF viewToSourceCoord(float vx, float vy) {
		return viewToSourceCoord(vx, vy, new PointF());
	}
	
	/**
	 * Convert screen coordinate to source coordinate.
	 * @param vxy view coordinates to convert.
	 * @param sTarget target object for result. The same instance is also returned.
	 * @return source coordinates. This is the same instance passed to the sTarget param.
	 */
	@Nullable
	public final PointF viewToSourceCoord(PointF vxy, @NonNull PointF sTarget) {
		return viewToSourceCoord(vxy.x, vxy.y, sTarget);
	}
	
	
	public final float viewToSourceFrameX(float vx, float vy, float tx, float ty,  boolean getY) {
		
		float sXPreRotate = viewToSourceX(vx, tx);
		float sYPreRotate = viewToSourceX(vy, ty);
		
		if (rotation == 0f) {
			return sXPreRotate;
		} else {
			// Calculate offset by rotation
			final float sourceVCenterX = viewToSourceX(getScreenWidth() / 2, tx);
			final float sourceVCenterY = viewToSourceX(getScreenHeight() / 2, ty);
			sXPreRotate -= sourceVCenterX;
			sYPreRotate -= sourceVCenterY;
			return getY?((float) (-sXPreRotate * sin + sYPreRotate * cos) + sourceVCenterY)
					:((float) (sXPreRotate * cos + sYPreRotate * sin) + sourceVCenterX);
		}
		
	}
	
	public final boolean viewFramesToSourceInSource(float vx_delta, float tx, float ty, boolean getY) {
		int scw = getScreenWidth();
		float vx1 = vx_delta;
		float vx2 = scw+vx_delta;
		float vy = getScreenHeight()/2;
		
		int maxX = (int) (getY?sHeight:sWidth);
		
		float sXPreRotate = viewToSourceX(vx1, tx);
		float sYPreRotate = viewToSourceX(getScreenHeight() / 2, ty);
		
		float sXAfterRotate;
		
		if (rotation == 0f) {
			if(sXPreRotate<0||sXPreRotate>maxX) return false;
			sXAfterRotate = viewToSourceX(vx2, tx);
		} else {
			// Calculate offset by rotation
			final float sourceVCenterX = viewToSourceX(getScreenWidth() / 2, tx);
			final float sourceVCenterY = viewToSourceX(getScreenHeight() / 2, ty);
			sXPreRotate -= sourceVCenterX;
			sYPreRotate -= sourceVCenterY;
			sXAfterRotate = getY?((float) (-sXPreRotate * vtParms_sin /*+ sYPreRotate * cos*/) + sourceVCenterY)
					:((float) (sXPreRotate * vtParms_cos /*+ sYPreRotate * sin*/) + sourceVCenterX);
			if(sXAfterRotate<0||sXAfterRotate>maxX) return false;
			sXPreRotate = viewToSourceX(vx2, tx);
			sXPreRotate -= sourceVCenterX;
			sXAfterRotate = getY?((float) (-sXPreRotate* vtParms_sin /* + sYPreRotate * cos*/) + sourceVCenterY)
					:((float) (sXPreRotate * vtParms_cos /*+ sYPreRotate * sin*/) + sourceVCenterX);
		}
		//CMN.Log("sXAfterRotate", sXAfterRotate, ty, vx_delta);
		return sXAfterRotate>=0 && sXAfterRotate<=maxX;
	}
	
	/**
	 * Convert screen coordinate to source coordinate.
	 * @param vx view X coordinate.
	 * @param vy view Y coordinate.
	 * @param sTarget target object for result. The same instance is also returned.
	 * @return source coordinates. This is the same instance passed to the sTarget param.
	 */
	@Nullable
	public final PointF viewToSourceCoord(float vx, float vy, @NonNull PointF sTarget) {
		
		float sXPreRotate = viewToSourceX(vx);
		float sYPreRotate = viewToSourceY(vy);
		
		if (rotation == 0f) {
			sTarget.set(sXPreRotate, sYPreRotate);
		} else {
			// Calculate offset by rotation
			final float sourceVCenterX = viewToSourceX(getScreenWidth() / 2);
			final float sourceVCenterY = viewToSourceY(getScreenHeight() / 2);
			sXPreRotate -= sourceVCenterX;
			sYPreRotate -= sourceVCenterY;
			sTarget.x = (float) (sXPreRotate * cos + sYPreRotate * sin) + sourceVCenterX;
			sTarget.y = (float) (-sXPreRotate * sin + sYPreRotate * cos) + sourceVCenterY;
		}
		
		return sTarget;
	}
	
	
	public void centerToSourceCoord(PointF tmpCenter, @NonNull PointF vTranslate) {
		float cx = tmpCenter.x;
		float cy = tmpCenter.y;
		
		float sXPreRotate = viewToSourceX(cx, vTranslate);
		float sYPreRotate = viewToSourceY(cy, vTranslate);
		
		if (rotation == 0f) {
			tmpCenter.set(sXPreRotate, sYPreRotate);
		} else {
			// Calculate offset by rotation
			final float sourceVCenterX = viewToSourceX(cx, vTranslate);
			final float sourceVCenterY = viewToSourceY(cy, vTranslate);
			sXPreRotate -= sourceVCenterX;
			sYPreRotate -= sourceVCenterY;
			tmpCenter.x = (float) (sXPreRotate * cos + sYPreRotate * sin) + sourceVCenterX;
			tmpCenter.y = (float) (-sXPreRotate * sin + sYPreRotate * cos) + sourceVCenterY;
		}
	}
	
	/**
	 * Convert source to screen x coordinate.
	 * NOTE: This operation corresponds to source coordinates before rotation is applied
	 */
	private float sourceToViewX(float sx) {
		return (sx * scale) + vTranslate.x;
	}
	
	/**
	 * Convert source to view y coordinate.
	 * NOTE: This operation corresponds to source coordinates before rotation is applied
	 */
	private float sourceToViewY(float sy) {
		return (sy * scale) + vTranslate.y;
	}
	
	/**
	 * Convert source coordinate to view coordinate.
	 * @param sxy source coordinates to convert.
	 * @return view coordinates.
	 */
	@Nullable
	public final PointF sourceToViewCoord(PointF sxy) {
		return sourceToViewCoord(sxy.x, sxy.y, new PointF());
	}
	
	/**
	 * Convert source coordinate to view coordinate.
	 * @param sx source X coordinate.
	 * @param sy source Y coordinate.
	 * @return view coordinates.
	 */
	@Nullable
	public final PointF sourceToViewCoord(float sx, float sy) {
		return sourceToViewCoord(sx, sy, new PointF());
	}
	
	/**
	 * Convert source coordinate to view coordinate.
	 * @param sxy source coordinates to convert.
	 * @param vTarget target object for result. The same instance is also returned.
	 * @return view coordinates. This is the same instance passed to the vTarget param.
	 */
	@SuppressWarnings("UnusedReturnValue")
	@Nullable
	public final PointF sourceToViewCoord(PointF sxy, @NonNull PointF vTarget) {
		return sourceToViewCoord(sxy.x, sxy.y, vTarget);
	}
	
	/**
	 * Convert source coordinate to view coordinate.
	 * @param sx source X coordinate.
	 * @param sy source Y coordinate.
	 * @param vTarget target object for result. The same instance is also returned.
	 * @return view coordinates. This is the same instance passed to the vTarget param.
	 */
	@Nullable
	public final PointF sourceToViewCoord(float sx, float sy, @NonNull PointF vTarget) {
		float xPreRotate = sourceToViewX(sx);
		float yPreRotate = sourceToViewY(sy);
		
		if (rotation == 0f) {
			vTarget.set(xPreRotate, yPreRotate);
		} else {
			// Calculate offset by rotation
			final float vCenterX = getScreenWidth() / 2;
			final float vCenterY = getScreenHeight() / 2;
			xPreRotate -= vCenterX;
			yPreRotate -= vCenterY;
			vTarget.x = (float) (xPreRotate * cos - yPreRotate * sin) + vCenterX;
			vTarget.y = (float) (xPreRotate * sin + yPreRotate * cos) + vCenterY;
		}
		
		return vTarget;
	}
	
	/**
	 * Convert source rect to screen rect, integer values.
	 */
	private void sourceToViewRect(@NonNull Rect sRect, @NonNull Rect vTarget) {
		// NOTE: Arbitrary rotation makes this impossible to implement literally, due to how Rect
		// is represented, but as this is used before rotation is applied, it doesn't matter
//		vTarget.set(
//				(int)sourceToViewX(sRect.left),
//				(int)sourceToViewY(sRect.top),
//				(int)sourceToViewX(sRect.right),
//				(int)sourceToViewY(sRect.bottom)
//		);
		vTarget.set(
				(int) (sRect.left * scale + vTranslate.x),
				(int) (sRect.top * scale + vTranslate.y),
				(int) (sRect.right * scale + vTranslate.x),
				(int) (sRect.bottom * scale + vTranslate.y)
		);
		
	}
	
	void sourceToViewRectF(@NonNull RectF sRect, @NonNull Rect vTarget) {
		// NOTE: Arbitrary rotation makes this impossible to implement literally, due to how Rect
		// is represented, but as this is used before rotation is applied, it doesn't matter
//		vTarget.set(
//				(int)sourceToViewX(sRect.left),
//				(int)sourceToViewY(sRect.top),
//				(int)sourceToViewX(sRect.right),
//				(int)sourceToViewY(sRect.bottom)
//		);
		vTarget.set(
				(int) (sRect.left * scale + vTranslate.x),
				(int) (sRect.top * scale + vTranslate.y),
				(int) (sRect.right * scale + vTranslate.x),
				(int) (sRect.bottom * scale + vTranslate.y)
		);
		
	}
	
	void sourceToViewRectFF(@NonNull RectF sRect, @NonNull RectF vTarget) {
		vTarget.set(
			sRect.left * scale + vTranslate.x,
			sRect.top * scale + vTranslate.y,
			sRect.right * scale + vTranslate.x,
			sRect.bottom * scale + vTranslate.y
		);
	}
	
	/**
	 * Get the translation required to place a given source coordinate at the center of the screen, with the center
	 * adjusted for asymmetric padding. Accepts the desired scale as an argument, so this is independent of current
	 * translate and scale. The result is fitted to bounds, putting the image point as near to the screen center as permitted.
	 */
	@NonNull
	private PointF vTranslateForSCenter(float sCenterX, float sCenterY, float scale) {
		int vxCenter = getPaddingLeft() + (getScreenWidth() - getPaddingRight() - getPaddingLeft())/2;
		int vyCenter = getPaddingTop() + (getScreenHeight() - getPaddingBottom() - getPaddingTop())/2;
		// TODO: Rotation
		strTemp.scale = scale;
		strTemp.vTranslate.set(vxCenter - (sCenterX * scale), vyCenter - (sCenterY * scale));
		fitToBounds_internal(true, strTemp);
		return strTemp.vTranslate;
	}
	
	/**
	 * Given a requested source center and scale, calculate what the actual center will have to be to keep the image in
	 * pan limits, keeping the requested center as near to the middle of the screen as allowed.
	 */
	@NonNull
	private PointF limitedSCenter(float sCenterX, float sCenterY, float scale, @NonNull PointF sTarget) {
		PointF vTranslate = vTranslateForSCenter(sCenterX, sCenterY, scale);
		int vxCenter = getPaddingLeft() + (getScreenWidth() - getPaddingRight() - getPaddingLeft())/2;
		int vyCenter = getPaddingTop() + (getScreenHeight() - getPaddingBottom() - getPaddingTop())/2;
		float sx = (vxCenter - vTranslate.x)/scale;
		float sy = (vyCenter - vTranslate.y)/scale;
		sTarget.set(sx, sy);
		return sTarget;
	}
	
	/**
	 * Returns the minimum allowed scale.
	 */
	private float minScale() {//
		int vPadding = getPaddingBottom() + getPaddingTop();
		int hPadding = getPaddingLeft() + getPaddingRight();
		float ret;
		if(pdoc.isHorizontalView()) {
			ret = (getScreenHeight() - vPadding) / (float) exifHeight();
		} else {
			ret = (getScreenWidth() - hPadding) / (float) exifWidth();
		}
		//CMN.Log("minScale", getScreenWidth(), exifWidth());
		if(ret<=0 || ret==Float.NaN) ret = 0.05f;
		return ret;
	}
	
	/**
	 * Returns the minimum allowed scale.
	 */
	private float currentMinScale() {//
		int vPadding = getPaddingBottom() + getPaddingTop();
		int hPadding = getPaddingLeft() + getPaddingRight();
		long sw = sWidth;
		long sh = sHeight;
		double r = rotation % doublePI;
		if(r>halfPI*2.5 && r<halfPI*3.5 || r>halfPI*0.5 && r<halfPI*1.5){
			sw = sHeight;
			sh = sWidth;
		}
		//CMN.Log("currentMinScale", getScreenWidth(), sw);
		if(pdoc.isHorizontalView()) {
			return (getScreenHeight() - vPadding) / (float) sh;
		} else {
			return (getScreenWidth() - hPadding) / (float) sw;
		}
	}
	
	/**
	 * Adjust a requested scale to be within the allowed limits.
	 */
	private float limitedScale(float targetScale) {
		//Log.e("anim_limitedScale",""+targetScale);
		//targetScale = Math.max(minScale(), targetScale);
		////targetScale = Math.min(maxScale, targetScale);
		return targetScale;
	}
	
	/**
	 * Apply a selected type of easing.
	 * @param type Easing type, from static fields
	 * @param time Elapsed time
	 * @param from Start value
	 * @param change Target value
	 * @param duration Anm duration
	 * @return Current value
	 */
	private float ease(int type, long time, float from, float change, long duration) {
		switch (type) {
			case EASE_IN_OUT_QUAD:
				return easeInOutQuad(time, from, change, duration);
			case EASE_OUT_QUAD:
				return easeOutQuad(time, from, change, duration);
			default:
				throw new IllegalStateException("Unexpected easing type: " + type);
		}
	}
	
	/**
	 * Quadratic easing for fling. With thanks to Robert Penner - http://gizma.com/easing/
	 * @param time Elapsed time
	 * @param from Start value
	 * @param change Target value
	 * @param duration Anm duration
	 * @return Current value
	 */
	private float easeOutQuad(long time, float from, float change, long duration) {
		float progress = (float)time/(float)duration;
		return -change * progress*(progress-2) + from;
//		float progress = (float)time/(float)duration;
//		return +change * fluidInterpolator.getInterpolation(progress) + from;
	}
	
	OverScroller flingScroller = new OverScroller(getContext());
	DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator(1f);
	ViscousFluidInterpolator fluidInterpolator = new ViscousFluidInterpolator();
	
	static class ViscousFluidInterpolator implements Interpolator {
		/** Controls the viscous fluid effect (how much of it). */
		private static final float VISCOUS_FLUID_SCALE = 8.0f;
		
		private static final float VISCOUS_FLUID_NORMALIZE;
		private static final float VISCOUS_FLUID_OFFSET;
		
		static {
			
			// must be set to 1.0 (used in viscousFluid())
			VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f);
			// account for very small floating-point error
			VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f);
		}
		
		private static float viscousFluid(float x) {
			x *= VISCOUS_FLUID_SCALE;
			if (x < 1.0f) {
				x -= (1.0f - (float)Math.exp(-x));
			} else {
				float start = 0.36787944117f;   // 1/e == exp(-1)
				x = 1.0f - (float)Math.exp(1.0f - x);
				x = start + x * (1.0f - start);
			}
			return x;
		}
		
		@Override
		public float getInterpolation(float input) {
			final float interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input);
			if (interpolated > 0) {
				return interpolated + VISCOUS_FLUID_OFFSET;
			}
			return interpolated;
		}
	}
	
	/**
	 * Quadratic easing for scale and center animations. With thanks to Robert Penner - http://gizma.com/easing/
	 * @param time Elapsed time
	 * @param from Start value
	 * @param change Target value
	 * @param duration Anm duration
	 * @return Current value
	 */
	private float easeInOutQuad(long time, float from, float change, long duration) {
		float timeF = time/(duration/2f);
		if (timeF < 1) {
			return (change/2f * timeF * timeF) + from;
		} else {
			timeF--;
			return (-change/2f) * (timeF * (timeF - 2) - 1) + from;
		}
	}
	
	/**
	 * Debug logger
	 */
	@AnyThread
	private void debug(String message, Object... args) {
		if (SSVD) {
			Log.d(TAG, String.format(message, args));
		}
	}
	
	/**
	 * For debug overlays. Scale pixel value according to screen density.
	 */
	private int px(int px) {
		return (int)(density * px);
	}

	/**
	 * Returns the minimum allowed scale.
	 * @return the minimum scale as a source/view pixels ratio.
	 */
	public final float getMinScale() {
		return minScale();
	}
	
	/**
	 * Returns the source point at the center of the view.
	 * @return the source coordinates current at the center of the view.
	 */
	@Nullable
	public final PointF getCenter() {
		int mX = getScreenWidth()/2;
		int mY = getScreenHeight()/2;
		return viewToSourceCoord(mX, mY);
	}
	
	/**
	 * Returns the current scale value.
	 * @return the current scale as a source/view pixels ratio.
	 */
	public final float getScale() {
		return scale;
	}
	
	/**
	 * Externally change the scale and translation of the source image. This may be used with getCenter() and getScale()
	 * to restore the scale and zoom after a screen rotate.
	 * @param scale New scale to set.
	 * @param sCenter New source image coordinate to center on the screen, subject to boundaries.
	 */
	public final void setScaleAndCenter(float scale, @Nullable PointF sCenter) {
		this.anim = null;
		this.pendingScale = scale;
		this.sPendingCenter = sCenter;
		this.sRequestedCenter = sCenter;
		invalidate();
	}
	
	/**
	 * Returns the current rotation value in degrees
	 */
	public final float getRotationDeg() {
		return (float) Math.toDegrees(rotation);
	}
	
	/**
	 * Returns the current rotation value in radians.
	 */
	public final float getRotationRad() {
		return rotation;
	}
	
	/**
	 * Externally change the rotation around the view center
	 * @param rot Rotation around view center in degrees
	 */
	public final void setRotationDeg(float rot) {
		setRotationInternal((float) Math.toRadians(rot));
		invalidate();
	}
	
	/**
	 * Externally change the rotation around the view center
	 * @param rot Rotation around view center in radians
	 */
	public final void setRotationRad(float rot) {
		setRotationInternal(rot);
		invalidate();
	}
	
	/**
	 * Sets rotation without invalidation
	 */
	private void setRotationInternal(float rot) {
		// Normalize rotation between 0..2pi
		this.rotation = rot % (float) (Math.PI * 2);
		if (this.rotation < 0) this.rotation += Math.PI * 2;
		
		this.cos = Math.cos(rot);
		this.sin = Math.sin(rot);
	}
	
	
	/**
	 * Fully zoom out and return the image to the middle of the screen. This might be useful if you have a view pager
	 * and want images to be reset when the user has moved to another page.
	 */
	public final void resetScaleAndCenter() {
		this.anim = null;
		this.pendingScale = limitedScale(0);
		if (isReady()) {
			this.sPendingCenter = new PointF(sWidth()/2, sHeight()/2);
		} else {
			this.sPendingCenter = new PointF(0, 0);
		}
		invalidate();
	}
	
	/**
	 * Call to find whether the view is initialised, has dimensions, and will display an image on
	 * the next draw. If a preview has been provided, it may be the preview that will be displayed
	 * and the full size image may still be loading. If no preview was provided, this is called once
	 * the base layer tiles of the full size image are loaded.
	 * @return true if the view is ready to display an image and accept touch gestures.
	 */
	public final boolean isReady() {
		return readySent;
	}
	
	/**
	 * Called once when the full size image or its base layer tiles have been loaded.
	 */
	@SuppressWarnings("EmptyMethod")
	protected void onImageLoaded() {
	
	}
	
	/**
	 * Get source width, ignoring orientation. If {@link #orientation} returns 90 or 270, you can use {@link #getSHeight()}
	 * for the apparent width.
	 * @return the source image width in pixels.
	 */
	public final int getSWidth() {
		return (int) sWidth;
	}
	
	/**
	 * Get source height, ignoring orientation. If {@link #orientation} returns 90 or 270, you can use {@link #getSWidth()}
	 * for the apparent height.
	 * @return the source image height in pixels.
	 */
	public final int getSHeight() {
		return (int) sHeight;
	}
	
	/**
	 * Get the current state of the view (scale, center, orientation) for restoration after rotate. Will return null if
	 * the view is not ready.
	 * @return an {@link ImageViewState} instance representing the current position of the image. null if the view isn't ready.
	 */
	@Nullable
	public final ImageViewState getState() {
		if (sWidth > 0 && sHeight > 0) {
			//noinspection ConstantConditions
			return new ImageViewState(getScale(), getCenter(), rotation);
		}
		return null;
	}
	
	/**
	 * Returns true if zoom gesture detection is enabled.
	 * @return true if zoom gesture detection is enabled.
	 */
	public final boolean isZoomEnabled() {
		return zoomEnabled;
	}
	
	/**
	 * Enable or disable zoom gesture detection. Disabling zoom locks the the current scale.
	 * @param zoomEnabled true to enable zoom gestures, false to disable.
	 */
	public final void setZoomEnabled(boolean zoomEnabled) {
		this.zoomEnabled = zoomEnabled;
	}
	
	/**
	 <<<<<<< HEAD
	 * Returns true if rotation gesture detection is enabled
	 */
	public final boolean isRotationEnabled() {
		return rotationEnabled;
	}
	
	/**
	 * Enable or disable rotation gesture detection. Disabling locks the current rotation.
	 */
	public final void setRotationEnabled(boolean rotationEnabled) {
		this.rotationEnabled = rotationEnabled;
	}
	
	/**
	 * Returns true if double tap & swipe to zoom is enabled.
	 =======
	 * Returns true if double tap &amp; swipe to zoom is enabled.
	 >>>>>>> master
	 */
	public final boolean isQuickScaleEnabled() {
		return quickScaleEnabled;
	}
	
	/**
	 * Enable or disable double tap &amp; swipe to zoom.
	 * @param quickScaleEnabled true to enable quick scale, false to disable.
	 */
	public final void setQuickScaleEnabled(boolean quickScaleEnabled) {
		this.quickScaleEnabled = quickScaleEnabled;
	}
	
	/**
	 * Returns true if pan gesture detection is enabled.
	 */
	public final boolean isPanEnabled() {
		return panEnabled;
	}
	
	/**
	 * Enable or disable pan gesture detection. Disabling pan causes the image to be centered. Pan
	 * can still be changed from code.
	 * @param panEnabled true to enable panning, false to disable.
	 */
	public final void setPanEnabled(boolean panEnabled) {
		this.panEnabled = panEnabled;
		if (!panEnabled) {
			// TODO: Rotation?
			vTranslate.x = (getScreenWidth()/2) - (scale * (sWidth()/2));
			vTranslate.y = (getScreenHeight()/2) - (scale * (sHeight()/2));
			if (isReady()) {
				refreshRequiredTiles(true);
				invalidate();
			}
		}
	}
	
	/**
	 * Set a solid color to render behind tiles, useful for displaying transparent PNGs.
	 * @param tileBgColor Background color for tiles.
	 */
	public final void setTileBackgroundColor(int tileBgColor) {
		if (Color.alpha(tileBgColor) == 0) {
			tileBgPaint = null;
		} else {
			tileBgPaint = new Paint();
			tileBgPaint.setStyle(Style.FILL);
			tileBgPaint.setColor(tileBgColor);
		}
		invalidate();
	}
	
	/**
	 * <p>
	 * Provide an {@link Executor} to be used for loading images. By default, {@link AsyncTask#THREAD_POOL_EXECUTOR}
	 * is used to minimise contention with other background work the app is doing. You can also choose
	 * to use {@link AsyncTask#SERIAL_EXECUTOR} if you want to limit concurrent background tasks.
	 * Alternatively you can supply an {@link Executor} of your own to avoid any contention. It is
	 * strongly recommended to use a single executor instance for the life of your application, not
	 * one per view instance.
	 * </p><p>
	 * <b>Warning:</b> If you are using a custom implementation of {@link ImageRegionDecoder}, and you
	 * supply an executor with more than one thread, you must make sure your implementation supports
	 * multi-threaded bitmap decoding or has appropriate internal synchronization. From SDK 21, Android's
	 * {@link android.graphics.BitmapRegionDecoder} uses an internal lock so it is thread safe but
	 * there is no advantage to using multiple threads.
	 * </p>
	 * @param executor an {@link Executor} for image loading.
	 */
	public void setExecutor(@NonNull Executor executor) {
		this.executor = executor;
	}
	
	/**
	 * Enable or disable eager loading of tiles that appear on screen during gestures or animations,
	 * while the gesture or animation is still in progress. By default this is enabled to improve
	 * responsiveness, but it can result in tiles being loaded and discarded more rapidly than
	 * necessary and reduce the animation frame rate on old/cheap devices. Disable this on older
	 * devices if you see poor performance. Tiles will then be loaded only when gestures and animations
	 * are completed.
	 * @param eagerLoadingEnabled true to enable loading during gestures, false to delay loading until gestures end
	 */
	public void setEagerLoadingEnabled(boolean eagerLoadingEnabled) {
		this.eagerLoadingEnabled = eagerLoadingEnabled;
	}
	
	/**
	 * Enables visual debugging, showing tile boundaries and sizes.
	 * @param debug true to enable debugging, false to disable.
	 */
	public final void setDebug(boolean debug) {
		this.SSVD = debug;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
		this.onLongClickListener = onLongClickListener;
	}
	
	/**
	 * Creates a panning animation builder, that when started will animate the image to place the given coordinates of
	 * the image in the center of the screen. If doing this would move the image beyond the edges of the screen, the
	 * image is instead animated to move the center point as near to the center of the screen as is allowed - it's
	 * guaranteed to be on screen.
	 * @param sCenter Target center point
	 * @return {@link AnimationBuilder} instance. Call {@link PDocView.AnimationBuilder#start()} to start the anim.
	 */
	@Nullable
	public AnimationBuilder animateCenter(PointF sCenter) {
		if (!isReady()) {
			return null;
		}
		return new AnimationBuilder(sCenter);
	}
	
	/**
	 * Creates a scale animation builder, that when started will animate a zoom in or out. If this would move the image
	 * beyond the panning limits, the image is automatically panned during the animation.
	 * @param scale Target scale.
	 * @return {@link AnimationBuilder} instance. Call {@link PDocView.AnimationBuilder#start()} to start the anim.
	 */
	@Nullable
	public AnimationBuilder animateScale(float scale) {
		if (!isReady()) {
			return null;
		}
		return new AnimationBuilder(scale);
	}
	
	/**
	 * Creates a scale animation builder, that when started will animate a zoom in or out. If this would move the image
	 * beyond the panning limits, the image is automatically panned during the animation.
	 * @param scale Target scale.
	 * @param sCenter Target source center.
	 * @return {@link AnimationBuilder} instance. Call {@link PDocView.AnimationBuilder#start()} to start the anim.
	 */
	@Nullable
	public AnimationBuilder animateScaleAndCenter(float scale, PointF sCenter) {
		if (!isReady()) {
			return null;
		}
		return new AnimationBuilder(scale, sCenter);
	}
	
	public AnimationBuilder animateSTR(float scale, PointF sCenter, float rotation) {
		if (!isReady()) {
			return null;
		}
		return new AnimationBuilder(scale, sCenter, rotation);
	}
	
	/**
	 * Builder class used to set additional options for a scale animation. Create an instance using {@link #animateScale(float)},
	 * then set your options and call {@link #start()}.
	 */
	public final class AnimationBuilder {
		private final float targetScale;
		private final PointF targetSCenter;
		private final float targetRotation;
		private final PointF vFocus;
		private long duration = 500;
		private int easing = EASE_IN_OUT_QUAD;
		private int origin = ORIGIN_ANIM;
		private boolean interruptible = true;
		private boolean panLimited = true;
		
		private AnimationBuilder(PointF sCenter) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		private AnimationBuilder(float scale) {
			this.targetScale = scale;
			this.targetSCenter = getCenter();
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		private AnimationBuilder(float scale, PointF sCenter) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		private AnimationBuilder(float scale, PointF sCenter, PointF vFocus) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = vFocus;
		}
		
		private AnimationBuilder(PointF sCenter, float rotation) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		private AnimationBuilder(float scale, PointF sCenter, float rotation) {
			this.targetScale = scale;
			this.targetSCenter = sCenter;
			this.targetRotation = rotation;
			this.vFocus = null;
		}
		
		/**
		 * Desired duration of the anim in milliseconds. Default is 500.
		 * @param duration duration in milliseconds.
		 * @return this builder for method chaining.
		 */
		@NonNull
		public AnimationBuilder withDuration(long duration) {
			this.duration = duration;
			return this;
		}
		
		/**
		 * Whether the animation can be interrupted with a touch. Default is true.
		 * @param interruptible interruptible flag.
		 * @return this builder for method chaining.
		 */
		@NonNull
		public AnimationBuilder withInterruptible(boolean interruptible) {
			this.interruptible = interruptible;
			return this;
		}
		
		/**
		 * Set the easing style. See static fields. {@link #EASE_IN_OUT_QUAD} is recommended, and the default.
		 * @param easing easing style.
		 * @return this builder for method chaining.
		 */
		@NonNull
		public AnimationBuilder withEasing(int easing) {
			this.easing = easing;
			return this;
		}
		
		/**
		 * Only for internal use. When set to true, the animation proceeds towards the actual end point - the nearest
		 * point to the center allowed by pan limits. When false, animation is in the direction of the requested end
		 * point and is stopped when the limit for each axis is reached. The latter behaviour is used for flings but
		 * nothing else.
		 */
		@NonNull
		private AnimationBuilder withPanLimited(boolean panLimited) {
			this.panLimited = panLimited;
			return this;
		}
		
		/**
		 * Only for internal use. Indicates what caused the animation.
		 */
		@NonNull
		private AnimationBuilder withOrigin(int origin) {
			this.origin = origin;
			return this;
		}
		
		/**
		 * Starts the animation.
		 */
		public void start() {
			// Make sure the view has dimensions and something to draw before starting animations.
			// Otherwise for example calls to sourceToViewCoord() may return null and cause errors.
			if (!isProxy && !checkReady()) {
				pendingAnimation = AnimationBuilder.this;
				anim = null;
				return;
			}
			//if (anim != null && anim.listener != null) anim.listener.onInterruptedByNewAnim();
			
			int vxCenter = getPaddingLeft() + (getScreenWidth() - getPaddingRight() - getPaddingLeft())/2;
			int vyCenter = getPaddingTop() + (getScreenHeight() - getPaddingBottom() - getPaddingTop())/2;
			float targetScale = limitedScale(this.targetScale);
			PointF targetSCenter = panLimited ? limitedSCenter(this.targetSCenter.x, this.targetSCenter.y, targetScale, new PointF()) : this.targetSCenter;
			anim = new Anim();
			anim.scaleStart = scale;
			anim.scaleEnd = targetScale;
			anim.rotationStart = rotation;
			anim.rotationEnd = targetRotation;
			anim.time = System.currentTimeMillis();
			anim.sCenterEndRequested = targetSCenter;
			anim.sCenterStart = getCenter();
			anim.sCenterEnd = targetSCenter;
			anim.vFocusStart = sourceToViewCoord(targetSCenter);
			anim.vFocusEnd = new PointF(
					vxCenter,
					vyCenter
			);
			anim.duration = duration;
			anim.interruptible = interruptible;
			anim.easing = easing;
			anim.origin = origin;
			anim.time = System.currentTimeMillis();
			
			if (vFocus != null) {
				// Calculate where translation will be at the end of the anim
				float vTranslateXEnd = vFocus.x - (targetScale * anim.sCenterStart.x);
				float vTranslateYEnd = vFocus.y - (targetScale * anim.sCenterStart.y);
				ScaleTranslateRotate satEnd = new ScaleTranslateRotate(targetScale, new PointF(vTranslateXEnd, vTranslateYEnd), targetRotation);
				// Fit the end translation into bounds
				fitToBounds_internal(true, satEnd);
				
				// Adjust the position of the focus point at end so image will be in bounds
				anim.vFocusEnd = new PointF(
						vFocus.x + (satEnd.vTranslate.x - vTranslateXEnd),
						vFocus.y + (satEnd.vTranslate.y - vTranslateYEnd)
				);
			}
			freeAnimation=true;
			
			if(isProxy){
				postSimulate();
			} else {
				invalidate();
			}
		}
		
	}
	
	public int getScreenWidth() {
		int ret = getWidth();
		if(ret==0&&dm!=null) ret = dm.widthPixels;
		return ret;
	}
	
	public int getScreenHeight() {
		int ret = getHeight();
		if(ret==0&&dm!=null) ret = dm.heightPixels;
		return ret;
	}
	
	
	public void checkDoc(Context a, boolean incremental, boolean reload) {
		if(pdoc!=null) { //syncstats
			if(mImageReadyListener!=null && pdoc.bookInfo!=null && (pdoc.isBookInfoDirty()||true)) {
				int pageIdx = getCurrentPageOnScreen();
				PDocument.PDocPage page = pdoc.mPDocPages[pageIdx];
				int offsetX, offsetY;
				offsetX = (int) (vTranslate.x/scale - page.getLateralOffset());
				offsetY = (int) (-vTranslate.y/scale - page.OffsetAlongScrollAxis);
				pdoc.bookInfo.setParms(pageIdx, offsetX, offsetY, scale);
				if(pdoc.bookInfo.isDirty) {
					mImageReadyListener.saveBookInfo(pdoc.bookInfo);
				}
			}
			if(pdoc.isDirty) {
				pdoc.saveDocAsCopy(a, null, incremental, reload);
			}
		}
	}
	

	
}