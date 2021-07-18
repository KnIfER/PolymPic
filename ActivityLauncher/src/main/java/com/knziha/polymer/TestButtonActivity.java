package com.knziha.polymer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.widgets.PopupMenuHelper;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.knziha.polymer.widgets.iammert.MultiSearchView;
import com.shockwave.pdfium.bookmarks.BookMarkEntry;
import com.shockwave.pdfium.treeview.TreeViewAdapter;
import com.shockwave.pdfium.treeview.TreeViewNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestButtonActivity extends Toastable_Activity implements View.OnClickListener {
	AlertDialog alert;
	private WebView webview;
	
	boolean bool;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.test_button);
		
		root = findViewById(R.id.root);
		webview = findViewById(R.id.webview);
		
		AdvancedBrowserWebView v = new AdvancedBrowserWebView(this);
		Utils.replaceView(v, webview);
		webview=v;
		
		v.layout = new WebFrameLayout(this, new BrowserActivity.TabHolder());
		v.layout.setImplementation(v);
		v.layout.updateUserAgentString();
		v.layout.setTextZoom();
		
		//v.layout.setBackEndSettings();
		
		//webview.getSettings().setBlockNetworkImage(false);
		//CMN.Log("getJavaScriptEnabled::"+webview.getSettings().getJavaScriptEnabled());
		
		//webview.getSettings().setJavaScriptEnabled(true);
		
		
		webview.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return false;
			}
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				view.getSettings().setJavaScriptEnabled(false);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				view.getSettings().setJavaScriptEnabled(true);
			}
		});
		
		webview.loadUrl("https://www.bing.com");
		webview.postDelayed(new Runnable() {
			@Override
			public void run() {
				webview.loadUrl("https://live.kuaishou.com/u/3xya8diheaydhdm/3x8bqm5zwivpaau?did=web_436066112a1c0580fa88818194107634");
			}
		}, 1000);
		
		
		
		alert = new androidx.appcompat.app.AlertDialog.Builder(this)
				.setSingleChoiceLayout(R.layout.singlechoice_plain)
				.setSingleChoiceItems(R.array.config_links, 0, null)
				.setTitle("HAPPY").create();
		
		CompoundButton button = findViewById(R.id.radio);
		
		if(false)
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				button.setChecked(!button.isChecked());
				showT(button.isChecked()?"checked":"not checked");
			}
		});
		
		//testTreeView();
		
		MultiSearchView multiSearch = findViewById(R.id.multiSearch);
		
		CMN.Log(multiSearch);
		
		
//		for (int i = 0; i < 20; i++) {
//			WebView webview = new WebView(this);
//			root.addView(webview);
//			webview.loadUrl("https://cn.bing.com/search?q=Test"+i);
//		}
		//testWebSeq.run();
		//testPopupView();
	}
	ArrayList<WebView> wvs = new ArrayList<>();
	String[] data = new String[]{
		"https://cn.bing.com/"
		,"https://qbi.uq.edu.au/brain/brain-anatomy/types-neurons"
		,"https://qbi.uq.edu.au/brain/brain-anatomy/what-neuron"
		,"https://cn.bing.com/search?q=glen+etive+highlands+scotland"
		,"http://mluxun.zuopinj.com/2241/90388.html"
		,"https://www.nasa.gov/feature/goddard/2020/hubble-finds-tha"
		,"https://www.luxun.org/post/70.html"
		,"https://www.xugongping.com/book/4/4250400.html"
	};
	WebViewClient mWebViewClient =  new WebViewClient(){
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
			return false;
		}
	};
	WebChromeClient mWebChromeClient =  new WebChromeClient(){
	
	};
	Runnable testWebSeq = new Runnable() {
		@Override
		public void run() {
			WebView webview = new WebView(TestButtonActivity.this);
			webview.setWebChromeClient(mWebChromeClient);
			webview.setWebViewClient(mWebViewClient);
			if(cc>0) {
				root.removeView(wvs.get(wvs.size()-1));
			}
			root.addView(webview);
			wvs.add(webview);
			//webview.loadUrl("https://cn.bing.com/search?q=Test"+cc++);
			webview.loadUrl(data[cc++]);
			if (cc<data.length) {
				root.postDelayed(this, 220);
			}
			showT(""+cc);
		}
	};
	int cc=0;
