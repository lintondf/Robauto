package com.bluelightning;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.text.ParseException;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.plaf.nimbus.NimbusStyle;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;

import org.slf4j.Logger;

import com.bluelightning.Events.StopsCommitEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.PlannerMode.UiHandler;
import com.bluelightning.data.TripPlan;
import com.google.common.eventbus.Subscribe;

import javafx.scene.layout.Region;
//import sun.net.www.ParseUtil;
//import sun.rmi.registry.RegistryImpl;

public class RobautoMain {
	
	public static final int REGISTRY_PORT = 10990;

	public static TripPlan tripPlan;
	public static Logger logger;

	public static boolean isMac = (System.getProperty("os.name").startsWith("Mac"));

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
