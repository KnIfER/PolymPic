package com.knziha.polymer.webstorage;

import android.os.Bundle;

public interface WebStacks {
	byte[] bakeData(Bundle bundle);
	void readData(Bundle bundle, byte[] data);
}
