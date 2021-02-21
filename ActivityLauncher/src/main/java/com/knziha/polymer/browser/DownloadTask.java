package com.knziha.polymer.browser;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.JSONObjectWrap;
import com.knziha.polymer.wget.WGet;
import com.knziha.polymer.wget.info.DownloadInfo;
import com.knziha.polymer.wget.info.ex.DownloadInterruptedError;
import com.knziha.polymer.wget.info.ex.DownloadMoved;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public class DownloadTask implements Runnable{
	final BrowseTaskExecutor taskExecutor;
	final WeakReference<BrowseActivity> aRef;
	final long id;
	final String url;
	final String ua;
	final File download_path;
	public String webTitle;
	final String shotExt;
	public String shotFn;
	final boolean fifoL;
	String extUrl;
	String title;
	final int flag1;
	final String ext;
	final String ext1;
	ArrayList<ExtRule> ext2;
	final float maxWaitTime;
	final int lives;
	JSONObjectWrap extObj;
	AtomicBoolean abort = new AtomicBoolean();
	Thread t;
	
	int state;
	
	public boolean ext2contains(String url) {
		if(ext2!=null)
		for(ExtRule etI:ext2) {
			if(etI.contains(url)) {
				return true;
			}
		}
		return false;
	}
	
	public static class ExtRule {
		String ext;
		Pattern p;
		int minLen=0;
		int maxLen=Integer.MAX_VALUE;
		
		public ExtRule(String ext) {
			this.ext =  ext;
		}
		
		public ExtRule(String ext, String p, int min, int max) {
			this.ext =  ext;
			this.p = p==null?null:Pattern.compile(p);
			this.minLen = min;
			this.maxLen = max;
		}
		
		public ExtRule(JSONObject obj) throws JSONException {
			this(obj.getString("ext")
					, obj.has("p")?obj.getString("p"):null
					, obj.has("min")?obj.getInt("min"):0
					, obj.has("max")?obj.getInt("max"):Integer.MAX_VALUE
					);
		}
		
		public boolean contains(String val) {
			if(val==null) {
				return false;
			}
			int len = val.length();
			if(len<minLen||len>maxLen) {
				return false;
			}
			if(ext!=null && val.contains(ext)) {
				return true;
			}
			if(p!=null && p.matcher(val).find()) {
				return true;
			}
			return false;
		}
	}
	
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
			extObj = new JSONObjectWrap(ext1);
		} catch (Exception e) {
			CMN.Log(e);
		}
		if(extObj==null) {
			extObj = new JSONObjectWrap();
		}
		this.ext1 = extObj.getString("ext1");
		String ext = extObj.getString("ext");
		if(ext!=null) {
			this.ext2 = new ArrayList<>();
			this.ext2.add(new ExtRule(ext));
		}
		JSONArray extX = extObj.getJSONArray("etx");
		if(extX!=null) {
			if(this.ext2==null) this.ext2 = new ArrayList<>();
			for (int i = 0; i < extX.length(); i++) {
				try {
					this.ext2.add(new ExtRule(extX.getJSONObject(i)));
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
		}
		
		this.fifoL = extObj.has("L") && extObj.getBoolean("L");
		this.shotExt = extObj.getString("shot");
		this.ua = extObj.getString("ua");
		this.lives = extObj.has("life")?extObj.getInt("life"):5;
		this.maxWaitTime = extObj.has("wait")?Math.max(0.03f, Math.min(5, extObj.getFloat("wait"))):0.6f;
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
		URL jumpTarget=null;
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
							if(shotExt ==null)
								a.thriveIfNeeded(id);
							notified = true;
						}
						break;
					default:
						break;
				}
			}
		};
		while(!abort.get())
		try {
			URL url = jumpTarget==null?new URL(extUrl):jumpTarget;
			info = new DownloadInfo(url);
			info.extract(abort, notify);
			//info.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
			info.setUserAgent("Mozilla/5.0 (Linux; Android 6.0.1; OPPO A57) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Mobile Safari/537.36 OPR/58.2.2878.53403");
			int cc=0;
			File target;
			String ext = ".flv";
			String fn = title;
			if(shotExt!=null) {
				ext = shotExt;
			}
			if(!TextUtils.isEmpty(shotFn)) {
				fn = shotFn;
			}
			if(fn.length()>18) {
				fn = fn.substring(0, 18);
			}
			while((target=new File(download_path, fn+"."+cc+ext)).exists()) {
			//while((target=new File("storage/sdcard1/vtech/"+title+"."+cc+".flv")).exists()) {
				cc++;
			}
			File p = target.getParentFile();
			if(!target.getParentFile().exists()) {
				p.mkdirs();
			}
			//target.createNewFile();
			WGet w = new WGet(info, target);
			w.download(abort, notify);
			break;
		} catch (Exception e) {
			if(e instanceof DownloadMoved && jumpTarget==null) {
				try {
					jumpTarget=((DownloadMoved) e).getMoved();
				} catch (Exception exception) {
					//CMN.Log("WTF111::", e);
					break;
				}
				CMN.Log("moving to...", jumpTarget);
				continue;
			}
			downloadInterrupted = e instanceof DownloadInterruptedError;
			//CMN.Log("WTF::", e);
			break;
		}
		CMN.Log("下载停止...", aRef.get());
		boolean prevStopped = abort.getAndSet(true);
		// 下载完毕
		if(a!=null) {
			// pull again if needed.
			boolean ended=true;
			if(!stopped)
			if(!downloadInterrupted && a.respawnTask(id)) { // if not interrupted by the user.
				ended = false;
			}
			if(ended) {
				if(!prevStopped&&fifoL) {
					a.drainTaskForDB(id);
				} else {
					a.markTaskEnded(id);
				}
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
