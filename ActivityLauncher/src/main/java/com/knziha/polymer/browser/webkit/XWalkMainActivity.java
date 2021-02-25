package com.knziha.polymer.browser.webkit;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.ViewGroup;

import com.knziha.polymer.R;

import org.xwalk.core.XWalkActivityDelegate;
import org.xwalk.core.XWalkView;

public class XWalkMainActivity extends Activity {
	private XWalkView mXWalkView;
	private ViewGroup root;
	
	private XWalkActivityDelegate mActivityDelegate;
	
	protected void onXWalkReady() {
		
		mXWalkView = new XWalkView(this);
		root.addView(mXWalkView);
		
		mXWalkView.load("http://www.baidu.com/", null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		root = findViewById(R.id.root);
		Runnable cancelCommand = new Runnable() {
			public void run() {
				finish();
			}
		};
		Runnable completeCommand = new Runnable() {
			public void run() {
				onXWalkReady();
			}
		};
		
		this.mActivityDelegate = new XWalkActivityDelegate(this, cancelCommand, completeCommand);
	
	}
	
	protected void onResume() {
		super.onResume();
		this.mActivityDelegate.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}
	
}
