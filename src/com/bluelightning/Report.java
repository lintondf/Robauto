package com.bluelightning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.bluelightning.data.TripPlan;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.model.LatLng;
//import com.graphhopper.util.PointList;
//import com.graphhopper.util.shapes.GHPoint3D;
import com.x5.template.Chunk;
import com.x5.template.Theme;

public class Report implements Serializable {

	public static double MPG = Configuration.getSingleton().milesPerGallon;
	public static double FUEL_CAPACITY = Configuration.getSingleton().fuelInGallons;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	final static String HEADER_FONT = "80pt";
	final static String ROW_FONT = "48pt";
	final static String DETAIL_FONT = "48pt";
	
	protected static void setFonts( Chunk c) {
		c.set("headerFontSize", HEADER_FONT);
		c.set("rowFontSize", ROW_FONT);
		c.set("detailFontSize", DETAIL_FONT);			
	}
	
	List<Day> days;
	static Theme theme;

	public static class Day {
		String day;
		String duration;
		String distance;
		List<Step> steps;

		Step firstStep;
		Step lastStep;
		
		String driver0Total;
		String driver1Total;
		String imbalance;

		public Day() {
			this.steps = new ArrayList<Step>();
		}

		public void add(Step step) {
			steps.add(step);
			if (firstStep == null) {
				firstStep = step;
			}
			lastStep = step;
		}

		public void finish(int driver0Minutes, int driver1Minutes) {
			int imbalanceMinutes = driver0Minutes - driver1Minutes;
			driver0Total = String.format("%d:%02d", driver0Minutes/60, driver0Minutes%60);
			driver1Total = String.format("%d:%02d", driver1Minutes/60, driver1Minutes%60);
			imbalance = String.format("%s%d:%02d", (imbalanceMinutes >= 0) ? "+" : "-", 
					Math.abs(imbalanceMinutes)/60, Math.abs(imbalanceMinutes)%60);
			int totalHours = 0;
			int totalMinutes = 0;
			int totalDistance = 0;
			for (Step step : steps) {
				if (step.drive != null) {
					String[] duration = (step.drive.leg1Duration!=null) ? 
							step.drive.leg1Duration.split(":") :
								step.drive.leg2Duration.split(":");
					int hours = Integer.parseInt(duration[0]);
					int minutes = Integer.parseInt(duration[1]);
					totalMinutes += minutes;
					if (totalMinutes >= 60) {
						totalHours += totalMinutes / 60;
						totalMinutes = totalMinutes % 60;
					}
					totalHours += hours;
					int distance = Integer.parseInt(step.drive.legDistance);
					totalDistance += distance;
					step.drive.totalDistance = String.format("%d",
							totalDistance);
					step.drive.totalDuration = String.format("%d:%02d",
							totalHours, totalMinutes);
				}
			}
			this.distance = String.format("%d", totalDistance);
			this.duration = String.format("%d:%02d", totalHours, totalMinutes);
		}

		public String toHtml() {
			Chunk c = theme.makeChunk("report#day");
			setFonts(c);
			c.set("dayName", day);
			c.set("dayDuration", duration);
			c.set("dayDistance", distance);
			StringBuffer sb = new StringBuffer();
			for (Step step : steps) {
				sb.append(step.toHtml());
			}
			Chunk t = theme.makeChunk("report#dayTotalRow");
			setFonts(c);
			t.set("imbalance", imbalance);
			t.set("leg1Total", driver0Total);
			t.set("leg2Total", driver1Total);
			t.set("totalDuration", duration);
			sb.append( t.toString() );
			c.set("rows", sb.toString());
			return c.toString();
		}

		public String getDay() {
			return day;
		}

		public String getDuration() {
			return duration;
		}

		public String getDistance() {
			return distance;
		}

		public List<Step> getSteps() {
			return steps;
		}

		public Step getFirstStep() {
			return firstStep;
		}

		public Step getLastStep() {
			return lastStep;
		}
	}

	public static class Step {
		String stepName;
		String placeName;
		String placeAddress;
		String refuel;
		Drive drive;
		public double latitude;
		public double longitude;

		public Step() {
		}

		public String toHtml() {
			StringBuffer sb = new StringBuffer();
			Chunk t = theme.makeChunk("report#titleRow");
			setFonts(t);
			//	https://www.google.com/maps/@35.5377787,-78.2506305,16.29z?shorturl=1
			String placeLink = String.format("<A TARGET='_blank' HREF='https://www.google.com/maps/@%.7f,%.7f,16.29z/data=!5m1!1e1'>%s</A>", latitude, longitude, placeName );
			t.set("stepName", stepName);
			t.set("placeName", placeLink);
			t.set("placeAddress", placeAddress);
			t.set("refuel", refuel);
			sb.append(t.toString());
			if (drive != null) {
				sb.append(drive.toHtml());
			}
			return sb.toString();
		}
	}

