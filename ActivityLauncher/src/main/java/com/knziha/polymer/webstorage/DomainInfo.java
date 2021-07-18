package com.knziha.polymer.webstorage;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import com.knziha.polymer.database.LexicalDBHelper;

import org.adrianwalker.multilinestring.Multiline;

public class DomainInfo {
	public static final DomainInfo EmptyInfo = new DomainInfo(SubStringKey.EmptyDomain, 0 , 0);
	public final SubStringKey domainKey;
	public long domainID;
	public long f1;
	public long f1Stamp;
	public Bitmap thumbnail;
	
	public DomainInfo(SubStringKey domainKey, long rowID, long f1) {
		this.domainKey = domainKey;
		this.domainID = rowID;
		this.f1 = f1;
		this.f1Stamp = f1;
	}
	@Multiline(flagPos=6) public boolean getApplyOverride_group_storage(){ f1=f1; throw new RuntimeException(); }
	@Multiline(flagPos=6) public void setApplyOverride_group_storage(boolean val){ f1=f1; throw new RuntimeException(); }
	
	@Multiline(flagPos=11) public boolean getApplyOverride_group_client(){ f1=f1; throw new RuntimeException(); }
	@Multiline(flagPos=11) public void setApplyOverride_group_client(boolean val){ f1=f1; throw new RuntimeException(); }
	
	@Multiline(flagPos=14) public boolean getApplyOverride_group_scroll(){ f1=f1; throw new RuntimeException(); }
	@Multiline(flagPos=14) public void setApplyOverride_group_scroll(boolean val){ f1=f1; throw new RuntimeException(); }

	@Multiline(flagPos=21) public boolean getApplyOverride_group_text(){ f1=f1; throw new RuntimeException(); }
	@Multiline(flagPos=21) public void setApplyOverride_group_text(boolean val){ f1=f1; throw new RuntimeException(); }
	
	@Multiline(flagPos=33) public boolean getApplyOverride_group_lock(){ f1=f1; throw new RuntimeException(); }
	@Multiline(flagPos=33) public void setApplyOverride_group_lock(boolean val){ f1=f1; throw new RuntimeException(); }
	
	public void updateFlag(long val) {
		f1 = val;
	}
	
	public void checkDirty() {
		if(f1Stamp != f1) {
			ContentValues values = new ContentValues();
			values.put("f1", f1);
			SQLiteDatabase database = LexicalDBHelper.getInstancedDb();
			if (database!=null) {
				try {
					if(domainID!=0) {
						database.update("domains", values, "id=?", new String[]{""+domainID});
					} else {
						values.put("url", domainKey.toString());
						domainID = database.insert("domains", null, values);
					}
				} catch (Exception ignored) {  }
			}
			f1Stamp = f1;
		}
	}
	
	public void remove() {
		if (domainID!=0) {
			SQLiteDatabase database = LexicalDBHelper.getInstancedDb();
			if (database!=null) {
				try {
					database.delete("domains", "id=?", new String[]{""+domainID});
				} catch (Exception ignored) { }
			}
			domainID = 0;
			f1Stamp = f1 = 0;
		}
	}
}