package com.knziha.polymer.widgets;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.knziha.polymer.Utils.CMN;

public class DialogVanishing extends Dialog {
	public DialogVanishing(@NonNull Context context) {
		super(context);
		CMN.Log("onBackPressed ???");
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		CMN.Log("onKeyDown", keyCode, event.getAction());
		if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
			event.startTracking();
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode,  KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE)
				&& event.isTracking()
				&& !event.isCanceled()) {
			CMN.Log("KEYCODE_BACK", keyCode, event.getAction());
			onBackPressed();
			return true;
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		CMN.Log("onBackPressed");
		super.onBackPressed();
	}

	void hideKeyboard() {
		View ht = getCurrentFocus();
		if(ht!=null) {
			InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(ht.getWindowToken(),0);
		}
	}
}
