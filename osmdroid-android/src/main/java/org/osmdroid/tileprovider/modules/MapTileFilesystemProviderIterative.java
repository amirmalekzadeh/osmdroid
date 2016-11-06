package org.osmdroid.tileprovider.modules;

import java.io.File;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileCache;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.TileScale;

import android.graphics.drawable.Drawable;

/**
 * Implements a file system cache and provides cached tiles. This functions as a tile provider by
 * serving cached tiles for the supplied tile source.  If the requested tile does not exist, it
 * tries other zoom levels.
 *
 * @author Amir Malekzadeh
 *
 */
public class MapTileFilesystemProviderIterative extends MapTileFilesystemProvider {
	
	protected MapTileCache mTileCache = null;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileFilesystemProviderIterative(final IRegisterReceiver pRegisterReceiver,
			final MapTileCache cache) {
		super(pRegisterReceiver);
		this.mTileCache = cache;
	}

	public MapTileFilesystemProviderIterative(final IRegisterReceiver pRegisterReceiver,
			final ITileSource aTileSource, final MapTileCache cache) {
		super(pRegisterReceiver, aTileSource);
		this.mTileCache = cache;
	}

	public MapTileFilesystemProviderIterative(final IRegisterReceiver pRegisterReceiver,
			final ITileSource pTileSource, final long pMaximumCachedFileAge,
			final MapTileCache cache) {
		super(pRegisterReceiver, pTileSource, pMaximumCachedFileAge);
		this.mTileCache = cache;
	}

	@Override
	protected String getName() {
		return "File System Cache Provider Iterative";
	}

	@Override
	protected String getThreadGroupName() {
		return "filesystemiterative";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	protected class TileLoader extends MapTileFilesystemProvider.TileLoader {

		@Override
		public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException {

			ITileSource tileSource = mTileSource.get();
			if (tileSource == null) {
				return null;
			}

			final MapTile pTile = pState.getMapTile();

			// if there's no sdcard then don't do anything
			if (!getSdCardAvailable()) {
				return null;
			}
			
			Drawable cachedDrawable = mTileCache.getMapTile(pTile);
			int diff = -1;
			if (cachedDrawable != null && ExpirableBitmapDrawable.drawableHasDiff(cachedDrawable))
				diff = ExpirableBitmapDrawable.getDrawableDiff(cachedDrawable);
			
			int minZoom = tileSource.getMinimumZoomLevel();
			if (diff != -1 && pTile.getZoomLevel() - diff + 1 > minZoom)
				minZoom = pTile.getZoomLevel() - diff + 1;
			
			for (int zoom = pTile.getZoomLevel(); zoom >= minZoom && zoom + 3 >= pTile.getZoomLevel(); zoom--) {
				Drawable tile = loadZoomedTile(pTile, zoom);				
				if (tile != null) {
					return tile;
				}
			}
			
			return null;
		}
		
		protected Drawable loadZoomedTile(final MapTile pTile, int zZoomLevel) throws CantContinueException {
			if (pTile.getZoomLevel() == zZoomLevel)
				return doLoadTile(pTile);
			
			MapTile zTile = TileScale.getScaledTile(pTile, zZoomLevel);
			Drawable zDrawable = doLoadTile(zTile);
			int tileSize = mTileSource.get().getTileSizePixels();
			
			if (zDrawable == null)
				return null;
			return TileScale.scale(zDrawable, zZoomLevel, pTile, tileSize);			
		}

		protected Drawable doLoadTile(final MapTile pTile) throws CantContinueException {
			ITileSource tileSource = mTileSource.get();
			
//			Drawable cachedDrawable = mTileCache.getMapTile(pTile);
//			if (cachedDrawable != null && ! ExpirableBitmapDrawable.isDrawableExpired(cachedDrawable))
//				return cachedDrawable;

			// Check the tile source to see if its file is available and if so, then render the
			// drawable and return the tile
			File file = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE,
					tileSource.getTileRelativeFilenameString(pTile) + OpenStreetMapTileProviderConstants.TILE_PATH_EXTENSION);

			if (! file.exists()) {
				file = new File(OpenStreetMapTileProviderConstants.TILE_PATH_BASE,
						tileSource.getTileRelativeFilenameString(pTile));
			}
			
			if (file.exists()) {

				try {
					final Drawable drawable = tileSource.getDrawable(file.getPath());

					// Check to see if file has expired
					final long now = System.currentTimeMillis();
					final long lastModified = file.lastModified();
					final boolean fileExpired = lastModified < now - mMaximumCachedFileAge;

					if (fileExpired && drawable != null) {
						ExpirableBitmapDrawable.setDrawableExpired(drawable);
//						Drawable cacheTile = mTileCache.getMapTile(pTile);
//						if (cacheTile == null || (cacheTile.isStateful() && ExpirableBitmapDrawable.isDrawableExpired(cacheTile)))
//							mTileCache.putTile(pTile, drawable);
					} else if (drawable != null) {
//							mTileCache.putTile(pTile, drawable);
					}
					return drawable;
					
				} catch (final LowMemoryException e) {
					// low memory so empty the queue
					throw new CantContinueException(e);
				}
			}

			// If we get here then there is no file in the file cache
			return null;
		}
	}
}
