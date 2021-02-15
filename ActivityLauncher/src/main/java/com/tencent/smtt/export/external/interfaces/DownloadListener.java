package com.tencent.smtt.export.external.interfaces;

public interface DownloadListener {
   void onDownloadStart(String var1, String var2, String var3, String var4, long var5);

   void onDownloadStart(String var1, String var2, byte[] var3, String var4, String var5, String var6, long var7, String var9, String var10);

   void onDownloadVideo(String var1, long var2, int var4);
}
