/**
 * 
 */
package com.bluelightning;

import java.io.Serializable;
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
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIBase;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.RestAreaPOI;

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
	protected List<LegSummary> legSummary;
	protected TripPlan tripPlan;

	public static class LegPoint {
		public double distance;
		double trafficTime;
		double travelTime;
		public String fuelAvailability;

		public LegPoint() {
		}

		public LegPoint(LegPoint that) {
			this.distance = that.distance;
			this.trafficTime = that.trafficTime;
			this.travelTime = that.travelTime;
		}

		public void plus(Leg leg) {
			this.distance += leg.getLength();
			this.trafficTime += leg.getTrafficTime();
			this.travelTime += leg.getTravelTime();
		}

		public String toString() {
			return String.format("%5.1f / %5s", distance * Here2.METERS_TO_MILES, Here2.toPeriod(trafficTime));
		}
	}

	public static class LegSummary {
		public Leg leg;
		public LegPoint start;
		public LegPoint finish;
		public ArrayList<POIResult> nearby = new ArrayList<>();

		public LegSummary(Leg leg, LegPoint start, LegPoint finish) {
			this.leg = leg;
			this.start = start;
			this.finish = finish;
		}

		public void setNearby(ArrayList<POIResult> all) {
			for (POIResult r : all) {
				if (r.totalProgress.distance >= start.distance && r.totalProgress.distance < finish.distance) {
					nearby.add(r);
				}
			}
		}

		public String toString() {
			return String.format("%s, %s, %s", start.toString(), finish.toString(), leg.getSummary().getText());
		}
	}

	public static class DriverAssignments implements Comparable<DriverAssignments>, Serializable {

		private static final long serialVersionUID = 1L;

		public static class Assignment implements Serializable {
			private static final long serialVersionUID = 1L;

			public List<OptimizeStops.StopData> stops;
			public List<Double> driveTimes;
		}

		public double driveImbalance;
		public double[] totalDriveTimes;
		public Assignment[] assignments;
		public double score;

		public DriverAssignments(int nDrivers) {
			totalDriveTimes = new double[nDrivers];
			assignments = new Assignment[nDrivers];
			for (int i = 0; i < nDrivers; i++) {
				assignments[i] = new Assignment();
				assignments[i].stops = new ArrayList<>();
				assignments[i].driveTimes = new ArrayList<>();
			}
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(String.format("%.1f, ", score));
			sb.append(String.format("%s, ", Here2.toPeriod(driveImbalance)));
			sb.append('[');
			for (double d : totalDriveTimes) {
				sb.append(String.format("%s,", Here2.toPeriod(d)));
			}
			sb.append(']');
			sb.append("  (");
			for (Assignment a : assignments) {
				sb.append('[');
				for (double d : a.driveTimes) {
					sb.append(String.format("%s,", Here2.toPeriod(d)));
				}
				sb.append(']');
			}
			sb.append(")");
			return sb.toString();
		}

		@Override
		public int compareTo(DriverAssignments that) {
			driveImbalance = Math.abs(totalDriveTimes[0] - totalDriveTimes[1]);
			return (int) (score - that.score);
		}

	}

	public static class StopData implements Serializable {
		private static final long serialVersionUID = 1L;

		public Boolean use;
		public Boolean refuel;
		public Double distance;
		public Double trafficTime;
		public Double totalDistance;
		public String road;
		public String state;
		public String mileMarker;
		public String direction;
		public String fuelAvailable;
		public String name;
		public Double[] driveTimes;

//		public StopData(OptimizeStops.LegData legData) {
//			this.use = null;
//			this.direction = "ARRIVE";
//			this.road = "";
//			this.state = "";
//			this.mileMarker = "";
//			this.distance = legData.distance;
//			this.trafficTime = legData.trafficTime;
//			this.totalDistance = this.distance;
//			this.name = legData.endLabel;
//		}

		public StopData(POIResult result) {
			this.use = true;
			this.distance = result.legProgress.distance;
			this.trafficTime = result.legProgress.trafficTime;
			this.totalDistance = result.totalProgress.distance;
			this.fuelAvailable = POIBase.toFuelString( result.poi.getFuelAvailable() );
			this.refuel = result.poi.getFuelAvailable() == POI.FuelAvailable.HAS_GAS || result.poi.getFuelAvailable() == POI.FuelAvailable.HAS_BOTH;
			if (result.poi instanceof RestAreaPOI) {
				RestAreaPOI restArea = (RestAreaPOI) result.poi;
				this.direction = restArea.getDirection();
				this.road = restArea.getHighway();
				this.state = restArea.getState();
				this.mileMarker = restArea.getMileMarker();
				this.name = restArea.getName();
			} else {
				this.direction = "";
				this.road = result.poi.getAddress();
				this.state = "";
				this.mileMarker = "";
				this.name = result.poi.getName();
			}
		}

		public StopData(LegSummary summary) {
			this.use = true;
			this.direction = "ARRIVE";
			String[] fields = summary.leg.getEnd().getUserLabel().split("/");
			this.road = (fields.length > 0) ? fields[1] : "";
			this.state = "";
			this.mileMarker = "";
			this.distance = summary.leg.getLength();
			this.trafficTime = summary.leg.getTrafficTime();
			this.totalDistance = summary.finish.distance;
			this.name = fields[0];
			this.fuelAvailable = summary.finish.fuelAvailability;
			this.refuel = fuelAvailable.equalsIgnoreCase("Gas") || fuelAvailable.equalsIgnoreCase("Both");
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (use != null) {
				sb.append(use.toString());
				sb.append(",");
			}
			sb.append(String.format("%5.1f, %5s,", distance * Here2.METERS_TO_MILES, Here2.toPeriod(trafficTime)));
			sb.append(road);
			sb.append('/');
			sb.append(state);
			sb.append(":");
			sb.append(mileMarker);
			sb.append(",");
			sb.append(direction);
			sb.append(",");
			sb.append(name);
			sb.append(",");
			if (driveTimes != null) {
				sb.append('[');
				for (Double d : driveTimes) {
					if (d != null)
						sb.append(Here2.toPeriod(d.doubleValue()));
					sb.append(",");
				}
				sb.append(']');
			}
			return sb.toString();
		}

		public String getAddress() {
			StringBuffer sb = new StringBuffer();
			sb.append(state);
			sb.append(' ');
			sb.append(road);
			sb.append(" @ ");
			sb.append(mileMarker);
			return sb.toString();
		}
	}

	public static class RoadDirectionData implements Serializable, Comparable<RoadDirectionData> {
		private static final long serialVersionUID = 1L;

		public String road;
		public String direction;

		public String toString() {
			return String.format("%s %s", road, direction);
		}

		@Override
		public int compareTo(RoadDirectionData that) {
			return road.compareTo(that.road);
		}
	}

	public static class LegData implements Serializable {
		private static final long serialVersionUID = 1L;

		public String startLabel;
		public String endLabel;
		public Double distance;
		public Double trafficTime;

		public String toString() {
			return String.format("%5.1f %5s; from %s to %s", distance * Here2.METERS_TO_MILES,
					Here2.toPeriod(trafficTime), startLabel, endLabel);
		}
	}

	protected List<LegSummary> generateLegSummaries() {
		ArrayList<LegSummary> starts = new ArrayList<>();
		LegPoint current = new LegPoint();
		Iterator<VisitedPlace> it = tripPlan.getPlaces().iterator();
		current.fuelAvailability = POIBase.toFuelString( it.next().getFuelAvailable() );
		for (Leg leg : route.getLeg()) {
			LegPoint next = new LegPoint(current);
			next.fuelAvailability = POIBase.toFuelString( it.next().getFuelAvailable() );
			next.plus(leg);
			starts.add(new LegSummary(leg, current, next));
			current = next;
		}
		return starts;
	}

	protected LegSummary getLegSummary(Leg leg) {
		for (LegSummary summary : legSummary) {
			if (summary.leg.hashCode() == leg.hashCode()) {
				return summary;
			}
		}
		return null;
	}

	public List<OptimizeStops.LegData> getUiLegData() {
		ArrayList<OptimizeStops.LegData> dataList = new ArrayList<>();
		for (LegSummary summary : legSummary) {
			OptimizeStops.LegData data = new OptimizeStops.LegData();
			data.distance = summary.leg.getLength();
			data.startLabel = summary.leg.getStart().getLabel();
			data.endLabel = summary.leg.getEnd().getUserLabel();
			data.trafficTime = summary.leg.getTrafficTime();
			dataList.add(data);
		}
		return dataList;
	}

	public ArrayList<OptimizeStops.RoadDirectionData> getUiRoadData(int iLeg) {
		LegSummary summary = legSummary.get(iLeg);
		return getUiRoadData(summary);
	}

	public ArrayList<OptimizeStops.RoadDirectionData> getUiRoadData(LegSummary summary) {
		TreeSet<OptimizeStops.RoadDirectionData> dataList = new TreeSet<>();
		Pattern regex = Pattern.compile("onto\\s(\\w+)-(\\d+[\\w])\\s(\\w)");
		for (Maneuver m : summary.leg.getManeuver()) {
			Matcher matcher = regex.matcher(m.getInstruction());
			if (matcher.find()) {
				OptimizeStops.RoadDirectionData data = new OptimizeStops.RoadDirectionData();
				data.direction = matcher.group(3);
				data.road = matcher.group(1) + "-" + matcher.group(2);
				dataList.add(data);
			}
		}
		return new ArrayList<>(dataList);
	}

	protected boolean filterDirections(OptimizeStops.StopData data,
			List<OptimizeStops.RoadDirectionData> roadDirections) {
		for (OptimizeStops.RoadDirectionData road : roadDirections) {
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

	public ArrayList<OptimizeStops.StopData> getUiStopData(int nDrivers, int iLeg, boolean includeTruckAreas,
			List<OptimizeStops.RoadDirectionData> roadDirections) {
		LegSummary summary = legSummary.get(iLeg);
		ArrayList<OptimizeStops.StopData> dataList = new ArrayList<>();
		for (POIResult r : summary.nearby) {
			OptimizeStops.StopData data = new OptimizeStops.StopData(r);
			if (!includeTruckAreas && data.name.toUpperCase().contains("TRUCK"))
				continue;
			if (filterDirections(data, roadDirections)) {
				dataList.add(data);
			}
		}
		OptimizeStops.StopData data = new OptimizeStops.StopData(summary);
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
		updateTripPlan();
	}

	protected void updateTripPlan() {
		if (! tripPlan.getTripLegs().isEmpty())
			return;
		List<OptimizeStops.LegData> legDataList = getUiLegData();
		for (int iLeg = 0; iLeg < legDataList.size(); iLeg++) {
			OptimizeStops.LegData leg = legDataList.get(iLeg);
			TripPlan.TripLeg tripLeg = new TripPlan.TripLeg();
			tripLeg.legData = leg;
			tripLeg.roadDirectionDataList = getUiRoadData(iLeg);
			tripLeg.stopDataList = getUiStopData(TripPlan.N_DRIVERS, iLeg, false, tripLeg.roadDirectionDataList);
			ArrayList<StopData> endPoints = new ArrayList<>();
//			endPoints.add( tripLeg.stopDataList.get(0) );
			endPoints.add( tripLeg.stopDataList.get(tripLeg.stopDataList.size()-1) );
			tripLeg.driverAssignments = generateDriverAssignments(TripPlan.N_DRIVERS, tripLeg.legData, endPoints );
//					tripLeg.stopDataList);
			tripPlan.getTripLegs().add(tripLeg);
		}
	}

	public static OptimizeStops.DriverAssignments generateDriverAssignments(int nDrivers, OptimizeStops.LegData legData,
			ArrayList<OptimizeStops.StopData> stopDataList, Integer[] elements) {
		ArrayList<OptimizeStops.StopData> sublist = new ArrayList<>();
		for (Integer i : elements) {
			sublist.add(stopDataList.get(i.intValue()));
		}
		sublist.add( stopDataList.get(stopDataList.size()-1) );  // always stop at arrival point
		return generateDriverAssignments(nDrivers, legData, stopDataList, sublist);
	}

	public static OptimizeStops.DriverAssignments generateDriverAssignments(int nDrivers, OptimizeStops.LegData legData,
			ArrayList<OptimizeStops.StopData> stopDataList) {
		return generateDriverAssignments(nDrivers, legData, stopDataList, stopDataList);
	}

	public static OptimizeStops.DriverAssignments generateDriverAssignments(int nDrivers, OptimizeStops.LegData legData,
			ArrayList<OptimizeStops.StopData> stopDataList, ArrayList<OptimizeStops.StopData> sublist) {

		OptimizeStops.DriverAssignments driverAssignments = new OptimizeStops.DriverAssignments(nDrivers);
		int driver = 0;
		double lastTime = 0.0;
		double score = 0.0;
		for (OptimizeStops.StopData stopData : sublist) {
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

	public static String toHtml(int nDrivers, OptimizeStops.LegData legData,
			OptimizeStops.DriverAssignments driverAssignments) {
		Report report = new Report();
		report.add( nDrivers, legData, driverAssignments );
		return report.toHtml();
	}

}
