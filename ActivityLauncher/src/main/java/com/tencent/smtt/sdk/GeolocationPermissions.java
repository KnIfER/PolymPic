package com.tencent.smtt.sdk;

import java.util.Set;

public class GeolocationPermissions {
   private static GeolocationPermissions a;

   public static GeolocationPermissions getInstance() {
      return a();
   }

   private static synchronized GeolocationPermissions a() {
      if (a == null) {
         a = new GeolocationPermissions();
      }

      return a;
   }

   public void getOrigins(ValueCallback<Set<String>> var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         var2.getWVWizardBase().geolocationPermissionsGetOrigins((android.webkit.ValueCallback)var1);
      } else {
         android.webkit.GeolocationPermissions.getInstance().getOrigins(var1);
      }

   }

   public void getAllowed(String var1, ValueCallback<Boolean> var2) {
      X5CoreEngine var3 = X5CoreEngine.getInstance();
      if (null != var3 && var3.isInCharge()) {
         var3.getWVWizardBase().geolocationPermissionsGetAllowed(var1, var2);
      } else {
         android.webkit.GeolocationPermissions.getInstance().getAllowed(var1, var2);
      }

   }

   public void clear(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         var2.getWVWizardBase().geolocationPermissionsClear(var1);
      } else {
         android.webkit.GeolocationPermissions.getInstance().clear(var1);
      }

   }

   public void allow(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      if (null != var2 && var2.isInCharge()) {
         var2.getWVWizardBase().geolocationPermissionsAllow(var1);
      } else {
         android.webkit.GeolocationPermissions.getInstance().allow(var1);
      }

   }

   public void clearAll() {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      if (null != var1 && var1.isInCharge()) {
         var1.getWVWizardBase().geolocationPermissionsClearAll();
      } else {
         android.webkit.GeolocationPermissions.getInstance().clearAll();
      }

   }
}
