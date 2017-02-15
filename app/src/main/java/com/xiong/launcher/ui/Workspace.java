package com.xiong.launcher.ui;

import java.util.ArrayList;
import java.util.Iterator;

import com.xiong.launcher.Launcher;
import com.xiong.launcher.LauncherApp;
import com.xiong.launcher.R;
import com.xiong.launcher.alg.AppInfo;
import com.xiong.launcher.alg.Deque;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.FolderInfo;
import com.xiong.launcher.alg.ItemInfo;
import com.xiong.launcher.alg.LauncherAppWidgetInfo;
import com.xiong.launcher.alg.ShortCutInfo;
import com.xiong.launcher.alg.Utils;
import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.controller.AnimationUtil;
import com.xiong.launcher.controller.DragItemLister;
import com.xiong.launcher.controller.LauncherAppWidgetHost;
import com.xiong.launcher.ui.AnimationSelectDialog.AnimationTypeSharedPerfences;
import com.xiong.launcher.ui.CellLayout.LayoutParams;
import com.xiong.launcher.ui.BaseTextView.PressedCallBack;
import com.xiong.launcher.ui.Folder.FolderIconFreshCallback;
import com.xiong.launcher.ui.FolderIcon.FolderRingAnimator;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;

public class Workspace extends ViewGroup implements DragItemLister, PressedCallBack,
                                                                     OnClickListener, OnLongClickListener, OnTouchListener{
	private Context mContext;
	private int mChildWidth, mChildHeight;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mTouchSlop;
	private static final int DEFAULTSCREEN = 1;

	private static final int TOUCH_STATE_REST = 0;
	private static final int TOUCH_STATE_SCROLLING = 1;
	private int mTouchState = TOUCH_STATE_REST;

	private float mLastMotoinX = 0;
	private int mCurrentScreen = DEFAULTSCREEN;
	public static final int NORMAL= 0;
	public static final int EDITING = 1;
	private int mode = NORMAL;
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	private int sX ;
	
	private DividingLister mDividingLister;
	private CellLayout currentLayout;
	private CellLayout startLayout;
	private WindowManager mWinMgr ;
	private WindowManager.LayoutParams mWindowLayoutParams;
	private CellLayout.LayoutParams startLayoutParams;
	private View mStartDragItem ;
	private ImageView mDragImageView;
	private Bitmap mBitmap;
	private WallpaperManager mWallPaperMgr;
	private ArrayList<AppInfo> appList ;
	private Deque<View> mCellLayout = new Deque<View>();
	private Launcher mLauncher;
	private LayoutInflater mInflater;
	private NewScreenButton leftButton;
	private NewScreenButton rightButton;
	private int mDownX = 0;
	private int mDownY = 0;
	private boolean isDragInHotseat = false;
	
	private Camera mCamera = new Camera();
	private Matrix mMatrix = new Matrix();
	private static final int rotateDegree = 30;
	private BaseTextView pressedIcon;
	private FolderRingAnimator mDragFolderRingAnimator = null;
	private boolean mFolderDragView = false;
	private int mFolderDragLeft;
	private int mFolderDragTop;
	private View mFolderDragImage;
	private FolderIcon foldericon;
	
	public Workspace(Context context) {
		this(context,null);
		
	}
	
	public Workspace(Context context,AttributeSet attrs) {
		this(context,attrs,0);
		
	}

	public Workspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mLauncher = (Launcher) mContext;
		init();
	}
	
	private void init() {
		mScroller = new Scroller(mContext);
		mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
		mWallPaperMgr = (WallpaperManager) mContext.getSystemService(Context.WALLPAPER_SERVICE);
		mWinMgr = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		mInflater = LayoutInflater.from(mContext);
		leftButton = new NewScreenButton(mContext);
		leftButton.setWorkSpace(this,LEFT);
		rightButton = new NewScreenButton(mContext);
		rightButton.setWorkSpace(this,RIGHT);
	}
	
	private void updateWallpaperOffset() {
     	 int scrollRange = getChildAt(getChildCount() -1).getRight() - getWidth();
		 IBinder token = getWindowToken();
		 if(token !=null) {
			 mWallPaperMgr.setWallpaperOffsetSteps(1.0f / (getChildCount() ),0);
			 mWallPaperMgr.setWallpaperOffsets(getWindowToken(),
					 Math.max(0.f, Math.min(getScrollX()/(float)scrollRange,1.f)),0);
		}
	}
	
	private void createDragImage(Bitmap bitmap, int downX , int downY){
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		int paddingW = (int)(w*0.1);
		int paddingH = (int)(h*0.1);
		Bitmap b = Bitmap.createScaledBitmap(bitmap, w+paddingW, h+paddingH, true);
		mWindowLayoutParams = new WindowManager.LayoutParams();
		mWindowLayoutParams.format = PixelFormat.TRANSLUCENT; //图片之外的其他地方透明
		//mWindowLayoutParams.windowAnimations = android.R.style.Animation_Dialog;
		mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
		mWindowLayoutParams.x = downX-paddingW/2;
		mWindowLayoutParams.y = downY-paddingH/2;
		mWindowLayoutParams.alpha = 1f; //透明度
		mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;  
		mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  |
				                                                 WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE  |
				                                                 WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS ;
		mWindowLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		mDragImageView = new ImageView(getContext());  
		mDragImageView.setImageBitmap(b);  
		mWinMgr.addView(mDragImageView, mWindowLayoutParams);  
	}
	
	private void buildDragItemImage(View v,int downx,int downy){
		mStartDragItem = v;
		if(mStartDragItem!=null){
			
			
			if(mFolderDragView){
				mFolderDragImage.setDrawingCacheEnabled(true);
				mBitmap = Bitmap.createBitmap(mFolderDragImage.getDrawingCache());
				mFolderDragImage.destroyDrawingCache();
			}else{
				mStartDragItem.setDrawingCacheEnabled(true);
				mBitmap = Bitmap.createBitmap(mStartDragItem.getDrawingCache());
				mStartDragItem.destroyDrawingCache();
			}
			if(mStartDragItem instanceof TextView)
				lockIconDrawable();
			else 
				mStartDragItem.setAlpha(0.4f);

			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
			mDownX = downx;
			mDownY = downy;
			int x = lp.x;
			int y = lp.y;
			
			if(mFolderDragView){
				x = mFolderDragLeft;
				y = mFolderDragTop;
			}
			Xlog.logd("x : " + x + "  y : " + y + " bitmap width : " + mBitmap.getWidth() + "  height : " + mBitmap.getHeight());
			if(mLauncher.getHotSeat().isHotSeatRect(downx, downy)){
				y = y+mLauncher.getHotSeat().getTop();
				Xlog.logd("is hotseat rect");
			}
			createDragImage(mBitmap, x, y);
		}
	}
	
	private void startDrag(int moveX,int moveY){
		int x = moveX;
		int y = moveY;
		
		int offsetX = moveX - mDownX;
		int offsetY = moveY - mDownY;
		mDownX = moveX;
		mDownY = moveY;
		int beforeX = mWindowLayoutParams.x;
		int beforeY = mWindowLayoutParams.y;
		mWindowLayoutParams.x = beforeX+offsetX;
		mWindowLayoutParams.y = beforeY + offsetY;
		mWinMgr.updateViewLayout(mDragImageView, mWindowLayoutParams);
		
		CellLayout.LayoutParams startViewLp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
		if(onDragingViewType(mStartDragItem) == Launcher.Settings.APPINFO_ITEM){
			if(mLauncher.getHotSeat().isHotSeatRect(moveX, moveY)){
				if(!isDragInHotseat){
					currentLayout.removeView(mStartDragItem);
					currentLayout = mLauncher.getHotSeat().getLayout();
					currentLayout.addView(mStartDragItem, new CellLayout.LayoutParams(-100, -100,startViewLp.cellHSpan,startViewLp.cellVSpan));
					isDragInHotseat = true;
				}
				y = y-mLauncher.getHotSeat().getTop();
				
			}else if(!mLauncher.getHotSeat().isHotSeatRect(moveX, moveY)&&isDragInHotseat){
				currentLayout.removeView(mStartDragItem);
				View v = mCellLayout.get(mCurrentScreen);
				if(v instanceof CellLayout){
					currentLayout = (CellLayout) v;
				}
				currentLayout.addView(mStartDragItem, new CellLayout.LayoutParams(-100, -100,startViewLp.cellHSpan,startViewLp.cellVSpan));
				isDragInHotseat = false;
			}
		} 
		do {
			int type = onDragingViewType(mStartDragItem);
			if(type == Launcher.Settings.APPINFO_ITEM || type == Launcher.Settings.SHORTCUT_ITEM){
					WindowManager.LayoutParams mDragViewLp = (WindowManager.LayoutParams) mDragImageView.getLayoutParams();
					View target = currentLayout.isCanCreateFolder(mDragViewLp.x+mDragImageView.getWidth()/2, mDragViewLp.y+mDragImageView.getHeight()/2);
					if(target != null && onDragingViewType(target) != Launcher.Settings.APPWIDGET_ITEM){
						mStartDragItem.setVisibility(View.INVISIBLE);
						break;
					}
			}
			
			CellLayout.LayoutParams lp = currentLayout.isOcuppyInCellLayout(x, y, startViewLp.cellHSpan, startViewLp.cellVSpan, true);
			if(lp != null){
				mStartDragItem.setLayoutParams(lp);
			}
		} while (false);
	}
	
	private void stopDrag(int upX,int upY){
		int x = upX;
		int y = upY;
		
		do {
			int type = onDragingViewType(mStartDragItem);
			if(type == Launcher.Settings.APPINFO_ITEM || type == Launcher.Settings.SHORTCUT_ITEM){
					WindowManager.LayoutParams mDragViewLp = (WindowManager.LayoutParams) mDragImageView.getLayoutParams();
					View target = currentLayout.isCanCreateFolder(mDragViewLp.x+mDragImageView.getWidth()/2, mDragViewLp.y+mDragImageView.getHeight()/2);
					int targetType;
					if(target != null && (targetType = onDragingViewType(target)) != Launcher.Settings.APPWIDGET_ITEM){
						Xlog.logd("targetType == " + targetType);
						currentLayout.exitFolder();
						if(!mFolderDragView){
							CellLayout.LayoutParams origLp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
							currentLayout.markCellsOcuppy(origLp.cellX, origLp.cellY, origLp.cellHSpan, origLp.cellVSpan,false);
						}
						currentLayout.removeView(mStartDragItem);
						if(targetType == Launcher.Settings.FOLDER_ITEM){
							FolderIcon icon = (FolderIcon) target;
							ItemInfo info = (ItemInfo) mStartDragItem.getTag();
							if(mFolderDragView){
								if(icon.mFolderinfo.childItems.contains(info)){
									break;
								}else{
									foldericon.deleteFolderIteminfo(info);
									foldericon.updateFolderIconThumbnail();
								}
							}
							CellLayout folderLayout = icon.mPreviewBackground;
							CellLayout.LayoutParams p = folderLayout.isOcuppyInCellLayoutAuto(1, 1);
							if(p != null){
								folderLayout.addView(createThumbnail(folderLayout, (ItemInfo)mStartDragItem.getTag()), p);
								folderLayout.markCellsOcuppy(p.cellX, p.cellY, p.cellHSpan, p.cellVSpan, true);
							}
							addInfoToFolderInfo(icon, mStartDragItem);
						}else if(targetType == Launcher.Settings.APPINFO_ITEM || targetType == Launcher.Settings.SHORTCUT_ITEM){
							final FolderInfo info = new FolderInfo(mLauncher);
							FolderIcon icon = FolderIcon.fromXml(R.layout.folder_icon, currentLayout, info,mLauncher.getFolder(), mLauncher);
							CellLayout.LayoutParams clp = (CellLayout.LayoutParams) target.getLayoutParams();
							currentLayout.addView(icon, clp);
							currentLayout.removeView(target);
							CellLayout folderLayout = icon.mPreviewBackground;
							CellLayout.LayoutParams p = folderLayout.isOcuppyInCellLayoutAuto(1, 1);
							if(p != null){
								folderLayout.addView(createThumbnail(folderLayout, (ItemInfo)mStartDragItem.getTag()), p);
								folderLayout.markCellsOcuppy(p.cellX, p.cellY, p.cellHSpan, p.cellVSpan, true);
								addInfoToFolderInfo(icon, mStartDragItem);
								ItemInfo draginfo = (ItemInfo) mStartDragItem.getTag();
								if(mFolderDragView){
									foldericon.deleteFolderIteminfo(draginfo);
									foldericon.updateFolderIconThumbnail();
								}
							}
							p = folderLayout.isOcuppyInCellLayoutAuto(1, 1); 
							if(p != null){
								folderLayout.addView(createThumbnail(folderLayout, (ItemInfo)target.getTag()), p);
								folderLayout.markCellsOcuppy(p.cellX, p.cellY, p.cellHSpan, p.cellVSpan, true);
								addInfoToFolderInfo(icon, target);
							}
						}
						break;
					}
			}
			
			if(mFolderDragView && mLauncher.atDeleteZone()){
				currentLayout.removeView(mStartDragItem);
				if(foldericon != null){
					foldericon.showDetailFolder();
					break;
				}
			}
			
			if(mLauncher.getHotSeat().isHotSeatRect(upX, upY)){
				if(onDragingViewType(mStartDragItem)!=Launcher.Settings.APPINFO_ITEM){
					currentLayout.removeView(mStartDragItem);
					startLayout.addView(mStartDragItem, startLayoutParams);
					startLayout.markCellsOcuppy(startLayoutParams.cellX, startLayoutParams.cellY, startLayoutParams.cellHSpan, startLayoutParams.cellVSpan, true);
					if(mStartDragItem instanceof TextView)resetIconDrawableState();
					else mStartDragItem.setAlpha(1.0f);
					if(mFolderDragView && foldericon!=null){
						foldericon.showDetailFolder();
						Toast.makeText(mLauncher, Utils.getStringFormResById(mLauncher, R.string.folder_nospace_tip), 1000).show();
					}
					currentLayout =null;
					return ;
				}
				y = y-mLauncher.getHotSeat().getTop();
				Xlog.logd("is hotseat rect");
			}
			CellLayout.LayoutParams startViewLp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
			LayoutParams lp  = currentLayout.isOcuppyInCellLayout(x, y,startViewLp.cellHSpan,startViewLp.cellVSpan,false);
			if(lp != null){
				mStartDragItem.setLayoutParams(lp);
				if(mFolderDragView){
					ItemInfo info = (ItemInfo)mStartDragItem.getTag();
					foldericon.deleteFolderIteminfo(info);
					foldericon.updateFolderIconThumbnail();
				}
			}else{
				Xlog.logd("lp ====== null");
				currentLayout.removeView(mStartDragItem);
				if(mFolderDragView && foldericon!=null){
					foldericon.showDetailFolder();
					Toast.makeText(mLauncher, Utils.getStringFormResById(mLauncher, R.string.folder_nospace_tip), 1000).show();
				}else{
					startLayout.addView(mStartDragItem, startLayoutParams);
					startLayout.markCellsOcuppy(startLayoutParams.cellX, startLayoutParams.cellY, startLayoutParams.cellHSpan, startLayoutParams.cellVSpan, true);
				}
			}
		} while (false);
        
		resetScreenStateIfEmpty(currentLayout);
		resetScreenStateIfEmpty(startLayout);
		if(mStartDragItem instanceof TextView)
			resetIconDrawableState();
		else 
			mStartDragItem.setAlpha(1.0f);

		currentLayout =null;
		mFolderDragView = false;
		mFolderDragImage = null;
		startLayout = null;
		startLayoutParams = null;
		removeDragImage();
	}
	
	private void addInfoToFolderInfo(FolderIcon foldericon,View other){
		FolderInfo folderinfo = (FolderInfo) foldericon.getTag();
		ItemInfo info = (ItemInfo) other.getTag();
		if(folderinfo != null && info != null){
			folderinfo.addItemIntoFolder(info);
		}
	}
	
	@SuppressLint("NewApi")
	public View createThumbnail(ViewGroup folder,ItemInfo  info){
		LauncherApp app = (LauncherApp) mLauncher.getApplication();
		DeviceProfile dp = app.getDeviceProfile();
		final TextView v = (TextView) mInflater.inflate(R.layout.folder_thumbnail_view, folder, false);
		v.setTag(info);
		int type = info.itemType;
		Bitmap thumbnail = null;
        if(type == Launcher.Settings.SHORTCUT_ITEM){
        	ShortCutInfo sinfo = (ShortCutInfo) info;
        	thumbnail = Utils.createIconBitmap(sinfo.drawable, (int)(dp.iconAppsize*0.25), (int)(dp.iconAppsize*0.25), mLauncher);
        }else if(type == Launcher.Settings.APPINFO_ITEM){
        	AppInfo ainfo = (AppInfo) info;
        	thumbnail = Utils.createIconBitmap(ainfo.bitmap,  (int)(dp.iconAppsize*0.25), (int)(dp.iconAppsize*0.25), mLauncher);
        }
		v.setBackground(Utils.createIconDrawable(thumbnail,thumbnail.getWidth(),thumbnail.getHeight()));
		return v;
	}
	
	public void onDragInFolder(View target){
		if(target !=null){
			Xlog.logd("dragview to in folder");
			FolderIcon icon = null;
			ItemInfo info = (ItemInfo) target.getTag();
			if(info.itemType == Launcher.Settings.FOLDER_ITEM){
				icon = (FolderIcon) target;
			}
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) target.getLayoutParams();
			mDragFolderRingAnimator = new FolderRingAnimator(mLauncher,icon);
			mDragFolderRingAnimator.setCellLayout(currentLayout);
			mDragFolderRingAnimator.setCell(lp.cellX,lp.cellY);
			mDragFolderRingAnimator.animateToAcceptState();
			currentLayout.showFolderAccept(mDragFolderRingAnimator);
		}

	}
	
	public void onDragOutFolder(){
		Xlog.logd("dragview to out folder");
		mStartDragItem.setVisibility(View.VISIBLE);
		if(mDragFolderRingAnimator != null){
			mDragFolderRingAnimator.animateToNeutralState();
			mDragFolderRingAnimator = null;
		}
	}
	
	private int onDragingViewType(View view){
		int type = -1;
		if(view != null){
			ItemInfo itemInfo = (ItemInfo) view.getTag();
			type = itemInfo.itemType;
		}
		
		return type;
	}
	
	private void resetScreenStateIfEmpty(CellLayout cur){
		final CellLayout layout = cur;
		if(layout ==null || layout.getType() == CellLayout.CELL_HOTSEAT){
			return;
		}
		if(layout.getChildCount() == 0 && !layout.isScreenIsEmpty() ){
			layout.addViewInto(inflateViewFromResource(R.layout.delete_screen_view, layout),this);
			if(mode == EDITING){
				layout.setDelBtnVisibility(View.VISIBLE);
			}else{
				layout.setDelBtnVisibility(View.INVISIBLE);
			}
		}
		if(layout.isScreenIsEmpty()&&layout.getChildCount()>1){
			if(mode == EDITING){
				layout.setDelBtnVisibility(View.GONE);
			}
			layout.removeViewFrom();
		}
	}
	
	private void removeDragImage(){
		if(mDragImageView != null){
			mDragImageView.setImageBitmap(null);
			mWinMgr.removeView(mDragImageView);
			mDragImageView = null;
			mBitmap.recycle();
			mBitmap = null;
		}
	}
	
	public int getMode(){
		return mode;
	}
	
	public void setDividingLister(DividingLister lister){
		mDividingLister = lister;
	}
	

	
	@SuppressLint("NewApi")
	private void onScaleFotEditting(View v,float scale){
		ObjectAnimator sx_anim = ObjectAnimator.ofFloat(v, "ScaleX", scale).setDuration(1000);
		ObjectAnimator sy_anim = ObjectAnimator.ofFloat(v, "ScaleY", scale).setDuration(1000);
		sx_anim.setInterpolator(new OvershootInterpolator());
		sx_anim.start();
		sy_anim.setInterpolator(new OvershootInterpolator());
		sy_anim.start();
		if(scale < 1){
			v.setPivotX(mChildWidth*0.5f);
			v.setPivotY(mChildHeight*0.3f);
		}

	}
	
	public void editingCellLayout(){
		if(mTouchState == TOUCH_STATE_SCROLLING){
			return;
		}
		
		mode = EDITING;
		mCurrentScreen++;
		Iterator<View> it = mCellLayout.iterator();
		while(it.hasNext()){
			CellLayout layout = (CellLayout) it.next();
			final int count = layout.getChildCount();
			final boolean empty = layout.isScreenIsEmpty();
			if(empty && count < 1){
				layout.addViewInto(inflateViewFromResource(R.layout.delete_screen_view, layout),this);
			}else if(empty){
				layout.setDelBtnVisibility(View.VISIBLE);
			}
			layout.setBackgroundResource(R.drawable.editing_screen_bg);
			onScaleFotEditting(layout, 0.8f);
		}
		addNewScreenButton();
		mDividingLister.updateDividing(getChildCount(), mCurrentScreen);
		setSystemUiVisibility(View.INVISIBLE);
		
		mLauncher.getHotSeat().resetVisibityState(mode);
	}
	
	public void finishEditCellLayout(){
		if(mTouchState == TOUCH_STATE_SCROLLING){
			return;
		}
		mode = NORMAL;
		if(mCurrentScreen == 0) mCurrentScreen = 0;
		else if(mCurrentScreen == (getChildCount()-1)) mCurrentScreen = mCurrentScreen - 2;
		else mCurrentScreen--;
		deleteNewScreenButton();
		Iterator<View> it = mCellLayout.iterator();
		while(it.hasNext()){
			CellLayout layout = (CellLayout) it.next();
			final boolean empty = layout.isScreenIsEmpty();
			if(empty){
				layout.setDelBtnVisibility(View.GONE);
			}
			layout.setBackgroundResource(0);
			onScaleFotEditting(layout,1.0f);
		}
		mDividingLister.updateDividing(getChildCount(), mCurrentScreen);
		setSystemUiVisibility(View.VISIBLE);
		
		mLauncher.getHotSeat().resetVisibityState(mode);
	}
	
	private void addNewScreenButton(){
		mCellLayout.addFist(leftButton);
		mCellLayout.addLast(rightButton);
		addView(leftButton, 0);
		addView(rightButton, getChildCount());
		onScaleFotEditting(leftButton, 0.8f);
		onScaleFotEditting(rightButton, 0.8f);
	}
	
	private void deleteNewScreenButton(){
		mCellLayout.removeFist();
		mCellLayout.removeLast();
		removeViewAt(0);
		removeViewAt(getChildCount()-1);
		onScaleFotEditting(leftButton, 1.0f);
		onScaleFotEditting(rightButton, 1.0f);
	}
	
	public void deleteScreen(CellLayout delScreen){
		mCellLayout.remove(delScreen);
		removeView(delScreen);
		mCurrentScreen--;
		mDividingLister.updateDividing(getChildCount(), mCurrentScreen);
	}
	
	public void addScreenOnleft(){
		CellLayout newScreen = new CellLayout(mContext);
		newScreen.initCellCount(DeviceProfile.COLUMN, DeviceProfile.ROW,CellLayout.CELL_SCREEN);
		newScreen.setBackgroundResource(R.drawable.editing_screen_bg);
		newScreen.addViewInto(inflateViewFromResource(R.layout.delete_screen_view, newScreen),this);
		mCellLayout.add(1, newScreen);
		mCurrentScreen = 1;
		addView(newScreen, 1);
		onScaleFotEditting(newScreen, 0.8f);
		mDividingLister.updateDividing(getChildCount(), mCurrentScreen);
	}
	
	public void addScreenOnRight(){
		CellLayout newScreen = new CellLayout(mContext);
		newScreen.initCellCount(DeviceProfile.COLUMN, DeviceProfile.ROW,CellLayout.CELL_SCREEN);
		newScreen.setBackgroundResource(R.drawable.editing_screen_bg);
		newScreen.addViewInto(inflateViewFromResource(R.layout.delete_screen_view, newScreen),this);
		mCellLayout.add(getChildCount()-1, newScreen);
		mCurrentScreen=getChildCount()-1;
		addView(newScreen, getChildCount()-1);
		onScaleFotEditting(newScreen, 0.8f);
		mDividingLister.updateDividing(getChildCount(), mCurrentScreen);
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);

		int childcount = getChildCount();
		mChildWidth = width - getPaddingLeft();
		mChildHeight = height-getPaddingTop();
		
		for (int i = 0; i < childcount; i++) {
			View child = getChildAt(i);
			child.measure(mChildWidth, mChildHeight);
		}
	}
	
	@Override
	protected void onLayout(boolean c, int l, int t, int r, int b) {
		//Xlog.logd("workspace l " + l + " t " + t + " r " + r + " b " + b);
		
		if(mode == EDITING){
			 sX = (int)((r-l)*0.2f) ;
		}else{
			sX= 0;
		}
		int margnLeft = getPaddingLeft();
		int margnTop = getPaddingTop();
		int childcount = getChildCount();
		for (int i = 0; i < childcount; i++) {
			View child = getChildAt(i);
			child.layout(margnLeft, margnTop, margnLeft + mChildWidth, margnTop + mChildHeight);
			margnLeft += (mChildWidth-sX);
		}
		scrollTo(mCurrentScreen*(mChildWidth-sX), 0);
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			invalidate();
		}
		if(getChildCount()>0)
		   updateWallpaperOffset();

	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
	}
	
	@SuppressLint("NewApi")
	private void rotationScreenOver(){
		for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v != null) {
                // float scrollProgress = getScrollProgress(screenCenter, v, i);
            	int derction = detaX > 0 ? 1 : -1;
                 float rotation = -24f  * detaX/4;
                 v.setRotation(rotation);         
                 v.setCameraDistance(3*1080);
                 v.setPivotX(v.getMeasuredWidth()*0.5f); //rotation > 0 ?v.getMeasuredWidth():0
                 v.setPivotY(v.getMeasuredHeight());
             }
        }
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		if(mode == EDITING){
			setChildAlpha();
			super.dispatchDraw(canvas);
			return;
		}
		AnimationTypeSharedPerfences sp = new AnimationTypeSharedPerfences(mContext);
		int type = sp.getValue();
		if(type != AnimationSelectDialog.FADE){
			setChildAlpha();
		}
		switch (type) {
		case AnimationSelectDialog.CLASSIC:
			super.dispatchDraw(canvas);
			break;
		case AnimationSelectDialog.FADE:
			childFadeInOut(canvas); 
			break;
		case AnimationSelectDialog.JUMP:
			childCanvasTranlate(canvas);
			break;
		case AnimationSelectDialog.ROTATE:
			childCanvasRotate(canvas);  
			break;
		case AnimationSelectDialog.SCALE:
			childCanvasScale(canvas);
			break;
		default:
			break;
		}
		
	}
	
	private void setChildAlpha(){
		int N = getChildCount();
		for (int i = 0; i < N; i++) {
			View child = getChildAt(i);
			child.setAlpha(1.0f);
		}
	}
	
	/*
	 * 滑动压缩效果
	 */
	private void childCanvasScale(Canvas canvas) {
		int count = getChildCount();
		int width = getWidth();
		int scrollX = getScrollX();

		for (int i = 0; i < count; i++) {
			canvas.save();
			View view = getChildAt(i);
			int childLeft = view.getLeft();
			int dx = Math.abs(scrollX - childLeft);
			if (dx <= width) {
				canvas.scale(1.0f, 1.0f - (float) 0.5 * dx / width);
				canvas.translate(0.0f, (0.5f * dx / width) * getHeight());
				drawChild(canvas, view, getDrawingTime());
				canvas.restore();
			} else {
				canvas.scale(1.0f, 0.5f);
				canvas.translate(0.0f, 0.5f * getHeight());
				drawChild(canvas, view, getDrawingTime());
				canvas.restore();
			}
		}
	}
	
	/*
	 * 滑动弹跳效果 
	 */
	private void childCanvasTranlate(Canvas canvas) {
		int count = getChildCount();
		int width = getWidth();
		int scrollX = getScrollX();

		for (int i = 0; i < count; i++) {
			canvas.save();
			View view = getChildAt(i);
			int childLeft = view.getLeft();
			int dx = Math.abs(scrollX - childLeft);
			if (dx <= width) {
				canvas.translate(0.0f, -(1.0f * dx / width) * getHeight());
				drawChild(canvas, view, getDrawingTime());
				canvas.restore();
			} else {
				canvas.translate(0.0f, -1.0f * getHeight());
				drawChild(canvas, view, getDrawingTime());
				canvas.restore();
			}
		}
	}
	/*
	 * 滑动转盘效果  
	 */
	private void childCanvasRotate(Canvas canvas) {
		//int N = canvas.save();
		int count = getChildCount();
		int width = getWidth();
		int height = getHeight();
		int scrollX = getScrollX();
		
		if(mCurrentScreen-1>=0){
			canvas.save();
			View leftChild = getChildAt(mCurrentScreen-1);
			if(leftChild != null){
				int childRight = leftChild.getRight();
				int childLeft = leftChild.getLeft();
				int dx = scrollX - childLeft;
				canvas.rotate(-(1.0f *dx/width)*rotateDegree ,childRight,height);
				drawChild(canvas, leftChild, getDrawingTime());
			}
			canvas.restore();
		}
		if(mCurrentScreen>=0){
			canvas.save();
			View currentChild = getChildAt(mCurrentScreen);
			if(currentChild != null){
				int childLeft = currentChild.getLeft();
				int dx = scrollX - childLeft;
				canvas.rotate(-(1.0f *dx/width)*rotateDegree ,scrollX+ width/2,height);
				drawChild(canvas, currentChild, getDrawingTime());
			}
			canvas.restore();
		}
		if(mCurrentScreen+1<=getChildCount()-1){
			canvas.save();
			View rightChild = getChildAt(mCurrentScreen+1);
			if(rightChild != null){
				int childLeft = rightChild.getLeft();
				int dx = scrollX - childLeft;
				canvas.rotate(-(1.0f *dx/width)*rotateDegree ,childLeft,height);
				drawChild(canvas, rightChild, getDrawingTime());
			}
			canvas.restore();
		}
	}
	
	/*
	 * 滑动淡进淡出
	 */
	private void childFadeInOut(Canvas canvas) {
		//int N = canvas.save();
		int count = getChildCount();
		int width = getWidth();
		int height = getHeight();
		int scrollX = getScrollX();
		
		if(mCurrentScreen-1>=0){
			View leftChild = getChildAt(mCurrentScreen-1);
			if(leftChild != null){
				int childLeft = leftChild.getLeft();
				int dx = Math.abs(scrollX - childLeft);
				leftChild.setAlpha(1.0f-1.0f *dx/width);
			}
		}
		if(mCurrentScreen>=0){
			View currentChild = getChildAt(mCurrentScreen);
			if(currentChild != null){
				int childLeft = currentChild.getLeft();
				int dx = Math.abs(scrollX - childLeft);
				currentChild.setAlpha(1.0f-1.0f *dx/width);
			}
		}
		if(mCurrentScreen+1<=getChildCount()-1){
			View rightChild = getChildAt(mCurrentScreen+1);
			if(rightChild != null){
				int childLeft = rightChild.getLeft();
				int dx = Math.abs(scrollX - childLeft);
				rightChild.setAlpha(1.0f-1.0f *dx/width);
			}
		}
		super.dispatchDraw(canvas);
	}

	/*
	 * 滑动3D
	 */
	private void childCanvas3D(Canvas canvas) {
		//int N = canvas.save();
		int count = getChildCount();
		int width = getWidth();
		int scrollX = getScrollX();

		for (int i = 0; i < count; i++) {
			canvas.save();		
			View view = getChildAt(i);
			int childLeft = view.getLeft();
			int dx = scrollX - childLeft;
			Xlog.logd("dx=====" + Math.abs(dx));
			if (Math.abs(dx) <= width) {
				mCamera.save();
				//mCamera.translate(-dx,0, 0);
	            mCamera.rotateZ((-1.0f *dx/width)*90);
	            mCamera.getMatrix(mMatrix);
	            mCamera.restore();
	            mMatrix.preTranslate(-width, -(getHeight()+75)/2);
	            mMatrix.postTranslate(width,(getHeight()+75)/2);
	            canvas.concat(mMatrix);
	            //canvas.setMatrix(mMatrix);
				drawChild(canvas, view, getDrawingTime());
				canvas.restore();
			} else {
				
				drawChild(canvas, view, getDrawingTime());
				canvas.restore();
			}
		}
		//canvas.restoreToCount(N);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(Xlog.DISPATCH_TOUCH_DEBUG)
			Xlog.logd("workspace dispatchTouchEvent");
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(Xlog.DISPATCH_TOUCH_DEBUG)
			Xlog.logd("workspace onInterceptTouchEvent");
		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotoinX = x;

			mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
					: TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_MOVE:

			final int xDiff = (int) Math.abs(mLastMotoinX - x);

			if (xDiff > 3*mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_UP:
			mTouchState = TOUCH_STATE_REST;
			break;

		}
		return mTouchState == TOUCH_STATE_SCROLLING && !mLauncher.isDragging();
	}
	
	int detaX;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(Xlog.DISPATCH_TOUCH_DEBUG)
			Xlog.logd("workspace onTouchEvent");
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
        
		final float x = event.getX();
		final float y = event.getY();
		final int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			
			if (mScroller != null) {
				if (!mScroller.isFinished()) {
					mScroller.abortAnimation();
				}
			}
			mLastMotoinX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(mLastMotoinX - x);

			if (xDiff > 3*mTouchSlop) {
				mTouchState = TOUCH_STATE_SCROLLING;
			}
			detaX = (int) (mLastMotoinX - x);
			if (mTouchState == TOUCH_STATE_SCROLLING && getScrollX()>-mChildWidth/3 && getScrollX() < (mChildWidth-sX)*(getChildCount()-1)+mChildWidth/3) {
				scrollBy(detaX, 0);
				mLastMotoinX = x;
			}
			if(getChildCount()>0)
			    updateWallpaperOffset();
			
			break;
		case MotionEvent.ACTION_UP:
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);
			
			int velocityX = (int) velocityTracker.getXVelocity();
			if (velocityX > 1000  && mCurrentScreen > 0) {
				snapToScreen(mCurrentScreen - 1);
			} else if (velocityX < -1000
					&& mCurrentScreen < getChildCount() - 1) {
				snapToScreen(mCurrentScreen + 1);
			} else {
				snapToDestination();
			}
			
			if(velocityTracker!=null){
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			mTouchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			mTouchState = TOUCH_STATE_REST;
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}
	
	private void snapToScreen(int whichScreen) {
		mCurrentScreen = whichScreen;
		
		if (mCurrentScreen > getChildCount() - 1)
			mCurrentScreen = getChildCount() - 1;

		int dx = mCurrentScreen * getWidth() - getScrollX()-mCurrentScreen*sX;

		mScroller.startScroll(getScrollX(), 0, dx, 0,  500); //Math.abs(dx)/2
		invalidate();
		mDividingLister.updateDividing(getChildCount(), mCurrentScreen);
	}

	private void snapToDestination() {
		int scrollX = getScrollX();
		int scrollY = getScrollY();
		int destScreen = (getScrollX() + mChildWidth / 2) / (mChildWidth-sX);
		snapToScreen(destScreen);
	}
	
	public interface DividingLister{
		public void updateDividing(int screenCount ,int currentScreen);
	}
	
	public CellLayout getCurrentScreenPutFolderIcon(){
		CellLayout c = null;
		if(mCurrentScreen == 0 || mCurrentScreen == mCellLayout.size()-1){
			return c;
		}
		c = (CellLayout) mCellLayout.get(mCurrentScreen);
		LayoutParams lp = c.isOcuppyInCellLayoutAuto(1, 1);
	//	CellLayout.LayoutParams l = (com.xiong.launcher.ui.CellLayout.LayoutParams) lp;
		//Xlog.logd("cellx: " + l.cellX + " celly: " + l.cellY);
		if(lp != null){
			c.setTag(lp);
		}else{
			c  = null;
		}
		return c;
	}
	
	public void addFolderIconIntoScreen(FolderIcon icon){
		CellLayout desCell = (CellLayout) mCellLayout.get(mCurrentScreen);
		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) icon.mParent.getTag();
		desCell.addView(icon,lp);
		desCell.markCellsOcuppy(lp.cellX, lp.cellY, 1, 1, true);
		resetScreenStateIfEmpty(desCell);
	}
	
	public CellLayout getCurrentScreen(){
		CellLayout c = null;
		if(mCurrentScreen == 0 || mCurrentScreen == mCellLayout.size()-1){
			if(mode == EDITING)
				return c;
		}
		c = (CellLayout) mCellLayout.get(mCurrentScreen);
		return c;
	}

	@Override
	public void onDragedItemImage(View v,int downX, int downY) {
		if(mCurrentScreen > mCellLayout.size()-1) return ;
		View view = mCellLayout.get(mCurrentScreen);
		if(mLauncher.getHotSeat().isHotSeatRect(downX, downY)){
			view = mLauncher.getHotSeat().getLayout();
		}
		if(view instanceof CellLayout){
			currentLayout = (CellLayout) view;
		}
		buildDragItemImage(v,downX, downY);
		
		if(!mFolderDragView){
			CellLayout.LayoutParams startViewLp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
			currentLayout.markCellsOcuppy(startViewLp.cellX, startViewLp.cellY,startViewLp.cellHSpan,startViewLp.cellVSpan,false);
			startLayoutParams = startViewLp;
			startLayout = currentLayout;
		}
		
		deleteZoneVisibility(View.VISIBLE);
		if(mode == NORMAL)
		    setSystemUiVisibility(View.INVISIBLE);
	}
	
	private void deleteZoneVisibility(int visibility){
		
		switch (visibility) {
		case View.VISIBLE:
			AnimationUtil.loadAnimationByResourceId(mContext, R.anim.deletezone_editing_enter);
			break;
		case View.INVISIBLE:
		case View.GONE:
			AnimationUtil.loadAnimationByResourceId(mContext, R.anim.deletezone_editing_exit);
			break;
		default:
			break;
		}
		AnimationUtil.startAnimation(mLauncher.getDeleteZone());
		mLauncher.getDeleteZone().setVisibility(visibility);
	}
	
	

	@Override
	public void onStratDrag(int moveX, int moveY) {
		startDrag(moveX, moveY);
		mLauncher.onDragIntoDeleteZone(moveY);
	}

	@Override
	public void onStopDrag(int upX, int upY) {
		stopDrag(upX, upY);
		deleteZoneVisibility(View.INVISIBLE);
		if(mode == NORMAL)
			setSystemUiVisibility(View.VISIBLE);
		ItemInfo itemInfo = (ItemInfo) mStartDragItem.getTag();
		mLauncher.onDragIntoDeleteZoneComplete(itemInfo, upY);
	}
	
	@Override
	public void onSearchRequest() {
		mLauncher.onSearchRequested();
	}
	
	public boolean isEdittingScreen(){
		boolean res = mCurrentScreen == 0 || (mCurrentScreen == mCellLayout.size()-1);
		return res;
	}
	
	public void setApps(ArrayList<AppInfo> apps){
		removeAllViews();
		mCellLayout.clearAll();
		appList = apps;
		int appNum = appList.size();
		int mScreenCount = appNum / (DeviceProfile.ROW*DeviceProfile.COLUMN);
		int reminder = appNum % (DeviceProfile.ROW*DeviceProfile.COLUMN);
		if(reminder > 0) mScreenCount++;
		syncScreenOnWrokspace(mScreenCount);
		mDividingLister.updateDividing(getChildCount(), mCurrentScreen);
		
		syncAppsOnScreen();
	}
	
	private void syncScreenOnWrokspace(int screenNum){
		for(int i=0;i<screenNum;i++){
			CellLayout cell = new CellLayout(mContext);
			cell.initCellCount(DeviceProfile.COLUMN, DeviceProfile.ROW,CellLayout.CELL_SCREEN);
			mCellLayout.add(cell);
			addView(cell,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}
	public View inflateViewFromResource(int res ,ViewGroup root){
		View view = mInflater.inflate(res, root, false);
		return view;
	}
	private void syncAppsOnScreen(){
		for (int i = 0; i < mCellLayout.size(); i++) {
			CellLayout layout = (CellLayout) mCellLayout.get(i);
			int cellsNum = DeviceProfile.COLUMN*DeviceProfile.ROW;
			int startIndex = i * cellsNum;
			int endIndex = Math.min(startIndex + cellsNum, appList.size());
			layout.removeAllViews();
			layout.setScreenIsEmpty(false);
			for (int index = startIndex; index < endIndex; index++) {
				ItemViewIcon icon = (ItemViewIcon) mInflater.inflate(R.layout.item_custom_view, layout, false);
				icon.setPerssedCallback(this);
				AppInfo app = appList.get(index);
                icon.applyFromApplicationInfo(app);
				icon.setOnClickListener(this);
				icon.setOnLongClickListener(this);
				icon.setOnTouchListener(this);

				int oneScreenIndex =  (index-startIndex) ;
				int x =oneScreenIndex % DeviceProfile.COLUMN;
				int y  = oneScreenIndex / DeviceProfile.COLUMN;
				CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x, y, app.xSpan, app.ySpan);
				layout.addView(icon, lp);
				
				layout.markCellsOcuppy(x, y, lp.cellHSpan,lp.cellVSpan,true);
			}
		}
	}
	
	public void bindAddAppOnScreen(AppInfo app){
		int lastScreen = 0;
		int childNum = getChildCount();
		if(mode == NORMAL){
			lastScreen = childNum -1;
		}else{
			lastScreen = childNum - 2;
		}
		CellLayout cellLayout = (CellLayout) mCellLayout.get(lastScreen);
		com.xiong.launcher.ui.CellLayout.LayoutParams lp = cellLayout.isOcuppyInCellLayoutAuto(app.xSpan,app.ySpan);
		if(lp == null){
			cellLayout = new CellLayout(mContext);
			cellLayout.initCellCount(DeviceProfile.COLUMN, DeviceProfile.ROW,CellLayout.CELL_SCREEN);
			mCellLayout.add(lastScreen+1, cellLayout);
			addView(cellLayout, lastScreen+1);
			mDividingLister.updateDividing(getChildCount(), mCurrentScreen);
			lp = new CellLayout.LayoutParams(0, 0, app.xSpan, app.ySpan);
		}
		ItemViewIcon icon = (ItemViewIcon) mInflater.inflate(R.layout.item_custom_view, cellLayout, false);
		icon.setPerssedCallback(this);
		icon.applyFromApplicationInfo(app);
		icon.setOnClickListener(this);
		icon.setOnLongClickListener(this);
		icon.setOnTouchListener(this);
		
		cellLayout.addView(icon, lp);
		cellLayout.markCellsOcuppy(lp.cellX, lp.cellY, lp.cellHSpan,lp.cellVSpan,true);
		resetScreenStateIfEmpty(cellLayout);
	}
	
	public void bindRemoveAppOnScreen(AppInfo appInfo){
		AppInfo target = appInfo;
		int childCount = mCellLayout.size();
		for (int i = 0; i < childCount; i++) {
			CellLayout cell = (CellLayout) mCellLayout.get(i);
			int mItemNum = cell.getChildCount();
			for (int j = 0; j < mItemNum; j++) {
				View v = cell.getChildAt(j);
				Object o = v.getTag();
				if(o instanceof AppInfo){
					AppInfo a = (AppInfo) o;
					if(a.getComponentName().equals(target.getComponentName())){
						com.xiong.launcher.ui.CellLayout.LayoutParams lp = (com.xiong.launcher.ui.CellLayout.LayoutParams) v.getLayoutParams();
						cell.markCellsOcuppy(lp.cellX, lp.cellY, lp.cellHSpan,lp.cellVSpan,false);
						cell.removeView(v);
						resetScreenStateIfEmpty(cell);
						return ;
					}
				}
			}
		}
		CellLayout hotSeatLayout = mLauncher.getHotSeat().getLayout();
		int N  = hotSeatLayout.getChildCount();
		for (int j = 0; j < N; j++) {
			View v = hotSeatLayout.getChildAt(j);
			AppInfo a = (AppInfo) v.getTag();
			if(a.getComponentName().equals(target.getComponentName())){
				com.xiong.launcher.ui.CellLayout.LayoutParams lp = (com.xiong.launcher.ui.CellLayout.LayoutParams) v.getLayoutParams();
				hotSeatLayout.markCellsOcuppy(lp.cellX, lp.cellY,lp.cellHSpan,lp.cellVSpan, false);
				hotSeatLayout.removeView(v);
				return ;
			}
		}
		
	}
	
	public void bindUpdateAppOnScreen(AppInfo appInfo){
		AppInfo target = appInfo;
		int childCount = mCellLayout.size();
		for (int i = 0; i < childCount; i++) {
			CellLayout cell = (CellLayout) mCellLayout.get(i);
			int mItemNum = cell.getChildCount();
			for (int j = 0; j < mItemNum; j++) {
				ItemInfo info = (ItemInfo) cell.getChildAt(j).getTag();
				if(info.itemType == Launcher.Settings.APPINFO_ITEM ){
					BaseTextView v = (BaseTextView) cell.getChildAt(j);
					AppInfo a = (AppInfo) info;
					if(a.getComponentName().equals(target.getComponentName())){
						v.applyFromApplicationInfo(target);
						return ;
					}
				}
			}
		}
		CellLayout hotSeatLayout = mLauncher.getHotSeat().getLayout();
		int N  = hotSeatLayout.getChildCount();
		for (int j = 0; j < N; j++) {
			ItemViewIcon v =  (ItemViewIcon)hotSeatLayout.getChildAt(j);
			AppInfo a = (AppInfo) v.getTag();
			if(a.getComponentName().equals(target.getComponentName())){
				v.applyFromApplicationInfo(target);
				return ;
			}
		}
	}

	@Override
	public void onClick(View v) {
		View view = v;
        if(v instanceof ImageView){
        	CellLayout delScreen = (CellLayout) v.getTag();
        	deleteScreen(delScreen);
        	return;
        }
		int type = onDragingViewType(v);
		if(type == Launcher.Settings.APPINFO_ITEM){
			lockIconDrawable();
			AppInfo appInfo = (AppInfo) view.getTag();
			mLauncher.startActivitySafely(view, appInfo.intent);
		}else if(type == Launcher.Settings.SHORTCUT_ITEM){
			lockIconDrawable();
			ShortCutInfo sc = (ShortCutInfo) view.getTag();
			mLauncher.startActivitySafely(view, sc.intent);
		}else if(type == Launcher.Settings.FOLDER_ITEM){
			foldericon = (FolderIcon) view;
			foldericon.showDetailFolder();
		}
	}
	
	public void dismissFolderOndragSteate(View dragImage,int folderImageL,int folderImageT){
		mFolderDragImage = dragImage;
		mFolderDragLeft = folderImageL;
		mFolderDragTop = folderImageT;
		Xlog.logd("left : " + mFolderDragLeft + "  top : " + mFolderDragTop);
		mFolderDragView = true;
		dismissFolder();
	}
	
	public void dismissFolder(){
		if(foldericon != null)
			foldericon.dismissDetailFolder();
	}

	@Override
	public boolean onLongClick(View v) {
		if(Xlog.DISPATCH_TOUCH_DEBUG)
			Xlog.logd("onlongclick***********************************");
		mLauncher.beginDragging(v);
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		return false;
	}

	@Override
	public void onDragItemToAnotherScreen(int direction) {
		if(direction == DragLayer.LEFT_DIRECTION){
			if(mCurrentScreen != 0)
				mCurrentScreen--;
		}else if(direction == DragLayer.RIGHT_DIRECTION){
			mCurrentScreen++;
		}
		currentLayout.removeView(mStartDragItem);
		View v =  mCellLayout.get(mCurrentScreen);
		if(v instanceof CellLayout){
			currentLayout = (CellLayout) v;
		}
		CellLayout.LayoutParams startLp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
		currentLayout.addView(mStartDragItem,new CellLayout.LayoutParams(-100,-100, startLp.cellHSpan, startLp.cellVSpan));
		
		snapToScreen(mCurrentScreen);
		mLauncher.setChangeScreen();
	}
	
	public int[] getAppWigetSpan(int minWidth,int minHeight){
		int[] spanXY = new int[2];
		final int w = minWidth;
		final int h = minHeight;
		LauncherApp app = (LauncherApp) mLauncher.getApplication();
		final DeviceProfile dp = app.getDeviceProfile();
		spanXY[0] = calSpanInLayout(dp.cellWidth, dp.columnGap, w);
		spanXY[1] = calSpanInLayout(dp.cellHeigth, dp.rowGap, h);
		return spanXY;
	}
	
	private int calSpanInLayout(int cell,int gap,int length){
		int span = 0;
		int temp = 0;
		int l = 0;
		l = length - cell;
		if(l > 0){
			span = (l / (cell+gap));
			temp = l % (cell+gap);
			temp = temp > 0 ? 1:0;
			span += temp;
			span++;
		}else{
			span = 1;
		}
		return span;
	}
	
	public void addAppWidgetViewInLayout(LauncherAppWidgetInfo appWidgetInfo){
		final LauncherAppWidgetInfo info = appWidgetInfo;
		if(mCurrentScreen == 0 || mCurrentScreen == mCellLayout.size()-1){
			Resources res = mContext.getResources();
			Toast.makeText(mContext, res.getString(R.string.oneditscreen), 1000).show();
			return ;
		}
		CellLayout layout = (CellLayout) mCellLayout.get(mCurrentScreen);
		CellLayout.LayoutParams lp = layout.isOcuppyInCellLayoutAuto(info.xSpan, info.ySpan);
		if(lp != null){
			info.hostView.setLongClickable(true);
			info.hostView.setOnLongClickListener(this);
			layout.addView(info.hostView, lp);
			layout.markCellsOcuppy(lp.cellX, lp.cellY, lp.cellHSpan, lp.cellVSpan, true);
		}else{
			Resources res = mContext.getResources();
			Toast.makeText(mContext, res.getString(R.string.addwidget_nospace_tip), 1000).show();
		}
		resetScreenStateIfEmpty(layout);
	}
	
	public void deleteAppWidgetViewFormLayout(LauncherAppWidgetHost host){
		if(mStartDragItem != null){
			View v = mCellLayout.get(mCurrentScreen);
			if(v instanceof CellLayout){
				currentLayout = (CellLayout) v;
			}
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
			currentLayout.markCellsOcuppy(lp.cellX, lp.cellY, lp.cellHSpan,lp.cellVSpan,false);
			currentLayout.removeView(mStartDragItem);
			LauncherAppWidgetInfo info = (LauncherAppWidgetInfo) mStartDragItem.getTag();
			host.deleteAppWidgetId(info.appWidgetId);
		}
		resetScreenStateIfEmpty(currentLayout);
		currentLayout = null;
	}

	public void addShortCutViewInLayout(ShortCutInfo scInfo){
		final ShortCutInfo sc = scInfo;
		if(mCurrentScreen == 0 || mCurrentScreen == mCellLayout.size()-1){
			Resources res = mContext.getResources();
			Toast.makeText(mContext, res.getString(R.string.oneditscreen), 1000).show();
			return ;
		}
		CellLayout layout = (CellLayout) mCellLayout.get(mCurrentScreen);
		CellLayout.LayoutParams lp = layout.isOcuppyInCellLayoutAuto(scInfo.xSpan, scInfo.ySpan);
		if(lp != null){
			ShortCutViewIcon icon = (ShortCutViewIcon) mInflater.inflate(R.layout.shortcut_custom_view, layout, false);
			icon.setPerssedCallback(this);
			icon.applyFromApplicationInfo(sc);
			icon.setOnClickListener(this);
			icon.setOnLongClickListener(this);
			icon.setOnTouchListener(this);
			layout.addView(icon, lp);
			layout.markCellsOcuppy(lp.cellX,lp.cellY,lp.cellHSpan, lp.cellVSpan, true);
		}else{
			Resources res = mContext.getResources();
			Toast.makeText(mContext, res.getString(R.string.shortcup_nospace_tip), 1000).show();
		}
		resetScreenStateIfEmpty(layout);
	}
	
	public void deleteShortCutViewFromLayout(){
		if(mStartDragItem != null){
			View v = mCellLayout.get(mCurrentScreen);
			if(v instanceof CellLayout){
				currentLayout = (CellLayout) v;
			}
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
			currentLayout.markCellsOcuppy(lp.cellX, lp.cellY, lp.cellHSpan,lp.cellVSpan,false);
			currentLayout.removeView(mStartDragItem);
			resetScreenStateIfEmpty(currentLayout);
		}
		currentLayout = null;
	}
	
	public int deleteOrDissolveFolderFromLayout(){
		final FolderIcon icon = (FolderIcon) mStartDragItem;
		final int N = icon.mFolderinfo.childItems.size();
		if(N == 0){
			currentLayout = (CellLayout) mCellLayout.get(mCurrentScreen);
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) mStartDragItem.getLayoutParams();
			currentLayout.markCellsOcuppy(lp.cellX, lp.cellY, lp.cellHSpan,lp.cellVSpan,false);
			currentLayout.removeView(mStartDragItem);
			resetScreenStateIfEmpty(currentLayout);
			currentLayout = null;
			mLauncher.removeFolderUpdateListener(icon);
			return Launcher.UNINSTALL_FOLDER_ZENO;
		}else{
			icon.mPreviewBackground.setDrawingCacheEnabled(true);
			final Bitmap b = Bitmap.createBitmap(icon.mPreviewBackground.getDrawingCache());
			icon.mPreviewBackground.destroyDrawingCache();
			mLauncher.setFolderDissolveShow(b, icon.mFolderinfo.folderName);
			return Launcher.UNINSTALL_FOLDER_LESSONE;
		}
		
	}
	
	public void dissolveFolderComplete(){
		final FolderIcon icon = (FolderIcon) mStartDragItem;
		final int N = icon.mFolderinfo.childItems.size();
		currentLayout = (CellLayout) mCellLayout.get(mCurrentScreen);
		if(N <= currentLayout.getEmptyCellCount()){
			dissolveFolder(icon, N);
		}else{
			int lastScreen = 0;
			int childNum = getChildCount();
			if(mode == NORMAL){
				lastScreen = childNum -1;
			}else{
				lastScreen = childNum - 2;
			}
			CellLayout cellLayout = new CellLayout(mContext);
			cellLayout.initCellCount(DeviceProfile.COLUMN, DeviceProfile.ROW,CellLayout.CELL_SCREEN);
			mCellLayout.add(lastScreen+1, cellLayout);
			addView(cellLayout, lastScreen+1);
			snapToScreen(lastScreen+1);
			dissolveFolder(icon, N);
		}
		
	}
	
	private void dissolveFolder(FolderIcon icon,int N){
		for (int i = 0; i < N; i++) {
			ItemInfo info = icon.mFolderinfo.childItems.get(i);
			int type  = info.itemType;
			if(type == Launcher.Settings.APPINFO_ITEM){
				bindAddAppOnScreen((AppInfo)info);
			}else{
				addShortCutViewInLayout((ShortCutInfo)info);
			}
		}
		icon.clearFolderItem();
		icon.updateFolderIconThumbnail();
	}

	@Override
	public void iconPressed(BaseTextView icon) {
		Xlog.logd("icon hashcode: " + icon.hashCode());
		pressedIcon = icon;
	}
	
	public void resetIconDrawableState(){
		if(pressedIcon != null){
			pressedIcon.resetIconDrawable();
		}
		pressedIcon = null;
	}
	
	public void lockIconDrawable(){
		if(pressedIcon != null)
			pressedIcon.lockIconDrawable();
	}
}
