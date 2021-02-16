package com.tencent.smtt.export.external.interfaces;

import android.net.Uri;
import java.util.Map;

public interface WebResourceRequest{
   Uri getUrl();

   boolean isForMainFrame();

   boolean isRedirect();

   boolean hasGesture();

   String getMethod();

   Map<String, String> getRequestHeaders();
}
