package com.tencent.smtt.utils;

import android.os.Build.VERSION;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DirWalkCompareUtil {
   private FileWalker walkerA = null;
   private FileWalker walkerB = null;

   public void WalkA(File var1) {
      this.walkerA = new FileWalker(var1);
   }

   public void WalkB(File var1) {
      this.walkerB = new FileWalker(var1);
   }

   public boolean isIdentical() {
      if (this.walkerB != null && this.walkerA != null) {
         return this.walkerB.getFiles().size() == this.walkerA.getFiles().size() && this.isIdentical(this.walkerA, this.walkerB);
      } else {
         return false;
      }
   }

   private boolean isIdentical(FileWalker w1, FileWalker w2) {
      if (w1 != null && w1.getFiles() != null && w2 != null && w2.getFiles() != null) {
         Map fs1 = w1.getFiles();
         Map fs2 = w2.getFiles();
         Iterator iterator = fs1.entrySet().iterator();

         FileRecord fs1Item;
         FileRecord fs2Item;
         do {
            if (!iterator.hasNext()) {
               return true;
            }

            Entry entry = (Entry)iterator.next();
            String key = (String)entry.getKey();
            fs1Item = (FileRecord)entry.getValue();
            if (!fs2.containsKey(key)) {
               return false;
            }

            fs2Item = (FileRecord)fs2.get(key);
         } while(fs1Item.getFileLength() == fs2Item.getFileLength() && fs1Item.getLastModifiedTime() == fs2Item.getLastModifiedTime());

         return false;
      } else {
         return false;
      }
   }

   class FileWalker {
      private Map<String, FileRecord> fileRecords = new HashMap();

      Map<String, FileRecord> getFiles() {
         return this.fileRecords;
      }

      FileWalker(File file) {
         this.fileRecords.clear();
         this.walkDir(file);
      }

      private void walkDir(File dir) {
         if (dir.isDirectory()) {
            File[] listFiles = dir.listFiles();
            if (listFiles == null && VERSION.SDK_INT >= 26) {
               return;
            }

            for(int var3 = 0; var3 < listFiles.length; ++var3) {
               this.walkDir(listFiles[var3]);
            }
         } else if (dir.isFile()) {
            this.recordFile(dir.getName(), dir.length(), dir.lastModified());
         }

      }

      private void recordFile(String fileName, long fileLen, long time) {
         if (fileName != null && fileName.length() > 0 && fileLen > 0L && time > 0L) {
            FileRecord fileRecord = DirWalkCompareUtil.this.new FileRecord(fileName, fileLen, time);
            if (!this.fileRecords.containsKey(fileName)) {
               this.fileRecords.put(fileName, fileRecord);
            }
         }

      }
   }

   class FileRecord {
      private String fileName;
      private long fileLen;
      private long time;

      FileRecord(String fileName, long fileLen, long time) {
         this.fileName = fileName;
         this.fileLen = fileLen;
         this.time = time;
      }

      long getFileLength() {
         return this.fileLen;
      }

      long getLastModifiedTime() {
         return this.time;
      }
   }
}
