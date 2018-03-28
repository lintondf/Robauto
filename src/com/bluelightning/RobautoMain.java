package com.bluelightning;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class RobautoMain {

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

	public static void main(String[] args) {
		try {
			ProcessBuilder pb = new ProcessBuilder();
			pb.command( Arrays.asList( getJavaPath(), "-cp", "Robauto.jar", "com.bluelightning.TravelMode" ) );
			Process p = pb.start();
		} catch (Exception x) {
			x.printStackTrace();
		}
		PlannerMode.main(args);
	}

}
