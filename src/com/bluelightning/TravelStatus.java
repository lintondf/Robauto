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
import com.bluelightning.ManeuverMetrics.ClosestManeuver;
import com.bluelightning.data.TripPlan;
import com.bluelightning.data.TripPlan.TripLeg;
import com.bluelightning.json.Maneuver;
import com.x5.template.Chunk;
import com.x5.template.Theme;

public class TravelStatus {
	
	static Theme theme;
	
	protected static final String oTD = "<TD>";
	protected static final String oTDs = "<TD colspan='2'>";
	protected static final String oTDf = "<TD style='font-size:48pt'>";
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
					orTD, planned, cTD, orTD, actual, cTD, oTDf, (actual-planned), cTD );
		}
	}
	
	protected class DistanceTracker extends Tracker {
		
		public DistanceTracker( String title, double planned ) {
			super(title, planned);
		}
		
		public String value() {
			return String.format("%.1f", Here2.METERS_TO_MILES*actual);
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
		
		public String value() {
			return Here2.toPeriod(actual);
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
		GeoPosition where;
		ClosestManeuver closest;
		String   name;
		String   markedUpName;
		double   totalTime;
		double   totalDistance;
		double   timeRemaining;
		double   distanceRemaining;
		boolean  hasGas;
		
		final static String onDeckRow1Style = "<TD colspan='2' style='font-size:48pt;font-weight:bolder;text-align:left;background-color:cyan'>";
		final static String onDeckRow2Style = "<TD style='font-size:96pt;font-weight:bolder;text-align:right;background-color:cyan'>";

		
		public UpcomingStop( double latitude, double longitude, String name, double totalTime, double totalDistance, String fuelAvailable ) {
			this.where = new GeoPosition( latitude, longitude );
			this.closest = ManeuverMetrics.findCurrentManeuver( this.where );
//			System.out.printf("%10.7f,%10.7f,%10.7f,%10.7f,%s\n", latitude, longitude, 
//					closest.closestPoint.y, closest.closestPoint.x, name );
			this.name = name;
			this.markedUpName = name;
			this.hasGas = false;
			if (! fuelAvailable.equals("None")) {
				this.markedUpName = "<b>" + this.markedUpName + "</b>";
				this.hasGas = true;
			}
			this.totalTime = totalTime;
			this.totalDistance = totalDistance;
			update(totalTime, totalDistance);
		}
		
		public String toString() {
			return String.format("%s", name );
		}
		
		void update( double timeRemaining, double distanceRemaining) {
			this.timeRemaining = timeRemaining;
			this.distanceRemaining = distanceRemaining;
		}
		
		public String[] toHtmlCells() {
			return new String[] {
				String.format("%s%s%s", 
						onDeckRow1Style, markedUpName, cTD),
				String.format("%s%s%s", 
						onDeckRow2Style, Here2.toPeriod(timeRemaining), cTD),
				String.format("%s%.1f%s", 
						onDeckRow2Style, Here2.METERS_TO_MILES*distanceRemaining, cTD)
			};
		}
		
		public String toHtmlRow() {
			return String.format("%s%s%s%s%s%s%s%.1f%s", 
					oblTD, markedUpName, cTD,
					orTD, Here2.toPeriod(timeRemaining), cTD,
					orTD, Here2.METERS_TO_MILES*distanceRemaining, cTD);
		}
	}
	
	protected double  speed;
	protected GPS.Fix lastFix;
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
		stoppedTime = new TimeTracker( "Stopped [mm:ss]", 0.0 );
		distanceDriven = new DistanceTracker( "Driving [miles]", tripLeg.legData.distance );
		upcomingStops = new ArrayList<>();
		availableStops = new ArrayList<>();
		for (TripPlan.StopData stopData : tripLeg.stopDataList) {
			UpcomingStop upcomingStop = new UpcomingStop( stopData.latitude, stopData.longitude, stopData.name, stopData.trafficTime, stopData.distance, stopData.fuelAvailable );
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
	
	UpcomingStop nextStop = null;
	
	public UpcomingStop update( double timeSoFar, double distanceSoFar, 
			ManeuverMetrics.ClosestManeuver currentManeuver, List<ManeuverMetrics> maneuverMetrics) {
		drivingTime.update(timeSoFar);
		distanceDriven.update(distanceSoFar);
		update(upcomingStops, currentManeuver, maneuverMetrics );
		if (! upcomingStops.isEmpty() ) {
			if (nextStop == null) {
				nextStop = upcomingStops.get(0);
			}
			if (!nextStop.equals(upcomingStops.get(0))) {
				nextStop = upcomingStops.get(0);
			}
		}
		update(availableStops, currentManeuver, maneuverMetrics );
		return nextStop;
	}
	
	protected void update(List<UpcomingStop> stops, ManeuverMetrics.ClosestManeuver currentManeuver, List<ManeuverMetrics> maneuverMetrics ) {
//		for (int i = 0; i < maneuverMetrics.size(); i++ ) {
//			ManeuverMetrics metrics = maneuverMetrics.get(i);
//			System.out.printf("%d, %10.0f, %s\n", i, metrics.maneuver.getLength(), metrics.maneuver.getInstruction() );
//		}
		int iNow = maneuverMetrics.indexOf(currentManeuver.metrics);
		double nowSpeed = currentManeuver.metrics.maneuver.getLength() / currentManeuver.metrics.maneuver.getTravelTime();
		if (iNow >= 0) {
			Iterator<UpcomingStop> it = stops.iterator();
			while (it.hasNext()) {
				UpcomingStop upcomingStop = it.next();
				double timeToStop = currentManeuver.distanceToEnd / nowSpeed;
				double distanceToStop = currentManeuver.distanceToEnd;
				int iStop = maneuverMetrics.indexOf( upcomingStop.closest.metrics );
//				System.out.printf("%d,%d,%s\n",iNow, iStop, upcomingStop.name );
				if (iStop == iNow) {
					timeToStop -= upcomingStop.closest.distanceToEnd / nowSpeed;
					distanceToStop -= upcomingStop.closest.distanceToEnd;
				} else if (iStop > iNow) {
					for (int i = iNow+1; i < iStop; i++) {
						ManeuverMetrics metrics = maneuverMetrics.get(i);
						timeToStop += metrics.maneuver.getTravelTime();
						distanceToStop += metrics.maneuver.getLength();
					}
					double stopSpeed = upcomingStop.closest.metrics.maneuver.getLength() / upcomingStop.closest.metrics.maneuver.getTravelTime();
					timeToStop += upcomingStop.closest.distanceFromStart / stopSpeed;
					distanceToStop += upcomingStop.closest.distanceFromStart;
				}
				upcomingStop.update(timeToStop, distanceToStop);
				if (upcomingStop.distanceRemaining < 0) {
					it.remove();
				}
			}
		}
	}
	
	public void update(Fix fix) {
		lastFix = fix;
		if (maneuverFinish != null) {
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84,
				new GlobalCoordinates(maneuverFinish), new GlobalCoordinates(fix.getLatitude(), fix.getLongitude()));
			distanceToManeuver = curve.getEllipsoidalDistance();
		}
	}

	final static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss EEE MM/dd");
	
	public String toDrivingHtml() {
		if (theme == null) {
			theme = new Theme();
		}
		Chunk c = theme.makeChunk("report#report");
		Chunk t = theme.makeChunk("report#driving");
		
		Date now = new Date();
		if (lastFix != null) {
			now = lastFix.date;
			//t.set("where", String.format("%10.6f %10.6f", lastFix.getLatitude(), lastFix.getLongitude()));
		}
		t.set("time", format.format(now) );
		t.set("drivingTime", drivingTime.value() );
		t.set("drivingDistance", distanceDriven.value() );
		t.set("stoppedTime", stoppedTime.value() );
		
		StringBuffer sb = new StringBuffer();
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
	
	public String toNextUpHtml() {
		if (theme == null) {
			theme = new Theme();
		}
		Chunk c = theme.makeChunk("report#report");
		Chunk t = theme.makeChunk("report#next");
		
		UpcomingStop nextStop = null;
		UpcomingStop nextGas = null;
		
		for (UpcomingStop upcomingStop : upcomingStops) {
			if (nextStop == null)
				nextStop = upcomingStop;
			if (nextGas == null && upcomingStop.hasGas)
				nextGas = upcomingStop;
		}
		for (UpcomingStop upcomingStop : availableStops) {
			if (nextGas == null && upcomingStop.hasGas)
				nextGas = upcomingStop;
		}
		
		StringBuffer sb = new StringBuffer();
		if (nextStop != null) {
			String[] cells = nextStop.toHtmlCells();
			String rows = String.format("<TR>%s</TR>\n<TR>%s%s</TR>\n", cells[0], cells[1], cells[2] );
			sb.append(rows);
			if (nextGas != null && nextGas != nextStop) {
				cells = nextGas.toHtmlCells();
				rows = String.format("<TR>%s</TR>\n<TR>%s%s</TR>\n", cells[0], cells[1], cells[2] );
				sb.append(rows);				
			}
		}
		t.set("onDeck", sb.toString());
		
		c.set("body", t.toString());
		try {
			String css = IOUtils.toString(new FileInputStream("themes/style.css"));
			c.set("styles", css);
		} catch (Exception x) {}
		return c.toString();
	}
	
	@SuppressWarnings("deprecation")
	public String toOldHtml() {
		if (theme == null) {
			theme = new Theme();
		}
		Chunk c = theme.makeChunk("report#report");
		Chunk t = theme.makeChunk("report#travel");
		
		Date now = new Date();
		if (lastFix != null) {
			now = lastFix.date;
			t.set("where", String.format("%10.6f %10.6f", lastFix.getLatitude(), lastFix.getLongitude()));
		}
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

}
