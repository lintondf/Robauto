package com.bluelightning.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bluelightning.Here2;
import com.bluelightning.Main;
import com.bluelightning.OptimizeStops;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIBase;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.RestAreaPOI;
import com.bluelightning.Report;

import seedu.addressbook.data.place.VisitedPlace;

public class TripPlan implements Comparable<TripPlan>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final int N_DRIVERS = 2;
	
	protected Date                    lastModified;
	
	protected Boolean                 placesChanged;
	
	protected ArrayList<VisitedPlace> places;
	
	protected Route                   route;
	
	protected ArrayList<TripLeg>      tripLegs;
	
	protected ArrayList<LegData>      legDataList;
	
	protected List<TripPlan.LegSummary> legSummary;
	

	public static class TripLeg implements Serializable {
		private static final long serialVersionUID = 1L;

		public TripPlan.LegData                      legData;
		public ArrayList<TripPlan.RoadDirectionData> roadDirectionDataList;
		public ArrayList<TripPlan.StopData>          stopDataList;
		public DriverAssignments                     driverAssignments;
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(" TripLeg\n");
			sb.append("    legData: " + legData.toString() ); sb.append('\n');
			sb.append(String.format("   roadDirectionDataList (%d)\n", roadDirectionDataList.size() ) );
			for (RoadDirectionData data : this.roadDirectionDataList) {
				sb.append("     " + data.toString() );
			}
			sb.append(String.format("   stopDataList (%d)\n", stopDataList.size() ) );
			for (StopData stopData : this.stopDataList ) {
				sb.append("      " + stopData.toString() );
			}
			
			sb.append("    driverAssignments: " + this.driverAssignments.toString() );
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

	private static class LegSummary implements Serializable {
		private static final long serialVersionUID = 1L;

		public String[]  instructions; 
		public String    startUserLabel; 
		public String    endUserLabel;
		public Double    length;
		public Double    trafficTime;
		public LegPoint start;
		public LegPoint finish;
		public ArrayList<POIResult> nearby = new ArrayList<>();
	
		public LegSummary(Leg leg, LegPoint start, LegPoint finish) {
			instructions = new String[ leg.getManeuver().size() ];
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
			return String.format("%s, %s, %s to %s", start.toString(), finish.toString(), this.startUserLabel, this.endUserLabel);
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

	public static class DriverAssignments implements Comparable<DriverAssignments>, Serializable {
	
		private static final long serialVersionUID = 1L;
	
		public static class Assignment implements Serializable {
			private static final long serialVersionUID = 1L;
	
			public List<StopData> stops;
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
		sb.append( String.format("  Places: %d\n", places.size()));
		sb.append( String.format("  Legs: %d\n", tripLegs.size()));
		return sb.toString();
	}

	@Override
	public int compareTo(TripPlan o) {
		return lastModified.compareTo(o.lastModified);
	}
	
	public Report getTripReport() {
		Iterator<TripPlan.LegData> it = legDataList.iterator();
		Report report = new Report();
		for (TripLeg leg : tripLegs) {
			leg.legData = it.next();
			report.add( TripPlan.N_DRIVERS, leg.legData, leg.driverAssignments );
		}
		return report;
	}
	
	public ArrayList<TripPlan.LegData> getTripLegData() {
		return legDataList;
	}
	

	public void log() {
		Main.logger.info(this.toString());
	}

	public void save(File file) {
		log();
		placesChanged = false;
		lastModified = new Date();
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			Main.logger.info(""+lastModified);
			out.writeObject(this.lastModified);
			Main.logger.info(""+placesChanged);
			out.writeObject(this.placesChanged);
			Main.logger.info(""+places.size());
			out.writeObject(this.places);
			String jsonString = Here2.gson.toJson(this.route);
			Main.logger.info(""+jsonString.length());
			out.writeObject(jsonString);
			Main.logger.info(""+tripLegs.size());
			out.writeObject(this.tripLegs);
			Main.logger.info(""+legDataList.size());
			out.writeObject(this.legDataList);
			Main.logger.info(""+legSummary.size());
			out.writeObject(this.legSummary);
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static TripPlan load(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fis);
			TripPlan tripPlan = new TripPlan();
			tripPlan.lastModified = (Date) in.readObject();
			Main.logger.info(""+tripPlan.lastModified);
			tripPlan.placesChanged = (Boolean) in.readObject();
			Main.logger.info(""+tripPlan.placesChanged);
			tripPlan.places = (ArrayList<VisitedPlace>) in.readObject();
			Main.logger.info(""+tripPlan.places.size());
			String json = (String) in.readObject();
			Main.logger.info(""+json.length());
			tripPlan.route = (Route) Here2.gson.fromJson(json, Route.class);
			tripPlan.tripLegs = (ArrayList<TripLeg>) in.readObject();
			Main.logger.info(""+tripPlan.tripLegs.size());
			tripPlan.legDataList = (ArrayList<LegData>) in.readObject();
			Main.logger.info(""+tripPlan.legDataList.size());
			tripPlan.legSummary = (List<LegSummary>) in.readObject();
			Main.logger.info(""+tripPlan.legSummary.size());
			in.close();
			
			String[] report = tripPlan.toString().split("\n");
			for (String line : report)
				Main.logger.info(line);
			tripPlan.placesChanged = false;
			tripPlan.setRoute( tripPlan.getRoute() );
			Main.logger.info("Load complete");
			tripPlan.log();
			return tripPlan;
		} catch (FileNotFoundException e) {
			Main.logger.error( "Prior Trip Plan file not found on load ", e);
			return new TripPlan();
		} catch (Exception x) {
			Main.logger.error( "Error loading prior trip plan ", x);
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

	protected static boolean filterDirections(TripPlan.StopData data,
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

	public static ArrayList<TripPlan.StopData> getStopData(List<TripPlan.LegSummary> legSummary, int nDrivers, int iLeg, boolean includeTruckAreas,
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
	
	public Route getRoute() {
		return route;
	}
	
	
	public void setRoute(Route route) {
		setRoute( route, new ArrayList<POIResult>() );
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
				current.fuelAvailability = POIBase.toFuelString( it.next().getFuelAvailable() );
			Iterator<Leg> itLeg = route.getLeg().iterator();
			for (int iLeg = 0; iLeg < route.getLeg().size(); iLeg++) {
				Leg leg = itLeg.next();
				LegPoint next = new LegPoint(current);
				next.fuelAvailability = POIBase.toFuelString( it.next().getFuelAvailable() );
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
				tripLeg.stopDataList = getStopData(legSummary, TripPlan.N_DRIVERS, iLeg, false, tripLeg.roadDirectionDataList);
				tripLeg.driverAssignments = generateDriverAssignments(TripPlan.N_DRIVERS, tripLeg.legData, tripLeg.stopDataList );
			}
			for (int iPrevious = 0; iPrevious < this.legSummary.size(); iPrevious++) {
				for (int jNew = 0; jNew < legSummary.size(); jNew++) {
					boolean match = this.legSummary.get(iPrevious).getId().equalsIgnoreCase(legSummary.get(jNew).getId());
					//System.out.println( match + ": " + this.legSummary.get(iPrevious).getId() + " vs " + legSummary.get(jNew).getId());
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

	public ArrayList<RoadDirectionData> getRoadDirectionData(int iLeg) {
		LegSummary summary = legSummary.get(iLeg);
		return getRoadDirectionData(summary);
	}

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

}
