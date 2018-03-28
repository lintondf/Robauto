package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.Events.AddWaypointEvent;
import com.bluelightning.Events.POIClickEvent;
import com.bluelightning.Events.StopsCommitEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.RobautoMain.ModeHandler;
import com.bluelightning.data.TripPlan;
import com.bluelightning.data.TripPlan.DriverAssignments;
import com.bluelightning.data.TripPlan.LegData;
import com.bluelightning.data.TripPlan.RoadDirectionData;
import com.bluelightning.data.TripPlan.StopData;
import com.bluelightning.gui.AddAddressDialog;
import com.bluelightning.gui.MainControlPanel;
import com.bluelightning.gui.MainPanel;
import com.bluelightning.gui.OptimizeStopsDialog;
import com.bluelightning.gui.RoutePanel;
import com.bluelightning.gui.WebBrowser;
import com.bluelightning.gui.AddAddressDialog.AddAddressActionListener;
import com.bluelightning.gui.OptimizeStopsDialog.OptimizeActionListener;
import com.bluelightning.gui.OptimizeStopsDialog.OptimizeLegSelectionListener;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.POIMarker;
import com.bluelightning.map.StopMarker;
import com.bluelightning.poi.MurphyPOI;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.RestAreaPOI;
import com.bluelightning.poi.SamsClubPOI;
import com.bluelightning.poi.TruckStopPOI;
import com.bluelightning.poi.WalmartPOI;
import com.bluelightning.poi.POI.FuelAvailable;
import com.google.common.eventbus.Subscribe;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.place.ReadOnlyPlace;
import seedu.addressbook.data.place.VisitedPlace;
import seedu.addressbook.logic.Logic;

// http://forecast.weather.gov/MapClick.php?lat=28.23&lon=-80.7&FcstType=digitalDWML

public class PlannerMode extends JPanel {

	protected static final String INITIAL_RESULTS_TEXT = "<html><center>Run Optimize Stops to populate this panel</center></html>";

	protected File tripPlanFile = new File("RobautoTripPlan.obj");
	protected List<ButtonWaypoint> waypoints;
	protected Map map;
	protected JXMapViewer mapViewer;
	protected RoutePanel routePanel;
	protected MainControlPanel controlPanel;
	protected EnumMap<PlannerMode.MarkerKinds, POISet> poiMap = new EnumMap<>(PlannerMode.MarkerKinds.class);
	protected EnumMap<PlannerMode.MarkerKinds, ArrayList<POIResult>> nearbyMap = new EnumMap<>(PlannerMode.MarkerKinds.class);
	protected ArrayList<ButtonWaypoint> nearby = new ArrayList<>();
	protected MainPanel mainPanel;
	// protected WebBrowser browserCanvas;
	protected Logic controller;
	protected AddressBook addressBook;
	protected JTextPane resultsPane;

	protected ch.qos.logback.classic.Logger rootLogger;

	protected TextAreaAppender textAreaAppender;
	
	public class POIClickHandler {
		@Subscribe
		protected void handle(POIClickEvent event) {
			System.out.println(event.toString());
			// browserCanvas.moveTo(event.poi.getLatitude(),
			// event.poi.getLongitude());
			// int index =
			// mainPanel.getRightTabbedPane().indexOfTab("AllStays");
			// mainPanel.getRightTabbedPane().setSelectedIndex(index);
		}
	}

	public void insureNearbyMapLoaded(Route route, PlannerMode.MarkerKinds key, POISet pset) {
		nearbyMap.put(key, pset.getPointsOfInterestAlongRoute(route, controlPanel.getMarkerSearchRadius(key)));
	}

