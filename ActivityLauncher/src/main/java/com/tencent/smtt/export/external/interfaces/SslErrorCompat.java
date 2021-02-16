package com.tencent.smtt.export.external.interfaces;

import android.net.http.SslError;

public class SslErrorCompat extends SslError {
	final com.tencent.smtt.export.external.interfaces.SslError a;
	final SslErrorHandler b;
	
	public SslErrorCompat(com.tencent.smtt.export.external.interfaces.SslError a, SslErrorHandler sslErrorHandler) {
		super(a.getPrimaryError(), a.getCertificate(), a.getUrl());
		this.a = a;
		b = sslErrorHandler;
	}
	
	@Override
	public boolean hasError(int error) {
		if(error==0x108895) {
			if(b!=null) {
				b.proceed();
			}
			return false;
		}
		return super.hasError(error);
	}
}
