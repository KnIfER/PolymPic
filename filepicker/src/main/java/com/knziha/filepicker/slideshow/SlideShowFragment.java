package com.knziha.filepicker.slideshow;

import android.graphics.Point;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.alexvasilkov.gestures.Settings;
import com.alexvasilkov.gestures.State;
import com.alexvasilkov.gestures.animation.ViewPositionAnimator;
import com.alexvasilkov.gestures.commons.RecyclePagerAdapter;
import com.alexvasilkov.gestures.utils.GravityUtils;
import com.alexvasilkov.gestures.views.GestureImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.knziha.filepicker.R;
import com.knziha.filepicker.model.DepthPageTransformer2;
import com.knziha.filepicker.model.MyRequestListener;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.filepicker.view.FileListItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SlideShowFragment extends Fragment {
    List mMediaList;
    List empty = new ArrayList<>();
    String tally;
    int initialPosition=-1;

	@Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(CMNF.UniversalObject instanceof List) try {
            mMediaList = (List) CMNF.UniversalObject;
        } catch (Exception ignored) { }
        if(mMediaList==null) mMediaList=empty;
        tally = getArguments().getString(SlideShowActivity.TAYYL);
        if(CMNF.UniversalHashMap!=null && tally!=null){
            Object val = CMNF.UniversalHashMap.get(tally);
            if(val instanceof Integer) initialPosition = (int) val;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View main_pager_layout = inflater.inflate(R.layout.frag_slideshow, container, false);
        //main_pager_layout.setBackgroundColor(Color.BLUE);

        ViewPager2 viewpager = main_pager_layout.findViewById(R.id.viewpager);
        viewpager.setPageTransformer(new DepthPageTransformer2());
        viewpager.setOffscreenPageLimit(5);
        //viewpager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewpager.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ViewHolder holder = new ViewHolder(container);
                return holder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
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
                    ViewHolder vh = (ViewHolder) holder;
                    vh.itemView.setLayoutParams(new FrameLayout.LayoutParams(-1,-1));
                    //Priority priority = Priority.LOW;
                    int curr = viewpager.getCurrentItem();
                    Priority priority = (position>=curr-1&&position<=curr+1)?Priority.HIGH:Priority.LOW;
                    //CMNF.Log(priority==Priority.LOW?"Priority.LOW":"Priority.HIGH");
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

            @Override
            public int getItemCount() {
                return mMediaList.size();
            }
        });
        if(initialPosition!=-1) viewpager.setCurrentItem(initialPosition, false);
        viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Object item = mMediaList.get(position);
                String location=null;
                if(item instanceof File){
                    location=((File)item).getAbsolutePath();
                }else if(item instanceof FileListItem){
                    location=((FileListItem)item).getLocation();
                }
                if(tally!=null && CMNF.UniversalHashMap!=null){
                    CMNF.UniversalHashMap.put(tally, location);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });
        return main_pager_layout;
    }

    MyRequestListener<Drawable> myreqL2 = new MyRequestListener<>();
    class ViewHolder2 extends RecyclePagerAdapter.ViewHolder {
        GestureImageView image;
        ViewHolder2(ViewGroup container) {
            //super(Views.inflate(container, R.layout.layout_pager_item));
            super(new GestureImageView(container.getContext()));
            image= (GestureImageView) itemView;
        }
    }
    class ViewHolder extends RecyclerView.ViewHolder {
        GestureImageView image;
        ViewHolder(ViewGroup container) {
            //super(Views.inflate(container, R.layout.layout_pager_item));
            super(new FrameLayout(container.getContext()));
            image = new GestureImageView(container.getContext());
            ((FrameLayout)itemView).addView(image);
            image.getController().getSettings()
                    .setZoomEnabled(true)
                    .setMaxZoom(100).setDoubleTapZoom(10)
                    .setOverscrollDistance(getContext(), 12, 0)
                    .setOverzoomFactor(1.2f)
                    .setFitMethod(Settings.Fit.INSIDE).setExitEnabled(true).setExitType(Settings.ExitType.SCROLL)
                    .setGravity(Gravity.CENTER);
            //image.setBackgroundColor(Color.RED);
            //image.getController().enableScrollInViewPager();

            image.setTag(R.id.home, false);
        }
    }

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public class ZoomOnExit implements ViewPositionAnimator.PositionUpdateListener {

        private static final float MIN_ZOOM_FACTOR = 0.6f;

        private final Point tmpPivot = new Point();
        private final ViewPositionAnimator animator;

        public ZoomOnExit(ViewPositionAnimator animator, GestureImageView image) {
            this.animator = animator;
            view=image;
        }

        GestureImageView view;
        @Override
        public void onPositionUpdate(float position, boolean isLeaving) {

            if (view != null) {
                // Zoom-to-exit gesture will not work with this class, allowing only scroll-to-exit
                view.getController().getSettings().setExitType(Settings.ExitType.SCROLL);

                // Indirectly checking if we are in exit gesture
                float pos = view.getPositionAnimator().getPosition();
                float posTo = view.getPositionAnimator().getToPosition();
                boolean isExitGesture = pos < 1f && State.equals(pos, posTo);

                if (isExitGesture) {
                    // Calculating new zoom level
                    State state = view.getController().getState();
                    float minZoom = view.getController().getStateController().getMinZoom(state);
                    float zoom = minZoom * (MIN_ZOOM_FACTOR + pos * (1f - MIN_ZOOM_FACTOR));

                    // Calculating pivot point
                    GravityUtils.getDefaultPivot(view.getController().getSettings(), tmpPivot);

                    // Applying new zoom level
                    state.zoomTo(zoom, tmpPivot.x, tmpPivot.y);
                    view.getController().updateState();
                }
            }
        }
    }

}
