package com.knziha.polymer;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
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

import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.HistoryBinding;
import com.knziha.polymer.databinding.HistoryItemBinding;
import com.knziha.polymer.widgets.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.knziha.polymer.widgets.Utils.EmptyCursor;

public class BrowserDownloads extends DialogFragment implements View.OnClickListener {
	HistoryBinding UIData;
	RecyclerView hyRv;
	Cursor cursor = EmptyCursor;
	private RecyclerView.Adapter<ViewHolder> adapter;
	Date date = new Date();
	Date date1 = new Date();
	static final SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd HH:mm");
	static final SimpleDateFormat dateFormatter1 = new SimpleDateFormat("MM/dd");
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			}else
				toolbar.setNavigationIcon(com.knziha.filepicker.R.drawable.abc_ic_ab_back_material);
			toolbar.setNavigationOnClickListener(this);
			hyRv = UIData.historyRv;
			hyRv.setLayoutManager(new LinearLayoutManager(inflater.getContext()));
			hyRv.setItemAnimator(null);
			hyRv.setRecycledViewPool(Utils.MaxRecyclerPool(35));
			hyRv.setHasFixedSize(true);
			hyRv.setAdapter(adapter=new RecyclerView.Adapter<ViewHolder>() {
				@NonNull
				@Override
				public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
					ViewHolder vh = new ViewHolder(HistoryItemBinding.inflate(inflater, parent, false));
					return vh;
				}
				
				@Override
				public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
					cursor.moveToPosition(position);
					String url = cursor.getString(2);
					String title = cursor.getString(7);
					long time = cursor.getLong(10);
					
					int idx = Utils.httpIndex(url);
					if(idx>0) {
						if(url.startsWith("www.", idx+1)) {
							idx+=4;
						}
						url = url.substring(idx+1);
					}
					holder.itemData.subtitle.setText(url);
					
					holder.itemData.title.setText(title);
					holder.itemData.title.setMaxLines(Utils.httpIndex(title)>0?1:10);
					
					date.setTime(time);
					
					holder.itemData.time.setText(dateFormatter.format(date));
					
					boolean showTimeCapsule = position==0;
					
					if(!showTimeCapsule) {
						cursor.moveToPosition(position-1);
						long time1 = cursor.getLong(10);
						date1.setTime(time1);
						if(date.getDay()!=date1.getDay()||Math.abs(time-time1)>24*60*60*1000) {
							showTimeCapsule = true;
						}
					}
					
					if(showTimeCapsule) {
						holder.itemData.capsule.setVisibility(View.VISIBLE);
						holder.itemData.capsule.setText(dateFormatter1.format(date));
						((ViewGroup.MarginLayoutParams)holder.itemData.icon.getLayoutParams()).topMargin=(int) (15* GlobalOptions.density);
					} else {
						holder.itemData.capsule.setVisibility(View.GONE);
						((ViewGroup.MarginLayoutParams)holder.itemData.icon.getLayoutParams()).topMargin=(int) (8* GlobalOptions.density);
					}
					
					holder.itemData.icon.setImageResource(R.drawable.ic_file_download_black_24dp);
					
				}
				
				@Override
				public int getItemCount() {
					return cursor.getCount();
				}
			});
			
			hyRv.addItemDecoration(new RecyclerView.ItemDecoration(){
				final ColorDrawable mDivider = new ColorDrawable(Color.GRAY);
				final int mDividerHeight = 1;
				@Override
				public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
					final int childCount = parent.getChildCount();
					final int width = parent.getWidth();
					for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
						final View view = parent.getChildAt(childViewIndex);
						if (shouldDrawDividerBelow(view, parent)) {
							int top = (int) view.getY() + view.getHeight();
							mDivider.setBounds(0, top, width, top + mDividerHeight);
							mDivider.draw(c);
						}
					}
				}
				@Override
				public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
					if (shouldDrawDividerBelow(view, parent)) {
						outRect.bottom = mDividerHeight;
					}
				}
				private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
					//return parent.getChildViewHolder(view).getLayoutPosition()<parent.getChildCount()-1;
					return parent.getChildViewHolder(view).getBindingAdapterPosition()<adapter.getItemCount()-1;
				}
			});
		} else {
			Utils.removeIfParentBeOrNotBe(UIData.history, null, false);
		}
		if(true) {
			pullData();
		}
		return UIData.history;
	}
	
	private void pullData() {
		cursor = LexicalDBHelper.getInstancedDb().rawQuery("select * from downloads order by creation_time DESC", null);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
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
		public ViewHolder(HistoryItemBinding itemData) {
			super(itemData.root);
			this.itemData = itemData;
			itemData.root.setTag(this);
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
