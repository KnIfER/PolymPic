package com.knziha.polymer.browser.webkit;

import android.graphics.Picture;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.widgets.WebFrameLayout;

import java.util.Map;

public interface UniversalWebviewInterface {
	void loadUrl(String url);
	
	void clearHistory();
	
	void clearView();
	
	void stopLoading();
	
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
	
	Object initPrintDocumentAdapter(String name);
	
	Picture capturePicture();
	
	void requestFocusNodeHref(@Nullable Message hrefMsg);
	
	void requestImageRef(Message var1);
	
	int getType();
	
	int getContentWidth();
	
	int getContentHeight();
	
	void setPictureListener(WebView.PictureListener pictureListener);
	
	//Object getTag();
}
