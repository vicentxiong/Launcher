package com.xiong.launcher.controller;

import android.view.View;

public interface DragItemLister {
	
	public void onDragedItemImage(View v,int downX,int downY);
	
	public void onStratDrag(int moveX,int moveY);
	
	public void onStopDrag(int upX,int upY);
	
	public void onDragItemToAnotherScreen(int direction);
	
	public void onSearchRequest();

}
