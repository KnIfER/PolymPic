package com.tencent.smtt.export.external.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class UrlResponseInfo {
   public abstract String getUrl();

   public abstract List<String> getUrlChain();

   public abstract int getHttpStatusCode();

   public abstract String getHttpStatusText();

   public abstract List<Entry<String, String>> getAllHeadersAsList();

   public abstract Map<String, List<String>> getAllHeaders();

   public abstract boolean wasCached();

   public abstract String getNegotiatedProtocol();

   public abstract String getProxyServer();

   public abstract long getReceivedByteCount();

   public String getServerIP() {
      return "";
   }

   public Map<String, List<String>> getRequestHeaders() {
      HashMap var1 = new HashMap();
      return var1;
   }

   public abstract static class HeaderBlock {
      public abstract List<Entry<String, String>> getAsList();

      public abstract Map<String, List<String>> getAsMap();
   }
}
