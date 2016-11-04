package org.osmdroid.util;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.ReusableBitmapDrawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class TileScale {
	
	public static Drawable scale(Drawable zDrawable, int zZoomLevel, MapTile pTile, int tileSize) {
		if (zZoomLevel == pTile.getZoomLevel()) {
			return zDrawable;
		}
		
		int diff = pTile.getZoomLevel() - zZoomLevel;
		int tileSize_2 = tileSize >> diff;

		final int xx = (pTile.getX() % (1 << diff)) * tileSize_2;
		final int yy = (pTile.getY() % (1 << diff)) * tileSize_2;
		Rect mSrcRect = new Rect(xx, yy, xx + tileSize_2, yy + tileSize_2);
		Rect mDestRect = new Rect(0, 0, tileSize, tileSize);

		Bitmap bitmap = BitmapUtils.getBitmap(tileSize);
		final Canvas canvas = new Canvas(bitmap);
		final boolean isReusable = zDrawable instanceof ReusableBitmapDrawable;
		final ReusableBitmapDrawable reusableBitmapDrawable =
				isReusable ? (ReusableBitmapDrawable) zDrawable : null;
		
		if (isReusable)
			reusableBitmapDrawable.beginUsingDrawable();
		
		if (!isReusable || reusableBitmapDrawable.isBitmapValid()) {
			final BitmapDrawable bitmapDrawable = BitmapUtils.convertToBitmapDrawable(zDrawable, tileSize);
			final Bitmap oldBitmap = bitmapDrawable.getBitmap();
			canvas.drawBitmap(oldBitmap, mSrcRect, mDestRect, null);
		}
		if (isReusable)
			reusableBitmapDrawable.finishUsingDrawable();
		
		ExpirableBitmapDrawable drawable = new ReusableBitmapDrawable(bitmap);
		ExpirableBitmapDrawable.setDrawableExpired(drawable);
		ExpirableBitmapDrawable.setDrawableDiff(drawable, Math.abs(diff));
		return drawable;		
	}
	
	public static MapTile getScaledTile(MapTile tile, int zoomLevel) {
		if (tile.getZoomLevel() == zoomLevel)
			return tile;
		int diff = tile.getZoomLevel() - zoomLevel;
		return new MapTile(zoomLevel, tile.getX() >> diff, tile.getY() >> diff);
	}
}
