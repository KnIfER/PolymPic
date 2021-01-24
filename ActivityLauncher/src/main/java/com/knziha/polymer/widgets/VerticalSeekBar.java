/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2017 KnIfER
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

package com.knziha.polymer.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import java.util.ArrayList;


/**
 * 姬动人心的 Vertical SeekBar！<br>
 * 最初从网络各处辗转粘贴而来。<br>
 * Exciting ! Fantastic ! <br>
 * @attr ref android.R.styleable#VerticalSeekBar_thumb
 */
public class VerticalSeekBar extends SeekBar{
    public static boolean bForbidRequestLayout;

    private Drawable mThumb;
    private Paint mPaint;
    private Rect bounds;
    //public TreeMap<Long,String> treev2;
    public long timeLength;
    private float xLeft;
    private float xRight;
    private float scale;
    boolean bIsThumbHidden=false;
    /** Some bottom brands*/
    public ArrayList<String> btmTags = new ArrayList<>();

    private OnSeekBarChangeListener mOnSeekBarChangeListener;

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener o){
        super.setOnSeekBarChangeListener(o);
        mOnSeekBarChangeListener = o;
    }

    public VerticalSeekBar(Context context) {
        this(context, null);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.seekBarStyle);
    }

    public VerticalSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ini();
    }

    public void ini(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(1f);
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(9.666f * getResources().getDisplayMetrics().density);//29
        bounds = new Rect();
        //CMN.Log("sadsadasd",getResources().getDisplayMetrics().density);
    }

    @Override
    protected
    void onDraw(Canvas canvas) {
        // 反转90度，将水平SeekBar竖起来
        canvas.rotate(-90);
        // 画布移到正确的位置,注意宽高互换
        canvas.translate(-getHeight(), 0);
        // 有点难懂奥
        super.onDraw(canvas);

        xLeft = getPaddingTop();
        xRight = getMeasuredHeight() - getPaddingBottom();

        float anchorY = 5 * getResources().getDisplayMetrics().density;
        canvas.rotate(90, 0, 0);
        for(String btmTag:btmTags){
            mPaint.getTextBounds(btmTag, 0, btmTag.length(), bounds);
            int gap = (getWidth()-bounds.width())/2;
            if(gap<0)gap=0;
            //drawText(canvas, btmTag,bounds, -bounds.height(), 80.f, mPaint, 90);
            canvas.drawText(btmTag,bounds.centerX()+gap,-anchorY, mPaint);
            anchorY += bounds.height() + 5 * getResources().getDisplayMetrics().density;
        }
        canvas.rotate(-90);
    }
    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumb = thumb;
        if(mThumb!=null) {
            if(bIsThumbHidden) mThumb.setAlpha(0);
        }
    }

    void drawText(Canvas canvas ,String text , Rect bounds, float x ,float y,Paint paint ,float angle){
        if(angle != 0){
            canvas.rotate(angle, x, y);
        }
        if(bounds==null) {
            bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
        }
        //Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        //float baseline = mPaint.getTextSize()/2 - mPaint.getFontMetrics().descent;

        //canvas.drawText(text, x, y, paint);
        canvas.drawText(text,x-bounds.centerX(),bounds.height(), paint);
        if(angle != 0){
            canvas.rotate(-angle, x, y);
        }
    }

    @Override
    public void setThumbOffset(int thumbOffset) {//截他一胡 for kitka
        super.setThumbOffset(mThumb!=null?mThumb.getIntrinsicWidth()/2:thumbOffset);
    }

    @SuppressWarnings("")
    private void setThumbPos(int w, Drawable thumb, float scale) {
        int available = w - getPaddingLeft() - getPaddingRight();
        int thumbWidth = thumb.getIntrinsicWidth();
        int thumbHeight = thumb.getIntrinsicHeight();
        available -= thumbWidth;

        // The extra space for the thumb to move on the track  
        available += getThumbOffset() * 2;

        int thumbPos = (int) (scale * available);

        int gap = (getWidth()-thumbHeight)/2;

        int topBound = gap;
        int bottomBound = gap + thumbHeight;

        thumb.setBounds(thumbPos, topBound, thumbPos + thumbWidth, bottomBound);
        final Drawable background = getBackground();
        if(background!=null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            gap = background.getIntrinsicHeight()/2;
            int top = getHeight() - thumbPos - gap - getPaddingLeft();
            background.setHotspotBounds(topBound, top, bottomBound, top + background.getIntrinsicHeight());
            background.setHotspot(topBound, thumbPos);
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());// 宽高值互换  
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldw, oldh);// 宽高值互换
        UpdateMyThumb();
    }

    void onStartTrackingTouch() {
        if (mOnSeekBarChangeListener != null) mOnSeekBarChangeListener.onStartTrackingTouch(this);
    }

    void onStopTrackingTouch() {
        if (mOnSeekBarChangeListener != null) mOnSeekBarChangeListener.onStopTrackingTouch(this);
    }

    public float lastX,lastY;
    // 与源码完全相同，仅为调用宽高值互换处理的 onStartTrackingTouch()方法
    // 最新源码 使用 mIsDragging 标志， 不必跟进
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        lastX = event.getX();
        lastY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                setPressed(true);
                onStartTrackingTouch();
                trackTouchEvent(event);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                trackTouchEvent(event);
                attemptClaimDrag();
                break;
            }
            case MotionEvent.ACTION_UP: {
                trackTouchEvent(event);
                onStopTrackingTouch();
                setPressed(false);
                // ProgressBar doesn't know to repaint the thumb drawable
                // in its inactive state when the touch stops (because the
                // value has not apparently changed)
                invalidate();
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                onStopTrackingTouch();
                setPressed(false);
                invalidate(); // see above explanation
                break;
            }
            default: break;
        }
        return true;
    }

    // 宽高值互换处理  
    public void trackTouchEvent(MotionEvent event) {
        final int height = getHeight();
        //final int available = height - getPaddingBottom() - getPaddingTop();
        final int available = height  - getPaddingLeft()- getPaddingRight();
        int Y = (int) event.getY();

        float progress;
        if (Y > height - getPaddingLeft()) {
            scale = 0.0f;
        } else if (Y < getPaddingRight()) {
            scale = 1.0f;
        } else {
            scale = (float) (height - getPaddingLeft() - Y) / (float) available;
        }
        int min = Build.VERSION.SDK_INT>26?getMin():0;
        progress = min + scale * (getMax() - min);
        //onProgressRefresh(progress, false);

        setProgress((int) progress);
        if(mThumb!=null)
            setThumbPos(getHeight(), mThumb, scale);
    }

    public void UpdateMyThumb(){
        if(mThumb!=null) {
            setThumbPos(getHeight(), mThumb, getProgress() * 1.f / getMax());
        }
    }

    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    @Override
    public void requestLayout() {
        if(!bForbidRequestLayout)
            super.requestLayout();
    }
}
