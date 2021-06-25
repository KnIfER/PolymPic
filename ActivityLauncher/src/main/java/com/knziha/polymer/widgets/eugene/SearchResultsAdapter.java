package com.knziha.polymer.widgets.eugene;

public interface SearchResultsAdapter {
	String getText(Object item);
	boolean onQueryTextSubmit(CharSequence query);
	boolean onQueryTextChange(CharSequence newText);
	boolean onItemClick(Object obj, boolean locateTo);
}