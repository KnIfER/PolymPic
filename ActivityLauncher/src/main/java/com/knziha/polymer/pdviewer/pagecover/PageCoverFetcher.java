package com.knziha.polymer.pdviewer.pagecover;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.pdviewer.PDocView;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.widgets.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.knziha.polymer.pdviewer.PDocument.initFlag;

public class PageCoverFetcher implements DataFetcher<Bitmap> {

	private final PageCover model;
	PDocument pdoc;
	
	public PageCoverFetcher(PageCover model) {
		this.model = model;
	}

	public PageCover getModel() {
		return model;
	}

	@Override
	public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
		Bitmap bm = null;
		CMN.Log("PageCoverFetcher loadData");
		SQLiteDatabase db = LexicalDBHelper.getInstancedDb();
		byte[] data=null;
		String[] where = new String[]{String.valueOf(model.rowID)};
		boolean insert = false;
		if(db!=null) {
			Cursor csr = db.rawQuery("select thumbnail from pdoc where id=?", where);
			if(csr.moveToFirst()) {
				data = csr.getBlob(0);
			}
			insert = true;
			csr.close();
		}
		if(data!=null) {
			bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			CMN.Log("已取出...", bm.getWidth(), bm.getHeight());
		}
		if(bm==null) {
			Uri path = Uri.parse(model.path);
			String key = Utils.getRunTimePath(path);
			PDocument pdoc = PDocView.books.get(key);
			boolean responsibleForThisBook = false;
			if(pdoc==null) {
				try {
					this.pdoc = pdoc = new PDocument(model.contentResolver, path, model.dm, initFlag);
					responsibleForThisBook = true;
				} catch (IOException e) {
					CMN.Log(e);
				}
			}
			if(pdoc!=null) {
				if(pdoc._num_entries>0) {
					PDocument.PDocPage page = pdoc.mPDocPages[0];
					float scale = pdoc.ThumbsLoResFactor;
					int w = (int) (scale*page.size.getWidth());
					int h = (int) (scale*page.size.getHeight());
					bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
					pdoc.drawTumbnail(bm, 0, scale);
				}
				if(responsibleForThisBook) {
					pdoc.close();
					this.pdoc = null;
				}
			}
		} else {
			insert=false;
		}
		if(bm!=null) {
			callback.onDataReady(bm);
		} else {
			callback.onLoadFailed(new Exception("load page cover fail"));
		}
		if(insert) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream((int) (bm.getAllocationByteCount()*0.75));
			bm.compress(Bitmap.CompressFormat.PNG, 75, bos);
			ContentValues values = new ContentValues();
			data = bos.toByteArray();
			values.put("thumbnail", data);
			db.update("pdoc", values, "id=?", where);
			CMN.Log("已插入...", data.length);
		}
	}

	@Override public void cleanup() {
		try {
			if(pdoc!=null) pdoc.close();
		} catch (Exception ignored) { }
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