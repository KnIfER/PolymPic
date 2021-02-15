package com.tencent.smtt.export.external.interfaces;

import android.net.Uri;

public interface PermissionRequest {
   String RESOURCE_AUDIO_CAPTURE = "android.webkit.resource.AUDIO_CAPTURE";
   String RESOURCE_MIDI_SYSEX = "android.webkit.resource.MIDI_SYSEX";
   String RESOURCE_PROTECTED_MEDIA_ID = "android.webkit.resource.PROTECTED_MEDIA_ID";
   String RESOURCE_VIDEO_CAPTURE = "android.webkit.resource.VIDEO_CAPTURE";

   Uri getOrigin();

   String[] getResources();

   void grant(String[] var1);

   void deny();
}
