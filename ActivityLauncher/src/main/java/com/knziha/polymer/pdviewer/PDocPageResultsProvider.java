package com.knziha.polymer.pdviewer;

import android.os.Looper;

import com.knziha.polymer.webslideshow.RecyclerViewPagerSubsetProvider;
import com.shockwave.pdfium.SearchRecord;

import java.util.ArrayList;

/** Basic Search Results Recorder and Provider */
public class PDocPageResultsProvider extends RecyclerViewPagerSubsetProvider {
	final ArrayList<SearchRecord> results;
	private int lastQuery;
	
	/** @param results the sorted array of matched page index along with the first match index. */
	public PDocPageResultsProvider(ArrayList<SearchRecord> results) {
		this.results = results;
	}
	
	public int getResultCount() {
		return results.size();
	}
	
	/** Get actual page index at adapter position without headerview.   */
	public int getActualPageAtPosition(int position) {
		if(position<0||position>=results.size()) {
			return -1;
		}
		return results.get(position).pageIdx;
	}
	
	public int reduce(int page,int start,int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			return page - results.get(start + len - 1).pageIdx>0
					? reduce(page,start+len,end)
					: reduce(page,start,start+len);
		} else {
			return start;
		}
	}
	
	/** Get adapter position without headerview for actual page */
	public int queryPositionForActualPage(int page) {
		int size = results.size();
		if(size==0) {
			return -1;
		}
		int position = reduce(page, 0, size);
		int queryRet = results.get(position).pageIdx;
		boolean cacheQuery = Looper.myLooper()==Looper.getMainLooper();
		if(queryRet==page) {
			if(cacheQuery) {
				lastQuery = position;
			}
			return position;
		} else {
			if(cacheQuery) {
				lastQuery = -(position+1);
			}
			return -(position+1);
		}
	}
	
	public int getLastQuery() {
		return lastQuery;
	}
}
