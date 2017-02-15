package com.xiong.launcher.ui;

import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.controller.CheckLongClickHelper;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.MotionEvent;

public class LauncherAppWidgetHostView extends AppWidgetHostView {
	private CheckLongClickHelper mLongClickHelper;

	public LauncherAppWidgetHostView(Context context) {
		super(context);
		mLongClickHelper = new CheckLongClickHelper(this);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(mLongClickHelper.isHasPerpfromLongClick()){
			mLongClickHelper.cancelCheckLongClick();
			return true;
		}
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLongClickHelper.postCheckLongClick();
			Xlog.logd("appwidget long down");
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mLongClickHelper.cancelCheckLongClick();
			Xlog.logd("appwidget long up");
			break;
		default:
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		return false;
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mLongClickHelper.cancelCheckLongClick();
	}
	

}
