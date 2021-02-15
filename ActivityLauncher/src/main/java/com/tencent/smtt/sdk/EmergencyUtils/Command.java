package com.tencent.smtt.sdk.EmergencyUtils;

import org.json.JSONObject;

public class Command {
   private int id;
   private int cmd;
   private String extra;
   private long expiration;

   private Command() {
   }

   public int getId() {
      return this.id;
   }

   public int getCmd() {
      return this.cmd;
   }

   public String getExtra() {
      return this.extra;
   }

   public long getExpiration() {
      return this.expiration;
   }

   public static Command parseCommand(JSONObject var0) {
      Command cmd = null;
      if (var0 != null) {
         cmd = new Command();
         cmd.id = var0.optInt("id", -1);
         cmd.cmd = var0.optInt("cmd_id", -1);
         cmd.extra = var0.optString("ext_params", "");
         cmd.expiration = 1000L * var0.optLong("expiration", 0L);
      }

      return cmd;
   }

   public String toString() {
      return "[id=" + this.id + ", cmd=" + this.cmd + ", extra='" + this.extra + '\'' + ", expiration=" + FormatTimeHelper.formatTime(this.expiration) + ']';
   }

   public boolean isExpired() {
      long var1 = System.currentTimeMillis();
      return var1 > this.expiration;
   }
}
