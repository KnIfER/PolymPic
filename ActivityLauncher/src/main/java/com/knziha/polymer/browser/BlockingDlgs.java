package com.knziha.polymer.browser;

import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Contacts;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.IU;

import org.xwalk.core.XWalkActivityDelegate;
import org.xwalk.core.XWalkUpdater;

public class BlockingDlgs {
	public static void blameNoSpace(BrowserActivity a, Bundle savedInstanceState) {
		new AlertDialog.Builder(a)
				.setTitle("启动出错")
				.setMessage("存储空间已满，无法打开数据库，请清理后重试。")
				.setPositiveButton("重试", (dialog, which) -> {
					if(a.mXWalkDelegate!=null) {
						((XWalkActivityDelegate)a.mXWalkDelegate).onResume();
					}
					a.further_loading(savedInstanceState);
				})
				.setCancelable(false)
				.show();
	}
	
	public static boolean initXWalk(BrowserActivity a, Bundle savedInstanceState) {
		if(a.mXWalkDelegate==null) {
			Runnable completeCommand = () -> a.further_loading(savedInstanceState);
			try {
				a.mXWalkDelegate = new XWalkActivityDelegate(a, completeCommand, completeCommand);
				((XWalkActivityDelegate)a.mXWalkDelegate).onResume();
				return true;
			} catch (Exception e) {
				CMN.Log(e);
				a.setWebType(0);
			}
		}
		return false;
	}
	
	public static void blameNoWebView(BrowserActivity a, Bundle savedInstanceState) {
		AlertDialog d = new AlertDialog.Builder(a)
				.setTitle("启动出错")
				.setMessage("无法初始化 WebView，请确保已安装正确的 WebView.apk ！")
				.setNeutralButton("切换 WEBVIEW 实现", null)
				.setPositiveButton("重试", (dialog, which) -> a.further_loading(savedInstanceState))
				.setCancelable(false)
				.show();
		d.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
			showSwitchWebViewDlg(a);
		});
	}
	
	public static void showSwitchWebViewDlg(BrowserActivity a) {
		int type = a.getWebType();
		DialogInterface.OnClickListener swDecorator = (dialog, which) -> {
			CMN.Log("setSingleChoiceItems!!!");
			AlertDialog d11 = (AlertDialog) dialog;
			Button btn = d11.getButton(DialogInterface.BUTTON_NEUTRAL);
			btn.setText(which==0?"安装":which==1?"卸载":"下载");
			d11.tag = which;
		};
		AlertDialog d1 = new AlertDialog.Builder(a)
				.setTitle("切换 WEBVIEW 实现")
				.setPositiveButton("确认", (dialog, which) -> a.opt.putWebType(a.setWebType(IU.parsint(((AlertDialog)dialog).tag))))
				.setNeutralButton("ASD", null)
				.setSingleChoiceItems(new String[]{"系统 WebView", "X5 WebView", "CrossWalk"}, type, swDecorator)
				.show();
		swDecorator.onClick(d1, type);
		d1.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v1 -> {
			int sw = IU.parsint(d1.tag);
			if(sw==1) {
				// todo 卸载 x5
			} else {
				//mWebListener.showT("下载完成", "查看", "history");
				new AlertDialog.Builder(a)
						.setTitle("下载"+" "+(sw==0?"WebView":"CrossWalk"))
						.setPositiveButton("取消", (dialog, which) -> { })
						.setSingleChoiceItems(new String[]{"前往系统应用市场", "直接下载"}, 0, (dialog, which) -> {
							if(which==0) {
								new XWalkUpdater(null, a).downloadXWalkApk(sw==0);
							} else {
								a.showDownloadDialog("https://dl-tc.coolapkmarket.com/down/apk_file/2021/0225/Coolapk-11.0.2-2102251-coolapk-app-sign.apk?t=1614418511&sign=e6fb10501eb137949c3746674b76918b", 0, "application/vnd.android.package-archive");
							}
						})
						.show();
			}
		});
	}
}
