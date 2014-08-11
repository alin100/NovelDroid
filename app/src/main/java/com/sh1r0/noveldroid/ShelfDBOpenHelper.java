package com.sh1r0.noveldroid;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ShelfDBOpenHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "shelf.db";
	private static final int DB_VER = 1;
	public static final String TABLE_NAME = "NDShelf";

	public ShelfDBOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VER);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (");
		sql.append("_id INTEGER PRIMARY KEY AUTOINCREMENT, ");
		sql.append("siteID INTEGER NOT NULL, ");
		sql.append("bookID INTEGER NOT NULL, ");
		sql.append("name TEXT, ");
		sql.append("author TEXT ");
		sql.append(");");
		db.execSQL(sql.toString());
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
	}
}
