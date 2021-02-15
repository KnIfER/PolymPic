package com.tencent.smtt.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.tbs.video.interfaces.IVideoActivityCallback;

public class VideoActivity extends Activity {
   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      super.requestWindowFeature(1);
      super.getWindow().setFormat(-3);
      Intent intent = super.getIntent();
      Bundle bundle = intent != null ? intent.getBundleExtra("extraData") : null;
      if (bundle != null) {
         bundle.putInt("callMode", 1);
         VideoActivityHelper.getInstance(super.getApplicationContext()).play((String)null, bundle, (IVideoActivityCallback)null);
      }

   }

   protected void onResume() {
      super.onResume();
      VideoActivityHelper.getInstance(this).a(this, 2);
   }

   protected void onStop() {
      super.onStop();
      VideoActivityHelper.getInstance(this).a(this, 1);
   }

   protected void onPause() {
      super.onPause();
      VideoActivityHelper.getInstance(this).a(this, 3);
   }

   protected void onDestroy() {
      super.onDestroy();
      VideoActivityHelper.getInstance(this).a(this, 4);
   }

   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      VideoActivityHelper.getInstance(this).onActivityResult(requestCode, resultCode, data);
   }
}
