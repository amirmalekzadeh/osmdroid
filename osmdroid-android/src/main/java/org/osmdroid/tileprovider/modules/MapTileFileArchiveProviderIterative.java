// Created by plusminus on 21:46:41 - 25.09.2008
package org.osmdroid.tileprovider.modules;

import java.io.InputStream;

import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.util.StreamUtils;
import org.osmdroid.util.TileScale;

import android.graphics.drawable.Drawable;

/**
 * A tile provider that can serve tiles from an archive using the supplied tile source. The tile
 * provider will automatically find existing archives and use each one that it finds. If the
 * requested tile does not exist, it tries other zoom levels.
 *
 * @author Amir Malekzadeh
 *
 */
public class MapTileFileArchiveProviderIterative extends MapTileFileArchiveProvider {

	// ===========================================================
	// Constructors
	// ===========================================================

	/**
	 * The tiles may be found on several media. This one works with tiles stored on the file system.
	 * It and its friends are typically created and controlled by {@link MapTileProviderBase}.
	 */
	public MapTileFileArchiveProviderIterative(final IRegisterReceiver pRegisterReceiver,
			final ITileSource pTileSource, final IArchiveFile[] pArchives) {
		super(pRegisterReceiver, pTileSource, pArchives);
	}

	public MapTileFileArchiveProviderIterative(final IRegisterReceiver pRegisterReceiver,
			final ITileSource pTileSource) {
		super(pRegisterReceiver, pTileSource);
	}

	@Override
	protected String getName() {
		return "File Archive Provider Iterative";
	}

	@Override
	protected String getThreadGroupName() {
		return "filearchiveiterative";
	}

	@Override
	protected Runnable getTileLoader() {
		return new TileLoader();
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	protected class TileLoader extends MapTileFileArchiveProvider.TileLoader {
		
		@Override
		public Drawable loadTile(final MapTileRequestState pState) {

			ITileSource tileSource = mTileSource.get();
			if (tileSource == null) {
				return null;
			}

			final MapTile pTile = pState.getMapTile();

			if (!isSdCardAvailable()) {
				return null;
			}
			
			for (int zoom = pTile.getZoomLevel(); zoom >= tileSource.getMinimumZoomLevel(); zoom--) {
				Drawable tile = loadZoomedTile(pTile, zoom);				
				if (tile != null) 
					return tile;
			}
			return null;
		}
		
		protected Drawable loadZoomedTile(final MapTile pTile, int zZoomLevel) {
			if (pTile.getZoomLevel() == zZoomLevel)
				return doLoadTile(pTile);
			
			MapTile zTile = TileScale.getScaledTile(pTile, zZoomLevel);
			Drawable zDrawable = doLoadTile(zTile);
			int tileSize = mTileSource.get().getTileSizePixels();
			
			if (zDrawable == null)
				return null;
			return TileScale.scale(zDrawable, zZoomLevel, pTile, tileSize);			
		}

		protected Drawable doLoadTile(final MapTile pTile) {
			ITileSource tileSource = mTileSource.get();

			InputStream inputStream = null;
			try {

				inputStream = getInputStream(pTile, tileSource);
				if (inputStream != null) {
					final Drawable drawable = tileSource.getDrawable(inputStream);
					return drawable;
				}
			} catch (final Throwable e) {
			} finally {
				if (inputStream != null) {
					StreamUtils.closeStream(inputStream);
				}
			}
			
			return null;
		}
	}
}
