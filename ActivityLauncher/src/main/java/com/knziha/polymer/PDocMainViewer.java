package com.knziha.polymer;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;

import com.knziha.polymer.pdviewer.PDFPageParms;

public class PDocMainViewer extends PDocViewerActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this_instanceof_PDocMainViewer=true;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
			ActivityManager.TaskDescription taskDesc = new ActivityManager.TaskDescription(
					"PDF Viewer",//title
					BitmapFactory.decodeResource(getResources(), R.drawable.ic_pdoc_house1),//图标
					ResourcesCompat.getColor(getResources(), R.color.colorPrimary,
							getTheme()));
			setTaskDescription(taskDesc);
		}
	}
}
