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

package com.knziha.polymer.equalizer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.Options;

import java.util.Random;

/** Wonderful Equalizer Group designed and delegated to hold and manipulate vertical equalizer seekbars
 *  <br>See <b>{@link VerticalSeekBar VerticalSeekBar}<b/>*/
public class EqualizerGroup extends LinearLayout {
    public Options opt;
    public int baseLevel;
	
	/** <b>Frequncy unit : Hertz<p>Amplitude unit : milliBel*/
    public interface EqualizerListener{
        float getBandFrequency(int Index);
        int setAmp(int Index, int Amplitute);
        int[] getBandRange();
        int getBandCount();
        int getAmp(int i);
    }
    public EqualizerListener mEqualizerListener;
    private boolean isShunt;

    public int intflate_id = R.layout.equalizer_item;
    VerticalSeekBar CurrentSeekbar;
    //public boolean bTintBackGround = false;
    //public int spacing=10;
    //public int size=(int) (30 * getResources().getDisplayMetrics().density);
    public SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;

    public EqualizerGroup(Context context) {
        this(context, null);
    }

    public EqualizerGroup(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EqualizerGroup(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
	
	public void setEqualizerListeners(SeekBar.OnSeekBarChangeListener mSeekbarsListener, EqualizerListener equalizerListener) {
		mOnSeekBarChangeListener = mSeekbarsListener;
		mEqualizerListener = equalizerListener;
	}
	
    /** Initialize or update equlizer seekbars
     * @param bUpdateProgress to fetch equalizer values from {@link EqualizerListener EqualizerListener}
     *                        to set progresses of child seekbars.  <p>
     * @param bUpdateBars   to update seekbars' appearance. (size and spacing)*/
    public void inflate(LayoutInflater inf
            , boolean bUpdateProgress, boolean bUpdateBars){
    	int c = mEqualizerListener.getBandCount();
        int spacing=opt.getEqBarSpacing();
		int size=opt.getEqBarSize();
        if (spacing==-1) {
			int width = opt.dm.widthPixels;
			spacing = (int) ((width-size*c)*1.f/(c*2));
			//CMN.Log("spacing::"+spacing, width);
			if (spacing<0) {
				spacing = 0;
				size = width/c;
			}
		}
        Random rand = new Random();
        ColorStateList color = ColorStateList.valueOf(0x45ffffff);
        int seat_count = getChildCount();
        int max = 20;
        if(mEqualizerListener!=null) {
            int[] range = mEqualizerListener.getBandRange();//may fail
            max=range[1];
            baseLevel=range[0];
        }
        for(int i=0;i<c;i++) {
            VerticalSeekBar skI;
            if (inf!=null && i >= seat_count) {//新建
                skI = (VerticalSeekBar) inf.inflate(intflate_id, this, false);
                skI.setId(i);
                addView(skI);
                skI.setPadding((int) (skI.getPaddingLeft() * 2.5 * 1.1), skI.getPaddingTop(), skI.getPaddingRight(), skI.getPaddingBottom());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((RippleDrawable) skI.getBackground()).setColor(color);
                }
                skI.btmTags.add("0db");
                skI.btmTags.add("--");
            } else {//旧用
                skI = (VerticalSeekBar) getChildAt(i);
            }
            if(skI==null)
                break;
            if (false){ // opt.getTintEqBars()
                if(!(skI.getBackground() instanceof WrappedBackGroundColor)){
                   skI.setBackground(wrapInDrawable(skI.getBackground(), rand.nextInt() | 0x34000000));
                }
            } else if(skI.getBackground() instanceof WrappedBackGroundColor){
                skI.setBackground(((WrappedBackGroundColor)skI.getBackground()).unwrap());
            }
            if (bUpdateProgress) {
                skI.setOnSeekBarChangeListener(null);
                skI.setMax(max - baseLevel);
                if (mEqualizerListener != null) {
                    int level = mEqualizerListener.getAmp(i);
                    skI.btmTags.set(0, String.format("%.1f", level * 1.f / 100) + "db");
                    skI.btmTags.set(1, decorateFreq(mEqualizerListener.getBandFrequency(i)));
                    skI.setProgress(level - baseLevel);
                } else {
                    skI.setProgress((max - baseLevel) / 2);
                }
                skI.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
            }
            if (bUpdateBars) {
                MarginLayoutParams lp = (MarginLayoutParams) skI.getLayoutParams();
                lp.width = size;
                lp.height = -1;
                lp.leftMargin = spacing;
                lp.rightMargin = spacing;
                skI.setLayoutParams(lp);
            }
        }
        if(bUpdateBars) {
            if (seat_count - c > 0) {
                for (int i = c; i < seat_count; i++) {
                    VerticalSeekBar skI = (VerticalSeekBar) getChildAt(i);
                    skI.setOnSeekBarChangeListener(null);
                }
                removeViews(c, seat_count - c);
            }
        }
    }

    private Drawable wrapInDrawable(Drawable background, int color) {
        if(background!=null){
            Drawable[] layers = {background, new ColorDrawable(color)};
            return new MarkedLayerDrawable(layers);
        }else{
            return new MarkedColorDrawable(color);
        }
    }

    private String decorateFreq(float bandFrequency) {
        int base = (int)(bandFrequency+0.5);
        String sufix="Hz";
        if(base>=1000000){
            base/=1000000;
            sufix = "m"+sufix;
        }else if(base>=1000){
            base/=1000;
            sufix = "k"+sufix;
        }
        return base+sufix;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev){
        boolean ret = super.onTouchEvent(ev);
        if(isShunt)
            return true;
        if(CurrentSeekbar!=null){
			float ex = getLayoutDirection()== View.LAYOUT_DIRECTION_RTL?getWidth()-ev.getX():ev.getX();
			int skIdx = (int) ((ex)/(getWidth()/getChildCount()));
            VerticalSeekBar NextSeekbar = (VerticalSeekBar) getChildAt(skIdx);
            if(NextSeekbar!=null)
            if(ev.getX()>=NextSeekbar.getX() && ev.getX()<=NextSeekbar.getX()+NextSeekbar.getWidth() && ev.getY()>=NextSeekbar.getY() && ev.getY()<=NextSeekbar.getY()+NextSeekbar.getHeight())
            {
                if(CurrentSeekbar!=null && CurrentSeekbar!=NextSeekbar){
                    //CMN.Log("doing...");
                    MotionEvent UpEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, CurrentSeekbar.lastX, CurrentSeekbar.lastY, 0);
                    CurrentSeekbar.onTouchEvent(UpEvent);
                    //CurrentSeekbar.trackTouchEvent(UpEvent);
                    UpEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, ev.getX(), ev.getY(), 0);
                    NextSeekbar.onTouchEvent(UpEvent);
                }
                CurrentSeekbar = NextSeekbar;
            }
        }

