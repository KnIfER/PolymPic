/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2019 KnIfER
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
package com.knziha.filepicker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;


public class TextViewSplitter extends TextView {
    public TextViewSplitter(Context context) {
        super(context);
    }

    public TextViewSplitter(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewSplitter(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint textPaint = getPaint();
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float top = fontMetrics.ascent;
        float bottom = fontMetrics.descent;

        float density = getResources().getDisplayMetrics().density;
        float textwidth = textPaint.measureText(getText().toString());
        float textcenter = (getHeight()) / 2;
        //Paint slinepainter = new Paint();
        //slinepainter.setColor(Color.WHITE);
        textPaint.setStrokeWidth(density*1);
        float pad = density*2;
        canvas.drawLine(0,textcenter,(getWidth()-textwidth)/2-pad,textcenter,textPaint);
        canvas.drawLine((getWidth()+textwidth)/2+pad,textcenter,getWidth(),textcenter,textPaint);
    }
}
