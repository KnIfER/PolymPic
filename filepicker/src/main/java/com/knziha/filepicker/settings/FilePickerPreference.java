/*
 * Copyright (C) 2016 Angad Singh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.knziha.filepicker.settings;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Window;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.knziha.filepicker.R;
import com.knziha.filepicker.model.DialogSelectionListener;
import com.knziha.filepicker.model.DialogConfigs;
import com.knziha.filepicker.model.DialogProperties;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.filepicker.view.FilePickerDialog;

import java.io.File;


public class FilePickerPreference extends Preference implements
        DialogSelectionListener,
        Preference.OnPreferenceClickListener {
    private FilePickerDialog mDialog;
    private DialogProperties properties;
    private String titleText=null;
    private String mPath=null;
	private boolean initialized;

	public FilePickerPreference(Context context) {
        super(context);
		initProperties(null);
    }

    public FilePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initProperties(attrs);
    }

    public FilePickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initProperties(attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return super.onGetDefaultValue(a, index);
    }

	@Override
	public void setDefaultValue(Object defaultValue) {
		super.setDefaultValue(defaultValue);
		if(!initialized) onSetInitialValue(defaultValue);
	}

	@Override
    protected void onSetInitialValue(Object defaultValue) {
		mPath = getPersistedString((String) defaultValue);
		if(mPath!=null){
			properties.offset = new File(mPath);
			setSummary(mPath);
			initialized=true;
		}
    }

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder) {
		super.onBindViewHolder(holder);

	}

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (mDialog == null || !mDialog.isShowing()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.dialogBundle = mDialog.onSaveInstanceState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        showDialog(myState.dialogBundle);
    }

    private void showDialog(Bundle state) {
        mDialog = new FilePickerDialog((Activity) getContext());
		mDialog.setProperties(properties);
        mDialog.setDialogSelectionListener(this);
        if (state != null) {
            mDialog.onRestoreInstanceState(state);
        }
        mDialog.setTitle(titleText);
        mDialog.show();
    }

    @Override
    public void onSelectedFilePaths(String[] files,String p) {
        if (callChangeListener(mPath = files[0]) && isPersistent()) {
            persistString(mPath);
        }
		setSummary(mPath);
    }

    @Override
    public void onEnterSlideShow(Window win, int delay) {

    }

    @Override
    public void onExitSlideShow() {

    }

    @Override
    public Activity getDialogActivity() {
        return null;
    }

	@Override
	public void onDismiss() {

	}

	@Override
    public boolean onPreferenceClick(Preference preference) {
        showDialog(null);
        return false;
    }

    private static class SavedState extends BaseSavedState {
        Bundle dialogBundle;

        public SavedState(Parcel source) {
            super(source);
            dialogBundle = source.readBundle(getClass().getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Creator<SavedState> CREATOR =
        new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    private void initProperties(AttributeSet attrs) {
		properties = new DialogProperties();
		setOnPreferenceClickListener(this);
		properties.root=new File("/");
		properties.error_dir=getContext().getExternalFilesDir("");
		properties.opt_dir=new File(getContext().getExternalFilesDir(""), "/PLOD/video/favorite_dirs/");
		if(attrs==null) return;
        TypedArray tarr=getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.FilePickerPreference,0,0);
        final int N = tarr.getIndexCount();
        for (int i = 0; i < N; ++i)
        {   int attr = tarr.getIndex(i);
            if (attr == R.styleable.FilePickerPreference_selection_mode) {
                properties.selection_mode=tarr.getInteger(R.styleable.FilePickerPreference_selection_mode, DialogConfigs.SINGLE_MODE);
            }
            else if (attr == R.styleable.FilePickerPreference_selection_type) {
                properties.selection_type=tarr.getInteger(R.styleable.FilePickerPreference_selection_type,DialogConfigs.FILE_SELECT);
            }
            else if (attr == R.styleable.FilePickerPreference_root_dir) {
				properties.root=new File(tarr.getString(R.styleable.FilePickerPreference_root_dir));
            }
            else if (attr == R.styleable.FilePickerPreference_error_dir) {
				properties.error_dir=new File(tarr.getString(R.styleable.FilePickerPreference_error_dir));
            }
            else if (attr == R.styleable.FilePickerPreference_title_text) {
                titleText=tarr.getString(R.styleable.FilePickerPreference_title_text);
            }
        }
        tarr.recycle();
    }
}
