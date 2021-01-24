package com.knziha.polymer.browser.AppIconCover;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public class AppIconCoverLoaderFactory implements ModelLoaderFactory<AppIconCover, Drawable> {

	@NonNull
	@Override
	public ModelLoader<AppIconCover, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
		return new AppIconModelLoader();
	}

	@Override
	public void teardown() {

	}
}