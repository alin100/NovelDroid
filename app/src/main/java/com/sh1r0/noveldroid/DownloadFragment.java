package com.sh1r0.noveldroid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.SubmitProcessButton;
import com.sh1r0.noveldroid.downloader.AbstractDownloader;
import com.squareup.otto.Subscribe;

import java.io.File;

public class DownloadFragment extends Fragment {
	private static final int SUCCESS = 0x10000;
	private static final int PREPARING = 0x10001;
	private static final int FAIL = 0x10002;

	private SharedPreferences prefs;
	private NotificationManager mNotificationManager;
	private Menu menu;

	private boolean isBookmarked;
	private Novel novel;
	private AbstractDownloader novelDownloader;
	private String downDirPath;
	private String filename;

	private EditText etNovelName;
	private EditText etAuthor;
	private EditText etFromPage;
	private EditText etToPage;
	private SubmitProcessButton btnDownload;
	private ProgressDialog progressDialog;

	public DownloadFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_download, container, false);
		etNovelName = (EditText) v.findViewById(R.id.et_novel_name);
		etAuthor = (EditText) v.findViewById(R.id.et_author);
		etFromPage = (EditText) v.findViewById(R.id.et_from_page);
		etToPage = (EditText) v.findViewById(R.id.et_to_page);

		btnDownload = (SubmitProcessButton) v.findViewById(R.id.btn_download);
		btnDownload.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				novel.name = etNovelName.getText().toString();
				novel.author = etAuthor.getText().toString();

				if (novel.name.isEmpty()) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.setTitle(R.string.error);
					dialog.setMessage(R.string.empty_name_dialog_msg);
					dialog.setCancelable(false);
					dialog.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							}
					);
					dialog.show();
					return;
				}

				try {
					novel.fromPage = Integer.parseInt(etFromPage.getText().toString());
					novel.toPage = Integer.parseInt(etToPage.getText().toString());
					if (novel.fromPage < 1 || novel.fromPage > novel.toPage
							|| novel.toPage > novel.lastPage) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException e) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
					dialog.setIcon(android.R.drawable.ic_dialog_alert);
					dialog.setTitle(R.string.error);
					dialog.setMessage(R.string.wrong_page_dialog_msg);
					dialog.setCancelable(false);
					dialog.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							}
					);
					dialog.show();
					return;
				}

				btnDownload.setEnabled(false);

				novel.addToShelf(isBookmarked);

				File tempDir = new File(NovelUtils.TEMP_DIR);
				if (!tempDir.exists()) {
					tempDir.mkdirs();
				}

				new Thread(new Runnable() {
					@SuppressLint("Wakelock")
					public void run() {
						PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
						PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "btnDownload");

						wl.acquire();
						try {
							novelDownloader.download(progressHandler);

							downDirPath = prefs.getString("down_dir", NovelUtils.APP_DIR);
							mHandler.sendEmptyMessage(PREPARING);
							filename = novelDownloader.process(downDirPath,
									Integer.parseInt(prefs.getString("naming_rule", "0")),
									prefs.getString("encoding", "UTF-8"));
							if (filename == null) {
								throw new Exception();
							}
							mHandler.sendEmptyMessage(SUCCESS);
						} catch (Exception e) {
							e.printStackTrace();
							mHandler.sendEmptyMessage(FAIL);
						}
						wl.release();
					}
				}).start();
			}
		});

		return v;
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case PREPARING:
					progressDialog = ProgressDialog.show(getActivity(), getResources()
									.getString(R.string.progress_dialog_title),
							getResources().getString(R.string.progress_dialog_msg)
					);
					break;

				case SUCCESS:
					btnDownload.setProgress(0);
					btnDownload.setEnabled(true);
					progressDialog.dismiss();
					Toast.makeText(getActivity(), R.string.download_success_tooltip,
							Toast.LENGTH_LONG).show();

					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri uri = Uri.fromFile(new File(downDirPath + filename));
					intent.setDataAndType(uri, "text/plain");
					String ticker = filename + " " + getString(R.string.novel_saved_tooltip);

					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
							getActivity()).setContentTitle(getString(R.string.app_name))
							.setContentText(downDirPath + filename).setTicker(ticker)
							.setSmallIcon(android.R.drawable.stat_sys_download_done)
							.setAutoCancel(true);
					PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0,
							intent, 0);
					mBuilder.setContentIntent(contentIntent);
					mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.notify(0, mBuilder.build());
					break;

				case FAIL:
					btnDownload.setProgress(0);
					btnDownload.setEnabled(true);
					if (progressDialog != null && progressDialog.isShowing())
						progressDialog.dismiss();
					Toast.makeText(getActivity(), R.string.download_fail_tooltip, Toast.LENGTH_SHORT).show();
					break;
			}
		}
	};

	@SuppressLint("HandlerLeak")
	public Handler progressHandler = new Handler() {
		int completeTaskNum;
		int totalTaskNum;
		int progress;

		@Override
		public void handleMessage(Message msg) {
			if (msg.what < 0) {
				completeTaskNum = 0;
				totalTaskNum = msg.arg1;
				btnDownload.setProgress(0);
				return;
			}

			completeTaskNum += msg.what;
			progress = (int) completeTaskNum * 100 / totalTaskNum;
			btnDownload.setProgress(progress);
		}
	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.removeItem(R.id.menu_search);
		inflater.inflate(R.menu.fragment_download, menu);
		menu.findItem(R.id.menu_bookmark).setVisible(!isBookmarked);
		menu.findItem(R.id.menu_unbookmark).setVisible(isBookmarked);
		this.menu = menu;
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_bookmark:
				toggleBookmark();
				return true;
			case R.id.menu_unbookmark:
				toggleBookmark();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void toggleBookmark() {
		isBookmarked = !isBookmarked;

		novel.addToShelf(isBookmarked);

		menu.findItem(R.id.menu_bookmark).setVisible(!isBookmarked);
		menu.findItem(R.id.menu_unbookmark).setVisible(isBookmarked);
	}

	@Subscribe
	public void onNovelAnalysisDone(Novel novel) {
		etAuthor.setText(novel.author);
		etNovelName.setText(novel.name);
		etFromPage.setText(String.valueOf(novel.fromPage));
		etToPage.setText(String.valueOf(novel.toPage));

		this.isBookmarked = novel.isInShelf();
		this.novelDownloader = DownloaderFactory.getDownloader(novel.siteID);
		this.novel = novel;
	}

	@Override
	public void onResume() {
		super.onResume();
		ApplicationController.getBus().register(this);
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		ApplicationController.getBus().unregister(this);
	}
}
