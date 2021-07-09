package com.knziha.polymer.paging;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PagingRecyclerView extends RecyclerView {
	public PagingCursorAdapter dataAdapter;
	
	public PagingRecyclerView(@NonNull Context context) {
		this(context, null);
	}
	
	public PagingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public PagingRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
	@Override
	public void computeScroll() {
		super.computeScroll();
	}
}
