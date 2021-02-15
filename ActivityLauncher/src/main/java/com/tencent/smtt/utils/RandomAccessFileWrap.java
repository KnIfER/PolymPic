package com.tencent.smtt.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccessFileWrap implements Closeable {
   private final RandomAccessFile randomAccessFile;
   private final File file;
   private final byte[] buff;
   private boolean SmallEnd;

   public RandomAccessFileWrap(String var1) throws FileNotFoundException {
      this(new File(var1));
   }

   public RandomAccessFileWrap(File var1) throws FileNotFoundException {
      this.buff = new byte[8];
      this.file = var1;
      this.randomAccessFile = new RandomAccessFile(this.file, "r");
   }

   public void setSmallEnd(boolean var1) {
      this.SmallEnd = var1;
   }

   public void seek(long var1) throws IOException {
      this.randomAccessFile.seek(var1);
   }

   public final int read(byte[] var1) throws IOException {
      return this.randomAccessFile.read(var1);
   }

   public final int read(char[] var1) throws IOException {
      byte[] var2 = new byte[var1.length];
      int var3 = this.randomAccessFile.read(var2);

      for(int var4 = 0; var4 < var1.length; ++var4) {
         var1[var4] = (char)var2[var4];
      }

      return var3;
   }

   public final short readShort() throws IOException {
      short var1 = this.randomAccessFile.readShort();
      return this.SmallEnd ? (short)((var1 & 255) << 8 | (var1 & '\uff00') >>> 8) : var1;
   }

   public final int readInt() throws IOException {
      int var1 = this.randomAccessFile.readInt();
      return this.SmallEnd ? (var1 & 255) << 24 | (var1 & '\uff00') << 8 | (var1 & 16711680) >>> 8 | (var1 & -16777216) >>> 24 : var1;
   }

   public final long readFully() throws IOException {
      if (this.SmallEnd) {
         this.randomAccessFile.readFully(this.buff, 0, 8);
         return (long)this.buff[7] << 56 | (long)(this.buff[6] & 255) << 48 | (long)(this.buff[5] & 255) << 40 | (long)(this.buff[4] & 255) << 32 | (long)(this.buff[3] & 255) << 24 | (long)(this.buff[2] & 255) << 16 | (long)(this.buff[1] & 255) << 8 | (long)(this.buff[0] & 255);
      } else {
         return this.randomAccessFile.readLong();
      }
   }

   public void close() {
      try {
         this.randomAccessFile.close();
      } catch (IOException var2) {
         var2.printStackTrace();
      }

   }
}
