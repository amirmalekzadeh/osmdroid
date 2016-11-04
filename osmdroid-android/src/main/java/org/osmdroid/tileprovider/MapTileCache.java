// Created by plusminus on 17:58:57 - 25.09.2008
package org.osmdroid.tileprovider;

import java.util.Calendar;

import org.osmdroid.icon.IconProviderBase;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

import android.graphics.drawable.Drawable;

/**
 * 
 * @author Nicolas Gramlich
 * 
 */
public class MapTileCache {
	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================

	protected final Object mCachedTilesLockObject = new Object();
	protected final Object mRequestLogLockObject = new Object();
	protected LRUMapTileCache mCachedTiles;
	protected LRURequestLog mRequestLog;
	
	protected IconProviderBase mIconProvider;

	// ===========================================================
	// Constructors
	// ===========================================================

	public MapTileCache() {
		this(OpenStreetMapTileProviderConstants.CACHE_MAPTILECOUNT_DEFAULT);
	}

	/**
	 * @param aMaximumCacheSize
	 *            Maximum amount of MapTiles to be hold within.
	 */
	public MapTileCache(final int aMaximumCacheSize) {
		this(aMaximumCacheSize, null);
	}
	
	public MapTileCache(final IconProviderBase pIconProvider) {
		this(OpenStreetMapTileProviderConstants.CACHE_MAPTILECOUNT_DEFAULT, pIconProvider);
	}
	
	public MapTileCache(final int aMaximumCacheSize, final IconProviderBase pIconProvider) {
		this.mCachedTiles = new LRUMapTileCache(aMaximumCacheSize);
		this.mRequestLog = new LRURequestLog(aMaximumCacheSize);
		this.mIconProvider = pIconProvider;
	}

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	public void ensureCapacity(final int aCapacity) {
		synchronized (mCachedTilesLockObject) {
			mCachedTiles.ensureCapacity(aCapacity);
		}
		synchronized (mRequestLogLockObject) {
			mRequestLog.ensureCapacity(aCapacity);
		}
	}

	public Drawable getMapTile(final MapTile aTile) {
		synchronized (mCachedTilesLockObject) {
			return this.mCachedTiles.get(aTile);
		}
	}
	
	public void setIconProvider(final IconProviderBase pIconProvider) {
		this.mIconProvider = pIconProvider;
	}

	public void putTile(final MapTile aTile, final Drawable aDrawable) {
		if (aDrawable != null) {
			if (mIconProvider != null) {
				mIconProvider.renderTileIcons(aTile, aDrawable);
			}
			synchronized (mCachedTilesLockObject) {
				this.mCachedTiles.put(aTile, aDrawable);
			}
		}
	}
	
	public void putRequest(final MapTile aTile) {
		synchronized (mRequestLogLockObject) {
			this.mRequestLog.put(aTile, Calendar.getInstance().getTimeInMillis());
		}
	}
	
	public boolean isTileNeeded(final MapTile aTile) {
		Long requestTime;
		synchronized (mRequestLogLockObject) {
			requestTime = this.mRequestLog.get(aTile);
		}
		long now = Calendar.getInstance().getTimeInMillis();
		if (requestTime != null && now - requestTime <= OpenStreetMapTileProviderConstants.TILE_REQUEST_TIME_LIMIT) {
			return true;
		}
		return false;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================

	// ===========================================================
	// Methods
	// ===========================================================

	public boolean containsTile(final MapTile aTile) {
		synchronized (mCachedTilesLockObject) {
			return this.mCachedTiles.containsKey(aTile);
		}
	}

	public void clear() {
		synchronized (mCachedTilesLockObject) {
			this.mCachedTiles.clear();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
