package com.bluelightning.poi;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.SwingMarker;
import com.bluelightning.poi.POI.FuelAvailable;
import com.opencsv.CSVReader;

import seedu.addressbook.data.tag.UniqueTagList;

public abstract class POIBase implements POI {
	
	protected double latitude;
	protected double longitude;
	protected double tanLatitude;
	protected double tanLongitude;
	protected static Image  image;
	
	protected String name;
	protected UniqueTagList tagList = new UniqueTagList();
	
	public static GeodeticCalculator geoCalc = new GeodeticCalculator();
	public static Ellipsoid wgs84 = Ellipsoid.WGS84;

	static {
		if (image == null) try {
			Dimension size = ButtonWaypoint.getImageSize();
			image = ImageIO.read(new File("images/restarea.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {
		}		
	}
	
//	public static POISet factory( String filePath ) {
//		POISet list = new POISet();
//		try {
//		     CSVReader reader = new CSVReader(new FileReader(filePath));
//		     String [] nextLine;
//		     while ((nextLine = reader.readNext()) != null) {
//		        POI poi = new POIBase( nextLine );
//		        list.add(poi);
//		     }
//			 reader.close();
//		} catch (Exception e) {
//			list.clear();;
//		}
//		 return list;
//	}
	
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
		GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84, 
				new GlobalCoordinates(point.getLatitude(), point.getLongitude()),
				this.getCoordinates());
		double d = curve.getEllipsoidalDistance();
		return d;
	}
	
	/**
	 * Wrapper function to accept the same arguments as the other examples
	 * 
	 * @param x3 - point x
	 * @param y3 - point y
	 * @param x1 - segment start x
	 * @param y1
	 * @param x2 - segment end x
	 * @param y2
	 * @return
	 */
	public static double distanceToSegment(double x3, double y3, double x1, double y1, double x2, double y2) {
		final Point2D p3 = new Point2D.Double(x3, y3);
		final Point2D p1 = new Point2D.Double(x1, y1);
		final Point2D p2 = new Point2D.Double(x2, y2);
		return distanceToSegment(p1, p2, p3);
	}

	/**
	 * Returns the distance of p3 to the segment defined by p1,p2;
	 * 
	 * @param p1
	 *            First point of the segment
	 * @param p2
	 *            Second point of the segment
	 * @param p3
	 *            Point to which we want to know the distance of the segment
	 *            defined by p1,p2
	 * @return The distance of p3 to the segment defined by p1,p2
	 */
	public static double distanceToSegment(Point2D p1, Point2D p2, Point2D p3) {

		final double xDelta = p2.getX() - p1.getX();
		final double yDelta = p2.getY() - p1.getY();

		if ((xDelta == 0) && (yDelta == 0)) {
			throw new IllegalArgumentException("p1 and p2 cannot be the same point");
		}

		final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta)
				/ (xDelta * xDelta + yDelta * yDelta);

		final Point2D closestPoint;
		if (u < 0) {
			closestPoint = p1;
		} else if (u > 1) {
			closestPoint = p2;
		} else {
			closestPoint = new Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
		}

		return closestPoint.distance(p3);
	}
	
	public double roughDistance( GeoPosition seg1, GeoPosition seg2) {
		double distDeg = distanceToSegment( this.getLongitude(), this.getLatitude(),
				seg1.getLongitude(), seg1.getLatitude(), seg2.getLongitude(), seg2.getLatitude() );
		return 6378140.0 * Math.toRadians( distDeg );
	}

	public double roughDistance( GeoPosition point) {
		double dLat = this.getLatitude() - point.getLatitude();
		double dLon = this.getLongitude() - point.getLongitude();
		return 6378140.0 * Math.toRadians( Math.sqrt(dLat*dLat + dLon*dLon) );
//		double lat1R = Math.toRadians(this.getCoordinates().getLatitude());
//		double lat2R = Math.toRadians(point.getLatitude());
//		double dLatR = Math.abs(lat2R - lat1R);
//		double dLngR = Math.abs(Math.toRadians(point.getLongitude() - this.getCoordinates().getLongitude()));
//		double a = Math.sin(dLatR / 2) * Math.sin(dLatR / 2) + Math.cos(lat1R) * Math.cos(lat2R)
//				* Math.sin(dLngR / 2) * Math.sin(dLngR / 2);
//		return 6378140.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
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
	public Image getImage() {
		return image;
	}

	@Override
	public FuelAvailable getFuelAvailable() {
		return FuelAvailable.NO_FUEL;
	}
}
