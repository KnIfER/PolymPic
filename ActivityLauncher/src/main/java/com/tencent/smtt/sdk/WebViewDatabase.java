package com.tencent.smtt.sdk;

import android.content.Context;

public class WebViewDatabase {
   private static WebViewDatabase a;
   private Context b;

   protected WebViewDatabase(Context var1) {
      this.b = var1;
   }

   public static WebViewDatabase getInstance(Context var0) {
      return a(var0);
   }

   private static synchronized WebViewDatabase a(Context var0) {
      if (a == null) {
         a = new WebViewDatabase(var0);
      }

      return a;
   }

   /** @deprecated */
   @Deprecated
   public boolean hasUsernamePassword() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().webViewDatabaseHasUsernamePassword(this.b) : android.webkit.WebViewDatabase.getInstance(this.b).hasUsernamePassword();
   }

   /** @deprecated */
   @Deprecated
   public void clearUsernamePassword() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         var1.getWVWizardBase().webViewDatabaseClearUsernamePassword(this.b);
      } else {
         android.webkit.WebViewDatabase.getInstance(this.b).clearUsernamePassword();
      }

   }

   public boolean hasHttpAuthUsernamePassword() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().webViewDatabaseHasHttpAuthUsernamePassword(this.b) : android.webkit.WebViewDatabase.getInstance(this.b).hasHttpAuthUsernamePassword();
   }

   public void clearHttpAuthUsernamePassword() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         var1.getWVWizardBase().webViewDatabaseClearHttpAuthUsernamePassword(this.b);
      } else {
         android.webkit.WebViewDatabase.getInstance(this.b).clearHttpAuthUsernamePassword();
      }

   }

   public boolean hasFormData() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().webViewDatabaseHasFormData(this.b) : android.webkit.WebViewDatabase.getInstance(this.b).hasFormData();
   }

   public void clearFormData() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         var1.getWVWizardBase().webViewDatabaseClearFormData(this.b);
      } else {
         android.webkit.WebViewDatabase.getInstance(this.b).clearFormData();
      }

   }
}
