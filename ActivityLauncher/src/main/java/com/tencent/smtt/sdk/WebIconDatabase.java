package com.tencent.smtt.sdk;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import com.tencent.smtt.export.external.interfaces.IconListener;

/** @deprecated */
@Deprecated
public class WebIconDatabase {
   private static WebIconDatabase a;

   public void open(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         var2.getWVWizardBase().openIconDB(var1);
      } else {
         android.webkit.WebIconDatabase.getInstance().open(var1);
      }

   }

   public void close() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         var1.getWVWizardBase().closeIconDB();
      } else {
         android.webkit.WebIconDatabase.getInstance().close();
      }

   }

   public void removeAllIcons() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         var1.getWVWizardBase().removeAllIcons();
      } else {
         android.webkit.WebIconDatabase.getInstance().removeAllIcons();
      }

   }

   public void requestIconForPageUrl(String var1, final WebIconDatabase.a var2) {
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      if (null != var3 && var3.isInCharge()) {
         var3.getWVWizardBase().requestIconForPageUrl(var1, new IconListener() {
            public void onReceivedIcon(String var1, Bitmap var2x) {
               var2.a(var1, var2x);
            }
         });
      } else {
         android.webkit.WebIconDatabase.getInstance().requestIconForPageUrl(var1, new android.webkit.WebIconDatabase.IconListener() {
            public void onReceivedIcon(String var1, Bitmap var2x) {
               var2.a(var1, var2x);
            }
         });
      }

   }

   public void bulkRequestIconForPageUrl(ContentResolver var1, String var2, WebIconDatabase.a var3) {
   }

   public void retainIconForPageUrl(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         var2.getWVWizardBase().retainIconForPageUrl(var1);
      } else {
         android.webkit.WebIconDatabase.getInstance().retainIconForPageUrl(var1);
      }

   }

   public void releaseIconForPageUrl(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         var2.getWVWizardBase().releaseIconForPageUrl(var1);
      } else {
         android.webkit.WebIconDatabase.getInstance().releaseIconForPageUrl(var1);
      }

   }

   public static WebIconDatabase getInstance() {
      return a();
   }

   private static synchronized WebIconDatabase a() {
      if (a == null) {
         a = new WebIconDatabase();
      }

      return a;
   }

   private WebIconDatabase() {
   }

   /** @deprecated */
   @Deprecated
   public interface a {
      void a(String var1, Bitmap var2);
   }
}
