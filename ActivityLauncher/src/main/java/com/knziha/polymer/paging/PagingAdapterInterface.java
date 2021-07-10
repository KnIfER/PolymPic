package com.knziha.polymer.paging;

public interface PagingAdapterInterface<T extends CursorReader> {
	int getCount();
	T getReaderAt(int position);
	void startPaging(long resume_to_sort_number, int i);
}
