package com.tencent.smtt.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Looper;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import com.knziha.polymer.Utils.CMN;
import com.tencent.smtt.utils.ApkMd5Util;
import com.tencent.smtt.utils.AppUtil;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.TbsLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class TbsShareManager {
	private static Context a;
	private static boolean b;
	private static String c = null;
	public static boolean mHasQueryed = false;
	private static String mAvailableCorePath = null;
	private static int mAvailableCoreVersion = 0;
	private static String mSrcPackageName = null;
	private static boolean g = false;
	private static boolean h = false;
	private static boolean i = false;
	private static String j = null;
	private static boolean k = false;
	private static boolean l = false;
	
	static void a(Context var0) {
		TbsLog.i("TbsShareManager", "shareTbsCore #1");
		
		try {
			TbsLinuxToolsJni var1 = new TbsLinuxToolsJni(var0);
			File var2 = TbsInstaller.a().getTbsCoreShareDir(var0);
			a(var0, var1, var2);
			File var3 = TbsInstaller.a().getTbsShareDir(var0);
			TbsLog.i("TbsShareManager", "shareTbsCore tbsShareDir is " + var3.getAbsolutePath());
			var1.a(var3.getAbsolutePath(), "755");
		} catch (Throwable var4) {
			TbsLog.i("TbsShareManager", "shareTbsCore tbsShareDir error is " + var4.getMessage() + " ## " + var4.getCause());
			var4.printStackTrace();
		}
		
	}
	
	static void b(Context var0) {
		try {
			TbsLinuxToolsJni var1 = new TbsLinuxToolsJni(var0);
			File var2 = TbsInstaller.a().getTbsCoreShareDecoupleDir(var0);
			a(var0, var1, var2);
		} catch (Throwable var3) {
			var3.printStackTrace();
		}
		
	}
	
	private static void a(Context var0, TbsLinuxToolsJni var1, File var2) {
		TbsLog.i("TbsShareManager", "shareAllDirsAndFiles #1");
		if (var2 != null && var2.exists() && var2.isDirectory()) {
			TbsLog.i("TbsShareManager", "shareAllDirsAndFiles dir is " + var2.getAbsolutePath());
			var1.a(var2.getAbsolutePath(), "755");
			File[] var3 = var2.listFiles();
			int var4 = var3.length;
			
			for(int var5 = 0; var5 < var4; ++var5) {
				File var6 = var3[var5];
				if (var6.isFile()) {
					if (var6.getAbsolutePath().indexOf(".so") > 0) {
						var1.a(var6.getAbsolutePath(), "755");
					} else {
						var1.a(var6.getAbsolutePath(), "644");
					}
				} else if (var6.isDirectory()) {
					a(var0, var1, var6);
				} else {
					TbsLog.e("TbsShareManager", "unknown file type.", true);
				}
			}
			
		}
	}
	
	public static void setHostCorePathAppDefined(String var0) {
		c = var0;
	}
	
	public static String getHostCorePathAppDefined() {
		return c;
	}
	
	public static boolean isThirdPartyApp(Context var0) {
		try {
			if (a != null && a.equals(var0.getApplicationContext())) {
				return b;
			}
			
			a = var0.getApplicationContext();
			String var1 = a.getPackageName();
			String[] var2 = getCoreProviderAppList();
			String[] var3 = var2;
			int var4 = var2.length;
			
			for(int var5 = 0; var5 < var4; ++var5) {
				String var6 = var3[var5];
				if (var1.equals(var6)) {
					b = false;
					return false;
				}
			}
		} catch (Throwable var7) {
			var7.printStackTrace();
		}
		
		b = true;
		return true;
	}
	
	public static String[] getCoreProviderAppList() {
		return new String[]{"com.tencent.tbs", "com.tencent.mm", "com.tencent.mobileqq", "com.qzone", "com.tencent.qqlite"};
	}
	
	public static long getHostCoreVersions(Context var0) {
		long var1 = 0L;
		String[] var3 = getCoreProviderAppList();
		String[] var4 = var3;
		int var5 = var3.length;
		
		for(int var6 = 0; var6 < var5; ++var6) {
			String var7 = var4[var6];
			if (var7.equalsIgnoreCase("com.tencent.mm")) {
				var1 += (long)getSharedTbsCoreVersion(var0, var7) * 10000000000L;
			} else if (var7.equalsIgnoreCase("com.tencent.mobileqq")) {
				var1 += (long)getSharedTbsCoreVersion(var0, var7) * 100000L;
			} else if (var7.equalsIgnoreCase("com.qzone")) {
				var1 += (long)getSharedTbsCoreVersion(var0, var7);
			}
		}
		
		return var1;
	}
	
	public static int getSharedTbsCoreVersion(Context var0, String var1) {
		Context var2 = getPackageContext(var0, var1, true);
		return var2 != null ? TbsInstaller.a().getTbsCoreInstalledVerInNolock(var2) : 0;
	}
	
	public static int getCoreShareDecoupleCoreVersion(Context var0, String var1) {
		Context var2 = getPackageContext(var0, var1, true);
		return var2 != null ? TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(var2) : 0;
	}
	
	public static int getBackupCoreVersion(Context var0, String var1) {
		try {
			Context var2 = getPackageContext(var0, var1, false);
			File var3 = new File(FileHelper.getBackUpDir(var2, 4));
			File var4 = new File(var3, TbsDownloader.getBackupFileName(false));
			if (var4.exists()) {
				return ApkMd5Util.b(var4);
			}
		} catch (Throwable var5) {
		}
		
		return 0;
	}
	
	public static int getBackupDecoupleCoreVersion(Context var0, String var1) {
		try {
			Context var2 = getPackageContext(var0, var1, false);
			File var3 = new File(FileHelper.getBackUpDir(var2, 4));
			File var4 = new File(var3, TbsDownloader.getBackupFileName(true));
			if (var4.exists()) {
				return ApkMd5Util.b(var4);
			}
		} catch (Throwable var5) {
		}
		
		return 0;
	}
	
	public static File getBackupCoreFile(Context var0, String var1) {
		try {
			Context var2 = getPackageContext(var0, var1, false);
			File var3 = new File(FileHelper.getBackUpDir(var2, 4));
			File var4 = new File(var3, TbsDownloader.getBackupFileName(false));
			if (var4.exists()) {
				return var4;
			}
		} catch (Throwable var5) {
		}
		
		return null;
	}
	
	public static File getBackupDecoupleCoreFile(Context var0, String var1) {
		try {
			Context var2 = getPackageContext(var0, var1, true);
			File var3 = new File(FileHelper.getBackUpDir(var2, 4));
			File var4 = new File(var3, TbsDownloader.getBackupFileName(true));
			if (var4.exists()) {
				return var4;
			}
		} catch (Throwable var5) {
		}
		
		return null;
	}
	
	public static boolean getCoreDisabled() {
		return g;
	}
	
	static String c(Context var0) {
		j(var0);
		return mAvailableCorePath;
	}
	
	static String a() {
		return mAvailableCorePath;
	}
	
	static int d(Context var0) {
		return a(var0, true);
	}
	
	static int a(Context var0, boolean var1) {
		isShareTbsCoreAvailable(var0, var1);
		return mAvailableCoreVersion;
	}
	
	static Context e(Context var0) {
		Context var1 = null;
		j(var0);
		if (mSrcPackageName != null) {
			var1 = getPackageContext(var0, mSrcPackageName, true);
			if (!TbsInstaller.a().f(var1)) {
				var1 = null;
			}
		}
		
		return c != null ? a : var1;
	}
	
	private static boolean k(Context var0) {
		if (mSrcPackageName == null) {
			return false;
		} else if (mAvailableCoreVersion == getSharedTbsCoreVersion(var0, mSrcPackageName)) {
			return true;
		} else {
			return mAvailableCoreVersion == getCoreShareDecoupleCoreVersion(var0, mSrcPackageName);
		}
	}
	
	private static boolean l(Context var0) {
		if (QbSdk.getOnlyDownload()) {
			return false;
		} else {
			String[] var1 = getCoreProviderAppList();
			String[] var2 = var1;
			int var3 = var1.length;
			
			int var4;
			String var5;
			Context var6;
			for(var4 = 0; var4 < var3; ++var4) {
				var5 = var2[var4];
				if (mAvailableCoreVersion > 0 && mAvailableCoreVersion == getSharedTbsCoreVersion(var0, var5)) {
					var6 = getPackageContext(var0, var5, true);
					if (TbsInstaller.a().f(var0)) {
						mAvailableCorePath = TbsInstaller.a().getTbsCoreShareDir(var0, var6).getAbsolutePath();
						mSrcPackageName = var5;
						return true;
					}
				}
			}
			
			var2 = var1;
			var3 = var1.length;
			
			for(var4 = 0; var4 < var3; ++var4) {
				var5 = var2[var4];
				if (mAvailableCoreVersion > 0 && mAvailableCoreVersion == getCoreShareDecoupleCoreVersion(var0, var5)) {
					var6 = getPackageContext(var0, var5, true);
					if (TbsInstaller.a().f(var0)) {
						mAvailableCorePath = TbsInstaller.a().getTbsCoreShareDecoupleDir(var0, var6).getAbsolutePath();
						mSrcPackageName = var5;
						return true;
					}
				}
			}
			
			return false;
		}
	}
	
	public static int findCoreForThirdPartyApp(Context var0) {
		n(var0);
		TbsLog.i("TbsShareManager", "core_info mAvailableCoreVersion is " + mAvailableCoreVersion + " mAvailableCorePath is " + mAvailableCorePath + " mSrcPackageName is " + mSrcPackageName);
		if (mSrcPackageName == null) {
			TbsLog.e("TbsShareManager", "mSrcPackageName is null !!!");
		}
		
		if (mSrcPackageName != null && mSrcPackageName.equals("AppDefined")) {
			if (mAvailableCoreVersion != TbsInstaller.a().getTbsCoreVersionIdDir(c)) {
				mAvailableCoreVersion = 0;
				mAvailableCorePath = null;
				mSrcPackageName = null;
				TbsLog.i("TbsShareManager", "check AppDefined core is error src is " + mAvailableCoreVersion + " dest is " + TbsInstaller.a().getTbsCoreVersionIdDir(c));
			}
		} else if (!k(var0) && !l(var0)) {
			mAvailableCoreVersion = 0;
			mAvailableCorePath = null;
			mSrcPackageName = null;
			TbsLog.i("TbsShareManager", "core_info error checkCoreInfo is false and checkCoreInOthers is false ");
		}
		
		if (mAvailableCoreVersion > 0) {
			String var1 = "com.tencent.android.qqdownloader";
			String var2 = "com.jd.jrapp";
			ApplicationInfo var3 = var0.getApplicationInfo();
			boolean var4 = var1.equals(var3.packageName) || var2.equals(var3.packageName);
			boolean var5 = false;
			if (!var4) {
				var5 = QbSdk.a(var0, mAvailableCoreVersion);
			}
			
			if (var5 || g) {
				mAvailableCoreVersion = 0;
				mAvailableCorePath = null;
				mSrcPackageName = null;
				TbsLog.i("TbsShareManager", "core_info error QbSdk.isX5Disabled ");
			}
		}
		
		return mAvailableCoreVersion;
	}
	
	private static boolean m(Context var0) {
		if (var0 == null) {
			return false;
		} else {
			writeProperties(var0, Integer.toString(0), "", "", Integer.toString(0));
			return true;
		}
	}
	
	private static void c(Context var0, boolean var1) {
		FileInputStream var2 = null;
		FileOutputStream var3 = null;
		BufferedInputStream var4 = null;
		BufferedOutputStream var5 = null;
		
		try {
			File var6 = getTbsShareFile(var0, "core_info");
			if (var6 == null) {
				return;
			}
			
			var2 = new FileInputStream(var6);
			var4 = new BufferedInputStream(var2);
			Properties var7 = new Properties();
			var7.load(var4);
			var7.setProperty("core_disabled", String.valueOf(false));
			if (var1) {
				String var8 = TbsInstaller.a().getTbsCoreShareDir(var0).getAbsolutePath();
				String var9 = var0.getApplicationContext().getPackageName();
				int var10 = AppUtil.getVersionCode(var0);
				var7.setProperty("core_packagename", var9);
				var7.setProperty("core_path", var8);
				var7.setProperty("app_version", String.valueOf(var10));
			}
			
			var3 = new FileOutputStream(var6);
			var5 = new BufferedOutputStream(var3);
			var7.store(var5, (String)null);
		} catch (Throwable var25) {
			var25.printStackTrace();
		} finally {
			try {
				if (var4 != null) {
					var4.close();
				}
			} catch (Exception var24) {
			}
			
			try {
				if (var5 != null) {
					var5.close();
				}
			} catch (Exception var23) {
			}
			
		}
		
	}
	
	public static boolean forceLoadX5FromTBSDemo(Context var0) {
		if (var0 != null && !TbsInstaller.a().a((Context)var0, (File[])null)) {
			int var1 = getSharedTbsCoreVersion(var0, "com.tencent.tbs");
			if (var1 > 0) {
				Context var2 = getPackageContext(var0, "com.tencent.tbs", true);
				String var3 = TbsInstaller.a().getTbsCoreShareDir(var2).getAbsolutePath();
				writeProperties(var0, Integer.toString(var1), "com.tencent.tbs", var3, "1");
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public static synchronized void writeCoreInfoForThirdPartyApp(Context var0, int var1, boolean var2) {
		TbsLog.i("TbsShareManager", "writeCoreInfoForThirdPartyApp coreVersion is " + var1);
		if (var1 == 0) {
			m(var0);
			TbsDownloadConfig.getInstance(a).setDownloadInterruptCode(-401);
		} else {
			int var3 = h(var0);
			TbsLog.i("TbsShareManager", "writeCoreInfoForThirdPartyApp coreVersionFromConfig is " + var3);
			if (var3 < 0) {
				TbsDownloadConfig.getInstance(a).setDownloadInterruptCode(-402);
			} else if (var1 == var3) {
				if (d(var0) == 0 && !var2) {
					a(var0, var1);
				}
				
				c(var0, var2);
				TbsDownloadConfig.getInstance(a).setDownloadInterruptCode(-403);
			} else if (var1 < var3) {
				m(var0);
				TbsDownloadConfig.getInstance(a).setDownloadInterruptCode(-404);
			} else {
				int var4 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(var0);
				TbsLog.i("TbsShareManager", "writeCoreInfoForThirdPartyApp coreVersionFromCoreShare is " + var4);
				if (var1 < var4) {
					m(var0);
					TbsDownloadConfig.getInstance(a).setDownloadInterruptCode(-404);
				} else {
					String[] var5 = d(var0, var2);
					int var8;
					File var14;
					File var16;
					if (c != null) {
						if (var1 == TbsInstaller.a().getTbsCoreVersionIdDir(c)) {
							writeProperties(var0, Integer.toString(var1), "AppDefined", c, Integer.toString(1));
							
							try {
								File var27 = getTbsShareFile(var0, "core_info");
								if (!i && var27 != null) {
									TbsLinuxToolsJni var28 = new TbsLinuxToolsJni(a);
									var28.a(var27.getAbsolutePath(), "644");
									File var29 = TbsInstaller.a().getTbsShareDir(var0);
									var28.a(var29.getAbsolutePath(), "755");
									i = true;
								}
							} catch (Throwable var19) {
								var19.printStackTrace();
							}
							
							return;
						}
						
						if (var1 > TbsInstaller.a().getTbsCoreVersionIdDir(c)) {
							String[] var6 = var5;
							int var7 = var5.length;
							
							for(var8 = 0; var8 < var7; ++var8) {
								String var9 = var6[var8];
								if (var1 == getSharedTbsCoreVersion(var0, var9)) {
									Context var10 = getPackageContext(var0, var9, true);
									String var11 = TbsInstaller.a().getTbsCoreShareDir(var10).getAbsolutePath();
									int var12 = AppUtil.getVersionCode(var0);
									if (TbsInstaller.a().f(var10)) {
										File var13 = new File(c);
										var14 = new File(var11);
										FileFilter var15 = new FileFilter() {
											public boolean accept(File var1) {
												return !var1.getName().endsWith(".dex");
											}
										};
										
										try {
											FileHelper.forceTransferFile(var14, var13, var15);
											writeProperties(var0, Integer.toString(var1), "AppDefined", c, Integer.toString(1));
											var16 = getTbsShareFile(var0, "core_info");
											if (!i && var16 != null) {
												TbsLinuxToolsJni var17 = new TbsLinuxToolsJni(a);
												var17.a(var16.getAbsolutePath(), "644");
												File var18 = TbsInstaller.a().getTbsShareDir(var0);
												var17.a(var18.getAbsolutePath(), "755");
												i = true;
											}
										} catch (Throwable var20) {
											var20.printStackTrace();
										}
										
										return;
									}
								}
							}
						}
					}
					
					boolean var25 = false;
					String[] var26 = var5;
					var8 = var5.length;
					
					for(int var30 = 0; var30 < var8; ++var30) {
						String var31 = var26[var30];
						
						try {
							Context var32;
							String var33;
							int var34;
							TbsLinuxToolsJni var35;
							if (var1 == getSharedTbsCoreVersion(var0, var31)) {
								var32 = getPackageContext(var0, var31, true);
								if (var32 != null) {
									var33 = TbsInstaller.a().getTbsCoreShareDir(var32).getAbsolutePath();
									var34 = AppUtil.getVersionCode(var0);
									if (TbsInstaller.a().f(var32)) {
										if (!var31.equals(var0.getApplicationContext().getPackageName())) {
											TbsLog.i("TbsShareManager", "thirdAPP pre--> delete old core_share Directory:" + var1);
											TbsCoreInstallPropertiesHelper.getInstance(a).setIntProperty("remove_old_core", 1);
										}
										
										writeProperties(var0, Integer.toString(var1), var31, var33, Integer.toString(var34));
										
										try {
											var14 = getTbsShareFile(var0, "core_info");
											if (!i && var14 != null) {
												var35 = new TbsLinuxToolsJni(a);
												var35.a(var14.getAbsolutePath(), "644");
												var16 = TbsInstaller.a().getTbsShareDir(var0);
												var35.a(var16.getAbsolutePath(), "755");
												i = true;
											}
										} catch (Throwable var23) {
											var23.printStackTrace();
										}
										
										var25 = true;
										break;
									}
								}
							} else if (var1 == getCoreShareDecoupleCoreVersion(var0, var31)) {
								var32 = getPackageContext(var0, var31, true);
								var33 = TbsInstaller.a().getTbsCoreShareDecoupleDir(var32).getAbsolutePath();
								var34 = AppUtil.getVersionCode(var0);
								if (TbsInstaller.a().f(var32)) {
									if (!var31.equals(var0.getApplicationContext().getPackageName())) {
										TbsLog.i("TbsShareManager", "thirdAPP pre--> delete old core_share Directory:" + var1);
										var14 = TbsInstaller.a().getTbsCoreShareDir(var0);
										
										try {
											FileHelper.delete(var14);
											TbsLog.i("TbsShareManager", "thirdAPP success--> delete old core_share Directory");
										} catch (Throwable var22) {
											var22.printStackTrace();
										}
									}
									
									writeProperties(var0, Integer.toString(var1), var31, var33, Integer.toString(var34));
									
									try {
										var14 = getTbsShareFile(var0, "core_info");
										if (!i && var14 != null) {
											var35 = new TbsLinuxToolsJni(a);
											var35.a(var14.getAbsolutePath(), "644");
											var16 = TbsInstaller.a().getTbsShareDir(var0);
											var35.a(var16.getAbsolutePath(), "755");
											i = true;
										}
									} catch (Throwable var21) {
										var21.printStackTrace();
									}
									
									var25 = true;
									break;
								}
							}
						} catch (Exception var24) {
							TbsLog.i(var24);
						}
					}
					
					if (!var25 && !var2) {
						a(var0, var1);
					}
					
				}
			}
		}
	}
	
	private static String[] d(Context var0, boolean var1) {
		String[] var2 = null;
		if (QbSdk.getOnlyDownload()) {
			var2 = new String[]{var0.getApplicationContext().getPackageName()};
		} else {
			var2 = getCoreProviderAppList();
			if (var1) {
				var2 = new String[]{var0.getApplicationContext().getPackageName()};
			}
		}
		
		return var2;
	}
	
	private static void a(Context var0, int var1) {
		if (!TbsPVConfig.getInstance(a).isDisableHostBackupCore() && TbsInstaller.a().getTbsInstallingFileLock(var0)) {
			String[] var2 = new String[]{"com.tencent.tbs", "com.tencent.mm", "com.tencent.mobileqq", "com.qzone", var0.getPackageName()};
			TbsLog.i("TbsShareManager", "find host backup core to unzip #1" + Log.getStackTraceString(new Throwable()));
			String[] var3 = var2;
			int var4 = var2.length;
			
			for(int var5 = 0; var5 < var4; ++var5) {
				String var6 = var3[var5];
				Context var7;
				File var8;
				if (var1 == getBackupCoreVersion(var0, var6)) {
					var7 = getPackageContext(var0, var6, false);
					if (TbsInstaller.a().f(var7)) {
						var8 = getBackupCoreFile(var0, var6);
						if (ApkMd5Util.a(var0, var8, 0L, var1)) {
							TbsLog.i("TbsShareManager", "find host backup core to unzip normal coreVersion is " + var1 + " packageName is " + var6);
							TbsInstaller.a().b(var0, var8, var1);
							break;
						}
					}
				} else if (var1 == getBackupDecoupleCoreVersion(var0, var6)) {
					var7 = getPackageContext(var0, var6, false);
					if (TbsInstaller.a().f(var7)) {
						var8 = getBackupDecoupleCoreFile(var0, var6);
						if (ApkMd5Util.a(var0, var8, 0L, var1)) {
							TbsLog.i("TbsShareManager", "find host backup core to unzip decouple coreVersion is " + var1 + " packageName is " + var6);
							TbsInstaller.a().b(var0, var8, var1);
							break;
						}
					}
				}
			}
			
			TbsInstaller.a().releaseTbsInstallingFileLock();
		}
		
	}
	
	public static void writeProperties(Context var0, String var1, String var2, String var3, String var4) {
		TbsLog.i("TbsShareManager", "writeProperties coreVersion is " + var1 + " corePackageName is " + var2 + " corePath is " + var3);
		TbsLog.i("TbsShareManager", "writeProperties -- stack: " + Log.getStackTraceString(new Throwable("#")));
		FileInputStream var5 = null;
		FileOutputStream var6 = null;
		BufferedInputStream var7 = null;
		BufferedOutputStream var8 = null;
		
		try {
			File var9 = getTbsShareFile(var0, "core_info");
			if (var9 != null) {
				var5 = new FileInputStream(var9);
				var7 = new BufferedInputStream(var5);
				Properties var10 = new Properties();
				var10.load(var7);
				int var11 = 0;
				
				try {
					var11 = Integer.parseInt(var1);
				} catch (Exception var28) {
				}
				
				if (var11 != 0) {
					var10.setProperty("core_version", var1);
					var10.setProperty("core_disabled", String.valueOf(false));
					var10.setProperty("core_packagename", var2);
					var10.setProperty("core_path", var3);
					var10.setProperty("app_version", var4);
				} else {
					var10.setProperty("core_disabled", String.valueOf(true));
				}
				
				var6 = new FileOutputStream(var9);
				var8 = new BufferedOutputStream(var6);
				var10.store(var8, (String)null);
				l = false;
				TbsDownloadConfig.getInstance(a).setDownloadInterruptCode(-406);
				return;
			}
			
			TbsDownloadConfig.getInstance(a).setDownloadInterruptCode(-405);
		} catch (Throwable var29) {
			var29.printStackTrace();
			return;
		} finally {
			try {
				if (var7 != null) {
					var7.close();
				}
			} catch (Exception var27) {
				var27.printStackTrace();
			}
			
			try {
				if (var8 != null) {
					var8.close();
				}
			} catch (Exception var26) {
				var26.printStackTrace();
			}
			
		}
		
	}
	
	static synchronized String f(Context var0) {
		FileInputStream var1 = null;
		BufferedInputStream var2 = null;
		
		String var6;
		try {
			File var3 = getTbsShareFile(var0, "core_info");
			Properties var4;
			if (var3 == null) {
				var4 = null;
				return null;//w
			}
			
			var1 = new FileInputStream(var3);
			var2 = new BufferedInputStream(var1);
			var4 = new Properties();
			var4.load(var2);
			String var5 = var4.getProperty("core_packagename", "");
			if ("".equals(var5)) {
				var6 = null;
				return var6;//w
			}
			
			var6 = var5;
		} catch (Throwable var18) {
			var18.printStackTrace();
			return null;
		} finally {
			try {
				if (var2 != null) {
					var2.close();
				}
			} catch (Exception var17) {
			}
			
		}
		
		return var6;
	}
	
	static String g(Context var0) {
		StringBuilder var1 = null;
		
		try {
			n(var0);
			if (mAvailableCorePath == null || TextUtils.isEmpty(mAvailableCorePath)) {
				return null;
			}
			
			var1 = new StringBuilder(mAvailableCorePath);
			var1.append(File.separator);
			var1.append("res.apk");
		} catch (Throwable var3) {
			Log.e("", "getTbsResourcesPath exception: " + Log.getStackTraceString(var3));
			return null;
		}
		
		return var1.toString();
	}
	
	static synchronized int h(Context var0) {
		TbsLog.i("TbsShareManager", "readCoreVersionFromConfig #1");
		FileInputStream var1 = null;
		BufferedInputStream var2 = null;
		
		try {
			File var3 = getTbsShareFile(var0, "core_info");
			if (var3 == null) {
				TbsLog.i("TbsShareManager", "readCoreVersionFromConfig #2");
				byte var20 = 0;
				return var20;
			}
			
			var1 = new FileInputStream(var3);
			var2 = new BufferedInputStream(var1);
			Properties var4 = new Properties();
			var4.load(var2);
			String var5 = var4.getProperty("core_version", "");
			if ("".equals(var5)) {
				TbsLog.i("TbsShareManager", "readCoreVersionFromConfig #4");
				byte var21 = 0;
				return var21;
			}
			
			TbsLog.i("TbsShareManager", "readCoreVersionFromConfig #3");
			int var6 = Math.max(Integer.parseInt(var5), 0);
			return var6;
		} catch (Throwable var18) {
			var18.printStackTrace();
		} finally {
			try {
				if (var2 != null) {
					var2.close();
				}
			} catch (Exception var17) {
				var17.printStackTrace();
			}
			
		}
		
		TbsLog.i("TbsShareManager", "readCoreVersionFromConfig #5");
		return -2;
	}
	
	public static boolean getCoreFormOwn() {
		return k;
	}
	
	private static void n(Context var0) {
		if (!l) {
			Class var1 = TbsShareManager.class;
			synchronized(TbsShareManager.class) {
				if (!l) {
					FileInputStream var2 = null;
					BufferedInputStream var3 = null;
					
					try {
						File var4 = getTbsShareFile(var0, "core_info");
						if (var4 != null) {
							var2 = new FileInputStream(var4);
							var3 = new BufferedInputStream(var2);
							Properties var5 = new Properties();
							var5.load(var3);
							String var6 = var5.getProperty("core_version", "");
							if (!"".equals(var6)) {
								mAvailableCoreVersion = Math.max(Integer.parseInt(var6), 0);
								TbsLog.i("TbsShareManager", "loadProperties -- mAvailableCoreVersion: " + mAvailableCoreVersion + " " + Log.getStackTraceString(new Throwable("#")));
							}
							
							var6 = var5.getProperty("core_packagename", "");
							if (!"".equals(var6)) {
								mSrcPackageName = var6;
							}
							
							if (mSrcPackageName != null && a != null) {
								if (mSrcPackageName.equals(a.getPackageName())) {
									k = true;
								} else {
									k = false;
								}
							}
							
							var6 = var5.getProperty("core_path", "");
							if (!"".equals(var6)) {
								mAvailableCorePath = var6;
							}
							
							var6 = var5.getProperty("app_version", "");
							if (!"".equals(var6)) {
								j = var6;
							}
							
							var6 = var5.getProperty("core_disabled", "false");
							g = Boolean.parseBoolean(var6);
							l = true;
							return;
						}
					} catch (Throwable var18) {
						var18.printStackTrace();
						return;
					} finally {
						try {
							if (var3 != null) {
								var3.close();
							}
						} catch (Exception var17) {
							var17.printStackTrace();
						}
						
					}
					
				}
			}
		}
	}
	
	public static void forceToLoadX5ForThirdApp(Context context, boolean var1) {
		try {
			//CMN.Log("forceToLoadX5ForThirdApp",QbSdk.isNeedInitX5FirstTime(), isThirdPartyApp(context),QbSdk.getOnlyDownload(),  TbsInstaller.a().getTbsShareDir(context));
			if (!QbSdk.isNeedInitX5FirstTime()) {
				return;
			}
			
			if (!isThirdPartyApp(context)) {
				return;
			}
			
			if (QbSdk.getOnlyDownload()) {
				return;
			}
			
			File var2 = TbsInstaller.a().getTbsShareDir(context);
			if (var2 == null) {
				return;
			}
			
			if (var1) {
				//xxx
				File var3 = new File(var2, "core_info");
				CMN.Log("core_info::F::", var3, var3.exists());
				if (var3.exists()) {
					return;
				}
			}
			
			int var14;
			if (c != null) {
				var14 = TbsInstaller.a().getTbsCoreVersionIdDir(c);
				if (var14 > 0) {
					mAvailableCorePath = c;
					mSrcPackageName = "AppDefined";
					mAvailableCoreVersion = var14;
					TbsLog.i("TbsShareManager", "forceToLoadX5ForThirdApp #1 -- mAvailableCoreVersion: " + mAvailableCoreVersion + " " + Log.getStackTraceString(new Throwable("#")));
					writeProperties(context, Integer.toString(mAvailableCoreVersion), mSrcPackageName, mAvailableCorePath, Integer.toString(1));
					return;
				}
			}
			
			TbsLog.i("TbsShareManager", "forceToLoadX5ForThirdApp #1");
			var14 = h(context);
			int var4 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(context);
			TbsLog.i("TbsShareManager", "forceToLoadX5ForThirdApp coreVersionFromConfig is " + var14);
			TbsLog.i("TbsShareManager", "forceToLoadX5ForThirdApp coreVersionFromCoreShare is " + var4);
			String[] var5 = getCoreProviderAppList();
			String[] var6 = var5;
			int var7 = var5.length;
			
			int var8;
			String var9;
			int var10;
			Context var11;
			int var12;
			for(var8 = 0; var8 < var7; ++var8) {
				var9 = var6[var8];
				var10 = getCoreShareDecoupleCoreVersion(context, var9);
				if (var10 >= var14 && var10 >= var4 && var10 > 0) {
					var11 = getPackageContext(context, var9, true);
					mAvailableCorePath = TbsInstaller.a().getTbsCoreShareDecoupleDir(context, var11).getAbsolutePath();
					mSrcPackageName = var9;
					mAvailableCoreVersion = var10;
					TbsLog.i("TbsShareManager", "forceToLoadX5ForThirdApp #2 -- mAvailableCoreVersion: " + mAvailableCoreVersion + " " + Log.getStackTraceString(new Throwable("#")));
					if (QbSdk.canLoadX5FirstTimeThirdApp(context)) {
						var12 = AppUtil.getVersionCode(context);
						TbsLog.i("TbsShareManager", "forceToLoadX5ForThirdApp #2");
						writeProperties(context, Integer.toString(mAvailableCoreVersion), mSrcPackageName, mAvailableCorePath, Integer.toString(var12));
						return;
					}
					
					mAvailableCoreVersion = 0;
					mAvailableCorePath = null;
					mSrcPackageName = null;
				}
			}
			
			var6 = var5;
			var7 = var5.length;
			
			for(var8 = 0; var8 < var7; ++var8) {
				var9 = var6[var8];
				var10 = getSharedTbsCoreVersion(context, var9);
				if (var10 >= var14 && var10 >= var4 && var10 > 0) {
					var11 = getPackageContext(context, var9, true);
					mAvailableCorePath = TbsInstaller.a().getTbsCoreShareDir(context, var11).getAbsolutePath();
					mSrcPackageName = var9;
					mAvailableCoreVersion = var10;
					TbsLog.i("TbsShareManager", "forceToLoadX5ForThirdApp #3 -- mAvailableCoreVersion: " + mAvailableCoreVersion + " " + Log.getStackTraceString(new Throwable("#")));
					if (QbSdk.canLoadX5FirstTimeThirdApp(context)) {
						var12 = AppUtil.getVersionCode(context);
						writeProperties(context, Integer.toString(mAvailableCoreVersion), mSrcPackageName, mAvailableCorePath, Integer.toString(var12));
						return;
					}
					
					mAvailableCoreVersion = 0;
					mAvailableCorePath = null;
					mSrcPackageName = null;
				}
			}
			
			if (!TbsPVConfig.getInstance(a).isDisableHostBackupCore()) {
				if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
					var6 = var5;
					var7 = var5.length;
					
					for(var8 = 0; var8 < var7; ++var8) {
						var9 = var6[var8];
						var10 = getBackupCoreVersion(context, var9);
						File var15;
						if (var10 >= var14 && var10 >= var4 && var10 > 0) {
							TbsLog.i("TbsShareManager", "find host backup core to unzip forceload coreVersion is " + var10 + " packageName is " + var9);
							var15 = getBackupCoreFile(context, var9);
							TbsInstaller.a().a(context, var15, var10);
							TbsLog.i("TbsShareManager", "find host backup core to unzip forceload after unzip ");
							return;
						}
						
						var10 = getBackupDecoupleCoreVersion(context, var9);
						if (var10 >= var14 && var10 >= var4 && var10 > 0) {
							TbsLog.i("TbsShareManager", "find host backup core to unzip forceload decouple coreVersion is " + var10 + " packageName is " + var9);
							var15 = getBackupCoreFile(context, var9);
							TbsInstaller.a().a(context, var15, var10);
							TbsLog.i("TbsShareManager", "find host backup decouple core to unzip forceload after unzip ");
							return;
						}
					}
				} else {
					TbsLog.i("TbsShareManager", "in mainthread so do not find host backup core to install ");
				}
			}
		} catch (Exception var13) {
		}
		
	}
	
	public static File getTbsShareFile(Context var0, String var1) {
		File var2 = TbsInstaller.a().getTbsShareDir(var0);
		if (var2 == null) {
			return null;
		} else {
			File var3 = new File(var2, var1);
			if (var3 != null && var3.exists()) {
				return var3;
			} else {
				try {
					var3.createNewFile();
					return var3;
				} catch (IOException var5) {
					var5.printStackTrace();
					return null;
				}
			}
		}
	}
	
	static boolean isShareTbsCoreAvailableInner(Context var0) {
		try {
			if (mAvailableCoreVersion == 0) {
				findCoreForThirdPartyApp(var0);
			}
			
			if (mAvailableCoreVersion == 0) {
				TbsLog.addLog(994, (String)null);
				return false;
			} else {
				if (c == null) {
					if (mAvailableCoreVersion != 0 && getSharedTbsCoreVersion(var0, mSrcPackageName) == mAvailableCoreVersion) {
						return true;
					}
				} else if (mAvailableCoreVersion != 0 && TbsInstaller.a().getTbsCoreVersionIdDir(c) == mAvailableCoreVersion) {
					return true;
				}
				
				if (l(var0)) {
					return true;
				} else {
					TbsCoreLoadStat.getInstance().a(var0, 418, new Throwable("mAvailableCoreVersion=" + mAvailableCoreVersion + "; mSrcPackageName=" + mSrcPackageName + "; getSharedTbsCoreVersion(ctx, mSrcPackageName) is " + getSharedTbsCoreVersion(var0, mSrcPackageName) + "; getHostCoreVersions is " + getHostCoreVersions(var0)));
					mAvailableCorePath = null;
					mAvailableCoreVersion = 0;
					TbsLog.addLog(993, (String)null);
					QbSdk.forceSysWebViewInner(var0, "TbsShareManager::isShareTbsCoreAvailableInner forceSysWebViewInner!");
					return false;
				}
			}
		} catch (Throwable var2) {
			var2.printStackTrace();
			TbsLog.addLog(992, (String)null);
			return false;
		}
	}
	
	static boolean j(Context var0) {
		return isShareTbsCoreAvailable(var0, true);
	}
	
	static boolean isShareTbsCoreAvailable(Context var0, boolean var1) {
		if (isShareTbsCoreAvailableInner(var0)) {
			return true;
		} else {
			if (var1) {
				QbSdk.forceSysWebViewInner(var0, "TbsShareManager::isShareTbsCoreAvailable forceSysWebViewInner!");
			}
			
			return false;
		}
	}
	
	public static Context getPackageContext(Context var0, String var1, boolean var2) {
		Context var3 = null;
		
		try {
			if (var2 && !var0.getPackageName().equals(var1)) {
				TbsLog.i("TbsShareManager", "gray no core app,skip get context");
				if (TbsPVConfig.getInstance(var0).isEnableNoCoreGray() || VERSION.SDK_INT >= 29) {
					return null;
				}
			}
			
			var3 = var0.createPackageContext(var1, 2);
			return var3;
		} catch (NameNotFoundException var5) {
			return null;
		} catch (Exception var6) {
			var6.printStackTrace();
			return null;
		}
	}
}
