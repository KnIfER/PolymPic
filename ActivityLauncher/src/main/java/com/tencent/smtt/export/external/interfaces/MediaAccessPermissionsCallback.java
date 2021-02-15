package com.tencent.smtt.export.external.interfaces;

public interface MediaAccessPermissionsCallback {
   long ALLOW_VIDEO_CAPTURE = 2L;
   long ALLOW_AUDIO_CAPTURE = 4L;
   long BITMASK_RESOURCE_VIDEO_CAPTURE = 2L;
   long BITMASK_RESOURCE_AUDIO_CAPTURE = 4L;

   void invoke(String var1, long var2, boolean var4);
}
