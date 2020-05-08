package com.KnaIvER.polymer.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.filepicker.utils.CMNF;

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

	private static void updateFFAt(long o, boolean val) {
		FirstFlag &= (~o);
		if(val) FirstFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}

	public static boolean getAlwaysRefreshThumbnail() {
		return (FirstFlag & 0x1) != 0x1;
	}
	public static boolean setAlwaysRefreshThumbnail(boolean val) {
		updateFFAt(0x1,!val);
		return val;
	}

	public boolean getLaunchServiceLauncher() {
		return (FirstFlag & 1) != 1;
	}
	public boolean setLaunchServiceLauncher(boolean val) {
		updateFFAt(1,!val);
		return val;
	}

	public boolean getUseLruDiskCache() {
		return (SecondFlag & 0x100) != 0x100;
	}

	public boolean getInDarkMode() {
		boolean ret = (FirstFlag & 0x80000) == 0x80000;
		GlobalOptions.isDark |= ret;
		return ret;
	}

	public static boolean isFullScreen() {
		return (FirstFlag & 0x10000) == 0x10000;
	}
	public boolean setFullScreen(boolean val) {
		updateFFAt(0x10000,val);
		return val;
	}







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
	private static void updateSFAt(int o, boolean val) {
		SecondFlag &= (~o);
		if(val) SecondFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}
	private static void updateSFAt(long o, boolean val) {
		SecondFlag &= (~o);
		if(val) SecondFlag |= o;
		//defaultReader.edit().putInt("MFF",FirstFlag).commit();
	}


	public static boolean getKeepScreen() {
		return (SecondFlag & 0x10000000L) != 0x10000000L;
	}
	public static boolean getKeepScreen(long SecondFlag) {
		return (SecondFlag & 0x10000000L) != 0x10000000L;
	}
	public static boolean setKeepScreen(boolean val) {
		updateSFAt(0x10000000L,!val);
		return val;
	}

	//start crash handler settings
	public boolean getUseCustomCrashCatcher() {
		return true;//(SecondFlag & 0x80000L) == 0x80000L;
	}

	public boolean setUseCustomCrashCatcher(boolean val) {
		updateSFAt(0x80000L,val);
		return val;
	}

	public boolean getSilentExitBypassingSystem() {
		return (SecondFlag & 0x100000L) != 0x100000L;
	}

	public boolean setSilentExitBypassingSystem(boolean val) {
		updateSFAt(0x100000L,!val);
		return val;
	}

	/** ffmr */
	public boolean getFFmpegThumbsGeneration(){
		return (SecondFlag & 0x200000)!=0;
	}

	public boolean getLogToFile() {
		return (SecondFlag & 0x400000L) != 0x400000L;
	}

	public boolean setLogToFile(boolean val) {
		updateSFAt(0x400000L,!val);
		return val;
	}

	public static boolean getRebuildToast() {
		return true;//(SecondFlag & 0x20000000000000L) == 0x20000000000000L;
	}
	public static boolean setRebuildToast(boolean val) {
		updateSFAt(0x20000000000000L,val);
		return val;
	}


	public static boolean getToastRoundedCorner() {
		return (SecondFlag & 0x80000000000000L) != 0x80000000000000L;
	}
	public static boolean setToastRoundedCorner(boolean val) {
		updateSFAt(0x80000000000000L,!val);
		return val;
	}


	/** 32~64 */
	public int getHeight(){
		return 33+((int) ((FirstFlag >> 1) & 31)+29)%31;
	}
	public int getHeight(long FirstFlag){
		return 33+((int) ((FirstFlag >> 1) & 31)+29)%31;
	}
	public int SetHeight(int val){
		FirstFlag &= ~(0x2|0x4|0x8|0x10|0x20);
		FirstFlag |= ((long)(((val-33)+2)%31) & 31) << 1;
		return val;
	}
}