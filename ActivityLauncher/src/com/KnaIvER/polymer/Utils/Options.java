package com.knaiver.polymer.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;

import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.filepicker.utils.CMNF;

import org.adrianwalker.multilinestring.Multiline;

@SuppressWarnings("ALL")
public class Options
{
	
	private final SharedPreferences defaultReader;
	public DisplayMetrics dm;
	public static boolean isLarge;
	private static long FirstFlag;
	public static String locale;
	public Configuration mConfiguration;

	public Options(Context a_){
		defaultReader = PreferenceManager.getDefaultSharedPreferences(a_);
	}

	public String getLocale() {
		return locale!=null?locale:(locale=defaultReader.getString("locale",""));
	}

	public int getMainBackground() {
		return defaultReader.getInt("BCM",0xFF8f8f8f);
	}
	public int getToastBackground() {
		return defaultReader.getInt("TTB",0xFFBFDEF8);
	}
	public int getToastColor() {
		return defaultReader.getInt("TTT",0xFF0D2F4B);
	}
	public int getHintsLimitation() {
		return defaultReader.getInt("hints",16);
	}

	public String pathToGlide(@NonNull Context context) {
		return defaultReader.getString("cache_p", GlideCacheModule.DEFAULT_GLIDE_PATH=context.getExternalCacheDir().getAbsolutePath()+"/thumnails/");
	}

	/** @param CommitOrApplyOrNothing 0=nothing;1=apply;2=commit*/
	public void setFlags(SharedPreferences.Editor editor, int CommitOrApplyOrNothing) {
		if(editor==null){
			editor = defaultReader.edit();
			CommitOrApplyOrNothing=1;
		}
		editor.putLong("MFF", FirstFlag).putLong("MSF", SecondFlag);//.putLong("MTF", ThirdFlag);
		if(CommitOrApplyOrNothing==1) editor.apply();
		else if(CommitOrApplyOrNothing==2) editor.commit();
		//CMN.Log("apply changes");
	}

	/////////////////////////////////////////start first flag/////////////////////////////////////////
	public long getFirstFlag() {
		return CMNF.FirstFlag=FirstFlag=defaultReader.getLong("MFF",0);
	}

	public long FirstFlag() {
		return FirstFlag;
	}

	private void setFirstFlag(long val) {
		FirstFlag=val;
	}

	private void putFirstFlag(long val) {
		defaultReader.edit().putLong("MFF",FirstFlag=val).apply();
	}
	
	@Multiline(flagPos=0, shift=1) public static boolean getAlwaysRefreshThumbnail(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=0, shift=1) public static boolean setAlwaysRefreshThumbnail(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=15) public static boolean getInDarkMode(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=15) public static boolean setInDarkMode(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=16) public static boolean isFullScreen(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=16) public static boolean setFullScreen(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=17, shift=1) public static boolean getTransitSplashScreen(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=18, shift=1) public static boolean getTransitSearchHints(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=19, shift=0) public static boolean getLimitHints(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=20) public boolean getTextPanel(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=20) public void setTextPanel(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=22, shift=1) public boolean getUseBackKeyClearWebViewFocus(){ FirstFlag=FirstFlag; throw new RuntimeException(); }



	/////////////////////End First Flag////////////////////////////////////
	/////////////////////Start Second Flag////////////////////////////////////
	public static Long SecondFlag=null;
	public long getSecondFlag() {
		if(SecondFlag==null) {
			return FilePickerOptions.SecondFlag=SecondFlag=defaultReader.getLong("MSF",0);
		}
		return SecondFlag;
	}
	private void putSecondFlag(long val) {
		defaultReader.edit().putLong("MSF",SecondFlag=val).commit();
	}
	public void putSecondFlag() {
		putFirstFlag(SecondFlag);
	}
	public Long SecondFlag() {
		return SecondFlag;
	}
	public static void SecondFlag(long _SecondFlag) {
		SecondFlag=_SecondFlag;
	}

	@Multiline(flagPos=19, shift=1) public static boolean getUseCustomCrashCatcher(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=20, shift=1) public static boolean getSilentExitBypassingSystem(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	/** ffmr */
	@Multiline(flagPos=21) public static boolean getFFmpegThumbsGeneration(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=22, shift=1) public static boolean getLogToFile(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	public boolean getUseLruDiskCache() {
		return (SecondFlag & 0x100) != 0x100;
	}
	@Multiline(flagPos=28, shift=1) public static boolean getKeepScreen(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=28, shift=1) public static boolean getKeepScreen(long SecondFlag){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=28, shift=1) public static boolean setKeepScreen(boolean val){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=53, debug=1) public static boolean getRebuildToast(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=55, shift=1) public static boolean getToastRoundedCorner(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	
}