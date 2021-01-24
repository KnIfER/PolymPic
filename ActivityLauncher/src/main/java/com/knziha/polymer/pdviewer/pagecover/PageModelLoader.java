package com.knziha.polymer.pdviewer.pagecover;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;

public class PageModelLoader implements ModelLoader<PageCover, Bitmap> {
	@Nullable
	@Override
	public LoadData<Bitmap> buildLoadData(@NonNull PageCover pageCover, int width, int height, @NonNull Options options) {
		return new LoadData<>(new PageCoverSignature(pageCover.path), new PageCoverFetcher(pageCover));
	}

	@Override
	public boolean handles(@NonNull PageCover audioCover) {
		return true;
	}
}