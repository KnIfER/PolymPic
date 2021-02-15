package com.tencent.smtt.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;

public class TBSFileLockUtil implements Runnable {
   public static String a = "TBSFileLock";
   File file = null;
   RandomAccessFile randomAccessFile = null;
   FileLock fileLock = null;
   long delaySeconds = 0L;
   private static Object f = new Object();
   private static Object g = new Object();
   private static HashMap<TBSFileLockUtil, Object> hashMap = null;
   private static Handler handler = null;

   public TBSFileLockUtil(File var1, String var2) {
      this.file = new File(var1, "." + var2 + ".lock");
   }

   Handler getHandler() {
      if (handler == null) {
         Class var1 = TBSFileLockUtil.class;
         synchronized(TBSFileLockUtil.class) {
            if (handler == null) {
               HandlerThread var2 = new HandlerThread("QBFileLock.Thread");
               var2.start();
               Looper var3 = var2.getLooper();
               handler = new Handler(var3);
            }
         }
      }

      return handler;
   }

   public synchronized void b() {
      try {
         this.randomAccessFile = new RandomAccessFile(this.file, "rw");
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      if (this.randomAccessFile != null) {
         FileChannel var1 = this.randomAccessFile.getChannel();
         if (var1 != null) {
            if (this.delaySeconds > 0L) {
               this.getHandler().postDelayed(this, this.delaySeconds);
            }

            FileLock var2 = null;
            long var3 = System.currentTimeMillis();

            while(true) {
               try {
                  var2 = var1.lock();
                  if (var2 != null) {
                     break;
                  }
               } catch (Exception var8) {
                  var8.printStackTrace();
                  Log.d(a, ">>> lock failed, sleep...");
               }

               try {
                  Thread.sleep(50L);
               } catch (InterruptedException var6) {
                  var6.printStackTrace();
               }

               if (Math.abs(System.currentTimeMillis() - var3) >= 1000L) {
                  Log.d(a, ">>> lock timeout, quit...");
                  break;
               }
            }

            this.fileLock = var2;
            Log.d(a, ">>> lock [" + this.file.getName() + "] cost: " + (System.currentTimeMillis() - var3));
         }
      }

      if (this.fileLock != null) {
         this.c();
      }

   }

   void c() {
      synchronized(g) {
         if (hashMap == null) {
            hashMap = new HashMap();
         }

         hashMap.put(this, f);
      }
   }

   void d() {
      synchronized(g) {
         if (hashMap != null) {
            hashMap.remove(this);
         }
      }
   }

   public void e() {
      this.a(true);
   }

   public synchronized void a(boolean var1) {
      Log.d(a, ">>> release lock: " + this.file.getName());
      if (this.fileLock != null) {
         try {
            this.fileLock.release();
         } catch (Exception var4) {
            var4.printStackTrace();
         }

         this.fileLock = null;
      }

      if (this.randomAccessFile != null) {
         try {
            this.randomAccessFile.close();
         } catch (Exception e) {
            e.printStackTrace();
         }

         this.randomAccessFile = null;
      }

      if (handler != null && this.delaySeconds > 0L) {
         handler.removeCallbacks(this);
      }

      if (var1) {
         this.d();
      }

   }

   public void run() {
      Log.d(a, ">>> releaseLock on TimeOut");
      this.e();
   }
}
