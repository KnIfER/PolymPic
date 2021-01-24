package com.knziha.polymer.browser;

import android.text.TextUtils;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.Utils.IU;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduleTask extends DownloadTask{
	Integer[] scheduleSeq;
	int scheduleIter=-1;
	final boolean leapToDay;
	int lifeSpanExpectancy;
	
	public ScheduleTask(BrowseActivity a, long id, String url, File download_path, String title, int flag1, String ext1) {
		super(a, null, id, url, download_path, title, flag1, ext1);
		leapToDay = extObj.getBooleanValue("leap");
		String val = extObj.getString("seq");
		if(!TextUtils.isEmpty(val)) {
			Matcher m = Pattern.compile("\\((.*?)\\)x([0-9]*)").matcher(val);
			StringBuffer sb = new StringBuffer();
			while(m.find()) {
				String pat = m.group(1);
				int rep = IU.parsint(m.group(2));
				m.appendReplacement(sb, "");
				for (int i = 0; i < rep; i++) {
					sb.append(pat);
				}
			}
			m.appendTail(sb);
			val = sb.toString();
			CMN.Log("expand tail...", val);
			String[] arr = val.split(" ");
			ArrayList<Integer> scheduleSeqLis = new ArrayList<>();
			for(String sI:arr) {
				int value = IU.parsint(sI, -1);
				if(value>0) {
					scheduleSeqLis.add(value);
				}
				lifeSpanExpectancy+=value;
			}
			scheduleSeq = scheduleSeqLis.toArray(new Integer[]{});
		}
		if(scheduleSeq==null||scheduleSeq.length==0) {
			scheduleSeq = new Integer[]{5, 5, 10, 10, 5, 5, 10, 20, 30, 30, 30};
		}
	}
}
