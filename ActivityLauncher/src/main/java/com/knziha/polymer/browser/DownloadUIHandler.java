package com.knziha.polymer.browser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.appcompat.app.GlobalOptions;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.knziha.filepicker.utils.FU;
import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.WeakReferenceHelper;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.DownloadBottomSheetBinding;
import com.knziha.polymer.widgets.Utils;

import java.io.File;

import static com.knziha.polymer.widgets.Utils.RequsetUrlFromStorage;
import static com.knziha.polymer.widgets.Utils.getSimplifiedUrl;
import static com.knziha.polymer.widgets.Utils.setOnClickListenersOneDepth;

public class DownloadUIHandler {
	final DownloadHandlerStd downloader;
	final Toastable_Activity a;
	private BottomSheetDialog bottomDwnldDlg;
	
	public File downloadTargetDir;
	private String toName;
	private Cursor pursor;
	
	public DownloadUIHandler(Toastable_Activity a, LexicalDBHelper historyCon) {
		this.a = a;
		downloader = new DownloadHandlerStd(a, historyCon);
		downloadTargetDir = new File("/storage/emulated/0/download");
	}
	
	public DownloadHandlerStd getDownloader() {
		return downloader;
	}
	
	@SuppressLint("NonConstantResourceId")
	public void showDownloadDialog(String url, long contentLength, String mimetype) {
		//showT(text);
//		Intent pageView = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
//		pageView.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		startActivity(pageView);
		
		String toName = new File(url).getName();
		int idx = toName.indexOf("?");
		if(idx!=-1) {
			toName = toName.substring(0, idx);
		}
		
		int id = WeakReferenceHelper.bottom_download_dialog;
		BottomSheetDialog bottomPlaylist = (BottomSheetDialog) a.getReferencedObject(id);
		if(bottomPlaylist==null) {
			CMN.Log("重建底部弹出");
			a.putReferencedObject(id, bottomPlaylist = new BottomSheetDialog(a));
			DownloadBottomSheetBinding downloadDlg = DownloadBottomSheetBinding.inflate(LayoutInflater.from(a));
			BottomSheetDialog final_bottomPlaylist = bottomPlaylist;
			View.OnClickListener clicker = v -> {
				switch (v.getId()) {
					case R.id.open: { //打开
						String path=new File(downloadTargetDir, this.toName).getPath();
						String mime=guessMimeTypeFromName(this.toName);
						if(pursor!=null) {
							long dwnldID = pursor.getLong(1);
							path = pursor.getString(0);
							if(TextUtils.isEmpty(path))
							{
								path = downloader.getDownloadPathForDwnldID(dwnldID);
							}
							mime = downloader.getMimeTypeForDwnldID(dwnldID);
							CMN.Log("found dwnld in db !!!");
						}
						if(!TextUtils.isEmpty(path)) {
							CMN.Log("打开::", path);
							if(!TextUtils.isEmpty(path)) {
								Uri data = getSimplifiedUrl(a, Uri.parse(path));
								Intent viewer = new Intent(Intent.ACTION_VIEW)
										.setDataAndType(data, mime)
										.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
										.addCategory(Intent.CATEGORY_DEFAULT);
								if(data.toString().startsWith("file://")) {
									Utils.fuckVM();
								}
								try {
									a.startActivity(viewer);
								} catch (Exception e) {
									a.showT("打开失败");
									CMN.Log(e);
								}
							}
						}
					} break;
					case R.id.abort: {
						final_bottomPlaylist.dismiss();
					} break;
					case R.id.new_folder:{
						boolean goInternalDirectoryPicker = false;
						if(!goInternalDirectoryPicker) {
							try {
								Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
								i.addCategory(Intent.CATEGORY_DEFAULT);
								a.startActivityForResult(Intent.createChooser(i,"Pick Download Path"),
										RequsetUrlFromStorage);
							} catch (Exception e) {
								goInternalDirectoryPicker = true;
								CMN.Log(e);
							}
						}
						if(goInternalDirectoryPicker) {
						
						}
					} break;
					case R.id.replace:{
						Cursor cursor = (Cursor) downloadDlg.open.getTag();
						if(cursor!=null) {
							String path = cursor.getString(0);
							if(path!=null) {
								FU.delete3(a, FU.getFile(path));
								CMN.Log("删除1…", path);
							}
							long dwnldID = cursor.getLong(1);
							path = downloader.getDownloadPathForDwnldID(dwnldID);
							if(path!=null) {
								Uri data = getSimplifiedUrl(a, Uri.parse(path));
								path = data.toString();
								if(path.startsWith("file://")) {
									CMN.Log("删除2…", path);
									FU.delete3(a, FU.getFile(path));
								}
							}
						}
					}
					case R.id.download:
						startDownload(true, false);
						final_bottomPlaylist.dismiss();
						break;
				}
			};
			setOnClickListenersOneDepth(downloadDlg.dialog, clicker, 999, null);
			bottomPlaylist.setContentView(downloadDlg.dialog);
			bottomPlaylist.setOnDismissListener(dialog -> {
				bottomDwnldDlg = null;
				setLastQueriedCursor(null);
			});
			bottomPlaylist.tag=downloadDlg;
			Window win = bottomPlaylist.getWindow();
			if(win!=null) {
				win.setDimAmount(0.2f);
				win.findViewById(R.id.design_bottom_sheet).setBackground(null);
				View decor = win.getDecorView();
				decor.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom)
						-> v.postDelayed(() -> {
					int targetTranY = 0;
					if(Utils.isKeyboardShown(v)) {
						int resourceId = a.mResource.getIdentifier("status_bar_height", "dimen", "android");
						a.mStatusBarH = a.mResource.getDimensionPixelSize(resourceId);
						int max = Utils.rect.height() - downloadDlg.dirPath.getHeight()-a.mStatusBarH;
						if(max>0) {
							targetTranY = Math.min(max, (int) (-50 * GlobalOptions.density));
						}
					}
					((ViewGroup)v).getChildAt(0).setTranslationY(targetTranY);
					Utils.TrimWindowWidth(win, a.dm);
					CMN.Log("addOnLayoutChangeListener", Utils.isKeyboardShown(v), a.dm.widthPixels);
				}, 0));
			}
			bottomPlaylist.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
			if(GlobalOptions.isDark) {
				downloadDlg.dialog.setBackgroundColor(Color.BLACK);
				downloadDlg.dirPath.setTextColor(Color.WHITE);
			}
		}

