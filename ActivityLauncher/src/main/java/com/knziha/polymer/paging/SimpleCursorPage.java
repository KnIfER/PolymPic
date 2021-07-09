package com.knziha.polymer.paging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class SimpleCursorPage<T extends CursorReader>{
	long st_fd;
	long st_id;
	long ed_fd=Long.MAX_VALUE;
	long ed_id=-1;
	long pos = 0;
	long end = 0;
	int number_of_row;
	T[] rows;
	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd, HH-mm-ss", Locale.CHINA);
	@Override
	public String toString() {
		return "SimplePage{" +
				"st_fd=" + formatter.format(new Date(st_fd)) +
				", ed_fd=" + formatter.format(new Date(ed_fd)) +
				", number_of_row=" + number_of_row +
				", rows=" + (rows!=null?rows[0]+ " ~ " + rows[number_of_row-1]:rows) +
				'}';
	}
}