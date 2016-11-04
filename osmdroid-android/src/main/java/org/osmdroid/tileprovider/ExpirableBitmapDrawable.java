package org.osmdroid.tileprovider;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * A {@link BitmapDrawable} for a {@link MapTile} that has a state to indicate that it's expired.
 */
public class ExpirableBitmapDrawable extends BitmapDrawable {

	private static final int EXPIRED = -1;
	private static final int ICONS_RENDERED = -3;

	private int[] mState;

	public ExpirableBitmapDrawable(final Bitmap pBitmap) {
		super(pBitmap);
		mState = new int[0];
	}

	@Override
	public int[] getState() {
		return mState;
	}

	@Override
	public boolean isStateful() {
		return mState.length > 0;
	}

	@Override
	public boolean setState(final int[] pStateSet) {
		mState = pStateSet;
		return true;
	}

	public static boolean isDrawableExpired(final Drawable pTile) {
		if (!pTile.isStateful()) {
			return false;
		}
		final int[] state = pTile.getState();
		for(int i = 0; i < state.length; i++) {
			if (state[i] == EXPIRED) {
				return true;
			}
		}
		return false;
	}
	
	public static void setDrawableExpired(final Drawable pTile) {
		if (drawableHasDiff(pTile))
			pTile.setState(new int[]{ExpirableBitmapDrawable.EXPIRED, getDrawableDiff(pTile)});
		else
			pTile.setState(new int[]{ExpirableBitmapDrawable.EXPIRED});
	}
	
	public static void setDrawableDiff(final Drawable pTile, final int diff) {
		int exp = isDrawableExpired(pTile) ? EXPIRED : 0;
		pTile.setState(new int[]{exp, diff});
	}
	
	public static boolean drawableHasDiff(final Drawable pTile) {
		return pTile.getState().length > 1;
	}

	public static int getDrawableDiff(final Drawable pTile) {
		if (pTile.getState().length < 2)
			return 0;
		return pTile.getState()[1];
	}

	public static void setIconsRendered(final Drawable pTile) {
		int exp = isDrawableExpired(pTile) ? EXPIRED : 0;
		int diff = getDrawableDiff(pTile);
		pTile.setState(new int[]{exp, diff, ICONS_RENDERED});
	}
		
	public static boolean areIconsRendered(final Drawable pTile) {
		if (pTile.getState().length < 3)
			return false;
		return pTile.getState()[2] == ICONS_RENDERED;
	}

}
