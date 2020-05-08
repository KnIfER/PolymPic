package com.knziha.filepicker.slideshow;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.knziha.filepicker.R;
import com.knziha.filepicker.utils.CMNF;

public class SlideShowActivity extends AppCompatActivity {
    public static final String TAYYL = "vptally";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		ClickDismissFrameLayout fl = new ClickDismissFrameLayout(getApplicationContext());
        fl.setId(R.id.home);
        setContentView(fl);
        setStatusBarColor(getWindow());
		fl.setOnClickListener(view ->
				{
					finish();
				}
				);
        getWindow().setBackgroundDrawable(null);
        if(CMNF.FirstFlag!=null && (CMNF.FirstFlag & 0x10000)!=0){
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        Fragment frag = new SlideShowFragment();
        Bundle args = new Bundle();
        args.putString(TAYYL, getIntent().getStringExtra(TAYYL));
        frag.setArguments(args);
        FragmentManager mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.home, frag)
                .commit();
    }


    public static void setStatusBarColor(Window window){
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        if(Build.VERSION.SDK_INT>=21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

}
