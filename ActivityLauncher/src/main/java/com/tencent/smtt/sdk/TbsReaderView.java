package com.tencent.smtt.sdk;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import com.tencent.smtt.sdk.stat.MttLoader;
import com.tencent.smtt.utils.Apn;

public class TbsReaderView extends FrameLayout {
   public static final String IS_BAR_ANIMATING = "is_bar_animating";
   public static final String IS_BAR_SHOWING = "is_bar_show";
   public static final String IS_INTO_DOWNLOADING = "into_downloading";
   public static final String KEY_FILE_PATH = "filePath";
   public static final String KEY_TEMP_PATH = "tempPath";
   public static String gReaderPackName = "";
   public static String gReaderPackVersion = "";
   public static final String TAG = "TbsReaderView";
   Context a = null;
   ReaderWizard b = null;
   Object c = null;
   TbsReaderView.ReaderCallback d = null;
   TbsReaderView.ReaderCallback e = null;
   public static final String READER_STATISTICS_COUNT_CLICK_LOADED_BTN = "AHNG801";
   public static final String READER_STATISTICS_COUNT_CANCEL_LOADED_BTN = "AHNG802";
   public static final String READER_STATISTICS_COUNT_RETRY_BTN = "AHNG803";
   public static final String READER_STATISTICS_COUNT_PPT_PLAY_BTN = "AHNG806";
   public static final String READER_STATISTICS_COUNT_PPT_INTO_DIALOG = "AHNG807";
   public static final String READER_STATISTICS_COUNT_PPT_INTO_DOWNLOAD = "AHNG808";
   public static final String READER_STATISTICS_COUNT_PPT_INTO_BROWSER = "AHNG809";
   public static final String READER_STATISTICS_COUNT_PDF_FOLDER_BTN = "AHNG810";
   public static final String READER_STATISTICS_COUNT_PDF_INTO_DIALOG = "AHNG811";
   public static final String READER_STATISTICS_COUNT_PDF_INTO_DOWNLOAD = "AHNG812";
   public static final String READER_STATISTICS_COUNT_PDF_INTO_BROWSER = "AHNG813";
   public static final String READER_STATISTICS_COUNT_TXT_NOVEL_BTN = "AHNG814";
   public static final String READER_STATISTICS_COUNT_TXT_INTO_DIALOG = "AHNG815";
   public static final String READER_STATISTICS_COUNT_TXT_INTO_DOWNLOAD = "AHNG816";
   public static final String READER_STATISTICS_COUNT_TXT_INTO_BROWSER = "AHNG817";
   public static final String READER_STATISTICS_COUNT_DOC_SEARCH_BTN = "AHNG826";
   public static final String READER_STATISTICS_COUNT_DOC_INTO_DIALOG = "AHNG827";
   public static final String READER_STATISTICS_COUNT_DOC_INTO_DOWNLOAD = "AHNG828";
   public static final String READER_STATISTICS_COUNT_DOC_INTO_BROWSER = "AHNG829";
   public static final int READER_CHANNEL_PPT_ID = 10833;
   public static final int READER_CHANNEL_PDF_ID = 10834;
   public static final int READER_CHANNEL_TXT_ID = 10835;
   public static final int READER_CHANNEL_DOC_ID = 10965;
   static boolean f = false;

