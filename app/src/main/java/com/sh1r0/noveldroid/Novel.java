package com.sh1r0.noveldroid;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Novel {
	private static final SQLiteDatabase mDB;

	static {
		ShelfDBOpenHelper helper = new ShelfDBOpenHelper(ApplicationController.getContext());
		mDB = helper.getWritableDatabase();
	}

	public static Cursor getAll() {
		return mDB.query(ShelfDBOpenHelper.TABLE_NAME, null, null, null, null, null, "_id ASC");
	}

	public static void deleteID(long id) {
		mDB.delete(ShelfDBOpenHelper.TABLE_NAME, "_id=?", new String[]{String.valueOf(id)});
	}


	public int siteID;
	public String bookID;
	public int fromPage = 1;
	public int toPage;
	public int lastPage = 1;
	public String name;
	public String author;

	public Novel() {
	}

	public Novel(Cursor c) {
		this.siteID = c.getInt(c.getColumnIndex("siteID"));
		this.bookID = String.valueOf(c.getInt(c.getColumnIndex("bookID")));
		this.name = c.getString(c.getColumnIndex("name"));
		this.author = c.getString(c.getColumnIndex("author"));
		this.toPage = this.lastPage;
	}

	public void putOnShelf(boolean on) {
		if (on) {
			ContentValues values = new ContentValues();
			values.put("siteID", siteID);
			values.put("bookID", bookID);
			values.put("name", name);
			values.put("author", author);

			Cursor c = mDB.query(ShelfDBOpenHelper.TABLE_NAME, null, "siteID=? and bookID=?",
					new String[]{String.valueOf(this.siteID), this.bookID}, null, null, null, "1");
			if (c.moveToFirst()) {
				values.put("_id", c.getLong(c.getColumnIndex("_id")));
				mDB.replace(ShelfDBOpenHelper.TABLE_NAME, null, values);
			} else {
				mDB.insert(ShelfDBOpenHelper.TABLE_NAME, null, values);
			}
		} else {
			mDB.delete(ShelfDBOpenHelper.TABLE_NAME, "siteID=? and bookID=?",
					new String[]{String.valueOf(this.siteID), this.bookID});
		}
	}

	public boolean isOnShelf() {
		Cursor c = mDB.query(ShelfDBOpenHelper.TABLE_NAME, null, "siteID=? and bookID=?",
				new String[]{String.valueOf(this.siteID), this.bookID}, null, null, null, "1");
		return c.getCount() > 0;
	}
}
