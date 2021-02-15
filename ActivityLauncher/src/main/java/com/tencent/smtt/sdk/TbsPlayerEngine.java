package com.tencent.smtt.sdk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;
import com.tencent.smtt.export.external.DexLoader;
import dalvik.system.DexClassLoader;

class TbsPlayerEngine {
   protected DexLoader dexLoader = null;

   public TbsPlayerEngine(DexLoader var1) {
      this.dexLoader = var1;
   }

   public Object getTbsPlayerProxy(Context context) {
      Object tbsPlayerProxy = null;
      tbsPlayerProxy = this.dexLoader.newInstance("com.tencent.tbs.player.TbsPlayerProxy", new Class[]{Context.class, DexClassLoader.class}, context, this.dexLoader.getClassLoader());
      return tbsPlayerProxy;
   }

   public boolean play(Object var1, Bundle var2, FrameLayout frameLayout, Object var4) {
      Object var5 = null;
      if (var4 != null) {
         var5 = this.dexLoader.invokeMethod(var1, "com.tencent.tbs.player.TbsPlayerProxy", "play", new Class[]{Bundle.class, FrameLayout.class, Object.class}, var2, frameLayout, var4);
      } else {
         var5 = this.dexLoader.invokeMethod(var1, "com.tencent.tbs.player.TbsPlayerProxy", "play", new Class[]{Bundle.class, FrameLayout.class}, var2, frameLayout);
      }

      return var5 instanceof Boolean ? (Boolean)var5 : false;
   }

   public void onActivity(Object var1, Activity var2, int var3) {
      this.dexLoader.invokeMethod(var1, "com.tencent.tbs.player.TbsPlayerProxy", "onActivity", new Class[]{Activity.class, Integer.TYPE}, var2, var3);
   }

   public void onUserStateChanged(Object var1) {
      this.dexLoader.invokeMethod(var1, "com.tencent.tbs.player.TbsPlayerProxy", "onUserStateChanged", new Class[0]);
   }
}
