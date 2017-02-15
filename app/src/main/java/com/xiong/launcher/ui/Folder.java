package com.xiong.launcher.ui;

import java.util.List;

import com.xiong.launcher.Launcher;
import com.xiong.launcher.LauncherApp;
import com.xiong.launcher.R;
import com.xiong.launcher.alg.AppInfo;
import com.xiong.launcher.alg.DeviceProfile;
import com.xiong.launcher.alg.FolderInfo;
import com.xiong.launcher.alg.ItemInfo;
import com.xiong.launcher.alg.ShortCutInfo;
import com.xiong.launcher.alg.Xlog;
import com.xiong.launcher.ui.CellLayout.LayoutParams;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class Folder extends LinearLayout implements View.OnClickListener{
	private Launcher mContext;
	private View editRootView;
	private EditText editFolderLabel;
	private Button mSave;
	private GridView mGridView;
	private TextView folderLabel;
	private FolderItemAdapter adapter;
	private FolderInfo mInfo;
	private FolderIconFreshCallback freshCallback;
	private LayoutInflater mInflater;
	private LauncherApp app;
	private DeviceProfile dp;
	private boolean mEditMode = false;
	private int mLongDownX;
	private int mLongDownY;
	
	public Folder(Context context) {
		this(context,null);
	}
	
	public Folder(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}

	public Folder(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	

	public boolean ismEditMode() {
		return mEditMode;
	}

	public void setmEditMode(boolean mEditMode) {
		this.mEditMode = mEditMode;
	}
	
	public void updateEditView(){
		folderLabel.setVisibility(View.VISIBLE);
		editRootView.setVisibility(View.GONE);
	}

	public void initData(Launcher context, FolderInfo info,FolderIconFreshCallback cb){
		mContext = context;
		mInfo = info;
		freshCallback = cb;
		mInflater = LayoutInflater.from(mContext);
		app = (LauncherApp) mContext.getApplication();
		dp = app.getDeviceProfile();
		
		mGridView = (GridView) findViewById(R.id.folderGrid);
		folderLabel = (TextView) findViewById(R.id.folder_dentail_label);
		editRootView = findViewById(R.id.folder_edit_root);
		editFolderLabel  = (EditText) editRootView.findViewById(R.id.folder_edit_label);
		mSave = (Button) editRootView.findViewById(R.id.folder_edit_ok);
		String foldername= mInfo.folderName;
		folderLabel.setText((foldername==null||foldername.length()==0)?mContext.getResources().getString(R.string.folder_lable_null):foldername);
		folderLabel.setOnClickListener(this);
		mSave.setOnClickListener(this);
		adapter = new FolderItemAdapter(mContext, mInfo.childItems);
		mGridView.setAdapter(adapter);
		mGridView.setOnItemClickListener(new ItemClickListener());
		mGridView.setOnItemLongClickListener(new ItemLongClickListener());
	}
	
	public void notifyUpdateFolder(){
		if(adapter != null){
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		return true;
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mLongDownX = (int) ev.getRawX();
		mLongDownY = (int) ev.getRawY();
		//Xlog.logd("x: " + mLongDownX + " y : " + mLongDownY);
		return super.dispatchTouchEvent(ev);
	}
	
	class ItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ItemInfo info = (ItemInfo) view.getTag();
			int type = info.itemType;
			if (type == Launcher.Settings.APPINFO_ITEM) {
				 Folder.this.mContext.getWorkSpace().lockIconDrawable();
				AppInfo appInfo = (AppInfo) view.getTag();
				mContext.startActivitySafely(view, appInfo.intent);
			} else if (type == Launcher.Settings.SHORTCUT_ITEM) {
				Folder.this.mContext.getWorkSpace().lockIconDrawable();
				ShortCutInfo sc = (ShortCutInfo) view.getTag();
				mContext.startActivitySafely(view, sc.intent);
			}
		}
	}

	class ItemLongClickListener implements OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			ItemInfo info = (ItemInfo) view.getTag();
			int type = info.itemType;
			CellLayout mCurrent = mContext.getWorkSpace().getCurrentScreen();
			BaseTextView newIcon = null;
			if (type == Launcher.Settings.APPINFO_ITEM) {
				newIcon = (BaseTextView) mInflater.inflate(R.layout.item_custom_view,mCurrent , false);
			} else if (type == Launcher.Settings.SHORTCUT_ITEM) {
				newIcon = (BaseTextView) mInflater.inflate(R.layout.shortcut_custom_view, mCurrent, false);
			}
			newIcon.setPerssedCallback(mContext.getWorkSpace());
			newIcon.applyFromApplicationInfo(info);
			newIcon.setOnClickListener(mContext.getWorkSpace());
			newIcon.setOnLongClickListener(mContext.getWorkSpace());
			newIcon.setOnTouchListener(mContext.getWorkSpace());
			mContext.getWorkSpace().iconPressed(newIcon);
			int l = mLongDownX - dp.cellWidth/2;
			int t = mLongDownY - dp.cellHeigth/2;
			
//			newIcon.setLeft(l);
//			newIcon.setTop(t);
//			newIcon.setRight(l+dp.cellWidth);
//			newIcon.setBottom(t+dp.cellHeigth);
//			newIcon.measure(dp.cellWidth, dp.cellHeigth);
			
			CellLayout.LayoutParams lp = new CellLayout.LayoutParams(100, 100, info.xSpan, info.ySpan);
			lp.setUp(dp.cellWidth, dp.cellHeigth, dp.columnGap, dp.rowGap,dp.cellMarginLeft,dp.cellMarginTop);
			newIcon.setLayoutParams(lp);
			mCurrent.addView(newIcon);
			mContext.getWorkSpace().dismissFolderOndragSteate(view,l ,t);
			mContext.beginDragging(newIcon);
			return true;
		}

	}
	
	class FolderItemAdapter extends BaseAdapter {
		private Launcher mLauncher;
		private List<ItemInfo> itemInfos;

		public FolderItemAdapter(Launcher c, List<ItemInfo> infos) {
			mLauncher = c;
			itemInfos = infos;
		}

		@Override
		public int getCount() {

			return itemInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return itemInfos.get(position);
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public int getItemViewType(int position) {
			if(itemInfos.size()==0)
				return 0;
			return itemInfos.get(position).itemType;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = null;
			ItemInfo info = itemInfos.get(position);
			if (convertView == null) {
				if (info.itemType == Launcher.Settings.APPINFO_ITEM)
					v = mInflater.inflate(R.layout.item_custom_view, parent,
							false);
				else if (info.itemType == Launcher.Settings.SHORTCUT_ITEM)
					v = mInflater.inflate(R.layout.shortcut_custom_view,
							parent, false);
			} else {
				v = convertView;
			}
			BaseTextView itemView = (BaseTextView) v;
			itemView.applyFromApplicationInfo(info);
			itemView.setPerssedCallback(mLauncher.getWorkSpace());
			v.setTag(info);
			return v;
		}

	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.folder_dentail_label:
			folderLabel.setVisibility(View.GONE);
			editRootView.setVisibility(View.VISIBLE);
			editFolderLabel.setText(mInfo.folderName);
			editFolderLabel.setSelectAllOnFocus(true);
			editFolderLabel.setSelection(mInfo.folderName.length());
			showKeyboard();
			mEditMode = true;
			break;
		case R.id.folder_edit_ok:
			String  newFolderLabel = editFolderLabel.getText().toString();
			Resources r = mContext.getResources();
			mInfo.folderName = newFolderLabel;
			folderLabel.setText(mInfo.folderName.length()==0?r.getString(R.string.folder_lable_null):newFolderLabel);
			folderLabel.setVisibility(View.VISIBLE);
			editRootView.setVisibility(View.GONE);
			if(freshCallback != null)
				freshCallback.updateFolderIcon();
			
			mEditMode = false;
			break;
		default:
			break;
		}
	}
	
	private void showKeyboard() {
		if(editFolderLabel!=null){
			editFolderLabel.setFocusable(true);
			editFolderLabel.setFocusableInTouchMode(true);
			editFolderLabel.requestFocus();
			editFolderLabel.setSelected(true);
			InputMethodManager inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.showSoftInput(editFolderLabel, 0);
		}
	}
	
	public interface FolderIconFreshCallback{
		public void updateFolderIcon();
	}
}
