package com.tencent.smtt.sdk;

import android.os.HandlerThread;

class TbsHandlerThread extends HandlerThread {
   private static TbsHandlerThread instance;

   public TbsHandlerThread(String var1) {
      super(var1);
   }

   public static synchronized TbsHandlerThread getInstance() {
      if (instance == null) {
         instance = new TbsHandlerThread("TbsHandlerThread");
         instance.start();
      }

      return instance;
   }
}
