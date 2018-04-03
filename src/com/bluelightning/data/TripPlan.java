package com.bluelightning.data;

import java.awt.FlowLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingUtilities;

import com.bluelightning.GeodeticPosition;
import com.bluelightning.Here2;
import com.bluelightning.OptimizeStops;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIBase;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.RestAreaPOI;

import javafx.util.Pair;

import com.bluelightning.Report;
import com.bluelightning.RobautoMain;
import com.bluelightning.TripPlanUpdate;

import seedu.addressbook.data.place.VisitedPlace;

public class TripPlan implements Comparable<TripPlan>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final int N_DRIVERS = 2;

	protected Date lastModified;

	protected Boolean placesChanged;

	protected ArrayList<VisitedPlace> places;

	protected Route route;

	protected ArrayList<TripLeg> tripLegs;

	protected ArrayList<LegData> legDataList;

	protected List<TripPlan.LegSummary> legSummary;

	protected ArrayList<Route> finalizedDays;

	protected ArrayList<Integer> finalizedMarkers;

	protected ArrayList<ArrayList<VisitedPlace>> finalizedPlaces;
	
	public void tripPlanUpdated(String savePath) {
		try {
		    Registry registry = LocateRegistry.getRegistry("192.168.0.13", RobautoMain.REGISTRY_PORT);
		    TripPlanUpdate stub = (TripPlanUpdate) registry.lookup("Update");
		    String response = stub.update(savePath);
		    System.out.println("tripPlanUpdated::response: " + response);
		} catch (Exception e) {
		    System.err.println("Client exception: " + e.toString());
		    e.printStackTrace();
		}
	}
	

	public void clear(String action) {
		switch (action) {
		case "Normal":
			break;
		case "Clear Places":
			this.places = new ArrayList<>();
			this.placesChanged = true;
		case "Clear Route":
			route = null;
			tripLegs = new ArrayList<>();
			legDataList = new ArrayList<>();
			legSummary = new ArrayList<>();
			finalizedDays = new ArrayList<>();
			finalizedMarkers = new ArrayList<>();
			break;
		default:
			break;
		}
		route = null;
	}

	public static class TripLeg implements Serializable {
		private static final long serialVersionUID = 1L;

		public TripPlan.LegData legData;
		public ArrayList<TripPlan.RoadDirectionData> roadDirectionDataList;
		public ArrayList<TripPlan.StopData> stopDataList;
		public DriverAssignments driverAssignments;

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(" TripLeg\n");
			sb.append("    legData: " + legData.toString());
			sb.append('\n');
			sb.append(String.format("   roadDirectionDataList (%d)\n", roadDirectionDataList.size()));
			for (RoadDirectionData data : this.roadDirectionDataList) {
				sb.append("     " + data.toString() + "\n");
			}
			sb.append(String.format("   stopDataList (%d)\n", stopDataList.size()));
			for (StopData stopData : this.stopDataList) {
				sb.append("      " + stopData.toString() + "\n");
			}

			sb.append("    driverAssignments: " + this.driverAssignments.toString());
			return sb.toString();
		}
	}

	private static class LegPoint implements Serializable {
		private static final long serialVersionUID = 1L;

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

	public static class LegSummary implements Serializable, GeodeticPosition {
		private static final long serialVersionUID = 1L;

		public double latitude;
		public double longitude;
		public String[] instructions;
		public String startUserLabel;
		public String endUserLabel;
		public Double length;
		public Double trafficTime;
		public LegPoint start;
		public LegPoint finish;
		public ArrayList<POIResult> nearby = new ArrayList<>();
		
		public LegSummary() {
			startUserLabel = "";
			endUserLabel = "";
			instructions = new String[0];
		}

		public LegSummary(Leg leg, LegPoint start, LegPoint finish) {
			latitude = leg.getEnd().getMappedPosition().getLatitude();
			longitude = leg.getEnd().getMappedPosition().getLongitude();
			instructions = new String[leg.getManeuver().size()];
			Iterator<Maneuver> it = leg.getManeuver().iterator();
			for (int i = 0; i < leg.getManeuver().size(); i++) {
				instructions[i] = it.next().getInstruction();
			}
			startUserLabel = leg.getStart().getUserLabel();
			endUserLabel = leg.getEnd().getUserLabel();
			length = leg.getLength();
			trafficTime = leg.getTrafficTime();
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

		public String getId() {
			return String.format("[%s][%s]", this.startUserLabel, this.endUserLabel);
		}

		public String toString() {
			return String.format("%s, %s, %s to %s", start.toString(), finish.toString(), this.startUserLabel,
					this.endUserLabel);
		}

		@Override
		public double getLatitude() {
			return latitude;
		}

		@Override
		public double getLongitude() {
			return longitude;
		}
	}

	public static class StopData implements Serializable, GeodeticPosition {
		private static final long serialVersionUID = 1L;

		public double latitude;
		public double longitude;
		public Boolean use;
		public Boolean refuel;
		public Boolean drivers; // true if switch drivers; or consider it
		public Double distance;
		public Double trafficTime;
		public Double totalDistance;
		public String road;
		public String state;
		public String mileMarker;
		public String direction;
		public String fuelAvailable;
		public String name;

		public StopData(POIResult result) {
			this.latitude = result.poi.getLatitude();
			this.longitude = result.poi.getLongitude();
			this.use = true;
			this.drivers = true;
			this.distance = result.legProgress.distance;
			this.trafficTime = result.legProgress.trafficTime;
			this.totalDistance = result.totalProgress.distance;
			this.fuelAvailable = POIBase.toFuelString(result.poi.getFuelAvailable());
			this.refuel = result.poi.getFuelAvailable().has(POI.FuelAvailable.HAS_GAS);
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
			this.latitude = summary.getLatitude();
			this.longitude = summary.getLongitude();
			this.use = true;
			this.drivers = true;
			this.direction = "ARRIVE";
			String[] fields = summary.endUserLabel.split("/");
			this.road = (fields.length > 0) ? fields[1] : "";
			this.state = "";
			this.mileMarker = "";
			this.distance = summary.length;
			this.trafficTime = summary.trafficTime;
			this.totalDistance = summary.finish.distance;
			this.name = fields[0];
			this.fuelAvailable = summary.finish.fuelAvailability;
			this.refuel = fuelAvailable.equalsIgnoreCase("Gas") || fuelAvailable.equalsIgnoreCase("Both");
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (use != null) {
				sb.append(use.toString());
			}
			sb.append(",");
			if (drivers != null) {
				sb.append(drivers.toString());
			}
			sb.append(",");
			if (refuel != null) {
				sb.append(refuel.toString());
			}
			sb.append(",");
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
			// if (driveTimes != null) {
			// sb.append('[');
			// for (Double d : driveTimes) {
			// if (d != null)
			// sb.append(Here2.toPeriod(d.doubleValue()));
			// sb.append(",");
			// }
			// sb.append(']');
			// }
			return sb.toString();
		}

		public String getAddress() {
			StringBuffer sb = new StringBuffer();
			sb.append(state);
			sb.append(' ');
			sb.append(road);
			if (mileMarker != null && !mileMarker.isEmpty()) {
				sb.append(" @ ");
				sb.append(mileMarker);
			}
			return sb.toString();
		}

		@Override
		public double getLatitude() {
			return latitude;
		}

		@Override
		public double getLongitude() {
			return longitude;
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

	public static class DriverAssignments implements Comparable<DriverAssignments>, Serializable {

		private static final long serialVersionUID = 1L;

		public static class Turn implements Serializable {
			private static final long serialVersionUID = 1L;

			public Integer driver;
			public StopData stop;
			public Double driveTime;
		}

		public double driveImbalance;
		public double[] totalDriveTimes;
		public ArrayList<Turn> turns;
		public double score;

		public DriverAssignments(int nDrivers) {
			totalDriveTimes = new double[nDrivers];
			turns = new ArrayList<>();
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
			for (Turn turn : turns) {
				sb.append('[');
				sb.append(String.format("%d: %s; %.0f", turn.driver, turn.stop.toString(), turn.driveTime));
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

	public TripPlan() {
		lastModified = new Date();
		places = new ArrayList<>();
		route = null;
		tripLegs = new ArrayList<>();
		placesChanged = false;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Robauto TripPlan: ");
		sb.append(lastModified.toString());
		sb.append('\n');
		sb.append(String.format("  Places: %d\n", places.size()));
		sb.append(String.format("  Legs: %d\n", tripLegs.size()));
		return sb.toString();
	}

	@Override
	public int compareTo(TripPlan o) {
		return lastModified.compareTo(o.lastModified);
	}

	public Report getTripReport() {
		Report report = new Report();
		if (legDataList == null || legDataList.isEmpty())
			return report;
		Iterator<TripPlan.LegData> it = legDataList.iterator();
		for (TripLeg leg : tripLegs) {
			leg.legData = it.next();
			report.add(TripPlan.N_DRIVERS, leg.legData, leg.driverAssignments);
		}
		return report;
	}

	public ArrayList<TripPlan.LegData> getTripLegData() {
		return legDataList;
	}

	public void log() {
		RobautoMain.logger.info(this.toString());
	}

	public void save(File file) {
		log();
		placesChanged = false;
		lastModified = new Date();

		try {
			int step = 1;
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(lastModified);
			out.writeObject(placesChanged);
			out.writeObject(places);
			String jsonString = Here2.gson.toJson(route);
			out.writeObject(jsonString);
			out.writeObject(tripLegs);
			out.writeObject(legDataList);
			out.writeObject(legSummary);
			out.writeObject(finalizedDays);
			out.writeObject(finalizedMarkers);
			out.writeObject(finalizedPlaces);
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
		
	}

	@SuppressWarnings("unchecked")
	public static TripPlan load(File file, JFrame frame ) {
		try {
			RobautoMain.logger.info("Load started " + file.getName());
			InputStream fis = new FileInputStream(file);
			if (frame != null) {
				ProgressMonitorInputStream pmis = new ProgressMonitorInputStream( frame, "Loading trip plan...", fis);
				pmis.getProgressMonitor().setMillisToPopup(100);
				fis = pmis;
			}
			ObjectInputStream in = new ObjectInputStream(fis);
			TripPlan tripPlan = new TripPlan();
			tripPlan.lastModified = (Date) in.readObject();
			tripPlan.placesChanged = (Boolean) in.readObject();
			tripPlan.places = (ArrayList<VisitedPlace>) in.readObject();
			String json = (String) in.readObject();
			tripPlan.route = (Route) Here2.gson.fromJson(json, Route.class);
			tripPlan.tripLegs = (ArrayList<TripLeg>) in.readObject();
			tripPlan.legDataList = (ArrayList<LegData>) in.readObject();
			tripPlan.legSummary = (List<LegSummary>) in.readObject();
			try {
				tripPlan.finalizedDays = (ArrayList<Route>) in.readObject();
				tripPlan.finalizedMarkers = (ArrayList<Integer>) in.readObject();
			} catch (Exception x) {
			}
			try {
				tripPlan.finalizedPlaces = (ArrayList<ArrayList<VisitedPlace>>) in.readObject();
			} catch (Exception x) {
			}
			in.close();

			tripPlan.placesChanged = false;
			tripPlan.setRoute(tripPlan.getRoute());
			RobautoMain.logger.info("Load complete");
			tripPlan.log();
			return tripPlan;
		} catch (FileNotFoundException e) {
			RobautoMain.logger.error("Prior Trip Plan file not found on load.");
			return new TripPlan();
		} catch (Exception x) {
			RobautoMain.logger.error("Error loading prior trip plan ", x);
			return new TripPlan();
		}
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public ArrayList<VisitedPlace> getPlaces() {
		return places;
	}

	public void setPlaces(ArrayList<VisitedPlace> places) {
		this.places = places;
		this.placesChanged = false;
	}

	public boolean isLegsEmpty() {
		return tripLegs.isEmpty();
	}

	public TripLeg getTripLeg(int i) {
		return tripLegs.get(i);
	}

	public boolean getPlacesChanged() {
		return placesChanged;
	}

	public void setPlacesChanged(boolean placesChanged) {
		this.placesChanged = placesChanged;
		this.route = null;
	}

	protected static boolean filterDirections(TripPlan.StopData data, List<TripPlan.RoadDirectionData> roadDirections) {
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

	// input road direction data built from maneuver turn 'onto' instructions.
	// add roads listed in stopDataList with unknown directions
	protected void addUnknownDirectionRoads(ArrayList<RoadDirectionData> roadDirectionDataList,
			ArrayList<StopData> stopDataList) {

		TreeMap<String, RoadDirectionData> roadDirectionMap = new TreeMap<>();
		roadDirectionDataList.forEach(item -> {
			roadDirectionMap.put(item.road, item);
		});
		for (int i = 0; i < stopDataList.size() - 1; i++) {
			StopData stopData = stopDataList.get(i);
			if (!roadDirectionMap.containsKey(stopData.road)) {
				RoadDirectionData roadDirectionData = new RoadDirectionData();
				roadDirectionData.direction = "?";
				roadDirectionData.road = stopData.road;
				roadDirectionMap.put(roadDirectionData.road, roadDirectionData);
			}
		}
		roadDirectionDataList.clear();
		roadDirectionDataList.addAll(roadDirectionMap.values());
	}

	public static ArrayList<TripPlan.StopData> getStopData(List<TripPlan.LegSummary> legSummary, int nDrivers, int iLeg,
			boolean includeTruckAreas, List<TripPlan.RoadDirectionData> roadDirections) {
		TripPlan.LegSummary summary = legSummary.get(iLeg);
		return getStopData(legSummary, nDrivers, summary, includeTruckAreas, roadDirections);
	}

	public static ArrayList<TripPlan.StopData> getStopData(List<TripPlan.LegSummary> legSummary, int nDrivers,
			LegSummary summary, boolean includeTruckAreas, List<TripPlan.RoadDirectionData> roadDirections) {
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

	public static TripPlan.DriverAssignments generateDriverAssignments(int nDrivers, TripPlan.LegData legData,
			ArrayList<TripPlan.StopData> stopDataList, Integer[] elements) {
		ArrayList<TripPlan.StopData> sublist = new ArrayList<>();
		for (Integer i : elements) {
			sublist.add(stopDataList.get(i.intValue()));
		}
		sublist.add(stopDataList.get(stopDataList.size() - 1)); // always stop
																// at arrival
																// point
		return generateDriverAssignments(nDrivers, legData, sublist);
	}

	// public static TripPlan.DriverAssignments generateDriverAssignments(int
	// nDrivers, TripPlan.LegData legData,
	// ArrayList<TripPlan.StopData> stopDataList) {
	// return generateDriverAssignments(nDrivers, legData, stopDataList);
	// }

	public static TripPlan.DriverAssignments generateDriverAssignments(int nDrivers, TripPlan.LegData legData,
			ArrayList<TripPlan.StopData> sublist) {

		TripPlan.DriverAssignments driverAssignments = new TripPlan.DriverAssignments(nDrivers);
		int driver = 0;
		double lastTime = 0.0;
		double score = 0.0;
		for (TripPlan.StopData stopData : sublist) {
			// System.out.printf("%10.0f %10.0f %s\n", legData.trafficTime,
			// stopData.trafficTime, stopData.toString());
			TripPlan.DriverAssignments.Turn turn = new TripPlan.DriverAssignments.Turn();
			turn.driver = driver;
			turn.stop = stopData;
			double dTime = stopData.trafficTime - lastTime;
			turn.driveTime = dTime;
			driverAssignments.totalDriveTimes[driver] += dTime;
			score += OptimizeStops.scoreTime(dTime);
			lastTime = stopData.trafficTime;
			driver = (driver + 1) % nDrivers;
			driverAssignments.turns.add(turn);
		}
		driverAssignments.driveImbalance = Math
				.abs(driverAssignments.totalDriveTimes[1] - driverAssignments.totalDriveTimes[0]);
		driverAssignments.score = score + driverAssignments.driveImbalance;
		return driverAssignments;
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		setRoute(route, new ArrayList<POIResult>());
	}

	public void setRoute(Route route, ArrayList<POIResult> restAreas) {
		this.route = route;
		ArrayList<LegSummary> legSummary = new ArrayList<>();
		ArrayList<LegData> legDataList = new ArrayList<>();
		ArrayList<TripLeg> tripLegs = new ArrayList<>();
		if (route != null) {
			Iterator<VisitedPlace> it = this.getPlaces().iterator();
			LegPoint current = new LegPoint();
			if (it.hasNext())
				current.fuelAvailability = POIBase.toFuelString(it.next().getFuelAvailable());
			Iterator<Leg> itLeg = route.getLeg().iterator();
			for (int iLeg = 0; iLeg < route.getLeg().size(); iLeg++) {
				Leg leg = itLeg.next();
				LegPoint next = new LegPoint(current);
				next.fuelAvailability = POIBase.toFuelString(it.next().getFuelAvailable());
				next.plus(leg);
				LegSummary summary = new LegSummary(leg, current, next);
				legSummary.add(summary);
				summary.setNearby(restAreas);
				LegData data = new LegData();
				legDataList.add(data);
				data.distance = summary.length;
				data.startLabel = summary.startUserLabel;
				data.endLabel = summary.endUserLabel;
				data.trafficTime = summary.trafficTime;
				current = next;
				TripPlan.TripLeg tripLeg = new TripPlan.TripLeg();
				tripLeg.legData = data;
				tripLegs.add(tripLeg);
				tripLeg.roadDirectionDataList = getRoadDirectionData(summary);
				tripLeg.stopDataList = getStopData(legSummary, TripPlan.N_DRIVERS, iLeg, false,
						tripLeg.roadDirectionDataList);
				addUnknownDirectionRoads(tripLeg.roadDirectionDataList, tripLeg.stopDataList);
				tripLeg.driverAssignments = generateDriverAssignments(TripPlan.N_DRIVERS, tripLeg.legData,
						tripLeg.stopDataList);
			}
			if (this.legSummary != null)
				for (int iPrevious = 0; iPrevious < this.legSummary.size(); iPrevious++) {
					for (int jNew = 0; jNew < legSummary.size(); jNew++) {
						boolean match = this.legSummary.get(iPrevious).getId()
								.equalsIgnoreCase(legSummary.get(jNew).getId());
						// System.out.println( match + ": " +
						// this.legSummary.get(iPrevious).getId() + " vs " +
						// legSummary.get(jNew).getId());
						if (match && this.tripLegs.get(iPrevious).stopDataList.size() > 1) {
							legSummary.set(jNew, this.legSummary.get(iPrevious));
							legDataList.set(jNew, this.legDataList.get(iPrevious));
							tripLegs.set(jNew, this.tripLegs.get(iPrevious));
						}
					}

				}
		}
		this.legSummary = legSummary;
		this.legDataList = legDataList;
		this.tripLegs = tripLegs;
	}

	// public ArrayList<RoadDirectionData> getRoadDirectionData(int iLeg) {
	// LegSummary summary = legSummary.get(iLeg);
	// return getRoadDirectionData(summary);
	// }

	public ArrayList<RoadDirectionData> getRoadDirectionData(LegSummary summary) {
		TreeSet<RoadDirectionData> dataList = new TreeSet<>();
		Pattern regex = Pattern.compile("onto\\s(\\w+)-(\\d+[\\w])\\s(\\w)");
		for (String instruction : summary.instructions) {
			Matcher matcher = regex.matcher(instruction);
			if (matcher.find()) {
				RoadDirectionData data = new RoadDirectionData();
				data.direction = matcher.group(3);
				data.road = matcher.group(1) + "-" + matcher.group(2);
				dataList.add(data);
			}
		}
		return new ArrayList<>(dataList);
	}

	public void updateRoadDirectionData(TripLeg tripLeg, List<RoadDirectionData> list) {
		int i = tripLegs.indexOf(tripLeg);
		tripLeg.roadDirectionDataList = new ArrayList<>(list);
		tripLeg.stopDataList = getStopData(legSummary, TripPlan.N_DRIVERS, legSummary.get(i), false,
				tripLeg.roadDirectionDataList);
		tripLeg.driverAssignments = generateDriverAssignments(TripPlan.N_DRIVERS, tripLeg.legData,
				tripLeg.stopDataList);
	}

	public void setTripLegs(ArrayList<TripLeg> tripLegs) {
		this.tripLegs = tripLegs;
	}

	public ArrayList<TripLeg> getTripLegs() {
		return tripLegs;
	}

	public void setFinalizedRoute(ArrayList<Route> days, ArrayList<Integer> markers, ArrayList<ArrayList<VisitedPlace>> allPlaces) {
		this.finalizedDays = days;
		this.finalizedMarkers = markers;
		this.finalizedPlaces = allPlaces;
	}

	public ArrayList<Route> getFinalizedDays() {
		if (finalizedDays == null)
			finalizedDays = new ArrayList<>();
		return finalizedDays;
	}

	public ArrayList<Integer> getFinalizedMarkers() {
		if (finalizedMarkers == null)
			finalizedMarkers = new ArrayList<>();
		return finalizedMarkers;
	}
	
	public ArrayList<ArrayList<VisitedPlace>> getFinalizedPlaces() {
		if (finalizedPlaces == null)
			finalizedPlaces = new ArrayList<>();
		return finalizedPlaces;
	}

	public List<TripPlan.LegSummary> getLegSummary() {
		return legSummary;
	}

}
