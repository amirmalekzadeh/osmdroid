package org.osmdroid.tileprovider;

import java.util.LinkedHashMap;

import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;

public class LRURequestLog extends LinkedHashMap<MapTile, Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5669273195607548636L;
	private int mCapacity;

	public LRURequestLog(final int aCapacity) {
		super(aCapacity + 2, 0.1f, true);
		mCapacity = aCapacity;
	}

	public void ensureCapacity(final int aCapacity) {
		if (aCapacity > mCapacity) {
			mCapacity = aCapacity;
		}
	}
	
	@Override
	protected boolean removeEldestEntry(
			java.util.Map.Entry<MapTile, Long> eldest) {
		return size() > mCapacity;
	}
	
	
}
