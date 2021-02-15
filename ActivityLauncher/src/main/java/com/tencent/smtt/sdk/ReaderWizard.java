package com.tencent.smtt.sdk;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import com.tencent.smtt.export.external.DexLoader;

public class ReaderWizard {
   private DexLoader a = null;
   private TbsReaderView.ReaderCallback b = null;

   public static boolean isSupportCurrentPlatform(Context var0) {
      boolean var1 = false;
      DexLoader var2 = a();
      if (var2 != null) {
         Object var3 = var2.invokeStaticMethod("com.tencent.tbs.reader.TbsReader", "isSupportCurrentPlatform", new Class[]{Context.class}, var0);
         if (var3 instanceof Boolean) {
            var1 = (Boolean)var3;
         }
      }

      return var1;
   }

   public static boolean isSupportExt(String var0) {
      boolean var1 = false;
      DexLoader var2 = a();
      if (var2 != null) {
         Object var3 = var2.invokeStaticMethod("com.tencent.tbs.reader.TbsReader", "isSupportExt", new Class[]{String.class}, var0);
         if (var3 instanceof Boolean) {
            var1 = (Boolean)var3;
         }
      }

      return var1;
   }

   public static Drawable getResDrawable(int var0) {
      Drawable var1 = null;
      DexLoader var2 = a();
      if (var2 != null) {
         Object var3 = var2.invokeStaticMethod("com.tencent.tbs.reader.TbsReader", "getResDrawable", new Class[]{Integer.class}, var0);
         if (var3 instanceof Drawable) {
            var1 = (Drawable)var3;
         }
      }

      return var1;
   }

   public static String getResString(int var0) {
      String var1 = "";
      DexLoader var2 = a();
      if (var2 != null) {
         Object var3 = var2.invokeStaticMethod("com.tencent.tbs.reader.TbsReader", "getResString", new Class[]{Integer.class}, var0);
         if (var3 instanceof String) {
            var1 = (String)var3;
         }
      }

      return var1;
   }

   public ReaderWizard(TbsReaderView.ReaderCallback var1) {
      this.a = a();
      this.b = var1;
   }

   private static DexLoader a() {
      TbsWizard var0 = SDKEngine.getInstance(true).c();
      DexLoader var1 = null;
      if (var0 != null) {
         var1 = var0.getDexLoader();
      }

      return var1;
   }

   public Object getTbsReader() {
      Object var1 = null;
      var1 = this.a.newInstance("com.tencent.tbs.reader.TbsReader", new Class[0]);
      return var1;
   }

   public boolean initTbsReader(Object var1, Context var2) {
      if (this.a != null && var1 != null) {
         Object var3 = this.a.invokeMethod(var1, "com.tencent.tbs.reader.TbsReader", "init", new Class[]{Context.class, DexLoader.class, Object.class}, var2, this.a, this);
         if (!(var3 instanceof Boolean)) {
            Log.e("ReaderWizard", "Unexpect return value type of call initTbsReader!");
            return false;
         } else {
            return (Boolean)var3;
         }
      } else {
         Log.e("ReaderWizard", "initTbsReader:Unexpect null object!");
         return false;
      }
   }

   public boolean checkPlugin(Object var1, Context var2, String var3, boolean var4) {
      if (this.a == null) {
         Log.e("ReaderWizard", "checkPlugin:Unexpect null object!");
         return false;
      } else {
         Object var5 = this.a.invokeMethod(var1, "com.tencent.tbs.reader.TbsReader", "checkPlugin", new Class[]{Context.class, String.class, Boolean.class}, var2, var3, var4);
         if (!(var5 instanceof Boolean)) {
            Log.e("ReaderWizard", "Unexpect return value type of call checkPlugin!");
            return false;
         } else {
            return (Boolean)var5;
         }
      }
   }

   public boolean openFile(Object var1, Context var2, Bundle var3, FrameLayout var4) {
      if (this.a == null) {
         Log.e("ReaderWizard", "openFile:Unexpect null object!");
         return false;
      } else {
         Object var5 = this.a.invokeMethod(var1, "com.tencent.tbs.reader.TbsReader", "openFile", new Class[]{Context.class, Bundle.class, FrameLayout.class}, var2, var3, var4);
         if (!(var5 instanceof Boolean)) {
            Log.e("ReaderWizard", "Unexpect return value type of call openFile!");
            return false;
         } else {
            return (Boolean)var5;
         }
      }
   }

   public void onSizeChanged(Object var1, int var2, int var3) {
      if (this.a == null) {
         Log.e("ReaderWizard", "onSizeChanged:Unexpect null object!");
      } else {
         this.a.invokeMethod(var1, "com.tencent.tbs.reader.TbsReader", "onSizeChanged", new Class[]{Integer.class, Integer.class}, new Integer(var2), new Integer(var3));
      }
   }

   public void onCallBackAction(Integer var1, Object var2, Object var3) {
      if (this.b != null) {
         this.b.onCallBackAction(var1, var2, var3);
      }

   }

   public void doCommand(Object var1, Integer var2, Object var3, Object var4) {
      if (this.a == null) {
         Log.e("ReaderWizard", "doCommand:Unexpect null object!");
      } else {
         this.a.invokeMethod(var1, "com.tencent.tbs.reader.TbsReader", "doCommand", new Class[]{Integer.class, Object.class, Object.class}, new Integer(var2), var3, var4);
      }
   }

   public void destroy(Object var1) {
      this.b = null;
      if (this.a != null && var1 != null) {
         this.a.invokeMethod(var1, "com.tencent.tbs.reader.TbsReader", "destroy", new Class[0]);
      } else {
         Log.e("ReaderWizard", "destroy:Unexpect null object!");
      }
   }

   public void userStatistics(Object var1, String var2) {
      if (this.a == null) {
         Log.e("ReaderWizard", "userStatistics:Unexpect null object!");
      } else {
         this.a.invokeMethod(var1, "com.tencent.tbs.reader.TbsReader", "userStatistics", new Class[]{String.class}, var2);
      }
   }
}
