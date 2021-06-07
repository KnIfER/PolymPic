package com.knziha.polymer.widgets;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.print.PdfPrint;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.tencent.smtt.sdk.WebView;

import java.io.File;
import java.util.List;

public class PDFPrintManager extends Activity {
	public static UniversalWebviewInterface webview;
	private boolean startLis;
	
	public static void printPDF(Activity context, UniversalWebviewInterface currentWebView, boolean bSkipUI) {
		String name = currentWebView.getTitle()+".pdf";
		if(context instanceof PDFPrintManager) {
			PrintManager printManager = (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
			printManager.print("Print", (PrintDocumentAdapter) currentWebView.initPrintDocumentAdapter(name)
					, new PrintAttributes.Builder()
							//.setColorMode(PrintAttributes.COLOR_MODE_COLOR)
							//.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
							.build());
		} else {
			if(bSkipUI || context==null) {
				try {
					PrintAttributes.Builder attributes = new PrintAttributes.Builder()
							.setMediaSize(PrintAttributes.MediaSize.NA_LEGAL)
							.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600))
							.setMinMargins(PrintAttributes.Margins.NO_MARGINS);
					if (context==null) {
						attributes.setMediaSize(new PrintAttributes.MediaSize("xyz", "xyz"
								, currentWebView.getContentWidth()
								, currentWebView.getContentHeight()
						));
					}
					File path = Environment.getExternalStoragePublicDirectory("/pdf_output");
					
					new PdfPrint(attributes.build()).print(
							(PrintDocumentAdapter) currentWebView.initPrintDocumentAdapter(name),
							path,
							"output_$date.pdf"
					);
				} catch (Exception e) {
					CMN.Log(e);
				}
				return;
			}
			webview = currentWebView;
			context.startActivity(new Intent(context, PDFPrintManager.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
		}
	}
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		printPDF(this, webview, false);
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
