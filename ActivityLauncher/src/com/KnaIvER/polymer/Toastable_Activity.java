package com.KnaIvER.polymer;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.GlobalOptions;
import androidx.appcompat.widget.Toolbar;

import com.KnaIvER.polymer.Utils.CMN;
import com.KnaIvER.polymer.Utils.Options;
import com.KnaIvER.polymer.widgets.SimpleTextNotifier;
import com.bumptech.glide.load.engine.cache.DiskCache;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Pattern;

public class Toastable_Activity extends AppCompatActivity {
	public boolean systemIntialized;
	protected String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

	private static boolean MarginChecked;
	private static int DockerMarginL;
	private static int DockerMarginR;
	private static int DockerMarginT;
	private static int DockerMarginB;
	protected ViewGroup root;
	//public dictionary_App_Options opt;
	//public List<mdict> md = new ArrayList<mdict>();//Collections.synchronizedList(new ArrayList<mdict>());

	public Options opt;
	public DisplayMetrics dm;
	public LayoutInflater inflater;
	public InputMethodManager imm;

	public long lastClickTime=0;

	protected long FFStamp;
	protected long SFStamp;
	//protected long TFStamp;
	protected int MainBackground;
	public int AppBlack;
	public int AppWhite;
	public float ColorMultiplier_Wiget=0.9f;
	public float ColorMultiplier_Web=1;
	public float ColorMultiplier_Web2=1;

	public ViewGroup contentview;
	protected ViewGroup dialogHolder;
	protected ViewGroup dialog_;
	protected Toolbar toolbar;
	protected EditText etSearch;
	protected ImageView ivDeleteText;
	protected ImageView ivBack;

	protected ObjectAnimator objectAnimator;

	public Dialog d;
	public View dv;
	Configuration mConfiguration;
	boolean isDarkStamp;

	static class BaseHandler extends Handler {
		float animator = 0.1f;
		float animatorD = 0.15f;
	}

