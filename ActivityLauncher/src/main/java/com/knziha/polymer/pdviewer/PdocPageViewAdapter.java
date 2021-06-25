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
import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.databinding.DocPageItemBinding;
import com.knziha.polymer.webslideshow.RecyclerViewPager;
import com.knziha.polymer.webslideshow.RecyclerViewPagerAdapter;
import com.knziha.polymer.webslideshow.RecyclerViewPagerSubsetProvider;
import com.knziha.polymer.webslideshow.ViewUtils;
import com.knziha.polymer.widgets.Utils;
import com.shockwave.pdfium.SearchRecord;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/** This class is used for displaying small thumbnails of all pages or
 * 		only matching pages ( provided via {@link RecyclerViewPagerAdapter#resultsProvider} ) in a search action.  */
public class PDocPageViewAdapter extends RecyclerViewPagerAdapter<ViewUtils.ViewDataHolder<DocPageItemBinding>> implements View.OnTouchListener, RecyclerViewPager.OnPageChangedListener {
	/** Spec Activity. */
	private final PDocViewerActivity a;
	/** The Parent View Group. */
	final ViewGroup viewpagerParent;
	/** Indicates the page number. */
	private final TextView pageIndicator;
	/** The Pool for Recycler View. */
	private final RecyclerView.RecycledViewPool recyclerViewPool;
	/** Turn horizontal view to grid view. */
	private final GridLayoutManager gridLayoutManager;
	/** Putting red circle in selected textView*/
	private TextView lastSelTv;
	/** Whether by scrolling or clicking the user've changed the current page. */
	private boolean tapping;
	
