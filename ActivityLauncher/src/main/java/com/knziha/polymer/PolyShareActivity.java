package com.knziha.polymer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Window;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.pdviewer.PDFPageParms;
import com.knziha.polymer.pdviewer.PDocView;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.widgets.Utils;

import java.util.HashMap;

import static com.knziha.polymer.PDocViewerActivity.MultiInstMode;
import static com.knziha.polymer.PDocViewerActivity.PDFPageParmsMap;
import static com.knziha.polymer.PDocViewerActivity.parsePDFPageParmsFromIntent;

/**
 * Recreated by KnIfER on 2019
 */
public class PolyShareActivity extends Activity {
	public final static int SingleTaskFlags = Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setTheme(R.style.AppTheme);
		ProcessIntent(getIntent());
		finish();
	}

	public void ProcessIntent(Intent intent) {
		Uri uri = intent.getData();
		if(uri!=null) {
			String path = Utils.getRunTimePath(uri);
			CMN.Log("PolySharing", "path = "+path);
			if(path!=null) {
				if (".pdf".equals(Utils.getSuffix(path))) {
					CMN.Log("PDocView.books", PDocView.books);
					PDocument doc = PDocView.books.get(path);
					if (doc == null) {
						Intent popup = new Intent().setData(uri);
						if (MultiInstMode) {
							popup.setClassName("com.knziha.polymer", "com.knziha.polymer.PDocViewerActivity");
							popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							popup.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
							popup.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
							//CMN.Log("pop that way!");
						} else {
							popup.setClassName("com.knziha.polymer", "com.knziha.polymer.PDocViewerActivity");
							popup.setAction(Intent.ACTION_MAIN);
							popup.setFlags(0);
							popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							popup.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							popup.putExtra("sin", true); // you are single, remove dead history
						}
						getApplicationContext().startActivity(popup);
					} else {
						int aid = doc.aid;
						ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
						if (aid != 0 && manager != null) {
							manager.moveTaskToFront(aid, ActivityManager.MOVE_TASK_WITH_HOME);
							PDFPageParms pageParms = parsePDFPageParmsFromIntent(intent);
							if (pageParms != null) {
								PDFPageParmsMap.put(aid, pageParms);
							}
						}
						CMN.Log("启动旧实例……");
					}
				}
			}
		}
	}

}