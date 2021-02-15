package com.tencent.smtt.sdk.EmergencyUtils;

import android.text.TextUtils;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ParametersHelper {
   private String a;
   private String b;
   private Integer c;
   private String d;
   private String e;
   private Integer f;
   private Integer g;
   private List<Integer> h;

   public String buildParameters() {
      JSONObject var1 = new JSONObject();
      JSONObject var2 = new JSONObject();

      try {
         if (!TextUtils.isEmpty(this.a)) {
            var2.put("PP", this.a);
         }

         if (!TextUtils.isEmpty(this.b)) {
            var2.put("PPVN", this.b);
         }

         if (this.c != null) {
            var2.put("ADRV", this.c);
         }

         if (!TextUtils.isEmpty(this.d)) {
            var2.put("MODEL", this.d);
         }

         if (!TextUtils.isEmpty(this.e)) {
            var2.put("NAME", this.e);
         }

         if (this.f != null) {
            var2.put("SDKVC", this.f);
         }

         if (this.g != null) {
            var2.put("COMPVC", this.g);
         }

         var1.put("terminal_params", var2);
         if (this.h != null) {
            JSONArray var3 = new JSONArray();

            for(int var4 = 0; var4 < this.h.size(); ++var4) {
               var3.put(this.h.get(var4));
            }

            var1.put("ids", var3);
         }
      } catch (JSONException var5) {
         var5.printStackTrace();
      }

      return var1.toString();
   }

   public void a(String var1) {
      this.a = var1;
   }

   public void b(String var1) {
      this.b = var1;
   }

   public void a(Integer var1) {
      this.c = var1;
   }

   public void c(String var1) {
      this.d = var1;
   }

   public void setEmergenceIds(List<Integer> var1) {
      this.h = var1;
   }
}
