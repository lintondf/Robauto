package com.bluelightning;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.IOUtils;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.gps.SerialGps;
import com.bluelightning.gps.GpsComplete;
import com.bluelightning.gps.ISerialGps;
import com.bluelightning.gps.NMEA.GpsState;
import com.fazecast.jSerialComm.SerialPort;

public class GPS {

	protected static File gpsDat = new File("//SurfacePro3/NA/save/gpssave.dat");
	protected static Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	protected static GeodeticCalculator geoCalc = new GeodeticCalculator();
	protected static Ellipsoid wgs84 = Ellipsoid.WGS84;

	protected ObjectInputStream gpsOis;
	protected ObjectOutputStream gpsOos;

	public static class Fix extends GeoPosition implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		double speed;
		double heading;
		double movement; // [m] distance traveled since last fix
		Date date;

		public Fix() {
		}

		public Fix(GpsState state) {
			super(state.lat, state.lon);
			date = state.toDate();
			speed = state.velocity * 0.514444; // knots -> m/s
			heading = state.dir;
		}

		public Fix(Date when, GeoPosition position) {
			this.date = when;
			this.latitude = position.getLatitude();
			this.longitude = position.getLongitude();
			this.speed = 0.0;
			this.heading = 0.0;
		}

		public String toString() {
			return String.format("%s, %12.8f, %12.8f, %5.0f, %6.1f %6.1f", date.toString(), latitude, longitude, heading,
					Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR * speed, Here2.METERS_TO_MILES * movement);
		}

		@SuppressWarnings("deprecation")
		protected static Fix readGpsDataFile() {
			Fix fix = new Fix();
			try {
				List<String> lines = IOUtils.readLines(new FileInputStream(gpsDat));
				StringBuffer contents = new StringBuffer();
				lines.forEach(line -> {
					contents.append("[");
					contents.append(line);
					contents.append("],");
				});
				// lines.forEach(System.out::println);
				String[] fields = lines.get(0).split("=");
				if (fields[0].equalsIgnoreCase("Latitude")) {
					double value = Double.parseDouble(fields[1].substring(1));
					if (fields[1].startsWith("S"))
						value *= -1.0;
					fix.latitude = value;
				} else {
					RobautoMain.logger.error("Bad gpssave.dat :" + contents.toString());
					return null;
				}
				fields = lines.get(1).split("=");
				if (fields[0].equalsIgnoreCase("Longitude")) {
					double value = Double.parseDouble(fields[1].substring(1));
					if (fields[1].startsWith("W"))
						value *= -1.0;
					fix.longitude = value;
				} else {
					RobautoMain.logger.error("Bad gpssave.dat :" + contents.toString());
					return null;
				}
				fields = lines.get(2).split("=");
				if (fields[0].equalsIgnoreCase("UTCDate")) {
					if (fields[1].length() < 6)
						fields[1] = "0" + fields[1];
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
					if (fields[1].length() < 6)
						fields[1] = "0" + fields[1];
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
				RobautoMain.logger.error("Bad gpssave.dat: ", e);
				e.printStackTrace();
				return null;
			}
		}
	}

	protected Fix lastFix = null;

