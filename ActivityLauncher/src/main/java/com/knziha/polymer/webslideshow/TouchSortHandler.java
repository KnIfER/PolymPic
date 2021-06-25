package com.knziha.polymer.webslideshow;


import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Utils.CMN;

import static com.google.android.material.color.MaterialColors.ALPHA_FULL;

public class TouchSortHandler extends ItemTouchHelper.Callback {
	public interface MoveSwapAdapter {
		void onMove(int fromPosition,int toPosition);
		void onSwiped(int position);
		void onDragFinished(RecyclerView.ViewHolder viewHolder);
	}
	private MoveSwapAdapter moveSwap;
	private Drawable background = null;
	private RecyclerView.ViewHolder draggingView = null;
	public boolean alterAlpha = true;
	public boolean isDragging=false;
	public boolean hasDragDecoration=false;
	public ItemTouchHelper touchHelper;
	public int mItemPadStart;
	public int mItemPadEnd;
	public boolean bAllowFastDrag = true;
	private final int mDragFlags;
	private final int mSwipeFlags;
	public float mAutoScrollSpeed = 1;
	public Drawable dragBackground;
	public float dragScale = 1;
	public TouchSortHandler(MoveSwapAdapter itemTouchAdapter, int mDragFlags, int mSwipeFlags){
		this.moveSwap = itemTouchAdapter;
		this.mDragFlags = mDragFlags;
		this.mSwipeFlags = mSwipeFlags;
	}
	@Override
	public boolean isLongPressDragEnabled() {
		return false;
	}
	@Override
	public boolean isItemViewSwipeEnabled() {
		return true;
	}
	@Override
	public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		int dragFlags = mDragFlags;
		int swipeFlags = mSwipeFlags;
		int position = viewHolder.getLayoutPosition();
		if(position<mItemPadStart
				|| mItemPadEnd>0 && position>=recyclerView.getAdapter().getItemCount()-mItemPadEnd) {
			dragFlags=swipeFlags=0;
		}
		return makeMovementFlags(dragFlags, swipeFlags);
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		int fromPosition = viewHolder.getLayoutPosition();
		int toPosition = target.getLayoutPosition();
		moveSwap.onMove(fromPosition-mItemPadStart,toPosition-mItemPadStart);
		return true;
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		int position = viewHolder.getLayoutPosition();
		moveSwap.onSwiped(position-mItemPadStart);
		viewHolder.itemView.setAlpha(1);
	}

	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && alterAlpha)
		{
			//滑动时改变Item的透明度
			if ((mSwipeFlags&ItemTouchHelper.UP)!=0||(mSwipeFlags&ItemTouchHelper.DOWN)!=0) {
				final float alpha = ALPHA_FULL - Math.abs(dY) / (float) viewHolder.itemView.getHeight();
				viewHolder.itemView.setAlpha(alpha);
				viewHolder.itemView.setTranslationY(dY);
			} else {
				final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
				viewHolder.itemView.setAlpha(alpha);
				viewHolder.itemView.setTranslationX(dX);
			}
		}
		else {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
		}
	}

	@Override
	public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
		if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && draggingView==viewHolder
			&& viewHolder!=null && !hasDragDecoration)
		{
			hasDragDecoration = true;
			if (dragBackground!=null) {
				background = viewHolder.itemView.getBackground();
				viewHolder.itemView.setBackground(dragBackground);
			}
		}
		super.onSelectedChanged(viewHolder, actionState);
		CMN.Log("onSelectedChanged::", viewHolder==null?"null":"viewHolder"
				, actionState, actionState == ItemTouchHelper.ACTION_STATE_DRAG
				//, recyclerView.getScrollState(), isDraggingDown
		);
		if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && !isDragging && touchHelper!=null && viewHolder==draggingView) {
			startDrag(viewHolder);
			touchHelper.startDrag(viewHolder);
			CMN.Log("—— restart dragging ——");
		}
		if(bAllowFastDrag && isDragging && viewHolder==null) {
			stopDrag();
		}
	}
	
	private void stopDrag() {
		//CMN.Log("onDragFinished::", draggingView, lastDragToPosition);
		if (dragScale!=1) {
			draggingView.itemView.animate().scaleX(1).scaleY(1);
		}
		moveSwap.onDragFinished(draggingView);
		isDragging=false;
	}
	
	private final Interpolator sDragViewScrollInterpolator = new LinearInterpolator();
	
	@Override
	public int interpolateOutOfBoundsScroll(@NonNull RecyclerView recyclerView
			, int viewSize, int viewSizeOutOfBounds, int totalSize, long msSinceStartScroll) {
		//CMN.Log("interpolateOutOfBoundsScroll", viewSize, viewSizeOutOfBounds, totalSize);
		//if(true) return super.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll);
		int absOutOfBounds = Math.abs(viewSizeOutOfBounds);
		int direction = (int) Math.signum(viewSizeOutOfBounds);
		float outOfBoundsRatio = Math.min(1f, 1f * absOutOfBounds / viewSize);
		float interpolator = sDragViewScrollInterpolator.getInterpolation(outOfBoundsRatio);
		int maxScroll = (int) (8* GlobalOptions.density);
//			if (absOutOfBounds<Math.max(Math.min(viewSize/2, 35*GlobalOptions.density), bottomPaddding/2+10*GlobalOptions.density)) {
//			} else {
//				interpolator -= 0.2;
//				maxScroll = getMaxDragScroll(recyclerView);
//			}
		return (int) (maxScroll * direction * interpolator * mAutoScrollSpeed);
	}
	
	@Override
	public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		super.clearView(recyclerView, viewHolder);
		removeDragDecoration(viewHolder);
		if(!bAllowFastDrag && isDragging) {
			stopDrag();
			if (dragScale!=1) {
				draggingView.itemView.setScaleX(1);
				draggingView.itemView.setScaleY(1);
			}
		}
	}
	
	public void removeDragDecoration(RecyclerView.ViewHolder viewHolder) {
		if (hasDragDecoration) {
			hasDragDecoration = false;
			if(viewHolder!=null && draggingView==viewHolder) {
				viewHolder.itemView.setAlpha(1.0f);
				if (background != null)
				{
					viewHolder.itemView.setBackground(background);
					background = null;
				}
			}
		}
	}
	
	public void startDrag(RecyclerView.ViewHolder vh) {
		if (dragScale!=1) {
			vh.itemView.animate().scaleX(dragScale).scaleY(dragScale);
		}
		draggingView = vh;
		isDragging = true;
	}
}