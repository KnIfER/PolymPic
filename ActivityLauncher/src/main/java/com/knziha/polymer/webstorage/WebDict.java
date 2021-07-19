package com.knziha.polymer.webstorage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.databinding.SearchEnginesItemBinding;
import com.knziha.polymer.widgets.SuperItemListener;

public class WebDict {
	public String url;
	public String name;
	public boolean isEditing;
	
	public WebDict(String url, String name) {
		this.url = url;
		this.name = name;
	}
	
	public void expandEditView(SearchEnginesItemBinding itemData, SuperItemListener onClickListener, boolean expand, boolean animate) {
		if(animate) {
			isEditing = expand;
		}
		ViewGroup inflated = (ViewGroup) itemData.tick.getTag();
		
		if(expand && inflated==null) {
			ViewStub stub = itemData.moreOptions.getViewStub();
			if(stub!=null) {
				stub.setOnInflateListener(null);
				inflated = (ViewGroup) stub.inflate();
				//Utils.setOnClickListenersOneDepth(inflated, onClickListener, 3, null);
				itemData.tick.setTag(inflated);
			}
		}
		CMN.Log("expandEditView…… ?? ", animate, expand, name, inflated);
		if(inflated!=null)
		CMN.Log("expandEditView…… ?? ", inflated.getVisibility()==View.VISIBLE, expand);
		if(inflated!=null&&((inflated.getVisibility()==View.VISIBLE^expand))) {
			CMN.Log("expandEditView……", name);
			expandViewInner(inflated, itemData.getRoot(), expand, animate);
		}
	}
	
	private static void expandViewInner(ViewGroup inflated, View root, boolean expand, boolean animate) {
		if(animate) {
			int width = root.getWidth();
			if(expand) {
				inflated.setAlpha(0);
				inflated.setTranslationX(width/3);
				inflated.animate().setListener(null).alpha(1)
						.translationX(0);
			} else {
				ViewGroup finalInflated = inflated;
				inflated.animate().setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						finalInflated.setVisibility(View.GONE);
					}
				}).alpha(0).translationX(width/3);
			}
		}
		if(expand||!animate) {
			inflated.setVisibility(expand?View.VISIBLE:View.GONE);
		}
	}
	
	public static void expandSyncView(View storageV, SuperItemListener onClickListener, boolean expand, boolean animate) {
		ViewGroup inflated = (ViewGroup) storageV.getTag();
		if(inflated==null&&expand) {
			ViewStub stub = storageV.findViewById(R.id.more_options);
			if(stub!=null) {
				stub.setOnInflateListener(null);
				inflated = (ViewGroup) stub.inflate();
				//Utils.setOnClickListenersOneDepth(inflated, onClickListener, 3, null);
				storageV.setTag(inflated);
			}
		}
		if(inflated!=null&&inflated.getVisibility()==View.VISIBLE^expand) {
			//CMN.Log("expandEditView……", name);
			expandViewInner(inflated, storageV, expand, animate);
		}
	}
}
