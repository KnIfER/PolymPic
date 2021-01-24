package com.shockwave.pdfium.bookmarks;

import androidx.annotation.NonNull;

import com.shockwave.pdfium.treeview.TreeViewNode;

public class BookMarkNode extends TreeViewNode<BookMarkEntry> {
	public BookMarkNode(@NonNull BookMarkEntry content) {
		super(content);
	}
	
	public BookMarkNode add(String title, int pageIdx) {
		BookMarkNode ret = new BookMarkNode(new BookMarkEntry(title, pageIdx));
		addChild(ret);
		return ret;
	}
	
	public BookMarkNode addToParent(String title, int pageIdx) {
		if(getParent()==null) {
			return add(title, pageIdx);
		}
		BookMarkNode ret = new BookMarkNode(new BookMarkEntry(title, pageIdx));
		getParent().addChild(ret);
		return ret;
	}
	
}
