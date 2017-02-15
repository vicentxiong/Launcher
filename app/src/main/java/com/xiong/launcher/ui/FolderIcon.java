package com.xiong.launcher.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiong.launcher.Launcher;
import com.xiong.launcher.LauncherApp;
import com.xiong.launcher.R;
import com.xiong.launcher.Launcher.FolderUpdateListener;
import com.xiong.launcher.alg.AppInfo;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.FolderInfo;
import com.xiong.launcher.alg.ItemInfo;
import com.xiong.launcher.alg.LauncherAnimatorUtils;
import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.controller.AnimationUtil;
import com.xiong.launcher.ui.Folder.FolderIconFreshCallback;

public class FolderIcon extends LinearLayout implements FolderIconFreshCallback,FolderUpdateListener{
	private static final long FOLDER_ANIMATOR_DURATION = 100;
	private static final float  RING_GROW_FACEOR = 0.3f;
	public static final int FOLDER_COLUMN = 3;
	public static final int FOLDER_ROW = 3;
	
	public CellLayout mPreviewBackground;
	public TextView mName;
	public ViewGroup mParent;
	public FolderInfo mFolderinfo;
	private Folder mDetailFolder;
	private Launcher mLauncher;
	private boolean isShowDetail = false;
	private Animation expendAnim,collpseAnimation ;
	
