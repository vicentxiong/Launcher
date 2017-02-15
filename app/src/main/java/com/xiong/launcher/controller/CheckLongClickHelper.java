package com.xiong.launcher.controller;

import android.view.View;

public class CheckLongClickHelper {
	private View mView;
	private boolean mHasPerpfromLongClick;
	private CheckLongClickRunnable checkRunnable;
	private static final long DELAY = 500;
	
	public CheckLongClickHelper(View v){
		mView = v;
	}
	
	class CheckLongClickRunnable implements Runnable{

		@Override
		public void run() {
			if(mView.getParent()!=null && mView.hasWindowFocus() && !mHasPerpfromLongClick){
				if(mView.performLongClick()){
					mView.setPressed(false);
					mHasPerpfromLongClick = true;
				}
			}
			
		}
		
	}
	
	public void postCheckLongClick(){
		mHasPerpfromLongClick = false;
		if(checkRunnable == null){
			checkRunnable = new CheckLongClickRunnable();
		}
		mView.postDelayed(checkRunnable, DELAY);
	}
	
	
	public void cancelCheckLongClick(){
		mHasPerpfromLongClick = false;
		if(checkRunnable != null){
			mView.removeCallbacks(checkRunnable);
			checkRunnable =  null;
		}
	}
	
	public boolean isHasPerpfromLongClick(){
		return mHasPerpfromLongClick;
	}
	
}
