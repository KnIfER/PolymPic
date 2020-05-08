package com.knziha.filepicker.slideshow;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.commons.DepthPageTransformer;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.knziha.filepicker.R;
import com.knziha.filepicker.model.MyRequestListener;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.filepicker.view.FileListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SlideShowFragmentLegacy extends Fragment {
    List mMediaList;
    List empty = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(CMNF.UniversalObject instanceof List) try {
            mMediaList = (List) CMNF.UniversalObject;
        } catch (Exception ignored) { }
        if(mMediaList==null) mMediaList=empty;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View main_pager_layout = new ViewPager(container.getContext());
        //main_pager_layout.setBackgroundColor(Color.BLUE);

        ViewPager viewpager = (ViewPager) main_pager_layout;
        viewpager.setPageTransformer(false, new DepthPageTransformer(), View.LAYER_TYPE_NONE);
        viewpager.setAdapter(new RecyclePagerAdapter<ViewHolder>() {
            @Override
            public int getCount() {
                return mMediaList.size();
            }

            @Override
            public SlideShowFragmentLegacy.ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {
                SlideShowFragmentLegacy.ViewHolder holder = new SlideShowFragmentLegacy.ViewHolder(container);
                holder.image.getController().enableScrollInViewPager(viewpager);
                return holder;
            }

            @Override
            public void onBindViewHolder(@NonNull SlideShowFragmentLegacy.ViewHolder holder, int position) {
                Object item = mMediaList.get(position);
                long time=-1; String location=null;
                if(item instanceof File){
                    time=((File)item).lastModified();
                    location=((File)item).getAbsolutePath();
                }else if(item instanceof FileListItem){
                    time=((FileListItem)item).getTime();
                    location=((FileListItem)item).getLocation();
                }
                if(time!=-1){
                    SlideShowFragmentLegacy.ViewHolder vh = (SlideShowFragmentLegacy.ViewHolder) holder;
                    //vh.itemView.setLayoutParams(new ViewPager.LayoutParams());
                    Priority priority = Priority.LOW;
                    RequestOptions options = new RequestOptions()
                            .signature(new ObjectKey(time))//+"|"+item.size
                            .format(DecodeFormat.PREFER_ARGB_8888)//DecodeFormat.PREFER_ARGB_8888
                            .priority(priority)
                            .skipMemoryCache(false)
                            //.diskCacheStrategy(DiskCacheStrategy.NONE)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .fitCenter()
                            .override(360, Target.SIZE_ORIGINAL);
                    Glide.with(getActivity().getApplicationContext())
                            .load(location)
                            .apply(options)
                            .format(DecodeFormat.PREFER_RGB_565)
                            //.listener(myreqL2.setCrop(true))
                            .into(vh.image)
                    ;
                }
            }
        });
        //viewpager.addOnPageChangeListener();
        return main_pager_layout;
    }

    MyRequestListener<Drawable> myreqL2 = new MyRequestListener<>();
    class ViewHolder extends RecyclePagerAdapter.ViewHolder {
        GestureImageView image;
        ViewHolder(ViewGroup container) {
            //super(Views.inflate(container, R.layout.layout_pager_item));
            super(new FrameLayout(container.getContext()));
            image = new GestureImageView(container.getContext());
            ((FrameLayout)itemView).addView(image);
            image.getController().getSettings()
                    .setZoomEnabled(true)
                    .setMaxZoom(100).setDoubleTapZoom(10)
                    .setFitMethod(Settings.Fit.INSIDE)
                    .setGravity(Gravity.CENTER);
            //image.setBackgroundColor(Color.RED);
            //image.getController().enableScrollInViewPager();
            image.setTag(R.id.home, false);
        }
    }
}
