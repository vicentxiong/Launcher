package com.xiong.launcher.alg;

import com.xiong.launcher.Launcher;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;

public class LauncherAppWidgetInfo extends ItemInfo {

	public int appWidgetId = NO_ID;
	public ComponentName cmp;
	public AppWidgetHostView hostView;
	
	public LauncherAppWidgetInfo(int id,ComponentName componentName){
		itemType = Launcher.Settings.APPWIDGET_ITEM;
		appWidgetId = id;
		cmp = componentName;
	}
	
	
}
