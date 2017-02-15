package com.xiong.launcher.alg;

import java.util.HashSet;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

public class LauncherAnimatorUtils {
	static HashSet<Animator> mAnimators = new HashSet<Animator>();
	
	static Animator.AnimatorListener mAnimatorListener = new Animator.AnimatorListener() {
		
		@Override
		public void onAnimationStart(Animator animation) {
			mAnimators.add(animation);
		}
		
		@Override
		public void onAnimationRepeat(Animator animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animator animation) {
			mAnimators.remove(animation);
		}
		
		@Override
		public void onAnimationCancel(Animator animation) {
			mAnimators.remove(animation);
		}
	};
	
	public static ValueAnimator ofFloat(View target ,float... value){
		ValueAnimator anim = new ValueAnimator();
		anim.setFloatValues(value);
		anim.addListener(mAnimatorListener);
		return anim;
		
	}
}
