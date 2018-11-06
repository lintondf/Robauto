package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineSegment;
import org.slf4j.LoggerFactory;

import com.bluelightning.Events.UiEvent;
import com.bluelightning.Report.Drive;
import com.bluelightning.data.TripPlan;
import com.google.common.eventbus.Subscribe;

import seedu.addressbook.data.place.Address;
import seedu.addressbook.data.place.VisitedPlace;

import java.awt.FlowLayout;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import java.awt.Font;
import java.awt.Image;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.bluelightning.gui.TravelActivePanel;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;

public class TravelMode extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int ICON_SCALE = 1;

	public static String hostName = "localhost";
	public static boolean isSurface = true;

	public TripPlan tripPlan = null;
	public Report report = null;
	protected Map map;
	protected JXMapViewer mapViewer;
	private boolean displayRunning = true;

	
	public TravelStatus travelStatus = null;
	
	public static class ManeuverMetrics {
		
		Maneuver maneuver;
		ArrayList<LineSegment> segments = new ArrayList<>();
		
		public ManeuverMetrics( Maneuver maneuver) {
			this.maneuver = maneuver;
			for (int i = 1; i < maneuver.getShape().size(); i++) {
				Coordinate p1 = new Coordinate( maneuver.getShape().get(i-1).getLongitude(), maneuver.getShape().get(i-1).getLatitude() );
				Coordinate p2 = new Coordinate( maneuver.getShape().get(i-1).getLongitude(), maneuver.getShape().get(i-1).getLatitude() );
				segments.add( new LineSegment(p1, p2 ) );
			}
		}
	}
	
	public List<ManeuverMetrics> maneuverMetrics = null;
	public HashMap<Maneuver, Maneuver> nextManeuverMap = null;

	/**
	 * HandleTripPlanUpdate - respond to RMI update() calls when PlannerMode
	 * changes TripPlan on disk.
	 */
	public class HandleTripPlanUpdate implements TripPlanUpdate {
		@Override
		public String update(final String tripPlanPath) throws RemoteException {
			RobautoMain.logger.debug(tripPlanPath);
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
			RobautoMain.logger.debug("UIEvent: " + event.source + " " + event.awtEvent);
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
	
	//protected double

	protected Maneuver findCurrentManeuver( GPS.Fix fix ) {
		Coordinate where = new Coordinate( fix.getLongitude(), fix.getLatitude() );
		double closestDistance = 7e6;
		Maneuver closest = null;
		if (maneuverMetrics == null)
			return null;
		for (ManeuverMetrics metrics : maneuverMetrics) {
			for (LineSegment segment : metrics.segments) {
				double d = segment.distance(where);
				if (d < closestDistance) {
					closestDistance = d;
					closest = metrics.maneuver;
				}
			}
		}
		//RobautoMain.logger.debug( closest );
		return closest;
	}
	
	protected GPS gps = new GPS();

	public class GpsHandler {
		Date  startTime = null;
		
		@Subscribe
		public void handle(final Events.GpsEvent event) {
			if (startTime == null && event.fix.movement > 0.1)
				startTime = event.fix.date;
			RobautoMain.logger.debug(
					String.format("%s %5.1f %6.4f", event.fix.toString(), Here2.METERS_TO_MILES * distanceTraveled, 
							Here2.METERS_TO_MILES * event.fix.movement / Report.MPG) );
			distanceTraveled += event.fix.movement;
			Maneuver currentManeuver = findCurrentManeuver( event.fix );
			if (currentManeuver != null) {
				travelStatus.update(event.fix.speed, currentManeuver, nextManeuverMap.get(currentManeuver));
			}
			if (travelStatus != null && event.fix.speed < 0.1) {
				timeStopped += 60;
				travelStatus.stopped(timeStopped);
			}
			currentFuel -= Here2.METERS_TO_MILES * event.fix.movement / Report.MPG;
			double timeSoFar = 0.001 * (double) (event.fix.date.getTime() - startTime.getTime());
			travelStatus.update(timeSoFar, distanceTraveled);
			travelStatus.update( event.fix );
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					if (displayRunning) {
						setFuelLevel();
						map.moveYouAreHere(event.fix);
						activePanel.getTextPane().setText(travelStatus.toHtml());
						activePanel.getTextPane().setCaretPosition( 0 );
					}
				}
			});
		}
	}

	protected void setupGps(Route route) {
		Dimension size = ButtonWaypoint.getImageSize();
		Image image = null;
		try {
			image = ImageIO.read(new File("images/youarehere.png")).getScaledInstance(
					ICON_SCALE * (int) size.getWidth(),
					ICON_SCALE * (int) size.getHeight(), 
					Image.SCALE_SMOOTH);
		} catch (Exception x) {
		}

		List<GeoPosition> path = route.getShape();
		final Iterator<GeoPosition> it = path.iterator();
		final ButtonWaypoint buttonWaypoint = new ButtonWaypoint(new ImageIcon(image), it.next());
		map.setYouAreHere(buttonWaypoint);
		Events.eventBus.register(new GpsHandler());
		gps.initialize(frame, isSurface);
	}

	protected void setDay() {
		final Report.Day day = report.getDays().get(currentDay);
		final ArrayList<Route> days = tripPlan.getFinalizedDays();
		final ArrayList<Integer> markers = tripPlan.getFinalizedMarkers();
		final ArrayList<ArrayList<VisitedPlace>> allPlaces = tripPlan.getFinalizedPlaces();
		Drive drive = day.steps.get(0).drive;
		currentFuel = Double.parseDouble(drive.fuelRemaining) + Double.parseDouble(drive.fuelUsed);
		distanceTraveled = 0.0;
		setupGps(days.get(currentDay));
		travelStatus = new TravelStatus( tripPlan.getTripLeg(currentDay) );
		activePanel.getTextPane().setContentType("text/html");
		activePanel.getTextPane().setEditable(false);
		maneuverMetrics = new ArrayList<>();
		nextManeuverMap = new HashMap<>();
		Maneuver lastManeuver = null;
		for (Leg leg : days.get(currentDay).getLeg() ) {
			for (Maneuver maneuver : leg.getManeuver() ) {
				List<GeoPosition> points = maneuver.getShape();
				if (! points.isEmpty()) {
					maneuverMetrics.add( new ManeuverMetrics(maneuver) );
				}
				if (lastManeuver != null) {
					nextManeuverMap.put( lastManeuver, maneuver);
				}
				lastManeuver = maneuver;
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
				/*List<ButtonWaypoint> waypoints = */ map.showRoute(days, markers, allPlaces, currentDay);
				activePanel.getSplitPane().setDividerLocation(0.4);
				activePanel.getTextPane().setText(travelStatus.toHtml());
				activePanel.getTextPane().setCaretPosition( 0 );
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
		setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel();
		this.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		JButton btnPauseResume = new JButton("Pause");
		buttonPanel.add(btnPauseResume);
		btnPauseResume.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (btnPauseResume.getText().equals("Pause")) {
					displayRunning = false;
					btnPauseResume.setText("Resume");
				} else {
					displayRunning = true;
					btnPauseResume.setText("Pause");					
				}
			}
		});

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

		// try {
		// Image image = ImageIO.read(new File("images/compass.jpg"));
		// activePanel.getComponentsPanel().add(new ImageIcon(image));
		// } catch (Exception x) {}
		//

		SwingUtilities.updateComponentTreeUI(this);

		// Bind event handlers
		Events.eventBus.register(new UiHandler());

