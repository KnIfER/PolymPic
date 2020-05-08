package com.knziha.filepicker.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.SwitchPreference;

public class TwinkleSwitchPreference extends SwitchPreference {
	public TwinkleSwitchPreference(Context context) {
		this(context, null);
	}

	public TwinkleSwitchPreference(Context context, AttributeSet attrs) {
		this(context, attrs, TypedArrayUtils.getAttr(context,
				androidx.preference.R.attr.switchPreferenceStyle,
				android.R.attr.switchPreferenceStyle));
	}

	public TwinkleSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setDefaultValue(Object defaultValue) {
		super.setDefaultValue(defaultValue);
		if(!isPersistent())
			setChecked((Boolean) defaultValue);
	}
}