   public TbsReaderView(Context var1, TbsReaderView.ReaderCallback var2) throws RuntimeException {
      super(var1.getApplicationContext());
      if (!(var1 instanceof Activity)) {
         throw new RuntimeException("error: unexpect context(none Activity)");
      } else {
         this.d = var2;
         this.a = var1;
         this.e = new TbsReaderView.ReaderCallback() {
            public void onCallBackAction(Integer var1, Object var2, Object var3) {
               boolean var4 = false;
               Bundle var5;
               Bundle var6;
               String var7;
               String var8;
               switch(var1) {
               case 5008:
                  if (!MttLoader.isBrowserInstalledEx(TbsReaderView.this.a)) {
                     var1 = 5011;
                     var7 = TbsReaderView.getResString(TbsReaderView.this.a, 5023);
                     var6 = new Bundle();
                     var6.putString("tip", var7);
                     var6.putString("statistics", "AHNG812");
                     var6.putInt("channel_id", 10834);
                     var2 = var6;
                     TbsReaderView.this.userStatistics("AHNG811");
                  } else {
                     var5 = null;
                     var8 = "";
                     if (var2 != null) {
                        var5 = (Bundle)var2;
                        var8 = var5.getString("docpath");
                     }

                     QbSdk.startQBForDoc(TbsReaderView.this.a, var8, 4, 0, "pdf", var5);
                     TbsReaderView.this.userStatistics("AHNG813");
                     var4 = true;
                  }
                  break;
               case 5009:
                  if (!MttLoader.isBrowserInstalledEx(TbsReaderView.this.a)) {
                     var1 = 5011;
                     var7 = TbsReaderView.getResString(TbsReaderView.this.a, 5021);
                     var6 = new Bundle();
                     var6.putString("tip", var7);
                     var6.putString("statistics", "AHNG808");
                     var6.putInt("channel_id", 10833);
                     var2 = var6;
                     TbsReaderView.this.userStatistics("AHNG807");
                  } else {
                     var5 = null;
                     var8 = "";
                     if (var2 != null) {
                        var5 = (Bundle)var2;
                        var8 = var5.getString("docpath");
                     }

                     QbSdk.startQBForDoc(TbsReaderView.this.a, var8, 4, 0, "", var5);
                     TbsReaderView.this.userStatistics("AHNG809");
                     var4 = true;
                  }
                  break;
               case 5010:
                  if (!MttLoader.isBrowserInstalledEx(TbsReaderView.this.a)) {
                     var1 = 5011;
                     var7 = TbsReaderView.getResString(TbsReaderView.this.a, 5022);
                     var6 = new Bundle();
                     var6.putString("tip", var7);
                     var6.putString("statistics", "AHNG816");
                     var6.putInt("channel_id", 10835);
                     var2 = var6;
                     TbsReaderView.this.userStatistics("AHNG815");
                  } else {
                     var5 = null;
                     var8 = "";
                     if (var2 != null) {
                        var5 = (Bundle)var2;
                        var8 = var5.getString("docpath");
                     }

                     QbSdk.startQBForDoc(TbsReaderView.this.a, var8, 4, 0, "txt", var5);
                     var4 = true;
                  }
                  break;
               case 5026:
                  if (!MttLoader.isBrowserInstalledEx(TbsReaderView.this.a)) {
                     var1 = 5011;
                     var7 = TbsReaderView.getResString(TbsReaderView.this.a, 5029);
                     var6 = new Bundle();
                     var6.putString("tip", var7);
                     var6.putString("statistics", "AHNG828");
                     var6.putInt("channel_id", 10965);
                     var2 = var6;
                     TbsReaderView.this.userStatistics("AHNG827");
                  } else {
                     var5 = null;
                     var8 = "";
                     if (var2 != null) {
                        var5 = (Bundle)var2;
                        var8 = var5.getString("docpath");
                     }

                     QbSdk.startQBForDoc(TbsReaderView.this.a, var8, 4, 0, "doc", var5);
                     TbsReaderView.this.userStatistics("AHNG829");
                     var4 = true;
                  }
                  break;
               case 5030:
                  var5 = null;
                  if (var2 != null) {
                     var5 = (Bundle)var2;
                     TbsReaderView.gReaderPackName = var5.getString("name");
                     TbsReaderView.gReaderPackVersion = var5.getString("version");
                  }

                  var4 = true;
               }

               if (TbsReaderView.this.d != null && !var4) {
                  TbsReaderView.this.d.onCallBackAction(var1, var2, var3);
               }

            }
         };
      }
   }

   static boolean a(Context var0) {
      if (!f) {
         SDKEngine.getInstance(true).init(var0.getApplicationContext(), true, false);
         f = SDKEngine.getInstance(false).isInitialized();
      }

      return f;
   }

   public static boolean isSupportExt(Context var0, String var1) {
      boolean var2 = false;
      boolean var3 = a(var0);
      if (var3) {
         var2 = ReaderWizard.isSupportCurrentPlatform(var0) && ReaderWizard.isSupportExt(var1);
      }

      return var2;
   }

   public boolean preOpen(String var1, boolean var2) {
      boolean var3 = isSupportExt(this.a, var1);
      if (!var3) {
         Log.e("TbsReaderView", "not supported by:" + var1);
         return false;
      } else {
         boolean var4 = false;
         var4 = a(this.a);
         if (var4) {
            var4 = this.a();
            if (var2 && var4) {
               boolean var5 = Apn.getApnType(this.a) == 3;
               var4 = this.b.checkPlugin(this.c, this.a, var1, var5);
            }
         }

         return var4;
      }
   }

   public boolean downloadPlugin(String var1) {
      if (this.c == null) {
         Log.e("TbsReaderView", "downloadPlugin failed!");
         return false;
      } else {
         return this.b.checkPlugin(this.c, this.a, var1, true);
      }
   }

   public static Drawable getResDrawable(Context var0, int var1) {
      Drawable var2 = null;
      if (a(var0)) {
         var2 = ReaderWizard.getResDrawable(var1);
      }

      return var2;
   }

   public static String getResString(Context var0, int var1) {
      String var2 = "";
      if (a(var0)) {
         var2 = ReaderWizard.getResString(var1);
      }

      return var2;
   }

