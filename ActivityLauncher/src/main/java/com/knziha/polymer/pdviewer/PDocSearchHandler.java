package com.knziha.polymer.pdviewer;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.widgets.Utils;

public class PDocSearchHandler implements View.OnClickListener {
	final PDocViewerActivity a;
	private final ViewGroup searchView;
	private final ViewGroup searchViewContent;
	private PDocSearchTask task;
	private EditText etSearch;
	
	public PDocSearchHandler(PDocViewerActivity a, ViewGroup vg) {
		this.a = a;
		this.searchView = vg;
		searchViewContent = (ViewGroup) vg.getChildAt(0);
		Object[] fetcher = new Object[]{R.id.etSearch};
		Utils.setOnClickListenersOneDepth(searchViewContent, this, 3, fetcher);
		
		etSearch = (EditText)fetcher[0];
		
		etSearch.setText("l-system");
		
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.browser_widget4:{
				etSearch.setText(null);
				boolean showKeyBoardOnClean=true;
				if(showKeyBoardOnClean) {
					etSearch.requestFocus();
					a.imm.showSoftInput(etSearch, 0);
				}
			} break;
			case R.id.browser_widget5:{
				//a.showT("go search!");
				if(task!=null) {
					task.abort();
				}
				task = new PDocSearchTask(a, a.currentViewer.pdoc, etSearch.getText().toString());
				task.start();
			} break;
		}
	}
	
	public void close() {
		if(task!=null) {
			task.abort();
		}
		task = null;
	}
}
