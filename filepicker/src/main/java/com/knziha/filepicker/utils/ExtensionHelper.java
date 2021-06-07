package com.knziha.filepicker.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class ExtensionHelper {
    public static HashSet<String> FOOTAGE;
    public static HashSet<String> MPEGS;
    public static HashSet<String> PHOTO;
    public static HashSet<String> SOUNDS;
	public static HashSet<String> MIMES;
    public static HashMap<String, String> MIME_TABLE;

    static{
        String[] videoExtensions = new String[]{".flv",".f4v",
                ".mp4",".mkv",".webm",".avi",".3gp",".mov",".m4v",".3gpp",

                ".mtv",".mts",".mpv2",".mpg",".mpeg4",".mpeg2",".mpeg1",
                ".mpeg",".mpe",".mp4v",".mp2v",".mp2",".m2v",".m2ts",".m2t",".m1v",".iso",

                ".xesc",".wtv",".wmv",".wm",".vro",".vob",".tts",".ts",".tod",
                ".rmvb",".rm",".rec",".ps",".ogx",".ogv",".ogm",".nuv",".nut",
                ".nsv",".mxg",".mxf",".ismv",".gxf",".gvi",".dv",".drc",".divx"
                ,".asf",".amv",".acc",".3gp2",".3g2"};
        final String[] photoExtensions = new String[]{".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif"};
        final String[] audioExtensions = new String[]{".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac"};
		final String[] mpegExtensions = new String[]{".mp4",".mpv2",".mpg",".mpeg4",".mpeg2",".mpeg1",
				".mpeg",".mpe",".mp4v",".mp2v",".mp2",".m2v",".m2ts",".m2t",".m1v",".iso",".acc",".mdl"};
		final String[] mimes = new String[]{"application", "audio", "image", "message", "text", "video"};
        FOOTAGE=new HashSet<>(videoExtensions.length);
        FOOTAGE.addAll(Arrays.asList(videoExtensions));
        PHOTO=new HashSet<>(photoExtensions.length);
        PHOTO.addAll(Arrays.asList(photoExtensions));
        SOUNDS=new HashSet<>(audioExtensions.length);
        SOUNDS.addAll(Arrays.asList(audioExtensions));
        MPEGS=new HashSet<>(mpegExtensions.length);
		MPEGS.addAll(Arrays.asList(mpegExtensions));
		MIMES=new HashSet<>(mimes.length);
		MIMES.addAll(Arrays.asList(mimes));
		final String[][] mimeTable={
				{".apk",    "application/vnd.android.package-archive"},
				{".bin",    "application/octet-stream"},
				{".class",    "application/octet-stream"},
				{".doc",    "application/msword"},
				{".gif",    "image/gif"},
				{".gtar",    "application/x-gtar"},
				{".gz",        "application/x-gzip"},
				{".html",    "text/html"},
				{".m3u",    "audio/x-mpegurl"},
				{".m4a",    "audio/mp4a-latm"},
				{".m4b",    "audio/mp4a-latm"},
				{".m4p",    "audio/mp4a-latm"},
				{".m4u",    "video/vnd.mpegurl"},
				{".m4v",    "video/x-m4v"},
				{".mpc",    "application/vnd.mpohun.certificate"},
				{".mpg4",    "video/mp4"},
				{".mpga",    "audio/mpeg"},
				{".pdf",    "application/pdf"},
				{".pps",    "application/vnd.ms-powerpoint"},
				{".ppt",    "application/vnd.ms-powerpoint"},
				{".rar",    "application/x-rar-compressed"},
				{".tar",    "application/x-tar"},
				{".tgz",    "application/x-compressed"},
				{".txt",    "text/plain"},
				//{".xml",    "text/xml"},
				{".xml",    "text/plain"},
				{".zip",    "application/zip"}
		};
		MIME_TABLE=new HashMap<>(mimeTable.length);
		for (int i = 0; i < mimeTable.length; i++) {
			MIME_TABLE.put(mimeTable[i][0], mimeTable[i][1]);
		}
    }

    public static String InferMimeTypeForName(final String name) {
    	String ret = null;
    	int sufIdx = name.lastIndexOf(".");
		String suf;
    	if (sufIdx>=0) {
			suf = name.substring(sufIdx);
			ret = MIME_TABLE.get(suf);
			if (ret==null && FOOTAGE.contains(suf)) {
				ret = "video/*";
			}
			if (ret==null && PHOTO.contains(suf)) {
				ret = "image/*";
			}
			if (ret==null && SOUNDS.contains(suf)) {
				ret = "audio/*";
			}
		}
		if (ret==null) {
			sufIdx = name.indexOf("/");
			if (sufIdx>=0) {
				suf = name.substring(0, sufIdx);
				if (MIMES.contains(suf)) {
					if (suf.equals("image")||suf.equals("audio")||suf.equals("video")) {
						ret = suf+"/*";
					} else {
						ret = name;
					}
				}
			}
		}
		//CMNF.Log("InferMimeTypeForName", name, ret);
		return ret;
	}

}
