package com.tencent.smtt.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.widget.TextView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TbsLogClient {
   static TbsLogClient a = null;
   TextView b;
   static File c = null;
   private SimpleDateFormat f = null;
   static String d = null;
   static byte[] e = null;
   private Context g = null;
   private StringBuffer h = new StringBuffer();
   private static boolean i = true;

   public TbsLogClient(Context var1) {
      try {
         this.g = var1.getApplicationContext();
         this.f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS", Locale.US);
      } catch (Exception var3) {
         this.f = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
      }

   }

   private void a() {
      try {
         if (c == null) {
            if (Environment.getExternalStorageState().equals("mounted")) {
               String var1 = FileHelper.getBackUpDir(this.g, 6);
               if (var1 == null) {
                  c = null;
               } else {
                  c = new File(var1, "tbslog.txt");
                  d = LogFileUtils.createKey();
                  e = LogFileUtils.createHeaderText(c.getName(), d);
               }
            } else {
               c = null;
            }
         }
      } catch (SecurityException var2) {
         var2.printStackTrace();
      } catch (NullPointerException var3) {
         var3.printStackTrace();
      }

   }

   public void writeLog(String var1) {
      try {
         String var2 = this.f.format(System.currentTimeMillis());
         this.h.append(var2).append(" pid=").append(Process.myPid()).append(" tid=").append(Process.myTid()).append(var1).append("\n");
         if (Thread.currentThread() != Looper.getMainLooper().getThread() || i) {
            this.writeLogToDisk();
         }

         if (this.h.length() > 524288) {
            this.h.delete(0, this.h.length());
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }
   }

   public void writeLogToDisk() {
      try {
         this.a();
         if (c != null) {
            LogFileUtils.writeDataToStorage(c, d, e, this.h.toString(), true);
            this.h.delete(0, this.h.length());
         }
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   public void showLog(String var1) {
      if (this.b != null) {
         this.b.post(new TbsLogClient.a(var1));
      }
   }

   public void setLogView(TextView var1) {
      this.b = var1;
   }

   public static void setWriteLogJIT(boolean var0) {
      i = var0;
   }

   public void i(String var1, String var2) {
   }

   public void e(String var1, String var2) {
   }

   public void w(String var1, String var2) {
   }

   public void d(String var1, String var2) {
   }

   public void v(String var1, String var2) {
   }

   private class a implements Runnable {
      String a = null;

      a(String var2) {
         this.a = var2;
      }

      public void run() {
         if (TbsLogClient.this.b != null) {
            TbsLogClient.this.b.append(this.a + "\n");
         }

      }
   }
}
