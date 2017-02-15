package com.xiong.launcher.alg;

import com.xiong.launcher.R;
import com.xiong.launcher.ui.IconBitmapDrawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.DisplayMetrics;

public class Utils {
	private static final Canvas sCanvas = new Canvas();
	private static final Rect sOldBounds = new Rect();
	
    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    public static int sIconTextureWidth = -1;
    public static int sIconTextureHeight = -1;
    
    /**
     * Returns a FastBitmapDrawable with the icon, accurately sized.
     */
    public static Drawable createIconDrawable(Bitmap icon) {
    	return createIconDrawable(icon, sIconTextureWidth, sIconTextureHeight);
    }
    
    public static Drawable createIconDrawable(Bitmap icon,int realwidth,int realheight){
        IconBitmapDrawable d = new IconBitmapDrawable(icon);
        d.setFilterBitmap(true);
        resizeIconDrawable(d,realwidth,realheight);
        return d;
    }

    /**
     * Resizes an icon drawable to the correct icon size.
     */
    static void resizeIconDrawable(Drawable icon,int width,int height) {
        icon.setBounds(0, 0, width, height);
    }
	
	public static Bitmap createIconBitmap(Drawable icon, Context context) {
        return createIconBitmap(icon, sIconTextureWidth, sIconTextureHeight, context);
    }
	
	public static Bitmap createIconBitmap(Bitmap src,int realwidth,int realheight,Context context){
		final Bitmap b = Bitmap.createScaledBitmap(src, realwidth, realheight, false);
		return b;
	}
	
	public static Bitmap createIconBitmap(Drawable icon, int realwidth,int realheight, Context context){
		synchronized (sCanvas) { 
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = realwidth;
            int height = realheight;

            if (icon instanceof PaintDrawable) {
                PaintDrawable painter = (PaintDrawable) icon;
                painter.setIntrinsicWidth(width);
                painter.setIntrinsicHeight(height);
            } else if (icon instanceof BitmapDrawable) {
                // Ensure the bitmap has a density.
                BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                    bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
                }
            }
            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // Scale the icon proportionally to the icon dimensions
                final float ratio = (float) sourceWidth / sourceHeight;
                if (sourceWidth > sourceHeight) {
                    height = (int) (width / ratio);
                } else if (sourceHeight > sourceWidth) {
                    width = (int) (height * ratio);
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = realwidth;
            int textureHeight = realheight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);
            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            sOldBounds.set(icon.getBounds());
            icon.setBounds(left, top, left+width, top+height);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            canvas.setBitmap(null);

            return bitmap;
        }
	}
	
    public static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;

    }
    
    public static void setIconSize(int widthPx) {
        sIconWidth = sIconHeight = widthPx;
        sIconTextureWidth = sIconTextureHeight = widthPx;
    }
    
    public static String getStringFormResById(Context c,int resId){
    	Resources res = c.getResources();
    	return res.getString(resId);
    }

}
