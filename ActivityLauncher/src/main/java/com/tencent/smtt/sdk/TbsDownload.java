package com.tencent.smtt.sdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.smtt.utils.ApkMd5Util;
import com.tencent.smtt.utils.Apn;
import com.tencent.smtt.utils.AppUtil;
import com.tencent.smtt.utils.FileHelper;
import com.tencent.smtt.utils.FsSpaceUtil;
import com.tencent.smtt.utils.TbsLog;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.SSLHandshakeException;

class TbsDownload {
   private boolean d = false;
   private static int e = 5;
   private static int f = 1;
   private static final String[] g = new String[]{"tbs_downloading_com.tencent.mtt", "tbs_downloading_com.tencent.mm", "tbs_downloading_com.tencent.mobileqq", "tbs_downloading_com.tencent.tbs", "tbs_downloading_com.qzone"};
   private Context context;
   private String i;
   private String j;
   private String k;
   private File l;
   private long m;
   private int n = 30000;
   private int o = 20000;
   private boolean p;
   private int q;
   private int r;
   private boolean s;
   private boolean t;
   private HttpURLConnection u;
   private String v;
   private TbsLogReport.TbsLogInfo w;
   private String x;
   private int y;
   private boolean z;
   private Handler handler;
   private Set<String> wifiSSIDs;
   private int C;
   private boolean downloadForeground;
   String a;
   String[] b;
   int c;

   public TbsDownload(Context var1) throws NullPointerException {
      this.C = e;
      this.b = null;
      this.c = 0;
      this.context = var1.getApplicationContext();
      this.w = TbsLogReport.getInstance(this.context).tbsLogInfo();
      this.wifiSSIDs = new HashSet();
      this.v = "tbs_downloading_" + this.context.getPackageName();
      TbsInstaller.a();
      this.l = TbsInstaller.getTbsCorePrivateDir(this.context);
      if (this.l == null) {
         throw new NullPointerException("TbsCorePrivateDir is null!");
      } else {
         this.g();
         this.x = null;
         this.y = -1;
      }
   }

   private void g() {
      this.q = 0;
      this.r = 0;
      this.m = -1L;
      this.k = null;
      this.p = false;
      this.s = false;
      this.t = false;
      this.z = false;
   }

   private void a(String var1) throws Exception {
      URL var2 = new URL(var1);
      if (this.u != null) {
         try {
            this.u.disconnect();
         } catch (Throwable var4) {
            TbsLog.e("TbsDownload", "[initHttpRequest] mHttpRequest.disconnect() Throwable:" + var4.toString());
         }
      }

      this.u = (HttpURLConnection)var2.openConnection();
      this.u.setRequestProperty("User-Agent", TbsDownloader.b(this.context));
      this.u.setRequestProperty("Accept-Encoding", "identity");
      this.u.setRequestMethod("GET");
      this.u.setInstanceFollowRedirects(false);
      this.u.setConnectTimeout(this.o);
      this.u.setReadTimeout(this.n);
   }

   private void h() {
      TbsLog.i("TbsDownload", "[TbsApkDownloader.closeHttpRequest]");
      if (this.u != null) {
         if (!this.s) {
            this.w.setResolveIp(this.a(this.u.getURL()));
         }

         try {
            this.u.disconnect();
         } catch (Throwable var4) {
            TbsLog.e("TbsDownload", "[closeHttpRequest] mHttpRequest.disconnect() Throwable:" + var4.toString());
         }

         this.u = null;
      }

      int var1 = this.w.a;
      if (!this.s && this.z) {
         this.w.setEventTime(System.currentTimeMillis());
         String var2 = Apn.getApnInfo(this.context);
         if (var2 == null) {
            var2 = "";
         }

         int var3 = Apn.getApnType(this.context);
         this.w.setApn(var2);
         this.w.setNetworkType(var3);
         if (var3 != this.y || !var2.equals(this.x)) {
            this.w.setNetworkChange(0);
         }

         if ((this.w.a == 0 || this.w.a == 107) && this.w.getDownFinalFlag() == 0) {
            if (!Apn.isNetworkAvailable(this.context)) {
               this.a(101, (String)null, true);
            } else if (!this.m()) {
               this.a(101, (String)null, true);
            }
         }

         if (TbsDownloader.a(this.context)) {
            TbsLogReport.getInstance(this.context).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD_DECOUPLE, this.w);
         } else {
            TbsLogReport.getInstance(this.context).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, this.w);
         }

