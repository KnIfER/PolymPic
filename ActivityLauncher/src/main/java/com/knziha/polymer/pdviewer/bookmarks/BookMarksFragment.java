package com.knziha.polymer.pdviewer.bookmarks;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.databinding.BookmarksBinding;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.widgets.Utils;

import java.util.ArrayList;

public class BookMarksFragment extends DialogFragment {
	private BookmarksBinding bmView;
	private BookMarkFragment f1;
	
	public int width=-1,height=-1,mMaxH=-1;
	private ArrayList<Fragment> fragments = new ArrayList<>(6);
	private int lastUpdatedDoc;
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(bmView==null) {
			bmView = BookmarksBinding.inflate(getLayoutInflater(), null, false);
			fragments.add(f1 = new BookMarkFragment());
			
			ViewPager viewPager = bmView.viewpager;
			TabLayout mTabLayout = bmView.mTabLayout;
			
			viewPager.setAdapter(new FragAdapter(getChildFragmentManager(), fragments));
			
			String[] tabTitle = {"目录"};
			for (String s : tabTitle) {
				mTabLayout.addTab(mTabLayout.newTab().setText(s));
			}
			mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
				@Override
				public void onTabSelected(TabLayout.Tab tab) {
					bmView.viewpager.setCurrentItem(tab.getPosition());
				}
				@Override public void onTabUnselected(TabLayout.Tab tab) {}
				@Override public void onTabReselected(TabLayout.Tab tab) {}
			});
			
			mTabLayout.setSelectedTabIndicatorColor(ColorUtils.blendARGB(CMN.MainBackground, Color.BLACK, 0.28f));
			
			mTabLayout.setSelectedTabIndicatorHeight((int) (3.8* GlobalOptions.density));
			
			viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout) {
				@Override public void onPageSelected(int page) {
					Fragment fI = fragments.get(page);
					//bmView.viewpager.setOffscreenPageLimit(Math.max(bmView.viewpager.getOffscreenPageLimit(), Math.max(1+page, 1)));
					super.onPageSelected(page);
				}
			});
			
			viewPager.setCurrentItem(0);
			
			viewPager.setOffscreenPageLimit(1);
		} else {
			Utils.removeIfParentBeOrNotBe(bmView.root, null, false);
			//@Hide(1)
			bmView.viewpager.setAdapter(new FragAdapter(getChildFragmentManager(), fragments));
		}
		return bmView.root;
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		CMN.Log("onAttach");
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CMN.Log("onActivityCreated", CMN.id(this));
		PDocViewerActivity a = (PDocViewerActivity) getActivity();
		if(a!=null) {
			PDocument doc = a.currentViewer.pdoc;
			if(lastUpdatedDoc!=CMN.id(doc)) {
				refreshToc();
			}
		}
	}
	
	private void refreshToc() {
		PDocViewerActivity a = (PDocViewerActivity) getActivity();
		if(a!=null) {
			PDocument doc = a.currentViewer.pdoc;
			doc.prepareBookMarks();
			f1.refresh(doc);
			lastUpdatedDoc = CMN.id(doc);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(width!=-1 || height!=-1) {
			if(getDialog()!=null) {
				Window window = getDialog().getWindow();
				if(window!= null) {
					WindowManager.LayoutParams  attr = window.getAttributes();
					if(attr.width!=width || attr.height!=height) {
						//CMN.Log("onResume_");
						window.setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND, WindowManager.LayoutParams.FLAG_DIM_BEHIND);
						window.setDimAmount(0.1f);
						window.setBackgroundDrawableResource(R.drawable.popup_shadow_l);
						bmView.root.mMaxHeight=mMaxH;
						window.setLayout(width,height);
					}
				}
				getDialog().setCanceledOnTouchOutside(true);
			}
		}
	}
	
}
