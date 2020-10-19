package com.knziha.polymer.webslideshow;

import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import static com.google.android.material.color.MaterialColors.ALPHA_FULL;

public class ViewPagerTouchHelper<T extends RecyclerView.ViewHolder> extends ItemTouchHelper.Callback{
    private final ItemTouchHelperCallback<T> helperCallback;

    public ViewPagerTouchHelper(ItemTouchHelperCallback<T> helperCallback) {
        this.helperCallback = helperCallback;
    }
    
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.UP);
    }
    
    @Override
    public boolean isLongPressDragEnabled() {
        return super.isLongPressDragEnabled();
    }
    
    @Override
    public boolean isItemViewSwipeEnabled() {
        return super.isItemViewSwipeEnabled();
    }
	
	@Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        helperCallback.onItemSlide((T) viewHolder,target.getAdapterPosition());
        return false;
    }
	
	@Override
	public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
		helperCallback.onItemSwipe((T)viewHolder);
	}
	
	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
		if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
			final float alpha = ALPHA_FULL - Math.abs(dY) / (float) viewHolder.itemView.getHeight();
			viewHolder.itemView.setAlpha(alpha);
			viewHolder.itemView.setTranslationY(dY);
		} else {
			super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
		}
	}
	
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
    }
    public interface ItemTouchHelperCallback<T extends RecyclerView.ViewHolder>{
        void onItemSwipe(T viewHolder);
        void onItemSlide(T viewHolder, int toPosition);
    }
}
