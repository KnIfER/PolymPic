package com.KnaIvER.polymer.webslideshow;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;

public class WebModelLoader implements ModelLoader<WebPic, Bitmap> {
	@Nullable
	@Override
	public LoadData<Bitmap> buildLoadData(@NonNull WebPic mddPic, int width, int height, @NonNull Options options) {
		return new LoadData<>(new WebPicSignature(mddPic), new WebPicFetcher(mddPic));
	}

	@Override
	public boolean handles(@NonNull WebPic mddPic) {
		return true;
	}
}