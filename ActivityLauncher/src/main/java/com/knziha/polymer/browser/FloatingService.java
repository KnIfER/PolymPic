package com.knziha.polymer.browser;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import androidx.core.app.NotificationCompat;

import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.MovableFloatingView;

public abstract class FloatingService extends Service implements OnTouchListener {
	public MovableFloatingView mview;
	private static int notificationId = 202342;
	private static Notification notification;
	
	Options opt;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		opt = new Options(this);
		opt.dm = getResources().getDisplayMetrics();
		opt.getSecondFlag();
		opt.getThirdFlag();
		mview = new MovableFloatingView(getContentView());
		mview.init((WindowManager) getSystemService(WINDOW_SERVICE), (int) (59*opt.dm.heightPixels*1.0/64));
		if (notification == null) {
			String CHANNEL_ID = "com.knziha.proservis";
			String CHANNEL_NAME = "vis";
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
				NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.createNotificationChannel(notificationChannel);
			}
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
			notification = builder.build();
		}
	}
	
	protected abstract View getContentView();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		mview.updateViewPosition();
		startForeground(notificationId, notification);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mview!=null) {
			mview.removeView();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (v != null) {
				v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
			}
		}
		return false;
	}
}
