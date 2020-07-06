package com.KnaIvER.polymer.slideshow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.KnaIvER.polymer.R;
import com.KnaIvER.polymer.Toastable_Activity;
import com.KnaIvER.polymer.Utils.CMN;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ViewpagerDebugActivity extends Toastable_Activity {
	//"/storage/2486-F9E1/photo3.jpg", new File("/storage/2486-F9E1/img/CCITT_1.TIF"),
	//new File("/storage/2486-F9E1/sample.png"),
	private static File[] imageUrls
	=new File[]{new File("/sdcard/myFolder/huge/千里江山图.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon2.png"),new File("/sdcard/myFolder/huge/themoon.jpg")
			,new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg"),new File("/sdcard/myFolder/huge/themoon.jpg")};
	
	ViewPager viewPager;
	LinkedList<PageHolder> mViewCache = new LinkedList<>();
	
	@Override
	protected void onPause() {
		super.onPause();
		System.gc();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_pager);
		//imageUrls = new File("/storage/2486-F9E1/img").listFiles();
		viewPager = findViewById(R.id.view_pager);
		
		viewPager.setPageMargin(0);
		
		
		viewPager.setAdapter(new PagerAdapter() {
			@Override
			public int getCount() {
				return imageUrls.length;
			}

			@Override
			public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
				return view == object;
			}

			@NonNull
			@Override
			public Object instantiateItem(@NonNull ViewGroup container, int position) {
				PageHolder vh;
				if (mViewCache.size() > 0) {
					vh = mViewCache.removeFirst();
				} else {
					vh = new PageHolder(LayoutInflater.from(container.getContext()).inflate(R.layout.view_pager_page, container, false));
					//pv.setMaxTileSize(1024);
					vh.subview.dm = opt.dm;
					vh.subview.view_pager_toguard = viewPager;
				}
				if (vh.itemView.getParent() != null)
					((ViewGroup) vh.itemView.getParent()).removeView(vh.itemView);
				container.addView(vh.itemView);
				vh.position=position;
				File key=imageUrls[position];
				vh.path = key.getPath();
				vh.pv.setTranslationX(0);
				vh.pv.setTranslationY(0);
				vh.pv.setScaleX(1);
				vh.pv.setScaleY(1);
				vh.pv.setRotation(0);
				vh.pv.setTag(R.id.home, false);
				try {

					vh.subview.recycle();
					vh.subview.dm = container.getContext().getResources().getDisplayMetrics();
					RequestBuilder<Bitmap> incanOpen = Glide.with(ViewpagerDebugActivity.this)
							.asBitmap()
							.load(key)
							//.override(getResources().getDisplayMetrics().widthPixels, Target.SIZE_ORIGINAL)
							.fitCenter();
					
					incanOpen.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
							.diskCacheStrategy(DiskCacheStrategy.NONE)
							.listener(new RequestListener<Bitmap>() {
								@Override
								public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
									ImageView iv = ((ImageViewTarget<?>) target).getView();
									iv.setImageDrawable(getResources().getDrawable(R.drawable.sky_background));
									getDimensionsAndOpenQuickScale(null, iv);
									return true;
								}

								@Override
								public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
									ImageView iv = ((ImageViewTarget<?>) target).getView();
									if(iv.getTag(R.id.home)==null){//true ||
										return true;
									}
									getDimensionsAndOpenQuickScale(resource, iv);
									return false;
								}
							})
							.into(vh.pv);
				}catch (Exception e){
					CMN.Log(e);
				}
				return vh.itemView;
			}


			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView((View) object);
				mViewCache.add((PageHolder) ((View) object).getTag());
			}
		});
		
		viewPager.setCurrentItem(2);
		
