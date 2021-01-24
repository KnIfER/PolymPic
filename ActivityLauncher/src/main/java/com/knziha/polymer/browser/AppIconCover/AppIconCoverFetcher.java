package com.knziha.polymer.browser.AppIconCover;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.knziha.polymer.Utils.CMN;

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
		CMN.Log("IconCoverFetcher loadData");
		Drawable dw = model.path.load();
		if(dw!=null) {
			callback.onDataReady(dw);
		} else {
			callback.onLoadFailed(new Exception("load Icon cover fail"));
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