	public static BufferedImage getScreenShot(Component component) {

		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(),
				BufferedImage.TYPE_INT_RGB);
		// call the Component's paint method, using
		// the Graphics object of the image.
		component.paint(image.getGraphics()); // alternately use .printAll(..)
		return image;
	}

	public class UiHandler {
		@Subscribe
		protected void handle(StopsCommitEvent event) {
			resultsPane.setText(event.html);
		}

		@Subscribe
		protected void handle(UiEvent event) {
			//System.out.println(event.source + " " + event.awtEvent);
			switch (event.source) {
			case "RoutePanel.AddAfter":
				RobautoMain.tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeAdd(true);
					}
				});
				break;
			case "RoutePanel.AddBefore":
				RobautoMain.tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeAdd(false);
					}
				});
				break;
			case "RoutePanel.MoveDown":
				RobautoMain.tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeMoveDown();
					}
				});
				break;
			case "RoutePanel.MoveUp":
				RobautoMain.tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeMoveUp();
					}
				});
				break;
			case "RoutePanel.Remove":
				RobautoMain.tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeRemove();
					}
				});
				break;
			case "ControlPanel.Route":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						route();
					}
				});
				break;

			case "ControlPanel.Optimize":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (RobautoMain.tripPlan.getRoute() != null) {
							optimizeStops();
						}
					}
				});
				break;
				
			case "ControlPanel.Finalize":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (RobautoMain.tripPlan.getRoute() != null) {
							finalizeRoute();
						}
					}
				});
				break;

			case "ControlPanel.Waypoints":
				nearby.clear();
				nearby.addAll(waypoints); // always add stops along route
				for (PlannerMode.MarkerKinds key : poiMap.keySet()) {
					if (controlPanel.getMarkerStatus(key) && !nearbyMap.containsKey(key)) {
						POISet pset = poiMap.get(key);
						if (pset != null) {
							controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							insureNearbyMapLoaded(RobautoMain.tripPlan.getRoute(), key, pset);
							controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}
				for (Entry<PlannerMode.MarkerKinds, ArrayList<POIResult>> entry : nearbyMap.entrySet()) {
					if (controlPanel.getMarkerStatus(entry.getKey())) {
						System.out.println("Adding " + entry.getValue().size() + " " + entry.getKey().toString());
						nearby.addAll(POIMarker.factory(entry.getValue()));
					}
				}
				System.out.println(nearby.size() + " total markers");
				map.updateWaypoints(nearby);
				break;

			case "ControlPanel.CoPilotOutput":
				if (! RobautoMain.tripPlan.getFinalizedPlaces().isEmpty()) {
					try {
						outputToCopilot( RobautoMain.tripPlan.getFinalizedPlaces() );
					} catch (Exception x) {
						x.printStackTrace();
					}
				}
				BufferedImage i = getScreenShot( mapViewer );
				try {
				    File outputfile = new File("saved.png");
				    ImageIO.write(i, "png", outputfile);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				
			case "ControlPanel.TravelMode":
				try {
					String javaDir = "C:\\ProgramData\\Oracle\\Java\\javapath\\java.exe";
					ProcessBuilder pb = new ProcessBuilder();
					pb.command( Arrays.asList( javaDir, "-cp", "Robauto.jar", "com.bluelightning.TravelMode" ) );
					pb.redirectErrorStream(true);
					Process p = pb.start();
					System.exit(0);
				} catch (Exception x) {
					x.printStackTrace();
				}
				break;
				
			case "ControlPanel.ClearActions":
				System.out.println(event.source + " " + event.awtEvent);
				JComboBox box = (JComboBox) event.awtEvent.getSource();
				String action = (String) box.getSelectedItem();
				System.out.println( action );
				RobautoMain.tripPlan.clear( action );
				break;
				
			default:
				break;
			}
		}

		private void finalizeRoute() {
			RobautoMain.logger.info("Finalizing route...");
			if (RobautoMain.tripPlan.getPlacesChanged()) {
				RobautoMain.tripPlan.setRoute(null);
				RobautoMain.tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
				RobautoMain.logger.info("  Route points saved");
			}
			map.clearRoute();
			ArrayList<Route> days = RobautoMain.tripPlan.getFinalizedDays();
			ArrayList<Integer> markers = RobautoMain.tripPlan.getFinalizedMarkers();
			if (days.isEmpty()) {
				ArrayList<ArrayList<VisitedPlace>> allPlaces = new ArrayList<>();
				markers.add(StopMarker.ORIGIN);
				for (TripPlan.TripLeg tripLeg : RobautoMain.tripPlan.getTripLegs()) {
					ArrayList<VisitedPlace> places = new ArrayList<>();
					String[] fields = tripLeg.legData.startLabel.split("/");
					if (fields.length < 2) {
						RobautoMain.logger.error("Invalid VisitedPlace label: " + tripLeg.legData.startLabel);
						return;
					}
					List<ReadOnlyPlace> startMatches = addressBook.getPlacesWithAddress(fields[1]);
					if (startMatches.isEmpty()) {
						RobautoMain.logger.error("No matching address book entry for: " + tripLeg.legData.startLabel);
						return;
					}
					VisitedPlace start = new VisitedPlace(startMatches.get(0));
					places.add(start);
					int refuel = 0;
					System.out.println(tripLeg.legData.startLabel);
					tripLeg.stopDataList.forEach(System.out::println);
					for (TripPlan.StopData stopData : tripLeg.stopDataList) {
						if (stopData.use) {
							try {
								places.add(new VisitedPlace(stopData));
								refuel = (stopData.refuel) ? StopMarker.FUEL : 0;
								markers.add(StopMarker.DRIVERS + refuel);
							} catch (IllegalValueException e) {
								e.printStackTrace();
							}
						}
					} // for stopData
					markers.set(markers.size() - 1, StopMarker.OVERNIGHT + refuel);
					// places.forEach( place -> {
					// Main.logger.debug(place.toString());
					// });
					days.add(Here2.computeRoute(places));
					allPlaces.add(places);
				} // tripLeg
				markers.set(markers.size() - 1, StopMarker.TERMINUS);
				RobautoMain.tripPlan.setFinalizedRoute(days, markers, allPlaces);
			}
			waypoints = map.showRoute(days, markers);
			int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
			mainPanel.getRightTabbedPane().setSelectedIndex(index);
			RobautoMain.logger.info("  Route shown on map");
		}

		private void route() {
			RobautoMain.logger.info("Planning route...");
			RobautoMain.tripPlan.setFinalizedRoute(null, null, null);
			if (RobautoMain.tripPlan.getPlacesChanged()) {
				RobautoMain.tripPlan.setRoute(null);
				RobautoMain.tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
				RobautoMain.logger.info("  Route points saved");
			}
			Route route = Here2.computeRoute(RobautoMain.tripPlan); // will reload from
														// routeJson if not
														// empty
			RobautoMain.logger.info("  Route planned");
			if (route != null) {
				waypoints = map.showRoute(route);
				int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
				mainPanel.getRightTabbedPane().setSelectedIndex(index);
				RobautoMain.logger.info("  Route shown on map");
				RobautoMain.tripPlan.setRoute(route);
			}
			RobautoMain.tripPlan.save(tripPlanFile);
			RobautoMain.logger.info("  Trip plan saved");
		}

		private void optimizeStops() {
			if (!RobautoMain.tripPlan.getFinalizedDays().isEmpty()) {
				waypoints = map.showRoute(RobautoMain.tripPlan.getRoute());
				RobautoMain.tripPlan.setFinalizedRoute(null, null, null);
			}
			EnumMap<PlannerMode.MarkerKinds, POISet> poiMap = new EnumMap<>(PlannerMode.MarkerKinds.class);
			EnumMap<PlannerMode.MarkerKinds, ArrayList<POIResult>> nearbyMap = new EnumMap<>(PlannerMode.MarkerKinds.class);
			PlannerMode.loadPOIMap(poiMap);
			nearbyMap.clear();
			poiMap.forEach((kind, set) -> {
				nearbyMap.put(kind, set.getPointsOfInterestAlongRoute(RobautoMain.tripPlan.getRoute(), 2e3));
			});

			OptimizeStops optimizeStops = new OptimizeStops(RobautoMain.tripPlan, controller, addressBook, poiMap, nearbyMap);

			dialog = new OptimizeStopsDialog(optimizeStops);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);

			int iLeg = 0; // start with first leg
			dialog.setCurrentLeg(RobautoMain.tripPlan.getTripLeg(iLeg));

			dialog.addListeners(dialog.new OptimizeActionListener(), dialog.new OptimizeLegSelectionListener(),
					dialog.new OptimizeRoadModelListener());

			dialog.generateLegStopChoices(iLeg);
			final String html = dialog.updateTripData();

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					resultsPane.setText(html);
				}
			});
		}

		private CallbackHandler handler = null;
		protected OptimizeStopsDialog dialog = null;

		private class CallbackHandler {
			boolean addAfter = true;

			public CallbackHandler(boolean after) {
				addAfter = after;
			}

			@Subscribe
			protected void handle(AddWaypointEvent event) {
				System.out.println(event + " " + event.place);
				Events.eventBus.unregister(this); // one shot
				handler = null;
				map.clearRoute();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
						VisitedPlace place = new VisitedPlace(event.place);
						if (place.getFuelAvailable().get() == FuelAvailable.NO_FUEL)
							place.setFuelAvailable(getNearByFuel(place.getLatitude(), place.getLongitude(), 2e3));
						ArrayList<VisitedPlace> places = routePanel.getWaypointsModel().getData();
						if (places == null)
							places = new ArrayList<>();
						if (selectedWaypointRow < 0) {
							places.add(place);
						} else if (addAfter) {
							places.add(selectedWaypointRow + 1, place);
						} else {
							places.add(selectedWaypointRow, place);
						}
						routePanel.getWaypointsModel().setData(places);
						RobautoMain.tripPlan.setPlaces(places);
					}
				});
			}
		}

		private void routeAdd(boolean after) {
			Events.eventBus.register(new CallbackHandler(after));
			AddAddressDialog dialog = new AddAddressDialog(controller, addressBook);
			AddAddressActionListener listener = dialog.new AddAddressActionListener();
			dialog.setListener(listener);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}

		private void routeMoveDown() { // increase index position
			ArrayList<VisitedPlace> places = routePanel.getWaypointsModel().getData();
			if (places == null)
				places = new ArrayList<>();
			int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
			if (selectedWaypointRow < 0 || selectedWaypointRow >= places.size() - 1)
				return; // nothing selected or selection already at end
			// swap this and next
			VisitedPlace place = places.get(selectedWaypointRow);
			places.set(selectedWaypointRow, places.get(selectedWaypointRow + 1));
			places.set(selectedWaypointRow + 1, place);
			routePanel.getWaypointsModel().setData(places);
			RobautoMain.tripPlan.setPlaces(places);
			map.clearRoute();
		}

		private void routeMoveUp() { // decrease index position
			ArrayList<VisitedPlace> places = routePanel.getWaypointsModel().getData();
			if (places == null)
				places = new ArrayList<>();
			int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
			if (selectedWaypointRow <= 0)
				return; // nothing selected or selection already first
			// swap this and prior
			VisitedPlace place = places.get(selectedWaypointRow);
			places.set(selectedWaypointRow, places.get(selectedWaypointRow - 1));
			places.set(selectedWaypointRow - 1, place);
			routePanel.getWaypointsModel().setData(places);
			RobautoMain.tripPlan.setPlaces(places);
			map.clearRoute();
		}

		private void routeRemove() {
			ArrayList<VisitedPlace> places = routePanel.getWaypointsModel().getData();
			if (places == null)
				return;
			int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
			if (selectedWaypointRow < 0)
				return; // nothing selected
			// swap this and prior
			VisitedPlace place = places.get(selectedWaypointRow);
			places.remove(selectedWaypointRow);
			routePanel.getWaypointsModel().setData(places);
			RobautoMain.tripPlan.setPlaces(places);
			map.clearRoute();
		}
	}

	public static enum MarkerKinds {
		WALMARTS, SAMSCLUBS, COSTCOS, TRUCKSTOPS, RESTAREAS, MURPHY
	}

	// static support test use
	public static void loadPOIMap(EnumMap<PlannerMode.MarkerKinds, POISet> poiMap) {
		POISet pset = WalmartPOI.factory();
		poiMap.put(PlannerMode.MarkerKinds.WALMARTS, pset);
		pset = SamsClubPOI.factory();
		poiMap.put(PlannerMode.MarkerKinds.SAMSCLUBS, pset);
		pset = RestAreaPOI.factory();
		poiMap.put(PlannerMode.MarkerKinds.RESTAREAS, pset);
		pset = TruckStopPOI.factory();
		poiMap.put(PlannerMode.MarkerKinds.TRUCKSTOPS, pset);
		pset = MurphyPOI.factory();
		poiMap.put(PlannerMode.MarkerKinds.MURPHY, pset);
		// TODO Costco, Cabelas
	}

	public POI.FuelAvailable getNearByFuel(double latitude, double longitude, double radius) {
		GeoPosition position = new GeoPosition(latitude, longitude);
		for (POISet pset : poiMap.values()) {
			java.util.Map<POI, POIResult> map = pset.nearBy(position, 0, radius);
			if (!map.isEmpty()) {
				POIResult closest = null;
				for (POIResult r : map.values()) {
					if (closest == null) {
						closest = r;
					} else {
						if (r.distance < closest.distance) {
							closest = r;
						}
					}
				}
				if (closest != null) {
					return closest.poi.getFuelAvailable();
				}
			}
		}
		return new POI.FuelAvailable();
	}

	public class TextAreaAppender extends AppenderBase<ILoggingEvent> {

		Layout<ILoggingEvent> layout;

		public TextAreaAppender(LoggerContext lc) {
			setContext(lc);
			setName("robauto");
			PatternLayout layout = new PatternLayout();
			layout.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level: %msg%n");
			layout.setContext(lc);
			layout.start();
			setLayout(layout);
		}

		public Layout<ILoggingEvent> getLayout() {
			return layout;
		}

		public void setLayout(Layout<ILoggingEvent> layout) {
			this.layout = layout;
		}

		@Override
		public void start() {
			if (this.layout == null) {
				addError("No layout set for the appender named [" + name + "].");
				return;
			}
			super.start();
		}

		@Override
		protected void append(ILoggingEvent event) {
			// System.out.println("TAE: " + event.toString() + " " + layout);
			final String message = layout.doLayout(event);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					StringBuffer sb = new StringBuffer(mainPanel.getLowerTextArea().getText());
					sb.append(message);
					mainPanel.getLowerTextArea().setText(sb.toString());
				}
			});
		}

	}

	public EnumMap<PlannerMode.MarkerKinds, ArrayList<POIResult>> getNearbyMap() {
		return nearbyMap;
	}

	public JTextPane getResultsPane() {
		return resultsPane;
	}

	public void setResultsPane(JTextPane resultsPane) {
		this.resultsPane = resultsPane;
	}

	public PlannerMode() {
		setLayout(new BorderLayout());
		RobautoMain.logger = LoggerFactory.getLogger("com.bluelightning.Robauto");
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
		textAreaAppender = new TextAreaAppender(lc);
		textAreaAppender.start();

		mainPanel = new MainPanel();
		mainPanel.getLeftPanel().setLayout(new BorderLayout());
		routePanel = new RoutePanel(new Events.EventActionListener());
		mainPanel.getRightTabbedPane().addTab("Route", null, routePanel, null);
		map = new com.bluelightning.Map();
		mapViewer = map.getMapViewer();
		mainPanel.getRightTabbedPane().addTab("Map", null, mapViewer, null);
		// browserCanvas = WebBrowser.factory(mainPanel);
		controlPanel = new MainControlPanel();
		mainPanel.getLeftPanel().add(controlPanel);
		this.add(mainPanel);

		resultsPane = new JTextPane();
		resultsPane.setVisible(true);
		resultsPane.setContentType("text/html");
		resultsPane.setEditable(false);
		resultsPane.setText(INITIAL_RESULTS_TEXT);
		JScrollPane scroll = new JScrollPane(resultsPane);
		mainPanel.getRightTabbedPane().addTab("Results", null, scroll, null);
	}
	
	public void initialize() {
		// browserCanvas.initialize(frame);

		// logging to text area can start once frame is visible
		rootLogger.addAppender(textAreaAppender);
		RobautoMain.logger.info("Robauto Planner starting");

		// load base POI sets
		RobautoMain.logger.info("Loading points of interest");
		loadPOIMap(poiMap);

		// Bind event handlers
		Events.eventBus.register(new UiHandler());
		Events.eventBus.register(new POIClickHandler());
		try {
			controller = new Logic();
			controller.getStorage().load();
			addressBook = controller.getAddressBook();
		} catch (Exception x) {
			RobautoMain.logger.trace(x.getMessage());
		}

		RobautoMain.logger.info("Loading previous trip plan");
		RobautoMain.tripPlan = TripPlan.load(tripPlanFile);
		routePanel.getWaypointsModel().setData(RobautoMain.tripPlan.getPlaces());
		if (!RobautoMain.tripPlan.getFinalizedDays().isEmpty()) {
			Events.eventBus.post(new Events.UiEvent("ControlPanel.Finalize", null));
			reportDetails(RobautoMain.tripPlan.getFinalizedDays());
		} else if (RobautoMain.tripPlan.getRoute() != null) {
			Events.eventBus.post(new Events.UiEvent("ControlPanel.Route", null));
		}
		Report report = RobautoMain.tripPlan.getTripReport();
		if (report != null) {
			String html = report.toHtml();
			resultsPane.setText(html);
		}
	}

	private void outputToCopilot(ArrayList<ArrayList<VisitedPlace>> finalizedPlaces) {
		CoPilot13Format format = new CoPilot13Format();
		int iDay = 1;
		for (ArrayList<VisitedPlace> dayStops : finalizedPlaces) {
			try {
				String title = String.format("Day-%02d", iDay++);
				String opath = String.format("\\\\Surfacepro3\\na\\save\\%s.trp", title);
				PrintWriter stream = new PrintWriter(opath, CoPilot13Format.UTF16LE_ENCODING);
				// PrintWriter stream = new PrintWriter(System.out);
				format.write(title, dayStops, stream, 0, dayStops.size());
				stream.close();
			} catch (Exception x) {
				RobautoMain.logger.error("Failed to write CoPilot file: ", x);
				x.printStackTrace();
			}
		}
	}

	private void reportDetails(ArrayList<Route> finalizedDays) {
		int iDay = 1;
		for (Route dayRoute : finalizedDays) {
			System.out.printf("Day %d\n", iDay);
			iDay++;
			double dayTime = 0.0;
			double dayDistance = 0.0;
			for (Leg leg : dayRoute.getLeg()) {
				System.out.printf("  Leg from %s to %s\n", leg.getStart().getUserLabel(), leg.getEnd().getUserLabel());
				double legTime = 0.0;
				double legDistance = 0.0;
				for (Maneuver maneuver : leg.getManeuver()) {
					dayTime += maneuver.getTrafficTime();
					dayDistance += maneuver.getLength();
					legTime += maneuver.getTrafficTime();
					legDistance += maneuver.getLength();
					System.out.printf("    %6.1f %-6s  %6.1f %-6s %6.1f %-6s: %s\n",
							dayDistance * Here2.METERS_TO_MILES, Here2.toPeriod(dayTime),
							legDistance * Here2.METERS_TO_MILES, Here2.toPeriod(legTime),
							maneuver.getLength() * Here2.METERS_TO_MILES, Here2.toPeriod(maneuver.getTrafficTime()),
							maneuver.getInstruction());
				}
			}
		}
	}

	public static JFrame frame;

	private static void initLookAndFeel(double textSizeInPixels, double fontSize) {
		try {
			System.out.println( Toolkit.getDefaultToolkit().getScreenSize() );
			//double fontSize = 0.8 * textSizeInPixels * Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
			System.out.printf("%f %d %f\n", textSizeInPixels, Toolkit.getDefaultToolkit().getScreenResolution(), fontSize);
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            NimbusLookAndFeel laf = (NimbusLookAndFeel) UIManager.getLookAndFeel();
		            //laf.getDefaults().entrySet().forEach(System.out::println);
		            Font font = new Font("Tahoma", Font.BOLD, (int) fontSize );
		            System.out.println(font);
		            laf.getDefaults().put("defaultFont", font );
		            laf.getDefaults().put("ScrollBar.thumbHeight", (int) textSizeInPixels);
		            laf.getDefaults().put("Table.rowHeight", (int) textSizeInPixels); 
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
	}
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					initLookAndFeel( 30, 24 );
					frame = new JFrame();
					frame.setTitle("Robauto - Planner Mode");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					
					PlannerMode plannerMode = new PlannerMode();
					
					frame.setContentPane(plannerMode);
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
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
