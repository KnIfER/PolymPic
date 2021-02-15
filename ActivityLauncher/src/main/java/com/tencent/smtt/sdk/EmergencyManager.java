package com.tencent.smtt.sdk;

import android.content.Context;
import android.text.TextUtils;
import com.tencent.smtt.export.external.DexLoader;
import com.tencent.smtt.sdk.EmergencyUtils.Command;
import com.tencent.smtt.sdk.EmergencyUtils.CommandsHelper;
import com.tencent.smtt.sdk.EmergencyUtils.FetchHelper;
import com.tencent.smtt.sdk.EmergencyUtils.ParametersHelper;
import com.tencent.smtt.sdk.EmergencyUtils.PreferenceHelper;
import com.tencent.smtt.utils.AppUtil;
import com.tencent.smtt.utils.TbsCommonConfig;
import com.tencent.smtt.utils.TbsLog;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EmergencyManager {
   private static String a = "EmergencyManager";
   private static int b = 0;
   private static int c = 1;
   private static int d = 2;
   private static int e = 3;
   private static int f = 4;
   private static int g = 5;
   private static EmergencyManager instance;
   private long i = 60000L;
   private long j = 86400000L;
   private boolean initilized = false;
   private DexLoader dexLoader;

   private EmergencyManager() {
   }

   public static synchronized EmergencyManager getInstance() {
      if (instance == null) {
         instance = new EmergencyManager();
      }

      return instance;
   }

   public void dispatchEmergencyCommand(Context context, Integer var2, Map<Integer, String> commands) {
      if (null != this.dexLoader) {
         TbsLog.e(a, "Dispatch emergency commands on tbs shell");
         Class[] var4 = new Class[]{Integer.class, Map.class};
         Object[] var5 = new Object[]{var2, commands};
         this.dexLoader.invokeStaticMethod("com.tencent.tbs.tbsshell.TBSShell", "dispatchEmergencyCommand", var4, var5);
      } else {
         TbsLog.e(a, "Dex loader is null, cancel commands dispatching of tbs shell");
      }

      TbsLog.e(a, "Dispatch emergency commands on tbs extension");
      QbSdk.a(context, var2, commands);
   }

   public void init(Context context) {
      if (!this.initilized) {
         this.initilized = true;
         PreferenceHelper var2 = PreferenceHelper.getInstance();
         if (!var2.hasFileLock()) {
            var2.createFileLock(context);

            try {
               long var3 = PreferenceHelper.getInstance().getLong(context, "emergence_timestamp");
               long var5 = PreferenceHelper.getInstance().getLong(context, "emergence_req_interval");
               long var7 = System.currentTimeMillis();
               long var9 = var7 - var3;
               long var11 = Math.min(Math.max(this.i, var5), this.j);
               if (var9 > var11) {
                  PreferenceHelper.getInstance().putLong(context, "emergence_timestamp", var7);
                  this.b(context);
               } else {
                  this.dispatchEmergencyCommandsValid(context, c, new ArrayList());
               }
            } catch (Exception var16) {
               this.dispatchEmergencyCommandsValid(context, g, new ArrayList());
            } finally {
               PreferenceHelper.getInstance().c();
            }
         } else {
            this.dispatchEmergencyCommandsValid(context, f, new ArrayList());
         }
      }

   }

   private void b(final Context context) {
      ParametersHelper parametersHelper = new ParametersHelper();
      parametersHelper.a(AppUtil.getPackageName(context));
      parametersHelper.b(AppUtil.getVersionName(context));
      parametersHelper.a(AppUtil.getVersion(context));
      parametersHelper.c(AppUtil.getIsoModel());
      List emergenceIds = PreferenceHelper.getInstance().getArrayList(context, "emergence_ids");
      Iterator emidsIter = emergenceIds.iterator();
      ArrayList emids = new ArrayList();

      while(emidsIter.hasNext()) {
         try {
            String text = (String)emidsIter.next();
            if (!TextUtils.isEmpty(text)) {
               String[] arr = PreferenceHelper.splitAsArr(text);
               if (arr != null && arr.length == 2) {
                  int eId = Integer.parseInt(arr[0]);
                  long exp = Long.parseLong(arr[1]);
                  long now = System.currentTimeMillis();
                  if (now < exp) {
                     emids.add(eId);
                  }
               }
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      }

      parametersHelper.setEmergenceIds((List)emids);
      FetchHelper fetchHelper = new FetchHelper(context, TbsCommonConfig.getInstance(context).getTbsEmergencyPostUrl(), parametersHelper.buildParameters());
      fetchHelper.postFetch(new FetchHelper.OnResponsedCallback() {
         public void onResponsed(String jsonText) {
            CommandsHelper commandsHelper = CommandsHelper.feedNewCommands(jsonText);
            if (commandsHelper != null && commandsHelper.getRetCode() == 0) {
               PreferenceHelper.getInstance().putLong(context, "emergence_req_interval", commandsHelper.getNextReqInterval());
               List commands = commandsHelper.getCommands();
               if (commands != null) {
                  EmergencyManager.this.dispatchEmergencyCommandsValid(context, EmergencyManager.b, commands);
               } else {
                  EmergencyManager.this.dispatchEmergencyCommandsValid(context, EmergencyManager.d, new ArrayList());
               }
            } else {
               EmergencyManager.this.dispatchEmergencyCommandsValid(context, EmergencyManager.e, new ArrayList());
            }

         }
      });
   }

   private void dispatchEmergencyCommandsValid(Context context, int type, List<Command> var3) {
      LinkedHashMap linkedHashMap = new LinkedHashMap();
      PreferenceHelper var5 = PreferenceHelper.getInstance();
      List emIds = var5.getArrayList(context, "emergence_ids");
      HashSet var7 = new HashSet();
      Iterator var8;
      if (emIds != null && !emIds.isEmpty()) {
         var8 = emIds.iterator();

         while(var8.hasNext()) {
            String var9 = (String)var8.next();
            String[] var10 = PreferenceHelper.splitAsArr(var9);
            if (var10 != null && var10.length == 2) {
               var7.add(Integer.parseInt(var10[0]));
            }
         }
      }

      var8 = var3.iterator();

      while(var8.hasNext()) {
         Command command = (Command)var8.next();
         int cmd = command.getCmd();
         int id = command.getId();
         if (!var7.contains(id) && !command.isExpired()) {
            linkedHashMap.put(cmd, command.getExtra());
            String strArr = PreferenceHelper.joinStrArr(new String[]{String.valueOf(id), String.valueOf(command.getExpiration())});
            var5.appendArrayListStr(context, "emergence_ids", strArr);
         }
      }

      SDKEcService.a().handleEmergencyCommands(type, (Map)linkedHashMap);
      this.dispatchEmergencyCommand(context, type, linkedHashMap);
   }

   public void setDexLoader(DexLoader dexLoader1) {
      this.dexLoader = dexLoader1;
   }
}