//		View v = (View) _bottomPlaylist.getWindow().getDecorView().getTag();
//		DisplayMetrics dm2 = new DisplayMetrics();
//		getWindowManager().getDefaultDisplay().getRealMetrics(dm2);
//		v.getLayoutParams().height = (int) (Math.max(dm2.heightPixels, dm2.widthPixels) * _bottomPlaylist.getBehavior().getHalfExpandedRatio() - getResources().getDimension(R.dimen._45_) * 1.75);
//		v.requestLayout();
		Utils.TrimWindowWidth(bottomPlaylist.getWindow(), a.dm);
		DownloadBottomSheetBinding downloadDlg = (DownloadBottomSheetBinding) bottomPlaylist.tag;
		downloadDlg.dirPath.setText(this.toName = toName);
		downloadDlg.dirPath.setTag(url);
		
		Cursor recorded = downloader.queryDownloadUrl(url);
		int vis = recorded==null?View.GONE:View.VISIBLE;
		boolean schFileSystem= true;
		if(recorded==null&&schFileSystem&&new File(downloadTargetDir, toName).exists()) {
			vis=View.VISIBLE;
		}
		setLastQueriedCursor(recorded);
		//if(recorded!=null) CMN.Log("downloadDlg.dirPath ", recorded.getString(0), recorded.getLong(1));
		downloadDlg.replace.setVisibility(vis);
		downloadDlg.replace.setTag(contentLength);
		downloadDlg.abort.setTag(mimetype);
		downloadDlg.open.setVisibility(vis);
		downloadDlg.download.setText(a.mResource.getString(R.string.downloadWithSz, FU.formatSize(contentLength)));
		bottomPlaylist.show();
		
		bottomDwnldDlg = bottomPlaylist;
	}
	
	private void setLastQueriedCursor(Cursor recorded) {
		if(pursor!=null) {
			pursor.close();
		}
		pursor = recorded;
	}
	
	private String guessMimeTypeFromName(String toName) {
		return "*/*";
	}
	
	
	@SuppressLint("NewApi")
	public void startDownload(boolean req, boolean blame) {
		if(bottomDwnldDlg!=null) {
			downloader.historyCon.ensureDwnldTable(null);
			DownloadBottomSheetBinding downloadDlg = (DownloadBottomSheetBinding) bottomDwnldDlg.tag;
			String fileName = downloadDlg.dirPath.getText().toString();
			String mimetype = String.valueOf(downloadDlg.abort.getTag());
			long contentLength = (long) downloadDlg.replace.getTag();
			if(TextUtils.isEmpty(fileName)) {
				fileName = "unknown.download";
			}
			String URL = (String) downloadDlg.dirPath.getTag();
			downloader.start(a, URL, fileName, downloadTargetDir, contentLength, mimetype, req, blame);
		}
	}
}