        if(CurrentSeekbar!=null) {
            //CMN.Log("doing... 2...");
            //if(ev.getX()>=CurrentSeekbar.getX() && ev.getX()<=CurrentSeekbar.getX()+CurrentSeekbar.getWidth() && ev.getY()>=CurrentSeekbar.getY() + CurrentSeekbar.getPaddingRight() && ev.getY()<=CurrentSeekbar.getY()+CurrentSeekbar.getHeight())
                CurrentSeekbar.onTouchEvent(ev);
            //CurrentSeekbar.trackTouchEvent(ev);
        }
        if(ev.getAction()==MotionEvent.ACTION_UP) {
            CurrentSeekbar = null;
        }

        return true;
    }

    //padding 处，亦要拦截。
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        if(opt.getEqAdjustOctopusMode()){
            isShunt=true;
            return super.onInterceptTouchEvent(ev);
        }
        isShunt=false;
		float ex = getLayoutDirection()== View.LAYOUT_DIRECTION_RTL?getWidth()-ev.getX():ev.getX();
		int skIdx = (int) ((ex)/(getWidth()/getChildCount()));
        VerticalSeekBar NextSeekbar = (VerticalSeekBar) getChildAt(skIdx);
        if(NextSeekbar!=null)
        if(ev.getX()>=NextSeekbar.getX() && ev.getX()<=NextSeekbar.getX()+NextSeekbar.getWidth()
                && ev.getY()>=NextSeekbar.getY() && ev.getY()<=NextSeekbar.getY()+NextSeekbar.getHeight()){
            CurrentSeekbar = NextSeekbar;
            if(ev.getY()>=CurrentSeekbar.getY() + CurrentSeekbar.getPaddingRight() &&
                    ev.getY()<=CurrentSeekbar.getY()+CurrentSeekbar.getHeight() - + CurrentSeekbar.getPaddingLeft()){
                CurrentSeekbar.onTouchEvent(ev);
                CurrentSeekbar.trackTouchEvent(ev);
                //CMN.Log("onInterceptTouchEvent sk assigned... ",CurrentSeekbar.getPaddingRight(), CurrentSeekbar.getPaddingLeft() ,    ev.getY(),CurrentSeekbar.getY()+CurrentSeekbar.getHeight() - + CurrentSeekbar.getPaddingLeft());
            }else{
                isShunt=true;
                CurrentSeekbar = null;
                //CMN.Log("      shunting...");
            }
        }
        return true;
    }
}
