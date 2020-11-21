package com.knziha.polymer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import com.google.zxing.common.StringUtils;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.pdviewer.PDocView;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.widgets.Utils;

/**
 * Recreated by KnIfER on 2019
 */
public class PolyShareActivity extends Activity {
	private String debugString;
	static ActivityManager.AppTask hiddenId;
	public final static int SingleTaskFlags = Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP;
	public static boolean launched;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setTheme(R.style.AppTheme);
		ProcessIntent(getIntent());
		finish();
		launched=true;
	}

	public void ProcessIntent(Intent intent) {
		Uri uri = intent.getData();
		if(uri!=null) {
			String path = uri.getPath();
			if(path!=null) {
				if(".pdf".equals(Utils.getSuffix(path))) {
					CMN.Log("PDocView.books",PDocView.books);
					PDocument doc = PDocView.books.get(path);
					if(doc==null) {
						Intent popup = new Intent().setClassName("com.knziha.polymer", "com.knziha.polymer.pdviewer.PDocViewerActivity").setData(uri);
						//popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						popup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						popup.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
						popup.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
						//popup.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						//CMN.Log("pop that way!");
						getApplicationContext().startActivity(popup);
					} else {
						int aid = doc.aid;
						ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
						if (aid!=0 && manager != null)
							manager.moveTaskToFront(aid, ActivityManager.MOVE_TASK_WITH_HOME);
						CMN.Log("启动旧实例……");
					}
				}
			}
		}
	}

}