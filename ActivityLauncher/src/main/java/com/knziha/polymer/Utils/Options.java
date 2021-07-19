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
import com.knziha.polymer.pdviewer.bookdata.BookOptions;
import com.knziha.polymer.widgets.Utils;
import com.knziha.polymer.widgets.XYTouchRecorder;

import org.adrianwalker.multilinestring.Multiline;

//@SuppressWarnings("ALL")
public class Options implements BookOptions {
	public final SharedPreferences defaultReader;
	public DisplayMetrics dm;
	public static boolean isLarge;
	public static long FirstFlag = 0;
	public static String locale;
	public Configuration mConfiguration;
	public static int AppVersion = 0;

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
	
	public long getLastOpenedTabID() {
		return defaultReader.getLong("tabId", -1);
	}
	
	public void putLastOpenedTabID(long tabId) {
		defaultReader.edit().putLong("tabId", tabId).apply();
	}
	
	public String getOpenedTabs() {
		return defaultReader.getString("tabs", "");
	}
	
	public void putOpenedTabs(String val, long id) {
		defaultReader.edit().putString("tabs", val).putLong("tabId", id).apply();
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
		editor.putLong("MFF", FirstFlag).putLong("MSF", SecondFlag).putLong("MTF", ThirdFlag);
		if(CommitOrApplyOrNothing==1) editor.apply();
		else if(CommitOrApplyOrNothing==2) editor.commit();
		//CMN.Log("apply changes");
	}