	public static class Drive {
		String leg1Duration;
		String leg2Duration;
		String totalDuration;
		String legDistance;
		String totalDistance;
		String fuelUsed;
		String fuelRemaining;

		public String toHtml() {
			Chunk d = theme.makeChunk("report#detailRow");
			setFonts(d);
			d.set("leg1Duration", leg1Duration);
			d.set("leg2Duration", leg2Duration);
			d.set("totalDuration", totalDuration);
			d.set("legDistance", legDistance);
			d.set("totalDistance", totalDistance);
			d.set("fuelUsed", fuelUsed);
			d.set("fuelRemaining", fuelRemaining);
			return d.toString();
		}
	}

	protected double mpg = MPG;
	protected double fuelLevel = FUEL_CAPACITY; // gallons
	protected Day lastDay = null;

	public double fillUp() {
		return 80.0 - fuelLevel;
	}

	public Report() {
		fuelLevel = RobautoMain.startingFuel;
		this.days = new ArrayList<Day>();
	}

	public void add(Day day) {
		days.add(day);
		lastDay = day;
	}

	public void depart(String place, String address, double refuel) {
		Day day = new Day();
		add(day);
		int n = days.size();
		day.day = String.format("Day %d", n);
		Step step = new Step();
		day.add(step);
		step.placeName = place;
		step.placeAddress = address;
		step.stepName = "Depart";
		driver0Minutes = 0;
		driver1Minutes = 0;
		if (refuel > 0.0) {
			fuelLevel += refuel;
			step.refuel = String.format("%.0f", refuel);
		}
	}
	
	protected int  driver0Minutes = 0;
	protected int  driver1Minutes = 0;
	

	public void depart(double refuel) {
		depart(lastDay.lastStep.placeName, lastDay.lastStep.placeAddress,
				refuel);
		driver0Minutes = 0;
		driver1Minutes = 0;
	}

	public void arrive(double refuel, String toPlace, String toAddress) {
		lastDay.lastStep.stepName = "Arrive";
		lastDay.lastStep.placeName = toPlace;
		lastDay.lastStep.placeAddress = toAddress;
		if (refuel > 0.0) {
			fuelLevel += refuel;
			lastDay.lastStep.refuel = String.format("+%.0f", refuel);
			lastDay.lastStep.stepName = "Arrive/Fuel";
		}
		lastDay.finish(driver0Minutes, driver1Minutes);
	}

	public void drive(int driver, int hours, int minutes, double distance) {
		Drive d = new Drive();
		d.legDistance = String.format("%.0f", distance);
		if (driver == 0) {
			d.leg1Duration = String.format("%d:%02d", hours, minutes);
			driver0Minutes += 60*hours + minutes;
		} else {
			d.leg2Duration = String.format("%d:%02d", hours, minutes);
			driver1Minutes += 60*hours + minutes;
		}
		double fuelUsed = distance / mpg;
		d.fuelUsed = String.format("%.0f", fuelUsed);
		fuelLevel -= fuelUsed;
		d.fuelRemaining = String.format("%.0f", fuelLevel);
		lastDay.lastStep.drive = d;
	}

	public void stop(TripPlan.StopData stop, double refuel) {
		String toPlace = stop.name;
		String toAddress = stop.getAddress(); 
		Step step = new Step();
		int n = lastDay.steps.size();
		step.placeName = toPlace;
		step.placeAddress = toAddress;
		step.latitude = stop.getLatitude();
		step.longitude = stop.getLongitude();
		if (refuel > 0.0) {
			fuelLevel += refuel;
			step.refuel = String.format("+%.0f", refuel);
			step.stepName = String.format("%d. Fuel", n);
		} else {
			step.stepName = String.format("%d. Stop", n);			
		}
		lastDay.add(step);
	}
	
	protected String formatUserLabel(String userLabel) {
		String[] fields = userLabel.split("/");
		if (fields.length < 2)
			return fields[0];
		return String.format("%s<br/><small>%s</small>", fields[0], fields[1] );
	}

	
	public void add(int nDrivers, TripPlan.LegData legData,
			TripPlan.DriverAssignments driverAssignments) {
		
		this.depart(formatUserLabel(legData.startLabel), "", 0.0);
		Iterator<TripPlan.DriverAssignments.Turn> iTurn = driverAssignments.turns.iterator();
		double lastDistance = 0.0;
		while (iTurn.hasNext()) {
			TripPlan.DriverAssignments.Turn turn = iTurn.next();
			TripPlan.StopData stopData = turn.stop;
			Double driveTime = turn.driveTime;
			int hours = (int) (driveTime / 3600.0);
			int minutes = ((int) (driveTime / 60.0)) % 60;
			double stepDistance = stopData.distance - lastDistance;
			lastDistance = stopData.distance;
			this.drive(turn.driver, hours, minutes, stepDistance * Here2.METERS_TO_MILES);
			double refuel = (stopData.refuel) ? this.fillUp() : 0.0;
			this.stop(stopData, refuel );
		}
		this.arrive(0.0, formatUserLabel(legData.endLabel), "");
	}
	

