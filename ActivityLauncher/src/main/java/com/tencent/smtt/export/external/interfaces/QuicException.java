package com.tencent.smtt.export.external.interfaces;

public abstract class QuicException extends NetworkException {
   protected QuicException(String var1, Throwable var2) {
      super(var1, var2);
   }

   public abstract int getQuicDetailedErrorCode();
}
