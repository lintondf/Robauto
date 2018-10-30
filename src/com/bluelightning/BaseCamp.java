/**
 * 
 */
package com.bluelightning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import com.bluelightning.BaseCampDirectionsParser.ParsedDirections;
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
			return String.format("%6.3f km", 0.001*distance);
		}
		
		public String toString() {
			return String.format("'%s' [%s @ %3.0f] {%s} (%s) %s", 
					directions, distance2String(distance), heading, distance2String(totalDistance), duration2String(totalDuration),
					(parsed != null) ? parsed.toString() : "null");
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
			matcher = interiorMatricsPattern.matcher(text);
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
		}
	}
	
	
	static final String pdfBox = "/Users/lintondf/GIT/Robauto/pdfbox-app-2.0.9.jar";
	
	public Garmin.Day day;
	public Route route;
	public ArrayList<Turn> turns = new ArrayList<>();
	public ArrayList<ButtonWaypoint> vias;
	
	public BaseCamp() {} // for generics users only
	
	public BaseCamp( Garmin.Day day, String pdfPath ) {
		this.day = day;
		RobautoMain.logger.info("BASECAMP: " + pdfPath);
		String htmlPath = pdfPath.replace(".pdf", ".html");
		//String gpxPath = pdfPath.replace(".pdf", ".GPX");
		File htmlFile = new File(htmlPath);
		if (!htmlFile.exists()) {
			String[] pdfBoxCommand = {
				"/usr/bin/java",
				"-jar",
				pdfBox,
				"ExtractText",
				"-html",
				pdfPath,
				htmlPath
			};
			Execute.run(pdfBoxCommand);
		}
		try {
			Document doc = Jsoup.parse(htmlFile, "UTF-8");
			Elements body = doc.getElementsByTag("BODY");
			if (!body.isEmpty()) {
				Elements divs = body.first().children();
				for (Element div : divs) {
					//System.out.println(div.tagName().toUpperCase());
					Elements ps = div.getElementsByTag("P");
					Iterator<Element> pit = ps.iterator();
					pit.next();  // skip column header
					pit.next();  // title 
					pit.next();  // page n of m
					//Turn last = null;
					while (pit.hasNext()) {
						Element p = pit.next();
						Turn turn = new Turn(p.text());
//						if (last != null) {
//							turn.totalDistance = last.totalDistance + turn.distance;
//						}
						//last = turn;
						//System.out.println( turn );
						turns.add(turn);
					}
				}
			}
			htmlFile.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
//		garmin = new Garmin( gpxPath );
//		garmin.setLayout(new BorderLayout());
//		map = new com.bluelightning.Map();
//		mapViewer = map.getMapViewer();
//		garmin.add(mapViewer, BorderLayout.CENTER);
////		for (int i = 0; i < 500; i++)
////			System.out.println( garmin.trackPoints.get(i) );
//		System.out.println( garmin.trackPoints.get(garmin.trackPoints.size()-1) );
		
		route = new Route();
		List<Object> routeShape = new ArrayList<>();
		for (TrackPoint point : day.trackPoints) {
			routeShape.add( String.format("%15.8f, %15.8f", point.getLatitude(), point.getLongitude()) );
		}
		route.setShape( routeShape );
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
			iTrack = findNearestTrackPoint( current, iTrack, day.trackPoints );
			List<Object> maneuverShape = new ArrayList<Object>();
			if (startTrack < iTrack && iTrack < leg.getShape().size()) {
				maneuverShape.addAll( leg.getShape().subList(startTrack, iTrack) );
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
			System.out.printf(" M%d %d %d %d %d\n", iTurn, startTrack, iTrack, leg.getShape().size(), maneuver.getShape().size() );
			
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
	

	private int findNearestTrackPoint(Turn current, int iTrack,
			List<TrackPoint> trackPoints) {
		while (iTrack < trackPoints.size()) {
			TrackPoint point = trackPoints.get(iTrack);
			if (point.distanceStartToHere > current.totalDistance) {
				return iTrack; // Math.max(0,  iTrack-1);
			}
			iTrack++;
		}
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

	// ((\d+)\sft)?(([1-9]\d*(\.\d+))\smi)?((\d+)\shour\(s\))?((,\s)?(\d+)\sminutes)?((\d+)\sseconds)?$
	protected static String durationPatternStr = "(((\\d+)\\shour\\(s\\)((,\\s)?(\\d+)\\sminutes)?)|((\\d+)\\sminutes)|((\\d+)\\sseconds))";
	protected static Pattern endingDurationPattern = Pattern.compile(durationPatternStr + "$");
	protected static Pattern distancePattern = Pattern.compile("(((\\d+)\\sft)|((\\d*(\\.\\d+)?)\\smi))$");
	// (((\d+)\sft)|((\d*(\.\d+)?)\smi))(\s(\d+)° true)?
	protected static Pattern interiorMatricsPattern = Pattern.compile("(((\\d+)\\sft)|((\\d*(\\.\\d+)?)\\smi))(\\s(\\d+)° true)?" +
	                 "\\s" + durationPatternStr + "?");
	
	/**
	 * @param args
	 */
	
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
