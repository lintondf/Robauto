package com.bluelightning.gps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.LoggerFactory;

import com.bluelightning.RobautoMain;


public class GpsComplete implements ISerialGps {
	
	private List<StateListener> stateListeners = new ArrayList<>();
	private Thread thread;
	private String  portName = "COM5";
	
	public GpsComplete(String portName) {
		this.portName = portName;
	}
	
	private volatile boolean running = false;
	private volatile double deltaT = 0; 

	@Override
	public boolean start() {
		NMEA nmea = new NMEA();
		InputStream is;
		try {
			is = new FileInputStream(portName);
			LineIterator it = IOUtils.lineIterator(is, "UTF-8");
			RobautoMain.logger.debug( String.format("GPS input starting" ) );
			
	        Thread thread = new Thread(() -> {
	        	
	        	running = true;
	        	
	        	Double firstT = null;  // pseudo yymmdd * 1e6 + hhmmss;
	        	
	        	while (running) {
	        		try {
		        	String line = it.nextLine();
	
		        	NMEA.GpsState gpsState = nmea.getUpdatedStatus(line);
	
		        	if (gpsState.hasFix) {
		        		if (firstT == null) {
		        			firstT = (double) gpsState.date * 1e6 + (double) gpsState.time;
		        		} else {
		        			double t = (double) gpsState.date * 1e6 + (double) gpsState.time;
		        			deltaT = t - firstT;
		        		}
		        	}
		        	updateState(gpsState);
		        	
	        		} catch (Exception x) {
	        			RobautoMain.logger.error("GPS input error", x);
	        			running = false;
	        			break;
	        		 }
	        	} 
	        	RobautoMain.logger.debug( String.format("GPS input exited" ) );
	        });

	        thread.start();
	        while (!thread.isAlive()) {
	        	try {
					thread.wait(100);
				} catch (InterruptedException e) {
					break;
				}
	        }
	        RobautoMain.logger.debug( String.format("GPS input running " + deltaT ) );
	        try {
	        	thread.join(5000);
		        return deltaT > 0;
	        } catch (Exception x) {
	        	if (deltaT == 0) {
	        		running = false;
	        		thread.interrupt();
	        		return false;
	        	}
	        }
	        return deltaT > 0;
		} catch (IOException e) {
			RobautoMain.logger.error("GPS input error", e);
		}
		return false;
	}

	@Override
	public void stop() throws InterruptedException {
		if (thread != null && running) {
			running = false;
			thread.interrupt();
			thread.join();
		}
	}

	@Override
	public void addStateListener(StateListener stateListener) {
		stateListeners.add(stateListener);
	}
	
    private void updateState(NMEA.GpsState gpsState) {
    	if (gpsState.hasFix) {
    		RobautoMain.logger.debug( stateListeners.size() + " " + gpsState.toString() );
        	stateListeners.forEach(stateListener -> stateListener.onGpsStateUpdated(gpsState));
    	}
    }


	public static void main(String[] args) {
		RobautoMain.logger = LoggerFactory.getLogger("com.bluelightning.RobautoTravel");
		GpsComplete serialGps = new GpsComplete("COM5");

      serialGps.addStateListener(state -> System.out.println( state.toString() ) );

      serialGps.start();
	}

}
