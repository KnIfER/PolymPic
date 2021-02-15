package com.knziha.polymer;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.AppIconCover.AppIconCover;
import com.knziha.polymer.browser.AppIconCover.AppIconCoverLoaderFactory;
import com.knziha.polymer.pdviewer.pagecover.PageCover;
import com.knziha.polymer.pdviewer.pagecover.PageCoverLoaderFactory;
import com.knziha.polymer.webslideshow.WebPic.WebPic;
import com.knziha.polymer.webslideshow.WebPic.WebPicLoaderFactory;

import static com.knziha.polymer.Utils.CMN.dummyWV;

public class AgentApplication extends Application {
	static {
		GlideCacheModule.mOnGlideRegistry =
				registry -> {
					registry.append(WebPic.class, Bitmap.class, new WebPicLoaderFactory());
					registry.append(PageCover.class, Bitmap.class, new PageCoverLoaderFactory());
					registry.append(AppIconCover.class, Drawable.class, new AppIconCoverLoaderFactory());
				};
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		System.exit(0);
	}

	public void clearNonsenses() {
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if(dummyWV==null) {
			dummyWV = new WebViewHolder(getApplicationContext());
		}
	}
	
	static class WebViewHolder extends android.webkit.WebView {
		Object tag;
		public WebViewHolder(@NonNull Context context) {
			super(context);
		}
		
		@Override
		public void setTag(Object tag) {
			this.tag = tag;
		}
		
		@Override
		public Object getTag() {
			Object ret = tag;
			try {
				CMN.lock.unlock();
			} catch (Exception ignored) { }
			return ret;
		}
	}
}