package com.knziha.polymer.toolkits;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
public class MyX509TrustManager implements X509TrustManager {
 
    @Override
    public void checkClientTrusted(X509Certificate certificates[],String authType) throws CertificateException {
    }
 
    @Override
    public void checkServerTrusted(X509Certificate[] ax509certificate,String s) throws CertificateException {
    }
 
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[] {};
		//return null;
    }
}