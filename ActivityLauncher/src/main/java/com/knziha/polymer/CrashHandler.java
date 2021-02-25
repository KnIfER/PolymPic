package com.knziha.polymer;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.GlobalOptions;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.Options;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

//美滋滋
public class CrashHandler implements UncaughtExceptionHandler {
	/** System default handler */
	private UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler instance;
	//private Context mContext;

	private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd, HH-mm-ss", Locale.CHINA);
	StringBuilder info_builder;
	private String log_path;
	private final boolean bSilentExitBypassingSystem;
	private final boolean bLogToFile;
	private boolean registered;
	private boolean turnedon;

	public static CrashHandler getInstance(Context contex, Options opt) {
		if(instance == null)
			instance = new CrashHandler(contex,opt);
		return instance;
	}

	public String getLogFile(){
		return log_path;
	}

	private CrashHandler(Context contex, Options opt){
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		bSilentExitBypassingSystem = true;//opt.getSilentExitBypassingSystem();
		bLogToFile = opt.getLogToFile();
		info_builder=new StringBuilder();
		info_builder.setLength(0);
		info_builder.append(contex.getResources().getString(R.string.app_name)).append("[device_n.").append(Build.VERSION.CODENAME)
				.append(", v.").append(Build.VERSION.SDK_INT);
	}

	public void register(Context context) {
		if(registered) return;
		Thread.setDefaultUncaughtExceptionHandler(this);
		log_path=context.getExternalFilesDir("").getAbsolutePath()+"/logs/crash.txt";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo program_info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
			if (program_info != null) {
				info_builder.append("][app_n.").append(program_info.versionName)
						.append(", v.").append(program_info.versionCode).append("]\n")
				;
				ApplicationInfo app_info = context.getApplicationInfo();
				GlobalOptions.debug=(app_info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
			}
		} catch (Exception ignored) { }
		registered=true;
	}

	public void unRegister(){
		registered=false;
		if(mDefaultHandler!=null)
			Thread.setDefaultUncaughtExceptionHandler(mDefaultHandler);
	}

	public void TurnOn(){
		turnedon=true;
	}

	public void TurnOff(){
		turnedon=false;
	}

	@Override
	public void uncaughtException(@NonNull Thread thread, @NonNull Throwable exception) {
		String message = exception.toString();
		Log.e("PolymPic","::fatal exception : "+ message);
//		if(message.contains("disk is full")) {
//			return;
//		}
//		if (exception instanceof SQLiteFullException) {
//			CMN.Log(exception);
//			AgentApplication.exception = exception;
//			return;
//		}
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		exception.printStackTrace(printWriter);
		Throwable cause = exception.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		String time = formatter.format(new Date());
		info_builder.append("crash-=====Log-start=====")
				.append(time).append("\n");
		info_builder.append(result);
		if(bLogToFile){
			try {
				File log=new File(log_path);
				File dir = log.getParentFile();
				if((dir.isDirectory() || dir.mkdirs()) && dir.getFreeSpace()>1024*1024) {
					if(log.isDirectory()) log.delete();
					FileOutputStream fos = new FileOutputStream(log_path);
					fos.write(info_builder.toString().getBytes()); fos.close();
					new File(dir, "lock").mkdirs();
				}
			} catch (Exception ignored) {  }
		}
		if(GlobalOptions.debug) {
			CMN.Log(exception);
		}
		postUncaughtException(thread, exception);
	}

	private void postUncaughtException(@NonNull Thread thread, @NonNull Throwable exception) {
		if(!turnedon || !registered || !bSilentExitBypassingSystem){
			if(mDefaultHandler != null)
				mDefaultHandler.uncaughtException(thread, exception);
			unRegister();
		} else {
			unRegister();
			System.exit(1);
		}
	}
}
