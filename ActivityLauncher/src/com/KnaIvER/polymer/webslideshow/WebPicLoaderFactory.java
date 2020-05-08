package com.KnaIvER.polymer.webslideshow;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

public class WebPicLoaderFactory implements ModelLoaderFactory<WebPic, Bitmap> {

	@NonNull
	@Override
	public ModelLoader<WebPic, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
		return new WebModelLoader();
	}

	@Override
	public void teardown() {

	}
}