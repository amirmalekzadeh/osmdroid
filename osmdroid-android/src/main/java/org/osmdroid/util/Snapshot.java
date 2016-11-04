package org.osmdroid.util;

import java.util.HashMap;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IMapTileProviderCallback;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;

import android.os.Bundle;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Snapshot extends Fragment implements IMapTileProviderCallback {
	
	protected Bitmap bitmap;
	protected HashMap<MapTile, Drawable> working;
	protected Point upperLeft;
	protected MapTileProviderBase tileProvider;
	protected boolean gray;
	protected Bitmap marker;
	protected ImageView imageView = null;
	protected boolean done = false;
	protected boolean started = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		imageView = new ImageView(getActivity());
		makeGray();
		drawMarker();
		imageView.setImageBitmap(bitmap);
        return imageView;
	}
	
	public boolean isDone() {
		return done;
	}
	
	public void update() {
		makeGray();
		drawMarker();
		FragmentActivity activity = getActivity();
		if (activity == null)
			return;
		activity.runOnUiThread(new Runnable() {
		     public void run() {
		 		View view = getView();
		 		synchronized (bitmap) {

		 			if (view != null && bitmap != null) {
		 				imageView = (ImageView) view;
		 				imageView.setImageBitmap(bitmap);
		 			}
		 		}
		     }
		});
	}
	
	public Snapshot() {
		super();
	}
		
	public Snapshot(MapTileProviderBase tileProvider, GeoPoint center,
					int width, int height, int zoom, boolean gray, Bitmap marker) {
		super();
		bitmap = BitmapUtils.getBitmap(width, height);
		working = new HashMap<MapTile, Drawable>();
		this.tileProvider = tileProvider;
		this.gray = gray;
		this.marker = marker;
		
		upperLeft = TileSystem.LatLongToPixelXY(center.getLatitude(), center.getLongitude(), zoom, null);
		Point upperLeftTile = TileSystem.PixelXYToTileXY(upperLeft.x, upperLeft.y, null);
		Point lowerRightTile = TileSystem.PixelXYToTileXY(upperLeft.x + width, upperLeft.y + height, null);
		
		for (int x = upperLeftTile.x; x <= lowerRightTile.x; x++) {
			for (int y = upperLeftTile.y; y <= lowerRightTile.y; y++) {
				final MapTile tile = new MapTile(zoom, x, y);
				
				Drawable drawable = tileProvider.getMapTile(tile, this);
				if (drawable != null) {
					draw(drawable, tile);
				}
				if (drawable == null || ExpirableBitmapDrawable.isDrawableExpired(drawable)) {
					synchronized (working) {
						working.put(tile, null);
					}
				}
			}
		}
		synchronized (working) {
			if (working.isEmpty())
				done();
			else
				started = true;
		}
	}
	
	protected void draw(Drawable drawable, MapTile tile) {
		if (drawable == null || done) {
			return;
		}

		int tileSize = TileSystem.getTileSize();
		synchronized (bitmap) {			
			Canvas canvas = new Canvas(bitmap);
			Point point = TileSystem.TileXYToPixelXY(tile.getX(), tile.getY(), null);

			drawable.setBounds(point.x - upperLeft.x, point.y - upperLeft.y,
					point.x - upperLeft.x + tileSize, point.y - upperLeft.y + tileSize);
			drawable.draw(canvas);
		}
	}

	protected void done() {
		done = true;
		update();
	}
		
	private void makeGray() {
		if (! gray) {
			return;
		}
		synchronized (bitmap) {			
			Bitmap grayscaleBitmap = BitmapUtils.getBitmap(bitmap.getWidth(), bitmap.getHeight());
			Canvas c = new Canvas(grayscaleBitmap);
			Paint p = new Paint();
			ColorMatrix cm = new ColorMatrix();

			cm.setSaturation(0);
			ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
			p.setColorFilter(filter); 
			c.drawBitmap(bitmap, 0, 0, p);

			bitmap = grayscaleBitmap;
		}
	}
	
	private void drawMarker() {
		synchronized (bitmap) {	
			if (marker != null) {
				Canvas canvas = new Canvas(bitmap);
				canvas.drawBitmap(marker, (bitmap.getWidth() - marker.getWidth()) / 2,
						(bitmap.getHeight() - marker.getHeight()) / 2, null);
			}
		}
	}

	protected void removeWorking(MapTile tile) {
		synchronized (working) {
			working.remove(tile);
			if (working.isEmpty() && started)
				done();
		}
	}
	
	@Override
	public void mapTileRequestCompleted(MapTileRequestState aState, Drawable aDrawable) {
		draw(aDrawable, aState.getMapTile());
		removeWorking(aState.getMapTile());
	}

	@Override
	public void mapTileRequestFailed(MapTileRequestState aState) {
		final MapTileModuleProviderBase nextProvider = ((MapTileProviderArray) tileProvider).findNextAppropriateProvider(aState);
		if (nextProvider != null) {
			nextProvider.loadMapTileAsync(aState);
		} else {
			removeWorking(aState.getMapTile());
		}
	}

	@Override
	public void mapTileRequestExpiredTile(MapTileRequestState aState, Drawable aDrawable) {
		Drawable d;
		synchronized (working) {
			d = MapTileProviderBase.getBetterTile(working.get(aState.getMapTile()), aDrawable);
			working.put(aState.getMapTile(), d);
		}

		draw(d, aState.getMapTile());
		
		final MapTileModuleProviderBase nextProvider = ((MapTileProviderArray) tileProvider).findNextAppropriateProvider(aState);
		if (nextProvider != null) {
			nextProvider.loadMapTileAsync(aState);
		} else {
			removeWorking(aState.getMapTile());
		}
	}

	@Override
	public boolean useDataConnection() {
		return tileProvider.useDataConnection();
	}
}

