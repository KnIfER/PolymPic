package com.knziha.polymer;

import android.os.Build;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;


public class WebCompatListener extends WebCompoundListener{
	public WebCompatListener(BrowserActivity activity) {
		super(activity);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
		return shouldInterceptRequest(view, request.getUrl().toString(), request.getMethod(), request.getRequestHeaders());
	}

	public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
	}

}
