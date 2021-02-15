package com.tencent.smtt.sdk;

import android.content.Context;
import android.os.Bundle;
import com.tencent.smtt.export.external.DexLoader;

class TbsVideoCacheTaskEngine {
   private DexLoader dexLoader = null;
   private Object tbsVideoCacheTaskProxy = null;

   public TbsVideoCacheTaskEngine(DexLoader var1) {
      this.dexLoader = var1;
   }

   public Object getTbsVideoCacheTaskProxy(Context var1, Object var2, Bundle var3) {
      if (this.dexLoader != null) {
         this.tbsVideoCacheTaskProxy = this.dexLoader.newInstance("com.tencent.tbs.cache.TbsVideoCacheTaskProxy", new Class[]{Context.class, Object.class, Bundle.class}, var1, var2, var3);
      }

      return this.tbsVideoCacheTaskProxy;
   }

   public void pauseTask() {
      if (this.dexLoader != null) {
         this.dexLoader.invokeMethod(this.tbsVideoCacheTaskProxy, "com.tencent.tbs.cache.TbsVideoCacheTaskProxy", "pauseTask", new Class[0]);
      }

   }

   public void resumeTask() {
      if (this.dexLoader != null) {
         this.dexLoader.invokeMethod(this.tbsVideoCacheTaskProxy, "com.tencent.tbs.cache.TbsVideoCacheTaskProxy", "resumeTask", new Class[0]);
      }

   }

   public void stopTask() {
      if (this.dexLoader != null) {
         this.dexLoader.invokeMethod(this.tbsVideoCacheTaskProxy, "com.tencent.tbs.cache.TbsVideoCacheTaskProxy", "stopTask", new Class[0]);
      }

   }

   public void removeTask(boolean var1) {
      if (this.dexLoader != null) {
         this.dexLoader.invokeMethod(this.tbsVideoCacheTaskProxy, "com.tencent.tbs.cache.TbsVideoCacheTaskProxy", "removeTask", new Class[]{Boolean.TYPE}, var1);
      }

   }

   public long getContentLength() {
      if (this.dexLoader != null) {
         Object var1 = this.dexLoader.invokeMethod(this.tbsVideoCacheTaskProxy, "com.tencent.tbs.cache.TbsVideoCacheTaskProxy", "getContentLength", new Class[0]);
         if (var1 instanceof Long) {
            return (Long)var1;
         }
      }

      return 0L;
   }

   public int getDownloadedSize() {
      if (this.dexLoader != null) {
         Object var1 = this.dexLoader.invokeMethod(this.tbsVideoCacheTaskProxy, "com.tencent.tbs.cache.TbsVideoCacheTaskProxy", "getDownloadedSize", new Class[0]);
         if (var1 instanceof Integer) {
            return (Integer)var1;
         }
      }

      return 0;
   }

   public int getProgress() {
      if (this.dexLoader != null) {
         Object var1 = this.dexLoader.invokeMethod(this.tbsVideoCacheTaskProxy, "com.tencent.tbs.cache.TbsVideoCacheTaskProxy", "getProgress", new Class[0]);
         if (var1 instanceof Integer) {
            return (Integer)var1;
         }
      }

      return 0;
   }
}
