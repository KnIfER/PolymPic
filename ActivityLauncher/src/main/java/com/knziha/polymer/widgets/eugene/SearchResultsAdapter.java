package com.knziha.polymer.widgets.eugene;

public interface SearchResultsAdapter {
	String getText(Object item);
	boolean onQueryTextSubmit(CharSequence query);
	boolean onQueryTextChange(CharSequence newText);
	void onItemClick(Object obj, boolean locateTo);
}