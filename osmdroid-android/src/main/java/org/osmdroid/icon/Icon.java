package org.osmdroid.icon;

import org.osmdroid.util.GeoPoint;

public class Icon {
	private GeoPoint latLong;
	private int zoom;
	private String type;
	
	public Icon(GeoPoint latLong, int zoom, String type) {
		super();
		this.latLong = latLong;
		this.zoom = zoom;
		this.type = type;
	}
	public GeoPoint getLatLong() {
		return latLong;
	}
	public void setLatLong(GeoPoint latLong) {
		this.latLong = latLong;
	}
	public int getZoom() {
		return zoom;
	}
	public void setZoom(int zoom) {
		this.zoom = zoom;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
