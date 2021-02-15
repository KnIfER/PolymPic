package com.tencent.smtt.sdk;

import android.graphics.Bitmap;
import com.tencent.smtt.export.external.interfaces.IX5WebHistoryItem;

public class WebHistoryItem {
   private IX5WebHistoryItem a = null;
   private android.webkit.WebHistoryItem b = null;

   private WebHistoryItem() {
   }

   static WebHistoryItem a(IX5WebHistoryItem var0) {
      if (var0 == null) {
         return null;
      } else {
         WebHistoryItem var1 = new WebHistoryItem();
         var1.a = var0;
         return var1;
      }
   }

   static WebHistoryItem a(android.webkit.WebHistoryItem var0) {
      if (var0 == null) {
         return null;
      } else {
         WebHistoryItem var1 = new WebHistoryItem();
         var1.b = var0;
         return var1;
      }
   }

   public String getUrl() {
      return this.a != null ? this.a.getUrl() : this.b.getUrl();
   }

   public String getOriginalUrl() {
      return this.a != null ? this.a.getOriginalUrl() : this.b.getOriginalUrl();
   }

   public String getTitle() {
      return this.a != null ? this.a.getTitle() : this.b.getTitle();
   }

   public Bitmap getFavicon() {
      return this.a != null ? this.a.getFavicon() : this.b.getFavicon();
   }
}
