package com.tencent.smtt.export.external.interfaces;

import com.knziha.polymer.browser.webkit.WebResourceResponseCompat;
import com.knziha.polymer.widgets.Utils;

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

   public WebResourceResponse(String mMimeType, String mEncoding, InputStream Data) {
      this.mMimeType = mMimeType;
      this.mEncoding = mEncoding;
      this.setData(Data);
   }

   public WebResourceResponse(String mMimeType, String mEncoding, int StatusCode, String ReasonPhrase, Map<String, String> ResponseHeaders, InputStream Data) {
      this(mMimeType, mEncoding, Data);
      this.setStatusCodeAndReasonPhrase(StatusCode, ReasonPhrase);
      this.setResponseHeaders(ResponseHeaders);
   }
   
	public static WebResourceResponse new_WebResourceResponse(android.webkit.WebResourceResponse ret) {
		return ret==null?null:new WebResourceResponse(ret);
	}
	
	public WebResourceResponse(android.webkit.WebResourceResponse ret) {
		this(ret.getMimeType(), ret.getEncoding(), ret.getData());
		if(Utils.bigCake) {
			setResponseHeaders(ret.getResponseHeaders());
		} else if(ret instanceof WebResourceResponseCompat) {
			setResponseHeaders(((WebResourceResponseCompat) ret).getResponseHeaders());
		}
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
