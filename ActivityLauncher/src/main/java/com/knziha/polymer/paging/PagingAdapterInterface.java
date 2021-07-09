package com.knziha.polymer.paging;

import androidx.recyclerview.widget.RecyclerView;

public interface PagingAdapterInterface<T extends CursorReader> {
	int getCount();
	T getReaderAt(int position);
	void bindTo(RecyclerView recyclerView);
	void startPaging(long resume_to_sort_number, int i);
}
