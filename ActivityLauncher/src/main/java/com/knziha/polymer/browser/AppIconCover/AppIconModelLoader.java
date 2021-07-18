package com.knziha.polymer.browser.AppIconCover;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;

public class AppIconModelLoader implements ModelLoader<AppIconCover, Drawable> {
	@Nullable
	@Override
	public LoadData<Drawable> buildLoadData(@NonNull AppIconCover appIconCover, int width, int height, @NonNull Options options) {
		return new LoadData<>(new AppIconCoverSignature(appIconCover), new AppIconCoverFetcher(appIconCover));
	}

	@Override
	public boolean handles(@NonNull AppIconCover audioCover) {
		return true;
	}
}