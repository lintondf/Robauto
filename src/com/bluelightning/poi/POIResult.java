package com.bluelightning.poi;

import java.io.Serializable;
import java.util.ArrayList;
import org.gavaghan.geodesy.GlobalCoordinates;

import com.bluelightning.Here2;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.json.Leg.CumulativeTravel;

public class POIResult implements Comparable<POIResult>, Serializable {
	private static final long serialVersionUID = 1L;
	
	public POI poi;
	public GlobalCoordinates center;
	public double distance;
	public Leg      leg;
	public Maneuver maneuver;
	public int index;
	public CumulativeTravel legProgress;
	public CumulativeTravel totalProgress;

	public POIResult() {}
	
	public String toReport() {
		return String.format("%6.1f mi %5s / %5.1f mi %5s: %4.1f mi: %s",
				totalProgress.distance / (0.3048 * 5280.0), Here2.toPeriod(totalProgress.trafficTime),
				legProgress.distance / (0.3048 * 5280.0), Here2.toPeriod(legProgress.trafficTime),
				distance / (0.3048 * 5280.0), poi.toString());
	}

	public String toString() {
		return String.format("POIResult: %10.1f (%10.6f, %10.6f) away: %8.0f; %s; %s @ %d", totalProgress.distance / (0.3048 * 5280.0),
				center.getLatitude(),
				center.getLongitude(), distance, "" + poi, "" + maneuver.getInstruction(), index);
	}

	@Override
	public int compareTo(POIResult that) {
		return (int) (this.totalProgress.distance - that.totalProgress.distance);
	}
	
	public static POIResult factory(Route route, POI poi) {
		POISet set = new POISet();
		set.add(poi);
		ArrayList<POIResult> results = set.getPointsOfInterestAlongRoute(route, 25e3);
		if (results.isEmpty())
			return null;
		return results.get(0);
	}
	
}