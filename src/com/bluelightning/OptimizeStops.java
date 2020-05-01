/**
 * 
 */
package com.bluelightning;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

import com.bluelightning.PlannerMode.MarkerKinds;
import com.bluelightning.data.TripPlan;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.POISet;

import seedu.addressbook.data.AddressBook;
import seedu.addressbook.logic.Logic;

/**
 * @author NOOK
 *
 */
public class OptimizeStops {

	protected double minDrive = 45.0 * 60.0; // [s]
	protected double maxDrive = 100.0 * 60.0; // [s]
	protected double driveFuzz = 15.0 * 60.0;
	protected double nDrivers = 2;

	protected Route route;
	public EnumMap<MarkerKinds, POISet> poiMap;
	protected TripPlan tripPlan;
	public Logic controller;
	public AddressBook addressBook;

	
	public List<TripPlan.LegData> getUiLegData() {
		return tripPlan.getTripLegData();
	}

	
	public ArrayList<POIResult> getRouteSegmentPOI(Double start, Double finish) {
		RobautoMain.logger.debug( String.format("gRSP %f %f", start, finish) );
		ArrayList<POIResult> resultList = new ArrayList<>();
		poiMap.forEach((kind, pset) -> {
			resultList.addAll(pset.getPointsOfInterestAlongRoute(route, 1500));
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

	public OptimizeStops(TripPlan tripPlan, Logic controller, AddressBook addressBook, EnumMap<MarkerKinds, POISet> poiMap,
			EnumMap<PlannerMode.MarkerKinds, ArrayList<POIResult>> nearbyMap) {
		this.tripPlan = tripPlan;
		this.controller = controller;
		this.addressBook = addressBook;
		this.route = tripPlan.getRoute();
		this.poiMap = poiMap;
		ArrayList<POIResult> restAreas = nearbyMap.get(PlannerMode.MarkerKinds.RESTAREAS);
		tripPlan.setRoute(route, restAreas);
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
