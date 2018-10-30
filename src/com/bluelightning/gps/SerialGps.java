package com.bluelightning.gps;

import com.bluelightning.RobautoMain;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused"})
public class SerialGps {

    public interface StateListener {
        void onGpsStateUpdated(NMEA.GpsState state);
    }

    private String portName;
    private int baudRate;
    private boolean isRunning = false;
    private List<StateListener> stateListeners = new ArrayList<>();

    public SerialGps(String portName, int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
    }

    public SerialGps(String portName) {
        this(portName, 4800);
    }

    public void addStateListener(StateListener stateListener) {
        stateListeners.add(stateListener);
    }

    public boolean start() {
        NMEA nmea = new NMEA();

        SerialPort[] serialPorts = SerialPort.getCommPorts();
        SerialPort gpsPort = null;

        RobautoMain.logger.debug(portName);
        for (SerialPort serialPort : serialPorts) {
        	RobautoMain.logger.debug("Serial Port: " +  serialPort.getDescriptivePortName() + " " + serialPort.toString() );
            if (serialPort.getDescriptivePortName().equalsIgnoreCase(portName) ||
            	serialPort.getDescriptivePortName().contains(portName)) {
                gpsPort = serialPort;
            }
        }

        if (gpsPort == null) {
            RobautoMain.logger.warn("failed to find gps serial port");

            return false;
        }

        RobautoMain.logger.info("using serial port: " + gpsPort.getDescriptivePortName());

        gpsPort.setBaudRate(4800);
        gpsPort.openPort();
        InputStream inStream = gpsPort.getInputStream();

        if (inStream == null) {
            RobautoMain.logger.error("opening port " + gpsPort.getDescriptivePortName() + " failed");

            return false;
        }
        
        while (true) {
        	RobautoMain.logger.info( "Waiting for GPS input...");
        	try {
				if (inStream.available() > 0)
					break;
			} catch (IOException e) {
				RobautoMain.logger.warn("  " + e.getMessage() );
			}
        	try { Thread.sleep(1000); } catch (Exception x) {};
        }

        Thread thread = new Thread(() -> {
            String line = "";

            isRunning = true;

            while (isRunning) {
                try {
                    if (inStream.available() > 0) {
                        char b = (char) inStream.read();

                        if (b == '\n') {
                            NMEA.GpsState gpsState = nmea.getUpdatedStatus(line);

                            updateState(gpsState);

                            line = "";
                        } else {
                            line += b;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
        return true;
    }

    public void stop() throws InterruptedException {
        isRunning = false;
    }

    private void updateState(NMEA.GpsState gpsState) {
        stateListeners.forEach(stateListener -> stateListener.onGpsStateUpdated(gpsState));
    }

    public static void main(String[] args) throws Exception {
        SerialGps serialGps = new SerialGps("XGPS10M-4269F2-SPPDev");

        serialGps.addStateListener(state -> System.out.println( state.toString() ) );
//                state.lat + ", " + state.lon + " (" + (state.hasFix ? "got fix" : "no fix") + ")"
//        ));

        serialGps.start();
    }
}
