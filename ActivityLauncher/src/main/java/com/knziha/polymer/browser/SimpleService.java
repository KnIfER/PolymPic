package com.knziha.polymer.browser;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.widgets.SplitView;

import java.io.FileDescriptor;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.knziha.polymer.LauncherActivity.l;

public class SimpleService extends FloatingService implements OnClickListener {
	ViewGroup root;

	private EditText etSearch;
	
	String servis_f1;
	String servis_f2;
	String servis_f3;
	
	@Override
	public void onCreate() {
		super.onCreate();
		servis_f1 = opt.defaultReader.getString("servis_f1", null);
		servis_f2 = opt.defaultReader.getString("servis_f2", null);
		servis_f3 = opt.defaultReader.getString("servis_f3", null);
		if(opt.getLaunchView()) {
			LaunchView();
		}
		if(l!=null) l.finish();
	}
	
	public Intent fillIntentByField(String field) {
		if(TextUtils.isEmpty(field)) {
			return new Intent();
		}
		Intent intent=new Intent();
		ComponentName cn = null;
		String[] arr = field.split(";");
		String arrI;
		String[] arr2;
		for (int i = 0; i < arr.length; i++) {
			arrI = arr[i];
			arr2 = arrI.split("/");
			if(arr2.length==2) {
				if(cn==null) {
					cn = new ComponentName(arr2[0], arr2[1]);
					intent.setComponent(cn);
				} else {
					intent.putExtra(arr2[0], arr2[1]);
				}
			} else if(arr2.length==1) {
				intent.setAction(arrI);
			}
		}
		CMN.Log("fillIntentByField", intent);
		return intent;
	}
	
	private void LaunchView() {
		Intent intent=fillIntentByField(servis_f1);
		intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
		try {
			startActivity(intent);
		} catch (Exception e) {
			CMN.Log(e);
			Toast.makeText(getApplicationContext(),"2"+e, Toast.LENGTH_LONG).show();
		}
	}
	
	
	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			int what = msg.what;
			switch(what){
				case 2:
					Intent intent1=fillIntentByField(servis_f2);
					try {
						stopService(intent1);
					} catch (Exception e) {
						CMN.Log(e);
					}
					root.performLongClick();
					break;
			}
			return false;
		}
	});
	
	private void doMyTask(boolean nxt) {
		Intent intent1=fillIntentByField(servis_f2);
		ComponentName ret = null;
		try {
			ret = startService(intent1);
		} catch (Exception e) {
			CMN.Log(e);
		}
		CMN.Log("startService doMyTask::", ret);
		if(ret==null && nxt) {
			Intent intent2=fillIntentByField(servis_f3)
					.addFlags(FLAG_ACTIVITY_NEW_TASK);
			try {
				startActivity(intent2);
			} catch (Exception e) {
				CMN.Log(e);
			}
			root.postDelayed(() -> doMyTask(false), 250);
		}
		if(ret!=null) {
			root.postDelayed(() -> {
				try {
					startService(intent1);
				} catch (Exception e) {
					CMN.Log(e);
				}
				Exit();
				System.exit(0);
			}, 600);
		}
	}
	
	@Override
	protected View getContentView() {
		root = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.service_main, null);
		init();
		Toast.makeText(getApplicationContext(),"2"+ Build.MANUFACTURER, Toast.LENGTH_LONG).show();
		return root;
	}


	@SuppressLint("ClickableViewAccessibility")
	private void init() {
		if(root instanceof SplitView){
			final SplitView spv = ((SplitView) root);
			spv.setPageSliderInf(new SplitView.PageSliderInf() {
				@Override public void onPreparePage(int orgSize) {

				}
				@Override public void onMoving(SplitView webcontentlist, float val) {

				}
				@Override public void onPageTurn(SplitView webcontentlist) {

				}
				@Override public void onHesitate() {

				}
				@Override public void SizeChanged(int newSize, float delta) {

				}
				@Override public void onDrop(int size) {

				}
				@Override
				public int preResizing(int size) {
					// Math.min(getResources().getDisplayMetrics().heightPixels-spv.getHandle().getHeight(), size)
					return Math.max(getResources().getDisplayMetrics().heightPixels/2, Math.min(getResources().getDisplayMetrics().heightPixels-spv.getHandle().getHeight(), size));
				}
			});
		}
		root.findViewById(R.id.exit_btn).setOnClickListener(this);
		root.findViewById(R.id.open_btn).setOnClickListener(this);
		root.findViewById(R.id.bottombar).setOnClickListener(this);
		root.findViewById(R.id.logo).setOnClickListener(this);
		etSearch = root.findViewById(R.id.etSearch);
		etSearch.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId==EditorInfo.IME_ACTION_SEARCH) {
				String text = v.getText().toString();
				String trimmedText = text.trim();
				if(!TextUtils.isEmpty(trimmedText)) {
					char c0 = trimmedText.charAt(0);
					if(trimmedText.length()==1) {
						if(servis_f2!=null && c0=='x') {
							doMyTask(true);
							return false;
						}
						if(servis_f1!=null && c0=='z') {
							if(opt.toggleLaunchView()) {
								LaunchView();
							}
							opt.putSecondFlag();
							return false;
						}
					}
					Intent intent = new Intent().setClassName("com.knziha.polymer", "com.knziha.polymer.BrowserActivity")
						.addFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
						.putExtra(Intent.EXTRA_TEXT, trimmedText)
						.setAction(Intent.ACTION_MAIN);
					startActivity(intent);
					root.postDelayed(this::Exit, 500);
				} else {
				
				}
			}
			return false;
		});
		handler.sendEmptyMessageDelayed(2, 500);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	private void Exit() {
		stopService(new Intent(getApplicationContext(), SimpleService.class));
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

	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.exit_btn:
				Exit();
			break;
			case R.id.logo:{
			
			} break;
			case R.id.bottombar:{
			
			} break;
		}
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new IBinder(){
			@Nullable
			@Override
			public String getInterfaceDescriptor() throws RemoteException {
				return null;
			}
			
			@Override
			public boolean pingBinder() {
				return false;
			}
			
			@Override
			public boolean isBinderAlive() {
				return false;
			}
			
			@Nullable
			@Override
			public IInterface queryLocalInterface(@NonNull String descriptor) {
				return null;
			}
			
			@Override
			public void dump(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
			
			}
			
			@Override
			public void dumpAsync(@NonNull FileDescriptor fd, @Nullable String[] args) throws RemoteException {
			
			}
			
			@Override
			public boolean transact(int code, @NonNull Parcel data, @Nullable Parcel reply, int flags) throws RemoteException {
				return false;
			}
			
			@Override
			public void linkToDeath(@NonNull DeathRecipient recipient, int flags) throws RemoteException {
			
			}
			
			@Override
			public boolean unlinkToDeath(@NonNull DeathRecipient recipient, int flags) {
				return false;
			}
		};
	}
}


