package com.tencent.smtt.export.external.embeddedwidget.interfaces;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.Surface;

public interface IEmbeddedWidgetClient {
   void onSurfaceCreated(Surface var1);

   void onSurfaceDestroyed(Surface var1);

   boolean onTouchEvent(MotionEvent var1);

   void onRectChanged(Rect var1);

   void onVisibilityChanged(boolean var1);

   void onDestroy();

   void onActive();

   void onDeactive();

   void onRequestRedraw();
}
