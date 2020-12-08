package com.knziha.polymer;

import android.app.Application;
import android.graphics.Bitmap;

import com.knziha.polymer.pdviewer.pagecover.PageCover;
import com.knziha.polymer.pdviewer.pagecover.PageCoverLoaderFactory;
import com.knziha.polymer.webslideshow.WebPic;
import com.knziha.polymer.webslideshow.WebPicLoaderFactory;
import com.knziha.filepicker.model.GlideCacheModule;

public class AgentApplication extends Application {
	static {
		GlideCacheModule.mOnGlideRegistry =
				registry -> {
					registry.append(WebPic.class, Bitmap.class, new WebPicLoaderFactory());
					registry.append(PageCover.class, Bitmap.class, new PageCoverLoaderFactory());
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