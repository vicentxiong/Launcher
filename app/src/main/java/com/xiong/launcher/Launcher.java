package com.xiong.launcher;

import java.util.ArrayList;

import com.xiong.launcher.alg.AppInfo;
import com.xiong.launcher.alg.FolderInfo;
import com.xiong.launcher.alg.ItemInfo;
import com.xiong.launcher.alg.LauncherAppWidgetInfo;
import com.xiong.launcher.alg.LauncherModel.CallBack;
import com.xiong.launcher.alg.ShortCutInfo;
import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.controller.AnimationUtil;
import com.xiong.launcher.controller.LauncherAppWidgetHost;
import com.xiong.launcher.ui.AnimationSelectDialog;
import com.xiong.launcher.ui.CellLayout;
import com.xiong.launcher.ui.CellLayout.LayoutParams;
import com.xiong.launcher.ui.DragLayer;
import com.xiong.launcher.ui.Folder;
import com.xiong.launcher.ui.FolderIcon;
import com.xiong.launcher.ui.HotSeat;
import com.xiong.launcher.ui.SelectDialog;
import com.xiong.launcher.ui.ShadowFrameLayout;
import com.xiong.launcher.ui.WorkSpaceDividing;
import com.xiong.launcher.ui.Workspace;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Launcher extends Activity implements CallBack, OnLongClickListener,OnClickListener{
	private LauncherApp app;
	private Workspace mWorkSpace;
	private WorkSpaceDividing mDividing;
	private DragLayer mDragLayer;
	private LinearLayout mDeleteZone;
	private ImageView mDeleteImg;
	private HotSeat mHotseat;
	private CellLayout layout;
	private Folder mFolder;
	private LinearLayout mUninstallFolderArea;
	private ImageView mFolderBackground;
	private Button mFolderComfire;
	private Button mFolderCancel;
	private TextView mFolderLabel;
	private ShadowFrameLayout sdFramelayout;
	public static final int DELETEZONE_IN = 0;
	public static final int DELETEZONE_OUT = 1;
	public static final int APPWIDGET_HOST_ID = 1024;
    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;
	private int mDeleteZoneState = DELETEZONE_OUT;
	public static final int UNINSTALL_FOLDER_ZENO = 0;
	public static final int UNINSTALL_FOLDER_LESSONE = 1;
	private LauncherAppWidgetHost appWidgetHost;
	private AppWidgetManager appWidgetManager;
	private ArrayList<FolderUpdateListener> listeners ;
	private Bitmap mFolderBackGroundBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT>=19){
	        getWindow().addFlags(0x04000000);
	        getWindow().addFlags(0x08000000);
		}
		
		app = (LauncherApp) getApplication();
		Display display = getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		display.getMetrics(dm);
		app.initDeviceProfile(dm);
		app.initCallBacks(this);
		appWidgetHost = new LauncherAppWidgetHost(this, APPWIDGET_HOST_ID);
		appWidgetHost.startListening();
		appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		setContentView(R.layout.launcher);
		setUpViews();
		app.getDeviceProfile().layout(this);
		app.startLoader();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mWorkSpace.resetIconDrawableState();
	}
	
	private void setUpViews(){
		mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
	    mWorkSpace = (Workspace) findViewById(R.id.workspace);
	    mDividing  = (WorkSpaceDividing) findViewById(R.id.workspace_dividing);
	    mDeleteZone = (LinearLayout) findViewById(R.id.deleteZone);
	    mDeleteImg = (ImageView) mDeleteZone.findViewById(R.id.deleteImg);
		mWorkSpace.setHapticFeedbackEnabled(false);
		mWorkSpace.setOnLongClickListener(this);
		mDragLayer.setonDragItemLister(mWorkSpace);
		mWorkSpace.setDividingLister(mDividing);
	    mHotseat = (HotSeat) findViewById(R.id.hotseats);
	    mHotseat.initChildView(this);
	    mFolder  = (Folder) findViewById(R.id.folder);
	    mUninstallFolderArea = (LinearLayout) findViewById(R.id.folder_dissolve_area);
	    mFolderBackground = (ImageView) mUninstallFolderArea.findViewById(R.id.folder_background);
	    mFolderComfire = (Button) mUninstallFolderArea.findViewById(R.id.folder_dissolve_comfire);
	    mFolderCancel = (Button) mUninstallFolderArea.findViewById(R.id.folder_dissolve_cancel);
	    mFolderLabel = (TextView) findViewById(R.id.uninstall_folder_name);
	    mFolderComfire.setOnClickListener(this);
	    mFolderCancel.setOnClickListener(this);
	    sdFramelayout = (ShadowFrameLayout) findViewById(R.id.shadowlayer);
	}
	
	public void addFolderUpdateListener(FolderUpdateListener l){
		if(listeners == null){
			listeners = new ArrayList<FolderUpdateListener>();
		}
		listeners.add(l);
	}
	
	public void removeFolderUpdateListener(FolderUpdateListener l){
		if(listeners != null){
			if(listeners.contains(l))
				listeners.remove(l);
		}
	}
	
	public Workspace getWorkSpace(){
		return mWorkSpace;
	}
	
	public Folder getFolder(){
		return mFolder;
	}
	
	public WorkSpaceDividing getwsDividing(){
		return mDividing;
	}
	
	public HotSeat getHotSeat(){
		return mHotseat;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.launcher, menu);
		return true;
	}
	
	@SuppressLint("NewApi")
	public void startActivitySafely(View v,Intent intent){
		ActivityOptions opts = ActivityOptions.makeScaleUpAnimation(v, 0, 0,v.getMeasuredWidth(), v.getMeasuredHeight());
		startActivity(intent,opts.toBundle());
	}
	
	public  LinearLayout getDeleteZone(){
		return mDeleteZone;
	}
	
	public boolean atDeleteZone(){
		return mDeleteZoneState == DELETEZONE_IN;
	}
	
	public void onDragIntoDeleteZone(int top){
		int deleteZoneHeight = mDeleteZone.getMeasuredHeight();
		if(top < deleteZoneHeight && mDeleteZoneState == DELETEZONE_OUT){
			mDeleteImg.setImageResource(R.drawable.delete_zone_trush_in);
			AnimationDrawable animationIn = (AnimationDrawable) mDeleteImg.getDrawable();
			animationIn.start();
			mDeleteZoneState = DELETEZONE_IN;
			dropDownDragLayer(R.drawable.delete_zone_bg, deleteZoneHeight);
		}else if(top > deleteZoneHeight && mDeleteZoneState == DELETEZONE_IN){
			mDeleteImg.setImageResource(R.drawable.delete_zone_trush_out);
			AnimationDrawable animationOut = (AnimationDrawable) mDeleteImg.getDrawable();
			animationOut.start();
			mDeleteZoneState = DELETEZONE_OUT;
            dropDownDragLayer(0, 0);
		}
	}
	
	public void onDragIntoDeleteZoneComplete(ItemInfo itemInfo,int top){
		final int type = itemInfo.itemType;
		int deleteZoneHeight = mDeleteZone.getMeasuredHeight();
		if(top < deleteZoneHeight ){
			switch (type) {
			case Launcher.Settings.APPINFO_ITEM:
				AppInfo appInfo = (AppInfo) itemInfo;
				int flag = appInfo.flags;
				String packageName = appInfo.getComponentName().getPackageName();
				if( flag == AppInfo.DOWNLOADED_FLAG || flag == AppInfo.UPDATED_SYSTEM_APP_FLAG){
					Intent unInstallIntent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:"+packageName));
					startActivity(unInstallIntent);
				}else{
					Toast.makeText(getApplicationContext(), R.string.system_app_tip, 1000).show();
				}
				dropDownDragLayer(0, 0);
				break;
			case Launcher.Settings.APPWIDGET_ITEM:
				mWorkSpace.deleteAppWidgetViewFormLayout(appWidgetHost);
				dropDownDragLayer(0, 0);
				break;
			case Launcher.Settings.SHORTCUT_ITEM:
				mWorkSpace.deleteShortCutViewFromLayout();
				dropDownDragLayer(0, 0);
				break;
			case Launcher.Settings.FOLDER_ITEM:
				int var = mWorkSpace.deleteOrDissolveFolderFromLayout();
				if(var == UNINSTALL_FOLDER_ZENO){
					dropDownDragLayer(0, 0);
				}else{
					mDeleteZone.setBackgroundResource(0);
				}
				break;
			default:
				break;
			}
			mDeleteZoneState = DELETEZONE_OUT;
			
		}
	
	}
	
	@Override
	public boolean onSearchRequested() {
		startSearch(null, false, null, true);
		return true;
	}

	@SuppressLint("NewApi")
	private void dropDownDragLayer(int res,int height){
		mDeleteZone.setBackgroundResource(res);
		ObjectAnimator.ofFloat(mDragLayer, "TranslationY", height).setDuration(200).start();
		
	}
	
	public void setFolderDissolveShow(Bitmap b,String label){
		mFolderBackGroundBitmap = b;
		mFolderBackground.setImageBitmap(mFolderBackGroundBitmap);
		mFolderLabel.setText(label);
		mUninstallFolderArea.setVisibility(View.VISIBLE);
		mUninstallFolderArea.setSystemUiVisibility(View.INVISIBLE);
		mUninstallFolderArea.postDelayed(new Runnable() {
			@Override
			public void run() {
				int height = mUninstallFolderArea.getMeasuredHeight();
				ObjectAnimator.ofFloat(mDragLayer, "TranslationY", height).setDuration(200).start();
				sdFramelayout.setVisibility(View.VISIBLE);
			}
		}, 100);

	}
	
	public void setFolderDissolveDisAppear(){
		mUninstallFolderArea.setVisibility(View.GONE);
		mUninstallFolderArea.setSystemUiVisibility(View.VISIBLE);
		mFolderBackground.setImageBitmap(null);
		mFolderBackGroundBitmap.recycle();
		mFolderBackGroundBitmap = null;
		ObjectAnimator.ofFloat(mDragLayer, "TranslationY", 0).setDuration(200).start();
		sdFramelayout.setVisibility(View.GONE);
	}
	
	public void beginDragging(View v){
		mDragLayer.startDraggingItem(v);
	}
	
	public boolean isDragging(){
		return mDragLayer.getDragState();
	}
	
	public void setChangeScreen(){
		mDragLayer.setChangeScreenEnable();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			do {
				if(sdFramelayout.getVisibility() == View.VISIBLE){
					setFolderDissolveDisAppear();
					break;
				}
				if(mFolder.ismEditMode()){
					mFolder.setmEditMode(false);
					mFolder.updateEditView();
					Xlog.logd("folder edit status");
					break;
				}
				if(mFolder.getVisibility() == View.VISIBLE){
					mWorkSpace.dismissFolder();
					break;
				}
				if(mWorkSpace.getMode() == Workspace.EDITING){
					if(!mWorkSpace.isEdittingScreen()){
						mWorkSpace.finishEditCellLayout();
						break;
					}
				}
			} while (false);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	@Override
	public void bindAllAppsOnWorkspace(ArrayList<AppInfo> apps) {
		mWorkSpace.setApps(apps);
	}
	
	@Override
	public void bindAddItem(ArrayList<AppInfo> adds) {
		final ArrayList<AppInfo> apps = adds;
 		for (int i = 0; i < apps.size(); i++) {
			mWorkSpace.bindAddAppOnScreen(apps.get(i));
		}
	}
	
	@Override
	public void bindRemoveItem(ArrayList<AppInfo> removes) {
		final ArrayList<AppInfo> removeds = removes;
		int Num = removeds.size();
		for (int i = 0; i < Num; i++) {
			mWorkSpace.bindRemoveAppOnScreen(removeds.get(i));
			if(listeners != null){
				int num = listeners.size();
				for (int j = 0; j < num; j++) {
					listeners.get(j).onRemoveFolderDynamic(removeds.get(i));
				}
			}
		}
	}
	
	@Override
	public void bindUpdateItem(ArrayList<AppInfo> updates) {
		final ArrayList<AppInfo> update = updates;
		int N = update.size();
		for (int i = 0; i < N; i++) {
			mWorkSpace.bindUpdateAppOnScreen(update.get(i));
			if(listeners != null){
				int num = listeners.size();
				for (int j = 0; j < num; j++) {
					listeners.get(j).onUpdateFolderDynamic(update.get(i));
				}
			}
		}
	}

	@Override
	public boolean onLongClick(View v) {
		mWorkSpace.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
		int mode = mWorkSpace.getMode();
		if(mode == Workspace.NORMAL){
			mWorkSpace.editingCellLayout();
		}else{
			if(!mWorkSpace.isEdittingScreen()){
				mWorkSpace.finishEditCellLayout();
			}
		}
		
		return true;
	}

	@Override
	public void onClick(View v) {
		SelectDialog dialog = null;
		switch (v.getId()) {
		case R.id.animationItem:
			dialog = new AnimationSelectDialog(this);
			dialog.show();
			break;
		case R.id.wallPaperItem:
			Intent chooseIntent = new Intent(Intent.ACTION_SET_WALLPAPER); 
			// 启动系统选择应用 
			Intent intent = new Intent(Intent.ACTION_CHOOSER); 
			intent.putExtra(Intent.EXTRA_INTENT, chooseIntent); 
			intent.putExtra(Intent.EXTRA_TITLE, "选择壁纸"); 
			startActivity(intent); 
			 break;
		case R.id.widgetItem:
			int appWidgetId = appWidgetHost.allocateAppWidgetId();
			Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
			pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
			break;
		case R.id.shortCutItem:
			Intent shortCutPick = new Intent(Intent.ACTION_PICK_ACTIVITY);
			Intent extraIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
			shortCutPick.putExtra(Intent.EXTRA_INTENT, extraIntent);
			shortCutPick.putExtra(Intent.EXTRA_TITLE, "快捷方式");
			startActivityForResult(shortCutPick, REQUEST_PICK_SHORTCUT);
			break;
		case R.id.folderItem:
			CellLayout c = mWorkSpace.getCurrentScreenPutFolderIcon();
			if(c != null){
				final FolderInfo folderinfo = new FolderInfo(getApplicationContext());
				FolderIcon newFoler = FolderIcon.fromXml(R.layout.folder_icon, c, folderinfo,mFolder, this);
				mWorkSpace.addFolderIconIntoScreen(newFoler);
				AnimationUtil.loadAnimationByResourceId(this, R.anim.folder_gen_from_hotseat);
				AnimationUtil.startAnimation(newFoler);
			}
			break;
		case R.id.folder_dissolve_comfire:
			setFolderDissolveDisAppear();
			mWorkSpace.dissolveFolderComplete();
			break;
		case R.id.folder_dissolve_cancel:
			setFolderDissolveDisAppear();
			break;
		default:
			break;
		}
		
	}
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Xlog.logd("requestCode: " + requestCode);
		if(resultCode == RESULT_CANCELED) return;
		if(resultCode == RESULT_OK){
			switch (requestCode) {
			case REQUEST_PICK_APPWIDGET:
				addWidget(data);
				break;
			case REQUEST_CREATE_APPWIDGET:
				completeAddWidget(data);
				break;
			case REQUEST_PICK_SHORTCUT:
				addShortCut(data);
				break;
			case REQUEST_CREATE_SHORTCUT:
				completeShortCut(data);
				break;
			default:
				break;
			}
			
		}
		
	}
	
	private void addShortCut(Intent data){
		startActivityForResult(data, REQUEST_CREATE_SHORTCUT);
	}
	
	@SuppressWarnings("deprecation")
	private void completeShortCut(Intent data){
		final PackageManager mPackageManager = getPackageManager();
		Drawable shortcutIcon = null;  //快捷方式的图标 ， 可以有两种方式获取，如下 if else 判断        
        // 获得快捷方式Label  
        String label = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);         
        Parcelable bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);  
        // 直接了图片 ， 即设置了 EXTRA_SHORTCUT_ICON 参数值  
        if (bitmap != null && bitmap instanceof Bitmap)  
        {  
            shortcutIcon = new BitmapDrawable((Bitmap) bitmap);  
        }  
        else   //设置了EXTRA_SHORTCUT_ICON_RESOURCE 附加值  
        {  
            Parcelable iconParcel = data .getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);  
            if(iconParcel != null && iconParcel instanceof ShortcutIconResource)  
            {  
                // 获得ShortcutIconResource对象  
                ShortcutIconResource iconRes = (ShortcutIconResource) iconParcel;  
                //获得inconRes对象的Resource对象  
                try  
                {  
                    //获取对应packageName的Resources对象  
                    Resources resources = mPackageManager.getResourcesForApplication(iconRes.packageName);                  
                    //获取对应图片的id号  
                    int iconid = resources.getIdentifier(iconRes.resourceName, null, null);  
                    Xlog.logd("icon identifier is " + iconRes.resourceName) ;  
                    //获取资源图片  
                    shortcutIcon = resources.getDrawable(iconid);  
                }  
                catch (NameNotFoundException e)  
                {  
                	Xlog.loge("NameNotFoundException  at completeAddShortCut method") ;  
                }  
  
            }  
        }  
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        
        ShortCutInfo sc = new ShortCutInfo(label, intent, shortcutIcon);
        mWorkSpace.addShortCutViewInLayout(sc);
	}
	
	private void addWidget(Intent data){
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		Xlog.logd("addWidget appWidgetId: " + appWidgetId);
		AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		if(widgetInfo.configure != null){
			Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(widgetInfo.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
		}else{
			onActivityResult(REQUEST_CREATE_APPWIDGET, RESULT_OK, data);
		}
	}
	
	private void completeAddWidget(Intent data){
		int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		if(appWidgetId == -1){
			appWidgetHost.deleteAppWidgetId(appWidgetId);
			return ;
		}
		Xlog.logd("completeAddWidget appWidgetId: " + appWidgetId);
		AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		int mWidth = widgetInfo.minWidth;
		int mHeight = widgetInfo.minHeight;
		int[] spanxy = mWorkSpace.getAppWigetSpan(mWidth, mHeight);
		
		Xlog.logd("mwidth: " + mWidth + "  mheight: " + mHeight);
		Xlog.logd("spanX: " + spanxy[0] + "  spanY: " + spanxy[1]);
		LauncherAppWidgetInfo lAppWidgetInfo = new LauncherAppWidgetInfo(appWidgetId, widgetInfo.provider);
		lAppWidgetInfo.hostView = appWidgetHost.createView(this, appWidgetId, widgetInfo);
		lAppWidgetInfo.hostView.setAppWidget(appWidgetId, widgetInfo);
		lAppWidgetInfo.hostView.setTag(lAppWidgetInfo);
		lAppWidgetInfo.xSpan = spanxy[0];
		lAppWidgetInfo.ySpan = spanxy[1];
		mWorkSpace.addAppWidgetViewInLayout(lAppWidgetInfo);
		
	}
	
	public static class Settings{
		public static final int APPINFO_ITEM = 0;
		public static final int  SHORTCUT_ITEM= 1;
		public static final int  FOLDER_ITEM = 2;
		public static final int APPWIDGET_ITEM = 3;
	} 
	
	public interface FolderUpdateListener{
		public void onRemoveFolderDynamic(AppInfo app);
		
		public void onUpdateFolderDynamic(AppInfo app);
	}

}
