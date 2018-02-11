package com.bluelightning.poi;

import java.awt.Image;
import java.util.List;

import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.map.SwingMarker;

public interface POI {
	public GlobalCoordinates getCoordinates();
	public double distance( POIBase that );
	public double distance(  GeoPosition point  );
	public boolean isNearby( POIBase that, double cutoffMeters );
	public double getLatitude();
	public void setLatitude(double latitude);
	public double getLongitude();
	public void setLongitude(double longitude);
	public String getName();
	public void setName(String name);
	public Image getImage();
//	public SwingMarker getMarker();
//	public SwingMarker getMarker(String report);
	
}
