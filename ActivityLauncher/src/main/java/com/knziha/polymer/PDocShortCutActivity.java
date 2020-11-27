package com.knziha.polymer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.pdviewer.PDFPageParms;

import java.io.File;
import java.util.HashSet;

public class PDocShortCutActivity extends Activity {
	public static SparseArray<PDocShortCutActivity> blackSmithStack = new SparseArray<>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		CMN.Log("PDocShortCutActivity", getIntent(), getIntent().hasExtra("ASD"));
		
		Intent popup = new Intent(Intent.ACTION_VIEW).setData(Uri.fromFile(new File("/storage/emulated/0/myFolder/Gpu Pro 1.pdf")));
		popup.setClassName("com.knziha.polymer", "com.knziha.polymer.PDocViewerActivity");
		popup.setFlags(0);
		popup.putExtra("sin", true);
		popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		popup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		boolean bStatic = PDocViewerActivity.singleInstCout==0;
		
//		overridePendingTransition(0, 0);
		
		popup.putExtra("BST", getTaskId());
		blackSmithStack.put(getTaskId(), this);
		
		getApplicationContext().startActivity(popup);
		
		if(bStatic) {
			//overridePendingTransition(R.anim.alpha_in, 0); //
		}
		
		
		super.onCreate(savedInstanceState);
		
		//finish();
		
//		overridePendingTransition(0, 0);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		blackSmithStack.remove(getTaskId());
	}
	
}
