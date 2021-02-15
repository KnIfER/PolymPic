package com.tencent.smtt.export.external.interfaces;

import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public abstract class ClientCertRequest {
   public abstract String[] getKeyTypes();

   public abstract Principal[] getPrincipals();

   public abstract String getHost();

   public abstract int getPort();

   public abstract void proceed(PrivateKey var1, X509Certificate[] var2);

   public abstract void ignore();

   public abstract void cancel();
}
