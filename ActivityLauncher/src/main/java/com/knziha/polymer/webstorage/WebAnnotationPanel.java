package com.knziha.polymer.webstorage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.databinding.HistoryItemBinding;
import com.knziha.polymer.databinding.WebAnnotationTextBinding;
import com.knziha.polymer.paging.CursorAdapter;
import com.knziha.polymer.paging.CursorReader;
import com.knziha.polymer.paging.PagingAdapterInterface;
import com.knziha.polymer.paging.PagingCursorAdapter;
import com.knziha.polymer.paging.PagingRecyclerView;
import com.knziha.polymer.paging.SimpleClassConstructor;
import com.knziha.polymer.webslideshow.ViewUtils;
import com.knziha.polymer.widgets.Utils;

import java.util.Date;

import static com.knziha.polymer.widgets.Utils.EmptyCursor;

public class WebAnnotationPanel extends BrowserAppPanel {
	static long last_visible_entry_time = 0;
	
	WebAnnotationTextBinding UIData;
	PagingAdapterInterface<WebAnnotationCursorReader> dataAdapter;
	ImageView pageAsyncLoader;
	
	PagingRecyclerView mAnnotsListView;
	Date date = new Date();
	
	public static class WebAnnotationCursorReader implements CursorReader {
		long row_id;
		long sort_number;
		long tab_id;
		String title;
		String time_text;
		@Override
		public void ReadCursor(Cursor cursor, long rowID, long sortNum) {
			title = cursor.getString(2);
			tab_id = cursor.getLong(3);
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
	
	public WebAnnotationPanel(BrowserActivity a) {
		super(a);
	}
	
	protected class AppDataTimeRenownedBaseAdapter extends RecyclerView.Adapter<ViewUtils.ViewDataHolder<HistoryItemBinding>> {
		final LayoutInflater inflater;
		AppDataTimeRenownedBaseAdapter(LayoutInflater inflater) {
			this.inflater = inflater;
		}
		@NonNull @Override
		public ViewUtils.ViewDataHolder<HistoryItemBinding> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			ViewUtils.ViewDataHolder<HistoryItemBinding> ret = new ViewUtils.ViewDataHolder<>(HistoryItemBinding.inflate(inflater, parent, false));
			ret.data.title.setSingleLine();
			ret.data.icon.setOnClickListener(WebAnnotationPanel.this);
			return ret;
		}
		
		@Override
		public void onBindViewHolder(@NonNull ViewUtils.ViewDataHolder<HistoryItemBinding> holder, int position) {
			WebAnnotationCursorReader reader = dataAdapter.getReaderAt(position);
			holder.tag = reader;
			HistoryItemBinding viewData = holder.data;
			viewData.title.setTextSize(16.5f);
			if (reader==null) {
				viewData.title.setText("加载中 ……");
				viewData.icon.setVisibility(View.INVISIBLE);
			} else {
				viewData.title.setText(reader.title);
				if (reader.time_text == null) {
					date.setTime(reader.sort_number);
					reader.time_text = date.toLocaleString();
				}
				viewData.subtitle.setText(reader.time_text);
				viewData.icon.setVisibility(reader.tab_id>0?View.VISIBLE:View.INVISIBLE);
			}
		}
		
		@Override
		public int getItemCount() {
			return dataAdapter.getCount();
		}
	}
	
	@Override
	protected void init(Context context, ViewGroup root) {
		dataAdapter = new CursorAdapter<>(EmptyCursor, new WebAnnotationCursorReader());
		a=(BrowserActivity) context;
		showInPopWindow = true;
		showPopOnAppbar = true;
		
		UIData = WebAnnotationTextBinding.inflate(a.getLayoutInflater());
		settingsLayout = (ViewGroup) UIData.getRoot();
		pageAsyncLoader = new ImageView(a);
		
		//settingsLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
		
		mBackgroundColor = Color.WHITE;
		
		PagingRecyclerView recyclerView = UIData.annotsRv;
		recyclerView.setNestedScrollingEnabled(false);
		recyclerView.setLayoutManager(new LinearLayoutManager(a.getLayoutInflater().getContext()));
		recyclerView.setItemAnimator(null);
		recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
		recyclerView.setHasFixedSize(true);
		AppDataTimeRenownedBaseAdapter ada = new AppDataTimeRenownedBaseAdapter(a.getLayoutInflater());
		ada.setHasStableIds(false);
		recyclerView.setAdapter(ada);
		recyclerView.setRecycledViewPool(Utils.MaxRecyclerPool(35));
		this.mAnnotsListView = recyclerView;
		
		SQLiteDatabase db = LexicalDBHelper.getInstancedDb();
		
		if (db!=null) {
			boolean bSingleThreadLoading = false;
			if (bSingleThreadLoading) {
				Cursor cursor = db.rawQuery("SELECT id,creation_time,text,tab_id FROM annott ORDER BY creation_time desc", null);
				CMN.Log("查询个数::"+cursor.getCount());
				dataAdapter = new CursorAdapter<>(cursor, new WebAnnotationCursorReader());
				ada.notifyDataSetChanged();
			} else {
				PagingCursorAdapter<WebAnnotationCursorReader> dataAdapter = new PagingCursorAdapter<>(db
						, new SimpleClassConstructor<>(WebAnnotationCursorReader.class)
						, WebAnnotationCursorReader[]::new);
				this.dataAdapter = dataAdapter;
				dataAdapter.bindTo(recyclerView)
						.setAsyncLoader(a, pageAsyncLoader)
						.sortBy(LexicalDBHelper.TABLE_ANNOTS_TEXT, LexicalDBHelper.FIELD_CREATE_TIME, true, "text,tab_id")
						.startPaging(last_visible_entry_time, 20, 15);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.root) {
			ViewUtils.ViewDataHolder<HistoryItemBinding> vh = (ViewUtils.ViewDataHolder<HistoryItemBinding>) Utils.getViewHolderInParents(v);
			WebAnnotationCursorReader reader = (WebAnnotationCursorReader) vh.tag;
			if (reader!=null) {
				a.NavigateToTab(reader.tab_id);
				//a.showT(reader.tab_id);
			}
		}
	}
	
//	@Override
//	public boolean toggle(ViewGroup root) {
//		boolean ret = super.toggle(root);
////		if(!ret) {
////			a.currentViewImpl.setVisibility(View.VISIBLE);
////		}
//		return ret;
//	}
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		View ca = mAnnotsListView.getChildAt(0);
		if (ca!=null) {
			ViewUtils.ViewDataHolder<HistoryItemBinding> holder = (ViewUtils.ViewDataHolder<HistoryItemBinding>) ca.getTag();
			WebAnnotationCursorReader reader = (WebAnnotationCursorReader) holder.tag;
			last_visible_entry_time = reader.sort_number;
//			a.showT(new Date(last_visible_entry_time).toLocaleString());
		}
	}
	
	@Override
	protected void decorateInterceptorListener(boolean install) {
		if (install) {
			a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess_ic_back);
			a.UIData.browserWidget8.setImageResource(R.drawable.ic_baseline_book_24);
		} else {
			a.UIData.browserWidget7.setImageResource(R.drawable.chevron_recess);
			a.UIData.browserWidget8.setImageResource(R.drawable.chevron_forward);
		}
	}
}
