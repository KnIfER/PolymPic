package com.knziha.polymer.pdviewer.pagecover;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public class PageCoverLoaderFactory implements ModelLoaderFactory<PageCover, Bitmap> {

	@NonNull
	@Override
	public ModelLoader<PageCover, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
		return new PageModelLoader();
	}

	@Override
	public void teardown() {

	}
}