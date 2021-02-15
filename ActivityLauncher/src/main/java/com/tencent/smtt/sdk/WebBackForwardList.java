package com.tencent.smtt.sdk;

import com.tencent.smtt.export.external.interfaces.IX5WebBackForwardList;

public class WebBackForwardList {
   private IX5WebBackForwardList a = null;
   private android.webkit.WebBackForwardList b = null;

   static WebBackForwardList a(IX5WebBackForwardList var0) {
      if (var0 == null) {
         return null;
      } else {
         WebBackForwardList var1 = new WebBackForwardList();
         var1.a = var0;
         return var1;
      }
   }

   static WebBackForwardList a(android.webkit.WebBackForwardList var0) {
      if (var0 == null) {
         return null;
      } else {
         WebBackForwardList var1 = new WebBackForwardList();
         var1.b = var0;
         return var1;
      }
   }

   public WebHistoryItem getCurrentItem() {
      return this.a != null ? WebHistoryItem.a(this.a.getCurrentItem()) : WebHistoryItem.a(this.b.getCurrentItem());
   }

   public int getCurrentIndex() {
      return this.a != null ? this.a.getCurrentIndex() : this.b.getCurrentIndex();
   }

   public WebHistoryItem getItemAtIndex(int var1) {
      return this.a != null ? WebHistoryItem.a(this.a.getItemAtIndex(var1)) : WebHistoryItem.a(this.b.getItemAtIndex(var1));
   }

   public int getSize() {
      return this.a != null ? this.a.getSize() : this.b.getSize();
   }
}
