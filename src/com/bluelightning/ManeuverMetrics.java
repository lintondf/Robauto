package com.bluelightning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;

import com.bluelightning.GPS.Fix;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;

public class ManeuverMetrics {

	protected static class ClosestManeuver {
		ManeuverMetrics metrics;
		double   distanceFrom;
		double   distanceFromStart;
		double   distanceToEnd;
		LineSegment segment;
		double   distanceInto;
		Coordinate closestPoint;
		
		public void update(Coordinate where) {
			this.closestPoint = segment.closestPoint(where);
			GeodeticCurve curve = GPS.geoCalc.calculateGeodeticCurve(GPS.wgs84, new GlobalCoordinates(where.y, where.x),
					new GlobalCoordinates(closestPoint.y, closestPoint.x));
			this.distanceFrom = curve.getEllipsoidalDistance();
			curve = GPS.geoCalc.calculateGeodeticCurve(GPS.wgs84, new GlobalCoordinates(segment.p0.y, segment.p0.x),
					new GlobalCoordinates(closestPoint.y, closestPoint.x));
			this.distanceFromStart = distanceInto + curve.getEllipsoidalDistance();
			this.distanceToEnd = metrics.maneuver.getLength() - this.distanceFromStart;
		}
	}

	Maneuver maneuver;
	ArrayList<LineSegment> segments = new ArrayList<>();
	ArrayList<Double> distancesInto = new ArrayList<>();
	public static HashMap<Maneuver, Maneuver> nextManeuverMap = null;
	public static List<ManeuverMetrics> maneuverMetrics = null;

	public ManeuverMetrics(Maneuver maneuver) {
		this.maneuver = maneuver;
		double d = 0;
		for (int i = 1; i < maneuver.getShape().size(); i++) {
			Coordinate p1 = new Coordinate(maneuver.getShape().get(i - 1).getLongitude(),
					maneuver.getShape().get(i - 1).getLatitude());
			Coordinate p2 = new Coordinate(maneuver.getShape().get(i).getLongitude(),
					maneuver.getShape().get(i).getLatitude());
			LineSegment line = new LineSegment(p1, p2);
			segments.add(line);
			GeodeticCurve curve = GPS.geoCalc.calculateGeodeticCurve(GPS.wgs84, new GlobalCoordinates(p1.y, p1.x),
					new GlobalCoordinates(p2.y, p2.x));
			distancesInto.add(d);
			d += curve.getEllipsoidalDistance();
		}
	}

	protected static ClosestManeuver findCurrentManeuver(GeoPosition fix) {
		Coordinate where = new Coordinate(fix.getLongitude(), fix.getLatitude());
		ClosestManeuver closest = new ClosestManeuver();
		closest.distanceFrom = 7e6;
		if (maneuverMetrics == null)
			return null;
		for (ManeuverMetrics metrics : maneuverMetrics) {
			Iterator<Double> dit = metrics.distancesInto.iterator();
			for (LineSegment segment : metrics.segments) {
				double dInto = dit.next();
				double d = segment.distance(where);
				if (d < closest.distanceFrom) {
					closest.distanceInto = dInto;
					closest.distanceFrom = d;
					closest.metrics = metrics;
					closest.segment = segment;
				}
			}
		}
		closest.update(where);
		return closest;
	}

	public static void initializeMetrics( int currentDay, ArrayList<Route> days ) {
		maneuverMetrics = new ArrayList<>();
		nextManeuverMap = new HashMap<>();
		Maneuver lastManeuver = null;
		for (Leg leg : days.get(currentDay).getLeg()) {
			for (Maneuver maneuver : leg.getManeuver()) {
				List<GeoPosition> points = maneuver.getShape();
				if (!points.isEmpty()) {
					maneuverMetrics.add(new ManeuverMetrics(maneuver));
				}
				if (lastManeuver != null) {
					nextManeuverMap.put(lastManeuver, maneuver);
				}
				lastManeuver = maneuver;
			}
		}
	}
}