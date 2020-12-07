package com.knziha.polymer.pdviewer.bookdata;

import android.database.Cursor;
import android.net.Uri;

import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.pdviewer.PDFPageParms;

public class PDocBookInfo {
	public PDFPageParms parms;
	public int visit_count;
	public long creation_time;
	public long last_visit_time;
	public boolean isDirty;
	public long rowID;
	public String name;
	public Uri url;
	public long firstflag;
	public int count;
	
	public PDocBookInfo(String name, Cursor cursor) {
		this.name = name;
		count = cursor.getCount();
		rowID = LexicalDBHelper.getLong(cursor, 0);
		last_visit_time = LexicalDBHelper.getLong(cursor, 15);
		parms = new PDFPageParms(LexicalDBHelper.getString(cursor, 3));
	}
	
	public PDocBookInfo(String name) {
		this.name = name;
		parms = new PDFPageParms(0, 0, 0, -1);
		last_visit_time = creation_time = System.currentTimeMillis();
		visit_count = 0;
		isDirty = true;
		rowID = -1;
	}
}
