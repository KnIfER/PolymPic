/*
 * Copyright 2015 lsjwzh/RecyclerViewPager
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * https://github.com/lsjwzh/RecyclerViewPager/commits/master/lib/src/main/java/com/lsjwzh/widget/recyclerviewpager/RecyclerViewPager.java
 */

package com.knziha.polymer.webslideshow;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;


import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BuildConfig;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewPager extends RecyclerView {
	public static final boolean DEBUG = BuildConfig.DEBUG;
	public int mItemWidth;
	public int itemPad;
	
	private RecyclerView.Adapter<?> mViewPagerAdapter;
	private float mTriggerOffset = 0.25f;
	private float mFlingFactor = 0.15f;
	private float mTouchSpan;
	private List<OnPageChangedListener> mOnPageChangedListeners;
	private int mSmoothScrollTargetPosition = -1;
	private int mPositionBeforeScroll = -1;
	
	boolean mNeedAdjust;
	int mFisrtLeftWhenDragging;
	int mFirstTopWhenDragging;
	View mCurView;
	int mMaxLeftWhenDragging = Integer.MIN_VALUE;
	int mMinLeftWhenDragging = Integer.MAX_VALUE;
	int mMaxTopWhenDragging = Integer.MIN_VALUE;
	int mMinTopWhenDragging = Integer.MAX_VALUE;
	public boolean bFromIdle;
	private boolean isGridView = false;
	
	public RecyclerViewPager(Context context) {
		this(context, null);
	}
	
	public RecyclerViewPager(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public RecyclerViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAttrs(context, attrs, defStyle);
	}
	
	private void initAttrs(Context context, AttributeSet attrs, int defStyle) {
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecyclerViewPager, defStyle,
				0);
		mFlingFactor = a.getFloat(R.styleable.RecyclerViewPager_flingFactor, 0.15f);
		mTriggerOffset = a.getFloat(R.styleable.RecyclerViewPager_triggerOffset, 0.25f);
		a.recycle();
	}
	
	public void setFlingFactor(float flingFactor) {
		mFlingFactor = flingFactor;
	}
	
	public float getFlingFactor() {
		return mFlingFactor;
	}
	
	public void setTriggerOffset(float triggerOffset) {
		mTriggerOffset = triggerOffset;
	}
	
	public float getTriggerOffset() {
		return mTriggerOffset;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		try {
			Field fLayoutState = state.getClass().getDeclaredField("mLayoutState");
			fLayoutState.setAccessible(true);
			Object layoutState = fLayoutState.get(state);
			Field fAnchorOffset = layoutState.getClass().getDeclaredField("mAnchorOffset");
			Field fAnchorPosition = layoutState.getClass().getDeclaredField("mAnchorPosition");
			fAnchorPosition.setAccessible(true);
			fAnchorOffset.setAccessible(true);
			if (fAnchorOffset.getInt(layoutState) > 0) {
				fAnchorPosition.set(layoutState, fAnchorPosition.getInt(layoutState) - 1);
			} else {
				fAnchorPosition.set(layoutState, fAnchorPosition.getInt(layoutState) + 1);
			}
			fAnchorOffset.setInt(layoutState, 0);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.onRestoreInstanceState(state);
	}
	
	@Override
	public boolean fling(int velocityX, int velocityY) {
		if(isGridView) return super.fling(velocityX, velocityY);
		boolean flinging = super.fling((int) (velocityX * mFlingFactor), (int) (velocityY * mFlingFactor));
		if (flinging) {
			if (getLayoutManager().canScrollHorizontally()) {
				adjustPositionX(velocityX);
				if (DEBUG) {
					Log.d("@", "velocityX:" + velocityX);
				}
			} else {
				adjustPositionY(velocityY);
				if (DEBUG) {
					Log.d("@", "velocityY:" + velocityY);
				}
			}
		}
		return flinging;
	}
	
	@Override
	public void smoothScrollToPosition(int position) {
		CMN.Log("@", "smoothScrollToPosition:" + position);
		mSmoothScrollTargetPosition = position;
		super.smoothScrollToPosition(position);
	}
	
	/**
	 * get item position in center of viewpager
	 */
	public int getCurrentPosition() {
		int curPosition = -1;
		if (getLayoutManager().canScrollHorizontally()) {
			curPosition = ViewUtils.getCenterXChildPositionV1(this);
		} else {
			curPosition = ViewUtils.getCenterYChildPositionV1(this);
		}
		return curPosition;
	}
	
	/***
	 * adjust position before Touch event complete and fling action start.
	 */
	protected void adjustPositionX(int velocityX) {
		int childCount = getChildCount();
		if (childCount > 0) {
			int targetPosition;
			int curPosition = ViewUtils.getCenterXChildPositionV1(this);
			if (curPosition==-1) return;
			if (curPosition==-1) {
				curPosition = ((CenterLinearLayoutManager)getLayoutManager()).targetPos;
			}
			if (curPosition==-1)  {
				//curPosition =
				targetPosition = ((CenterLinearLayoutManager)getLayoutManager()).targetPos;
			} else {
				int childWidth = getWidth() - getPaddingLeft() - getPaddingRight();
				childWidth = mItemWidth;
				int flingCount = (int) (velocityX * mFlingFactor / childWidth);
				targetPosition = curPosition + flingCount;
			}
			targetPosition = Math.max(targetPosition, 0);
			targetPosition = Math.min(targetPosition, getAdapter().getItemCount() - 1);
			if (targetPosition == curPosition) {
				View centerXChild = ViewUtils.getCenterXChildV1(this);
				if (centerXChild != null) {
					if (mTouchSpan > centerXChild.getWidth() * mTriggerOffset * mTriggerOffset && targetPosition != 0) {
						targetPosition--;
					} else if (mTouchSpan < centerXChild.getWidth() * -mTriggerOffset && targetPosition != getAdapter().getItemCount() - 1) {
						targetPosition++;
					}
				}
			}
			if (DEBUG) {
				Log.d("@", "mTouchSpan:" + mTouchSpan);
				Log.d("@", "adjustPositionX:" + targetPosition);
			}
			
			CMN.Log("SCROLL_ADJ :: ", ((LinearLayoutManager)getLayoutManager()).findFirstVisibleItemPosition(),targetPosition," from :: "+curPosition);
			
			smoothScrollToPosition(safeTargetPosition(targetPosition, getAdapter().getItemCount()));
		}
	}
	
	public void addOnPageChangedListener(OnPageChangedListener listener) {
		if (mOnPageChangedListeners == null) {
			mOnPageChangedListeners = new ArrayList<>();
		}
		mOnPageChangedListeners.add(listener);
	}
	
	public void removeOnPageChangedListener(OnPageChangedListener listener) {
		if (mOnPageChangedListeners != null) {
			mOnPageChangedListeners.remove(listener);
		}
	}
	
	public void clearOnPageChangedListeners() {
		if (mOnPageChangedListeners != null) {
			mOnPageChangedListeners.clear();
		}
	}
	
	/***
	 * adjust position before Touch event complete and fling action start.
	 */
	protected void adjustPositionY(int velocityY) {
		int childCount = getChildCount();
		if (childCount > 0) {
			int curPosition = ViewUtils.getCenterYChildPosition(this);
			int childHeight = getHeight() - getPaddingTop() - getPaddingBottom();
			int flingCount = (int) (velocityY * mFlingFactor / childHeight);
			int targetPosition = curPosition + flingCount;
			targetPosition = Math.max(targetPosition, 0);
			targetPosition = Math.min(targetPosition, getAdapter().getItemCount() - 1);
			if (targetPosition == curPosition) {
				View centerYChild = ViewUtils.getCenterYChild(this);
				if (centerYChild != null) {
					if (mTouchSpan > centerYChild.getHeight() * mTriggerOffset && targetPosition != 0) {
						targetPosition--;
					} else if (mTouchSpan < centerYChild.getHeight() * -mTriggerOffset && targetPosition != getAdapter().getItemCount() - 1) {
						targetPosition++;
					}
				}
			}
			
			CMN.Log("@", "mTouchSpan:" + mTouchSpan);
			CMN.Log("@", "adjustPositionY:" + targetPosition);
			
			smoothScrollToPosition(safeTargetPosition(targetPosition, getAdapter().getItemCount()));
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// recording the max/min value in touch track
		if (e.getAction() == MotionEvent.ACTION_MOVE) {
			if (mCurView != null) {
				mMaxLeftWhenDragging = Math.max(mCurView.getLeft(), mMaxLeftWhenDragging);
				mMaxTopWhenDragging = Math.max(mCurView.getTop(), mMaxTopWhenDragging);
				mMinLeftWhenDragging = Math.min(mCurView.getLeft(), mMinLeftWhenDragging);
				mMinTopWhenDragging = Math.min(mCurView.getTop(), mMinTopWhenDragging);
			}
		}
		return super.onTouchEvent(e);
	}
	
	@Override
	public void onScrollStateChanged(int state) {
		super.onScrollStateChanged(state);
		if(isGridView) return;
		if (state == SCROLL_STATE_DRAGGING) {
			mNeedAdjust = true;
			mCurView = getLayoutManager().canScrollHorizontally() ? ViewUtils.getCenterXChildV1(this) :
					ViewUtils.getCenterYChildV1(this);
			if (mCurView != null) {
				mPositionBeforeScroll = getChildLayoutPosition(mCurView);
				if (DEBUG) {
					Log.d("@", "mPositionBeforeScroll:" + mPositionBeforeScroll);
				}
				mFisrtLeftWhenDragging = mCurView.getLeft();
				mFirstTopWhenDragging = mCurView.getTop();
			} else {
				mPositionBeforeScroll = -1;
			}
			mTouchSpan = 0;
		} else if (state == SCROLL_STATE_SETTLING) {
			mNeedAdjust = false;
			if (mCurView != null) {
				if (getLayoutManager().canScrollHorizontally()) {
					mTouchSpan = mCurView.getLeft() - mFisrtLeftWhenDragging;
				} else {
					mTouchSpan = mCurView.getTop() - mFirstTopWhenDragging;
				}
			} else {
				mTouchSpan = 0;
			}
			mCurView = null;
		} else if (state == SCROLL_STATE_IDLE) {
			if (mNeedAdjust) {
				int targetPosition = getLayoutManager().canScrollHorizontally()
						? ViewUtils.getCenterXChildPositionV1(this)
						: ViewUtils.getCenterYChildPositionV1(this);
//				if (mCurView != null) {
//					targetPosition = getChildAdapterPosition(mCurView);
//					if (getLayoutManager().canScrollHorizontally()) {
//						int spanX = mCurView.getLeft() - mFisrtLeftWhenDragging;
//						// if user is tending to cancel paging action, don't perform position changing
//						if (spanX > mCurView.getWidth() * mTriggerOffset && mCurView.getLeft() >= mMaxLeftWhenDragging) {
//							targetPosition--;
//						} else if (spanX < mCurView.getWidth() * -mTriggerOffset && mCurView.getLeft() <= mMinLeftWhenDragging) {
//							targetPosition++;
//						}
//					} else {
//						int spanY = mCurView.getTop() - mFirstTopWhenDragging;
//						if (spanY > mCurView.getHeight() * mTriggerOffset && mCurView.getTop() >= mMaxTopWhenDragging) {
//							targetPosition--;
//						} else if (spanY < mCurView.getHeight() * -mTriggerOffset && mCurView.getTop() <= mMinTopWhenDragging) {
//							targetPosition++;
//						}
//					}
//				}
				CMN.Log("SCROLL_IDLE :: ", ((LinearLayoutManager)getLayoutManager()).findFirstVisibleItemPosition(),targetPosition);
				
				bFromIdle=true;
				
				smoothScrollToPosition(safeTargetPosition(targetPosition, getAdapter().getItemCount()));
				
				bFromIdle=false;
				
				mCurView = null;
			} else if (mSmoothScrollTargetPosition != mPositionBeforeScroll) {
				if (DEBUG) {
					Log.d("@", "onPageChanged:" + mSmoothScrollTargetPosition);
				}
				if (mOnPageChangedListeners != null) {
					for (OnPageChangedListener onPageChangedListener : mOnPageChangedListeners) {
						if (onPageChangedListener != null) {
							onPageChangedListener.OnPageChanged(mPositionBeforeScroll, mSmoothScrollTargetPosition);
						}
					}
				}
			}
			// resetAll
			mMaxLeftWhenDragging = Integer.MIN_VALUE;
			mMinLeftWhenDragging = Integer.MAX_VALUE;
			mMaxTopWhenDragging = Integer.MIN_VALUE;
			mMinTopWhenDragging = Integer.MAX_VALUE;
		}
	}
	
	private int safeTargetPosition(int position, int count) {
		if (position < 0) {
			return 0;
		}
		if (position >= count) {
			return count - 1;
		}
		return position;
	}
	
	@Override
	public void setLayoutManager(@Nullable LayoutManager layout) {
		isGridView = layout instanceof GridLayoutManager;
		super.setLayoutManager(layout);
	}
	
	public boolean isGridView() {
		return isGridView;
	}
	
	public interface OnPageChangedListener {
		void OnPageChanged(int oldPosition, int newPosition);
	}
}