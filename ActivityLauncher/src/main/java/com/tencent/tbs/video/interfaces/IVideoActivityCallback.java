package com.tencent.tbs.video.interfaces;

import android.content.Intent;

public interface IVideoActivityCallback {
   void setUserStateChangedListener(IUserStateChangedListener var1);

   void onActivityResult(int requestCode, int resultCode, Intent data);
}
