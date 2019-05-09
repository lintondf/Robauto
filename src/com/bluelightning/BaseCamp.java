/**
 * 
 */
package com.bluelightning;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.LoggerFactory;

import com.bluelightning.BaseCampDirectionsParser.ParsedDirections;
import com.bluelightning.Garmin.Day;
import com.bluelightning.Garmin.TrackPoint;
import com.bluelightning.json.BottomRight;
import com.bluelightning.json.BoundingBox;
import com.bluelightning.json.End;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.MappedPosition;
import com.bluelightning.json.Route;
import com.bluelightning.json.Start;
import com.bluelightning.json.TopLeft;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.StopMarker;
import com.bluelightning.poi.POIBase;

/**
 * @author lintondf
 *
 */
public class BaseCamp {
	
	protected Map map;
	protected JXMapViewer mapViewer;

	
	public static class Turn {
		protected static BaseCampDirectionsParser parser = new BaseCampDirectionsParser();
		public String directions;
		public double distance; // [m] distance until this turn
		public double duration; // [s] time until this turn
		public double heading;  // [deg] heading toward this turn
		public double totalDistance;  // [m]
		public double totalDuration;  // [s]
		public ParsedDirections parsed;
		// # seconds
		// # minutes
		// # # hour(s), # minutes
		// https://www.regexplanet.com/advanced/java/index.html
		// ((\d+)\shour\(s\))?((\d+)\sminutes)?((\d+)\sseconds)?
		
		protected String gather( String[] fields, int from, int to) {
			StringBuffer sb = new StringBuffer();
			for (int i = from; i < to; i++) {
				sb.append( fields[i]);
				sb.append(' ');
			}
			return sb.toString().trim();
		}
		
		protected String duration2String( double duration) {
			if (duration < 60.0)
				return String.format("%2.0f seconds", duration );
			if (duration < 3600.0) {
				int minutes = (int) Math.floor( duration / 60.0 );
				int seconds = (int) (duration - 60.0*minutes);
				return String.format("%2d minutes, %2d seconds", minutes, seconds);
			}
			int hours = (int) Math.floor( duration / 3600.0 );
			int minutes = (int) ((duration - 3600.0*hours) / 60.0);
			return String.format("%2d hours; %2d minutes", hours, minutes);
		}
		
		protected String distance2String( double distance ) {
			return String.format("%6.3f mi", Here2.METERS_TO_MILES*distance);
		}
		
		public String toString() {
			return String.format("'%s' [%s @ %3.0f] {%s} (%s) %s", 
					directions, distance2String(distance), heading, distance2String(totalDistance), duration2String(totalDuration),
					(parsed != null) ? parsed.toString() : "null");
		}
		
		public static double string2Distance( String d ) {
			Matcher matcher = distancePattern.matcher(d);
			if (matcher.find()) {			
				String[] f = d.split(" ");
				double x = Double.parseDouble(f[0]);
				if (f.length > 1) {
					if (f[1].equals("ft"))
						x *= 0.3048;
					if (f[1].equals("mi"))
						x /= Here2.METERS_TO_MILES;
				}
				return x;
			}
			return 0.0;
		}
		
		public static double string2Duration( String d ) {
			double duration = 0;
			Matcher matcher = durationPatternW.matcher(d);
			if (matcher.find()) {
//				for (int g = 0; g < matcher.groupCount(); g++) {
//					System.out.printf("%5d: %s\n", g, matcher.group(g));
//				}
				if (matcher.group(10) != null) { // # s
					duration = 1.0*Integer.parseInt(matcher.group(10));
				} else if (matcher.group(8) != null) { // # min
					duration = 60.0*Integer.parseInt(matcher.group(8));
				} else if (matcher.group(3) != null) { // # h
					duration = 3600.0*Integer.parseInt(matcher.group(3));
					if (matcher.group(6) != null) { //, # min
						duration += 60.0*Integer.parseInt(matcher.group(6));
					}
				}
			}
			return duration;
		}
		
