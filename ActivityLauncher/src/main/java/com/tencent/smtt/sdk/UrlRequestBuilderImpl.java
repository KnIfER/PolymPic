package com.tencent.smtt.sdk;

import android.util.Pair;
import com.tencent.smtt.export.external.DexLoader;
import com.tencent.smtt.export.external.interfaces.UrlRequest;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class UrlRequestBuilderImpl extends UrlRequest.Builder {
   private static final String a = UrlRequestBuilderImpl.class.getSimpleName();
   private final String b;
   private final UrlRequest.Callback c;
   private final Executor d;
   private String e;
   private final ArrayList<Pair<String, String>> f = new ArrayList();
   private boolean g;
   private int h = 3;
   private String i;
   private byte[] j;
   private String k;
   private String l;

   public UrlRequestBuilderImpl(String var1, UrlRequest.Callback var2, Executor var3) {
      if (var1 == null) {
         throw new NullPointerException("URL is required.");
      } else if (var2 == null) {
         throw new NullPointerException("Callback is required.");
      } else if (var3 == null) {
         throw new NullPointerException("Executor is required.");
      } else {
         this.b = var1;
         this.c = var2;
         this.d = var3;
      }
   }

   public UrlRequest.Builder setHttpMethod(String var1) {
      if (var1 == null) {
         throw new NullPointerException("Method is required.");
      } else {
         this.e = var1;
         return this;
      }
   }

   public UrlRequest.Builder setRequestBody(String var1) {
      if (var1 == null) {
         throw new NullPointerException("Body is required.");
      } else {
         this.i = var1;
         return this;
      }
   }

   public UrlRequest.Builder setRequestBodyBytes(byte[] var1) {
      if (var1 == null) {
         throw new NullPointerException("Body is required.");
      } else {
         this.j = var1;
         return this;
      }
   }

   public UrlRequestBuilderImpl addHeader(String var1, String var2) {
      if (var1 == null) {
         throw new NullPointerException("Invalid header name.");
      } else if (var2 == null) {
         throw new NullPointerException("Invalid header value.");
      } else if ("Accept-Encoding".equalsIgnoreCase(var1)) {
         return this;
      } else {
         this.f.add(Pair.create(var1, var2));
         return this;
      }
   }

   public UrlRequestBuilderImpl setDns(String var1, String var2) {
      if (var1 != null && var2 != null) {
         this.k = var1;
         this.l = var2;

         try {
            X5CoreEngine var3 = X5CoreEngine.getInstance();
            if (null != var3 && var3.isInCharge()) {
               DexLoader var4 = var3.getWVWizardBase().getDexLoader();
               var4.invokeStaticMethod("com.tencent.smtt.net.X5UrlRequestProvider", "setDns", new Class[]{String.class, String.class}, this.k, this.l);
            }
         } catch (Exception var5) {
         }

         return this;
      } else {
         throw new NullPointerException("host and address are required.");
      }
   }

   public UrlRequestBuilderImpl disableCache() {
      this.g = true;
      return this;
   }

   public UrlRequestBuilderImpl setPriority(int var1) {
      this.h = var1;
      return this;
   }

   public UrlRequest build() throws NullPointerException {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         DexLoader var2 = var1.getWVWizardBase().getDexLoader();
         UrlRequest var3 = (UrlRequest)var2.invokeStaticMethod("com.tencent.smtt.net.X5UrlRequestProvider", "GetX5UrlRequestProvider", new Class[]{String.class, Integer.TYPE, UrlRequest.Callback.class, Executor.class, Boolean.TYPE, String.class, ArrayList.class, String.class, byte[].class, String.class, String.class}, this.b, this.h, this.c, this.d, this.g, this.e, this.f, this.i, this.j, this.k, this.l);
         if (var3 == null) {
            var3 = (UrlRequest)var2.invokeStaticMethod("com.tencent.smtt.net.X5UrlRequestProvider", "GetX5UrlRequestProvider", new Class[]{String.class, Integer.TYPE, UrlRequest.Callback.class, Executor.class, Boolean.TYPE, String.class, ArrayList.class, String.class}, this.b, this.h, this.c, this.d, this.g, this.e, this.f, this.i);
         }

         if (var3 == null) {
            var3 = (UrlRequest)var2.invokeStaticMethod("com.tencent.smtt.net.X5UrlRequestProvider", "GetX5UrlRequestProvider", new Class[]{String.class, Integer.TYPE, UrlRequest.Callback.class, Executor.class, Boolean.TYPE, String.class, ArrayList.class}, this.b, this.h, this.c, this.d, this.g, this.e, this.f);
         }

         if (var3 == null) {
            var3 = (UrlRequest)var2.invokeStaticMethod("com.tencent.tbs.tbsshell.WebCoreProxy", "UrlRequest_getX5UrlRequestProvider", new Class[]{String.class, Integer.TYPE, UrlRequest.Callback.class, Executor.class, Boolean.TYPE, String.class, ArrayList.class, String.class, byte[].class, String.class, String.class}, this.b, this.h, this.c, this.d, this.g, this.e, this.f, this.i, this.j, this.k, this.l);
         }

         if (var3 == null) {
            throw new NullPointerException("UrlRequest build fail");
         } else {
            return var3;
         }
      } else {
         return null;
      }
   }
}
