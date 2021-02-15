package com.tencent.smtt.utils;

import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UnknownFormatConversionException;

public class ELFUtils implements Closeable {
   static final char[] a = new char[]{'\u007f', 'E', 'L', 'F', '\u0000'};
   final char[] b = new char[16];
   private final RandomAccessFileWrap g;
   private final ELFUtils.a h;
   private final ELFUtils.k[] i;
   private byte[] j;
   boolean c;
   ELFUtils.j[] d;
   ELFUtils.l[] e;
   byte[] f;

   final boolean a() {
      return this.b[0] == a[0];
   }

   final char b() {
      return this.b[4];
   }

   final char c() {
      return this.b[5];
   }

   public final boolean d() {
      return this.b() == 2;
   }

   public final boolean e() {
      return this.c() == 1;
   }

   public ELFUtils(File var1) throws IOException, UnknownFormatConversionException {
      RandomAccessFileWrap var2 = this.g = new RandomAccessFileWrap(var1);
      var2.read(this.b);
      if (!this.a()) {
         throw new UnknownFormatConversionException("Invalid elf magic: " + var1);
      } else {
         var2.setSmallEnd(this.e());
         boolean var3 = this.d();
         if (var3) {
            ELFUtils.f var4 = new ELFUtils.f();
            var4.a = var2.readShort();
            var4.b = var2.readShort();
            var4.c = var2.readInt();
            var4.k = var2.readFully();
            var4.l = var2.readFully();
            var4.m = var2.readFully();
            this.h = var4;
         } else {
            ELFUtils.b var9 = new ELFUtils.b();
            var9.a = var2.readShort();
            var9.b = var2.readShort();
            var9.c = var2.readInt();
            var9.k = var2.readInt();
            var9.l = var2.readInt();
            var9.m = var2.readInt();
            this.h = var9;
         }

         ELFUtils.a var10 = this.h;
         var10.d = var2.readInt();
         var10.e = var2.readShort();
         var10.f = var2.readShort();
         var10.g = var2.readShort();
         var10.h = var2.readShort();
         var10.i = var2.readShort();
         var10.j = var2.readShort();
         this.i = new ELFUtils.k[var10.i];

         for(int var5 = 0; var5 < var10.i; ++var5) {
            long var6 = var10.a() + (long)(var5 * var10.h);
            var2.seek(var6);
            if (var3) {
               ELFUtils.h var8 = new ELFUtils.h();
               var8.g = var2.readInt();
               var8.h = var2.readInt();
               var8.a = var2.readFully();
               var8.b = var2.readFully();
               var8.c = var2.readFully();
               var8.d = var2.readFully();
               var8.i = var2.readInt();
               var8.j = var2.readInt();
               var8.e = var2.readFully();
               var8.f = var2.readFully();
               this.i[var5] = var8;
            } else {
               ELFUtils.d var13 = new ELFUtils.d();
               var13.g = var2.readInt();
               var13.h = var2.readInt();
               var13.a = var2.readInt();
               var13.b = var2.readInt();
               var13.c = var2.readInt();
               var13.d = var2.readInt();
               var13.i = var2.readInt();
               var13.j = var2.readInt();
               var13.e = var2.readInt();
               var13.f = var2.readInt();
               this.i[var5] = var13;
            }
         }

         if (var10.j > -1 && var10.j < this.i.length) {
            ELFUtils.k var11 = this.i[var10.j];
            if (var11.h == 3) {
               int var12 = var11.a();
               this.j = new byte[var12];
               var2.seek(var11.b());
               var2.read(this.j);
               if (this.c) {
                  this.f();
               }

            } else {
               throw new UnknownFormatConversionException("Wrong string section e_shstrndx=" + var10.j);
            }
         } else {
            throw new UnknownFormatConversionException("Invalid e_shstrndx=" + var10.j);
         }
      }
   }

