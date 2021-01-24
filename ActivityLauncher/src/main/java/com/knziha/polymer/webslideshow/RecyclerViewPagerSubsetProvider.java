package com.knziha.polymer.webslideshow;

public abstract class RecyclerViewPagerSubsetProvider {
	public abstract int getResultCount();
	
	/** Get actual page index at adapter position without headerview.   */
	public abstract int getActualPageAtPosition(int position);
	
	/** Get adapter position without headerview for actual page */
	public abstract int queryPositionForActualPage(int page);
	
	public abstract int getLastQuery();
}
