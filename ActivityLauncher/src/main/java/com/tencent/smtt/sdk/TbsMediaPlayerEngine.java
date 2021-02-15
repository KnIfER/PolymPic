package com.tencent.smtt.sdk;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import com.tencent.smtt.export.external.DexLoader;

class TbsMediaPlayerEngine {
   private DexLoader dexLoader = null;
   private Object tbsMediaPlayerProxy = null;

   public TbsMediaPlayerEngine(DexLoader var1, Context var2) {
      this.dexLoader = var1;
      this.tbsMediaPlayerProxy = this.dexLoader.newInstance("com.tencent.tbs.player.TbsMediaPlayerProxy", new Class[]{Context.class}, var2);
   }

   public boolean a() {
      return this.tbsMediaPlayerProxy != null;
   }

   public void setSurfaceTexture(SurfaceTexture var1) {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "setSurfaceTexture", new Class[]{SurfaceTexture.class}, var1);
   }

   public void setPlayerListener(TbsMediaPlayer.TbsMediaPlayerListener var1) {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "setPlayerListener", new Class[]{Object.class}, var1);
   }

   public float getVolume() {
      Float var1 = (Float)this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "getVolume", new Class[0]);
      return var1 != null ? var1 : 0.0F;
   }

   public void setVolume(float var1) {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "setVolume", new Class[]{Float.TYPE}, var1);
   }

   public void startPlay(String var1, Bundle var2) {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "startPlay", new Class[]{String.class, Bundle.class}, var1, var2);
   }

   public void subtitle(int var1) {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "subtitle", new Class[]{Integer.TYPE}, var1);
   }

   public void audio(int var1) {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "audio", new Class[]{Integer.TYPE}, var1);
   }

   public void pause() {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "pause", new Class[0]);
   }

   public void play() {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "play", new Class[0]);
   }

   public void seek(long var1) {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "seek", new Class[]{Long.TYPE}, var1);
   }

   public void close() {
      this.dexLoader.invokeMethod(this.tbsMediaPlayerProxy, "com.tencent.tbs.player.TbsMediaPlayerProxy", "close", new Class[0]);
   }
}
