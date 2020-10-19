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

package com.knziha.polymer.widgets;

import android.app.Dialog;
import android.content.Context;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;

import org.apache.commons.lang3.ObjectUtils;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.knziha.polymer.Utils.IU.parseInt;

public class Utils {
	public final static Matrix IDENTITYXIRTAM = new Matrix();
	public final static Object DummyTransX = new Object(){
		public void setTranslationX(float val) { }
	};
	public final static Cursor EmptyCursor=new AbstractWindowedCursor() {
		@Override
		public int getCount() {
			return 0;
		}
		public String[] getColumnNames() {
			return new String[0];
		}
	};
	public static Rect rect = new Rect();
	
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
	
	public static RecyclerView.RecycledViewPool MaxRecyclerPool(int i) {
		RecyclerView.RecycledViewPool pool = new RecyclerView.RecycledViewPool();
		pool.setMaxRecycledViews(0, i);
		return pool;
	}
	
	public static boolean strEquals(CharSequence cs1, CharSequence cs2) {
		final int length = cs1.length();
		for (int i = 0; i < length; i++) {
			if (cs1.charAt(i) != cs2.charAt(i)) {
				return false;
			}
		}
		return true;
	}
	
	public static void TrimWindowWidth(Window win, DisplayMetrics dm) {
    	if(win!=null) {
			int maxWidth = (int) (GlobalOptions.density*480);
			WindowManager.LayoutParams attr = win.getAttributes();
			int targetW=dm.widthPixels>maxWidth?maxWidth: ViewGroup.LayoutParams.MATCH_PARENT;
			if(targetW!=attr.width){
				attr.width = targetW;
				win.setAttributes(attr);
			}
		}
	}
	
	protected static int DockerMarginL,DockerMarginR,DockerMarginT,DockerMarginB;
	
	static boolean read=true;
	
	private static boolean hasFrame;
 
	public static void PadWindow(View root) {
		File additional_config = new File("/sdcard/PLOD/appsettings.txt");
		if(read && additional_config.exists()) {
			read=false;
			try {
				java.io.BufferedReader in = new java.io.BufferedReader(new FileReader(additional_config));
				String line;
				while((line=in.readLine())!=null) {
					String[] arr = line.split(":", 2);
					if(arr.length==2) {
						if(arr[0].equals("float window margin")||arr[0].equals("浮动窗体边框")) {
							arr = arr[1].split(" ");
							if(arr.length==4) {
								try {
									DockerMarginL = Integer.parseInt(arr[2]);
									DockerMarginR = Integer.parseInt(arr[3]);
									DockerMarginT = parseInt(arr[0]);
									DockerMarginB = Integer.parseInt(arr[1]);
									hasFrame=true;
								} catch (Exception ignored) {}
							}
						}
					}
				}
			} catch (Exception ignored) {}
		}
		if(hasFrame) {
			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
			lp.leftMargin=DockerMarginL;
			lp.rightMargin=DockerMarginR;
			lp.topMargin=DockerMarginT;
			lp.bottomMargin=DockerMarginB;
			root.requestLayout();
		}
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
	
	public static boolean isWindowDetached(Window window) {
		return window==null || window.getDecorView().getParent()==null;
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
	
	public static ColorDrawable GrayBG = new ColorDrawable(0xff8f8f8f);
	
	public static boolean DGShowing(Dialog dTmp) {
		Window win = dTmp==null?null:dTmp.getWindow();
		return win!=null&&win.getDecorView().getParent()!=null;
	}
	
	static Object instance_WindowManagerGlobal;
	static Class class_WindowManagerGlobal;
	static Field field_mViews;
	
	public static void logAllViews(){
		List<View> views = getWindowManagerViews();
		for(View vI:views){
			CMN.Log("\n\n\n\n\n::  "+vI);
			CMN.recurseLog(vI);
		}
	}
	
	/* get the list from WindowManagerGlobal.mViews */
	public static List<View> getWindowManagerViews() {
		if(instance_WindowManagerGlobal instanceof Exception) {
			return new ArrayList<>();
		}
		try {
			if(instance_WindowManagerGlobal==null) {
				class_WindowManagerGlobal = Class.forName("android.view.WindowManagerGlobal");
				field_mViews = class_WindowManagerGlobal.getDeclaredField("mViews");
				field_mViews.setAccessible(true);
				Method method_getInstance = class_WindowManagerGlobal.getMethod("getInstance");
				instance_WindowManagerGlobal = method_getInstance.invoke(null);
			}
			Object views = field_mViews.get(instance_WindowManagerGlobal);
			if (views instanceof List) {
				return (List<View>) views;
			} else if (views instanceof View[]) {
				return Arrays.asList((View[])views);
			}
		} catch (Exception e) {
			CMN.Log(e);
			instance_WindowManagerGlobal = new Exception();
		}
		
		return new ArrayList<>();
	}
	
	public static int indexOf(CharSequence text, char cc, int now) {
		for (int i = now; i < text.length(); i++) {
			if(text.charAt(i)==cc){
				return i;
			}
		}
		return -1;
	}
	
	public static View getViewItemByPath(Object obj, int...path) {
		int cc=0;
		while(cc<path.length) {
			//CMN.Log(cc, obj);
			if(obj instanceof ViewGroup) {
				obj = ((ViewGroup)obj).getChildAt(path[cc]);
			} else {
				obj = null;
				break;
			}
			cc++;
		}
		return Objects.requireNonNull((View)obj);
	}
	
	
	public static void setOnClickListenersOneDepth(ViewGroup vg, View.OnClickListener clicker, int depth, Object[] viewFetcher) {
		int cc = vg.getChildCount();
		View ca;
		for (int i = 0; i < cc; i++) {
			ca = vg.getChildAt(i);
			//CMN.Log("setOnClickListenersOneDepth", ca, (i+1)+"/"+(cc));
			if(ca instanceof ViewGroup) {
				if(--depth>0) {
					setOnClickListenersOneDepth((ViewGroup) ca, clicker, depth, viewFetcher);
				}
			} else {
				int id = ca.getId();
				if(ca.getId()!=View.NO_ID){
					if(!(ca instanceof EditText) && ca.isEnabled()) {
						ca.setOnClickListener(clicker);
						if(clicker instanceof View.OnLongClickListener && ca.isLongClickable()) {
							ca.setOnLongClickListener((View.OnLongClickListener) clicker);
						}
					}
					if(viewFetcher!=null) {
						for (int j = 0; j < viewFetcher.length; j++) {
							if(viewFetcher[j] instanceof Integer && (int)viewFetcher[j]==id) {
								viewFetcher[j]=ca;
								break;
							}
						}
					}
				}
			}
		}
	}

}
