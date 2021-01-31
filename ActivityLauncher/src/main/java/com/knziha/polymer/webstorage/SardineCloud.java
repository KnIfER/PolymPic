package com.knziha.polymer.webstorage;

import android.database.Cursor;
import android.os.Bundle;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.BufferedReader;
import com.knziha.polymer.Utils.CMN;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.knziha.polymer.AdvancedBrowserWebView.webStacksWriterSer;

public class SardineCloud implements Runnable {
	OkHttpSardine sardine;
	Thread t;
	StringBuilder pathBuilder;
	public ArrayList<TabBean> pulledTabs = new ArrayList<>();
	public ArrayList<TabBean> mergedTabs = new ArrayList<>();
	HashMap<Long, TabBean> pulledTabsTable = new HashMap<>();
	WeakReference<BrowserActivity> aRef = new WeakReference<>(null);
	volatile boolean threadSuspended;
	private AtomicBoolean abort;
	private AtomicBoolean mayAbort;
	int progress;
	int progressMax;
	
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
		if(aRef.get()==null&&a!=null) {
			aRef = new WeakReference<>(a);
		}
		task = TaskType.idle;
		if(t==null) {
			task = taskType;
			t = new Thread(this);
			sardine = new OkHttpSardine(username, password, false);
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
			notify();
		}
	}
	
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (t==thisThread) {
			runningTask = task;
			mayAbort = new AtomicBoolean(true);
			abort = new AtomicBoolean();
			handleTask(abort, mayAbort);
			abort.set(true);
			threadSuspended = true;
			try {
				Thread.sleep(1000);
				if(threadSuspended) {
					synchronized(this) {
						while (threadSuspended && t!=null)
							wait();
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
		public long id;
		public int type;
		TabBean(long creation, long token, String title) {
			this.creation = creation;
			this.token = token;
			this.title = title;
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
						sb.append("\n");
					}
					cursor.close();
					progress+=25;
					break;
				}
				mayAbort.set(false);
				// 上传标签页列表
				sardine.put(PLBR_TABS_ALL, sb.toString().getBytes());
				progress+=10;
			}
			
			
			//CMN.Log("exists::", sardine.exists("https://dav.jianguoyun.com/dav"));
			//sardine.put("https://dav.jianguoyun.com/dav/PLBrowser/test.txt", " hello webdav ".getBytes());


//					resources = sardine.list("https://dav.jianguoyun.com/dav/");//如果是目录一定别忘记在后面加上一个斜杠
//					for (DavResource res : resources)
//					{
//						CMN.Log("webdav--->", res);
//					}
		} catch (Exception e) {
			CMN.Log(e);
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
				if(abort.get()) return;
				String[] arr = line.split(";");
				if(arr.length>=3) {
					try {
						long creationTime = Long.parseLong(arr[0]);
						long last_visit_time = Long.parseLong(arr[1]);
						String title = arr[2];
						if(!pulledTabsTable.containsKey(creationTime)){
							TabBean tb = new TabBean(creationTime, last_visit_time, title);
							pulledTabs.add(tb);
							pulledTabsTable.put(creationTime, tb);
						}
					} finally { }
				}
			}
		}
	}
	
	public void pullTabs(AtomicBoolean abort, AtomicBoolean mayAbort) {
		mergedTabs.clear();
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
			Cursor cursor = a.historyCon.getDB().rawQuery("select * from webtabs where id=? limit 1", new String[]{""+id});
			TabBean itemInTheRecord = pulledTabsTable.remove(tbI.creation);
			long last_visit_time = cursor.moveToNext()?cursor.getLong(cursor.getColumnIndex("last_visit_time")):0;
			cursor.close();
			if(itemInTheRecord==null) {
				// delete
				itemInTheRecord = new TabBean(tbI.creation, last_visit_time, tbI.title);
				itemInTheRecord.type = -1;
				// todo verify
			} else {
				itemInTheRecord.id = id;
				if(itemInTheRecord.token!=last_visit_time) {
					// 入库时间不同
					itemInTheRecord.type = itemInTheRecord.token>last_visit_time?2:3;
				}
				// TabBean.type==0 means no change
			}
			mergedTabs.add(itemInTheRecord);
		}
		int cc=0;
		for (TabBean ptI:pulledTabs) {
			if(pulledTabsTable.get(ptI.creation)!=null) {
				// create new
				ptI.type=1;
				mergedTabs.add(Math.min(cc, mergedTabs.size()), ptI);
			}
			cc++;
		}
		a.postSelectSyncTabs();
	}
	
	public void syncTabs(AtomicBoolean abort, AtomicBoolean mayAbort) {
	
	}
	
	private boolean pollSardineDirectories(Sardine sardine, String path, boolean create, StringBuilder pathBuilder, String...parts) throws IOException {
		for(String sI:parts) {
			pathBuilder.append(sI);
		}
		if(sardine.exists(pathBuilder.toString())) {
			return true;
		}
		if(!create) {
			return false;
		}
		pathBuilder.setLength(path.length());
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