//////////////////////
	private PopupWindow mPopupWindow;
	private ListView mListView;
	
	private void showPopupWindow(View anchorView) {
		int[] texts = new int[] {
				 R.string.duoxuanmoshi
				,R.string.houtaidakai
				,R.string.xinbiaoqianyedaikai
				,R.string.sheweimorenye
				,R.string.fuzhilianjie
				,R.layout.menu_edit_and_delete
				,R.string.share
		};
		PopupMenuHelper.PopupMenuListener listener = (popupMenuHelper, v, isLongClick) -> {
			boolean ret=true;
			boolean dismiss = !isLongClick;
			switch (v.getId()) {
				case R.string.duoxuanmoshi:
					showT("进入多选模式啦");
				break;
			}
			if (dismiss) {
				popupMenuHelper.postDismiss(80);
			}
			return ret;
		};
		new PopupMenuHelper(this, texts, listener).show(anchorView, x, y);
	}
	
	private void testPopupView() {
		mListView = findViewById(R.id.listview);
		mListView.setAdapter(new CustomAdapter());
		mListView.setVisibility(View.VISIBLE);
	}
	
	class CustomAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			return 20;
		}
		
		@Override
		public Object getItem(int position) {
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return 0;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder viewHolder;
			final View finalConvertView;
			if (convertView == null) {
				convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.bookmark_item, null);
				viewHolder = new ViewHolder();
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			finalConvertView = convertView;
			convertView.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					showPopupWindow(finalConvertView);
					return false;
				}
			});
			convertView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					x = (int) event.getRawX();
					y = (int) event.getRawY();
					return false;
				}
			});
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//点击事件
				}
			});
			return convertView;
		}
	}
	
	int x;
	int y;
	
	class ViewHolder {
		View moreRoot;
		View moreImgv;
	}
