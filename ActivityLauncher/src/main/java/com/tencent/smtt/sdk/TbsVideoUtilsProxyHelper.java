package com.tencent.smtt.sdk;

import android.content.Context;
import com.tencent.smtt.export.external.DexLoader;

class TbsVideoUtilsProxyHelper {
   private DexLoader a = null;

   public TbsVideoUtilsProxyHelper(DexLoader var1) {
      this.a = var1;
   }

   public void a(Context var1, String var2) {
      if (this.a != null) {
         Object var3 = this.a.newInstance("com.tencent.tbs.utils.TbsVideoUtilsProxy", new Class[0]);
         if (var3 != null) {
            this.a.invokeMethod(var3, "com.tencent.tbs.utils.TbsVideoUtilsProxy", "deleteVideoCache", new Class[]{Context.class, String.class}, var1, var2);
         }
      }

   }

   public String a(Context var1) {
      if (this.a != null) {
         Object var2 = this.a.newInstance("com.tencent.tbs.utils.TbsVideoUtilsProxy", new Class[0]);
         if (var2 != null) {
            Object var3 = this.a.invokeMethod(var2, "com.tencent.tbs.utils.TbsVideoUtilsProxy", "getCurWDPDecodeType", new Class[]{Context.class}, var1);
            if (var3 != null) {
               return String.valueOf(var3);
            }
         }
      }

      return "";
   }
}