	@SuppressWarnings("deprecation")
	public String toHtml() {
		if (theme == null) {
			theme = new Theme();
		}
		Chunk c = theme.makeChunk("report#report");
		setFonts(c);
		StringBuffer sb = new StringBuffer();
		for (Day day : days) {
			sb.append(day.toHtml());
		}
		c.set("body", sb.toString());
		try {
			String css = IOUtils.toString(new FileInputStream("themes/style.css"));
			c.set("styles", css);
		} catch (Exception x) {}
		return c.toString();
	}

	@SuppressWarnings("deprecation")
	public String toHtml(Day day) {
		if (theme == null) {
			theme = new Theme();
		}
		Chunk c = theme.makeChunk("report#report");
		setFonts(c);
		StringBuffer sb = new StringBuffer();
		sb.append(day.toHtml());
		c.set("body", sb.toString());
		try {
			String css = IOUtils.toString(new FileInputStream("themes/style.css"));
			c.set("styles", css);
		} catch (Exception x) {}
		return c.toString();
	}

	public static void test_basic(String[] args) {
		Report report = new Report();

		Day day = new Day();
		day.day = "Day 1";
		day.distance = "283 mi";
		day.duration = "1.30 hrs";
		Step step = new Step();
		step.stepName = "Depart";
		step.placeName = "Pilot";
		step.placeAddress = "491 St Marys Rd, St Marys, GA";

		step.drive = new Drive();
		step.drive.leg1Duration = "1:30";
		step.drive.totalDuration = "1:30";
		step.drive.legDistance = "95";
		step.drive.totalDistance = "95";
		step.drive.fuelRemaining = "65";
		step.drive.fuelUsed = "15";
		day.add(step);

		step = new Step();
		step.stepName = "Arrive";
		step.placeName = "Home";
		step.placeAddress = "7 Manor Lane, Sullivan, ME 04664";
		day.add(step);
		report.add(day);

		try {
			PrintWriter out = new PrintWriter("report.html");
			out.println(report.toHtml());
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(report);
		System.out.println(json);
		//
		// report = gson.fromJson(json, Report.class );
	}

	public void serialize(File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static Report deserialize(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fis);
			Report report = (Report) in.readObject();
			in.close();
			return report;
		} catch (Exception x) {
			x.printStackTrace();
			return null;
		}
	}
	
//	public static void writeKmlWaypoints( String filePath, List<Waypoint> points ) {
//		ArrayList<LatLng> p = new ArrayList<>();
//		for (Waypoint w : points)
//			p.add(w.getLatLng());
//		writeKml( filePath, p );
//	}
	
	public static void writeKml( String filePath, LatLng origin, List<LatLng> points, LatLng destination ) {
		Theme theme = new Theme();
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%g,%g,0\n", origin.lng, origin.lat));
		for (LatLng p : points) {
			sb.append(String.format("%g,%g,0\n", p.lng, p.lat));
		}
		sb.append(String.format("%g,%g,0\n", destination.lng, destination.lat));
		Chunk t = theme.makeChunk("report#kml");
		t.set("coordinates", sb.toString());
		
		try {
			PrintWriter out = new PrintWriter(filePath);
			out.println(t.toString());
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	
	public static void writeKml( String filePath, List<LatLng> points ) {
		Theme theme = new Theme();
		StringBuffer sb = new StringBuffer();
		for (LatLng p : points) {
			sb.append(String.format("%g,%g,0\n", p.lng, p.lat));
		}
		Chunk t = theme.makeChunk("report#kml");
		t.set("coordinates", sb.toString());
		
		try {
			PrintWriter out = new PrintWriter(filePath);
			out.println(t.toString());
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

//	public static void writeKml( String filePath, PointList points ) {
//		Theme theme = new Theme();
//		StringBuffer sb = new StringBuffer();
//		for (GHPoint3D p : points) {
//			sb.append(String.format("%g,%g,0\n", p.lon, p.lat));
//		}
//		Chunk t = theme.makeChunk("report#kml");
//		t.set("coordinates", sb.toString());
//		
//		try {
//			PrintWriter out = new PrintWriter(filePath);
//			out.println(t.toString());
//			out.close();
//		} catch (Exception x) {
//			x.printStackTrace();
//		}
//	}

	public static void mainKml(String[] args) {
		Theme theme = new Theme();
		StringBuffer sb = new StringBuffer();
		Chunk t = theme.makeChunk("report#kml");
		t.set("coordinates", "45,75");
		sb.append(t.toString());
		System.out.println(sb.toString());
	}
	
	
	public List<Day> getDays() {
		return days;
	}
}
