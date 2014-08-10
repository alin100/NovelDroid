package com.sh1r0.noveldroid;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;

public class ApplicationController extends Application {
	private static Context appContext;
	private static final Bus BUS = new Bus();

	@Override
	public void onCreate() {
		super.onCreate();
		appContext = getApplicationContext();
	}

	public static Context getContext() {
		return appContext;
	}

	public static Bus getBus() {
		return BUS;
	}
}
