package com.tencent.smtt.utils;

import android.os.Build.VERSION;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class f {
   public static String a(String var0, byte[] var1, f.a var2, boolean var3) {
      String var4 = null;
      String var5 = null;

      try {
         String var6;
         if (var3) {
            var6 = RSAUtil.getInstance().c();
         } else {
            var6 = DESedeUtils.getInstance().b();
         }

         var5 = var0 + var6;
      } catch (Exception var9) {
         var9.printStackTrace();
         return var4;
      }

      try {
         if (var3) {
            var1 = RSAUtil.getInstance().a(var1);
         } else {
            var1 = DESedeUtils.getInstance().a(var1);
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      if (var1 == null) {
         return var4;
      } else {
         HashMap var10 = new HashMap();
         var10.put("Content-Type", "application/x-www-form-urlencoded");
         var10.put("Content-Length", String.valueOf(var1.length));
         HttpURLConnection var7 = a((String)var5, (Map)var10);
         if (var7 != null) {
            b(var7, var1);
            var4 = a(var7, var2, var3);
         }

         return var4;
      }
   }

   public static String a(String var0, Map<String, String> var1, byte[] var2, f.a var3, boolean var4) {
      String var5 = null;
      if (var2 == null) {
         return var5;
      } else {
         HttpURLConnection var6 = a(var0, var1);
         if (var6 != null) {
            if (var4) {
               a(var6, var2);
            } else {
               b(var6, var2);
            }

            var5 = a(var6, var3, false);
         }

         return var5;
      }
   }

   private static HttpURLConnection a(String var0, Map<String, String> var1) {
      HttpURLConnection httpURLConnection = null;

      try {
         URL var3 = new URL(var0);
         httpURLConnection = (HttpURLConnection)var3.openConnection();
         httpURLConnection.setRequestMethod("POST");
      } catch (Exception var5) {
         var5.printStackTrace();
         return httpURLConnection;
      }

      httpURLConnection.setDoOutput(true);
      httpURLConnection.setDoInput(true);
      httpURLConnection.setUseCaches(false);
      httpURLConnection.setConnectTimeout(20000);
      if (VERSION.SDK_INT > 13) {
         httpURLConnection.setRequestProperty("Connection", "close");
      } else {
         httpURLConnection.setRequestProperty("http.keepAlive", "false");
      }

      Iterator iterator = var1.entrySet().iterator();

      while(iterator.hasNext()) {
         Entry entry = (Entry)iterator.next();
         httpURLConnection.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
      }

      return httpURLConnection;
   }

   private static void a(HttpURLConnection var0, byte[] var1) {
      Object var2 = null;
      GZIPOutputStream var3 = null;
      OutputStream var4 = null;

      try {
         var4 = var0.getOutputStream();
         var3 = new GZIPOutputStream(new BufferedOutputStream(var4, 204800));
         var3.write(var1);
         var3.flush();
      } catch (Exception var10) {
         var10.printStackTrace();
      } finally {
         a((Closeable)var2);
         a(var3);
      }

   }

   private static void b(HttpURLConnection var0, byte[] var1) {
      OutputStream var2 = null;

      try {
         var2 = var0.getOutputStream();
         var2.write(var1);
         var2.flush();
      } catch (Exception var7) {
         var7.printStackTrace();
      } finally {
         a(var2);
      }

   }

   private static String a(HttpURLConnection var0, f.a var1, boolean var2) {
      String var3 = null;
      Object var4 = null;
      ByteArrayOutputStream var5 = null;

      try {
         int var6 = var0.getResponseCode();
         if (var1 != null) {
            var1.a(var6);
         }

         if (var6 == 200) {
            InputStream var7 = var0.getInputStream();
            String var8 = var0.getContentEncoding();
            if (var8 != null && var8.equalsIgnoreCase("gzip")) {
               var4 = new GZIPInputStream(var7);
            } else if (var8 != null && var8.equalsIgnoreCase("deflate")) {
               var4 = new InflaterInputStream(var7, new Inflater(true));
            } else {
               var4 = var7;
            }

            var5 = new ByteArrayOutputStream();
            byte[] var9 = new byte[128];
            boolean var10 = false;

            int var16;
            while((var16 = ((InputStream)var4).read(var9)) != -1) {
               var5.write(var9, 0, var16);
            }

            if (var2) {
               var3 = new String(var5.toByteArray(), "utf-8");
            } else {
               var3 = new String(DESedeUtils.getInstance().c(var5.toByteArray()));
            }
         }
      } catch (Throwable var14) {
         var14.printStackTrace();
      } finally {
         a((Closeable)var4);
         a(var5);
      }

      return var3;
   }

   private static void a(Closeable var0) {
      if (var0 != null) {
         try {
            var0.close();
         } catch (Exception var2) {
         }
      }

   }

   public interface a {
      void a(int var1);
   }
}
