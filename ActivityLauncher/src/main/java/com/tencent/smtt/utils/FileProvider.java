package com.tencent.smtt.utils;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.tencent.smtt.sdk.QbSdk;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.xmlpull.v1.XmlPullParserException;

public class FileProvider extends ContentProvider {
   private static final String[] a = new String[]{"_display_name", "_size"};
   private static final File b = new File("/");
   private static HashMap<String, FileProvider.a> c = new HashMap();
   private FileProvider.a d;

   public boolean onCreate() {
      return true;
   }

   public void attachInfo(Context var1, ProviderInfo var2) {
      super.attachInfo(var1, var2);
      if (var2.exported) {
         throw new SecurityException("Provider must not be exported");
      } else if (!var2.grantUriPermissions) {
         throw new SecurityException("Provider must grant uri permissions");
      } else {
         this.d = b(var1, var2.authority);
      }
   }

   public static Uri a(Context var0, String var1, File var2) {
      FileProvider.a var3 = b(var0, var1);
      return var3.a(var2);
   }

   public Cursor query(Uri var1, String[] var2, String var3, String[] var4, String var5) {
      File var6 = this.d.a(var1);
      if (var2 == null) {
         var2 = a;
      }

      String[] var7 = new String[var2.length];
      Object[] var8 = new Object[var2.length];
      int var9 = 0;
      String[] var10 = var2;
      int var11 = var2.length;

      for(int var12 = 0; var12 < var11; ++var12) {
         String var13 = var10[var12];
         if ("_display_name".equals(var13)) {
            var7[var9] = "_display_name";
            var8[var9++] = var6.getName();
         } else if ("_size".equals(var13)) {
            var7[var9] = "_size";
            var8[var9++] = var6.length();
         }
      }

      var7 = a(var7, var9);
      var8 = a(var8, var9);
      MatrixCursor var14 = new MatrixCursor(var7, 1);
      var14.addRow(var8);
      return var14;
   }

   public String getType(Uri var1) {
      File var2 = this.d.a(var1);
      int var3 = var2.getName().lastIndexOf(46);
      if (var3 >= 0) {
         String var4 = var2.getName().substring(var3 + 1);
         String var5 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var4);
         if (var5 != null) {
            return var5;
         }
      }

