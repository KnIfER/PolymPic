package com.knziha.polymer.pdviewer;

import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.Utils.CMN;
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
						a.notifyItemAdded(this, arr, schRecord);
					}
				}
			}
			finished = true;
			a.post(this);
		}
	}
	
	public void start() {
		if(t==null) {
			PDocViewerActivity a = this.a.get();
			if(a!=null) {
				a.setSearchResults(arr);
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
