/*  Copyright 2021 The 多聚浏览 Open Source Project
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
 **/
package com.knziha.polymer.webslideshow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.TestHelper;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.databinding.SearchViewBinding;
import com.knziha.polymer.databinding.WebPageItemBinding;
import com.knziha.polymer.webslideshow.WebPic.WebPic;
import com.knziha.polymer.widgets.PopupMenuHelper;
import com.knziha.polymer.widgets.SpacesItemDecoration;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.knziha.polymer.widgets.eugene.SearchResultsAdapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import static com.knziha.polymer.widgets.Utils.DummyBMRef;
import static com.knziha.polymer.widgets.Utils.DummyTransX;

/** 管理多窗口(多标签页)视图，包括横向卡片式界面、动画、搜索。 | Manages the ViewPager-like multi-tab view based on RecyclerView. */
public class TabViewAdapter extends RecyclerView.Adapter<ViewUtils.ViewDataHolder<WebPageItemBinding>>
		implements View.OnClickListener, View.OnLongClickListener, PopupMenuHelper.PopupMenuListener {
	public RecyclerViewPager recyclerView;
	public View viewpager_holder;
	
	private LinearInterpolator linearInterpolator = new LinearInterpolator();
	
	ImageView imageViewCover;
	private long tmpBmRefId;
	private WeakReference<Bitmap> tmpBmRef = DummyBMRef;
	private Runnable removeIMCoverRunnable = () -> imageViewCover.setVisibility(View.GONE);
	
	private BrowserActivity a;
	RecyclerView.ViewHolder draggingView;
	ItemTouchHelper touchHelper;
	TouchSortHandler touchHandler;
	
	public CenterLinearLayoutManager layoutManager;
	private int mItemWidth;
	private int mItemHeight;
	
	private int padWidth;
	private int itemPad;
	
	private ColorDrawable AppWhiteDrawable;
	private Drawable frame_web;
	
	private Runnable scrollHereRunnnable;
	private boolean bNeedReCalculateItemWidth;
	
	private View longClickView;
	
	DisplayMetrics dm;
	Options opt;
	private int refPos;
	//private int lastDragFromPosition;
	private boolean draggingCurrentTab;
	
	public TabViewAdapter(BrowserActivity a) {
		this.a = a;
		this.imageViewCover = a.imageViewCover;
		this.dm = a.dm;
		this.opt = a.opt;
	}
	
	@Override
	public boolean onLongClick(View v) {
		RecyclerView.ViewHolder vh = Utils.getViewHolderInParents(v);
		int draggingPosSt = vh.getLayoutPosition()-1;
		if (targetIsPage(draggingPosSt)) {
			CMN.Log("—— startDrag ——");
			draggingView = vh;
			draggingCurrentTab = draggingPosSt==a.adapter_idx;
			//draggingPosStPvNode = draggingPosSt>0? (NavigationNode) displayNodes.get(draggingPosSt-1) :null;
			//draggingNode = (NavigationNode) displayNodes.get(draggingPosSt);
			refPos = a.adapter_idx;
			touchHandler.startDrag(vh);
			touchHelper.startDrag(vh);
		}
		return false;
	}
	
	public int getItemCount() { return 2+a.TabHolders.size(); }
	@NonNull
	@Override
	public ViewUtils.ViewDataHolder<WebPageItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ViewUtils.ViewDataHolder<WebPageItemBinding> vh = new ViewUtils.ViewDataHolder<>(WebPageItemBinding.inflate(a.getLayoutInflater(), parent, false));
		vh.itemView.setOnClickListener(this);
		vh.itemView.setOnLongClickListener(this);
		vh.data.title.setOnClickListener(this);
		vh.data.close.setOnClickListener(this);
		//CMN.Log("onCreateViewHolder");
		return vh;
	}
	
	@Override
	public void onBindViewHolder(@NonNull ViewUtils.ViewDataHolder<WebPageItemBinding> viewHolder, int position) {
		//CMN.Log("onBindViewHolder", position);
		WebPageItemBinding vh = viewHolder.data;
		ImageView iv = vh.iv;
		position-=1;
		View itemView = viewHolder.itemView;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			itemView.setForeground(null);
		}
		//iv.setImageBitmap(Pages.get(position).getBitmap());
		iv.getLayoutParams().height=ViewGroup.LayoutParams.MATCH_PARENT;
		if(targetIsPage(position)) {
			BrowserActivity.TabHolder holder = a.TabHolders.get(position);
			iv.setTag(R.id.home, false);
			itemView.getLayoutParams().width=mItemWidth;
			itemView.setVisibility(View.VISIBLE);
			String title=holder.title;
			if(title==null||title.equals("")) title=holder.url;
			vh.title.setText(title);
			vh.title.setText(holder.id+" :  "+title);
			vh.title.setText((position+1)+" :  "+title);
			//int version = mWebView==null?0:(int) mWebView.version; //ttt
			int version = holder.version;
			//CMN.Log("setImageBitmap__", iv);
			viewHolder.position=holder.id;
			a.glide.load(new WebPic(holder.id, version, a.id_table))
					.into(iv);
		}
		else {
			iv.setTag(R.id.home, null);
			//iv.setImageBitmap(null);
			itemView.getLayoutParams().width=padWidth;
			itemView.setVisibility(View.INVISIBLE);
			//vh.itemView.getLayoutParams().width=mItemWidth;
		}
	}
	
	public long getItemId(int position) {
		position-=1;
		if(position>=0&&position<a.TabHolders.size()) {
			BrowserActivity.TabHolder holder = a.TabHolders.get(position);
			return holder.id;
		}
		if(position==a.TabHolders.size()) {
			return -2;
		}
		return position;
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		boolean ret=true;
		boolean dismiss = !isLongClick;
		boolean blink = false;
		ViewUtils.ViewDataHolder<WebPageItemBinding> viewHolder = (ViewUtils.ViewDataHolder<WebPageItemBinding>) Utils.getViewHolderInParents(longClickView);
		int position = viewHolder.getLayoutPosition()-1;
		BrowserActivity.TabHolder tab = a.TabHolders.get(position);
		if (a!=null) {
			switch (v.getId()) {
				case R.string.copy_title:{
					a.showT(tab.title);
				} break;
				case R.string.newtab_prev:{
					a.newTab(null, false, true, position);
				} break;
				case R.string.newtab_nxt:{
					a.newTab(null, false, true, position+1);
				} break;
				case R.string.web_screen_shot:{
					WebFrameLayout wv = a.id_table.get(tab.id);
					if (wv==null) {
						blink = true;
					} else {
						//PDFPrintManager.printPDF(null, wv.mWebView, true);
						Bitmap bm = Bitmap.createBitmap(wv.mWebView.getContentWidth(), wv.mWebView.getContentHeight(), Bitmap.Config.ARGB_8888);
						Canvas canvas = new Canvas(bm);
						wv.implView.draw(canvas);
						try (FileOutputStream fout = new FileOutputStream("/sdcard/tmp.png")){
							bm.compress(Bitmap.CompressFormat.PNG, 100, fout);
						} catch (IOException e) {
							CMN.Log(e);
						}
					}
				} break;
			}
		}
		if (blink) {
			Utils.blinkView(v, false);
		} else if (dismiss) {
			popupMenuHelper.postDismiss(80);
		}
		return ret;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		ViewUtils.ViewDataHolder<WebPageItemBinding> vh = (ViewUtils.ViewDataHolder<WebPageItemBinding>) Utils.getViewHolderInParents(v);
		int target = vh.getLayoutPosition()-1; // 不能是 vh.position
		if(id ==R.id.title) {
			longClickView = v;
			PopupMenuHelper popupMenu = a.getPopupMenu();
			if (popupMenu.tag!=R.string.save_thumb_as) {
				int[] texts = new int[] {
					R.string.copy_title
					,R.string.browse_prev
					,R.string.browser_nxt
					,R.string.locate_currtab
					,R.string.newtab_prev
					,R.string.newtab_nxt
					,R.string.show_tabid
					,R.string.show_close
					,R.string.allow_lclk_sort
					,R.string.unload_tab
					,R.string.save_webstacks
					,R.string.web_screen_shot
					,R.string.save_thumb_as
				};
				popupMenu.initLayout(texts, this);
				popupMenu.tag=R.string.save_thumb_as;
			}
			int[] vLocationOnScreen = new int[2];
			recyclerView.getLocationOnScreen(vLocationOnScreen);
			popupMenu.show(a.root, recyclerView.mLastTouchX+vLocationOnScreen[0], recyclerView.mLastTouchY+vLocationOnScreen[1]);
		} else if(id ==R.id.close) {
			a.closeTabAt(target, 0);
		} else {
//						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//							if(target!=adapter_idx) {
//								vh.itemView.setForeground(frame_web);
//							}
//						}
			
			//CMN.Log("tabView onClick::", target, vh.getLayoutPosition()-1, recyclerViewPager.getChildAdapterPosition(vh.itemView)-1);
			if(targetIsPage(target)) {
				// 点击视图
				//AttachWebAt(target);
				//currentWebView.setVisibility(View.INVISIBLE);
				DismissingViewHolder =false;
				findBMTmpInViewHolder(vh);
				toggleTabView(target, v, 0);
			}
		}
	}
	
	private void findBMTmpInViewHolder(ViewUtils.ViewDataHolder<WebPageItemBinding> vh) {
		//CMN.Log("findBMTmpInViewHolder", vh);
		if(vh!=null) {
			Drawable d = vh.data.iv.getDrawable();
			if(d instanceof BitmapDrawable) {
				tmpBmRefId = vh.position;
				tmpBmRef = new WeakReference<>(((BitmapDrawable)d).getBitmap());
				return;
			}
		}
		if (tmpBmRefId!=0) {
			tmpBmRefId = 0;
			tmpBmRef = DummyBMRef;
		}
	}
	
	public void toggle(View v) {
		a.onLeaveCurrentTab(DismissingViewHolder ?0:1);
		boolean post = false;
		long delay=0;
		boolean init = viewpager_holder==null;
		if(init) {
			init_tabs_layout();
			post=true;
			if(opt.getAnimateTabsManager()) {
				delay = 350;
			} else {
				post = false;
				delay = 180;
			}
		} else if(bNeedReCalculateItemWidth && DismissingViewHolder) {
			calculateItemWidth(false);
			bNeedReCalculateItemWidth = false;
		}
		if(false) {
			//post = false;
			if(post) {
				recyclerView.postDelayed(() -> toggleTabView(-1, v, 0), delay);
			} else {
				toggleTabView(-1, v, 0);
			}
		}
		toggleTabView(-1, v, init?0x2:0);
	}
	
	private void init_tabs_layout() {
		viewpager_holder = a.UIData.viewpagerHolder.getViewStub().inflate();
		//viewpager_holder.setAlpha(0.5f);
		RecyclerViewPager recyclerViewPager = viewpager_holder.findViewById(R.id.viewpager);
		recyclerView = recyclerViewPager;
		recyclerViewPager.setHasFixedSize(true);
		AppWhiteDrawable = new ColorDrawable(Toastable_Activity.AppWhite);
		frame_web = a.mResource.getDrawable(R.drawable.frame_web);
		itemPad = (int) (a.mResource.getDimension(R.dimen._35_)/5);
		calculateItemWidth(false);
		scrollHereRunnnable = () -> {
			int target = (mItemWidth+2*itemPad)*a.adapter_idx;
			//recyclerView.requestLayout();
			View C0 = recyclerViewPager.getChildAt(0);
			if(C0!=null){
				int p=((ViewUtils.ViewDataHolder)C0.getTag()).getLayoutPosition()-1;
				if(p<0) {
					target-= -C0.getLeft() + itemPad;
				} else {
					target-=(mItemWidth+2*itemPad)*p+padWidth+2*itemPad-C0.getLeft() + itemPad;
				}
			}
			//CMN.Log("首发", C0==null, target);
			recyclerViewPager.scrollBy(target,0);
			//layoutManager.targetPos = ViewUtils.getCenterXChildPositionV1(recyclerViewPager);
			layoutManager.targetPos = a.adapter_idx + 1;
			CMN.Log("scrollHere", ViewUtils.getCenterXChildPositionV2(recyclerViewPager), layoutManager.targetPos);
		};
		recyclerViewPager.setFlingFactor(0.175f * 1.0f);
		recyclerViewPager.setTriggerOffset(0.125f * 105.5f);
		recyclerViewPager.setTriggerOffset(10060*GlobalOptions.density);
		
		setHasStableIds(true);
		recyclerViewPager.setAdapter(this);
		layoutManager = new CenterLinearLayoutManager(a);
		layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
		//recyclerViewPager.setHasFixedSize(true);
		recyclerViewPager.setLayoutManager(layoutManager);
		recyclerViewPager.addItemDecoration(new SpacesItemDecoration(itemPad));
		
		TouchSortHandler touchHandler = new TouchSortHandler(new TouchSortHandler.MoveSwapAdapter() {
			int lastDragToPos;
			@Override
			public void onMove(int fromPosition, int toPosition) {
				//CMN.Log("onMove", fromPosition, toPosition);
				if (fromPosition<0) fromPosition=0;
				toPosition = Math.max(0, Math.min(a.TabHolders.size()-1, toPosition));
				lastDragToPos = toPosition;
				if (fromPosition != toPosition) {
					if (draggingCurrentTab) {
						refPos = toPosition;
					}
					if (fromPosition < toPosition) {
						for (int i = fromPosition; i < toPosition; i++) {
							Collections.swap(a.TabHolders, i, i + 1);
						}
					} else {
						for (int i = fromPosition; i > toPosition; i--) {
							Collections.swap(a.TabHolders, i, i - 1);
						}
					}
					notifyItemMoved(fromPosition+1, toPosition+1);
				}
			}
			@Override
			public void onSwiped(int position) {
				a.closeTabAt(position, 1);
			}
			@Override
			public void onDragFinished(RecyclerView.ViewHolder viewHolder) {
				a.tabsDirty = true;
				//adapter_idx = ;
				a.fast_recalc_adapter_idx(refPos);
				int target = ViewUtils.getCenterXChildPositionV2(recyclerViewPager);
				target = Math.max(1, Math.min(getItemCount()-1, target));
				recyclerViewPager.smoothScrollToPosition(target);
			}
		}, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.UP);
		ItemTouchHelper touchHelper=new ItemTouchHelper(touchHandler);
		touchHandler.mAutoScrollSpeed = 1.6f;
		touchHandler.mItemPadStart = 1;
		touchHandler.mItemPadEnd = 1;
		//touchHandler.bAllowFastDrag = false;
		touchHandler.touchHelper = touchHelper;
		touchHandler.dragScale = 1.1f;
		touchHelper.attachToRecyclerView(recyclerViewPager);
		this.touchHandler = touchHandler;
		this.touchHelper = touchHelper;
	}
	
	public boolean isVisible() {
		return !DismissingViewHolder;
	}
	
	public void onConfigurationChanged() {
		if(isVisible()) {
			calculateItemWidth(true);
		} else {
			bNeedReCalculateItemWidth = true;
		}
	}
	
	int MoveRangePositon;
	Runnable MoveRangeIvalidator = new Runnable() {
		@Override
		public void run() {
			notifyItemRangeChanged(MoveRangePositon, a.TabHolders.size() - MoveRangePositon);
			//adaptermy.notifyItemRangeChanged(MoveRangePositon, MoveRangePositon+1);
		}
	};
	
	public void notifyItemRemovedAt(int positon, int source) {
		if (viewpager_holder!=null) {
			positon++;
			super.notifyItemRemoved(positon);
			//notifyDataSetChanged();
			//notifyItemRangeRemoved(positon, 1);
			//notifyItemRangeChanged(positon, TabHolders.size()-positon);
//				MoveRangePositon = positon;
			Runnable MoveRangeIvalidator = new ItemInvalidator(positon);
			//viewpager_holder.removeCallbacks(MoveRangeIvalidator);
			if (source == 0) {
				viewpager_holder.postDelayed(MoveRangeIvalidator, 180);
			} else {
				viewpager_holder.post(MoveRangeIvalidator);
			}
		}
	}
	
	Dialog d = null;
	SearchViewBinding searchView;
	public void showSearchView(boolean showKeyboard) {
		if(d==null){
			d = new Dialog(a);
			d.setCancelable(true);
			d.setCanceledOnTouchOutside(true);
			d.show();
			if (!Utils.bigCake) {
				Utils.removeView(d.findViewById(android.R.id.title));
			}
			searchView = SearchViewBinding.inflate(LayoutInflater.from(a), a.root, false);
			searchView.searchHolder.d = d;
			searchView.searchHolder.setSearchAdapter(new SearchResultsAdapter(){
				@Override
				public String getText(Object item) {
					return ((BrowserActivity.TabHolder)item).title;
				}
				@Override
				public boolean onQueryTextSubmit(CharSequence query) {
					return false;
				}
				@Override
				public boolean onQueryTextChange(CharSequence newText) {
					String str = newText.toString().toLowerCase();
					ArrayList<BrowserActivity.TabHolder> results = new ArrayList<>();
					BrowserActivity.TabHolder curr = a.currentWebHolder;
					int selection = -1;
					for (int i = 0, len=a.TabHolders.size(); i < len; i++) {
						BrowserActivity.TabHolder tab = a.TabHolders.get(i);
						// todo more performant search
						if (tab.title!=null && (tab.title.toLowerCase().contains(str) || tab.url.toLowerCase().contains(str))) {
							if (tab==curr) {
								selection = results.size();
							}
							results.add(tab);
						}
					}
					searchView.searchHolder.setResults(results, selection);
					return false;
				}
				@Override
				public boolean onItemClick(Object obj, boolean locateTo) {
					BrowserActivity.TabHolder tab = ((BrowserActivity.TabHolder) obj);
					int newPos = a.TabHolders.indexOf(tab);
					CMN.Log("onItemClick::", newPos);
					if (locateTo) {
					
					} else if(newPos>=0){
						searchView.searchHolder.hide();
						a.selectTab(newPos);
						return true;
					}
					return false;
				}
			});
		}
		
		a.setSoftInputMode(a.softModeHold);
		
		Window window = d.getWindow();
		a.setStatusBarColor(window);
		d.show();
		
		View vToAnimate = window.getDecorView();
		vToAnimate.setAlpha(0);
		
		window.setDimAmount(0);
		WindowManager.LayoutParams layoutParams = window.getAttributes();
		layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		layoutParams.horizontalMargin = 0;
		layoutParams.verticalMargin = 0;
		window.setAttributes(layoutParams);
		
		searchView.dismiss.setOnClickListener(v -> searchView.searchHolder.hide());
		
		window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
		window.getDecorView().setPadding(0,0,0,0);
		d.setContentView(searchView.getRoot());
		
		vToAnimate.animate()
				//.setDuration(1000)
				.alpha(1);
		
		searchView.getRoot().post(new Runnable() {
			@Override
			public void run() {
				searchView.searchHolder.refreshSearch();
				searchView.searchHolder.show(a.root, showKeyboard);
			}
		});
	}
	
	class ItemInvalidator implements Runnable {
		final int position;
		ItemInvalidator(int position) {
			this.position = position;
		}
		@Override
		public void run() {
			notifyItemRangeChanged(position, position+1);
		}
	}
	
	private void calculateItemWidth(boolean postMeasure) {
		DisplayMetrics dm = a.dm;
		if(dm.widthPixels<= GlobalOptions.realWidth) { // portrait
			mItemWidth = (int) (GlobalOptions.realWidth * 0.65);
			int idealItemHeight = (int) (((float) mItemWidth) / dm.widthPixels * dm.heightPixels);
			mItemHeight = Math.min(idealItemHeight, dm.heightPixels);
			CMN.Log("idealItemHeight", idealItemHeight, mItemHeight, idealItemHeight-mItemHeight);
		} else { // landscape
			mItemWidth = dm.widthPixels/4;
			mItemHeight = dm.heightPixels/2;
		}

		recyclerView.mItemWidth = mItemWidth;

		if(postMeasure) {
			a.root.postDelayed(() -> {
				padViewpager();
				recyclerView.smoothScrollToPosition(layoutManager.targetPos);
			}, 350);
		} else {
			padViewpager();
		}

		notifyDataSetChanged();
	}
	
	void padViewpager() {
		int pad = (a.root.getHeight() - mItemHeight - a.UIData.bottombar2.getHeight())/2;
		recyclerView.setPadding(0, pad, 0, pad);
		padWidth = (a.root.getWidth()-mItemWidth)/2-3*itemPad;
		if(padWidth<0) padWidth=0;
	}
	
	private boolean targetIsPage(int target) {
		return target>=0 && target<a.TabHolders.size();
	}
	
	/** 列表化/窗口化标签管理 */
	public void toggleTabView(int fromClick, View v, int forceAnimation) {
		CMN.Log("toggleTabView???", a.currentViewImpl==null?"noImpl":"hasImpl", a.adapter_idx, fromClick);
		boolean b1=isVisible();
		if(a.currentViewImpl==null) {
			a.checkCurrentTab(false);
			notifyDataSetChanged();
		}
		//CMN.Log("toggleTabView", currentViewImpl==null?"noImpl":"hasImpl", adapter_idx);
		if(a.webtitle.getVisibility()!=View.VISIBLE) {
			a.webtitle_setVisibility(false);
		}
		if(DismissingViewHolder) {
			a.webtitle.setText("标签页管理");
			a.setSoftInputMode(a.softModeResize);
			a.currentViewImpl.hideForTabView = true;
		} else {
			a.webtitle.setText(a.currentViewImpl.getTitle());
			if(fromClick==-1) {
				// todo findViewByPosition
				int fvp = layoutManager.findFirstVisibleItemPosition();
				int target = layoutManager.targetPos;
				if(target<=1) {
					target=1;
				}
				View ca = recyclerView.getChildAt(target-fvp);
				findBMTmpInViewHolder(ca==null?null:(ViewUtils.ViewDataHolder<WebPageItemBinding>) ca.getTag());
			}
		}
		if(layoutManager.targetPos>a.TabHolders.size()) {
			layoutManager.targetPos=a.TabHolders.size();
		}
		boolean AnimateTabsManager = opt.getAnimateTabsManager();
		if ((forceAnimation&0x1)!=0) {
			AnimateTabsManager = false;
		}
		if(AnimateTabsManager)
			if(tabsAnimator==null) {
				scaleX = PropertyValuesHolder.ofFloat("scaleX", 0, 1);
				scaleY = PropertyValuesHolder.ofFloat("scaleY", 0, 1);
				translationX = PropertyValuesHolder.ofFloat("translationX", 0, 1);
				translationY = PropertyValuesHolder.ofFloat("translationY", 0, 1);
				alpha2 = PropertyValuesHolder.ofFloat("alpha", 0, 1);
				alpha = new ObjectAnimator();
				alpha.setPropertyName("alpha");
				alpha.setTarget(DummyTransX);
				//alpha.setTarget(viewpager_holder);
				alpha1 = ObjectAnimator.ofInt(a.bottombar2Background().mutate(), "alpha", 1, 0);
				alpha3 = ObjectAnimator.ofFloat(a.UIData.appbar, "alpha", 0, 1);
				animatorObj = ObjectAnimator.ofPropertyValuesHolder(a.currentViewImpl, scaleX, scaleY, translationX, translationY/*, alpha*//*, alpha2*//*, alpha3*/);
				//webAnimators = new ObjectAnimator[]{scaleX, scaleY, translationX, translationY, alpha1, alpha2};
				//if(false)
				animatorObj.addListener(animatorLis);
				//animatorSet.playTogether(scaleX, scaleY, translationX, translationY/*, alpha*/, alpha2/*, alpha3*/);
				animatorSet = new AnimatorSet();
				
				//animatorSet.playTogether(ValueAnimator.);
				
				//tabsAnimator = animatorObj;
				tabsAnimator = animatorObj;
				
				tabsAnimator.setDuration(350);
				tabsAnimator.setTarget(a.currentViewImpl);
			} else {
				tabsAnimator.setDuration(235); // 220
			}
		if(AnimateTabsManager) {
			toggleInternalTabViewAnima(fromClick, v, (forceAnimation&0x2)!=0);
		} else {
			toggleInternalTabView(fromClick);
		}
		ImageView ivRefresh = a.UIData.ivRefresh;
		if(!TestHelper.showSearchTabs) {
			int pad;
			if(b1) {
				ivRefresh.setImageResource(a.currentWebView.getProgress()==100?R.drawable.ic_refresh_white_24dp :R.drawable.ic_close_white_24dp);
				pad = (int) (GlobalOptions.density*10);
			} else {
				ivRefresh.setImageResource(R.drawable.ic_viewpager_carousel);
				pad = (int) (GlobalOptions.density*8.8);
			}
			ivRefresh.setPadding(pad, pad, pad, pad);
		} else {
			ivRefresh.setVisibility(b1?View.VISIBLE:View.GONE);
		}
	}
	
	public void dismiss() {
		if(isVisible()) {
			toggleTabView(-1, a.UIData.browserWidget10, 0);
		}
	}
	
	public void hideTabView() {
		if (viewpager_holder!=null && viewpager_holder.getVisibility()==View.VISIBLE) {
			int targetPos = layoutManager.targetPos-1;
			if(targetPos<0) targetPos=0;
			if(targetPos!=a.adapter_idx) {
				a.onLeaveCurrentTab(3);
				a.adapter_idx = targetPos;
				a.currentViewImpl = null;
			}
			DismissingViewHolder = true;
			viewpager_holder.setVisibility(View.GONE); // 放
		}
	}
	
	private void toggleInternalTabView(int fromClick) {
		DismissingViewHolder = viewpager_holder.getVisibility()==View.VISIBLE;
		if(DismissingViewHolder) {
			int targetPos = layoutManager.targetPos-1;
			if(fromClick>=0) {
				targetPos = fromClick;
			}
			if(targetPos<0) targetPos=0;
			if(targetPos!=a.adapter_idx||a.currentViewImpl==null||a.currentViewImpl.getParent()==null) {
				boolean added = a.AttachWebAt(targetPos, 0);
				a.currentViewImpl.setBMRef(tmpBmRef);
				coverupTheTab(a.currentViewImpl, added);
				uncoverTheTab(225);
			} else {
				a.currentViewImpl.setVisibility(View.VISIBLE);
			}
			a.currentViewImpl.setTranslationX(0);
			a.currentViewImpl.setTranslationY(0);
			viewpager_holder.setVisibility(View.GONE); // 放
			a.currentViewImpl.hideForTabView = false;
		} else {
			scrollHereRunnnable.run();
			viewpager_holder.setVisibility(View.VISIBLE); // 收
			a.currentViewImpl.setVisibility(View.INVISIBLE);
			if(recyclerView.getTag(R.id.home)==null) {
				//recyclerView.scrollToPosition(adapter_idx + 1);
				recyclerView.setTag(R.id.home, false);
			}
			recyclerView.post(scrollHereRunnnable);
			recyclerView.postDelayed(scrollHereRunnnable, 350);
			if(a.tabsManagerIsDirty) {
				notifyDataSetChanged();
				a.tabsManagerIsDirty =false;
			}
			else {
				boolean b1=Options.getAlwaysRefreshThumbnail();
				if(b1) {
					//currentWebView.time = System.currentTimeMillis();
				}
				//adaptermy.notifyDataSetChanged();
				//recyclerView.setItemAnimator(null);
				//
				//((SimpleItemAnimator)recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
				notifyItemChanged(a.adapter_idx+1, false);
				//adaptermy.onBindViewHolder((RecyclerView.ViewHolder) recyclerView.getChildAt(0).getTag(), layoutManager.findFirstVisibleItemPosition());
			}
		}
	}
	
	/** cheap animated version */
	Animator tabsAnimator;
	AnimatorSet animatorSet;
	ValueAnimator animatorObj;
	ViewPropertyAnimator animatorProp;
	PropertyValuesHolder scaleX;
	PropertyValuesHolder scaleY;
	PropertyValuesHolder translationX;
	PropertyValuesHolder translationY;
	ObjectAnimator alpha;
	ObjectAnimator alpha1;
	PropertyValuesHolder alpha2;
	ObjectAnimator alpha3;
	boolean DismissingViewHolder=true;
	Runnable startAnimationRunnable = () -> {
		if(a.anioutTopBotForTab) {
			a.UIData.appbar.setExpanded(DismissingViewHolder, true);
		}
		tabsAnimator.start();
	};
	private void startAnimation(boolean post) {
		//CMN.Log("动画咯", post);
		if(post) {
			a.root.removeCallbacks(startAnimationRunnable);
			a.root.postDelayed(startAnimationRunnable, 160);
		} else {
			startAnimationRunnable.run();
		}
	}
	private Animator.AnimatorListener animatorLis = new AnimatorListenerAdapter() {
		@Override
		public void onAnimationStart(Animator animation) {
			if(opt.getHideWebViewWhenShowingWebCoverDuringTransition()) {
				a.currentViewImpl.implView.setVisibility(View.INVISIBLE);
			}
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			//alpha1.setFloatValues(1f, 0f);
			//alpha1.setDuration(180);
			//alpha1.start();
			//recyclerView.suppressLayout(false);
			WebFrameLayout layout = a.currentViewImpl; //todo
			if (layout !=null) {
				if(DismissingViewHolder) {
					layout.hideForTabView = false;
					viewpager_holder.setVisibility(View.GONE);
					a.UIData.appbar.setAlpha(1);
					//imageViewCover.setVisibility(View.GONE);
					layout.setVisibility(View.VISIBLE);
					uncoverTheTab(180);
				} else {
					layout.setVisibility(View.INVISIBLE);
					//etSearch.setText("标签模式");
				}
				layout.implView.setVisibility(View.VISIBLE);
				layout.onViewAttached(1);
			}
			//v.setBackgroundResource(R.drawable.surrtrip1);
		}
	};
	private void toggleInternalTabViewAnima(int fromClick, View v, boolean bNeedPost) {
		WebFrameLayout layout = a.currentViewImpl;
		if(opt.getAnimateImageviewAlone()) {
			if(a.webFrameLayout==null) {
				a.webFrameLayout = new WebFrameLayout(a, new BrowserActivity.TabHolder());
				a.webFrameLayout.setPivotX(0);
				a.webFrameLayout.setPivotY(0);
			}
			//if(true) return;
			if(Utils.removeIfParentBeOrNotBe(a.webFrameLayout, a.UIData.webcoord, false)) {
				a.webFrameLayout.setLayoutParams(layout.getLayoutParams());
				a.webFrameLayout.setScaleX(layout.getScaleX());
				a.webFrameLayout.setScaleY(layout.getScaleY());
				a.webFrameLayout.setTranslationX(layout.getTranslationX());
				a.webFrameLayout.setTranslationY(layout.getTranslationY());
				a.UIData.webcoord.addView(a.webFrameLayout
						, a.UIData.webcoord.indexOfChild(viewpager_holder)+1);
			}
			a.webFrameLayout.setPadding(0, 0, 0, layout.getPaddingBottom());
			a.webFrameLayout.setVisibility(View.VISIBLE);
			layout.setScaleX(1);
			layout.setScaleY(1);
			layout.setTranslationX(0);
			layout.setTranslationY(0);
			layout = a.webFrameLayout;
			layout.getLayoutParams().height=-1;
			layout.getLayoutParams().width=-1;
		}
		tabsAnimator.pause();
		a.mBrowserSlider.pause();
		float ttY=1, ttX=1, targetScale=mItemWidth;
		boolean bc=fromClick>=0;
		View appbar=a.UIData.appbar;
		if(true|| DismissingViewHolder ||bc) {
			int W = a.root.getWidth();
			if(W==0) W=dm.widthPixels;
			
			a.mStatusBarH = Utils.getStatusBarHeight(a.mResource);
			
			int H = a.root.getHeight()-a.mStatusBarH;
			if(H<=0) H=dm.heightPixels;
			
			targetScale /= W;
			
			ttX=(W-W*targetScale)/2;
			
			ttY=(appbar.getHeight()+a.UIData.bottombar2.getHeight())/2-appbar.getTop();
			
			//ttY=((H/*-bottombar2.getHeight()*/)-(H-appbar.getHeight())*targetScale)/2-appbar.getHeight()-appbar.getTop();
			
			float titleH = a.mResource.getDimension(R.dimen._35_);
			
			//ttY=(currentWebView.layout.getHeight()*(1-targetScale))/2+(+titleH-viewpager_holder.getPaddingBottom())/2-appbar.getHeight()-appbar.getTop();
			
			//ttY=-((currentWebView.getHeight()*targetScale)/2-appbar.getHeight()/2)+(root.getHeight()+titleH-viewpager_holder.getPaddingBottom())/2;
			int tabH = a.currentViewImpl.getImplHeight();
			if(tabH==0) {
				tabH = a.UIData.webcoord.getHeight()-a.currentViewImpl.getPaddingBottom();
			}
			
			ttY=(H+titleH-viewpager_holder.getPaddingBottom()-tabH*targetScale)/2-10;
			
			ttY=(H+titleH-viewpager_holder.getPaddingBottom()-(tabH-a.currentViewImpl.getPaddingTop())*targetScale)/2-10;
			
			if(!a.anioutTopBotForTab)
			{
				ttY+=-appbar.getHeight()-appbar.getTop();
			}
			
			//CMN.Log("AAHH", currentViewImpl.getImplHeight(), UIData.webcoord.getHeight()-currentViewImpl.getPaddingBottom());
			//CMN.Log(H, mStatusBarH, UIData.bottombar2.getHeight(), "appbar.h="+appbar.getHeight(), "appbar.top="+appbar.getTop(), "titleH="+titleH, "bottom.h="+appbar.getTop(), "idealItemHeight2="+currentWebView.getHeight()*targetScale);
			
			
			//ttY=(currentWebView.getHeight()*(1-targetScale))/2;
			
			//CMN.Log("ttX", currentWebView.getTranslationY(), currentWebView.getTop(), appbar.getTop(), appbar.getHeight());
		}
		
		boolean added = false;
		
		if(animatorProp!=null) {
			animatorProp.cancel();
			animatorProp=null;
		}
		
		if(!DismissingViewHolder) {
			int targetPos = layoutManager.targetPos-1;
			targetPos = ViewUtils.getCenterXChildPositionV2(recyclerView) - 1;
			CMN.Log("预放计算::", " target="+targetPos, " from="+fromClick, "bcc="+(fromClick-targetPos));
			if(targetPos<0) targetPos=0;
			int bcc=0;
			if(bc) {
				bcc = fromClick-targetPos;
				targetPos = fromClick;
			}
			if(bc) {
				if(v!=null) {
					int delta = recyclerView.mLastTouchX - a.root.getWidth()/2;
					//CMN.Log("delta", delta, mItemWidth/2+itemPad*2);
					int theta = Math.abs(delta)-(mItemWidth/2+itemPad*2);
					if (theta>0) {
						ttX += (theta/(mItemWidth+itemPad*2)+1) * (mItemWidth+itemPad*2) * Math.signum(delta);
					}
				} else {
					ttX += (mItemWidth+2*itemPad)*bcc;
				}
			}
			CMN.Log("预放计算::", " targetPos="+targetPos, " ada="+a.adapter_idx, (a.TabHolders.get(targetPos)!=a.currentWebHolder), " bcc="+bcc);
			if((a.TabHolders.get(targetPos)!=a.currentWebHolder)||a.currentViewImpl==null||a.currentViewImpl.getParent()==null)
			{
				//added = TabHolders.get(targetPos)!=null;
				added = a.AttachWebAt(targetPos, 0);
				layout = a.currentViewImpl;
				if(tmpBmRefId==layout.holder.id) layout.setBMRef(tmpBmRef);
				layout.setScaleX(targetScale);
				layout.setScaleY(targetScale);
				layout.setTranslationY(ttY);
				layout.setTranslationX(ttX);
			} else if(bcc!=0) { //todo
				layout.setTranslationX(ttX);
			}
		}
		//currentWebView.setVisibility(View.INVISIBLE);
		//animatorObj.setDuration(1000);
		if(opt.getUseStdViewAnimator()) {
			animatorObj.setTarget(DummyTransX);
		} else {
			animatorObj.setTarget(layout);
		}
		alpha2.setFloatValues(1, 1);
		int bottombar2_alpha = a.bottombar2Background().getAlpha();
		float appbar_alpha = appbar.getAlpha();
		a.root.removeCallbacks(removeIMCoverRunnable);
		//recyclerView.suppressLayout(true);
		if(!DismissingViewHolder){// 放
			alpha1.setIntValues(bottombar2_alpha, 255);
			alpha3.setFloatValues(appbar_alpha, 1);
			if(added/* || bc*/) {
				//bNeedPost = true;
				//currentWebView.layout.setAlpha(0);
				//alpha2.setFloatValues(0, 1);
			}
			
			bNeedPost |= coverupTheTab(layout, added||opt.getAlwaysShowWebCoverDuringTransition());
			
			DismissingViewHolder =true;
			if(!opt.getAnimateImageviewAlone())
				layout.setVisibility(View.VISIBLE);
			//layout.setAlpha(1);
			
			if(opt.getUseStdViewAnimator()) {
				animatorProp = layout.animate().setListener(animatorLis)
						.scaleX(1).scaleY(1)
						.translationX(0).translationY(0)
						//.setDuration(220)
						//.setInterpolator(linearInterpolator)
				;
			} else {
				scaleX.setFloatValues(bc?targetScale:layout.getScaleX(), 1f);
				scaleY.setFloatValues(bc?targetScale:layout.getScaleY(), 1f);
				translationX.setFloatValues(bc?ttX:layout.getTranslationX(), 0);
				translationY.setFloatValues(bc?ttY:layout.getTranslationY(), 0);
				//animatorObj.setInterpolator(new AccelerateDecelerateInterpolator());
			}
			
			
			//alpha.setFloatValues(viewpager_holder.getAlpha(), 0);
			//animatorObj.setInterpolator(new AccelerateInterpolator(.5f));
			
			//alpha2.setInterpolator(null);
			alpha3.setInterpolator(null);
			//alpha2.setDuration(10);
		}
		else {  // 收
			alpha1.setIntValues(bottombar2_alpha, 128);
			alpha3.setFloatValues(appbar_alpha, 0);
			DismissingViewHolder =false;
			boolean b1=Options.getAlwaysRefreshThumbnail();
			if(b1){
//				currentWebView.version++;
//				currentWebView.time = System.currentTimeMillis();
			}
			if(opt.getAlwaysShowWebCoverDuringTransition()) {
				coverupTheTab(layout, true);
			}
			
			notifyItemChanged(a.adapter_idx+1, false);
			float startAlpha = 0;
			if(layout.getScaleX()==1) {
				//viewpager_holder.setAlpha(0f);
			} else {
				startAlpha = viewpager_holder.getAlpha();
			}
			viewpager_holder.setVisibility(View.VISIBLE);
			recyclerView.post(scrollHereRunnnable);
			//recyclerView.postDelayed(scrollHereRunnnable, 350);
			
			if(opt.getUseStdViewAnimator()) {
				animatorProp = layout.animate().setListener(animatorLis)
						.scaleX(targetScale).scaleY(targetScale)
						.translationX(ttX).translationY(ttY)
						//.setDuration(220)
						//.setInterpolator(linearInterpolator)
				;
			} else {
				scaleX.setFloatValues(layout.getScaleX(), targetScale);
				scaleY.setFloatValues(layout.getScaleY(), targetScale);
				translationX.setFloatValues(layout.getTranslationX(), ttX);
				translationY.setFloatValues(layout.getTranslationY(), ttY);
				animatorObj.setInterpolator(linearInterpolator);
				//animatorObj.setInterpolator(new AccelerateDecelerateInterpolator());
			}
			alpha.setFloatValues(startAlpha, 1f);
		}
		if(animatorProp!=null) {
			CMN.Log("animatorProp", animatorProp.getDuration(), animatorProp.getInterpolator());
			//animatorProp.setStartDelay(180);
			//animatorProp.start();
		}
		
		startAnimation(bNeedPost||opt.getAlwaysPostAnima());
		//startAnimation(opt.getShowWebCoverDuringTransition());
		//startAnimation(false);
	}
	
	public void uncoverTheTab(long delay) {
		//if(true) return;
		if(opt.getShowWebCoverDuringTransition()) {
			//CMN.Log("uncovering...");
			ImageView cover = this.imageViewCover;
			WebFrameLayout layout = (WebFrameLayout) cover.getParent();
			if(layout==null) {
				layout = a.currentViewImpl;
			}
			boolean removeCoverNow = /*!opt.getDelayHideWebCover() || */layout.holder.version>1;
			if (Utils.metaKill) layout.suppressLayout(true);
			//layout.addView(cover, 0);
			if (removeCoverNow) {
				Utils.removeIfParentBeOrNotBe(cover, null, false);
			} else {
				cover.setAlpha(0.95f);
				a.root.postDelayed(removeIMCoverRunnable, delay);
			}
			layout.recover=false;
			if (Utils.metaKill) layout.suppressLayout(false);
		}
	}
	
	public boolean coverupTheTab(WebFrameLayout layout, boolean added) {
		if(opt.getShowWebCoverDuringTransition() && added) {
			//CMN.Log("covering...");
			if (Utils.metaKill) layout.suppressLayout(true);
			ImageView cover = this.imageViewCover;
			cover.setAlpha(1.0f);
			cover.setVisibility(View.VISIBLE);
			boolean ret=true;
			if(Utils.removeIfParentBeOrNotBe(cover, layout, false)) {
				cover.setImageBitmap(layout.getBitmap());
				layout.addView(cover);
			} else {
				cover.setImageBitmap(layout.getBitmap());
				ret=false;
			}
			layout.recover = true;
			if (Utils.metaKill) layout.suppressLayout(false);
			return ret;
		}
		return false;
	}
}
