package com.knziha.polymer.browser.benchmarks;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebViewImplExt;
import com.knziha.polymer.widgets.WebFrameLayout;

public class V8Benchmark extends Activity {
	UniversalWebviewInterface wv;
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WebFrameLayout layout = new WebFrameLayout(this, new BrowserActivity.TabHolder());
		setContentView(layout);
		wv=newImplWebView();
		wv.setLayoutParent(layout, true);
		wv.setWebChromeClient(new WebChromeClient(){
			
		});
		wv.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return false;
			}
		});
		final WebSettings settings = wv.getSettings();
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setDefaultTextEncodingName("UTF-8");
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
//		}
		
		settings.setNeedInitialFocus(false);
		//settings.setDefaultFontSize(40);
		//settings.setTextZoom(100);
		//setInitialScale(25);
		
		settings.setJavaScriptEnabled(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setMediaPlaybackRequiresUserGesture(false);
		
		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
		
		//settings.setUseWideViewPort(true);//设定支持viewport
		//settings.setLoadWithOverviewMode(true);
		
		settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		WebView.setWebContentsDebuggingEnabled(false);
		
		CMN.Log("UA::", settings.getUserAgentString());
		
		wv.loadUrl("http://chrome.360.cn/test/v8/run.html");
	}
	
	protected UniversalWebviewInterface newImplWebView() {
		return new WebViewImplExt(this);
	}
}
