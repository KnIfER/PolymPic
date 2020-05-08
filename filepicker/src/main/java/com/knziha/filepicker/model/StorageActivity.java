package com.knziha.filepicker.model;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import mp4meta.utils.CMN;

public class StorageActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 700);
        Toast.makeText(this, "请选择sd卡路径", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent duco) {
        super.onActivityResult(requestCode, resultCode, duco);
        switch (requestCode) {
            case 700:if(resultCode==RESULT_OK){
                CMN.Log("RESULT_OK");
                Uri treeUri = duco.getData();
                if(treeUri!=null) {
                    int GRANTFLAGS = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                    grantUriPermission(getPackageName(), treeUri, GRANTFLAGS);
                    getContentResolver().takePersistableUriPermission(treeUri, GRANTFLAGS);
                }
                break;
            }
        }
    }
}
