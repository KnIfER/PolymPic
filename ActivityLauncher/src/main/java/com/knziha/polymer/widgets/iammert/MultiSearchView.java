package com.knziha.polymer.widgets.iammert;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.knziha.polymer.R;
import com.knziha.polymer.databinding.ViewMultiSearchBinding;


public class MultiSearchView extends RelativeLayout {
	private ViewMultiSearchBinding binding;
	
	interface MultiSearchViewListener {
		void onTextChanged(Integer index, CharSequence s);
		void onSearchComplete(Integer index, CharSequence s);
		void onSearchItemRemoved(Integer index);
		void onItemSelected(Integer index, CharSequence s);
	}
	
	public MultiSearchView(Context context) {
		this(context, null);
	}
	
	public MultiSearchView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public MultiSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		binding = ViewMultiSearchBinding.inflate(LayoutInflater.from(context), this, true);
		
		TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MultiSearchView, defStyleAttr, defStyleAttr);
		int searchTextStyle = typedArray.getResourceId(R.styleable.MultiSearchView_searchTextStyle, 0);
		
		binding.searchViewContainer.searchTextStyle = searchTextStyle;
		
		binding.imageViewSearch.setOnClickListener(v -> {
			if (!binding.searchViewContainer.isInSearchMode()) {
				binding.searchViewContainer.search();
			} else {
				binding.searchViewContainer.completeSearch();
			}
		});
		
		//Utils.replaceView(binding.getRoot(), this);
	}
}
