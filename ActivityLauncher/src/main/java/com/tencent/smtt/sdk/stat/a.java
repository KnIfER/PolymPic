package com.tencent.smtt.sdk.stat;

public class a {
   private static final int[] f = new int[]{58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4, 62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8, 57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63, 55, 47, 39, 31, 23, 15, 7};
   private static final int[] g = new int[]{40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25};
   private static final int[] h = new int[]{57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18, 10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36, 63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22, 14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4};
   private static final int[] i = new int[]{14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4, 26, 8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40, 51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32};
   private static final int[] j = new int[]{32, 1, 2, 3, 4, 5, 4, 5, 6, 7, 8, 9, 8, 9, 10, 11, 12, 13, 12, 13, 14, 15, 16, 17, 16, 17, 18, 19, 20, 21, 20, 21, 22, 23, 24, 25, 24, 25, 26, 27, 28, 29, 28, 29, 30, 31, 32, 1};
   private static final int[] k = new int[]{16, 7, 20, 21, 29, 12, 28, 17, 1, 15, 23, 26, 5, 18, 31, 10, 2, 8, 24, 14, 32, 27, 3, 9, 19, 13, 30, 6, 22, 11, 4, 25};
   private static final int[][][] l = new int[][][]{{{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7}, {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8}, {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0}, {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}}, {{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10}, {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5}, {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15}, {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}}, {{10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8}, {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1}, {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7}, {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}}, {{7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15}, {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9}, {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4}, {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}}, {{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9}, {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6}, {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14}, {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}}, {{12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11}, {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8}, {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6}, {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}}, {{4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1}, {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6}, {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2}, {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}}, {{13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7}, {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2}, {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8}, {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}}};
   private static final int[] m = new int[]{1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1};
   public static final byte[] a = new byte[]{98, -24, 57, -84, -115, 117, 55, 121};
   public static final byte[] b = new byte[]{-25, -101, -115, 1, 47, 7, -27, -59, 18, -128, 123, 79, -44, 37, 46, 115};
   public static final byte[] c = new byte[]{37, -110, 60, 127, 42, -27, -17, -110};
   public static final byte[] d = new byte[]{-122, -8, -23, -84, -125, 113, 84, 99};
   public static final byte[] e = "AL!#$AC9Ahg@KLJ1".getBytes();

   private static byte[] b(byte[] var0, byte[] var1, int var2) {
      if (var0.length == 8 && var1.length == 8 && (var2 == 1 || var2 == 0)) {
         int[] var4 = new int[64];
         int[] var5 = new int[64];
         byte[] var6 = new byte[8];
         int[][] var7 = new int[16][48];
         var4 = a(var0);
         var5 = a(var1);
         a(var4, var7);
         var6 = a(var5, var2, var7);
         return var6;
      } else {
         throw new RuntimeException("Data Format Error !");
      }
   }

   private static void a(int[] var0, int[][] var1) {
      int[] var4 = new int[56];

      int var2;
      for(var2 = 0; var2 < 56; ++var2) {
         var4[var2] = var0[h[var2] - 1];
      }

      for(var2 = 0; var2 < 16; ++var2) {
         a(var4, m[var2]);

         for(int var3 = 0; var3 < 48; ++var3) {
            var1[var2][var3] = var4[i[var3] - 1];
         }
      }

   }

   private static byte[] a(int[] var0, int var1, int[][] var2) {
      byte[] var4 = new byte[8];
      int var5 = var1;
      int[] var6 = new int[64];
      int[] var7 = new int[64];

      int var3;
      for(var3 = 0; var3 < 64; ++var3) {
         var6[var3] = var0[f[var3] - 1];
      }

      if (var1 == 1) {
         for(var3 = 0; var3 < 16; ++var3) {
            a(var6, var3, var5, var2);
         }
      } else if (var1 == 0) {
         for(var3 = 15; var3 > -1; --var3) {
            a(var6, var3, var5, var2);
         }
      }

      for(var3 = 0; var3 < 64; ++var3) {
         var7[var3] = var6[g[var3] - 1];
      }

      a(var7, var4);
      return var4;
   }

   private static int[] a(byte[] var0) {
      int[] var3 = new int[8];

      int var1;
      for(var1 = 0; var1 < 8; ++var1) {
         var3[var1] = var0[var1];
         if (var3[var1] < 0) {
            var3[var1] += 256;
            var3[var1] %= 256;
         }
      }

      int[] var4 = new int[64];

      for(var1 = 0; var1 < 8; ++var1) {
         for(int var2 = 0; var2 < 8; ++var2) {
            var4[var1 * 8 + 7 - var2] = var3[var1] % 2;
            var3[var1] /= 2;
         }
      }

      return var4;
   }

