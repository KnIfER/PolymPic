package com.knziha.filepicker.model;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;

import java.io.InputStream;

public class AudioCoverLoaderFactory implements ModelLoaderFactory<AudioCover, InputStream> {

	@NonNull
	@Override
	public ModelLoader<AudioCover, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
		return new AudioModelLoader();
	}

	@Override
	public void teardown() {

	}
}