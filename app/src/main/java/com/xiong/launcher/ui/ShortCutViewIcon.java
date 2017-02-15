package com.xiong.launcher.ui;

import com.xiong.launcher.LauncherApp;
import com.xiong.launcher.R;
import com.xiong.launcher.alg.AppInfo;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.ItemInfo;
import com.xiong.launcher.alg.ShortCutInfo;
import com.xiong.launcher.alg.Utils;
import com.xiong.launcher.alg.Xlog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.TextView;

public class ShortCutViewIcon extends BaseTextView {

	public ShortCutViewIcon(Context context) {
		this(context,null);
	}
	
	public ShortCutViewIcon(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public ShortCutViewIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	@Override
	public void applyFromApplicationInfo(ItemInfo Info) {
		ShortCutInfo shortCutInfo = (ShortCutInfo) Info;
		setCompoundDrawables(null, Utils.createIconDrawable(Utils.createIconBitmap(shortCutInfo.drawable,mContext)), null, null);
		setText(shortCutInfo.label);
		setTag(shortCutInfo);
	}

}
