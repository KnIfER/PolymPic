package com.knziha.polymer.pdviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.filepicker.widget.HorizontalNumberPicker;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.databinding.DocPageItemBinding;
import com.knziha.polymer.webslideshow.RecyclerViewPager;
import com.knziha.polymer.webslideshow.RecyclerViewPagerAdapter;
import com.knziha.polymer.widgets.Utils;

import java.lang.ref.WeakReference;

public class PdocPageViewAdapter extends RecyclerViewPagerAdapter<BrowserActivity.ViewDataHolder<DocPageItemBinding>> implements View.OnTouchListener, RecyclerViewPager.OnPageChangedListener {
	private final PDocViewerActivity a;
	private final ViewGroup viewpagerParent;
	private final TextView pageIndicator;
	private final RecyclerView.RecycledViewPool recyclerViewPool;
	private final GridLayoutManager gridLayoutManager;
	private TextView lastSelTv;
	private boolean tapping;
	
	public PdocPageViewAdapter(Context context, ViewGroup vg, RecyclerViewPager recyclerViewPager
			, ItemTouchHelper.Callback rvpSwipeCb, PDocViewerActivity a
			, int itemPad, int itemWidth) {
		super(context, recyclerViewPager, rvpSwipeCb, itemPad, itemWidth);
		this.a = a;
		this.viewpagerParent = vg;
		layoutManager.setInitialPrefetchItemCount(8);
		recyclerViewPager.setOnScrollChangedListener(this);
		recyclerViewPager.setClipChildren(true);
		recyclerViewPager.setOnTouchListener(this);
		pageIndicator = vg.findViewById(R.id.pageIndicator);
		pageIndicator.setOnClickListener(this);
		recyclerViewPool = Utils.MaxRecyclerPool(64);
		recyclerViewPager.setRecycledViewPool(recyclerViewPool);
		gridLayoutManager = new GridLayoutManager(a, 6);
	}
	
