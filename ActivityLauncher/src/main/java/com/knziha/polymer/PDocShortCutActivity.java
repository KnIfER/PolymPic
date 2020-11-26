package com.knziha.polymer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;

import com.knziha.polymer.pdviewer.PDFPageParms;

import java.io.File;

public class PDocShortCutActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent popup = new Intent(Intent.ACTION_MAIN).setData(Uri.fromFile(new File("/sdcard/myFolder/Gpu Pro 1.pdf")));
		popup.setClassName("com.knziha.polymer", "com.knziha.polymer.PDocViewerActivity");
		popup.setFlags(0);
		popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		popup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		getApplicationContext().startActivity(popup);
		
		finish();
	}
}
