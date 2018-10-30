package com.bluelightning;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.LoggerFactory;
import org.tools4j.meanvar.MeanVarianceSampler;

import com.bluelightning.GPS.Fix;
import com.bluelightning.data.TripPlan;
import com.bluelightning.data.TripPlan.TripLeg;
import com.bluelightning.json.Maneuver;
import com.x5.template.Chunk;
import com.x5.template.Theme;

public class TravelStatus {
	
	static Theme theme;
	
	protected static final String oTD = "<TD style='font-size:48pt'>";
	protected static final String obcTD = "<TD style='font-size:48pt;font-weight:bolder;text-align:center'>";
	protected static final String oblTD = "<TD style='font-size:48pt;font-weight:bolder;text-align:left'>";
	protected static final String orTD = "<TD style='font-size:48pt;text-align:right'>";
	protected static final String cTD = "</TD>";
	
	public static String toDeltaPeriod( double seconds ) {
		String prefix = "+";
		if (seconds < 0) {
			seconds *= -1;
			prefix = "-";
		}
		int h = (int) (seconds/3600.0);
		int m = (int) ((seconds - 3600.0*h)/60.0);
		return String.format("%s%2d:%02d", prefix, h, m );
	}
	
	protected static GeodeticCalculator geoCalc = new GeodeticCalculator();
	protected static Ellipsoid wgs84 = Ellipsoid.WGS84;


	protected class Tracker {
		String title;
		double planned;
		double actual;
		double delta;
		
		public Tracker( String title, double planned ) {
			this.title = title;
			this.planned = planned;
			this.actual = 0;
			this.delta = 0;
		}
		
		public void update( double actual ) {
			this.actual = actual;
			this.delta = actual - planned;
		}
		public String toHtmlRow() {
			return String.format("%s%s%s%s%.1f%s%s%.1f%s%s%.1f%s", 
					oblTD, title, cTD,
					orTD, planned, cTD, orTD, actual, cTD, oTD, (actual-planned), cTD );
		}
	}
	
	protected class DistanceTracker extends Tracker {
		
		public DistanceTracker( String title, double planned ) {
			super(title, planned);
		}
		
		@Override
		public String toHtmlRow() {
			return String.format("%s%s%s%s%.1f%s%s%.1f%s%s%.1f%s", 
					oblTD, title, cTD,
					orTD, Here2.METERS_TO_MILES*actual, cTD, 
					orTD, Here2.METERS_TO_MILES*planned, cTD,
					orTD, Here2.METERS_TO_MILES*(actual-planned), cTD );
		}
	}

	protected class TimeTracker extends Tracker {
		public TimeTracker( String title, double planned ) {
			super(title, planned);
		}
		
		@Override
		public String toHtmlRow() {
			return String.format("%s%s%s%s%s%s%s%s%s%s%s%s", 
					oblTD, title, cTD,
					orTD, Here2.toPeriod(actual), cTD,
					orTD, Here2.toPeriod(planned), cTD,
					orTD, toDeltaPeriod(actual-planned), cTD );
		}
	}
	
	protected class UpcomingStop {
		String   name;
		double   totalTime;
		double   totalDistance;
		double   timeRemaining;
		double   distanceRemaining;
		
		public UpcomingStop( String name, double totalTime, double totalDistance ) {
			this.name = name;
			this.totalTime = totalTime;
			this.totalDistance = totalDistance;
			update(0, 0);
		}
		
		void update( double timeSoFar, double distanceSoFar) {
			timeRemaining = totalTime - timeSoFar;
			distanceRemaining = totalDistance - distanceSoFar;
		}
		
		public String toHtmlRow() {
			return String.format("%s%s%s%s%s%s%s%.1f%s", 
					oblTD, name, cTD,
					orTD, Here2.toPeriod(timeRemaining), cTD,
					orTD, Here2.METERS_TO_MILES*distanceRemaining, cTD);
		}
	}
	
	protected double  speed;
	protected Maneuver maneuver = null;
	
	protected GeoPosition maneuverFinish = null;
	protected MeanVarianceSampler meanSpeed = null;
	
	protected TimeTracker  drivingTime;
	protected TimeTracker  stoppedTime;
	protected DistanceTracker  distanceDriven;
	protected double           distanceToNext;
	
	protected List<UpcomingStop> upcomingStops;
	protected List<UpcomingStop> availableStops;
	
	protected TripLeg            tripLeg;
	protected String             nextManeuver;
	protected double             distanceToManeuver;
	
	public TravelStatus(TripPlan.TripLeg tripLeg) {
		this.tripLeg = tripLeg;
		RobautoMain.logger.debug( tripLeg.toString() );
		drivingTime = new TimeTracker( "Driving [hh:mm]", tripLeg.legData.trafficTime );
		stoppedTime = new TimeTracker( "Stopped [hh:mm]", 0.0 );
		distanceDriven = new DistanceTracker( "Driving [miles]", tripLeg.legData.distance );
		upcomingStops = new ArrayList<>();
		availableStops = new ArrayList<>();
		for (TripPlan.StopData stopData : tripLeg.stopDataList) {
			UpcomingStop upcomingStop = new UpcomingStop( stopData.name, stopData.trafficTime, stopData.distance );
			if (stopData.use) {
				upcomingStops.add( upcomingStop );
			} else {
				availableStops.add( upcomingStop );
			}
		}
		
	}
	
