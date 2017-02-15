package com.xiong.launcher.ui;

import java.util.List;
import java.util.Map;

import com.xiong.launcher.R;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

public class WidgetSelectDialog extends SelectDialog {

	public WidgetSelectDialog(Context context) {
		super(context);
		
	}

	@Override
	protected List<? extends Map<String, Object>> buildAllItem() {
		return null;
	}

	@Override
	protected int getItemLayoutResource() {
		return 0;
	}

	@Override
	protected String[] getRrom() {
		return null;
	}

	@Override
	protected int[] getTo() {
		return null;
	}

	@Override
	protected void itemClick(AdapterView<?> parent, View view, int position,
			long id) {

	}

	@Override
	protected void itemLongClick(AdapterView<?> parent, View view,
			int position, long id) {

	}

	@Override
	protected void updateUIState(GridView v) {

	}

	@Override
	protected boolean needUpdateUI() {
		return false;
	}

}
