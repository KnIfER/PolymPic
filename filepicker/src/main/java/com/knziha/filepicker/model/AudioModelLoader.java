package com.knziha.filepicker.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;

import java.io.InputStream;

public class AudioModelLoader implements ModelLoader<AudioCover, InputStream> {
	@Nullable
	@Override
	public LoadData<InputStream> buildLoadData(@NonNull AudioCover audioCover, int width, int height, @NonNull Options options) {
		return new ModelLoader.LoadData<>(new AudioCoverSignature(audioCover.path), new AudioCoverFetcher(audioCover));
	}

	@Override
	public boolean handles(@NonNull AudioCover audioCover) {
		return true;
	}
}