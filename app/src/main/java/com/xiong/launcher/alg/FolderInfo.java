package com.xiong.launcher.alg;

import java.util.ArrayList;

import com.xiong.launcher.Launcher;
import com.xiong.launcher.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class FolderInfo extends ItemInfo {
	public String folderName;
	public int resid;
	public ArrayList<ItemInfo> childItems = new ArrayList<ItemInfo>();
	
	public FolderInfo(Context context){
		Resources res = context.getResources();
		resid = R.drawable.hs_folder;
		folderName = res.getString(R.string.folder_default_name);
		
		xSpan = 1;
		ySpan = 1;
		
		itemType = Launcher.Settings.FOLDER_ITEM;
	}
	
	public boolean addItemIntoFolder(ItemInfo iteminfo){
		return childItems.add(iteminfo);
	}
	
	public boolean contains(ItemInfo iteminfo){
		return childItems.contains(iteminfo);
	}
	
	public void removeItemFormFolder(ItemInfo iteminfo){
		if(childItems.contains(iteminfo)){
			 childItems.remove(iteminfo);
		}
	}
}
