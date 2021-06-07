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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.GlobalOptions;
import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Toastable_Activity;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.knziha.polymer.Utils.IU.parseInt;

public class Utils {
	public static final int RequsetUrlFromCamera=1101;
	public static final int RequsetUrlFromStorage=1102;
	public static final int RequsetFileFromFilePicker=1103;
	public final static Matrix IDENTITYXIRTAM = new Matrix();
	public final static Object DummyTransX = new Object(){
		public void setTranslationX(float val) { }
		public void setTranslationY(float val) { }
		public void setScaleX(float val) { }
		public void setScaleY(float val) { }
		public void setAlpha(float val) { }
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
	public static final boolean littleCat = Build.VERSION.SDK_INT<=Build.VERSION_CODES.KITKAT;
	public static final boolean littleCake = Build.VERSION.SDK_INT<=21;
	public static final boolean bigCake= Build.VERSION.SDK_INT>=21;
	public static final boolean bigMountain = Build.VERSION.SDK_INT>22;
	public static final boolean bigMouth = Build.VERSION.SDK_INT>=Build.VERSION_CODES.O;
	public static final boolean hugeHimalaya = Build.VERSION.SDK_INT>=Build.VERSION_CODES.P;
	public static final boolean metaKill = Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q;
	//public static final boolean isHuawei = Build.MANUFACTURER.contains("HUAWEI");
	public static final WeakReference<Bitmap> DummyBMRef = new WeakReference<>(null);
	public static final int version = Build.VERSION.SDK_INT;
	
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
	
	public static String getSuffix(String path) {
		int idx=path.lastIndexOf(".");
		if(idx>=0) {
			return  path.substring(idx).toLowerCase();
		}
		return null;
	}
	
	public static String getRunTimePath(Uri uri) {
		String path  = uri.getPath();
		if(TextUtils.isEmpty(path)) {
			path = uri.toString();
		}
		return path;
	}
	
	public static String getPath(final Context context, final Uri uri) {
		if (DocumentsContract.isDocumentUri(context, uri)) {
			//CMN.Log("getPath autho", uri.getAuthority());
			// ExternalStorageProvider
			if ("com.android.externalstorage.documents".equals(uri.getAuthority()))
			{
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				System.out.println("getPath() docId: " + docId + ", split: " + split.length + ", type: " + type);
				
				// This is for checking Main Memory
				if ("primary".equalsIgnoreCase(type)) {
					if (split.length > 1) {
						return Environment.getExternalStorageDirectory() + "/" + split[1] + "/";
					} else {
						return Environment.getExternalStorageDirectory() + "/";
					}
					// This is for checking SD Card
				} else {
					return "storage" + "/" + docId.replace(":", "/");
				}
				
			}
		}
		if("content".equals(uri.getScheme())) {
			return resolveContentUri(context, uri);
		}
		return null;
	}
	
	static String resolveContentUri(Context a, Uri uri) {
		String filePath = null;
		try {
			if (uri != null) {
				Cursor cursor = a.getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
				if(cursor.moveToNext() && cursor.getColumnCount()==1) {
					filePath = cursor.getString(0);
				}
				cursor.close();
			}
		} catch (Exception e) {
			CMN.Log(e);
		}
		return filePath;
	}
	
	/** Simplify content uri to file uri if permitted */
	public static Uri getSimplifiedUrl(Activity a, Uri data) {
		try {
			Uri symda = data;
			String path = data.toString();
			if(path.contains("content:")) {
				//CMN.Log("path 1", path); CMN.Log();
				path = URLDecoder.decode(data.toString(), "utf8");;
				//CMN.Log("path 2", path); CMN.Log();
				int idx = path.indexOf("content://com.android.externalstorage");
				if(idx>0) {
					symda = Uri.parse(path.substring(idx));
				}
				path = getPath(a, symda);
				if(path!=null) {
					File f = new File(path).getAbsoluteFile();
					CMN.Log("getSimplifiedUrl", path, f, symda.getAuthority());
					return Uri.fromFile(f);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	public static void UnZipAssetsFolder(Context context, String zipAsset, File outPath) {
		try {
			ZipInputStream inZip = new ZipInputStream(context.getAssets().open(zipAsset));
			ZipEntry zipEntry;
			String entryName;
			while ((zipEntry = inZip.getNextEntry()) != null) {
				entryName = zipEntry.getName();
				if (zipEntry.isDirectory()) {
					entryName = entryName.substring(0, entryName.length() - 1);
					File folder = new File(outPath, entryName);
					folder.mkdirs();
				} else {
					File file = new File(outPath, entryName);
					if (!file.exists()) {
						file.getParentFile().mkdirs();
						file.createNewFile();
					}
					FileOutputStream out = new FileOutputStream(file);
					int len;
					byte[] buffer = new byte[1024];
					while ((len = inZip.read(buffer)) != -1) {
						out.write(buffer, 0, len);
						out.flush();
					}
					out.close();
				}
			}
			inZip.close();
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public static File preparePDFGuide(Activity a) {
		final String name = "indir.pdf";
		File guide = new File(a.getExternalFilesDir(null), name);
		if(!guide.exists()) {
			//UnZipAssetsFolder(a, "pdf.zip", a.getExternalFilesDir(null));
			try {
				InputStream is = a.getAssets().open(name);
				FileOutputStream fos = new FileOutputStream(guide);
				byte[] buffer = new byte[1024];
				int byteCount;
				while ((byteCount = is.read(buffer)) != -1) {
					fos.write(buffer, 0, byteCount);
				}
				fos.flush();
				is.close();
				fos.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return guide;
	}
	
	public static void blameAndroidIfNeeded(Context a) {
		if(a instanceof Toastable_Activity) {
			((Toastable_Activity)a).showT("Permission denied. Blame Android! ");
		}
	}
	
	static boolean vmFucked;
	
	public static void fuckVM() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !vmFucked) {
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
			vmFucked = true;
		}
	}
	
	public static int httpIndex(String url) {
		int idx = url==null?0:url.indexOf("://");
		if(idx>=4&&url.charAt(0)=='h') {
			return idx+2;
		}
		return -1;
	}
	
	public static String getSubStrWord(String text, int start, int end) {
		char c = text.charAt(start);
		if(c>='a'&&c<='z') {
			return Character.toUpperCase(c)+text.substring(start+1, end);
		}
		return text.substring(start, end);
	}
	
	public static boolean actualLandscapeMode(Context c) {
		int angle = ((WindowManager)c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
		return angle==Surface.ROTATION_90||angle==Surface.ROTATION_270;
	}
	
	public static String getTextInView(View view) {
		return ((TextView)view).getText().toString();
	}
	
	public static String getFieldInView(View view) {
		return ((TextView)view).getText().toString().trim().replaceAll("[\r\n]", "");
	}
	
	public static String getTextInView(View view, int id) {
		return ((TextView)view.findViewById(id)).getText().toString();
	}
	
	public static View replaceView(View viewToAdd, View viewToRemove) {
		return replaceView(viewToAdd, viewToRemove, true);
	}
	
	public static View replaceView(View viewToAdd, View viewToRemove, boolean layoutParams) {
		ViewGroup.LayoutParams lp = viewToRemove.getLayoutParams();
		ViewGroup vg = (ViewGroup) viewToRemove.getParent();
		if(vg!=null) {
			int idx = vg.indexOfChild(viewToRemove);
			removeView(viewToRemove);
			removeView(viewToAdd);
			if (layoutParams) {
				vg.addView(viewToAdd, idx, lp);
			} else {
				vg.addView(viewToAdd, idx);
			}
		}
		return viewToAdd;
	}
	
	public static Drawable getThemeDrawable(Context context, int attrId) {
		int[] attrs = new int[] { attrId };
		TypedArray ta = context.obtainStyledAttributes(attrs);
		Drawable drawableFromTheme = ta.getDrawable(0);
		ta.recycle();
		return drawableFromTheme;
	}
	
	public static int getViewIndex(View sv) {
		ViewGroup svp = (ViewGroup) sv.getParent();
		if (svp!=null) {
			return svp.indexOfChild(sv);
		}
		return -1;
	}
	
	public static void blinkView(View blinkView, boolean post) {
		Animation anim = new AlphaAnimation(0.1f, 1.0f);
		anim.setDuration(50);
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(2);
		if (post) {
			blinkView.post(() -> blinkView.startAnimation(anim));
		} else {
			blinkView.startAnimation(anim);
		}
	}
	
	public static void preventDefaultTouchEvent(View view, int x, int y) {
		MotionEvent evt = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, x, y, 0);
		if (view!=null) view.dispatchTouchEvent(evt);
		evt.recycle();
	}
	
	public static void performClick(View view, float x, float y) {
		MotionEvent evt = MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, x, y, 0);
		view.dispatchTouchEvent(evt);
		evt.setAction(MotionEvent.ACTION_UP);
		view.dispatchTouchEvent(evt);
		evt.recycle();
	}
	
	public static RecyclerView.ViewHolder getViewHolderInParents(View v) {
		ViewParent vp;
		Object tag;
		while(v!=null) {
			if ((tag = v.getTag()) instanceof RecyclerView.ViewHolder) {
				return (RecyclerView.ViewHolder) tag;
			}
			vp = v.getParent();
			v = vp instanceof View?(View) vp:null;
		}
		return null;
	}
	
	public static class DummyOnClick implements View.OnClickListener {
		@Override
		public void onClick(View v) {

		}
	}
	
	public static boolean isKeyboardShown(View rootView) {
		final int softKeyboardHeight = 100;
		rootView.getWindowVisibleDisplayFrame(rect);
		int heightDiff = rootView.getBottom() - rect.bottom;
		return heightDiff > softKeyboardHeight * GlobalOptions.density;
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
	
	public static View getViewItemByPath(View obj, int...path) {
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
		return (View)obj;
	}
	
	
	public static void setOnClickListenersOneDepth(ViewGroup vg, View.OnClickListener clicker, int depth, Object[] viewFetcher) {
		int cc = vg.getChildCount();
		View ca;
		boolean longClickable = clicker instanceof View.OnLongClickListener;
		if(vg.isClickable()) {
			click(vg, clicker, longClickable);
		}
		for (int i = 0; i < cc; i++) {
			ca = vg.getChildAt(i);
			//CMN.Log("setOnClickListenersOneDepth", ca, (i+1)+"/"+(cc));
			if(ca instanceof ViewGroup) {
				if(--depth>0) {
					if(ca.isClickable()) {
						click(ca, clicker, longClickable);
					} else {
						setOnClickListenersOneDepth((ViewGroup) ca, clicker, depth, viewFetcher);
					}
				}
			} else {
				int id = ca.getId();
				if(ca.getId()!=View.NO_ID){
					if(!(ca instanceof EditText) && ca.isEnabled()) {
						click(ca, clicker, longClickable);
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
	
	private static void click(View ca, View.OnClickListener clicker, boolean longClickable) {
		ca.setOnClickListener(clicker);
		if(longClickable&&ca.isLongClickable()) {
			ca.setOnLongClickListener((View.OnLongClickListener) clicker);
		}
	}
	
	public static void removeView(View viewToRemove) {
		removeIfParentBeOrNotBe(viewToRemove, null, false);
	}
	
	public static boolean removeIfParentBeOrNotBe(View view, ViewGroup parent, boolean tobe) {
		if(view!=null) {
			ViewParent svp = view.getParent();
			if(parent==svp ^ !tobe) {
				if(svp!=null) {
					((ViewGroup)svp).removeView(view);
				}
				return true;
			}
		}
		return false;
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent, int index) {
		if(removeIfParentBeOrNotBe(view2Add, parent, false)) {
			int cc=parent.getChildCount();
			if(index<0) {
				index = cc+index;
				if(index<0) {
					index = 0;
				}
			} else if(index>cc) {
				index = cc;
			}
			parent.addView(view2Add, index);
			return true;
		}
		return false;
	}
	
	public static boolean addViewToParent(View view2Add, ViewGroup parent) {
		if(removeIfParentBeOrNotBe(view2Add, parent, false)) {
			parent.addView(view2Add);
			return true;
		}
		return false;
	}
	
	public static void postInvalidateLayout(View view) {
		view.post(view::requestLayout);
	}
	
	static int resourceId=-1;
	public static int getStatusBarHeight(Resources resources) {
		if(resourceId==-1)
			try {
				resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
			} catch (Exception ignored) { }
		if (resourceId != -1) {
			return resources.getDimensionPixelSize(resourceId);
		}
		return 0;
	}
	
	private static final String PRIMARY_VOLUME_NAME = "primary";
	
	@Nullable
	public static String getFullPathFromTreeUri(Context con, @Nullable final Uri treeUri) {
		if (treeUri == null) return null;
		String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri),con);
		if (volumePath == null) return File.separator;
		if (volumePath.endsWith(File.separator))
			volumePath = volumePath.substring(0, volumePath.length() - 1);
		
		String documentPath = getDocumentPathFromTreeUri(treeUri);
		if (documentPath.endsWith(File.separator))
			documentPath = documentPath.substring(0, documentPath.length() - 1);
		
		if (documentPath.length() > 0) {
			if (documentPath.startsWith(File.separator))
				return volumePath + documentPath;
			else
				return volumePath + File.separator + documentPath;
		}
		else return volumePath;
	}
	
	private static String getVolumePath(final String volumeId, Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
			return null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
			return getVolumePathForAndroid11AndAbove(volumeId, context);
		else
			return getVolumePathBeforeAndroid11(volumeId, context);
	}
	
	private static String getVolumePathBeforeAndroid11(final String volumeId, Context context){
		try {
			StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
			Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
			Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
			Method getUuid = storageVolumeClazz.getMethod("getUuid");
			Method getPath = storageVolumeClazz.getMethod("getPath");
			Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
			Object result = getVolumeList.invoke(mStorageManager);
			
			final int length = Array.getLength(result);
			for (int i = 0; i < length; i++) {
				Object storageVolumeElement = Array.get(result, i);
				String uuid = (String) getUuid.invoke(storageVolumeElement);
				Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);
				if (primary && PRIMARY_VOLUME_NAME.equals(volumeId))    // primary volume?
					return (String) getPath.invoke(storageVolumeElement);
				if (uuid != null && uuid.equals(volumeId))    // other volumes?
					return (String) getPath.invoke(storageVolumeElement);
			}
			// not found.
			return null;
		} catch (Exception ex) {
			return null;
		}
	}
	
	@TargetApi(Build.VERSION_CODES.R)
	private static String getVolumePathForAndroid11AndAbove(final String volumeId, Context context) {
		try {
			StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
			List<StorageVolume> storageVolumes = mStorageManager.getStorageVolumes();
			for (StorageVolume storageVolume : storageVolumes) {
				// primary volume?
				if (storageVolume.isPrimary() && PRIMARY_VOLUME_NAME.equals(volumeId))
					return storageVolume.getDirectory().getPath();
				
				// other volumes?
				String uuid = storageVolume.getUuid();
				if (uuid != null && uuid.equals(volumeId))
					return storageVolume.getDirectory().getPath();
				
			}
			// not found.
			return null;
		} catch (Exception ex) {
			return null;
		}
	}
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static String getVolumeIdFromTreeUri(final Uri treeUri) {
		final String docId = DocumentsContract.getTreeDocumentId(treeUri);
		final String[] split = docId.split(":");
		if (split.length > 0) return split[0];
		else return null;
	}
	
	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private static String getDocumentPathFromTreeUri(final Uri treeUri) {
		final String docId = DocumentsContract.getTreeDocumentId(treeUri);
		final String[] split = docId.split(":");
		if ((split.length >= 2) && (split[1] != null)) return split[1];
		else return File.separator;
	}
	
	public static int hashCode(String toHash, int start, int len) {
		int h=0;
		len = Math.min(toHash.length(), len);
		for (int i = start; i < len; i++) {
			h = 31 * h + Character.toLowerCase(toHash.charAt(i));
		}
		return h;
	}
	
	public static class RecyclerViewDivider extends RecyclerView.ItemDecoration {
		static RecyclerViewDivider INSTANCE;
		final ColorDrawable mDivider = new ColorDrawable(Color.GRAY);
		final int mDividerHeight = 1;
		@Override
		public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
			final int childCount = parent.getChildCount();
			final int width = parent.getWidth();
			for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
				final View view = parent.getChildAt(childViewIndex);
				if (shouldDrawDividerBelow(view, parent)) {
					int top = (int) view.getY() + view.getHeight();
					mDivider.setBounds(0, top, width, top + mDividerHeight);
					mDivider.draw(c);
				}
			}
		}
		@Override
		public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
			if (shouldDrawDividerBelow(view, parent)) {
				outRect.bottom = mDividerHeight;
			}
		}
		private boolean shouldDrawDividerBelow(View view, RecyclerView parent) {
			//return parent.getChildViewHolder(view).getLayoutPosition()<parent.getChildCount()-1;
			return parent.getChildViewHolder(view).getBindingAdapterPosition()<parent.getAdapter().getItemCount()-1;
		}
		public static RecyclerViewDivider getInstance() {
			if(INSTANCE==null) {
				INSTANCE = new RecyclerViewDivider();
			}
			return INSTANCE;
		}
	}
}
