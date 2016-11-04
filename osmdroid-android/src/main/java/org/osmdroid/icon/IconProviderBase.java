package org.osmdroid.icon;

import java.util.List;

import microsoft.mappoint.TileSystem;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.BitmapUtils;
import org.osmdroid.util.GeoPoint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public abstract class IconProviderBase {
	
	public abstract List<Icon> getIcons(GeoPoint upperLeft, GeoPoint lowerRight, int zoom);
	
	public abstract Bitmap getIconBitmap(String iconType);
	
	public abstract int getMaxIconPixelSize();
	
	private IconCache mIconCache;
	private ITileSource mTileSource;
	
	public IconProviderBase() {
		mIconCache = new IconCache(this);
	}
	
	public void setTileSource(ITileSource pTileSource) {
		mTileSource = pTileSource;
	}
	
	public Drawable renderTileIcons(MapTile pTile, Drawable drawable) {
		if (ExpirableBitmapDrawable.areIconsRendered(drawable))
			return drawable;

		int tileSize = mTileSource.getTileSizePixels();
		Point upperLeftPx = TileSystem.TileXYToPixelXY(pTile.getX(), pTile.getY(), null, tileSize);
		Point lowerRightPx = new Point(upperLeftPx.x + tileSize, upperLeftPx.y + tileSize);
		int maxIconSize = getMaxIconPixelSize();
		
		BitmapDrawable bitmapDrawable = BitmapUtils.convertToBitmapDrawable(drawable, tileSize);
		if (! bitmapDrawable.getBitmap().isMutable()) {
			return bitmapDrawable;
		}

		List<Icon> icons = this.getIcons(TileSystem.PixelXYToLatLong(upperLeftPx.x - maxIconSize, upperLeftPx.y - maxIconSize, pTile.getZoomLevel(), null, tileSize),
				TileSystem.PixelXYToLatLong(lowerRightPx.x + maxIconSize, lowerRightPx.y + maxIconSize, pTile.getZoomLevel(), null, tileSize),
				pTile.getZoomLevel());

		if (icons == null || icons.size() == 0) {
			ExpirableBitmapDrawable.setIconsRendered(drawable);
			return drawable;
		}

		Canvas canvas = new Canvas(bitmapDrawable.getBitmap());
		Rect rect = new Rect();
		Point iconCenter = new Point();

		for (Icon icon: icons) {
			iconCenter = TileSystem.LatLongToPixelXY(icon.getLatLong().getLatitude(), icon.getLatLong().getLongitude(), pTile.getZoomLevel(), iconCenter, tileSize);
			iconCenter.set(iconCenter.x - upperLeftPx.x, iconCenter.y - upperLeftPx.y);
			Bitmap iconBitmap = mIconCache.getIcon(icon.getType());
			rect.set(iconCenter.x - iconBitmap.getWidth()/2, iconCenter.y - iconBitmap.getHeight()/2,
					iconCenter.x + iconBitmap.getWidth()/2, iconCenter.y + iconBitmap.getHeight()/2);
			canvas.drawBitmap(iconBitmap, null, rect, null);
		}

		if (ExpirableBitmapDrawable.isDrawableExpired(drawable))
			ExpirableBitmapDrawable.setDrawableExpired(bitmapDrawable);
		if (ExpirableBitmapDrawable.drawableHasDiff(drawable))
			ExpirableBitmapDrawable.setDrawableDiff(bitmapDrawable, ExpirableBitmapDrawable.getDrawableDiff(drawable));
		ExpirableBitmapDrawable.setIconsRendered(bitmapDrawable);
		return bitmapDrawable;
	}

}
