package com.knziha.polymer.pdviewer;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WaveView;

public class PDocSearchHandler implements View.OnClickListener {
	final PDocViewerActivity a;
	private final ViewGroup searchView;
	private final ViewGroup searchViewContent;
	private PDocSearchTask task;
	private EditText etSearch;
	private TextView searchBtn;
	private Drawable drawableSearch;
	private Drawable drawableAbort;
	
	WaveView waveView;
	
	public PDocSearchHandler(PDocViewerActivity a, ViewGroup vg) {
		this.a = a;
		this.searchView = vg;
		searchViewContent = (ViewGroup) vg.getChildAt(0);
		Object[] fetcher = new Object[]{R.id.etSearch, R.id.browser_widget5};
		Utils.setOnClickListenersOneDepth(searchViewContent, this, 3, fetcher);
		
		etSearch = (EditText)fetcher[0];
		searchBtn = (TextView)fetcher[1];
		
		drawableSearch = searchBtn.getCompoundDrawables()[0];
		drawableAbort = a.getResources().getDrawable(R.drawable.ic_search_abort);
		drawableAbort.setBounds(drawableSearch.getBounds());
		
		etSearch.setText("l-system");
		
		waveView = a.adaptermy.viewpagerParent.findViewById(R.id.wave);
		
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.browser_widget1:{
			
			} break;
			case R.id.browser_widget2:{
				a.showT("Not Implemented！");
			} break;
			case R.id.browser_widget3:{
				ClipboardManager clipboard = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData primaryClip = clipboard.getPrimaryClip();
				if (primaryClip != null && primaryClip.getItemCount()>0) {
					etSearch.setText(primaryClip.getItemAt(0).getText());
				}
			} break;
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
	
	public void startSearch() {
		searchBtn.setCompoundDrawables(drawableAbort, null, null, null);
		searchBtn.setText("取消");
		waveView.setVisibility(View.VISIBLE);
		waveView.setProgress(0);
		waveView.setProgressVis(true);
		waveView.setMax(a.currentViewer.pdoc._num_entries);
	}
	
	public void endSearch() {
		searchBtn.setCompoundDrawables(drawableSearch, null, null, null);
		searchBtn.setText("搜索");
		waveView.setProgressVis(false);
	}
	
	public void setProgress(int progress) {
		waveView.setProgress(progress);
	}
}
