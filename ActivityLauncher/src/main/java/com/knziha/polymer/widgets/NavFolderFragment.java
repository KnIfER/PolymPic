package com.knziha.polymer.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuPopup;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.knziha.polymer.PDocViewerActivity;
import com.knziha.polymer.R;
import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.databinding.BookmarksBinding;
import com.knziha.polymer.databinding.NavFolderViewBinding;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.pdviewer.bookmarks.AnnotListFragment;
import com.knziha.polymer.pdviewer.bookmarks.BookMarkFragment;
import com.knziha.polymer.pdviewer.bookmarks.FragAdapter;

import java.util.ArrayList;

public class NavFolderFragment extends DialogFragment implements Toolbar.OnMenuItemClickListener {
	private NavFolderViewBinding folderView;
	public NavigationHomeAdapter foldViewAdapter;
	public DisplayMetrics dm;
	
	public int width=-1,height=-2,mMaxH=-1;
	private ArrayList<Fragment> fragments = new ArrayList<>(6);
	private int lastUpdatedDoc;
	private MenuBuilder AllMenus;
	private MenuBuilder ActMenu;
	
	int lastW;
	int lastH;
	
	boolean viewBound=false;
	
	@Override
	public void onConfigurationChanged(@NonNull Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		resizeLayout(true);
	}
	
	public void resizeLayout(boolean refresh) {
		if (dm==null) {
			return;
		}
		int w = dm.widthPixels, h=dm.heightPixels;
		if(w!=lastW||h!=lastH) {
			//CMN.Log("w*h", dm.widthPixels, dm.heightPixels);
			width=(int) Math.min(dm.widthPixels-2*28*dm.density, GlobalOptions.density*480);
			if(dm.heightPixels>dm.widthPixels) {
				mMaxH=(int) (dm.heightPixels-2*48*dm.density);
			} else {
				mMaxH=-1;
			}
			lastW = w;
			lastH = h;
			if(refresh) {
				resize();
			}
		}
	}
	
	private void resize() {
		//if(false)
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
						folderView.root.mMaxHeight=mMaxH;
						window.setLayout(width,height);
					}
				}
				getDialog().setCanceledOnTouchOutside(true);
			}
		}
	}
	
	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(folderView ==null) {
			GlobalOptions.shouldRecordMenuView = true;
			folderView = NavFolderViewBinding.inflate(getLayoutInflater(), null, false);
			Toolbar mToolbar = folderView.toolbar;
			mToolbar.setOnMenuItemClickListener(this);
			
			mToolbar.inflateMenu(R.menu.bookmark_tools);
			AllMenus = (MenuBuilder) mToolbar.getMenu();
			ActMenu = AllMenus;
			if (foldViewAdapter!=null) {
				foldViewAdapter.root = folderView.root;
			}
		} else {
			Utils.removeIfParentBeOrNotBe(folderView.root, null, false);
		}
		return folderView.root;
	}
	
	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		CMN.Log("onAttach");
	}
	
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		dm = ((Toastable_Activity)getActivity()).dm;
		CMN.Log("onActivityCreated", CMN.id(this));
		if(!viewBound) {
			foldViewAdapter.SetRecyclerView(folderView.recyclerView);
			viewBound = true;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		resize();
	}
	
	@SuppressLint("NonConstantResourceId")
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		PDocViewerActivity a = (PDocViewerActivity) getActivity();
		if(a==null) {
			return false;
		}
		int id = item.getItemId();
		MenuItemImpl menuItem = item instanceof MenuItemImpl?(MenuItemImpl)item:null;
		boolean isLongClicked= menuItem!=null && menuItem.isLongClicked;
		boolean ret = !isLongClicked;
		boolean closeMenu=ret;
		switch (id) {
			case R.id.bm_expand: {
				if(!isLongClicked) {
					View anchorView = menuItem.actionView;
					if(anchorView!=null) {
						MenuBuilder menuBuilder = new MenuBuilder(a);
						MenuInflater inflater = a.getMenuInflater();
						inflater.inflate(R.menu.bookmark_tools_expand, menuBuilder);
						menuBuilder.setCallback(new MenuBuilder.Callback() {
							@Override
							public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
								MenuBuilder tmpMenu = ActMenu;
								ActMenu = menu;
								boolean ret = onMenuItemClick(item);
								ActMenu = tmpMenu;
								return ret;
							}
							@Override public void onMenuModeChange(MenuBuilder menu) { }
						});
						MenuPopup m_popup = new MenuPopupHelper(a, menuBuilder, anchorView).getPopup();
						m_popup.show();
					}
				}
			} break;
			case R.id.bm_exp_all:
			case R.id.bm_csp_all: {
			
			} break;
		}
		if(closeMenu)
			closeIfNoActionView(menuItem);
		return ret;
	}
	
	void closeIfNoActionView(MenuItemImpl mi) {
		if(mi!=null && !mi.isActionButton()) ActMenu.close();
	}
}
