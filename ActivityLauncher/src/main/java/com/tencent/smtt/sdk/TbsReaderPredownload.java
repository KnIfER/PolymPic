package com.tencent.smtt.sdk;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.LinkedList;

public class TbsReaderPredownload {
   Handler a = null;
   static final String[] b = new String[]{"docx", "pptx", "xlsx", "pdf", "epub", "txt"};
   LinkedList<String> c = new LinkedList();
   boolean d = false;
   ReaderWizard e = null;
   TbsReaderView.ReaderCallback f = null;
   Object g = null;
   Context h = null;
   TbsReaderPredownload.ReaderPreDownloadCallback i = null;
   String j = "";
   public static final int READER_SO_SUCCESS = 2;
   public static final int READER_WAIT_IN_QUEUE = 3;

   public TbsReaderPredownload(TbsReaderPredownload.ReaderPreDownloadCallback var1) {
      this.i = var1;
      String[] var2 = b;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String var5 = var2[var4];
         this.c.add(var5);
      }

      this.a();
   }

   public boolean init(Context var1) {
      if (var1 == null) {
         return false;
      } else {
         this.h = var1.getApplicationContext();
         boolean var2 = TbsReaderView.a(var1.getApplicationContext());
         this.f = new TbsReaderView.ReaderCallback() {
            public void onCallBackAction(Integer var1, Object var2, Object var3) {
               switch(var1) {
               case 5012:
                  int var4 = (Integer)var2;
                  if (5014 == var4) {
                     return;
                  } else {
                     if (5013 == var4) {
                        TbsReaderPredownload.this.a(0);
                     } else if (var4 == 0) {
                        TbsReaderPredownload.this.a(0);
                     } else {
                        TbsReaderPredownload.this.a(-1);
                     }

                     TbsReaderPredownload.this.j = "";
                     TbsReaderPredownload.this.a(3, 100);
                  }
               default:
               }
            }
         };

         try {
            if (this.e == null) {
               this.e = new ReaderWizard(this.f);
            }

            if (null == this.g) {
               this.g = this.e.getTbsReader();
            }

            if (this.g != null) {
               var2 = this.e.initTbsReader(this.g, var1.getApplicationContext());
            }
         } catch (NullPointerException var4) {
            Log.e("TbsReaderPredownload", "Unexpect null object!");
            var2 = false;
         }

         return var2;
      }
   }

   public void startAll() {
      this.d = false;
      boolean var1 = false;
      var1 |= this.c(3);
      if (!var1) {
         this.a(3, 100);
      }

   }

   public void start(String var1) {
      this.d = false;
      this.b(3);
      this.c.add(var1);
      this.a(3, 100);
   }

   public void pause() {
      this.d = true;
   }

   public void shutdown() {
      this.i = null;
      this.d = false;
      this.c.clear();
      this.b();
      if (this.e != null) {
         this.e.destroy(this.g);
         this.g = null;
      }

      this.h = null;
   }

   private void b() {
      this.b(3);
   }

   boolean a(String var1) {
      if (null != this.g && this.e != null) {
         return !ReaderWizard.isSupportExt(var1) ? false : this.e.checkPlugin(this.g, this.h, var1, true);
      } else {
         return false;
      }
   }

   void a(int var1) {
      if (this.i != null) {
         boolean var2 = this.c.isEmpty();
         this.i.onEvent(this.j, var1, var2);
      }

   }

   void a() {
      this.a = new Handler(Looper.getMainLooper()) {
         public void handleMessage(Message var1) {
            switch(var1.what) {
            case 3:
               if (!TbsReaderPredownload.this.c.isEmpty() && !TbsReaderPredownload.this.d) {
                  String var2 = (String)TbsReaderPredownload.this.c.removeFirst();
                  TbsReaderPredownload.this.j = var2;
                  if (!TbsReaderPredownload.this.a(var2)) {
                     TbsReaderPredownload.this.a(-1);
                  }
               }
            default:
            }
         }
      };
   }

   void b(int var1) {
      this.a.removeMessages(var1);
   }

   boolean c(int var1) {
      return this.a.hasMessages(var1);
   }

   void a(int var1, int var2) {
      Message var3 = this.a.obtainMessage(var1);
      this.a.sendMessageDelayed(var3, (long)var2);
   }

   public interface ReaderPreDownloadCallback {
      int NOTIFY_PLUGIN_FAILED = -1;
      int NOTIFY_PLUGIN_SUCCESS = 0;

      void onEvent(String var1, int var2, boolean var3);
   }
}