	/////////////////////////////////////////start first flag/////////////////////////////////////////
	public long getFirstFlag() {
		if(FirstFlag==0) {
			FirstFlag=defaultReader.getLong("MFF",0);
			CMN.Log("Init FirstFlag::", FirstFlag, CMN.now());
		}
		return FirstFlag;
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
	
	@Multiline(flagPos=1) public boolean getAdjustSystemVolume(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=1) public void setAdjustSystemVolume(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=2) public boolean getAdjustSystemVolumeShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=2) public boolean toggleAdjustSystemVolumeShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=3) public boolean getEqAdjustOctopusMode(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=4) public boolean getAdjustIMShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=4) public boolean toggleAdjustIMShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=5) public boolean getAdjustFrequentSettingsShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=5) public boolean toggleAdjustFrequentSettingsShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=6) public boolean getAdjustTextShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=6) public boolean toggleAdjustTextShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=7) public boolean getAdjustLockShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=7) public boolean toggleAdjustLockShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	
	@Multiline(flagPos=8) public boolean getLockScreenOn(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=9) public boolean getAdjustScnShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=9) public boolean toggleAdjustScnShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=10) public boolean getAdjustWebsiteInfoShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=10) public boolean toggleAdjustWebsiteInfoShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=11) public boolean    getAdjustStanzaShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=11) public boolean toggleAdjustStanzaShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	@Multiline(flagPos=12) public boolean    getAdjustPdfShown(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=12) public boolean toggleAdjustPdfShown(){ FirstFlag=FirstFlag; throw new IllegalArgumentException(); }
	
	
	@Multiline(flagPos=15) public static boolean getInDarkMode(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=15) public static boolean setInDarkMode(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=16) public static boolean isFullScreen(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=16) public static boolean setFullScreen(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=17, shift=1) public boolean getTransitSplashScreen(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=17, shift=1) public void setTransitSplashScreen(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=18, shift=1) public static boolean getTransitSearchHints(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=19, shift=0) public static boolean getLimitHints(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=20) public boolean getTextPanel(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=20) public void setTextPanel(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=21) public boolean getV1Initialized(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=21) public void setV1Initialized(boolean val){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=22, shift=1) public boolean getUseBackKeyClearWebViewFocus(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=23, shift=1) public boolean getSelectAllOnFocus(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=24, shift=1) public boolean getShowImeImm(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
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
	
	@Multiline(flagPos=51, shift=1) public boolean getShowSearchHints(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=52, shift=0) public boolean getShowIdleSearchHints(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=53, shift=1) public boolean getShowSearchHintsOnClear(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=54, shift=1) public boolean getTransitListBG(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=55, shift=1) public boolean getHideKeyboardOnShowSearchHints(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=56, shift=1) public boolean getHideKeyboardOnScrollSearchHints(){ FirstFlag=FirstFlag; throw new RuntimeException(); }
	@Multiline(flagPos=57, shift=1) public boolean getShowKeyIMEOnClean(){ FirstFlag=FirstFlag; throw new RuntimeException(); }

	// 58 59


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
	
	@Multiline(flagPos=0, debug=0) public boolean checkTabsOnPause(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=1) public boolean saveTabsOnPause(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=2, debug=0) public boolean getDelayRemovingClosedTabs(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=3, shift=0) public boolean getUpdateUAOnClkLnk(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=4, shift=1) public boolean getUpdateUAOnPageSt(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=4, shift=1) public void setUpdateUAOnPageSt(boolean val){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=5, shift=1) public boolean getUpdateUAOnPageFn(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=6, shift=1, debug=1) public boolean getUpdateTextZoomOnPageSt(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=7) public boolean getOnReloadIncreaseVisitCount(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=8, shift=1) public boolean getOnReloadUpdateHistory(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	/**  升级webview.apk 后，有时加载网页会修改缩放值，导致读取值与实际值不符，且导致同一数值的更新无效。  |  Waste you battery due to Webview bugs after updating WebView.apk . */
	@Multiline(flagPos=9, shift=1) public boolean getSetTextZoomAggressively(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	
	@Multiline(flagPos=19, shift=1) public static boolean getUseCustomCrashCatcher(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=20, shift=1) public static boolean getSilentExitBypassingSystem(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	/** ffmr */
	@Multiline(flagPos=21) public static boolean getFFmpegThumbsGeneration(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=22, shift=1) public static boolean getLogToFile(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	public boolean getUseLruDiskCache() {
		return (SecondFlag & 0x100) != 0x100;
	}
	@Multiline(flagPos=28, shift=0) public static boolean getKeepScreen(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=28, shift=1) public static boolean getKeepScreen(long SecondFlag){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=28, shift=1) public static boolean setKeepScreen(boolean val){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	// 管理标签动画效果
	@Multiline(flagPos=29, shift=1, debug=1) public boolean getShowWebCoverDuringTransition(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=30, debug=0) public boolean getAlwaysPostAnima(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=31, shift=1, debug=1) public boolean getAnimateTabsManager(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=32, shift=1, debug=0) public boolean getUseStdViewAnimator(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=33, shift=1, debug=0) public boolean getNeedCntNavNodesToDel(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	
	
	
	
	@Multiline(flagPos=35, debug=0) public boolean getAlwaysShowWebCoverDuringTransition(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	@Multiline(flagPos=36, debug=0) public boolean getHideWebViewWhenShowingWebCoverDuringTransition(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	/** Need to Rearrange ViewPager's View order. */
	@Multiline(flagPos=37, debug=0) public boolean getAnimateImageviewAlone(){ SecondFlag=SecondFlag; throw new RuntimeException(); }

	// 38 - 41
	/** 在网站请求APP跳转后，短时间内禁止URL跳转（默认关闭）。 */
	@Multiline(flagPos=42) public boolean getRequestAppNoFurtherLoading(){ SecondFlag=SecondFlag; throw new RuntimeException(); }
	
	
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
	
//	@Multiline(flagPos=2) public boolean getForbidLocalStorage(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	@Multiline(flagPos=2) public boolean toggleForbidLocalStorage(){ ThirdFlag=ThirdFlag; throw new IllegalArgumentException(); }
//	@Multiline(flagPos=3, shift=1) public boolean getForbidCookie(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	@Multiline(flagPos=4, shift=1) public boolean getForbidDom(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	@Multiline(flagPos=5, shift=1) public boolean getForbidDatabase(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	//@Multiline(flagPos=6, shift=1) public boolean getApplyOverride_group_storage(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//
//	@Multiline(flagPos=7) public boolean getPCMode(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	@Multiline(flagPos=7) public boolean togglePCMode(){ ThirdFlag=ThirdFlag; throw new IllegalArgumentException(); }
//	@Multiline(flagPos=8, shift=1) public boolean getEnableJavaScript(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	@Multiline(flagPos=9) public boolean getMuteAlert(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	@Multiline(flagPos=10) public boolean getMuteDownload(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	//@Multiline(flagPos=11, shift=1) public boolean getApplyOverride_group_client(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	@Multiline(flagPos=12) public boolean getForbitNetworkImage(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
//	@Multiline(flagPos=13) public boolean getPremature(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	@Multiline(flagPos=21, debug=0) public boolean getForceTextWrapForAllWebs(){ ThirdFlag=ThirdFlag; throw new RuntimeException(); }
	
	// 25 getNavHomeShowMultiline
	// 26 getShowDragHandle
	// 27 getNavHomeSelMode
	// 28 getEditMode
	
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
	
	public final static int WebViewSettingsSource_SYSTEM=3;
	public final static int WebViewSettingsSource_DOMAIN=6;
	public final static int WebViewSettingsSource_TAB=7;
	
	public long Flag(int flagIndex) {
		switch (flagIndex){
			case 1:
				return FirstFlag;
			case 2:
				return SecondFlag;
			case 3:
				return ThirdFlag;
		}
		return 0;
	}
	
	public void Flag(int flagIndex, long val) {
		switch (flagIndex){
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
	
//	public boolean EvalBooleanForFlag(BrowserActivity a, int flagIndex, int flagPos, boolean reverse) {
//		long flag = Flag(a, flagIndex);
//		return reverse ^ (((flag>>flagPos)&0x1)!=0);
//	}
//
//	public void PutBooleanForFlag(BrowserActivity a, int flagIndex, int flagPos, boolean value, boolean reverse) {
//		long flag = Flag(a, flagIndex);
//		long mask = 1l<<flagPos;
//		if(value ^ reverse) {
//			flag |= mask;
//		} else {
//			flag &= ~mask;
//		}
//		Flag(a, flagIndex, flag);
//	}
	
	public long getLastOpenedPDocID() {
		return defaultReader.getLong("docId", -1);
	}
	
	public void putLastOpenedPDocID(long rowID) {
		defaultReader.edit().putLong("docId", rowID).apply();
	}
	
	public int getGlobalEqShift() {
		return defaultReader.getInt("eqSft", 5000);
	}
	
	public void putGlobalEqShift(int val) {
		defaultReader.edit().putInt("eqSft", val).apply();
	}
	
	public void getBandLevels(int[] levels) {
		String[] bands = defaultReader.getString("bands", "").split(";");
		for (int i = 0, len=Math.min(levels.length, bands.length); i < len; i++) {
			levels[i] = IU.parsint(bands[i]);
		}
	}
	
	public void putBandLevels(int[] levels, int globalEqShift) {
		StringBuilder str= new StringBuilder(32);
		for (int i = 0, len=levels.length; i < len; i++) {
			str.append(levels[i]);
			if (i<len-1) {
				str.append(";");
			}
		}
		defaultReader.edit().putString("bands", str.toString()).putInt("eqSft", globalEqShift).apply();
	}
	
	public int getEqBarSpacing() {
		return -1;
		//return defaultReader.getInt("EqGap",(int) (10 * GlobalOptions.density));
	}
	
	public int getEqBarSize() {
		return (int) (30 * GlobalOptions.density);
		//return Math.max(defaultReader.getInt("EqSize",(int) (30 * GlobalOptions.density)), (int) (5 * GlobalOptions.density));
	}
	
	public void putWebType(int type) {
		defaultReader.edit().putInt("webType", type).apply();
	}
	
	public int getWebType() {
		return defaultReader.getInt("webType", 0);
	}
	
	@Override
	public boolean getSingleTapSelWord() {
		return true;
	}
	
	@Override
	public boolean getSingleTapClearSel() {
		return true;
	}
	
	public void initializeV1() {
		if (!getV1Initialized())
		{
			setTransitSplashScreen(Utils.version>=23);
		}
	}
}