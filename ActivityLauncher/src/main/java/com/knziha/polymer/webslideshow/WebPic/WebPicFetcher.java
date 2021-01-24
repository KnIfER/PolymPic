package com.knziha.polymer.webslideshow.WebPic;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.knziha.polymer.AdvancedBrowserWebView;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.database.LexicalDBHelper;


public class WebPicFetcher implements DataFetcher<Bitmap> {
	private final WebPic pic;

	public WebPicFetcher(WebPic model) {
		this.pic = model;
	}

	@Override
	public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
		Bitmap bm = null;
		CMN.Log("WebPicFetcher loadData");
		long st = System.currentTimeMillis();
		SQLiteDatabase db = LexicalDBHelper.getInstancedDb();
		byte[] data=null;
		long tabID_Fetcher = pic.tabID;
		String[] where = new String[]{String.valueOf(tabID_Fetcher)};
		AdvancedBrowserWebView wv = pic.id_table.get(tabID_Fetcher);
		if(wv!=null) {
			bm = wv.saveBitmap();
			if(bm!=null)
			{
				pic.version = wv.version;
			}
		}
		if(bm==null) {
			if(db!=null) {
				Cursor csr = db.rawQuery("select thumbnail from webtabs where id=?", where);
				if(csr.moveToFirst()) {
					data = csr.getBlob(0);
				}
				csr.close();
			}
			if(data!=null) {
				bm = BitmapFactory.decodeByteArray(data, 0, data.length);
				CMN.Log(tabID_Fetcher, "WebPic__已取出...", pic.version, bm.getWidth(), bm.getHeight(), System.currentTimeMillis()-st);
			}
		}
		if(bm!=null) {
			callback.onDataReady(bm);
		} else {
			callback.onLoadFailed(new Exception("load page cover fail"));
		}
	}

	@Override public void cleanup() {
	}
	@Override public void cancel() {
	}

	@NonNull
	@Override
	public Class<Bitmap> getDataClass() {
		return Bitmap.class;
	}

	@NonNull
	@Override
	public DataSource getDataSource() {
		return DataSource.LOCAL;
	}
}