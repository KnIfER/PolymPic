package com.tencent.smtt.export.external.jscore.interfaces;

import android.os.Looper;

public interface IX5JsVirtualMachine {
   Looper getLooper();

   IX5JsContext createJsContext();

   void onPause();

   void onResume();

   void destroy();
}
