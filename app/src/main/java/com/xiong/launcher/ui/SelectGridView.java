package com.xiong.launcher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class SelectGridView extends GridView {
	private SelectDialog dialog;

	public SelectGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SelectGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SelectGridView(Context context) {
		super(context);
	}
	
	public void setDialog(SelectDialog dialog){
		this.dialog = dialog;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final SelectDialog d = dialog;
		if(d !=  null && d.needUpdateUI()){
			d.updateUIState(this);
		}
		
	}

}
	
	


