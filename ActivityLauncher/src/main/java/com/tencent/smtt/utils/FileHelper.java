package com.tencent.smtt.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.Build.VERSION;
import android.util.Log;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsDownloadConfig;
import com.tencent.smtt.sdk.TbsLogReport;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Enumeration;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@SuppressLint({"NewApi"})
public class FileHelper {
   private static final int lib_ln = "lib/".length();
   public static String a = null;
   private static RandomAccessFile randomAccessFile = null;
   public static final IdenticalComparator b = new IdenticalComparator() {
      public boolean identical(File var1, File var2) {
         return var1.length() == var2.length() && var1.lastModified() == var2.lastModified();
      }
   };

   public static String getBackUpDir(Context context, int type) {
      return getBackUpDir(context, context.getApplicationInfo().packageName, type, true);
   }

   public static String getBackUpDir(Context context, String packageName, int type, boolean var3) {
      if (context == null) {
         return "";
      } else {
         String var4 = "";

         try {
            var4 = Environment.getExternalStorageDirectory() + File.separator;
         } catch (Exception var8) {
            var8.printStackTrace();
         }

         switch(type) {
         case 1:
            return var4.equals("") ? var4 : var4 + "tencent" + File.separator + "tbs" + File.separator + packageName;
         case 2:
            return var4.equals("") ? var4 : var4 + "tbs" + File.separator + "backup" + File.separator + packageName;
         case 3:
            return var4.equals("") ? var4 : var4 + "tencent" + File.separator + "tbs" + File.separator + "backup" + File.separator + packageName;
         case 4:
            if (var4.equals("")) {
               return getExternalFilesDir(context, "backup");
            }

            String var5 = var4 + "tencent" + File.separator + "tbs" + File.separator + "backup" + File.separator + packageName;
            if (var3) {
               File file = new File(var5);
               if (!file.exists() || !file.canWrite()) {
                  if (!file.exists()) {
                     file.mkdirs();
                     boolean var7 = file.canWrite();
                     if (!var7) {
                        var5 = getExternalFilesDir(context, "backup");
                     }
                  } else {
                     var5 = getExternalFilesDir(context, "backup");
                  }
               }
            }

            return var5;
         case 5:
            return var4.equals("") ? var4 : var4 + "tencent" + File.separator + "tbs" + File.separator + packageName;
         case 6:
            if (a != null) {
               return a;
            }

            a = getExternalFilesDir(context, "tbslog");
            return a;
         case 7:
            return var4.equals("") ? var4 : var4 + "tencent" + File.separator + "tbs" + File.separator + "backup" + File.separator + "core";
         case 8:
            return getExternalFilesDir(context, "env");
         default:
            return "";
         }
      }
   }

   private static String getExternalFilesDir(Context context, String filesDirName) {
      String ret = "";
      if (context == null) {
         return ret;
      } else {
         Context context1 = context.getApplicationContext();
         if (context1 == null) {
            context1 = context;
         }

         try {
            ret = context1.getExternalFilesDir(filesDirName).getAbsolutePath();
         } catch (Throwable throwable) {
            TbsLog.i(throwable);

            try {
               ret = Environment.getExternalStorageDirectory() + File.separator + "Android" + File.separator + "data" + File.separator + context1.getApplicationInfo().packageName + File.separator + "files" + File.separator + filesDirName;
            } catch (Exception e) {
               e.printStackTrace();
               return "";
            }
         }

         return ret;
      }
   }

   public static boolean hasWriteExternalStoragePermission(Context context) {
      if (VERSION.SDK_INT < 23) {
         return true;
      } else {
         boolean ret = false;
         if (context != null) {
            ret = context.getApplicationContext().checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") == 0;
         }
         return ret;
      }
   }

   public static boolean a(File var0, File var1) throws Exception {
      return a(var0.getPath(), var1.getPath());
   }

   @SuppressLint({"InlinedApi"})
   public static boolean a(String var0, String var1) throws Exception {
      String var2 = Build.CPU_ABI;
      String var3 = VERSION.SDK_INT >= 8 ? Build.CPU_ABI2 : null;
      String var4 = PropertyUtils.getQuickly("ro.product.cpu.upgradeabi", "armeabi");
      return a(var0, var1, var2, var3, var4);
   }

