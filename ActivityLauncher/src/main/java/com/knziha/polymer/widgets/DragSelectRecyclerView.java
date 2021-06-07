/*
* Copyright 2017 , Aidan Follestad (author)
*
* Copyright 2021 The 多聚浏览 Open Source Project
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
* from https://github.com/afollestad/drag-select-recyclerview/tree/1fa9999875bbd521b1aa8506e97b351acb634e77/library/src/main/java/com/afollestad/dragselectrecyclerview
**/
package com.knziha.polymer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
public class DragSelectRecyclerView extends RecyclerView {
	public interface IDragSelectAdapter {
		void setSelected(int index, boolean selected);
		int getItemCount();
	}
	
	private static final int AUTO_SCROLL_DELAY = 10;
	
	public DragSelectRecyclerView(Context context) {
		this(context, null);
	}
	
	public DragSelectRecyclerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DragSelectRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		hotspotHeight = (int) (GlobalOptions.density*56);
		hotspotOffsetTop = 30;
		hotspotOffsetBottom = 30;
	}
	
	private int lastDraggingIndex = -1;
	private IDragSelectAdapter adapter;
	private int initialSelection;
	private boolean dragSelectActive;
	private int minReached;
	private int maxReached;
	
	private int hotspotHeight;
	private int hotspotOffsetTop;
	private int hotspotOffsetBottom;
	
	private int hotspotTopStart;
	private int hotspotTopEnd;
	private int hotspotBottomStart;
	private int hotspotBottomEnd;
	private int autoScrollVelocity;
	
	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
		if (hotspotHeight > -1) {
			hotspotTopStart = hotspotOffsetTop;
			hotspotTopEnd = hotspotOffsetTop + hotspotHeight;
			hotspotBottomStart = (getMeasuredHeight() - hotspotHeight) - hotspotOffsetBottom;
			hotspotBottomEnd = getMeasuredHeight() - hotspotOffsetBottom;
			//CMN.Log("RecyclerView height = ", getMeasuredHeight());
			//CMN.Log("Hotspot top bound = %d to %d", hotspotTopStart, hotspotTopStart);
			//CMN.Log("Hotspot bottom bound = %d to %d", hotspotBottomStart, hotspotBottomEnd);
		}
	}
	
	public boolean setDragSelectActive(boolean active, int initialSelection) {
		if (active && dragSelectActive) {
			//CMN.Log("Drag selection is already active.");
			return false;
		}
		lastDraggingIndex = -1;
		minReached = Integer.MAX_VALUE;
		maxReached = Integer.MIN_VALUE;
		adapter.setSelected(initialSelection, true);
		dragSelectActive = active;
		this.initialSelection = initialSelection;
		lastDraggingIndex = initialSelection;
		//CMN.Log("Drag selection initialized, starting at index ", initialSelection);
		return true;
	}
	
	public boolean getDragSelectActive() {
		return dragSelectActive;
	}
	
	@Override
	public void setAdapter(Adapter adapter) {
		if (!(adapter instanceof IDragSelectAdapter)) {
			throw new IllegalArgumentException("Adapter must be implement IDragSelectAdapter.");
		}
		this.adapter = (IDragSelectAdapter) adapter;
		super.setAdapter(adapter);
	}
	
	/**0=not in; 1=bottom; 2=top*/
	private int inHotspot;
	//boolean bUseRunnable = true;
	boolean bUseRunnable = false;
	
	private Runnable autoScrollRunnable = new Runnable() {
		@Override
		public void run() {
			scrollBy(0, autoScrollVelocity);
			if(bUseRunnable && dragSelectActive) {
				postDelayed(this, AUTO_SCROLL_DELAY);
			}
		}
	};
	
	@Override
	public void computeScroll() {
		super.computeScroll();
		if(dragSelectActive && !bUseRunnable && inHotspot>0) {
			scrollBy(0, autoScrollVelocity);
		}
	}
	
	private void postScroll(int newHot) {
		inHotspot = newHot;
		removeCallbacks(autoScrollRunnable);
		if (newHot>0) {
			//CMN.Log("Now in hotspot : "+newHot);
			postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
		}
	}

	private int getItemPosition(MotionEvent e) {
		final View v = findChildViewUnder(e.getX(), e.getY());
		if (v == null) {
			return NO_POSITION;
		}
		return getChildAdapterPosition(v);
	}
	
