package com.tencent.smtt.sdk;

import android.os.Build.VERSION;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import com.tencent.smtt.export.external.interfaces.ServiceWorkerClient;
import com.tencent.smtt.export.external.interfaces.ServiceWorkerWebSettings;
// https://github.com/bino7/chromium/blob/4666a6bb6fdcb1114afecf77bdaa239d9787b752/android_webview/glue/java/src/com/android/webview/chromium/ServiceWorkerControllerAdapter.java

/**
 * Chromium implementation of ServiceWorkerController -- forwards calls to
 * the chromium internal implementation.
 */
public class ServiceWorkerControllerAdapter extends ServiceWorkerController {
   public ServiceWorkerWebSettings getServiceWorkerWebSettings() {
      if (VERSION.SDK_INT < 24) {
         return null;
      } else {
         final android.webkit.ServiceWorkerWebSettings serviceWorkerWebSettings = android.webkit.ServiceWorkerController.getInstance().getServiceWorkerWebSettings();
         return new ServiceWorkerWebSettings() {
            public void setCacheMode(int var1x) {
               if (VERSION.SDK_INT >= 24) {
                  serviceWorkerWebSettings.setCacheMode(var1x);
               }

            }

            public int getCacheMode() {
               return VERSION.SDK_INT >= 24 ? serviceWorkerWebSettings.getCacheMode() : -1;
            }

            public void setAllowContentAccess(boolean var1x) {
               if (VERSION.SDK_INT >= 24) {
                  serviceWorkerWebSettings.setAllowContentAccess(var1x);
               }

            }

            public boolean getAllowContentAccess() {
               return VERSION.SDK_INT >= 24 ? serviceWorkerWebSettings.getAllowContentAccess() : false;
            }

            public void setAllowFileAccess(boolean var1x) {
               if (VERSION.SDK_INT >= 24) {
                  serviceWorkerWebSettings.setAllowContentAccess(var1x);
               }

            }

            public boolean getAllowFileAccess() {
               return VERSION.SDK_INT >= 24 ? serviceWorkerWebSettings.getAllowFileAccess() : false;
            }

            public void setBlockNetworkLoads(boolean var1x) {
               if (VERSION.SDK_INT >= 24) {
                  serviceWorkerWebSettings.setBlockNetworkLoads(var1x);
               }

            }

            public boolean getBlockNetworkLoads() {
               return VERSION.SDK_INT >= 24 ? serviceWorkerWebSettings.getBlockNetworkLoads() : false;
            }
         };
      }
   }

   public void setServiceWorkerClient(final ServiceWorkerClient serviceWorkerClient) {
      if (VERSION.SDK_INT >= 24) {
         android.webkit.ServiceWorkerController.getInstance()
				 .setServiceWorkerClient(new android.webkit.ServiceWorkerClient() {
            public WebResourceResponse shouldInterceptRequest(WebResourceRequest webResourceRequest) {
               WebResourceRequestEx requestEx = new WebResourceRequestEx(webResourceRequest.getUrl().toString(), webResourceRequest.isForMainFrame(), webResourceRequest.isRedirect(), webResourceRequest.hasGesture(), webResourceRequest.getMethod(), webResourceRequest.getRequestHeaders());
               com.tencent.smtt.export.external.interfaces.WebResourceResponse response = serviceWorkerClient.shouldInterceptRequest(requestEx);
               if (response == null) {
                  return null;
               } else {
                  WebResourceResponse resourceResponse = new WebResourceResponse(response.getMimeType(), response.getEncoding(), response.getData());
                  resourceResponse.setResponseHeaders(response.getResponseHeaders());
                  int var5 = response.getStatusCode();
                  String var6 = response.getReasonPhrase();
                  if (var5 != resourceResponse.getStatusCode() || var6 != null && !var6.equals(resourceResponse.getReasonPhrase())) {
                     resourceResponse.setStatusCodeAndReasonPhrase(var5, var6);
                  }

                  return resourceResponse;
               }
            }
         });
      }

   }
}
