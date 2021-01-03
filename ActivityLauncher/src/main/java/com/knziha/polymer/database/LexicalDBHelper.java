package com.knziha.polymer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.util.SparseArray;

import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.pdviewer.PDocument;
import com.knziha.polymer.pdviewer.bookdata.PDocBookInfo;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.knziha.polymer.widgets.Utils.EmptyCursor;

/**
 * Created by KnIfER on 2020/7/8.
 */

public class LexicalDBHelper extends SQLiteOpenHelper {
	private static LexicalDBHelper INSTANCE;
	private static int INSTANCE_COUNT;
	public final String DATABASE;
	private SQLiteDatabase database;
	private boolean history_active_updated;
	private boolean history_empty_updated;
	private boolean bookmark_active_updated;
	private boolean bookmark_empty_updated;
	private boolean search_active_updated;
	private boolean search_empty_updated;
	private List<Cursor> cursors_to_close = Collections.synchronizedList(new ArrayList<>());
	
	public static synchronized LexicalDBHelper connectInstance(Context context) {
		if(INSTANCE==null) {
			INSTANCE = new LexicalDBHelper(context.getApplicationContext());
		}
		INSTANCE_COUNT++;
		return INSTANCE;
	}
	
	public static LexicalDBHelper getInstance() {
		return INSTANCE;
	}
	
	public static SQLiteDatabase getInstancedDb() {
		return INSTANCE==null?null:INSTANCE.database;
	}
	
	public static long getLong(Cursor cursor, int i) {
		try {
			return cursor.getLong(i);
		} catch (Exception e) {
			CMN.Log(e);
			return 0;
		}
	}
	
	public static int getInt(Cursor cursor, int i) {
		try {
			return cursor.getInt(i);
		} catch (Exception e) {
			CMN.Log(e);
			return 0;
		}
	}
	
	public static String getString(Cursor cursor, int i) {
		try {
			return cursor.getString(i);
		} catch (Exception e) {
			CMN.Log(e);
			return null;
		}
	}
	
	public synchronized void try_close() {
		if(--INSTANCE_COUNT<=0) {
			close();
			INSTANCE=null;
			INSTANCE_COUNT=0;
			CMN.Log("关闭数据库……");
		}
	}
	
	public SQLiteDatabase getDB(){
		return database;
	}
    
    public static final String HISTORY_SEARCH = "t1";

    public static String Key_ID = "lex"; //主键
    public static final String Date = "date"; //路径
    
    public String pathName;

	/** 创建历史纪录数据库 */
	public LexicalDBHelper(Context context) {
		super(context, new File(context.getExternalFilesDir(null), "history.sql").getPath(), null, CMN.dbVersionCode);
		DATABASE="history.sql";
		onConfigure();
	}
	
	void onConfigure() {
		database = getWritableDatabase();
		pathName = database.getPath();
		oldVersion=CMN.dbVersionCode;
	}
	
