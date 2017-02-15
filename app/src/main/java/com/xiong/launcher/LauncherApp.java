package com.xiong.launcher;

import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.IconCache;
import com.xiong.launcher.alg.LauncherModel;
import com.xiong.launcher.alg.Utils;
import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.alg.LauncherModel.CallBack;
import com.xiong.launcher.controller.AppFilter;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.DisplayMetrics;

public class LauncherApp extends Application {
	private LauncherModel model;
	private IconCache iconCache;
	private DeviceProfile devProfile;

	@Override
	public void onCreate() {
		super.onCreate();
		iconCache = new IconCache(this);
		AppFilter mAppFilter = AppFilter.loadByName(getResources().getString(R.string.app_filter_class));
		model = new LauncherModel(this,iconCache,mAppFilter);
		
		//注册程序的安装  卸载  改变 等 广播 
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(model, filter);
        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        registerReceiver(model, filter);
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		//取消注册的广播接收器 
		unregisterReceiver(model);
	}
	
	public void initCallBacks(CallBack cb){
		model.initCallBack(cb);
	}
	
	public void startLoader(){
		Xlog.logd("start loader !");
		model.startLoader();
	}
	
	public void initDeviceProfile(DisplayMetrics dm){
		devProfile = new DeviceProfile(this, dm);
		devProfile.initConfigure();
		
		Utils.setIconSize(devProfile.iconAppsize);
	}
	
	public DeviceProfile getDeviceProfile(){
		return devProfile;
	}
}
