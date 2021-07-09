package com.knziha.polymer.paging;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.recyclerview.widget.RecyclerView;

import com.knziha.polymer.Utils.CMN;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

public class PagingCursorAdapter<T extends CursorReader> implements PagingAdapterInterface<T> {
	ArrayList<SimpleCursorPage<T>> pages =  new ArrayList<>(1024);
	int number_of_rows_detected;
	final ConstructorInterface<T> mRowConstructor;
	final ConstructorInterface<T[]> mRowArrConstructor;
	final SQLiteDatabase db;
	
	final String sortField = "creation_time";
	final String dataFields = "text";
	final String table = "annott";
	boolean DESC = true;
	int pageSz = 20;
	String sql = "SELECT ROWID," + sortField + "," + dataFields
			+ " FROM " + table + " WHERE " + sortField + (DESC?"<=?":">=?")
			+ " ORDER BY " + sortField + " " + (DESC?"DESC":"ASC")  + " LIMIT " + pageSz;
	
	String sql_reverse = "SELECT ROWID," + sortField + "," + dataFields
			+ " FROM " + table + " WHERE " + sortField + (!DESC?"<=?":">=?")
			+ " ORDER BY " + sortField + " " + (!DESC?"DESC":"ASC")  + " LIMIT " + pageSz;
	
	String sql_fst = "SELECT ROWID," + sortField
			+ " FROM " + table + " WHERE " + sortField + (DESC?"<=?":">=?")
			+ " ORDER BY " + sortField + " " + (DESC?"DESC":"ASC")  + " LIMIT " + pageSz;
	
	
	public PagingCursorAdapter(SQLiteDatabase db, ConstructorInterface<T> mRowConstructor
			, ConstructorInterface<T[]> mRowArrConstructor) {
		this.mRowConstructor = mRowConstructor;
		this.mRowArrConstructor = mRowArrConstructor;
		this.db = db;
		
	}
	
	@Override
	public int getCount() {
		return number_of_rows_detected;
	}
	
	@Override
	public T getReaderAt(int position) {
		int idx = getPageAt(position);
		SimpleCursorPage<T> page = pages.get(idx);
		int offsetedPos = (int) (position-basePosOffset);
		CMN.Log("getReaderAt basePosOffset="+basePosOffset
				, position, "@"+idx, basePosOffset+page.pos, basePosOffset+page.end);
		CMN.Log("--- "+page);
		CMN.Log("--- "+offsetedPos, page.rows[(int) (offsetedPos-page.pos)]);
		boolean b1=idx==pages.size()-1 && offsetedPos >=page.end-page.number_of_row/2;
		if (b1
				|| idx==0 && offsetedPos<=page.pos+page.number_of_row/2
		) {
			PrepareNxtPage(page, b1);
		}
		return page.rows[(int) (offsetedPos-page.pos)];
	}
	
	@Override
	public void bindTo(RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
	}
	
	@Override
	public void startPaging(long resume_to_sort_number, int init_page) {
		// 回复到 resume_to_sort_number。
		pages.clear();
		
		
//		if (resume_to_sort_number!=0) {
//			lastPage.ed_fd = resume_to_sort_number;
//		}
		
		PreparePageAt(init_page, resume_to_sort_number);
		
		//getReaderAt(init_page);
	}
	
	public RecyclerView recyclerView;
	
	Runnable mGrowRunnable = new Runnable() {
		@Override
		public void run() {
			int st = number_of_rows_detected;
			GrowPage(mGrowingPage, mGrowingPageDir);
			//PreparePageAt((int) (page.end+1));
			if (!mGrowingPageDir) {
				CMN.Log("reverse GrowPage::", number_of_rows_detected - st);
			}
			if (number_of_rows_detected!=st) {
				RecyclerView.Adapter ada = recyclerView.getAdapter();
				if (mGrowingPageDir) {
					ada.notifyItemRangeInserted(st + 1, number_of_rows_detected - st);
				} else {
					ada.notifyItemRangeInserted(0, number_of_rows_detected - st);
				}
			}
		}
	};
	
	private void PrepareNxtPage(SimpleCursorPage<T> page, boolean dir) {
		if (recyclerView!=null && (mGrowingPage!=page || mGrowingPageDir!=dir)) {
			mGrowingPage = page;
			mGrowingPageDir = dir;
			//recyclerView.removeCallbacks(mGrowRunnable);
			recyclerView.post(mGrowRunnable);
		}
	}
	
	public int reduce(int position,int start,int end) {//via mdict-js
		int len = end-start;
		if (len > 1) {
			len = len >> 1;
			return position - pages.get(start + len - 1).end >0
					? reduce(position,start+len,end)
					: reduce(position,start,start+len);
		} else {
			return start;
		}
	}
	
	private int getPageAt(int position) {
		PreparePageAt(position, 0);
		return reduce((int) (position-basePosOffset), 0, pages.size());
	}
	
	boolean finished = false;
	
	SimpleCursorPage<T> mGrowingPage;
	boolean mGrowingPageDir;
	long basePosOffset;
	
