package com.knziha.polymer.pdviewer.bookmarks;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.widgets.Utils;
import com.shockwave.pdfium.bookmarks.BookMarkEntry;
import com.shockwave.pdfium.treeview.TreeViewAdapter;
import com.shockwave.pdfium.treeview.TreeViewNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookMarkFragment extends Fragment {
	public final static List<BookMarkEntryBinder> TreeViewBIInst = Collections.singletonList(new BookMarkEntryBinder());
	
	RecyclerView bmRv;
	final TreeViewAdapter adapter = new TreeViewAdapter(null, TreeViewBIInst);
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		CMN.Log("onCreateView1");
		if(bmRv==null) {
			Context context = inflater.getContext();
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
			
			adapter.setOnTreeNodeListener(new TreeViewAdapter.OnTreeNodeListener() {
				@Override
				public boolean onClick(TreeViewNode node, RecyclerView.ViewHolder holder) {
					if (!node.isLeaf()) {
						onToggle(!node.isExpand(), holder);
					}
					CMN.Log("onToggle", bmRv.isLayoutSuppressed(), CMN.id(node.getContent()), CMN.id(bmRv), CMN.id(bmRv.getAdapter()), node.getContent());
					return false;
				}
				
				@Override
				public void onToggle(boolean isExpand, RecyclerView.ViewHolder holder) {
					if(holder instanceof BookMarkEntryBinder.ViewHolder) {
						BookMarkEntryBinder.ViewHolder viewholder = (BookMarkEntryBinder.ViewHolder) holder;
						viewholder.ivArrow.animate().rotation(isExpand ? 90 : 0).start();
					}
				}
			});
			adapter.setHasStableIds(true);
			recyclerView.setAdapter(adapter);
			bmRv = recyclerView;
		} else {
			Utils.removeIfParentBeOrNotBe(bmRv, null, false);
		}
		return bmRv;
	}
	
	public void refresh(PDocument pdoc) {
		adapter.refresh(pdoc.bmRoot.getChildList(), pdoc.bmCount);
		adapter.notifyDataSetChanged();
		//bmRv.setAdapter(null);
		//bmRv.setAdapter(adapter);
		//bmRv.scrollToPosition(0);
	}
	
	public static class BookMarkEntryBinder extends TreeViewAdapter.TreeViewBinderInterface<BookMarkEntryBinder.ViewHolder> {
		@Override
		public ViewHolder provideViewHolder(View itemView) {
			return new ViewHolder(itemView);
		}
		
		@Override
		public void bindView(ViewHolder holder, int position, TreeViewNode node) {
			holder.ivArrow.setRotation(node.isExpand() ? 90 : 0);
			BookMarkEntry entryNode = (BookMarkEntry) node.getContent();
			holder.tvName.setText(entryNode.entryName);
			holder.tvPage.setText(entryNode.page<0?null:Integer.toString(entryNode.page));
			holder.ivArrow.setVisibility(node.isLeaf()?View.INVISIBLE:View.VISIBLE);
		}
		
		@Override
		public int getLayoutId() {
			return R.layout.bookmark_item;
		}
		
		public static class ViewHolder extends TreeViewAdapter.TreeViewBinderInterface.ViewHolder {
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
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CMN.Log("oac1", CMN.id(this));
	}
}