	@Override
    public void onCreate(SQLiteDatabase db) {
		CMN.Log("onDbCreate");
		// browse history.
		final String createHistoryTable = "create table if not exists urls(" +
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"url LONGVARCHAR," +
				"title LONGVARCHAR," +
				"visit_count INTEGER DEFAULT 0 NOT NULL," +
				"note_id INTEGER DEFAULT -1 NOT NULL,"+
				"creation_time INTEGER NOT NULL,"+
				"last_visit_time INTEGER NOT NULL)";
        db.execSQL(createHistoryTable);
		
		// search history.
		final String createSearchTable = "create table if not exists \"keyword_search_terms\" ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"term LONGVARCHAR NOT NULL,"+
				"creation_time INTEGER NOT NULL,"+
				"last_visit_time INTEGER NOT NULL)";
		db.execSQL(createSearchTable);
		
		// web annotations.
		final String createNotesTable = "create table if not exists \"annots\" ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT," +
				"url LONGVARCHAR," +
				"notes LONGVARCHAR,"+
				"texts LONGVARCHAR,"+
				"url_id INTEGER DEFAULT -1 NOT NULL,"+
				"flag INTEGER DEFAULT -1 NOT NULL,"+
				"creation_time INTEGER DEFAULT 0 NOT NULL," +
				"last_visit_time INTEGER NOT NULL)";
		db.execSQL(createNotesTable);
		
		// pdoc history.
		//db.execSQL("drop table if exists pdoc");
		final String createPDocTable = "create table if not exists \"pdoc\" ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT," + // 0
				"name TEXT NOT NULL," + // 1
				"url TEXT NOT NULL," + // 2
				"page_info TEXT," + // 3 页面位置的记忆
				"zoom_info TEXT," + // 4 缩放信息 TODO
				"bookmarks BLOB," + // 5 书签id TODO
				"toc BLOB," +  // 6 toc expand / collapse states TODO
				"thumbnail BLOB," + // 7
				"ext1 TEXT,"+ // 8 comments
				"f1 INTEGER DEFAULT 0 NOT NULL," + // 9
				"f2 INTEGER DEFAULT 0 NOT NULL," +  // 10
				"favor INTEGER DEFAULT 0 NOT NULL," +  // 11 喜爱等级 TODO
				"pages INTEGER DEFAULT 0 NOT NULL," + // 12 页面总数
				"progress INTEGER DEFAULT 0 NOT NULL," + // 13 (0~10000) 阅读进度
				"visit_count INTEGER DEFAULT 0 NOT NULL,"+ // 14
				"creation_time INTEGER DEFAULT 0 NOT NULL," + // 15
				"last_visit_time INTEGER NOT NULL" + // 16
				")";
		db.execSQL(createPDocTable);
		
		// web tabs.
		//db.execSQL("drop table if exists webtabs");
		final String createWebTable = "create table if not exists \"webtabs\" ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT," + // 0
				"title TEXT," + // 1
				"url TEXT NOT NULL," + // 2
				"search TEXT," + // 1
				"page_info TEXT," + // 3 页面位置的记忆
				"zoom_info TEXT," + // 4 缩放信息 TODO
				"thumbnail BLOB," + // 7
				"webstack BLOB," + // 7
				"ext1 TEXT,"+ // 8 comments
				"f1 INTEGER DEFAULT 0 NOT NULL," + // 9
				"f2 INTEGER DEFAULT 0 NOT NULL," +  // 10
				"favor INTEGER DEFAULT 0 NOT NULL," +  // 11 喜爱等级 TODO
				"visit_count INTEGER DEFAULT 0 NOT NULL,"+ // 14
				"rank INTEGER DEFAULT 0 NOT NULL," + // 15
				"creation_time INTEGER DEFAULT 0 NOT NULL," + // 15
				"last_visit_time INTEGER DEFAULT 0 NOT NULL" + // 16
				")";
		db.execSQL(createWebTable);
		
		ensureDwnldTable(db);
		
		db.execSQL("CREATE INDEX if not exists urls_url_index ON urls (url)");
		db.execSQL("CREATE INDEX if not exists annots_url_index ON annots (url)");
		db.execSQL("CREATE INDEX if not exists keyword_search_terms_index3 ON keyword_search_terms (term)");
		db.execSQL("CREATE INDEX if not exists pdoc_name_index ON pdoc (name)");
		db.execSQL("CREATE INDEX if not exists pdoc_time_index ON pdoc (last_visit_time)");
		if(false) {
			db.execSQL("CREATE INDEX if not exists pdoc_visit_index ON pdoc (visit_count)");
			db.execSQL("CREATE INDEX if not exists pdoc_time1_index ON pdoc (creation_time)");
			db.execSQL("CREATE INDEX if not exists pdoc_pages_index ON pdoc (pages)");
			db.execSQL("CREATE INDEX if not exists pdoc_progress_index ON pdoc (progress)");
		}
		//db.execSQL("CREATE INDEX if not exists pdoc_url_index ON pdoc (url)");
		db.execSQL("CREATE INDEX if not exists webtabs_rank_index ON webtabs (rank)");
		
		
		CMN.Log("onDbCreate..done");
    }
	
