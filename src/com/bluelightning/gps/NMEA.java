package com.bluelightning.gps;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;

import com.bluelightning.GPS.Fix;

public class NMEA {

    interface SentenceParser {
        boolean parse(String[] tokens, GpsState position);
    }

    private class GPGGA implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.time = Float.parseFloat(tokens[1]);
            position.lat = Latitude2Decimal(tokens[2], tokens[3]);
            position.lon = Longitude2Decimal(tokens[4], tokens[5]);
            position.quality = Integer.parseInt(tokens[6]);
            position.altitude = Float.parseFloat(tokens[9]);
            position.hasFix = false;
            return true;
        }
    }

    private class GPGGL implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.lat = Latitude2Decimal(tokens[1], tokens[2]);
            position.lon = Longitude2Decimal(tokens[3], tokens[4]);
            position.time = Float.parseFloat(tokens[5]);

            return true;
        }
    }

    private class GPRMC implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.time = Float.parseFloat(tokens[1]);
            position.lat = Latitude2Decimal(tokens[3], tokens[4]);
            position.lon = Longitude2Decimal(tokens[5], tokens[6]);
            position.velocity = Float.parseFloat(tokens[7]);
            position.dir = Float.parseFloat(tokens[8]);
            position.date = Float.parseFloat(tokens[9]);
            position.updatefix();
            
            return true;
        }
    }

    private class GPVTG implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.dir = Float.parseFloat(tokens[3]);

            return true;
        }
    }

    private class GPRMZ implements SentenceParser {
        public boolean parse(String[] tokens, GpsState position) {
            position.altitude = Float.parseFloat(tokens[1]);

            return true;
        }
    }

    private static float Latitude2Decimal(String lat, String NS) {
        float result = (Float.parseFloat(lat.substring(2)) / 60.0f) + Float.parseFloat(lat.substring(0, 2));

        if (NS.startsWith("S")) {
            result *= -1.0f;
        }

        return result;
    }

    private static float Longitude2Decimal(String lon, String WE) {
        float med = (Float.parseFloat(lon.substring(3)) / 60.0f) + Float.parseFloat(lon.substring(0, 3));

        if (WE.startsWith("W")) {
            med *= -1.0f;
        }

        return med;
    }

    public class GpsState {
    	public float date = 0.0f;
        public float time = 0.0f;
        public float lat = 0.0f;
        public float lon = 0.0f;
        public boolean hasFix = false;
        public int quality = 0;
        public float dir = 0.0f;
        public float altitude = 0.0f;
        public float velocity = 0.0f;

        void updatefix() {
            hasFix = quality > 0;
        }
 
       	protected final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

        public Date toDate() {
        	int d = (int) date;
        	int day = (d / 10000) % 100;
        	int month = (d / 100) % 100;
        	int year = d % 100;
        	int t = (int) time;
        	int hour = (t / 10000) % 100;
        	int minute = (t / 100) % 100;
        	int second = t % 100;
         	calendar.set( 2000+year, month-1, day, hour, minute, second);
        	return calendar.getTime();
        }

        @SuppressWarnings("deprecation")
		public String toString() {
		    return String.format("POSITION: lat: %f, lon: %f, time: %s, Q: %d, dir: %f, alt: %f, vel: %f; %b", lat, lon, format.format(toDate()), quality, dir, altitude, velocity, hasFix);
		}

    }
    
	final static SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    private GpsState position = new GpsState();

    private static final Map<String, SentenceParser> sentenceParsers = new HashMap<>();
    
    private static File nemaLog = new File("nemalog.txt");
    private static PrintStream logout = null;

    NMEA() {
    	try {
			logout = new PrintStream( nemaLog );
		} catch (IOException e) {
		}
        sentenceParsers.put("GPGGA", new GPGGA());
        //sentenceParsers.put("GPGGL", new GPGGL());
        sentenceParsers.put("GPRMC", new GPRMC());
        //sentenceParsers.put("GPRMZ", new GPRMZ());
        //sentenceParsers.put("GPVTG", new GPVTG());
    }

    private String checksum( String nmea) {
    	int sum = 0;
    	int end = nmea.indexOf('*');
    	if (end == -1) {
    		end = nmea.length();
    	}
    	for (int i = 0; i < end; i++) {
    		sum ^= nmea.charAt(i);
    	}
    	return String.format("*%02X", sum);
    }
    
    GpsState getUpdatedStatus(String line) {
        if (line.startsWith("$")) {
            String nmea = line.substring(1);
            if (! Character.isLetterOrDigit(nmea.charAt(nmea.length()-1))) {
            	nmea = nmea.substring(0, nmea.length()-1);
            }
            logout.println( line );
            String[] tokens = nmea.split(",");
            String type = tokens[0];
            boolean parsed = false;
            if (nmea.endsWith(checksum(nmea))) {
	            if (sentenceParsers.containsKey(type)) {
	                try {
	                    sentenceParsers.get(type).parse(tokens, position);
	                    parsed = true;
	                } catch (Exception e) {}
	            }
            } else {
            	logout.println("BAD CKS: " + line + " vs " + checksum(nmea));
            }
            if (!parsed)
            	position.hasFix = false;
        }

        return position;
    }
    
    public static void main( String[] args ) throws FileNotFoundException, IOException {
    	File playback = new File("nemalog.txt");
    	nemaLog = new File("/dev/null");
    	NMEA nmea = new NMEA();
    	nmea.logout = System.out;
		File file = new File("robauto-gps.obj");
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream gpsOos = new ObjectOutputStream( fos );
    	
    	List<String> lines = IOUtils.readLines(new FileReader( playback ) );
    	for (String line : lines) {
    		if (line.startsWith("$")) {
	    		GpsState state = nmea.getUpdatedStatus(line);
    			try {
    				Fix fix = new Fix(state);
    				gpsOos.writeObject(fix);
    			} catch (Exception x) {
    				x.printStackTrace();
    			}
	    		if (state.hasFix) {
	    			System.out.println( state );
	    		}
    		}
    	}
    	gpsOos.close();
    }
}