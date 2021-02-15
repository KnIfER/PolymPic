package com.tencent.smtt.export.external.proxy;

import com.tencent.smtt.export.external.WebViewWizardBase;
import com.tencent.smtt.export.external.interfaces.IX5WebViewClient;

public abstract class X5ProxyWebViewClient extends ProxyWebViewClient {
   public X5ProxyWebViewClient(WebViewWizardBase var1) {
      this.mWebViewClient = (IX5WebViewClient)var1.newInstance("com.tencent.smtt.webkit.WebViewClient");
   }

   public X5ProxyWebViewClient(IX5WebViewClient var1) {
      this.mWebViewClient = var1;
   }
}
