package com.bluelightning.gps;

import com.bluelightning.RobautoMain;
import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({ "unused" })
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

	private Thread thread;
	private long linesRead;
	private SerialPort gpsPort;
	private NMEA nmea;

	private boolean startGpsRead() {
		gpsPort.setBaudRate(2*4800);
		gpsPort.openPort();
		InputStream inStream = gpsPort.getInputStream();

		if (inStream == null) {
			RobautoMain.logger.error("opening port " + gpsPort.getDescriptivePortName() + " failed");
			return false;
		}

		while (true) {
			RobautoMain.logger.info("Waiting for GPS input...");
			try {
				if (inStream.available() > 0)
					break;
			} catch (IOException e) {
				RobautoMain.logger.warn("  " + e.getMessage());
			}
			try {
				Thread.sleep(1000);
			} catch (Exception x) {
			}
		}

		thread = new Thread(() -> {
			String line = "";

			isRunning = true;
			RobautoMain.logger.debug("Serial GPS running");

			while (isRunning) {
				try {
					if (inStream.available() > 0) {
						int ich = inStream.read();
						if (ich == -1) {
							RobautoMain.logger.error("Serial IO error");
							return;
						}
						char b = (char) ich;

						if (b == '\n') {
							linesRead++;
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
			try {
				inStream.close();
			} catch (IOException e) {
			}
			gpsPort.closePort();
			isRunning = false;
			RobautoMain.logger.debug("Serial GPS exiting");
		});

		thread.start();
		return true;
	}

	private void waitStarted() {
		while (true) {
			RobautoMain.logger.info("Watchdog waiting started");
			if (isRunning)
				break;
			try {
				Thread.sleep(1000);
			} catch (Exception x) {
			}
		}
	}

	public boolean start() {
		nmea = new NMEA();

		SerialPort[] serialPorts = SerialPort.getCommPorts();
		gpsPort = null;

		RobautoMain.logger.debug(portName);
		for (SerialPort serialPort : serialPorts) {
			RobautoMain.logger
					.debug("Serial Port: " + serialPort.getDescriptivePortName() + " " + serialPort.toString());
			if (serialPort.getDescriptivePortName().equalsIgnoreCase(portName)
					|| serialPort.getDescriptivePortName().contains(portName)) {
				gpsPort = serialPort;
			}
		}

		if (gpsPort == null) {
			RobautoMain.logger.warn("failed to find gps serial port");
			return false;
		}

		RobautoMain.logger.info("using serial port: " + gpsPort.getDescriptivePortName());

		if (!startGpsRead()) {
			RobautoMain.logger.warn("failed to start gps serial port");
			return false;
		}

		Thread watchdog = new Thread(() -> {
			long lastRead = 0;
			RobautoMain.logger.debug("Watchdog started");
			waitStarted();
			while (isRunning) {
				try {
					Thread.sleep(10000);
				} catch (Exception x) {
				}
				RobautoMain.logger.debug(String.format("Watchdog %d %d", linesRead, lastRead));
				if (linesRead > lastRead) {
					lastRead = linesRead;
				} else {
					RobautoMain.logger.debug("Watchdog interrupting read thread");
					isRunning = false;
					thread.interrupt();
					try {
						RobautoMain.logger.debug("Watchdog joining read thread");
						thread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					RobautoMain.logger.debug("Watchdog restarting read thread");
					while (!startGpsRead()) {
						RobautoMain.logger.debug("Watchdog start failed...retrying");
						try {
							Thread.sleep(1000);
						} catch (Exception x) {
						}
					}
					waitStarted();
				}
			}
			RobautoMain.logger.debug("Watchdog exiting");
		});
		watchdog.start();
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

		serialGps.addStateListener(state -> System.out.println(state.toString()));
		// state.lat + ", " + state.lon + " (" + (state.hasFix ? "got fix" : "no
		// fix") + ")"
		// ));

		serialGps.start();
	}
}