      return "application/octet-stream";
   }

   public Uri insert(Uri var1, ContentValues var2) {
      throw new UnsupportedOperationException("No external inserts");
   }

   public int update(Uri var1, ContentValues var2, String var3, String[] var4) {
      throw new UnsupportedOperationException("No external updates");
   }

   public int delete(Uri var1, String var2, String[] var3) {
      File var4 = this.d.a(var1);
      return var4.delete() ? 1 : 0;
   }

   public ParcelFileDescriptor openFile(Uri var1, String var2) throws FileNotFoundException {
      File var3 = this.d.a(var1);
      int var4 = a(var2);
      return ParcelFileDescriptor.open(var3, var4);
   }

   private static FileProvider.a b(Context var0, String var1) {
      synchronized(c) {
         FileProvider.a var2 = (FileProvider.a)c.get(var1);
         if (var2 == null) {
            try {
               var2 = c(var0, var1);
            } catch (IOException var6) {
               throw new IllegalArgumentException("Failed to parse android.support.FILE_PROVIDER_PATHS meta-data", var6);
            } catch (XmlPullParserException var7) {
               throw new IllegalArgumentException("Failed to parse android.support.FILE_PROVIDER_PATHS meta-data", var7);
            }

            c.put(var1, var2);
         }

         return var2;
      }
   }

   private static FileProvider.a c(Context var0, String var1) throws IOException, XmlPullParserException {
      FileProvider.b var2 = new FileProvider.b(var1);
      ProviderInfo var3 = var0.getPackageManager().resolveContentProvider(var1, 128);
      if (var3 == null) {
         throw new RuntimeException("Must declare com.tencent.smtt.utils.FileProvider in AndroidManifest above Android 7.0,please view document in x5.tencent.com");
      } else {
         XmlResourceParser var4 = var3.loadXmlMetaData(var0.getPackageManager(), "android.support.FILE_PROVIDER_PATHS");
         if (var4 == null) {
            throw new IllegalArgumentException("Missing android.support.FILE_PROVIDER_PATHS meta-data");
         } else {
            int var5;
            while((var5 = var4.next()) != 1) {
               if (var5 == 2) {
                  String var6 = var4.getName();
                  String var7 = var4.getAttributeValue((String)null, "name");
                  String var8 = var4.getAttributeValue((String)null, "path");
                  File var9 = null;
                  if ("root-path".equals(var6)) {
                     var9 = a(b, var8);
                  } else if ("files-path".equals(var6)) {
                     var9 = a(var0.getFilesDir(), var8);
                  } else if ("cache-path".equals(var6)) {
                     var9 = a(var0.getCacheDir(), var8);
                  } else if ("external-path".equals(var6)) {
                     var9 = a(Environment.getExternalStorageDirectory(), var8);
                  }

                  if (var9 != null) {
                     var2.a(var7, var9);
                  }
               }
            }

            return var2;
         }
      }
   }

   private static int a(String var0) {
      int var1;
      if ("r".equals(var0)) {
         var1 = 268435456;
      } else if (!"w".equals(var0) && !"wt".equals(var0)) {
         if ("wa".equals(var0)) {
            var1 = 704643072;
         } else if ("rw".equals(var0)) {
            var1 = 939524096;
         } else {
            if (!"rwt".equals(var0)) {
               throw new IllegalArgumentException("Invalid mode: " + var0);
            }

            var1 = 1006632960;
         }
      } else {
         var1 = 738197504;
      }

      return var1;
   }

   private static File a(File var0, String... var1) {
      File var2 = var0;
      String[] var3 = var1;
      int var4 = var1.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String var6 = var3[var5];
         if (var6 != null) {
            var2 = new File(var2, var6);
         }
      }

      return var2;
   }

   private static String[] a(String[] var0, int var1) {
      String[] var2 = new String[var1];
      System.arraycopy(var0, 0, var2, 0, var1);
      return var2;
   }

   private static Object[] a(Object[] var0, int var1) {
      Object[] var2 = new Object[var1];
      System.arraycopy(var0, 0, var2, 0, var1);
      return var2;
   }

   static Uri a(Context var0, File var1) {
      Uri var2 = null;
      String var3 = "";
      if (VERSION.SDK_INT >= 24) {
         try {
            ComponentName var4 = new ComponentName(var0.getPackageName(), "android.support.v4.content.FileProvider");
            ProviderInfo var5 = var0.getPackageManager().getProviderInfo(var4, 0);
            var3 = var5.authority;
         } catch (Exception var8) {
            var8.printStackTrace();
         }

         if (!TextUtils.isEmpty(var3)) {
            try {
               Class var9 = Class.forName("android.support.v4.content.FileProvider");
               if (var9 != null) {
                  Method var10 = var9.getDeclaredMethod("getUriForFile", Context.class, String.class, File.class);
                  if (var10 != null) {
                     Object var6 = var10.invoke((Object)null, var0, var3, var1);
                     if (var6 instanceof Uri) {
                        var2 = (Uri)var6;
                     }
                  }
               }
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }
      }

      return var2;
   }

   public static Uri a(Context var0, String var1) {
      Uri var2 = null;
      if (var0 != null && var0.getApplicationContext() != null && "com.tencent.mobileqq".equals(var0.getApplicationContext().getApplicationInfo().packageName)) {
         try {
            Class var3 = Class.forName("com.tencent.mobileqq.utils.kapalaiadapter.FileProvider7Helper");
            var2 = (Uri) ReflectionUtils.a(var3, "getUriForFile", new Class[]{Context.class, File.class}, var0, new File(var1));
            return var2;
         } catch (Exception var4) {
            var4.printStackTrace();
            return null;
         }
      } else {
         if (var0 != null && var0.getApplicationInfo().targetSdkVersion >= 24 && VERSION.SDK_INT >= 24) {
            var2 = a(var0, new File(var1));
            if (var2 == null && QbSdk.checkContentProviderPrivilage(var0)) {
               var2 = a(var0, var0.getApplicationInfo().packageName + ".provider", new File(var1));
            }
         }

         if (var2 == null) {
            try {
               var2 = Uri.fromFile(new File(var1));
            } catch (Exception var5) {
               var5.printStackTrace();
               Log.e("FileProvider", "create uri failed,please check again");
            }
         }

         return var2;
      }
   }

   static class b implements FileProvider.a {
      private final String a;
      private final HashMap<String, File> b = new HashMap();

      public b(String var1) {
         this.a = var1;
      }

      public void a(String var1, File var2) {
         if (TextUtils.isEmpty(var1)) {
            throw new IllegalArgumentException("Name must not be empty");
         } else {
            try {
               var2 = var2.getCanonicalFile();
            } catch (IOException var4) {
               throw new IllegalArgumentException("Failed to resolve canonical path for " + var2, var4);
            }

            this.b.put(var1, var2);
         }
      }

      public Uri a(File var1) {
         String var2;
         try {
            var2 = var1.getCanonicalPath();
         } catch (IOException var7) {
            throw new IllegalArgumentException("Failed to resolve canonical path for " + var1);
         }

         Entry var3 = null;
         Iterator var4 = this.b.entrySet().iterator();

         while(true) {
            Entry var5;
            String var6;
            do {
               do {
                  if (!var4.hasNext()) {
                     if (var3 == null) {
                        throw new IllegalArgumentException("Failed to find configured root that contains " + var2);
                     }

                     String var8 = ((File)var3.getValue()).getPath();
                     if (var8.endsWith("/")) {
                        var2 = var2.substring(var8.length());
                     } else {
                        var2 = var2.substring(var8.length() + 1);
                     }

                     var2 = Uri.encode((String)var3.getKey()) + '/' + Uri.encode(var2, "/");
                     return (new Builder()).scheme("content").authority(this.a).encodedPath(var2).build();
                  }

                  var5 = (Entry)var4.next();
                  var6 = ((File)var5.getValue()).getPath();
               } while(!var2.startsWith(var6));
            } while(var3 != null && var6.length() <= ((File)var3.getValue()).getPath().length());

            var3 = var5;
         }
      }

      public File a(Uri var1) {
         String var2 = var1.getEncodedPath();
         int var3 = var2.indexOf(47, 1);
         String var4 = Uri.decode(var2.substring(1, var3));
         var2 = Uri.decode(var2.substring(var3 + 1));
         File var5 = (File)this.b.get(var4);
         if (var5 == null) {
            throw new IllegalArgumentException("Unable to find configured root for " + var1);
         } else {
            File var6 = new File(var5, var2);

            try {
               var6 = var6.getCanonicalFile();
            } catch (IOException var8) {
               throw new IllegalArgumentException("Failed to resolve canonical path for " + var6);
            }

            if (!var6.getPath().startsWith(var5.getPath())) {
               throw new SecurityException("Resolved path jumped beyond configured root");
            } else {
               return var6;
            }
         }
      }
   }

   interface a {
      Uri a(File var1);

      File a(Uri var1);
   }
}
