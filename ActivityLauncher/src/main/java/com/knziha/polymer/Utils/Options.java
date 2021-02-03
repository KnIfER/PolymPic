package com.knziha.polymer.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.filepicker.model.GlideCacheModule;
import com.knziha.filepicker.settings.FilePickerOptions;
import com.knziha.filepicker.utils.CMNF;
import com.knziha.polymer.BrowserActivity;
import com.knziha.polymer.pdviewer.bookdata.BookOptions;
import com.knziha.polymer.widgets.XYTouchRecorder;

import org.adrianwalker.multilinestring.Multiline;

//@SuppressWarnings("ALL")
public class Options implements WebOptions, BookOptions {
	
	public final SharedPreferences defaultReader;
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
	
	public String getOpenedTabs() {
		return defaultReader.getString("tabs", "");
	}
	
	public void putOpenedTabs(String val) {
		defaultReader.edit().putString("tabs", val).apply();
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
	@Multiline(flagPos=23, shift=1) public boolean getSelectAllOnFocus(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=24, shift=0) public boolean getShowImeImm(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=25) public boolean getTorchLight(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=25) public boolean toggleTorchLight(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=26, shift=1) public boolean getRemberTorchLight(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=27, shift=1) public boolean getQRFrameDrawLaser(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=28, shift=1) public boolean getQRFrameDrawLocations(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=29) public boolean getContinuousFocus(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=30) public boolean getLoopAutoFocus(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=31, shift=1) public boolean getSensorAutoFocus(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=32, shift=1) public boolean getTryAgainWithRotatedData(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=33) public boolean getTryAgainImmediately(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=34, shift=1) public boolean getOneShotAndReturn(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=35, shift=1) public boolean getRememberedLaunchCamera(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=35, shift=1) public void setRememberedLaunchCamera(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=36, shift=0) public boolean getLaunchPickerIm(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=37, shift=1) public boolean getTryAgainWithInverted(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=38, shift=1, flagSize=2) public int getLaunchCameraType(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=40) public boolean getLockOrientationEnabled(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=41) public boolean getLockOrientationLandOrPort(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=42) public boolean getLockOrientationInitial(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=43, flagSize=2) public int getLockOrientationType(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=45, shift=1) public boolean getDecode1DBar(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=46, shift=1) public boolean getDecode2DBar(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=47, shift=1) public boolean getDecode2DMatrixBar(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=48, shift=1) public boolean getDecodeUPCBar(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=49, shift=1) public boolean getDecodeAZTECBar(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=50, shift=1) public boolean getDecodePDF417Bar(){ FirstFlag=FirstFlag; throw new RuntimeException(); }



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
		defaultReader.edit().putLong("MSF",SecondFlag=val).apply();
	}
	public void putSecondFlag() {
		putSecondFlag(SecondFlag);
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
	
	@Multiline(flagPos=56) public boolean getLaunchView(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=56) public boolean toggleLaunchView(){ SecondFlag=SecondFlag; throw new IllegalArgumentException(); }
	
	
	/////////////////////End Second Flag////////////////////////////////////
	/////////////////////Start Third Flag////////////////////////////////////
	public static Long ThirdFlag=null;
	public long getThirdFlag() {
		if(ThirdFlag==null) {
			return ThirdFlag=defaultReader.getLong("MTF",0);
		}
		return ThirdFlag;
	}
	public Long ThirdFlag() {
		return ThirdFlag;
	}
	
	@Multiline(flagPos=2) public boolean getForbidLocalStorage(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=2) public boolean toggleForbidLocalStorage(){ ThirdFlag=ThirdFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=3, shift=1) public boolean getForbidCookie(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=4, shift=1) public boolean getForbidDom(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=5, shift=1) public boolean getForbidDatabase(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	//@Multiline(flagPos=6, shift=1) public boolean getApplyOverride_group_storage(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=7) public boolean getPCMode(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=7) public boolean togglePCMode(){ ThirdFlag=ThirdFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=8, shift=1) public boolean getEnableJavaScript(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=9) public boolean getMuteAlert(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=10) public boolean getMuteDownload(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	//@Multiline(flagPos=11, shift=1) public boolean getApplyOverride_group_client(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=12) public boolean getForbitNetworkImage(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=13) public boolean getPremature(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	// 管理标签动画效果
	@Multiline(flagPos=14, shift=1, debug=1) public boolean getShowWebCoverDuringTransition(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=15, debug=0) public boolean getAlwaysPostAnima(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=16, shift=1, debug=1) public boolean getAnimateTabsManager(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=17, shift=1, debug=0) public boolean getUseStdViewAnimator(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=18, shift=0, debug=0) public boolean getUpdateUALowEnd(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=18, shift=0) public void setUpdateUALowEnd(boolean val){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=19, debug=0) public boolean getAlwaysShowWebCoverDuringTransition(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=20, debug=0) public boolean getHideWebViewWhenShowingWebCoverDuringTransition(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	/** Need to Rearrange ViewPager's View order. */
	@Multiline(flagPos=21, debug=0) public boolean getAnimateImageviewAlone(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=22, debug=0) public boolean getDelayRemovingClosedTabs(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=23, debug=0) public boolean checkTabsOnPause(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	@Multiline(flagPos=24) public boolean saveTabsOnPause(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	
	/////////////////////End Third Flag////////////////////////////////////
	/////////////////////Start Fourth Flag////////////////////////////////////
	public static Long FourthFlag=null;
	public long getFourthFlag() {
		if(FourthFlag==null) {
			return FourthFlag=defaultReader.getLong("MQF",0);
		}
		return FourthFlag;
	}
	public Long FourthFlag() {
		return FourthFlag;
	}
	
	@Multiline(flagPos=0, shift=1) public void setPDocImmersive(boolean val){ FourthFlag=FourthFlag; throw new IllegalArgumentException(); }
	
	@Multiline(flagPos=1, shift=0) public void getPDocImmersiveAutoHideMenu(boolean val){ FourthFlag=FourthFlag; throw new IllegalArgumentException(); }
	
	
	/////////////////////End Fourth Flag////////////////////////////////////
	
	static XYTouchRecorder xyt;
	public static  XYTouchRecorder XYTouchRecorder() {
		if(xyt==null) xyt = new XYTouchRecorder();
		return xyt;
	}
	
	@SuppressLint("ClickableViewAccessibility")
	public static void setAsLinkedTextView(TextView tv) {
		if(xyt==null) xyt = new XYTouchRecorder();
		tv.setOnClickListener(xyt);
		tv.setOnTouchListener(xyt);
		tv.setTextSize(GlobalOptions.isLarge?22f:17f);
		if(GlobalOptions.isLarge) {
			tv.setTextSize(tv.getTextSize());
		}
		tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
	}
	
	public long Flag(BrowserActivity a, int flagIndex) {
		switch (flagIndex){
			case -2:
				return a.currentWebView.getDomainFlag();
			case -1:
				return a.currentWebView.holder.flag;
			case 1:
				return FirstFlag;
			case 2:
				return SecondFlag;
			case 3:
				return ThirdFlag;
		}
		return 0;
	}
	
	public void Flag(BrowserActivity a, int flagIndex, long val) {
		switch (flagIndex){
			case -2:
				a.currentWebView.setDomainFlag(val);
			break;
			case -1:
				a.currentWebView.holder.flag=val;
			break;
			case 1:
				FirstFlag=val;
			break;
			case 2:
				SecondFlag=val;
			break;
			case 3:
				ThirdFlag=val;
			break;
		}
	}
	
	public long getLastOpendPDocID() {
		return defaultReader.getLong("docId", -1);
	}
	
	public void putLastOpendPDocID(long rowID) {
		defaultReader.edit().putLong("docId", rowID).apply();
	}
	
	@Override
	public boolean getSingleTapSelWord() {
		return true;
	}
	
	@Override
	public boolean getSingleTapClearSel() {
		return true;
	}
}