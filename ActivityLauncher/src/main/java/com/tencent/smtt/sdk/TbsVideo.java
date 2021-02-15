package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.tbs.video.interfaces.IVideoActivityCallback;

public class TbsVideo {
   public static void openVideo(Context var0, String var1) {
      openVideo(var0, var1, (Bundle)null);
   }

   public static void openVideo(Context var0, String var1, Bundle var2) {
      if (TextUtils.isEmpty(var1)) {
         Log.e("TbsVideo", "videoUrl is empty!");
      } else {
         if (var2 == null) {
            var2 = new Bundle();
         }

         var2.putString("videoUrl", var1);
         Intent var3 = new Intent("com.tencent.smtt.tbs.video.PLAY");
         var3.setFlags(268435456);
         var3.setPackage(var0.getPackageName());
         var3.putExtra("extraData", var2);
         var0.startActivity(var3);
      }
   }

   public static boolean openYunboVideo(Context var0, String var1, Bundle var2, IVideoActivityCallback var3) {
      return canUseYunbo(var0) ? VideoActivityHelper.getInstance(var0).play(var1, var2, var3) : false;
   }

   public static boolean canUseTbsPlayer(Context var0) {
      return VideoActivityHelper.getInstance(var0).a();
   }

   public static boolean canUseYunbo(Context var0) {
      return VideoActivityHelper.getInstance(var0).a() && QbSdk.canUseVideoFeatrue(var0, 1);
   }
}
