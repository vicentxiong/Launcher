package com.xiong.launcher.ui;

import java.util.List;
import java.util.Map;

import com.xiong.launcher.R;
import com.xiong.launcher.alg.Xlog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

public abstract class SelectDialog extends AlertDialog {
	private View rootView;
	protected SelectGridView mGridView;
	protected SimpleAdapter simpleAdapter;
	private Context mContext;
	
	protected SelectDialog(Context context) {
		super(context,R.style.Translucent_NoTitle_FullScreen);
		mContext = context;
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(Build.VERSION.SDK_INT>=19){
	        getWindow().addFlags(0x04000000);
	        getWindow().addFlags(0x08000000);
		}
		initViewAndData();
		
	}
	
	private void initViewAndData(){
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rootView = createLayoutView(inflater);
		mGridView = (SelectGridView) rootView.findViewById(R.id.selectGrid);
		simpleAdapter = new SimpleAdapter(mContext, buildAllItem(), getItemLayoutResource(), getRrom(), getTo());
		mGridView.setAdapter(simpleAdapter);
		mGridView.setOnItemClickListener(new ItemClickListener());
		mGridView.setOnItemLongClickListener(new ItemLongClickListener());
		mGridView.setDialog(this);
		setContentView(rootView);
	}
	
	private  View createLayoutView(LayoutInflater inflater){
		View v = inflater.inflate(R.layout.select_dialog_view, null, false);
		return v;
	}
	
	protected abstract List<? extends Map<String, Object>> buildAllItem();
	
	protected abstract int getItemLayoutResource();
	
	protected abstract String[] getRrom();
	
	protected abstract int[] getTo();
	
	protected abstract void itemClick(AdapterView<?> parent, View view, int position,long id);
	
	protected abstract void itemLongClick(AdapterView<?> parent, View view,int position, long id);
	
	protected abstract void updateUIState(GridView v);
	
	protected abstract boolean needUpdateUI();
	
	class ItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			
			itemClick(parent, view, position, id);
		}
		
	}
	
	class ItemLongClickListener implements OnItemLongClickListener{

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
			itemLongClick(parent, view, position, id);
			return true;
		}
		
	}

}
