package com.KnaIvER.polymer.browser;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.KnaIvER.polymer.R;
import com.KnaIvER.polymer.SimpleService;
import com.KnaIvER.polymer.Utils.CMN;
import com.KnaIvER.polymer.Utils.Options;


public class BrowseActivity extends Activity {
	WebView webview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_main);
		webview = findViewById(R.id.webview);

		final WebSettings settings = webview.getSettings();
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setDefaultTextEncodingName("UTF-8");

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
		//settings.setSupportZoom(support);

		settings.setAllowUniversalAccessFromFileURLs(true);

		WebView.setWebContentsDebuggingEnabled(true);

		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				view.loadUrl(url);
				return true;
			}
		});

		//webview.loadUrl("http://192.168.1.102:8080/base/4/raw/niu");
		webview.loadUrl("http://192.168.1.104:8080/base/0/");
		//webview.loadUrl("http://www.baidu.com");
	}
}
