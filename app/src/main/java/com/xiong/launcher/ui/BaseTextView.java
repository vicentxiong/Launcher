package com.xiong.launcher.ui;

import com.xiong.launcher.LauncherApp;
import com.xiong.launcher.alg.AppInfo;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.ItemInfo;
import com.xiong.launcher.alg.Xlog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public abstract class BaseTextView extends TextView {
	protected Context mContext;
	private PressedCallBack mCallback;
	private boolean isLockIconDrawable = false;

	public BaseTextView(Context context) {
		super(context);
	}
	
	public BaseTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public BaseTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}
	
	public void setPerssedCallback(PressedCallBack callBack){
		mCallback = callBack;
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LauncherApp application = (LauncherApp) mContext.getApplicationContext();
		DeviceProfile devprofile = application.getDeviceProfile();
		setTextSize(TypedValue.COMPLEX_UNIT_SP, devprofile.iconTextSzie);
	}
	
	
	public void lockIconDrawable(){
		setAlpha(0.4f);
		isLockIconDrawable = true;
	}
	
	public void resetIconDrawable(){
		isLockIconDrawable = false;
		post(new Runnable() {
			@Override
			public void run() {
				refreshDrawableState();
			}
		});
	}
	
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if(isPressed()){
			setAlpha(0.4f);
			if(mCallback != null){
				mCallback.iconPressed(this);
			}
		}else if(!isLockIconDrawable){
			setAlpha(1.0f);
		}
	}
	
	public abstract void applyFromApplicationInfo(ItemInfo Info);
	
	public static interface PressedCallBack{
		public void iconPressed(BaseTextView icon);
	}

}
