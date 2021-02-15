package com.tencent.smtt.sdk;

import java.util.Map;

public class WebStorage {
   private static WebStorage a;

   public void getOrigins(ValueCallback<Map> var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         var2.getWVWizardBase().webStorageGetOrigins((android.webkit.ValueCallback)var1);
      } else {
         android.webkit.WebStorage.getInstance().getOrigins(var1);
      }

   }

   public void getUsageForOrigin(String var1, ValueCallback<Long> var2) {
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      if (null != var3 && var3.isInCharge()) {
         var3.getWVWizardBase().webStorageGetUsageForOrigin((String)var1, (android.webkit.ValueCallback)var2);
      } else {
         android.webkit.WebStorage.getInstance().getUsageForOrigin(var1, var2);
      }

   }

   public void getQuotaForOrigin(String var1, ValueCallback<Long> var2) {
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      if (null != var3 && var3.isInCharge()) {
         var3.getWVWizardBase().webStorageGetQuotaForOrigin(var1, var2);
      } else {
         android.webkit.WebStorage.getInstance().getQuotaForOrigin(var1, var2);
      }

   }

   /** @deprecated */
   @Deprecated
   public void setQuotaForOrigin(String var1, long var2) {
      X5CoreEngine var4 = X5CoreEngine.getInstance();
      if (null != var4 && var4.isInCharge()) {
         var4.getWVWizardBase().webStorageSetQuotaForOrigin(var1, var2);
      } else {
         android.webkit.WebStorage.getInstance().setQuotaForOrigin(var1, var2);
      }

   }

   public void deleteOrigin(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         var2.getWVWizardBase().webStorageDeleteOrigin(var1);
      } else {
         android.webkit.WebStorage.getInstance().deleteOrigin(var1);
      }

   }

   public void deleteAllData() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         var1.getWVWizardBase().webStorageDeleteAllData();
      } else {
         android.webkit.WebStorage.getInstance().deleteAllData();
      }

   }

   public static WebStorage getInstance() {
      return a();
   }

   private static synchronized WebStorage a() {
      if (a == null) {
         a = new WebStorage();
      }

      return a;
   }

   /** @deprecated */
   @Deprecated
   public interface QuotaUpdater extends android.webkit.WebStorage.QuotaUpdater{
      void updateQuota(long var1);
   }
}
