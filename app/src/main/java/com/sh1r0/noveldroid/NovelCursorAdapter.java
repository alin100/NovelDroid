package com.sh1r0.noveldroid;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

/**
 * Created by shiro on 2014/8/11 011.
 */
public class NovelCursorAdapter extends ResourceCursorAdapter {
	public NovelCursorAdapter(Context context, Cursor c) {
		super(context, android.R.layout.simple_list_item_1, c);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView tvName = (TextView) view.findViewById(android.R.id.text1);
		tvName.setText(cursor.getString(cursor.getColumnIndex("name")));
	}
}
