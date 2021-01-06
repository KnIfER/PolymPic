package com.knziha.polymer.webstorage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.knziha.polymer.R;
import com.knziha.polymer.database.LexicalDBHelper;

public class BrowserDownloads extends BrowserHistory implements View.OnClickListener {
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseIconIndicatorRes = R.drawable.ic_file_download_black_24dp;
	}
	
	protected void pullData() {
		cursor = LexicalDBHelper.getInstancedDb().rawQuery("select id,url,filename,creation_time from downloads order by creation_time DESC", null);
		adapter.notifyDataSetChanged();
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.home:
				dismiss();
			break;
		}
	}
}
