/*
 * Copyright (C) 2017 Angad Singh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.filepicker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.knziha.filepicker.R;

/**
 * <p>
 * Created by Angad on 19-05-2017.
 * </p>
 */

public class MaterialCheckbox extends View {
    private Context context;
    private int paddingRight;
    private int minDim;
    private int delta;
    private Paint paint;
    private RectF bounds;
    private int checked;
    private OnCheckedChangeListener onCheckedChangeListener;
    private Path tick;
    public int bgInner = 0xFFFFFFFF;//FDFDFE
    private int bgFrame = 0xFFC1C1C1;
    boolean bIsInflated;

    public MaterialCheckbox(Context context) {
        this(context, null);
        initView(context, null);
    }

    public MaterialCheckbox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    public void initView(Context context, AttributeSet attrs) {
        this.context = context;
        checked = 0;
        tick = new Path();
        paint = new Paint();
        bounds = new RectF();
        OnClickListener onClickListener = v -> {
			if(bIsInflated){
				setChecked(checked!=1);
				onCheckedChangeListener.onCheckedChanged(MaterialCheckbox.this, isChecked(), false);
			}else{
				setChecked(true);
				onCheckedChangeListener.onCheckedChanged(MaterialCheckbox.this, isChecked(), true);
			}
		};

        setOnClickListener(onClickListener);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(bIsInflated) {
            if (checked > 0) {
                //居中处理
                paint.reset();
                paint.setAntiAlias(true);
                float height = minDim - minDim / 10 * 2;
                bounds.set(minDim / 10, (delta - height) / 2, minDim - (minDim / 10), height + (delta - height) / 2);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    paint.setColor(getResources().getColor(R.color.colorAccent, context.getTheme()));
                } else {
                    paint.setColor(getResources().getColor(R.color.colorAccent));
                }
                canvas.drawRoundRect(bounds, minDim / 8, minDim / 8, paint);

                paint.setColor(bgInner);
                paint.setStrokeWidth(minDim / 10);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.BEVEL);
                if (checked == 1) {
                    canvas.drawPath(tick, paint);
                }
            } else {
                //居中处理
                paint.reset();
                paint.setAntiAlias(true);
                int height = minDim - minDim / 10 * 2;
                bounds.set(minDim / 10, (delta - height) / 2, minDim - (minDim / 10), height + (delta - height) / 2);
                paint.setColor(bgFrame);
                canvas.drawRoundRect(bounds, minDim / 8, minDim / 8, paint);


                height = minDim - minDim / 5 * 2;
                bounds.set(minDim / 5, (delta - height) / 2, minDim - (minDim / 5), height + (delta - height) / 2);
                paint.setColor(bgInner);
                //paint.setColor(0);
                //paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                canvas.drawRect(bounds, paint);
            }
        }else{
            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(bgFrame);
            canvas.drawCircle(minDim/2,delta/2,3*getResources().getDisplayMetrics().density, paint);
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        paddingRight = getPaddingRight();
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        minDim = Math.min(width, height)-paddingRight;
        delta = height;
        bounds.set(minDim / 10, minDim / 10, minDim - (minDim/10), minDim - (minDim/10));
        moveTick();
        setMeasuredDimension(width, height);
    }
  
    void moveTick(){
    	tick.reset();//!!! important
    	float heightD = (delta - minDim)/2 ;
        tick.moveTo(minDim / 4,heightD + minDim / 2);
        tick.lineTo(minDim / 2.5f, heightD +minDim - (minDim / 3));

        tick.moveTo(minDim / 2.75f, heightD +minDim - (minDim / 3.25f));
        tick.lineTo(minDim - (minDim / 4), heightD +minDim / 3);
    }
    
    public boolean isChecked() {
        return checked==1;
    }
  
    
    public void setChecked(boolean _checked) {
    	int val = _checked?1:0;
    	if(checked != val){
			checked=val;
			invalidate();
		}
    }
    
    public void setHalfChecked() {
        checked = 2;
        invalidate();
    }
    public boolean isHalfChecked() {
        return checked>=2;
    }
    
    public void setOnCheckedChangedListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public boolean isInflated() {
        return bIsInflated;
    }

    public void setInflated(boolean val) {
        if(bIsInflated!=val){
            bIsInflated = val;
            invalidate();
        }
    }
}
