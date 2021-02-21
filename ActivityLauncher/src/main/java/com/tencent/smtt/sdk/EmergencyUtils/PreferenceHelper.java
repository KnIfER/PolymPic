package com.tencent.smtt.sdk.EmergencyUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreferenceHelper {
   private static PreferenceHelper instance;
   private static FileLockHelper fileLock;

   private PreferenceHelper() {
   }

   public static synchronized PreferenceHelper getInstance() {
      if (instance == null) {
         instance = new PreferenceHelper();
      }

      return instance;
   }

   public static String[] splitAsArr(String var0) {
      return !TextUtils.isEmpty(var0) ? var0.split(",") : null;
   }

   public static String joinStrArr(String[] arr) {
      StringBuilder sb = new StringBuilder();
      if (arr != null && arr.length > 0) {
         if (arr.length > 1) {
            for(int var2 = 0; var2 < arr.length - 1; ++var2) {
               sb.append(arr[var2]).append(",");
            }
         }

         sb.append(arr[arr.length - 1]);
      }

      return sb.toString();
   }

   private synchronized SharedPreferences getSharedPreference(Context var1) {
      return var1.getSharedPreferences("tbs_emergence", 4);
   }

   public void appendArrayListStr(Context context, String s, String s1) {
      List arrayList = this.getArrayList(context, s);
      arrayList.add(s1);
      this.putArrayList(context, s, arrayList);
   }

   public void createFileLock(Context var1) {
      fileLock = FileLockHelper.create(new File(var1.getFilesDir(), "prefs.lock"));
   }

   public boolean hasFileLock() {
      return fileLock != null;
   }

   public void releaseFileLock() {
      if (fileLock != null) {
         fileLock.tryDelete();
         fileLock = null;
      }
   }

   public void putArrayList(Context var1, String var2, List<String> var3) {
      SharedPreferences var4 = this.getSharedPreference(var1);
      Editor var5 = var4.edit();
      StringBuilder var6 = new StringBuilder();
      if (var3 != null && !var3.isEmpty()) {
         if (var3.size() > 1) {
            for(int var7 = 0; var7 < var3.size() - 1; ++var7) {
               var6.append((String)var3.get(var7));
               var6.append(";");
            }
         }

         var6.append((String)var3.get(var3.size() - 1));
      }

      var5.putString(var2, var6.toString());
      var5.apply();
      var5.commit();
   }

   public List<String> getArrayList(Context var1, String var2) {
      SharedPreferences var3 = this.getSharedPreference(var1);
      String var4 = var3.getString(var2, "");
      ArrayList var5 = new ArrayList();
      String[] var6 = var4.split(";");
      if (var6.length > 0) {
         var5.addAll(Arrays.asList(var6));
      }

      return var5;
   }

   public long getLong(Context var1, String var2) {
      SharedPreferences var3 = this.getSharedPreference(var1);
      return var3.getLong(var2, -1L);
   }

   public void putLong(Context context, String s, long l) {
      SharedPreferences sharedPreferences = this.getSharedPreference(context);
      Editor editor = sharedPreferences.edit();
      editor.putLong(s, l);
      editor.apply();
      editor.commit();
   }
}
