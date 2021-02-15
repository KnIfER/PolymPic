package com.tencent.smtt.sdk;

public class MimeTypeMap {
   private static MimeTypeMap a;

   private MimeTypeMap() {
   }

   public static String getFileExtensionFromUrl(String var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      return null != var1 && var1.isInCharge() ? var1.getWVWizardBase().mimeTypeMapGetFileExtensionFromUrl(var0) : android.webkit.MimeTypeMap.getFileExtensionFromUrl(var0);
   }

   public boolean hasMimeType(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      return null != var2 && var2.isInCharge() ? var2.getWVWizardBase().mimeTypeMapHasMimeType(var1) : android.webkit.MimeTypeMap.getSingleton().hasMimeType(var1);
   }

   public String getMimeTypeFromExtension(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      return null != var2 && var2.isInCharge() ? var2.getWVWizardBase().mimeTypeMapGetMimeTypeFromExtension(var1) : android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(var1);
   }

   public boolean hasExtension(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      return null != var2 && var2.isInCharge() ? var2.getWVWizardBase().mimeTypeMapHasExtension(var1) : android.webkit.MimeTypeMap.getSingleton().hasExtension(var1);
   }

   public String getExtensionFromMimeType(String var1) {
      X5CoreEngine var2 = X5CoreEngine.getInstance();
      return null != var2 && var2.isInCharge() ? var2.getWVWizardBase().l(var1) : android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(var1);
   }

   public static synchronized MimeTypeMap getSingleton() {
      if (a == null) {
         a = new MimeTypeMap();
      }

      return a;
   }
}
