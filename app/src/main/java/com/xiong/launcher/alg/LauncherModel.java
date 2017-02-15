package com.xiong.launcher.alg;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.xiong.launcher.controller.AppFilter;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

public class LauncherModel extends BroadcastReceiver {
	private HandlerThread mLoaderHandlerThread ;
	private Handler mLoaderHandler;
	private Handler mDispatchHandler;
	private LoadTask mLoadTask ;
	private Context mContext;
	private AppPersistList appPerList;
	private IconCache mIconCache;
	private WeakReference<CallBack> callBacks ;
	
	public LauncherModel(Context cx,IconCache iconCache,AppFilter appFilter){
		this.mContext = cx;
		mIconCache = iconCache;
		mLoaderHandlerThread = new HandlerThread("loader handler");
		mLoaderHandlerThread.start();
		mLoaderHandler = new Handler(mLoaderHandlerThread.getLooper());
		mDispatchHandler = new Handler();
		appPerList = new AppPersistList(appFilter,mIconCache);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(Intent.ACTION_PACKAGE_ADDED.endsWith(action) ||
			Intent.ACTION_PACKAGE_CHANGED.equals(action) ||
			Intent.ACTION_PACKAGE_REMOVED.equals(action)){
			String packageName = intent.getData().getSchemeSpecificPart();
			boolean isReplace = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
			int op = PackageUpdateTask.OP_NONE;
			Xlog.logd("onReceive: packageName "+ packageName +"  isReplace " + isReplace);
			if(Intent.ACTION_PACKAGE_ADDED.equals(action)){
				if(isReplace)
					op = PackageUpdateTask.OP_UPDATE;
				else
					op = PackageUpdateTask.OP_ADD;
			}else if(Intent.ACTION_PACKAGE_REMOVED.equals(action)){
				if(!isReplace){
					op = PackageUpdateTask.OP_REMOVE;
				}
			}else if(Intent.ACTION_PACKAGE_CHANGED.equals(action)){
				op = PackageUpdateTask.OP_UPDATE;
			}
			if(op != PackageUpdateTask.OP_NONE){
				enqueuePackageUpdate(new PackageUpdateTask(op, new String[]{packageName}));
			}
		}
	}
	
	private void enqueuePackageUpdate(Runnable task){
		mLoaderHandler.post(task);
	}
	
	public void initCallBack(CallBack cb){
		callBacks = new WeakReference<LauncherModel.CallBack>(cb);
	}
	
	public void startLoader(){
		mLoadTask = new LoadTask();
		mLoaderHandler.post(mLoadTask);
	}
	
	class PackageUpdateTask implements Runnable{
		private int mOp;
		private String[] packages;

		public static final int OP_NONE = 0;
		public static final int OP_ADD = 1;
		public static final int OP_UPDATE = 2;
		public static final int OP_REMOVE = 3;
		
		public PackageUpdateTask(int mOp, String[] packages) {
			this.mOp = mOp;
			this.packages = packages;
		}

		@Override
		public void run() {
			final String[] pkg = packages;
			int Num = pkg.length;
			switch (mOp) {
			case OP_ADD:
				for (int i = 0; i < Num; i++) {
					appPerList.addPackage(mContext, pkg[i]);
				}
				break;
			case OP_REMOVE:
				for (int i = 0; i < Num; i++) {
					appPerList.removePackage(pkg[i]);
				}
				break;
			case OP_UPDATE:
				for (int i = 0; i < Num; i++) {
					appPerList.updatePackage(mContext, pkg[i]);
				}
				break;
			default:
				break;
			}
			ArrayList<AppInfo> add = null;
			ArrayList<AppInfo> update = null;
			ArrayList<AppInfo> remove = null;
			Xlog.logd("run: remove size:  " + appPerList.removed.size());
			Xlog.logd("run: add size:  " + appPerList.added.size());
			Xlog.logd("run: update size:  " + appPerList.updated.size());
			if(appPerList.added.size()>0){
				add =new ArrayList<AppInfo>(appPerList.added);
				appPerList.added.clear();
			}
			if(appPerList.removed.size()>0){
				remove = new ArrayList<AppInfo>(appPerList.removed);
				appPerList.removed.clear();
			}
			if(appPerList.updated.size()>0){
				update = new ArrayList<AppInfo>(appPerList.updated);
				appPerList.updated.clear();
			}
            if(add != null){
            	bindAddAppsToScreenLayout(add);
            }
            if(remove != null){
            	bindRemoveAppsUpdateScreenLayout(remove);
            }
            if(update!=null){
            	bindUpdateAppsScreenLayout(update);
            }
		}
	}
	
	private void bindUpdateAppsScreenLayout(final ArrayList<AppInfo> updates){
    	Runnable updateRunnable = new Runnable() {
			@Override
			public void run() {
				CallBack cb = tryGetCallback();
				if(cb != null){
					cb.bindUpdateItem(updates);
				}
			}
		};
		runOnMainRunnable(updateRunnable);
	}
	
	private void bindRemoveAppsUpdateScreenLayout(final ArrayList<AppInfo> removes){
    	Runnable removeRunnable = new Runnable() {
			@Override
			public void run() {
				CallBack cb = tryGetCallback();
				if(cb != null){
					cb.bindRemoveItem(removes);
				}
			}
		};
		runOnMainRunnable(removeRunnable);
	}
	
	private void bindAddAppsToScreenLayout(final ArrayList<AppInfo> adds){
    	Runnable addRunnable = new Runnable() {
			@Override
			public void run() {
				CallBack cb = tryGetCallback();
				if(cb != null){
					cb.bindAddItem(adds);
				}
				
			}
		};
		runOnMainRunnable(addRunnable);
	}
	
	private void runOnMainRunnable(Runnable r){
		if(mLoaderHandlerThread.getThreadId() == Process.myTid()){
			mDispatchHandler.post(r);
		}else{
			r.run();
		}
	}
	
	class LoadTask implements Runnable{
		@Override
		public void run() {
			 loadAndbindAllApps();
		}
		
	}
	
	public void loadAndbindAllApps(){
		appPerList.clear();
		final PackageManager packageManager = mContext.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        for (ResolveInfo resolveInfo : apps) {
			appPerList.addApp(new AppInfo(packageManager, resolveInfo, mIconCache));
		}
        final ArrayList<AppInfo> appinfos = new ArrayList<AppInfo>(appPerList.added);
        appPerList.added.clear();
        runOnMainRunnable(new Runnable() {
			@Override
			public void run() {
				CallBack mCallback = tryGetCallback();
				if(mCallback !=null){
					mCallback.bindAllAppsOnWorkspace(appinfos);
				}
				
			}
		});
	}
	
	public CallBack tryGetCallback(){
		CallBack cb = null;
		if(callBacks !=null)
			cb = callBacks.get();
		return cb;
	}
	
	public interface CallBack{
		//加载完数据后回调 
		public void bindAllAppsOnWorkspace(ArrayList<AppInfo> apps);
		
		public void bindAddItem(ArrayList<AppInfo> adds);
		
		public void bindRemoveItem(ArrayList<AppInfo> removes);
		
		public void bindUpdateItem(ArrayList<AppInfo> updates);
	}
	

}
