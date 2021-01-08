package com.bluelightning;

import java.io.File;
import java.net.UnknownHostException;

import org.slf4j.Logger;

import com.bluelightning.data.TripPlan;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.TriggeringPolicyBase;

public class RobautoMain {
	
	public static final int REGISTRY_PORT = 10990;

	public static TripPlan tripPlan;
	public static Logger logger;
	
	public static boolean isMac = (System.getProperty("os.name").startsWith("Mac"));
	
	public static double startingFuel = Report.FUEL_CAPACITY;

//	@NoAutoStart
//	public static class StartupTriggeringPolicy<E> extends TriggeringPolicyBase<E> {
//	    private boolean triggerRollover = true;
//
//	    @Override
//	    public boolean isTriggeringEvent(final File activeFile, final E event) {
//	        if (!triggerRollover) { return false; }
//	        triggerRollover = false;
//	        return true;
//	    }
//	}
//

	public static String getJavaPath() {
		if (isMac) {
			return "/usr/bin/java";
		} else {
			return "C:\\ProgramData\\Oracle\\Java\\javapath\\java.exe";
		}
	}
	
	public static String getRmiPath() {
		return "C:\\Program Files\\Java\\jre1.8.0_91\\bin\\rmiregistry.exe";
	}

	public static String getDataPath() {
		String where = System.getProperty("user.home") + "/Google Drive/0Robauto";
		java.net.InetAddress localMachine = null;
		try {
			localMachine = java.net.InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {
		}
		final String hostName = (localMachine != null) ? localMachine.getHostName() : "localhost";
		if (hostName.equals("raspberrypi")) {
			where = System.getProperty("user.home") + "/0Robauto";
		}
		return where;
	}

	public static void main(String[] args) {
		String classPath = "Robauto.jar";
		Execute registryExecutor = new Execute();
		Process registryProcess = registryExecutor
				.start(new String[] { getRmiPath(), Integer.toString(REGISTRY_PORT) }, classPath);

		Execute travelExecutor = new Execute();
		Process travelProcess = travelExecutor.start(new String[] { getJavaPath(), "com.bluelightning.TravelMode" }, classPath);
		Execute plannerExecutor = new Execute();
		Process plannerProcess = plannerExecutor.start(new String[] { getJavaPath(), "com.bluelightning.PlannerMode" }, classPath);

		while (travelProcess.isAlive() || plannerProcess.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (Exception x) {}
		}
		String out = travelExecutor.join();
		System.out.println("T: " + out);
		out = plannerExecutor.join();
		System.out.println("P: " + out);

    	registryProcess.destroy();
		out = registryExecutor.join();
		System.out.println("Registry: " + out);
	}

}
