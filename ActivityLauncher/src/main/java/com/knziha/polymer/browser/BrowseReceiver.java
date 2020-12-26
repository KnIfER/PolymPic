package com.knziha.polymer.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.knziha.polymer.Utils.CMN;

public class BrowseReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		CMN.Log("接收到任务：", intent);
		//	alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TIME_INTERVAL, pendingIntent);
		int task = intent.getIntExtra("task", -1);
		if(task!=-1) {
			CMN.Log("接收到任务：", task, CMN.id(this));
		}
	}
}
