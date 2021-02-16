package com.knziha.polymer.browser.webkit;

import android.os.Bundle;
import android.print.PrintDocumentAdapter;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.WebCompoundListener;
import com.knziha.polymer.widgets.WebFrameLayout;

import java.util.Map;

import static com.knziha.polymer.Utils.CMN.dummyWV;

public interface UniversalWebviewInterface {
	static WebView getLockedView(UniversalWebviewInterface impl) {
		if(impl instanceof WebView) {
			return (WebView) impl;
		}
		CMN.lock.lock();
		dummyWV.setTag(impl);
		return dummyWV;
	}
	
	public void loadUrl(String url);
	
	public void clearHistory();
	
	public void clearView();
	
	public void stopLoading();
	
	void setWebChromeClient(WebChromeClient mWebClient);
	
	void setWebViewClient(WebViewClient webBrowseListener);
	
	void addJavascriptInterface(Object jsBridge, String name);
	
	WebSettings getSettings();
	
	String getUrl();
	
	String getTitle();
	
	void evaluateJavascript(String jsText, ValueCallback<String> valueCallback);
	
	void setLayoutParent(WebFrameLayout layout, boolean addView);
	
	void destroy();
	
	void resumeTimers();
	
	void onResume();
	
	WebBackForwardList restoreState(Bundle bundle);
	
	WebBackForwardList saveState(Bundle bundle);
	
	String getOriginalUrl();
	
	void pauseTimers();
	
	void onPause();
	
	WebBackForwardList copyBackForwardList();
	
	int getProgress();
	
	void reload();
	
	boolean canGoBack();
	
	void goBack();
	
	boolean canGoForward();
	
	void goForward();
	
	void saveWebArchive(String path, boolean b, ValueCallback<String> stringValueCallback);
	
	void SafeScrollTo(int x, int y);
	
	int getHitType(Object hitObj);
	
	String getHitExtra(Object hitObj);
	
	Object getHitResultObject();
	
	void setDownloadListener(DownloadListener listener);
	
	void setOnScrollChangedListener(RecyclerView.OnScrollChangedListener onScrollChangedListener);
	
	View getView();
	
	Map<String, String> getLastRequestHeaders();
	
	PrintDocumentAdapter createPrintDocumentAdapter(String name);
	
	//Object getTag();
}