//	@Override
//	public void onDraw(Canvas c) {
//		super.onDraw(c);
//		Paint debugPaint = new Paint();
//		debugPaint.setColor(Color.BLACK);
//		debugPaint.setAntiAlias(true);
//		debugPaint.setStyle(Paint.Style.FILL);
//		RectF topBoundRect = new RectF(0, hotspotTopStart, getMeasuredWidth(), hotspotTopEnd);
//		RectF bottomBoundRect = new RectF(0, hotspotBottomStart, getMeasuredWidth(), hotspotBottomEnd);
//		c.drawRect(topBoundRect, debugPaint);
//		c.drawRect(bottomBoundRect, debugPaint);
//	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		if (dragSelectActive && adapter!=null && adapter.getItemCount()>0) {
			if (e.getAction() == MotionEvent.ACTION_UP) {
				dragSelectActive = false;
				inHotspot = 0;
				removeCallbacks(autoScrollRunnable);
				return true;
			}
			else if (e.getAction() == MotionEvent.ACTION_MOVE) {
				// Check for auto-scroll hotspot
				if (hotspotHeight > -1) {
					float lastY = e.getY();
					float simulatedFactor=0;
					if (/*lastY >= hotspotTopStart && */lastY <= hotspotTopEnd) {
						if (inHotspot!=2) postScroll(2);
						simulatedFactor = hotspotTopEnd;
					} else if (lastY >= hotspotBottomStart/* && lastY <= hotspotBottomEnd*/) {
						if (inHotspot!=1) postScroll(1);
						simulatedFactor = hotspotBottomStart;
					} else if (inHotspot>0) {
						postScroll(0);
						//CMN.Log("Left the hotspot");
					}
					autoScrollVelocity = (int) (lastY - simulatedFactor) / 2;
					//CMN.Log("Auto scroll velocity = ", autoScrollVelocity);
				}
				final int itemPosition = getItemPosition(e);
				// Drag selection Logic
				if (itemPosition != NO_POSITION && lastDraggingIndex != itemPosition) {
					lastDraggingIndex = itemPosition;
					int minReached=0, maxReached=0;
					if (initialSelection > lastDraggingIndex) {
						minReached = lastDraggingIndex;
						maxReached = initialSelection;
					} else if (initialSelection < lastDraggingIndex) {
						minReached = initialSelection;
						maxReached = lastDraggingIndex;
					}
					if (initialSelection == lastDraggingIndex) {
						minReached = lastDraggingIndex;
						maxReached = lastDraggingIndex;
					}
					if ((this.maxReached!=maxReached || minReached!=this.minReached) && adapter != null) {
						selectRangeV1(initialSelection, lastDraggingIndex, minReached, maxReached);
					}
				}
				return true;
			}
		}
		return super.dispatchTouchEvent(e);
	}
	
	private void selectRangeV1(int from, int to, int min, int max)
	{
		int start = min, end=max;
		//CMN.Log("selectRangeV1", minReached, maxReached, min, max);
		if (minReached<min) {
			for (int i = minReached; i < Math.min(min, maxReached); i++) {
				adapter.setSelected(i, false);
			}
		} else if(minReached==min) {
			start = Math.max(maxReached, min);
		}
		if (maxReached>max) {
			for (int i = Math.max(max+1, minReached); i <= maxReached; i++) {
				adapter.setSelected(i, false);
			}
		} else if(maxReached==max) {
			end = Math.min(minReached, max);
		}
		for (int i = start; i < end; i++) {
			adapter.setSelected(i, true);
		}
		minReached = min;
		maxReached = max;
	}
}