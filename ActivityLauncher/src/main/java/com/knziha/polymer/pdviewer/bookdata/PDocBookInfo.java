package com.knziha.polymer.pdviewer.bookdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.alibaba.fastjson.JSONObject;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.database.LexicalDBHelper;
import com.knziha.polymer.pdviewer.PDFPageParms;

/** Represents a history record in the database */
public class PDocBookInfo {
	public PDFPageParms parms;
	/** The visit count. */
	public int visit_count;
	/** The first visit time. */
	public long creation_time;
	/** The latest visit time. */
	public long last_visit_time;
	/** Need to save the info. */
	public boolean isDirty;
	/** The rowID for the info entry in the db. according to https://sqlite.org/c3ref/last_insert_rowid.html,
	 * 	this id is identical to the INTEGER PRIMARY KEY.
	 * */
	public long rowID;
	/** The file name which is used as the storage key. */
	public String name;
	/** The file url to reopen with. */
	public Uri url;
	/** Some Settings. */
	public long firstflag;
	/** The results count from the query of key. */
	public int count;
	/** for the pages info table whose name is pages_[rowID]. */
	String pagesTblName;
	
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
	
	public void setParms(int pageIdx, int offsetX, int offsetY, float scale) {
		if(parms.setIfNotEqual(pageIdx, offsetX, offsetY, scale)) {
			isDirty = true;
		}
	}
	
	public boolean connectPagesTable(LexicalDBHelper db) {
		if(pagesTblName==null && rowID!=-1 && db!=null) {
			String name = "pages_"+rowID;
			if(db.connectPagesTable(name)) {
				pagesTblName = name;
			}
		}
		return pagesTblName != null;
	}
	
	public String connectPagesTableIfExists(LexicalDBHelper db) {
		if(rowID!=-1)
		try{
			String name = "pages_"+rowID;
			db.getDB().rawQuery("select count(1) from "+name,null);
			return connectPagesTable(db)?name:null;
		} catch(Exception e){
			CMN.Log(e);
		}
		return null;
	}
	
	/** record and digest highlight annotation into the db.*/
	public void appendAnnotRecord(PDFPageParms parms, int colorInt, int selStart, int selEnd, String text, boolean writeToFile) {
		LexicalDBHelper db = LexicalDBHelper.getInstance();
		if(connectPagesTable(db)) {
			SQLiteDatabase database = db.getDB();
			final String tableName = pagesTblName;
			
			ContentValues values = new ContentValues();
			values.put("pid", parms.pageIdx);
			if(text!=null) {
				values.put("text", text);
			}
			JSONObject obj = JSONObject.parseObject(parms.toString());
			obj.put("ss", selStart);
			obj.put("se", selEnd);
			
			int flag=writeToFile?1:0;
			values.put("type", 1);
			values.put("f1", flag);
			values.put("color", colorInt);
			values.put("parms", obj.toString());
			values.put("creation_time", System.currentTimeMillis());
			
			database.insert(tableName, null, values);
			
			CMN.Log("Annot 大概保存了吧……", obj.toString());
		}
	}
}
