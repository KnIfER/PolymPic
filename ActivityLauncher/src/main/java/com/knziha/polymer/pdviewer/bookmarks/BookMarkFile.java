package com.knziha.polymer.pdviewer.bookmarks;

import android.view.View;
import android.widget.TextView;

import com.knziha.polymer.R;

/**
 * Created by tlh on 2016/10/1 :)
 */

public class BookMarkFile implements TreeViewAdapter.LayoutItemType {
    public String fileName;

    public BookMarkFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int getLayoutId() {
        return R.layout.test_bookmark_item_file;
    }
    
    static class FileNodeBinder extends TreeViewAdapter.TreeViewBinderInterface<FileNodeBinder.ViewHolder> {
		@Override
		public ViewHolder provideViewHolder(View itemView) {
			return new ViewHolder(itemView);
		}
	
		@Override
		public void bindView(ViewHolder holder, int position, TreeViewNode node) {
			BookMarkFile fileNode = (BookMarkFile) node.getContent();
			holder.tvName.setText(fileNode.fileName);
		}
	
		@Override
		public int getLayoutId() {
			return R.layout.test_bookmark_item_file;
		}
	
		public class ViewHolder extends TreeViewAdapter.TreeViewBinderInterface.ViewHolder {
			public TextView tvName;
			public TextView tvPage;
		
			public ViewHolder(View rootView) {
				super(rootView);
				this.tvName = rootView.findViewById(R.id.tv_name);
				this.tvPage = rootView.findViewById(R.id.tv_page);
				itemView.setTag(this);
			}
		}
	}
}
