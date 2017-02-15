package com.xiong.launcher.alg;

import com.xiong.launcher.Launcher;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class ShortCutInfo extends ItemInfo {

	public String label;
	public Intent intent;
	public Drawable drawable;
	
	public ShortCutInfo(String label,Intent i,Drawable d){
		this.label = label;
		this.intent = i;
		this.drawable = d;
		
		xSpan = 1;
		ySpan = 1;
		itemType = Launcher.Settings.SHORTCUT_ITEM;
		setFlag();
	}
	
	private void setFlag(){
		if(intent != null){
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
	}
}
