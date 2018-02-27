/**
 * 
 */
package com.bluelightning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.data.TripPlan;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POIBase;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.POISet;

import seedu.addressbook.data.place.VisitedPlace;

/**
 * @author NOOK
 *
 */
public class OptimizeStops {

	protected double minDrive = 45.0 * 60.0; // [s]
	protected double maxDrive = 120.0 * 60.0; // [s]
	protected double driveFuzz = 15.0 * 60.0;
	protected double nDrivers = 2;

	protected Route route;
	protected EnumMap<MarkerKinds, POISet> poiMap;
	protected List<TripPlan.LegSummary> legSummary;
	protected TripPlan tripPlan;

	protected List<TripPlan.LegSummary> generateLegSummaries() {
		ArrayList<TripPlan.LegSummary> starts = new ArrayList<>();
		TripPlan.LegPoint current = new TripPlan.LegPoint();
		Iterator<VisitedPlace> it = tripPlan.getPlaces().iterator();
		current.fuelAvailability = POIBase.toFuelString( it.next().getFuelAvailable() );
		for (Leg leg : route.getLeg()) {
			TripPlan.LegPoint next = new TripPlan.LegPoint(current);
			next.fuelAvailability = POIBase.toFuelString( it.next().getFuelAvailable() );
			next.plus(leg);
			starts.add(new TripPlan.LegSummary(leg, current, next));
			current = next;
		}
		return starts;
	}

	protected TripPlan.LegSummary getLegSummary(Leg leg) {
		for (TripPlan.LegSummary summary : legSummary) {
			if (summary.leg.hashCode() == leg.hashCode()) {
				return summary;
			}
		}
		return null;
	}
	
	public List<TripPlan.LegData> getUiLegData() {
		ArrayList<TripPlan.LegData> dataList = new ArrayList<>();
		for (TripPlan.LegSummary summary : legSummary) {
			TripPlan.LegData data = new TripPlan.LegData();
			data.distance = summary.leg.getLength();
			data.startLabel = summary.leg.getStart().getUserLabel();
			data.endLabel = summary.leg.getEnd().getUserLabel();
			data.trafficTime = summary.leg.getTrafficTime();
			dataList.add(data);
		}
		return dataList;
	}

	public ArrayList<TripPlan.RoadDirectionData> getUiRoadData(int iLeg) {
		TripPlan.LegSummary summary = legSummary.get(iLeg);
		return getUiRoadData(summary);
	}

	public ArrayList<TripPlan.RoadDirectionData> getUiRoadData(TripPlan.LegSummary summary) {
		TreeSet<TripPlan.RoadDirectionData> dataList = new TreeSet<>();
		Pattern regex = Pattern.compile("onto\\s(\\w+)-(\\d+[\\w])\\s(\\w)");
		for (Maneuver m : summary.leg.getManeuver()) {
			Matcher matcher = regex.matcher(m.getInstruction());
			if (matcher.find()) {
				TripPlan.RoadDirectionData data = new TripPlan.RoadDirectionData();
				data.direction = matcher.group(3);
				data.road = matcher.group(1) + "-" + matcher.group(2);
				dataList.add(data);
			}
		}
		return new ArrayList<>(dataList);
	}

	protected boolean filterDirections(TripPlan.StopData data,
			List<TripPlan.RoadDirectionData> roadDirections) {
		for (TripPlan.RoadDirectionData road : roadDirections) {
			if (road.road.equalsIgnoreCase(data.road)) {
				// RestAreas: NB, EB, SB, WB, NB/SB, EB/WB
				if (road.direction.equalsIgnoreCase("ANY"))
					return true;
				if (data.direction.contains("/"))
					return true;
				return data.direction.startsWith(road.direction.substring(0, 1));
			}
		}
		return true;
	}

	public ArrayList<TripPlan.StopData> getUiStopData(int nDrivers, int iLeg, boolean includeTruckAreas,
			List<TripPlan.RoadDirectionData> roadDirections) {
		TripPlan.LegSummary summary = legSummary.get(iLeg);
		ArrayList<TripPlan.StopData> dataList = new ArrayList<>();
		for (POIResult r : summary.nearby) {
			TripPlan.StopData data = new TripPlan.StopData(r);
			if (!includeTruckAreas && data.name.toUpperCase().contains("TRUCK"))
				continue;
			if (filterDirections(data, roadDirections)) {
				dataList.add(data);
			}
		}
		TripPlan.StopData data = new TripPlan.StopData(summary);
		dataList.add(data);
		return dataList;
	}

	public ArrayList<POIResult> getRouteSegmentPOI(Double start, Double finish) {
		ArrayList<POIResult> resultList = new ArrayList<>();
		poiMap.forEach((kind, pset) -> {
			resultList.addAll(pset.getPointsOfInterestAlongRoute(route, 5e3));
		});
		Iterator<POIResult> it = resultList.iterator();
		while (it.hasNext()) {
			POIResult result = it.next();
			if (result.totalProgress.distance < start || result.totalProgress.distance > finish) {
				it.remove();
			}
		}
		return resultList;
	}

