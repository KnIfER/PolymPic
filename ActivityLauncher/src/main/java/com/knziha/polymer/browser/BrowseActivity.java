package com.knziha.polymer.browser;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.util.Util;
import com.google.android.material.appbar.AppBarLayout;
import com.jess.ui.TwoWayAdapterView;
import com.jess.ui.TwoWayGridView;
import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.model.DialogSelectionListener;
import com.knziha.filepicker.view.FilePickerDialog;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebViewImplExt;
import com.knziha.polymer.browser.webkit.XPlusWebView;
import com.knziha.polymer.databinding.BrowseMainBinding;
import com.knziha.polymer.databinding.TaskItemsBinding;
import com.knziha.polymer.toolkits.MyX509TrustManager;
import com.knziha.polymer.widgets.DescriptiveImageView;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.WebFrameLayout;
import com.tencent.smtt.sdk.QbSdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import static com.knziha.polymer.browser.BrowseFieldHandler.dateToStr;
import static com.knziha.polymer.widgets.Utils.RequsetUrlFromCamera;

/** BrowseActivity Is Not The Browser. It is a work station for resource sniffers.
 * 		The extraction logic is extracted and is not included.
 * 		It is extracted as a field in the db.	*/
public class BrowseActivity extends Toastable_Activity
		implements View.OnClickListener, View.OnLongClickListener {
	BrowseMainBinding UIData;
	
	UniversalWebviewInterface webview_Player;
	
	WebBrowseListener listener;
	
	BrowseTaskExecutor taskExecutor;
	private File download_path;
	private boolean autoRefreshing;
	private SharedPreferences preference;
	
	static {
		try {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				WebView.setDataDirectorySuffix("st");
			}
			com.tencent.smtt.sdk.WebView.setDataDirectorySuffix("stx");
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	private long deletingRow;
	private boolean isViewDirty;
	String SearchText;
	private LinearLayoutManager layoutManager;
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if(hasFocus&&isViewDirty) {
			CMN.Log("isViewDirty updated...");
			updateTaskList();
			isViewDirty=false;
		}
	}
	
	private void initX5WebStation() throws IOException {
		QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
			@Override
			public void onViewInitFinished(boolean arg0) {
				// TODO Auto-generated method stub
				//x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
				CMN.Log( " onViewInitFinished is " + arg0);
			}
			@Override
			public void onCoreInitFinished() {
				// TODO Auto-generated method stub
			}
		};
		//x5内核初始化接口
		QbSdk.initX5Environment(getApplicationContext(),  cb);
		CMN.Log("initX5WebStation");
		WebFrameLayout layout = new WebFrameLayout(this, new BrowserActivity.TabHolder());
		layout.appBarLayout = new AppBarLayout(this);
		webview_Player = new XPlusWebView(BrowseActivity.this);
		layout.appBarLayout = new AppBarLayout(this);
		webview_Player.setLayoutParent(layout, true);
		Utils.addViewToParent(layout, UIData.webStation);
		listener = new WebBrowseListener(BrowseActivity.this, webview_Player);
	}
	
	private void initStdWebStation() {
		WebFrameLayout layout = new WebFrameLayout(this, new BrowserActivity.TabHolder());
		webview_Player = new WebViewImplExt(this);
		layout.appBarLayout = new AppBarLayout(this);
		webview_Player.setLayoutParent(layout, true);
		Utils.addViewToParent(layout, UIData.webStation);
		listener = new WebBrowseListener(this, webview_Player);
	}
	
	BrowseDBHelper tasksDB;
	
	AlarmManager alarmManager;
	
	private String task_action = "knziha.task";
	
	private TextPaint menu_grid_painter;
	
	BrowseFieldHandler editHandler;
	
	FileOutputStream fout;
	private static BrowseReceiver receiver;
	private EnchanterReceiver locationReceiver;
	
	ArrayList<String> menuList = new ArrayList<>();
	RecyclerView.Adapter adapter;
	private Cursor cursor = Utils.EmptyCursor;
	
	DisplayMetrics dm;
	private int selectionPos;
	private long selectionRow;
	
	Map<Long, DownloadTask> taskMap = Collections.synchronizedMap(new HashMap<>());
	Map<Long, AtomicBoolean> runningMap = Collections.synchronizedMap(new HashMap<>());
	Map<Long, Integer> lifesMap = Collections.synchronizedMap(new HashMap<>());
	Map<Long, ScheduleTask> scheduleMap = Collections.synchronizedMap(new HashMap<>());
	Map<Long, PendingIntent> intentMap = Collections.synchronizedMap(new HashMap<>());
	Map<Long, ViewHolder> viewMap = new HashMap<>();
	
	Random random = new Random();
	
	public boolean MainMenuListVis;
	
	AppHandler appHandler = new AppHandler(this);
	
	static class AppHandler extends Handler{
		final WeakReference<BrowseActivity> aRef;
		
		AppHandler(BrowseActivity a) {
			this.aRef = new WeakReference<>(a);
		}
		
		@Override
		public void handleMessage(@NonNull Message msg) {
			super.handleMessage(msg);
			BrowseActivity a = aRef.get();
			if(a!=null)
			if(msg.what==110) {
				removeMessages(110);
				a.adapter.notifyDataSetChanged();
				if(a.autoRefreshing) {
					sendEmptyMessageDelayed(110, 900);
				}
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		if(editHandler!=null&&editHandler.getVis()) {
			editHandler.setVis(false);
		} else if(MainMenuListVis) {
			setMainMenuListVis(false);
		} else {
			super.onBackPressed();
		}
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		UIData = DataBindingUtil.setContentView(this, R.layout.browse_main);
		preference = getSharedPreferences("browse", 0);
		root = UIData.root;
		if(autoRefreshing = preference.getBoolean("autoRefresh", false)) {
			updateRefreshBtn();
		}
		
		String path_download = preference.getString("path", null);
		if(path_download!=null) {
			download_path = new File(path_download);
			download_path.mkdir();
			if(!download_path.exists()) {
				download_path = null;
			}
		}
		if(download_path==null) {
			download_path = getExternalFilesDir(null);
		}
		try {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
		} catch (Exception e) {
			CMN.Log(e);
		}
		Resources mResources = getResources();
		dm = mResources.getDisplayMetrics();
		GlobalOptions.density = dm.density;
		menuList.add("开始/停止");
		menuList.add("附加字段");
		menuList.add("新增项目");
		menuList.add("下载至…");
		menuList.add("计划任务");
		menuList.add("编辑命令");
		menuList.add("删除");
		menu_grid_painter = DescriptiveImageView.createTextPainter();
		TwoWayGridView mainMenuLst = UIData.mainMenuLst;
		mainMenuLst.setHorizontalSpacing(0);
		mainMenuLst.setVerticalSpacing(0);
		mainMenuLst.setHorizontalScroll(true);
		mainMenuLst.setStretchMode(GridView.NO_STRETCH);
		MenuAdapter menuAda = new MenuAdapter();
		mainMenuLst.setAdapter(menuAda);
		mainMenuLst.setOnItemClickListener(menuAda);
		mainMenuLst.setScrollbarFadingEnabled(false);
		mainMenuLst.setSelector(mResources.getDrawable(R.drawable.listviewselector0));
		mainMenuLst.post(() -> mainMenuLst.setTranslationY(mainMenuLst.getHeight()));
		if (true) {
			startService(new Intent(this, ServiceEnhancer.class));
			locationReceiver = new EnchanterReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction("plodlock");
			registerReceiver(locationReceiver, filter);
		}
		tasksDB = BrowseDBHelper.connectInstance(this);
		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		RecyclerView recyclerView = UIData.tasks;
		recyclerView.setLayoutManager(layoutManager=new LinearLayoutManager(this));
		recyclerView.setItemAnimator(null);
		recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
		recyclerView.setHasFixedSize(true);
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		recyclerView.setAdapter(adapter = new RecyclerView.Adapter() {
			@NonNull
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				return new ViewHolder(TaskItemsBinding.inflate(layoutInflater, parent, false));
			}
			
			@Override
			public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
				if(position<0) return;
				cursor.moveToPosition(position);
				ViewHolder viewHolder = (ViewHolder) holder;
				viewMap.remove(viewHolder.lastBoundRow);
				long rowID = cursor.getLong(0);
				viewMap.put(viewHolder.lastBoundRow=rowID, viewHolder);
				String title = cursor.getString(1);
				String url = cursor.getString(2);
				DownloadTask task = taskMap.get(rowID);
				if(title==null && task!=null) {
					if(task.title!=null) {
						title = task.title;
					} else {
						title = task.webTitle;
					}
				}
				TaskItemsBinding taskItemView = viewHolder.taskItemData;
				taskItemView.title.setText(TextUtils.isEmpty(title)?"Untitled":title);
				taskItemView.url.setText(url);
				int color = Color.BLACK;
				long len=0;
				if(taskRunning(rowID)) {
					if(task!=null) {
						color = Color.GREEN;
						len = task.getDownloadedLength();
					} else if(taskExecutor !=null && taskExecutor.inQueue(rowID)) {
						color = Color.YELLOW;
					} else {
						color = Color.GRAY;
					}
				}
				//CMN.Log("onBindViewHolder", position, rowID, task, taskRunning(rowID));
				TextView number = taskItemView.number;
				TextView downloadIndicator = taskItemView.downloadIndicator;
				number.setText(""+position);
				number.setTextColor(color);
				if(len>0) {
					downloadIndicator.setText(mp4meta.utils.CMN.formatSize(len));
					downloadIndicator.setVisibility(View.VISIBLE);
				} else {
					downloadIndicator.setVisibility(View.GONE);
				}
				if(!TextUtils.isEmpty(SearchText) && ((title!=null&&title.contains(SearchText))||url!=null&&url.contains(SearchText))) {
					taskItemView.itemRoot.setBackgroundResource(R.drawable.xuxian2);
				} else {
					taskItemView.itemRoot.setBackground(null);
				}
			}
			
			@Override
			public int getItemCount() {
				return cursor.getCount();
			}
		});
		updateTaskList();
		try {
			if(true) {
				fout = new FileOutputStream("/sdcard/myFolder/browser.log", true);
				write(fout, "启动..."+new Date()+"\n\n");
			}
		} catch (IOException e) {
			//CMN.Log(e);
		}
		boolean x5 = Utils.littleCat;
		//x5 = true;
		//CMN.Log("111111");
		if(x5) {
			try {
				initX5WebStation();
			} catch (IOException e) {
				x5=false;
				CMN.Log(e);
			}
		}
		if(!x5){
			initStdWebStation();
		}
		CMN.Log("启动...");
		Utils.setOnClickListenersOneDepth(UIData.toolbarContent, this, 1, null); //switchWidget
		Window window = getWindow();
		if(Build.VERSION.SDK_INT>=21) {
			window.setStatusBarColor(0xff8f8f8f);
		}
	}
	
	private void updateRefreshBtn() {
		Drawable drawable = UIData.refresh.getDrawable();
		if(autoRefreshing) {
			drawable.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
		} else {
			drawable.setColorFilter(null);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(autoRefreshing) {
			appHandler.removeMessages(110);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(autoRefreshing) {
			appHandler.sendEmptyMessage(110);
		}
	}
	
	void updateTaskList() {
		if(cursor!=null) {
			cursor.close();
		}
		cursor = tasksDB.getCursor();
		if(cursor.getCount()==0) {
			tasksDB.insertNewEntry(null);
			cursor = tasksDB.getCursor();
		}
		adapter.notifyDataSetChanged();
	}
	
	
	@Override
	public boolean onLongClick(View v) {
		int id = v.getId();
		if(id==R.id.refresh) {
			autoRefreshing = !autoRefreshing;
			preference.edit().putBoolean("autoRefresh", autoRefreshing).apply();
			if(autoRefreshing) {
				appHandler.sendEmptyMessage(110);
				showT("自动刷新");
			} else {
				showT("已关闭");
			}
			updateRefreshBtn();
		}
		return true;
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id==R.id.switchWidget) {
			View ca = UIData.webs.getChildAt(0);
			if(ca!=null) {
				UIData.webs.removeView(ca);
				UIData.webs.addView(ca, 1);
			}
		}
		else if(id==R.id.upward) {
			layoutManager.scrollToPositionWithOffset(0, 0);
		}
		else if(id==R.id.downward) {
			layoutManager.scrollToPositionWithOffset(adapter.getItemCount()-1, 0);
		}
		else if(id==R.id.search) { //search
			setFieldRowAndIndex(SearchText, -1, 211);
			editHandler.setGotoQR(false);
		}
		else if(id==R.id.refresh) {
			updateTaskList();
		}
		else if(id==R.id.etSearchp) {
		}
		else {
			ViewHolder vh = (ViewHolder) v.getTag();
			selectionPos = vh.getLayoutPosition();
			selectionRow = vh.lastBoundRow;
			if(id==R.id.itemRoot) {
				setMainMenuListVis(true);
			} else {
				int idx = getFieldIndex(id);
				if(idx>=0) {
					cursor.moveToPosition(selectionPos);
					setFieldRowAndIndex(((TextView)v).getText().toString()
							, cursor.getLong(0), idx);
				}
			}
		}
	}
	
	public void setSearchText(String value) {
		//CMN.Log("setSearchText...", value);
		if(value!=null&&TextUtils.equals(SearchText, value)) {
			int fvp = layoutManager.findFirstVisibleItemPosition();
			if(fvp>=0&&fvp<cursor.getCount()) {
				searchInDatabase(fvp, value);
			}
		} else {
			SearchText = value;
			if(!TextUtils.isEmpty(value)) {
				searchInDatabase(-1, value);
			}
			adapter.notifyDataSetChanged();
		}
	}
	
	private void searchInDatabase(int start, String kay) {
		kay = kay.toLowerCase();
		cursor.moveToPosition(start);
		int foundIdx=-1;
		while(cursor.moveToNext()) {
			String val = cursor.getString(1);
			if(val!=null&&val.toLowerCase().contains(kay)
					||(val=cursor.getString(2))!=null&&val.toLowerCase().contains(kay)) {
				foundIdx = cursor.getPosition();
				break;
			}
		}
		if(foundIdx>0) {
			layoutManager.scrollToPositionWithOffset(foundIdx, 0);
		}
	}
	
	private void setFieldRowAndIndex(String text, long rowID, int idx) {
		if(editHandler==null) {
			editHandler = new BrowseFieldHandler(this);
		}
		editHandler.setText(text);
		if(idx<100) {
			editHandler.setGotoQR(true);
		}
		editHandler.setVis(true);
		editHandler.setFieldCxt(rowID, idx);
	}
	
	private int getFieldIndex(int id) {
		switch (id) {
			case R.id.title:
			return 0;
			case R.id.url:
			return 1;
		}
		return -1;
	}
	
	public void stopWebView() {
		listener.stopWebview();
	}
	
	public void clearWebview() {
		listener.clearWebview();
	}
	
	public void updateTitleForRow(long id, String newTitle) {
//		ViewHolder item = viewMap.get(id);
//		if(item!=null && item.lastBoundRow==id) {
//			item.taskItemData.title.set(newTitle);
//		}
		updateViewForRow(id);
		//updateTaskList();
	}
	
	public void updateViewForRow(long id) {
		ViewHolder view = viewMap.get(id);
		if(view!=null) {
			if(hasWindowFocus()) {
				if(Util.isOnMainThread()) {
					updateViewForRowRunnable(id);
				} else {
					UIData.root.post(() -> updateViewForRowRunnable(id));
				}
			} else {
				isViewDirty = true;
			}
		}
	}
	
	public void updateViewForRowRunnable(long id) {
		ViewHolder view = viewMap.get(id);
		if(view!=null) {
			adapter.onBindViewHolder(view, view.getLayoutPosition());
		}
	}
	
	public boolean taskRunning(long id) {
		return getRunningFlagForRow(id).get();
	}
	
	public void markTaskEnded(long id) {
		getRunningFlagForRow(id).set(false);
		notifyTaskStopped(id);
	}
	
	public void batRenWithPat(String path, String pattern, String replace) {
		File f = new File(path);
		if(f.isDirectory()) {
			File[] files = f.listFiles();
			if(files!=null) {
				File newF;
				String newName, suffix;
				Matcher m;
				Pattern p = Pattern.compile(pattern);
				for(File fI:files) {
					if(fI.isFile() && fI.length()>0 && (m=p.matcher(fI.getName())).find()) {
						newName = m.replaceAll(replace);
						suffix = "";
						int  suffix_idx = newName.lastIndexOf(".");
						if(suffix_idx>=0) {
							suffix = newName.substring(suffix_idx);
							newName = newName.substring(0, suffix_idx);
						}
						int cc=0;
						while(true) {
							String fn = cc==0?(newName+suffix):(newName+cc+suffix);
							newF = new File(f, fn);
							if(!newF.exists()) {
								break;
							}
						}
						CMN.Log("renameTo", fI.renameTo(newF), fI, newF);
					}
				}
			}
		}
	}
	
	public void notifyTaskStopped(long id) {
		updateViewForRow(id);
//		if(Looper.myLooper()==Looper.getMainLooper()) {
//			updateTaskList();
//		} else {
//			root.post(this::updateTaskList);
//		}
	}
	
	class ViewHolder extends RecyclerView.ViewHolder {
		final TaskItemsBinding taskItemData;
		public Long lastBoundRow;
		
		public ViewHolder(TaskItemsBinding taskItemData) {
			super(taskItemData.itemRoot);
			taskItemData.itemRoot.setTag(this);
			taskItemData.itemRoot.setOnClickListener(BrowseActivity.this);
			taskItemData.title.setTag(this);
			taskItemData.title.setOnClickListener(BrowseActivity.this);
			taskItemData.url.setTag(this);
			taskItemData.url.setOnClickListener(BrowseActivity.this);
			this.taskItemData = taskItemData;
		}
	}
	
	public void setMainMenuListVis(boolean vis) {
		int TargetTransY = 0;
		TwoWayGridView animMenu = UIData.mainMenuLst;
		if(MainMenuListVis&&vis) {
			animMenu.setTranslationY(8* GlobalOptions.density);
		}
		if(MainMenuListVis = vis) {
			animMenu.setVisibility(View.VISIBLE);
		} else {
			deletingRow = -1;
			TargetTransY = TargetTransY + animMenu.getHeight();
		}
		animMenu
				.animate()
				.translationY(TargetTransY)
				.setDuration(220)
				.start();
	}
	
	//for menu list
	public class MenuAdapter extends BaseAdapter implements TwoWayAdapterView.OnItemClickListener
	{
		@Override
		public int getCount() {
			return menuList.size();
		}
		
		@Override
		public View getItem(int position) {
			return null;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			PDocViewerActivity.MenuItemViewHolder holder;
			if(convertView==null) {
				convertView = getLayoutInflater().inflate(R.layout.menu_item, parent, false);
				convertView.setTag(holder=new PDocViewerActivity.MenuItemViewHolder(convertView));
				holder.tv.textPainter = menu_grid_painter;
			} else {
				holder = (PDocViewerActivity.MenuItemViewHolder) convertView.getTag();
			}
			holder.tv.setText(menuList.get(position));
			return convertView;
		}
		
		@Override
		public void onItemClick(TwoWayAdapterView<?> parent, View view, int position, long id) {
			long rowID = selectionRow;
			switch (position) {
				case 0:{ //start
					DownloadTask task = taskMap.remove(rowID);
					if(task!=null) {
						task.stop();
					}
					AtomicBoolean runFlag = getRunningFlagForRow(rowID);
					boolean start=!runFlag.get();
					runFlag.set(start);
					if(start) {
						queueTaskForDB(rowID, true);
						//startTaskForDB(cursor);
					} else {
						setTaskDelayed(rowID, -1, false);
						if(task!=null) {
							BrowseTaskExecutor taskExecutor = task.taskExecutor;
							if(taskExecutor !=null) {
								taskExecutor.removeTasks(task.abort, task.id);
							}
						}
						scheduleMap.remove(rowID);
						notifyTaskStopped(rowID);
					}
					showT(start?"开始":"终止");
					setMainMenuListVis(false);
				} break;
				case 1:{ //ext
					cursor.moveToPosition(selectionPos);
					String ext1 = cursor.getString(7);
					if(ext1==null) {
						//ext1 = "{ext1:\""+yourScript+"\"}";
					}
					setFieldRowAndIndex(ext1
							, cursor.getLong(0), 7);
					editHandler.setGotoQR(false);
				} break;
				case 2: { //add
					cursor.moveToPosition(selectionPos);
					String ext1 = cursor.getString(7);
					tasksDB.insertNewEntry(ext1);
					updateTaskList();
					setMainMenuListVis(false);
				} break;
				case 3: { //folder
					//CMN.Log(fileChooserParams.getAcceptTypes());
					DialogProperties properties = new DialogProperties();
					properties.selection_mode = DialogConfigs.SINGLE_MODE;
					properties.selection_type = DialogConfigs.DIR_SELECT;
					properties.root = new File("/");
					properties.error_dir = Environment.getExternalStorageDirectory();
					properties.offset = download_path;
					properties.opt_dir=new File(getExternalFilesDir(null), "favorite_dirs");
					properties.opt_dir.mkdirs();
					properties.title_id = 0;
					properties.isDark = false;
					FilePickerDialog dialog = new FilePickerDialog(BrowseActivity.this, properties);
					dialog.setDialogSelectionListener(new DialogSelectionListener() {
						@Override
						public void onSelectedFilePaths(String[] files, String currentPath) {
							if(files.length>0) {
								download_path = new File(files[0]);
								preference.edit().putString("path", download_path.getPath()).apply();
							}
						}
						@Override
						public void onEnterSlideShow(Window win, int delay) { }
						@Override
						public void onExitSlideShow() { }
						@Override
						public Activity getDialogActivity() {
							return BrowseActivity.this;
						}
						@Override
						public void onDismiss() { }
					});
					dialog.show();
					dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
				} break;
				case 4: { //schedule
					cursor.moveToPosition(selectionPos);
					String ext=cursor.getString(7);
					String url = cursor.getString(2);
					ScheduleTask task = new ScheduleTask(
							BrowseActivity.this, cursor.getLong(0)
							, url
							, download_path
							, cursor.getString(1)
							, 0
							, ext);
					
					//scheduleMap.put(selectionRow, task);
					//scheduleMap.put(selectionRow, new int[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50});
					
					setFieldRowAndIndex(dateToStr(new Date(CMN.now()+1000*25)), rowID, 115);
					
					editHandler.taskToSchedule = task;
				} break;
				case 5: { //mingling
					cursor.moveToPosition(selectionPos);
					rowID = cursor.getLong(0);
					String ext1 = cursor.getString(7);
					setFieldRowAndIndex(ext1, rowID, 7);
				} break;
				case 6: { //delete
					if(deletingRow!=rowID) {
						deletingRow = rowID;
						showT("Tap again to delete!");
					} else {
						if(tasksDB.delete(rowID)>0) {
							if(taskRunning(rowID)) {
								onItemClick(null, null, 0, 0);
							}
							updateTaskList();
							showT("deleted...");
						}
						deletingRow = -1;
					}
				} break;
			}
		}
	}
	
	public void thriveIfNeeded(long id) {
		Integer val = lifesMap.get(id);
		if(val==null) {
			//if(scheduleMap.get(id)!=null)
			{
				DownloadTask task = taskMap.get(id);
				lifesMap.put(id, task==null?5:task.lives);
				CMN.Log("thriving...", task.lives);
			}
		}
	}
	
	public void drainTaskForDB(long rowID) {
		//CMN.Log("drainTaskForDB...");
		Cursor taskNxt = tasksDB.getCursorByRowID(rowID);
		if(taskNxt.moveToNext()) {
			String urls = taskNxt.getString(2);
			int idx = urls.indexOf("\n");
			if(idx>0) {
				//CMN.Log("draining out::", idx, urls.substring(0, idx));
				urls = urls.substring(idx+1);
				if(!TextUtils.isEmpty(urls)) {
					ContentValues values = new ContentValues();
					values.put("url", urls);
					tasksDB.getDatabase().update("tasks", values, "id=?", new String[]{"" + rowID});
					if(taskRunning(rowID)) {
						queueTaskForDB(rowID, true);
					}
				}
			}
		}
	}
	
	void queueTaskForDB(long rowID, boolean proliferate) {
		BrowseTaskExecutor taskExecutor = acquireExecutor();
		taskExecutor.run(this, rowID);
		taskExecutor.start();
		if(proliferate) {
			lifesMap.put(rowID, null);
		}
		updateViewForRow(rowID);
//		DownloadTask task = taskMap.get(rowID);
//		if(task==null || !task.isDownloading()) {
//			updateViewForRow(rowID);
//		}
	}
	
	private BrowseTaskExecutor acquireExecutor() {
		synchronized (this) {
			if(taskExecutor !=null) {
				if(taskExecutor.acquire()) return taskExecutor;
				taskExecutor.stop();
			}
			return taskExecutor = new BrowseTaskExecutor(this);
		}
	}
	
	// 任务启动，添加记录至图
	DownloadTask startTaskForDB(BrowseTaskExecutor taskExecutor, Cursor cursor) {
		long id = cursor.getLong(0);
		DownloadTask task = taskMap.get(id);
		if(task!=null) {
			task.abort();
		}
		String ext=cursor.getString(7);
		String url = cursor.getString(2);
		task = new DownloadTask(
				this, taskExecutor, id
				, url
				, download_path
				, cursor.getString(1)
				, 0
				, ext);
		taskMap.put(id, task);
		DownloadTask finalTask = task;
		boolean started=false;
		//CMN.Log("启动...", task.ext1);
		if(ext!=null) {
			if(task.ext1!=null || task.ext2!=null) {
				UIData.root.post(() -> {
					listener.loadUrl(url, finalTask);
				});
				started=true;
			}
		}
		if(!started) {
			taskMap.remove(id);
			//task=null;
		}
		return task;
	}
	
	
	public boolean scheduleNxtAuto(long id) {
		if(!respawnTask(id)) {
			ScheduleTask task = scheduleMap.get(id);
			Integer[] schedule = task==null?null:task.scheduleSeq;
			if(schedule!=null) {
				if(++task.scheduleIter<schedule.length) {
					int delay = schedule[task.scheduleIter];
					delay = (int) (delay*60*1000+random.nextFloat()*1*30*1000);
					setTaskDelayed(id, delay, true);
				} else {
					scheduleMap.remove(id);
				}
			} else {
				return false;
			}
		}
		return true;
	}
	
	void setTaskDelayed(long id, int delay, boolean schedule) {
		if(schedule) {
			getRunningFlagForRow(id).set(true);
		}
		PendingIntent pendingIntent = intentMap.get(id);
		if(pendingIntent!=null) {
			alarmManager.cancel(pendingIntent);
		}
		
		if(delay<0) {
			scheduleMap.remove(id);
			return;
		}
		
		CMN.Log("setTaskDelayed...", delay/1000/60.f);
		Intent intent = new Intent();
		intent.setClass(this, BrowseActivity.class);
		intent.putExtra("task", id);
		intent.putExtra("pro", schedule);
		intent.setData(Uri.parse(""+id));
		
		pendingIntent = PendingIntent.getActivity(getApplicationContext(),
				110, intent,
				0);
		
		intentMap.put(id, pendingIntent);
		
		//alarmManager.cancel(pendingIntent);
		alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+delay, pendingIntent);
	}
	
	/** respawn from end of downloading
	 * respawn on failure of extraction
	 * respawn on extraction timeout
	 *
	 * @return whether the task is respawned*/
	public boolean respawnTask(long id) {
		CMN.Log("respawnTask", taskRunning(id), lifesMap.get(id));
		if(!taskRunning(id)) {
			return false;
		}
		int lifeSpan = getIntValue(lifesMap.get(id));
		if(--lifeSpan>0) {
			// queue the next analogous task.
			CMN.Log("queue nxt respawner");
			setTaskDelayed(id, (int) (1.5*5*1180*Math.max(0.65, random.nextFloat())), false);
			//a.queueTaskForDB(id, false);
			lifesMap.put(id, lifeSpan);
			return true;
		}
		return false;
	}
	
	private int getIntValue(Integer val) {
		return val==null?0:val;
	}
	
	public static class EnchanterReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String intentAction = intent.getAction();
			if ("plodlock".equals(intentAction)) {
				CMN.Log("plodlock!!!");
			}
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		//CMN.Log("onNewIntent");
		CMN.Log("接收到任务：", intent);
		long task = intent.getLongExtra("task", -1);
		if(taskRunning(task)) {
			if(task!=-1) {
				boolean schedule = intent.getBooleanExtra("pro", false);
				CMN.Log("接收到任务：", task, CMN.id(this), schedule);
				queueTaskForDB(task, schedule);
			}
			try {
				acquireWakeLock();
				mWakeLock.release();
				mWakeLocked = false;
			} catch (Exception e) {
				CMN.Log(e);
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		CMN.Log("onActivityResult::", data, requestCode, resultCode, StaticTextExtra);
		if(requestCode==RequsetUrlFromCamera&&editHandler!=null)
		{
			String text = data==null?null:data.getStringExtra(Intent.EXTRA_TEXT);
			if(Utils.littleCat) {
				checkResumeQRText = true;
			}
			onQRGetText(text);
		}
	}
	
	@Override
	protected void onQRGetText(String text) {
		if(text!=null) {
			editHandler.setText(text);
		}
	}
	
	static AtomicBoolean stop = new AtomicBoolean(false);
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stop.set(true);
		for(Long kI: runningMap.keySet()) {
			AtomicBoolean task = runningMap.get(kI);
			if(task!=null) {
				task.set(false);
			}
		}
		for(Long kI:taskMap.keySet()) {
			DownloadTask task = taskMap.get(kI);
			if(task!=null) {
				task.stop();
			}
		}
		taskMap.clear();
		if(fout!=null) {
			write(fout, "终止..."+new Date()+"\n\n");
			try {
				fout.close();
			} catch (IOException e) {
				CMN.Log(e);
			}
		}
		if(taskExecutor !=null) {
			taskExecutor.stop();
		}
	}
	
	synchronized public AtomicBoolean getRunningFlagForRow(long id) {
		AtomicBoolean ret = runningMap.get(id);
		if(ret==null) {
			runningMap.put(id, ret = new AtomicBoolean());
		}
		return ret;
	}
	
	public void onUrlExtracted(DownloadTask task, String url, String title) {
		CMN.Log("onUrlExtracted", url, title);
		if(task!=null) {
			if(taskRunning(task.id)) {
				if(!TextUtils.isEmpty(url) && !task.abort.get()) {
					if(TextUtils.isEmpty(task.title)) {
						if(title!=null) {
							task.updateTitle(title);
						} else if(task.webTitle!=null) {
							task.updateTitle(task.webTitle);
						}
					}
					if(title!=null&&task.shotExt !=null) {
						task.shotFn = title;
					}
					task.download(url);
				} else {
					task.abort();
					// fail to extract url and start download. schedule nxt.
					scheduleNxtAuto(task.id);
					taskMap.remove(task.id);
					notifyTaskStopped(task.id);
				}
			}
			BrowseTaskExecutor taskExecutor = task.taskExecutor;
			CMN.Log("尝试打断", taskExecutor);
			if(taskExecutor !=null
					//&& taskExecutor.token==task.abort
			) {
				taskExecutor.interrupt();
			}
		}
	}
	
	private void write(FileOutputStream fout, String val) {
		try {
			fout.write(val.getBytes());
			fout.flush();
		} catch (IOException e) {
			CMN.Log(e);
		}
	}
	
	static HashMap<String, String> defaultHeaders(String referer, String cookie) {
		//a reasonable UA
		String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36";
		HashMap<String, String> headers = new HashMap<>();
		headers.put("Accept", "*/*");
		headers.put("Accept-Language", "en-US,en;q=0.5");
		headers.put("User-Agent", ua);
		//if referer is not None:
		//headers.update({'Referer': referer})
		//if cookie is not None:
		//headers.update({'Cookie': cookie})
		return headers;
	}
	
	@JavascriptInterface
	public String httpGet(String url, String[] headerArr) {
		//if(true) return "x";
		CMN.Log("httpGet", url, headerArr);
		HashMap<String, String> headers;
		if(headerArr!=null) {
			headers = new HashMap<>();
			for (int i = 0; i+1 < headerArr.length; i+=2) {
				headers.put(headerArr[i], headerArr[i+1]);
			}
		} else {
			headers = defaultHeaders(null, null);
		}
		return get_content_std(url, headers);
	}
	
	public static String get_content_std(String httpurl, HashMap<String, String> headers) {
		HttpURLConnection httpConnect = null;
		InputStream is = null;
		BufferedReader br = null;
		String result = null;// 返回结果字符串
		try {
			URL url = new URL(httpurl);
			httpConnect = (HttpURLConnection) url.openConnection();
			httpConnect.setRequestMethod("GET");
			httpConnect.setConnectTimeout(15000);
			httpConnect.setReadTimeout(60000);
			if(headers!=null) {
				for(String key:headers.keySet()) {
					httpConnect.setRequestProperty(key, headers.get(key));
				}
			}
			httpConnect.connect();
			if (httpConnect.getResponseCode() == 200) {
				is = httpConnect.getInputStream();
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				StringBuffer sbf = new StringBuffer();
				String temp = null;
				while ((temp = br.readLine()) != null) {
					sbf.append(temp);
					sbf.append("\r\n");
				}
				result = sbf.toString();
			}
		} catch (Exception e) {
			CMN.Log(e);
		} finally {
			// 关闭资源
			if (null != br) {
				try {
					br.close();
				} catch (IOException ignored) { }
			}
			if (null != is) {
				try {
					is.close();
				} catch (IOException ignored) { }
			}
			httpConnect.disconnect();
		}
		return result;
	}
	
	/** https://www.cnblogs.com/hhhshct/p/8523697.html */
	public static String get_content(String url, HashMap<String, String> headers) {
		String result = "";
//		CloseableHttpClient httpClient = null;
//		CloseableHttpResponse response = null;
//		try {
//			CMN.Log("result?1?", result);
//			httpClient = HttpClients.createDefault();
//			HttpGet httpGet = new HttpGet(url);
//			httpGet.setHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
//			if(headers!=null) {
//				for(String key:headers.keySet()) {
//					httpGet.setHeader(key, headers.get(key));
//				}
//			}
//			RequestConfig requestConfig =
//					RequestConfig.custom()
//							.setConnectTimeout(35000)// 连接主机服务超时时间
//							.setConnectionRequestTimeout(35000)// 请求超时时间
//							.setSocketTimeout(60000)// 数据读取超时时间
//							.build();
//			httpGet.setConfig(requestConfig);
//			response = httpClient.execute(httpGet);
//			HttpEntity entity = response.getEntity();
//			result = EntityUtils.toString(entity);
//		} catch (Exception e) {
//			CMN.Log(e);
//		} finally {
//			// 关闭资源
//			if (null != response) {
//				try {
//					response.close();
//				} catch (IOException ignored) { }
//			}
//			if (null != httpClient) {
//				try {
//					httpClient.close();
//				} catch (IOException ignored) { }
//			}
//		}
		return result;
	}
}
