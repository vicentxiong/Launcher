package com.xiong.launcher.ui;

import com.xiong.launcher.LauncherApp;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.controller.DragItemLister;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class DragLayer extends FrameLayout{
	public static final int LEFT_DIRECTION = 1;
	public static final int RIGHT_DIRECTION = 2;
	private static final long DELAYTIME = 4;
	private int mDirection;
	private DragItemLister mDragLister;
	private int downX;
	private int downY;
	private boolean mDrag = false;
	private DeviceProfile dp;
	private Vibrator mVibrator;
	private Handler mHandler = new Handler();
	private boolean isChangeScreen = false;

	public DragLayer(Context context) {
		this(context ,null);
	}
	
	public DragLayer(Context context, AttributeSet attrs) {
		this(context ,attrs,0);
	}
	
	public DragLayer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		LauncherApp application = (LauncherApp) context.getApplicationContext();
		dp = application.getDeviceProfile();
		
	}
	
	public boolean getDragState(){
		return mDrag;
	}
	
   public void setChangeScreenEnable(){
	  mHandler.postDelayed(onChangeScreenState, DELAYTIME+1000);
   }
	
   private Runnable onChangeScreenState = new Runnable() {
	
	@Override
	public void run() {
		isChangeScreen = false;
		
	}
};
	
	public void setonDragItemLister(DragItemLister lister){
		this.mDragLister = lister;
	}
	
	public void startDraggingItem(View v){
		if(!mDrag){
			mDrag = true;
			mVibrator.vibrate(50);
	        mDragLister.onDragedItemImage(v,downX, downY);
		}
	}
	
	private Runnable onDragItemToanScreenTimer = new Runnable() {
		
		@Override
		public void run() {
			switch (mDirection) {
			case LEFT_DIRECTION:
				mDragLister.onDragItemToAnotherScreen(LEFT_DIRECTION);
				break;
			case RIGHT_DIRECTION:
				mDragLister.onDragItemToAnotherScreen(RIGHT_DIRECTION);
				break;
			default:
				break;
			}
			
		}
	};
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(Xlog.DISPATCH_TOUCH_DEBUG)
		     Xlog.logd("draglayer dispatchTouchEvent");
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN: 
			downX = (int) ev.getX();
			downY = (int) ev.getY();
			
			break;
		case MotionEvent.ACTION_MOVE:
			if(mDrag){
			    int x = (int) ev.getX();
			    int y = (int) ev.getY();
			    mDragLister.onStratDrag(x, y);
                if(!isChangeScreen){
    			    if(x < 5){
    			    	mDirection = LEFT_DIRECTION;
    			    	mHandler.postDelayed(onDragItemToanScreenTimer, DELAYTIME);
    			    	isChangeScreen = true;
    			    }else if(x > dp.mDisplayWidth-5){
    			    	mDirection = RIGHT_DIRECTION;
    			    	mHandler.postDelayed(onDragItemToanScreenTimer,DELAYTIME);
    			    	isChangeScreen = true;
    			    }
                }
			}
            break;
		case MotionEvent.ACTION_UP:
			int x = (int) ev.getX();
		    int y = (int) ev.getY();
			final float offSetY = y - downY;
			if(offSetY > dp.mDisplayHeight/3 && !mDrag){
			
				mDragLister.onSearchRequest();
			}
			if(mDrag){
				mDragLister.onStopDrag(x,y);
				mDrag = false;
				isChangeScreen = false;
				mHandler.removeCallbacks(onDragItemToanScreenTimer);
			}
			break;
		default:
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(Xlog.DISPATCH_TOUCH_DEBUG)
			Xlog.logd("draglayer onInterceptTouchEvent");
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				
				break;
			case MotionEvent.ACTION_MOVE:

			    
				break;
			case MotionEvent.ACTION_UP:
				
				break;
			default:
				break;

		}
		return true;
	}


}
