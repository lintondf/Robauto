package com.bluelightning;
/**TODO
SEVERE: Exception thrown by subscriber method handle(com.bluelightning.Events$GpsEvent) on subscriber com.bluelightning.TravelMode$GpsHandler@7526fda5 when dispatching event: com.bluelightning.Events$GpsEvent@7389587b
java.util.ConcurrentModificationException
	at java.util.ArrayList$Itr.checkForComodification(Unknown Source)
	at java.util.ArrayList$Itr.next(Unknown Source)
	at com.bluelightning.TravelMode.findCurrentManeuver(TravelMode.java:188)
	at com.bluelightning.TravelMode$GpsHandler.handle(TravelMode.java:216)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 */

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.slf4j.LoggerFactory;

import com.bluelightning.Events.UiEvent;
import com.bluelightning.GPS.Fix;
import com.bluelightning.Report.Drive;
import com.bluelightning.TravelStatus.UpcomingStop;
import com.bluelightning.data.TripPlan;
import com.google.common.eventbus.Subscribe;

import ch.qos.logback.core.joran.spi.NoAutoStart;
import ch.qos.logback.core.rolling.TriggeringPolicyBase;
import seedu.addressbook.data.place.Address;
import seedu.addressbook.data.place.VisitedPlace;

import java.awt.FlowLayout;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.bluelightning.gui.FuelPanel;
import com.bluelightning.gui.TravelActivePanel;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;
import com.fazecast.jSerialComm.SerialPort;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class TravelMode extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int ICON_SCALE = 1;

	public static String hostName = "localhost";
	public static boolean isSurface = true;

	// current active
	public TripPlan tripPlan = null;
	public Report report = null;
	protected Map map;
	// plans and reports for all days
	public ArrayList<TripPlan> tripPlans = new ArrayList<>();
	public ArrayList<Report> reports = new ArrayList<>();
	protected JXMapViewer mapViewer;
	private boolean displayRunning = true;

	public TravelStatus travelStatus = null;
	

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
	protected FuelPanel fuelPanel;

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

	protected final int dayInTrip = 0;
	protected int currentDay = 0;

	protected void nextDay() {
		int n = listOfDays.getModel().getSize();
		currentDay = (currentDay + 1) % n;
		tripPlan = tripPlans.get(currentDay);
		report = reports.get(currentDay);
		setDay();
	}

	protected void priorDay() {
		int n = listOfDays.getModel().getSize();
		currentDay = (currentDay - 1) % n;
		tripPlan = tripPlans.get(currentDay);
		report = reports.get(currentDay);
		setDay();
	}

	protected double distanceTraveled; // [m]
	protected double currentFuel; // [gallons]
	protected double timeStopped; // [s]
	protected double timeSoFar;   // [s]

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

	protected GPS gps = new GPS();

	public class GpsHandler {
		Date startTime = null;
		Date lastTime = null;
		double lastLat = 0;
		double lastLon = 0;
		boolean firstMotion = false;

		@Subscribe
		public void handle(final Events.GpsEvent event) {
			if (startTime == null) {
				startTime = event.fix.date;
				lastTime = startTime;
			}
			//if (event.fix.getLatitude() != lastLat || event.fix.getLongitude() != lastLon) 
			RobautoMain.logger.debug(String.format("%s %5.1f %6.4f", event.fix.toString(),
					Here2.METERS_TO_MILES * distanceTraveled, Here2.METERS_TO_MILES * event.fix.movement / Report.MPG));
			lastLat = event.fix.getLatitude();
			lastLon = event.fix.getLongitude();
			if (travelStatus == null)
				return;
			travelStatus.update(event.fix);
			distanceTraveled += event.fix.movement;
			ManeuverMetrics.ClosestManeuver currentManeuver = ManeuverMetrics.findCurrentManeuver(event.fix);
			if (currentManeuver != null) {
				travelStatus.update(event.fix.speed, currentManeuver.metrics.maneuver, ManeuverMetrics.nextManeuverMap.get(currentManeuver.metrics.maneuver));
			}
			double dt = 0.001 * (event.fix.date.getTime() - lastTime.getTime());
			if (event.fix.speed < 0.1) {
				if (firstMotion) {
					timeStopped += dt;
					travelStatus.stopped(timeStopped);
				}
			} else {
				firstMotion = true;
				timeSoFar += dt;
			}
			lastTime = event.fix.date;
			currentFuel -= Here2.METERS_TO_MILES * event.fix.movement / Report.MPG;
			UpcomingStop nextStop = travelStatus.update(timeSoFar, distanceTraveled, currentManeuver, ManeuverMetrics.maneuverMetrics);
			fuelPanel.update(currentManeuver, ManeuverMetrics.maneuverMetrics);
			
			rotateMapView( event.fix, nextStop );

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					if (displayRunning) {
						setFuelLevel();
						map.moveYouAreHere(event.fix);
						activePanel.getTextPane().setContentType("text/html");
						activePanel.getTextPane().setText(travelStatus.toDrivingHtml());
						activePanel.getTextPane().setCaretPosition(0);
						activePanel.getNextTextPane().setContentType("text/html");
						activePanel.getNextTextPane().setText(travelStatus.toNextUpHtml());
						//RobautoMain.logger.debug("GUI updated");
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
					ICON_SCALE * (int) size.getWidth(), ICON_SCALE * (int) size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {
		}

		List<GeoPosition> path = route.getShape();
		final Iterator<GeoPosition> it = path.iterator();
		final ButtonWaypoint buttonWaypoint = new ButtonWaypoint("You Are Here", new ImageIcon(image), it.next());
		map.setYouAreHere(buttonWaypoint);
//		Events.eventBus.register(new GpsHandler());
//		gps.initialize(frame, isSurface);
	}
	
	@SuppressWarnings("deprecation")
	public void rotateMapView(Fix fix, UpcomingStop nextStop) {
		if (nextStop !=null && waypoints != null && !waypoints.isEmpty() &&
				(fix.date.getSeconds() % 10) == 0) {
			Set<GeoPosition> positions = new HashSet<GeoPosition>();
			positions.add(new GeoPosition(fix.getLatitude(), fix.getLongitude()));
			int n = waypoints.size()-1;
			if ((fix.date.getSeconds() % 20) == 0) {
				String match = nextStop.name.toLowerCase();
				for (n = 0; n < waypoints.size()-1; n++) {
					//System.out.println(n + ": " + match + " / " + waypoints.get(n).getName().toLowerCase());
					if (waypoints.get(n).getName().toLowerCase().startsWith(match)) {
						break;
					}
				}
			}
			positions.add(waypoints.get(n).getPosition());
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					map.mapViewer.zoomToBestFit(positions, 0.95);
				}
			});
		}
	}

	protected void setDay() {
		final Report.Day day = report.getDays().get(dayInTrip);
		final ArrayList<Route> days = tripPlan.getFinalizedDays();
		final ArrayList<Integer> markers = tripPlan.getFinalizedMarkers();
		final ArrayList<ArrayList<VisitedPlace>> allPlaces = tripPlan.getFinalizedPlaces();
		Drive drive = day.steps.get(0).drive;
		currentFuel = Double.parseDouble(drive.fuelRemaining) + Double.parseDouble(drive.fuelUsed);
		distanceTraveled = 0.0;
		ManeuverMetrics.initializeMetrics( dayInTrip, days );
		setupGps(days.get(dayInTrip));
		travelStatus = new TravelStatus(tripPlan.getTripLeg(dayInTrip));
		activePanel.getTextPane().setContentType("text/html");
		activePanel.getTextPane().setEditable(false);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (Exception x) {
				}
				map.clearRoute();
				waypoints = map.showRoute(days, markers, allPlaces, dayInTrip);
				activePanel.getSplitPane().setDividerLocation(0.4);
				activePanel.getTextPane().setContentType("text/html");
				activePanel.getTextPane().setText(travelStatus.toDrivingHtml());
				activePanel.getTextPane().setCaretPosition(0);
				activePanel.getScroll().getVerticalScrollBar().setValue(0);
				activePanel.getNextTextPane().setContentType("text/html");
				activePanel.getNextTextPane().setText(travelStatus.toNextUpHtml());
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
			tripPlan = tripPlans.get(currentDay);
			report = reports.get(currentDay);
			setDay();
		}
	}

	/**
	 * Create the frame.
	 */
	public TravelMode() {
		setLayout(new BorderLayout());
		
		Font buttonFont = new Font("Arial", Font.BOLD, 32);
        JPanel buttonArea = new JPanel(); 
		this.add(buttonArea, BorderLayout.SOUTH);
		JPanel buttonPanel = new JPanel();
        buttonArea.add(buttonPanel, new FlowLayout(FlowLayout.LEFT, 5, 5));
        
        JButton drivingButton = new JButton("Driving");
        drivingButton.setFont(buttonFont);
        buttonPanel.add( drivingButton );
        drivingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				drivingMode();
			}
		});
        
        JButton fuelButton = new JButton("Fuel");
        fuelButton.setFont(buttonFont);
        buttonPanel.add( fuelButton );
        fuelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fuelMode();
			}
		});
        
 		JButton btnPauseResume = new JButton("Pause");
		btnPauseResume.setFont(buttonFont);
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
		btnPriorDay.setFont(buttonFont);
		buttonPanel.add(btnPriorDay);
		btnPriorDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				priorDay();
			}
		});

		JButton btnNextDay = new JButton("Next Day");
		btnNextDay.setFont(buttonFont);
		buttonPanel.add(btnNextDay);
		btnNextDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nextDay();
			}
		});

		JButton btnSelectDay = new JButton("Select Day");
		btnSelectDay.setFont(buttonFont);
		btnSelectDay.setActionCommand("SelectDay");
		btnSelectDay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post(new Events.UiEvent("SelectDay", event));
			}
		});
		buttonPanel.add(btnSelectDay);
		
		fuelPanel = new FuelPanel();
		this.add(fuelPanel, BorderLayout.CENTER);
		fuelPanel.setVisible(false);

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
		activePanel.getMapPanel().add(mapViewer, BorderLayout.CENTER);
		activePanel.getNextTextPane().setText("Hello");

		// try {
		// Image image = ImageIO.read(new File("images/compass.jpg"));
		// activePanel.getComponentsPanel().add(new ImageIcon(image));
		// } catch (Exception x) {}
		//

		SwingUtilities.updateComponentTreeUI(this);

		// Bind event handlers
		Events.eventBus.register(new UiHandler());

		// try {
		// HandleTripPlanUpdate obj = new HandleTripPlanUpdate();
		// TripPlanUpdate stub = (TripPlanUpdate)
		// UnicastRemoteObject.exportObject(obj, 0);
		// if (isSurface) {
		// // Bind the remote object's stub in the registry
		// Registry registry =
		// LocateRegistry.getRegistry(localHostAddress.toString(),
		// RobautoMain.REGISTRY_PORT);
		// registry.bind("Update", stub);
		// }
		// RobautoMain.logger.debug("Server ready");
		// } catch (Exception e) {
		// System.err.println("Server exception: " + e.toString());
		// }
	}

	protected void fuelMode() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (fuelPanel != null) {
					remove(fuelPanel);
				}
				activePanel.setVisible(false);
				fuelPanel = new FuelPanel(tripPlan.getFuelStops(), lafFont );
				fuelPanel.setVisible(true);
				add(fuelPanel, BorderLayout.CENTER);
			}
		});
	}

	protected void drivingMode() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				fuelPanel.setVisible(false);
				activePanel.setVisible(true);
			}
		});
	}

	protected void initialize(final Path base, final List<String> dayfiles) {
		activePanel.getProgressBar().setValue(-1);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				int dayNo = 1;
				DefaultListModel<String> model = new DefaultListModel<>();
				for (String dayfile : dayfiles) {
					File tripPlanFile = base.resolve(dayfile).toFile();
					tripPlan = TripPlan.load(tripPlanFile, frame);
					tripPlans.add(tripPlan);
					// Here2.reportRoute(tripPlan.getRoute());
					report = tripPlan.getTripReport();
					reports.add(report);
					ArrayList<ArrayList<VisitedPlace>> placesByDay = tripPlan.getFinalizedPlaces();
					for (ArrayList<VisitedPlace> stops : placesByDay) {
						int iLast = stops.size() - 1;
						Address from = stops.get(0).getAddress();
						RobautoMain.logger.debug(from.toString());
						Address to = stops.get(iLast).getAddress();
						RobautoMain.logger.debug(to.toString());
						String entry = String.format("Day %02d: %s", dayNo++, dayfile);
						RobautoMain.logger.debug(entry);
						model.addElement(entry);
					}
				}
				if (model.isEmpty()) {
					model.addElement("No Finalized Route Stops Found");
				}
				System.out.printf("%d, %d, %d\n", model.size(), reports.size(), tripPlans.size());
				tripPlan = tripPlans.get(0);
				report = reports.get(0);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (model.size() > 0)
							listOfDays.setModel(model);
						map.clearRoute();
					}
				});
			}
		});
		t.start();
		Events.eventBus.register(new GpsHandler());
		gps.initialize(frame, isSurface);
	}
	
	protected void initialize(File tripPlanFile) {
		activePanel.getProgressBar().setValue(-1);
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				tripPlan = TripPlan.load(tripPlanFile, frame);
				tripPlans.add(tripPlan);
				// Here2.reportRoute(tripPlan.getRoute());
				report = tripPlan.getTripReport();
				reports.add(report);

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
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (model.size() > 0)
							listOfDays.setModel(model);
						map.clearRoute();
					}
				});
			}
		});
		t.start();
		Events.eventBus.register(new GpsHandler());
		gps.initialize(frame, isSurface);
	}

	/**
	 * Launch the application.
	 */

	public static JFrame frame;

	@SuppressWarnings("unused")
	private static String localHostAddress;
	protected JScrollPane dayListScrollPane;
	
	protected List<ButtonWaypoint> waypoints;
	protected Font lafFont;

	public static Font initLookAndFeel(double textSizeInPixels, double fontSize) {
		Font lafFont = null;
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
					lafFont = new Font("Tahoma", Font.BOLD, (int) fontSize);
					RobautoMain.logger.debug(lafFont.toString());
					laf.getDefaults().put("defaultFont", lafFont);
					laf.getDefaults().put("ScrollBar.thumbHeight", (int) textSizeInPixels);
					laf.getDefaults().put("Table.rowHeight", (int) textSizeInPixels);
					break;
				}
			}
		} catch (Exception e) {
			// If Nimbus is not available, you can set the GUI to another look
			// and feel.
		}
		return lafFont;
	}

	public class ReportHandler extends AbstractHandler
	{
	    @Override
	    public void handle(String target,
	                       Request baseRequest,
	                       HttpServletRequest request,
	                       HttpServletResponse response) throws IOException,
	        ServletException
	    {
	        // Declare response encoding and types
	        response.setContentType("text/html; charset=utf-8");

	        // Declare response status code
	        response.setStatus(HttpServletResponse.SC_OK);

	        // Write back response
	        int dn = 1;
	        for (Report report : reports ) {
	        	Report.Day day = report.getDays().get(0);
	        	day.day = String.format("Day %d", dn++);
	        	response.getWriter().println(report.toHtml(day));
	        	response.getWriter().println(tripPlan.getObscuraHtml());
	        }

	        // Inform jetty that this request has now been handled
	        baseRequest.setHandled(true);
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
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Font lafFont =  initLookAndFeel(48,32); //(60, 48);
					frame = new JFrame();
					frame.setTitle("Robauto - Travel Mode");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					Image icon = ImageIO.read(new File("images/icon-travel.jpg")).getScaledInstance((int) 64, (int) 64,
							Image.SCALE_SMOOTH);
					frame.setIconImage(icon);

					TravelMode travelMode = new TravelMode();
					travelMode.lafFont = lafFont;
					
			        int port = 8080;
			        Server server = new Server(port);
			        server.setHandler(travelMode.new ReportHandler());
			        try {
				        server.start();
//				        server.join();
			        } catch (Exception x) {
			        	x.printStackTrace();
			        }
					

					frame.setContentPane(travelMode);
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					SwingUtilities.updateComponentTreeUI(frame);
					frame.pack();
					frame.setVisible(true);
					frame.addWindowListener(new WindowAdapter() {
						// @Override
						// public void windowActivated(WindowEvent e) {
						// try {
						// Thread.sleep(10*1000);
						// } catch (Exception x) {}
						// RobautoMain.logger.debug("toBack");
						// frame.toBack();
						// }
						@Override
						public void windowClosing(WindowEvent e) {
							System.out.println(frame.getBounds() );
							// 1455x935 on Surface Pro 3
							if (travelMode.gps != null) {
								travelMode.gps.shutdown();
							}
						}
					});
					String path = null;
					if (args.length > 0)
						path = args[0];
					else {
						// Create a file chooser
						final JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileFilter(new FileFilter() {
							@Override
							public boolean accept(File f) {
								if (f.isDirectory())
									return true;
								String name = f.getName().toLowerCase();
								if (name.endsWith(".robauto"))
									return true;
								return false;
							}

							@Override
							public String getDescription() {
								return "Robauto TripPlans";
							}
						});
						// In response to a button click:
						int returnVal = fileChooser.showOpenDialog(frame);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							path = fileChooser.getSelectedFile().getAbsolutePath();
							// This is where a real application would open the file.
						}
					}
					if (path == null) {
						System.err.println("No trip plan file specified");
						System.exit(0);
					}
					if (path.endsWith(".dayfile")) {
						List<String> lines = Files.readAllLines( Paths.get(path) );
						RobautoMain.logger.debug(path);
						for (String day : lines) 
							RobautoMain.logger.debug(day);
						travelMode.initialize(Paths.get(path).getParent(), lines);
					} else {
						RobautoMain.logger.debug(path);
						final File tripPlanFile = new File(path);
						travelMode.initialize(tripPlanFile);
					}
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							JPanel glass = (JPanel) frame.getGlassPane();
							glass.setLayout(new BorderLayout());
							glass.add(new Overlay(), BorderLayout.CENTER);
							glass.setVisible(true);							
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
