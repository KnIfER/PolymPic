package com.knziha.polymer.browser.AppIconCover;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.knziha.polymer.Utils.CMN;

import java.io.IOException;

public class AppIconCoverFetcher implements DataFetcher<Drawable> {
	private final AppIconCover model;
	
	public AppIconCoverFetcher(AppIconCover model) {
		this.model = model;
	}

	public AppIconCover getModel() {
		return model;
	}

	@Override
	public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Drawable> callback) {
		//CMN.Log("IconCoverFetcher loadData");
		Drawable dw = null;
		Exception exception = null;
		try {
			dw = model.load();
		} catch (IOException e) {
			exception = e;
			CMN.Log(e);
		}
		if(dw!=null) {
			callback.onDataReady(dw);
		} else {
			if (exception==null) {
				exception = new Exception("load Icon cover fail");
			}
			callback.onLoadFailed(exception);
		}
	}

	@Override public void cleanup() {
	}
	
	@Override public void cancel() {
	}

	@NonNull
	@Override
	public Class<Drawable> getDataClass() {
		return Drawable.class;
	}

	@NonNull
	@Override
	public DataSource getDataSource() {
		return DataSource.LOCAL;
	}
}