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
		Map<String, String> headers = request.getRequestHeaders();
		String url=request.getUrl().toString();
		//CMN.Log("SIR::", url);
		//CMN.Log("SIR::", headers);
		String acc = headers.get("Accept");
		//CMN.Log("SIR::Accept_", acc, acc.equals("*/*"));
		if(acc==null) {
			acc = "*/*;";
		}
		
		boolean lishanxizhe = false;
		
		//acc.equals("*/*");
		
		String host = null;
		
		if(true) {
			String addr = jinkeSheaths.get(SubStringKey.new_hostKey(url));
			if(addr!=null) {
				try {
					URL oldUrl = new URL(url);
					host = oldUrl.getHost();
					url = url.replaceFirst(oldUrl.getHost(), addr);
					CMN.Log("秦王绕柱走", url);
					lishanxizhe = true;
				} catch (Exception e) {
					CMN.Log(e);
				}
			}
		}
		
		if(lishanxizhe /*&& !(url.endsWith("google.com/")||url.endsWith("google.cn/"))*/) {//
			try {
				headers.put("Access-Control-Allow-Origin", "*");
				headers.put("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
				InputStream input = null;
				String MIME = null;
				CMN.rt("转构开始……");
				if(false) {
					OkHttpClient klient = (OkHttpClient) k3client;
					if(klient==null) {
						int cacheSize = 10 * 1024 * 1024;
						Interceptor headerInterceptor = new Interceptor() {
							@Override
							public Response intercept(Chain chain) throws IOException {
								Request request = chain.request();
								Response response = chain.proceed(request);
								Response response1 = response.newBuilder()
										.removeHeader("Pragma")
										.removeHeader("Cache-Control")
										//cache for 30 days
										.header("Cache-Control", "max-age=" + 3600 * 24 * 30)
										.build();
								return response1;
							}
						};
						klient = new OkHttpClient.Builder()
								.connectTimeout(5, TimeUnit.SECONDS)
								.addNetworkInterceptor(headerInterceptor)
								.cache(true?
										new Cache(new File(a.getExternalCacheDir(), "k3cache")
												, cacheSize) :null ) // 配置缓存
								//.readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
								//.setCache(getCache())
								//.certificatePinner(getPinnedCerts())
								//.setSslSocketFactory(getSSL())
								.hostnameVerifier(DO_NOT_VERIFY)
								.build()
						;
						k3client = klient;
					}
					Request.Builder k3request = new Request.Builder()
							.url(url)
							.header("Accept-Charset", "utf-8")
							.header("Access-Control-Allow-Origin", "*")
							.header("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept")
							;
					for(String kI:headers.keySet()) {
						k3request.header(kI, headers.get(kI));
					}
					//int maxSale = 60 * 60 * 24 * 28; // tolerate 4-weeks sale
//					if (!NetworkUtils.isConnected(a))
//					k3request.removeHeader("Pragma")
//							.cacheControl(new CacheControl.Builder()
//									.maxAge(0, TimeUnit.SECONDS)
//									.maxStale(365,TimeUnit.DAYS).build())
//							.header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale);
					if(host!=null) k3request.header("Host", host);
					k3request.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36");
					Response k3response = klient.newCall(k3request.build()).execute();
					input = k3response.body().byteStream();
					MIME = k3response.header("content-type");
				}
				else {
					HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
					//urlConnection.setRequestProperty("contentType", headers.get("contentType"));
					//urlConnection.setRequestProperty("Accept", headers.get("Accept"));
					if(false) {
						File httpCacheDir = new File(a.getExternalCacheDir(), "k1cache");
						int cacheSize = 10 * 1024 * 1024;
						try {
							HttpResponseCache.install(httpCacheDir, cacheSize);
						} catch (IOException e) {
							CMN.Log(e);
						}
					}
					
					if(urlConnection instanceof HttpsURLConnection) {
						((HttpsURLConnection)urlConnection).setHostnameVerifier(DO_NOT_VERIFY);
					}
					urlConnection.setRequestProperty("Accept-Charset", "utf-8");
					urlConnection.setRequestProperty("connection", "Keep-Alive");
					urlConnection.setRequestMethod(request.getMethod());
					urlConnection.setConnectTimeout(5000);
					urlConnection.setUseCaches(true);
					urlConnection.setDefaultUseCaches(true);
					for(String kI:headers.keySet()) {
						urlConnection.setRequestProperty(kI, headers.get(kI));
					}
					if(host!=null) urlConnection.setRequestProperty("Host", host);
					urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36");
					//int maxSale = 60 * 60 * 24 * 28; // tolerate 4-weeks sale
					//if(NetworkUtils.isConnected(a))
					//urlConnection.setRequestProperty("Cache-Control", "max-age=" + maxSale);
					//else
					//urlConnection.setRequestProperty("Cache-Control", "public, only-if-cached, max-stale=" + maxSale);
					
					//urlConnection.setRequestProperty("User-Agent", a.android_ua);
					//urlConnection.setRequestProperty("Host", "translate.google.cn");
					//urlConnection.setRequestProperty("Origin", "https://translate.google.cn");
					urlConnection.connect();
					input = urlConnection.getInputStream();
					input = new AutoCloseNetStream(input, urlConnection);
					MIME = urlConnection.getHeaderField("content-type");
				}
				CMN.pt("转构完毕！！！", input.available(), MIME);
				if(TextUtils.isEmpty(MIME)) {
					MIME = acc;
				}
				int idx = MIME.indexOf(",");
				if(idx<0) {
					idx = MIME.indexOf(";");
				}
				if(idx>=0) {
					MIME = MIME.substring(0, idx);
				}
				WebResourceResponse webResourceResponse=new WebResourceResponse(MIME, "utf8", input);
				webResourceResponse.setResponseHeaders(headers);
				//CMN.Log("百代春秋泽被万世");
				return webResourceResponse;
			} catch (IOException e) {
				CMN.Log(e);
				//return null;
			}
		}
		
		return super.shouldInterceptRequest(view, request);
	}
}
