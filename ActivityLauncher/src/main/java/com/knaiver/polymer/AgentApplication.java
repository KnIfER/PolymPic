package com.knaiver.polymer;

import android.app.Application;
import android.graphics.Bitmap;

import com.knaiver.polymer.webslideshow.WebPic;
import com.knaiver.polymer.webslideshow.WebPicLoaderFactory;
import com.knziha.filepicker.model.GlideCacheModule;

public class AgentApplication extends Application {
	static {
		GlideCacheModule.mOnGlideRegistry =
				registry -> {
					registry.append(WebPic.class, Bitmap.class, new WebPicLoaderFactory());
				};
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		System.exit(0);
	}

	public void clearNonsenses() {
	}
}