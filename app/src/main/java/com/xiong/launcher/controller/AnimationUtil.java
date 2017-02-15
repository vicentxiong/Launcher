package com.xiong.launcher.controller;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class AnimationUtil {
	private static Animation mAnimation;

	public static void loadAnimationByResourceId(Context context,int animationId){
		mAnimation = AnimationUtils.loadAnimation(context, animationId);
	}
	
	public static void startAnimation(View v){
		v.startAnimation(mAnimation);
	}
	
	public static Animation getAnimation(){
		return mAnimation;
	}
}