	public Cursor queryTabs() {
		return database.rawQuery("select id,title,url,search,f1,rank from webtabs order by rank", null);
	}
	
	public long insertNewTab(String defaultUrl) {
		ContentValues values = new ContentValues();
		values.put("url", defaultUrl);
		return database.insert("webtabs", null, values);
	}
	
    @Override
    public void onUpgrade(SQLiteDatabase db, int _oldVersion, int newVersion) {
        //在 setVersion 前已经调用
		onCreate(db);
        //oldVersion=_oldVersion;
		db.setVersion(oldVersion=newVersion);
        CMN.Log("onUpgrade",oldVersion+":"+newVersion+":"+db.getVersion());
    }

    //lazy Upgrade
	boolean isDirty=false;
    int oldVersion=1;
    @Override
    public void onOpen(SQLiteDatabase db) {
        db.setVersion(oldVersion);
    }
	
	Cursor ActiveUrlCursor;
	Cursor ActiveSearchCursor;
	public Cursor EmptySearchCursor;
	private String LastSearchTerm;
	StringBuilder MainBuilder = new StringBuilder(128);
 
	public Cursor queryUrl(String url_key, int limitation, CancellationSignal stopSign) {
		if(StringUtils.isEmpty(url_key)) {
			return EmptyCursor;
		}
    	if(ActiveUrlCursor!=null) {
			if(url_key.equals(LastSearchTerm)&&!history_active_updated) {
				return ActiveUrlCursor;
			}
			cursors_to_close.add(ActiveUrlCursor);
			ActiveUrlCursor = null;
		}
		String sql = "select url,title from urls where url like ? or title like ?  order by visit_count desc, last_visit_time desc";
		MainBuilder.setLength(0);
		String term = MainBuilder.append("%").append(url_key).append("%").toString();
		if(limitation!=Integer.MAX_VALUE) {
			if(limitation<=0) {
				return EmptyCursor;
			}
			MainBuilder.setLength(0);
			sql = MainBuilder.append(sql).append(" limit ").append(limitation).toString();
		}
		Cursor activeUrlCursor = database.rawQuery(sql, new String[]{term, term}, stopSign);
		//Cursor activeUrlCursor = database.query(true, "urls", new String[]{"url","title"}, "url like ? or title like ?",new String[]{term, term}, null, null,"visit_count desc, last_visit_time desc", "0,8",stopSign);
		history_active_updated =false;
		return ActiveUrlCursor = activeUrlCursor;
	}
	
	public Cursor querySearchTerms(String url_key, int limitation, CancellationSignal stopSign) {
		if(StringUtils.isEmpty(url_key)) {
			if(EmptySearchCursor!=null) {
				if(!search_empty_updated) {
					return EmptySearchCursor;
				}
				cursors_to_close.add(EmptySearchCursor);
				EmptySearchCursor=null;
			}
			String sql = "select term from keyword_search_terms";
//			if(limitation!=Integer.MAX_VALUE) {
//				if(limitation<=0) {
//					return EmptyCursor;
//				}
//				MainBuilder.setLength(0);
//				sql = MainBuilder.append(sql).append(" limit ").append(limitation).toString();
//			}
			Cursor emptySearchCursor = database.rawQuery(sql, null ,stopSign);
			search_empty_updated=false;
			return EmptySearchCursor=emptySearchCursor;
		} else {
			if(ActiveSearchCursor!=null) {
				if(url_key.equals(LastSearchTerm)&&!search_active_updated) {
					return ActiveSearchCursor;
				}
				cursors_to_close.add(ActiveSearchCursor);
				ActiveSearchCursor=null;
			}
			String sql = "select term from keyword_search_terms where term like ? order by last_visit_time desc";
			MainBuilder.setLength(0);
			String term = MainBuilder.append(url_key).append("%").toString();
			if(limitation!=Integer.MAX_VALUE) {
				if(limitation<=0) {
					return EmptyCursor;
				}
				MainBuilder.setLength(0);
				sql = MainBuilder.append(sql).append(" limit ").append(limitation).toString();
			}
			Cursor activeUrlCursor = database.rawQuery(sql, new String[]{term}, stopSign);
			search_active_updated=false;
			return ActiveSearchCursor = activeUrlCursor;
		}
	}
	
