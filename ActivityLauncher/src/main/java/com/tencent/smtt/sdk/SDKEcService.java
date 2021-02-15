package com.tencent.smtt.sdk;

import com.tencent.smtt.utils.TbsLog;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class SDKEcService {
   private static String a = "SDKEcService";
   private static SDKEcService b;
   private Map<Integer, SDKEcService.a> c = new LinkedHashMap();
   private Map<Integer, String> d = new LinkedHashMap();
   private int e = -1;

   public static SDKEcService a() {
      if (b == null) {
         b = new SDKEcService();
      }

      return b;
   }

   public void executedCommand(int var1, SDKEcService.a var2) {
      if (this.d.containsKey(var1)) {
         String var3 = (String)this.d.get(var1);
         this.d.remove(var1);
         var2.a(var3);
         TbsLog.i(a, "Executed command: " + var1 + ", extra: " + var3 + ", emergency configuration has requested");
      } else if (this.e == -1) {
         this.c.put(var1, var2);
         TbsLog.i(a, "Emergency configuration has not yet dispatched. Command query: " + var1 + " has been suspended");
      } else {
         TbsLog.i(a, "Emergency configuration has been dispatched, status: " + this.e + ". Command query: " + var1 + " ignored");
      }

   }

   public void handleEmergencyCommands(int var1, Map<Integer, String> var2) {
      TbsLog.i(a, "Handle emergency commands in sdk, status: " + var1);
      LinkedHashMap var3 = new LinkedHashMap();
      if (var1 == 0) {
         Iterator var4 = var2.keySet().iterator();

         while(var4.hasNext()) {
            Integer var5 = (Integer)var4.next();
            if (this.c.containsKey(var5)) {
               SDKEcService.a var6 = (SDKEcService.a)this.c.get(var5);
               if (var6 != null) {
                  var6.a((String)var2.get(var5));
               }
            } else {
               String var7 = (String)var2.get(var5);
               var3.put(var5, var7 != null ? var7 : "");
            }
         }
      } else {
         this.c.clear();
         TbsLog.i(a, "Handle emergency commands failed, ignore all unhandled emergencies, status: " + var1);
      }

      this.d = var3;
      this.e = var1;
   }

   public interface a {
      void a(String var1);
   }
}
