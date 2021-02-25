package com.knziha.polymer.browser.benchmarks;


import android.os.Bundle;

import androidx.annotation.Nullable;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebViewImplExt;
import com.knziha.polymer.browser.webkit.XPlusWebView;
import com.knziha.polymer.browser.webkit.XWalkWebView;

import org.xwalk.core.XWalkActivityDelegate;
import org.xwalk.core.XWalkView;

import java.io.IOException;

public class V8BenchmarkXW extends V8Benchmark {
	private XWalkActivityDelegate mActivityDelegate;
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Runnable completeCommand = () -> onXWalkReady();
		
		this.mActivityDelegate = new XWalkActivityDelegate(this, completeCommand, completeCommand);
	}
	
	protected void onResume() {
		super.onResume();
		this.mActivityDelegate.onResume();
	}
	
	protected void onXWalkReady() {
		loadWebView();
	}
	
	@Override
	protected UniversalWebviewInterface newImplWebView() {
		try {
			return new XWalkWebView(this);
		} catch (Exception e) {
			CMN.Log(e);
			return new WebViewImplExt(this);
		}
	}
}
