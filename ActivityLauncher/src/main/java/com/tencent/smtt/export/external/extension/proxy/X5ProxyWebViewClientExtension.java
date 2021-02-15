package com.tencent.smtt.export.external.extension.proxy;

import com.tencent.smtt.export.external.WebViewWizardBase;
import com.tencent.smtt.export.external.extension.interfaces.IX5WebViewClientExtension;

public abstract class X5ProxyWebViewClientExtension extends ProxyWebViewClientExtension {
   public X5ProxyWebViewClientExtension(WebViewWizardBase var1) {
      this.mWebViewClientExt = (IX5WebViewClientExtension)var1.newInstance("com.tencent.smtt.webkit.WebViewClientExtension");
   }

   public X5ProxyWebViewClientExtension(IX5WebViewClientExtension var1) {
      this.mWebViewClientExt = var1;
   }
}
