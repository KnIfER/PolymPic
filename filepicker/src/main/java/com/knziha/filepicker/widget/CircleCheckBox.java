package com.knziha.filepicker.widget;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;


import com.knziha.filepicker.R;
import com.knziha.filepicker.utils.CMNF;

import java.lang.reflect.Field;
import java.util.ArrayList;

//com.litao.android.checkbox_sample.CheckBoxSample

/**
 * Created by litao on 16/4/6.
 */
public class CircleCheckBox extends View implements Checkable {

    private final static float BOUNCE_VALUE = 0.2f;

    private ArrayList<Drawable> checkDrawables;

    private Paint bitmapPaint;
    private Paint bitmapEraser;
    private Paint checkEraser;
    private Paint borderPaint;

    private Bitmap drawBitmap;
    private Bitmap checkBitmap;
    private Canvas bitmapCanvas;
    private Canvas checkCanvas;

    private float progress;
    private ObjectAnimator checkAnim;

    private boolean attachedToWindow;
    //public boolean isChecked;
    private int checked;

    private int size = 22;
    private int bitmapColor = 0xFF3F51B5;
    private int borderColor = 0xFFFFFFFF;
    private int mStateCount = 2;
    public boolean drawIconForEmptyState = true;
    /** tint inner drawable as checked */
    public boolean drawInnerForEmptyState = false;
    public boolean noTint = false;

    private int mHintLeftPadding,mHintSurrondingPad;
	public float circle_shrinkage;

	public CircleCheckBox(Context context) {
        this(context, null);
    }

