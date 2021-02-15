package com.tencent.smtt.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.tencent.tbs.video.interfaces.IUserStateChangedListener;
import com.tencent.tbs.video.interfaces.IVideoActivityCallback;

class VideoActivityHelper {
   private static VideoActivityHelper instance = null;
   VideoFrameLayout videoFrameLayout = null;
   Context context;
   IVideoActivityCallback videoActivityCallback;
   IUserStateChangedListener userStateChangedListener;

   public static synchronized VideoActivityHelper getInstance(Context context) {
      if (instance == null) {
         instance = new VideoActivityHelper(context);
      }

      return instance;
   }

   private VideoActivityHelper(Context context) {
      this.context = context.getApplicationContext();
      this.videoFrameLayout = new VideoFrameLayout(this.context);
   }

   public boolean play(String videoUrl, Bundle bundle
		   , IVideoActivityCallback activityCallback) {
      if (bundle == null) {
         bundle = new Bundle();
      }

      if (!TextUtils.isEmpty(videoUrl)) {
         bundle.putString("videoUrl", videoUrl);
      }

      if (activityCallback != null) {
         this.videoFrameLayout.init();
         if (!this.videoFrameLayout.isValid()) {
            return false;
         }

         this.videoActivityCallback = activityCallback;
         this.userStateChangedListener = new IUserStateChangedListener() {
            public void onUserStateChanged() {
               VideoActivityHelper.this.videoFrameLayout.onUserStateChanged();
            }
         };
         this.videoActivityCallback.setUserStateChangedListener(this.userStateChangedListener);
         bundle.putInt("callMode", 3);
      } else {
         bundle.putInt("callMode", 1);
      }

      this.videoFrameLayout.play(bundle, activityCallback == null ? null : this);
      return true;
   }

   void a(Activity var1, int var2) {
      this.videoFrameLayout.a(var1, var2);
   }

   public boolean a() {
      this.videoFrameLayout.init();
      return this.videoFrameLayout.isValid();
   }

   public void onActivityResult(int var1, int var2, Intent var3) {
      if (this.videoActivityCallback != null) {
         this.videoActivityCallback.onActivityResult(var1, var2, var3);
      }

   }
}
