package com.xiong.launcher.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.xiong.launcher.LauncherApp;
import com.xiong.launcher.R;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.ui.FolderIcon.FolderRingAnimator;

public class CellLayout extends ViewGroup {
	public static int CELL_SCREEN = 0;
	public static int CELL_HOTSEAT = 1;
	public static int CELL_FOLDER = 2;
	private int mChildWidth, mChildHeight;
	private int delBtnWidth,delBtnHeight;
	private int mRow;
	private int mColumn;

	private Handler mHandler = new Handler();
	private DeviceProfile dp;
	private Context mContext;
	private boolean[][] mOcuppy ;
	private int  mType = CELL_SCREEN;
	private Rect mRect = new Rect();
	private View target = null;
	
	private ImageView delScreen ;
	private boolean screenIsEmpty = true;
	private boolean mInFolder = false;
	
	private ArrayList<FolderRingAnimator> mFolderRingAnimators = new ArrayList<FolderRingAnimator>();

	public CellLayout(Context context) {
		this(context ,null);
	}

	public CellLayout(Context context, AttributeSet attrs) {
		this(context ,attrs,0);
	}

	public CellLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setWillNotDraw(false);
		mContext = context;
	    LauncherApp application = (LauncherApp) mContext.getApplicationContext();
		dp = application.getDeviceProfile();
		//setBackgroundColor(Color.RED);
	
	}
	
	public void initCellCount(int column,int row,int type){
		mRow = row;
		mColumn = column;
		mOcuppy = new boolean[mColumn][mRow];
		this.mType = type;
	}
	
	public int getType(){
		return mType;
	}
	
	public boolean isScreenIsEmpty() {
		return screenIsEmpty;
	}

	public void setScreenIsEmpty(boolean screenIsEmpty) {
		this.screenIsEmpty = screenIsEmpty;
	}
	
	public void addViewInto(View delB,OnClickListener l){
		delScreen = (ImageView) delB;
		delScreen.setOnClickListener(l);
		delScreen.setTag(this);
		addView(delScreen);
		setScreenIsEmpty(true);
	}
	
	public void removeViewFrom(){
		removeView(delScreen);
		setScreenIsEmpty(false);
	}
	
	public void setDelBtnVisibility(int visibility){
		if(delScreen != null)
			delScreen.setVisibility(visibility);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);

		int childcount = getChildCount();
		mChildWidth = width;
		mChildHeight = height;
		
		int delScreenWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST);
		int delScreenHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
		if(delScreen!=null)
			delScreen.measure(delScreenWidth, delScreenHeight);
		
		for (int i = 0; i < childcount; i++) {
			View child = getChildAt(i);
			if(!(child instanceof ImageView)){
				measureChild(child);
			}
		}
	}
	
	public void markCellsOcuppy(int x,int y,int spanx,int spany,boolean opy){
		for (int i = x; i < x+spanx; i++) {
			for (int j = y; j < y+spany; j++) {
				mOcuppy[i][j] = opy;
			}
		}
	}
	
	public void markAllCellsOcuppy(boolean opy){
		for (int i = 0; i < mColumn; i++) {
			for (int j = 0; j < mRow; j++) {
				mOcuppy[i][j] = opy;
			}
		}
	}
	
	private void measureChild(View child){
		LayoutParams lp = (LayoutParams) child.getLayoutParams();
		if(mType == CELL_FOLDER)
			lp.setUp((int)(dp.cellWidth*0.15), (int)(dp.cellWidth*0.15), 10, 10,25,25);
		else
			lp.setUp(dp.cellWidth, dp.cellHeigth, dp.columnGap, dp.rowGap,dp.cellMarginLeft,dp.cellMarginTop);
		int exWidth = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
		int exHeight = MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
		child.measure(exWidth, exHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(delScreen != null){
			delBtnWidth = delScreen.getMeasuredWidth();
			delBtnHeight = delScreen.getMeasuredHeight();
			delScreen.layout(0, 0, delBtnWidth, delBtnHeight);
		}
		
		//Xlog.logd("celllayout l " + l + " t " + t + " r " + r + " b " + b+ " change: " + changed);
		int childCount = getChildCount();
		int left = 0;
		int top = 0;
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if(!(child instanceof ImageView)){
				LayoutParams lp = (LayoutParams) child.getLayoutParams();
				left = lp.x;
				top = lp.y;
				child.layout(left, top, left+lp.width, top+lp.height);
			}
		}
	}
	
	public void showFolderAccept(FolderRingAnimator anim){
		mFolderRingAnimators.add(anim);
	}
	
	public void hideFolderAccept(FolderRingAnimator anim){
		if(mFolderRingAnimators.contains(anim)){
			mFolderRingAnimators.remove(anim);
		}
		invalidate();
	}
	
