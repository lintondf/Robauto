package com.bluelightning;

import java.util.ArrayList;
import java.util.List;

import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.map.ButtonWaypoint;

import slash.navigation.gpx.binding11.WptType;

public class Day {
	
	public static class Waypoint {
		public String getName() {
			return name;
		}
		public String getDesc() {
			return desc;
		}
		private String name;
		private String desc;
		
		public Waypoint( WptType wt ) {
			name = wt.getName();
			desc = wt.getDesc();
		}
		
		public Waypoint( String name, String desc ) {
			this.name = name;
			this.desc = desc;
		}
	}
	
	public void add(LatLon point, String description, double duration) {
		TrackPoint tp = new TrackPoint(point, description);
		tp.duration = duration;
		if (track.isEmpty()) {
			track.add(tp);
			trackPoints.add(tp);
		} else {
			TrackPoint last = (TrackPoint) track.get(track.size() - 1);
			if (last.getLatitude() == point.getLatitude()
					&& last.getLongitude() == point.getLongitude()) {
				if (last.duration == 0.0) {
					last.duration = tp.duration;
				}
				if (last.maneuver == null) {
					last.maneuver = tp.maneuver;
				}
				return;
			}
			GeodeticCurve curve = Garmin.geoCalc.calculateGeodeticCurve(Garmin.wgs84,
					new GlobalCoordinates(last), new GlobalCoordinates(point));
			tp.distancePriorToHere = curve.getEllipsoidalDistance();
			tp.distanceStartToHere = last.distanceStartToHere
					+ curve.getEllipsoidalDistance();
			tp.heading = curve.getAzimuth();
			track.add(tp);
			trackPoints.add(tp);
		}
	}
	
	public void addWaypoint( Waypoint wp) {
		wpts.add(wp);
	}
	
	public void addWaypoint(WptType wt) {
		wpts.add(new Waypoint(wt));
	}
	
	public List<Waypoint> wpts = new ArrayList<>();
	public ArrayList<ButtonWaypoint> waylist = new ArrayList<>();
	public List<GeoPosition> track = new ArrayList<>();
	public List<TrackPoint> trackPoints = new ArrayList<>();		
}