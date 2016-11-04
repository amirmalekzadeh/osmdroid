package org.osmdroid.icon;

import java.util.HashMap;

import android.graphics.Bitmap;

public class IconCache {
	private static final int DEFAULT_CACHE_CAPACITY = 20;
	private IconProviderBase mIconProvider;
	private HashMap<String, Bitmap> mCache;
	
	public IconCache(IconProviderBase pIconProvider) {
		this(pIconProvider, DEFAULT_CACHE_CAPACITY);
	}
	
	public IconCache(IconProviderBase pIconProvider, int cacheCapacity) {
		this.mIconProvider = pIconProvider;
		this.mCache = new HashMap<String, Bitmap>(cacheCapacity);
	}

	public Bitmap getIcon(String type) {
		if (mCache.containsKey(type))
			return mCache.get(type);
		else {
			Bitmap bitmap = mIconProvider.getIconBitmap(type);
			mCache.put(type, bitmap);
			return bitmap;
		}
	}
}
