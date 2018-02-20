/**
 * 
 */
package com.bluelightning;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import org.javatuples.Pair;

import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.OptimizeStopsDialog.LegData;
import com.bluelightning.OptimizeStopsDialog.RoadDirectionData;
import com.bluelightning.OptimizeStopsDialog.StopData;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.POISet.POIResult;
import com.bluelightning.poi.RestAreaPOI;

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
	protected List<LegSummary> legSummary;
	
	public static class LegPoint {
		double  distance;
		double  trafficTime;
		double  travelTime;
		
		public LegPoint() {}
		
		public LegPoint( LegPoint that) {
			this.distance = that.distance;
			this.trafficTime = that.trafficTime;
			this.travelTime = that.travelTime;
		}
		
		public void plus( Leg leg) {
			this.distance += leg.getLength();
			this.trafficTime += leg.getTrafficTime();
			this.travelTime += leg.getTravelTime();
		}
		
		public String toString() {
			return String.format("%5.1f / %5s", distance*Here2.METERS_TO_MILES, Here2.toPeriod(trafficTime));
		}
	}
	
	public static class LegSummary {
		public Leg   leg;
		public LegPoint  start;
		public LegPoint  finish;
		public ArrayList<POISet.POIResult> nearby = new ArrayList<>();
		
		public LegSummary( Leg leg, LegPoint start, LegPoint finish) {
			this.leg = leg;
			this.start = start;
			this.finish = finish;
		}
		
		public void setNearby( ArrayList<POISet.POIResult> all ) {
			for (POISet.POIResult r : all) {
				if (r.totalProgress.distance >= start.distance && r.totalProgress.distance < finish.distance) {
					nearby.add(r);
				}
			}
		}
		
		public String toString() {
			return String.format("%s, %s, %s", start.toString(), finish.toString(), leg.getSummary().getText() );
		}
	}
	
	protected List<LegSummary> generateLegSummaries() {
		ArrayList<LegSummary> starts = new ArrayList<>();
		LegPoint current = new LegPoint();
		for (Leg leg : route.getLeg()) {
			LegPoint next = new LegPoint(current);
			next.plus(leg);
			starts.add( new LegSummary(leg, current, next) );
			current = next;
		}
		return starts;
	}
	
	
	protected LegSummary getLegSummary( Leg leg ) {
		for (LegSummary summary : legSummary) {
			if (summary.leg.hashCode() == leg.hashCode()) {
				return summary;
			}
		}
		return null;
	}
	
	public List<LegData> getUiLegData() {
		ArrayList<LegData> dataList = new ArrayList<>();
		for (LegSummary summary : legSummary) {
			LegData data = new LegData();
			data.distance = summary.leg.getLength();
			data.startLabel = summary.leg.getStart().getLabel();
			data.endLabel = summary.leg.getEnd().getLabel();
			data.trafficTime = summary.leg.getTrafficTime();
			dataList.add(data);
		}
		return dataList;
	}
	
	public List<RoadDirectionData> getUiRoadData( int iLeg ) {
		LegSummary summary = legSummary.get(iLeg);
		TreeSet<RoadDirectionData> dataList = new TreeSet<>();
		Pattern regex = Pattern.compile("onto\\s(\\w+)-(\\d+[\\w])\\s(\\w)");
		for (Maneuver m : summary.leg.getManeuver()) {
			Matcher matcher = regex.matcher(m.getInstruction());
			if (matcher.find()) {
				RoadDirectionData data = new RoadDirectionData();
				data.direction = matcher.group(3);
				data.road = matcher.group(1) + "-" + matcher.group(2);
				dataList.add(data);
			}
		}
		return new ArrayList<>(dataList);
	}
	
	protected boolean filterDirections(StopData data, List<RoadDirectionData> roadDirections) {
		for (RoadDirectionData road : roadDirections) {
			if (road.road.equalsIgnoreCase(data.road)) {
				// RestAreas: NB, EB, SB, WB, NB/SB, EB/WB
				if (road.direction.equalsIgnoreCase("ANY"))
					return true;
				if (data.direction.contains("/"))
					return true;
				return data.direction.startsWith( road.direction.substring(0, 1));
			}
		}
		return false;
	}

	public List<StopData> getUiStopData( int nDrivers, int iLeg, boolean includeTruckAreas, List<RoadDirectionData> roadDirections ) {
		LegSummary summary = legSummary.get(iLeg);
		ArrayList<StopData> dataList = new ArrayList<>();
		for (POISet.POIResult r : summary.nearby) {
			RestAreaPOI restArea = (RestAreaPOI) r.poi;
			StopData data = new StopData();
			data.use = true;
			data.direction = restArea.getDirection();
			data.road = restArea.getHighway();
			data.state = restArea.getState();
			data.mileMarker = restArea.getMileMarker();
			data.distance = r.totalProgress.distance;
			data.trafficTime = r.totalProgress.trafficTime;
			data.name = restArea.getName();
			//data.driveTimes = new Double[nDrivers];
			if (!includeTruckAreas && data.name.toUpperCase().contains("TRUCK"))
				continue;
			if (filterDirections(data, roadDirections)) {
				dataList.add(data);
			}
		}
		return dataList;
	}
	
	public OptimizeStops(Route route, EnumMap<Main.MarkerKinds, ArrayList<POISet.POIResult>> nearbyMap) {
		this.route = route;
		legSummary = generateLegSummaries();
		ArrayList<POISet.POIResult> restAreas = nearbyMap.get(Main.MarkerKinds.RESTAREAS);
		legSummary.forEach( ls-> {
			ls.setNearby(restAreas);
		});
//		for (Leg leg : route.getLeg()) {
//			System.out.println( leg.getSummary() );
//			System.out.printf("%d Maneuvers\n", leg.getManeuver().size() );
//			for (Maneuver m : leg.getManeuver()) {
//				if (m.getId().equalsIgnoreCase("M91"))
//					System.out.println(m.getId());
//			}
//			double totalTime = leg.getTrafficTime();
//			double totalTimePerDriver = totalTime / nDrivers;
//			double nDrives = Math.floor( totalTimePerDriver / minDrive);
//			double mDrives = Math.ceil( totalTimePerDriver / maxDrive);
//			double driveTimePerDriver = totalTimePerDriver / mDrives;
//			if (totalTime <= minDrive) {
//				System.out.println("One drive of " + Here2.toPeriod(totalTime) );
//			}
//			if (totalTimePerDriver < maxDrive) {
//				System.out.println("Two drives of " + Here2.toPeriod(totalTimePerDriver) );				
//			} else {
//				System.out.println( Here2.toPeriod(totalTimePerDriver) + " per driver");
//				System.out.printf("%f %f drives\n", nDrives, mDrives );
//			}
//			
//			
//			double nextTime = driveTimePerDriver;
//			System.out.println( Here2.toPeriod(nextTime) );
//			
//			for (POISet.POIResult restArea : restAreas) {
////				if (restArea.totalProgress.trafficTime > totalTime)
////					break;
//				//if (Math.abs( restArea.totalProgress.trafficTime - nextTime) < driveFuzz) {
//				RestAreaPOI restAreaPOI = (RestAreaPOI) restArea.poi;
//				String road = restAreaPOI.getHighway() + " " + restAreaPOI.getDirection(); 
//				System.out.println(road + ": " + restArea.toReport() );
//				//}
////				if (restArea.totalProgress.trafficTime > (nextTime+driveFuzz)) {
////					nextTime += driveTimePerDriver;
////					System.out.println( Here2.toPeriod(nextTime) );
////				}
//			}
////			break;
//		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}


}
