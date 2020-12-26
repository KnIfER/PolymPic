package com.knziha.polymer;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.SimpleService;


public class LauncherActivity extends Activity {
	private long FFStamp;
	public static LauncherActivity l;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(true){
			if(l!=null) l.finish();
			l = this;
			Intent intent = new Intent(this, SimpleService.class);
			ServiceConnection mConnection = new ServiceConnection() {
				@Override
				public void onServiceConnected(ComponentName name, IBinder service) {
					CMN.Log("onServiceConnected");
					finish();
				}
				@Override
				public void onServiceDisconnected(ComponentName name) { }
			};
			bindService(intent, mConnection, BIND_AUTO_CREATE);
			startService(intent);
			return;
		}
		setContentView(R.layout.service_main);
	}
}
