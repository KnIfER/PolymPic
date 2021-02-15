package com.tencent.smtt.export.external.interfaces;

import java.io.InputStream;
import java.util.Map;

public class WebResourceResponse {
   private String mMimeType;
   private String mEncoding;
   private int mStatusCode;
   private String mReasonPhrase;
   private Map<String, String> mResponseHeaders;
   private InputStream mInputStream;

   public WebResourceResponse() {
   }

   public WebResourceResponse(String var1, String var2, InputStream var3) {
      this.mMimeType = var1;
      this.mEncoding = var2;
      this.setData(var3);
   }

   public WebResourceResponse(String var1, String var2, int var3, String var4, Map<String, String> var5, InputStream var6) {
      this(var1, var2, var6);
      this.setStatusCodeAndReasonPhrase(var3, var4);
      this.setResponseHeaders(var5);
   }
   
	public static WebResourceResponse new_WebResourceResponse(android.webkit.WebResourceResponse ret) {
		return ret==null?null:new WebResourceResponse(ret);
	}
	
	public WebResourceResponse(android.webkit.WebResourceResponse ret) {
		this(ret.getMimeType(), ret.getEncoding(), ret.getData());
	}
	
	public void setMimeType(String var1) {
      this.mMimeType = var1;
   }

   public String getMimeType() {
      return this.mMimeType;
   }

   public void setEncoding(String var1) {
      this.mEncoding = var1;
   }

   public String getEncoding() {
      return this.mEncoding;
   }

   public void setStatusCodeAndReasonPhrase(int var1, String var2) {
      this.mStatusCode = var1;
      this.mReasonPhrase = var2;
   }

   public int getStatusCode() {
      return this.mStatusCode;
   }

   public String getReasonPhrase() {
      return this.mReasonPhrase;
   }

   public void setResponseHeaders(Map<String, String> var1) {
      this.mResponseHeaders = var1;
   }

   public Map<String, String> getResponseHeaders() {
      return this.mResponseHeaders;
   }

   public void setData(InputStream var1) {
      this.mInputStream = var1;
   }

   public InputStream getData() {
      return this.mInputStream;
   }
}
