package com.knziha.polymer.webstorage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.HistoryItemBinding;
import com.knziha.polymer.databinding.WebAnnotationTextBinding;
import com.knziha.polymer.paging.CursorAdapter;
import com.knziha.polymer.paging.CursorReader;
import com.knziha.polymer.paging.PagingAdapterInterface;
import com.knziha.polymer.paging.PagingCursorAdapter;
import com.knziha.polymer.paging.PagingRecyclerView;
import com.knziha.polymer.paging.SimpleClassConstructor;
import com.knziha.polymer.preferences.SettingsPanel;
import com.knziha.polymer.widgets.Utils;

import java.util.Date;

import static com.knziha.polymer.widgets.Utils.EmptyCursor;

public class WebAnnotationPanel extends SettingsPanel {
	private BrowserActivity a;
	WebAnnotationTextBinding UIData;
	
	public static class WebAnnotationCursorReader implements CursorReader {
		long row_id;
		long sort_number;
		String title;
		String time_text;
		@Override
		public void ReadCursor(Cursor cursor, long rowID, long sortNum) {
			title = cursor.getString(2);
			if (row_id!=-1) {
				row_id = rowID;
				sort_number = sortNum;
			} else {
				row_id = cursor.getLong(0);
				sort_number = cursor.getLong(1);
			}
		}
		
		@Override
		public String toString() {
			return "WebAnnotationCursorReader{" +
					"title='" + title + '\'' +
					'}';
		}
	}
	PagingAdapterInterface<WebAnnotationCursorReader> dataAdapter;
	
	public WebAnnotationPanel(Context context, ViewGroup root, int bottomPaddding, Options opt) {
		super(context, root, bottomPaddding, opt, (BrowserActivity) context);
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
	
	Date date = new Date();
	
	protected class AppDataTimeRenownedBaseAdapter extends RecyclerView.Adapter<ViewHolder> {
		final LayoutInflater inflater;
		AppDataTimeRenownedBaseAdapter(LayoutInflater inflater) {
			this.inflater = inflater;
		}
		@NonNull @Override
		public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			ViewHolder ret = new ViewHolder(HistoryItemBinding.inflate(inflater, parent, false));
			ret.itemData.title.setSingleLine();
			return ret;
		}
		
		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			WebAnnotationCursorReader reader = dataAdapter.getReaderAt(position);
			holder.tag = reader;
			holder.itemData.title.setText(reader.row_id + " " + reader.title);
			if (reader.time_text == null) {
				date.setTime(reader.sort_number);
				reader.time_text = date.toLocaleString();
			}
			holder.itemData.subtitle.setText(reader.time_text);
		}
		
		@Override
		public int getItemCount() {
			return dataAdapter.getCount();
		}
	}
	
	@Override
	protected void showPop() {
		if (pop==null) {
			pop = new PopupWindow(a);
			pop.setContentView(settingsLayout);
		}
		a.embedPopInCoordinatorLayout(pop);
	}
	
	RecyclerView.Adapter ada;
	PagingRecyclerView recyclerView;
	
	
	@Override
	protected void init(Context context, ViewGroup root) {
		dataAdapter = new CursorAdapter<>(EmptyCursor, new WebAnnotationCursorReader());
		mBackgroundColor = Color.WHITE;
		a=(BrowserActivity) context;
		showInPopWindow = true;
		shouldWrapInScrollView = false;
		UIData = WebAnnotationTextBinding.inflate(a.getLayoutInflater());
		
		recyclerView = UIData.annotsRv;
		
		Utils.replaceView(recyclerView, UIData.annotsRv);
		
		LinearLayoutManager ll;
		recyclerView.setLayoutManager(ll = new LinearLayoutManager(a.getLayoutInflater().getContext()));
		recyclerView.setItemAnimator(null);
		recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
		recyclerView.setHasFixedSize(true);
		ada = new AppDataTimeRenownedBaseAdapter(a.getLayoutInflater());
		ada.setHasStableIds(false);
		recyclerView.setAdapter(ada);
		
		SQLiteDatabase db = LexicalDBHelper.getInstancedDb();
		
		if (false) {
			Cursor cursor = db.rawQuery("SELECT id,creation_time,text FROM annott ORDER BY creation_time desc", null);
			CMN.Log("查询个数::"+cursor.getCount());
			dataAdapter = new CursorAdapter<>(cursor, new WebAnnotationCursorReader());
		} else {
			dataAdapter = new PagingCursorAdapter<>(db
					, new SimpleClassConstructor<>(WebAnnotationCursorReader.class)
					, length -> new WebAnnotationCursorReader[length]);
			
			dataAdapter.bindTo(recyclerView);
			
			dataAdapter.startPaging(last_visible_entry_time, 20);
		}
		
		ada.notifyDataSetChanged();
		
		settingsLayout = (ViewGroup) UIData.getRoot();
	}
	
	static long last_visible_entry_time = 0;
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		View ca = recyclerView.getChildAt(0);
		if (ca!=null) {
			ViewHolder holder = (ViewHolder) ca.getTag();
			WebAnnotationCursorReader reader = (WebAnnotationCursorReader) holder.tag;
			last_visible_entry_time = reader.sort_number;
			a.showT(new Date(last_visible_entry_time).toLocaleString());
		}
	}
}
