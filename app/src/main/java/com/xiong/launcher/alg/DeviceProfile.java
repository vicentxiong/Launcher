package com.xiong.launcher.alg;

import com.xiong.launcher.Launcher;
import com.xiong.launcher.R;
import com.xiong.launcher.ui.HotSeat;
import com.xiong.launcher.ui.WorkSpaceDividing;
import com.xiong.launcher.ui.Workspace;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.widget.FrameLayout.LayoutParams;

public class DeviceProfile {
	public static final int ROW = 5;
	public static final int COLUMN =4;
	public static final int HOTSEAT_ROW = 1;
	public static final int HOTSEAT_COLUMN = 4;
	public int cellWidth;
	public int cellHeigth;
	public int iconAppsize;
	public int folderPreSize;
	public int iconTextSzie;
	public int columnGap;
	public int rowGap;
	public int statusBarHeight ;
	public int navigationBarHeight;
	public int cellMarginLeft;
	public int cellMarginTop;
	public int mDisplayWidth;
	public int mDisplayHeight;
	public int mDividingHeight;

	private Context mContext;
	private DisplayMetrics mDm;

	public DeviceProfile(Context context, DisplayMetrics dm) {
		mContext = context;
		mDm = dm;

	}

	public void initConfigure() {
		Resources r = mContext.getResources();
		mDisplayWidth = mDm.widthPixels;
		mDisplayHeight = mDm.heightPixels;
		 columnGap = (int) r.getDimension(R.dimen.column_gap);
		 rowGap = (int) r.getDimension(R.dimen.row_gap);
		 cellMarginLeft = (int) r.getDimension(R.dimen.cell_margin_left);
		 cellMarginTop = (int) r.getDimension(R.dimen.cell_margin_top);
		 mDividingHeight = (int) r.getDimension(R.dimen.dividingHeight);

		int width = mDm.widthPixels;
		int height = mDm.heightPixels;

		 statusBarHeight = getStatusBarHeight(r);
		 navigationBarHeight = getNavigationBarHeight(r);
		 
		 cellWidth = (width-columnGap*(COLUMN+1)  )/COLUMN;
		 cellHeigth = (height-rowGap*(ROW+1)-navigationBarHeight )/(ROW+1);
		 iconTextSzie = (int) r.getDimension(R.dimen.app_text_size);
		 iconAppsize =  (int) r.getDimension(R.dimen.app_icon_size);
		 
		 folderPreSize = (int) (cellWidth);
		
		 Xlog.logd("DeviceProfile>>cellWidth : " + cellWidth + "  cellHeigth : " + cellHeigth + "  iconAppsize : " + iconAppsize);
		
	}

	private int getStatusBarHeight(Resources res) {
		int result = 0;
		int resourceId = res.getIdentifier("status_bar_height", "dimen","android");
		if (resourceId > 0) {
			result = res.getDimensionPixelSize(resourceId);
		}
		return result;
	}

	private  int getNavigationBarHeight(Resources res) {
		int navigationBarHeight = 0;
		int id = res.getIdentifier("navigation_bar_height", "dimen", "android");
		if (id > 0 && checkDeviceHasNavigationBar(res)) {
			navigationBarHeight = res.getDimensionPixelSize(id);
		}
		return navigationBarHeight;
	}

	private  boolean checkDeviceHasNavigationBar(Resources res) {
		boolean hasNavigationBar = false;
		int id = res.getIdentifier("config_showNavigationBar", "bool","android");
		if (id > 0) {
			hasNavigationBar = res.getBoolean(id);
		}
		// try {
		// Class systemPropertiesClass =
		// Class.forName("android.os.SystemProperties");
		// Method m = systemPropertiesClass.getMethod("get", String.class);
		// String navBarOverride = (String) m.invoke(systemPropertiesClass,
		// "qemu.hw.mainkeys");
		// if ("1".equals(navBarOverride)) {
		// hasNavigationBar = false;
		// } else if ("0".equals(navBarOverride)) {
		// hasNavigationBar = true;
		// }
		// } catch (Exception e) {
		// Log.w(TAG, e);
		// }

		return hasNavigationBar;
	}
	
	public void layout(Launcher l){
		Workspace mWorkspace = l.getWorkSpace();
		mWorkspace.setPadding(0,statusBarHeight, 0,0);
		LayoutParams lp = (LayoutParams) mWorkspace.getLayoutParams();
		lp.height = statusBarHeight + (ROW)*(cellHeigth)+cellHeigth/2 +(ROW+1)*(rowGap) ;
		mWorkspace.setLayoutParams(lp);
		
		WorkSpaceDividing mDividing = l.getwsDividing();
		lp = (LayoutParams) mDividing.getLayoutParams();
		lp.topMargin = statusBarHeight + (ROW)*(cellHeigth) +(ROW+1)*(rowGap);
		mDividing.setLayoutParams(lp);
		
		HotSeat mHotseat = l.getHotSeat();
		lp = (LayoutParams) mHotseat.getLayoutParams();
		lp.height = (cellHeigth + rowGap);
		lp.topMargin = statusBarHeight + (ROW)*(cellHeigth) +(ROW+1)*(rowGap) + mDividingHeight;
		mHotseat.setLayoutParams(lp);
	}

}
