
package com.knziha.polymer.webstorage;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.BufferedReader;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.ReusabeBufferedInputStream;
import com.knziha.polymer.webslideshow.WebPic.WebPic;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.knziha.polymer.widgets.WebFrameLayout.webStacksWriterSer;

public class SardineCloud implements Runnable {
	OkHttpSardine sardine;
	Thread t;
	StringBuilder pathBuilder;
	public ArrayList<TabBean> pulledTabs = new ArrayList<>();
	public ArrayList<TabBean> mergedTabs = new ArrayList<>();
	HashMap<Long, TabBean> pulledTabsTable = new HashMap<>();
	WeakReference<BrowserActivity> aRef = new WeakReference<>(null);
	volatile boolean threadSuspended;
	private volatile AtomicBoolean abort;
	private volatile AtomicBoolean mayAbort;
	public int progress;
	public int progressMax;
	
	public enum TaskType {
		idle,
		uploadTabs,
		pullTabs,
		syncTabs,
	}
	volatile TaskType task;
	volatile TaskType runningTask;
	private boolean stopped;
	String username = "302772670@qq.com";
	String password = "ae9jp9qnwgbj639a";
	String webdavServer = "https://dav.jianguoyun.com/dav";
	
	public void scheduleTask(BrowserActivity a, TaskType taskType) {
		progress = 0;
		if(stopped) {
			return;
		}
		if(aRef.get()==null) {
			aRef = new WeakReference<>(a);
		}
		task = TaskType.idle;
		if(t==null) {
			task = taskType;
			t = new Thread(this);
			sardine = new OkHttpSardine(a, username, password, false);
			pathBuilder = new StringBuilder(webdavServer);
			t.start();
		} else {
			if(abort!=null) {
				abort.set(true);
			}
			if(sardine!=null && mayAbort==null||mayAbort.get()) {
				sardine.abort();
			}
			task = taskType;
			threadSuspended = false;
			try {
				t.notify();
			} catch (Exception ignored) {
				t.interrupt();
			}
		}
	}
	
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (t==thisThread) {
			runningTask = task;
			mayAbort = new AtomicBoolean(true);
			abort = new AtomicBoolean();
			try {
				handleTask(abort, mayAbort);
			} catch (Exception e) {
				CMN.Log(e);
			}
			abort.set(true);
			threadSuspended = true;
			try {
				Thread.sleep(1000);
				if(threadSuspended) {
					synchronized(t) {
						while (threadSuspended && t!=null)
							t.wait();
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}
	
	private void handleTask(AtomicBoolean abort, AtomicBoolean mayAbort) {
		switch (runningTask) {
			case uploadTabs:
				uploadTabs(abort, mayAbort);
			break;
			case pullTabs:
				pullTabs(abort, mayAbort);
			break;
			case syncTabs:
				syncTabs(abort, mayAbort);
			break;
		}
	}
	
	public synchronized void stop() {
		stopped = true;
		Thread moribund = t;
		t = null;
		notify();
		if (moribund != null) {
			moribund.interrupt();
		}
	}
	
	public static class TabBean {
		public final long creation;
		public final long token;
		public final String title;
		public final String url;
		public long id;
		public int type;
		public boolean selected;
		public long flag;
		
		TabBean(long creation, long token, String title, String url) {
			this.creation = creation;
			this.token = token;
			this.title = title;
			this.url = url;
		}
		
		@Override
		public String toString() {
			return "TabBean{" +
					"creation=" + creation +
					", token=" + token +
					", title='" + title + '\'' +
					'}';
		}
	}
	
	public void uploadTabs(AtomicBoolean abort, AtomicBoolean mayAbort) {
		List<DavResource> resources = null;
		BrowserActivity a = aRef.get();
		if(a==null) {
			return;
		}
		try {
			ArrayList<BrowserActivity.TabHolder> holders = a.TabHolders;
			progressMax = holders.size()*25+30;
			progress=10;
			pulledTabs.clear();
			pulledTabsTable.clear();
			// 上传数据库内容。
			if(pollSardineDirectories(sardine, webdavServer, true, pathBuilder, "/PLBrowser/", "tabs/")) {
				int LEN_PLBR_TABS = pathBuilder.length();
				final String PLBR_TABS_ALL = pathBuilder.append("all.txt").toString();
				readTabsList(abort, PLBR_TABS_ALL);
				progress+=10;
				CMN.Log(pulledTabs.toArray());
				StringBuffer sb = new StringBuffer();
				for(BrowserActivity.TabHolder thI:holders) {
					if(abort.get()) return;
					long id = thI.id;
					Cursor cursor = a.historyCon.getDB().rawQuery("select * from webtabs where id=? limit 1", new String[]{""+id});
					if(cursor.moveToNext()) {
						Bundle bundle = new Bundle();
						long creationTime = cursor.getLong(cursor.getColumnIndex("creation_time"));
						long last_visit_time = cursor.getLong(cursor.getColumnIndex("last_visit_time"));
						String title = cursor.getString(cursor.getColumnIndex("title"));
						if(TextUtils.isEmpty(title)) {
							title = "Untitled";
						}
						TabBean tb = pulledTabsTable.get(creationTime);
						if(tb==null||last_visit_time!=tb.token) { // 最后入库时大于读取值时，需要上传
							bundle.putLong("last_visit_time", last_visit_time);
							bundle.putLong("creation_time", creationTime);
							bundle.putLong("f1", cursor.getLong(cursor.getColumnIndex("f1")));
							bundle.putLong("favor", cursor.getLong(cursor.getColumnIndex("favor")));
							bundle.putLong("visit_count", cursor.getLong(cursor.getColumnIndex("visit_count")));
							bundle.putByteArray("thumbnail", cursor.getBlob(cursor.getColumnIndex("thumbnail")));
							bundle.putByteArray("webstack", cursor.getBlob(cursor.getColumnIndex("webstack")));
							bundle.putString("url", cursor.getString(cursor.getColumnIndex("url")));
							bundle.putString("title", title);
							bundle.putString("ext1", cursor.getString(cursor.getColumnIndex("ext1")));
							byte[] data = webStacksWriterSer.bakeData(bundle);
							pathBuilder.setLength(LEN_PLBR_TABS);
							sardine.put(pathBuilder.append("tab_")
											.append(creationTime)
											.append(".bin").toString()
									, data);
							CMN.Log("webdav::写入成功 ", id, data.length);
						}
						int tlen = title.length();
						boolean ellipsizeEnd = tlen>16;
						sb.append(creationTime)
								.append(";")
								.append(last_visit_time)
								.append(";")
						;
						title = title.replace(";", "");
						if(ellipsizeEnd) {
							sb.append(title, 0, 16).append("...");
						} else {
							sb.append(title);
						}
						sb.append(";");
						sb.append(thI.url);
						sb.append("\r");
					}
					cursor.close();
					progress+=25;
					//break; // early break test.
				}
				mayAbort.set(false);
				// 上传标签页列表
				sardine.put(PLBR_TABS_ALL, sb.toString().getBytes());
				progress+=10;
			}
			
			a.postDoneSyncing(TaskType.uploadTabs);
			//CMN.Log("exists::", sardine.exists("https://dav.jianguoyun.com/dav"));
			//sardine.put("https://dav.jianguoyun.com/dav/PLBrowser/test.txt", " hello webdav ".getBytes());


//					resources = sardine.list("https://dav.jianguoyun.com/dav/");//如果是目录一定别忘记在后面加上一个斜杠
//					for (DavResource res : resources)
//					{
//						CMN.Log("webdav--->", res);
//					}
		} catch (Exception e) {
			CMN.Log(e);
			// todo handle error
			a.postDoneSyncing(TaskType.uploadTabs);
		}
	}
	
	private void readTabsList(AtomicBoolean abort, String PLBR_TABS_ALL) throws IOException {
		if(sardine.exists(PLBR_TABS_ALL))
		{
			if(abort.get()) return;
			// 存在旧的记录
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(sardine.get(PLBR_TABS_ALL)));
			String line;
			while((line=bufferedReader.readLine())!=null) {
				line = line.trim();
				if(abort.get()) return;
				String[] arr = line.split(";");
				CMN.Log("sardine.line=", line, arr);
				if(arr.length>=4) {
					try {
						long creationTime = Long.parseLong(arr[0]);
						long last_visit_time = Long.parseLong(arr[1]);
						String title = arr[2];
						String url = arr[3];
						if(!pulledTabsTable.containsKey(creationTime)){
							TabBean tb = new TabBean(creationTime, last_visit_time, title, url);
							pulledTabs.add(tb);
							pulledTabsTable.put(creationTime, tb);
						}
					} finally { }
				}
			}
		}
	}
	
	public void pullTabs(AtomicBoolean abort, AtomicBoolean mayAbort) {
		//if(true) return;
		ArrayList<TabBean> mergedTabs = new ArrayList<>();
		pulledTabs.clear();
		pulledTabsTable.clear();
		BrowserActivity a = aRef.get();
		if(a==null) {
			return;
		}
		progressMax = a.TabHolders.size()*25+30;
		progress=10;
		try {
			if(pollSardineDirectories(sardine, webdavServer, false, pathBuilder, "/PLBrowser/", "tabs/")) {
				final String PLBR_TABS_ALL = pathBuilder.append("all.txt").toString();
				readTabsList(abort, PLBR_TABS_ALL);
				progress+=10;
				CMN.Log("tabs pulled::");
				CMN.Log(pulledTabs.toArray());
			}
		} catch (IOException e) {
			CMN.Log(e);
		}
		mayAbort.set(false);
		// merge by creation time
		for (BrowserActivity.TabHolder tbI:a.TabHolders) {
			long id = tbI.id;
			Cursor cursor = a.historyCon.getDB().rawQuery("select creation_time,last_visit_time from webtabs where id=? limit 1", new String[]{""+id});
			long creation_time = 0, last_visit_time=0;
			if(cursor.moveToNext()) {
				creation_time = cursor.getLong(0);
				last_visit_time = cursor.getLong(1);
			}
			TabBean itemInTheRecord = pulledTabsTable.remove(creation_time);
			cursor.close();
			if(itemInTheRecord==null) {
				// delete
				itemInTheRecord = new TabBean(creation_time, last_visit_time, tbI.title, tbI.url);
				itemInTheRecord.type = -1;
				itemInTheRecord.selected=false;
				// todo verify
			} else {
				if(itemInTheRecord.token!=last_visit_time) {
					// 入库时间不同
					itemInTheRecord.selected=itemInTheRecord.token>last_visit_time;
					itemInTheRecord.type = itemInTheRecord.selected?2:3;
				}
				// TabBean.type==0 means no change
			}
			itemInTheRecord.id = id;
			mergedTabs.add(itemInTheRecord);
		}
		int cc=0;
		for (TabBean ptI:pulledTabs) {
			if(pulledTabsTable.get(ptI.creation)!=null) {
				// create new
				Cursor cursor = a.historyCon.getDB().rawQuery("select id from webtabs where creation_time=? limit 1", new String[]{""+ptI.creation});
				if(cursor.moveToNext()) {
					ptI.id = cursor.getLong(0);
				}
				cursor.close();
				ptI.type=1;
				ptI.selected=true;
				mergedTabs.add(Math.min(cc, mergedTabs.size()), ptI);
			}
			cc++;
		}
		this.mergedTabs = mergedTabs;
		a.postSelectSyncTabs();
	}
	
	public void syncTabs(AtomicBoolean abort, AtomicBoolean mayAbort) {
		ArrayList<TabBean> mergedTabs = this.mergedTabs;
		progressMax = mergedTabs.size()*25+30;
		progress=30;
		BrowserActivity a = aRef.get();
		if(a==null) {
			return;
		}
		if(a.checkWebViewDirtyMap.size()>0) {
			WebFrameLayout[] arr = a.checkWebViewDirtyMap.toArray(new WebFrameLayout[]{});
			a.checkWebViewDirtyMap.clear();
			for(WebFrameLayout wfl:arr) {
				wfl.saveIfNeeded();
			}
		}
		ArrayList<BrowserActivity.TabHolder> newHolders = new ArrayList<>((int)(mergedTabs.size()*1.5));
		
		int LEN_PLBR_TABS = 0;
		ReusabeBufferedInputStream input=null;
		Bundle bundle = new Bundle();
		try {
			if(pollSardineDirectories(sardine, webdavServer, false, pathBuilder, "/PLBrowser/", "tabs/")) {
				LEN_PLBR_TABS = pathBuilder.length();
			}
			input = new ReusabeBufferedInputStream(null);
		} catch (IOException e) {
			CMN.Log(e);
		}
		int deltaProgress;
		HashMap<Long, BrowserActivity.TabHolder> allTabsMap = new HashMap<>();
		for(BrowserActivity.TabHolder tbI:a.closedTabs) {
			allTabsMap.put(tbI.id, tbI);
		}
		for(BrowserActivity.TabHolder tbI:a.TabHolders) {
			allTabsMap.put(tbI.id, tbI);
		}
		for(TabBean tbI:mergedTabs) {
			if(abort.get()) {
				return;
			}
			BrowserActivity.TabHolder tabI = null;
			WebFrameLayout weblayout = a.id_table.get(tbI.id);
			if(weblayout!=null) {
				tabI = weblayout.holder;
			}
			if(tabI==null) {
				tabI = allTabsMap.get(tbI.id);
			}
			deltaProgress=25;
			if(tbI.type==1) {
				CMN.Log("同步中...", tbI.selected, tbI.type, tbI);
				CMN.Log("同步中???", tabI);
			}
			if(tbI.type<0) { // 删除
				if(tabI!=null) {
					if(tbI.selected) {
						a.setTabClosed(tabI);
					} else {
						newHolders.add(tabI);
					}
				}
			}
			else if(tbI.type==0) { // 不变
				if(tabI!=null) {
					newHolders.add(tabI);
				}
			}
			else { // 1 新建	2/3 改
				if(tbI.selected) {
					deltaProgress=0;
					long creationTime = tbI.creation;
					if(tabI==null) {
						tabI = new BrowserActivity.TabHolder();
					}
					if(tabI.id==0) {
						tabI.id=tbI.id;
					}
					if(tabI.id==0) {
						tabI.id = a.historyCon.insertNewTab(tbI.url, creationTime);
					}
					ContentValues values = new ContentValues();
					values.put("url", tbI.url);
					values.put("creation_time", creationTime);
					values.put("last_visit_time", tbI.token);
					progress+=10;
					if(input!=null) {
						pathBuilder.setLength(LEN_PLBR_TABS);
						try{
							InputStream stream = sardine.get(pathBuilder.append("tab_")
									.append(creationTime)
									.append(".bin").toString());
							//input.reuse(stream);
							input = input.reconstruct(stream);
							//input = new ReusabeBufferedInputStream(stream);
							bundle.clear();
							webStacksWriterSer.readStream(bundle, input);
							if(tbI.token==0)values.put("last_visit_time", bundle.getLong("last_visit_time", 0));
							if(creationTime==0)values.put("creation_time", bundle.getLong("creation_time", 0));
							values.put("f1", tbI.flag=bundle.getLong("f1", 0));
							values.put("favor", bundle.getLong("favor", 0));
							values.put("visit_count", bundle.getLong("visit_count", 0));
							values.put("thumbnail", bundle.getByteArray("thumbnail"));
							values.put("webstack", bundle.getByteArray("webstack"));
							values.put("url", bundle.getString("url", tbI.url));
							values.put("title", bundle.getString("title", tbI.title));
							values.put("ext1", bundle.getString("ext1", null));
							stream.close();
						} catch (Exception e) {
							CMN.Log(e);
						}
					}
					a.historyCon.getDB().update("webtabs", values, "id=?", new String[]{""+tabI.id});
					progress+=15;
					tabI.url = tbI.url;
					tabI.title = tbI.title;
					tabI.flag = tbI.flag;
					Integer ver = WebPic.versionMap.remove(tabI.id); // 虚高？
					tabI.version = ver==null?0:ver;
					tabI.version++;
					tabI.lastCaptureVer = tabI.lastSaveVer = tabI.version;
					if(tabI.last_visit_time!=tbI.token) {
						tabI.onSalvaged();
					}
					newHolders.add(tabI);
				} else { // 不变
					if(tbI.type>1) {
						newHolders.add(tabI);
					}
				}
			}
			progress+=deltaProgress;
		}
		allTabsMap.clear();
		if(abort.get()) {
			return;
		}
		mayAbort.set(false);
		a.TabHolders = newHolders;
		CMN.Log("标签页同步完毕::"); CMN.Log(newHolders.toArray());
		a.postDoneSyncing(TaskType.syncTabs);
	}
	
	private boolean pollSardineDirectories(Sardine sardine, String path, boolean create, StringBuilder pathBuilder, String...parts) throws IOException {
		int length = path.length();
		pathBuilder.setLength(length);
		for(String sI:parts) {
			pathBuilder.append(sI);
		}
		if(sardine.exists(pathBuilder.toString())) {
			return true;
		}
		if(!create) {
			return false;
		}
		pathBuilder.setLength(length);
		for(String sI:parts) {
			pathBuilder.append(sI);
			path = pathBuilder.toString();
			sardine.createDirectory(path);
			if(!sardine.exists(path)) {
				return false;
			}
		}
		return parts.length>0;
	}
	
}