	public Cursor queryNote(String url_key) {
		String sql = "select notes,texts,url,url_id from annots where url=?";
		return database.rawQuery(sql, new String[]{url_key});
	}
	
	public void updateLastSearchTerm(String url_key) {
		LastSearchTerm = url_key;
	}
	
	public long insertUpdateBrowserUrl(String lex, String tile) {
		history_empty_updated=history_active_updated=true;
    	int count=-1;
		int id=-1;
		final String sql = "select id,visit_count from urls where url = ? ";
		String[] where = new String[]{lex};
		Cursor c = database.rawQuery(sql, where);
		if(c.getCount()>0) {
			c.moveToFirst();
			id = c.getInt(0);
			count = c.getInt(1);
		}
		c.close();
		
		boolean notUpdateHistory=false;
		
		if(count>0&&notUpdateHistory) {
			return 1;
		}
		
		//CMN.Log("countcount", count);
		
		ContentValues values = new ContentValues();
		values.put("url", lex);
		values.put("title", tile);
		values.put("visit_count", ++count);
		values.put("last_visit_time", System.currentTimeMillis());
		values.put("creation_time", System.currentTimeMillis());
		
		if(count>0) {
			values.put("id", id);
			//database.update("urls", values, "url=?", where);
			database.insertWithOnConflict("urls", null, values, SQLiteDatabase.CONFLICT_REPLACE);
		} else {
			database.insert("urls", null, values);
		}
		return count;
	}
	
	public long insertSearchTerm(String lex) {
		search_empty_updated=search_active_updated=true;
		ContentValues values = new ContentValues();
		values.put("term", lex);
		values.put("last_visit_time", System.currentTimeMillis());
		String[] where = new String[]{lex};
		int count=0;
//		final String sql = "select term from keyword_search_terms where term=?";
//		Cursor c = database.rawQuery(sql, where);
//		count = c.getCount();
//		c.close();
		database.delete("keyword_search_terms", "term=?", where);
		if(count==0) {
			return database.insert("keyword_search_terms", null, values);
		}
		return count;
	}
	
	public long insertUpdateNote (String lex, String notes, String texts) {
		history_empty_updated=history_active_updated=true;
		long id=-1;
		final String sql = "select id from urls where url = ? ";
		String[] where = new String[]{lex};
		Cursor c = database.rawQuery(sql, where);
		if(c.getCount()>0) {
			c.moveToFirst();
			id = c.getInt(0);
		}
		c.close();
		
		ContentValues values = new ContentValues();
		values.put("url", lex);
		values.put("notes", notes);
		values.put("texts", texts);
		values.put("last_visit_time", System.currentTimeMillis());
		
		if(id>=0) {
			values.put("id", (int)id);
			//id = database.update("annots", values, "id=?", new String[]{Integer.toString((int) id)});
			id = database.insertWithOnConflict("annots", null, values, SQLiteDatabase.CONFLICT_REPLACE);
		} else {
			values.put("creation_time", System.currentTimeMillis());
			id = database.insert("annots", null, values);
		}
		return id;
	}
	
	SQLiteStatement preparedSelectExecutor;
	private void prepareContain() {
		if(preparedSelectExecutor==null) {
	     	String sql = "select * from urls where url = ? ";
			preparedSelectExecutor = database.compileStatement(sql);
		}
	}
	
