package com.knziha.filepicker.model;

import android.content.DialogInterface;
import android.view.View;

public class ViewDissmisser implements DialogInterface.OnDismissListener {
    View[] mViews;
    public ViewDissmisser(View...Views){
        mViews=Views;
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        if(mViews!=null)
        for (View vv:mViews){
            vv.setVisibility(View.VISIBLE);
        }
        mViews=null;
    }
}