   private static boolean a(String var0, final String path, String var2, String var3, String var4) throws Exception {
      return a(var0, var2, var3, var4, new FileHelper.b() {
         public boolean a(InputStream inputStream, ZipEntry zipEntry, String fn) throws Exception {
            try {
               return FileHelper.extractZipEntry(inputStream, zipEntry, path, fn);
            } catch (Exception var5) {
               throw new Exception("copyFileIfChanged Exception", var5);
            }
         }
      });
   }

   private static boolean a(String zipPath, String var1, String var2, String var3, FileHelper.b var4) throws Exception {
      try (ZipFile zipFile = new ZipFile(zipPath)){
         boolean var6 = false;
         boolean var7 = false;
         Enumeration entries = zipFile.entries();

         while(entries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry)entries.nextElement();
            String entryName = zipEntry.getName();
            if (entryName != null && !entryName.contains("../") && (entryName.startsWith("lib/") || entryName.startsWith("assets/"))) {
               String fileName = entryName.substring(entryName.lastIndexOf(47));
               if (fileName.endsWith(".so")) {
                  if (entryName.regionMatches(lib_ln, var1, 0, var1.length()) && entryName.charAt(lib_ln + var1.length()) == '/') {
                     var6 = true;
                  } else if (var2 != null && entryName.regionMatches(lib_ln, var2, 0, var2.length()) && entryName.charAt(lib_ln + var2.length()) == '/') {
                     var7 = true;
                     if (var6) {
                        continue;
                     }
                  } else if (var3 == null || !entryName.regionMatches(lib_ln, var3, 0, var3.length()) || entryName.charAt(lib_ln + var3.length()) != '/' || var6 || var7) {
                     continue;
                  }
               }

               ;

               try (InputStream inputStream = zipFile.getInputStream(zipEntry)){
                  if (!var4.a(inputStream, zipEntry, fileName.substring(1))) {
					  return false;
                  }
               }
               
            }
         }

         return true;
      }
   }

   @SuppressLint({"NewApi"})
   private static boolean extractZipEntry(InputStream inputStream, ZipEntry zipEntry, String path, String fileName) throws Exception {
      forceCreateNewDir(new File(path));
      String filePath = path + File.separator + fileName;
      File file = new File(filePath);
      FileOutputStream fileOutputStream = null;

      try {
         fileOutputStream = new FileOutputStream(file);
         byte[] buff = new byte[8192];

         int len;
         while((len = inputStream.read(buff)) > 0) {
            fileOutputStream.write(buff, 0, len);
         }
      } catch (IOException e) {
         delete(file);
         throw new IOException("Couldn't write dst file " + file, e);
      } finally {
         if (fileOutputStream != null) {
            fileOutputStream.close();
         }

      }

      if (tallyFileCRC32(filePath, zipEntry.getSize(), zipEntry.getTime(), zipEntry.getCrc())) {
         TbsLog.e("FileHelper", "file is different: " + filePath);
         return false;
      } else {
         if (!file.setLastModified(zipEntry.getTime())) {
            TbsLog.e("FileHelper", "Couldn't set time for dst file " + file);
         }

         return true;
      }
   }

   private static boolean tallyFileCRC32(String path, long size, long var3, long tally) throws Exception {
      File file = new File(path);
      if (file.length() != size) {
         TbsLog.e("FileHelper", "file size doesn't match: " + file.length() + " vs " + size);
         return true;
      } else {
         try (FileInputStream fileInputStream = new FileInputStream(file)){
            CRC32 crc32 = new CRC32();
            byte[] buff = new byte[8192];

            int len;
            while((len = fileInputStream.read(buff)) > 0) {
               crc32.update(buff, 0, len);
            }

            long value = crc32.getValue();
            TbsLog.i("FileHelper", "" + file.getName() + ": crc = " + value + ", zipCrc = " + tally);
            if (value != tally) {
				return true;
            }
         }

         return false;
      }
   }

   public static boolean forceTransferFile(File src, File target) throws Exception {
      return forceTransferFile(src, target, (FileFilter)null);
   }

   public static boolean forceTransferFile(File src, File target, FileFilter fileFilter) throws Exception {
      return forceTransferFile(src, target, fileFilter, b);
   }

   public static boolean forceTransferFile(File src, File target, FileFilter fileFilter, IdenticalComparator identicalComparator) throws Exception {
      if (src != null && target != null) {
         if (!src.exists()) {
            return false;
         } else if (src.isFile()) {
            return forceTransferFileInner(src, target, fileFilter, identicalComparator);
         } else {
            File[] var4 = src.listFiles(fileFilter);
            if (var4 == null) {
               return false;
            } else {
               boolean var5 = true;
               File[] var6 = var4;
               int var7 = var4.length;

               for(int var8 = 0; var8 < var7; ++var8) {
                  File var9 = var6[var8];
                  if (!forceTransferFile(var9, new File(target, var9.getName()), fileFilter)) {
                     var5 = false;
                  }
               }

               return var5;
            }
         }
      } else {
         return false;
      }
   }

   private static boolean forceTransferFileInner(File src, File target, FileFilter fileFilter, IdenticalComparator identicalComparator) throws Exception {
      if (src != null && target != null) {
         if (fileFilter != null && !fileFilter.accept(src)) {
            return false;
         } else {
            FileChannel var4 = null;
            FileChannel var5 = null;

            boolean var11;
            try {
               if (!src.exists() || !src.isFile()) {
                  return false;
               }

               if (target.exists()) {
                  if (identicalComparator != null && identicalComparator.identical(src, target)) {
                     return true;
                  }

                  delete(target);
               }

               File parentFile = target.getParentFile();
               if (parentFile.isFile()) {
                  delete(parentFile);
               }

               if (!parentFile.exists() && !parentFile.mkdirs()) {
                  boolean var16 = false;
                  return var16;
               }

               var4 = (new FileInputStream(src)).getChannel();
               var5 = (new FileOutputStream(target)).getChannel();
               long size = var4.size();
               long size1 = var5.transferFrom(var4, 0L, size);
               if (size1 == size) {
                  return true;
               }

               delete(target);
               var11 = false;
            } finally {
               if (var4 != null) {
                  var4.close();
               }

               if (var5 != null) {
                  var5.close();
               }

            }

            return var11;
         }
      } else {
         return false;
      }
   }

   public static boolean forceCreateNewDir(File file) {
      if (file == null) {
         return false;
      } else if (file.exists() && file.isDirectory()) {
         return true;
      } else {
         delete(file);
         return file.mkdirs();
      }
   }

   public static void delete(File file) {
      delete(file, false);
   }

   public static void delete(File file, boolean ignore) {
      TbsLog.i("FileUtils", "delete file,ignore=" + ignore + file + Log.getStackTraceString(new Throwable()));
      if (file != null && file.exists()) {
         if (file.isFile()) {
            file.delete();
         } else {
            File[] var2 = file.listFiles();
            if (var2 != null) {
               File[] var3 = var2;
               int var4 = var2.length;

               for(int var5 = 0; var5 < var4; ++var5) {
                  File var6 = var3[var5];
                  delete(var6, ignore);
               }

               if (!ignore) {
                  file.delete();
               }

            }
         }
      }
   }

   public static void delete(File file, boolean ignore, String except) {
      TbsLog.i("FileUtils", "delete file,ignore=" + ignore + "except" + except + file + Log.getStackTraceString(new Throwable()));
      if (file != null && file.exists()) {
         if (file.isFile()) {
            file.delete();
         } else {
            File[] var3 = file.listFiles();
            if (var3 != null) {
               File[] var4 = var3;
               int var5 = var3.length;

               for(int var6 = 0; var6 < var5; ++var6) {
                  File var7 = var4[var6];
                  if (!var7.getName().equals(except)) {
                     delete(var7, ignore);
                  }
               }

               if (!ignore) {
                  file.delete();
               }

            }
         }
      }
   }

   public static boolean fileWritten(File file) {
      return file != null && file.exists() && file.isFile() && file.length() > 0L;
   }

   public static long copyInner(InputStream inputStream, OutputStream outputStream) throws IOException, OutOfMemoryError {
      if (inputStream == null) {
         return -1L;
      } else {
         byte[] var2 = new byte[4096];
         long var3 = 0L;

         int var6;
         for(boolean var5 = false; -1 != (var6 = inputStream.read(var2)); var3 += (long)var6) {
            outputStream.write(var2, 0, var6);
         }

         return var3;
      }
   }

   public static int copy(InputStream inputStream, OutputStream outputStream) throws IOException, OutOfMemoryError {
      long var2 = copyInner(inputStream, outputStream);
      return var2 > 2147483647L ? -1 : (int)var2;
   }

   public static FileOutputStream getOutputStream(File file) throws IOException {
      if (file.exists()) {
         if (file.isDirectory()) {
            throw new IOException("File '" + file + "' exists but is a directory");
         }

         if (!file.canWrite()) {
            throw new IOException("File '" + file + "' cannot be written to");
         }
      } else {
         File parentFile = file.getParentFile();
         if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("File '" + file + "' could not be created");
         }
      }

      return new FileOutputStream(file);
   }

   public static boolean hasEnoughFreeSpace(Context context) {
      long spaceAvailable = FsSpaceUtil.calcFsSpaceAvailable();
      boolean ret = spaceAvailable >= TbsDownloadConfig.getInstance(context).getDownloadMinFreeSpace();
      if (!ret) {
         TbsLog.e("TbsDownload", "[TbsApkDwonloader.hasEnoughFreeSpace] freeSpace too small,  freeSpace = " + spaceAvailable);
      }

      return ret;
   }

   public static String getSdcardFileLocksPath(Context context) {
      return Environment.getExternalStorageDirectory() + File.separator + "tbs" + File.separator + "file_locks";
   }

   static String getCorePrivatePath(Context context) {
      File tbsFolderDir = QbSdk.getTbsFolderDir(context);
      File corePrivate = new File(tbsFolderDir, "core_private");
      if (corePrivate != null) {
         if (!corePrivate.isDirectory()) {
            if (!corePrivate.mkdir()) {
               return null;
            }
         }

         return corePrivate.getAbsolutePath();
      } else {
         return null;
      }
   }

   public static File getLockFileInner(Context context, boolean isAppLock, String fileName) {
      String path = null;
      if (isAppLock) {
         path = getCorePrivatePath(context);
      } else {
         path = getSdcardFileLocksPath(context);
      }

      File pathDir = null;
      File ret = null;
      if (path == null) {
         return null;
      } else {
         pathDir = new File(path);
         if (!pathDir.exists()) {
            pathDir.mkdirs();
         }

         if (!pathDir.canWrite()) {
            return null;
         } else {
            ret = new File(pathDir, fileName);
            if (!ret.exists()) {
               try {
                  ret.createNewFile();
               } catch (IOException e) {
                  e.printStackTrace();
                  return null;
               }
            }

            return ret;
         }
      }
   }

   public static File getPermanentTbsFile(Context var0, String var1) {
      File var2 = var0.getFilesDir();
      File var3 = new File(var2, "tbs");
      File var5 = null;
      if (var3 == null) {
         return null;
      } else {
         if (!var3.exists()) {
            var3.mkdirs();
         }

         if (!var3.canWrite()) {
            TbsLog.e("FileHelper", "getPermanentTbsFile -- no permission!");
            return null;
         } else {
            var5 = new File(var3, var1);
            if (!var5.exists()) {
               try {
                  var5.createNewFile();
               } catch (IOException var7) {
                  TbsLog.e("FileHelper", "getPermanentTbsFile -- exception: " + var7);
                  return null;
               }
            }

            return var5;
         }
      }
   }

   public static FileOutputStream getLockFile(Context context, boolean isAppLock, String fileName) {
      File file = getLockFileInner(context, isAppLock, fileName);
      if (file != null) {
         try {
            return new FileOutputStream(file);
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         }
      }

      return null;
   }

   public static FileLock lockStream(Context context, FileOutputStream fileOutputStream) {
      if (fileOutputStream == null) {
         return null;
      } else {
         FileLock fileLock = null;

         try {
            fileLock = fileOutputStream.getChannel().tryLock();
            if (fileLock.isValid()) {
               return fileLock;
            }
         } catch (OverlappingFileLockException var4) {
            var4.printStackTrace();
         } catch (Exception var5) {
            var5.printStackTrace();
         }

         return null;
      }
   }

   public static void releaseFileLock(FileLock fileLock, FileOutputStream fileOutputStream) {
      if (fileLock != null) {
         try {
            FileChannel fileChannel = fileLock.channel();
            if (fileChannel != null && fileChannel.isOpen()) {
               fileLock.release();
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      if (fileOutputStream != null) {
         try {
            fileOutputStream.close();
         } catch (Exception ignored) { }
      }

   }

   public static FileLock getTbsCoreLoadFileLock(Context context) {
      TbsLog.i("FileHelper", "getTbsCoreLoadFileLock #1");
      boolean var1 = true;

      try {
         var1 = TbsDownloadConfig.getInstance().getTbsCoreLoadRenameFileLockEnable();
      } catch (Throwable var7) {
      }

      FileLock var2;
      if (!var1) {
         var2 = null;
         FileOutputStream var8 = getLockFile(context, true, "tbs_rename_lock");
         if (var8 == null) {
            TbsLog.i("FileHelper", "init -- failed to get rename fileLock#1!");
         } else {
            var2 = lockStream(context, var8);
            if (var2 == null) {
               TbsLog.i("FileHelper", "init -- failed to get rename fileLock#2!");
            } else {
               TbsLog.i("FileHelper", "init -- get rename fileLock success!");
            }
         }

         TbsLog.i("FileHelper", "getTbsCoreLoadFileLock #2 renameFileLock is " + var2);
         return var2;
      } else {
         TbsLog.i("FileHelper", "getTbsCoreLoadFileLock #3");
         var2 = null;
         String var3 = "tbs_rename_lock";
         File var4 = getPermanentTbsFile(context, var3);
         TbsLog.i("FileHelper", "getTbsCoreLoadFileLock #4 " + var4);

         try {
            randomAccessFile = new RandomAccessFile(var4.getAbsolutePath(), "r");
            FileChannel var5 = randomAccessFile.getChannel();
            var2 = var5.tryLock(0L, Long.MAX_VALUE, true);
         } catch (Throwable var6) {
            TbsLog.e("FileHelper", "getTbsCoreLoadFileLock -- exception: " + var6);
         }

         if (var2 == null) {
            var2 = getTbsCoreLoadFileLockRetry(context);
         }

         if (var2 == null) {
            TbsLog.i("FileHelper", "getTbsCoreLoadFileLock -- failed: " + var3);
         } else {
            TbsLog.i("FileHelper", "getTbsCoreLoadFileLock -- success: " + var3);
         }

         return var2;
      }
   }

   private static FileLock getTbsCoreLoadFileLockRetry(Context context) {
      FileLock var1 = null;

      try {
         TbsLogReport.TbsLogInfo var2 = TbsLogReport.getInstance(context).tbsLogInfo();
         var2.setErrorCode(803);
         String var3 = "tbs_rename_lock";
         File var4 = getPermanentTbsFile(context, var3);
         if (TbsDownloadConfig.getInstance(context).getTbsCoreLoadRenameFileLockWaitEnable()) {
            int var5;
            for(var5 = 0; var5 < 20 && var1 == null; ++var5) {
               try {
                  try {
                     Thread.sleep(100L);
                  } catch (Exception var7) {
                     var7.printStackTrace();
                  }

                  randomAccessFile = new RandomAccessFile(var4.getAbsolutePath(), "r");
                  FileChannel var6 = randomAccessFile.getChannel();
                  var1 = var6.tryLock(0L, Long.MAX_VALUE, true);
               } catch (Throwable var8) {
                  TbsLog.i(var8);
               }
            }

            if (var1 != null) {
               var2.setErrorCode(802);
            } else {
               var2.setErrorCode(801);
            }

            TbsLogReport.getInstance(context).eventReport(TbsLogReport.EventType.TYPE_SDK_REPORT_INFO, var2);
            TbsLog.i("FileHelper", "getTbsCoreLoadFileLock,retry num=" + var5 + "success=" + (var1 == null));
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      }

      return var1;
   }

   public static FileLock getTbsCoreRenameFileLock(Context var0) {
      FileLock var1 = null;
      String var2 = "tbs_rename_lock";
      File var3 = getPermanentTbsFile(var0, var2);
      TbsLog.i("FileHelper", "getTbsCoreRenameFileLock #1 " + var3);

      try {
         randomAccessFile = new RandomAccessFile(var3.getAbsolutePath(), "rw");
         FileChannel var4 = randomAccessFile.getChannel();
         var1 = var4.tryLock(0L, Long.MAX_VALUE, false);
      } catch (Throwable var5) {
         TbsLog.e("FileHelper", "getTbsCoreRenameFileLock -- excpetion: " + var2);
      }

      if (var1 == null) {
         TbsLog.i("FileHelper", "getTbsCoreRenameFileLock -- failed: " + var2);
      } else {
         TbsLog.i("FileHelper", "getTbsCoreRenameFileLock -- success: " + var2);
      }

      return var1;
   }

   public static synchronized void releaseTbsCoreRenameFileLock(Context context, FileLock fileLock) {
      TbsLog.i("FileHelper", "releaseTbsCoreRenameFileLock -- lock: " + fileLock);
      FileChannel var2 = fileLock.channel();
      if (var2 != null && var2.isOpen()) {
         try {
            fileLock.release();
         } catch (IOException var4) {
            var4.printStackTrace();
         }
      }

   }

   public interface b {
      boolean a(InputStream var1, ZipEntry var2, String var3) throws Exception;
   }

   public interface IdenticalComparator {
      boolean identical(File file1, File file2);
   }
}
