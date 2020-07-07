/*
 *  Copyright © 2016, Turing Technologies, an unincorporated organisation of Wynne Plaga
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.KnaIvER.polymer.widgets;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;

import com.KnaIvER.polymer.Utils.CMN;
import com.KnaIvER.polymer.Utils.Options;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

public class Utils {
	public static final Matrix IDENTITYXIRTAM = new Matrix();
	static Rect rect = new Rect();
    /**
     * @param dp Desired size in dp (density-independent pixels)
     * @param v View
     * @return Number of corresponding density-dependent pixels for the given device
     */
    static int getDP(int dp, View v){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, v.getResources().getDisplayMetrics());
    }

    /**
     * @param dp Desired size in dp (density-independent pixels)
     * @param c Context
     * @return Number of corresponding density-dependent pixels for the given device
     */
    static int getDP(int dp, Context c){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    /**
     *
     * @param c Context
     * @return True if the current layout is RTL.
     */
    static boolean isRightToLeft(Context c) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                c.getResources().getConfiguration().getLayoutDirection() == LayoutDirection.RTL;
    }

    static <T> String getGenericName(T object){
        return ((Class<T>) ((ParameterizedType) object.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getSimpleName();
    }

    public static class DummyOnClick implements View.OnClickListener {
		@Override
		public void onClick(View v) {

		}
	}
	
	public static boolean isKeyboardShown(View rootView) {
		final int softKeyboardHeight = 100;
		rootView.getWindowVisibleDisplayFrame(rect);
		DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
		int heightDiff = rootView.getBottom() - rect.bottom;
		return heightDiff > softKeyboardHeight * dm.density;
	}
	
	static class ViewConfigurationDog extends ViewConfiguration{
		@Override
		public int getScaledTouchSlop() {
			CMN.Log("ScaledTouchSlop return 0");
			return 0;
		}
	}
	
	static SparseArray<ViewConfiguration> sConfigurations;
	
	public static void fetConfigList() {
		try {
			Field fConfigurations = ViewConfiguration.class.getDeclaredField("sConfigurations");
			fConfigurations.setAccessible(true);
			sConfigurations = (SparseArray<ViewConfiguration>) fConfigurations.get(null);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public static void sendog(Options opt) {
		if(sConfigurations!=null) {
			final int density = (int) (100.0f * opt.dm.density);
			sConfigurations.put(density, new ViewConfigurationDog());
		}
	}
	
	public static void sencat(Options opt, ViewConfiguration cat) {
		if(sConfigurations!=null) {
			final int density = (int) (100.0f * opt.dm.density);
			sConfigurations.put(density, cat);
		}
	}
}