	public PDocPageViewAdapter(Context context, ViewGroup vg, RecyclerViewPager recyclerViewPager
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
		return headViewSize*2+(resultsProvider==null?a.currentViewer.pages():resultsProvider.getResultCount());
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
				View ca = mViewPager.getChildAt(position - pageScoper.scopeStart);
				if(ca==null) {
					continue;
				}
				DocPageItemBinding vh = ((ViewUtils.ViewDataHolder<DocPageItemBinding>) ca.getTag()).data;
				resideThumbnailToAdapterView(vh, resultsProvider==null?position:resultsProvider.getActualPageAtPosition(position));
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
					numberpicker.setValue(mViewPager.getCurrentPosition()-headViewSize+1);
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
			ViewUtils.ViewDataHolder<?> vh = (ViewUtils.ViewDataHolder<?>) v.getTag();
			int position = (int) vh.position;
			if(targetIsPage(position)) {
				tapping = true;
				updateIndicatorAndCircle(position);
				mViewPager.smoothScrollToPosition(mViewPager.getChildAdapterPosition(vh.itemView));
				a.currentViewer.goToPageCentered(resultsProvider==null?position:resultsProvider.getActualPageAtPosition(position), true);
				tapping = false;
			}
		}
	}
	
	private boolean targetIsPage(int position) {
		return position>=0 && position<(resultsProvider==null?a.currentViewer.pages():resultsProvider.getResultCount());
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
	public ViewUtils.ViewDataHolder<DocPageItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ViewUtils.ViewDataHolder<DocPageItemBinding> vh = new ViewUtils.ViewDataHolder<>(DocPageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		vh.itemView.setOnClickListener(this);
		vh.itemView.setOnTouchListener(this);
		CMN.Log("onCreateViewHolder");
		return vh;
	}
	
	@Override
	public void onBindViewHolder(@NonNull ViewUtils.ViewDataHolder<DocPageItemBinding> viewHolder, int position) {
		DocPageItemBinding vh = viewHolder.data;
		ImageView iv = vh.iv;
		position-=headViewSize;
		viewHolder.position=position;
		View itemView = viewHolder.itemView;
		iv.getLayoutParams().height=ViewGroup.LayoutParams.MATCH_PARENT;
		if(targetIsPage(position)) {
			if(resultsProvider!=null) position = resultsProvider.getActualPageAtPosition(position);
			vh.tv.setText(String.valueOf(position+1));
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
	
	/** @param position index of the actual page.
	 *  Must be called on the main thread. */
	@Override
	protected void updateItemAt(Object obj, int position) {
		if(pageScoper.pageInScope(position))
		{
			//CMN.Log("updateItemAt...", position, resultsProvider.getLastQuery());
			View ca = mViewPager.getChildAt((resultsProvider==null?position:resultsProvider.getLastQuery()) - pageScoper.scopeStart);
			if(ca!=null) {
				Bitmap bm = ((PDocView)obj).getLoRThumbnailForPageAt(position);
				((ViewUtils.ViewDataHolder<DocPageItemBinding>)ca.getTag()).data.iv.setImageBitmap(bm);
			}
		}
	}
	
	/** Alter Z-order of the viewpager */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(a.MainMenuListVis && event.getActionMasked()==MotionEvent.ACTION_DOWN) {
			reorderViewPager(true);
		}
		return false;
	}
	
	/** Alter Z-order of the viewpager */
	public void reorderToBackIfVis() {
		if(viewpagerParent.getVisibility()==View.VISIBLE) {
			reorderViewPager(false);
		}
	}
	
	/** Alter Z-order of the viewpager */
	public void reorderViewPager(boolean front) {
		ViewGroup rootiumPageView = a.root;
		int cc = rootiumPageView.getChildCount() - 1;
		int delta = 1;
		View ca = rootiumPageView.getChildAt(cc - delta);
		CMN.Log("reorderViewPager", front, ca, viewpagerParent);
		CMN.recurseLogCascade(rootiumPageView);
		if(front) {
			if(ca!=viewpagerParent) {
				Utils.removeIfParentBeOrNotBe(viewpagerParent, null, false);
				rootiumPageView.addView(viewpagerParent, cc-delta);
			}
		} else {
			if(ca==viewpagerParent) {
				Utils.removeIfParentBeOrNotBe(viewpagerParent, null, false);
				rootiumPageView.addView(viewpagerParent, cc-1-delta);
			}
		}
	}
	
	/** @param newPage index of the new page */
	@Override
	public void OnPageChanged(int oldPosition, int newPage) {
		//if(!targetIsPage(newPage)) return;
		boolean updateInc=true;
		if(resultsProvider!=null) {
			newPage = resultsProvider.queryPositionForActualPage(newPage);
			if(newPage<0) {
				newPage=-newPage-1;
				updateInc = false;
			}
		}
		int finalNewPage = newPage+headViewSize;
		// A little cumbersome but necessary.
		mViewPager.scrollToPosition(finalNewPage);
		if(tapping) {
			mViewPager.smoothScrollToPosition(finalNewPage);
		} else {
			mViewPager.post(() -> {
				mViewPager.scrollToPosition(finalNewPage);
				mViewPager.smoothScrollToPosition(finalNewPage);
			});
		}
		if(updateInc) {
			updateIndicatorAndCircle(newPage);
		}
	}
	
	/** @param newPage position in the adapter without headerview */
	private void updateIndicatorAndCircle(int newPage) {
		pageIndicator.setText(" "+(newPage+1)+"/"+(resultsProvider==null?a.currentViewer.getPageCount():resultsProvider.getResultCount())+" ");
		View ca = mViewPager.getChildAt(newPage - pageScoper.scopeStart);
		if(lastSelTv!=null) {
			lastSelTv.setBackgroundResource(0);
		}
		if(ca!=null) {
			lastSelTv = ((ViewUtils.ViewDataHolder<DocPageItemBinding>)ca.getTag()).data.tv;
			lastSelTv.setBackgroundResource(R.drawable.circle);
		}
	}
	
	public void refreshIndicator() {
		int newPage = a.currentViewer.getCurrentPageOnScreen();
		RecyclerViewPagerSubsetProvider resultsProvider = this.resultsProvider;
		if(resultsProvider!=null) {
			newPage = resultsProvider.queryPositionForActualPage(newPage);
			if(newPage<0) {
				newPage=-newPage-1;
			}
		}
		pageIndicator.setText(" "+(newPage+1)+"/"+(resultsProvider==null?a.currentViewer.getPageCount():resultsProvider.getResultCount())+" ");
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
	
	public void setSearchResults(ArrayList<SearchRecord> arr, String key, int flag) {
		resultsProvider = arr==null?null:new PDocPageResultsProvider(arr, key, flag);
		notifyDataSetChanged();
		if(arr==null) {
			OnPageChanged(0, a.currentViewer.getCurrentPageOnScreen());
		} else {
			refreshIndicator();
		}
	}
	
	public boolean getVisibility() {
		return viewpagerParent.getVisibility()==View.VISIBLE;
	}
	
	public PDocPageResultsProvider getSearchProvider() {
		return (PDocPageResultsProvider)resultsProvider;
	}
}
