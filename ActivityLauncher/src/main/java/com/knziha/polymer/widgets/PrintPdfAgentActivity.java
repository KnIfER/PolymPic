package com.knziha.polymer.widgets;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.knziha.polymer.BrowserActivity;

import java.util.List;

public class PrintPdfAgentActivity extends Activity {
	public static WebViewmy webview;
	private boolean startLis;
	
	public static void printPDF(Activity context, WebViewmy currentWebView) {
		if(context instanceof PrintPdfAgentActivity) {
			String name = currentWebView.getTitle()+".pdf";
			PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
			printManager.print("Print", currentWebView.createPrintDocumentAdapter(name)
					, new PrintAttributes.Builder().setColorMode(PrintAttributes.COLOR_MODE_COLOR).build());
		} else {
			webview = currentWebView;
			context.startActivity(new Intent(context, PrintPdfAgentActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		printPDF(this, webview);
		webview=null;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		startLis=true;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(startLis && webview==null) {
			getBaseContext().startActivity(new Intent(this, BrowserActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP));
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				setTaskHidden(true);
			}
			finish();
		}
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	void setTaskHidden(boolean hidden_) {
		ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.AppTask> tasks = am.getAppTasks();
		int taskId = getTaskId();
		for (int i = 0; i < tasks.size(); i++) {
			ActivityManager.AppTask appTask = tasks.get(i);
			if(appTask.getTaskInfo().id==taskId){
				appTask.setExcludeFromRecents(true);
				break;
			}
		}
	}
}
