package com.knziha.polymer.browser;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.knziha.filepicker.utils.FU;
import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.database.LexicalDBHelper;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.DOWNLOAD_SERVICE;
import static com.knziha.polymer.widgets.Utils.RequsetUrlFromStorage;

public class DownloadHandlerStd {
	Toastable_Activity a;
	final LexicalDBHelper historyCon;
	private final DownloadManager downloadManager;
	DownloadCompleteReceiver dwnldReceiver;
	
	static Set<Object> queuedDownloads = Collections.synchronizedSet(new HashSet<>());
	
	public String getDownloadPathForDwnldID(long dwnldID) {
		Uri path = downloadManager.getUriForDownloadedFile(dwnldID);
		return path==null?null:path.toString();
	}
	
	public String getMimeTypeForDwnldID(long dwnldID) {
		return downloadManager.getMimeTypeForDownloadedFile(dwnldID);
	}
	
	class GetDwnldPathRunnable implements Runnable {
		final Toastable_Activity a;
		final long dwnldID;
		GetDwnldPathRunnable(Toastable_Activity a, long dwnldID) {
			this.a = a;
			this.dwnldID = dwnldID;
		}
		@Override
		public void run() {
			if(a.systemIntialized) {
				long rowId = historyCon.getRowIdForTaskID(dwnldID);
				if(rowId!=-1 && dwnldReceiver.recordRealPathForDwnldWithRow(dwnldID, rowId, downloadManager, historyCon)) {
					queuedDownloads.remove(dwnldID);
				}
			}
		}
	}
	
	public DownloadHandlerStd( Toastable_Activity a, LexicalDBHelper historyCon) {
		this.historyCon = historyCon;
		downloadManager = (DownloadManager) a.getSystemService(Context.DOWNLOAD_SERVICE);
		dwnldReceiver = new DownloadCompleteReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
		a.registerReceiver(dwnldReceiver, intentFilter);
		this.a = a;
	}
	
