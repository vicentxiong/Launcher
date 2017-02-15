package com.xiong.launcher.alg;

import java.util.List;

import com.xiong.launcher.Launcher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;

public class AppInfo extends ItemInfo{

	 public Intent intent;  // 启动的intent
	
	 public String tiltle;  // 程序名称  
	
	 public Bitmap bitmap; // 程序图标   
	
	 long firstInstallTime; // 程序第一次安装的时间  
	
	 public int flags = 0; 
	 
	 ComponentName componentName;
	 
	 public static final int DOWNLOADED_FLAG = 1<<0;
	 public static final int UPDATED_SYSTEM_APP_FLAG = 1<<1;
	
	public AppInfo(PackageManager pm, ResolveInfo resolveInfo ,IconCache iconCache){
		xSpan = 1;
		ySpan = 1;
		itemType = Launcher.Settings.APPINFO_ITEM;
		String packageName = resolveInfo.activityInfo.applicationInfo.packageName;
		componentName = new ComponentName(packageName, resolveInfo.activityInfo.name);
		setActivity(componentName);
		setFlagAndFirstInstallTime(pm, packageName);
		iconCache.getIconAndTitle(this, resolveInfo);
	}
	
	public ComponentName getComponentName(){
		ComponentName name = null;
		if(componentName != null){
			name = componentName;
		}
		return name;
	}
	
	private final void setActivity(ComponentName componentName){
		intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setComponent(componentName);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	}
	
	private final void setFlagAndFirstInstallTime(PackageManager pm ,String packageName){
		try {
			PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
			firstInstallTime = packageInfo.firstInstallTime;
			flags = initFlags(packageInfo);
		} catch (NameNotFoundException e) {
			
			e.printStackTrace();
		}
	}
	
		private final int initFlags(PackageInfo packageInfo){
	        int appFlags = packageInfo.applicationInfo.flags;
	        int flags = 0;
	        if ((appFlags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
	            flags |= DOWNLOADED_FLAG;
	
	            if ((appFlags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
	                flags |= UPDATED_SYSTEM_APP_FLAG;
	            }
	        }
	        return flags;
		}
	
	@Override
	public String toString() {
		String info  = "name : " + tiltle;
		return info;
	}
	
}
