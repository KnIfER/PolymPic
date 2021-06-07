package com.knziha.polymer.webstorage;

import android.annotation.SuppressLint;
import android.app.Dialog;
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

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.HistoryBinding;
import com.knziha.polymer.databinding.HistoryItemBinding;
import com.knziha.polymer.widgets.PopupMenuHelper;
import com.knziha.polymer.widgets.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.knziha.polymer.widgets.Utils.EmptyCursor;

public class BrowserHistory extends DialogFragment implements View.OnClickListener
		, View.OnLongClickListener, PopupMenuHelper.PopupMenuListener {
	private Dialog mDlg;
	int tag = R.string.fragment_history;
	HistoryBinding UIData;
	Cursor cursor = EmptyCursor;
	RecyclerView.Adapter<ViewHolder> adapter;
	Date date = new Date();
	Date date1 = new Date();
	//static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd HH:mm");
	static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd");
	static final SimpleDateFormat dateFormatter1 = new SimpleDateFormat("MM/dd");
	int baseIconIndicatorRes = R.drawable.ic_baseline_history_gray;
	boolean showTime=false;
	protected ViewHolder longClickView;
	private boolean animExtSet;
	
	@Override
	public boolean onLongClick(View view) {
		ViewHolder viewHolder = (ViewHolder) view.getTag();
//		if (recyclerView instanceof DragSelectRecyclerView) {
//			//selMode = true;
//			if(getSelMode() && recyclerView.mLastTouchX>view.getWidth()/3) {
//				((DragSelectRecyclerView)recyclerView).setDragSelectActive(true, viewHolder.getLayoutPosition());
//				//view.jumpDrawablesToCurrentState();
//				if (viewHolder.selected) {
//					Drawable bg = viewHolder.itemView.getBackground();
//					viewHolder.itemView.setBackground(null);
//					viewHolder.itemView.setBackground(bg);
//				}
//				return true;
//			}
//		}
		longClickView = viewHolder;
		BrowserActivity a = (BrowserActivity) getActivity();
		if (a!=null) {
			PopupMenuHelper popupMenu = a.getPopupMenu();
			if (popupMenu.tag!=tag) {
				final int[] texts = getLongClickMenuList();
				popupMenu.initLayout(texts, this);
				popupMenu.tag=tag;
			}
			int[] vLocationOnScreen = new int[2];
			UIData.historyRv.getLocationOnScreen(vLocationOnScreen);
			popupMenu.show(UIData.getRoot(), UIData.historyRv.mLastTouchX+vLocationOnScreen[0], UIData.historyRv.mLastTouchY+vLocationOnScreen[1]);
			Utils.preventDefaultTouchEvent(UIData.getRoot(), -100, -100);
		}
		return true;
	}
	
	protected int[] getLongClickMenuList() {
		return new int[] {
			R.string.houtaidakai
			,R.string.xinbiaoqianyedaikai
			,R.string.tianjiadoahang
			,R.string.fuzhilianjie
			,R.string.fuzhiwenben
			,R.string.delete
			,R.string.share
		};
	}
	
	@Override
	public boolean onMenuItemClick(PopupMenuHelper popupMenuHelper, View v, boolean isLongClick) {
		boolean ret=true;
		boolean dismiss = !isLongClick;
		View blinkView = null;
		BrowserActivity a = (BrowserActivity) getActivity();
		ViewHolder viewHolder = longClickView;
		if (a!=null) {
			switch (v.getId()) {
				case R.string.houtaidakai:{
					a.newTab(viewHolder.url, true, true, -1);
				} break;
				case R.string.xinbiaoqianyedaikai: {
					a.newTab(viewHolder.url, false, true, -1);
					dismiss();
				} break;
				case R.string.tianjiadoahang:{
					a.getNavAdapter().InsertNavNode(viewHolder.url, viewHolder.title);
				} break;
				case R.string.fuzhilianjie:{
					a.TextToClipboard(viewHolder.url, 0);
				} break;
				case R.string.fuzhiwenben:{
					a.TextToClipboard(viewHolder.title, 1);
				} break;
				case R.string.delete:{
				} break;
				case R.string.share:{
					a.shareUrlOrText(viewHolder.url, null);
				} break;
			}
		}
		if (blinkView!=null) {
			Utils.blinkView(blinkView, false);
		} else if (dismiss) {
			popupMenuHelper.postDismiss(80);
		}
		return ret;
	}
	
	protected class AppDataTimeRenownedBaseAdapter extends RecyclerView.Adapter<ViewHolder> {
		final LayoutInflater inflater;
		AppDataTimeRenownedBaseAdapter(LayoutInflater inflater) {
			this.inflater = inflater;
		}
		@NonNull @Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			ViewHolder ret = new ViewHolder(HistoryItemBinding.inflate(inflater, parent, false));
			ret.itemView.setOnLongClickListener(BrowserHistory.this);
			ret.itemView.setOnClickListener(BrowserHistory.this);
			return ret;
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
			viewData.title.setSingleLine();
			viewData.title.setTextSize(15f);
			date.setTime(time);
			if (showTime) {
				viewData.time.setText(dateFormatter.format(date));
			}
			
			BrowserHistory.this.onBindViewHolder(holder, cursor, position);
			
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
		}
		
		@Override
		public int getItemCount() {
			return cursor.getCount();
		}
	}
	
	protected void onBindViewHolder(ViewHolder holder, Cursor cursor, int position) {
		holder.itemData.icon.setImageResource(baseIconIndicatorRes);
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
		if(cursor==EmptyCursor) {
			pullData();
		}
		if (animExtSet) setWindowAnimationStyle(0);
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
			case R.id.root: {
				BrowserActivity a = (BrowserActivity) getActivity();
				if (a!=null) {
					ViewHolder viewHolder = (ViewHolder) v.getTag();
					a.execBrowserGoTo(viewHolder.url);
				}
				postDismiss(v);
			} break;
		}
	}
	
	public void postDismiss(View v) {
		setWindowAnimationStyle(R.style.DialogAnimation1);
		v.postDelayed(this::dismiss, 120);
	}
	
	private void setWindowAnimationStyle(int animationStyle) {
		try {
			// why application resource works here?
			getDialog().getWindow().setWindowAnimations(animationStyle);
			animExtSet = animationStyle!=0;
		} catch (Exception ignored) { }
	}
	
	static class ViewHolder extends RecyclerView.ViewHolder {
		final HistoryItemBinding itemData;
		long rowID;
		String url;
		String title;
		long time;
		Object tag;
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
	
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
		CMN.Log("onCreateDialog");
		if (mDlg==null) {
			return mDlg=super.onCreateDialog(savedInstanceState);
		}
		return mDlg;
	}
	
	@Override
	public int getTheme() {
		return R.style.Dialog;
	}
}