//	@Override
//	protected void dispatchDraw(Canvas canvas) {
//				Xlog.logd("CellLayout dispatchdraw: ");
//				super.dispatchDraw(canvas);
//	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		final int N = mFolderRingAnimators.size();
		//Xlog.logd("ondraw folderRingAnimator : " + N );
		for (int i = 0; i < N; i++) {
			FolderRingAnimator fra = mFolderRingAnimators.get(i);
			
			Drawable d;
			int width,height;
			View child = getTargetChild(fra.cellX, fra.cellY);
			if(child != null){
				//Xlog.logd("ondraw for open folder");
				LayoutParams lp = (LayoutParams) child.getLayoutParams();
				int centerX = lp.x + lp.width/2;
				int centerY = lp.y + lp.width/2;
				
				d = FolderRingAnimator.mSharedRingDrawabble;
				width = (int) fra.mRingSize;
				height = width;
				canvas.save();
				canvas.translate(centerX-width/2, centerY-height/2);
				d.setBounds(0, 0, width, height);
				d.draw(canvas);
				canvas.restore();
			}
			
		}
	}

	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	
	public int getEmptyCellCount(){
		int count = 0;
		for (int i = 0; i < mColumn; i++) {
			for (int j = 0; j < mRow; j++) {
                if(!mOcuppy[i][j]){
    				count++;
                }
			}
		}
		return count;
	}
	
	public LayoutParams isOcuppyInCellLayoutAuto(int spanX,int spanY){
		LayoutParams lp;
		for (int i = 0; i < mColumn; i++) {
			for (int j = 0; j < mRow; j++) {
                if(!mOcuppy[i][j]){
                	if(isHaveSpace(i, j, spanX, spanY)){
                		lp = new LayoutParams(i, j, spanX, spanY);
        				//lp.setUp(dp.cellWidth, dp.cellHeigth, dp.columnGap, dp.rowGap,dp.cellMarginLeft,dp.cellMarginTop);
        				return lp;
                	}
    				
                }
			}
		}
        lp = null;
		return lp;
	}
	
	private boolean isHaveSpace(int i,int j,int x,int y){
		boolean is = true;
		for (int k = i; k < i+x; k++) {
			for (int k2 = j; k2 < j+y; k2++) {
				if(k== DeviceProfile.COLUMN|| k2 == DeviceProfile.ROW || mOcuppy[k][k2]){
					is = false;
					return is;
				}
			}
		}
		return is;
	}
	
	public LayoutParams isOcuppyInCellLayout(int touchX,int touchY,int spanx,int spany,boolean isMove){
		LayoutParams minLp = null;
		int pos = Integer.MAX_VALUE;
		int startx = -1;
		int starty = -1;
		for (int i = 0; i < mColumn; i++) {
			for (int j = 0; j < mRow; j++) {
				if(isHaveSpace(i, j, spanx, spany)){
					LayoutParams lp = new LayoutParams(i, j, spanx, spany);
					lp.setUp(dp.cellWidth, dp.cellHeigth, dp.columnGap, dp.rowGap,dp.cellMarginLeft,dp.cellMarginTop);
					int l = lp.x;
					int t = lp.y;
					int w = lp.width;
					int h = lp.height;
					int distance = (int) Math.sqrt(Math.abs(touchX-(l+w/2))*Math.abs(touchX-(l+w/2))+
							                       Math.abs(touchY - (t+h/2))*Math.abs(touchY - (t+h/2)));
					if(distance < pos){
						minLp = lp;
						pos = distance;
						startx = i;
						starty = j;
					}
				}
			}
		}
		if( minLp != null && startx != -1 && starty != -1){
			markCellsOcuppy(startx, starty, spanx, spany, !isMove);
		}
		return minLp;
	}
	
	public View isCanCreateFolder(int x,int y){
		
		for (int i = 0; i < mColumn; i++) {
			for (int j = 0; j < mRow; j++) {
				LayoutParams lp = new LayoutParams(i, j, 1, 1);
				lp.setUp(dp.cellWidth, dp.cellHeigth, dp.columnGap, dp.rowGap,dp.cellMarginLeft,dp.cellMarginTop);
				int l = lp.x;
				int t = lp.y;
				int w = lp.width;
				int h = lp.height;
				mRect.set(l, t, l+w, t+h);
				if(mRect.contains(x, y)){
					if(mOcuppy[i][j]){
						int distance = (int) Math.sqrt(Math.abs(x-(l+w/2))*Math.abs(x-(l+w/2))+
			                       Math.abs(y - (t+h/2))*Math.abs(y - (t+h/2)));
						Xlog.loge("distance = " + distance);
						if(distance<=50){
							target = getTargetChild(lp.cellX, lp.cellY);
							enterFolder();
							return target;
						}else{
							exitFolder();
						}
					}
				}
			}
		}
		return target;
	}
	
	public void enterFolder(){
		if(!mInFolder){
			((Workspace)getParent()).onDragInFolder(target);
			mInFolder = true;
		}
	}
	
	public void exitFolder(){
		if(mInFolder){
			((Workspace)getParent()).onDragOutFolder();
			target =null;
			mInFolder = false;
		}
	}
	private View getTargetChild(int cellx,int celly){
		View v = null;
		int num = getChildCount();
		for (int i = 0; i < num; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			if(cellx == lp.cellX && celly == lp.cellY) {
					//Xlog.logd("gettargetchild===>" );
					v = child;
					break;
			}
		}
		return v;
	}
	
	public static class LayoutParams extends MarginLayoutParams{
		int cellX;
		int cellY;
		int cellHSpan;
		int cellVSpan;
		int x;
		int y;

		public LayoutParams(Context c, AttributeSet attr) {
			super(c, attr);
		}
		
        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.cellX = source.cellX;
            this.cellY = source.cellY;
            this.cellHSpan = source.cellHSpan;
            this.cellVSpan = source.cellVSpan;
        }
		
		public LayoutParams(int cellx,int celly,int cellHspan,int cellVspan){
			super(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
			this.cellX = cellx;
			this.cellY = celly;
			this.cellHSpan = cellHspan;
			this.cellVSpan = cellVspan;
		}
		
	    public void setUp(int cellWidth,int cellHeight,int columnGap,int rowGap,int marginLeft,int marginTop){
	    	this.width = cellWidth*cellHSpan + columnGap*(cellHSpan - 1);
	    	this.height = cellHeight*cellVSpan + rowGap*(cellVSpan - 1);
	    	
	    	this.x = cellX * cellWidth + cellX*(columnGap ) + marginLeft; 
	    	this.y = cellY * cellHeight + cellY*(rowGap )  + marginTop;
	    }
		
	}


}
