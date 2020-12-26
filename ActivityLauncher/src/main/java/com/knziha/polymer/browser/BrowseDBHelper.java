package com.knziha.polymer.browser;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.knziha.polymer.Utils.CMN;

import java.io.File;


/** extract methods: 0=direct; 1=web; 2=JSAPP
* //hide()
*  extract methods 1: extract download url from the webview. ext1 = {js:'js to evaluate'}
*/
public class BrowseDBHelper extends SQLiteOpenHelper {
	private SQLiteDatabase database;
	private static BrowseDBHelper INSTANCE;
	private static int INSTANCE_COUNT;
	
	public static BrowseDBHelper connectInstance(Context context) {
		if(INSTANCE==null) {
			INSTANCE = new BrowseDBHelper(context.getApplicationContext());
		}
		INSTANCE_COUNT++;
		return INSTANCE;
	}
	
	public static BrowseDBHelper getInstance() {
		return INSTANCE;
	}
	
	public static SQLiteDatabase getInstancedDb() {
		return INSTANCE==null?null:INSTANCE.database;
	}
	
	public void try_close() {
		if(--INSTANCE_COUNT<=0) {
			close();
			INSTANCE=null;
			INSTANCE_COUNT=0;
			CMN.Log("关闭数据库……");
		}
	}
	
	public BrowseDBHelper(Context context) {
		super(context, new File(context.getExternalFilesDir(null), "vip.sql").getPath(), null, CMN.dbVersionCode);
		database = getWritableDatabase();
		//pathName = database.getPath();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		final String createTasksTable = "CREATE TABLE if not exists \"tasks\" (" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT"+ // 0
				",title TEXT DEFAULT ''"+ // 1
				",url TEXT"+ // 2
				",files TEXT"+ // 3
				",f1 INTEGER DEFAULT 0 NOT NULL"+ // 4
				",states INTEGER DEFAULT 0 NOT NULL"+ // 5
				",folder_url TEXT"+ // 6
				",ext1 TEXT"+ // 7
				",ext2 TEXT"+ // 8
				",real_time INTEGER"+ // 9
				",creation_time INTEGER DEFAULT 0 NOT NULL"+ // 10
				")";
		
		db.execSQL(createTasksTable);
		
		db.execSQL("CREATE INDEX if not exists tasks_time_index ON tasks (creation_time)");
		
		database = db;
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	
	}
	
	public long insertNewEntry() {
		ContentValues values = new ContentValues();
		//values.put("title", "");
		values.put("creation_time", System.currentTimeMillis());
		return database.insert("tasks", null, values);
	}
	
	public Cursor getCursor() {
		return database.rawQuery("select * from tasks", null);
	}
	
	public Cursor getCursorByRowID(long rowID) {
		return database.rawQuery("select * from tasks where id=?", new String[]{""+rowID});
	}
}
