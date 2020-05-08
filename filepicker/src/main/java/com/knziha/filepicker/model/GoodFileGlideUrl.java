package com.knziha.filepicker.model;

import com.bumptech.glide.load.model.GlideUrl;

import java.io.File;

public class GoodFileGlideUrl extends GlideUrl {

    private String mUrl;

    public GoodFileGlideUrl(String url) {
        super(url);
        mUrl = url;
    }

    @Override
    public String getCacheKey() {
        String name = new File(mUrl).getName();
        int suffix_idx;
        if((suffix_idx=name.lastIndexOf("."))!=-1) name=name.substring(0,suffix_idx);
        return name;
    }

}