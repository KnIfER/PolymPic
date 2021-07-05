package com.knziha.polymer.preferences;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.knziha.polymer.R;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;
import com.knziha.polymer.widgets.Utils;

import org.apache.commons.lang3.ArrayUtils;

public class QuickBrowserSettingsPanel_V1 extends QuickBrowserSettingsPanel{
	ListView listView;
	final static int PanelsCnt = 7*3;
	View[] Panels;
	View bottomPadding;
	BaseAdapter adapter;
	public QuickBrowserSettingsPanel_V1(Context context, ViewGroup root, int bottomPaddding, Options opt) {
		super(context, root, bottomPaddding, opt);
	}
	
	@Override
	protected void init(Context context, ViewGroup root) {
		inflateUIData(context, root);
		bottomPadding = new View(context);
		bottomPadding.setLayoutParams(new LinearLayout.LayoutParams(-1, 0));
		Panels = new View[PanelsCnt];
		root = UIData.root;
		for (int i = 0, len=root.getChildCount(), idx=0; i < len && idx<PanelsCnt; i++) {
			View child = root.getChildAt(i);
			if (!(child instanceof ViewStub)) {
				Panels[idx++] = child;
				if ((idx-1)%3==0) {
					idx++;
				}
			}
		}
		CMN.Log("000"); CMN.Log(Panels);
		listView = new ListView(context, null, R.style.AppBaseTheme);
		listView.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
		listView.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
		listView.setDivider(null);
		
		listView.setAdapter(adapter = new BaseAdapter() {
			@Override
			public int getCount() {
				return PanelsCnt+1;
			}
			@Override
			public Object getItem(int position) {
				return null;
			}
			@Override
			public long getItemId(int position) {
				return 0;
			}
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if(position==Panels.length) {
					return bottomPadding;
				}
				View v = Panels[position];
				if (v==null) {
					Panels[position] = v = new RelativeLayout(a);
				}
				Utils.removeIfParentBeOrNotBe(v, parent, false);
				return v;
			}
		});
		super.init(context, root);
		CMN.Log("000"); CMN.Log(Panels);
		settingsLayout = listView;
	}
	
	@Override
	public void setBottomPadding(int padding) {
		bottomPadding.getLayoutParams().height = padding;
	}
	
	@Override
	protected void addPanelViewBelow(View settingsLayout, LinearLayout panelTitle) {
		//super.addPanelViewBelow(settingsLayout, panelTitle);
		if (ArrayUtils.indexOf(Panels, panelTitle)+1>0) {
			Panels[ArrayUtils.indexOf(Panels, panelTitle)+1]=settingsLayout;
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onClick(View v) {
		super.onClick(v);
		//listView.requestLayout();
		CMN.Log("111");
		CMN.Log(Panels);
	}
	
	@Override
	protected void setPanelVis(View settingsLayout, boolean show) {
		super.setPanelVis(settingsLayout, show);
		try {
			settingsLayout.getLayoutParams().height=show?-2:1;
			settingsLayout.requestLayout();
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
}
