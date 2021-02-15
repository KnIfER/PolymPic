package com.tencent.smtt.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.tencent.smtt.export.external.DexLoader;

class VideoFrameLayout extends FrameLayout implements OnErrorListener {
   private Object tbsPlayerProxy;
   private TbsPlayerEngine tbsPlayerEngine;
   private VideoView videoView;
   private Context context = null;
   private String urlStr;

   public VideoFrameLayout(Context var1) {
      super(var1.getApplicationContext());
      this.context = var1;
   }

   void play(Bundle var1, Object var2) {
      this.playInner(var1, var2);
   }

   private void playInner(Bundle bundle, Object var2) {
      this.init();
      boolean sucess = false;
      if (this.isValid()) {
         bundle.putInt("callMode", bundle.getInt("callMode"));
         sucess = this.tbsPlayerEngine.play(this.tbsPlayerProxy, bundle, this, var2);
      }

      if (!sucess) {
         if (this.videoView != null) {
            this.videoView.stopPlayback();
         }

         if (this.videoView == null) {
            this.videoView = new VideoView(this.getContext());
         }

         this.urlStr = bundle.getString("videoUrl");
         this.videoView.setVideoURI(Uri.parse(this.urlStr));
         this.videoView.setOnErrorListener(this);
         Intent var4 = new Intent("com.tencent.smtt.tbs.video.PLAY");
         var4.addFlags(268435456);
         Context var5 = this.getContext().getApplicationContext();
         var4.setPackage(var5.getPackageName());
         var5.startActivity(var4);
      }

   }

   void init() {
      this.setBackgroundColor(-16777216);
      if (this.tbsPlayerEngine == null) {
         SDKEngine.getInstance(true).init(this.getContext().getApplicationContext(), false, false);
         TbsWizard tbsWizard = SDKEngine.getInstance(true).a();
         DexLoader dexLoader = null;
         if (tbsWizard != null) {
            dexLoader = tbsWizard.getDexLoader();
         }

         if (dexLoader != null && QbSdk.canLoadVideo(this.getContext())) {
            this.tbsPlayerEngine = new TbsPlayerEngine(dexLoader);
         }
      }

      if (this.tbsPlayerEngine != null && this.tbsPlayerProxy == null) {
         this.tbsPlayerProxy = this.tbsPlayerEngine.getTbsPlayerProxy(this.getContext().getApplicationContext());
      }

   }

   public boolean isValid() {
      return this.tbsPlayerEngine != null && this.tbsPlayerProxy != null;
   }

   public void a(Activity var1) {
      if (!this.isValid() && this.videoView != null) {
         if (this.videoView.getParent() == null) {
            Window window = var1.getWindow();
            FrameLayout frameLayout = (FrameLayout)window.getDecorView();
            window.addFlags(1024);
            window.addFlags(128);
            frameLayout.setBackgroundColor(-16777216);
            MediaController mediaController = new MediaController(var1);
            mediaController.setMediaPlayer(this.videoView);
            this.videoView.setMediaController(mediaController);
            LayoutParams layoutParams = new LayoutParams(-1, -1);
            layoutParams.gravity = 17;
            frameLayout.addView(this.videoView, layoutParams);
         }

         if (VERSION.SDK_INT >= 8) {
            this.videoView.start();
         }
      }

   }

   void a(Activity var1, int var2) {
      if (var2 == 3 && !this.isValid() && this.videoView != null) {
         this.videoView.pause();
      }

      if (var2 == 4) {
         this.context = null;
         if (!this.isValid() && this.videoView != null) {
            this.videoView.stopPlayback();
            this.videoView = null;
         }
      }

      if (var2 == 2 && !this.isValid()) {
         this.context = var1;
         this.a(var1);
      }

      if (this.isValid()) {
         this.tbsPlayerEngine.onActivity(this.tbsPlayerProxy, var1, var2);
      }

   }

   public boolean onError(MediaPlayer var1, int var2, int var3) {
      try {
         if (this.context instanceof Activity) {
            Activity var4 = (Activity)this.context;
            if (!var4.isFinishing()) {
               var4.finish();
            }
         }

         Context var8 = this.getContext();
         if (var8 != null) {
            Toast.makeText(var8, "播放失败，请选择其它播放器播放", 1).show();
            Context var5 = var8.getApplicationContext();
            Intent var6 = new Intent("android.intent.action.VIEW");
            var6.addFlags(268435456);
            var6.setDataAndType(Uri.parse(this.urlStr), "video/*");
            var5.startActivity(var6);
         }

         return true;
      } catch (Throwable var7) {
         return false;
      }
   }

   public void onUserStateChanged() {
      if (this.isValid()) {
         this.tbsPlayerEngine.onUserStateChanged(this.tbsPlayerProxy);
      }

   }
}
