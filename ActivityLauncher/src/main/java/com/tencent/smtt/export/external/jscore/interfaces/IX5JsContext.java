package com.tencent.smtt.export.external.jscore.interfaces;

import android.webkit.ValueCallback;
import java.net.URL;

public interface IX5JsContext {
   IX5JsValue evaluateScript(String var1, URL var2);

   void evaluateScriptAsync(String var1, ValueCallback<IX5JsValue> var2, URL var3);

   void evaluateJavascript(String var1, ValueCallback<String> var2, URL var3);

   void addJavascriptInterface(Object var1, String var2);

   void removeJavascriptInterface(String var1);

   void setExceptionHandler(ValueCallback<IX5JsError> var1);

   void setPerContextData(Object var1);

   void setName(String var1);

   void destroy();

   void stealValueFromOtherCtx(String var1, IX5JsContext var2, String var3);

   int setNativeBuffer(int var1, byte[] var2);

   int getNativeBufferId();

   byte[] getNativeBuffer(int var1);
}
