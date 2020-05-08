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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;


/**
 * A derived horizontal NumberPicker
 * first copy sources,
 * then fix styles & dependencies
 * then replace private with protected
 * then derive, with minimum modification!
 */
public class HorizontalNumberPicker extends NumberKicker{
    public HorizontalNumberPicker(Context context) {
        super(context);
    }

    public HorizontalNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public HorizontalNumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected
    void onDraw(Canvas canvas) {
        if (!mHasSelectorWheel) {
            super.onDraw(canvas);
            return;
        }
        final boolean showSelectorWheel = mHideWheelUntilFocused ? hasFocus() : true;
        float x, y;
        x = mCurrentScrollOffset;
        y = mInputText.getBaseline() + mInputText.getTop();


        // draw the virtual buttons pressed state if needed
        if (showSelectorWheel && mVirtualButtonPressedDrawable != null
                && mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (mDecrementVirtualButtonPressed) {
                mVirtualButtonPressedDrawable.setState(PRESSED_STATE_SET);
                mVirtualButtonPressedDrawable.setBounds(0, 0, getRight(), mTopSelectionDividerTop);
                mVirtualButtonPressedDrawable.draw(canvas);
            }
            if (mIncrementVirtualButtonPressed) {
                mVirtualButtonPressedDrawable.setState(PRESSED_STATE_SET);
                mVirtualButtonPressedDrawable.setBounds(0, mBottomSelectionDividerBottom, getRight(),
                        getBottom());
                mVirtualButtonPressedDrawable.draw(canvas);
            }
        }

        // draw the selector wheel
        int[] selectorIndices = mSelectorIndices;
        for (int i = 0; i < selectorIndices.length; i++) {
            int selectorIndex = selectorIndices[i];
            String scrollSelectorValue = mSelectorIndexToStringCache.get(selectorIndex);
            // Do not draw the middle item if input is visible since the input
            // is shown only if the wheel is static and it covers the middle
            // item. Otherwise, if the user starts editing the text via the
            // IME he may see a dimmed version of the old value intermixed
            // with the new one.
            if ((showSelectorWheel && i != SELECTOR_MIDDLE_ITEM_INDEX) ||
                (i == SELECTOR_MIDDLE_ITEM_INDEX && mInputText.getVisibility() != VISIBLE)) {
                canvas.drawText(scrollSelectorValue, x, y, mSelectorWheelPaint);
            }
            x += mSelectorElementHeight;
        }

        // draw the selection dividers
        if (showSelectorWheel && mSelectionDivider != null) {
            // draw the top divider
            int topOfTopDivider = mTopSelectionDividerTop;
            int bottomOfTopDivider = topOfTopDivider + mSelectionDividerHeight;
            //mSelectionDivider.setBounds(0, topOfTopDivider, getRight(), bottomOfTopDivider);
            mSelectionDivider.setBounds(topOfTopDivider, 0, bottomOfTopDivider, getHeight());
            mSelectionDivider.draw(canvas);

            // draw the bottom divider
            int bottomOfBottomDivider = mBottomSelectionDividerBottom;
            int topOfBottomDivider = bottomOfBottomDivider - mSelectionDividerHeight;
            //mSelectionDivider.setBounds(0, topOfBottomDivider, getRight(), bottomOfBottomDivider);
            mSelectionDivider.setBounds(topOfBottomDivider, 0, bottomOfBottomDivider, getHeight());
            mSelectionDivider.draw(canvas);
        }
    }

