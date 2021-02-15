package com.tencent.smtt.utils;

import java.security.KeyFactory;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import javax.crypto.Cipher;

public class RSAUtil {
   private static final char[] mould = "0123456789abcdef".toCharArray();
   private static RSAUtil instance;
   private String c;
   private String d;
   private String e;

   private RSAUtil() {
      int var1 = (new Random()).nextInt(89999999) + 10000000;
      int var2 = (new Random()).nextInt(89999999) + 10000000;
      this.e = String.valueOf(var1);
      this.c = this.e + String.valueOf(var2);
   }

   public static synchronized RSAUtil getInstance() {
      if (instance == null) {
         instance = new RSAUtil();
      }

      return instance;
   }

   public void b() throws Exception {
      ClassLoader var1 = ClassLoader.getSystemClassLoader();
      Class var2 = Class.forName("com.android.org.bouncycastle.jce.provider.BouncyCastleProvider", true, var1);
      Provider var3 = (Provider)var2.newInstance();
      Security.addProvider(var3);
   }

   public String c() throws Exception {
      if (this.d == null) {
         byte[] var1 = this.c.getBytes();
         Cipher var2 = null;

         try {
            var2 = Cipher.getInstance("RSA/ECB/NoPadding");
         } catch (Exception var9) {
            try {
               this.b();
               var2 = Cipher.getInstance("RSA/ECB/NoPadding");
            } catch (Exception var8) {
               var8.printStackTrace();
            }
         }

         byte[] var3 = "MCwwDQYJKoZIhvcNAQEBBQADGwAwGAIRAMRB/Q0hTCD+XtnQhpQJefUCAwEAAQ==".getBytes();
         X509EncodedKeySpec var4 = new X509EncodedKeySpec(android.util.Base64.decode(var3, 0));
         KeyFactory var5 = KeyFactory.getInstance("RSA");
         PublicKey var6 = var5.generatePublic(var4);
         var2.init(1, var6);
         byte[] var7 = var2.doFinal(var1);
         this.d = this.b(var7);
      }

      return this.d;
   }

   public byte[] a(byte[] var1) throws Exception {
      return com.tencent.smtt.sdk.stat.a.a(this.e.getBytes(), var1, 1);
   }

   private String b(byte[] var1) {
      char[] var2 = new char[var1.length * 2];

      for(int var3 = 0; var3 < var1.length; ++var3) {
         int var4 = var1[var3] & 255;
         var2[var3 * 2] = mould[var4 >>> 4];
         var2[var3 * 2 + 1] = mould[var4 & 15];
      }

      return new String(var2);
   }
}
