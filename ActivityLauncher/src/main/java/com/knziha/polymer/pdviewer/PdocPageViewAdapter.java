package com.knziha.polymer.pdviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.databinding.DocPageItemBinding;
import com.knziha.polymer.webslideshow.RecyclerViewPager;
import com.knziha.polymer.webslideshow.RecyclerViewPagerAdapter;
import com.knziha.polymer.widgets.Utils;

public class PdocPageViewAdapter extends RecyclerViewPagerAdapter<BrowserActivity.ViewDataHolder<DocPageItemBinding>> implements View.OnTouchListener, OnPageChangeListener {
	private final PDocViewerActivity a;
	private final ViewGroup viewpagerParent;
	
	public PdocPageViewAdapter(Context context, ViewGroup vg, RecyclerViewPager recyclerViewPager
			, ItemTouchHelper.Callback rvpSwipeCb, PDocViewerActivity a
			, int itemPad, int itemWidth) {
		super(context, recyclerViewPager, rvpSwipeCb, itemPad, itemWidth);
		this.a = a;
		this.viewpagerParent = vg;
		recyclerViewPager.setOnScrollChangedListener(this);
		recyclerViewPager.setClipChildren(true);
		recyclerViewPager.setOnTouchListener(this);
	}
	
	public int getItemCount() {
		return 2+a.currentViewer.pages();
	}
	
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
		boolean changed = pageScoper.recalcScope();
		if(changed) {
			a.currentViewer.resetLoRThumbnailTick();
			for (int position = pageScoper.scopeStart; position <= pageScoper.scopeEnd; position++) {
				DocPageItemBinding vh = ((BrowserActivity.ViewDataHolder<DocPageItemBinding>) mViewPager.getChildAt(position-pageScoper.scopeStart).getTag()).data;
				resideThumbnailToAdapterView(vh, position);
			}
		}
	}
	
	private void resideThumbnailToAdapterView(DocPageItemBinding vh, int position) {
		Bitmap bitmap=a.currentViewer.getLoRThumbnailForPageAt(position);
		if(vh.iv.getTag()!=bitmap) {
			vh.iv.setImageBitmap(bitmap);
			vh.iv.setTag(bitmap);
		}
		if(bitmap==null) {
			a.currentViewer.requestLoRThumbnailForPageAt(position);
		}
	}
	
	@Override
	public void onClick(View v) {
		BrowserActivity.ViewDataHolder<?> vh = (BrowserActivity.ViewDataHolder<?>) v.getTag();
		mViewPager.smoothScrollToPosition(mViewPager.getChildAdapterPosition(vh.itemView));
		a.currentViewer.goToPageCentered(vh.position);
	}
	
	@NonNull
	@Override
	public BrowserActivity.ViewDataHolder<DocPageItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		BrowserActivity.ViewDataHolder<DocPageItemBinding> vh = new BrowserActivity.ViewDataHolder<>(DocPageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		vh.itemView.setOnClickListener(this);
		vh.itemView.setOnTouchListener(this);
		return vh;
	}
	
	@Override
	public void onBindViewHolder(@NonNull BrowserActivity.ViewDataHolder<DocPageItemBinding> viewHolder, int position) {
		DocPageItemBinding vh = viewHolder.data;
		ImageView iv = vh.iv;
		position-=1;
		viewHolder.position=position;
		View itemView = viewHolder.itemView;
		vh.tv.setText(String.valueOf(position));
		iv.getLayoutParams().height=ViewGroup.LayoutParams.MATCH_PARENT;
		if(a.targetIsPage(position)) {
			itemView.getLayoutParams().width=mViewPager.mItemWidth;
			resideThumbnailToAdapterView(vh, position);
			itemView.setVisibility(View.VISIBLE);
		}
		else {
			iv.setTag(R.id.home, null);
			iv.setImageBitmap(null);
			itemView.getLayoutParams().width=(a.root.getWidth()-mViewPager.mItemWidth)/2-3*mViewPager.itemPad;
			itemView.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	protected void updateItemAt(Object obj, int position) {
		if(pageScoper.pageInScope(position))
		{
			View ca = mViewPager.getChildAt(position - pageScoper.scopeStart);
			if(ca!=null) {
				Bitmap bm = ((PDocView)obj).getLoRThumbnailForPageAt(position);
				((BrowserActivity.ViewDataHolder<DocPageItemBinding>)ca.getTag()).data.iv.setImageBitmap(bm);
			}
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(a.MainMenuListVis && event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			reorderViewPager(true);
		}
		return false;
	}
	
	public void reorderToBackIfVis() {
		if(viewpagerParent.getVisibility()==View.VISIBLE) {
			reorderViewPager(false);
		}
	}
	
	public void reorderViewPager(boolean front) {
		ViewGroup rootiumPageView = a.root;
		int cc = rootiumPageView.getChildCount();
		View ca = rootiumPageView.getChildAt(cc - 1);
		if(front) {
			if(ca!=viewpagerParent) {
				Utils.removeIfParentBeOrNotBe(viewpagerParent, null, false);
				rootiumPageView.addView(viewpagerParent, cc-1);
			}
		} else {
			if(ca==viewpagerParent) {
				Utils.removeIfParentBeOrNotBe(viewpagerParent, null, false);
				rootiumPageView.addView(viewpagerParent, cc-3);
			}
		}
	}
	
	@Override
	public void OnPageChange(int oldPage, int newPage) {
		newPage++;
		mViewPager.scrollToPosition(newPage);
		mViewPager.smoothScrollToPosition(newPage);
	}
	
	public boolean togglePagesVisibility() {
		boolean vis = viewpagerParent.getVisibility()==View.VISIBLE;
		if(vis) {
			viewpagerParent.setVisibility(View.GONE);
			a.currentViewer.pageScoper = null;
			a.currentViewer.setOnPageChangeListener(null);
		} else {
			viewpagerParent.setVisibility(View.VISIBLE);
			a.currentViewer.pageScoper = pageScoper;
			a.currentViewer.setOnPageChangeListener(this);
			reorderViewPager(true);
		}
		return vis;
	}
}
