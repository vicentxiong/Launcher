package com.xiong.launcher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ShadowFrameLayout extends FrameLayout {

	public ShadowFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ShadowFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ShadowFrameLayout(Context context) {
		super(context);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

}
