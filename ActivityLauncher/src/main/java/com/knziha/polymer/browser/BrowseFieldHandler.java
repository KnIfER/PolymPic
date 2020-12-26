package com.knziha.polymer.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.qrcode.QRActivityPlus;
import com.knziha.polymer.widgets.Utils;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.knziha.polymer.widgets.Utils.RequsetUrlFromCamera;


public class BrowseFieldHandler implements View.OnClickListener, View.OnLongClickListener {
	final BrowseActivity a;
	private final ViewGroup root;
	
	EditText etField;
	TextView etField_indicator;
	
	long rowID;
	int index;
	
	TextView QRBtn;
	
	private boolean goToBarcodeScanner;
	private boolean longClickedQr;
	private String lastTextBackup;
	
	public BrowseFieldHandler(BrowseActivity a) {
		this.a = a;
		root = (ViewGroup) a.UIData.et.getViewStub().inflate();
		Object[] fetcher = new Object[]{R.id.etField, R.id.browser_widget5};
		Utils.setOnClickListenersOneDepth(root, this, 999, fetcher);
		etField = (EditText) fetcher[0];
		QRBtn = (TextView) fetcher[1];
		
		QRBtn.setOnLongClickListener(this);
		
		etField.setOnEditorActionListener((v, actionId, event) -> {
			
			return false;
		});
		
		//tc
		etField.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				CMN.Log("afterTextChanged");
				goToBarcodeScanner=s.length()==0;
				updateQRBtn();
			}
		});
	}
	
	private void updateQRBtn() {
		CMN.Log("updateQRBtn", goToBarcodeScanner);
		TextView QRBtn = this.QRBtn;
		if(QRBtn.getTag()!=null^goToBarcodeScanner) {
			QRBtn.setText(goToBarcodeScanner?"扫码":"前往");
			QRBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
					a.getResources().getDrawable(goToBarcodeScanner?R.drawable.ic_baseline_qrcode:R.drawable.abc_ic_go_search_api_material)
					, null, null, null);
			QRBtn.setTag(goToBarcodeScanner?Utils.DummyTransX:null);
		}
	}
	
	public void setVis(boolean vis) {
		root.setVisibility(vis?View.VISIBLE:View.GONE);
		
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.browser_widget1:
				setVis(false);
			break;
			case R.id.browser_widget2:{
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
			case R.id.browser_widget4:
				lastTextBackup = etField.getText().toString();
				etField.setText(null);
			break;
			case R.id.browser_widget5:{
				String value = etField.getText().toString().trim();
				if(longClickedQr) {
					commitField(value);
					longClickedQr=false;
				} else {
					if(goToBarcodeScanner||value.length()==0) { //二维码
						Intent intent = new Intent(a, QRActivityPlus.class);
						a.startActivityForResult(intent, RequsetUrlFromCamera);
					} else {
						commitField(value);
					}
				}
			} break;
		}
	}
	
	static SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm.ss");
	
	public static Date strToDate(String strDate) {
		ParsePosition pos = new ParsePosition(0);
		return dateFormatter.parse(strDate, pos);
	}
	
	public static String dateToStr(Date dateDate) {
		return dateFormatter.format(dateDate);
	}
	
	private void commitField(String value) {
		//CMN.Log("commitField", value, rowID, index);
		if(rowID==-1) {
			return;
		}
		if(index>=100) {
			if(index==115) { // schedule delayed task
				if(value.contains(":")||value.contains("时")||value.contains("h")) {
					Pattern timeReg = Pattern.compile(".*?([0-9]+):([0-9]+)(.[0-9]+)?.*?");
					Matcher m = timeReg.matcher(value);
					if(m.find()) {
						int hour = Integer.parseInt(m.group(1));
						int minutes = Integer.parseInt(m.group(2));
						
						String secondsStr = m.groupCount()>=3?m.group(3):null;
						if(secondsStr==null) {
							secondsStr=".0";
						}
						
						Date date = strToDate(hour + ":" + minutes+secondsStr);
						long delta = date.getTime() - strToDate(dateToStr(new Date())).getTime();
						//delta = Math.abs(delta);
						//CMN.Log("schedule delayed task !!", delta, hour + ":" + minutes+secondsStr, dateToStr(new Date()), dateToStr(new Date(date.getTime())));
						if(delta<0) {
							delta += 24*60*60*1000;
						}
						if(delta>1000) {
							a.setTaskDelayed(rowID, (int) delta, true);
							Toast.makeText(a, "Task dispatched after "+delta/1000.f/60+" minutes", Toast.LENGTH_LONG).show();
							setVis(false);
						}
						
					}
				}
			}
			return;
		}
		SQLiteDatabase db = BrowseDBHelper.getInstancedDb();
		String field = fieldForIndex(index);
		if (!TextUtils.isEmpty(field)) {
			ContentValues values = new ContentValues();
			values.put(field, value);
			db.update("tasks", values, "id=?", new String[]{"" + rowID});
			a.updateTaskList();
			setVis(false);
		}
	}
	
	private String fieldForIndex(int index) {
		switch (index) {
			case 0:
			return "title";
			case 1:
			return "url";
			case 7:
			return "ext1";
		}
		return null;
	}
	
	public void setFieldCxt(long row, int idx) {
		rowID = row;
		index = idx;
	}
	
	public void setText(String text) {
		etField.setText(text);
	}
	
	public void setGotoQR(boolean val) {
		goToBarcodeScanner=val;
	}
	
	@Override
	public boolean onLongClick(View v) {
		longClickedQr = true;
		v.performClick();
		return true;
	}
}
