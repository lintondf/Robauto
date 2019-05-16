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

	public static class ClosestManeuver {
		public ManeuverMetrics metrics;
		public double   distanceFrom;  // sideways displacement
		public double   distanceFromStart; // from start of maneuver
		public double   distanceToEnd;     // to end of maneuver
		public LineSegment segment;
		public double   distanceInto;      // from start of route to start of maneuver
		public Coordinate closestPoint;    // nearest point on maneuver to current position
		
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

	public Maneuver maneuver;
	public double   totalDistance;
	public double   totalTime;
	ArrayList<LineSegment> segments = new ArrayList<>();
	ArrayList<Double> distancesInto = new ArrayList<>();  // from start of maneuver to start of corresponding segment
	
	public static HashMap<Maneuver, Maneuver> nextManeuverMap = null;
	public static List<ManeuverMetrics> maneuverMetrics = null;
	
	@Override
	public String toString() {
		double from = 0.0;
		double to = 0.0;
		if (! distancesInto.isEmpty()) {
			int n = distancesInto.size()-1;
			from = distancesInto.get(0);
			to = distancesInto.get(n);
		}
		return String.format("%10.0f %s %d %10.0f %10.0f %s", totalDistance, maneuver.getId(), segments.size(), from, to, maneuver.getInstruction() );
	}

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
		double totalDistance = 0.0;
		double totalTime = 0.0;
		for (Leg leg : days.get(currentDay).getLeg()) {
			for (Maneuver maneuver : leg.getManeuver()) {
				List<GeoPosition> points = maneuver.getShape();
				if (!points.isEmpty()) {
					ManeuverMetrics mm = new ManeuverMetrics(maneuver);
					mm.totalDistance = totalDistance;
					mm.totalTime = totalTime;
					maneuverMetrics.add(mm);
				}
				if (lastManeuver != null) {
					nextManeuverMap.put(lastManeuver, maneuver);
				}
				totalDistance += maneuver.getLength();
				totalTime += maneuver.getTravelTime();
				lastManeuver = maneuver;
			}
		}
		maneuverMetrics.forEach(System.out::println);
	}
}