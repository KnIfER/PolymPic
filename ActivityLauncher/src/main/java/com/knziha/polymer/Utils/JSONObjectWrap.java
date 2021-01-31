package com.knziha.polymer.Utils;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectWrap extends JSONObject {
	public JSONObjectWrap(String val) throws JSONException {
		super(val);
	}
	
	public JSONObjectWrap() {
		super();
	}
	
	@Override
	public int getInt(@NonNull String name) {
		try {
			return super.getInt(name);
		} catch (JSONException e) {
			//e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public boolean getBoolean(@NonNull String name) {
		try {
			return super.getBoolean(name);
		} catch (JSONException e) {
			//e.printStackTrace();
		}
		return false;
	}
	
	
	public float getFloat(@NonNull String name) {
		try {
			return (float) super.getDouble(name);
		} catch (JSONException e) {
			//e.printStackTrace();
		}
		return 0;
	}
	
	@NonNull
	@Override
	public String getString(@NonNull String name) {
		try {
			return super.getString(name);
		} catch (JSONException e) {
			//e.printStackTrace();
		}
		return null;
	}
	
	@NonNull
	@Override
	public JSONArray getJSONArray(@NonNull String name) {
		try {
			return super.getJSONArray(name);
		} catch (JSONException e) {
			//e.printStackTrace();
		}
		return null;
	}
}
