package com.knziha.polymer.webstorage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.browser.AppIconCover.AppIconCover;
import com.knziha.polymer.browser.AppIconCover.AppLoadableBean;
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

import java.io.IOException;
import java.util.Date;

import static com.knziha.polymer.webslideshow.ImageViewTarget.FuckGlideDrawable;
import static com.knziha.polymer.widgets.Utils.EmptyCursor;

public class WebAnnotationPanel extends SettingsPanel {
	private BrowserActivity a;
	WebAnnotationTextBinding UIData;
	
	PagingAdapterInterface<WebAnnotationCursorReader> dataAdapter;
	ImageView pageAsyncLoader;
	
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
	
	public WebAnnotationPanel(Context context, ViewGroup root, int bottomPaddding, Options opt) {
		super(context, root, bottomPaddding, opt, (BrowserActivity) context);
	}
	
	static class ViewHolder extends RecyclerView.ViewHolder {
		final HistoryItemBinding itemData;
		Object tag;
		public ViewHolder(HistoryItemBinding itemData) {
			super(itemData.root);
			this.itemData = itemData;
			itemData.root.setTag(this);
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
			ret.itemData.icon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ViewHolder vh = (ViewHolder) Utils.getViewHolderInParents(v);
					WebAnnotationCursorReader reader = (WebAnnotationCursorReader) vh.tag;
					if (reader!=null) {
						a.NavigateToTab(reader.tab_id);
						//a.showT(reader.tab_id);
					}
				}
			});
			return ret;
		}
		
		@Override
		public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
			WebAnnotationCursorReader reader = dataAdapter.getReaderAt(position);
			holder.tag = reader;
			if (reader==null) {
				holder.itemData.title.setText("加载中 ……");
				holder.itemData.icon.setVisibility(View.INVISIBLE);
			} else {
				holder.itemData.title.setText(reader.title);
				if (reader.time_text == null) {
					date.setTime(reader.sort_number);
					reader.time_text = date.toLocaleString();
				}
				holder.itemData.subtitle.setText(reader.time_text);
				holder.itemData.icon.setVisibility(reader.tab_id>0?View.VISIBLE:View.INVISIBLE);
			}
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
	
	boolean init = false;
	
	
	@Override
	protected void init(Context context, ViewGroup root) {
		dataAdapter = new CursorAdapter<>(EmptyCursor, new WebAnnotationCursorReader());
		mBackgroundColor = Color.WHITE;
		a=(BrowserActivity) context;
		showInPopWindow = true;
		shouldWrapInScrollView = false;
		UIData = WebAnnotationTextBinding.inflate(a.getLayoutInflater());
		
		pageAsyncLoader = new ImageView(a);
		
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
			Cursor cursor = db.rawQuery("SELECT id,creation_time,text,tab_id FROM annott ORDER BY creation_time desc", null);
			CMN.Log("查询个数::"+cursor.getCount());
			dataAdapter = new CursorAdapter<>(cursor, new WebAnnotationCursorReader());
			ada.notifyDataSetChanged();
		} else {
			PagingCursorAdapter<WebAnnotationCursorReader> dataAdapter = new PagingCursorAdapter<>(db
					, new SimpleClassConstructor<>(WebAnnotationCursorReader.class)
					, length -> new WebAnnotationCursorReader[length]);
			
			if(!init) {
				RequestBuilder<Drawable> glide = Glide.with(a).load(new AppIconCover(new AppLoadableBean() {
					@Override
					public Drawable load() throws IOException {
						//try { Thread.sleep(200); } catch (InterruptedException ignored) { }
						dataAdapter.startPaging(last_visible_entry_time, 20);
						return FuckGlideDrawable;
					}
				}))
				.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
				.skipMemoryCache(true)
				.diskCacheStrategy(DiskCacheStrategy.NONE)
				.listener(new RequestListener<Drawable>() {
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
						//a.showT("onLoadFailed！");
						CMN.Log(e);
						return false;
					}
					
					@Override
					public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
						//a.showT("onResourceReady！");
						ada.notifyDataSetChanged();
						return true;
					}
				});
				
				dataAdapter.bindTo(recyclerView)
						//.setAsyncLoader(glide, pageAsyncLoader)
						.sortBy("annott", "creation_time", true, "text,tab_id");
				
				glide.into(pageAsyncLoader);

				this.dataAdapter = dataAdapter;
				init = true;
			}
		}
		
		settingsLayout = (ViewGroup) UIData.getRoot();
	}
	
	@Override
	public boolean toggle(ViewGroup root) {
		boolean ret = super.toggle(root);
		return ret;
	}
	
	static long last_visible_entry_time = 0;
	
	@Override
	protected void onDismiss() {
		super.onDismiss();
		a.toggleMenuGrid(false);
		View ca = recyclerView.getChildAt(0);
		if (ca!=null) {
			ViewHolder holder = (ViewHolder) ca.getTag();
			WebAnnotationCursorReader reader = (WebAnnotationCursorReader) holder.tag;
			last_visible_entry_time = reader.sort_number;
//			a.showT(new Date(last_visible_entry_time).toLocaleString());
		}
	}
}
