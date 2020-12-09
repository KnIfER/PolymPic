package com.knziha.polymer.pdviewer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.ActivityPdocHistoryBinding;
import com.knziha.polymer.databinding.ActivityPdocHistoryItemBinding;
import com.knziha.polymer.pdviewer.pagecover.PageCover;

public class PDocHistoryActivity extends Toastable_Activity implements Toolbar.OnMenuItemClickListener , View.OnClickListener{
	
	private LexicalDBHelper historyCon;
	private ActivityPdocHistoryBinding UIData;
	
	Cursor historyCursor;
	private RecyclerView recyclerView;
	
	void setStatusBarColor(Window window){
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				| WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
				
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		if(Build.VERSION.SDK_INT>=21) {
			window.setStatusBarColor(0xff6f6f6f);
			//window.setNavigationBarColor(Color.TRANSPARENT);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Window win = getWindow();
		win.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setStatusBarColor(win);
		
		historyCon = LexicalDBHelper.connectInstance(this);
		
		UIData = DataBindingUtil.setContentView(this, R.layout.activity_pdoc_history);
		
		Toolbar mToolbar = UIData.toolbar;
		
		mToolbar.inflateMenu(R.menu.history_tools);
		mToolbar.setOnMenuItemClickListener(this);
		mToolbar.setId(R.id.action_context_bar);
		
		mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
		mToolbar.setNavigationOnClickListener(v1 -> {
			finish();
		});
		
		RecyclerView rv = UIData.rv;
		
		this.recyclerView = rv;
		
		toggleGridView(1);
		
		historyCursor = historyCon.queryPdocHistory();
		
		rv.setTag(this);
		
		rv.setAdapter(new RecyclerView.Adapter() {
			@NonNull
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				return new ViewHolder(ActivityPdocHistoryItemBinding.inflate(inflater, parent, false), (View.OnClickListener)parent.getTag());
			}
			
			@Override
			public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
				
				ViewHolder viewholder = (ViewHolder) holder;
				
				historyCursor.moveToPosition(position);
				
				PageCover model = new PageCover(getContentResolver(), viewholder.path = historyCursor.getString(2)
						, historyCursor.getInt(0), dm);
				
				ActivityPdocHistoryItemBinding itemData = viewholder.itemData;
				itemData.tv.setText(historyCursor.getString(1));
				Priority priority = Priority.HIGH;
				RequestOptions options = new RequestOptions()
						//.signature(new ObjectKey(model.path))
						.format(DecodeFormat.PREFER_ARGB_8888)//DecodeFormat.PREFER_ARGB_8888
						.priority(priority)
						.skipMemoryCache(false)
						.diskCacheStrategy(DiskCacheStrategy.NONE)
						//.onlyRetrieveFromCache(true)
						.fitCenter()
						.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
				
				RequestManager IncanOpen = Glide.with(PDocHistoryActivity.this);
				
				IncanOpen.load(model)
						.apply(options)
						.format(DecodeFormat.PREFER_RGB_565)
						//.listener(myreqL2.setCrop(opt.getCropTumbnails()))
						.into(itemData.iv);
			}
			
			@Override
			public int getItemCount() {
				return historyCursor.getCount();
			}
		});
		
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		toggleGridView(0);
		return true;
	}
	
	/** 切换网格视图
	 * @param toGrid 0=toggle; 1=normal list;*/
	private void toggleGridView(int toGrid) {
		boolean grid = toGrid==0?recyclerView.getLayoutManager() instanceof GridLayoutManager
				:toGrid==1;
		if(grid) {
			recyclerView.setLayoutManager(new LinearLayoutManager(this));
		} else {
			recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
		}
	}
	
	@Override
	public void onClick(View v) {
		ViewHolder vh = (ViewHolder) v.getTag();
		if(vh.path!=null) {
			setResult(RESULT_OK, new Intent().setData(Uri.parse(vh.path)));
			finish();
		}
	}
	
	static class ViewHolder extends RecyclerView.ViewHolder {
		final ActivityPdocHistoryItemBinding itemData;
		public String path;
		
		public ViewHolder(ActivityPdocHistoryItemBinding itemData, View.OnClickListener listener) {
			super(itemData.v);
			this.itemData = itemData;
			itemData.v.setTag(this);
			itemData.v.setOnClickListener(listener);
		}
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		historyCursor.close();
		historyCon.try_close();
	}
}
