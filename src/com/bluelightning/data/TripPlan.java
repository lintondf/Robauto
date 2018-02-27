package com.bluelightning.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.bluelightning.Here2;
import com.bluelightning.Main;
import com.bluelightning.OptimizeStops;
import com.bluelightning.data.TripPlan.DriverAssignments;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POI.FuelAvailable;
import com.bluelightning.poi.POIBase;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.RestAreaPOI;
import com.bluelightning.Report;

import seedu.addressbook.data.place.VisitedPlace;

public class TripPlan implements Comparable<TripPlan>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final int N_DRIVERS = 2;
	
	protected Date                    lastModified;
	
	protected boolean                 placesChanged;
	
	protected ArrayList<VisitedPlace> places;
	
	protected String                  routeJson;
	
	protected ArrayList<TripLeg>      tripLegs;
	

	public static class TripLeg implements Serializable {
		public TripPlan.LegData                      legData;
		public ArrayList<TripPlan.RoadDirectionData> roadDirectionDataList;
		public ArrayList<TripPlan.StopData>          stopDataList;
		public DriverAssignments                     driverAssignments;
	}
	
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
		routeJson = "";
		tripLegs = new ArrayList<>();
		placesChanged = false;
	}
	
	public void update(Route route) {
		//TODO
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Robauto TripPlan: ");
		sb.append(lastModified.toString());
		sb.append('\n');
		sb.append( String.format("  RouteJson: %d\n", routeJson.length()));
		sb.append( String.format("  Places: %d\n", places.size()));
		sb.append( String.format("  Legs: %d\n", tripLegs.size()));
		return sb.toString();
	}

	@Override
	public int compareTo(TripPlan o) {
		return lastModified.compareTo(o.lastModified);
	}
	
	public Report getTripReport() {
		List<TripPlan.LegData> legDataList = getTripLegData();
		ArrayList<TripLeg> tripLegs = getTripLegs();
		Iterator<TripPlan.LegData> it = legDataList.iterator();
		Report report = new Report();
		for (TripLeg leg : tripLegs) {
			leg.legData = it.next();
			report.add( TripPlan.N_DRIVERS, leg.legData, leg.driverAssignments );
		}
		return report;
	}
	
	public ArrayList<TripPlan.LegData> getTripLegData() {
		ArrayList<TripLeg> tripLegs = getTripLegs();
		ArrayList<TripPlan.LegData> legDataList = new ArrayList<>();
		for (TripLeg leg : tripLegs) {
			legDataList.add( leg.legData );
		}
		return legDataList;
	}
	


	public void save(File file) {
		placesChanged = false;
		lastModified = new Date();
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static TripPlan load(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fis);
			TripPlan tripPlan = (TripPlan) in.readObject();
			in.close();
			String[] report = tripPlan.toString().split("\n");
			for (String line : report)
				Main.logger.info(line);
			tripPlan.placesChanged = false;
			return tripPlan;
		} catch (Exception x) {
			
			return new TripPlan();
		}
	}
	public static void main(String[] args) {
		TripPlan tripPlan = new TripPlan();
		System.out.println(tripPlan);
		File file = new File("robautoTripPlan.obj");
		tripPlan.save( file );
		TripPlan out = TripPlan.load( file );
		System.out.println(out);
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

	public String getRouteJson() {
		return routeJson;
	}

	public void setRouteJson(String routeJson) {
		this.routeJson = routeJson;
	}

	public ArrayList<TripLeg> getTripLegs() {
		return tripLegs;
	}

//	public void setTripLegs(ArrayList<TripLeg> tripLegs) {
//		this.tripLegs = tripLegs;
//	}

	public boolean getPlacesChanged() {
		return placesChanged;
	}

	public void setPlacesChanged(boolean placesChanged) {
		this.placesChanged = placesChanged;
	}

}
