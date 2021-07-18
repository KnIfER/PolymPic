package com.knziha.polymer.paging;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.knziha.polymer.Utils.CMN;
import com.knziha.polymer.browser.AppIconCover.AppIconCover;
import com.knziha.polymer.browser.AppIconCover.AppLoadableBean;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.knziha.polymer.webslideshow.ImageViewTarget.FuckGlideDrawable;

public class PagingCursorAdapter<T extends CursorReader> implements PagingAdapterInterface<T> {
	private RecyclerView recyclerView;
	
	ArrayList<SimpleCursorPage<T>> pages =  new ArrayList<>(1024);
	Queue<Pair<Integer, SimpleCursorPage<T>>> insertQueue = new ConcurrentLinkedQueue<>();
	Queue<SimpleCursorPage<T>> dataQueue = new ConcurrentLinkedQueue<>();
	Queue<SimpleCursorPage<T>> updateQueue = new ConcurrentLinkedQueue<>();
	AtomicInteger dataQueue_size = new AtomicInteger();
	
	int number_of_rows_detected;
	final ConstructorInterface<T> mRowConstructor;
	final ConstructorInterface<T[]> mRowArrConstructor;
	final SQLiteDatabase db;
	
	private ImageView pageAsyncLoader;
	private boolean glide_initialized = true;
	private RequestBuilder<Drawable> glide;
	
	private int pageSz = 20;
	
	private String sortField;
	private String dataFields;
	private String table;
	private boolean DESC = true;
	private String sql, sql_reverse, sql_fst;
	
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
	
	int pageDataSz = 100;
	
	@Override
	public T getReaderAt(int position) {
		int idx = getPageAt(position);
		SimpleCursorPage<T> page = pages.get(idx);
		int offsetedPos = (int) (position-basePosOffset);
		//CMN.Log("getReaderAt basePosOffset="+basePosOffset
		//		, position, "@"+idx, basePosOffset+page.pos, basePosOffset+page.end);
		//CMN.Log("--- "+page);
		//CMN.Log("--- "+offsetedPos, page.rows[(int) (offsetedPos-page.pos)]);
		boolean b1=idx==pages.size()-1 && offsetedPos >=page.end-page.number_of_row/2;
		if (b1
				|| idx==0 && offsetedPos<=page.pos+page.number_of_row/2
		) {
			PrepareNxtPage(page, b1);
		}
		T ret;
		if (page.rows==null) {
			ret = null;
			CMN.Log("重新加载数据……");
			if (glide!=null) {
				init_glide();
				glide.load(new AppIconCover(new PageAsyncLoaderBean(number_of_rows_detected, page, false)))
						.into(pageAsyncLoader);
			} else {
				ReLoadPage(page);
				ret = page.rows[(int) (offsetedPos-page.pos)];
			}
		} else {
			ret = page.rows[(int) (offsetedPos-page.pos)];
		}
		if(dataQueue_size.get()>pageDataSz) {
			SimpleCursorPage<T> toRemove;
			int toRemoveCnt = dataQueue.size()-pageDataSz;
			int removed = 0;
			while((toRemove=dataQueue.poll())!=null && toRemoveCnt>0) {
				if (toRemove.rows!=null) {
					toRemove.rows = null;
					toRemoveCnt--;
				}
				removed++;
			}
			dataQueue_size.addAndGet(-removed);
		}
		return ret;
	}
	
	public PagingCursorAdapter<T> bindTo(RecyclerView recyclerView) {
		this.recyclerView = recyclerView;
		return this;
	}
	
	public PagingCursorAdapter<T> setAsyncLoader(Context context, ImageView pageAsyncLoader) {
		this.glide = Glide.with(context).load((String)null);
		this.pageAsyncLoader = pageAsyncLoader;
		glide_initialized = false;
		return this;
	}
	
	public PagingCursorAdapter<T> sortBy(String table, String sortField, boolean desc, String dataFields) {
		this.table = table;
		this.sortField = sortField;
		this.dataFields = dataFields;
		this.DESC = desc;
		glide_initialized = false;
		return this;
	}
	
