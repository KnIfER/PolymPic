package com.knziha.polymer.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteFullException;
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
import com.knziha.polymer.qrcode.QRGenerator;
import com.knziha.polymer.widgets.TextWatcherAdapter;
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
	public ScheduleTask taskToSchedule;
	
	EditText etField;
	TextView etField_indicator;
	
	long rowID;
	int index;
	
	TextView QRBtn;
	
	private boolean goToBarcodeScanner;
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
		etField.addTextChangedListener(new TextWatcherAdapter() {
			@Override
			public void afterTextChanged(Editable s) {
				setGotoQR(s.length()==0);
			}
		});
	}
	
	public void setVis(boolean vis) {
		root.setVisibility(vis?View.VISIBLE:View.GONE);
	}
	
	public boolean getVis() {
		return root.getVisibility()==View.VISIBLE;
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
			case R.id.browser_widget5: {
				String value = etField.getText().toString().trim();
				if(goToBarcodeScanner||value.length()==0) { //二维码
					Intent intent = new Intent(a, QRActivityPlus.class);
					a.startActivityForResult(intent, RequsetUrlFromCamera);
				} else {
					commitField(value);
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
		if(index>=100) {
			if(index==115) { // schedule delayed task
				if(rowID==-1) return;
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
							a.scheduleMap.put(taskToSchedule.id, taskToSchedule);
							a.setTaskDelayed(rowID, (int) delta, true);
							float timing = delta / 1000.f / 60;
							String danwei = "minutes";
							if(timing>120) {
								timing/=60;
								danwei = "hours";
							}
							String nxtTiming =  String.format("Task dispatched after %.2f "
									+danwei+" \r\n that lasts for about %.2f hours"
									, timing
									, taskToSchedule.lifeSpanExpectancy / 60.f );
							Toast.makeText(a, ""+nxtTiming, Toast.LENGTH_LONG).show();
							setVis(false);
						}
					}
				}
			} else if(index==211) {
				a.setSearchText(value);
				setVis(false);
			}
			return;
		}
		if(rowID==-1) return;
		try {
			SQLiteDatabase db = BrowseDBHelper.getInstancedDb();
			String field = fieldForIndex(index);
			if (!TextUtils.isEmpty(field)) {
				ContentValues values = new ContentValues();
				values.put(field, value);
				db.update("tasks", values, "id=?", new String[]{"" + rowID});
				a.updateTaskList();
				//a.updateViewForRow(rowID);
				setVis(false);
			}
		} catch (SQLiteFullException e) {
			a.showT("磁盘已满！");
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
		if(goToBarcodeScanner!=val) {
			goToBarcodeScanner=val;
			QRBtn.setText(goToBarcodeScanner?"扫码":"前往");
			QRBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
					a.mResource.getDrawable(goToBarcodeScanner?R.drawable.ic_baseline_qrcode:R.drawable.abc_ic_go_search_api_material)
					, null, null, null);
		}
	}
	
	@Override
	public boolean onLongClick(View v) {
		String value = etField.getText().toString().trim();
		switch (v.getId()) {
			case R.id.browser_widget5: {
				Intent intent = new Intent(a, QRGenerator.class).putExtra(Intent.EXTRA_TEXT, value);
				a.startActivity(intent);
				a.acquireWakeLock();
			} break;
			case R.id.browser_widget4:{
				commitField(value);
			} break;
		}
		return true;
	}
}