         this.w.resetArgs();
         if (var1 != 100) {
            QbSdk.m.onDownloadFinish(var1);
         }
      } else if (!this.d) {
         TbsDownloader.downloading = false;
      }

   }

   private boolean copyTbsApkFromBackupToInstall(int var1) {
      try {
         File var2 = new File(this.l, "x5.tbs");
         File var3 = a(this.context);
         if (var3 != null) {
            File var4 = new File(var3, TbsDownloader.getOverSea(this.context) ? "x5.oversea.tbs.org" : TbsDownloader.getBackupFileName(false));
            var2.delete();
            FileHelper.forceTransferFile(var4, var2);
            if (!ApkMd5Util.a(this.context, var2, 0L, var1)) {
               TbsLog.i("TbsDownload", "[TbsApkDownloader.copyTbsApkFromBackupToInstall] verifyTbsApk error!!");
               return false;
            } else {
               return true;
            }
         } else {
            return false;
         }
      } catch (Exception var5) {
         var5.printStackTrace();
         TbsLog.e("TbsDownload", "[TbsApkDownloader.copyTbsApkFromBackupToInstall] Exception is " + var5.getMessage());
         return false;
      }
   }

   public boolean a(boolean var1, boolean var2) {
      boolean var3 = false;
      if (VERSION.SDK_INT == 23) {
         return false;
      } else {
         int var4 = TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("use_backup_version", 0);
         int var5 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(this.context);
         if (var4 == 0) {
            var4 = TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_download_version", 0);
            this.a = "by default key";
         } else {
            this.a = "by new key";
         }

         if (var4 != 0 && var4 != var5) {
            if (var2) {
               File var6 = TbsDownloader.a(var4);
               File var7;
               if (var6 != null && var6.exists()) {
                  var7 = new File(FileHelper.getBackUpDir(this.context, 4), TbsDownloader.getOverSea(this.context) ? "x5.oversea.tbs.org" : TbsDownloader.getBackupFileName(false));

                  try {
                     if (TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_download_version_type", 0) != 1) {
                        FileHelper.forceTransferFile(var6, var7);
                        var3 = true;
                     }
                  } catch (Exception var9) {
                     var9.printStackTrace();
                  }
               }

               var7 = this.i();
               if (var7 != null && var7.exists() && this.a(var7)) {
                  if (this.copyTbsApkFromBackupToInstall(var4)) {
                     TbsDownloadConfig.getInstance(this.context).mSyncMap.put("tbs_download_interrupt_code_reason", -214);
                     TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-214);
                     this.d(false);
                     if (var3) {
                        this.a(100, (String)("use local backup apk in startDownload" + this.a), true);
                        if (TbsDownloader.a(this.context)) {
                           TbsLogReport.getInstance(this.context).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD_DECOUPLE, this.w);
                        } else {
                           TbsLogReport.getInstance(this.context).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, this.w);
                        }

                        this.w.resetArgs();
                     }

                     return true;
                  }
               } else {
                  this.j();
                  if (var6 != null && var6.exists() && !ApkMd5Util.a(this.context, var6, 0L, var4) && var6 != null && var6.exists()) {
                     FileHelper.delete(var6);
                  }
               }
            }

            if (this.verifyTbsApk(false, var2)) {
               TbsDownloadConfig.getInstance(this.context).mSyncMap.put("tbs_download_interrupt_code_reason", -214);
               TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-214);
               this.d(false);
               return true;
            } else {
               boolean var10 = this.e(true);
               if (!var10) {
                  var10 = this.e(true);
                  if (!var10) {
                     TbsLog.e("TbsDownload", "[TbsApkDownloader] delete file failed!");
                     TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-301);
                  }
               }

               return false;
            }
         } else {
            return false;
         }
      }
   }

   public boolean verifyAndInstallDecoupleCoreFromBackup() {
      TbsLog.i("TbsApkDownloader", "verifyAndInstallDecoupleCoreFromBackup #1");

      try {
         File var1 = new File(FileHelper.getBackUpDir(this.context, 4), TbsDownloader.getBackupFileName(true));
         if (var1 != null && var1.exists()) {
            TbsLog.i("TbsApkDownloader", "verifyAndInstallDecoupleCoreFromBackup #2");
         } else {
            File var2 = TbsDownloader.b(TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_decouplecoreversion", -1));
            if (var2 != null && var2.exists()) {
               FileHelper.forceTransferFile(var2, var1);
            }
         }

         if (ApkMd5Util.a(this.context, var1, 0L, TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_decouplecoreversion", -1))) {
            TbsLog.i("TbsApkDownloader", "verifyAndInstallDecoupleCoreFromBackup #3");
            return TbsInstaller.a().e(this.context);
         }
      } catch (Exception var3) {
      }

      return false;
   }

   public void startDownload(boolean var1) {
      this.startDownload(var1, false);
   }

   public void startDownload(boolean var1, boolean var2) {
      boolean var3 = TbsInstaller.a().c(this.context);
      if (var3 && !var1) {
         TbsDownloader.downloading = false;
         TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-322);
      } else {
         int var4 = TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_responsecode", 0);
         boolean var5 = var4 == 1 || var4 == 2 || var4 == 4;
         if (!var2 && this.a(var1, var5)) {
            TbsDownloader.downloading = false;
         } else {
            this.downloadForeground = var1;
            this.i = TbsDownloadConfig.getInstance(this.context).mPreferences.getString("tbs_downloadurl", (String)null);
            String var6 = TbsDownloadConfig.getInstance(this.context).mPreferences.getString("tbs_downloadurl_list", (String)null);
            TbsLog.i("TbsDownload", "backupUrlStrings:" + var6, true);
            this.b = null;
            this.c = 0;
            if (!var1 && var6 != null && !"".equals(var6.trim())) {
               this.b = var6.trim().split(";");
            }

            TbsLog.i("TbsDownload", "[TbsApkDownloader.startDownload] mDownloadUrl=" + this.i + " backupUrlStrings=" + var6 + " mLocation=" + this.k + " mCanceled=" + this.s + " mHttpRequest=" + this.u);
            if (this.i == null && this.k == null) {
               QbSdk.m.onDownloadFinish(110);
               TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-302);
            } else if (this.u != null && !this.s) {
               QbSdk.m.onDownloadFinish(110);
               TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-303);
            } else if (!var1 && this.wifiSSIDs.contains(Apn.getWifiSSID(this.context))) {
               TbsLog.i("TbsDownload", "[TbsApkDownloader.startDownload] WIFI Unavailable");
               QbSdk.m.onDownloadFinish(110);
               TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-304);
            } else {
               this.g();
               TbsLog.i("TbsDownload", "STEP 1/2 begin downloading...", true);
               long var7 = TbsDownloadConfig.getInstance(this.context).getDownloadMaxflow();
               boolean var9 = false;
               long var10 = TbsDownloadConfig.getInstance(this.context).mPreferences.getLong("tbs_downloadflow", 0L);
               if (var1) {
                  this.C = f;
               } else {
                  this.C = e;
               }

               while(this.q <= this.C) {
                  if (this.r > 8) {
                     this.a(123, (String)null, true);
                     TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-306);
                     break;
                  }

                  long var12 = System.currentTimeMillis();

                  try {
                     if (!var1) {
                        long var16 = TbsDownloadConfig.getInstance(this.context).mPreferences.getLong("tbs_downloadstarttime", 0L);
                        if (var12 - var16 > 86400000L) {
                           TbsLog.i("TbsDownload", "[TbsApkDownloader.startDownload] OVER DOWNLOAD_PERIOD");
                           TbsDownloadConfig.getInstance(this.context).mSyncMap.put("tbs_downloadstarttime", var12);
                           TbsDownloadConfig.getInstance(this.context).mSyncMap.put("tbs_downloadflow", 0L);
                           TbsDownloadConfig.getInstance(this.context).commit();
                           var10 = 0L;
                        } else {
                           TbsLog.i("TbsDownload", "[TbsApkDownloader.startDownload] downloadFlow=" + var10);
                           if (var10 >= var7) {
                              TbsLog.i("TbsDownload", "STEP 1/2 begin downloading...failed because you exceeded max flow!", true);
                              this.a(112, (String)null, true);
                              TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-307);
                              break;
                           }
                        }

                        if (!FileHelper.hasEnoughFreeSpace(this.context)) {
                           TbsLog.i("TbsDownload", "DownloadBegin FreeSpace too small", true);
                           this.a(105, (String)null, true);
                           TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-308);
                           break;
                        }
                     }

                     this.z = true;
                     String var14 = this.k != null ? this.k : this.i;
                     TbsLog.i("TbsDownload", "try url:" + var14 + ",mRetryTimes:" + this.q, true);
                     if (!var14.equals(this.j)) {
                        this.w.setDownloadUrl(var14);
                     }

                     this.j = var14;
                     this.a(var14);
                     long var15 = 0L;
                     if (!this.p) {
                        var15 = this.l();
                        TbsLog.i("TbsDownload", "[TbsApkDownloader.startDownload] range=" + var15);
                        if (this.m <= 0L) {
                           TbsLog.i("TbsDownload", "STEP 1/2 begin downloading...current" + var15, true);
                           this.u.setRequestProperty("Range", "bytes=" + var15 + "-");
                        } else {
                           TbsLog.i("TbsDownload", "#1 STEP 1/2 begin downloading...current/total=" + var15 + "/" + this.m, true);
                           this.u.setRequestProperty("Range", "bytes=" + var15 + "-" + this.m);
                        }
                     }

                     this.w.setDownloadCancel(var15 == 0L ? 0 : 1);
                     int var17 = Apn.getApnType(this.context);
                     String var18 = Apn.getApnInfo(this.context);
                     if (this.x == null && this.y == -1) {
                        this.x = var18;
                        this.y = var17;
                     } else if (var17 != this.y || !var18.equals(this.x)) {
                        this.w.setNetworkChange(0);
                        this.x = var18;
                        this.y = var17;
                     }

                     if (this.q >= 1) {
                        this.u.addRequestProperty("Referer", this.i);
                     }

                     int var19 = this.u.getResponseCode();
                     TbsLog.i("TbsDownload", "[TbsApkDownloader.startDownload] responseCode=" + var19);
                     this.w.setHttpCode(var19);
                     if (!var1 && !TbsDownloader.getOverSea(this.context) && (Apn.getApnType(this.context) != 3 || Apn.getApnType(this.context) == 0) && !QbSdk.getDownloadWithoutWifi()) {
                        this.b();
                        if (QbSdk.m != null) {
                           QbSdk.m.onDownloadFinish(111);
                        }

                        TbsLog.i("TbsDownload", "Download is canceled due to NOT_WIFI error!", false);
                     }

                     if (this.s) {
                        TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-309);
                        break;
                     }

                     long var20;
                     if (var19 != 200 && var19 != 206) {
                        if (var19 >= 300 && var19 <= 307) {
                           String var53 = this.u.getHeaderField("Location");
                           if (TextUtils.isEmpty(var53)) {
                              this.a(124, (String)null, true);
                              TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-312);
                              break;
                           }

                           this.k = var53;
                           ++this.r;
                        } else {
                           this.a(102, (String)String.valueOf(var19), false);
                           if (var19 == 416) {
                              if (this.verifyTbsApk(true, var5)) {
                                 var9 = true;
                                 TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-214);
                              } else {
                                 this.e(false);
                                 TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-313);
                              }
                              break;
                           }

                           if ((var19 == 403 || var19 == 406) && this.m == -1L) {
                              TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-314);
                              break;
                           }

                           if (var19 != 202) {
                              if (this.q < this.C && var19 == 503) {
                                 var20 = Long.parseLong(this.u.getHeaderField("Retry-After"));
                                 this.a(var20);
                                 if (this.s) {
                                    TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-309);
                                    break;
                                 }
                              } else if (this.q >= this.C || var19 != 408 && var19 != 504 && var19 != 502 && var19 != 408) {
                                 if (this.l() > 0L || this.p || var19 == 410) {
                                    TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-315);
                                    break;
                                 }

                                 this.p = true;
                              } else {
                                 this.a(0L);
                                 if (this.s) {
                                    TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-309);
                                    break;
                                 }
                              }
                           }
                        }
                     } else {
                        this.m = (long)this.u.getContentLength() + var15;
                        TbsLog.i("TbsDownload", "[TbsApkDownloader.startDownload] mContentLength=" + this.m);
                        this.w.setPkgSize(this.m);
                        var20 = TbsDownloadConfig.getInstance(this.context).mPreferences.getLong("tbs_apkfilesize", 0L);
                        if (var20 != 0L && this.m != var20) {
                           TbsLog.i("TbsDownload", "DownloadBegin tbsApkFileSize=" + var20 + "  but contentLength=" + this.m, true);
                           if (var1 || !this.detectWifiNetworkAvailable() && (!QbSdk.getDownloadWithoutWifi() || !Apn.isNetworkAvailable(this.context))) {
                              this.a(101, (String)"WifiNetworkUnAvailable", true);
                              TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-304);
                              break;
                           }

                           if (this.b == null || !this.b(false)) {
                              this.a(113, (String)("tbsApkFileSize=" + var20 + "  but contentLength=" + this.m), true);
                              TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-310);
                              break;
                           }
                        } else {
                           FileOutputStream var22 = null;
                           InputStream var23 = null;
                           Object var24 = null;
                           TbsLog.i("TbsDownload", "[TbsApkDownloader.startDownload] begin readResponse");

                           try {
                              var23 = this.u.getInputStream();
                              if (var23 != null) {
                                 String var25 = this.u.getContentEncoding();
                                 if (var25 != null && var25.contains("gzip")) {
                                    var24 = new GZIPInputStream(var23);
                                 } else if (var25 != null && var25.contains("deflate")) {
                                    var24 = new InflaterInputStream(var23, new Inflater(true));
                                 } else {
                                    var24 = var23;
                                 }

                                 boolean var26 = false;
                                 long var27 = var15;
                                 long var29 = var15;
                                 byte[] var31 = new byte[8192];
                                 var22 = new FileOutputStream(new File(this.l, "x5.tbs.temp"), true);
                                 long var32 = System.currentTimeMillis();
                                 boolean var34 = false;

                                 label1479: {
                                    while(!this.s) {
                                       int var54 = ((InputStream)var24).read(var31, 0, 8192);
                                       if (var54 <= 0) {
                                          if (this.b != null && !this.verifyTbsApk(true, var5)) {
                                             if (!var1 && this.b(false)) {
                                                var34 = true;
                                                break label1479;
                                             }

                                             this.t = true;
                                             var9 = false;
                                             break label1479;
                                          }

                                          this.t = true;
                                          if (this.b != null) {
                                             var9 = true;
                                          }

                                          TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-311);
                                          break label1479;
                                       }

                                       var22.write(var31, 0, var54);
                                       var22.flush();
                                       if (!var1) {
                                          var10 += (long)var54;
                                          if (var10 >= var7) {
                                             TbsLog.i("TbsDownload", "STEP 1/2 begin downloading...failed because you exceeded max flow!", true);
                                             this.a(112, (String)("downloadFlow=" + var10 + " downloadMaxflow=" + var7), true);
                                             TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-307);
                                             break label1479;
                                          }

                                          if (!FileHelper.hasEnoughFreeSpace(this.context)) {
                                             TbsLog.i("TbsDownload", "DownloadEnd FreeSpace too small ", true);
                                             this.a(105, (String)("freespace=" + FsSpaceUtil.calcFsSpaceAvailable() + ",and minFreeSpace=" + TbsDownloadConfig.getInstance(this.context).getDownloadMinFreeSpace()), true);
                                             TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-308);
                                             break label1479;
                                          }
                                       }

                                       var12 = this.a(var12, (long)var54);
                                       long var35 = System.currentTimeMillis();
                                       var29 += (long)var54;
                                       if (var35 - var32 > 1000L) {
                                          TbsLog.i("TbsDownload", "#2 STEP 1/2 begin downloading...current/total=" + var29 + "/" + this.m, true);
                                          if (QbSdk.m != null) {
                                             int var37 = (int)((double)var29 / (double)this.m * 100.0D);
                                             QbSdk.m.onDownloadProgress(var37);
                                          }

                                          if (!var1 && var29 - var27 > 1048576L) {
                                             var27 = var29;
                                             if (!TbsDownloader.getOverSea(this.context) && (Apn.getApnType(this.context) != 3 || Apn.getApnType(this.context) == 0) && !QbSdk.getDownloadWithoutWifi()) {
                                                this.b();
                                                if (QbSdk.m != null) {
                                                   QbSdk.m.onDownloadFinish(111);
                                                }

                                                TbsLog.i("TbsDownload", "Download is paused due to NOT_WIFI error!", false);
                                                TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-304);
                                                break label1479;
                                             }
                                          }

                                          var32 = var35;
                                       }
                                    }

                                    TbsLog.i("TbsDownload", "STEP 1/2 begin downloading...Canceled!", true);
                                    TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-309);
                                 }

                                 if (var34) {
                                    continue;
                                 }
                              }
                           } catch (IOException var48) {
                              var48.printStackTrace();
                              if (!(var48 instanceof SocketTimeoutException) && !(var48 instanceof SocketException)) {
                                 if (!var1 && !FileHelper.hasEnoughFreeSpace(this.context)) {
                                    this.a(105, (String)("freespace=" + FsSpaceUtil.calcFsSpaceAvailable() + ",and minFreeSpace=" + TbsDownloadConfig.getInstance(this.context).getDownloadMinFreeSpace()), true);
                                    TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-308);
                                    break;
                                 }

                                 this.a(0L);
                                 if (!this.k()) {
                                    this.a(106, (String)this.a((Throwable)var48), false);
                                 } else {
                                    this.a(104, (String)this.a((Throwable)var48), false);
                                 }
                                 continue;
                              }

                              this.n = 100000;
                              this.a(0L);
                              this.a(103, (String)this.a((Throwable)var48), false);
                              continue;
                           } finally {
                              this.a((Closeable)var22);
                              this.a((Closeable)var24);
                              this.a((Closeable)var23);
                           }

                           if (!this.t) {
                              TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-319);
                           }
                           break;
                        }
                     }
                  } catch (Throwable var50) {
                     if (var50 instanceof SSLHandshakeException && !var1 && this.b != null && this.b(false)) {
                        TbsLog.e("TbsDownload", "[startdownload]url:" + this.k + " download exception：" + var50.toString());
                        this.a(125, (String)null, true);
                     } else {
                        var50.printStackTrace();
                        this.a(0L);
                        this.a(107, (String)this.a(var50), false);
                        if (this.s) {
                           TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-309);
                           break;
                        }
                     }

                     TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-316);
                  } finally {
                     if (!var1) {
                        TbsDownloadConfig.getInstance(this.context).mSyncMap.put("tbs_downloadflow", var10);
                        TbsDownloadConfig.getInstance(this.context).commit();
                     }

                  }
               }

               if (!this.s) {
                  if (this.t) {
                     if (this.b == null && !var9) {
                        var9 = this.verifyTbsApk(true, var5);
                     }

                     this.w.setUnpkgFlag(var9 ? 1 : 0);
                     if (!var5) {
                        this.w.setPatchUpdateFlag(var9 ? 1 : 2);
                     } else {
                        this.w.setPatchUpdateFlag(0);
                     }

                     if (var9) {
                        this.d(true);
                        TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-317);
                        this.a(100, (String)"success", true);
                     } else {
                        TbsDownloadConfig.getInstance(this.context).setDownloadInterruptCode(-318);
                        this.e(false);
                     }
                  }

                  TbsDownloadConfig var52 = TbsDownloadConfig.getInstance(this.context);
                  int var13;
                  if (var9) {
                     var13 = var52.mPreferences.getInt("tbs_download_success_retrytimes", 0);
                     ++var13;
                     var52.mSyncMap.put("tbs_download_success_retrytimes", var13);
                  } else {
                     var13 = var52.mPreferences.getInt("tbs_download_failed_retrytimes", 0);
                     ++var13;
                     var52.mSyncMap.put("tbs_download_failed_retrytimes", var13);
                     if (var13 == var52.getDownloadFailedMaxRetrytimes()) {
                        this.w.setDownloadCancel(2);
                     }
                  }

                  var52.commit();
                  this.w.setDownFinalFlag(var9 ? 1 : 0);
               }

               this.h();
            }
         }
      }
   }

   public boolean b(boolean var1) {
      if (var1 && !this.detectWifiNetworkAvailable() && (!QbSdk.getDownloadWithoutWifi() || !Apn.isNetworkAvailable(this.context))) {
         return false;
      } else if (this.b != null && this.c >= 0 && this.c < this.b.length) {
         this.k = this.b[this.c++];
         this.q = 0;
         this.r = 0;
         this.m = -1L;
         this.p = false;
         this.s = false;
         this.t = false;
         this.z = false;
         return true;
      } else {
         return false;
      }
   }

   private long a(long var1, long var3) {
      long var5 = System.currentTimeMillis();
      long var7 = var5 - var1;
      this.w.setDownConsumeTime(var7);
      this.w.setDownloadSize(var3);
      return var5;
   }

   private void a(int var1, String var2, boolean var3) {
      if (var3 || this.q > this.C) {
         this.w.setErrorCode(var1);
         this.w.setFailDetail(var2);
      }

   }

   private String a(Throwable var1) {
      String var2 = Log.getStackTraceString(var1);
      int var3 = var2.length();
      return var3 > 1024 ? var2.substring(0, 1024) : var2;
   }

   private void d(boolean var1) {
      FsSpaceUtil.a(this.context);
      TbsDownloadConfig tbsDownloadConfig = TbsDownloadConfig.getInstance(this.context);
      tbsDownloadConfig.mSyncMap.put("request_full_package", false);
      tbsDownloadConfig.mSyncMap.put("tbs_needdownload", false);
      tbsDownloadConfig.mSyncMap.put("tbs_download_interrupt_code_reason", -123);
      tbsDownloadConfig.commit();
      QbSdk.m.onDownloadFinish(var1 ? 100 : 120);
      int var3 = tbsDownloadConfig.mPreferences.getInt("tbs_responsecode", 0);
      boolean var4 = TbsDownloader.a(this.context);
      if (var3 == 5) {
         Bundle var5 = this.a(var3, var4);
         if (var5 == null) {
            return;
         }

         TbsInstaller.a().b(this.context, var5);
      } else if (var3 != 3 && var3 <= 10000) {
         int var8 = tbsDownloadConfig.mPreferences.getInt("tbs_download_version", 0);
         TbsInstaller.a().a(this.context, (new File(this.l, "x5.tbs")).getAbsolutePath(), var8);
         backupTbsApk(new File(this.l, "x5.tbs"), this.context);
      } else {
         File var7 = a(this.context);
         if (var7 != null) {
            Bundle var6 = this.a(var3, var7, var4);
            TbsInstaller.a().b(this.context, var6);
         } else {
            this.c();
            tbsDownloadConfig.mSyncMap.put("tbs_needdownload", true);
            tbsDownloadConfig.commit();
         }
      }

   }

   public Bundle a(int var1, boolean var2) {
      File var3;
      int var4;
      if (var2) {
         var3 = TbsInstaller.a().getTbsCoreShareDecoupleDir(this.context);
         var4 = TbsInstaller.a().getTbsCoreVersion_InTbsCoreShareDecoupleConfig(this.context);
      } else {
         var3 = TbsInstaller.a().getTbsCoreShareDir(this.context);
         var4 = TbsInstaller.a().getTbsCoreInstalledVerInNolock(this.context);
      }

      File var5 = new File(this.l, "x5.tbs");
      String var6 = var5.exists() ? var5.getAbsolutePath() : null;
      if (var6 == null) {
         return null;
      } else {
         int var7 = TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_download_version", 0);
         File var8;
         if (var2) {
            var8 = TbsInstaller.a().getCoreDir(this.context, 6);
         } else {
            var8 = TbsInstaller.a().getCoreDir(this.context, 5);
         }

         Bundle var9 = new Bundle();
         var9.putInt("operation", var1);
         var9.putInt("old_core_ver", var4);
         var9.putInt("new_core_ver", var7);
         var9.putString("old_apk_location", var3.getAbsolutePath());
         var9.putString("new_apk_location", var8.getAbsolutePath());
         var9.putString("diff_file_location", var6);
         String var10 = FileHelper.getBackUpDir(this.context, 7);
         File var11 = new File(var10);
         if (!var11.exists()) {
            var11.mkdirs();
         }

         var9.putString("backup_apk", (new File(var10, var7 + ".tbs")).getAbsolutePath());
         return var9;
      }
   }

   public Bundle a(int var1, File var2, boolean var3) {
      File var4;
      if (var3) {
         var4 = new File(var2, TbsDownloader.getBackupFileName(true));
      } else {
         var4 = new File(var2, TbsDownloader.getOverSea(this.context) ? "x5.oversea.tbs.org" : TbsDownloader.getBackupFileName(false));
      }

      int var5 = ApkMd5Util.getApkVersion(this.context, var4);
      File var6 = new File(this.l, "x5.tbs");
      String var7 = var6.exists() ? var6.getAbsolutePath() : null;
      int var8 = TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_download_version", 0);
      Bundle var9 = new Bundle();
      var9.putInt("operation", var1);
      var9.putInt("old_core_ver", var5);
      var9.putInt("new_core_ver", var8);
      var9.putString("old_apk_location", var4.getAbsolutePath());
      var9.putString("new_apk_location", var7);
      var9.putString("diff_file_location", var7);
      return var9;
   }

   private void a(Closeable var1) {
      if (var1 != null) {
         try {
            var1.close();
         } catch (IOException var3) {
         }

         var1 = null;
      }

   }

   private void a(long var1) {
      ++this.q;

      try {
         if (var1 <= 0L) {
            var1 = this.n();
         }

         Thread.sleep(var1);
      } catch (Exception var4) {
      }

   }

   private File i() {
      File var1 = new File(FileHelper.getBackUpDir(this.context, 4), TbsDownloader.getOverSea(this.context) ? "x5.oversea.tbs.org" : TbsDownloader.getBackupFileName(false));
      if (TbsDownloader.a(this.context)) {
         var1 = new File(FileHelper.getBackUpDir(this.context, 4), TbsDownloader.getBackupFileName(true));
      }

      return var1;
   }

   private boolean a(File var1) {
      int var2 = TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("use_backup_version", 0);
      if (var2 == 0) {
         var2 = TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_download_version", 0);
      }

      return ApkMd5Util.a(this.context, var1, 0L, var2);
   }

   private boolean verifyTbsApk(boolean var1, boolean var2) {
      TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] isTempFile=" + var1);
      File file = new File(this.l, !var1 ? "x5.tbs" : "x5.tbs.temp");
      if (!file.exists()) {
         return false;
      } else {
         String tbs_apk_md5 = TbsDownloadConfig.getInstance(this.context).mPreferences.getString("tbs_apk_md5", (String)null);
         String md5 = ApkMd5Util.getMD5(file);
         if (tbs_apk_md5 != null && tbs_apk_md5.equals(md5)) {
            TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] md5(" + md5 + ") successful!");
            long var6 = 0L;
            if (var1) {
               long var8 = TbsDownloadConfig.getInstance(this.context).mPreferences.getLong("tbs_apkfilesize", 0L);
               if (file == null || !file.exists() || var8 > 0L && var8 != (var6 = file.length())) {
                  TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] isTempFile=" + var1 + " filelength failed");
                  this.w.setCheckErrorDetail("fileLength:" + var6 + ",contentLength:" + var8);
                  return false;
               }
            }

            TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] length(" + var6 + ") successful!");
            int var13 = -1;
            if (var2 && !var1) {
               var13 = ApkMd5Util.getApkVersion(this.context, file);
               int var9 = TbsDownloadConfig.getInstance(this.context).mPreferences.getInt("tbs_download_version", 0);
               if (var9 != var13) {
                  TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] isTempFile=" + var1 + " versionCode failed");
                  if (var1) {
                     this.w.setCheckErrorDetail("fileVersion:" + var13 + ",configVersion:" + var9);
                  }

                  return false;
               }
            }

            TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] tbsApkVersionCode(" + var13 + ") successful!");
            if (var2 && !var1) {
               String var14 = AppUtil.a(this.context, false, file);
               if (!"3082023f308201a8a00302010202044c46914a300d06092a864886f70d01010505003064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f301e170d3130303732313036313835305a170d3430303731333036313835305a3064310b30090603550406130238363110300e060355040813074265696a696e673110300e060355040713074265696a696e673110300e060355040a130754656e63656e74310c300a060355040b13035753443111300f0603550403130873616d75656c6d6f30819f300d06092a864886f70d010101050003818d0030818902818100c209077044bd0d63ea00ede5b839914cabcc912a87f0f8b390877e0f7a2583f0d5933443c40431c35a4433bc4c965800141961adc44c9625b1d321385221fd097e5bdc2f44a1840d643ab59dc070cf6c4b4b4d98bed5cbb8046e0a7078ae134da107cdf2bfc9b440fe5cb2f7549b44b73202cc6f7c2c55b8cfb0d333a021f01f0203010001300d06092a864886f70d010105050003818100b007db9922774ef4ccfee81ba514a8d57c410257e7a2eba64bfa17c9e690da08106d32f637ac41fbc9f205176c71bde238c872c3ee2f8313502bee44c80288ea4ef377a6f2cdfe4d3653c145c4acfedbfbadea23b559d41980cc3cdd35d79a68240693739aabf5c5ed26148756cf88264226de394c8a24ac35b712b120d4d23a".equals(var14)) {
                  TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] isTempFile=" + var1 + " signature failed");
                  if (var1) {
                     this.w.setCheckErrorDetail("signature:" + (var14 == null ? "null" : var14.length()));
                  }

                  return false;
               }
            }

            TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] signature successful!");
            boolean var15 = false;
            if (var1) {
               Exception var10 = null;

               try {
                  var15 = file.renameTo(new File(this.l, "x5.tbs"));
               } catch (Exception var12) {
                  var10 = var12;
               }

               if (!var15) {
                  this.a(109, (String)this.a((Throwable)var10), true);
                  return false;
               }
            }

            TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] rename(" + var15 + ") successful!");
            return true;
         } else {
            TbsLog.i("TbsDownload", "[TbsApkDownloader.verifyTbsApk] isTempFile=" + var1 + " md5 failed");
            if (var1) {
               this.w.setCheckErrorDetail("fileMd5 not match");
            }

            return false;
         }
      }
   }

   private boolean e(boolean var1) {
      TbsLog.i("TbsDownload", "[TbsApkDownloader.deleteFile] isApk=" + var1);
      File var2 = null;
      if (var1) {
         var2 = new File(this.l, "x5.tbs");
      } else {
         var2 = new File(this.l, "x5.tbs.temp");
      }

      boolean var3 = true;
      if (var2 != null && var2.exists()) {
         FileHelper.delete(var2);
      }

      return var3;
   }

   private void j() {
      try {
         File var1 = this.i();
         if (var1 != null && var1.exists()) {
            FileHelper.delete(var1);
            File[] var2 = var1.getParentFile().listFiles();
            String var3 = ApkMd5Util.a(TbsDownloader.a(this.context)) + "(.*)";
            Pattern var4 = Pattern.compile(var3);
            File[] var5 = var2;
            int var6 = var2.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               File var8 = var5[var7];
               Matcher var9 = var4.matcher(var8.getName());
               if (var9.find() && var8.isFile() && var8.exists()) {
                  FileHelper.delete(var8);
               }
            }
         }
      } catch (Exception var10) {
         var10.printStackTrace();
      }

   }

   private boolean k() {
      boolean var1 = false;
      File var2 = new File(this.l, "x5.tbs.temp");
      if (var2 != null && var2.exists()) {
         var1 = true;
      }

      return var1;
   }

   private long l() {
      long var1 = 0L;
      File var3 = new File(this.l, "x5.tbs.temp");
      if (var3 != null && var3.exists()) {
         var1 = var3.length();
      }

      return var1;
   }

   private boolean m() {
      Runtime var1 = Runtime.getRuntime();
      Process var2 = null;
      String var3 = null;
      InputStream var4 = null;
      InputStreamReader var5 = null;
      BufferedReader var6 = null;
      String var7 = "www.qq.com";
      boolean var8 = false;

      try {
         var2 = var1.exec("ping " + var7);
         var4 = var2.getInputStream();
         var5 = new InputStreamReader(var4);
         var6 = new BufferedReader(var5);
         int var9 = 0;

         while((var3 = var6.readLine()) != null) {
            if (var3.contains("TTL") || var3.contains("ttl")) {
               var8 = true;
               break;
            }

            ++var9;
            if (var9 >= 5) {
               break;
            }
         }
      } catch (Throwable var13) {
         var13.printStackTrace();
      } finally {
         this.a((Closeable)var4);
         this.a((Closeable)var5);
         this.a((Closeable)var6);
      }

      return var8;
   }

   private String a(URL var1) {
      String var2 = "";

      try {
         InetAddress var3 = InetAddress.getByName(var1.getHost());
         var2 = var3.getHostAddress();
      } catch (Exception var4) {
         var4.printStackTrace();
      } catch (Error var5) {
         var5.printStackTrace();
      }

      return var2;
   }

   private long n() {
      long var1 = 20000L;
      switch(this.q) {
      case 1:
      case 2:
         var1 *= (long)this.q;
         break;
      case 3:
      case 4:
         var1 *= 5L;
         break;
      default:
         var1 *= 10L;
      }

      return var1;
   }

   @TargetApi(8)
   static File a(Context var0) {
      File var1 = null;

      try {
         if (VERSION.SDK_INT >= 8) {
            var1 = new File(FileHelper.getBackUpDir(var0, 4));
         }

         if (var1 != null && !var1.exists() && !var1.isDirectory()) {
            var1.mkdirs();
         }

         return var1;
      } catch (Exception var3) {
         var3.printStackTrace();
         TbsLog.e("TbsDownload", "[TbsApkDownloader.backupApkPath] Exception is " + var3.getMessage());
         return null;
      }
   }

   @TargetApi(8)
   static File b(Context var0) {
      File var1 = null;

      try {
         if (VERSION.SDK_INT >= 8) {
            var1 = a(var0, 4);
            if (var1 == null) {
               var1 = a(var0, 3);
            }

            if (var1 == null) {
               var1 = a(var0, 2);
            }

            if (var1 == null) {
               var1 = a(var0, 1);
            }
         }

         return var1;
      } catch (Exception var3) {
         var3.printStackTrace();
         TbsLog.e("TbsDownload", "[TbsApkDownloader.backupApkPath] Exception is " + var3.getMessage());
         return null;
      }
   }

   private static File a(Context var0, int var1) {
      File var2 = new File(FileHelper.getBackUpDir(var0, var1));
      if (var2 != null && var2.exists() && var2.isDirectory()) {
         File var3 = new File(var2, TbsDownloader.getOverSea(var0) ? "x5.oversea.tbs.org" : TbsDownloader.getBackupFileName(false));
         return var3 != null && var3.exists() ? var2 : null;
      } else {
         return null;
      }
   }

   public int c(boolean var1) {
      File var2 = a(this.context);
      if (var1) {
         return var2 == null ? 0 : ApkMd5Util.getApkVersion(this.context, new File(var2, TbsDownloader.getBackupFileName(true)));
      } else {
         return var2 == null ? 0 : ApkMd5Util.getApkVersion(this.context, new File(var2, TbsDownloader.getOverSea(this.context) ? "x5.oversea.tbs.org" : TbsDownloader.getBackupFileName(false)));
      }
   }

   public void b() {
      this.s = true;
      if (TbsShareManager.isThirdPartyApp(this.context)) {
         TbsLogReport.TbsLogInfo var1 = TbsLogReport.getInstance(this.context).tbsLogInfo();
         var1.setErrorCode(-309);
         var1.setFailDetail((Throwable)(new Exception()));
         if (TbsDownloader.a(this.context)) {
            TbsLogReport.getInstance(this.context).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD_DECOUPLE, var1);
         } else {
            TbsLogReport.getInstance(this.context).eventReport(TbsLogReport.EventType.TYPE_DOWNLOAD, var1);
         }
      }

   }

   public void c() {
      this.b();
      this.e(false);
      this.e(true);
   }

   public void a(int var1) {
      if (TbsInstaller.a().getTbsInstallingFileLock(this.context)) {
         TbsInstaller.a().releaseTbsInstallingFileLock();

         try {
            File var2 = new File(this.l, "x5.tbs");
            int var3 = ApkMd5Util.getApkVersion(this.context, var2);
            if (-1 == var3 || var1 > 0 && var1 == var3) {
               FileHelper.delete(var2);
            }
         } catch (Exception var4) {
         }
      }

   }

   public static void backupTbsApk(File var0, Context var1) {
      Class var2 = ApkMd5Util.class;
      synchronized(ApkMd5Util.class) {
         if (var0 != null && var0.exists()) {
            if (!TbsShareManager.isThirdPartyApp(var1)) {
               try {
                  File var3 = a(var1);
                  if (var3 != null) {
                     File var4 = null;
                     if (TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_download_version_type", 0) == 1) {
                        var4 = new File(var3, TbsDownloader.getBackupFileName(true));
                     } else {
                        var4 = new File(var3, TbsDownloader.getOverSea(var1) ? "x5.oversea.tbs.org" : TbsDownloader.getBackupFileName(false));
                     }

                     var4.delete();
                     FileHelper.forceTransferFile(var0, var4);
                     boolean var5 = var4.getName().contains("tbs.org");
                     boolean var6 = var4.getName().contains("x5.tbs.decouple");
                     if (var6 || var5) {
                        File[] var7 = var3.listFiles();
                        String var8 = ApkMd5Util.a(var6) + "(.*)";
                        Pattern var9 = Pattern.compile(var8);
                        File[] var10 = var7;
                        int var11 = var7.length;

                        for(int var12 = 0; var12 < var11; ++var12) {
                           File var13 = var10[var12];
                           Matcher var14 = var9.matcher(var13.getName());
                           if (var14.find() && var13.isFile() && var13.exists()) {
                              var13.delete();
                           }
                        }

                        TbsDownloadConfig var19 = TbsDownloadConfig.getInstance(var1);
                        var11 = var19.mPreferences.getInt("tbs_download_version", 0);
                        File var20 = new File(var3, ApkMd5Util.a(var6) + "." + var11);
                        if (var20 != null && var20.exists()) {
                           TbsLog.e("TbsDownload", "[TbsApkDownloader.backupTbsApk]delete bacup config file error ");
                           return;
                        }

                        var20.createNewFile();
                     }

                     if (TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_download_version_type", 0) != 1 && TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_decouplecoreversion", 0) == ApkMd5Util.getApkVersion(var1, var0)) {
                        int var18 = TbsDownloadConfig.getInstance(var1).mPreferences.getInt("tbs_responsecode", 0);
                        if (var18 == 5 || var18 == 3) {
                           TbsLog.i("TbsApkDownloader", "response code=" + var18 + "return backup decouple apk");
                        }

                        var4 = new File(var3, TbsDownloader.getBackupFileName(true));
                        if (ApkMd5Util.getApkVersion(var1, var0) != ApkMd5Util.getApkVersion(var1, var4)) {
                           var4.delete();
                           FileHelper.forceTransferFile(var0, var4);
                           return;
                        }
                     }

                  }
               } catch (Exception var16) {
               }
            }
         }
      }
   }

   public static void c(Context var0) {
      try {
         TbsInstaller.a();
         File var1 = TbsInstaller.getTbsCorePrivateDir(var0);
         (new File(var1, "x5.tbs")).delete();
         (new File(var1, "x5.tbs.temp")).delete();
         File var2 = a(var0);
         if (var2 != null) {
            (new File(var2, TbsDownloader.getBackupFileName(false))).delete();
            (new File(var2, "x5.oversea.tbs.org")).delete();
            File[] var3 = var2.listFiles();
            String var4 = ApkMd5Util.a(true) + "(.*)";
            Pattern var5 = Pattern.compile(var4);
            File[] var6 = var3;
            int var7 = var3.length;

            for(int var8 = 0; var8 < var7; ++var8) {
               File var9 = var6[var8];
               Matcher var10 = var5.matcher(var9.getName());
               if (var10.find() && var9.isFile() && var9.exists()) {
                  var9.delete();
               }
            }

            String var14 = ApkMd5Util.a(false) + "(.*)";
            Pattern var15 = Pattern.compile(var14);
            File[] var16 = var3;
            int var17 = var3.length;

            for(int var18 = 0; var18 < var17; ++var18) {
               File var11 = var16[var18];
               Matcher var12 = var15.matcher(var11.getName());
               if (var12.find() && var11.isFile() && var11.exists()) {
                  var11.delete();
               }
            }
         }
      } catch (Exception var13) {
      }

   }

   private boolean detectWifiNetworkAvailable() {
      boolean var1 = Apn.getApnType(this.context) == 3;
      boolean sucess = false;
      String wifi_ssid = null;
      TbsLog.i("TbsDownload", "[TbsApkDwonloader.detectWifiNetworkAvailable] isWifi=" + var1);
      if (var1) {
         wifi_ssid = Apn.getWifiSSID(this.context);
         TbsLog.i("TbsDownload", "[TbsApkDwonloader.detectWifiNetworkAvailable] localBSSID=" + wifi_ssid);
         HttpURLConnection httpURLConnection = null;

         try {
            URL url = new URL("https://pms.mb.qq.com/rsp204");
            httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.getInputStream();
            int responseCode = httpURLConnection.getResponseCode();
            TbsLog.i("TbsDownload", "[TbsApkDwonloader.detectWifiNetworkAvailable] responseCode=" + responseCode);
            sucess = responseCode == 204;
         } catch (Throwable var15) {
            var15.printStackTrace();
         } finally {
            if (httpURLConnection != null) {
               try {
                  httpURLConnection.disconnect();
               } catch (Exception ignored) {
               }
            }

         }
      }

      if (!sucess && !TextUtils.isEmpty(wifi_ssid) && !this.wifiSSIDs.contains(wifi_ssid)) {
         this.wifiSSIDs.add(wifi_ssid);
         this.initHandler();
         Message var17 = this.handler.obtainMessage(150, wifi_ssid);
         this.handler.sendMessageDelayed(var17, 120000L);
      }

      if (sucess && this.wifiSSIDs.contains(wifi_ssid)) {
         this.wifiSSIDs.remove(wifi_ssid);
      }

      return sucess;
   }

   private void initHandler() {
      if (this.handler == null) {
         this.handler = new Handler(TbsHandlerThread.getInstance().getLooper()) {
            public void handleMessage(Message message) {
               if (message.what == 150) {
                  TbsDownload.this.detectWifiNetworkAvailable();
               }

            }
         };
      }

   }

   public boolean isDownloadForeground() {
      TbsLog.i("TbsDownload", "[TbsApkDownloader.isDownloadForeground] mIsDownloadForeground=" + this.downloadForeground);
      return this.downloadForeground;
   }

   void pauseDownload() {
      TbsLog.i("TbsDownload", "pauseDownload,isPause=" + this.d + "isDownloading=" + TbsDownloader.isDownloading());
      if (!this.d && TbsDownloader.isDownloading()) {
         this.b();
         this.d = true;
         this.z = false;
      }

   }

   void f() {
      TbsLog.i("TbsDownload", "resumeDownload,isPause=" + this.d + "isDownloading=" + TbsDownloader.isDownloading());
      if (this.d && TbsDownloader.isDownloading()) {
         this.d = false;
         this.startDownload(false);
      }

   }
}
