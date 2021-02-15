package com.tencent.smtt.sdk.EmergencyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormatTimeHelper {
   public static String formatTime(long time) {
      return String.format(Locale.getDefault(), "%d(%s)"
			  , time
			  , (new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())).format(new Date(time)));
   }
}
