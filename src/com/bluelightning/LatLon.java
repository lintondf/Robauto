package com.bluelightning;

import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.json.MappedPosition;
import com.google.gson.JsonElement;

// 28.23469, -80.7532
	// 44.5228246, -68.2088976
	public class LatLon extends GeoPosition {
//		@SerializedName(value="latitude", alternate={"Latitude"})
//		protected double latitude;
//		@SerializedName(value="longitude", alternate={"Longitude"})
//		protected double longitude;
		
		public LatLon() {  }
		
		public LatLon( String parse ) {
			if (parse == null || parse.isEmpty())
				return;
			String[] coords = parse.split(",");
			if (coords.length != 2)
				return;
			latitude = Double.parseDouble(coords[0]);
			longitude = Double.parseDouble(coords[1]);
		}
		
		public LatLon(MappedPosition mappedPosition) {
			latitude = mappedPosition.getLatitude();
			longitude = mappedPosition.getLongitude();
		}

		public String toString() {
			return String.format("%10.6f, %10.6f", latitude, longitude );
		}
		public String toGeo() {
			return String.format("geo!%f,%f", latitude, longitude);
		}
		
		public GeoPosition toGeoPosition() {
			return new GeoPosition( latitude, longitude );
		}
		
		public static LatLon factory( JsonElement jelement ) {
    		return (LatLon) Here2.gson.fromJson(jelement, LatLon.class);			
		}
	}