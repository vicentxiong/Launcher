package com.xiong.launcher.ui;

import com.xiong.launcher.LauncherApp;
import com.xiong.launcher.R;
import com.xiong.launcher.alg.AppInfo;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.ItemInfo;
import com.xiong.launcher.alg.Utils;
import com.xiong.launcher.alg.Xlog;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;

public class ItemViewIcon extends BaseTextView {


	public ItemViewIcon(Context context) {
		this(context,null);
	}
	
	public ItemViewIcon(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public ItemViewIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		
	}
	
	@Override
	public void applyFromApplicationInfo(ItemInfo Info) {
		AppInfo appfo = (AppInfo) Info;
		setCompoundDrawables(null, Utils.createIconDrawable(appfo.bitmap), null, null);
		setText(appfo.tiltle);
		setTag(appfo);
	}
	


}
