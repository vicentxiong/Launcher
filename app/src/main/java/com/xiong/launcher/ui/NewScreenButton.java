package com.xiong.launcher.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.xiong.launcher.R;

public class NewScreenButton extends ViewGroup implements OnClickListener{
	private ImageView newButton ;
	private int mChildWidth, mChildHeight;
	private Workspace mWorkSpace;
	private int mIndex;
	

	public NewScreenButton(Context context) {
		this(context,null);
	}

	public NewScreenButton(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public NewScreenButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setBackgroundResource(R.drawable.editing_new_screen);
		newButton = new ImageView(context);
		newButton.setImageResource(R.drawable.editing_new_screen_btn);
		newButton.setOnClickListener(this);
		FrameLayout fm  = new FrameLayout(context);
		fm.addView(newButton, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
		addView(fm);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);
		mChildWidth = width;
		mChildHeight = height;
		int childcount = getChildCount();
		for (int i = 0; i < childcount; i++) {
			View child = getChildAt(i);
			child.measure(width, height);
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int margnLeft = 0;
		int margnTop = 0;
		int childcount = getChildCount();
		for (int i = 0; i < childcount; i++) {
			View child = getChildAt(i);
			child.layout(margnLeft, margnTop, margnLeft + mChildWidth, margnTop
					+ mChildHeight);
			margnLeft += mChildWidth;
		}
	}
	
	public void setWorkSpace(Workspace workspace,int index){
		mWorkSpace = workspace;
		mIndex = index;
	}

	@Override
	public void onClick(View v) {
		if(mIndex==Workspace.LEFT){
			mWorkSpace.addScreenOnleft();
		}else{
			mWorkSpace.addScreenOnRight();
		}
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		return true;
	}

}
