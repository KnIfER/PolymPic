package com.knziha.polymer.pdviewer;

import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.Utils.CMN;
import com.shockwave.pdfium.PdfiumCore;
import com.shockwave.pdfium.SearchRecord;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class PDocSearchTask implements Runnable{
	private final ArrayList<SearchRecord> arr = new ArrayList<>();
	private final WeakReference<PDocViewerActivity> a;
	private final WeakReference<PDocument> pdoc;
	public final AtomicBoolean abort = new AtomicBoolean();
	private final String key;
	private Thread t;
	private final int flag=0;
	
	private boolean finished;
	
	public PDocSearchTask(PDocViewerActivity a, PDocument pdoc, String key) {
		this.a = new WeakReference<>(a);
		this.pdoc = new WeakReference<>(pdoc);
		this.key = key+"\0";
	}
	
	@Override
	public void run() {
		PDocViewerActivity a = this.a.get();
		if(a==null) {
			return;
		}
		if(finished) {
			//a.setSearchResults(arr);
			//a.showT("findAllTest_Time : "+(System.currentTimeMillis()-CMN.ststrt)+" sz="+arr.size());
			a.endSearch(arr);
		} else {
			CMN.rt();
			PDocument pdoc = this.pdoc.get();
			if(pdoc!=null) {
				SearchRecord schRecord;
				for (int i = 0; i < pdoc._num_entries; i++) {
					if(abort.get()) {
						break;
					}
					schRecord = pdoc.findPageCached(key, i, 0);
					if(schRecord!=null) {
						a.notifyItemAdded(this, arr, schRecord, i);
					} else {
						a.notifyProgress(i);
					}
				}
			}
			finished = true;
			t=null;
			a.post(this);
		}
	}
	
	public void start() {
		if(finished) {
			return;
		}
		if(t==null) {
			PDocViewerActivity a = this.a.get();
			if(a!=null) {
				a.startSearch(arr, key, flag);
			}
			t=new Thread(this);
			t.start();
		}
	}
	
	public void abort() {
		abort.set(true);
	}
	
	public boolean isAborted() {
		return abort.get();
	}
}
