package com.knziha.polymer.browser.webkit;

import com.knziha.polymer.widgets.WebFrameLayout;

public interface BitmapWaiter {
	void onBitmapCaptured(WebFrameLayout view, int reason);
}
