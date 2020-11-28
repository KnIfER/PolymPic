package com.shockwave.pdfium.bookmarks;


import com.shockwave.pdfium.R;
import com.shockwave.pdfium.treeview.TreeViewAdapter;

public class BookMarkEntry implements TreeViewAdapter.LayoutItemType {
    public int page;
    public String entryName;
    public BookMarkEntry(String entryName, int page) {
        this.entryName = entryName;
        this.page = page;
    }

    @Override
    public int getLayoutId() {
        return R.layout.bookmark_item;
    }
	
	@Override
	public String toString() {
		return "BookMarkEntry{" +
				"page=" + page +
				", entryName='" + entryName + '\'' +
				'}';
	}
}
