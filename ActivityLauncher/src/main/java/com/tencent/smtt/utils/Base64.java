package com.tencent.smtt.utils;

import java.io.UnsupportedEncodingException;

public class Base64 {
   public static String encodeToString(byte[] var0, int var1) {
      try {
         return new String(a(var0, var1), "US-ASCII");
      } catch (UnsupportedEncodingException var3) {
         throw new AssertionError(var3);
      }
   }

   public static byte[] a(byte[] var0, int var1) {
      return a(var0, 0, var0.length, var1);
   }

   public static byte[] a(byte[] var0, int var1, int var2, int var3) {
      Base64.b var4 = new Base64.b(var3, (byte[])null);
      int var5 = var2 / 3 * 4;
      if (var4.d) {
         if (var2 % 3 > 0) {
            var5 += 4;
         }
      } else {
         switch(var2 % 3) {
         case 0:
         default:
            break;
         case 1:
            var5 += 2;
            break;
         case 2:
            var5 += 3;
         }
      }

      if (var4.e && var2 > 0) {
         var5 += ((var2 - 1) / 57 + 1) * (var4.f ? 2 : 1);
      }

      var4.a = new byte[var5];
      var4.a(var0, var1, var2, true);

      assert var4.b == var5;

      return var4.a;
   }

   private Base64() {
   }

   static class b extends Base64.a {
      private static final byte[] h = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47};
      private static final byte[] i = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 45, 95};
      private final byte[] j;
      int c;
      private int k;
      public final boolean d;
      public final boolean e;
      public final boolean f;
      private final byte[] l;

      public b(int var1, byte[] var2) {
         this.a = var2;
         this.d = (var1 & 1) == 0;
         this.e = (var1 & 2) == 0;
         this.f = (var1 & 4) != 0;
         this.l = (var1 & 8) == 0 ? h : i;
         this.j = new byte[2];
         this.c = 0;
         this.k = this.e ? 19 : -1;
      }

      public boolean a(byte[] var1, int var2, int var3, boolean var4) {
         byte[] var5 = this.l;
         byte[] var6 = this.a;
         int var7 = 0;
         int var8 = this.k;
         int var9 = var2;
         var3 += var2;
         int var10 = -1;
         int var10000;
         switch(this.c) {
         case 0:
         default:
            break;
         case 1:
            if (var2 + 2 <= var3) {
               var10000 = (this.j[0] & 255) << 16;
               var9 = var2 + 1;
               var10 = var10000 | (var1[var2] & 255) << 8 | var1[var9++] & 255;
               this.c = 0;
            }
            break;
         case 2:
            if (var2 + 1 <= var3) {
               var10000 = (this.j[0] & 255) << 16 | (this.j[1] & 255) << 8;
               var9 = var2 + 1;
               var10 = var10000 | var1[var2] & 255;
               this.c = 0;
            }
         }

         if (var10 != -1) {
            var6[var7++] = var5[var10 >> 18 & 63];
            var6[var7++] = var5[var10 >> 12 & 63];
            var6[var7++] = var5[var10 >> 6 & 63];
            var6[var7++] = var5[var10 & 63];
            --var8;
            if (var8 == 0) {
               if (this.f) {
                  var6[var7++] = 13;
               }

               var6[var7++] = 10;
               var8 = 19;
            }
         }

         while(var9 + 3 <= var3) {
            var10 = (var1[var9] & 255) << 16 | (var1[var9 + 1] & 255) << 8 | var1[var9 + 2] & 255;
            var6[var7] = var5[var10 >> 18 & 63];
            var6[var7 + 1] = var5[var10 >> 12 & 63];
            var6[var7 + 2] = var5[var10 >> 6 & 63];
            var6[var7 + 3] = var5[var10 & 63];
            var9 += 3;
            var7 += 4;
            --var8;
            if (var8 == 0) {
               if (this.f) {
                  var6[var7++] = 13;
               }

               var6[var7++] = 10;
               var8 = 19;
            }
         }

         if (var4) {
            int var11;
            if (var9 - this.c == var3 - 1) {
               var11 = 0;
               var10 = ((this.c > 0 ? this.j[var11++] : var1[var9++]) & 255) << 4;
               this.c -= var11;
               var6[var7++] = var5[var10 >> 6 & 63];
               var6[var7++] = var5[var10 & 63];
               if (this.d) {
                  var6[var7++] = 61;
                  var6[var7++] = 61;
               }

               if (this.e) {
                  if (this.f) {
                     var6[var7++] = 13;
                  }

                  var6[var7++] = 10;
               }
            } else if (var9 - this.c == var3 - 2) {
               var11 = 0;
               var10 = ((this.c > 1 ? this.j[var11++] : var1[var9++]) & 255) << 10 | ((this.c > 0 ? this.j[var11++] : var1[var9++]) & 255) << 2;
               this.c -= var11;
               var6[var7++] = var5[var10 >> 12 & 63];
               var6[var7++] = var5[var10 >> 6 & 63];
               var6[var7++] = var5[var10 & 63];
               if (this.d) {
                  var6[var7++] = 61;
               }

               if (this.e) {
                  if (this.f) {
                     var6[var7++] = 13;
                  }

                  var6[var7++] = 10;
               }
            } else if (this.e && var7 > 0 && var8 != 19) {
               if (this.f) {
                  var6[var7++] = 13;
               }

               var6[var7++] = 10;
            }

            assert this.c == 0;

            assert var9 == var3;
         } else if (var9 == var3 - 1) {
            this.j[this.c++] = var1[var9];
         } else if (var9 == var3 - 2) {
            this.j[this.c++] = var1[var9];
            this.j[this.c++] = var1[var9 + 1];
         }

         this.b = var7;
         this.k = var8;
         return true;
      }
   }

   abstract static class a {
      public byte[] a;
      public int b;
   }
}
