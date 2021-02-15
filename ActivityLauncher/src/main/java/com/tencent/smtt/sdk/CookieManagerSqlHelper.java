package com.tencent.smtt.sdk;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.TbsLog;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class CookieManagerSqlHelper {
   public static final String a;
   static File b;

   public static File getWebviewCookiesDir(Context context) {
      if (b == null && context != null) {
         b = new File(context.getDir("webview", 0), "Cookies");
      }

      if (b == null) {
         b = new File("/data/data/" + context.getPackageName() + File.separator + "app_webview" + File.separator + "Cookies");
      }

      return b;
   }

   public static boolean deleteWebviewCookiesDir(Context context) {
      if (context == null) {
         return false;
      } else {
         FileHelper.delete(getWebviewCookiesDir(context), false);
         return true;
      }
   }

   public static SQLiteDatabase openSqlDBIfExists(Context context) {
      if (context == null) {
         return null;
      } else {
         File file = getWebviewCookiesDir(context);
         if (file == null) {
            return null;
         } else {
            SQLiteDatabase var2 = null;

            try {
               var2 = SQLiteDatabase.openDatabase(file.getAbsolutePath(), (CursorFactory)null, 0);
            } catch (Exception var4) {
            }

            if (var2 == null) {
               TbsLog.i(a, "dbPath is not exist!");
            }

            return var2;
         }
      }
   }

   public static ArrayList<String> getSqlTableList(SQLiteDatabase var0) {
      if (var0 == null) {
         return null;
      } else {
         ArrayList var1 = new ArrayList();
         String var2 = "select * from sqlite_master where type='table'";
         Cursor var3 = null;

         try {
            var3 = var0.rawQuery(var2, (String[])null);
            if (var3.moveToFirst()) {
               do {
                  String var4 = var3.getString(1);
                  String var5 = var3.getString(4);
                  var1.add(var4);
                  getAllItemsInTable(var0, var4);
               } while(var3.moveToNext());
            }
         } catch (Throwable var9) {
         } finally {
            if (var3 != null) {
               var3.close();
            }

            if (var0 != null && var0.isOpen()) {
               var0.close();
            }

         }

         return var1;
      }
   }

   private static String getAllItemsInTable(SQLiteDatabase sqLiteDatabase, String table) {
      String var2 = "select * from " + table;
      Cursor cursor = sqLiteDatabase.rawQuery(var2, (String[])null);
      int count = cursor.getCount();
      int columnCount = cursor.getColumnCount();
      StringBuilder sb = new StringBuilder();
      sb.append("raws:" + count + ",columns:" + columnCount + "\n");
      if (count > 0 && cursor.moveToFirst()) {
         do {
            sb.append("\n");

            for(int i = 0; i < columnCount; ++i) {
               String str = null;

               try {
                  str = cursor.getString(i);
               } catch (Exception var10) {
                  continue;
               }

               sb.append(str).append(",");
            }

            sb.append("\n");
         } while(cursor.moveToNext());
      }

      return sb.toString();
   }

   public static int getSqlVersion(Context var0) {
      long now = System.currentTimeMillis();
      SQLiteDatabase sqLiteDatabase = null;
      Cursor cursor = null;
      int val = 0;

      try {
         sqLiteDatabase = openSqlDBIfExists(var0);
         if (sqLiteDatabase == null) {
			 return (byte) -1;
         }

         String sql = "select * from meta";
         cursor = sqLiteDatabase.rawQuery(sql, (String[])null);
         int var7 = cursor.getCount();
         int var8 = cursor.getColumnCount();
         if (var7 > 0 && cursor.moveToFirst()) {
            do {
               if (cursor.getString(0).equals("version")) {
                  String var9 = cursor.getString(1);
                  val = Integer.parseInt(var9);
                  break;
               }
            } while(cursor.moveToNext());
         }
      } catch (Throwable var13) {
      } finally {
         if (cursor != null) {
            cursor.close();
         }

         if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
         }

      }

      return val;
   }

   public static void readCookiesFromDB(Context context, CookieManager.CookieEnum var1, String var2, boolean var3, boolean var4) {
      if (context != null) {
         if (var1 != CookieManager.CookieEnum.b || !TextUtils.isEmpty(var2)) {
            String[] arr = var2.split(",");
            if (arr.length >= 1) {
               SQLiteDatabase sqLiteDatabase = openSqlDBIfExists(context);
               if (sqLiteDatabase != null) {
                  Cursor cursor = null;
                  HashMap<String, String> map = new HashMap<>();

                  String host_key;
                  try {
                     String sql = "select * from cookies";
                     cursor = sqLiteDatabase.rawQuery(sql, (String[])null);
                     int var10 = cursor.getCount();
                     if (var10 > 0 && cursor.moveToFirst()) {
                        do {
							host_key = cursor.getString(cursor.getColumnIndex("host_key"));
                           if (var1 == CookieManager.CookieEnum.b) {
                              boolean found = false;
                              for(int i = 0,len=arr.length; i < len; ++i) {
                                 if (host_key.equals(arr[i])) {
                                    found = true;
                                    break;
                                 }
                              }
                              if (!found) {
                                 continue;
                              }
                           }

                           StringBuilder sb = new StringBuilder();
                           sb.append(cursor.getString(cursor.getColumnIndex("value")));
                           sb.append(";").append(cursor.getString(cursor.getColumnIndex("name")));
                           sb.append(";").append(cursor.getInt(cursor.getColumnIndex("expires_utc")));
                           sb.append(";").append(cursor.getInt(cursor.getColumnIndex("priority")));
                           map.put(host_key, sb.toString());
                        } while(cursor.moveToNext());
                     }
                  } catch (Throwable throwable) {
                     Log.e(a, "getCookieDBVersion exception:" + throwable.toString());
                  } finally {
                     if (cursor != null) {
                        cursor.close();
                     }

                     if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
                        sqLiteDatabase.close();
                     }

                  }

                  if (!map.isEmpty()) {
                     deleteWebviewCookiesDir(context);
                     Iterator iter = map.entrySet().iterator();

                     while(iter.hasNext()) {
                        Entry entry = (Entry)iter.next();
                        host_key = (String)entry.getKey();
                        String value = (String)entry.getValue();
                        CookieManager.getInstance().setCookie(host_key, value, true);
                     }

                     if (VERSION.SDK_INT >= 21) {
                        CookieManager.getInstance().flush();
                     } else {
                        CookieSyncManager.getInstance().sync();
                     }

                     if (var3) {
                        getSqlTableList(openSqlDBIfExists(context));
                        int version = getSqlVersion(context);
                        if (version != -1) {
                           CookieManager.getInstance();
                           CookieManager.setROMCookieDBVersion(context, version);
                        }
                     }

                  }
               }
            }
         }
      }
   }

   static {
      a = CookieManager.LOGTAG;
   }
}
