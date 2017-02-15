package com.xiong.launcher.alg;

import android.util.Log;

public class Xlog {
	public static final String LOG_TAG = "Launcher_debug";
	public static final boolean DISPATCH_TOUCH_DEBUG = false;
	
	public static void logv(String loginfo){
		Log.v(LOG_TAG, loginfo);
	}
	
	public static void logi(String loginfo){
		Log.i(LOG_TAG, loginfo);
	}
	
	public static void logd(String loginfo){
		Log.d(LOG_TAG, loginfo);
	}
	
	public static void logw(String loginfo){
		Log.w(LOG_TAG, loginfo);
	}
	
	public static void loge(String loginfo){
		Log.e(LOG_TAG, loginfo);
	}
}
