package com.tencent.smtt.sdk;

import android.view.MotionEvent;
import android.view.View;

public interface WebViewCallbackClient {
   boolean onTouchEvent(MotionEvent var1, View var2);

   boolean overScrollBy(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, boolean var9, View var10);

   boolean dispatchTouchEvent(MotionEvent var1, View var2);

   void computeScroll(View var1);

   void onOverScrolled(int var1, int var2, boolean var3, boolean var4, View var5);

   boolean onInterceptTouchEvent(MotionEvent var1, View var2);

   void onScrollChanged(int var1, int var2, int var3, int var4, View var5);

   void invalidate();
}
