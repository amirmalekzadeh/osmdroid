package org.osmdroid.util;

import org.osmdroid.tileprovider.BitmapPool;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class BitmapUtils {
	public static BitmapDrawable convertToBitmapDrawable (Drawable drawable, int size) {
		if (drawable instanceof BitmapDrawable) 
			return (BitmapDrawable) drawable;
		
		Bitmap bitmap = getBitmap(size);
	    Canvas canvas = new Canvas(bitmap);
	    drawable.setBounds(0, 0, size, size);
	    drawable.draw(canvas);
	    
	    return new ReusableBitmapDrawable(bitmap);
	}
	
	public static Bitmap getBitmap(int size) {
		return getBitmap(size, size);
	}	

	public static Bitmap getBitmap(int width, int height) {
		Bitmap bitmap = BitmapPool.getInstance().obtainSizedBitmapFromPool(width, height);

		if (bitmap != null)
			return bitmap;
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}	
}
