package com.knziha.filepicker.utils;

import java.util.Arrays;
import java.util.HashSet;

public class ExtensionHelper {
    public static HashSet<String> FOOTAGE;
    public static HashSet<String> MPEGS;
    public static HashSet<String> PHOTO;
    public static HashSet<String> SOUNDS;

    static{
        String[] videoExtensions = new String[]{".flv",".f4v",
                ".mp4",".mkv",".webm",".avi",".3gp",".mov",".m4v",".3gpp",

                ".mtv",".mts",".mpv2",".mpg",".mpeg4",".mpeg2",".mpeg1",
                ".mpeg",".mpe",".mp4v",".mp2v",".mp2",".m2v",".m2ts",".m2t",".m1v",".iso",

                ".xesc",".wtv",".wmv",".wm",".vro",".vob",".tts",".ts",".tod",
                ".rmvb",".rm",".rec",".ps",".ogx",".ogv",".ogm",".nuv",".nut",
                ".nsv",".mxg",".mxf",".ismv",".gxf",".gvi",".dv",".drc",".divx"
                ,".asf",".amv",".acc",".3gp2",".3g2"};
        String[] photoExtensions = new String[]{".jpg", ".png", ".jpeg", ".bmp", ".webp", ".heic", ".heif"};
        String[] audioExtensions = new String[]{".mp3", ".wav", ".wma", ".ogg", ".m4a", ".opus", ".flac", ".aac"};
        String[] mpegExtensions = new String[]{".mp4",".mpv2",".mpg",".mpeg4",".mpeg2",".mpeg1",
                ".mpeg",".mpe",".mp4v",".mp2v",".mp2",".m2v",".m2ts",".m2t",".m1v",".iso",".acc",".mdl"};
        FOOTAGE=new HashSet<>(videoExtensions.length);
        FOOTAGE.addAll(Arrays.asList(videoExtensions));
        PHOTO=new HashSet<>(photoExtensions.length);
        PHOTO.addAll(Arrays.asList(photoExtensions));
        SOUNDS=new HashSet<>(audioExtensions.length);
        SOUNDS.addAll(Arrays.asList(audioExtensions));
        MPEGS=new HashSet<>(audioExtensions.length);
        MPEGS.addAll(Arrays.asList(mpegExtensions));

    }


}
