package com.tencent.smtt.export.external.proxy;

import com.tencent.smtt.export.external.WebViewWizardBase;
import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;

public abstract class X5ProxyWebChromeClient extends ProxyWebChromeClient {
   public X5ProxyWebChromeClient(WebViewWizardBase var1) {
      this.mWebChromeClient = (IX5WebChromeClient)var1.newInstance("com.tencent.smtt.webkit.WebChromeClient");
   }

   public X5ProxyWebChromeClient(IX5WebChromeClient var1) {
      this.setWebChromeClient(var1);
   }
}
