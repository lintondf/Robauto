package com.bluelightning.poi;

import java.awt.Image;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.map.SwingMarker;
import com.opencsv.CSVReader;

public class POIBase implements POI {
	
	protected double latitude;
	protected double longitude;
	protected double tanLatitude;
	protected double tanLongitude;
	
	protected String name;
	
	public static GeodeticCalculator geoCalc = new GeodeticCalculator();
	public static Ellipsoid wgs84 = Ellipsoid.WGS84;

	public static POISet factory( String filePath ) {
		POISet list = new POISet();
		try {
		     CSVReader reader = new CSVReader(new FileReader(filePath));
		     String [] nextLine;
		     while ((nextLine = reader.readNext()) != null) {
		        POI poi = new POIBase( nextLine );
		        list.add(poi);
		     }
			 reader.close();
		} catch (Exception e) {
			list.clear();;
		}
		 return list;
	}
	
	public POIBase() {}
	
	public POIBase( String[] fields ) {
		// long (deg), lat (deg), name+, ...
		this.setLongitude( Double.parseDouble( fields[0]) );
		this.setLatitude( Double.parseDouble( fields[1]) );
		this.setName( fields[2] );
	}
	
	@Override
	public String toString() {
		return String.format("%8.5f, %8.5f, %s", latitude, longitude, name );
	}
	
	public GlobalCoordinates getCoordinates() {
		return new GlobalCoordinates( latitude, longitude );
	}
	
	public double distance( POIBase that ) {
//		GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84, 
//				this.getCoordinates(), 
//				that.getCoordinates());
//		double d = curve.getEllipsoidalDistance();
		double lat1R = Math.toRadians(this.getCoordinates().getLatitude());
		double lat2R = Math.toRadians(that.getCoordinates().getLatitude());
		double dLatR = Math.abs(lat2R - lat1R);
		double dLngR = Math.abs(Math.toRadians(that.getCoordinates().getLongitude() - this.getCoordinates().getLongitude()));
		double a = Math.sin(dLatR / 2) * Math.sin(dLatR / 2) + Math.cos(lat1R) * Math.cos(lat2R)
				* Math.sin(dLngR / 2) * Math.sin(dLngR / 2);
		return 6378140.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}
	
	public double distance( GeoPosition point ) {
//		GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84, 
//				point,
//				this.getCoordinates());
//		double d = curve.getEllipsoidalDistance();
//		return d;
		double lat1R = Math.toRadians(this.getCoordinates().getLatitude());
		double lat2R = Math.toRadians(point.getLatitude());
		double dLatR = Math.abs(lat2R - lat1R);
		double dLngR = Math.abs(Math.toRadians(point.getLongitude() - this.getCoordinates().getLongitude()));
		double a = Math.sin(dLatR / 2) * Math.sin(dLatR / 2) + Math.cos(lat1R) * Math.cos(lat2R)
				* Math.sin(dLngR / 2) * Math.sin(dLngR / 2);
		return 6378140.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}
	
	public boolean isNearby( POIBase that, double cutoffMeters ) {
		return distance(that) <= cutoffMeters;
	}
	
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
		this.tanLatitude = Math.tan(latitude);
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
		this.tanLongitude = Math.tan(longitude);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public SwingMarker getMarker() {
		return getMarker( toString() );
	}
	
	@Override
	public SwingMarker getMarker(String report) {
		return new SwingMarker( new GeoPosition(latitude, longitude), getName(), report );
	}
//	public static void main(String[] args) {
//		List<POI> list = factory("C:\\Users\\NOOK\\GIT\\default\\RobautoFX\\POI\\RestAreasCombined_USA.csv");
////		POI p1 = list.get(0);
////		POI p2 = list.get(1);
////		System.out.println(p1);
////		System.out.println(p2);
////		double d = p1.distance(p2);
////		System.out.println( d );
////		System.out.println( p1.isNearby(p2, d ));
//		long startTime = System.nanoTime();
//		double sumd = 0;
//		for (POI p1 : list) {
//			for (POI p2 : list) {
//				if (!p1.equals(p2)) {
//					sumd += p1.distance(p2);
//				}
//			}
//		}
//		long endTime = System.nanoTime();
//		long duration = (endTime - startTime)/1000000L;  //divide to get milliseconds.
//		System.out.println( list.size() + " " + sumd );
//		System.out.println(duration);
//		System.out.println((double) duration / (double)(list.size()*list.size() - list.size()) );
//	}

}