		public Turn( String directions, double distance, double duration, double distanceSoFar, double durationSoFar ) {
			this.directions = directions;
			this.distance = distance;
			this.duration = duration;
			this.totalDistance = distanceSoFar + this.distance;
			this.totalDuration = durationSoFar + this.duration;
		}
		
		public Turn( String text ) {
			text = text.trim();
			text = text.replaceAll("tr…", "true");
			//System.out.println(text);
			Matcher matcher = endingDurationPattern.matcher(text);
			if (matcher.find()) {
//				System.out.print("Tn: " + matcher.groupCount() + ": ");
//				for (int i = 0; i < matcher.groupCount()+1; i++) {
//					System.out.printf("%d:{%s}; ", i, matcher.group(i));
//				}
//				System.out.println();
				this.totalDuration = 0.0;
				if (matcher.group(10) != null) {
					this.totalDuration += Double.parseDouble( matcher.group(10) );
				}
				if (matcher.group(6) != null) {
					this.totalDuration += 60*Double.parseDouble( matcher.group(6) );
				}
				if (matcher.group(8) != null) {
					this.totalDuration += 60*Double.parseDouble( matcher.group(8) );
				}
				if (matcher.group(3) != null) {
					this.totalDuration += 3600*Double.parseDouble( matcher.group(3) );
				}
	            text = text.substring(0, matcher.start()).trim();
			}
			matcher = distancePattern.matcher(text);
			if (matcher.find()) {
//				System.out.print("Dn: ");
//				for (int i = 0; i < matcher.groupCount()+1; i++) {
//					System.out.printf("%d:{%s}; ", i, matcher.group(i));
//				}
//				System.out.println();
	            text = text.substring(0, matcher.start()).trim();
	            this.totalDistance = 0.0;
	            if (matcher.group(3) != null) {
	            	totalDistance += Double.parseDouble( matcher.group(3)) * 0.3048;  // ft -> m
	            }
	            if (matcher.group(5) != null) {
	            	totalDistance += Double.parseDouble( matcher.group(5)) * 5280.0 * 0.3048;  // mi -> m
	            }
			}
			this.distance = 0;
			this.duration = 0;
			this.heading = 0;
			matcher = interiorMetricsPattern.matcher(text);
			if (matcher.find()) {
//				System.out.println(text);
//				System.out.print("In: ");
//				for (int i = 0; i < matcher.groupCount()+1; i++) {
//					System.out.printf("%d:{%s}; ", i, matcher.group(i));
//				}
//				System.out.println();
	            if (matcher.group(3) != null) {
	            	distance = Double.parseDouble( matcher.group(3)) * 0.3048;  // ft -> m
	            }
	            if (matcher.group(5) != null) {
	            	distance = Double.parseDouble( matcher.group(5)) * 5280.0 * 0.3048;  // mi -> m
	            }
	            if (matcher.group(8) != null) {
	            	heading = Double.parseDouble( matcher.group(8));  // deg
	            }
	            if (matcher.group(18) != null) {
	            	duration += Double.parseDouble( matcher.group(18));
	            }
	            if (matcher.group(16) != null) {
	            	duration += Double.parseDouble( matcher.group(16)) * 60.0; // minutes -> s
	            }
	            if (matcher.group(11) != null) {
	            	duration += Double.parseDouble( matcher.group(11)) * 3600.0; // hours -> s
	            }
	            if (matcher.group(14) != null) {
	            	duration += Double.parseDouble( matcher.group(14)) * 60.0; // minutes -> s
	            }
				
	            text = text.substring(0, matcher.start()).trim();
			}
			this.directions = text;
			this.parsed = parser.parse(text);
			//System.out.println(this);
		}
	}
	
	static final String pdfBox = "pdfbox-app-2.0.9.jar";
	
	public Garmin.Day day;
	public Route route;
	public ArrayList<Turn> turns = new ArrayList<>();
	public ArrayList<ButtonWaypoint> vias;
	