//		try {
//			HandleTripPlanUpdate obj = new HandleTripPlanUpdate();
//			TripPlanUpdate stub = (TripPlanUpdate) UnicastRemoteObject.exportObject(obj, 0);
//			if (isSurface) {
//				// Bind the remote object's stub in the registry
//				Registry registry = LocateRegistry.getRegistry(localHostAddress.toString(), RobautoMain.REGISTRY_PORT);
//				registry.bind("Update", stub);
//			}
//			RobautoMain.logger.debug("Server ready");
//		} catch (Exception e) {
//			System.err.println("Server exception: " + e.toString());
//		}
	}

	protected void initialize(File tripPlanFile) {
		activePanel.getProgressBar().setValue(-1);
		Thread t = new Thread( new Runnable() {
			@Override
			public void run() {
				tripPlan = TripPlan.load(tripPlanFile, frame);
				//Here2.reportRoute(tripPlan.getRoute());
				report = tripPlan.getTripReport();

				ArrayList<ArrayList<VisitedPlace>> placesByDay = tripPlan.getFinalizedPlaces();
				int dayNo = 1;
				DefaultListModel<String> model = new DefaultListModel<>();
				for (ArrayList<VisitedPlace> stops : placesByDay) {
					int iLast = stops.size() - 1;
					Address from = stops.get(0).getAddress();
					RobautoMain.logger.debug(from.toString());
					Address to = stops.get(iLast).getAddress();
					RobautoMain.logger.debug(to.toString());
					String entry = String.format("Day %02d: %s, %s to %s, %s", dayNo++, from.getCity(), from.getState(),
							to.getCity(), to.getState());
					RobautoMain.logger.debug(entry);
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

	@SuppressWarnings("unused")
	private static String localHostAddress;
	protected JScrollPane dayListScrollPane;

	private static void initLookAndFeel(double textSizeInPixels, double fontSize) {
		try {
			RobautoMain.logger.debug(Toolkit.getDefaultToolkit().getScreenSize().toString());
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
					RobautoMain.logger.debug(font.toString());
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
		// configured via resources/logback.xml
		RobautoMain.logger = LoggerFactory.getLogger("com.bluelightning.RobautoTravel");

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
		RobautoMain.logger.info("Hostname of local machine: " + isSurface + " " + localMachine);
		localHostAddress = localMachine.getHostAddress();
		File file = new File("pwm-trace.obj");

		String path = "RobautoTripPlan.obj";
		if (args.length > 0)
			path = args[0];
		RobautoMain.logger.debug(path);
		final File tripPlanFile = new File(path);
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
//							RobautoMain.logger.debug("toBack");
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
