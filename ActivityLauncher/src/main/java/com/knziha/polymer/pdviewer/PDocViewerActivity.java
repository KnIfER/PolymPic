package com.knziha.polymer.pdviewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.databinding.DataBindingUtil;

import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.databinding.ImageviewDebugBinding;
import com.knziha.polymer.text.BreakIteratorHelper;
import com.shockwave.pdfium.PdfiumCore;

import java.io.IOException;

public class PDocViewerActivity extends Toastable_Activity {
	ImageviewDebugBinding UIData;
	
	@Override
	public void onBackPressed() {
		if(UIData.wdv.draggingHandle==null && UIData.wdv.shouldDrawSelection()) {
			UIData.wdv.clearSelection();
		} else {
			super.onBackPressed();
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		UIData = DataBindingUtil.setContentView(this, R.layout.imageview_debug);
		
		try {
			UIData.wdv.dm=dm;
			//PDocument pdoc = new PDocument(this, "/sdcard/myFolder/sample_hetero_dimension.pdf");
			//PDocument pdoc = new PDocument(this, "/sdcard/myFolder/Gpu Pro 1.pdf", dm, null);
			//UIData.wdv.setDocument(pdoc);
			
			UIData.wdv.a=this;
			
			UIData.wdv.setContextMenuView(UIData.contextMenu);
			
			UIData.wdv.setSelectionPaintView(UIData.sv);
			
			MenuBuilder context_menu = new MenuBuilder(this);
			getMenuInflater().inflate(R.menu.context_menu, context_menu);
			SpannableStringBuilder text = new SpannableStringBuilder();
			for (int i = 0; i < context_menu.size(); i++) {
				int start = text.length();
				MenuItem item = context_menu.getItem(i);
				text.append(item.getTitle());
				text.setSpan(new ClickableSpan() {
					@Override
					public void updateDrawState(TextPaint ds) {
					
					}
					@Override
					public void onClick(@NonNull View widget) {
						OnMenuClicked(item);
					}},start,text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				text.append("  ");
			}
			UIData.contextMenu.setText(text, TextView.BufferType.SPANNABLE);
			
			
			UIData.contextMenu.setOnClickListener(CMN.XYTouchRecorder());
			
			UIData.contextMenu.setOnTouchListener(CMN.XYTouchRecorder());
			
			
			//UIData.wdv.setDocumentPath("/sdcard/myFolder/Gpu Pro 1.pdf");
			//UIData.wdv.setDocumentPath("/sdcard/myFolder/YotaSpec02.pdf"); // √
			//UIData.wdv.setDocumentPath("/sdcard/myFolder/1.pdf");
			//UIData.wdv.setDocumentPath("/sdcard/myFolder/sample.pdf");
			//UIData.wdv.setDocumentPath("/sdcard/myFolder/sig-notes.pdf");
			UIData.wdv.setDocumentPath("/sdcard/myFolder/sig-notes-new-txt.pdf");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		UIData.wdv.checkDoc();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		UIData.wdv.checkDoc();
	}
	
	public void OnMenuClicked(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.ctx_copy:{
				String text = UIData.wdv.getSelection();
				if(text!=null) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Activity.CLIPBOARD_SERVICE);
					clipboard.setPrimaryClip(ClipData.newPlainText("POLYM", text));
					showT("已复制！");
					UIData.wdv.clearSelection();
				}
			} break;
			case R.id.ctx_hightlight:{
				UIData.wdv.highlightSelection();
			} break;
			case R.id.ctx_enlarge:{
				UIData.wdv.enlargeSelection();
			} break;
		}
	}
	
}
