package com.KnaIvER.polymer;

import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.KnaIvER.polymer.Utils.CMN;
import com.KnaIvER.polymer.Utils.Options;
import com.KnaIvER.polymer.widgets.MovableFloatingView;

public abstract class FloatingService extends Service implements OnTouchListener {
	public MovableFloatingView mview;
	private static int notificationId = 2;
	private static Notification notification;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void onCreate() {
		super.onCreate();

		postCreate();
	}

	void postCreate() {
		CMN.opt = new Options(this);
		CMN.opt.dm = getResources().getDisplayMetrics();
		mview = new MovableFloatingView(getFloatingView());
		mview.init((WindowManager) getSystemService(WINDOW_SERVICE), (int) (59*CMN.opt.dm.heightPixels*1.0/64));
		if (notification == null) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
			notification = builder.build();
		}
	}

	protected abstract View getFloatingView();

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
		mview.removeView();
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