	boolean animationSnackOut;
	SimpleTextNotifier topsnack;
	Runnable snackWorker;
	Runnable snackRemover= () -> {
		if(topsnack!=null && topsnack.getParent()!=null)
			((ViewGroup)topsnack.getParent()).removeView(topsnack);
	};
	private Animator.AnimatorListener topsnackListener;
	static final int SHORT_DURATION_MS = 1500;
	static final int LONG_DURATION_MS = 2355;
	int NextSnackLength;
	protected ViewGroup DefaultTSView;
	long exitTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		opt = new Options(this);
		opt.dm = dm = new DisplayMetrics();
		Display display = getWindowManager().getDefaultDisplay();
		if (GlobalOptions.realWidth <= 0) {
			display.getRealMetrics(dm);
			GlobalOptions.realWidth = Math.min(dm.widthPixels, dm.heightPixels);
			GlobalOptions.density = dm.density;
			GlobalOptions.densityDpi = dm.densityDpi;
		}
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		super.onCreate(savedInstanceState);
		FFStamp=opt.getFirstFlag();
		SFStamp=opt.getSecondFlag();
		//TFStamp=opt.getThirdFlag();
		inflater=getLayoutInflater();
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		mConfiguration = new Configuration(getResources().getConfiguration());
		if(Build.VERSION.SDK_INT>=29){
			GlobalOptions.isDark = (mConfiguration.uiMode & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
		}else
			GlobalOptions.isDark = false;
		opt.getInDarkMode();
		isDarkStamp = GlobalOptions.isDark;
		AppBlack= GlobalOptions.isDark?Color.WHITE:Color.BLACK;
		AppWhite= GlobalOptions.isDark?Color.BLACK:Color.WHITE;

		if(opt.getUseCustomCrashCatcher()){
			CrashHandler.getInstance(this, opt).TurnOff();
			CrashHandler.getInstance(this, opt).register(getApplicationContext());
		}

		if(Options.getKeepScreen())
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		opt.mConfiguration = mConfiguration = new Configuration(getResources().getConfiguration());
		Options.isLarge = (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=3 ;

		checkLanguage();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CrashHandler.getInstance(this, opt).TurnOn();
	}

	@Override
	protected void onResume() {
		try {
			super.onResume();
		} catch (Exception ignored) { /*无敌*/ }
	}

	@Override
	public File getDatabasePath(String pathname){
		return new File(pathname);
	}

	protected void checkLog(Bundle savedInstanceState){
		boolean[] launching=new boolean[]{false};
		if(DoesActivityCheckLog()){
			if(opt.getLogToFile()){
				try {
					File log=new File(CrashHandler.getInstance(this, opt).getLogFile());
					File lock;
					if(log.exists()){
						if((lock=new File(log.getParentFile(),"lock")).exists()){
							byte[] buffer = new byte[Math.min((int) log.length(), 4096)];
							int len = new FileInputStream(log).read(buffer);
							String message=new String(buffer,0,len);
							launching[0]=true;
							Dialog d = new androidx.appcompat.app.AlertDialog.Builder(this)
									.setMessage(message)
									.setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
										lock.delete();
										dialog.dismiss();
										checkLaunch(savedInstanceState);
									})
									.setTitle("检测到异常捕获。（如发现仍不能启动，可尝试重新初始化）")
									.setCancelable(false)
									.show();
							//.create();
							((TextView)d.findViewById(R.id.alertTitle)).setSingleLine(false);
							((TextView)d.findViewById(android.R.id.message)).setTextIsSelectable(true);
							//FilePickerDialog.stylize_simple_message_dialog(d, getApplicationContext());
						}
					}
				} catch (Exception e) { CMN.Log(e); }finally {
					if(!launching[0])
						checkLaunch(savedInstanceState);
				}
			}else{
				checkLaunch(savedInstanceState);
			}
		}else{
			File lock;
			File log=new File(CrashHandler.getInstance(this, opt).getLogFile());
			if((lock=new File(log.getParentFile(),"lock")).exists())
				lock.delete();
		}
	}

	protected boolean DoesActivityCheckLog() {
		return true;
	}

	public void fix_full_screen(@Nullable View decorView) {
		if(opt.isFullScreen() && false) {//opt.isFullscreenHideNavigationbar()
			if(decorView==null) decorView=getWindow().getDecorView();
			//int options = decorView.getSystemUiVisibility();
			int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LOW_PROFILE
					| View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE
					;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	protected void checkFlags() {
		if(checkFlagsChanged()){
			opt.setFlags(null, 1);
			FFStamp=opt.FirstFlag();
			SFStamp=opt.SecondFlag();
			//TFStamp=opt.ThirdFlag();
		}
	}

	protected boolean checkFlagsChanged() {
		return FFStamp!=opt.FirstFlag() || SFStamp!=opt.SecondFlag();// || TFStamp!=opt.getThirdFlag();
	}

	protected void checkLanguage() {
		Options.locale =null;
		String language=opt.getLocale();
		if(language!=null){
			Locale locale = null;
			if(language.length()==0){
				locale=Locale.getDefault();
			}else try {
				if(language.contains("-r")){
					String[] arr=language.split("-r");
					if(arr.length==2){
						locale=new Locale(arr[0], arr[1]);
					}
				}else
					locale=new Locale(language);
			} catch (Exception ignored) { }
			//CMN.Log("language is : ", language, locale);
			if(locale!=null)
				forceLocale(this, locale);
		}
	}

	protected void forceLocale(Context context, Locale locale) {
		Configuration conf = context.getResources().getConfiguration();
		conf.setLocale(locale);
		context.getResources().updateConfiguration(conf, context.getResources().getDisplayMetrics());
	}

	protected void checkLaunch(Bundle savedInstanceState) {
		further_loading(savedInstanceState);
	}



	// 动态获取权限
	@RequiresApi(api = Build.VERSION_CODES.M)
	protected void showDialogTipUserRequestPermission() {
		new AlertDialog.Builder(this)
				.setTitle(R.string.stg_require)
				.setMessage(R.string.stg_statement)
				.setPositiveButton(R.string.stg_grantnow, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestPermissions(permissions, 321);
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						File trialPath = getExternalFilesDir("Trial");
						int trialCount=-1;
						boolean b1=trialPath.exists()&&trialPath.isDirectory();
						Toast.makeText(Toastable_Activity.this, R.string.stgerr_fail, Toast.LENGTH_SHORT).show();
					}
				}).setCancelable(false).show().getWindow().setBackgroundDrawableResource(R.drawable.popup_shadow_l);
	}

	protected void scanSettings() {

	}

	/** 如有必要，重建日志文件 */
	void CheckGlideJournal() {
		String path=opt.pathToGlide(getApplicationContext());
		File thumbs_dir=new File(path);
		if(!thumbs_dir.isDirectory())
			thumbs_dir.mkdirs();
		File journal_file = new File(path, "journal");
		if(opt.getUseLruDiskCache()){
			if(!journal_file.exists()){
				Pattern p = Pattern.compile("[a-z0-9_-]+\\.[0-9]{1,3}");
				File[] arr = thumbs_dir.listFiles(name -> !name.isDirectory() && p.matcher(name.getName()).matches());
				if(arr!=null){
					ArrayList<File> as = new ArrayList<>(Arrays.asList(arr));
					Collections.sort(as, (f1, f2) ->
					{long ret=f1.lastModified()-f2.lastModified();if(ret<0)return -1;if(ret>0)return 1;return 0;});
					long size_count=0; int trim_start=-1;
					for (int i = 0; i < as.size(); i++) {
						size_count+=as.get(as.size()-i-1).length();
						if(size_count>= DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE){
							trim_start=i;
							break;
						}
					}
					if(trim_start!=-1)
						as.subList(0, as.size() - trim_start + 1).clear();
					//size_count=0;
					//for (int i = 0; i < as.size(); i++)   size_count+=as.get(i).length();
					//CMN.Log("oh oh",size_count-DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
					try {
						BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(journal_file));
						bo.write("libcore.io.DiskLruCache\n1\n1\n1\n\n".getBytes());
						for (File fn:as) {String name=fn.getName();name=name.substring(0,name.lastIndexOf(".")); bo.write(("DIRTY "+name+  ("\nCLEAN "+name+" "+fn.length())  +"\n").getBytes(StandardCharsets.US_ASCII));}
						bo.flush();bo.close();
					} catch (Exception ignored) {}
				}
			}
		}else{
			journal_file.delete();
		}
	}

	protected void further_loading(Bundle savedInstanceState) {
		scanSettings();
	}

	Toast m_currentToast;
	TextView toastTv;
	View toastV;
	public void showX(int ResId,int len, Object...args) {
		String text = StringUtils.EMPTY;
		try {
			text = getResources().getString(ResId, args);
		} catch (Exception ignored) {  }
		showT(text,len);
	}
	public void show(int ResId,Object...args) {
		showX(ResId,Toast.LENGTH_SHORT, args);
	}
	public void showT(Object text)
	{
		showT(text,Toast.LENGTH_LONG);
	}
	public void showT(Object obj,int len)
	{
		CharSequence text = obj instanceof Integer? getResources().getText((Integer) obj):String.valueOf(obj);
		if(m_currentToast == null || Options.getRebuildToast()){
			if(m_currentToast!=null)
				m_currentToast.cancel();
			if(toastTv==null) {
				toastV = getLayoutInflater().inflate(R.layout.toast, null);
				toastTv = toastV.findViewById(R.id.message);
			}else if(toastV.getParent() instanceof ViewGroup){
				((ViewGroup)toastV.getParent()).removeView(toastV);
			}
			m_currentToast = new Toast(this);
			m_currentToast.setView(toastV);
		}
		m_currentToast.setGravity(Gravity.BOTTOM, 0, 135);
		if(toastV.getBackground() instanceof GradientDrawable){
			GradientDrawable drawable = (GradientDrawable) toastV.getBackground();
			drawable.setCornerRadius(Options.getToastRoundedCorner()?dm.density*15:0);
			drawable.setColor(opt.getToastBackground());
		}
		m_currentToast.setDuration(len);
		toastTv.setText(text);
		toastTv.setTextColor(opt.getToastColor());
		m_currentToast.show();
	}
	public void showMT(Object text){
		showT(text);
		m_currentToast.setGravity(Gravity.CENTER, 0, 0);
	}
	public void cancleToast(){
		if(m_currentToast!=null)
			m_currentToast.cancel();
	}

	void showTopSnack(Object messageVal){
		showTopSnack(DefaultTSView, messageVal, 0.8f, -1, -1, false);
	}

	void showTopSnack(ViewGroup parentView, Object messageVal, float alpha, int duration, int gravity, boolean SingleLine) {
		if(objectAnimator!=null){
			objectAnimator.cancel();
			objectAnimator=null;
		}
		if(topsnack==null){
			topsnack = new SimpleTextNotifier(getBaseContext());
			Resources res = getResources();
			topsnack.setTextColor(Color.WHITE);
			topsnack.setBackgroundColor(res.getColor(R.color.colorHeaderBlueT));
			int pad = (int) res.getDimension(R.dimen.design_snackbar_padding_horizontal);
			topsnack.setPadding(pad,pad/2,pad,pad/2);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				topsnack.setElevation(res.getDimension(R.dimen.design_snackbar_elevation));
			}
		}
		else{
			topsnack.removeCallbacks(snackRemover);
			topsnack.setAlpha(1);
		}
		NextSnackLength=duration<0?SHORT_DURATION_MS:duration;
		topsnack.getBackground().setAlpha((int) (alpha*255));
		topsnack.setSingleLine(SingleLine);
		if(messageVal instanceof Integer) {
			topsnack.setText((int) messageVal);
			topsnack.setTag(messageVal);
		}else {
			topsnack.setText(String.valueOf(messageVal));
			topsnack.setTag(null);
		}
		topsnack.setGravity(gravity==-1?Gravity.CENTER:gravity);
		ViewGroup sp = (ViewGroup) topsnack.getParent();
		if(sp!=null && sp!=parentView) sp.removeView(topsnack);
		if(topsnack.getParent()!=parentView) {
			topsnack.setVisibility(View.INVISIBLE);
			parentView.addView(topsnack);
			topsnack.getLayoutParams().height=-2;
			topsnack.post(snackWorker);
		}else{
			topsnack.removeCallbacks(snackWorker);
			snackWorker.run();
		}
	}

	protected void cancleSnack() {
		if(topsnack!=null && topsnack.getParent()!=null) {
			if(objectAnimator!=null){
				objectAnimator.removeAllListeners();
				objectAnimator.cancel();
			}
			//if(R.string.warn_exit== IU.parseInteger(topsnack.getTag(),0))
			//	exitTime=0;
			objectAnimator = ObjectAnimator.ofFloat(topsnack,"alpha",topsnack.getAlpha(),0f);
			objectAnimator.setDuration(240);
			objectAnimator.start();
			if(topsnackListener==null){
				topsnackListener = new Animator.AnimatorListener() {
					@Override public void onAnimationStart(Animator animation) { }
					@Override public void onAnimationEnd(Animator animation) {
						removeSnackView();
					}
					@Override public void onAnimationCancel(Animator animation) { }
					@Override public void onAnimationRepeat(Animator animation) { }
				};
			}
			objectAnimator.addListener(topsnackListener);
		}
	}

	void removeSnackView(){
		topsnack.removeCallbacks(snackRemover);
		topsnack.postDelayed(snackRemover, 2000);
	}

	public static void checkMargin(Activity a) {
		if(!MarginChecked) {
			//File additional_config = new File(opt.pathToMain()+"appsettings.txt");
			File additional_config = new File(Environment.getExternalStorageDirectory(), "PLOD/appsettings.txt");
			if (additional_config.exists()) {
				try {
					BufferedReader in = new BufferedReader(new FileReader(additional_config));
					String line;
					while ((line = in.readLine()) != null) {
						String[] arr = line.split(":", 2);
						if (arr.length == 2) {
							switch (arr[0]) {
								case "window margin":
								case "窗体边框":
									arr = arr[1].split(" ");
									if (arr.length == 4) {
										try {
											DockerMarginL = Integer.valueOf(arr[2]);
											DockerMarginR = Integer.valueOf(arr[3]);
											DockerMarginT = Integer.valueOf(arr[0]);
											DockerMarginB = Integer.valueOf(arr[1]);
										} catch (Exception e) {//CMN.Log(e);
										}
									}
									break;
							}
						}
					}
				} catch (Exception ignored) {
				}
			}
			MarginChecked=true;
		}
		View targetView;
		if(a instanceof Toastable_Activity)
			targetView=((Toastable_Activity)a).root;
		else
			targetView=a.findViewById(R.id.root);
		if(targetView != null && (DockerMarginL!=0 || DockerMarginR!=0 || DockerMarginT!=0 || DockerMarginB!=0)){
			ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) targetView.getLayoutParams();
			lp.leftMargin = DockerMarginL;
			lp.rightMargin = DockerMarginR;
			lp.topMargin = DockerMarginT;
			lp.bottomMargin = DockerMarginB;
			targetView.setTag(false);
			targetView.setLayoutParams(lp);
		}
	}

}