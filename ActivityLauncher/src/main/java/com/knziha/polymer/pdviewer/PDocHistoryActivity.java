package com.knziha.polymer.pdviewer;

import android.database.Cursor;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.ActivityPdocHistoryBinding;
import com.knziha.polymer.databinding.ActivityPdocHistoryItemBinding;
import com.knziha.polymer.pdviewer.pagecover.PageCover;

public class PDocHistoryActivity extends Toastable_Activity {
	
	private LexicalDBHelper historyCon;
	private ActivityPdocHistoryBinding UIData;
	
	Cursor historyCursor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		historyCon = LexicalDBHelper.connectInstance(this);
		
		UIData = DataBindingUtil.setContentView(this, R.layout.activity_pdoc_history);
		
		RecyclerView rv = UIData.rv;
		
		rv.setLayoutManager(new LinearLayoutManager(this));
		
		historyCursor = historyCon.queryPdocHistory();
		
		rv.setAdapter(new RecyclerView.Adapter() {
			@NonNull
			@Override
			public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
				return new ViewHolder(ActivityPdocHistoryItemBinding.inflate(inflater, parent, false));
			}
			
			@Override
			public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
				
				ViewHolder viewholder = (ViewHolder) holder;
				
				historyCursor.moveToPosition(position);
				
				PageCover model = new PageCover(getContentResolver(), historyCursor.getString(2), historyCursor.getInt(0), dm);
				
				ActivityPdocHistoryItemBinding itemData = viewholder.itemData;
				itemData.tv.setText(historyCursor.getString(1));
				Priority priority = Priority.HIGH;
				RequestOptions options = new RequestOptions()
						.signature(new ObjectKey(model.path))
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
	
	static class ViewHolder extends RecyclerView.ViewHolder {
		final ActivityPdocHistoryItemBinding itemData;
		public ViewHolder(ActivityPdocHistoryItemBinding itemData) {
			super(itemData.v);
			this.itemData = itemData;
		}
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		historyCon.try_close();
	}
}
