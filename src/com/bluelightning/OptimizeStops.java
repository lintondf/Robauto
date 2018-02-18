/**
 * 
 */
package com.bluelightning;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map.Entry;

import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.POISet.POIResult;

/**
 * @author NOOK
 *
 */
public class OptimizeStops {
	
	protected double  minDrive = 45.0*60.0;   // [s]
	protected double  maxDrive = 120.0*60.0;  // [s]
	protected double  driveFuzz = 15.0*60.0;
	protected double  nDrivers = 2;
	
	protected Route   route;
	
	public OptimizeStops(Route route, EnumMap<Main.MarkerKinds, ArrayList<POISet.POIResult>> nearbyMap) {
		this.route = route;
		for (Leg leg : route.getLeg()) {
			System.out.println( leg.getSummary() );
			double totalTime = leg.getTrafficTime();
			if (totalTime <= minDrive) {
				System.out.println("One drive of " + Here2.toPeriod(totalTime) );
				return;
			}
			double totalTimePerDriver = totalTime / nDrivers;
			if (totalTimePerDriver < maxDrive) {
				System.out.println("Two drives of " + Here2.toPeriod(totalTimePerDriver) );				
				return;
			}
			System.out.println( Here2.toPeriod(totalTimePerDriver) + " per driver");
			
			double nDrives = Math.floor( totalTimePerDriver / minDrive);
			double mDrives = Math.ceil( totalTimePerDriver / maxDrive);
			
			System.out.printf("%f %f drives\n", nDrives, mDrives );
			
			double driveTimePerDriver = totalTimePerDriver / mDrives;
			
			double nextTime = driveTimePerDriver;
			System.out.println( Here2.toPeriod(nextTime) );
			
			ArrayList<POISet.POIResult> restAreas = nearbyMap.get(Main.MarkerKinds.RESTAREAS);
			for (POISet.POIResult restArea : restAreas) {
//				if (restArea.totalProgress.trafficTime > totalTime)
//					break;
				//if (Math.abs( restArea.totalProgress.trafficTime - nextTime) < driveFuzz) {
					System.out.println(restArea.maneuver.getRoadNumber() + " " + restArea.toReport() );
				//}
//				if (restArea.totalProgress.trafficTime > (nextTime+driveFuzz)) {
//					nextTime += driveTimePerDriver;
//					System.out.println( Here2.toPeriod(nextTime) );
//				}
			}
			break;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
