package com.tencent.smtt.utils;

import android.content.Context;
import android.os.Process;
import android.util.Log;
import android.widget.TextView;

import com.knziha.polymer.Utils.CMN;

import java.util.LinkedList;
import java.util.List;

public class TbsLog {
   private static boolean a = false;
   private static boolean b = true;
   public static final String X5LOGTAG = "x5logtag";
   private static TbsLogClient c = null;
   public static final int TBSLOG_CODE_SDK_BASE = 1000;
   public static final int TBSLOG_CODE_SDK_INIT = 999;
   public static final int TBSLOG_CODE_SDK_LOAD_ERROR = 998;
   public static final int TBSLOG_CODE_SDK_INVOKE_ERROR = 997;
   public static final int TBSLOG_CODE_SDK_SELF_MODE = 996;
   public static final int TBSLOG_CODE_SDK_THIRD_MODE = 995;
   public static final int TBSLOG_CODE_SDK_NO_SHARE_X5CORE = 994;
   public static final int TBSLOG_CODE_SDK_CONFLICT_X5CORE = 993;
   public static final int TBSLOG_CODE_SDK_UNAVAIL_X5CORE = 992;
   public static List<String> sTbsLogList = new LinkedList();
   public static int sLogMaxCount = 10;

   public static void setWriteLogJIT(boolean var0) {
      b = var0;
      if (c != null) {
         TbsLogClient var10000 = c;
         TbsLogClient.setWriteLogJIT(var0);
      }
   }

   public static void app_extra(String var0, Context var1) {
      try {
         Context var2 = var1.getApplicationContext();
         String[] var3 = new String[]{"com.tencent.tbs", "com.tencent.mtt", "com.tencent.mm", "com.tencent.mobileqq", "com.tencent.mtt.sdk.test", "com.qzone"};
         String[] var4 = new String[]{"DEMO", "QB", "WX", "QQ", "TEST", "QZ"};
         boolean var5 = false;

         int var7;
         for(var7 = 0; var7 < var3.length; ++var7) {
            if (var2.getPackageName().contains(var3[var7])) {
               i(var0, "app_extra pid:" + Process.myPid() + "; APP_TAG:" + var4[var7] + "!");
               break;
            }
         }

         if (var7 == var3.length) {
            i(var0, "app_extra pid:" + Process.myPid() + "; APP_TAG:OTHER!");
         }
      } catch (Throwable var6) {
         w(var0, "app_extra exception:" + Log.getStackTraceString(var6));
      }

   }

   public static void i(Throwable var0) {
      i("handle_throwable", Log.getStackTraceString(var0));
   }

   public static void i(String var0, String var1) {
      if (c != null) {
//         c.i(var0, "TBS:" + var1);
//         c.writeLog("(I)-" + var0 + "-TBS:" + var1);
      }
	   CMN.Log("TBS::", var1);
   }

   public static void i(String var0, String var1, boolean var2) {
//      i(var0, var1);
//      if (c != null && a && var2) {
//         c.showLog(var0 + ": " + var1);
//      }
      //if(var2)
	   CMN.Log("TBS::", var1);

   }

   public static void e(String var0, String var1) {
      if (c != null) {
		  //c.e(var0, "TBS:" + var1);
         //c.writeLog("(E)-" + var0 + "-TBS:" + var1);
      }
	   CMN.Log("TBS::", var1);
   }

   public static void e(String var0, String var1, boolean var2) {
//      e(var0, var1);
//      if (c != null && a && var2) {
//         c.showLog(var0 + ": " + var1);
//      }
	//if(var2)
	   CMN.Log("TBS::", var1);
   }

   public static void w(String var0, String var1) {
//      if (c != null) {
//         c.w(var0, "TBS:" + var1);
//         c.writeLog("(W)-" + var0 + "-TBS:" + var1);
//      }
	   CMN.Log("TBS::", var1);
   }

   public static void w(String var0, String var1, boolean var2) {
//      w(var0, var1);
//      if (c != null && a && var2) {
//         c.showLog(var0 + ": " + var1);
//      }
	//if(var2)
	   CMN.Log("TBS::", var1);
   }

   public static void d(String var0, String var1) {
      if (c != null) {
        // c.d(var0, "TBS:" + var1);
      }
	   //if(var2)
		   CMN.Log("TBS::", var1);
   }

   public static void d(String var0, String var1, boolean var2) {
//      d(var0, var1);
//      if (c != null && a && var2) {
//         c.showLog(var0 + ": " + var1);
//      }
	
	   //if(var2)
		   CMN.Log("TBS::", var1);
   }

   public static void v(String var0, String var1) {
      if (c != null) {
        // c.v(var0, "TBS:" + var1);
      }
	   //if(var2)
		   CMN.Log("TBS::", var1);
   }

   public static void v(String var0, String var1, boolean var2) {
//      v(var0, var1);
//      if (c != null && a && var2) {
//         c.showLog(var0 + ": " + var1);
//      }
	
	  //if(var2)
		   CMN.Log("TBS::", var1);
   }

   public static void setLogView(TextView var0) {
      if (var0 != null && c != null) {
         c.setLogView(var0);
      }
   }

   public static boolean setTbsLogClient(TbsLogClient var0) {
      if (var0 == null) {
         return false;
      } else {
         c = var0;
         TbsLogClient var10000 = c;
         TbsLogClient.setWriteLogJIT(b);
         return true;
      }
   }

   public static String getTbsLogFilePath() {
      return TbsLogClient.c != null ? TbsLogClient.c.getAbsolutePath() : null;
   }

   public static synchronized void initIfNeed(Context var0) {
      if (c == null) {
         setTbsLogClient(new TbsLogClient(var0));
      }
   }

   public static synchronized void writeLogToDisk() {
//      if (c != null) {
//         c.writeLogToDisk();
//      }

   }

   public static void addLog(int var0, String var1, Object... var2) {
   	CMN.Log("addLog", var0, var1);
   	CMN.Log("addLog", var2);
//      synchronized(sTbsLogList) {
//         try {
//            if (sTbsLogList.size() > sLogMaxCount) {
//               int var4 = sTbsLogList.size() - sLogMaxCount;
//
//               while(var4-- > 0 && sTbsLogList.size() > 0) {
//                  sTbsLogList.remove(0);
//               }
//            }
//
//            String var10 = null;
//
//            try {
//               if (var1 != null) {
//                  var10 = String.format(var1, var2);
//               }
//            } catch (Exception var7) {
//            }
//
//            if (var10 == null) {
//               var10 = "";
//            }
//
//            String var5 = String.format("[%d][%d][%c][%d]%s", System.currentTimeMillis(), 1, '0', var0, var10);
//            sTbsLogList.add(var5);
//         } catch (Exception var8) {
//            var8.printStackTrace();
//         }
//
//      }
   }
}