	public void close_for_browser(){
		if(preparedSelectExecutor!=null)
			preparedSelectExecutor.close();
		if(ActiveSearchCursor!=null)
			ActiveSearchCursor.close();
		if(EmptySearchCursor!=null)
			EmptySearchCursor.close();
		if(ActiveUrlCursor!=null)
			ActiveUrlCursor.close();
	}

	public boolean wipeData() {
		return database.delete(HISTORY_SEARCH, null, null)>0;
	}
	
	SparseArray<Object[]> searchcursoremptycahce = new SparseArray<>();
	SparseArray<Object[]> searchcursoractivecahce = new SparseArray<>();
	
	public Object[] getActiveSearchTermAt(Cursor cursor, int position) {
		Object[] ret=null;
		SparseArray<Object[]> cursorcahce=null;
		if(cursor==ActiveSearchCursor) {
			cursorcahce=searchcursoremptycahce;
		} else if(cursor==EmptySearchCursor) {
			searchcursoractivecahce=searchcursoractivecahce;
		}
		ret=cursorcahce.get(position);
		if(ret==null) {
			cursor.moveToPosition(position);
			ret=new Object[]{cursor.getString(0)};
		}
		return ret;
	}
	
	
	public void closeCursors() {
		for(Cursor cursor:cursors_to_close) {
			cursor.close();
		}
		cursors_to_close.clear();
	}
	
	public boolean isOpen() {
		return database.isOpen();
	}
	
	public Cursor queryPDoc(String name_key) {
		String sql = "select * from pdoc where name=?";
		return database.rawQuery(sql, new String[]{name_key});
	}
	
	public int savePDocInfo(PDocument pdoc, PDocBookInfo bookInfo) {
		final String tableName = "pdoc";
		String name = bookInfo.name;
		if(TextUtils.isEmpty(name)) {
			return 0;
		}
		int count=bookInfo.count;
		long id=bookInfo.rowID;
		
		boolean notUpdateHistory=false;
		if(count>0&&notUpdateHistory) {
			return 1;
		}
		
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("url", bookInfo.url.toString());
		values.put("page_info", bookInfo.parms.toString());
		values.put("visit_count", ++count);
		values.put("f1", bookInfo.firstflag);
		values.put("pages", pdoc._num_entries);
		values.put("progress", bookInfo.parms.pageIdx*10000/pdoc._num_entries);
		
		values.put("last_visit_time", System.currentTimeMillis());
		
		boolean insert=true;
		
		if(id!=-1) {
			values.put("id", id);
			if(database.update(tableName, values, "id=?", new String[]{String.valueOf(id)})>0) {
				insert = false;
			}
		}
		
		if(insert) {
			CMN.Log("新建 1");
			values.remove("id");
			id = database.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if(id==-1) {
				CMN.Log("新建 2");
				id = database.insert(tableName, null, values);
			}
			if(id!=-1) {
				insert = false;
				bookInfo.rowID = id;
			}
		}
		if(!insert)
		{
			bookInfo.count++;
		}
		
		CMN.Log("大概保存了吧……");
		return bookInfo.count;
	}
	
	public Uri getDocUrlForID(long val) {
		try {
			String sql = "select url from pdoc where id=?";
			Cursor cursor = database.rawQuery(sql, new String[]{Long.toString(val)});
			String ret = null;
			if(cursor.getCount()>0 && cursor.moveToFirst()) {
				ret = cursor.getString(0);
			}
			cursor.close();
			if(ret!=null) {
				return Uri.parse(ret);
			}
		} catch (Exception e) { CMN.Log(e); }
		return null;
	}
	
