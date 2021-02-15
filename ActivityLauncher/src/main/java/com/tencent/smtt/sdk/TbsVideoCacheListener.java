package com.tencent.smtt.sdk;

import android.os.Bundle;

public interface TbsVideoCacheListener {
   void onVideoDownloadInit(TbsVideoCacheTask var1, int var2, String var3, Bundle var4);

   void onVideoDownloadStart(TbsVideoCacheTask var1, Bundle var2);

   void onVideoDownloadProgress(TbsVideoCacheTask var1, int var2, long var3, int var5, Bundle var6);

   void onVideoDownloadCompletion(TbsVideoCacheTask var1, long var2, long var4, Bundle var6);

   void onVideoDownloadError(TbsVideoCacheTask var1, int var2, String var3, Bundle var4);
}
