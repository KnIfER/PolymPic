package com.knziha.polymer.pdviewer.bookmarks;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knziha.polymer.R;
import com.knziha.polymer.treeview.TreeViewAdapter;
import com.knziha.polymer.treeview.TreeViewNode;

/**
 * Created by tlh on 2016/10/1 :)
 */

public class BookMarkEntry implements TreeViewAdapter.LayoutItemType {
    public int page;
    public String entryName;

    public BookMarkEntry(String entryName) {
        this.entryName = entryName;
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_item;
    }
    
    static class BookMarkEntryBinder extends TreeViewAdapter.TreeViewBinderInterface<BookMarkEntryBinder.ViewHolder> {
		@Override
		public ViewHolder provideViewHolder(View itemView) {
			//holder.ivArrow.setImageResource(R.drawable.ic_keyboard_arrow_right_black_18dp);
			return new ViewHolder(itemView);
		}
	
		@Override
		public void bindView(ViewHolder holder, int position, TreeViewNode node) {
			holder.ivArrow.setRotation(node.isExpand() ? 90 : 0);
			BookMarkEntry entryNode = (BookMarkEntry) node.getContent();
			holder.tvName.setText(entryNode.entryName);
			holder.ivArrow.setVisibility(node.isLeaf()?View.INVISIBLE:View.VISIBLE);
		}
	
		@Override
		public int getLayoutId() {
			return R.layout.bookmark_item;
		}
	
		public static class ViewHolder extends TreeViewAdapter.TreeViewBinderInterface.ViewHolder {
			private final ImageView ivArrow;
			private final TextView tvName;
			private final TextView tvPage;
		
			public ViewHolder(View rootView) {
				super(rootView);
				this.ivArrow = rootView.findViewById(R.id.iv_arrow);
				this.tvName = rootView.findViewById(R.id.tv_name);
				this.tvPage = rootView.findViewById(R.id.tv_page);
				itemView.setTag(this);
			}
		
			public ImageView getIvArrow() {
				return ivArrow;
			}
		
			public TextView getTvName() {
				return tvName;
			}
		}
	}
}
