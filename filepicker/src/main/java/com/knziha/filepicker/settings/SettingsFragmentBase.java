package com.knziha.filepicker.settings;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.DialogPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.filepicker.R;


public abstract class SettingsFragmentBase extends PreferenceFragmentCompat implements View.OnClickListener ,
												Preference.OnPreferenceChangeListener {
	protected Toolbar toolbar;
	protected TextView text1;
	protected int resId=R.string.settings;

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

	}

	public static Preference init_switch_preference(SettingsFragmentBase preference, String key, Object val, Object dynamicSummary, Object dynamicTitle) {
		Preference perfer = preference.findPreference(key);
		if(perfer != null){
			if(val!=null) perfer.setDefaultValue(val);
			if(dynamicSummary instanceof Integer) perfer.setSummary((int)dynamicSummary);
			else if(dynamicSummary!=null)  perfer.setSummary(String.valueOf(dynamicSummary));
			if(dynamicTitle!=null)  perfer.setTitle(perfer.getTitle()+String.valueOf(dynamicTitle));
			perfer.setOnPreferenceChangeListener(preference);
		}
		return perfer;
	}

	public static Preference init_number_info_preference(SettingsFragmentBase preference, String key, int index, int infoArrayRes, Object dynamicTitle) {
		Preference perfer = preference.findPreference(key);
		if(perfer != null){
			((ListPreference)perfer).setValue(String.valueOf(index));
			perfer.setSummary(preference.getContext().getResources().getStringArray(infoArrayRes)[index]);
			if(dynamicTitle!=null)  perfer.setTitle(perfer.getTitle()+String.valueOf(dynamicTitle));
			perfer.setOnPreferenceChangeListener(preference);
		}
		return perfer;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		//CMN.recurseLogCascade(v);
		if(v != null) {
			RecyclerView lv = v.findViewById(R.id.recycler_view);
			//int pad = (int) (container.getContext().getResources().getDisplayMetrics().density*5);
			lv.setPadding(0, 0, 0, 0);

			int title = getArguments().getInt("title", 0);
			if(title!=0) resId=title;

			ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.preference_layout, container, false);
			toolbar=vg.findViewById(R.id.toolbar);
			text1=toolbar.findViewById(R.id.text1);
			text1.setText(resId);
			text1.setTextColor(GlobalOptions.isDark?Color.WHITE:Color.BLACK);

			if(GlobalOptions.isDark) {
				toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
				toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
			}else
				toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
			toolbar.setNavigationOnClickListener(this);
			if(v.getParent()!=null) ((ViewGroup)v.getParent()).removeView(v);
			vg.addView(v);
			return vg;
		}
		return v;
	}

	@Override
	public abstract void onClick(View view);
}