   public void openFile(Bundle var1) {
      boolean var2 = false;
      if (this.c != null && var1 != null) {
         boolean var3 = MttLoader.isBrowserInstalledEx(this.a);
         var3 |= !MttLoader.isBrowserInstalled(this.a);
         var1.putBoolean("browser6.0", var3);
         long var4 = 6101625L;
         long var6 = 610000L;
         boolean var8 = MttLoader.isGreatBrowserVer(this.a, var4, var6);
         var8 |= !MttLoader.isBrowserInstalled(this.a);
         var1.putBoolean("browser6.1", var8);
         var2 = this.b.openFile(this.c, this.a, var1, this);
         if (!var2) {
            Log.e("TbsReaderView", "OpenFile failed!");
         }

      } else {
         Log.e("TbsReaderView", "init failed!");
      }
   }

   public void doCommand(Integer var1, Object var2, Object var3) {
      if (this.b != null && this.c != null) {
         this.b.doCommand(this.c, var1, var2, var3);
      }

   }

   public void onSizeChanged(int var1, int var2) {
      if (null != this.b && null != this.c) {
         this.b.onSizeChanged(this.c, var1, var2);
      }

   }

   public void onStop() {
      if (this.b != null) {
         this.b.destroy(this.c);
         this.c = null;
      }

      this.a = null;
      f = false;
   }

   public void userStatistics(String var1) {
      if (this.b != null) {
         this.b.userStatistics(this.c, var1);
      }

   }

   boolean a() {
      boolean var1 = false;

      try {
         if (this.b == null) {
            this.b = new ReaderWizard(this.e);
         }

         if (null == this.c) {
            this.c = this.b.getTbsReader();
         }

         if (this.c != null) {
            var1 = this.b.initTbsReader(this.c, this.a);
         }
      } catch (NullPointerException var3) {
         Log.e("TbsReaderView", "Unexpect null object!");
         var1 = false;
      }

      return var1;
   }

   public interface ReaderCallback {
      int NOTIFY_CANDISPLAY = 12;
      int NOTIFY_ERRORCODE = 19;
      int GET_BAR_ANIMATING = 5000;
      int HIDDEN_BAR = 5001;
      int SHOW_BAR = 5002;
      int COPY_SELECT_TEXT = 5003;
      int SEARCH_SELECT_TEXT = 5004;
      int READER_TOAST = 5005;
      int SHOW_DIALOG = 5006;
      int READER_PDF_LIST = 5008;
      int READER_PPT_PLAY_MODEL = 5009;
      int READER_TXT_READING_MODEL = 5010;
      int INSTALL_QB = 5011;
      int READER_PLUGIN_STATUS = 5012;
      int READER_PLUGIN_CANLOAD = 5013;
      int READER_PLUGIN_DOWNLOADING = 5014;
      int READER_PLUGIN_COMMAND_FIXSCREEN = 5015;
      int READER_PLUGIN_RES_FIXSCREEN_NORMAL = 5016;
      int READER_PLUGIN_RES_FIXSCREEN_PRESS = 5017;
      int READER_PLUGIN_COMMAND_ROTATESCREEN = 5018;
      int READER_PLUGIN_RES_ROTATESCREEN_NORMAL = 5019;
      int READER_PLUGIN_RES_ROTATESCREEN_PRESS = 5020;
      int READER_PLUGIN_RES_PPT_GUIDE = 5021;
      int READER_PLUGIN_RES_TXT_GUIDE = 5022;
      int READER_PLUGIN_RES_PDF_GUIDE = 5023;
      int GET_BAR_ISSHOWING = 5024;
      int READER_PLUGIN_SO_ERR = 5025;
      int READER_SEARCH_IN_DOCUMENT = 5026;
      int READER_PLUGIN_SO_INTO_START = 5027;
      int READER_PLUGIN_SO_PROGRESS = 5028;
      int READER_PLUGIN_RES_DOC_GUIDE = 5029;
      int READER_PLUGIN_SO_VERSION = 5030;
      int READER_OPEN_QQ_FILE_LIST = 5031;
      int READER_PLUGIN_ACTIVITY_PAUSE = 5032;
      int READER_PLUGIN_COMMAND_PPT_PLAYER = 5035;
      int READER_PLUGIN_COMMAND_PDF_LIST = 5036;
      int READER_PLUGIN_COMMAND_TEXT_FIND = 5038;
      int READER_PLUGIN_COMMAND_TEXT_FIND_NEXT = 5039;
      int READER_PLUGIN_COMMAND_TEXT_FIND_PREV = 5040;
      int READER_PLUGIN_COMMAND_TEXT_FIND_CLEAR = 5041;
      int READER_PLUGIN_TEXT_FIND_RESULT = 5042;

      void onCallBackAction(Integer var1, Object var2, Object var3);
   }
}
