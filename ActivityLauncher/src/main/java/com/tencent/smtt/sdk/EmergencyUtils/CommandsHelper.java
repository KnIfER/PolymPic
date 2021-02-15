package com.tencent.smtt.sdk.EmergencyUtils;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommandsHelper {
   private int retCode;
   private long nextReqInterval;
   private List<Command> commands;

   private CommandsHelper() {
   }

   public static CommandsHelper feedNewCommands(String jsonText) {
      CommandsHelper commandsHelper = null;
      if (jsonText != null) {
         try {
            JSONObject json = new JSONObject(jsonText);
            commandsHelper = new CommandsHelper();
            commandsHelper.retCode = json.optInt("ret_code", -1);
            commandsHelper.nextReqInterval = json.optLong("next_req_interval", 1000L);
            JSONArray cmds = json.optJSONArray("cmds");
            if (cmds != null) {
               commandsHelper.commands = new ArrayList();

               for(int var4 = 0; var4 < cmds.length(); ++var4) {
				  json = cmds.optJSONObject(var4);
                  Command command = Command.parseCommand(json);
                  if (command != null) {
                     commandsHelper.commands.add(command);
                  }
               }
            }
         } catch (JSONException var7) {
            var7.printStackTrace();
         }
      }

      return commandsHelper;
   }

   public int getRetCode() {
      return this.retCode;
   }

   public long getNextReqInterval() {
      return this.nextReqInterval;
   }

   public List<Command> getCommands() {
      return this.commands;
   }
}
