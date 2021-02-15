package com.tencent.smtt.export.external.interfaces;

import android.net.http.SslCertificate;

public interface SslError {
   SslCertificate getCertificate();

   boolean addError(int var1);

   boolean hasError(int var1);

   int getPrimaryError();

   String getUrl();
}
