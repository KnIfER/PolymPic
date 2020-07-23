package com.knaiver.polymer.flowtextview;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified version for https://github.com/deano2390/FlowTextView/blob/master/flowtextview/src/main/java/uk/co/deanwild/flowtextview/FlowTextView.java <br/>
 * ———— The goal is to acquire full control over the drawing process within minimal cost, <br/>
 * 			which should yield the simplest resizable multi-line TextView. <br/>
 * Original comments:
 * FlowTextView is basically a TextView that has children that it avoids while laying itself out.
 * Considering that it is a TextView at heart, it makes sense for it to honor the TextView attributes
 * provided by Android. In its latest incarnation, it handles android:lineSpacingExtra and
 * android:lineSpacingMultiplier. I also adjusted the layout rules to attempt to account for the new
 * descendance and ascendance of the line height, but I'm not certain that is correct.
 * <p/>
 * This version also has some demorgan interestingness that I turned around to make less interesting,
 * and I added respect for children's margins when calculating text-drawing bounds.
 * <p/>
 * I also made minor performance improvements, as the original author had stated that they didn't
 */
public class FlowTextView extends View {
	List<LineObject> lineObjects = new ArrayList<>();
	private TextPaint mTextPaint;
	private float mTextsize = getResources().getDisplayMetrics().scaledDensity * 20.0f;
	private int mTextColor = Color.BLACK;
	private Typeface typeFace;
	private int mDesiredHeight = 100; // height of the whole view
	private String mText = "";
	
	private float mSpacingMult;
	private float mSpacingAdd;
	private int mLength;
	
	public FlowTextView(Context context) {
		this(context, null);
	}
	
	public FlowTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public FlowTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (attrs != null) {
			int[] attrsArray = new int[]{
					android.R.attr.lineSpacingExtra, // 0
					android.R.attr.lineSpacingMultiplier, // 1
					android.R.attr.textSize, // 2
					android.R.attr.textColor, // 3
			};
			
			TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);
			mSpacingAdd = ta.getDimensionPixelSize(0, 0);  // 0 is the index in the array, 0 is the default
			mSpacingMult = ta.getFloat(1, 1.0f);  // 1 is the index in the array, 1.0f is the default
			mTextsize = ta.getDimension(2, mTextsize); // 2 is the index in the array of the textSize attribute
			mTextColor = ta.getColor(3, Color.BLACK); // 3 is the index of the array of the textColor attribute
			ta.recycle();
		}
		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.density = getResources().getDisplayMetrics().density;
		mTextPaint.setTextSize(mTextsize);
		mTextPaint.setColor(mTextColor);
	}
	
	/* text content */
	public void setText(String text) {
		mText = text;
		mLength = text.length();
		postCalcTextLayout();
	}
	
	public CharSequence getText() {
		return mText;
	}
	
	private int splitChunk(String text, int start, int end, float maxWidth) {
		int break_length = start + mTextPaint.breakText(text, start, end,true, maxWidth, null);
		// if it's 0 or less, we can't fit any more chars on this line
		// if it's >= text length, everything fits, we're done
		// if the break character is a space, we're set
		if (break_length <= start || break_length >= end || text.charAt(break_length - 1) == ' ') {
			return break_length;
		} else if (text.charAt(break_length) == ' ') {
			return break_length + 1; // or if the following char is a space then return this length - it is fine
		}
		
		// otherwise, count back until we hit a space and return that as the break length
		int tempLength = break_length - 1;
		while (text.charAt(tempLength) != ' ') {
			tempLength--;
			if (tempLength <= 0)
				return break_length; // if we count all the way back to 0 then this line cannot be broken, just return the original break length
		}
		
		return tempLength + 1; // return the nicer break length which doesn't split a word up
		
	}
	
	private void postCalcTextLayout() {
		if(getWidth()==0){
			post(this::calcTextLayout);
		} else {
			calcTextLayout();
		}
	}
	
	private void calcTextLayout() {
		float mViewWidth = getWidth();
		// set up some counter and helper variables we will us to traverse through the string to be rendered
		int charOffsetStart = 0; // tells us where we are in the original string
		int charOffsetEnd; // tells us where we are in the original string
		int lineIndex = 0;
		float maxWidth; // how far to the right it can stretch
		float yOffset = 0;
		int lineHeight = getLineHeight(); // get the height in pixels of a line for our current TextPaint
		int paddingTop = getPaddingTop();
		
		lineObjects.clear(); // this will get populated with special html objects we need to render
		
		if (mLength > 0) { // is some actual text
			while (charOffsetStart < mLength) { // churn through the block spitting it out onto seperate lines until there is nothing left to render
				lineIndex++; // we need a new line
				yOffset = getPaddingTop() + lineIndex * lineHeight - (getLineHeight() + mTextPaint.getFontMetrics().ascent); // calculate our new y position based on number of lines * line height
				maxWidth = mViewWidth;
				
				charOffsetEnd = splitChunk(mText, charOffsetStart, mLength, maxWidth);
				
				LineObject htmlLine = new LineObject(charOffsetStart, charOffsetEnd, yOffset);
				
				lineObjects.add(htmlLine);
				
				//if(htmlLine.end>mLength) htmlLine.end=mLength;
				
				charOffsetStart = charOffsetEnd;
			}
		}
		
		int mDesiredHeight = (int) yOffset + lineHeight/2;
		if(mDesiredHeight != this.mDesiredHeight){
			this.mDesiredHeight = mDesiredHeight;
			requestLayout();
		} else {
			invalidate();
		}
	}
	
	/* INTERESTING DRAWING STUFF */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		LineObject htmlLine;
		int size = lineObjects.size();
		for (int i = 0; i < size; i++) {
			htmlLine = lineObjects.get(i);
			canvas.drawText(mText, htmlLine.start, htmlLine.end, 0, htmlLine.yOffset, mTextPaint);
		}
	}
	
	/* MINOR VIEW EVENTS */
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		postCalcTextLayout();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int width;
		int height;
		
		if (widthMode == MeasureSpec.EXACTLY) {
			// Parent has told us how big to be. So be it.
			width = widthSize;
		} else {
			width = this.getWidth();
		}
		
		if (heightMode == MeasureSpec.EXACTLY) {
			// Parent has told us how big to be. So be it.
			height = heightSize;
		} else {
			height = mDesiredHeight;
		}
		
		setMeasuredDimension(width, height);
	}
	
	// GETTERS AND SETTERS
	// text size
	public float getTextsize() {
		return mTextsize;
	}
	
	public void setTextSize(float textSize) {
		this.mTextsize = textSize;
		mTextPaint.setTextSize(mTextsize);
		invalidate();
	}
	
	public int getTextColor() {
		return mTextColor;
	}
	
	public void setTextColor(int color) {
		mTextColor = color;
		mTextPaint.setColor(mTextColor);
	}
	
	/* typeface */
	public Typeface getTypeFace() {
		return typeFace;
	}
	
	public void setTypeface(Typeface type) {
		this.typeFace = type;
		mTextPaint.setTypeface(typeFace);
		invalidate();
	}
	
	/* line height */
	public int getLineHeight() {
		return Math.round(mTextPaint.getFontMetricsInt(null) * mSpacingMult
				+ mSpacingAdd);
	}
	
	private static class LineObject {
		public int start;
		public int end;
		public float yOffset;
		public LineObject(int start, int end, float yOffset) {
			this.start = start;
			this.end = end;
			this.yOffset = yOffset;
		}
	}
}
