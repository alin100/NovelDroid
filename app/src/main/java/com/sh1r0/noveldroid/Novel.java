package com.sh1r0.noveldroid;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Novel {
	private static final SQLiteDatabase mDB;

	static {
		ShelfOpenHelper helper = new ShelfOpenHelper(ApplicationController.getContext());
		mDB = helper.getWritableDatabase();
	}


	public int siteID;
	public String bookID;
	public int fromPage = 1;
	public int toPage;
	public int lastPage = 1;
	public String name;
	public String author;

	public void addToShelf(boolean val) {
		if (val) {
			mDB.execSQL("REPLACE INTO " + ShelfOpenHelper.TABLE_NAME + " (siteID, bookID, name, author) VALUES (?,?,?,?);",
					new Object[]{(long) siteID, Long.valueOf(bookID), name, author});
		} else {
			mDB.delete(ShelfOpenHelper.TABLE_NAME, "siteID=? and bookID=?",
					new String[]{String.valueOf(this.siteID), this.bookID});
		}
	}

	public boolean isInShelf() {
		Cursor row = mDB.query(ShelfOpenHelper.TABLE_NAME, null, "siteID=? and bookID=?",
				new String[]{String.valueOf(this.siteID), this.bookID}, null, null, null, "1");
		return row.getCount() > 0;
	}
}
