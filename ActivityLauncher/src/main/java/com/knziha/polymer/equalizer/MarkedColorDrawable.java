package com.knziha.polymer.equalizer;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

public class MarkedColorDrawable extends ColorDrawable implements WrappedBackGroundColor {
    public MarkedColorDrawable(int color) {
        super(color);
    }

    @Override
    public Drawable unwrap() {
        return null;//new ColorDrawable(getColor());
    }
}
