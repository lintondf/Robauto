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

import com.bluelightning.Main;
import com.bluelightning.OptimizeStops;
import com.bluelightning.OptimizeStops.DriverAssignments;
import com.bluelightning.OptimizeStops.LegData;
import com.bluelightning.OptimizeStops.RoadDirectionData;
import com.bluelightning.OptimizeStops.StopData;
import com.bluelightning.data.TripPlan.TripLeg;
import com.bluelightning.json.Route;
import com.bluelightning.Report;

import seedu.addressbook.data.place.VisitedPlace;

public class TripPlan implements Comparable<TripPlan>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final int N_DRIVERS = 2;
	
	protected Date                    lastModified;
	
	protected boolean                 placesChanged;
	
	protected ArrayList<VisitedPlace> places;
	
	protected String                  routeJson;
	
	public static class TripLeg implements Serializable {
		public OptimizeStops.LegData                      legData;
		public ArrayList<OptimizeStops.RoadDirectionData> roadDirectionDataList;
		public ArrayList<OptimizeStops.StopData>          stopDataList;
		public OptimizeStops.DriverAssignments            driverAssignments;
	}
	
	protected ArrayList<TripLeg>      tripLegs;
	
	
	public TripPlan() {
		lastModified = new Date();
		places = new ArrayList<>();
		routeJson = "";
		tripLegs = new ArrayList<>();
		placesChanged = false;
	}
	
	public void update(Route route) {
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
		List<LegData> legDataList = getTripLegData();
		ArrayList<TripLeg> tripLegs = getTripLegs();
		Iterator<LegData> it = legDataList.iterator();
		Report report = new Report();
		for (TripLeg leg : tripLegs) {
			leg.legData = it.next();
			report.add( TripPlan.N_DRIVERS, leg.legData, leg.driverAssignments );
		}
		return report;
	}
	
	public ArrayList<LegData> getTripLegData() {
		ArrayList<TripLeg> tripLegs = getTripLegs();
		ArrayList<OptimizeStops.LegData> legDataList = new ArrayList<>();
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

	public void setTripLegs(ArrayList<TripLeg> tripLegs) {
		this.tripLegs = tripLegs;
	}

	public boolean getPlacesChanged() {
		return placesChanged;
	}

	public void setPlacesChanged(boolean placesChanged) {
		this.placesChanged = placesChanged;
	}

}