	protected boolean addObservation(Fix fix) {
		if (lastFix != null) {
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84, new GlobalCoordinates(lastFix),
					new GlobalCoordinates(fix));
			fix.heading = curve.getAzimuth();
			if (Double.isNaN(fix.heading))
				fix.heading = 0.0;
			double dt = 1e-3 * (double) (fix.date.getTime() - lastFix.date.getTime());
			if (dt > 0.0 && Double.isFinite(curve.getEllipsoidalDistance())) {
				double movement = curve.getEllipsoidalDistance();
				double speed = fix.movement / dt;
				if (speed < 120.0 / Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR) {
					fix.movement = movement;
					fix.speed = speed;
					Events.GpsEvent event = new Events.GpsEvent(fix);
					if (gpsOos != null) {
						try {
							gpsOos.writeObject( fix );
						} catch (IOException e) {
						}
					}
					
					Events.eventBus.post(event);
				} else {
					RobautoMain.logger.warn(String.format("EDITED: %s %10.1f %10.0f", fix.toString(), speed*Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR, movement));
					return false;
				}
			} else if (dt < 0.0) {
				RobautoMain.logger.warn(String.format("BAD TIME: %s %10.1f", fix.toString(), dt));
				return false;
			}
		}
		lastFix = fix;
		return true;
	}

	Thread readerThread = null;
	ISerialGps serialGps = null;

	public void initialize(JFrame frame, boolean isSurface) {
		String port = null;
		SerialPort[] serialPorts = SerialPort.getCommPorts();
		for (SerialPort serialPort : serialPorts) {
			if (serialPort.getPortDescription().contains("GNSS")) {
				port = serialPort.getSystemPortName();
				break;
			}
		}
		
		if (port != null ) {
		
			//serialGps = new GpsComplete(port);
			serialGps = new SerialGps( port, 9600 );
			serialGps.addStateListener(state -> {
				//RobautoMain.logger.debug(state.toString());
				if (state.hasFix && state.quality > 0) {
					Fix fix = new Fix(state);
					if (!addObservation(fix)) {
						RobautoMain.logger.debug(fix.toString() + " / " + state.toString());
					}
				}
			});
			if (serialGps.start()) {
				// started correctly, open recording file
				try {
					File file = new File("robauto-gps.obj");
					FileOutputStream fos = new FileOutputStream(file);
					gpsOos = new ObjectOutputStream( fos );
				} catch (IOException e) {
					e.printStackTrace();
					gpsOos = null;
				}
				return;
			}
	
		}
		// Create a file chooser
		final JFileChooser fileChooser = new JFileChooser( new File(".") );
		fileChooser.setDialogTitle("No GPS Detected; Select Playback File");
		fileChooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				String name = f.getName().toLowerCase();
				if (name.endsWith(".obj"))
					return true;
				return false;
			}

			@Override
			public String getDescription() {
				return "Robauto GPS capture";
			}
		});
		// In response to a button click:
		int returnVal = fileChooser.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				FileInputStream fis = new FileInputStream(file);
				gpsOis = new OpenObjectInputStream(fis);
				debugSetup(gpsOis);
			} catch (IOException e) {
				System.err.println("No GPS detected; selected playback invalid; exiting..");
				e.printStackTrace();
				System.exit(0);
			}
		} else {
			System.out.println("No GPS detected; no playback selected; exiting..");
			System.exit(0);
		}
	}

	@SuppressWarnings("deprecation")
	public void shutdown() {
		if (readerThread != null) {
			readerThread.interrupt();
			try {
				readerThread.join(1000);
			} catch (Exception x) {
				//readerThread.destroy();
			}
		}
	}

	Thread debugThread = null;

	@SuppressWarnings("deprecation")
	protected void debugClear() {
		lastFix = null;
		if (debugThread != null) {
			debugThread.interrupt();
			try {
				debugThread.join(1000);
			} catch (Exception x) {
				//debugThread.destroy();
			}
		}
	}

	public void debugSetup(final ObjectInputStream ois) {
		debugClear();
		try {
			lastFix = (GPS.Fix) ois.readObject();
		} catch (Exception x) {
			x.printStackTrace();
			return;
		}
		long SPEED_UP = 10;
		debugThread = new Thread(new Runnable() {
			@Override
			public void run() {
				RobautoMain.logger.debug("Starting: " + lastFix.date);
				double lastLat = 0;
				double lastLon = 0;
				while (!Thread.interrupted()) {
					try {
						GPS.Fix nextFix = (GPS.Fix) ois.readObject();
						if (nextFix.getLatitude() == lastLat && nextFix.getLongitude() == lastLon)
							continue;
						lastLat = nextFix.getLatitude();
						lastLon = nextFix.getLongitude();
						long msRemaining = nextFix.date.getTime() - lastFix.date.getTime();
						msRemaining = Math.min(5000L, msRemaining);
//						 RobautoMain.logger.debug("Waiting: " + nextFix +
//						 " / delay = " + msRemaining );
						if (msRemaining > 0) {
							Thread.sleep(msRemaining / SPEED_UP);
						}
						if (addObservation(nextFix)) {
							lastFix = nextFix;
						} else {
							RobautoMain.logger.debug(nextFix.toString());
						}
					} catch (Exception x) {
						break;
					}
				}
				RobautoMain.logger.debug("Exiting: " + this);
			}
		});
		debugThread.start();
	}

	// public void debugSetup(final Iterator<GeoPosition> it) {
	// debugClear();
	//
	// debugThread = new Thread( new Runnable() {
	// @Override
	// public void run() {
	// System.out.println("Starting: " +this);
	// Date date = new Date();
	// while (!Thread.interrupted() && it.hasNext()) {
	// final GeoPosition position = it.next();
	// try {
	// Thread.sleep(100);
	// date = new Date( date.getTime() + 10000L );
	// addObservation( new Fix( date, position ) );
	// } catch (Exception x) {
	// break;
	// }
	// }
	// System.out.println("Exiting: " +this);
	// }
	// } );
	// debugThread.start();
	// }

	public static void main(String[] args) {
		// GPS gps = new GPS();
		File file = new File("pwm-trace.obj");
		ObjectInputStream gpsOis = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			gpsOis = new ObjectInputStream(fis);
			// gps.debugSetup(gpsOis);
			while (true) {
				GPS.Fix nextFix = (GPS.Fix) gpsOis.readObject();
				System.out.println(nextFix);
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (gpsOis != null)
				try {
					gpsOis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

}