	/** 做标注时，将页面位置、文本索引、文本片段储存在数据库。 */
	public boolean connectPagesTable(String name) {
		SQLiteDatabase db = getDB();
		if(db==null) {
			return false;
		}
		//db.execSQL("drop table if exists "+name);
		// page info.
		final String createPagesTable = "create table if not exists \""+name+"\" ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT," + // 0
				"pid INTEGER NOT NULL," + // 1 页码
				"type INTEGER NOT NULL," + // 2 类型
				"favor INTEGER DEFAULT 0 NOT NULL," + // 3 喜爱程度
				"text TEXT," + // 4 文本内容
				"parms TEXT,"+ // 5 页面位置
				"thumbnail BLOB," + // 6 缩略图
				"ext1 TEXT,"+ // 7
				"color INTEGER DEFAULT 0 NOT NULL," + // 8
				"f1 INTEGER DEFAULT 0 NOT NULL," + // 9
				"creation_time INTEGER DEFAULT 0 NOT NULL" + // 10 创建时间
				")";
		
		try {
			db.execSQL(createPagesTable);
			db.execSQL("CREATE INDEX if not exists "+name+"_time_index ON "+name+" (creation_time)"); // 时间索引
			db.execSQL("CREATE INDEX if not exists "+name+"_page_index ON "+name+" (pid)"); // 页码索引
			if(false) {
				db.execSQL("CREATE INDEX if not exists "+name+"_favor_index ON "+name+" (favor)");
				db.execSQL("CREATE INDEX if not exists "+name+"_type_index ON "+name+" (type)");
			}
			return true;
		} catch (SQLException e) { CMN.Log(e); }
		
		return false;
	}
	
	public Cursor queryPdocHistory() {
		String sql = "select * from pdoc";
		return database.rawQuery(sql, null);
	}
	
	public void ensureDwnldTable(SQLiteDatabase db) {
		if(db==null) db = database;
		//db.execSQL("drop table if exists downloads");
		final String createDwnldTable = "create table if not exists \"downloads\" ("+
				"id INTEGER PRIMARY KEY AUTOINCREMENT," + // 0
				"tid TEXT," + // 1
				"url TEXT NOT NULL," + // 2
				"path TEXT," + // 1
				"type INTEGER DEFAULT 0 NOT NULL," + // 15
				"ext TEXT," + // 16
				"mime TEXT," + // 16
				"filename TEXT," + // 16
				"fromUrl TEXT," + // 16
				"size INTEGER DEFAULT 0 NOT NULL," + // 15
				"creation_time INTEGER DEFAULT 0 NOT NULL" + // 15
				")";
		db.execSQL(createDwnldTable);
		db.execSQL("CREATE INDEX if not exists downloads_url_index ON downloads (url)");
		db.execSQL("CREATE INDEX if not exists downloads_tid_index ON downloads (tid)");
		db.execSQL("CREATE INDEX if not exists downloads_time_index ON downloads (creation_time)");
	}
	
	public long getRowIdForTaskID(long dwnldID) {
		String sql = "select id from downloads where tid=?";
		Cursor qursor = database.rawQuery(sql, new String[]{""+dwnldID});
		long ret=-1;
		if(qursor.moveToNext()) {
			ret = qursor.getLong(0);
		}
		qursor.close();
		return ret;
	}
	
	public void updatePathForDownload(long rowId, String path) {
		try {
			ContentValues values = new ContentValues();
			values.put("id", rowId);
			values.put("path", path);
			String[] where = new String[]{""+rowId};
			database.update("downloads", values, "id=?", where);
		} catch (Exception e) {
			CMN.Log(e);
		}
	}
	
	public long recordDwnldItem(long tid, String url, String fileName, long contentLength, String mimetype) {
		ContentValues values = new ContentValues();
		values.put("tid", tid);
		values.put("url", url);
		values.put("creation_time", CMN.now());
		values.put("filename", fileName);
		values.put("size", contentLength);
		values.put("mime", mimetype);
		return database.insert("downloads", null, values);
	}
	
	public String getIntenedFileNameForDownload(long rowId) {
		String sql = "select path from downloads where id=?";
		Cursor qursor = database.rawQuery(sql, new String[]{""+rowId});
		String ret = null;
		if(qursor.moveToNext()) {
			ret = qursor.getString(0);
		}
		qursor.close();
		return ret;
	}
}
