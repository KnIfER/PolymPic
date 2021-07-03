package com.knziha.polymer.equalizer;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import androidx.annotation.NonNull;

public class MarkedLayerDrawable extends LayerDrawable implements WrappedBackGroundColor{
    /**
     * Creates a new layer drawable with the list of specified layers.
     *
     * @param layers a list of drawables to use as layers in this new drawable,
     *               must be non-null
     */
    public MarkedLayerDrawable(@NonNull Drawable[] layers) {
        super(layers);
    }

    @Override
    public Drawable unwrap() {
        return getNumberOfLayers()>=0?getDrawable(0):null;
    }
}
