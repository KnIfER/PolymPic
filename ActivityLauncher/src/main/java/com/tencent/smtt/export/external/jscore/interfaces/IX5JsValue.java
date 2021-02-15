package com.tencent.smtt.export.external.jscore.interfaces;

import java.nio.ByteBuffer;

public interface IX5JsValue {
   boolean isUndefined();

   boolean isNull();

   boolean isArray();

   boolean isBoolean();

   boolean toBoolean();

   boolean isInteger();

   int toInteger();

   boolean isNumber();

   Number toNumber();

   boolean isString();

   String toString();

   boolean isObject();

   boolean isJavascriptInterface();

   Object toJavascriptInterface();

   boolean isArrayBufferOrArrayBufferView();

   ByteBuffer toByteBuffer();

   boolean isFunction();

   IX5JsValue call(Object[] var1);

   IX5JsValue construct(Object[] var1);

   <T> T toObject(Class<T> var1);

   boolean isPromise();

   void resolveOrReject(Object var1, boolean var2);

   public interface JsValueFactory {
      String getJsValueClassName();

      Object wrap(IX5JsValue var1);

      IX5JsValue unwrap(Object var1);
   }
}
