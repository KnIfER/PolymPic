package com.knziha.polymer.browser;

import android.database.Cursor;

import com.knziha.polymer.Utils.CMN;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class BrowseTaskExecuter implements Runnable{
	final WeakReference<BrowseActivity> aRef;
	Queue<Long> taskQueue = new ConcurrentLinkedQueue<>();
	Set<Long> taskSet = Collections.synchronizedSet(new HashSet<>());
	Thread t;
	
	boolean finished;
	volatile AtomicBoolean token;
	final AtomicBoolean abort = new AtomicBoolean();
	final AtomicBoolean acquired = new AtomicBoolean();
	final AtomicBoolean ASleeping = new AtomicBoolean();
	
	
	public BrowseTaskExecuter(BrowseActivity a) {
		aRef = new WeakReference<>(a);
	}
	
	@Override
	public void run() {
		BrowseActivity a = aRef.get();
		if(a!=null) {
			if(finished) {
			
			} else {
				Long item;
				while((item = taskQueue.poll())!=null || acquired.get()) {
					if(item!=null) {
						taskSet.remove(item);
						Cursor cursor = a.tasksDB.getCursorByRowID(item);
						DownloadTask task = null;
						if(cursor.getCount()>=1) {
							cursor.moveToFirst();
							task = a.startTaskForDB(cursor);
						}
						cursor.close();
						if(task!=null) {
							AtomicBoolean token = this.token = task.abort;
							boolean interrupted=false;
							try {
								CMN.Log("等待2.5min");
								// 等待2.5min, 这是 webStation 的占用时限。
								Thread.sleep((long) (1000*60*task.maxWaitTime));
							} catch (InterruptedException e) {
								// 中断，放弃任务。
								interrupted = true;
							}
							CMN.Log("等待2.5min over");
							if(!interrupted) { //timeout
								token.set(true);
								if(taskQueue.size()==0) {
									a.clearWebview();
								} else {
									a.stopWebView();
								}
								a.respawnTask(task.id);
							}
							this.token = null;
						}
						// 执行下一任务直至清空表
					}
					if(abort.get()) {
						break;
					}
					if(item==null||taskQueue.peek()==null) {
						try {
							ASleeping.set(true);
							Thread.sleep(1000*60);
						} catch (InterruptedException e) {
							ASleeping.set(false);
						}
					}
					if(abort.get()) {
						break;
					}
				}
				boolean normalExit = abort.getAndSet(finished=true);
				if(normalExit) {
					a.clearWebview();
				}
				// 退出线程。
			}
		}
	}
	
	
	public boolean acquire() {
		if(abort.get()) {
			return false;
		}
		acquired.set(true);
		if(abort.get()) {
			acquired.set(false);
		} else {
//			if(ASleeping.get()) {
//				interrupt();
//				ASleeping.set(false);
//			}
			return true;
		}
		return false;
	}
	
	public void dequire() {
		acquired.set(false);
	}
	
	public void run(BrowseActivity a, long rowID) {
		DownloadTask runningAnalogue = a.taskMap.get(rowID);
		if(runningAnalogue!=null) {
			runningAnalogue.abort();
			a.taskMap.remove(rowID);
		}
		if(taskSet.contains(rowID)) {
			return;
		}
		taskQueue.add(rowID);
		taskSet.add(rowID);
	}
	
	public void start() {
		if(t==null) {
			t = new Thread(this);
			t.start();
		} else {
			acquired.set(false);
			if(ASleeping.get()) {
				interrupt();
				ASleeping.set(false);
			}
		}
	}
	
	
	public void stop() {
		CMN.Log("stopping executor!!!");
		abort.set(true);
		if(t!=null) {
			t.interrupt();
		}
		finished=true;
		aRef.clear();
	}
	
	public boolean inQueue(long rowID) {
		return taskSet.contains(rowID);
	}
	
	public void interrupt() {
		if(t!=null) {
			t.interrupt();
		}
	}
}
