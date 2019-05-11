package com.bluelightning.poi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.RobautoMain;
import com.bluelightning.json.BoundingBox;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.json.Leg.CumulativeTravel;

public class POISet extends ArrayList<POI> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Map<POI, POIResult> nearBy(GeoPosition position, int i, double rangeMeters) {
		HashMap<POI, POIResult> neighbors = new HashMap<POI, POIResult>();
		for (POI p : this) {
			double d = p.roughDistance(position);
			if (d <= rangeMeters) {
				POIResult result = new POIResult();
				result.poi = p;
				result.center = new GlobalCoordinates(position);
				result.distance = d;
				result.index = i;
				result.maneuver = null;
				result.leg = null;
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
		for (POIResult r : neighbors.values()) {
			r.distance = r.poi.distance(r.center);
		}
		return neighbors;
	}

	public Map<POI, POIResult> nearBy(GeoPosition seg1, GeoPosition seg2, int i, double rangeMeters) {
		HashMap<POI, POIResult> neighbors = new HashMap<POI, POIResult>();
		for (POI p : this) {
			double d = p.roughDistance(seg1, seg2);
			if (d <= rangeMeters) {
				POIResult result = new POIResult();
				result.poi = p;
				result.center = new GlobalCoordinates(seg1);
				result.distance = d;
				result.index = i;
				result.maneuver = null;
				result.leg = null;
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

	
	public Map<POI, POIResult> nearBy(List<GeoPosition> list, double rangeMeters) {
		HashMap<POI, POIResult> neighbors = new HashMap<POI, POIResult>();
		for (int i = 1; i < list.size(); i++) {
			GeoPosition p0 = list.get(i-1);
			GeoPosition p1 = list.get(i);
			neighbors.putAll(nearBy(p0, p1, i-1, rangeMeters));
		}
		return neighbors;
	}

	public static void contains(String where, Collection<POI> collection ) {
		for (POI p : collection) {
			if (p.getName().startsWith("DILLON")) {
				RobautoMain.logger.debug(String.format("%s contains %s", where, p.toString() ));
			}
		}
	}

	public static void contains1(String where, Collection<ArrayList<POIResult>> collection ) {
		for (ArrayList<POIResult>  results : collection) {
			for (POIResult r : results) {
				if (r.poi.getName().startsWith("DILLON")) {
					RobautoMain.logger.debug(String.format("%s contains %s", where, r.toString() ));
				}
			}
		}
	}
	
	public static void containsPOIResult(String where, Collection<POIResult> collection ) {
		for (POIResult p : collection) {
			if (p.poi.getName().startsWith("DILLON")) {
				RobautoMain.logger.debug(String.format("%s contains %s", where, p.toString() ));
			}
		}
	}

	public Map<POI, POIResult> nearBy(Leg leg, CumulativeTravel legStart, double rangeMeters) {
		contains("nearBy1", this );
		HashMap<POI, POIResult> neighbors = new HashMap<POI, POIResult>();
		RobautoMain.logger.debug( String.format("POISet::nB %s M%d\n", leg.getStart().toString(), leg.getManeuver().size() ));
		for (Maneuver maneuver : leg.getManeuver()) {
			RobautoMain.logger.debug( String.format("POISet::nB M %s %d, %10.0f, %10.0f, %10.1f", maneuver.getId(), maneuver.getShape().size(),
					maneuver.getFirstPoint(), maneuver.getLastPoint(), maneuver.getLength() ));
			Map<POI, POIResult> pList = nearBy(maneuver.getShape(), rangeMeters);
			for (POI poi : pList.keySet()) {
				POIResult result = pList.get(poi);
				result.maneuver = maneuver;
				result.leg = leg;
				result.legProgress = leg.getProgress(result);
				result.totalProgress = legStart.plus(result.legProgress);
//				POIResult last = neighbors.get(maneuver);
//				if (last != null) {
//					if (last.distance < result.distance) {
//						last.maneuver = maneuver;
//						neighbors.replace(poi, last);
//					} else {
//						neighbors.replace(poi, result);
//					}
//					continue;
//				}
				neighbors.put(poi, result);
				RobautoMain.logger.debug(poi.toString());
			}
		}
		contains("nearBy2", neighbors.keySet() );
		return neighbors;
	}

	public POISet filter(BoundingBox box) {
//		RobautoMain.logger.debug(String.format("filter %10.6f, %10.6f  %10.6f, %10.6f", 
//				box.getTopLeft().getLatitude(),box.getTopLeft().getLongitude(),
//				box.getBottomRight().getLatitude(), box.getBottomRight().getLongitude() ));
		POISet trim = new POISet();
		for (POI p : this) {
			if (p.getLatitude() >= box.getBottomRight().getLatitude()
					&& p.getLatitude() <= box.getTopLeft().getLatitude()
					&& p.getLongitude() >= box.getTopLeft().getLongitude()
					&& p.getLongitude() <= box.getBottomRight().getLongitude()) {
				trim.add(p);
			}
		}
		//System.out.printf("POISet::filter %d -> %d\n", this.size(), trim.size() );
		//trim.forEach(System.out::println);
		return trim;
	}

	public ArrayList<POIResult> getPointsOfInterestAlongRoute(Route route, double radiusMeters) {
		POISet pset = filter(route.getBoundingBox());
		contains( "gPOIAR ", this );
		ArrayList<POIResult> output = new ArrayList<>();
		CumulativeTravel totalProgress = new CumulativeTravel();
		//System.out.printf("POISet::gPOIAR %d\n", route.getLeg().size());
		for (Leg leg : route.getLeg()) {
			RobautoMain.logger.debug( String.format("  gPPOAR %s %s", leg.getStart().getUserLabel(), totalProgress.toString() ) );
			Map<POI, POIResult> nearby = pset.nearBy(leg, totalProgress, radiusMeters);
			ArrayList<POIResult> byManeuver = new ArrayList<POIResult>();
			for (Entry<POI, POIResult> e : nearby.entrySet()) {
				byManeuver.add(e.getValue());
			}
			Collections.sort(byManeuver);
			for (POIResult r : byManeuver) {
				output.add(r);
			}
			totalProgress.distance += leg.getLength();
			totalProgress.trafficTime += leg.getTrafficTime();
			totalProgress.travelTime += leg.getTravelTime();
		}
		return output;
	}
}
