package com.tencent.smtt.sdk;

import android.content.Context;
import android.os.Bundle;
import com.tencent.smtt.export.external.DexLoader;

public class TbsVideoCacheTask {
   Context a = null;
   TbsVideoCacheListener b = null;
   private boolean c = false;
   public static final String KEY_VIDEO_CACHE_PARAM_URL = "url";
   public static final String KEY_VIDEO_CACHE_PARAM_FILENAME = "filename";
   public static final String KEY_VIDEO_CACHE_PARAM_FOLDERPATH = "folderPath";
   public static final String KEY_VIDEO_CACHE_PARAM_HEADER = "header";
   private TbsVideoCacheTaskEngine d = null;
   private String e;
   private String f;
   private Object g = null;

   public TbsVideoCacheTask(Context var1, Bundle var2, TbsVideoCacheListener var3) {
      this.a = var1;
      this.b = var3;
      if (var2 != null) {
         this.e = var2.getString("taskId");
         this.f = var2.getString("url");
      }

      this.a(var2);
   }

   public String getTaskID() {
      return this.e;
   }

   public String getTaskUrl() {
      return this.f;
   }

   private void a(Bundle var1) {
      if (this.d == null) {
         SDKEngine.getInstance(true).init(this.a, false, false);
         TbsWizard var2 = SDKEngine.getInstance(true).a();
         DexLoader var3 = null;
         if (var2 != null) {
            var3 = var2.getDexLoader();
         } else {
            this.b.onVideoDownloadError(this, -1, "init engine error!", (Bundle)null);
         }

         if (var3 != null) {
            this.d = new TbsVideoCacheTaskEngine(var3);
         } else {
            this.b.onVideoDownloadError(this, -1, "Java dexloader invalid!", (Bundle)null);
         }
      }

      if (this.d != null) {
         this.g = this.d.getTbsVideoCacheTaskProxy(this.a, this, var1);
         if (this.g == null) {
            this.b.onVideoDownloadError(this, -1, "init task error!", (Bundle)null);
         }
      } else if (this.b != null) {
         this.b.onVideoDownloadError(this, -1, "init error!", (Bundle)null);
      }

   }

   public void pauseTask() {
      if (this.d != null && this.g != null) {
         this.d.pauseTask();
      } else if (this.b != null) {
         this.b.onVideoDownloadError(this, -1, "pauseTask failed, init uncompleted!", (Bundle)null);
      }

   }

   public void stopTask() {
      if (this.d != null && this.g != null) {
         this.d.stopTask();
      } else if (this.b != null) {
         this.b.onVideoDownloadError(this, -1, "stopTask failed, init uncompleted!", (Bundle)null);
      }

   }

   public void resumeTask() {
      if (this.d != null && this.g != null) {
         this.d.resumeTask();
      } else if (this.b != null) {
         this.b.onVideoDownloadError(this, -1, "resumeTask failed, init uncompleted!", (Bundle)null);
      }

   }

   public void removeTask(boolean var1) {
      if (this.d != null && this.g != null) {
         this.d.removeTask(var1);
      } else if (this.b != null) {
         this.b.onVideoDownloadError(this, -1, "removeTask failed, init uncompleted!", (Bundle)null);
      }

   }

   public long getContentLength() {
      if (this.d != null && this.g != null) {
         return this.d.getContentLength();
      } else {
         if (this.b != null) {
            this.b.onVideoDownloadError(this, -1, "getContentLength failed, init uncompleted!", (Bundle)null);
         }

         return 0L;
      }
   }

   public int getDownloadedSize() {
      if (this.d != null && this.g != null) {
         return this.d.getDownloadedSize();
      } else {
         if (this.b != null) {
            this.b.onVideoDownloadError(this, -1, "getDownloadedSize failed, init uncompleted!", (Bundle)null);
         }

         return 0;
      }
   }

   public int getProgress() {
      if (this.d != null && this.g != null) {
         return this.d.getProgress();
      } else {
         if (this.b != null) {
            this.b.onVideoDownloadError(this, -1, "getProgress failed, init uncompleted!", (Bundle)null);
         }

         return 0;
      }
   }
}
