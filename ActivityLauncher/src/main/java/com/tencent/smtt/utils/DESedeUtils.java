package com.tencent.smtt.utils;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class DESedeUtils {
   private static String b = "";
   private static byte[] c = null;
   private Cipher d = null;
   private Cipher e = null;
   protected static final char[] mould = "0123456789abcdef".toCharArray();
   private static DESedeUtils instance = null;
   private static String g;

   private DESedeUtils() throws Exception {
      g = (new Random()).nextInt(89999999) + 10000000 + String.valueOf((new Random()).nextInt(89999999) + 10000000) + ((new Random()).nextInt(89999999) + 10000000);
      boolean var1 = false;
      String var2 = "00000000";

      for(int var12 = 0; var12 < 12; ++var12) {
         var2 = var2 + String.valueOf((new Random()).nextInt(89999999) + 10000000);
      }

      var2 = var2 + g;
      c = var2.getBytes();
      this.d = Cipher.getInstance("RSA/ECB/NoPadding");
      StringBuilder var3 = new StringBuilder();
      var3.append(this.d());
      var3.append(this.e());
      byte[] var4 = var3.toString().getBytes();
      X509EncodedKeySpec var5 = new X509EncodedKeySpec(android.util.Base64.decode(var4, 0));
      KeyFactory var6 = KeyFactory.getInstance("RSA");
      PublicKey var7 = var6.generatePublic(var5);
      this.d.init(1, var7);
      byte[] var8 = this.d.doFinal(c);
      b = b(var8);
      DESedeKeySpec var9 = new DESedeKeySpec(g.getBytes());
      SecretKeyFactory var10 = SecretKeyFactory.getInstance("DESede");
      SecretKey var11 = var10.generateSecret(var9);
      this.e = Cipher.getInstance("DESede/ECB/PKCS5Padding");
      this.e.init(1, var11);
   }

   private String d() {
      return "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDcEQ3TCNWPBqgIiY7WQ/IqTOTTV2w8aZ/GPm68FK0";
   }

   private String e() {
      return "fAJBemZKtYR3Li46VJ+Hwnor7ZpQnblGWPFaLv5JoPqvavgB0GInuhm+T+syPs1mw0uPLWaqwvZsCfoaIvUuxy5xHJgmWARrK4/9pHyDxRlZte0PCIoR1ko5B8lVVH1X1dQIDAQAB";
   }

   public static DESedeUtils getInstance() {
      try {
         if (instance == null) {
            instance = new DESedeUtils();
         }

         return instance;
      } catch (Exception var1) {
         instance = null;
         var1.printStackTrace();
         return null;
      }
   }

   public byte[] a(byte[] var1) throws Exception {
      return this.e.doFinal(var1);
   }

   public static String b(byte[] var0) {
      char[] var1 = new char[var0.length * 2];

      for(int var2 = 0; var2 < var0.length; ++var2) {
         int var3 = var0[var2] & 255;
         var1[var2 * 2] = mould[var3 >>> 4];
         var1[var2 * 2 + 1] = mould[var3 & 15];
      }

      return new String(var1);
   }

   public byte[] c(byte[] var1) {
      byte[] var3 = g.getBytes();

      try {
         SecretKeyFactory var2 = SecretKeyFactory.getInstance("DESede");
         DESedeKeySpec var4 = new DESedeKeySpec(var3);
         SecretKey var5 = var2.generateSecret(var4);
         Cipher var6 = Cipher.getInstance("DESede/ECB/PKCS5Padding");
         var6.init(2, var5);
         return var6.doFinal(var1);
      } catch (Exception var7) {
         TbsLog.i(var7);
         return null;
      }
   }

   public String b() {
      return b;
   }

   public static byte[] a(byte[] var0, String var1) throws Exception {
      DESedeKeySpec var2 = new DESedeKeySpec(var1.getBytes());
      SecretKeyFactory var3 = SecretKeyFactory.getInstance("DESede");
      SecretKey var4 = var3.generateSecret(var2);
      Cipher var5 = Cipher.getInstance("DESede/ECB/PKCS5Padding");
      var5.init(1, var4);
      return var5.doFinal(var0);
   }

   public static byte[] b(byte[] var0, String var1) {
      try {
         SecretKeyFactory var2 = SecretKeyFactory.getInstance("DESede");
         DESedeKeySpec var3 = new DESedeKeySpec(var1.getBytes());
         SecretKey var4 = var2.generateSecret(var3);
         Cipher var5 = Cipher.getInstance("DESede/ECB/PKCS5Padding");
         var5.init(2, var4);
         return var5.doFinal(var0);
      } catch (Exception var6) {
         var6.printStackTrace();
         return null;
      }
   }

   public static String c() {
      return g;
   }
}
