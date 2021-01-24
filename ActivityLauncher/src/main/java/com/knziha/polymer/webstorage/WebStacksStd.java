package com.knziha.polymer.webstorage;

import android.os.Bundle;
import android.os.Parcel;

public class WebStacksStd implements WebStacks{
	public byte[] bakeData(Bundle bundle) {
		Parcel parcel = Parcel.obtain();
		parcel.setDataPosition(0);
		bundle.writeToParcel(parcel, 0);
		byte[] marshall = parcel.marshall();
		parcel.recycle();
		return marshall;
	}
	
	public void readData(Bundle bundle, byte[] data) {
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(data, 0, data.length);
		parcel.setDataPosition(0);
		bundle.readFromParcel(parcel);
		parcel.recycle();
	}
}
