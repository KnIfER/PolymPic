package com.knziha.polymer.webslideshow;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.widgets.SpacesItemDecoration;

/**
 * RecyclerViewPagerAdapter </br>
 *
 * @author KnIfER
 * @since 2020/12/02 下午1:16
 */
public abstract class RecyclerViewPagerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements View.OnClickListener, RecyclerView.OnScrollChangedListener {
	protected final RecyclerViewPager mViewPager;
	protected final CenterLinearLayoutManager layoutManager;
	public final PageScope pageScoper = new PageScope();
	public int headViewSize = 1;
	
	public RecyclerViewPagerAdapter(Context context, RecyclerViewPager recyclerViewPager, ItemTouchHelper.Callback rvpSwipeCb, int itemPad, int itemWidth) {
		mViewPager = recyclerViewPager;
		
		recyclerViewPager.setHasFixedSize(true);
		
		recyclerViewPager.setFlingFactor(0.175f);
		recyclerViewPager.setTriggerOffset(0.125f);
		
		setHasStableIds(true);
		recyclerViewPager.setAdapter(this);
		
		layoutManager = new CenterLinearLayoutManager(context);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		
		recyclerViewPager.itemPad = itemPad;
		recyclerViewPager.mItemWidth = itemWidth;
		
		recyclerViewPager.setLayoutManager(layoutManager);
		recyclerViewPager.addItemDecoration(new SpacesItemDecoration(recyclerViewPager.itemPad));
		
		if(rvpSwipeCb!=null) {
			ItemTouchHelper itemTouchHelper=new ItemTouchHelper(rvpSwipeCb);
			itemTouchHelper.attachToRecyclerView(recyclerViewPager);
		}
	}
	
	@Override
    public int getItemCount() {
        return 0;
    }
    
    public class PageScope {
		public int scopeStart;
		public int scopeEnd;
		public void notifyItemChanged(Object obj, int position) {
			mViewPager.post(() -> updateItemAt(obj, position));
		}
	
		public boolean pageInScope(int pageIdx) {
			//CMN.Log("pageInScope", scopeStart, scopeEnd);
			return pageIdx>=scopeStart && scopeStart<=scopeEnd;
		}
	
		public boolean recalcScope() {
			View ca = mViewPager.getChildAt(0);
			int scopeStart, scopeEnd;
			if(ca!=null) {
				scopeStart = mViewPager.getChildAdapterPosition(ca)-headViewSize;
				ca = mViewPager.getChildAt(mViewPager.getChildCount()-1);
				scopeEnd = mViewPager.getChildAdapterPosition(ca)-headViewSize;
				if(scopeStart!=this.scopeStart || scopeEnd!=this.scopeEnd) {
					this.scopeStart = scopeStart;
					this.scopeEnd = scopeEnd;
					return true;
				}
			}
			return false;
		}
	}
	
	protected void updateItemAt(Object obj, int position) {
	
	}
}