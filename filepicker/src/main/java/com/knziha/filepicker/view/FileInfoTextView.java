package com.knziha.filepicker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.Nullable;

import mp4meta.utils.CMN;

public class FileInfoTextView extends TextView {
    public String subText;
    private Paint bgPaint;
    private int oldColor=0;

    public FileInfoTextView(Context context) {
        this(context, null);
    }

    public FileInfoTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textStyle);
    }

    public FileInfoTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    float density = getResources().getDisplayMetrics().density;
    @Override
    protected void onDraw(Canvas canvas) {
        if(subText!=null) {
            Paint textPaint = getPaint();
            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
            float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
            float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
            String tailing = subText;
            float LengthSub = textPaint.measureText(tailing);
            if(oldColor!=0)textPaint.setColor(oldColor);
            canvas.drawText(tailing, getWidth() - LengthSub - 3 * density - getPaddingLeft(), (getHeight() - top - bottom) / 2 - 0 * density, textPaint);
            if(LengthSub>=getWidth()*2.f/3){
                getBgPaint();
                canvas.drawRect(0,0,textPaint.measureText(getText().toString()),getHeight(),bgPaint);
                setTextColor(Color.WHITE);
            }
            //TODO optmise
        }
        super.onDraw(canvas);
    }

    private void getBgPaint() {
        if(bgPaint==null){
            bgPaint = new Paint();
            bgPaint.setColor(0xe8667686);
            oldColor=getTextColors().getDefaultColor();
        }
    }
}
