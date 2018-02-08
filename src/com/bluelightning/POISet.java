package com.bluelightning;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.json.BoundingBox;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Leg.CumulativeTravel;
import com.opencsv.CSVReader;


public class POISet extends ArrayList<POI> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static class POIResult implements Comparable<POIResult> {
		public POI               poi;
		public GlobalCoordinates center;
		public double            distance;
		public Maneuver          maneuver;
		public int               index;
		public CumulativeTravel progress;
		
		public String toString() {
			return String.format("POIResult: (%10.6f, %10.6f) away: %8.0f; %s; %s @ %d", 
					center.getLatitude(), center.getLongitude(),
					distance, ""+poi, ""+maneuver, index );
		}

		@Override
		public int compareTo(POIResult that) {
			return (int) (this.progress.distance - that.progress.distance);
		}
	}
	
	public Map<POI, POIResult> nearBy( GeoPosition position, int i, double rangeMeters ) {
		HashMap<POI, POIResult> neighbors = new HashMap<POI, POIResult>();
		for (POI p : this) {
			double d = p.distance(position);
			if (d <= rangeMeters) {
				POIResult result = new POIResult();
				result.poi = p;
				result.center = new GlobalCoordinates( position );
				result.distance = d;
				result.index = i;
				result.maneuver = null;
				POIResult last = neighbors.get(p);
				if (last != null) {
					if (last.distance > result.distance) {
						neighbors.replace(p, result);
					}
					continue;
				}
				neighbors.put(p, result);
			}
		}
		return neighbors;
	}
	
	public Map<POI, POIResult> nearBy( List<GeoPosition> list, double rangeMeters ) {
		HashMap<POI, POIResult> neighbors = new HashMap<POI, POIResult>();
		for (int i = 0; i < list.size(); i++) {
			GeoPosition p = list.get(i);
			neighbors.putAll( nearBy( p, i, rangeMeters ) );
		}
		return neighbors;
	}
	
	public Map<POI, POIResult> nearBy( Leg leg, double rangeMeters ) {
		HashMap<POI, POIResult> neighbors = new HashMap<POI, POIResult>();
		for (Maneuver maneuver : leg.getManeuver()) {
			Map<POI, POIResult> pList = nearBy( maneuver.getShape(), rangeMeters );
			for (POI poi : pList.keySet()) {
				POIResult result = pList.get(poi);
				result.maneuver = maneuver;
				result.progress = leg.getProgress(result);
				POIResult last = neighbors.get(maneuver);
				if (last != null) {
					if (last.distance < result.distance) {
						last.maneuver = maneuver;
						neighbors.replace(poi, last);
					} else {
						neighbors.replace(poi, result);						
					}
					continue;
				}
				neighbors.put(poi, result);
			}
		}
		return neighbors;		
	}
	
	public POISet filter( BoundingBox box ) {
		POISet trim = new POISet();
		for (POI p : this) {
			if (p.getLatitude() >= box.getBottomRight().getLatitude() && 
				p.getLatitude() <= box.getTopLeft().getLatitude() &&
				p.getLongitude() >= box.getTopLeft().getLongitude() &&
				p.getLongitude() <= box.getBottomRight().getLongitude() )
				trim.add(p);
		}
		return trim;
	}
}
