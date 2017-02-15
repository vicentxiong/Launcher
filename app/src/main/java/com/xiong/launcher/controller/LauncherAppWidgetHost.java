package com.xiong.launcher.controller;

import com.xiong.launcher.Launcher;
import com.xiong.launcher.ui.LauncherAppWidgetHostView;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;

public class LauncherAppWidgetHost extends AppWidgetHost {
	private Launcher mLauncher;

	public LauncherAppWidgetHost(Launcher launcher, int hostId) {
		super(launcher, hostId);
		mLauncher = launcher;
	}
	
	@Override
	protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
			AppWidgetProviderInfo appWidget) {
		// TODO Auto-generated method stub
		return new LauncherAppWidgetHostView(context);
	}

}
