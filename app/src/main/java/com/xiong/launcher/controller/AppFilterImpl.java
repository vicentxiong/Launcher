package com.xiong.launcher.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;

public class AppFilterImpl extends AppFilter {
	public static List<ComponentName> filteredApp = new ArrayList<ComponentName>();
	
	static{
		filteredApp.add(new ComponentName("com.xiong.launcher", "com.xiong.launcher.Launcher"));
	}
	

	@Override
	public boolean shoudAppShow(ComponentName cmp) {
		ComponentName c = cmp;
		for (ComponentName filterapp : filteredApp) {
			if(filterapp.equals(c)) return false;
		}
		return true;
	}

}