    protected void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = mSelectorIndices;
        int totalTextHeight = selectorIndices.length * mTextSize;
        float totalTextGapHeight = (getRight() - getLeft()) - totalTextHeight;
        float textGapCount = selectorIndices.length;
        mSelectorTextGapHeight = (int) (totalTextGapHeight / textGapCount + 0.5f);
        mSelectorElementHeight = mTextSize + mSelectorTextGapHeight;
        // Ensure that the middle item is positioned the same as the text in
        // mInputText
        int editTextTextPosition = getWidth()/2;//mInputText.getBaseline() + mInputText.getTop();
        mInitialScrollOffset = editTextTextPosition
                - (mSelectorElementHeight * SELECTOR_MIDDLE_ITEM_INDEX);
        mCurrentScrollOffset = mInitialScrollOffset;
        updateInputTextView();
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!mHasSelectorWheel) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        final int msrdWdth = getMeasuredWidth();
        final int msrdHght = getMeasuredHeight();

        // Input text centered horizontally.
        final int inptTxtMsrdWdth = mInputText.getMeasuredWidth();
        final int inptTxtMsrdHght = mInputText.getMeasuredHeight();
        final int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        final int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        final int inptTxtRight = inptTxtLeft + inptTxtMsrdWdth;
        final int inptTxtBottom = inptTxtTop + inptTxtMsrdHght;
        mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom);

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel();
            initializeFadingEdges();
            mTopSelectionDividerTop = (getWidth() - mSelectionDividersDistance) / 2
                    - mSelectionDividerHeight;
            mBottomSelectionDividerBottom = mTopSelectionDividerTop + 2 * mSelectionDividerHeight
                    + mSelectionDividersDistance;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mHasSelectorWheel || !isEnabled()) {
            return false;
        }
        final int action = event.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			removeAllCallbacks();
			hideSoftInput();
			mLastDownOrMoveEventY = mLastDownEventY = event.getX();
			mLastDownEventTime = event.getEventTime();
			mIgnoreMoveEvents = false;
			mPerformClickOnTap = false;
			// Handle pressed state before any state change.
			if (mLastDownEventY < mTopSelectionDividerTop) {
				if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					mPressedStateHelper.buttonPressDelayed(
							PressedStateHelper.BUTTON_DECREMENT);
				}
			} else if (mLastDownEventY > mBottomSelectionDividerBottom) {
				if (mScrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					mPressedStateHelper.buttonPressDelayed(
							PressedStateHelper.BUTTON_INCREMENT);
				}
			}
			// Make sure we support flinging inside scrollables.
			getParent().requestDisallowInterceptTouchEvent(true);
			if (!mFlingScroller.isFinished()) {
				mFlingScroller.forceFinished(true);
				mAdjustScroller.forceFinished(true);
				onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
				//CMNF.Log("ast... 1");
			} else if (!mAdjustScroller.isFinished()) {
				mFlingScroller.forceFinished(true);
				mAdjustScroller.forceFinished(true);
				//CMNF.Log("ast... 2");
			} else if (mLastDownEventY < mTopSelectionDividerTop) {
				postChangeCurrentByOneFromLongPress(
						false, ViewConfiguration.getLongPressTimeout());
				//CMNF.Log("ast... 3");
			} else if (mLastDownEventY > mBottomSelectionDividerBottom) {
				postChangeCurrentByOneFromLongPress(
						true, ViewConfiguration.getLongPressTimeout());
				//CMNF.Log("ast... 4");
			} else {
				mPerformClickOnTap = true;
				postBeginSoftInputOnLongPressCommand();
				//CMNF.Log("ast... 5!!!");
			}
			return true;
		}
        return false;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || !mHasSelectorWheel) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (mIgnoreMoveEvents) {
                    break;
                }
                float currentMoveY = event.getX();
                if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
                    if (deltaDownY > mTouchSlop) {
                        removeAllCallbacks();
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    }
                } else {
                    int deltaMoveY = (int) ((currentMoveY - mLastDownOrMoveEventY));
                    scrollBy(0, deltaMoveY);
                    invalidate();
                }
                mLastDownOrMoveEventY = currentMoveY;
            } break;
            case MotionEvent.ACTION_UP: {
                removeBeginSoftInputCommand();
                removeChangeCurrentByOneFromLongPress();
                mPressedStateHelper.cancel();
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getXVelocity();
                if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                    fling(initialVelocity);
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                } else {
                    int eventY = (int) event.getX();
                    int deltaMoveY = (int) Math.abs(eventY - mLastDownEventY);
                    long deltaTime = event.getEventTime() - mLastDownEventTime;
                    if (deltaMoveY <= mTouchSlop && deltaTime < ViewConfiguration.getTapTimeout()) {
                        if (mPerformClickOnTap) {
                            mPerformClickOnTap = false;
                            performClick();
                        } else {
                            int selectorIndexOffset = (eventY / mSelectorElementHeight)
                                    - SELECTOR_MIDDLE_ITEM_INDEX;
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true);
                                mPressedStateHelper.buttonTapped(
                                        PressedStateHelper.BUTTON_INCREMENT);
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false);
                                mPressedStateHelper.buttonTapped(
                                        PressedStateHelper.BUTTON_DECREMENT);
                            }
                        }
                    } else {
                        ensureScrollWheelAdjusted();
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            } break;
        }
        return true;
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());// 宽高值互换  
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldw, oldh);// 宽高值互换  
    }
}
