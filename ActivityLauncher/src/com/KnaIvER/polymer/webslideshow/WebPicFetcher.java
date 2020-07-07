package com.KnaIvER.polymer.webslideshow;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.KnaIvER.polymer.Utils.CMN;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;


public class WebPicFetcher implements DataFetcher<Bitmap> {

	private final WebPic model;

	public WebPicFetcher(WebPic model) {
		this.model = model;
	}

	public WebPic getModel() {
		return model;
	}

	@Override
	public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
		String err=null;
		try {
			Bitmap res = model.createBitMap();
			if(res!=null) {
				callback.onDataReady(res);
				return;
			}
		} catch (Exception e) {
			err=e.toString();
			CMN.Log(e);
		}
		//CMN.Log(err, err==null);
		callback.onLoadFailed(new Exception("load mdd picture fail"+err));
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