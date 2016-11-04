// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import org.osmdroid.icon.IconProviderBase;
import org.osmdroid.util.TileScale;

import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * 
 * @author Amir Malekzadeh
 * 
 */
public class MapTileCacheIterative extends MapTileCache {
	// ===========================================================
	// Constructors
	// ===========================================================
	
	public MapTileCacheIterative() {
		super();
	}
	
	public MapTileCacheIterative(final IconProviderBase pIconProvider) {
		super(pIconProvider);
	}

	public Drawable getMapTileIterative(final MapTile aTile, int tileSize, int minZoomLevel) {
		for (int zoom = aTile.getZoomLevel(); zoom >= minZoomLevel && zoom + 3 >= aTile.getZoomLevel(); zoom--) {
			Drawable drawable = getMapTile(TileScale.getScaledTile(aTile, zoom));
			if (drawable != null)
				return TileScale.scale(drawable, zoom, aTile, tileSize);
		}
		return null;
	}

	public Drawable getMapTileIterative(final MapTile aTile, int tileSize) {
		return getMapTileIterative(aTile, tileSize, 0);
	}	
}