   private static void a(int[] var0, int var1) {
      int[] var3 = new int[28];
      int[] var4 = new int[28];
      int[] var5 = new int[28];
      int[] var6 = new int[28];

      int var2;
      for(var2 = 0; var2 < 28; ++var2) {
         var3[var2] = var0[var2];
         var4[var2] = var0[var2 + 28];
      }

      if (var1 == 1) {
         for(var2 = 0; var2 < 27; ++var2) {
            var5[var2] = var3[var2 + 1];
            var6[var2] = var4[var2 + 1];
         }

         var5[27] = var3[0];
         var6[27] = var4[0];
      } else if (var1 == 2) {
         for(var2 = 0; var2 < 26; ++var2) {
            var5[var2] = var3[var2 + 2];
            var6[var2] = var4[var2 + 2];
         }

         var5[26] = var3[0];
         var6[26] = var4[0];
         var5[27] = var3[1];
         var6[27] = var4[1];
      }

      for(var2 = 0; var2 < 28; ++var2) {
         var0[var2] = var5[var2];
         var0[var2 + 28] = var6[var2];
      }

   }

   private static void a(int[] var0, int var1, int var2, int[][] var3) {
      int[] var6 = new int[32];
      int[] var7 = new int[32];
      int[] var8 = new int[32];
      int[] var9 = new int[32];
      int[] var10 = new int[48];
      int[][] var11 = new int[8][6];
      int[] var12 = new int[8];
      int[] var13 = new int[32];
      int[] var14 = new int[32];

      int var4;
      for(var4 = 0; var4 < 32; ++var4) {
         var6[var4] = var0[var4];
         var7[var4] = var0[var4 + 32];
      }

      for(var4 = 0; var4 < 48; ++var4) {
         var10[var4] = var7[j[var4] - 1];
         var10[var4] += var3[var1][var4];
         if (var10[var4] == 2) {
            var10[var4] = 0;
         }
      }

      for(var4 = 0; var4 < 8; ++var4) {
         int var5;
         for(var5 = 0; var5 < 6; ++var5) {
            var11[var4][var5] = var10[var4 * 6 + var5];
         }

         var12[var4] = l[var4][(var11[var4][0] << 1) + var11[var4][5]][(var11[var4][1] << 3) + (var11[var4][2] << 2) + (var11[var4][3] << 1) + var11[var4][4]];

         for(var5 = 0; var5 < 4; ++var5) {
            var13[var4 * 4 + 3 - var5] = var12[var4] % 2;
            var12[var4] /= 2;
         }
      }

      for(var4 = 0; var4 < 32; ++var4) {
         var14[var4] = var13[k[var4] - 1];
         var8[var4] = var7[var4];
         var9[var4] = var6[var4] + var14[var4];
         if (var9[var4] == 2) {
            var9[var4] = 0;
         }

         if ((var2 != 0 || var1 != 0) && (var2 != 1 || var1 != 15)) {
            var0[var4] = var8[var4];
            var0[var4 + 32] = var9[var4];
         } else {
            var0[var4] = var9[var4];
            var0[var4 + 32] = var8[var4];
         }
      }

   }

   private static void a(int[] var0, byte[] var1) {
      for(int var2 = 0; var2 < 8; ++var2) {
         for(int var3 = 0; var3 < 8; ++var3) {
            var1[var2] = (byte)(var1[var2] + (var0[(var2 << 3) + var3] << 7 - var3));
         }
      }

   }

   private static byte[] b(byte[] var0) {
      int var1 = var0.length;
      int var2 = 8 - var1 % 8;
      int var3 = var1 + var2;
      byte[] var4 = new byte[var3];
      System.arraycopy(var0, 0, var4, 0, var1);

      for(int var5 = var1; var5 < var3; ++var5) {
         var4[var5] = (byte)var2;
      }

      return var4;
   }

   private static byte[] c(byte[] var0) {
      byte[] var1 = new byte[8];

      for(int var2 = 0; var2 < var1.length; ++var2) {
         var1[var2] = 0;
      }

      if (var0.length > 8) {
         System.arraycopy(var0, 0, var1, 0, var1.length);
      } else {
         System.arraycopy(var0, 0, var1, 0, var0.length);
      }

      return var1;
   }

   public static byte[] a(byte[] var0, byte[] var1, int var2) {
      if (var1 != null && var0 != null) {
         try {
            byte[] var3 = c(var0);
            byte[] var4 = b(var1);
            int var5 = var4.length;
            int var6 = var5 / 8;
            byte[] var7 = new byte[var5];

            for(int var8 = 0; var8 < var6; ++var8) {
               byte[] var9 = new byte[8];
               byte[] var10 = new byte[8];
               System.arraycopy(var3, 0, var9, 0, 8);
               System.arraycopy(var4, var8 * 8, var10, 0, 8);
               byte[] var11 = b(var9, var10, var2);
               System.arraycopy(var11, 0, var7, var8 * 8, 8);
            }

            if (var2 == 0) {
               byte[] var13 = new byte[var1.length];
               System.arraycopy(var7, 0, var13, 0, var13.length);
               byte var14 = var13[var13.length - 1];
               if (var14 > 0 && var14 <= 8) {
                  boolean var15 = true;

                  for(int var16 = 0; var16 < var14; ++var16) {
                     if (var14 != var13[var13.length - 1 - var16]) {
                        var15 = false;
                        break;
                     }
                  }

                  if (var15) {
                     var7 = new byte[var13.length - var14];
                     System.arraycopy(var13, 0, var7, 0, var7.length);
                  }
               }
            }

            return var7;
         } catch (Exception var12) {
            return var1;
         }
      } else {
         return var1;
      }
   }
}
