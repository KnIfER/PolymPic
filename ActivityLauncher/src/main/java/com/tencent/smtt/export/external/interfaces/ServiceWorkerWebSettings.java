package com.tencent.smtt.export.external.interfaces;

public abstract class ServiceWorkerWebSettings {
   public abstract void setCacheMode(int var1);

   public abstract int getCacheMode();

   public abstract void setAllowContentAccess(boolean var1);

   public abstract boolean getAllowContentAccess();

   public abstract void setAllowFileAccess(boolean var1);

   public abstract boolean getAllowFileAccess();

   public abstract void setBlockNetworkLoads(boolean var1);

   public abstract boolean getBlockNetworkLoads();
}
