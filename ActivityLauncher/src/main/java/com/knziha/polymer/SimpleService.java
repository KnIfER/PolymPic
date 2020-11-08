package com.knziha.polymer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.knziha.polymer.browser.BrowseActivity;
import com.knziha.polymer.widgets.SplitView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class SimpleService extends FloatingService implements OnClickListener {
	ViewGroup root;


	@Override
	public void onCreate() {
		super.onCreate();

	}

	@Override
	protected View getFloatingView() {
		root = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.service_main, null);
		init();
		return root;
	}


	@SuppressLint("ClickableViewAccessibility")
	private void init() {
		if(root instanceof SplitView){
			final SplitView spv = ((SplitView) root);
			spv.setPageSliderInf(new SplitView.PageSliderInf() {
				@Override
				public void onPreparePage(int orgSize) {

				}

				@Override
				public void onMoving(SplitView webcontentlist, float val) {

				}

				@Override
				public void onPageTurn(SplitView webcontentlist) {

				}

				@Override
				public void onHesitate() {

				}

				@Override
				public void SizeChanged(int newSize, float delta) {

				}

				@Override
				public void onDrop(int size) {

				}

				@Override
				public int preResizing(int size) {
					return Math.max(getResources().getDisplayMetrics().heightPixels/2, Math.min(getResources().getDisplayMetrics().heightPixels-spv.getHandle().getHeight(), size));
				}
			});
		}

		root.findViewById(R.id.exit_btn).setOnClickListener(this);

		root.findViewById(R.id.open_btn).setOnClickListener(this);

		root.findViewById(R.id.bottombar).setOnClickListener(this);

		root.findViewById(R.id.logo).setOnClickListener(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		init();
		return super.onStartCommand(intent, flags, startId);
	}

	private void Exit() {
		Intent intent = new Intent(getApplicationContext(), SimpleService.class);
		stopService(intent);
		System.exit(0);
	}

	void openUrlInChrome(String url) {
		try {
			try {
				Uri uri = Uri.parse("googlechrome://navigate?url="+ url);
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				i.addFlags(FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			} catch (ActivityNotFoundException e) {
				Uri uri = Uri.parse(url);
				// Chrome is probably not installed
				// OR not selected as default browser OR if no Browser is selected as default browser
				Intent i = new Intent(Intent.ACTION_VIEW, uri);
				i.addFlags(FLAG_ACTIVITY_NEW_TASK);
				startActivity(i);
			}
		} catch (Exception ignored) {
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.exit_btn:
				Exit();
			break;
			case R.id.open_btn:{
				Intent intent1=new Intent();
				AlertDialog d = new AlertDialog.Builder(SimpleService.this).setMessage("asdasd").create();
				d.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
				d.show();


				ComponentName cn=new ComponentName("com.mycompany.myapp2",
						"com.mycompany.myapp2.MainActivity");
				intent1.setComponent(cn);

				DisplayMetrics dm = getResources().getDisplayMetrics();
				Toast.makeText(getApplicationContext(),dm.heightPixels+"timer", Toast.LENGTH_LONG).show();
				intent1.setAction("darkerpro.START");
				cn=new ComponentName("com.mlhg.screenfilterpro",
						"com.mlhg.screenfilterpro.OverlayService");
				intent1.setComponent(cn);
				//startService(intent1);

			} break;
			case R.id.bottombar:{
				Intent intent1=new Intent();
				intent1.setAction("darkerpro.START");
				ComponentName cn=new ComponentName("com.mlhg.screenfilterpro", "com.mlhg.screenfilterpro.OverlayService");
				intent1.setComponent(cn);
				startService(intent1);
				Intent intent = new Intent(getApplicationContext(), SimpleService.class);
				stopService(intent);
				System.exit(0);
			} break;
			case R.id.logo:{
				startActivity(
						new Intent(this, BrowseActivity.class)
								.addFlags(FLAG_ACTIVITY_NEW_TASK)
				);
				mview.removeView();
				Intent intent = new Intent(getApplicationContext(), SimpleService.class);
				stopService(intent);
			} break;
		}
	}
}


