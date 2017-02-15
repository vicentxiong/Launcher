package com.xiong.launcher.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class IconBitmapDrawable extends Drawable {
	private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
	private Bitmap bitmap;
	
	public IconBitmapDrawable(Bitmap b){
		bitmap = b;
	}

	@Override
	public void draw(Canvas canvas) {
		Rect r = getBounds();
		canvas.drawBitmap(bitmap, null, r, mPaint);

	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		

	}

	@Override
	public int getOpacity() {
		
		return PixelFormat.TRANSLUCENT;
	}

}
