package com.tencent.smtt.export.external.interfaces;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public interface IX5WebHistoryItem {
   int getId();

   String getUrl();

   String getOriginalUrl();

   String getTitle();

   Bitmap getFavicon();

   String getTouchIconUrl();

   Object getCustomData();

   void setCustomData(Object var1);

   void setFavicon(Bitmap var1);

   void setUrl(String var1);

   boolean getIsSubmitForm();

   boolean drawBaseLayer(Canvas var1, boolean var2);

   boolean canDrawBaseLayer();
}
