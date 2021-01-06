package com.knziha.polymer;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.databinding.HistoryBinding;
import com.knziha.polymer.databinding.HistoryItemBinding;
import com.knziha.polymer.widgets.Utils;

import static com.knziha.polymer.widgets.Utils.EmptyCursor;

public class BrowserHistory extends DialogFragment {
	HistoryBinding UIData;
	RecyclerView histroyRv;
	Cursor cursor = EmptyCursor;
	private RecyclerView.Adapter<ViewHolder> adaptermy;
	
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(UIData==null) {
			UIData = HistoryBinding.inflate(inflater, container, false);
			histroyRv = UIData.historyRv;
			histroyRv.setAdapter(adaptermy=new RecyclerView.Adapter<ViewHolder>() {
				@NonNull
				@Override
				public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
					ViewHolder vh = new ViewHolder(HistoryItemBinding.inflate(inflater));
					
					return vh;
				}
				
				@Override
				public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
					cursor.moveToPosition(position);
					
				}
				
				@Override
				public int getItemCount() {
					return cursor.getCount();
				}
			});
		} else {
			Utils.removeIfParentBeOrNotBe(UIData.history, null, false);
		}
		return UIData.history;
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
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
	}
	
}
