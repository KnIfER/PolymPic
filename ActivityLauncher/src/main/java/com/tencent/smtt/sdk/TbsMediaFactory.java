package com.tencent.smtt.sdk;

import android.content.Context;
import android.util.Log;
import com.tencent.smtt.export.external.DexLoader;

public class TbsMediaFactory {
   private Context a = null;
   private TbsWizard b = null;
   private DexLoader c = null;

   public TbsMediaFactory(Context var1) {
      this.a = var1.getApplicationContext();
      this.a();
   }

   private void a() {
      if (this.a == null) {
         Log.e("TbsVideo", "TbsVideo needs context !!");
      } else {
         if (this.b == null) {
            SDKEngine.getInstance(true).init(this.a, false, false);
            this.b = SDKEngine.getInstance(true).a();
            if (this.b != null) {
               this.c = this.b.getDexLoader();
            }
         }

         if (this.b == null || this.c == null) {
            throw new RuntimeException("tbs core dex(s) load failure !!!");
         }
      }
   }

   public TbsMediaPlayer createPlayer() {
      if (this.b != null && this.c != null) {
         return new TbsMediaPlayer(new TbsMediaPlayerEngine(this.c, this.a));
      } else {
         throw new RuntimeException("tbs core dex(s) did not loaded !!!");
      }
   }
}
