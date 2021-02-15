package com.tencent.smtt.sdk;

import android.content.Context;
import android.os.Build.VERSION;
import com.tencent.smtt.export.external.interfaces.IX5CoreServiceWorkerController;
import com.tencent.smtt.export.external.interfaces.ServiceWorkerClient;
import com.tencent.smtt.export.external.interfaces.ServiceWorkerWebSettings;

public abstract class ServiceWorkerController {
   public static ServiceWorkerController getInstance(Context var0) {
      X5CoreEngine var1 = X5CoreEngine.getInstance();
      var1.init(var0);
      if (var1.isInCharge()) {
         final IX5CoreServiceWorkerController var2 = X5CoreEngine.getInstance().getWVWizardBase().getServiceWorkerController();
         return var2 != null ? new ServiceWorkerController() {
            public ServiceWorkerWebSettings getServiceWorkerWebSettings() {
               return var2.getServiceWorkerWebSettings();
            }

            public void setServiceWorkerClient(ServiceWorkerClient var1) {
               var2.setServiceWorkerClient(var1);
            }
         } : null;
      } else {
         return VERSION.SDK_INT >= 24 ? new ServiceWorkerControllerAdapter() : null;
      }
   }

   public abstract ServiceWorkerWebSettings getServiceWorkerWebSettings();

   public abstract void setServiceWorkerClient(ServiceWorkerClient var1);
}
