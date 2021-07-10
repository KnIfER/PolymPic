package com.knziha.polymer.paging;

import android.database.Cursor;

import androidx.recyclerview.widget.RecyclerView;

public class CursorAdapter<T extends CursorReader> implements PagingAdapterInterface<T> {
	final Cursor cursor;
	final T reader;
	
	public CursorAdapter(Cursor cursor, T reader) {
		this.cursor = cursor;
		this.reader = reader;
	}
	
	@Override
	public int getCount() {
		return cursor.getCount();
	}
	
	@Override
	public T getReaderAt(int position) {
		cursor.moveToPosition(position);
		reader.ReadCursor(cursor, -1, 0);
		return reader;
	}
	
	@Override
	public void startPaging(long resume_to_sort_number, int i) {
	
	}
}