	public void GrowPage(SimpleCursorPage<T> lastPage, boolean dir) {
		lastPage = dir?pages.get(pages.size()-1):pages.get(0);
		CMN.Log("GrowPage::", dir, lastPage);
		boolean popData = true;
		try (Cursor cursor = db.rawQuery(dir?sql:sql_reverse, new String[]{(dir?lastPage.ed_fd:lastPage.st_fd)+""});){
			int len = cursor.getCount();
			if (len>0) {
				ArrayList<T> rows = new ArrayList<>(pageSz);
				long lastEndId = dir?lastPage.ed_id:lastPage.st_id;
				SimpleCursorPage<T> page = new SimpleCursorPage<>();
				boolean lastRowFound = false;
				long id = -1;
				long sort_number = 0;
				//if (!dir && cursor.moveToLast()) cursor.moveToPrevious();
				while (cursor.moveToNext()) {
					id = cursor.getLong(0);
					sort_number = cursor.getLong(1);
					if (lastEndId!=-1) {
						if (lastEndId==id) {
							lastEndId=-1;
							lastRowFound = true;
							if (page.number_of_row>0) {
								rows.clear();
								page.number_of_row=0;
							}
							continue;
						}
					}
					if (page.number_of_row==0) {
						if (dir) {
							page.st_fd = sort_number;
							page.st_id = id;
						} else {
							page.ed_fd = sort_number;
							page.ed_id = id;
						}
					}
					if (popData) {
						T row = mRowConstructor.newInstance(0);
						row.ReadCursor(cursor, id, sort_number);
						rows.add(row);
					}
					page.number_of_row++;
				}
				if ((!lastRowFound || id==lastEndId) && lastEndId!=-1) {
					if (len==1) {
						CMN.Log("pageSz too small! len==1");
						return;
					}
					throw new IllegalStateException("pageSz too small!"+lastRowFound+" "+rows.size()+" "+dir);
				}
				if (popData) {
					page.rows = rows.toArray(mRowArrConstructor.newInstance(rows.size()));
				}
				if (dir) {
					page.ed_fd = sort_number;
					page.ed_id = id;
					page.pos = lastPage.end + 1;
					page.end = lastPage.end + page.number_of_row;
					pages.add(page);
				} else {
					page.st_fd = sort_number;
					page.st_id = id;
					page.pos = lastPage.pos - page.number_of_row;
					page.end = lastPage.pos - 1;
					basePosOffset += page.number_of_row;
					pages.add(0, page);
					if (popData) {
						ArrayUtils.reverse(page.rows);
					}
				}
				number_of_rows_detected += page.number_of_row;
			}
		} catch (Exception e) {
			CMN.Log(e);
			throw new RuntimeException(e);
		}
	}
	
	public void PreparePageAt(int position, long resume) {
		if (!finished && position>=number_of_rows_detected) {
			SimpleCursorPage<T> lastPage;
			if (pages.size()>0) {
				lastPage = pages.get(pages.size()-1);
			} else {
				lastPage = new SimpleCursorPage<>();
				if (!DESC) {
					lastPage.ed_fd = Long.MIN_VALUE;
				}
				if (resume!=0) {
					lastPage.ed_fd = resume;
				}
			}
			while(true) {
				CMN.Log("PreparePageAt::");
				long st_pos = number_of_rows_detected;
				boolean popData = st_pos + pageSz > position;
				popData = true;
				try (Cursor cursor = db.rawQuery(popData?sql:sql_fst, new String[]{lastPage.ed_fd+""});){
					int len = cursor.getCount();
					if (len>0) {
						ArrayList<T> rows = new ArrayList<>(pageSz);
						long lastEndId = lastPage.ed_id;
						SimpleCursorPage<T> page = new SimpleCursorPage<>();
						boolean lastRowFound = false;
						long id = -1;
						long sort_number = 0;
						while (cursor.moveToNext()) {
							id = cursor.getLong(0);
							sort_number = cursor.getLong(1);
							if (lastEndId!=-1) {
								if (lastEndId==id) {
									lastEndId=-1;
									lastRowFound = true;
									if (page.number_of_row>0) {
										rows.clear();
										page.number_of_row=0;
									}
									continue;
								}
							}
							if (page.number_of_row==0) {
								page.st_fd = sort_number;
								page.st_id = id;
							}
							if (popData) {
								T row = mRowConstructor.newInstance(0);
								row.ReadCursor(cursor, id, sort_number);
								rows.add(row);
							}
							page.number_of_row++;
						}
						if ((!lastRowFound || id==lastEndId) && lastEndId!=-1) {
							throw new IllegalStateException("pageSz too small!");
						}
						page.ed_fd = sort_number;
						page.ed_id = id;
						page.pos = st_pos;
						page.end = st_pos + page.number_of_row - 1;
						if (popData) {
							page.rows = rows.toArray(mRowArrConstructor.newInstance(rows.size()));
						}
						number_of_rows_detected += page.number_of_row;
						pages.add(lastPage = page);
						finished |= len<pageSz;
					} else {
						finished |= true;
					}
					if (finished || number_of_rows_detected>position) {
						break;
					}
				} catch (Exception e) {
					CMN.Log(e);
					throw new RuntimeException(e);
				}
			}
		}
	}
}