	protected void loadRouteDetailLines( List<String> lines ) {
		double totalDuration = 0.0;
		double totalDistance = 0.0;
		for (String line : lines) {
			if (line.isBlank())
				continue;
			if (! Character.isDigit(line.charAt(0)))
				line = line.substring(6);
			System.out.println(line);
			String[] fields = line.split(",");
			for (int i = 0; i < fields.length; i++) {
				fields[i] = fields[i].trim();
			}
			if (line.startsWith("1. ")) {
				Turn turn = new Turn(fields[0].substring(3), 0.0, 0.0, totalDistance, totalDuration );
				turns.add(turn);
				System.out.println(turn.toString() );
				continue;
			}
//			ArrayList<String> f = new ArrayList<>();
			String directions = "";
			Matcher matcher = numberedItemPattern.matcher(fields[0]);
			if (matcher.find()) {
				directions = matcher.group(2); 
				int i = 1;
				double segmentDistance = 0;
				double segmentDuration = 0;
				double v = getDistance(fields[i]);
				if (v >= 0.0) {
					segmentDistance += v;
					i++;
					v = getDuration(fields[i]);
					while (v >= 0.0) {
						segmentDuration += v;
						i++;
						v = getDuration(fields[i]);
					}
					Turn turn = new Turn(directions, segmentDistance, segmentDuration, totalDistance, totalDuration );
					totalDistance += turn.distance;
					totalDuration += turn.duration;
					turns.add(turn);
					System.out.println(turn.toString() );
				}
			}
		}
	}