	public OptimizeStops(TripPlan tripPlan, Route route, EnumMap<MarkerKinds, POISet> poiMap,
			EnumMap<Main.MarkerKinds, ArrayList<POIResult>> nearbyMap) {
		this.tripPlan = tripPlan;
		this.route = route;
		this.poiMap = poiMap;
		legSummary = generateLegSummaries();
		ArrayList<POIResult> restAreas = nearbyMap.get(Main.MarkerKinds.RESTAREAS);
		legSummary.forEach(ls -> {
			ls.setNearby(restAreas);
		});
		if (! tripPlan.getTripLegs().isEmpty())
			return;
		List<TripPlan.LegData> legDataList = getUiLegData();
		for (int iLeg = 0; iLeg < legDataList.size(); iLeg++) {
			TripPlan.LegData leg = legDataList.get(iLeg);
			TripPlan.TripLeg tripLeg = new TripPlan.TripLeg();
			tripLeg.legData = leg;
			tripLeg.roadDirectionDataList = getUiRoadData(iLeg);
			tripLeg.stopDataList = getUiStopData(TripPlan.N_DRIVERS, iLeg, false, tripLeg.roadDirectionDataList);
			ArrayList<TripPlan.StopData> endPoints = new ArrayList<>();
//			endPoints.add( tripLeg.stopDataList.get(0) );
			endPoints.add( tripLeg.stopDataList.get(tripLeg.stopDataList.size()-1) );
			tripLeg.driverAssignments = generateDriverAssignments(TripPlan.N_DRIVERS, tripLeg.legData, endPoints );
//					tripLeg.stopDataList);
			tripPlan.getTripLegs().add(tripLeg);
		}
	}

	public static TripPlan.DriverAssignments generateDriverAssignments(int nDrivers, TripPlan.LegData legData,
			ArrayList<TripPlan.StopData> stopDataList, Integer[] elements) {
		ArrayList<TripPlan.StopData> sublist = new ArrayList<>();
		for (Integer i : elements) {
			sublist.add(stopDataList.get(i.intValue()));
		}
		sublist.add( stopDataList.get(stopDataList.size()-1) );  // always stop at arrival point
		return generateDriverAssignments(nDrivers, legData, stopDataList, sublist);
	}

	public static TripPlan.DriverAssignments generateDriverAssignments(int nDrivers, TripPlan.LegData legData,
			ArrayList<TripPlan.StopData> stopDataList) {
		return generateDriverAssignments(nDrivers, legData, stopDataList, stopDataList);
	}

	public static TripPlan.DriverAssignments generateDriverAssignments(int nDrivers, TripPlan.LegData legData,
			ArrayList<TripPlan.StopData> stopDataList, ArrayList<TripPlan.StopData> sublist) {

		TripPlan.DriverAssignments driverAssignments = new TripPlan.DriverAssignments(nDrivers);
		int driver = 0;
		double lastTime = 0.0;
		double score = 0.0;
		for (TripPlan.StopData stopData : sublist) {
			//System.out.printf("%10.0f %10.0f %s\n", legData.trafficTime, stopData.trafficTime, stopData.toString());
			driverAssignments.assignments[driver].stops.add(stopData);
			double dTime = stopData.trafficTime - lastTime;
			driverAssignments.assignments[driver].driveTimes.add(dTime);
			driverAssignments.totalDriveTimes[driver] += dTime;
			score += OptimizeStops.scoreTime(dTime);
			lastTime = stopData.trafficTime;
			driver = (driver + 1) % nDrivers;
		}
		driverAssignments.driveImbalance = Math
				.abs(driverAssignments.totalDriveTimes[1] - driverAssignments.totalDriveTimes[0]);
		driverAssignments.score = score + driverAssignments.driveImbalance;
		// System.out.println(driverAssignments);
		return driverAssignments;
	}

	public TripPlan getTripPlan() {
		return tripPlan;
	}

	public void setTripPlan(TripPlan tripPlan) {
		this.tripPlan = tripPlan;
	}

	public static double scoreTime( double time ) {
		//https://en.wikipedia.org/wiki/Logistic_function
		final double MIN_TIME = 45;
		final double MAX_TIME = 120;
		final double L = 1000;
		final double k = 0.5;
		
		time /= 60;  // to minutes
		// =$B$4/(1+EXP(-$C$4*(A5-$B$2)))
		double upper = L / (1.0 + Math.exp(-k*(time - MAX_TIME))); 
		// =$B$4/(1+EXP(-$C$4*($B$1-A5)))
		double lower = L / (1.0 + Math.exp(-k*(MIN_TIME - time))); 
		return 1 + upper + lower;
	}

	public static String toHtml(int nDrivers, TripPlan.LegData legData,
			TripPlan.DriverAssignments driverAssignments) {
		Report report = new Report();
		report.add( nDrivers, legData, driverAssignments );
		return report.toHtml();
	}

}
