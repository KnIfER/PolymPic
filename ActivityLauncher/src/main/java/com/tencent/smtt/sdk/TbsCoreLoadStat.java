package com.tencent.smtt.sdk;

import android.content.Context;
import com.tencent.smtt.utils.TbsLog;
import java.util.Arrays;

public class TbsCoreLoadStat {
   private TbsCoreLoadStat.TbsSequenceQueue a = null;
   private boolean b = false;
   private final int c = 3;
   public static volatile int mLoadErrorCode = -1;
   private static TbsCoreLoadStat d = null;

   private TbsCoreLoadStat() {
   }

   public static TbsCoreLoadStat getInstance() {
      if (d == null) {
         d = new TbsCoreLoadStat();
      }

      return d;
   }

   void a() {
      if (this.a != null) {
         this.a.clear();
      }

      this.b = false;
   }

   void a(Context var1, int var2) {
      this.a(var1, var2, (Throwable)null);
      TbsLog.e("loaderror", "" + var2);
   }

   synchronized void a(Context var1, int var2, Throwable var3) {
      if (mLoadErrorCode == -1) {
         mLoadErrorCode = var2;
         TbsLog.addLog(998, "code=%d,desc=%s", var2, String.valueOf(var3));
         if (var3 != null) {
            TbsLogReport.getInstance(var1).setLoadErrorCode(var2, var3);
         } else {
            TbsLog.e("TbsCoreLoadStat", "setLoadErrorCode :: error is null with errorCode: " + var2 + "; Check & correct it!");
         }

      } else {
         StringBuilder var4 = new StringBuilder("setLoadErrorCode :: error(");
         var4.append(mLoadErrorCode);
         var4.append(") was already reported; ");
         var4.append(var2);
         var4.append(" is duplicated. Try to remove it!");
         TbsLog.w("TbsCoreLoadStat", var4.toString());
      }
   }

   public class TbsSequenceQueue {
      private int b = 10;
      private int c;
      private int[] d;
      private int e = 0;
      private int f = 0;

      public TbsSequenceQueue() {
         this.c = this.b;
         this.d = new int[this.c];
      }

      public TbsSequenceQueue(int var2, int var3) {
         this.c = var3;
         this.d = new int[this.c];
         this.d[0] = var2;
         ++this.f;
      }

      public int length() {
         return this.f - this.e;
      }

      public void add(int var1) {
         if (this.f > this.c - 1) {
            throw new IndexOutOfBoundsException("sequeue is full");
         } else {
            this.d[this.f++] = var1;
         }
      }

      public int remove() {
         if (this.empty()) {
            throw new IndexOutOfBoundsException("sequeue is null");
         } else {
            int var1 = this.d[this.e];
            this.d[this.e++] = 0;
            return var1;
         }
      }

      public int element() {
         if (this.empty()) {
            throw new IndexOutOfBoundsException("sequeue is null");
         } else {
            return this.d[this.e];
         }
      }

      public boolean empty() {
         return this.f == this.e;
      }

      public void clear() {
         Arrays.fill(this.d, 0);
         this.e = 0;
         this.f = 0;
      }

      public String toString() {
         if (this.empty()) {
            return "";
         } else {
            StringBuilder var1 = new StringBuilder("[");

            int var2;
            for(var2 = this.e; var2 < this.f; ++var2) {
               var1.append(this.d[var2] + ",");
            }

            var2 = var1.length();
            return var1.delete(var2 - 1, var2).append("]").toString();
         }
      }
   }
}
