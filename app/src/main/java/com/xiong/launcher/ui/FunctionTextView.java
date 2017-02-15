package com.xiong.launcher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FunctionTextView extends TextView {

	public FunctionTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FunctionTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FunctionTextView(Context context) {
		super(context);
	}
	
	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if(isPressed()){
			setAlpha(0.4f);
		}else{
			setAlpha(1.0f);
		}
	}

}
