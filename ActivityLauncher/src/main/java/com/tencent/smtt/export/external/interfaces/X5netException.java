package com.tencent.smtt.export.external.interfaces;

import java.io.IOException;

public abstract class X5netException extends IOException {
   protected X5netException(String var1, Throwable var2) {
      super(var1, var2);
   }
}
