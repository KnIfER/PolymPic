package com.tencent.smtt.export.external;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;

public class DexClassLoaderProviderService extends Service {
   private static final String LOGTAG = "dexloader";

   public IBinder onBind(Intent var1) {
      return null;
   }

   public void onCreate() {
      super.onCreate();
      Log.d("dexloader", "DexClassLoaderProviderService -- onCreate()");
      DexClassLoaderProvider.setForceLoadDexFlag(true, this);
   }

   public int onStartCommand(Intent var1, int var2, int var3) {
      Log.d("dexloader", "DexClassLoaderProviderService -- onStartCommand(" + var1 + ")");

      try {
         if (var1 == null) {
            return 1;
         }

         ArrayList var4 = var1.getStringArrayListExtra("dex2oat");
         if (var4 == null) {
            return 1;
         }

         String var5 = (String)var4.get(0);
         String var6 = (String)var4.get(1);
         String var7 = (String)var4.get(2);
         String var8 = (String)var4.get(3);
         Log.d("dexloader", "DexClassLoaderProviderService -- onStartCommand(" + var5 + ")");
         ClassLoader var9 = this.getClassLoader();
         File var10 = new File(var7);
         if (!var10.exists()) {
            var10.mkdirs();
         }

         DexClassLoaderProvider.createDexClassLoader(var6, var7, var8, var9, this.getApplicationContext());
      } catch (Exception var11) {
         var11.printStackTrace();
      }

      return 1;
   }

   public void onDestroy() {
      Log.d("dexloader", "DexClassLoaderProviderService -- onDestroy()");
      System.exit(0);
   }
}
