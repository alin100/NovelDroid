package com.sh1r0.noveldroid;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ShelfFragment extends ListFragment {
	private OnShelfFragmentListener mListener;
	private NovelCursorAdapter adapter;

	public ShelfFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		adapter = new NovelCursorAdapter(getActivity(), Novel.getAll());
		setListAdapter(adapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_shelf, container, false);
	}

	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnShelfFragmentListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnShelfFragmentListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (null != mListener) {
			mListener.onShelfItemClick(new Novel((Cursor) adapter.getItem(position)));
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.shelf_context, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.context_edit:
				// TODO: edit entry
				return true;
			case R.id.context_delete:
				Novel.deleteID(info.id);
				adapter.getCursor().requery();
				adapter.notifyDataSetChanged();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.removeItem(R.id.menu_search);
	}

	public interface OnShelfFragmentListener {
		public void onShelfItemClick(Novel novel);
	}
}
