package com.tencent.smtt.export.external;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.os.Build;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class libwebp {
   private static final String LOGTAG = "[image]";
   private static libwebp mInstance = null;
   private static final int BITMAP_ALPHA_8 = 1;
   private static final int BITMAP_RGB_565 = 2;
   private static final int BITMAP_ARGB_4444 = 3;
   private static final int BITMAP_ARGB_8888 = 4;
   private int mBitmapType = 4;
   private static boolean mIsLoadLibSuccess = false;
   private static boolean isMultiCore = false;

   public static libwebp getInstance(Context var0) {
      if (mInstance == null) {
         loadWepLibraryIfNeed(var0);
         mInstance = new libwebp();
      }

      return mInstance;
   }

   public static void loadWepLibraryIfNeed(Context var0, String var1) {
      if (!mIsLoadLibSuccess) {
         try {
            System.load(var1 + File.separator + "libwebp_base.so");
            mIsLoadLibSuccess = true;
         } catch (UnsatisfiedLinkError var3) {
            Log.e("[image]", "Load WebP Library Error...: libwebp.java - loadWepLibraryIfNeed()");
         }
      }

   }

   public static void loadWepLibraryIfNeed(Context var0) {
      if (!mIsLoadLibSuccess) {
         try {
            LibraryLoader.loadLibrary(var0, "webp_base");
            mIsLoadLibSuccess = true;
         } catch (UnsatisfiedLinkError var2) {
            Log.e("[image]", "Load WebP Library Error...: libwebp.java - loadWepLibraryIfNeed()");
         }
      }

   }

   private boolean isMultiCore() {
      String var1 = this.getCPUinfo();
      boolean var2 = var1.contains("processor");
      return var2;
   }

   private String getCPUinfo() {
      String var2 = "";

      try {
         String[] var3 = new String[]{"/system/bin/cat", "/proc/cpuinfo"};
         ProcessBuilder var1 = new ProcessBuilder(var3);
         Process var4 = var1.start();
         InputStream var5 = var4.getInputStream();

         for(byte[] var6 = new byte[1024]; var5.read(var6) != -1; var2 = var2 + new String(var6)) {
         }

         var5.close();
      } catch (IOException var7) {
         var7.printStackTrace();
      }

      return var2;
   }

   public int getInfo(byte[] var1, int[] var2, int[] var3) {
      return !mIsLoadLibSuccess ? 0 : this.nativeGetInfo(var1, var2, var3);
   }

   public int[] decodeBase(byte[] var1, int[] var2, int[] var3) {
      if (!mIsLoadLibSuccess) {
         Log.e("[image]", "Load WebP Library Error...");
         return null;
      } else {
         return this.nativeDecode(var1, isMultiCore, var2, var3);
      }
   }

   public int[] decodeBase_16bit(byte[] var1, Config var2) {
      if (!mIsLoadLibSuccess) {
         Log.e("[image]", "Load WebP Library Error...");
         return null;
      } else {
         switch(var2) {
         case ARGB_4444:
            this.mBitmapType = 3;
            break;
         case RGB_565:
            this.mBitmapType = 2;
            break;
         default:
            this.mBitmapType = 2;
         }

         return this.nativeDecode_16bit(var1, isMultiCore, this.mBitmapType);
      }
   }

   public int[] decodeInto(byte[] var1, int[] var2, int[] var3) {
      if (!mIsLoadLibSuccess) {
         Log.e("[image]", "Load WebP Library Error...");
         return null;
      } else {
         return this.nativeDecodeInto(var1, isMultiCore, var2, var3);
      }
   }

   public int[] incDecode(byte[] var1, int[] var2, int[] var3) {
      if (!mIsLoadLibSuccess) {
         Log.e("[image]", "Load WebP Library Error...");
         return null;
      } else {
         return this.nativeIDecode(var1, isMultiCore, var2, var3);
      }
   }

   public native int nativeGetInfo(byte[] var1, int[] var2, int[] var3);

   public native int[] nativeDecode(byte[] var1, boolean var2, int[] var3, int[] var4);

   public native int[] nativeDecodeInto(byte[] var1, boolean var2, int[] var3, int[] var4);

   public native int[] nativeDecode_16bit(byte[] var1, boolean var2, int var3);

   public native int[] nativeIDecode(byte[] var1, boolean var2, int[] var3, int[] var4);

   public static int checkIsHuaModel() {
      String var0 = Build.BRAND.trim().toLowerCase();
      String var1 = Build.MODEL.trim().toLowerCase();
      byte var2 = 0;
      if (var0 != null && var0.length() > 0 && var0.contains("huawei")) {
         var2 = 1;
      }

      if (var1 != null && var1.length() > 0 && var1.contains("huawei")) {
         var2 = 1;
      }

      return var2;
   }
}
