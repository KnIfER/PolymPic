package com.tencent.smtt.sdk;

import android.graphics.SurfaceTexture;
import android.os.Bundle;

public class TbsMediaPlayer {
   private TbsMediaPlayerEngine tbsMediaPlayerEngine = null;

   public TbsMediaPlayer(TbsMediaPlayerEngine var1) {
      this.tbsMediaPlayerEngine = var1;
   }

   public boolean isAvailable() {
      return this.tbsMediaPlayerEngine.a();
   }

   public void setSurfaceTexture(SurfaceTexture var1) {
      this.tbsMediaPlayerEngine.setSurfaceTexture(var1);
   }

   public void setPlayerListener(TbsMediaPlayer.TbsMediaPlayerListener var1) {
      this.tbsMediaPlayerEngine.setPlayerListener(var1);
   }

   public float getVolume() {
      return this.tbsMediaPlayerEngine.getVolume();
   }

   public void setVolume(float var1) {
      this.tbsMediaPlayerEngine.setVolume(var1);
   }

   public void startPlay(String var1, Bundle var2) {
      this.tbsMediaPlayerEngine.startPlay(var1, var2);
   }

   public void subtitle(int var1) {
      this.tbsMediaPlayerEngine.subtitle(var1);
   }

   public void audio(int var1) {
      this.tbsMediaPlayerEngine.audio(var1);
   }

   public void pause() {
      this.tbsMediaPlayerEngine.pause();
   }

   public void play() {
      this.tbsMediaPlayerEngine.play();
   }

   public void seek(long var1) {
      this.tbsMediaPlayerEngine.seek(var1);
   }

   public void close() {
      this.tbsMediaPlayerEngine.close();
   }

   public interface TbsMediaPlayerListener {
      int ROTATE_ACTION_UNKNOWN = 0;
      int ROTATE_ACTION_NOTHING = 1;
      int ROTATE_ACTION_SETDEGREE = 2;
      int ROTATE_ACTION_HASROTATE = 3;
      int MEDIA_EXTRA_SUBTITLE_COUNT = 101;
      int MEDIA_EXTRA_SUBTITLE_INDEX = 102;
      int MEDIA_EXTRA_AUDIOTRACK_TITLES = 103;
      int MEDIA_EXTRA_AUDIOTRACK_INDEX = 104;
      int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
      int MEDIA_INFO_BUFFERING_START = 701;
      int MEDIA_INFO_BUFFERING_END = 702;
      int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
      int MEDIA_INFO_NO_VIDEO_DATA = 751;
      int MEDIA_INFO_HAVE_VIDEO_DATA = 752;
      int MEDIA_INFO_BUFFERING_PERCENTAGE = 790;
      int MEDIA_INFO_BAD_INTERLEAVING = 800;
      int MEDIA_INFO_NOT_SEEKABLE = 801;
      int MEDIA_INFO_METADATA_UPDATE = 802;
      int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
      int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
      int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
      int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;

      void onPlayerPrepared(long var1, int var3, int var4, int var5, int var6);

      void onPlayerExtra(int var1, Object var2);

      void onPlayerError(String var1, int var2, int var3, Throwable var4);

      void onPlayerInfo(int var1, int var2);

      void onPlayerPlaying();

      void onPlayerProgress(long var1);

      void onPlayerSubtitle(String var1);

      void onPlayerPaused();

      void onPlayerSeeked(long var1);

      void onPlayerCompleted();

      void onBufferingUpdate(float var1);
   }
}
