/*
 * Copyright 2020 The 多聚浏览 Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.polymer;

import android.content.Context;
import android.os.Build;
import android.webkit.WebSettings;

import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.webkit.UniversalWebviewInterface;
import com.knziha.polymer.browser.webkit.WebViewImplExt;

/** Advanced WebView For DuoJuLiuLanQi <br/>
 * 多聚浏览器 <br/>
 * Based On previous work on Plain-Dictionary*/
public class AdvancedBrowserWebView extends WebViewImplExt {
	
	public AdvancedBrowserWebView(Context context) {
		super(context);
		//super(context, null, android.R.attr.webViewStyle);
		
		//mChildHelper = new NestedScrollingChildHelper(this);
		//setNestedScrollingEnabled(true);
		
		//setVerticalScrollBarEnabled(false);
		//setHorizontalScrollBarEnabled(false);
		
		//setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		
		final WebSettings settings = getSettings();
		
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		//	settings.setSafeBrowsingEnabled(false);
		//}
		
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setDefaultTextEncodingName("UTF-8");
		
		settings.setNeedInitialFocus(false);
		settings.setTextZoom(100);
		//settings.setDefaultFontSize(60);
		//setInitialScale(25);
		
		//settings.setJavaScriptEnabled(true);
		//settings.setJavaScriptCanOpenWindowsAutomatically(false);
		//settings.setMediaPlaybackRequiresUserGesture(false);
		
		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);
		settings.setDomStorageEnabled(true);
		
		settings.setAllowFileAccess(true);
		
		settings.setUseWideViewPort(true);//设定支持viewport
		settings.setLoadWithOverviewMode(true);
		//settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
		//settings.setSupportZoom(support);
		
		settings.setAllowUniversalAccessFromFileURLs(true);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
		}
		
		//settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		
		//setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}
	
	///////// AdvancedNestScrollWebView START /////////
	////////  Copyright (C) 2016 Tobias Rohloff ////////
	////////  Apache License, Version 2.0 ////////
	
	///////// AdvancedNestScrollWebView END /////////
	
	///////// AdvancedBrowserWebView Start /////////
	
	
}
