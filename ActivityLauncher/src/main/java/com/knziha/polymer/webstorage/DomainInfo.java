package com.knziha.polymer.webstorage;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import com.knziha.polymer.database.LexicalDBHelper;

import org.adrianwalker.multilinestring.Multiline;

public class DomainInfo {
	public final SubStringKey domainKey;
	public long domainID;
	public long f1;
	public Bitmap thumbnail;
	public boolean isDirty;
	
	public DomainInfo(SubStringKey domainKey, long rowID, long f1) {
		this.domainKey = domainKey;
		this.domainID = rowID;
		this.f1 = f1;
	}
	@Multiline(flagPos=6) public boolean getApplyOverride_group_storage(){ f1=f1; throw new RuntimeException(); }
	@Multiline(flagPos=11) public boolean getApplyOverride_group_client(){ f1=f1; throw new RuntimeException(); }
	
	public void updateFlag(long val) {
		f1 = val;
		isDirty = true;
	}
	
	public void checkDirty() {
		if(isDirty) {
			ContentValues values = new ContentValues();
			values.put("f1", f1);
			SQLiteDatabase database = LexicalDBHelper.getInstancedDb();
			if (database!=null) {
				if(domainID!=0) {
					database.update("domains", values, "id=?", new String[]{""+domainID});
				} else {
					values.put("url", domainKey.toString());
					domainID = database.insert("domains", null, values);
				}
			}
			isDirty = false;
		}
	}
}