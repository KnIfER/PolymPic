package com.knziha.polymer.browser;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.wget.WGet;
import com.knziha.polymer.wget.info.DownloadInfo;
import com.knziha.polymer.wget.info.ex.DownloadInterruptedError;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadTask implements Runnable{
	final BrowseTaskExecutor taskExecutor;
	final WeakReference<BrowseActivity> aRef;
	final long id;
	final String url;
	final File download_path;
	public String webTitle;
	String extUrl;
	String title;
	final int flag1;
	final String ext;
	final String ext1;
	final String ext2;
	final float maxWaitTime;
	final int lives;
	JSONObject extObj;
	AtomicBoolean abort = new AtomicBoolean();
	Thread t;
	
	int state;
	
	DownloadInfo info;
	static long last;
	private boolean notified;
	private boolean stopped;
	
	public DownloadTask(BrowseActivity a, BrowseTaskExecutor taskExecutor, long id, String url, File download_path, String title, int flag1, String ext1) {
		this.aRef = new WeakReference<>(a);
		this.taskExecutor = taskExecutor;
		this.id = id;
		this.url = url;
		this.download_path = download_path;
		this.title = title;
		this.flag1 = flag1;
		this.ext = ext1;
		try {
			extObj = JSON.parseObject(ext1);
		} catch (Exception e) {
			//CMN.Log(e);
		}
		if(extObj==null) {
			extObj = new JSONObject();
		}
		this.ext1 = extObj.getString("ext1");
		this.ext2 = extObj.getString("ext");
		this.lives = extObj.containsKey("life")?extObj.getIntValue("life"):5;
		this.maxWaitTime = extObj.containsKey("wait")?Math.max(0.03f, Math.min(5, extObj.getFloatValue("wait"))):1.5f;
	}
	
	public void updateTitle(String newTitle) {
		this.title = newTitle;
		SQLiteDatabase db = BrowseDBHelper.getInstancedDb();
		ContentValues values = new ContentValues();
		try {
			values.put("title", newTitle);
			db.update("tasks", values, "id=?", new String[]{""+id});
			BrowseActivity a = aRef.get();
			if(a!=null) {
				a.updateTitleForRow(id, newTitle);
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	private ContentValues packValues() {
		ContentValues values = new ContentValues();
		return values;
	}
	
	public void abort() {
		abort.set(true);
		if(t!=null) {
			t.interrupt();
		}
		aRef.clear();
	}
	
	@Override
	public void run() {
		boolean downloadInterrupted = false;
		BrowseActivity a = aRef.get();
		try {
			Runnable notify = new Runnable() {
				@Override
				public void run() {
					switch (info.getState()) {
						case EXTRACTING:
						case EXTRACTING_DONE:
						case DONE:
							//CMN.Log(info.getState());
							break;
						case RETRYING:
							CMN.Log(info.getState() + " " + info.getDelay());
							break;
						case DOWNLOADING:
							//long now = System.currentTimeMillis();
							//if (now - 1000 > last) {
							//	last = now;
							//	CMN.Log(info.getCount());
							//}
							if(!notified && info.getCount()>0) {
								state = 1;
								BrowseActivity a = aRef.get();
								a.updateViewForRow(id);
								a.thriveIfNeeded(id);
								notified = true;
							}
							break;
						default:
						break;
					}
				}
			};
			URL url = new URL(extUrl);
			info = new DownloadInfo(url);
			info.extract(abort, notify);
			info.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
			int cc=0;
			File target;
			
			while((target=new File(download_path, title+"."+cc+".flv")).exists()) {
			//while((target=new File("storage/sdcard1/vtech/"+title+"."+cc+".flv")).exists()) {
				cc++;
			}
			File p = target.getParentFile();
			if(!target.getParentFile().exists()) {
				p.mkdirs();
			}
			target.createNewFile();
			WGet w = new WGet(info, target);
			w.download(abort, notify);
		} catch (Exception e) {
			downloadInterrupted = e instanceof DownloadInterruptedError;
			CMN.Log(e);
		}
		CMN.Log("下载停止...", aRef.get());
		abort.set(true);
		// 下载完毕
		if(a!=null) {
			// pull again if needed.
			boolean ended=true;
			if(!stopped)
			if(!downloadInterrupted && a.respawnTask(id)) { // if not interrupted by the user.
				ended = false;
			}
			if(ended) {
				a.markTaskEnded(id);
			}
			a.taskMap.remove(id);
			a.updateViewForRow(id);
		}
		aRef.clear();
	}
	
	public void download(String url) {
		extUrl = url;
		if(t==null) {
			t = new Thread(this);
			t.start();
		}
	}
	
	public boolean isDownloading() {
		return t!=null&&!abort.get();
	}
	
	public void stop() {
		stopped = true;
		abort();
	}
	
	public long getDownloadedLength() {
		return info==null?-1:info.getCount();
	}
}
