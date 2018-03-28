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

	static PlannerMode plannerMode;
	static TravelMode travelMode;
	public static TripPlan tripPlan;
	public static Logger logger;

	public static class ModeHandler {
		@Subscribe
		protected void handle(UiEvent event) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					switch (event.source) {
					case "PlannerMode":
						frame.setContentPane(plannerMode);
						break;
					case "TravelMode":
						frame.setContentPane(travelMode);
						break;
					default:
						break;
					}
				}
			});
		} // handle(UiEvent)
	} // class ModeHandler

	private static void initLookAndFeel() {
		try {
			double pixelSize = 30;
			double fontSize = 0.8 * pixelSize * Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            NimbusLookAndFeel laf = (NimbusLookAndFeel) UIManager.getLookAndFeel();
		            //laf.getDefaults().entrySet().forEach(System.out::println);
		            Font font = new Font("Tahoma", Font.BOLD, (int) fontSize );
//		            FontMetrics metrics = graphics.getFontMetrics(font);
//		         // get the height of a line of text in this
//		         // font and render context
//		         int hgt = metrics.getHeight();
//		            laf.getDefaults().put("Button.font", font );
//		            laf.getDefaults().put("CheckBox.font", font );
//		            laf.getDefaults().put("ComboBox.font", font );
//		            laf.getDefaults().put("Label.font", font );
//		            laf.getDefaults().put("List.font", font );
		            laf.getDefaults().put("defaultFont", font );
		            laf.getDefaults().put("ScrollBar.thumbHeight", pixelSize);
		            //laf.getDefaults().put("ScrollBar:\"ScrollBar.button\".size", pixelSize);
		            laf.getDefaults().put("Table.rowHeight", pixelSize); 
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
//		SynthLookAndFeel lookAndFeel = new SynthLookAndFeel();
//
//		// SynthLookAndFeel load() method throws a checked exception
//		// (java.text.ParseException) so it must be handled
//		try {
//			lookAndFeel.load(new FileInputStream("resources/synth.xml"), RobautoMain.class);
//			UIManager.setLookAndFeel(lookAndFeel);
//		}
//
//		catch (ParseException e) {
//			System.err.println("Couldn't get specified look and feel (" + lookAndFeel + "), for some reason.");
//			System.err.println("Using the default look and feel.");
//			e.printStackTrace();
//		} catch (UnsupportedLookAndFeelException e) {
//			e.printStackTrace();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public static JFrame frame;

	public static void main(String[] args) {
		initLookAndFeel();
		Events.eventBus.register(new ModeHandler());
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new JFrame();
					frame.setTitle("Robauto - Travel Mode");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					plannerMode = new PlannerMode();
					travelMode = new TravelMode();
					
					frame.setContentPane(plannerMode);
					// frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.setSize(800, 600);
					SwingUtilities.updateComponentTreeUI(frame);
					frame.pack();
					frame.setVisible(true);
					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							RobautoMain.tripPlan.setPlaces(plannerMode.routePanel.getWaypointsModel().getData());
							RobautoMain.tripPlan.save(plannerMode.tripPlanFile);
						}
					});
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					plannerMode.initialize();
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