	public void update( double speed, Maneuver maneuver, Maneuver nextManeuver ) {
		this.speed = speed;
		if (this.maneuver == null || this.maneuver != maneuver) {
			this.maneuver = maneuver;
			int n = maneuver.getShape().size();
			this.maneuverFinish = (n == 0) ? null : maneuver.getShape().get(n-1);
			meanSpeed = new MeanVarianceSampler();
			this.nextManeuver = (nextManeuver == null) ? "" : nextManeuver.getInstruction();
			this.nextManeuver += "<br/>" + maneuver.getInstruction();
		}
		if (meanSpeed != null) {
			meanSpeed.add(speed);
		}
	}
	
	public void stopped( double timeStopped ) {
		stoppedTime.update(timeStopped);
	}
	
	public void update( double timeSoFar, double distanceSoFar) {
		drivingTime.update(timeSoFar);
		distanceDriven.update(distanceSoFar);
		update(upcomingStops, timeSoFar, distanceSoFar );
		update(availableStops, timeSoFar, distanceSoFar );
	}
	
	protected void update(List<UpcomingStop> stops, double timeSoFar, double distanceSoFar ) {
		Iterator<UpcomingStop> it = stops.iterator();
		distanceToNext = 0.0;
		if (!stops.isEmpty()) {
			distanceToNext = stops.get(0).distanceRemaining;
		}
		while (it.hasNext()) {
			UpcomingStop upcomingStop = it.next();
			upcomingStop.update(timeSoFar, distanceSoFar);
			if (upcomingStop.distanceRemaining < 0) {
				it.remove();
			}
		}
	}
	
	public void update(Fix fix) {
		if (maneuverFinish != null) {
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84,
				new GlobalCoordinates(maneuverFinish), new GlobalCoordinates(fix.getLatitude(), fix.getLongitude()));
			distanceToManeuver = curve.getEllipsoidalDistance();
		}
	}

	final static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss  EEE, MMM dd");
	
	@SuppressWarnings("deprecation")
	public String toHtml() {
		if (theme == null) {
			theme = new Theme();
		}
		Chunk c = theme.makeChunk("report#report");
		Chunk t = theme.makeChunk("report#travel");
		
		Date now = new Date();
		t.set("time", format.format(now) );
		
		t.set("currentSpeed", String.format("%3.0f", speed * Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR ));
		t.set("legAverageSpeed", "   ");
		t.set("legExpectedSpeed", "   ");
		if (meanSpeed != null) {
			t.set("legAverageSpeed", String.format("%3.0f", meanSpeed.getMean() * Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR) );
		}
		if (maneuver != null && maneuver.getLength() > 0 && maneuver.getTrafficTime() > 0) {
			double expectedSpeed = maneuver.getLength() / maneuver.getTrafficTime() * Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR;
			t.set("legExpectedSpeed", String.format("%3.0f", expectedSpeed) );
		}
		if (nextManeuver != null) {
			t.set("nextManeuver", nextManeuver );
			t.set("distanceToNext", String.format("%5.1f mi", distanceToManeuver * Here2.METERS_TO_MILES ));
		} else {
			t.set("nextManeuver", "");
			t.set("distanceToNext", "" );
		}
		StringBuffer sb = new StringBuffer();
		sb.append( String.format("<TR>%s</TR>\n", drivingTime.toHtmlRow()) );
		sb.append( String.format("<TR>%s</TR>\n", distanceDriven.toHtmlRow()) );
		sb.append( String.format("<TR>%s</TR>\n", stoppedTime.toHtmlRow()) );
		t.set("trackerRows", sb.toString());
		
		sb = new StringBuffer();
		for (UpcomingStop upcomingStop : upcomingStops) {
			sb.append( String.format("<TR>%s</TR>\n", upcomingStop.toHtmlRow()) );			
		}
		t.set("upcomingRows", sb.toString() );
		sb = new StringBuffer();
		for (UpcomingStop upcomingStop : availableStops) {
			sb.append( String.format("<TR>%s</TR>\n", upcomingStop.toHtmlRow()) );			
		}
		t.set("availableRows", sb.toString() );
		c.set("body", t.toString());
		try {
			String css = IOUtils.toString(new FileInputStream("themes/style.css"));
			c.set("styles", css);
		} catch (Exception x) {}
		return c.toString();
	}

	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		File tripLegsFile = new File("TripLegs.obj");
		RobautoMain.logger = LoggerFactory.getLogger("com.bluelightning.TravelStatus");
		ArrayList<TripLeg> tripLegs = null;
//		if (false) {
//			TripPlan tripPlan = TripPlan.load( new File("RobautoTripPlan.obj"), null );
//			tripLegs = tripPlan.getTripLegs();
//			try {
//				FileOutputStream fos = new FileOutputStream(tripLegsFile);
//				ObjectOutputStream out = new ObjectOutputStream(fos);
//				out.writeObject(tripLegs);
//				out.close();
//			} catch (Exception x) {
//				x.printStackTrace();
//			}
//		}

		try {
			FileInputStream fis = new FileInputStream(tripLegsFile);
			ObjectInputStream in = new ObjectInputStream(fis);
			tripLegs = (ArrayList<TripLeg>) in.readObject();
			in.close();
			
		} catch (Exception x) {
			x.printStackTrace();
		}
		TravelStatus travelStatus = new TravelStatus( tripLegs.get(0) );
		travelStatus.update( 3600.0, 75e3);
		travelStatus.stopped( 300.0 );
		RobautoMain.logger.debug( travelStatus.toHtml() );
	}

}
