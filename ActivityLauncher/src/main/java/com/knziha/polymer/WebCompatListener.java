package com.knziha.polymer;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.knziha.polymer.Utils.AutoCloseNetStream;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.toolkits.MyX509TrustManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import static com.knziha.polymer.HttpRequestUtil.DO_NOT_VERIFY;


public class WebCompatListener extends WebCompoundListener{
	public WebCompatListener(BrowserActivity activity) {
		super(activity);
	}
	
	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
		try {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, new TrustManager[]{new MyX509TrustManager()}, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
		} catch (Exception e) {
			CMN.Log(e);
		}
		
		Map<String, String> headers = request.getRequestHeaders();
		String url=request.getUrl().toString();
		CMN.Log("SIR::", url, url.startsWith("http"));
		CMN.Log("SIR::", headers);
		String acc = headers.get("Accept");
		CMN.Log("SIR::Accept_", acc, acc.equals("*/*"));
		
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
				CMN.Log("转构开始……");
				HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
//				urlConnection.setRequestProperty("contentType", headers.get("contentType"));
//				urlConnection.setRequestProperty("Accept", headers.get("Accept"));
				if(urlConnection instanceof HttpsURLConnection) {
					((HttpsURLConnection)urlConnection).setHostnameVerifier(DO_NOT_VERIFY);
				}
				urlConnection.setRequestProperty("Accept-Charset", "utf-8");
				urlConnection.setRequestProperty("connection", "Keep-Alive");
				urlConnection.setRequestMethod(request.getMethod());
				urlConnection.setConnectTimeout(5000);
				headers.put("Access-Control-Allow-Origin", "*");
				headers.put("Access-Control-Allow-Headers","Origin, X-Requested-With, Content-Type, Accept");
				for(String kI:headers.keySet()) {
					urlConnection.setRequestProperty(kI, headers.get(kI));
				}
				if(host!=null) urlConnection.setRequestProperty("Host", host);
				urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Safari/537.36");
				//urlConnection.setRequestProperty("User-Agent", a.android_ua);
//				urlConnection.setRequestProperty("Host", "translate.google.cn");
//				urlConnection.setRequestProperty("Origin", "https://translate.google.cn");
				
				urlConnection.connect();
				InputStream input = urlConnection.getInputStream();
				input = new AutoCloseNetStream(input, urlConnection);
				String MIME = urlConnection.getHeaderField("content-type");
				CMN.Log("转构完毕！！！", input.available(), MIME);
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
