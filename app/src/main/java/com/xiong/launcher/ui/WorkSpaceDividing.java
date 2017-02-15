package com.xiong.launcher.ui;

import java.util.ArrayList;

import com.xiong.launcher.R;
import com.xiong.launcher.ui.Workspace.DividingLister;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class WorkSpaceDividing extends LinearLayout implements DividingLister{
	private ArrayList<ImageView> images = new ArrayList<ImageView>();
	private static final int COUNT = 10;
	private Context mContext;

	public WorkSpaceDividing(Context context) {
		this(context ,null);
	}

	public WorkSpaceDividing(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public WorkSpaceDividing(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initViews(COUNT);
	}
	
	private void initViews(int count){
		for(int i=0;i<count;i++){
			ImageView img = new ImageView(mContext);
			images.add(img);
		}
	}

	@Override
	public void updateDividing(int screenCount, int currentScreen) {
		removeAllViews();
		
		if(screenCount>images.size()){
			genImageView(screenCount-images.size());
		}
		for(int i=0;i<screenCount;i++){
			images.get(i).setImageResource(R.drawable.workspace_seekpoint_normal);
			addView(images.get(i),new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		}
		
		images.get(currentScreen).setImageResource(R.drawable.workspace_seekpoint_highlight);
	}
	
	private void genImageView(int amount){
		initViews(amount);
	}

}
