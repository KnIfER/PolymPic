package com.knziha.filepicker.model;

import android.util.Log;

import androidx.preference.CMN;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.ResourceCacheKey;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskCacheWriteLocker;
import com.bumptech.glide.load.engine.cache.SafeKeyGenerator;
import com.knziha.filepicker.utils.CMNF;

import java.io.File;

public class SimpleDiskCache implements DiskCache {
    private final String path;
    private final SafeKeyGenerator safeKeyGenerator;
    private final DiskCacheWriteLocker writeLocker = new DiskCacheWriteLocker();

    public SimpleDiskCache(String _path) {
        CMNF.Log("SimpleDiskCacheSimpleDiskCache");
        path=_path;
        safeKeyGenerator = new SafeKeyGenerator();
    }

    @Override
    public File get(Key key) {
        //new ResourceCacheKey(ek.model);
        String safeKey = safeKeyGenerator.getSafeKey(key);
		File cf = new File(path,safeKey+".0");
		//CMNF.Log("keykeykey",safeKey+" "+key.getClass()+" == "+key);
		if(cf.exists()){
			if(key instanceof ResourceCacheKey){
				Key sk = ((ResourceCacheKey) key).sourceKey;
				if(sk instanceof TimeAffordable){
					long time = ((TimeAffordable) sk).AffordTime();
					//CMN.Log("calibrating time : ", time, cf.lastModified(), time>cf.lastModified(), cf.getAbsolutePath());
					if(time!=-1 && (time==0||time>cf.lastModified())) {
						//ASK: will the disk storage be reoccupied after the path being deleted and recreated?
						//TODO 看下删除并建立同名文件是否会复用原先的物理存储。
						cf.delete();
						return null;
					}
				}
			}
			return cf;
		}
        //CMNF.Log("keykeykey",safeKey+"  "+rck.sourceKey+"  "+rck.toString());
        return null;
    }

    @Override
    public void put(Key key, Writer writer) {
        String safeKey = safeKeyGenerator.getSafeKey(key);
        writeLocker.acquire(safeKey);
        try {
            File file = new File(path,safeKey+".0");
            if (file.exists()) {
                ResourceCacheKey rck = (ResourceCacheKey) key;
                //CMNF.Log("current ",safeKey+"  "+rck.sourceKey+"  "+rck.toString());
                //Log.e("fatal","current "+safeKey+" "+key.getClass());
                return;
            }
            //Log.e("fatal","current writing new");

            writer.write(file);
        }finally {
            writeLocker.release(safeKey);
        }
    }

    @Override
    public void deleteCacheByKey(Key key) {
        //Log.e("fatal","deleteCacheByKey deleteCacheByKey deleteCacheByKey");
    }

    @Override
    public void clear() {
        //Log.e("fatal","clear clear clear");
    }
}
