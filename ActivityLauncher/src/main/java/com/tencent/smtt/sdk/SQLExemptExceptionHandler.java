package com.tencent.smtt.sdk;

import android.database.sqlite.SQLiteException;

import com.knziha.polymer.Utils.CMN;

import java.lang.Thread.UncaughtExceptionHandler;

public class SQLExemptExceptionHandler implements UncaughtExceptionHandler {
   public void uncaughtException(Thread thread, Throwable throwable) {
      if (!(throwable instanceof SQLiteException)) {
         throw new RuntimeException(throwable);
      }
   }
}