//////////////////////
	static class TVBinder extends TreeViewAdapter.TreeViewBinderInterface<TVBinder.ViewHolder> {
		@Override
		public ViewHolder provideViewHolder(View itemView) {
			return new ViewHolder(itemView);
		}
		
		@Override
		public void bindView(ViewHolder holder, int position, TreeViewNode node) {
			holder.ivArrow.setRotation(node.isExpand() ? 90 : 0);
			TVEntry entryNode = (TVEntry) node.getContent();
			holder.tvName.setText(entryNode.entryName);
			holder.tvPage.setText(entryNode.page<0?null:Integer.toString(entryNode.page));
			holder.ivArrow.setVisibility(node.isLeaf()?View.INVISIBLE:View.VISIBLE);
			holder.itemView.setBackgroundColor(Color.WHITE);
		}
		
		@Override
		public int getLayoutId() {
			return R.layout.bookmark_item;
		}
		
		public static class ViewHolder extends RecyclerView.ViewHolder {
			public final ImageView ivArrow;
			public final TextView tvName;
			
			public final TextView tvPage;
			
			public ViewHolder(View rootView) {
				super(rootView);
				this.ivArrow = rootView.findViewById(R.id.iv_arrow);
				this.tvName = rootView.findViewById(R.id.tv_name);
				this.tvPage = rootView.findViewById(R.id.tv_page);
				itemView.setTag(this);
			}
		}
	}
	public static class TVEntry implements TreeViewAdapter.LayoutItemType {
		public int page;
		public String entryName;
		public TVEntry(String entryName, int page) {
			this.entryName = entryName;
			this.page = page;
		}
		
		@Override
		public int getLayoutId() {
			return com.shockwave.pdfium.R.layout.bookmark_item;
		}
		
		@Override
		public String toString() {
			return "TVEntry{" +
					"page=" + page +
					", entryName='" + entryName + '\'' +
					'}';
		}
	}
	public static class TVNode extends TreeViewNode<TVEntry> {
		public TVNode(@NonNull TVEntry content) {
			super(content);
		}
		
		public TVNode add(String title, int pageIdx) {
			TVNode ret = new TVNode(new TVEntry(title, pageIdx));
			addChild(ret);
			return ret;
		}
		
		public TVNode addToParent(String title, int pageIdx) {
			if(getParent()==null) {
				return add(title, pageIdx);
			}
			TVNode ret = new TVNode(new TVEntry(title, pageIdx));
			getParent().addChild(ret);
			return ret;
		}
		
	}
	

	static class TreeViewTouchHelper extends ItemTouchHelper.Callback {
		private ItemTouchAdapter itemTouchAdapter;
		public TreeViewTouchHelper(ItemTouchAdapter itemTouchAdapter){
			this.itemTouchAdapter = itemTouchAdapter;
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
			final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
			//final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
			final int swipeFlags = 0;
			return makeMovementFlags(dragFlags, swipeFlags);
		}
		@Override
		public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
			int fromPosition = viewHolder.getAdapterPosition();//得到拖动ViewHolder的position
			int toPosition = target.getAdapterPosition();//得到目标ViewHolder的position
			itemTouchAdapter.onMove(fromPosition,toPosition);
			return true;
		}
		@Override
		public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
			int position = viewHolder.getAdapterPosition();
			itemTouchAdapter.onSwiped(position);
		}
		@Override
		public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
			if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
				//滑动时改变Item的透明度
				final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
				viewHolder.itemView.setAlpha(alpha);
				viewHolder.itemView.setTranslationX(dX);
			} else {
				super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
			}
		}
		@Override
		public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
			if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
				if (background == null && bkcolor == -1) {
					Drawable drawable = viewHolder.itemView.getBackground();
					if (drawable == null) {
						bkcolor = 0;
					} else {
						background = drawable;
					}
				}
				viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
			}
			super.onSelectedChanged(viewHolder, actionState);
		}
		@Override
		public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
			super.clearView(recyclerView, viewHolder);
			
			viewHolder.itemView.setAlpha(1.0f);
			if (background != null) viewHolder.itemView.setBackgroundDrawable(background);
			if (bkcolor != -1) viewHolder.itemView.setBackgroundColor(bkcolor);
			//viewHolder.itemView.setBackgroundColor(0);
			
			if (onDragListener!=null){
				onDragListener.onFinishDrag();
			}
		}
		private Drawable background = null;
		private int bkcolor = -1;
		private OnDragListener onDragListener;
		public TreeViewTouchHelper setOnDragListener(OnDragListener onDragListener) {
			this.onDragListener = onDragListener;
			return this;
		}
		public interface OnDragListener{
			void onFinishDrag();
		}
		public interface ItemTouchAdapter {
			void onMove(int fromPosition,int toPosition);
			void onSwiped(int position);
		}
	}
	
	
	private void testTreeView() {
		Utils.removeIfParentBeOrNotBe(findViewById(R.id.button), null, false);
		List<TVBinder> TreeViewBIInst = Collections.singletonList(new TVBinder());

		final TreeViewAdapter adapter = new TreeViewAdapter(null, TreeViewBIInst);
		Context context = this;
		RecyclerView recyclerView = new RecyclerView(context);
		recyclerView.setId(R.id.rv);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setItemAnimator(null);
		recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
		recyclerView.setHasFixedSize(true);
		
		recyclerView.addItemDecoration(new RecyclerView.ItemDecoration(){
			final ColorDrawable mDivider = new ColorDrawable(Color.GRAY);
			final int mDividerHeight = 1;
			@Override
			public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
				final int childCount = parent.getChildCount();
				final int width = parent.getWidth();
				for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
					final View view = parent.getChildAt(childViewIndex);
					if (shouldDrawDividerBelow(view, parent)) {
						int top = (int) view.getY() + view.getHeight();
						mDivider.setBounds(0, top, width, top + mDividerHeight);
						mDivider.draw(c);
					}
				}
			}
			@Override
			public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
				if (shouldDrawDividerBelow(view, parent)) {
					outRect.bottom = mDividerHeight;
				}
			}
			private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
				//return parent.getChildViewHolder(view).getLayoutPosition()<parent.getChildCount()-1;
				return parent.getChildViewHolder(view).getBindingAdapterPosition()<adapter.getItemCount()-1;
			}
		});
		
		adapter.setTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
			@Override
			public boolean onClick(TreeViewNode node, RecyclerView.ViewHolder holder) {
				if (!node.isLeaf()) {
					onToggle(!node.isExpand(), holder);
				} else {
					Object content = node.getContent();
					if(content instanceof BookMarkEntry) {
						//onJumpToBookMark((BookMarkEntry)content);
					}
				}
				//CMN.Log("onToggle", bmRv.isLayoutSuppressed(), CMN.id(node.getContent()), CMN.id(bmRv), CMN.id(bmRv.getAdapter()), node.getContent());
				
				return false;
			}
			
			@Override
			public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
				if(holder instanceof TVBinder.ViewHolder) {
					TVBinder.ViewHolder viewholder = (TVBinder.ViewHolder) holder;
					viewholder.ivArrow.animate().rotation(isExpand ? 90 : 0).start();
				}
			}
		});
		adapter.setHasStableIds(true);
		recyclerView.setAdapter(adapter);
		TVNode tvRoot = new TVNode(new TVEntry("root", 0));
		int cc=0;
		for (int i = 0; i < 10; i++) {
			tvRoot.add("Node#"+i, i);
			cc++;
		}
		//for (int i = 0; i < 10; i++) {
		//	((TVNode)tvRoot.getChildList().get(2)).add("Sub Node#"+i, i);
		//	cc++;
		//}
		adapter.refresh(tvRoot.getChildList(), cc);
		
		TreeViewTouchHelper.ItemTouchAdapter itemTouchAdapter = new TreeViewTouchHelper.ItemTouchAdapter() {
			@Override
			public void onMove(int fromPosition, int toPosition) {
				if (fromPosition==tvRoot.getChildList().size()-1 || toPosition==tvRoot.getChildList().size()-1){
					return;
				}
				if (fromPosition < toPosition) {
					for (int i = fromPosition; i < toPosition; i++) {
						Collections.swap(tvRoot.getChildList(), i, i + 1);
					}
				} else {
					for (int i = fromPosition; i > toPosition; i--) {
						Collections.swap(tvRoot.getChildList(), i, i - 1);
					}
				}
				adapter.notifyItemMoved(fromPosition, toPosition);
			}
			
			@Override
			public void onSwiped(int position) {
				tvRoot.getChildList().remove(position);
				adapter.notifyItemRemoved(position);
			}
		};
		
		final ItemTouchHelper itemTouchHelper
				= new ItemTouchHelper(new TreeViewTouchHelper(itemTouchAdapter));
		itemTouchHelper.attachToRecyclerView(recyclerView);
		
		
		recyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(recyclerView) {
			@Override
			public void onLongClick(RecyclerView.ViewHolder vh) {
				if (vh.getLayoutPosition()!=tvRoot.getChildList().size()-1) {
					itemTouchHelper.startDrag(vh);
				}
			}
		});
		
		root.addView(recyclerView);
	}
	public class OnRecyclerItemClickListener implements RecyclerView.OnItemTouchListener{
		private GestureDetectorCompat mGestureDetector;
		private RecyclerView recyclerView;
		
		public OnRecyclerItemClickListener(RecyclerView recyclerView){
			this.recyclerView = recyclerView;
			mGestureDetector = new GestureDetectorCompat(recyclerView.getContext()
					,new ItemTouchHelperGestureListener());
		}
		
		@Override
		public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
			mGestureDetector.onTouchEvent(e);
			return false;
		}
		
		@Override
		public void onTouchEvent(RecyclerView rv, MotionEvent e) {
			mGestureDetector.onTouchEvent(e);
		}
		
		@Override
		public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		
		}
		
		private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {
			
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
				if (child!=null) {
					RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(child);
					onItemClick(vh);
				}
				return true;
			}
			
			@Override
			public void onLongPress(MotionEvent e) {
				View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
				if (child!=null) {
					RecyclerView.ViewHolder vh = recyclerView.getChildViewHolder(child);
					onLongClick(vh);
				}
			}
		}
		
		public void onLongClick(RecyclerView.ViewHolder vh){}
		public void onItemClick(RecyclerView.ViewHolder vh){}
	}

//////////////////////
	
	@Override
	public void onClick(View v) {
		alert.show();
		//((ArrayAdapter)alert.getListView().getAdapter()).
		Utils.postInvalidateLayout(alert.getListView());
		
		//alert.getListView().post(new Runnable() {
		//	@Override
		//	public void run() {
		//		alert.getListView().requestLayout();
		//	}
		//});
	}
	
	
	
}
