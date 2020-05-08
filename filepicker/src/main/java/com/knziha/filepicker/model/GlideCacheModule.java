package com.knziha.filepicker.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.AppGlideModule;
import com.knziha.filepicker.utils.CMNF;

import java.io.File;
import java.io.InputStream;

/** Shared with main program */
@GlideModule
public class GlideCacheModule extends AppGlideModule {
	public static String DEFAULT_GLIDE_PATH;
	public static OnGlideRegistry mOnGlideRegistry;
	public interface OnGlideRegistry{
		void OnGlideRegistry(Registry registry);
	}

	@Override
	public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
		super.registerComponents(context, glide, registry);
		registry.append(AudioCover.class, InputStream.class, new AudioCoverLoaderFactory());
		if(mOnGlideRegistry!=null) mOnGlideRegistry.OnGlideRegistry(registry);
	}

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);
		SharedPreferences defaultReader = PreferenceManager.getDefaultSharedPreferences(context);
		String path = defaultReader.getString("cache_p", DEFAULT_GLIDE_PATH==null?context.getExternalCacheDir().getAbsolutePath()+"/thumnails/":DEFAULT_GLIDE_PATH);
		boolean bUseLruDiskCache = false;//(defaultReader.getLong("MSF", 0) & 0x100) == 0;
		//CMNF.Log("applyOptions", bUseLruDiskCache + " : " + path);
        builder.setDiskCache(bUseLruDiskCache?
                new GoodDiskCacheFactory(path, defaultReader.getInt("cache_s", 256) * 1024 * 1024)://300*1024 DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE
                new GoodDiskCacheFactoryForever(path));
        builder.setLogLevel(Log.ERROR);
    }

    class GoodDiskCacheFactory extends DiskLruCacheFactory {
        public GoodDiskCacheFactory(String p, long diskCacheSize) {
            super(() -> new File(p), diskCacheSize);
        }
    }

    class GoodDiskCacheFactoryForever implements  DiskCache.Factory{
		String path;
        public GoodDiskCacheFactoryForever(String p) {
        	path=p;
        }
        @Override
		public DiskCache build() {
            return new SimpleDiskCache(path);
        }
    }
}
