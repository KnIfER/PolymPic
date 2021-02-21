package com.knziha.polymer.widgets;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.qrcode.QRActivity;
import com.knziha.polymer.qrcode.QRActivityPlus;
import com.knziha.polymer.qrcode.QRGenerator;

import static com.knziha.polymer.widgets.Utils.RequsetUrlFromCamera;

public class EditFieldHandler implements View.OnClickListener, DialogInterface.OnKeyListener, View.OnTouchListener, View.OnLongClickListener {
	private Dialog d;
	public final Toastable_Activity a;
	public final ViewGroup mRoot;
	public final EditText etField;
	public final FrameLayout touchBG;
	
	private EditText proxyView;
	private boolean proxySmooth=true;
	
	TextView QRBtn;
	
	private boolean goToBarcodeScanner;
	private String lastTextBackup;
	
	public EditFieldHandler(Toastable_Activity context, ViewGroup root) {
		this.a = context;
		mRoot = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.edittext_field_main, root, false);
		
		Object[] fetcher = new Object[]{R.id.etField, R.id.browser_widget5};
		Utils.setOnClickListenersOneDepth(mRoot, this, 3, fetcher);
		etField = (EditText) fetcher[0];
		QRBtn = (TextView) fetcher[1];
		QRBtn.setOnLongClickListener(this);
		
		etField.getBackground().setAlpha(0);
		touchBG = new FrameLayout(etField.getContext());
		touchBG.setId(R.id.ivBack);
		touchBG.setBackgroundColor(0x88333333);
		touchBG.setOnTouchListener(this);
		
		etField.setOnEditorActionListener((v, actionId, event) -> {
			
			return false;
		});
		
		//tc
		etField.addTextChangedListener(new TextWatcherAdapter() {
			@Override
			public void afterTextChanged(Editable s) {
				CMN.Log("afterTextChanged");
				setGotoQR(s.length()==0);
			}
		});
	}
	
	public void dismiss(boolean setText) {
		Utils.removeIfParentBeOrNotBe(mRoot, null, false);
		Utils.removeIfParentBeOrNotBe(touchBG, null, false);
		if(d!=null) {
			d.setCancelable(true);
		}
		if(proxyView!=null&&(proxySmooth||setText)) {
			syncText(etField, proxyView);
		}
		d = null;
		proxyView = null;
		lastTextBackup = null;
	}
	
	public void setGotoQR(boolean val) {
		if(goToBarcodeScanner!=val) {
			goToBarcodeScanner=val;
			QRBtn.setText(goToBarcodeScanner?"扫码":"确认");
			QRBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
					a.mResource.getDrawable(goToBarcodeScanner?R.drawable.ic_baseline_qrcode:R.drawable.abc_ic_go_search_api_material)
					, null, null, null);
		}
	}
	
	private void syncText(EditText from, EditText to) {
		int st = from.getSelectionStart();
		int ed = from.getSelectionEnd();
		to.setText(from.getText());
		to.requestFocus();
		to.setSelection(st, ed);
	}
	
	public void showForEditText(@NonNull ViewGroup root, @NonNull EditText proxyView, Dialog d, int topMargin, int bottomMargin) {
		this.proxyView = proxyView;
		lastTextBackup = Utils.getTextInView(proxyView);
		syncText(proxyView, etField);
		setGotoQR(true);
		this.d = d;
		if(d!=null) {
			d.setOnKeyListener(this);
		}
		ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mRoot.getLayoutParams();
		lp.topMargin=topMargin;
		lp.bottomMargin=bottomMargin;
		Utils.addViewToParent(touchBG, root);
		Utils.addViewToParent(mRoot, root);
	}
	
	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK) {
			dismiss(false);
			return true;
		}
		return false;
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.browser_widget1:
				dismiss(false);
			break;
			case R.id.browser_widget2: {
				if(etField.getText().length()==0) {
					etField.setText(lastTextBackup);
				} else {
					String text = etField.getText().toString();
					if(etField.hasSelection()) {
						final int selectionStart = etField.getSelectionStart();
						final int selectionEnd = etField.getSelectionEnd();
						text = text.substring(selectionStart, selectionEnd);
					}
					ClipboardManager clipboard = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
					clipboard.setPrimaryClip(ClipData.newPlainText("PLOD", text));
				}
			} break;
			case R.id.browser_widget3:{
				ClipboardManager clipboard = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData primaryClip = clipboard.getPrimaryClip();
				if (primaryClip != null && primaryClip.getItemCount()>0) {
					etField.setText(primaryClip.getItemAt(0).getText());
				}
			} break;
			case R.id.browser_widget4:{
				lastTextBackup = Utils.getTextInView(etField);
				etField.setText(null);
			} break;
			case R.id.browser_widget5:{
				if(goToBarcodeScanner||etField.getText().length()==0) { //二维码
					Intent intent = new Intent(a, QRActivity.class);
					a.startActivityForResult(intent, RequsetUrlFromCamera);
				} else {
					dismiss(true);
				}
			} break;
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		String value = Utils.getTextInView(etField);
		switch (v.getId()) {
			case R.id.browser_widget5: {
				Intent intent = new Intent(a, QRGenerator.class).putExtra(Intent.EXTRA_TEXT, value);
				a.startActivity(intent);
				a.acquireWakeLock();
			} break;
			case R.id.browser_widget4:{
				dismiss(true);
			} break;
		}
		return false;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if(v.getId()==R.id.ivBack) {
			dismiss(false);
			return true;
		}
		return false;
	}
	
	public boolean visible() {
		return proxyView!=null;
	}
	
	public void setText(String text) {
		etField.setText(text);
	}
}
