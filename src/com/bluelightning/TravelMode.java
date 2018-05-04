package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.LoggerFactory;

import com.bluelightning.Events.StopsCommitEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.PlannerMode.UiHandler;
import com.bluelightning.Report.Drive;
import com.bluelightning.data.TripPlan;
import com.google.common.eventbus.Subscribe;

import seedu.addressbook.data.place.Address;
import seedu.addressbook.data.place.VisitedPlace;

import javax.swing.JSplitPane;
import javax.swing.ListModel;

import java.awt.FlowLayout;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import java.awt.Font;
import java.awt.Image;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.bluelightning.gui.TravelActivePanel;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.poi.POIBase;

public class TravelMode extends JPanel {

	public static String hostName = "localhost";
	public static boolean isSurface = false;
	public static boolean gpsNormal = true;

	public TripPlan tripPlan = null;
	public Report report = null;
	protected Map map;
	protected JXMapViewer mapViewer;
	
	public TravelStatus travelStatus = null;
	public List<GlobalCoordinates> maneuverStarts = null;

	/**
	 * HandleTripPlanUpdate - respond to RMI update() calls when PlannerMode
	 * changes TripPlan on disk.
	 */
	public class HandleTripPlanUpdate implements TripPlanUpdate {
		@Override
		public String update(final String tripPlanPath) throws RemoteException {
			System.out.println(tripPlanPath);
			final File tripPlanFile = new File(tripPlanPath);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						initialize(tripPlanFile);
					}
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			return Boolean.toString(tripPlan != null);
		}
	}

	protected JList<String> listOfDays;
	protected TravelActivePanel activePanel;

	public class UiHandler {
		@Subscribe
		protected void handle(UiEvent event) {
			// System.out.println(event.source + " " + event.awtEvent);
			switch (event.source) {
			case "SelectDay":
				dayListScrollPane.setVisible(true);
				activePanel.setVisible(false);
				break;
			}
		}
	}

	protected int currentDay = 0;

	protected void nextDay() {
		int n = listOfDays.getModel().getSize();
		currentDay = (currentDay + 1) % n;
		setDay();
	}

	protected void priorDay() {
		int n = listOfDays.getModel().getSize();
		currentDay = (currentDay - 1) % n;
		setDay();
	}

	protected double distanceTraveled;  // [m]
	protected double currentFuel;  // [gallons]
	protected double timeStopped;    // [s]

	protected void setFuelLevel() {
		activePanel.getProgressBar().setMaximum((int) Report.FUEL_CAPACITY);
		activePanel.getProgressBar().setStringPainted(true);
		activePanel.getProgressBar().setValue((int) currentFuel);
		int eigths = (int) (8.0 * currentFuel / Report.FUEL_CAPACITY);
		eigths = Math.min(Math.max(eigths, 0), 8);
		final String[] values = { "E", "1/8", "1/4", "3/8", "1/2", "5/8", "3/4", "7/8", "F" };
		String fraction = values[eigths];
		String report = String.format("%s (%.0f gallons; %.0f MTE)", fraction, currentFuel, Report.MPG * currentFuel);
		activePanel.getProgressBar().setString(report);
	}

	protected boolean nearManeuver( GPS.Fix fix ) {
		GlobalCoordinates where = new GlobalCoordinates( fix.getLatitude(), fix.getLongitude() );
		for (GlobalCoordinates point : maneuverStarts) {
			GeodeticCurve curve = POIBase.geoCalc.calculateGeodeticCurve(POIBase.wgs84, where, point );
			if (curve.getEllipsoidalDistance() < 2.0 / Here2.METERS_TO_MILES)
				return true;
		}
		return false;
	}
	
	protected GPS gps = new GPS();

	public class GpsHandler {
		Date  startTime = null;
		
		@Subscribe
		public void handle(final Events.GpsEvent event) {
			RobautoMain.logger.info( event.fix.toString() );
			if (startTime == null)
				startTime = event.fix.date;
			if (event.fix.movement == 0.0)
				startTime = event.fix.date;
			RobautoMain.logger.debug(
					String.format("%s %5.1f %6.4f\n", event.fix.toString(), Here2.METERS_TO_MILES * distanceTraveled, 
							Here2.METERS_TO_MILES * event.fix.movement / Report.MPG) );
			distanceTraveled += event.fix.movement;
			if (event.fix.speed < 0.1) {
				timeStopped += 60;
				travelStatus.stopped(timeStopped);
			}
			currentFuel -= Here2.METERS_TO_MILES * event.fix.movement / Report.MPG;
			double timeSoFar = 0.001 * (double) (event.fix.date.getTime() - startTime.getTime());
			travelStatus.update(timeSoFar, distanceTraveled);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					setFuelLevel();
					map.moveYouAreHere(event.fix);
					activePanel.getTextPane().setText(travelStatus.toHtml());
					activePanel.getScroll().getVerticalScrollBar().setValue(0);
					if (event.fix.speed > 0.1 && nearManeuver(event.fix)) {
						RobautoMain.logger.info("toBack");
						frame.toBack();
					}
				}
			});
		}
	}

	protected void setupGps(Route route) {
		Dimension size = ButtonWaypoint.getImageSize();
		Image image = null;
		try {
			image = ImageIO.read(new File("images/youarehere.png")).getScaledInstance(2 * (int) size.getWidth(),
					2 * (int) size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {
		}

		List<GeoPosition> path = route.getShape();
		final Iterator<GeoPosition> it = path.iterator();
		final ButtonWaypoint buttonWaypoint = new ButtonWaypoint(new ImageIcon(image), it.next());
		map.setYouAreHere(buttonWaypoint);
		Events.eventBus.register(new GpsHandler());
		if (gpsNormal)
			gps.initialize();
		else
			gps.debugSetup(it);
	}

	protected void setDay() {
		final Report.Day day = report.getDays().get(currentDay);
		final ArrayList<Route> days = tripPlan.getFinalizedDays();
		final ArrayList<Integer> markers = tripPlan.getFinalizedMarkers();
		Drive drive = day.steps.get(0).drive;
		currentFuel = Double.parseDouble(drive.fuelRemaining) + Double.parseDouble(drive.fuelUsed);
		distanceTraveled = 0.0;
		setupGps(days.get(currentDay));
		travelStatus = new TravelStatus( tripPlan.getTripLeg(currentDay) );
		activePanel.getTextPane().setContentType("text/html");
		activePanel.getTextPane().setEditable(false);
		maneuverStarts = new ArrayList<GlobalCoordinates>();
		for (Leg leg : days.get(currentDay).getLeg() ) {
			for (Maneuver manuever : leg.getManeuver() ) {
				List<GeoPosition> points = manuever.getShape();
				if (! points.isEmpty()) {
					RobautoMain.logger.info(
							String.format("%10.6f %10.6f %s\n", points.get(0).getLatitude(), points.get(0).getLongitude(), manuever.getInstruction() ) );
					maneuverStarts.add( new GlobalCoordinates(points.get(0).getLatitude(), points.get(0).getLongitude()) );
				}
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (Exception x) {
				}
				map.clearRoute();
				List<ButtonWaypoint> waypoints = map.showRoute(days, markers, currentDay);
				activePanel.getSplitPane().setDividerLocation(0.4);
				activePanel.getTextPane().setText(travelStatus.toHtml());
				activePanel.getScroll().getVerticalScrollBar().setValue(0);
			}
		});
		setFuelLevel();
	}

	public class DaySelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (event.getValueIsAdjusting())
				return;
			dayListScrollPane.setVisible(false);
			activePanel.setVisible(true);
			map.clearRoute();
			currentDay = listOfDays.getSelectedIndex();
			setDay();
		}
	}

	/**
	 * Create the frame.
	 */
	public TravelMode() {
		RobautoMain.logger = LoggerFactory.getLogger("com.bluelightning.RobautoTravel");

		setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		this.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		JButton btnPriorDay = new JButton("Prior Day");
		buttonPanel.add(btnPriorDay);
		btnPriorDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				priorDay();
			}
		});

		JButton btnNextDay = new JButton("Next Day");
		buttonPanel.add(btnNextDay);
		btnNextDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextDay();
			}
		});

		JButton btnSelectDay = new JButton("Select Day");
		btnSelectDay.setActionCommand("SelectDay");
		btnSelectDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post(new Events.UiEvent("SelectDay", event));
			}
		});
		buttonPanel.add(btnSelectDay);

		activePanel = new TravelActivePanel();
		this.add(activePanel, BorderLayout.CENTER);
		activePanel.setVisible(false);

		dayListScrollPane = new JScrollPane();
		this.add(dayListScrollPane, BorderLayout.NORTH);

		String[] days = { "Loading Finalized Route..." };
		listOfDays = new JList<String>(days);
		// listOfDays.setVisibleRowCount(-1);
		listOfDays.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOfDays.addListSelectionListener(new DaySelectionListener());
		dayListScrollPane.setViewportView(listOfDays);
		dayListScrollPane.setVisible(true);

		map = new com.bluelightning.Map();
		mapViewer = map.getMapViewer();
		activePanel.getLeftPanel().add(mapViewer, BorderLayout.CENTER);

		activePanel.getComponentsPanel().setVisible(false);

		// try {
		// Image image = ImageIO.read(new File("images/compass.jpg"));
		// activePanel.getComponentsPanel().add(new ImageIcon(image));
		// } catch (Exception x) {}
		//

		SwingUtilities.updateComponentTreeUI(this);

		// Bind event handlers
		Events.eventBus.register(new UiHandler());

		try {
			HandleTripPlanUpdate obj = new HandleTripPlanUpdate();
			TripPlanUpdate stub = (TripPlanUpdate) UnicastRemoteObject.exportObject(obj, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry("192.168.0.13", RobautoMain.REGISTRY_PORT);
			registry.bind("Update", stub);

			System.out.println("Server ready");
		} catch (Exception e) {
			System.err.println("Server exception: " + e.toString());
		}
	}

	protected void initialize(File tripPlanFile) {
		activePanel.getProgressBar().setValue(-1);
		Thread t = new Thread( new Runnable() {
			@Override
			public void run() {
				tripPlan = TripPlan.load(tripPlanFile, frame);
				report = tripPlan.getTripReport();

				ArrayList<ArrayList<VisitedPlace>> placesByDay = tripPlan.getFinalizedPlaces();
				int dayNo = 1;
				DefaultListModel<String> model = new DefaultListModel<>();
				for (ArrayList<VisitedPlace> stops : placesByDay) {
					int iLast = stops.size() - 1;
					Address from = stops.get(0).getAddress();
					System.out.println(from.toString());
					Address to = stops.get(iLast).getAddress();
					System.out.println(to.toString());
					String entry = String.format("Day %02d: %s, %s to %s, %s", dayNo++, from.getCity(), from.getState(),
							to.getCity(), to.getState());
					System.out.println(entry);
					model.addElement(entry);
				}
				if (model.isEmpty()) {
					model.addElement("No Finalized Route Stops Found");
				}
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						if ( model.size() > 0 )
							listOfDays.setModel(model);
						map.clearRoute();
					}
				});
			}
		} );
		t.start();
	}

	/**
	 * Launch the application.
	 */

	public static JFrame frame;
	protected JScrollPane dayListScrollPane;

	private static void initLookAndFeel(double textSizeInPixels, double fontSize) {
		try {
			System.out.println(Toolkit.getDefaultToolkit().getScreenSize());
			// double fontSize = 0.8 * textSizeInPixels *
			// Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
			System.out.printf("%f %d %f\n", textSizeInPixels, Toolkit.getDefaultToolkit().getScreenResolution(),
					fontSize);
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					NimbusLookAndFeel laf = (NimbusLookAndFeel) UIManager.getLookAndFeel();
					// laf.getDefaults().entrySet().forEach(System.out::println);
					Font font = new Font("Tahoma", Font.BOLD, (int) fontSize);
					System.out.println(font);
					laf.getDefaults().put("defaultFont", font);
					laf.getDefaults().put("ScrollBar.thumbHeight", (int) textSizeInPixels);
					laf.getDefaults().put("Table.rowHeight", (int) textSizeInPixels);
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look
			// and feel.
		}
	}

	public static void main(String[] args) {
		java.net.InetAddress localMachine = null;
		try {
			localMachine = java.net.InetAddress.getLocalHost();
			hostName = (localMachine != null) ? localMachine.getHostName() : "localhost";
			isSurface = hostName.toUpperCase().startsWith("SURFACE");
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		System.out.println("Hostname of local machine: " + localMachine.getHostName() + " " + localMachine);

		final File tripPlanFile = new File("RobautoTripPlan.obj");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (isSurface)
						initLookAndFeel(60, 48);
					frame = new JFrame();
					frame.setTitle("Robauto - Travel Mode");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					Image icon = ImageIO.read(new File("images/icon-travel.jpg")).getScaledInstance((int) 64, (int) 64,
							Image.SCALE_SMOOTH);
					frame.setIconImage(icon);

					TravelMode travelMode = new TravelMode();

					frame.setContentPane(travelMode);
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					SwingUtilities.updateComponentTreeUI(frame);
					frame.pack();
					frame.setVisible(true);
					frame.addWindowListener(new WindowAdapter() {
//						@Override
//						public void windowActivated(WindowEvent e) {
//							try {
//								Thread.sleep(10*1000);
//							} catch (Exception x) {}
//							System.out.println("toBack");
//							frame.toBack();
//						}
						@Override
						public void windowClosing(WindowEvent e) {
							if (travelMode.gps != null) {
								travelMode.gps.shutdown();
							}
						}
					});
					travelMode.initialize(tripPlanFile);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