	protected void readCsvFile( String csvPath ) {
		try {
			List<String> lines = Files.readAllLines( Paths.get(csvPath) );
			loadRouteDetailLines( lines );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public BaseCamp() {} // for generics users only
	
	public BaseCamp( Garmin.Day day, String pdfPath ) {
		this.day = day;
		RobautoMain.logger.info("BASECAMP: " + pdfPath);
		
		readCsvFile( pdfPath );
		initialize(pdfPath);
	}
		
	public BaseCamp(Day day, List<String> lines) {
		this.day = day;
		RobautoMain.logger.info("BASECAMP: From Clipboard");
		
		loadRouteDetailLines( lines );
		initialize("Clipboard");
	}
	
	protected void initialize( String source ) {
		route = new Route();
		List<Object> routeShape = new ArrayList<>();
		for (TrackPoint point : day.trackPoints) {
			routeShape.add( String.format("%15.8f, %15.8f", point.getLatitude(), point.getLongitude()) );
		}
		route.setShape( routeShape );
		double dayLength = computeShapeLength( route.getShape() );
		
//		System.out.printf("%10.4f %10.0f %10.0f\n", dayLength / turns.get(turns.size()-1).totalDistance, dayLength, turns.get(turns.size()-1).totalDistance);
//		System.out.println( turns.get(0) );
//		System.out.println( turns.get(turns.size()-1) );
//		System.out.println( day.wpts.get(0).getName() );
//		System.out.println( day.wpts.get(day.wpts.size()-1).getName() );
		
		String turnsStart = turns.get(0).directions.replace("Start at ", "").split(",")[0];
		String turnsFinish = turns.get(turns.size()-1).directions.replace("Arrive at ", "").split(",")[0];
		String dayStart = day.wpts.get(0).getName().split(",")[0];
		String dayFinish = day.wpts.get(day.wpts.size()-1).getName().split(",")[0];
//		System.out.println( turnsStart.equals( dayStart ) + " " + turnsFinish.equals( dayFinish ) );
		
		double lengthRatio = dayLength / turns.get(turns.size()-1).totalDistance;
		if (lengthRatio < 0.995 || lengthRatio > 1.005) {
			RobautoMain.logger.error(String.format("Length mismatch: %.0f vs %.0f on %s",
					dayLength * Here2.METERS_TO_MILES, 
					turns.get(turns.size()-1).totalDistance * Here2.METERS_TO_MILES,
					source ));
		}
		if (! turnsStart.equals( dayStart )) {
			RobautoMain.logger.error(String.format("Start mismatch: %s vs %s on %s", dayStart, turnsStart, source ));
		}
		if (! turnsFinish.equals( dayFinish )) {
			RobautoMain.logger.error(String.format("Finish mismatch: %s vs %s on %s", dayFinish, turnsFinish, source ));
		}
		
		
		route.setBoundingBox( generateBoundingBox(day.trackPoints) );
		Leg leg = new Leg();
		leg.setShape( routeShape );
		leg.setFirstPoint(0.0);
		route.getLeg().add(leg);  // only one
		vias = new ArrayList<>();
		ArrayList<LatLon> viaPoints = new ArrayList<>();
		Iterator<Turn> tit = turns.iterator();
		int iTrack = 0;
		int startTrack = iTrack;
		int iTurn = 1;
		double legLength = 0;
		double legDuration = 0.0;
		while (tit.hasNext()) {
			Turn current = tit.next();
			if (current.distance <= 0.0)
				continue;
			RobautoMain.logger.debug("T" + iTurn + " : " + current);
			
			Maneuver maneuver = new Maneuver();
			leg.getManeuver().add(maneuver);
			iTrack = findMatchingTrackPoint( current, iTrack, day.trackPoints );
			List<Object> maneuverShape = new ArrayList<Object>();
			if (startTrack < iTrack && startTrack < leg.getShape().size()) {
				maneuverShape.addAll( leg.getShape().subList(startTrack, iTrack) );
			}
			if (maneuverShape.isEmpty()) {
				RobautoMain.logger.debug("Empty maneuver");
			}
			maneuver.setShape( maneuverShape );
			maneuver.setLength( current.distance );
			maneuver.setTrafficTime( current.duration);
			maneuver.setTravelTime(current.duration);
			maneuver.setId( String.format("M%d", iTurn));
			maneuver.setInstruction(current.directions);
			if (current.parsed != null && current.parsed.road != null) {
				maneuver.setRoadName( current.parsed.road );
				maneuver.setRoadNumber( current.parsed.road );
			}
			maneuver.postProcess();
//			System.out.printf(" M%2d %5d %5d %5d %10.0f %10.0f %8.0f  %s\n", iTurn, startTrack, iTrack, maneuver.getShape().size(), 
//					maneuver.getLength(), computeShapeLength(maneuver.getShape()),
//					maneuver.getLength()-computeShapeLength(maneuver.getShape()),
//					current.directions );
			
			legLength += maneuver.getLength();
			legDuration += maneuver.getTrafficTime();

			startTrack = iTrack;
			iTrack++;
			iTurn++;
			if (iTrack < day.trackPoints.size()) {
				TrackPoint trackPoint = day.trackPoints.get(iTrack);
				RobautoMain.logger.debug( iTrack + " : " +  trackPoint);
				//StopMarker wp = new StopMarker(StopMarker.DRIVERS, Integer.toString(iTrack) + ":" + current.toString(), trackPoint);
				//vias.add(wp);
				viaPoints.add(trackPoint);
			}
		}
		MappedPosition mappedPosition = new MappedPosition();
		mappedPosition.setLatitude( day.trackPoints.get(0).getLatitude());
		mappedPosition.setLongitude( day.trackPoints.get(0).getLongitude());
		Start start = new Start();
		if (day.wpts.size() > 1) {
			StopMarker marker = new StopMarker( StopMarker.ORIGIN, day.wpts.get(0).getName(), 
					new GeoPosition( mappedPosition.getLatitude(), mappedPosition.getLongitude()) );
			vias.add( marker );
			String label = String.format("%s/%s", day.wpts.get(0).getName(), day.wpts.get(0).getDesc() );
			start.setUserLabel(label);
		}
		start.setMappedPosition(mappedPosition);
		leg.setStart(start);
		mappedPosition = new MappedPosition();
		mappedPosition.setLatitude( day.trackPoints.get(day.trackPoints.size()-1).getLatitude());
		mappedPosition.setLongitude( day.trackPoints.get(day.trackPoints.size()-1).getLongitude());
		End end = new End();
		if (day.wpts.size() > 1) {
			StopMarker marker = new StopMarker( StopMarker.TERMINUS, day.wpts.get(0).getName(), 
					new GeoPosition( mappedPosition.getLatitude(), mappedPosition.getLongitude()) );
			vias.add( marker );
			String label = String.format("%s/%s", day.wpts.get(1).getName(), day.wpts.get(1).getDesc() );
			end.setUserLabel(label);
		}
		end.setMappedPosition(mappedPosition);
		leg.setEnd(end);
		leg.setLength(legLength);
		leg.setTrafficTime(legDuration);
		leg.setTravelTime(legDuration);
	
		leg.postProcess();
//		ArrayList<ButtonWaypoint> viaPoints = new ArrayList<>();
//		viaPoints.forEach(ll->{
//			ButtonWaypoint wp = new ButtonWaypoint(ll.toString(), ll);
//			vias.add(wp);
//		});
//		HereRoutePlus hereRoute = Here2.getRoute( garmin.trackPoints.get(0), viaPoints, 
//				garmin.trackPoints.get(garmin.trackPoints.size()-1), "fastest;truck;traffic:disabled" );
//		System.out.println( hereRoute );
//		route = Here2.computeRouteBase(hereRoute.route);
//		System.out.println( turns.size() + " ? " + viaPoints.size() + " " + vias.size() );
//		System.out.println( day.trackPoints.get(0) );
//		System.out.println( day.trackPoints.get(1) );
//		System.out.println( turns.get(0) );
//		System.out.println( turns.get(1) );
//		System.out.println( viaPoints.get(0) );
		
//		Here2.reportRoute(route);

		
//		if (!garmin.track.isEmpty()) {
//			// validateTrack( track );
////			garmin.waylist.add(new StopMarker(StopMarker.TERMINUS, text, track
////					.get(track.size() - 1)));
//			EventQueue.invokeLater(new Runnable() {
//				public void run() {
//					ArrayList< List<GeoPosition> > tracks = new ArrayList<>();
//					tracks.add(garmin.track);
//					tracks.add(route.getShape());
//					map.show(tracks, vias );
////					map.show(tracks, garmin.waylist );
//				}
//			});
//		}
	}
	

	private double computeShapeLength(List<GeoPosition> shape) {
		double length = 0.0;
		for (int i = 1; i < shape.size(); i++) {
			length += POIBase.distanceBetween( shape.get(i-1).getLatitude(), shape.get(i-1).getLongitude(), 
					shape.get(i).getLatitude(), shape.get(i).getLongitude() );
		}
		return length;
	}

	private int findMatchingTrackPoint(Turn current, int iTrack,
			List<TrackPoint> trackPoints) {
//		int start = iTrack;
		while (iTrack < trackPoints.size()) {
			TrackPoint point = trackPoints.get(iTrack);
			if (point.distanceStartToHere > current.totalDistance) {
//				System.out.printf("fMTP %5d %10.0f %10.0f %8.0f\n", iTrack, point.distanceStartToHere, current.totalDistance, point.distanceStartToHere-current.totalDistance);
				return iTrack; // Math.max(0,  iTrack-1);
			}
			iTrack++;
		}
//		System.out.printf("fMTP runoff %10.0f\n", current.totalDistance);
//		for (int i = start; i < trackPoints.size(); i++) 
//			System.out.printf("  %5d %10.0f\n",  i, trackPoints.get(i).distanceStartToHere );
		return iTrack;
	}


//	private int addMidpointToViaPoints(double midway, ArrayList<LatLon> viaPoints,
//			List<TrackPoint> trackPoints, int iTrack) {
//		while (iTrack < trackPoints.size()) {
//			if (trackPoints.get(iTrack).distanceStartToHere > midway) {
//				double priorToMidway = 0.0;
//				if (iTrack > 0) {
//					priorToMidway = midway - trackPoints.get(iTrack-1).distanceStartToHere;
//				}
//				double midwayToCurrent = trackPoints.get(iTrack).distanceStartToHere - midway;
//				if (midwayToCurrent > priorToMidway) {
//					viaPoints.add(trackPoints.get(iTrack-1));
//				} else {
//					viaPoints.add(trackPoints.get(iTrack));
//				}
//				break;
//			}
//			iTrack++;
//		}
//		return iTrack;
//	}

	public <A extends GeodeticPosition> BoundingBox generateBoundingBox(List<A> position) {
		BoundingBox box = new BoundingBox();
		BottomRight br = new BottomRight();
		TopLeft tl = new TopLeft();
		if (position.size() > 0) {
			br.setLatitude( position.get(0).getLatitude());
			tl.setLatitude( position.get(0).getLatitude());
			br.setLongitude( position.get(0).getLongitude());
			tl.setLongitude( position.get(0).getLongitude());
			for (int i = 1; i < position.size(); i++) {
				double latitude = position.get(i).getLatitude();
				double longitude = position.get(i).getLongitude();
				if (latitude > tl.getLatitude()) {
					tl.setLatitude( latitude );
				}
				if (latitude < br.getLatitude()) {
					br.setLatitude(latitude);
				}
				if (longitude < tl.getLongitude()) {
					tl.setLongitude(longitude);
				}
				if (longitude > br.getLatitude()) {
					br.setLongitude(longitude);
				}
			}
		}
		box.setBottomRight(br);
		box.setTopLeft(tl);
		//System.out.printf( "BB %10.6f %10.6f  %10.6f %10.6f\n", tl.getLatitude(), br.getLatitude(), tl.getLongitude(), br.getLongitude() );
		return box;
	}
	
	protected double getDistance( String d ) {
		if (d.isBlank())
			return -1.0;
		if (Character.isDigit(d.charAt(0))) {
			if (d.endsWith(" ft")) {
				return 0.3048 * Double.parseDouble(d.replace(" ft", ""));
			} else if (d.endsWith(" mi")) {
				return 0.3048 * 5280.0 * Double.parseDouble(d.replace(" mi", ""));
			}
		}
		return -1.0;
	}
	protected double getDuration( String d ) {
		if (d.isBlank())
			return -1.0;
		if (Character.isDigit(d.charAt(0))) {
			if (d.endsWith(" h")) {
				return 3600.0 * Double.parseDouble(d.replace(" h", ""));
			} else if (d.endsWith(" min")) {
				return 60.0 * Double.parseDouble(d.replace(" min", ""));
			} else if (d.endsWith(" s")) {
				return Double.parseDouble(d.replace(" s", ""));				
			}
		}
		return -1.0;
	}

	protected static Pattern numberedItemPattern = Pattern.compile("^(\\d+)\\.\\s(.+)");
	protected static String durationPatternStrW = "(((\\d+)\\sh((,\\s)?(\\d+)\\smin)?)|((\\d+)\\smin)|((\\d+)\\ss))";
	protected static Pattern durationPatternW = Pattern.compile(durationPatternStrW);
	protected static String durationPatternStr = "(((\\d+)\\shour\\(s\\)((,\\s)?(\\d+)\\sminutes)?)|((\\d+)\\sminutes)|((\\d+)\\sseconds))";
	protected static Pattern endingDurationPattern = Pattern.compile(durationPatternStr + "$");
	protected static Pattern distancePattern = Pattern.compile("(((\\d+)\\sft)|((\\d*(\\.\\d+)?)\\smi))");
	// (((\d+)\sft)|((\d*(\.\d+)?)\smi))(\s(\d+)° true)?
	protected static Pattern interiorMetricsPattern = Pattern.compile("(((\\d+)\\sft)|((\\d*(\\.\\d+)?)\\smi))(\\s(\\d+)° true)?" +
	                 "\\s" + durationPatternStr + "?");
	
	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		RobautoMain.logger = LoggerFactory.getLogger("com.bluelightning.RobautoTravel");

		//	protected static String durationPatternStrW = "(((\\d+)\\sh\\(s\\)((,\\s)?(\\d+)\\smin)?)|((\\d+)\\smin)|((\\d+)\\ss))";
		BaseCamp basecamp = new BaseCamp(null, "C:\\Users\\NOOK\\Documents\\Trip to West Gardiner Twn.pdf");
//		String[] tests = {
//				"1 min",
//				"19 s",
//				"2 h, 20 min",
//				"3 h"
//		};
//		for (String test : tests) {
//			System.out.println(test);
//			Matcher matcher = durationPatternW.matcher(test);
//			if (matcher.find()) {
//				for (int i = 0; i <= matcher.groupCount(); i++) {
//					System.out.print(" " + i + ":");
//					System.out.print(matcher.group(i));
//				}
//				System.out.println();
//			}
//		}
//		
//		String[] tests2 = {
//				"1. zot",
//				"9. zot",
//				"11. zot",
//				"19. zot",
//				"101. zot",
//				"1. Home", 
//				"163 ft"
//		};
//		for (String test : tests2) {
//			System.out.println(test);
//			Matcher matcher = numberedItemPattern.matcher(test);
//			if (matcher.find()) {
//				for (int i = 0; i <= matcher.groupCount(); i++) {
//					System.out.print(" " + i + ":");
//					System.out.print(matcher.group(i));
//				}
//				System.out.println();
//			}
//		}
	}
	
//	protected static JFrame frame;
//	protected static BaseCamp baseCamp;
//	
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					frame = new JFrame();
//					frame.setTitle("Robauto - Garmin");
//					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//					Image icon = ImageIO.read(
//							new File("images/icon-travel.jpg"))
//							.getScaledInstance((int) 64, (int) 64,
//									Image.SCALE_SMOOTH);
//					frame.setIconImage(icon);
//
//					String pdfPath = "/Users/lintondf/basecamp.pdf";
//					baseCamp = new BaseCamp(pdfPath);
//
//					frame.setContentPane(baseCamp.garmin);
//					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
//					SwingUtilities.updateComponentTreeUI(frame);
//					frame.pack();
//					frame.setVisible(true);
//					frame.addWindowListener(new WindowAdapter() {
//						@Override
//						public void windowClosing(WindowEvent e) {
//						}
//					});
//
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		
////		System.out.println(durationPattern);
////		String pdfPath = "/Users/lintondf/basecamp.pdf";
////		BaseCamp baseCamp = new BaseCamp(pdfPath);
////		String[] test = {
//////				"Start at Maine Home 0 ft 0 seconds",
//////				"Get on Manor Ln and drive south 0 ft 200° true 0 seconds 0 ft 0 seconds",
//////				"Take exit 103 to the right onto I-295 S towards Me-9/Me-126/Gardiner/Brunswick 79.2 mi 213° true 1 hour(s), 11 minutes 119 mi 2 hour(s), 5 minutes",
//////				"Arrive at 700 Lafayette Rd1 0.2 mi 211° true 2 minutes 231 mi 3 hour(s), 55 minutes"
////		};
////		for (String text : test) {
////			//System.out.println("IN: {" + text +"}");
////			Matcher matcher = durationPattern.matcher(text);
////			if (matcher.find()) {
////				System.out.print(matcher.groupCount() + "  Start index: " + matcher.start());
////	            System.out.print(" End index: " + matcher.end() + " : ");
////	            System.out.println(matcher.group());
////	            text = text.substring(0, matcher.start()).trim();
////			}
////			//System.out.println("-T: {" + text +"}");
////			matcher = distancePattern.matcher(text);
////			if (matcher.find()) {
////				System.out.print("  Start index: " + matcher.start());
////	            System.out.print(" End index: " + matcher.end() + " : ");
////	            System.out.println(matcher.group());
////	            text = text.substring(0, matcher.start()).trim();
////			}
////			//System.out.println("-D: {" + text +"}");
////			matcher = interiorDistancePattern.matcher(text);
////			if (matcher.find()) {
////				System.out.print("  Start index: " + matcher.start());
////	            System.out.print(" End index: " + matcher.end() + " : ");
////	            System.out.println(matcher.group());
////	            text = text.substring(0, matcher.start()).trim();
////			}
////			System.out.println("-d: {" + text +"}");
////		}
//	}

}
