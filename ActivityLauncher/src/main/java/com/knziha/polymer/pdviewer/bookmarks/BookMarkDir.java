package com.knziha.polymer.pdviewer.bookmarks;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.knziha.polymer.R;

/**
 * Created by tlh on 2016/10/1 :)
 */

public class BookMarkDir implements TreeViewAdapter.LayoutItemType {
    public String dirName;

    public BookMarkDir(String dirName) {
        this.dirName = dirName;
    }

    @Override
    public int getLayoutId() {
        return R.layout.test_bookmark_item_dir;
    }
    
    static class DirectoryNodeBinder extends TreeViewAdapter.TreeViewBinderInterface<DirectoryNodeBinder.ViewHolder> {
		@Override
		public ViewHolder provideViewHolder(View itemView) {
			return new ViewHolder(itemView);
		}
	
		@Override
		public void bindView(ViewHolder holder, int position, TreeNode node) {
			holder.ivArrow.setRotation(0);
			holder.ivArrow.setImageResource(R.drawable.ic_keyboard_arrow_right_black_18dp);
			int rotateDegree = node.isExpand() ? 90 : 0;
			holder.ivArrow.setRotation(rotateDegree);
			BookMarkDir dirNode = (BookMarkDir) node.getContent();
			holder.tvName.setText(dirNode.dirName);
			if (node.isLeaf())
				holder.ivArrow.setVisibility(View.INVISIBLE);
			else holder.ivArrow.setVisibility(View.VISIBLE);
		}
	
		@Override
		public int getLayoutId() {
			return R.layout.test_bookmark_item_dir;
		}
	
		public static class ViewHolder extends TreeViewAdapter.TreeViewBinderInterface.ViewHolder {
			private ImageView ivArrow;
			private TextView tvName;
		
			public ViewHolder(View rootView) {
				super(rootView);
				this.ivArrow = rootView.findViewById(R.id.iv_arrow);
				this.tvName = rootView.findViewById(R.id.tv_name);
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
