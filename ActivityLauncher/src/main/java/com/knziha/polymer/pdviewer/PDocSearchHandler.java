package com.knziha.polymer.pdviewer;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WaveView;
import com.shockwave.pdfium.SearchRecord;

import java.util.ArrayList;

import static com.knziha.polymer.widgets.Utils.getStatusBarHeight;

public class PDocSearchHandler implements View.OnClickListener {
	final PDocViewerActivity a;
	private final ViewGroup searchView;
	private final ViewGroup searchViewContent;
	private final View padView;
	private PDocSearchTask task;
	private final EditText etSearch;
	private final TextView searchBtn;
	private final Drawable drawableSearch;
	private final Drawable drawableAbort;
	
	public boolean vis=false;
	
	WaveView waveView;
	private boolean shouldShow;
	
	public PDocSearchHandler(PDocViewerActivity a, ViewGroup vg) {
		this.a = a;
		this.searchView = vg;
		searchViewContent = (ViewGroup) vg.getChildAt(0);
		Object[] fetcher = new Object[]{R.id.etSearch, R.id.browser_widget5, R.id.padView};
		Utils.setOnClickListenersOneDepth(searchViewContent, this, 3, fetcher);
		
		etSearch = (EditText)fetcher[0];
		searchBtn = (TextView)fetcher[1];
		padView = (View)fetcher[2];
		
		drawableSearch = searchBtn.getCompoundDrawables()[0];
		drawableAbort = a.getResources().getDrawable(R.drawable.ic_search_abort);
		drawableAbort.setBounds(drawableSearch.getBounds());
		
		//etSearch.setText("l-system");
		//etSearch.setText("buffer");
		//etSearch.setText("Outdoor");
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_DONE ||actionId==EditorInfo.IME_ACTION_UNSPECIFIED) {
				searchBtn.performClick();
				return true;
			}
			return false;
		});
		setPadHeight(getStatusBarHeight(a.getResources()));
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.browser_widget1:{
				shouldShow=false;
				setVisibility(false);
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
					triggerKeyBoard();
				}
			} break;
			case R.id.browser_widget5:{
				//a.showT("go search!");
				if(task!=null) {
					close();
				} else {
					String text = etSearch.getText().toString();
					if(TextUtils.isEmpty(text)) {
					
					} else {
						task = new PDocSearchTask(a, a.currentViewer.pdoc, text);
						task.start();
					}
					if(true) {
						toggleVisibility();
					}
				}
				hideKeyBoard();
			} break;
			case R.id.wave:{
				if(task!=null) {
					close();
					waveView.setProgressVis(false);
				} else {
					a.setSearchResults(null, null, 0);
					waveView.setVisibility(View.GONE);
				}
			} break;
		}
	}
	
	private void triggerKeyBoard() {
		etSearch.requestFocus();
		a.imm.showSoftInput(etSearch, 0);
	}
	
	private void hideKeyBoard() {
		etSearch.clearFocus();
		a.imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
		//a.imm.showSoftInput(etSearch, 0);
	}
	
	public void close() {
		if(task!=null) {
			task.abort();
		}
		task = null;
	}
	
	public void startSearch(ArrayList<SearchRecord> arr, String key, int flag) {
		if(!a.isPagesViewVis()) {
			a.togglePagesView();
		}
		a.setSearchResults(arr, key, flag);
		if(waveView==null) {
			waveView = a.adaptermy.viewpagerParent.findViewById(R.id.wave);
		}
		searchBtn.setCompoundDrawables(drawableAbort, null, null, null);
		searchBtn.setText("取消");
		waveView.setOnClickListener(this);
		waveView.setVisibility(View.VISIBLE);
		waveView.setProgress(0);
		waveView.setProgressVis(true);
		waveView.setMax(a.currentViewer.pdoc._num_entries);
	}
	
	public void endSearch(ArrayList<SearchRecord> arr) {
		searchBtn.setCompoundDrawables(drawableSearch, null, null, null);
		searchBtn.setText("搜索");
		waveView.setProgressVis(false);
		if(arr.size()==0) {
			a.setSearchResults(null, null, 0);
			waveView.setVisibility(View.GONE);
		}
		task = null;
	}
	
	public void setProgress(int progress) {
		if(Math.abs(waveView.getProgress()-progress)*1.0/waveView.getMax()>=0.025) {
			waveView.setProgress(progress);
		}
	}
	
	public void toggleVisibility() {
		setVisibility(shouldShow=!vis);
	}
	
	public void setVisibility(boolean show) {
		ViewPropertyAnimator anima = searchView.animate();
		float ratio = 0.5f;
		anima.setDuration(280);
		vis=show;
		if(show) {
			//searchView.setScaleX(ratio); searchView.setScaleY(ratio);
			searchView.setTranslationY(-searchView.getHeight());
			anima.scaleX(1).scaleY(1)
					.translationY(0)
					//.alpha(1)
					.start();
			triggerKeyBoard();
		} else {
			anima//.scaleX(ratio).scaleY(ratio)
					.translationY(-searchView.getHeight())
					//.alpha(0)
					.start();
			hideKeyBoard();
		}
	}
	
	public void postInit() {
		searchView.post(() -> {
			searchView.setVisibility(View.VISIBLE);
			searchView.setTranslationY(-searchView.getHeight());
			onMenuImmersiveChanged(a.isImmersiveModeEnabled);
			setVisibility(true);
		});
	}
	
	public void setPadHeight(int statusBarHeight) {
		padView.getLayoutParams().height=statusBarHeight;
	}
	
	public void onMenuImmersiveChanged(boolean immersive) {
		padView.setVisibility(View.VISIBLE);
		if(immersive) {
			searchView.setTranslationY(-padView.getHeight());
		} else {
			searchView.animate().translationY(0).start();
		}
	}
}
