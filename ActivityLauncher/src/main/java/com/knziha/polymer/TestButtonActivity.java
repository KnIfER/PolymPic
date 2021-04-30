package com.knziha.polymer;

import android.app.Activity;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.knziha.polymer.widgets.Utils;

public class TestButtonActivity extends Toastable_Activity implements View.OnClickListener {
	AlertDialog alert;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.test_button);
		
		
		alert = new androidx.appcompat.app.AlertDialog.Builder(this)
				.setSingleChoiceLayout(R.layout.singlechoice_plain)
				.setSingleChoiceItems(R.array.config_links, 0, null)
				.setTitle("HAPPY").create();
		
		CompoundButton button = findViewById(R.id.radio);
		
		if(false)
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				button.setChecked(!button.isChecked());
				showT(button.isChecked()?"checked":"not checked");
			}
		});
	}
	
	
	@Override
	public void onClick(View v) {
		alert.show();
		//((ArrayAdapter)alert.getListView().getAdapter()).
		Utils.postInvalidateLayout(alert.getListView());
		
		//alert.getListView().post(new Runnable() {
		//	@Override
		//	public void run() {
		//		alert.getListView().requestLayout();
		//	}
		//});
	}
	
	
	
}
