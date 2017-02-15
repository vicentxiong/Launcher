package com.xiong.launcher.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.xiong.launcher.R;
import com.xiong.launcher.alg.Xlog;

import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class AnimationSelectDialog extends SelectDialog {
	private static final String IMAGEKEY = "itemImage";
	private static final String TEXTKEY = "itemText";
	public static final int CLASSIC = 0;
	public static final int FADE = 1;
	public static final int JUMP = 2;
	public static final int ROTATE = 3;
	public static final int SCALE = 4;
	private int[] images = new int[]{
            R.drawable.classic,
            R.drawable.fade,
            R.drawable.jump,
            R.drawable.rotate,
            R.drawable.scale
            };
	private String[] texts = new String[]{
       "经典",
       "淡进淡出",
       "弹跳",
       "转盘",
       "缩放"
       };

	public AnimationSelectDialog(Context context) {
		super(context);
		
	}
	

	private void updateSelectedState(GridView gv){
		int N = gv.getChildCount();
		for (int i = 0; i < N; i++) {
			View v = gv.getChildAt(i);
			ImageView mTick = (ImageView) v.findViewById(R.id.item_selected);
			mTick.setVisibility(View.INVISIBLE);
		}
		AnimationTypeSharedPerfences sp = new AnimationTypeSharedPerfences(getContext());
		int type = sp.getValue();
		View v = gv.getChildAt(type);
		if(v != null){
			ImageView mTick = (ImageView) v.findViewById(R.id.item_selected);
			mTick.setVisibility(View.VISIBLE);
		}

	}
	
	

	@Override
	protected List<? extends Map<String, Object>> buildAllItem() {
		
		ArrayList<HashMap<String, Object>> allItems = new ArrayList<HashMap<String,Object>>();
		final int count = images.length;
		for (int i = 0; i < count; i++) {
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put(IMAGEKEY, images[i]);
			item.put(TEXTKEY, texts[i]);
			allItems.add(item);
		}
		return allItems;
	}

	@Override
	protected int getItemLayoutResource() {
		return R.layout.animation_select_dialog_view;
	}

	@Override
	protected String[] getRrom() {
		return new String[] { IMAGEKEY, TEXTKEY};
	}

	@Override
	protected int[] getTo() {
		return new  int[]{R.id.itemImage,R.id.itemText};
	}

	@Override
	protected void itemClick(AdapterView<?> parent, View view, int position,long id) {
		//Xlog.logd("position: " + position + "  id:" + id);
		AnimationTypeSharedPerfences sp = new AnimationTypeSharedPerfences(getContext());
		sp.putValue(position);
		updateSelectedState(mGridView);
	}

	@Override
	protected void itemLongClick(AdapterView<?> parent, View view,int position, long id) {}
	
	public static class AnimationTypeSharedPerfences{
		private Context mContext;
		private SharedPreferences sp;
		private static final String NAME ="animationType";
		private static final String TYPEKEY = "type";
		
		public AnimationTypeSharedPerfences(Context cx){
			mContext = cx;
			sp = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		}
		
		public void putValue(int type){
			Editor editor = sp.edit();
			editor.putInt(TYPEKEY, type);
			editor.commit();
		}
		
		public int getValue(){
			int value = sp.getInt(TYPEKEY, 0);
			return value;
		}
		
	}

	@Override
	protected void updateUIState(GridView v) {
		updateSelectedState(v);
		
	}


	@Override
	protected boolean needUpdateUI() {
		return true;
	} 



}
