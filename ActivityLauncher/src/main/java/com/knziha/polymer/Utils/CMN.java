package com.knziha.polymer.Utils;

import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;

import org.xwalk.core.Utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;


//common
public class CMN {
    public final static String replaceReg =  " |:|\\.|,|-|\'|(|)";
    public final static String emptyStr = "";
    public static final HashMap<String, String> AssetMap = new HashMap<>();
	public static final Boolean OccupyTag = true;
	
	public static int GlobalPageBackground = 0;
	public static int MainBackground = 0;
	public static int FloatBackground;
	public static boolean touchThenSearch=true;
	public static int actionBarHeight;
	public static int lastFavorLexicalEntry = -1;
	public static int lastHisLexicalEntry = -1;
	public static int lastFavorLexicalEntryOff = 0;
	public static int lastHisLexicalEntryOff = 0;
    //static Boolean module_set_invalid = true;
	//public static dictionary_App_Options opt;
	//public static LayoutInflater inflater;
	//protected static ViewPager viewPager;
	public static int dbVersionCode = 2;
	public static long FloatLastInvokerTime=-1;
	public static int ShallowHeaderBlue;
	
	public static long stst;
	public static long ststrt;
	public static long stst_add;
	public static boolean testing;
	public static Resources mResource;
	public static int browserTaskId;
	
	public static void rt(Object... o) {
		ststrt = System.currentTimeMillis();
		Log(o);
	}
	public static void pt(Object...args) {
		CMN.Log(Arrays.toString(args)+" "+(System.currentTimeMillis()-ststrt));
	}
	
	static XYTouchRecorder xyt;
	public static XYTouchRecorder XYTouchRecorder() {
		if(xyt==null) xyt = new XYTouchRecorder();
		return xyt;
	}
	
	///*[!0] Start debug flags and methods
	public static boolean testFLoatSearch;
	public static boolean editAll;
	public static boolean darkRequest=true;
	public static void Log(Object... o) {
		Utils.testing = testing;
		Utils.Log(o);
	}
	public static void recurseLog(View v,String... depths) {
		String depth = depths!=null && depths.length>0?depths[0]:"- ";
		String depth_plus_1=depth+"- ";
		if(!ViewGroup.class.isInstance(v)) return;
		ViewGroup vg = (ViewGroup) v;
		for(int i=0;i<vg.getChildCount();i++) {
			View CI = vg.getChildAt(i);
			Log(depth+CI+" == "+Integer.toHexString(CI.getId())+"/"+CI.getBackground()+"/"+CI.getAnimation()+"/"+CI.getAlpha());
			if(ViewGroup.class.isInstance(CI))
				recurseLog(CI,depth_plus_1);
		}
	}
	public static void recurseLogCascade(View now) {
		if(now==null) return;
		while(now.getParent()!=null) {
	    	if(!View.class.isInstance(now.getParent())) {
	    		Log("-!-reached none view object or null : "+now.getParent());
	    		break;
	    	}
	    	now=(View) now.getParent();
	    }
		Log("Cascade Start Is : "+now+" == "+Integer.toHexString(now.getId())+"/"+now.getBackground()+"/"+now.getAnimation()+"/"+now.getAlpha());
		recurseLog(now);
		//now.setBackgroundResource(R.drawable.popup_shadow);
	}
	
	public static int id(Object obj) {
		return System.identityHashCode(obj);
	}
	//[!1] End debug flags and methods*/
	
	
	static long lastTimeRecorded;
	
	public static void recordTime() {
		lastTimeRecorded=System.currentTimeMillis();
	}
	
	public static long timeElapsed() {
		return System.currentTimeMillis()-lastTimeRecorded;
	}
	
	public static long now() {
		return System.currentTimeMillis();
	}
	
	public static Object tid() {
		return Thread.currentThread().getId();
	}
}