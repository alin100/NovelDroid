package com.sh1r0.noveldroid;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.squareup.otto.Produce;

import java.lang.reflect.Field;

public class MainActivity extends ActionBarActivity
		implements AdapterView.OnItemClickListener, ShelfFragment.OnShelfFragmentListener {

	private static final int API_VERSION = Build.VERSION.SDK_INT;

	private int width;
	private EditText etID;
	private ActionProcessButton btnAnalyze;
	private Spinner spnDomain;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private boolean isDrawerOpen = false;
	private Novel novel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		width = getScreenWidth();
		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		spnDomain = (Spinner) findViewById(R.id.spn_doamin);
		ArrayAdapter<String> spnAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, this.getResources().getStringArray(R.array.domain)
		);
		spnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spnDomain.setAdapter(spnAdapter);
		spnDomain.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				btnAnalyze.setProgress(0);
				btnAnalyze.setEnabled(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		etID = (EditText) findViewById(R.id.et_id);
		etID.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				btnAnalyze.setProgress(0);
				btnAnalyze.setEnabled(true);
			}
		});

		btnAnalyze = (ActionProcessButton) findViewById(R.id.btn_analyze);
		btnAnalyze.setMode(ActionProcessButton.Mode.ENDLESS);
		btnAnalyze.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isNetworkConnected()) {
					Toast.makeText(getApplicationContext(), R.string.no_connection_tooltip, Toast.LENGTH_SHORT).show();
					return;
				}

				btnAnalyze.setEnabled(false);
				String bookID = etID.getText().toString();
				if (bookID.isEmpty()) {
					etID.setError(getResources().getString(R.string.novel_id_tooltip));
					btnAnalyze.setProgress(-1);
					return;
				}

				btnAnalyze.setProgress(1);

				try {
					if ((novel = DownloaderFactory.analyze(spnDomain.getSelectedItemPosition(), bookID)) == null) {
						throw new Exception();
					}
				} catch (Exception e) {
					String err = (e.getMessage() == null) ? "analysis fail" : e.getMessage();
					Log.e("Error", err);
					btnAnalyze.setProgress(-1);
					return;
				}

				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.main_layout, new DownloadFragment());
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();

				btnAnalyze.setProgress(0);
				btnAnalyze.setEnabled(true);
			}
		});

		DrawerItem[] menu = new DrawerItem[]{
				DrawerItem.create(0, R.string.search, R.drawable.ic_action_search, this),
				DrawerItem.create(1, R.string.settings, R.drawable.ic_action_settings, this),
				DrawerItem.create(2, R.string.shelf, R.drawable.ic_fa_book, this),
				DrawerItem.create(3, R.string.quit, R.drawable.ic_action_quit, this)};

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new DrawerItemAdapter(this, R.layout.drawer_list_item, menu));
		mDrawerList.setOnItemClickListener(this);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, 0, 0) {
			@Override
			public void onDrawerClosed(View drawerView) {
				supportInvalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				supportInvalidateOptionsMenu();
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				if (slideOffset > .1 && !isDrawerOpen) {
					onDrawerOpened(drawerView);
					isDrawerOpen = true;
				} else if (slideOffset < .1 && isDrawerOpen) {
					onDrawerClosed(drawerView);
					isDrawerOpen = false;
				}
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		try {
			Field mDragger = mDrawerLayout.getClass().getDeclaredField("mLeftDragger");
			mDragger.setAccessible(true);
			ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(mDrawerLayout);
			Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
			mEdgeSize.setAccessible(true);
			int edge = mEdgeSize.getInt(draggerObj);
			mEdgeSize.setInt(draggerObj, edge * 10);
		} catch (Exception e) {
			e.printStackTrace();
		}

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
	}

	@Produce
	public Novel produceAnalyzedNovelInfo() {
		return this.novel;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		mDrawerLayout.closeDrawer(mDrawerList);

		switch (position) {
			case 0: // search
				popupSearchDialog();
				break;
			case 1: // settings
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case 2: // shelf
				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				fragmentTransaction.replace(R.id.main_layout, new ShelfFragment());
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				break;
			case 3: // exit
				finish();
				break;
			default:
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item;
		if ((item = menu.findItem(R.id.menu_search)) != null) {
			item.setVisible(!isDrawerOpen);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		if (item.getItemId() == R.id.menu_search) {
			popupSearchDialog();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ApplicationController.getBus().register(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		ApplicationController.getBus().unregister(this);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		View v = getCurrentFocus();
		boolean ret = super.dispatchTouchEvent(event);

		if (v instanceof EditText) {
			View w = getCurrentFocus();
			int scrcoords[] = new int[2];
			w.getLocationOnScreen(scrcoords);
			float x = event.getRawX() + w.getLeft() - scrcoords[0];
			float y = event.getRawY() + w.getTop() - scrcoords[1];

			if (event.getAction() == MotionEvent.ACTION_UP
					&& (x < w.getLeft() || x >= w.getRight() || y < w.getTop() || y > w.getBottom())) {
				closeKeyboard();
			}
		}
		return ret;
	}

	private boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void closeKeyboard() {
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@SuppressWarnings("deprecation")
	private int getScreenWidth() {
		Display display = getWindowManager().getDefaultDisplay();
		if (API_VERSION >= 13) {
			Point size = new Point();
			display.getSize(size);
			width = size.x;
		} else {
			width = display.getWidth();
		}
		return width;
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void popupSearchDialog() {
		LayoutInflater factory = LayoutInflater.from(this);
		final View searchDialogView = factory.inflate(R.layout.search_dialog, null);
		final AlertDialog searchDialog = new AlertDialog.Builder(this).setTitle(R.string.search)
				.setNegativeButton(R.string.close, null).setCancelable(false).create();
		searchDialog.setView(searchDialogView);

		final WebView wv = (WebView) searchDialogView.findViewById(R.id.wv_search);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl("https://googledrive.com/host/0By9mvBCbgqrycV9naFJSYm5mbjQ");
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		wv.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((keyCode == KeyEvent.KEYCODE_BACK)) {
					if (wv.canGoBack()) {
						wv.goBack();
					} else {
						searchDialog.dismiss();
					}
					return true;
				}
				return false;
			}
		});

		searchDialog.show();
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.copyFrom(searchDialog.getWindow().getAttributes());
		lp.width = width;
		searchDialog.getWindow().setAttributes(lp);
	}

	@Override
	public void onShelfItemClick(Novel novel) {
		this.novel = novel;
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.main_layout, new DownloadFragment());
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
}