	public int getItemCount() {
		return headViewSize*2+a.currentViewer.pages();
	}
	
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
		boolean changed = pageScoper.recalcScope();
		if(changed) {
			//CMN.Log("onScrollChange",  pageScoper.scopeStart, pageScoper.scopeEnd);
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
			//CMN.Log("requestLoRThumbnailForPageAt", position);
			a.currentViewer.requestLoRThumbnailForPageAt(position);
		}
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id ==R.id.pageIndicator || id ==R.id.grid) {
			WeakReference<AlertDialog> dlg = (WeakReference<AlertDialog>) pageIndicator.getTag();
			AlertDialog d = null;
			if(dlg!=null) {
				d = dlg.get();
			}
			if(id ==R.id.grid) {
				CheckedTextView checkGrid = (CheckedTextView) v;
				checkGrid.toggle();
				setGridView(checkGrid.isChecked());
				if(d!=null) {
					d.dismiss();
				}
			} else {
				if(d==null) {
					View view = a.getLayoutInflater().inflate(R.layout.doc_jump_dlg, null);
					HorizontalNumberPicker numberpicker = view.findViewById(R.id.numberpicker);
					numberpicker.setMinValue(1);
					numberpicker.setMaxValue(a.currentViewer.getPageCount());
					numberpicker.setValue(mViewPager.getCurrentPosition()-headViewSize);
					numberpicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
						newVal--;
						numberpicker.setTag(newVal);
						if(false) {
							int newPos = newVal+headViewSize;
							mViewPager.scrollToPosition(newPos);
							mViewPager.smoothScrollToPosition(newPos);
						}
					});
					CheckedTextView checkGrid = view.findViewById(R.id.grid);
					checkGrid.setOnClickListener(this);
					checkGrid.setChecked(mViewPager.isGridView());
					d = new AlertDialog.Builder(a)
							.setView(view)
							.create();
					d.setOnDismissListener(dialog -> {
						Object tag = numberpicker.getTag();
						if(tag instanceof Integer) {
							a.currentViewer.goToPageCentered((int) tag, false);
							numberpicker.setTag(null);
						}
					});
					v.setTag(new WeakReference<>(d));
				}
				d.show();
			}
		} else {
			BrowserActivity.ViewDataHolder<?> vh = (BrowserActivity.ViewDataHolder<?>) v.getTag();
			if(a.targetIsPage(vh.position)) {
				tapping = true;
				updateIndicatorAndCircle(vh.position);
				mViewPager.smoothScrollToPosition(mViewPager.getChildAdapterPosition(vh.itemView));
				a.currentViewer.goToPageCentered(vh.position, true);
				tapping = false;
			}
		}
	}
	
	private void setGridView(boolean gridify) {
		int position = a.currentViewer.getCurrentPageOnScreen();
		if(gridify) {
			// 网格化显示
			mViewPager.getLayoutParams().height = (int) (GlobalOptions.density*65*6);
			headViewSize = 0;
			viewpagerParent.setClipChildren(true);
			mViewPager.setLayoutManager(gridLayoutManager);
			mViewPager.setPadding(0, (int) (GlobalOptions.density*10), 0, 0);
		} else {
			// 卷轴显示
			mViewPager.getLayoutParams().height = (int) (GlobalOptions.density*65*1);
			headViewSize = 1;
			viewpagerParent.setClipChildren(false);
			mViewPager.setLayoutManager(layoutManager);
			mViewPager.setPadding(0, 0, 0, 0);
		}
		mViewPager.setItemAnimator(null);
		position+=headViewSize;
		notifyDataSetChanged();
		mViewPager.scrollToPosition(position);
		mViewPager.smoothScrollToPosition(position);
	}
	
	@NonNull
	@Override
	public BrowserActivity.ViewDataHolder<DocPageItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		BrowserActivity.ViewDataHolder<DocPageItemBinding> vh = new BrowserActivity.ViewDataHolder<>(DocPageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		vh.itemView.setOnClickListener(this);
		vh.itemView.setOnTouchListener(this);
		CMN.Log("onCreateViewHolder");
		return vh;
	}
	
	@Override
	public void onBindViewHolder(@NonNull BrowserActivity.ViewDataHolder<DocPageItemBinding> viewHolder, int position) {
		DocPageItemBinding vh = viewHolder.data;
		ImageView iv = vh.iv;
		position-=headViewSize;
		viewHolder.position=position;
		View itemView = viewHolder.itemView;
		vh.tv.setText(String.valueOf(position));
		iv.getLayoutParams().height=ViewGroup.LayoutParams.MATCH_PARENT;
		if(a.targetIsPage(position)) {
			itemView.getLayoutParams().width=mViewPager.mItemWidth;
			resideThumbnailToAdapterView(vh, position);
			itemView.setVisibility(View.VISIBLE);
			if(position==a.currentViewer.getCurrentPageOnScreen()) {
				lastSelTv = vh.tv;
				lastSelTv.setBackgroundResource(R.drawable.circle);
			} else {
				vh.tv.setBackgroundResource(0);
			}
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
	public void OnPageChanged(int oldPosition, int newPage) {
		int finalNewPage = newPage+headViewSize;
		mViewPager.scrollToPosition(finalNewPage);
		if(tapping) {
			mViewPager.smoothScrollToPosition(finalNewPage);
		} else {
			mViewPager.post(() -> {
				mViewPager.scrollToPosition(finalNewPage);
				mViewPager.smoothScrollToPosition(finalNewPage);
			});
		}
		updateIndicatorAndCircle(newPage);
	}
	
	private void updateIndicatorAndCircle(int newPage) {
		pageIndicator.setText(" "+(newPage+1)+"/"+a.currentViewer.getPageCount()+" ");
		View ca = mViewPager.getChildAt(newPage - pageScoper.scopeStart);
		if(lastSelTv!=null) {
			lastSelTv.setBackgroundResource(0);
		}
		if(ca!=null) {
			lastSelTv = ((BrowserActivity.ViewDataHolder<DocPageItemBinding>)ca.getTag()).data.tv;
			lastSelTv.setBackgroundResource(R.drawable.circle);
		}
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
