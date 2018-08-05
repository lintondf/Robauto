package com.bluelightning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.tools4j.meanvar.MeanVarianceSampler;

import com.bluelightning.Report.Day;
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
					orTD, Here2.METERS_TO_MILES*planned, cTD,
					orTD, Here2.METERS_TO_MILES*actual, cTD, 
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
					orTD, Here2.toPeriod(planned), cTD,
					orTD, Here2.toPeriod(actual), cTD,
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
	protected MeanVarianceSampler meanSpeed = null;
	
	protected TimeTracker  drivingTime;
	protected TimeTracker  stoppedTime;
	protected DistanceTracker  distanceDriven;
	protected double           distanceToNext;
	
	protected List<UpcomingStop> upcomingStops;
	protected List<UpcomingStop> availableStops;
	
	protected TripLeg            tripLeg;
	
	public TravelStatus(TripPlan.TripLeg tripLeg) {
		this.tripLeg = tripLeg;
		System.out.println( tripLeg.toString() );
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
	
	public void update( double speed, Maneuver maneuver ) {
		this.speed = speed;
		if (this.maneuver == null || this.maneuver != maneuver) {
			this.maneuver = maneuver;
			meanSpeed = new MeanVarianceSampler();
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
	
	
	public String toHtml() {
		if (theme == null) {
			theme = new Theme();
		}
		Chunk c = theme.makeChunk("report#report");
		Chunk t = theme.makeChunk("report#travel");
		
		t.set("currentSpeed", String.format("%3.0f", speed * Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR ));
		t.set("legAverageSpeed", "   ");
		t.set("legExpectedSpeed", "   ");
		if (meanSpeed != null) {
			t.set("legAverageSpeed", String.format("%3.0f", meanSpeed.getMean() * Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR) );
		}
		if (maneuver.getLength() > 0 && maneuver.getTrafficTime() > 0) {
			double expectedSpeed = maneuver.getLength() / maneuver.getTrafficTime() * Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR;
			t.set("legExpectedSpeed", String.format("%3.0f", expectedSpeed) );
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

	
	public static void main(String[] args) {
		File tripLegsFile = new File("TripLegs.obj");
		RobautoMain.logger = LoggerFactory.getLogger("com.bluelightning.TravelStatus");
		ArrayList<TripLeg> tripLegs = null;
		if (false) {
			TripPlan tripPlan = TripPlan.load( new File("RobautoTripPlan.obj"), null );
			tripLegs = tripPlan.getTripLegs();
			try {
				FileOutputStream fos = new FileOutputStream(tripLegsFile);
				ObjectOutputStream out = new ObjectOutputStream(fos);
				out.writeObject(tripLegs);
				out.close();
			} catch (Exception x) {
				x.printStackTrace();
			}
		}

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
		System.out.println( travelStatus.toHtml() );
	}

}
