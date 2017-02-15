package com.xiong.launcher.alg;

import java.util.ArrayList;
import java.util.List;

import com.xiong.launcher.controller.AppFilter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;


public class AppPersistList {
	private ArrayList<AppInfo> allApps = new ArrayList<AppInfo>();
	public ArrayList<AppInfo> added =  new ArrayList<AppInfo>();
	public ArrayList<AppInfo> updated =  new ArrayList<AppInfo>();
	public ArrayList<AppInfo> removed  = new ArrayList<AppInfo>();
	private AppFilter mAppFilter;
	private IconCache mIconCache;
	
	public AppPersistList(AppFilter appFilter,IconCache ic){ 
		mAppFilter = appFilter;
		mIconCache = ic;
	}
	
	public void addApp(AppInfo app){
		if(mAppFilter != null && !mAppFilter.shoudAppShow(app.componentName))
			return ;
		if(findActivity(allApps, app)){
			return;
		}
		allApps.add(app);
		added.add(app);
	}
	
	public void clear(){
		allApps.clear();
		added.clear();
		removed.clear();
		updated.clear();
	}
	
	private boolean findActivity(List<AppInfo> apps,AppInfo addingApp){
		final int num = apps.size();
		for (int i = 0; i < num; i++) {
			AppInfo app = apps.get(i);
			if(app.equals(addingApp))
				return true;
		}
		return false;
	}
	
	private boolean findActivity(ArrayList<ResolveInfo> rsvInfo,AppInfo appinfo){
		final int Num = rsvInfo.size();
		for (int i = 0; i < Num; i++) {
			ResolveInfo r = rsvInfo.get(i);
			ApplicationInfo info = r.activityInfo.applicationInfo;
			ComponentName cpm = new ComponentName(info.packageName,r.activityInfo.name);
			if(appinfo.getComponentName().equals(cpm)){
				return true;
			}
		}
		return false;
	}
	
	public void addPackage(Context context,String packageName){
		List<ResolveInfo> addApps = findActivityByPackageName(context, packageName);
		if(addApps.size()>0){
			for (ResolveInfo resolveInfo : addApps) {
				addApp(new AppInfo(context.getPackageManager(), resolveInfo, mIconCache));
			}
		}
	}
	
	public void removePackage(String packageName){
		int appsNum = allApps.size();
		for (int i = 0; i < appsNum; i++) {
			AppInfo a = allApps.get(i);
			if(packageName.equals(a.getComponentName().getPackageName())){
				removed.add(a);
				mIconCache.remove(a.getComponentName());
			}
		}
		removeAppFromAllApps(removed);
	}
	
	public void updatePackage(Context context,String packageName){
		List<ResolveInfo> matchs = findActivityByPackageName(context, packageName);
		if(matchs.size()>0){
			int count = allApps.size();
			for (int i = 0; i < count; i++) {
				AppInfo a = allApps.get(i);
				if(packageName.equals(a.getComponentName().getPackageName()) && !findActivity((ArrayList<ResolveInfo>)matchs, a)){
					removed.add(a);
				}
			}
			removeAppFromAllApps(removed);
			
			int matchCount = matchs.size();
			for (int i = 0; i < matchCount; i++) {
				final ResolveInfo r = matchs.get(i);
				final String pkgName = r.activityInfo.applicationInfo.packageName;
				final String className = r.activityInfo.name;
				AppInfo appInfo = findAppInfo(pkgName, className);
				if(appInfo ==  null){
					addApp(new AppInfo(context.getPackageManager(),r, mIconCache));
				}else{
					mIconCache.remove(appInfo.getComponentName());
					mIconCache.getIconAndTitle(appInfo, r);
					updated.add(appInfo);
				}
			}
		}else{
			for (int i = 0; i < allApps.size(); i++) {
				AppInfo a = allApps.get(i);
				ComponentName cmp = a.getComponentName();
				if(packageName.equals(cmp.getPackageName())){
					removed.add(a);
					mIconCache.remove(cmp);
				}
			}
			removeAppFromAllApps(removed);
		}
	}
	
	private AppInfo findAppInfo(String pkg,String className){
		AppInfo a = null;
		int N = allApps.size();
		for (int i = 0; i < N; i++) {
			AppInfo info = allApps.get(i);
			if(pkg.equals(info.getComponentName().getPackageName()) && className.equals(info.getComponentName().getClassName())){
				a = info;
				break;
			}
		}
		return a;
	}
	
	private void removeAppFromAllApps(ArrayList<AppInfo> removes){
		ArrayList<AppInfo> rmApps = removes;
		int Num = rmApps.size();
		for (int i = 0; i < Num; i++) {
			AppInfo rmApp = rmApps.get(i);
			allApps.remove(rmApp);
		}
	}
	
	public List<ResolveInfo> findActivityByPackageName(Context context,String packageName){
		final PackageManager pm = context.getPackageManager();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.setPackage(packageName);
		
		List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
		return apps!=null ? apps : new ArrayList<ResolveInfo>();
	}
	

}
