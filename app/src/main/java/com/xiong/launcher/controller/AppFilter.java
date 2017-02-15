package com.xiong.launcher.controller;

import android.content.ComponentName;

public abstract class AppFilter {

	public static AppFilter loadByName(String className){
		if(className == null) return null;
		try {
			Class<?> c = Class.forName(className);

			return (AppFilter) c.newInstance();

		} catch (ClassNotFoundException e) {
			return null;
		}catch (IllegalAccessException e) {
			return null;
		}catch (InstantiationException e) {
			return null;
		}
	}
	
	public abstract boolean shoudAppShow(ComponentName cmp);
}
