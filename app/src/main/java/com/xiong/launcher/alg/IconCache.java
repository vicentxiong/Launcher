package com.xiong.launcher.alg;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class IconCache {
	private final static int MAX_CAPACITY = 50;
	private HashMap<ComponentName, IconCacheEntry> cache = new HashMap<ComponentName, IconCache.IconCacheEntry>(MAX_CAPACITY);
	private Context mContext;
	private PackageManager packageMgr;
	private ActivityManager activityMgr;
	private int mIconDpi;
	
	public IconCache(Context context){
		mContext = context;
		packageMgr = mContext.getPackageManager();
		activityMgr = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		mIconDpi = activityMgr.getLauncherLargeIconDensity();
	}
	
	
	public void getIconAndTitle(AppInfo appInfo,ResolveInfo resolveInfo){
		IconCacheEntry entry = lockIconCache(appInfo.componentName,resolveInfo);
		appInfo.tiltle = entry.title;
		appInfo.bitmap = entry.bitmap;
	}
	
	//通过享元模式 设计 缓存 
	private IconCacheEntry lockIconCache(ComponentName cmp,ResolveInfo resolveInfo){
		IconCacheEntry entry = cache.get(cmp);
		if(entry == null){
			entry = new IconCacheEntry();
			entry.title = resolveInfo.loadLabel(packageMgr).toString();
			if(entry.title==null){
				entry.title = resolveInfo.activityInfo.name;
			}
			entry.bitmap = Utils.createIconBitmap(getFullResIcon(resolveInfo), mContext);
			cache.put(cmp, entry);
		}
		return entry;
	}
	
	private Drawable getFullResIcon(ResolveInfo resolveInfo){
		Resources resource = null;
		try {
			resource = packageMgr.getResourcesForApplication(resolveInfo.activityInfo.applicationInfo);
		} catch (NameNotFoundException e) {
			resource = null;
		}
		if(resource != null){
			int icon = resolveInfo.getIconResource();
			if(icon == 0)
				icon = android.R.mipmap.sym_def_app_icon;
			return getFullResIcon(resource, icon);
		}
		return null;
	}
	
	@SuppressLint("NewApi")
	private Drawable getFullResIcon(Resources resource,int icon){
		Drawable d = null;
		d = resource.getDrawableForDensity(icon, mIconDpi);
		return d;
	}
	
	public void remove(ComponentName componentName){
		cache.remove(componentName);
	}

	private static class IconCacheEntry{
		String title;
		Bitmap bitmap;
	}
}
