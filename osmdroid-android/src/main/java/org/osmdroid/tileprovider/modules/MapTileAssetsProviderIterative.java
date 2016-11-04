package org.osmdroid.tileprovider.modules;

import java.io.IOException;
import java.io.InputStream;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase.LowMemoryException;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.util.TileScale;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;

public class MapTileAssetsProviderIterative extends MapTileAssetsProvider {

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileAssetsProviderIterative(final IRegisterReceiver pRegisterReceiver,
			 final AssetManager pAssets,
			 final ITileSource pTileSource) {
		super(pRegisterReceiver, pAssets, pTileSource);
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	@Override
	protected String getName() {
		return "Assets Cache Provider Iterative";
	}

	@Override
	protected String getThreadGroupName() {
		return "assetsiterative";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader(mAssets);
	}

	@Override
	public void setTileSource(final ITileSource pTileSource) {
		mTileSource.set(pTileSource);
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	protected class TileLoader extends MapTileAssetsProvider.TileLoader {

		public TileLoader(AssetManager pAssets) {
			super(pAssets);
		}

		@Override
		public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException {
			ITileSource tileSource = mTileSource.get();
			if (tileSource == null) {
				return null;
			}

			final MapTile tile = pState.getMapTile();

			for (int zoom = tile.getZoomLevel(); zoom >= tileSource.getMinimumZoomLevel(); zoom--) {
				Drawable drawable = loadZoomedTile(tile, zoom);				
				if (drawable != null) 
					return drawable;
			}

			// If we get here then there is no file in the file cache
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
			
			try {
				InputStream is = mAssets.open(tileSource.getTileRelativeFilenameString(pTile));
				final Drawable drawable = tileSource.getDrawable(is);
				if (drawable != null) {
					ExpirableBitmapDrawable.setDrawableExpired(drawable);
				}
				return drawable;
			} catch (IOException e) {
			} catch (final LowMemoryException e) {
				throw new CantContinueException(e);
			}

			return null;
		}

	}
}
