package com.knziha.polymer.webstorage;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.browser.DownloadHandlerStd;
import com.knziha.polymer.database.LexicalDBHelper;

import java.util.HashSet;

public class BrowserDownloads extends BrowserHistory implements View.OnClickListener {
	private boolean isPaused;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		baseIconIndicatorRes = R.drawable.ic_file_download_black_24dp;
		showTime=false;
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
			case R.id.home:
				dismiss();
			break;
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
