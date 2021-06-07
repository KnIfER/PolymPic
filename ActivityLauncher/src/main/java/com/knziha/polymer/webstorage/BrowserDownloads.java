package com.knziha.polymer.webstorage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.DownloadHandlerStd;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.widgets.PopupMenuHelper;
import com.knziha.polymer.widgets.Utils;

import java.io.File;
import java.util.HashSet;

public class BrowserDownloads extends BrowserHistory implements View.OnClickListener {
	private boolean isPaused;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseIconIndicatorRes = R.drawable.ic_file_download_black_24dp;
		showTime=false;
		tag = R.string.downloads;
	}
	
	@Override
	protected int[] getLongClickMenuList() {
		return new int[] {
			R.string.dakaifangshi
			,R.string.daikaiwenjianjia
			,R.string.fuzhilianjie
			,R.string.delete
			,R.string.share
		};
	}
	
	@SuppressLint("ResourceType")
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		boolean ret=true;
		boolean dismiss = !isLongClick;
		View blinkView = null;
		BrowserActivity a = (BrowserActivity) getActivity();
		ViewHolder viewHolder = longClickView;
		int position = viewHolder.getLayoutPosition();
		DownloadHandlerStd.DownloadItemView downloadItemView = (DownloadHandlerStd.DownloadItemView) viewHolder.tag;
		long dwnldID = downloadItemView.tid;
		if (a!=null) {
			int id = v.getId();
			switch (id) {
				case R.id.root:
				case R.string.dakaifangshi:
				case R.string.daikaiwenjianjia: {
					Cursor cursor = LexicalDBHelper.getInstancedDb().rawQuery("select path,mime from downloads where tid=?", new String[]{String.valueOf(dwnldID)});
					if (cursor.moveToNext()) {
						String path = cursor.getString(0);
						String mime = cursor.getString(1);
						cursor.close();
						DownloadHandlerStd downloader = a.getDownloader();
						if(TextUtils.isEmpty(path))
						{
							path = downloader.getDownloadPathForDwnldID(dwnldID);
						}
						if(TextUtils.isEmpty(mime))
						{
							mime = downloader.getMimeTypeForDwnldID(dwnldID);
						}
						//CMN.Log(mime, path);
						try {
							Utils.fuckVM();
							Intent intent = new Intent(Intent.ACTION_VIEW);
							if (path.startsWith("file:")) {
								path = Uri.parse(path).getPath();
							}
							File file = new File(path);
							if (id ==R.string.daikaiwenjianjia) {
								file = file.getParentFile();
								intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								intent.setDataAndType(Uri.fromFile(file), "resource/folder");
								dismiss = false;
							} else {
								intent.setDataAndType(Uri.fromFile(file), mime);
								if (id==R.string.dakaifangshi) {
									intent = Intent.createChooser(intent, ((TextView)v).getText());
								}
							}
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
						} catch (Exception e) {
							CMN.Log(e);
							blinkView = v;
						}
					}
					cursor.close();
				} break;
				case R.string.delete:{
				
				} break;
				default: return super.onMenuItemClick(popupMenuHelper, v, isLongClick);
			}
		}
		if (blinkView!=null) {
			Utils.blinkView(blinkView, false);
		} else if (dismiss) {
			popupMenuHelper.postDismiss(80);
		}
		return ret;
	}
	
	HashSet<ViewHolder> runningTasks = new HashSet<>();
	ProgressRefreshHandler handler = new ProgressRefreshHandler();
	/*static */
	@SuppressLint("HandlerLeak")
	class ProgressRefreshHandler extends Handler {
		@Override
		public void handleMessage(@NonNull Message msg) {
			removeMessages(110);
			if(!isPaused) {
				if(UIData.historyRv.getScrollState()==RecyclerView.SCROLL_STATE_IDLE)
				for(ViewHolder vhI:runningTasks.toArray(new ViewHolder[]{})) {
					bindViewHolder(vhI, (DownloadHandlerStd.DownloadItemView) vhI.tag);
				}
				sendEmptyMessageDelayed(110, 850);
			}
		}
	}
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(adapter!=null) {
			adapter.notifyDataSetChanged();
		}
	}
	
	protected void pullData() {
		cursor = LexicalDBHelper.getInstancedDb().rawQuery("select id,url,filename,creation_time,tid from downloads order by creation_time DESC", null);
		adapter.notifyDataSetChanged();
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.root: {
				BrowserActivity a = (BrowserActivity) getActivity();
				if (a!=null) {
					longClickView = (ViewHolder) v.getTag();
					onMenuItemClick(a.getPopupMenu(), v, false);
				}
			} break;
			default: super.onClick(v);
		}
	}
	
	protected void onBindViewHolder(ViewHolder holder, Cursor cursor, int position) {
		if(holder.tag==null) {
			holder.tag = new DownloadHandlerStd.DownloadItemView(holder.itemData.icon, holder.itemData.subtitle1);
		}
		holder.itemData.title.setEllipsize(TextUtils.TruncateAt.MIDDLE);
		holder.itemData.title.setTextSize(15f);
		DownloadHandlerStd.DownloadItemView dwnldView = (DownloadHandlerStd.DownloadItemView) holder.tag;
		dwnldView.tid = cursor.getLong(4);
		dwnldView.downloadProgressIndicator.setText(""+dwnldView.tid);
		bindViewHolder(holder, dwnldView);
	}
	
	private void bindViewHolder(ViewHolder holder, DownloadHandlerStd.DownloadItemView dwnldView) {
		BrowserActivity a = (BrowserActivity) getActivity();
		dwnldView.running=false;
		if(a!=null) {
			a.getDownloader().queryDownloadStates(dwnldView.tid, dwnldView, baseIconIndicatorRes);
		}
		Animation mShake = dwnldView.mShake;
		if(dwnldView.running) {
			if(mShake==null) {
				mShake = AnimationUtils.loadAnimation(getContext().getApplicationContext(), R.anim.anim_updwn);
				mShake.setRepeatCount(-1);
				dwnldView.mShake = mShake;
			}
			if(!dwnldView.animStarted) {
				dwnldView.downloadIconView.startAnimation(mShake);
				runningTasks.add(holder);
				dwnldView.animStarted =true;
			}
		} else {
			if(mShake!=null&&dwnldView.animStarted) {
				mShake.cancel();
				dwnldView.animStarted =false;
				runningTasks.remove(holder);
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		isPaused = false;
		handler.sendEmptyMessage(110);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		isPaused = true;
	}
}
