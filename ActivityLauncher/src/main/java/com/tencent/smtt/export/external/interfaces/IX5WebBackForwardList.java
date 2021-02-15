package com.tencent.smtt.export.external.interfaces;

public interface IX5WebBackForwardList {
   IX5WebHistoryItem getCurrentItem();

   int getCurrentIndex();

   IX5WebHistoryItem getItemAtIndex(int var1);

   int getSize();
}
