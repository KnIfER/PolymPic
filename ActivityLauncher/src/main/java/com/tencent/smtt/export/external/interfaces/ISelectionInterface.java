package com.tencent.smtt.export.external.interfaces;

import android.graphics.Rect;

public interface ISelectionInterface {
   short HELD_NOTHING = -1;
   short HELD_FIRST_WIDGET = 0;
   short HELD_SECOND_WIDGET = 1;
   int NONESELECTION = 0;
   int CARETSELECTION = 1;
   int INPUTSELECTION = 2;
   int NORMALSELECTION = 3;
   int EDITABLESELECTION = 4;

   /** @deprecated */
   @Deprecated
   void onSelectionChange(Rect var1, Rect var2, int var3, int var4, short var5);

   /** @deprecated */
   @Deprecated
   void onSelectionBegin(Rect var1, Rect var2, int var3, int var4, short var5);

   /** @deprecated */
   @Deprecated
   void onSelectionBeginFailed(int var1, int var2);

   void onSelectionDone(Rect var1, boolean var2);

   void hideSelectionView();

   /** @deprecated */
   @Deprecated
   void onSelectCancel();

   void updateHelperWidget(Rect var1, Rect var2);

   /** @deprecated */
   @Deprecated
   void setText(String var1, boolean var2);

   /** @deprecated */
   @Deprecated
   String getText();

   void onRetrieveFingerSearchContextResponse(String var1, String var2, int var3);
}
