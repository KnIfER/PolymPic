package com.knaiver.polymer;


import android.app.Activity;
import android.os.Bundle;

import com.knaiver.polymer.Utils.CMN;
import com.knaiver.polymer.Utils.Options;


public class LauncherActivity extends Activity {
	private long FFStamp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(CMN.opt==null){
			CMN.opt = new Options(getApplicationContext());
			CMN.opt.dm = getResources().getDisplayMetrics();
		}
//		if(false && CMN.opt.getLaunchServiceLauncher()){
//			startService(new Intent(getApplicationContext(), SimpleService.class));
//			finish();
//			return;
//		}
		setContentView(R.layout.service_main);


	}

}
