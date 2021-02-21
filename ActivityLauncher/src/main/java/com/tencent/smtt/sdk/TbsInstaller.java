package com.tencent.smtt.sdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.utils.ApkMd5Util;
import com.tencent.smtt.utils.AppUtil;
import com.tencent.smtt.utils.DirWalkCompareUtil;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.FsSpaceUtil;
import com.tencent.smtt.utils.TbsLog;
import dalvik.system.DexClassLoader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class TbsInstaller {
	private static TbsInstaller d = null;
	private int e = 0;
	private FileLock f;
	private FileOutputStream g;
	private boolean h = false;
	private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();
	private static final Lock LOCK = new ReentrantLock();
	private boolean k = false;
	private static FileLock l = null;
	public static ThreadLocal<Integer> a = new ThreadLocal<Integer>() {
		public Integer a() {
			return 0;
		}
		
		// $FF: synthetic method
		public Integer initialValue() {
			return this.a();
		}
	};
	private static Handler m = null;
	private static final Long[][] n = new Long[][]{{44006L, 39094008L}, {44005L, 39094008L}, {43910L, 38917816L}, {44027L, 39094008L}, {44028L, 39094008L}, {44029L, 39094008L}, {44030L, 39094008L}, {44032L, 39094008L}, {44033L, 39094008L}, {44034L, 39094008L}, {43909L, 38917816L}};
	static boolean b = false;
	static final FileFilter c = new FileFilter() {
		public boolean accept(File var1) {
			String var2 = var1.getName();
			if (var2 == null) {
				return false;
			} else if (var2.endsWith(".jar_is_first_load_dex_flag_file")) {
				return false;
			} else if (VERSION.SDK_INT >= 21 && var2.endsWith(".dex")) {
				return false;
			} else if (VERSION.SDK_INT >= 26 && var2.endsWith(".prof")) {
				return false;
			} else {
				return VERSION.SDK_INT < 26 || !var2.equals("oat");
			}
		}
	};
	private static int o = 0;
	private static boolean p = false;
	
	private TbsInstaller() {
		if (m == null) {
			m = new Handler(TbsHandlerThread.getInstance().getLooper()) {
				public void handleMessage(Message var1) {
					QbSdk.setTBSInstallingStatus(true);
					Object[] var2;
					switch (var1.what) {
						case 1:
							TbsLog.i("TbsInstaller", "TbsInstaller--handleMessage--MSG_INSTALL_TBS_CORE");
							var2 = (Object[]) ((Object[]) var1.obj);
							TbsInstaller.this.b((Context) var2[0], (String) var2[1], (Integer) var2[2]);
							break;
						case 2:
							TbsLog.i("TbsInstaller", "TbsInstaller--handleMessage--MSG_COPY_TBS_CORE");
							var2 = (Object[]) ((Object[]) var1.obj);
							TbsInstaller.this.a((Context) var2[0], (Context) var2[1], (Integer) var2[2]);
							break;
						case 3:
							TbsLog.i("TbsInstaller", "TbsInstaller--handleMessage--MSG_INSTALL_TBS_CORE_EX");
							var2 = (Object[]) ((Object[]) var1.obj);
							TbsInstaller.this.b((Context) var2[0], (Bundle) var2[1]);
							break;
						case 4:
							TbsLog.i("TbsInstaller", "TbsInstaller--handleMessage--MSG_UNZIP_TBS_CORE");
							var2 = (Object[]) ((Object[]) var1.obj);
							TbsInstaller.this.b((Context) var2[0], (File) var2[1], (Integer) var2[2]);
							QbSdk.setTBSInstallingStatus(false);
							super.handleMessage(var1);
					}
					
				}
			};
		}
		
	}
	
	static synchronized TbsInstaller a() {
		if (d == null) {
			Class var0 = TbsInstaller.class;
			synchronized (TbsInstaller.class) {
				if (d == null) {
					d = new TbsInstaller();
				}
			}
		}
		
		return d;
	}
	
	public int a(boolean var1, Context var2) {
		if (var1 || (Integer) a.get() <= 0) {
			a.set(this.getTbsCoreInstalledVerInNolock(var2));
		}
		
		return (Integer) a.get();
	}
	
	private synchronized boolean c(Context var1, boolean var2) {
		TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromTpatch");
		boolean var3 = false;
		
		try {
			if (!this.getTbsInstallingFileLock(var1)) {
				return var3;
			}
			
			boolean var4 = REENTRANT_LOCK.tryLock();
			TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromTpatch Locked =" + var4);
			if (var4) {
				try {
					int var5 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty_DefNeg1("tpatch_status");
					int var6 = this.a(false, var1);
					TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromTpatch copyStatus =" + var5);
					TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromTpatch tbsCoreInstalledVer =" + var6);
					if (var5 == 1) {
						if (var6 == 0) {
							TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromTpatch tbsCoreInstalledVer = 0", true);
							this.generateNewTbsCoreFromTpatch(var1);
							var3 = true;
						} else if (var2) {
							TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromTpatch tbsCoreInstalledVer != 0", true);
							this.generateNewTbsCoreFromTpatch(var1);
							var3 = true;
						}
					}
				} finally {
					REENTRANT_LOCK.unlock();
				}
			}
			
			this.releaseTbsInstallingFileLock();
		} catch (Throwable var11) {
			TbsLogReport.getInstance(var1).setInstallErrorCode(215, (String) var11.toString());
			QbSdk.forceSysWebViewInner(var1, "TbsInstaller::enableTbsCoreFromTpatch exception:" + Log.getStackTraceString(var11));
		}
		
		return var3;
	}
	
	private synchronized boolean d(Context var1, boolean var2) {
		TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromCopy");
		boolean var3 = false;
		
		try {
			try {
				if (!this.getTbsInstallingFileLock(var1)) {
					return var3;
				}
				
				boolean var4 = REENTRANT_LOCK.tryLock();
				TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromCopy Locked =" + var4);
				if (var4) {
					try {
						int var5 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty_DefNeg1("copy_status");
						int var6 = this.a(false, var1);
						TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromCopy copyStatus =" + var5);
						TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromCopy tbsCoreInstalledVer =" + var6);
						if (var5 == 1) {
							if (var6 == 0) {
								TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromCopy tbsCoreInstalledVer = 0", true);
								this.generateNewTbsCoreFromCopy(var1);
								var3 = true;
							} else if (var2) {
								TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromCopy tbsCoreInstalledVer != 0", true);
								this.generateNewTbsCoreFromCopy(var1);
								var3 = true;
							}
						}
					} finally {
						REENTRANT_LOCK.unlock();
					}
				}
				
				this.releaseTbsInstallingFileLock();
			} catch (Throwable var16) {
				TbsLogReport.getInstance(var1).setInstallErrorCode(215, (String) var16.toString());
				QbSdk.forceSysWebViewInner(var1, "TbsInstaller::enableTbsCoreFromCopy exception:" + Log.getStackTraceString(var16));
			}
			
			return var3;
		} finally {
			;
		}
	}
	
	private synchronized boolean e(Context var1, boolean var2) {
		boolean var3 = false;
		if (var1 != null && "com.tencent.mm".equals(var1.getApplicationContext().getApplicationInfo().packageName)) {
			TbsLogReport.getInstance(var1).setInstallErrorCode(229, (String) " ");
		}
		
		TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromUnzip canRenameTmpDir =" + var2);
		TbsLog.i("TbsInstaller", "Tbsinstaller enableTbsCoreFromUnzip #1 ");
		
		try {
			if (!this.getTbsInstallingFileLock(var1)) {
				return false;
			}
			
			TbsLog.i("TbsInstaller", "Tbsinstaller enableTbsCoreFromUnzip #2 ");
			boolean var4 = REENTRANT_LOCK.tryLock();
			TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromUnzip locked=" + var4);
			if (var4) {
				try {
					int var5 = TbsCoreInstallPropertiesHelper.getInstance(var1).getInstallStatus();
					TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromUnzip installStatus=" + var5);
					int var6 = this.a(false, var1);
					if (var5 == 2) {
						TbsLog.i("TbsInstaller", "Tbsinstaller enableTbsCoreFromUnzip #4 ");
						if (var6 == 0) {
							TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromUnzip tbsCoreInstalledVer = 0", false);
							this.generateNewTbsCoreFromUnzip(var1);
							var3 = true;
						} else if (var2) {
							TbsLog.i("TbsInstaller", "TbsInstaller-enableTbsCoreFromUnzip tbsCoreInstalledVer != 0", false);
							this.generateNewTbsCoreFromUnzip(var1);
							var3 = true;
						}
					}
				} finally {
					REENTRANT_LOCK.unlock();
				}
			}
			
			this.releaseTbsInstallingFileLock();
		} catch (Exception var11) {
			QbSdk.forceSysWebViewInner(var1, "TbsInstaller::enableTbsCoreFromUnzip Exception: " + var11);
			var11.printStackTrace();
		}
		
		return var3;
	}
	
	private synchronized boolean f(Context var1, boolean var2) {
		boolean var3 = false;
		return var3;
	}
	
	void a(Context var1, boolean var2) {
		if (var2) {
			this.k = true;
		}
		
		TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore currentProcessName=" + var1.getApplicationInfo().processName);
		TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore currentProcessId=" + Process.myPid());
		TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore currentThreadName=" + Thread.currentThread().getName());
		if (this.getTbsInstallingFileLock(var1)) {
			int var3 = -1;
			int var4 = 0;
			String var5 = null;
			int var6 = -1;
			int var7 = 0;
			boolean var8 = REENTRANT_LOCK.tryLock();
			if (var8) {
				try {
					var3 = TbsCoreInstallPropertiesHelper.getInstance(var1).getInstallStatus();
					var4 = TbsCoreInstallPropertiesHelper.getInstance(var1).getInstallCoreVer();
					var5 = TbsCoreInstallPropertiesHelper.getInstance(var1).getStringProperty("install_apk_path");
					var7 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty("copy_core_ver");
					var6 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty_DefNeg1("copy_status");
				} finally {
					REENTRANT_LOCK.unlock();
				}
			}
			
			this.releaseTbsInstallingFileLock();
			TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore installStatus=" + var3);
			TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore tbsCoreInstallVer=" + var4);
			TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore tbsApkPath=" + var5);
			TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore tbsCoreCopyVer=" + var7);
			TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore copyStatus=" + var6);
			if (TbsShareManager.isThirdPartyApp(var1)) {
				this.c(var1, TbsShareManager.a(var1, false));
			} else {
				int var9 = TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_responsecode", 0);
				boolean var10 = var9 == 1 || var9 == 2 || var9 == 4;
				if (!var10 && var9 != 0 && var9 != 5) {
					Bundle var11 = new Bundle();
					var11.putInt("operation", 10001);
					this.a(var1, var11);
				}
				
				if (var3 > -1 && var3 < 2) {
					this.a(var1, var5, var4);
				}
				
				if (var6 == 0) {
					this.b(var1, var7);
				}
			}
			
		}
	}
	
	public static void a(Context var0) {
		if (!getTmpFolderCoreToRead(var0)) {
			if (a(var0, "core_unzip_tmp")) {
				TbsCoreLoadStat.getInstance().a(var0, 417, new Throwable("TMP_TBS_UNZIP_FOLDER_NAME"));
				TbsLog.e("TbsInstaller", "TbsInstaller-UploadIfTempCoreExistConfError INFO_TEMP_CORE_EXIST_CONF_ERROR TMP_TBS_UNZIP_FOLDER_NAME");
			} else if (a(var0, "core_share_backup_tmp")) {
				TbsCoreLoadStat.getInstance().a(var0, 417, new Throwable("TMP_BACKUP_TBSCORE_FOLDER_NAME"));
				TbsLog.e("TbsInstaller", "TbsInstaller-UploadIfTempCoreExistConfError INFO_TEMP_CORE_EXIST_CONF_ERROR TMP_BACKUP_TBSCORE_FOLDER_NAME");
			} else if (a(var0, "core_copy_tmp")) {
				TbsCoreLoadStat.getInstance().a(var0, 417, new Throwable("TMP_TBS_COPY_FOLDER_NAME"));
				TbsLog.e("TbsInstaller", "TbsInstaller-UploadIfTempCoreExistConfError INFO_TEMP_CORE_EXIST_CONF_ERROR TMP_TBS_COPY_FOLDER_NAME");
			}
		}
		
	}
	
	void installTbsCoreIfNeeded(Context context, boolean var2) {
		if (!QbSdk.forcedSysByOuter) {
			if (VERSION.SDK_INT < 8) {
				TbsLog.e("TbsInstaller", "android version < 2.1 no need install X5 core", true);
			} else {
				TbsLog.i("TbsInstaller", "Tbsinstaller installTbsCoreIfNeeded #1 ");
				if (TbsShareManager.isThirdPartyApp(context) && TbsCoreInstallPropertiesHelper.getInstance(context).getIntProperty_DefNeg1("remove_old_core") == 1 && var2) {
					File var3 = a().getTbsCoreShareDir(context);
					
					try {
						FileHelper.delete(var3);
						TbsLog.i("TbsInstaller", "thirdAPP success--> delete old core_share Directory");
					} catch (Throwable var5) {
						var5.printStackTrace();
					}
					
					TbsCoreInstallPropertiesHelper.getInstance(context).setIntProperty("remove_old_core", 0);
				}
				
				if (getTmpFolderCoreToRead(context)) {
					TbsLog.i("TbsInstaller", "Tbsinstaller installTbsCoreIfNeeded #2 ");
					if (a(context, "core_unzip_tmp") && this.e(context, var2)) {
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreIfNeeded, enableTbsCoreFromUnzip!!", true);
					} else if (a(context, "core_share_backup_tmp") && this.f(context, var2)) {
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreIfNeeded, enableTbsCoreFromBackup!!", true);
					} else if (a(context, "core_copy_tmp") && this.d(context, var2)) {
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreIfNeeded, enableTbsCoreFromCopy!!", true);
					} else {
						if (!a(context, "tpatch_tmp") || !this.c(context, var2)) {
							TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreIfNeeded, error !!", true);
							return;
						}
						
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreIfNeeded, enableTbsCoreFromTpatch!!", true);
					}
				}
				
			}
		}
	}
	
	static boolean a(Context var0, String var1) {
		File var2 = QbSdk.getTbsFolderDir(var0);
		File var3 = new File(var2, var1);
		if (var3 != null && var3.exists()) {
			File var4 = new File(var3, "tbs.conf");
			if (var4 != null && var4.exists()) {
				TbsLog.i("TbsInstaller", "TbsInstaller-isPrepareTbsCore, #3");
				return true;
			} else {
				TbsLog.i("TbsInstaller", "TbsInstaller-isPrepareTbsCore, #2");
				return false;
			}
		} else {
			TbsLog.i("TbsInstaller", "TbsInstaller-isPrepareTbsCore, #1");
			return false;
		}
	}
	
	void a(Context var1, String var2, int var3) {
		TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCore tbsApkPath=" + var2);
		TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCore tbsCoreTargetVer=" + var3);
		TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore currentProcessName=" + var1.getApplicationInfo().processName);
		TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCore currentProcessId=" + Process.myPid());
		TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCore currentThreadName=" + Thread.currentThread().getName());
		Object[] var4 = new Object[]{var1, var2, var3};
		Message var5 = new Message();
		var5.what = 1;
		var5.obj = var4;
		m.sendMessage(var5);
	}
	
	@TargetApi(11)
	private void b(Context var1, String var2, int var3) {
		boolean var4 = false;
		TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-501);
		if (this.c(var1)) {
			TbsLog.i("TbsInstaller", "isTbsLocalInstalled --> no installation!", true);
			TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-502);
		} else {
			TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread tbsApkPath=" + var2);
			TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread tbsCoreTargetVer=" + var3);
			TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore currentProcessName=" + var1.getApplicationInfo().processName);
			TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread currentProcessId=" + Process.myPid());
			TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread currentThreadName=" + Thread.currentThread().getName());
			SharedPreferences var5 = null;
			if (VERSION.SDK_INT >= 11) {
				var5 = var1.getSharedPreferences("tbs_preloadx5_check_cfg_file", 4);
			} else {
				var5 = var1.getSharedPreferences("tbs_preloadx5_check_cfg_file", 0);
			}
			
			int var6 = var5.getInt("tbs_precheck_disable_version", -1);
			if (var6 == var3) {
				TbsLog.e("TbsInstaller", "TbsInstaller-installTbsCoreInThread -- version:" + var3 + " is disabled by preload_x5_check!");
				TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-503);
			} else if (!FileHelper.hasEnoughFreeSpace(var1)) {
				long var55 = FsSpaceUtil.calcFsSpaceAvailable();
				long var56 = TbsDownloadConfig.getInstance(var1).getDownloadMinFreeSpace();
				TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-504);
				TbsLogReport.getInstance(var1).setInstallErrorCode(210, (String) ("rom is not enough when installing tbs core! curAvailROM=" + var55 + ",minReqRom=" + var56));
			} else if (!this.getTbsInstallingFileLock(var1)) {
				TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-505);
			} else {
				boolean var7 = LOCK.tryLock();
				TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread locked =" + var7);
				if (var7) {
					TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-507);
					REENTRANT_LOCK.lock();
					
					try {
						int var8 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty("copy_core_ver");
						int var9 = TbsCoreInstallPropertiesHelper.getInstance(var1).getInstallCoreVer();
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread tbsCoreCopyVer =" + var8);
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread tbsCoreInstallVer =" + var9);
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread tbsCoreTargetVer =" + var3);
						if (var9 > 0 && var3 > var9 || var8 > 0 && var3 > var8) {
							this.cleanStatusAndTmpDir(var1);
						}
						
						boolean var10 = false;
						int var11 = TbsCoreInstallPropertiesHelper.getInstance(var1).getInstallStatus();
						int var12 = this.getTbsCoreInstalledVerInNolock(var1);
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread installStatus1=" + var11);
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread tbsCoreInstalledVer=" + var12);
						if (var11 >= 0 && var11 < 2) {
							var10 = true;
							TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread -- retry.....", true);
						} else if (var11 == 3 && var12 >= 0 && (var3 > var12 || var3 == 88888888)) {
							var11 = -1;
							this.cleanStatusAndTmpDir(var1);
							TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread -- update TBS.....", true);
						}
						
						TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-508);
						TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread installStatus2=" + var11);
						int var13 = 0;
						int var14;
						String var57;
						if (var11 < 1) {
							TbsLog.i("TbsInstaller", "STEP 2/2 begin installation.....", true);
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-509);
							if (var10) {
								var14 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty("unzip_retry_num");
								if (var14 > 10) {
									TbsLogReport.getInstance(var1).setInstallErrorCode(201, (String) "exceed unzip retry num!");
									this.clearNewTbsCore(var1);
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-510);
									return;
								}
								
								TbsCoreInstallPropertiesHelper.getInstance(var1).setUnzipRetryNum(var14 + 1);
							}
							
							var57 = var2;
							if (var2 == null) {
								var57 = TbsCoreInstallPropertiesHelper.getInstance(var1).getStringProperty("install_apk_path");
								if (var57 == null) {
									TbsLogReport.getInstance(var1).setInstallErrorCode(202, (String) "apk path is null!");
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-511);
									return;
								}
							}
							
							TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreInThread apkPath =" + var57);
							var13 = this.c(var1, var57);
							if (var13 == 0) {
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-512);
								TbsLogReport.getInstance(var1).setInstallErrorCode(203, (String) "apk version is 0!");
								return;
							}
							
							TbsCoreInstallPropertiesHelper.getInstance(var1).setProperty("install_apk_path", var57);
							TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(var13, 0);
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-548);
							if (TbsDownloader.a(var1)) {
								if (!this.a(var1, new File(var57), true)) {
									TbsLogReport.getInstance(var1).setInstallErrorCode(207, "unzipTbsApk failed", TbsLogReport.EventType.TYPE_INSTALL_DECOUPLE);
									return;
								}
							} else if (!this.a(var1, new File(var57))) {
								TbsLogReport.getInstance(var1).setInstallErrorCode(207, (String) "unzipTbsApk failed");
								return;
							}
							
							int var15;
							if (var10) {
								var15 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty_DefNeg1("unlzma_status");
								if (var15 > 5) {
									TbsLogReport.getInstance(var1).setInstallErrorCode(223, (String) "exceed unlzma retry num!");
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-553);
									this.clearNewTbsCore(var1);
									TbsDownload.c(var1);
									TbsDownloadConfig.getInstance(var1).mSyncMap.put("tbs_needdownload", true);
									TbsDownloadConfig.getInstance(var1).mSyncMap.put("request_full_package", true);
									TbsDownloadConfig.getInstance(var1).commit();
									return;
								}
								
								TbsCoreInstallPropertiesHelper.getInstance(var1).setUnlzmaStatus(var15 + 1);
							}
							
							TbsLog.i("TbsInstaller", "unlzma begin");
							var15 = TbsDownloadConfig.getInstance().mPreferences.getInt("tbs_responsecode", 0);
							int var16 = this.getTbsCoreInstalledVerInNolock(var1);
							if (var16 != 0) {
								Object var17 = QbSdk.a(var1, (String) "can_unlzma", (Bundle) null);
								boolean var18 = false;
								if (var17 != null && var17 instanceof Boolean) {
									var18 = (Boolean) var17;
								}
								
								if (var18) {
									Bundle var19 = new Bundle();
									var19.putInt("responseCode", var15);
									if (TbsDownloader.a(var1)) {
										var19.putString("unzip_temp_path", this.getTbsCoreShareDecoupleDir(var1).getAbsolutePath());
									} else {
										var19.putString("unzip_temp_path", this.getCoreDir(var1, 0).getAbsolutePath());
									}
									
									Object var20 = QbSdk.a(var1, "unlzma", var19);
									boolean var21 = false;
									if (var20 == null) {
										TbsLog.i("TbsInstaller", "unlzma return null");
										TbsLogReport.getInstance(var1).setInstallErrorCode(222, (String) "unlzma is null");
									} else if (var20 instanceof Boolean) {
										boolean var22 = (Boolean) var20;
										if (var22) {
											var21 = true;
											TbsLog.i("TbsInstaller", "unlzma success");
										} else {
											TbsLog.i("TbsInstaller", "unlzma return false");
											TbsLogReport.getInstance(var1).setInstallErrorCode(222, (String) "unlzma return false");
										}
									} else if (var20 instanceof Bundle) {
										var21 = true;
									} else if (var20 instanceof Throwable) {
										TbsLog.i("TbsInstaller", "unlzma failure because Throwable" + Log.getStackTraceString((Throwable) var20));
										TbsLogReport.getInstance(var1).setInstallErrorCode(222, (Throwable) ((Throwable) var20));
									}
									
									if (!var21) {
										return;
									}
								}
							}
							
							TbsLog.i("TbsInstaller", "unlzma finished");
							TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(var13, 1);
						} else if (TbsDownloader.a(var1)) {
							var57 = var2;
							if (var2 == null) {
								var57 = TbsCoreInstallPropertiesHelper.getInstance(var1).getStringProperty("install_apk_path");
								if (var57 == null) {
									TbsLogReport.getInstance(var1).setInstallErrorCode(202, (String) "apk path is null!");
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-511);
									return;
								}
							}
							
							if (!this.a(var1, new File(var57), true)) {
							}
						}
						
						if (var11 < 2) {
							if (var10) {
								var14 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty("dexopt_retry_num");
								if (var14 > 10) {
									TbsLogReport.getInstance(var1).setInstallErrorCode(208, (String) "exceed dexopt retry num!");
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-514);
									this.clearNewTbsCore(var1);
									return;
								}
								
								TbsCoreInstallPropertiesHelper.getInstance(var1).setDexoptRetryNum(var14 + 1);
							}
							
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-549);
							if (this.doTbsDexOpt(var1, 0)) {
								TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(var13, 2);
								TbsLog.i("TbsInstaller", "STEP 2/2 installation completed! you can restart!", true);
								TbsLog.i("TbsInstaller", "STEP 2/2 installation completed! you can restart! version:" + var3);
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-516);
								if (VERSION.SDK_INT >= 11) {
									var5 = var1.getSharedPreferences("tbs_preloadx5_check_cfg_file", 4);
								} else {
									var5 = var1.getSharedPreferences("tbs_preloadx5_check_cfg_file", 0);
								}
								
								try {
									Editor var58 = var5.edit();
									var58.putInt("tbs_preload_x5_counter", 0);
									var58.putInt("tbs_preload_x5_recorder", 0);
									var58.putInt("tbs_preload_x5_version", var3);
									var58.commit();
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-517);
								} catch (Throwable var53) {
									TbsLog.e("TbsInstaller", "Init tbs_preload_x5_counter#1 exception:" + Log.getStackTraceString(var53));
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-518);
								}
								
								if (var3 == 88888888) {
									this.a(var3, var2, var1);
								}
								
								if (this.k) {
									TbsLogReport.getInstance(var1).setInstallErrorCode(this.u(var1), "continueInstallWithout core success");
								} else {
									TbsLogReport.getInstance(var1).setInstallErrorCode(this.u(var1), "success");
								}
								
								var4 = true;
							} else {
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-515);
							}
						} else {
							if (var11 == 2) {
								var4 = true;
								QbSdk.m.onInstallFinish(200);
							}
							
						}
					} finally {
						try {
							REENTRANT_LOCK.unlock();
							LOCK.unlock();
						} catch (Exception var52) {
							var52.printStackTrace();
						}
						
						try {
							this.releaseTbsInstallingFileLock();
						} catch (Exception var51) {
							var51.printStackTrace();
						}
						
						if (var4) {
							QbSdk.m.onInstallFinish(232);
						}
						
					}
				} else {
					TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-519);
					this.releaseTbsInstallingFileLock();
				}
			}
		}
	}
	
	private int u(Context var1) {
		boolean var2 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIncrupdateStatus() == 1;
		boolean var3 = TbsDownloader.a(var1);
		if (var2) {
			return var3 ? 234 : 221;
		} else {
			return var3 ? 233 : 200;
		}
	}
	
	public void b(Context var1) {
		this.g(var1, true);
		TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(this.getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var1), 2);
	}
	
	public void a(Context var1, int var2) {
		this.g(var1, true);
		TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(var2, 2);
	}
	
	boolean c(Context var1) {
		boolean var2 = false;
		File var3 = this.getTbsCoreShareDir(var1);
		File var4 = new File(var3, "tbs.conf");
		if (var4 != null && var4.exists()) {
			Properties var5 = new Properties();
			FileInputStream var6 = null;
			BufferedInputStream var7 = null;
			
			try {
				var6 = new FileInputStream(var4);
				var7 = new BufferedInputStream(var6);
				var5.load(var7);
				String var8 = var5.getProperty("tbs_local_installation", "false");
				var2 = Boolean.valueOf(var8);
				boolean var9 = false;
				if (var2) {
					var9 = System.currentTimeMillis() - var4.lastModified() > 259200000L;
				}
				
				TbsLog.i("TbsInstaller", "TBS_LOCAL_INSTALLATION is:" + var2 + " expired=" + var9);
				var2 &= !var9;
			} catch (Throwable var18) {
				var18.printStackTrace();
			} finally {
				if (var7 != null) {
					try {
						var7.close();
					} catch (IOException var17) {
						var17.printStackTrace();
					}
				}
				
			}
			
			return var2;
		} else {
			return var2;
		}
	}
	
	private static boolean getTmpFolderCoreToRead(Context var0) {
		if (var0 == null) {
			TbsLog.i("TbsInstaller", "TbsInstaller-getTmpFolderCoreToRead, #1");
			return true;
		} else {
			try {
				File var1 = QbSdk.getTbsFolderDir(var0);
				File var2 = new File(var1, "tmp_folder_core_to_read.conf");
				if (var2.exists()) {
					TbsLog.i("TbsInstaller", "TbsInstaller-getTmpFolderCoreToRead, #2");
					return true;
				} else {
					TbsLog.i("TbsInstaller", "TbsInstaller-getTmpFolderCoreToRead, #3");
					return false;
				}
			} catch (Exception var3) {
				TbsLog.i("TbsInstaller", "TbsInstaller-getTmpFolderCoreToRead, #4");
				return true;
			}
		}
	}
	
	private void g(Context var1, boolean var2) {
		if (var1 == null) {
			TbsLogReport.getInstance(var1).setInstallErrorCode(225, (String) "setTmpFolderCoreToRead context is null");
		} else {
			try {
				File var3 = QbSdk.getTbsFolderDir(var1);
				File var4 = new File(var3, "tmp_folder_core_to_read.conf");
				if (var2) {
					if (var4 == null || !var4.exists()) {
						var4.createNewFile();
					}
				} else {
					FileHelper.delete(var4);
				}
			} catch (Exception var5) {
				TbsLogReport.getInstance(var1).setInstallErrorCode(225, (String) ("setTmpFolderCoreToRead Exception message is " + var5.getMessage() + " Exception cause is " + var5.getCause()));
			}
			
		}
	}
	
	public void d(Context var1) {
		try {
			File var2 = this.getTbsCoreShareDir(var1);
			File var3 = new File(var2, "tbs.conf");
			Properties var4 = new Properties();
			BufferedInputStream var6 = null;
			BufferedOutputStream var7 = null;
			
			try {
				FileInputStream var5 = new FileInputStream(var3);
				var6 = new BufferedInputStream(var5);
				var4.load(var6);
				FileOutputStream var8 = new FileOutputStream(var3);
				var7 = new BufferedOutputStream(var8);
				var4.setProperty("tbs_local_installation", "false");
				var4.store(var7, (String) null);
			} catch (Throwable var22) {
			} finally {
				if (var7 != null) {
					try {
						var7.close();
					} catch (IOException var21) {
					}
				}
				
				if (var6 != null) {
					try {
						var6.close();
					} catch (IOException var20) {
					}
				}
				
			}
		} catch (Throwable var24) {
		}
		
	}
	
	private void a(int var1, String var2, Context var3) {
		(new File(var2)).delete();
		TbsLog.i("TbsInstaller", "Local tbs apk(" + var2 + ") is deleted!", true);
		File var4 = QbSdk.getTbsFolderDir(var3);
		File var5 = new File(var4, "core_unzip_tmp");
		if (var5 != null && var5.canRead()) {
			File var6 = new File(var5, "tbs.conf");
			Properties var7 = new Properties();
			BufferedInputStream var9 = null;
			BufferedOutputStream var10 = null;
			
			try {
				FileInputStream var8 = new FileInputStream(var6);
				var9 = new BufferedInputStream(var8);
				var7.load(var9);
				FileOutputStream var11 = new FileOutputStream(var6);
				var10 = new BufferedOutputStream(var11);
				var7.setProperty("tbs_local_installation", "true");
				var7.store(var10, (String) null);
				TbsLog.i("TbsInstaller", "TBS_LOCAL_INSTALLATION is set!", true);
			} catch (Throwable var24) {
				var24.printStackTrace();
			} finally {
				if (var10 != null) {
					try {
						var10.close();
					} catch (IOException var23) {
						var23.printStackTrace();
					}
				}
				
				if (var9 != null) {
					try {
						var9.close();
					} catch (IOException var22) {
						var22.printStackTrace();
					}
				}
				
			}
		}
		
	}
	
	boolean b(Context var1, int var2) {
		if (TbsDownloader.getOverSea(var1)) {
			return false;
		} else {
			TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTbsCore targetTbsCoreVer=" + var2);
			TbsLog.i("TbsInstaller", "TbsInstaller-continueInstallTbsCore currentProcessName=" + var1.getApplicationInfo().processName);
			TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTbsCore currentProcessId=" + Process.myPid());
			TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTbsCore currentThreadName=" + Thread.currentThread().getName());
			Context var3 = this.d(var1, var2);
			if (var3 != null) {
				Object[] var4 = new Object[]{var3, var1, var2};
				Message var5 = new Message();
				var5.what = 2;
				var5.obj = var4;
				m.sendMessage(var5);
				return true;
			} else {
				TbsLog.i("TbsInstaller", "TbsInstaller--installLocalTbsCore copy from null");
				return false;
			}
		}
	}
	
	void a(Context var1, Bundle var2) {
		if (var2 != null && var1 != null) {
			Object[] var3 = new Object[]{var1, var2};
			Message var4 = new Message();
			var4.what = 3;
			var4.obj = var3;
			m.sendMessage(var4);
		}
	}
	
	void a(Context var1, File var2, int var3) {
		TbsLog.i("TbsInstaller", "unzipTbsCoreToThirdAppTmp,ctx=" + var1 + "File=" + var2 + "coreVersion=" + var3);
		if (var2 != null && var1 != null) {
			Object[] var4 = new Object[]{var1, var2, var3};
			Message var5 = new Message();
			var5.what = 4;
			var5.obj = var4;
			m.sendMessage(var5);
		}
	}
	
	void b(Context var1, Bundle var2) {
		TbsLog.i("TbsInstaller", "TbsInstaller installLocalTbsCoreExInThreadthread " + Thread.currentThread().getName() + Log.getStackTraceString(new Throwable()));
		if (this.c(var1)) {
			TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-539);
		} else {
			TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread");
			if (var2 != null && var1 != null) {
				if (!FileHelper.hasEnoughFreeSpace(var1)) {
					long var22 = FsSpaceUtil.calcFsSpaceAvailable();
					long var23 = TbsDownloadConfig.getInstance(var1).getDownloadMinFreeSpace();
					TbsLogReport.getInstance(var1).setInstallErrorCode(210, (String) ("rom is not enough when patching tbs core! curAvailROM=" + var22 + ",minReqRom=" + var23));
					TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-540);
				} else if (!this.getTbsInstallingFileLock(var1)) {
					TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-541);
				} else {
					int var4;
					Bundle var5;
					int var6;
					int var8;
					String var25;
					label476:
					{
						boolean var3 = LOCK.tryLock();
						TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread locked=" + var3);
						if (var3) {
							var4 = TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_responsecode", 0);
							var5 = null;
							var6 = 2;
							boolean var19 = false;
							
							label468:
							{
								label486:
								{
									label470:
									{
										try {
											var19 = true;
											QbSdk.setTBSInstallingStatus(true);
											if (var4 == 5) {
												var6 = this.c(var1, var2);
												if (var6 == 1) {
													int var24 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty("tpatch_num");
													TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("tpatch_num", var24 + 1);
													var19 = false;
												} else {
													var19 = false;
												}
												break label486;
											}
											
											String var10;
											String var11;
											label443:
											{
												if (this.getTbsCoreInstalledVerInNolock(var1) > 0 && TbsCoreInstallPropertiesHelper.getInstance(var1).getIncrupdateStatus() != 1) {
													boolean var7 = var4 == 1 || var4 == 2 || var4 == 4;
													if (var7) {
														var19 = false;
														break label468;
													}
													
													if (var4 != 0) {
														var8 = TbsCoreInstallPropertiesHelper.getInstance(var1).getIntProperty("incrupdate_retry_num");
														if (var8 <= 5) {
															TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", var8 + 1);
															File var26 = getTbsCorePrivateDir(var1);
															if (var26 != null) {
																File var27 = new File(var26, "x5.tbs");
																if (var27 != null) {
																	if (var27.exists()) {
																		TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-550);
																		var5 = QbSdk.a(var1, var2);
																		if (var5 == null) {
																			var6 = 1;
																			TbsLogReport.getInstance(var1).setInstallErrorCode(228, (String) ("result null : " + var2.getInt("new_core_ver")));
																			var19 = false;
																		} else {
																			var6 = var5.getInt("patch_result");
																			if (var6 != 0) {
																				TbsLogReport.getInstance(var1).setInstallErrorCode(228, (String) ("result " + var6 + " : " + var2.getInt("new_core_ver")));
																				var19 = false;
																			} else {
																				var19 = false;
																			}
																		}
																	} else {
																		var19 = false;
																	}
																} else {
																	var19 = false;
																}
															} else {
																var19 = false;
															}
															break label468;
														}
														
														TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread exceed incrupdate num");
														String var9 = var2.getString("old_apk_location");
														var10 = var2.getString("new_apk_location");
														var11 = var2.getString("diff_file_location");
														if (!TextUtils.isEmpty(var9)) {
															FileHelper.delete(new File(var9));
														}
														break label443;
													}
													
													var19 = false;
													break label468;
												}
												
												QbSdk.setTBSInstallingStatus(false);
												var19 = false;
												break label476;
											}
											
											if (!TextUtils.isEmpty(var10)) {
												FileHelper.delete(new File(var10));
											}
											
											if (!TextUtils.isEmpty(var11)) {
												FileHelper.delete(new File(var11));
											}
											
											TbsDownloadConfig.getInstance(var1).mSyncMap.put("tbs_needdownload", true);
											TbsDownloadConfig.getInstance(var1).commit();
											TbsLogReport.getInstance(var1).setInstallErrorCode(224, (String) "incrUpdate exceed retry max num");
											var19 = false;
										} catch (Exception var20) {
											TbsLog.i("TbsInstaller", "installLocalTbsCoreExInThread exception:" + Log.getStackTraceString(var20));
											var20.printStackTrace();
											var6 = 1;
											TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-543);
											TbsLogReport.getInstance(var1).setInstallErrorCode(218, (String) var20.toString());
											var19 = false;
											break label470;
										} finally {
											if (var19) {
												LOCK.unlock();
												this.releaseTbsInstallingFileLock();
												if (var4 == 5) {
													this.h(var1, var6);
													return;
												}
												
												if (var6 == 0) {
													TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_SUCCESS");
													TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", 0);
													TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-544);
													TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, -1);
													TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(1);
													String var15 = var5.getString("apk_path");
													TbsDownload.backupTbsApk(new File(var15), var1);
													int var16 = var5.getInt("tbs_core_ver");
													this.b(var1, var15, var16);
													if (TbsDownloader.a(var1)) {
														TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
													}
												} else if (var6 == 2) {
													TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_NONEEDPATCH");
												} else {
													TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-546);
													TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_FAIL");
													TbsDownloadConfig.getInstance(var1).mSyncMap.put("tbs_needdownload", true);
													TbsDownloadConfig.getInstance(var1).commit();
													if (TbsDownloader.a(var1)) {
														TbsLogReport.getInstance(var1).setInstallErrorCode(235, (String) ("decouple incrUpdate fail! patch ret=" + var6));
													} else {
														TbsLogReport.getInstance(var1).setInstallErrorCode(217, (String) ("incrUpdate fail! patch ret=" + var6));
													}
												}
												
												QbSdk.setTBSInstallingStatus(false);
											}
										}
										
										LOCK.unlock();
										this.releaseTbsInstallingFileLock();
										if (var4 == 5) {
											this.h(var1, var6);
											return;
										}
										
										if (var6 == 0) {
											TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_SUCCESS");
											TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", 0);
											TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-544);
											TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, -1);
											TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(1);
											String var12 = var5.getString("apk_path");
											TbsDownload.backupTbsApk(new File(var12), var1);
											int var13 = var5.getInt("tbs_core_ver");
											this.b(var1, var12, var13);
											if (TbsDownloader.a(var1)) {
												TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
											}
										} else if (var6 == 2) {
											TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_NONEEDPATCH");
										} else {
											TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-546);
											TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_FAIL");
											TbsDownloadConfig.getInstance(var1).mSyncMap.put("tbs_needdownload", true);
											TbsDownloadConfig.getInstance(var1).commit();
											if (TbsDownloader.a(var1)) {
												TbsLogReport.getInstance(var1).setInstallErrorCode(235, (String) ("decouple incrUpdate fail! patch ret=" + var6));
											} else {
												TbsLogReport.getInstance(var1).setInstallErrorCode(217, (String) ("incrUpdate fail! patch ret=" + var6));
											}
										}
										
										QbSdk.setTBSInstallingStatus(false);
										return;
									}
									
									LOCK.unlock();
									this.releaseTbsInstallingFileLock();
									if (var4 == 5) {
										this.h(var1, var6);
										return;
									}
									
									if (var6 == 0) {
										TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_SUCCESS");
										TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", 0);
										TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-544);
										TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, -1);
										TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(1);
										var25 = var5.getString("apk_path");
										TbsDownload.backupTbsApk(new File(var25), var1);
										var8 = var5.getInt("tbs_core_ver");
										this.b(var1, var25, var8);
										if (TbsDownloader.a(var1)) {
											TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
										}
									} else if (var6 == 2) {
										TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_NONEEDPATCH");
									} else {
										TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-546);
										TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_FAIL");
										TbsDownloadConfig.getInstance(var1).mSyncMap.put("tbs_needdownload", true);
										TbsDownloadConfig.getInstance(var1).commit();
										if (TbsDownloader.a(var1)) {
											TbsLogReport.getInstance(var1).setInstallErrorCode(235, (String) ("decouple incrUpdate fail! patch ret=" + var6));
										} else {
											TbsLogReport.getInstance(var1).setInstallErrorCode(217, (String) ("incrUpdate fail! patch ret=" + var6));
										}
									}
									
									QbSdk.setTBSInstallingStatus(false);
									return;
								}
								
								LOCK.unlock();
								this.releaseTbsInstallingFileLock();
								if (var4 == 5) {
									this.h(var1, var6);
									return;
								}
								
								if (var6 == 0) {
									TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_SUCCESS");
									TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", 0);
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-544);
									TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, -1);
									TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(1);
									var25 = var5.getString("apk_path");
									TbsDownload.backupTbsApk(new File(var25), var1);
									var8 = var5.getInt("tbs_core_ver");
									this.b(var1, var25, var8);
									if (TbsDownloader.a(var1)) {
										TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
									}
								} else if (var6 == 2) {
									TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_NONEEDPATCH");
								} else {
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-546);
									TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_FAIL");
									TbsDownloadConfig.getInstance(var1).mSyncMap.put("tbs_needdownload", true);
									TbsDownloadConfig.getInstance(var1).commit();
									if (TbsDownloader.a(var1)) {
										TbsLogReport.getInstance(var1).setInstallErrorCode(235, (String) ("decouple incrUpdate fail! patch ret=" + var6));
									} else {
										TbsLogReport.getInstance(var1).setInstallErrorCode(217, (String) ("incrUpdate fail! patch ret=" + var6));
									}
								}
								
								QbSdk.setTBSInstallingStatus(false);
								return;
							}
							
							LOCK.unlock();
							this.releaseTbsInstallingFileLock();
							if (var4 == 5) {
								this.h(var1, var6);
								return;
							}
							
							if (var6 == 0) {
								TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_SUCCESS");
								TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", 0);
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-544);
								TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, -1);
								TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(1);
								var25 = var5.getString("apk_path");
								TbsDownload.backupTbsApk(new File(var25), var1);
								var8 = var5.getInt("tbs_core_ver");
								this.b(var1, var25, var8);
								if (TbsDownloader.a(var1)) {
									TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
								}
							} else if (var6 == 2) {
								TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_NONEEDPATCH");
							} else {
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-546);
								TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_FAIL");
								TbsDownloadConfig.getInstance(var1).mSyncMap.put("tbs_needdownload", true);
								TbsDownloadConfig.getInstance(var1).commit();
								if (TbsDownloader.a(var1)) {
									TbsLogReport.getInstance(var1).setInstallErrorCode(235, (String) ("decouple incrUpdate fail! patch ret=" + var6));
								} else {
									TbsLogReport.getInstance(var1).setInstallErrorCode(217, (String) ("incrUpdate fail! patch ret=" + var6));
								}
							}
							
							QbSdk.setTBSInstallingStatus(false);
						} else {
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-547);
							this.releaseTbsInstallingFileLock();
						}
						
						return;
					}
					
					LOCK.unlock();
					this.releaseTbsInstallingFileLock();
					if (var4 == 5) {
						this.h(var1, var6);
					} else {
						if (var6 == 0) {
							TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_SUCCESS");
							TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", 0);
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-544);
							TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, -1);
							TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(1);
							var25 = var5.getString("apk_path");
							TbsDownload.backupTbsApk(new File(var25), var1);
							var8 = var5.getInt("tbs_core_ver");
							this.b(var1, var25, var8);
							if (TbsDownloader.a(var1)) {
								TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
							}
						} else if (var6 == 2) {
							TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_NONEEDPATCH");
						} else {
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-546);
							TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTesCoreExInThread PATCH_FAIL");
							TbsDownloadConfig.getInstance(var1).mSyncMap.put("tbs_needdownload", true);
							TbsDownloadConfig.getInstance(var1).commit();
							if (TbsDownloader.a(var1)) {
								TbsLogReport.getInstance(var1).setInstallErrorCode(235, (String) ("decouple incrUpdate fail! patch ret=" + var6));
							} else {
								TbsLogReport.getInstance(var1).setInstallErrorCode(217, (String) ("incrUpdate fail! patch ret=" + var6));
							}
						}
						
						QbSdk.setTBSInstallingStatus(false);
					}
				}
			}
		}
	}
	
	private void h(Context var1, int var2) {
		TbsLog.i("TbsInstaller", "proceedTpatchStatus,result=" + var2);
		switch (var2) {
			case 0:
				if (TbsDownloader.a(var1)) {
					this.i(var1, 6);
				} else {
					this.g(var1, true);
					int var3 = TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_download_version", 0);
					TbsCoreInstallPropertiesHelper.getInstance(var1).setTpatchVerAndTpatchStatus(var3, 1);
				}
			case 1:
			case 2:
			default:
				QbSdk.setTBSInstallingStatus(false);
		}
	}
	
	private int c(Context var1, Bundle var2) {
		byte var4;
		try {
			Bundle var3 = QbSdk.a(var1, var2);
			TbsLog.i("TbsInstaller", "tpatch finished,ret is" + var3);
			int var5 = var3.getInt("patch_result");
			String var6;
			if (var5 == 0) {
				var6 = var2.getString("new_apk_location");
				int var7 = var2.getInt("new_core_ver");
				int var8 = this.getTbsVersion(new File(var6));
				if (var7 != var8) {
					TbsLog.i("TbsInstaller", "version not equals!!!" + var7 + "patchVersion:" + var8);
					TbsLogReport.getInstance(var1).setInstallErrorCode(240, (String) ("version=" + var7 + ",patchVersion=" + var8));
					return 1;
				}
				
				File var9 = new File(var2.getString("backup_apk"));
				String var10 = AppUtil.a(var1, true, var9);
				if (!"3082023f308201a8a00302010202044c46914a300d06092a864886f70d01010505003064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f301e170d3130303732313036313835305a170d3430303731333036313835305a3064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f30819f300d06092a864886f70d010101050003818d0030818902818100c209077044bd0d63ea00ede5b839914cabcc912a87f0f8b390877e0f7a2583f0d5933443c40431c35a4433bc4c965800141961adc44c9625b1d321385221fd097e5bdc2f44a1840d643ab59dc070cf6c4b4b4d98bed5cbb8046e0a7078ae134da107cdf2bfc9b440fe5cb2f7549b44b73202cc6f7c2c55b8cfb0d333a021f01f0203010001300d06092a864886f70d010105050003818100b007db9922774ef4ccfee81ba514a8d57c410257e7a2eba64bfa17c9e690da08106d32f637ac41fbc9f205176c71bde238c872c3ee2f8313502bee44c80288ea4ef377a6f2cdfe4d3653c145c4acfedbfbadea23b559d41980cc3cdd35d79a68240693739aabf5c5ed26148756cf88264226de394c8a24ac35b712b120d4d23a".equals(var10)) {
					TbsLog.i("TbsInstaller", "tpatch sig not equals!!!" + var9 + "signature:" + var10);
					TbsLogReport.getInstance(var1).setInstallErrorCode(241, (String) ("version=" + var7 + ",patchVersion=" + var8));
					FileHelper.delete(var9);
					return 0;
				}
				
				var4 = 0;
				if (TbsDownloader.a(var1)) {
					TbsLog.i("TbsInstaller", "Tpatch decouple success!");
					TbsLogReport.getInstance(var1).setInstallErrorCode(237, (String) "");
				} else {
					TbsLog.i("TbsInstaller", "Tpatch success!");
					TbsLogReport.getInstance(var1).setInstallErrorCode(236, (String) "");
				}
			} else {
				var4 = 1;
				var6 = var2.getString("new_apk_location");
				if (!TextUtils.isEmpty(var6)) {
					FileHelper.delete(new File(var6));
				}
				
				TbsLogReport.getInstance(var1).setInstallErrorCode(var5, "tpatch fail,patch error_code=" + var5);
			}
		} catch (Exception var11) {
			var11.printStackTrace();
			var4 = 1;
			TbsLogReport.getInstance(var1).setInstallErrorCode(239, (String) ("patch exception" + Log.getStackTraceString(var11)));
		}
		
		return var4;
	}
	
	void c(Context var1, int var2) {
		TbsLog.i("TbsInstaller", "TbsInstaller-installTbsCoreForThirdPartyApp");
		if (var2 > 0) {
			int var3 = this.getTbsCoreInstalledVerInNolock(var1);
			if (var3 != var2) {
				Context var4 = TbsShareManager.e(var1);
				if (var4 == null && TbsShareManager.getHostCorePathAppDefined() == null) {
					if (var3 <= 0) {
						TbsLog.i("TbsInstaller", "TbsInstaller--installTbsCoreForThirdPartyApp hostContext == null");
						QbSdk.forceSysWebViewInner(var1, "TbsInstaller::installTbsCoreForThirdPartyApp forceSysWebViewInner #2");
					}
				} else {
					TbsLog.i("TbsInstaller", "TbsInstaller--quickDexOptForThirdPartyApp hostContext != null");
					this.a(var1, var4);
				}
				
			}
		}
	}
	
	@TargetApi(11)
	private void a(Context var1, Context var2, int var3) {
		TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-524);
		if (!this.c(var2)) {
			TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread start!  tbsCoreTargetVer is " + var3);
			SharedPreferences var4 = null;
			if (VERSION.SDK_INT >= 11) {
				var4 = var2.getSharedPreferences("tbs_preloadx5_check_cfg_file", 4);
			} else {
				var4 = var2.getSharedPreferences("tbs_preloadx5_check_cfg_file", 0);
			}
			
			int var5 = var4.getInt("tbs_precheck_disable_version", -1);
			if (var5 == var3) {
				TbsLog.e("TbsInstaller", "TbsInstaller-copyTbsCoreInThread -- version:" + var3 + " is disabled by preload_x5_check!");
				TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-525);
			} else if (!this.getTbsInstallingFileLock(var2)) {
				TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-526);
			} else {
				boolean var6 = LOCK.tryLock();
				TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread #1 locked is " + var6);
				if (var6) {
					REENTRANT_LOCK.lock();
					File tbsCoreShareDecoupleDir = null;
					
					try {
						int var8 = TbsCoreInstallPropertiesHelper.getInstance(var2).getIntProperty("copy_core_ver");
						int var9 = TbsCoreInstallPropertiesHelper.getInstance(var2).getIntProperty_DefNeg1("copy_status");
						if (var8 == var3) {
							QbSdk.m.onInstallFinish(220);
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-528);
							return;
						}
						
						int var10 = this.getTbsCoreInstalledVerInNolock(var2);
						TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread tbsCoreInstalledVer=" + var10);
						if (var10 == var3) {
							QbSdk.m.onInstallFinish(220);
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-528);
							TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread return have same version is " + var10);
							return;
						}
						
						int var11 = TbsCoreInstallPropertiesHelper.getInstance(var2).getInstallCoreVer();
						if (var11 > 0 && var3 > var11 || var8 > 0 && var3 > var8) {
							this.cleanStatusAndTmpDir(var2);
						}
						
						if (var9 == 3 && var10 > 0 && (var3 > var10 || var3 == 88888888)) {
							var9 = -1;
							this.cleanStatusAndTmpDir(var2);
							TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread -- update TBS.....", true);
						}
						
						long var14;
						if (!FileHelper.hasEnoughFreeSpace(var2)) {
							long var54 = FsSpaceUtil.calcFsSpaceAvailable();
							var14 = TbsDownloadConfig.getInstance(var2).getDownloadMinFreeSpace();
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-529);
							TbsLogReport.getInstance(var2).setInstallErrorCode(210, (String) ("rom is not enough when copying tbs core! curAvailROM=" + var54 + ",minReqRom=" + var14));
							return;
						}
						
						if (var9 > 0 && (TbsShareManager.isThirdPartyApp(var2) || !TbsDownloader.a(var2) || var3 == this.getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var2))) {
							TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread return have copied is " + this.getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var2));
							return;
						}
						
						if (var9 == 0) {
							int var12 = TbsCoreInstallPropertiesHelper.getInstance(var2).getIntProperty("copy_retry_num");
							if (var12 > 2) {
								TbsLogReport.getInstance(var2).setInstallErrorCode(211, (String) "exceed copy retry num!");
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-530);
								return;
							}
							
							TbsCoreInstallPropertiesHelper.getInstance(var2).setIntProperty("copy_retry_num", var12 + 1);
						}
						
						File tbsCoreShareDir = this.getTbsCoreShareDir(var1);
						if (!TbsShareManager.isThirdPartyApp(var2)) {
							if (TbsDownloader.a(var2)) {
								tbsCoreShareDecoupleDir = this.getTbsCoreShareDecoupleDir(var2);
							} else {
								tbsCoreShareDecoupleDir = this.getCoreDir(var2, 1);
							}
						} else {
							tbsCoreShareDecoupleDir = this.getCoreDir(var2, 1);
						}
						
						if (tbsCoreShareDir != null && tbsCoreShareDecoupleDir != null) {
							TbsCoreInstallPropertiesHelper.getInstance(var2).setCopyCoreVerAndCopyStatus(var3, 0);
							DirWalkCompareUtil walkCompareUtil = new DirWalkCompareUtil();
							walkCompareUtil.WalkA(tbsCoreShareDir);
							var14 = System.currentTimeMillis();
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-551);
							boolean var16 = FileHelper.forceTransferFile(tbsCoreShareDir, tbsCoreShareDecoupleDir, c);
							if (TbsDownloader.a(var2)) {
								TbsShareManager.b(var2);
							}
							
							TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread time=" + (System.currentTimeMillis() - var14));
							if (var16) {
								walkCompareUtil.WalkB(tbsCoreShareDir);
								if (!walkCompareUtil.isIdentical()) {
									TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread copy-verify fail!");
									FileHelper.delete(tbsCoreShareDecoupleDir, true);
									TbsLogReport.getInstance(var2).setInstallErrorCode(213, (String) "TbsCopy-Verify fail after copying tbs core!");
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-531);
									return;
								}
								
								boolean var17 = true;
								boolean var18 = true;
								FileInputStream var19 = null;
								BufferedInputStream var20 = null;
								Properties var21 = null;
								
								File var22;
								try {
									var22 = new File(tbsCoreShareDecoupleDir, "1");
									var21 = new Properties();
									if (var22 != null && var22.exists() && var21 != null) {
										var19 = new FileInputStream(var22);
										var20 = new BufferedInputStream(var19);
										var21.load(var20);
									} else {
										var17 = false;
									}
								} catch (Exception var49) {
									var49.printStackTrace();
								} finally {
									if (var20 != null) {
										try {
											var20.close();
										} catch (IOException var47) {
											var47.printStackTrace();
										}
									}
									
								}
								
								if (var17) {
									File[] var55 = tbsCoreShareDecoupleDir.listFiles();
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-552);
									
									for (int var23 = 0; var23 < var55.length; ++var23) {
										File var24 = var55[var23];
										if (!"1".equals(var24.getName()) && !var24.getName().endsWith(".dex") && !"tbs.conf".equals(var24.getName()) && !var24.isDirectory() && !var24.getName().endsWith(".prof")) {
											String var25 = ApkMd5Util.getMD5(var24);
											String var26 = var21.getProperty(var24.getName(), "");
											if (var26.equals("") || !var25.equals(var26)) {
												var18 = false;
												TbsLog.e("TbsInstaller", "md5_check_failure for (" + var24.getName() + ")" + " targetMd5:" + var26 + ", realMd5:" + var25);
												break;
											}
											
											TbsLog.i("TbsInstaller", "md5_check_success for (" + var24.getName() + ")");
											var18 = true;
										}
									}
								}
								
								TbsLog.i("TbsInstaller", "copyTbsCoreInThread - md5_check_success:" + var18);
								if (var17 && !var18) {
									TbsLog.e("TbsInstaller", "copyTbsCoreInThread - md5 incorrect -> delete destTmpDir!");
									FileHelper.delete(tbsCoreShareDecoupleDir, true);
									TbsLogReport.getInstance(var2).setInstallErrorCode(213, (String) "TbsCopy-Verify md5 fail after copying tbs core!");
									TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-532);
									return;
								}
								
								TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread success!");
								this.g(var2, true);
								var22 = TbsDownload.b(var1);
								if (var22 != null && var22.exists()) {
									File var56 = new File(var22, TbsDownloader.getOverSea(var2) ? "x5.oversea.tbs.org" : TbsDownloader.getBackupFileName(false));
									TbsDownload.backupTbsApk(var56, var2);
								}
								
								TbsCoreInstallPropertiesHelper.getInstance(var2).setCopyCoreVerAndCopyStatus(var3, 1);
								if (this.k) {
									TbsLogReport.getInstance(var2).setInstallErrorCode(220, (String) "continueInstallWithout core success");
								} else {
									TbsLogReport.getInstance(var2).setInstallErrorCode(220, (String) "success");
								}
								
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-533);
								TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread success -- version:" + var3);
								if (VERSION.SDK_INT >= 11) {
									var4 = var2.getSharedPreferences("tbs_preloadx5_check_cfg_file", 4);
								} else {
									var4 = var2.getSharedPreferences("tbs_preloadx5_check_cfg_file", 0);
								}
								
								try {
									Editor var57 = var4.edit();
									var57.putInt("tbs_preload_x5_counter", 0);
									var57.putInt("tbs_preload_x5_recorder", 0);
									var57.putInt("tbs_preload_x5_version", var3);
									var57.commit();
								} catch (Throwable var48) {
									TbsLog.e("TbsInstaller", "Init tbs_preload_x5_counter#2 exception:" + Log.getStackTraceString(var48));
								}
								
								FsSpaceUtil.a(var2);
							} else {
								TbsLog.i("TbsInstaller", "TbsInstaller-copyTbsCoreInThread fail!");
								TbsCoreInstallPropertiesHelper.getInstance(var2).setCopyCoreVerAndCopyStatus(var3, 2);
								FileHelper.delete(tbsCoreShareDecoupleDir, false);
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-534);
								TbsLogReport.getInstance(var2).setInstallErrorCode(212, (String) "copy fail!");
							}
						} else {
							if (tbsCoreShareDir == null) {
								TbsLogReport.getInstance(var2).setInstallErrorCode(213, (String) "src-dir is null when copying tbs core!");
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-535);
							}
							
							if (tbsCoreShareDecoupleDir == null) {
								TbsLogReport.getInstance(var2).setInstallErrorCode(214, (String) "dst-dir is null when copying tbs core!");
								TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-536);
							}
						}
					} catch (Exception var51) {
						TbsLogReport.getInstance(var2).setInstallErrorCode(215, (String) var51.toString());
						TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-537);
						
						try {
							FileHelper.delete(tbsCoreShareDecoupleDir, false);
							TbsCoreInstallPropertiesHelper.getInstance(var2).setCopyCoreVerAndCopyStatus(0, -1);
						} catch (Exception var46) {
							TbsLog.e("TbsInstaller", "[TbsInstaller-copyTbsCoreInThread] delete dstTmpDir throws exception:" + var46.getMessage() + "," + var46.getCause());
						}
					} finally {
						REENTRANT_LOCK.unlock();
						LOCK.unlock();
						this.releaseTbsInstallingFileLock();
					}
				} else {
					this.releaseTbsInstallingFileLock();
					TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-538);
				}
				
			}
		}
	}
	
	private boolean e(Context var1, String var2) {
		PackageInfo var3 = null;
		
		try {
			var3 = var1.getPackageManager().getPackageInfo(var2, 0);
		} catch (NameNotFoundException var5) {
			var3 = null;
		}
		
		return var3 != null;
	}
	
	Context b(Context var1, String var2) {
		Context var3 = null;
		
		try {
			if (var1.getPackageName() != var2 && TbsPVConfig.getInstance(var1).isEnableNoCoreGray()) {
				return null;
			} else {
				var3 = var1.createPackageContext(var2, 2);
				return var3;
			}
		} catch (Exception var5) {
			return null;
		}
	}
	
	public void b(Context var1, File var2, int var3) {
		FileOutputStream var4 = FileHelper.getLockFile(var1, true, "core_unzip.lock");
		FileLock var5 = FileHelper.lockStream(var1, var4);
		if (var5 != null) {
			TbsLog.i("TbsInstaller", "unzipTbsCoreToThirdAppTmpInThread #1");
			boolean var6 = this.a(var1, var2, false);
			TbsLog.i("TbsInstaller", "unzipTbsCoreToThirdAppTmpInThread result is " + var6);
			if (var6) {
				a().a(var1, var3);
			}
			
			FileHelper.releaseFileLock(var5, var4);
		} else {
			TbsLog.i("TbsInstaller", "can not get Core unzip FileLock,skip!!!");
		}
		
	}
	
	private boolean a(Context var1, File var2) {
		long var3 = 0L;
		boolean var5 = this.a(var1, var2, false);
		return var5;
	}
	
	private boolean a(Context var1, File var2, boolean var3) {
		TbsLog.i("TbsInstaller", "TbsInstaller-unzipTbs start");
		if (!FileHelper.fileWritten(var2)) {
			TbsLogReport.getInstance(var1).setInstallErrorCode(204, (String) "apk is invalid!");
			TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-520);
			return false;
		} else {
			File var4;
			try {
				var4 = QbSdk.getTbsFolderDir(var1);
				File var5 = null;
				if (var3) {
					var5 = new File(var4, "core_share_decouple");
				} else {
					var5 = new File(var4, "core_unzip_tmp");
				}
				
				if (var5 != null && var5.exists() && !TbsDownloader.a(var1)) {
					FileHelper.delete(var5);
				}
			} catch (Throwable var23) {
				TbsLog.e("TbsInstaller", "TbsInstaller-unzipTbs -- delete unzip folder if exists exception" + Log.getStackTraceString(var23));
			}
			
			var4 = null;
			if (var3) {
				var4 = this.getCoreDir(var1, 2);
			} else {
				var4 = this.getCoreDir(var1, 0);
			}
			
			if (var4 == null) {
				TbsLogReport.getInstance(var1).setInstallErrorCode(205, (String) "tmp unzip dir is null!");
				TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-521);
				return false;
			} else {
				boolean var27 = false;
				
				try {
					boolean var7;
					try {
						FileHelper.forceCreateNewDir(var4);
						if (var3) {
							FileHelper.delete(var4, true);
						}
						
						boolean var6 = FileHelper.a(var2, var4);
						if (var6) {
							var6 = this.a(var4, var1);
						}
						
						if (var3) {
							String[] var28 = var4.list();
							
							File var9;
							for (int var8 = 0; var8 < var28.length; ++var8) {
								var9 = new File(var4, var28[var8]);
								if (var9.getName().endsWith(".dex")) {
									var9.delete();
								}
							}
							
							try {
								File var30 = getTbsCorePrivateDir(var1);
								var9 = new File(var30, "x5.tbs");
								var9.delete();
							} catch (Exception var22) {
							}
						}
						
						if (!var6) {
							FileHelper.delete(var4);
							TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-522);
							TbsLog.e("TbsInstaller", "copyFileIfChanged -- delete tmpTbsCoreUnzipDir#1! exist:" + var4.exists());
						} else {
							this.g(var1, true);
							if (var3) {
								File var29 = this.getTbsCoreShareDecoupleDir(var1);
								FileHelper.delete(var29, true);
								var4.renameTo(var29);
								TbsShareManager.b(var1);
							}
						}
						
						var7 = var6;
						return var7;
					} catch (IOException var24) {
						TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-523);
						TbsLogReport.getInstance(var1).setInstallErrorCode(206, (Throwable) var24);
						if (var4 != null && var4.exists()) {
							var27 = true;
						}
						
						var7 = false;
						return var7;
					} catch (Exception var25) {
						TbsDownloadConfig.getInstance(var1).setInstallInterruptCode(-523);
						TbsLogReport.getInstance(var1).setInstallErrorCode(207, (Throwable) var25);
						if (var4 != null && var4.exists()) {
							var27 = true;
						}
					}
					
					var7 = false;
					return var7;
				} finally {
					try {
						if (var27 && var4 != null) {
							FileHelper.delete(var4);
							TbsLog.e("TbsInstaller", "copyFileIfChanged -- delete tmpTbsCoreUnzipDir#2! exist:" + var4.exists());
						}
					} catch (Throwable var21) {
						TbsLog.e("TbsInstaller", "copyFileIfChanged -- delete tmpTbsCoreUnzipDir#2! exception:" + Log.getStackTraceString(var21));
					}
					
					TbsLog.i("TbsInstaller", "TbsInstaller-unzipTbs done");
				}
			}
		}
	}
	
	private boolean a(File var1, Context var2) {
		TbsLog.i("TbsInstaller", "finalCheckForTbsCoreValidity - " + var1 + ", " + var2);
		boolean var3 = true;
		boolean var4 = true;
		FileInputStream var5 = null;
		BufferedInputStream var6 = null;
		Properties var7 = null;
		
		try {
			File var8 = new File(var1, "1");
			var7 = new Properties();
			if (var8 != null && var8.exists() && var7 != null) {
				var5 = new FileInputStream(var8);
				var6 = new BufferedInputStream(var5);
				var7.load(var6);
			} else {
				var3 = false;
			}
		} catch (Exception var19) {
			var19.printStackTrace();
		} finally {
			if (var6 != null) {
				try {
					var6.close();
				} catch (IOException var18) {
					var18.printStackTrace();
				}
			}
			
		}
		
		TbsLog.i("TbsInstaller", "finalCheckForTbsCoreValidity - need_check:" + var3);
		if (var3) {
			File[] var21 = var1.listFiles();
			
			for (int var9 = 0; var9 < var21.length; ++var9) {
				File var10 = var21[var9];
				if (!"1".equals(var10.getName()) && !var10.getName().endsWith(".dex") && !"tbs.conf".equals(var10.getName()) && !var10.isDirectory() && !var10.getName().endsWith(".prof")) {
					String var11 = ApkMd5Util.getMD5(var10);
					String var12 = var7.getProperty(var10.getName(), "");
					if (var12.equals("") || !var12.equals(var11)) {
						var4 = false;
						TbsLog.e("TbsInstaller", "md5_check_failure for (" + var10.getName() + ")" + " targetMd5:" + var12 + ", realMd5:" + var11);
						break;
					}
					
					TbsLog.i("TbsInstaller", "md5_check_success for (" + var10.getName() + ")");
					var4 = true;
				}
			}
		}
		
		TbsLog.i("TbsInstaller", "finalCheckForTbsCoreValidity - md5_check_success:" + var4);
		if (var3 && !var4) {
			TbsLog.e("TbsInstaller", "finalCheckForTbsCoreValidity - Verify failed after unzipping!");
			return false;
		} else {
			TbsLog.i("TbsInstaller", "finalCheckForTbsCoreValidity success!");
			return true;
		}
	}
	
	public boolean e(Context context) {
		try {
			File var2 = new File(FileHelper.getBackUpDir(context, 4), TbsDownloader.getBackupFileName(true));
			File var3 = a().getCoreDir(context, 2);
			FileHelper.forceCreateNewDir(var3);
			FileHelper.delete(var3, true);
			FileHelper.a(var2, var3);
			String[] var5 = var3.list();
			
			for (int var6 = 0; var6 < var5.length; ++var6) {
				File var7 = new File(var3, var5[var6]);
				if (var7.getName().endsWith(".dex")) {
					var7.delete();
				}
			}
			
			this.i(context, 2);
			return true;
		} catch (Exception var8) {
			return false;
		}
	}
	
	private void i(Context var1, int var2) {
		File var3 = a().getCoreDir(var1, var2);
		a().g(var1, true);
		File var4 = this.getTbsCoreShareDecoupleDir(var1);
		FileHelper.delete(var4, true);
		var3.renameTo(var4);
		TbsShareManager.b(var1);
	}
	
	private boolean doTbsDexOpt(Context context, int var2) {
		TbsLog.i("TbsInstaller", "TbsInstaller-doTbsDexOpt start - dirMode: " + var2);
		
		try {
			File var3 = null;
			switch (var2) {
				case 0:
					if (TbsDownloader.a(context)) {
						return true;
					}
					
					var3 = this.getCoreDir(context, 0);
					break;
				case 1:
					var3 = this.getCoreDir(context, 1);
					break;
				case 2:
					var3 = this.getTbsCoreShareDir(context);
					break;
				default:
					TbsLog.e("TbsInstaller", "doDexoptOrDexoat mode error: " + var2);
					return false;
			}
			
			boolean var4 = false;
			
			try {
				String var5 = System.getProperty("java.vm.version");
				var4 = var5 != null && var5.startsWith("2");
			} catch (Throwable var8) {
				TbsLogReport.getInstance(context).setInstallErrorCode(226, (Throwable) var8);
			}
			
			boolean var10 = VERSION.SDK_INT == 23;
			boolean var6 = TbsDownloadConfig.getInstance(context).mPreferences.getBoolean("tbs_stop_preoat", false);
			boolean var7 = var4 && var10 && !var6;
			if (var7 && this.doDexoatForArtVm(context, var3)) {
				TbsLog.i("TbsInstaller", "doTbsDexOpt -- doDexoatForArtVm");
				return true;
			}
			
			if (!var4) {
				TbsLog.i("TbsInstaller", "doTbsDexOpt -- doDexoptForDavlikVM");
				return this.doDexoptForDavlikVM(context, var3);
			}
			
			TbsLog.i("TbsInstaller", "doTbsDexOpt -- is ART mode, skip!");
		} catch (Exception var9) {
			var9.printStackTrace();
			TbsLogReport.getInstance(context).setInstallErrorCode(209, (String) var9.toString());
		}
		
		TbsLog.i("TbsInstaller", "TbsInstaller-doTbsDexOpt done");
		return true;
	}
	
	public synchronized boolean a(final Context var1, final Context var2) {
		TbsLog.i("TbsInstaller", "TbsInstaller--quickDexOptForThirdPartyApp");
		if (p) {
			return true;
		} else {
			p = true;
			(new Thread() {
				public void run() {
					TbsLog.i("TbsInstaller", "TbsInstaller--quickDexOptForThirdPartyApp thread start");
					
					try {
						File var1x;
						if (var2 == null) {
							var1x = new File(TbsShareManager.getHostCorePathAppDefined());
						} else if (TbsShareManager.isThirdPartyApp(var1)) {
							if (TbsShareManager.c(var1) != null && TbsShareManager.c(var1).contains("decouple")) {
								var1x = TbsInstaller.this.getTbsCoreShareDecoupleDir(var2);
							} else {
								var1x = TbsInstaller.this.getTbsCoreShareDir(var2);
							}
						} else {
							var1x = TbsInstaller.this.getTbsCoreShareDir(var2);
						}
						
						File var2x = TbsInstaller.this.getTbsCoreShareDir(var1);
						int var3 = VERSION.SDK_INT;
						FileFilter var4;
						if (var3 != 19 && var3 < 21) {
							var4 = new FileFilter() {
								public boolean accept(File var1x) {
									return var1x.getName().endsWith(".dex");
								}
							};
							FileHelper.forceTransferFile(var1x, var2x, var4);
						}
						
						var4 = new FileFilter() {
							public boolean accept(File var1x) {
								return var1x.getName().endsWith("tbs.conf");
							}
						};
						FileHelper.forceTransferFile(var1x, var2x, var4);
						TbsLog.i("TbsInstaller", "TbsInstaller--quickDexOptForThirdPartyApp thread done");
					} catch (Exception var5) {
						var5.printStackTrace();
					}
					
				}
			}).start();
			return true;
		}
	}
	
	boolean f(Context var1) {
		if (TbsShareManager.getHostCorePathAppDefined() != null) {
			return true;
		} else {
			try {
				PackageInfo var2 = var1.getPackageManager().getPackageInfo(var1.getPackageName(), 64);
				Signature var3 = var2.signatures[0];
				if (var1.getPackageName().equals("com.tencent.mtt")) {
					if (!var3.toCharsString().equals("3082023f308201a8a00302010202044c46914a300d06092a864886f70d01010505003064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f301e170d3130303732313036313835305a170d3430303731333036313835305a3064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f30819f300d06092a864886f70d010101050003818d0030818902818100c209077044bd0d63ea00ede5b839914cabcc912a87f0f8b390877e0f7a2583f0d5933443c40431c35a4433bc4c965800141961adc44c9625b1d321385221fd097e5bdc2f44a1840d643ab59dc070cf6c4b4b4d98bed5cbb8046e0a7078ae134da107cdf2bfc9b440fe5cb2f7549b44b73202cc6f7c2c55b8cfb0d333a021f01f0203010001300d06092a864886f70d010105050003818100b007db9922774ef4ccfee81ba514a8d57c410257e7a2eba64bfa17c9e690da08106d32f637ac41fbc9f205176c71bde238c872c3ee2f8313502bee44c80288ea4ef377a6f2cdfe4d3653c145c4acfedbfbadea23b559d41980cc3cdd35d79a68240693739aabf5c5ed26148756cf88264226de394c8a24ac35b712b120d4d23a")) {
						return false;
					}
				} else if (var1.getPackageName().equals("com.tencent.mm")) {
					if (!var3.toCharsString().equals("308202eb30820254a00302010202044d36f7a4300d06092a864886f70d01010505003081b9310b300906035504061302383631123010060355040813094775616e67646f6e673111300f060355040713085368656e7a68656e31353033060355040a132c54656e63656e7420546563686e6f6c6f6779285368656e7a68656e2920436f6d70616e79204c696d69746564313a3038060355040b133154656e63656e74204775616e677a686f7520526573656172636820616e6420446576656c6f706d656e742043656e7465723110300e0603550403130754656e63656e74301e170d3131303131393134333933325a170d3431303131313134333933325a3081b9310b300906035504061302383631123010060355040813094775616e67646f6e673111300f060355040713085368656e7a68656e31353033060355040a132c54656e63656e7420546563686e6f6c6f6779285368656e7a68656e2920436f6d70616e79204c696d69746564313a3038060355040b133154656e63656e74204775616e677a686f7520526573656172636820616e6420446576656c6f706d656e742043656e7465723110300e0603550403130754656e63656e7430819f300d06092a864886f70d010101050003818d0030818902818100c05f34b231b083fb1323670bfbe7bdab40c0c0a6efc87ef2072a1ff0d60cc67c8edb0d0847f210bea6cbfaa241be70c86daf56be08b723c859e52428a064555d80db448cdcacc1aea2501eba06f8bad12a4fa49d85cacd7abeb68945a5cb5e061629b52e3254c373550ee4e40cb7c8ae6f7a8151ccd8df582d446f39ae0c5e930203010001300d06092a864886f70d0101050500038181009c8d9d7f2f908c42081b4c764c377109a8b2c70582422125ce545842d5f520aea69550b6bd8bfd94e987b75a3077eb04ad341f481aac266e89d3864456e69fba13df018acdc168b9a19dfd7ad9d9cc6f6ace57c746515f71234df3a053e33ba93ece5cd0fc15f3e389a3f365588a9fcb439e069d3629cd7732a13fff7b891499")) {
						return false;
					}
				} else if (var1.getPackageName().equals("com.tencent.mobileqq")) {
					if (!var3.toCharsString().equals("30820253308201bca00302010202044bbb0361300d06092a864886f70d0101050500306d310e300c060355040613054368696e61310f300d06035504080c06e58c97e4baac310f300d06035504070c06e58c97e4baac310f300d060355040a0c06e885bee8aeaf311b3019060355040b0c12e697a0e7babfe4b89ae58aa1e7b3bbe7bb9f310b30090603550403130251513020170d3130303430363039343831375a180f32323834303132303039343831375a306d310e300c060355040613054368696e61310f300d06035504080c06e58c97e4baac310f300d06035504070c06e58c97e4baac310f300d060355040a0c06e885bee8aeaf311b3019060355040b0c12e697a0e7babfe4b89ae58aa1e7b3bbe7bb9f310b300906035504031302515130819f300d06092a864886f70d010101050003818d0030818902818100a15e9756216f694c5915e0b529095254367c4e64faeff07ae13488d946615a58ddc31a415f717d019edc6d30b9603d3e2a7b3de0ab7e0cf52dfee39373bc472fa997027d798d59f81d525a69ecf156e885fd1e2790924386b2230cc90e3b7adc95603ddcf4c40bdc72f22db0f216a99c371d3bf89cba6578c60699e8a0d536950203010001300d06092a864886f70d01010505000381810094a9b80e80691645dd42d6611775a855f71bcd4d77cb60a8e29404035a5e00b21bcc5d4a562482126bd91b6b0e50709377ceb9ef8c2efd12cc8b16afd9a159f350bb270b14204ff065d843832720702e28b41491fbc3a205f5f2f42526d67f17614d8a974de6487b2c866efede3b4e49a0f916baa3c1336fd2ee1b1629652049")) {
						return false;
					}
				} else if (var1.getPackageName().equals("com.tencent.tbs")) {
					if (!var3.toCharsString().equals("3082023f308201a8a00302010202044c46914a300d06092a864886f70d01010505003064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f301e170d3130303732313036313835305a170d3430303731333036313835305a3064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f30819f300d06092a864886f70d010101050003818d0030818902818100c209077044bd0d63ea00ede5b839914cabcc912a87f0f8b390877e0f7a2583f0d5933443c40431c35a4433bc4c965800141961adc44c9625b1d321385221fd097e5bdc2f44a1840d643ab59dc070cf6c4b4b4d98bed5cbb8046e0a7078ae134da107cdf2bfc9b440fe5cb2f7549b44b73202cc6f7c2c55b8cfb0d333a021f01f0203010001300d06092a864886f70d010105050003818100b007db9922774ef4ccfee81ba514a8d57c410257e7a2eba64bfa17c9e690da08106d32f637ac41fbc9f205176c71bde238c872c3ee2f8313502bee44c80288ea4ef377a6f2cdfe4d3653c145c4acfedbfbadea23b559d41980cc3cdd35d79a68240693739aabf5c5ed26148756cf88264226de394c8a24ac35b712b120d4d23a")) {
						return false;
					}
				} else if (var1.getPackageName().equals("com.qzone")) {
					if (!var3.toCharsString().equals("308202ad30820216a00302010202044c26cea2300d06092a864886f70d010105050030819a310b3009060355040613023836311530130603550408130c4265696a696e672043697479311530130603550407130c4265696a696e67204369747931263024060355040a131d515a6f6e65205465616d206f662054656e63656e7420436f6d70616e7931183016060355040b130f54656e63656e7420436f6d70616e79311b301906035504031312416e64726f696420515a6f6e65205465616d301e170d3130303632373034303830325a170d3335303632313034303830325a30819a310b3009060355040613023836311530130603550408130c4265696a696e672043697479311530130603550407130c4265696a696e67204369747931263024060355040a131d515a6f6e65205465616d206f662054656e63656e7420436f6d70616e7931183016060355040b130f54656e63656e7420436f6d70616e79311b301906035504031312416e64726f696420515a6f6e65205465616d30819f300d06092a864886f70d010101050003818d003081890281810082d6aca037a9843fbbe88b6dd19f36e9c24ce174c1b398f3a529e2a7fe02de99c27539602c026edf96ad8d43df32a85458bca1e6fbf11958658a7d6751a1d9b782bf43a8c19bd1c06bdbfd94c0516326ae3cf638ac42bb470580e340c46e6f306a772c1ef98f10a559edf867f3f31fe492808776b7bd953b2cba2d2b2d66a44f0203010001300d06092a864886f70d0101050500038181006003b04a8a8c5be9650f350cda6896e57dd13e6e83e7f891fc70f6a3c2eaf75cfa4fc998365deabbd1b9092159edf4b90df5702a0d101f8840b5d4586eb92a1c3cd19d95fbc1c2ac956309eda8eef3944baf08c4a49d3b9b3ffb06bc13dab94ecb5b8eb74e8789aa0ba21cb567f538bbc59c2a11e6919924a24272eb79251677")) {
						return false;
					}
				} else if (var1.getPackageName().equals("com.tencent.qqpimsecure") && !var3.toCharsString().equals("30820239308201a2a00302010202044c96f48f300d06092a864886f70d01010505003060310b300906035504061302434e310b300906035504081302474431123010060355040713094775616e677a686f753110300e060355040a130754656e63656e74310b3009060355040b130233473111300f0603550403130857696c736f6e57753020170d3130303932303035343334335a180f32303635303632333035343334335a3060310b300906035504061302434e310b300906035504081302474431123010060355040713094775616e677a686f753110300e060355040a130754656e63656e74310b3009060355040b130233473111300f0603550403130857696c736f6e577530819f300d06092a864886f70d010101050003818d0030818902818100b56e79dbb1185a79e52d792bb3d0bb3da8010d9b87da92ec69f7dc5ad66ab6bfdff2a6a1ed285dd2358f28b72a468be7c10a2ce30c4c27323ed4edcc936080e5bedc2cbbca0b7e879c08a631182793f44bb3ea284179b263410c298e5f6831032c9702ba4a74e2ccfc9ef857f12201451602fc8e774ac59d6398511586c83d1d0203010001300d06092a864886f70d0101050500038181002475615bb65b8d8786b890535802948840387d06b1692ff3ea47ef4c435719ba1865b81e6bfa6293ce31747c3cd6b34595b485cc1563fd90107ba5845c28b95c79138f0dec288940395bc10f92f2b69d8dc410999deb38900974ce9984b678030edfba8816582f56160d87e38641288d8588d2a31e20b89f223d788dd35cc9c8")) {
					return false;
				}
				
				return true;
			} catch (Exception var4) {
				TbsLog.i("TbsInstaller", "TbsInstaller-installLocalTbsCore getPackageInfo fail");
				return false;
			}
		}
	}
	
	public Context d(Context var1, int var2) {
		TbsLog.i("TbsInstaller", "TbsInstaller--getTbsCoreHostContext tbsCoreTargetVer=" + var2);
		if (var2 <= 0) {
			return null;
		} else {
			String[] var3 = TbsShareManager.getCoreProviderAppList();
			
			for (int var4 = 0; var4 < var3.length; ++var4) {
				if (!var1.getPackageName().equalsIgnoreCase(var3[var4]) && this.e(var1, var3[var4])) {
					Context var5 = this.b(var1, var3[var4]);
					if (var5 != null) {
						if (!this.f(var5)) {
							TbsLog.e("TbsInstaller", "TbsInstaller--getTbsCoreHostContext " + var3[var4] + " illegal signature go on next");
						} else {
							int var6 = this.getTbsCoreInstalledVerInNolock(var5);
							TbsLog.i("TbsInstaller", "TbsInstaller-getTbsCoreHostContext hostTbsCoreVer=" + var6);
							if (var6 != 0 && var6 == var2) {
								TbsLog.i("TbsInstaller", "TbsInstaller-getTbsCoreHostContext targetApp=" + var3[var4]);
								return var5;
							}
						}
					}
				}
			}
			
			return null;
		}
	}
	
	int c(Context var1, String var2) {
		PackageInfo var3 = var1.getPackageManager().getPackageArchiveInfo(var2, 0);
		return var3 != null ? var3.versionCode : 0;
	}
	
	public void g(Context var1) {
		boolean var2 = true;
		
		try {
			var2 = TbsDownloadConfig.getInstance().getTbsCoreLoadRenameFileLockEnable();
		} catch (Throwable var4) {
		}
		
		if (var2 && l != null) {
			FileHelper.releaseTbsCoreRenameFileLock(var1, l);
		}
		
	}
	
	private boolean w(Context var1) {
		TbsLog.i("TbsInstaller", "Tbsinstaller getTbsCoreRenameFileLock #1 ");
		boolean var2 = true;
		
		try {
			var2 = TbsDownloadConfig.getInstance().getTbsCoreLoadRenameFileLockEnable();
		} catch (Throwable var4) {
		}
		
		TbsLog.i("TbsInstaller", "Tbsinstaller getTbsCoreRenameFileLock #2  enabled is " + var2);
		if (!var2) {
			l = X5CoreEngine.getInstance().tryTbsCoreLoadFileLock(var1);
		} else {
			l = FileHelper.getTbsCoreRenameFileLock(var1);
		}
		
		if (l == null) {
			TbsLog.i("TbsInstaller", "getTbsCoreRenameFileLock## failed!");
			return false;
		} else {
			TbsLog.i("TbsInstaller", "Tbsinstaller getTbsCoreRenameFileLock true ");
			return true;
		}
	}
	
	private void generateNewTbsCoreFromUnzip(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--generateNewTbsCoreFromUnzip");
		if (!this.w(var1)) {
			TbsLog.i("TbsInstaller", "get rename fileLock#4 ## failed!");
		} else {
			try {
				this.deleteOldCore(var1);
				this.renameShareDir(var1);
				TbsLog.i("TbsInstaller", "after renameTbsCoreShareDir");
				if (!TbsShareManager.isThirdPartyApp(var1)) {
					TbsLog.i("TbsInstaller", "prepare to shareTbsCore");
					TbsShareManager.a(var1);
				} else {
					TbsLog.i("TbsInstaller", "is thirdapp and not chmod");
				}
				
				TbsCoreInstallPropertiesHelper.getInstance(var1).setDexoptRetryNum(0);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setUnzipRetryNum(0);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setUnlzmaStatus(0);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", 0);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, 3);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallApkPath("");
				TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("tpatch_num", 0);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
				if (!TbsShareManager.isThirdPartyApp(var1)) {
					int var2 = TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_decouplecoreversion", 0);
					if (var2 > 0 && var2 != a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var1) && var2 == a().getTbsCoreInstalledVerInNolock(var1)) {
						this.coreShareCopyToDecouple(var1);
					} else {
						TbsLog.i("TbsInstaller", "TbsInstaller--generateNewTbsCoreFromUnzip #1 deCoupleCoreVersion is " + var2 + " getTbsCoreShareDecoupleCoreVersion is " + a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var1) + " getTbsCoreInstalledVerInNolock is " + a().getTbsCoreInstalledVerInNolock(var1));
					}
				}
				
				if (TbsShareManager.isThirdPartyApp(var1)) {
					TbsShareManager.writeCoreInfoForThirdPartyApp(var1, this.getTbsCoreInstalledVerWithLock(var1), true);
				}
				
				a.set(0);
				o = 0;
			} catch (Throwable var3) {
				var3.printStackTrace();
				TbsLogReport.getInstance(var1).setInstallErrorCode(219, (String) ("exception when renameing from unzip:" + var3.toString()));
				TbsLog.e("TbsInstaller", "TbsInstaller--generateNewTbsCoreFromUnzip Exception", true);
			}
			
			this.g(var1);
		}
	}
	
	private void generateNewTbsCoreFromTpatch(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--generateNewTbsCoreFromTpatch");
		if (!this.w(var1)) {
			TbsLog.i("TbsInstaller", "get rename fileLock#4 ## failed!");
		} else {
			try {
				this.deleteOldCore(var1);
				this.renameTbsTpatchCoreDir(var1);
				TbsShareManager.a(var1);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setTpatchVerAndTpatchStatus(0, -1);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("tpatch_num", 0);
				a.set(0);
			} catch (Exception var3) {
				var3.printStackTrace();
				TbsLogReport.getInstance(var1).setInstallErrorCode(242, (String) ("exception when renameing from tpatch:" + var3.toString()));
			}
			
			this.g(var1);
		}
	}
	
	private void generateNewTbsCoreFromCopy(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--generateNewTbsCoreFromCopy");
		if (!this.w(var1)) {
			TbsLog.i("TbsInstaller", "get rename fileLock#4 ## failed!");
		} else {
			try {
				this.deleteOldCore(var1);
				this.renameTbsCoreCopyDir(var1);
				TbsShareManager.a(var1);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setCopyCoreVerAndCopyStatus(0, 3);
				TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("tpatch_num", 0);
				if (!TbsShareManager.isThirdPartyApp(var1)) {
					int var2 = TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_decouplecoreversion", 0);
					if (var2 > 0 && var2 != a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var1) && var2 == a().getTbsCoreInstalledVerInNolock(var1)) {
						this.coreShareCopyToDecouple(var1);
					} else {
						TbsLog.i("TbsInstaller", "TbsInstaller--generateNewTbsCoreFromCopy #1 deCoupleCoreVersion is " + var2 + " getTbsCoreShareDecoupleCoreVersion is " + a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var1) + " getTbsCoreInstalledVerInNolock is " + a().getTbsCoreInstalledVerInNolock(var1));
					}
				}
				
				a.set(0);
			} catch (Exception var3) {
				var3.printStackTrace();
				TbsLogReport.getInstance(var1).setInstallErrorCode(219, (String) ("exception when renameing from copy:" + var3.toString()));
			}
			
			this.g(var1);
		}
	}
	
	int getTbsCoreVersionIdDir(String dir) {
		if (dir == null) {
			return 0;
		} else {
			FileInputStream var2 = null;
			BufferedInputStream var3 = null;
			
			try {
				File var4 = new File(dir);
				File var22 = new File(var4, "tbs.conf");
				if (var22 != null && var22.exists()) {
					Properties var23 = new Properties();
					var2 = new FileInputStream(var22);
					var3 = new BufferedInputStream(var2);
					var23.load(var3);
					var3.close();
					String var7 = var23.getProperty("tbs_core_version");
					if (var7 != null) {
						return Integer.parseInt(var7);
					} else {
						return (byte) 0;
					}
				} else {
					return (byte) 0;
				}
			} catch (Exception var20) {
				return (byte) 0;
			} finally {
				if (var3 != null) {
					try {
						var3.close();
					} catch (IOException var19) {
					}
				}
				
			}
		}
	}
	
	int getTbsVersion(Context context, int type) {
		return this.getTbsVersion(this.getCoreDir(context, type));
	}
	
	int getTbsVersion(File tbsShareDir) {
		FileInputStream var2 = null;
		BufferedInputStream var3 = null;
		
		try {
			byte var5;
			try {
				TbsLog.i("TbsInstaller", "TbsInstaller--getTbsVersion  tbsShareDir is " + tbsShareDir);
				File var4 = new File(tbsShareDir, "tbs.conf");
				if (var4 != null && var4.exists()) {
					Properties var21 = new Properties();
					var2 = new FileInputStream(var4);
					var3 = new BufferedInputStream(var2);
					var21.load(var3);
					var3.close();
					String var6 = var21.getProperty("tbs_core_version");
					if (var6 == null) {
						byte var22 = 0;
						return var22;
					} else {
						int var7 = Integer.parseInt(var6);
						return var7;
					}
				} else {
					var5 = 0;
					return var5;
				}
			} catch (Exception var19) {
				var5 = 0;
				return var5;
			}
		} finally {
			if (var3 != null) {
				try {
					var3.close();
				} catch (IOException var18) {
				}
			}
			
		}
	}
	
	public String getTbsCoreVersion_InTbsCoreShareConfig(Context var1, String var2) {
		if (TextUtils.isEmpty(var2)) {
			return null;
		} else {
			FileInputStream var3 = null;
			BufferedInputStream var4 = null;
			
			File var6;
			try {
				File var5 = this.getTbsCoreShareDir(var1);
				var6 = new File(var5, "tbs.conf");
				Properties var7;
				if (var6 != null && var6.exists()) {
					var7 = new Properties();
					var3 = new FileInputStream(var6);
					var4 = new BufferedInputStream(var3);
					var7.load(var4);
					var4.close();
					String var8 = var7.getProperty(var2);
					return var8;
				}
				// www
				return null;
			} catch (Exception var19) {
				var6 = null;
			} finally {
				if (var4 != null) {
					try {
						var4.close();
					} catch (IOException var18) {
					}
				}
				
			}
			
			return null;
		}
	}
	
	int getTbsCoreVersion_InTbsCoreShareDecoupleConfig(Context var1) {
		FileInputStream var2 = null;
		BufferedInputStream var3 = null;
		
		byte var5;
		try {
			File var4 = this.getTbsCoreShareDecoupleDir(var1);
			File var22 = new File(var4, "tbs.conf");
			if (var22 != null && var22.exists()) {
				Properties var23 = new Properties();
				var2 = new FileInputStream(var22);
				var3 = new BufferedInputStream(var2);
				var23.load(var3);
				var3.close();
				String var7 = var23.getProperty("tbs_core_version");
				if (var7 == null) {
					return (byte) 0;
				}
				
				return Integer.parseInt(var7);
			}
			
			return (byte) 0;
		} catch (Exception var20) {
			var5 = 0;
		} finally {
			if (var3 != null) {
				try {
					var3.close();
				} catch (IOException ignored) {
				}
			}
			
		}
		
		return var5;
	}
	
	int getTbsCoreInstalledVerInNolock(Context var1) {
		FileInputStream var2 = null;
		BufferedInputStream var3 = null;
		
		int var9;
		try {
			File var4 = this.getTbsCoreShareDir(var1);
			File var23 = new File(var4, "tbs.conf");
			if (var23 == null || !var23.exists()) {
				byte var24 = 0;
				return var24;
			}
			
			Properties var6 = new Properties();
			var2 = new FileInputStream(var23);
			var3 = new BufferedInputStream(var2);
			var6.load(var3);
			var3.close();
			String var7 = var6.getProperty("tbs_core_version");
			if (var7 == null) {
				byte var25 = 0;
				return var25;
			}
			
			int var8 = Integer.parseInt(var7);
			if (o == 0) {
				o = var8;
			}
			
			var9 = var8;
		} catch (Exception var21) {
			TbsLog.i("TbsInstaller", "TbsInstaller--getTbsCoreInstalledVerInNolock Exception=" + var21.toString());
			byte var5 = 0;
			return var5;
		} finally {
			if (var3 != null) {
				try {
					var3.close();
				} catch (IOException var20) {
					TbsLog.i("TbsInstaller", "TbsInstaller--getTbsCoreInstalledVerInNolock IOException=" + var20.toString());
				}
			}
			
		}
		
		return var9;
	}
	
	int j(Context var1) {
		return o != 0 ? o : this.getTbsCoreInstalledVerInNolock(var1);
	}
	
	void k(Context var1) {
		if (o == 0) {
			o = this.getTbsCoreInstalledVerInNolock(var1);
		}
	}
	
	boolean getHasTbsCoreShareConfigF(Context var1) {
		File var2 = this.getTbsCoreShareDir(var1);
		File var3 = new File(var2, "tbs.conf");
		return var3 != null && var3.exists();
	}
	
	int getTbsCoreInstalledVerWithLock(Context var1) {
		if (!this.getTbsInstallingFileLock(var1)) {
			return -1;
		} else {
			boolean var2 = REENTRANT_LOCK.tryLock();
			TbsLog.i("TbsInstaller", "TbsInstaller--getTbsCoreInstalledVerWithLock locked=" + var2);
			if (var2) {
				FileInputStream var3 = null;
				BufferedInputStream var4 = null;
				
				byte var6;
				try {
					File var5 = this.getTbsCoreShareDir(var1);
					File var29 = new File(var5, "tbs.conf");
					if (var29 == null || !var29.exists()) {
						byte var30 = 0;
						return var30;
					}
					
					Properties var7 = new Properties();
					var3 = new FileInputStream(var29);
					var4 = new BufferedInputStream(var3);
					var7.load(var4);
					var4.close();
					String var8 = var7.getProperty("tbs_core_version");
					if (var8 != null) {
						a.set(Integer.parseInt(var8));
						int var31 = (Integer) a.get();
						return var31;
					}
					
					byte var9 = 0;
					return var9;
				} catch (Exception var27) {
					TbsLog.i("TbsInstaller", "TbsInstaller--getTbsCoreInstalledVerWithLock Exception=" + var27.toString());
					var6 = 0;
				} finally {
					if (var4 != null) {
						try {
							var4.close();
						} catch (IOException var26) {
							TbsLog.i("TbsInstaller", "TbsInstaller--getTbsCoreInstalledVerWithLock IOException=" + var26.toString());
						}
					}
					
					try {
						if (REENTRANT_LOCK.isHeldByCurrentThread()) {
							REENTRANT_LOCK.unlock();
						}
					} catch (Throwable var25) {
						TbsLog.e("TbsInstaller", "TbsRenameLock.unlock exception: " + var25);
					}
					
					this.releaseTbsInstallingFileLock();
				}
				
				return var6;
			} else {
				this.releaseTbsInstallingFileLock();
				return 0;
			}
		}
	}
	
	private void deleteOldCore(Context context) {
		TbsLog.i("TbsInstaller", "TbsInstaller--deleteOldCore");
		FileHelper.delete(this.getTbsCoreShareDir(context), false);
	}
	
	private void renameShareDir(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--renameShareDir");
		File var2 = this.getCoreDir(var1, 0);
		File var3 = this.getTbsCoreShareDir(var1);
		if (var2 != null && var3 != null) {
			boolean var4 = var2.renameTo(var3);
			TbsLog.i("TbsInstaller", "renameTbsCoreShareDir rename success=" + var4);
			if (var1 != null && "com.tencent.mm".equals(var1.getApplicationContext().getApplicationInfo().packageName)) {
				if (var4) {
					TbsLogReport.getInstance(var1).setInstallErrorCode(230, (String) " ");
				} else {
					TbsLogReport.getInstance(var1).setInstallErrorCode(231, (String) " ");
				}
			}
			
			this.g(var1, false);
		} else {
			TbsLog.i("TbsInstaller", "renameTbsCoreShareDir return,tmpTbsCoreUnzipDir=" + var2 + "tbsSharePath=" + var3);
		}
	}
	
	public boolean coreShareCopyToDecouple(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--coreShareCopyToDecouple #0");
		File var2 = this.getTbsCoreShareDir(var1);
		File var3 = this.getTbsCoreShareDecoupleDir(var1);
		
		try {
			FileHelper.delete(var3, true);
			FileFilter var4 = new FileFilter() {
				public boolean accept(File var1) {
					return !var1.getName().endsWith(".dex") && !var1.getName().endsWith(".jar_is_first_load_dex_flag_file");
				}
			};
			FileHelper.forceTransferFile(var2, var3, var4);
			TbsShareManager.b(var1);
		} catch (Exception var5) {
			return false;
		}
		
		TbsLog.i("TbsInstaller", "TbsInstaller--coreShareCopyToDecouple success!!!");
		return true;
	}
	
	private void renameTbsCoreCopyDir(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--renameTbsCoreCopyDir");
		File var2 = this.getCoreDir(var1, 1);
		File var3 = this.getTbsCoreShareDir(var1);
		if (var2 != null && var3 != null) {
			var2.renameTo(var3);
			this.g(var1, false);
		}
	}
	
	private void renameTbsTpatchCoreDir(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--renameTbsTpatchCoreDir");
		File var2 = this.getCoreDir(var1, 5);
		File var3 = this.getTbsCoreShareDir(var1);
		if (var2 != null && var3 != null) {
			var2.renameTo(var3);
			this.g(var1, false);
		}
	}
	
	private void clearNewTbsCore(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--clearNewTbsCore");
		File var2 = this.getCoreDir(var1, 0);
		if (var2 != null) {
			FileHelper.delete(var2, false);
		}
		
		TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, 5);
		TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
		QbSdk.forceSysWebViewInner(var1, "TbsInstaller::clearNewTbsCore forceSysWebViewInner!");
	}
	
	void cleanStatusAndTmpDir(Context var1) {
		TbsLog.i("TbsInstaller", "TbsInstaller--cleanStatusAndTmpDir");
		TbsCoreInstallPropertiesHelper.getInstance(var1).setDexoptRetryNum(0);
		TbsCoreInstallPropertiesHelper.getInstance(var1).setUnzipRetryNum(0);
		TbsCoreInstallPropertiesHelper.getInstance(var1).setUnlzmaStatus(0);
		TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("incrupdate_retry_num", 0);
		if (!TbsDownloader.a(var1)) {
			TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallCoreVerAndInstallStatus(0, -1);
			TbsCoreInstallPropertiesHelper.getInstance(var1).setInstallApkPath("");
			TbsCoreInstallPropertiesHelper.getInstance(var1).setIntProperty("copy_retry_num", 0);
			TbsCoreInstallPropertiesHelper.getInstance(var1).setIncrupdateStatus(-1);
			TbsCoreInstallPropertiesHelper.getInstance(var1).setCopyCoreVerAndCopyStatus(0, -1);
			FileHelper.delete(this.getCoreDir(var1, 0), true);
			FileHelper.delete(this.getCoreDir(var1, 1), true);
		}
		
	}
	
	File getTbsCoreShareDir(Context context, Context var2) {
		File var3 = QbSdk.getTbsFolderDir(var2);
		File tbsShareDir = new File(var3, "core_share");
		if (tbsShareDir == null) {
			TbsLog.i("TbsInstaller", "getTbsCoreShareDir,tbsShareDir = null");
			return null;
		} else {
			if (!tbsShareDir.isDirectory() && (context == null || !TbsShareManager.isThirdPartyApp(context))) {
				boolean var5 = tbsShareDir.mkdir();
				if (!var5) {
					TbsLog.i("TbsInstaller", "getTbsCoreShareDir,mkdir false");
					return null;
				}
			}
			
			return tbsShareDir;
		}
	}
	
	File getTbsCoreShareDecoupleDir(Context var1) {
		File var2 = QbSdk.getTbsFolderDir(var1);
		File var3 = new File(var2, "core_share_decouple");
		if (var3 != null) {
			if (!var3.isDirectory()) {
				boolean var4 = var3.mkdir();
				if (!var4) {
					return null;
				}
			}
			
			return var3;
		} else {
			return null;
		}
	}
	
	File getTbsCoreShareDecoupleDir(Context var1, Context var2) {
		File tbsFolderDir = QbSdk.getTbsFolderDir(var2);
		File var4 = new File(tbsFolderDir, "core_share_decouple");
		if (var4 == null) {
			return null;
		} else {
			if (!var4.isDirectory() && (var1 == null || !TbsShareManager.isThirdPartyApp(var1))) {
				boolean var5 = var4.mkdir();
				if (!var5) {
					return null;
				}
			}
			
			return var4;
		}
	}
	
	File getTbsCoreShareDir(Context var1) {
		return this.getTbsCoreShareDir((Context) null, (Context) var1);
	}
	
	File getTbsShareDir(Context context) {
		File tbsFolderDir = QbSdk.getTbsFolderDir(context);
		File ret = new File(tbsFolderDir, "share");
		if (ret != null) {
			if (!ret.isDirectory()) {
				boolean var4 = ret.mkdir();
				if (!var4) {
					return null;
				}
			}
			
			return ret;
		} else {
			return null;
		}
	}
	
	static File getTbsCorePrivateDir(Context context) {
		File tbsFolderDir = QbSdk.getTbsFolderDir(context);
		File ret = new File(tbsFolderDir, "core_private");
		if (ret != null) {
			if (!ret.isDirectory()) {
				boolean var3 = ret.mkdir();
				if (!var3) {
					return null;
				}
			}
			
			return ret;
		} else {
			return null;
		}
	}
	
	File getCoreDir(Context context, int type) {
		return this.getCoreDir(context, type, true);
	}
	
	File getCoreDir(Context context, int type, boolean needMakeDir) {
		File tbsFolderDir = QbSdk.getTbsFolderDir(context);
		String folder = "";
		switch (type) {
			case 0:
				folder = "core_unzip_tmp";
				break;
			case 1:
				folder = "core_copy_tmp";
				break;
			case 2:
				folder = "core_unzip_tmp_decouple";
				break;
			case 3:
				folder = "core_share_backup";
				break;
			case 4:
				folder = "core_share_backup_tmp";
				break;
			case 5:
				folder = "tpatch_tmp";
				break;
			case 6:
				folder = "tpatch_decouple_tmp";
		}
		
		TbsLog.i("TbsInstaller", "type=" + type + "needMakeDir=" + needMakeDir + "folder=" + folder);
		File file = new File(tbsFolderDir, folder);
		if (file != null) {
			if (!file.isDirectory()) {
				if (!needMakeDir) {
					TbsLog.i("TbsInstaller", "getCoreDir,no need mkdir");
					return null;
				}
				
				boolean var7 = file.mkdir();
				if (!var7) {
					TbsLog.i("TbsInstaller", "getCoreDir,mkdir false");
					return null;
				}
			}
			
			return file;
		} else {
			TbsLog.i("TbsInstaller", "getCoreDir,tmpTbsShareDir = null");
			return null;
		}
	}
	
	boolean isTBSCoreLegal(Context var1, int var2) {
		boolean var3 = true;
		
		try {
			boolean var4 = false;
			File var5 = null;
			if (var4 = TbsShareManager.isThirdPartyApp(var1)) {
				if (!TbsShareManager.j(var1)) {
					var3 = false;
					TbsLog.e("TbsInstaller", "321");
					return var3;
				}
				
				var5 = new File(TbsShareManager.c(var1));
				if (var5.getAbsolutePath().contains("com.tencent.tbs")) {
					return true;
				}
			} else {
				var5 = this.getTbsCoreShareDir(var1);
			}
			
			if (var5 != null) {
				Long[][] var6 = n;
				int var7 = var6.length;
				
				for (int var8 = 0; var8 < var7; ++var8) {
					Long[] var9 = var6[var8];
					int var10 = var9[0].intValue();
					if (var2 == var10) {
						File var11 = new File(var5, "libmttwebview.so");
						if (var11 != null && var11.exists() && var11.length() == var9[1]) {
							TbsLog.d("TbsInstaller", "check so success: " + var2 + "; file: " + var11);
						} else {
							if (!var4) {
								File var12 = QbSdk.getTbsFolderDir(var1);
								FileHelper.delete(var12);
							}
							
							a.set(0);
							TbsLog.e("TbsInstaller", "322");
							var3 = false;
						}
						break;
					}
				}
			} else {
				TbsLog.e("TbsInstaller", "323");
				var3 = false;
			}
		} catch (Throwable var13) {
			TbsLog.e("TbsInstaller", "ISTBSCORELEGAL exception getMessage is " + var13.getMessage());
			TbsLog.e("TbsInstaller", "ISTBSCORELEGAL exception getCause is " + var13.getCause());
			var3 = false;
		}
		
		return var3;
	}
	
	public boolean a(Context var1, File[] var2) {
		return false;
	}
	
	synchronized boolean getTbsInstallingFileLock(Context context) {
		if (this.e > 0) {
			TbsLog.i("TbsInstaller", "getTbsInstallingFileLock success,is cached= true");
			++this.e;
			return true;
		} else {
			this.g = FileHelper.getLockFile(context, true, "tbslock.txt");
			if (this.g != null) {
				this.f = FileHelper.lockStream(context, this.g);
				if (this.f == null) {
					TbsLog.i("TbsInstaller", "getTbsInstallingFileLock tbsFileLockFileLock == null");
					return false;
				} else {
					TbsLog.i("TbsInstaller", "getTbsInstallingFileLock success,is cached= false");
					++this.e;
					return true;
				}
			} else {
				TbsLog.i("TbsInstaller", "getTbsInstallingFileLock get install fos failed");
				return false;
			}
		}
	}
	
	synchronized void releaseTbsInstallingFileLock() {
		if (this.e <= 0) {
			TbsLog.i("TbsInstaller", "releaseTbsInstallingFileLock currentTbsFileLockStackCount=" + this.e + "call stack:" + Log.getStackTraceString(new Throwable()));
		} else if (this.e > 1) {
			TbsLog.i("TbsInstaller", "releaseTbsInstallingFileLock with skip");
			--this.e;
		} else {
			if (this.e == 1) {
				TbsLog.i("TbsInstaller", "releaseTbsInstallingFileLock without skip");
				FileHelper.releaseFileLock(this.f, this.g);
				this.e = 0;
			}
			
		}
	}
	
	private boolean doDexoptForDavlikVM(Context context, File var2) {
		try {
			FileFilter var3 = new FileFilter() {
				public boolean accept(File var1) {
					return var1.getName().endsWith(".jar");
				}
			};
			File[] var4 = var2.listFiles(var3);
			int var5 = var4.length;
//			if (VERSION.SDK_INT < 16
//					&& context.getPackageName() != null
//					&& context.getPackageName().equalsIgnoreCase("com.tencent.tbs")) {
//				try {
//					Thread.sleep(5000L);
//				} catch (Exception var8) {
//				}
//			}
			
			ClassLoader var6 = context.getClassLoader();
			
			for (int var7 = 0; var7 < var5; ++var7) {
				TbsLog.i("TbsInstaller", "jarFile: " + var4[var7].getAbsolutePath());
				new DexClassLoader(var4[var7].getAbsolutePath(), var2.getAbsolutePath(), (String) null, var6);
			}
			
			return true;
		} catch (Exception var9) {
			var9.printStackTrace();
			TbsLogReport.getInstance(context).setInstallErrorCode(209, (String) var9.toString());
			TbsLog.i("TbsInstaller", "TbsInstaller-doTbsDexOpt done");
			return false;
		}
	}
	
	private boolean doDexoatForArtVm(Context context, File var2) {
		try {
			File dexjar = new File(var2, "tbs_sdk_extension_dex.jar");
			File dexdex = new File(var2, "tbs_sdk_extension_dex.dex");
			ClassLoader classLoader = context.getClassLoader();
			new DexClassLoader(dexjar.getAbsolutePath(), var2.getAbsolutePath(), (String) null, classLoader);
			String oat_command = TbsInstallerOATHelper.getOatCommand(context, dexdex.getAbsolutePath());
			if (TextUtils.isEmpty(oat_command)) {
				TbsLogReport.getInstance(context).setInstallErrorCode(226, (String) "can not find oat command");
				return false;
			} else {
				FileFilter var7 = new FileFilter() {
					public boolean accept(File var1) {
						return var1.getName().endsWith(".jar");
					}
				};
				File[] var8 = var2.listFiles(var7);
				File[] var9 = var8;
				int var10 = var8.length;
				
				for (int var11 = 0; var11 < var10; ++var11) {
					File var12 = var9[var11];
					String var13 = var12.getName().substring(0, var12.getName().length() - 4);
					String var14 = oat_command.replaceAll("tbs_sdk_extension_dex", var13);
					String var15 = "/system/bin/dex2oat " + var14 + " --dex-location=" + a().getTbsCoreShareDir(context) + File.separator + var13 + ".jar";
					java.lang.Process var16 = Runtime.getRuntime().exec(var15);
					var16.waitFor();
				}
				
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			TbsLogReport.getInstance(context).setInstallErrorCode(226, (Throwable) e);
			return false;
		}
	}
	
}