    public CircleCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    @SuppressLint("ResourceType")
    private void init(Context context, AttributeSet attrs) {
        fetch_android_src_id();
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CheckBox_Sample);
        checkDrawables = new ArrayList<>(ta.getInt(R.styleable.CheckBox_Sample_statecount,1));
        size = ta.getDimensionPixelSize(R.styleable.CheckBox_Sample_size, dp(size));
        bitmapColor = ta.getColor(R.styleable.CheckBox_Sample_color_background, bitmapColor);
        borderColor = ta.getColor(R.styleable.CheckBox_Sample_color_border, borderColor);

        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
        bitmapEraser.setColor(0);
        bitmapEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        checkEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkEraser.setColor(0);
        checkEraser.setStyle(Paint.Style.STROKE);
        checkEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(1));

        int[] fetcherHolder=R.styleable.dual;
        fetcherHolder[0]=CMNF.constants;
        fetcherHolder[1]=R.styleable.UIStyles[1];//dim1
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, fetcherHolder, 0, 0);
        Drawable checkDrawable = a.getDrawable(0);
        mHintSurrondingPad = (int) a.getDimension(1, 0);
        if(checkDrawable==null)
            checkDrawable = context.getResources().getDrawable(R.drawable.ic_check_black_24dp);
        checkDrawables.add(checkDrawable);
        setVisibility(VISIBLE);
        ta.recycle();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE && drawBitmap == null) {
            drawBitmap = Bitmap.createBitmap(dp(size), dp(size), Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(drawBitmap);
            checkBitmap = Bitmap.createBitmap(dp(size), dp(size), Bitmap.Config.ARGB_8888);
            checkCanvas = new Canvas(checkBitmap);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = size;
        int h = size;
        final int pleft = getPaddingLeft();
        final int pright = getPaddingRight();
        final int ptop = getPaddingTop();
        final int pbottom = getPaddingBottom();

        w += pleft + pright;
        h += ptop + pbottom;
        w = Math.max(w, getSuggestedMinimumWidth());
        h = Math.max(h, getSuggestedMinimumHeight());

        int widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
        int heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);

        setMeasuredDimension(widthSize, heightSize);
    }

    //@SuppressLint("NewApi")
    @Override
    protected void onDraw(Canvas canvas) {
        if (getVisibility() != VISIBLE) {
            return;
        }
        checkEraser.setStrokeWidth(size);

        drawBitmap.eraseColor(0);
        float rad = size / 2;

        float bitmapProgress = progress >= 0.5f ? 1.0f : progress / 0.5f;
        float checkProgress = progress < 0.5f ? 0.0f : (progress - 0.5f) / 0.5f;

        float p = isChecked() ? progress : (1.0f - progress);

        if (p < BOUNCE_VALUE) {
            rad -= dp(2) * p ;
        } else if (p < BOUNCE_VALUE * 2) {
            rad -= dp(2) - dp(2) * p;
        }
        int pLeft=getPaddingLeft();
        int pTop=getPaddingTop();
        int MeasuredWidth = getMeasuredWidth() - pLeft - getPaddingRight();
        int MeasuredHeight = getMeasuredHeight() - pTop - getPaddingBottom();

        borderPaint.setColor(borderColor);
        canvas.drawCircle(MeasuredWidth / 2+pLeft, MeasuredHeight / 2+pTop, rad - dp(1), borderPaint);

        bitmapPaint.setColor(bitmapColor);

        //if(drawInnerForEmptyState)
        //    canvas.drawCircle(MeasuredWidth / 2+pLeft, MeasuredHeight / 2+pTop, rad, bitmapPaint);

        bitmapCanvas.drawCircle(MeasuredWidth / 2+pLeft, MeasuredHeight / 2+pTop, rad, bitmapPaint);
        bitmapCanvas.drawCircle(MeasuredWidth / 2+pLeft, MeasuredHeight / 2+pTop, (rad-circle_shrinkage) * (1 - bitmapProgress), bitmapEraser);
        canvas.drawBitmap(drawBitmap, 0, 0, null);

        checkBitmap.eraseColor(0);
        int w = size-mHintSurrondingPad;//checkDrawable.getIntrinsicWidth();
        int h = size-mHintSurrondingPad;//checkDrawable.getIntrinsicHeight();
        int x = (MeasuredWidth - w) / 2+pLeft;
        int y = (MeasuredHeight - h) / 2+pTop;

        Drawable checkDrawable = drawInnerForEmptyState||checked==0?(drawIconForEmptyState ?checkDrawables.get(0):null):checkDrawables.get(checked-1);
        if(checkDrawable!=null) {
            if(!noTint && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            if (isChecked()||drawInnerForEmptyState)
                checkDrawable.setTint(Color.WHITE);
            else
                checkDrawable.setTint(CMNF.ShallowHeaderBlue);

            checkDrawable.setBounds(x, y, x + w, y + h);
            checkDrawable.draw(checkCanvas);
            //checkCanvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, rad * (1 - checkProgress), checkEraser);

            canvas.drawBitmap(checkBitmap, 0, 0, null);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachedToWindow = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attachedToWindow = false;
    }


    public void setProgress(float value) {
        if (progress == value) {
            return;
        }
        progress = value;
        invalidate();
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getProgress() {
        return progress;
    }

    public void setCheckedColor(int value) {
        bitmapColor = value;
    }

    public int getCheckedColor() {
        return bitmapColor;
    }

    public void setBorderColor(int value) {
        borderColor = value;
        borderPaint.setColor(borderColor);
    }

    private void cancelAnim() {
        if (checkAnim != null) {
            checkAnim.cancel();
        }
    }

    public void addAnim(boolean isChecked) {
        if(checkAnim!=null)
            checkAnim.cancel();
        checkAnim = ObjectAnimator.ofFloat(this, "progress", isChecked ? 1.0f : 0.0f);
        checkAnim.setDuration(300);
        checkAnim.start();
    }

    public void setChecked(int val, boolean animated) {
        val = val%getStateCount();
        if (checked == val) {
            return;
        }
        checked = val;

        if (attachedToWindow && animated) {
            addAnim(isChecked());
        } else {
            cancelAnim();
            setProgress(isChecked() ? 1.0f : 0.0f);
        }
    }

    @Override
    public void toggle() {
        toggle(true);
    }

    public void toggle(boolean animate) {
        if(isChecked())
            setChecked(0, animate);
        else
            setChecked(1, animate);
    }

    public void iterate() {
        checked=(checked+1)%getStateCount();
        if (attachedToWindow) {
            addAnim(checked!=0);
        } else {
            cancelAnim();
            setProgress(checked!=0 ? 1.0f : 0.0f);
        }
    }

    public void addStateWithDrawable(Drawable drawable) {
        checkDrawables.add(drawable);
        mStateCount++;
    }

    public void setDrawable(int idx, Drawable drawable) {
        checkDrawables.set(idx, drawable);
    }

    private int getStateCount() {
        return mStateCount;
    }


    @Override
    public void setChecked(boolean b) {
        setChecked(b?1:0, true);
    }

    public void setChecked(boolean b, boolean animated) {
        setChecked(b?1:0, animated);
    }

    public void setChecked(int val) {
        setChecked(val, true);
    }

    @Override
    public boolean isChecked() {
        return  checked > 0;
    }

    public int getChecked() {
        return  checked;
    }

    public int dp(float value) {
        if (value == 0) {
            return 0;
        }
        float density = getContext().getResources().getDisplayMetrics().density;
        return (int) Math.ceil(density * value);
    }

    public static void fetch_android_src_id() {
        if(CMNF.constants ==0) {
            //long stst = System.currentTimeMillis();
            int[] system_iv_identifiers=getReflactIntArray("com.android.internal.R$styleable", "ImageView");
            int system_src_id=getReflactField("com.android.internal.R$styleable", "ImageView_src");
            //getResources().getIdentifier("ImageView_src","R","android");
            //CMNF.Log("system_iv_identifiers",system_iv_identifiers, system_src_id);
            if(system_iv_identifiers!=null) if(system_src_id>=0 && system_src_id<system_iv_identifiers.length)
                CMNF.constants =system_iv_identifiers[system_src_id];
            //CMNF.Log("fetch_android_src_id time",System.currentTimeMillis()-stst, CMNF.constants);
        }
    }

    public static int getReflactField(String className,String fieldName){
        int result = 0;
        try {
            Class<?> clz = Class.forName(className);
            Field field = clz.getField(fieldName);
            field.setAccessible(true);
            result = field.getInt(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int[] getReflactIntArray(String className,String fieldName){
        int[] result = null;
        try {
            Class<?> clz = Class.forName(className);
            Field field = clz.getField(fieldName);
            field.setAccessible(true);
            result = (int[]) field.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}