//		viewPager.setAdapter(new RecyclerView.Adapter() {
//			 @NonNull
//			 @Override
//			 public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
//				 PageHolder vh = new PageHolder(LayoutInflater.from(container.getContext()).inflate(R.layout.view_pager_page, container, false));
//				 //pv.setMaxTileSize(1024);
//				 vh.subview.view_pager_toguard = viewPager;
//				 return vh;
//			 }
//
//			 @Override
//			 public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//				 PageHolder vh = (PageHolder) holder;
//				 vh.position=position;
//				 String key=imageUrls[position];
//				 try {
//					 Point dimension = getImageResolution(key);
//					 vh.subview.dm = getResources().getDisplayMetrics();
//					 //vh.subview.orientation = rotation;
//					 vh.subview.setDimensions(dimension, -1);
//					 vh.subview.isProxy=true;
//					 vh.subview.scale = vh.subview.getMinScale();
//					 vh.subview.maxScale = Math.max(vh.subview.scale*2,10.0f);
//					 vh.subview.preDraw2(new PointF(dimension.x*1.0f/2, dimension.y*1.0f/2),vh.subview.scale);
//					 vh.subview.imgsrc=key;
//					 vh.subview.setVisibility(View.VISIBLE);
//
//					 Glide.with(ViewpagerDebugActivity.this)
//							 .asBitmap()
//							 .load(new File(key))
//							 .override(getResources().getDisplayMetrics().widthPixels, Target.SIZE_ORIGINAL)
//							 .fitCenter()
//							 .diskCacheStrategy(DiskCacheStrategy.NONE)
//							 .into(vh.pv);
//				 }catch (Exception e){
//					 CMN.Log(e);
//				 }
//			 }
//
//			 @Override
//			 public long getItemId(int position) {
//				 return 0;
//			 }
//
//			 @Override
//			 public int getItemCount() {
//				 return imageUrls.length;
//			 }
//		});
		
	}
	
	private void getDimensionsAndOpenQuickScale(Bitmap resource, ImageView iv) {
		PageHolder vh = (PageHolder) iv.getTag();
		int[] dimension = null;
		SparseArray<int[]> bitmapDimensions = Glide.bitmapDimensions;
		if(resource==null){
			iv.setVisibility(View.GONE);
		} else {
			iv.setVisibility(View.VISIBLE);
			if(bitmapDimensions!=null){
				dimension=bitmapDimensions.get(System.identityHashCode(resource));
				CMN.Log("获得了", dimension);
			}
		}
		if(dimension==null){
			dimension = getImageResolution(vh.path);
			CMN.Log("手动获取", dimension);
		}
		//vh.subview.orientation = rotation;
		vh.subview.setProxy(dimension, -1, resource, vh.path);
		vh.subview.ImgSrc =vh.path;
		//vh.pv.setVisibility(View.VISIBLE);
		//if(false)
		if(dimension[0]*dimension[1]>2096*2096){
			vh.subview.setMaxTileSize(true? Integer.MAX_VALUE : 4096);
			vh.subview.setMaxTileSize(1080);
			float averageDpi = (dm.xdpi + dm.ydpi)/2;
			vh.subview.setMinimumTileDpi(averageDpi>400?280:(averageDpi>300?220:160));
			vh.subview.setMinimumTileDpi((int) (averageDpi/2));
			vh.subview.setMinimumTileDpi(280);
			CMN.Log(averageDpi, vh.subview.minimumTileDpi, "minimumTileDpi");
			vh.subview.setImage(ImageSource.uri(Uri.fromFile(new File(vh.path))));
		}
		
		vh.subview.resetScaleAndCenter();
	}
	
	static class PageHolder
			//extends RecyclerView.ViewHolder
	{
		public String path;
		ViewGroup itemView;
		int position;
		ImageView pv;
		TilesGridLayout pg;
		SubsamplingScaleImageView subview;
		PageHolder(View v){
			//super(v);
			itemView = (ViewGroup) v;
			pv = v.findViewById(R.id.imageView);
			pg = v.findViewById(R.id.grid);
			subview = v.findViewById(R.id.subView);
			itemView.setTag(this);
			pv.setTag(this);
			//pv.setColorFilter(SubsamplingScaleImageView.sample_fileter);
			subview.view_to_guard = pv;
			subview.view_to_paint = pg;
		}
	}
	
	int[] getImageResolution(String path){
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		CMN.Log(options.outWidth, options.outHeight);
		return new int[]{options.outWidth, options.outHeight};
	}
	
	static class FragAdapter extends FragmentPagerAdapter {
		private List<Fragment> mFragments;
		public FragAdapter(FragmentManager fm,List<Fragment> fragments) {
			super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
			mFragments=fragments;
		}
		
		@NonNull
		@Override
		public Fragment getItem(int arg0) {
			return mFragments.get(arg0);
		}
		
		@Override
		public int getCount() {
			return mFragments.size();
		}
		
	}
}
