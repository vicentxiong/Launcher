package com.xiong.launcher.ui;

import com.xiong.launcher.R;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.controller.AnimationUtil;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

public class HotSeat extends FrameLayout {
	private CellLayout layout;
	private HorizontalScrollView mHsc;
	private Context mContext;
	private TextView op_animation;
	private TextView op_wallpaper;
	private TextView op_widget;
	private TextView op_shortcut;
	private TextView op_folder;
	
	private OnClickListener l;

	public HotSeat(Context context) {
		this(context,null);
		
	}

	public HotSeat(Context context, AttributeSet attrs) {
		this(context,attrs,0);
		
	}

	public HotSeat(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		//setBackgroundColor(Color.BLACK);
	}
	
	public void initChildView(OnClickListener listener){
		l = listener;
		layout = (CellLayout) findViewById(R.id.layout);
		layout.initCellCount(DeviceProfile.HOTSEAT_COLUMN, DeviceProfile.HOTSEAT_ROW,CellLayout.CELL_HOTSEAT);
		mHsc = (HorizontalScrollView) findViewById(R.id.functionList);
		op_animation = (TextView) findViewById(R.id.animationItem);
		op_wallpaper = (TextView) findViewById(R.id.wallPaperItem);
		op_widget = (TextView) findViewById(R.id.widgetItem);
		op_shortcut = (TextView) findViewById(R.id.shortCutItem);
		op_folder = (TextView) findViewById(R.id.folderItem);
		op_animation.setOnClickListener(l);
		op_wallpaper.setOnClickListener(l);
		op_widget.setOnClickListener(l);
		op_shortcut.setOnClickListener(l);
		op_folder.setOnClickListener(l);
	}
	
	public boolean isHotSeatRect(int x,int y){
		int l = getLeft();
		int t = getTop();
		int r = getRight();
		int b = getBottom();
		//Xlog.logd("l " + l + " t " + t + " r " + r + " b " + b);
		Rect rect = new Rect(l, t, r, b);
		
		return rect.contains(x, y);
	}
	
	public CellLayout getLayout(){
		return layout;
	}
	
	public void resetVisibityState(int mode){
		final int m = mode;
		if(m == Workspace.NORMAL){
			layout.setVisibility(View.VISIBLE);
			mHsc.setVisibility(View.GONE);
			AnimationUtil.loadAnimationByResourceId(mContext, R.anim.hotseat_editing_enter);
			Animation layoutAnimation = AnimationUtil.getAnimation();
			layout.startAnimation(layoutAnimation);
			AnimationUtil.loadAnimationByResourceId(mContext, R.anim.hotseat_editing_exit);
			Animation hscAnimation = AnimationUtil.getAnimation();
			mHsc.startAnimation(hscAnimation);
		}else{
			layout.setVisibility(View.GONE);
			mHsc.setVisibility(View.VISIBLE);
			AnimationUtil.loadAnimationByResourceId(mContext, R.anim.hotseat_editing_exit);
			Animation layoutAnimation = AnimationUtil.getAnimation();
			layout.startAnimation(layoutAnimation);
			AnimationUtil.loadAnimationByResourceId(mContext, R.anim.hotseat_editing_enter);
			Animation hscAnimation = AnimationUtil.getAnimation();
			mHsc.startAnimation(hscAnimation);
		}
		
	}

}
