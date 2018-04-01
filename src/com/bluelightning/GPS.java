package com.bluelightning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

public class GPS {
	
	protected static File gpsDat = new File("gpssave.dat");
	protected static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	protected static GeodeticCalculator geoCalc = new GeodeticCalculator();
	protected static Ellipsoid wgs84 = Ellipsoid.WGS84;
	
	
	public static class Fix extends GeoPosition {

		double speed;
		double heading;
		double movement;  // [m] distance traveled since last fix
		Date   date;
		
		public Fix() {}
		
		public Fix( Date when, GeoPosition position ) {
			this.date = when;
			this.latitude = position.getLatitude();
			this.longitude = position.getLongitude();
			this.speed = 0.0;
			this.heading = 0.0;
		}
		
		public String toString() {
			return String.format("%s, %12.8f, %12.8f, %5.0f, %6.1f %6.1f", date.toString(), latitude, longitude, 
					heading, Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR*speed,  Here2.METERS_TO_MILES*movement );
		}
		
		protected static Fix readGpsDataFile() {
			Fix fix = new Fix();
			try {
				List<String> lines = IOUtils.readLines( new FileInputStream(gpsDat) );
				StringBuffer contents = new StringBuffer();
				lines.forEach( line -> {
					contents.append("[");
					contents.append(line);
					contents.append("],");
				});
				//lines.forEach(System.out::println);
				String[] fields = lines.get(0).split("=");
				if (fields[0].equalsIgnoreCase("Latitude")) {
					double value = Double.parseDouble( fields[1].substring(1));
					if (fields[1].startsWith("S"))
						value *= -1.0;
					fix.latitude = value;
				} else {
					RobautoMain.logger.error("Bad gpssave.dat :" + contents.toString());
					return null;
				}
				fields = lines.get(1).split("=");
				if (fields[0].equalsIgnoreCase("Longitude")) {
					double value = Double.parseDouble( fields[1].substring(1));
					if (fields[1].startsWith("W"))
						value *= -1.0;
					fix.longitude = value;
				} else {
					RobautoMain.logger.error("Bad gpssave.dat :" + contents.toString());
					return null;
				}
				fields = lines.get(2).split("=");
				if (fields[0].equalsIgnoreCase("UTCDate")) {
					int year = Integer.parseInt(fields[1].substring(0, 2));
					int month = Integer.parseInt(fields[1].substring(2, 4));
					int day = Integer.parseInt(fields[1].substring(4, 6));
					calendar.set(year, month, day, 0, 0, 0);
				} else {
					RobautoMain.logger.error("Bad gpssave.dat :" + contents.toString());
					return null;
				}
				fields = lines.get(3).split("=");
				if (fields[0].equalsIgnoreCase("UTCTime")) {
					int hour = Integer.parseInt(fields[1].substring(0, 2));
					int minutes = Integer.parseInt(fields[1].substring(2, 4));
					int seconds = Integer.parseInt(fields[1].substring(4, 6));
					calendar.set(Calendar.HOUR_OF_DAY, hour);
					calendar.set(Calendar.MINUTE, minutes);
					calendar.set(Calendar.SECOND, seconds);
					fix.date = calendar.getTime();
				} else {
					RobautoMain.logger.error("Bad gpssave.dat :" + contents.toString());
					return null;
				}
				fields = lines.get(4).split("=");
				if (fields[0].equalsIgnoreCase("Heading")) {
					fix.heading = Double.parseDouble(fields[1]);
					fix.speed = 0.0;
				} else {
					RobautoMain.logger.error("Bad gpssave.dat :" + contents.toString());
					return null;
				}
				return fix;
			} catch (IOException e) {
				RobautoMain.logger.error("Bad gpssave.dat: ", e );
				return null;
			}
		}
	}
	
	protected Fix lastFix = null;
	
	protected void addObservation( Fix fix ) {
		if (lastFix != null) {
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84, 
					new GlobalCoordinates(lastFix), new GlobalCoordinates(fix));
			fix.heading = curve.getAzimuth();
			double dt = 1e-3 * (double)(fix.date.getTime() - lastFix.date.getTime());
			if (dt > 0.0 && Double.isFinite(curve.getEllipsoidalDistance())) {
				fix.movement = curve.getEllipsoidalDistance();
				fix.speed = fix.movement / dt;
				Events.eventBus.post( new Events.GpsEvent(fix) );
			}
		}
		lastFix = fix;
	}
	
	
	public void debugSetup(final Iterator<GeoPosition> it) {
		Thread t = new Thread( new Runnable() {
			@Override
			public void run() {
				Date date = new Date();
				while (it.hasNext()) {
					final GeoPosition position = it.next();
					try { Thread.sleep(100); } catch (Exception x) {}
					date = new Date( date.getTime() + 10000L );
					addObservation( new Fix( date, position ) ); 
				}
			}
		} );
		t.start();
	}
	
	public static void main(String[] args) {
		GPS gps = new GPS();
		GPS.Fix fix = GPS.Fix.readGpsDataFile();
		System.out.println( fix.toString() );
	}

}