   private void f() throws IOException {
      ELFUtils.a var1 = this.h;
      RandomAccessFileWrap var2 = this.g;
      boolean var3 = this.d();
      ELFUtils.k var4 = this.a(".dynsym");
      int var5;
      if (var4 != null) {
         var2.seek(var4.b());
         var5 = var4.a() / (var3 ? 24 : 16);
         this.e = new ELFUtils.l[var5];
         char[] var6 = new char[1];

         for(int var7 = 0; var7 < var5; ++var7) {
            if (var3) {
               ELFUtils.i var8 = new ELFUtils.i();
               var8.c = var2.readInt();
               var2.read(var6);
               var8.d = var6[0];
               var2.read(var6);
               var8.e = var6[0];
               var8.a = var2.readFully();
               var8.b = var2.readFully();
               var8.f = var2.readShort();
               this.e[var7] = var8;
            } else {
               ELFUtils.e var11 = new ELFUtils.e();
               var11.c = var2.readInt();
               var11.a = var2.readInt();
               var11.b = var2.readInt();
               var2.read(var6);
               var11.d = var6[0];
               var2.read(var6);
               var11.e = var6[0];
               var11.f = var2.readShort();
               this.e[var7] = var11;
            }
         }

         ELFUtils.k var10 = this.i[var4.i];
         var2.seek(var10.b());
         this.f = new byte[var10.a()];
         var2.read(this.f);
      }

      this.d = new ELFUtils.j[var1.g];

      for(var5 = 0; var5 < var1.g; ++var5) {
         long var9 = var1.b() + (long)(var5 * var1.f);
         var2.seek(var9);
         if (var3) {
            ELFUtils.g var12 = new ELFUtils.g();
            var12.g = var2.readInt();
            var12.h = var2.readInt();
            var12.a = var2.readFully();
            var12.b = var2.readFully();
            var12.c = var2.readFully();
            var12.d = var2.readFully();
            var12.e = var2.readFully();
            var12.f = var2.readFully();
            this.d[var5] = var12;
         } else {
            ELFUtils.c var13 = new ELFUtils.c();
            var13.g = var2.readInt();
            var13.h = var2.readInt();
            var13.a = var2.readInt();
            var13.b = var2.readInt();
            var13.c = var2.readInt();
            var13.d = var2.readInt();
            var13.e = var2.readInt();
            var13.f = var2.readInt();
            this.d[var5] = var13;
         }
      }

   }

   public final ELFUtils.k a(String var1) {
      ELFUtils.k[] var2 = this.i;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         ELFUtils.k var5 = var2[var4];
         if (var1.equals(this.a(var5.g))) {
            return var5;
         }
      }

      return null;
   }

   public final String a(int var1) {
      if (var1 == 0) {
         return "SHN_UNDEF";
      } else {
         int var3;
         for(var3 = var1; this.j[var3] != 0; ++var3) {
         }

         return new String(this.j, var1, var3 - var1);
      }
   }

   public void close() {
      this.g.close();
   }

   public static boolean a(File var0) {
      long var1 = 0L;

      try {
         RandomAccessFile var3 = new RandomAccessFile(var0, "r");
         var1 = (long)var3.readInt();
         var3.close();
      } catch (Throwable var4) {
         var4.printStackTrace();
         return false;
      }

      return var1 == 2135247942L;
   }

   public static boolean checkElfFile(File file) {
      Object var1 = null;
      if (getJavaVersionStartsWith2() && a(file)) {
         try {
            new ELFUtils(file);
         } catch (IOException var3) {
            Log.e("ELF", "checkElfFile IOException: " + var3);
            return false;
         } catch (UnknownFormatConversionException var4) {
            Log.e("ELF", "checkElfFile UnknownFormatConversionException: " + var4);
         } catch (Throwable var5) {
            Log.e("ELF", "checkElfFile Throwable: " + var5);
         }
      }

      return true;
   }

   private static boolean getJavaVersionStartsWith2() {
      String var0 = System.getProperty("java.vm.version");
      return var0 != null && var0.startsWith("2");
   }

   static class g extends ELFUtils.j {
      long a;
      long b;
      long c;
      long d;
      long e;
      long f;
   }

   static class c extends ELFUtils.j {
      int a;
      int b;
      int c;
      int d;
      int e;
      int f;
   }

   abstract static class j {
      int g;
      int h;
   }

   static class i extends ELFUtils.l {
      long a;
      long b;
   }

   static class e extends ELFUtils.l {
      int a;
      int b;
   }

   abstract static class l {
      int c;
      char d;
      char e;
      short f;
   }

   static class h extends ELFUtils.k {
      long a;
      long b;
      long c;
      long d;
      long e;
      long f;

      public int a() {
         return (int)this.d;
      }

      public long b() {
         return this.c;
      }
   }

   static class d extends ELFUtils.k {
      int a;
      int b;
      int c;
      int d;
      int e;
      int f;

      public int a() {
         return this.d;
      }

      public long b() {
         return (long)this.c;
      }
   }

   public abstract static class k {
      int g;
      int h;
      int i;
      int j;

      public abstract int a();

      public abstract long b();
   }

   static class f extends ELFUtils.a {
      long k;
      long l;
      long m;

      long a() {
         return this.m;
      }

      long b() {
         return this.l;
      }
   }

   static class b extends ELFUtils.a {
      int k;
      int l;
      int m;

      long a() {
         return (long)this.m;
      }

      long b() {
         return (long)this.l;
      }
   }

   public abstract static class a {
      short a;
      short b;
      int c;
      int d;
      short e;
      short f;
      short g;
      short h;
      short i;
      short j;

      abstract long a();

      abstract long b();
   }
}
