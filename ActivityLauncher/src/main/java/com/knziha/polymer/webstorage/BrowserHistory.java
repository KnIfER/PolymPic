package com.knziha.polymer.webstorage;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.R;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.HistoryBinding;
import com.knziha.polymer.databinding.HistoryItemBinding;
import com.knziha.polymer.widgets.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.knziha.polymer.widgets.Utils.EmptyCursor;

public class BrowserHistory extends DialogFragment implements View.OnClickListener {
	HistoryBinding UIData;
	Cursor cursor = EmptyCursor;
	RecyclerView.Adapter<ViewHolder> adapter;
	Date date = new Date();
	Date date1 = new Date();
	static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd HH:mm");
	static final SimpleDateFormat dateFormatter1 = new SimpleDateFormat("MM/dd");
	int baseIconIndicatorRes = R.drawable.ic_baseline_history_gray;
	
	protected class AppDataTimeRenownedBaseAdapter extends RecyclerView.Adapter<ViewHolder> {
		final LayoutInflater inflater;
		AppDataTimeRenownedBaseAdapter(LayoutInflater inflater) {
			this.inflater = inflater;
		}
		@NonNull @Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			ViewHolder vh = new ViewHolder(HistoryItemBinding.inflate(inflater, parent, false));
			return vh;
		}
		
		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			cursor.moveToPosition(position);
			long rowID = cursor.getLong(0);
			String url = cursor.getString(1);
			String title = cursor.getString(2);
			long time = cursor.getLong(3);
			holder.setFields(rowID, url, title, time);
			
			int idx = Utils.httpIndex(url);
			if(idx>0) {
				if(url.startsWith("www.", idx+1)) {
					idx+=4;
				}
				url = url.substring(idx+1);
			}
			
			HistoryItemBinding viewData = holder.itemData;
			viewData.subtitle.setText(url);
			viewData.title.setText(title);
			viewData.title.setMaxLines(Utils.httpIndex(title)>0?1:10);
			
			date.setTime(time);
			
			viewData.time.setText(dateFormatter.format(date));
			
			boolean showTimeCapsule = position==0;
			
			if(!showTimeCapsule) {
				cursor.moveToPosition(position-1);
				long time1 = cursor.getLong(3);
				date1.setTime(time1);
				if(date.getDay()!=date1.getDay()||Math.abs(time-time1)>24*60*60*1000) {
					showTimeCapsule = true;
				}
			}
			
			if(showTimeCapsule) {
				viewData.capsule.setVisibility(View.VISIBLE);
				viewData.capsule.setText(dateFormatter1.format(date));
				((ViewGroup.MarginLayoutParams) viewData.icon.getLayoutParams()).topMargin=(int) (15* GlobalOptions.density);
			} else {
				viewData.capsule.setVisibility(View.GONE);
				((ViewGroup.MarginLayoutParams) viewData.icon.getLayoutParams()).topMargin=(int) (8* GlobalOptions.density);
			}
			
			viewData.icon.setImageResource(baseIconIndicatorRes);
		}
		
		@Override
		public int getItemCount() {
			return cursor.getCount();
		}
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(UIData==null) {
			UIData = HistoryBinding.inflate(inflater, container, false);
			Toolbar toolbar = UIData.toolbar;
			if(GlobalOptions.isDark) {
				toolbar.setNavigationIcon(com.knziha.filepicker.R.drawable.abc_ic_ab_back_material);
				toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
			} else
				toolbar.setNavigationIcon(com.knziha.filepicker.R.drawable.abc_ic_ab_back_material);
			toolbar.setNavigationOnClickListener(this);
			RecyclerView hyRv = UIData.historyRv;
			hyRv.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
			hyRv.setItemAnimator(null);
			hyRv.setRecycledViewPool(Utils.MaxRecyclerPool(35));
			hyRv.setHasFixedSize(true);
			hyRv.setAdapter(adapter=newAdapter(inflater));
			hyRv.addItemDecoration(Utils.RecyclerViewDivider.getInstance());
		} else {
			Utils.removeIfParentBeOrNotBe(UIData.history, null, false);
		}
		if(true) {
			pullData();
		}
		return UIData.history;
	}
	
	protected AppDataTimeRenownedBaseAdapter newAdapter(LayoutInflater inflater) {
		return new AppDataTimeRenownedBaseAdapter(inflater);
	}
	
	protected void pullData() {
		cursor = LexicalDBHelper.getInstancedDb().rawQuery("select id,url,title,last_visit_time from urls order by last_visit_time DESC", null);
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
	
	static class ViewHolder extends RecyclerView.ViewHolder {
		final HistoryItemBinding itemData;
		long rowID;
		String url;
		String title;
		long time;
		public ViewHolder(HistoryItemBinding itemData) {
			super(itemData.root);
			this.itemData = itemData;
			itemData.root.setTag(this);
		}
		
		public void setFields(long rowID, String url, String title, long time) {
			this.rowID = rowID;
			this.url = url;
			this.title = title;
			this.time = time;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(getDialog()!=null) {
			Window window = getDialog().getWindow();
			if (window != null) {
				WindowManager.LayoutParams layoutParams = window.getAttributes();
				layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
				layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
				layoutParams.horizontalMargin = 0;
				layoutParams.verticalMargin = 0;
				layoutParams.dimAmount=0;
				window.setAttributes(layoutParams);
				
				window.getDecorView().setBackground(null);
				window.getDecorView().setPadding(0,0,0,0);
				
				if(Build.VERSION.SDK_INT>=21) {
					window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
					window.setStatusBarColor(0xff8f8f8f);
				}
			}
		}
	}
	
	@Override
	public int getTheme() {
		return R.style.Dialog;
	}
}
