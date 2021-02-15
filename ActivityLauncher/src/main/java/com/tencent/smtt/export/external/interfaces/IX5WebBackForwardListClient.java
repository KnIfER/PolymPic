package com.tencent.smtt.export.external.interfaces;

public interface IX5WebBackForwardListClient {
   void onNewHistoryItem(IX5WebHistoryItem var1);

   void onIndexChanged(IX5WebHistoryItem var1, int var2);

   void onRemoveHistoryItem(IX5WebHistoryItem var1);
}
