package com.bluelightning;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.model.LatLng;
//import com.graphhopper.util.PointList;
//import com.graphhopper.util.shapes.GHPoint3D;
import com.x5.template.Chunk;
import com.x5.template.Theme;
import com.x5.util.Base64;

public class Report implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<Day> days;
	static Theme theme;

	public static class Day {
		String day;
		String duration;
		String distance;
		List<Step> steps;

		Step firstStep;
		Step lastStep;

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

		public void finish() {
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
			c.set("dayName", day);
			c.set("dayDuration", duration);
			c.set("dayDistance", distance);
			StringBuffer sb = new StringBuffer();
			for (Step step : steps) {
				sb.append(step.toHtml());
			}
			c.set("rows", sb.toString());
			return c.toString();
		}
	}

	public static class Step {
		String stepName;
		String placeName;
		String placeAddress;
		String refuel;
		Drive drive;

		public Step() {
		}

		public String toHtml() {
			StringBuffer sb = new StringBuffer();
			Chunk t = theme.makeChunk("report#titleRow");
			t.set("stepName", stepName);
			t.set("placeName", placeName);
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

	protected double mpg = 7.0;
	protected double fuelLevel = 80.0; // gallons
	protected Day lastDay = null;

	public double fillUp() {
		return 80.0 - fuelLevel;
	}

	public Report() {
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
		if (refuel > 0.0) {
			fuelLevel += refuel;
			step.refuel = String.format("%.0f", refuel);
		}
	}

	public void depart(double refuel) {
		depart(lastDay.lastStep.placeName, lastDay.lastStep.placeAddress,
				refuel);
	}

	public void arrive(double refuel) {
		lastDay.lastStep.stepName = "Arrive";
		if (refuel > 0.0) {
			fuelLevel += refuel;
			lastDay.lastStep.refuel = String.format("%.0f", refuel);
		}
		lastDay.finish();
	}

	public void drive(int driver, int hours, int minutes, double distance) {
		Drive d = new Drive();
		d.legDistance = String.format("%.0f", distance);
		if (driver == 0)
			d.leg1Duration = String.format("%d:%02d", hours, minutes);
		else
			d.leg2Duration = String.format("%d:%02d", hours, minutes);
		double fuelUsed = distance / mpg;
		d.fuelUsed = String.format("%.0f", fuelUsed);
		fuelLevel -= fuelUsed;
		d.fuelRemaining = String.format("%.0f", fuelLevel);
		lastDay.lastStep.drive = d;
	}

	public void stop(String toPlace, String toAddress, double refuel) {
		Step step = new Step();
		int n = lastDay.steps.size();
		step.placeName = toPlace;
		step.placeAddress = toAddress;
		step.stepName = String.format("%d. Stop", n);
		if (refuel > 0.0) {
			fuelLevel += refuel;
			step.refuel = String.format("%.0f", refuel);
		}
		lastDay.add(step);
	}

	public String toHtml() {
		if (theme == null) {
			theme = new Theme();
		}
		Chunk c = theme.makeChunk("report#report");
		StringBuffer sb = new StringBuffer();
		for (Day day : days) {
			sb.append(day.toHtml());
		}
		c.set("body", sb.toString());
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
	
	
	public static void main(String[] args) {
		Report report = new Report();
		report.depart("Home", "3533 Carambola Circle, Melbourne, FL", 0.0);
		report.drive(0, 2, 48, 195);
		report.stop("Pilot", "491 St Marys Rd, St Marys, GA", report.fillUp());
		report.drive(1, 0, 48, 45);
		report.stop("Jekyll Island SP",
				"1199 Beach View Dr N, Jekyll Island, GA", 0.0);
		report.arrive(0.0);

		report.depart(0.0);
		report.drive(0, 4, 4, 281);
		report.stop("Pilot", "1504 SC-38, Latta, SC 29565, USA",
				report.fillUp());
		report.drive(1, 1, 44, 116);
		report.stop("Walmart", "1299 N Brightleaf Blvd, Smithfield, NC", 0.0);
		report.arrive(0.0);

		report.depart(0.0);
		report.drive(0, 3, 39, 250);
		report.stop("Costco", "2708 Potomac Mills Cir, Woodbridge, VA",
				report.fillUp());
		report.drive(1, 1, 18, 65);
		report.stop("Patapsco SP",
				"8099 Park Dr, Ellicott City, MD 21043, USA", 0.0);
		report.arrive(0.0);

		report.depart(0.0);
		report.drive(0, 4, 46, 284);
		report.stop("Mills-Norie SP",
				"84 Campground Hill, Staatsburg, NY 12580", 0.0);
		report.arrive(0.0);

		report.depart(0.0);
		report.drive(0, 1, 34, 87);
		report.stop("Lee Service Plaza", "I-90 Eastbound", report.fillUp());
		report.drive(1, 1, 17, 72);
		report.stop("Walmart",
				"7 Dowling Village Boulevard, North Smithfield, RI�", 0.0);
		report.arrive(0.0);

		report.depart(0.0);
		report.drive(0, 2, 47, 173);
		report.stop("Freeport", "80 Depot St,Freeport, ME 04032", 0.0);
		report.drive(1, 0, 31, 32);
		report.stop("West Gardiner Service Plaza", "I-295/I-95 Junction", 0.0);
		report.arrive(0.0);

		report.depart(0.0);
		report.drive(0, 1, 31, 85);
		report.stop("Maple Lane Farm",
				"251 Upper-Charleston Rd, Charleston, ME�", 0.0);
		report.drive(1, 1, 14, 57);
		report.stop("Irving Oil", "High Street Ellsworth, ME", report.fillUp());
		report.drive(0, 0, 17, 11);
		report.stop("Home", "7 Manor Lane, Sullivan, ME 04664", 0.0);
		report.arrive(0.0);

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
	}
}
