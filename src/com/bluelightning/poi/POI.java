package com.bluelightning.poi;

import java.awt.Image;
import java.io.Serializable;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

import seedu.addressbook.data.tag.UniqueTagList;

public interface POI {
	public GlobalCoordinates getCoordinates();
	public double distance( POIBase that );
	public double distance(  GeoPosition point  );
	public double roughDistance(  GeoPosition point  );
	public double roughDistance( GeoPosition seg1, GeoPosition seg2);
	public boolean isNearby( POIBase that, double cutoffMeters );
	public double getLatitude();
	public void setLatitude(double latitude);
	public double getLongitude();
	public void setLongitude(double longitude);
	public String getName();
	public void setName(String name);
	public Image getImage();
	public String getAddress();
	public UniqueTagList getTags();
	
	public static class FuelAvailable implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public static final int NO_FUEL = 0x0;
		public static final int HAS_GAS = 0x1;
		public static final int HAS_DIESEL = 0x2;
		public static final int HAS_BOTH = 0x3;
		
		private int value = NO_FUEL;
		
		public int get() { return value; }
		
		public FuelAvailable set(int value) {
			this.value = value;
			return this;
		}
		
		public FuelAvailable add(int value) {
			this.value |= value;
			return this;
		}
		
		public Boolean has( int mask ) {
			return (value & mask) == mask;
		}
	}
	
	//public enum  FuelAvailable {NO_FUEL, HAS_GAS, HAS_DIESEL, HAS_BOTH};
	public FuelAvailable getFuelAvailable();
	
}
