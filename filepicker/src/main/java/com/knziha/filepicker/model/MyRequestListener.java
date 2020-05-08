package com.knziha.filepicker.model;

import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;

public class MyRequestListener<R> implements RequestListener<R> {
    public boolean crop;
    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
        ImageView view = ((ImageViewTarget<?>) target).getView();
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        //CMNF.Log("???");
        //Glide.with(coreInstance).load(model).apply(new RequestOptions().placeholder(R.drawable.blank_photo)).into(target);
        return false;
    }
    @Override
    public boolean onResourceReady(R resource, Object model, Target<R> target, DataSource dataSource, boolean isFirstResource) {
        ImageView medium_thumbnail = ((ImageViewTarget<?>) target).getView();
        if(medium_thumbnail.getTag(com.knziha.filepicker.R.id.home)==null){//true ||
            medium_thumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            return true;
        }
        ImageView.ScaleType targetScale =  crop? ImageView.ScaleType.CENTER_CROP: ImageView.ScaleType.FIT_CENTER;
        if(medium_thumbnail.getScaleType()!=targetScale)
            medium_thumbnail.setScaleType(targetScale);
        return false;
    }
    
    public MyRequestListener setCrop(boolean crop_) {
        crop = crop_;
        return this;
    }
}
