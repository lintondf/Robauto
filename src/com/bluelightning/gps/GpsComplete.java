package com.bluelightning.gps;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import com.bluelightning.RobautoMain;


public class GpsComplete implements ISerialGps {
	
	private List<StateListener> stateListeners = new ArrayList<>();
	private Thread thread;
	private boolean running = false;
	private String  portName = "COM5";
	
	public GpsComplete(String portName) {
		this.portName = portName;
	}

	@Override
	public boolean start() {
		NMEA nmea = new NMEA();
		InputStream is;
		try {
			is = new FileInputStream(portName);
			LineIterator it = IOUtils.lineIterator(is, "UTF-8");
			it.nextLine(); // lines come in pairs
			it.nextLine();
			RobautoMain.logger.debug( String.format("GPS input available" ) );
	        Thread thread = new Thread(() -> {
	        	
	        	running = true;
	        	
	        	while (running) {
	        	
		        	String line = it.nextLine();
	
		        	NMEA.GpsState gpsState = nmea.getUpdatedStatus(line);
	
		        	updateState(gpsState);
	        	}
	        });

	        thread.start();
	        return true;
		} catch (IOException e) {
			System.err.println("GPS Error: " + e.getMessage());
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
        stateListeners.forEach(stateListener -> stateListener.onGpsStateUpdated(gpsState));
    }


	public static void main(String[] args) {
		GpsComplete serialGps = new GpsComplete("COM5");

      serialGps.addStateListener(state -> System.out.println( state.toString() ) );

      serialGps.start();
	}

}