	public FolderIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}

	public FolderIcon(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public FolderIcon(Context context) {
		this(context,null);
		
	}
	
	private Animation initFolderExpendAnimation(){
		if(expendAnim == null){
			expendAnim = AnimationUtils.loadAnimation(mLauncher, R.anim.folder_expend);
			expendAnim.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					Xlog.logd("animation start");
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					Xlog.logd("animation end");
					mDetailFolder.setVisibility(View.VISIBLE);
				}
			});
		}
		return expendAnim;
	}
	
	private Animation initFolderCollpseAnimation(){
		if(collpseAnimation == null){
			collpseAnimation = AnimationUtils.loadAnimation(mLauncher, R.anim.folder_collpse);
			collpseAnimation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					mDetailFolder.setVisibility(View.GONE);
				}
			});
		}
		return collpseAnimation;
	}

	public static FolderIcon fromXml(int resId,ViewGroup parent,FolderInfo folderinfo,Folder folder,Launcher launcher){
		LauncherApp app = (LauncherApp) launcher.getApplication();
		DeviceProfile dp = app.getDeviceProfile();
		FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(resId, parent, false);
		icon.mLauncher = launcher;
		icon.mPreviewBackground = (CellLayout) icon.findViewById(R.id.folder_preview_background);
		icon.mName = (TextView) icon.findViewById(R.id.folder_label);
		icon.mName.setText(folderinfo.folderName);
		icon.mPreviewBackground.setBackgroundResource(folderinfo.resid);
		icon.mPreviewBackground.setLayoutParams(new LayoutParams(dp.iconAppsize, dp.iconAppsize));
		icon.mPreviewBackground.initCellCount(FOLDER_COLUMN, FOLDER_ROW, CellLayout.CELL_FOLDER);
		icon.mParent = parent;
		icon.mFolderinfo = folderinfo;
		icon.mDetailFolder = folder;
		icon.mLauncher.addFolderUpdateListener(icon);
		icon.setTag(folderinfo);
		icon.setOnClickListener(launcher.getWorkSpace());
		icon.setOnLongClickListener(launcher.getWorkSpace());
		return icon;
	}
	
	public void showDetailFolder(){
		if(mDetailFolder != null){
			mDetailFolder.initData(mLauncher, mFolderinfo, this);
			mDetailFolder.clearAnimation();
			Xlog.logd("showDetailFolder");
			mDetailFolder.startAnimation(initFolderExpendAnimation());
			isShowDetail = true;
		}
	}
	
	public void dismissDetailFolder(){
		if(mDetailFolder != null)
			mDetailFolder.clearAnimation();
			mDetailFolder.startAnimation(initFolderCollpseAnimation());
			isShowDetail = false;
	}
	
	public void deleteFolderIteminfo(ItemInfo info){
		if(mFolderinfo.contains(info)){
			mFolderinfo.removeItemFormFolder(info);
		}
	}
	
	public void clearFolderItem(){
		mFolderinfo.childItems.clear();
	}
	
	public void updateFolderIconThumbnail(){
		mPreviewBackground.removeAllViews();
		mPreviewBackground.markAllCellsOcuppy(false);
		CellLayout.LayoutParams lp = null;
		int infoNum = Math.min(mFolderinfo.childItems.size(), FOLDER_COLUMN*FOLDER_ROW);
		
		for (int i = 0; i < infoNum; i++) {
			lp =mPreviewBackground.isOcuppyInCellLayoutAuto(1, 1);
			mPreviewBackground.markCellsOcuppy(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, true);
			mPreviewBackground.addView(mLauncher.getWorkSpace().createThumbnail(mPreviewBackground, mFolderinfo.childItems.get(i)), lp);
		}
	}

	public static class FolderRingAnimator{
		public int cellX,cellY;
		public int mPreSize;
		public float mRingSize;
		public static Drawable mSharedRingDrawabble;
		private CellLayout mCellLayout;
		private ValueAnimator mAcceptAnimator;
		private ValueAnimator mNeutralAnimator;
		private FolderIcon mFolderIcon;
		
		public FolderRingAnimator(Launcher launcher,FolderIcon folderIcon){
			Resources res = launcher.getResources();
			LauncherApp application = (LauncherApp) launcher.getApplication();
			DeviceProfile dp = application.getDeviceProfile();
			mPreSize = dp.folderPreSize;
			mSharedRingDrawabble = res.getDrawable(R.drawable.hs_folder);
			mFolderIcon = folderIcon;
		}
		
		public void animateToAcceptState(){
			mAcceptAnimator = LauncherAnimatorUtils.ofFloat(mCellLayout, 0.0f,1.0f);
			mAcceptAnimator.setDuration(FOLDER_ANIMATOR_DURATION);
			final int previewSize = mPreSize;
			mAcceptAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float percent = (Float) animation.getAnimatedValue();
					mRingSize = (1+percent*RING_GROW_FACEOR)*previewSize;
					Xlog.logd("into  ring size: " + mRingSize + "  precent:" + percent + "   previewsieze: " + previewSize);
					if(mCellLayout!=null)
						mCellLayout.invalidate();
				}
			});
			mAcceptAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					if(mFolderIcon != null){
						mFolderIcon.mPreviewBackground.setVisibility(View.INVISIBLE);
					}
				}
			});
			mAcceptAnimator.start();
		}
		
		public void animateToNeutralState(){
			mNeutralAnimator = LauncherAnimatorUtils.ofFloat(mCellLayout, 0.0f,1.0f);
			mNeutralAnimator.setDuration(FOLDER_ANIMATOR_DURATION);
			final int previewSize = mPreSize;
			mNeutralAnimator.addUpdateListener(new AnimatorUpdateListener() {
				
				@Override
				public void onAnimationUpdate(ValueAnimator animation) {
					final float percent = (Float) animation.getAnimatedValue();
					mRingSize = (1+(1-percent)*RING_GROW_FACEOR)*previewSize;
					Xlog.logd("into  ring size: " + mRingSize + "  precent:" + percent + "   previewsieze: " + previewSize);
					if(mCellLayout!=null)
						mCellLayout.invalidate();
				}
			});
			mNeutralAnimator.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					if(mCellLayout != null)
						mCellLayout.hideFolderAccept(FolderRingAnimator.this);
					
					if(mFolderIcon != null){
						mFolderIcon.mPreviewBackground.setVisibility(View.VISIBLE);
					}
				}
			});
			mNeutralAnimator.start();
		}
		
		public void setCellLayout(CellLayout   cell){
			mCellLayout = cell;
		}
		
		public void setCell(int cx,int cy){
			cellX = cx;
			cellY = cy;
		}
	}

	@Override
	public void updateFolderIcon() {
		
		mName.setText(mFolderinfo.folderName);
	}

	@Override
	public void onRemoveFolderDynamic(AppInfo app) {
		String pkName = app.getComponentName().getPackageName();
		
		for (int i = 0; i < mFolderinfo.childItems.size(); i++) {
			ItemInfo info = mFolderinfo.childItems.get(i);
			if(info.itemType == Launcher.Settings.APPINFO_ITEM){
				AppInfo appinfo = (AppInfo) info;
				if(appinfo.getComponentName().getPackageName().equals(pkName)){
					mFolderinfo.removeItemFormFolder(info);
				}
			}
		}
		updateFolderIconThumbnail();
		if(isShowDetail && mDetailFolder != null){
			mDetailFolder.notifyUpdateFolder();
		}
	}

	@Override
	public void onUpdateFolderDynamic(AppInfo app) {
		updateFolderIconThumbnail();
		if(isShowDetail && mDetailFolder != null){
			mDetailFolder.notifyUpdateFolder();
		}
	}
}
