package com.knziha.polymer;

import android.net.http.HttpResponseCache;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.knziha.polymer.Utils.AutoCloseNetStream;
import com.knziha.polymer.Utils.CMN;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.knziha.polymer.HttpRequestUtil.DO_NOT_VERIFY;


public class WebCompatListener extends WebCompoundListener{
	public WebCompatListener(BrowserActivity activity) {
		super(activity);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
		return shouldInterceptRequest(view, request.getUrl().toString(), request.getMethod(), request.getRequestHeaders());
	}
}
