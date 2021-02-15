package com.tencent.smtt.sdk.EmergencyUtils;

import com.tencent.smtt.utils.TbsLog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;

public class FileLockHelper {
   private static String a = "EmergencyManager";
   private final File b;
   private final FileOutputStream c;
   private final FileLock d;

   private FileLockHelper(File var1, FileOutputStream var2, FileLock var3) {
      this.b = var1;
      this.c = var2;
      this.d = var3;
   }

   public static FileLockHelper create(File var0) {
      FileOutputStream var1 = null;

      try {
         var1 = new FileOutputStream(var0);
         FileLock fileLock = var1.getChannel().tryLock();
         if (fileLock != null) {
            TbsLog.i(a, "Created lock file: " + var0.getAbsolutePath());
			 return new FileLockHelper(var0, var1, fileLock);
         }
      } catch (Throwable var14) {
         TbsLog.e(a, "Failed to try to acquire lock: " + var0.getAbsolutePath() + " error: " + var14.getMessage());
      } finally {
         if (var1 != null) {
            try {
               var1.close();
            } catch (IOException var13) {
               TbsLog.e(a, "Failed to close: " + var13.getMessage());
            }
         }

      }

      return null;
   }

   public void delete() throws IOException {
      TbsLog.i(a, "Deleting lock file: " + this.b.getAbsolutePath());
      this.d.release();
      this.c.close();
      if (!this.b.delete()) {
         throw new IOException("Failed to delete lock file: " + this.b.getAbsolutePath());
      }
   }

   public void tryDelete() {
      try {
         this.delete();
      } catch (IOException var2) {
         TbsLog.e(a, "Failed to release process lock file: " + this.b.getAbsolutePath() + " error: " + var2);
      }

   }
}