	private void remakeSql() {
		sql = "SELECT ROWID," + sortField + "," + dataFields
				+ " FROM " + table + " WHERE " + sortField + (DESC?"<=?":">=?")
				+ " ORDER BY " + sortField + " " + (DESC?"DESC":"ASC")  + " LIMIT " + pageSz;
		
		sql_reverse = "SELECT ROWID," + sortField + "," + dataFields
				+ " FROM " + table + " WHERE " + sortField + (!DESC?"<=?":">=?")
				+ " ORDER BY " + sortField + " " + (!DESC?"DESC":"ASC")  + " LIMIT " + pageSz;
		
		sql_fst = "SELECT ROWID," + sortField
				+ " FROM " + table + " WHERE " + sortField + (DESC?"<=?":">=?")
				+ " ORDER BY " + sortField + " " + (DESC?"DESC":"ASC")  + " LIMIT " + pageSz;
	}
	
	
	public void startPaging(long resume_to_sort_number, int init_load_size, int page_size) {
		pages.clear();
		number_of_rows_detected = 0;
		pageSz = page_size;
		if (glide!=null) {
			glide_initialized = false;
			glide.load(new AppIconCover(() -> {
				//try { Thread.sleep(200); } catch (InterruptedException ignored) { }
				startPagingInternal(resume_to_sort_number, init_load_size);
				return FuckGlideDrawable;
			}))
			.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
			.skipMemoryCache(true)
			.diskCacheStrategy(DiskCacheStrategy.NONE)
			.listener(new RequestListener<Drawable>() {
				@Override
				public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
					CMN.Log(e);
					return false;
				}
				@Override
				public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
					recyclerView.getAdapter().notifyDataSetChanged();
					return true;
				}
			})
			.into(pageAsyncLoader);
		} else {
			startPagingInternal(resume_to_sort_number, init_load_size);
		}
	}
	
	public void startPagingInternal(long resume_to_sort_number, int init_load_size) {
		pages.clear();
		remakeSql();
		number_of_rows_detected = 0;
		PreparePageAt(init_load_size, resume_to_sort_number);
	}
	
	Runnable mGrowRunnable = new Runnable() {
		@Override
		public void run() {
			int st = number_of_rows_detected;
			boolean dir = mGrowingPageDir;
			SimpleCursorPage<T> pg = GrowPage(dir);
			if (pg!=null) {
				number_of_rows_detected += pg.number_of_row;
				if (!dir) {
					pages.add(0, pg);
					basePosOffset += pg.number_of_row;
				} else {
					pages.add(pg);
				}
				//if (!dir) CMN.Log("reverse GrowPage::", number_of_rows_detected - st);
				if (number_of_rows_detected!=st) {
					recyclerView.getAdapter().notifyItemRangeInserted(dir?st+1:0, number_of_rows_detected - st);
				}
			}
		}
	};
	
	class PageAsyncLoaderBean implements AppLoadableBean {
		final int st;
		final SimpleCursorPage<T> page;
		final boolean dir;
		PageAsyncLoaderBean(int st, SimpleCursorPage<T> page, boolean dir) {
			this.st = st;
			this.page = page;
			this.dir = dir;
		}
		@Override
		public Drawable load() {
			//CMN.Log("PrepareNxtPage :: load!!!");
			if (page!=null) {
				ReLoadPage(page);
				updateQueue.add(page);
			} else {
				SimpleCursorPage<T> inserted = GrowPage(dir);
				//if (!dir) CMN.Log("PrepareNxtPage :: reverse GrowPage::", inserted);
				if (inserted!=null) {
					insertQueue.add(new Pair<>(dir?st+1:0, inserted));
				}
			}
			return FuckGlideDrawable;
		}
		@Override
		public boolean equals(@Nullable Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PageAsyncLoaderBean other = ((PageAsyncLoaderBean) o);
			return dir==other.dir && page==other.page;
		}
		@Override
		public int hashCode() {
			return Objects.hash(page, dir);
		}
	}
	
	private void PrepareNxtPage(SimpleCursorPage<T> page, boolean dir) {
		//CMN.Log("PrepareNxtPage::???", dir, mGrowingPage);
		if (!glide_initialized || recyclerView!=null && (mGrowingPage!=page || mGrowingPageDir!=dir)) {
			//CMN.Log("PrepareNxtPage::", dir, page);
			if (glide!=null) {
				mGrowingPage = page;
				mGrowingPageDir = dir;
				//CMN.Log("PrepareNxtPage::glide loading...", dir, page);
				init_glide();
				glide.load(new AppIconCover(new PageAsyncLoaderBean(number_of_rows_detected, null, dir)))
						.into(pageAsyncLoader);
			} else {
				mGrowingPage = page;
				mGrowingPageDir = dir;
				recyclerView.removeCallbacks(mGrowRunnable);
				recyclerView.post(mGrowRunnable);
			}
		}
	}
	
	private void init_glide() {
		if (!glide_initialized) {
			pageAsyncLoader.setTag(null);
			glide = glide.listener(new RequestListener<Drawable>() {
				@Override
				public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
					mGrowingPage = null;
					return false;
				}
				@Override
				public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
					Pair<Integer, SimpleCursorPage<T>> rng;
					SimpleCursorPage<T> pg;
					RecyclerView.Adapter ada = recyclerView.getAdapter();
					while((rng = insertQueue.poll())!=null) {
						pg = rng.second;
						number_of_rows_detected += pg.number_of_row;
						if (rng.first==0) {
							pages.add(0, pg);
							basePosOffset += pg.number_of_row;
						} else {
							pages.add(pg);
						}
						ada.notifyItemRangeInserted(rng.first, pg.number_of_row);
					}
					SimpleCursorPage<T> page;
					while((page = updateQueue.poll())!=null) {
						ada.notifyItemRangeChanged((int)(page.pos+basePosOffset), (int) (page.end+basePosOffset));
					}
					return true;
				}
			});
			glide_initialized = true;
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
		//PreparePageAt(position, 0);
		return reduce((int) (position-basePosOffset), 0, pages.size());
	}
	
	boolean finished = false;
	
	SimpleCursorPage<T> mGrowingPage;
	boolean mGrowingPageDir;
	long basePosOffset;
	
	public void ReLoadPage(SimpleCursorPage<T> page) {
		try (Cursor cursor = db.rawQuery(sql, new String[]{page.st_fd+""})){
			int len = cursor.getCount();
			if (len>0) {
				ArrayList<T> rows = new ArrayList<>(pageSz);
				//if (!dir && cursor.moveToLast()) cursor.moveToPrevious();
				int cc=0;
				long id = -1;
				long sort_number = 0;
				while (cursor.moveToNext() && cc< page.number_of_row) {
					id = cursor.getLong(0);
					sort_number = cursor.getLong(1);
					T row = mRowConstructor.newInstance(0);
					row.ReadCursor(cursor, id, sort_number);
					rows.add(row);
					cc++;
				}
				page.rows = rows.toArray(mRowArrConstructor.newInstance(rows.size()));
				dataQueue.add(page);
				dataQueue_size.incrementAndGet();
			}
		} catch (Exception e) {
			CMN.Log(e);
			throw new RuntimeException(e);
		}
	}
	
	public SimpleCursorPage<T> GrowPage(boolean dir) {
		SimpleCursorPage<T> lastPage = dir?pages.get(pages.size()-1):pages.get(0);
		//CMN.Log("GrowPage::", dir, lastPage);
		boolean popData = true;
		SimpleCursorPage<T> ret=null;
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
					if (len==1) return null;
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
					//pages.add(page);
				} else {
					page.st_fd = sort_number;
					page.st_id = id;
					page.pos = lastPage.pos - page.number_of_row;
					page.end = lastPage.pos - 1;
					//basePosOffset += page.number_of_row;
					//pages.add(0, page);
					if (popData) {
						ArrayUtils.reverse(page.rows);
					}
				}
				dataQueue.add(page);
				dataQueue_size.incrementAndGet();
				ret = page;
				//number_of_rows_detected += page.number_of_row;
			}
		} catch (Exception e) {
			CMN.Log(e);
			throw new RuntimeException(e);
		}
		return ret;
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
				//CMN.Log("PreparePageAt::");
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