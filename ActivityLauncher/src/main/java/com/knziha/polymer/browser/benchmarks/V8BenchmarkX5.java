package com.knziha.polymer.browser.benchmarks;


import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebViewImplExt;
import com.knziha.polymer.browser.webkit.XPlusWebView;

import java.io.IOException;

public class V8BenchmarkX5 extends V8Benchmark {
	@Override
	protected UniversalWebviewInterface newImplWebView() {
		try {
			return new XPlusWebView(this);
		} catch (IOException e) {
			CMN.Log(e);
			return new WebViewImplExt(this);
		}
	}
}