	private class DownloadCompleteReceiver extends BroadcastReceiver {
		public File targetDirPath;
		private boolean recordRealPathForDwnldWithRow(long dwnldID, long rowId, DownloadManager downloadManager, LexicalDBHelper db) {
			String realUrl=null;
			Cursor qursor = downloadManager.query(new DownloadManager.Query().setFilterById(dwnldID));
			if(qursor!=null && qursor.moveToNext()) {
				realUrl = qursor.getString(qursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
			}
			if(realUrl!=null) {
				db.updatePathForDownload(rowId, realUrl);
				CMN.Log("updatePathForDownload", rowId, realUrl, "the dwnldID is :: ", dwnldID);
			} else {
				String fileName = db.getIntenedFileNameForDownload(rowId);
				if(fileName!=null) {
					db.updatePathForDownload(rowId, "file://"+new File(targetDirPath, fileName).getPath());
					CMN.Log("勉为其难得记下了目标。", new File(targetDirPath, fileName).getPath());
				}
			}
			CMN.Log("realUrl received...", rowId, realUrl);
			return realUrl!=null;
		}
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
					long dwnldID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
					if(queuedDownloads.remove(dwnldID)) {
						LexicalDBHelper db = LexicalDBHelper.connectInstance(context);
						long rowId = db.getRowIdForTaskID(dwnldID);
						if(rowId!=-1) {
							DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
							recordRealPathForDwnldWithRow(dwnldID, rowId, downloadManager, db);
						}
						db.try_close();
					}
					a.showT("downloads");
				}
			}
		}
	}
	
	@SuppressLint("NewApi")
	public void start(Toastable_Activity a
			, String url
			, String fileName
			, @NonNull File downloadTargetDir
			, long contentLength, String mimetype, boolean req, boolean blame) {
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		//request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		request.setDescription(a.mResource.getString(R.string.downloadingWithSz, FU.formatSize(contentLength)));
		request.setAllowedOverRoaming(false);
		//downloadTargetDir = new File("/storage/2486-F9E1/Android/data/com.android.providers.downloads");
		//if(downloadTargetDir!=null)
		{
			request.setDestinationUri(Uri.fromFile(new File(downloadTargetDir, fileName)));
		}
		dwnldReceiver.targetDirPath = downloadTargetDir;
		boolean needPermission = Build.VERSION.SDK_INT>=Build.VERSION_CODES.M
				&& a.checkSelfPermission(a.permissions[0]) != PackageManager.PERMISSION_GRANTED;
		if(needPermission&&req) {
			a.requestPermissions(a.permissions, RequsetUrlFromStorage);
			return;
		}
		if(needPermission) {
			request.setDestinationInExternalFilesDir(a, Environment.DIRECTORY_DOWNLOADS, fileName);
			if(blame) {
				a.showT("No Permission. Download to "+a.getExternalFilesDir(null)+" instead");
			}
		} else {
			request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
		}
		try {
			long dwnldID = downloadManager.enqueue(request);
			queuedDownloads.add(dwnldID);
			historyCon.recordDwnldItem(dwnldID, url, fileName, contentLength, mimetype);
			a.root.postDelayed(new GetDwnldPathRunnable(a, dwnldID), 350);
			CMN.Log("下载开始...", downloadTargetDir, dwnldID, fileName);
			if(!blame) {
				a.showT("下载中…");
			}
		} catch (Exception e) { CMN.Log(e); }
	}
	
	public Cursor queryDownloadUrl(String url) {
		String sql = "select path,tid from downloads where url=? order by creation_time DESC limit 50";
		Cursor qursor = historyCon.getDB().rawQuery(sql, new String[]{url});
		while(qursor.moveToNext()) {
			if(downloadManager.getUriForDownloadedFile(qursor.getLong(1))!=null
				||qursor.getString(0)!=null) {
				return qursor;
			}
		}
		qursor.close();
		return null;
	}
	
	
	public static class DownloadItemView {
		public final ImageView downloadIconView;
		public final TextView downloadProgressIndicator;
		public boolean running;
		public Animation mShake;
		public long tid;
		public boolean animStarted;
		
		public DownloadItemView(ImageView downloadIconView, TextView downloadProgressIndicator) {
			this.downloadIconView = downloadIconView;
			this.downloadProgressIndicator = downloadProgressIndicator;
			this.downloadProgressIndicator.setVisibility(View.VISIBLE);
		}
	}
	
	public void queryDownloadStates(long id, DownloadItemView dwnldView, int normalRes) {
		Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(id));
		dwnldView.running = false;
		int mRes = normalRes;
		String tmp = " -- ";
		if (cursor != null && cursor.moveToFirst()) {
			int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
			//CMN.Log(id, "status???", status, status==DownloadManager.STATUS_RUNNING, dwnldView);
			if(status==DownloadManager.STATUS_FAILED) {
				mRes = R.drawable.abc_ic_clear_material;
			} else if(status==DownloadManager.STATUS_PAUSED) {
				//downloadIndicator.setImageResource(R.drawable.pause);
			} else {
				mRes = normalRes;
				if(status==DownloadManager.STATUS_RUNNING) {
					dwnldView.running = true;
				}
			}
//			int fileName = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
//			int fileUri = cursor.getColumnIndex(DownloadManager.COLUMN_URI);
			long totalBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
			String D = FU.formatSize(totalBytes);
			if(status==DownloadManager.STATUS_SUCCESSFUL) {
				tmp = D+"  ";
			} else
			{
				long downloadedBytes = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
				String N = FU.formatSize(downloadedBytes);
				if(N.regionMatches(N.length()-2, D, D.length()-2, 2)) {
					N = N.substring(0, N.length()-3);
				}
				tmp = N+"/"+D;
			}
		}
		cursor.close();
		dwnldView.downloadIconView.setImageResource(mRes);
		dwnldView.downloadProgressIndicator.setText(tmp);
	}
}
