package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.Events.AddWaypointEvent;
import com.bluelightning.Events.POIClickEvent;
import com.bluelightning.Events.StopsCommitEvent;
import com.bluelightning.Events.UiEvent;
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

public class Main {
	
	protected static final String INITIAL_RESULTS_TEXT = "<html><center>Run Optimize Stops to populate this panel</center></html>";

	protected File tripPlanFile = new File("RobautoTripPlan.obj");
	protected TripPlan tripPlan;
	protected List<ButtonWaypoint> waypoints;
	protected Map map;
	protected JXMapViewer mapViewer;
	protected RoutePanel routePanel;
	protected MainControlPanel controlPanel;
	protected EnumMap<Main.MarkerKinds, POISet> poiMap = new EnumMap<>(Main.MarkerKinds.class);
	protected EnumMap<Main.MarkerKinds, ArrayList<POIResult>> nearbyMap = new EnumMap<>(Main.MarkerKinds.class);
	protected ArrayList<ButtonWaypoint> nearby = new ArrayList<>();
	protected MainPanel mainPanel;
	protected WebBrowser browserCanvas;
	protected Logic controller;
	protected AddressBook addressBook;
	public static Logger logger;
	protected JTextPane resultsPane;

	public class POIClickHandler {
		@Subscribe
		protected void handle(POIClickEvent event) {
			System.out.println(event.toString());
			browserCanvas.moveTo(event.poi.getLatitude(), event.poi.getLongitude());
			int index = mainPanel.getRightTabbedPane().indexOfTab("AllStays");
			mainPanel.getRightTabbedPane().setSelectedIndex(index);
		}
	}

	public void insureNearbyMapLoaded(Route route, Main.MarkerKinds key, POISet pset) {
		nearbyMap.put(key, pset.getPointsOfInterestAlongRoute(route, controlPanel.getMarkerSearchRadius(key)));
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
				tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeAdd(true);
					}
				});
				break;
			case "RoutePanel.AddBefore":
				tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeAdd(false);
					}
				});
				break;
			case "RoutePanel.MoveDown":
				tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeMoveDown();
					}
				});
				break;
			case "RoutePanel.MoveUp":
				tripPlan.setPlacesChanged(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeMoveUp();
					}
				});
				break;
			case "RoutePanel.Remove":
				tripPlan.setPlacesChanged(true);
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
						if (tripPlan.getRoute() != null) {
							optimizeStops();
						}
					}
				});
				break;
				
			case "ControlPanel.Finalize":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (tripPlan.getRoute() != null) {
							finalizeRoute();
						}
					}
				});
				break;

			case "ControlPanel.Waypoints":
				nearby.clear();
				nearby.addAll(waypoints); // always add stops along route
				for (Main.MarkerKinds key : poiMap.keySet()) {
					if (controlPanel.getMarkerStatus(key) && !nearbyMap.containsKey(key)) {
						POISet pset = poiMap.get(key);
						if (pset != null) {
							controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							insureNearbyMapLoaded(tripPlan.getRoute(), key, pset);
							controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}
				for (Entry<Main.MarkerKinds, ArrayList<POIResult>> entry : nearbyMap.entrySet()) {
					if (controlPanel.getMarkerStatus(entry.getKey())) {
						System.out.println("Adding " + entry.getValue().size() + " " + entry.getKey().toString());
						nearby.addAll(POIMarker.factory(entry.getValue()));
					}
				}
				System.out.println(nearby.size() + " total markers");
				map.updateWaypoints(nearby);
				break;

			default:
				break;
			}
		}
		
		private void finalizeRoute() {
			logger.info("Finalizing route...");
			if (tripPlan.getPlacesChanged()) {
				tripPlan.setRoute(null);
				tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
				logger.info("  Route points saved");
			}
			map.clearRoute();
			ArrayList<Route> days = tripPlan.getFinalizedDays();
			ArrayList<Integer> markers = tripPlan.getFinalizedMarkers();
			if (days.isEmpty()) {
				markers.add( StopMarker.ORIGIN );
				for (TripPlan.TripLeg tripLeg : tripPlan.getTripLegs()) {
					ArrayList<VisitedPlace> places = new ArrayList<>();
					String[] fields = tripLeg.legData.startLabel.split("/");
					if (fields.length < 2) {
						Main.logger.error("Invalid VisitedPlace label: " + tripLeg.legData.startLabel);
						return;
					}
					List<ReadOnlyPlace> startMatches = addressBook.getPlacesWithAddress(fields[1]);
					if (startMatches.isEmpty()) {
						Main.logger.error("No matching address book entry for: " + tripLeg.legData.startLabel);
						return;
					}
					VisitedPlace start = new VisitedPlace( startMatches.get(0) );
					places.add(start);
					int refuel = 0;
					System.out.println(tripLeg.legData.startLabel);
					tripLeg.stopDataList.forEach(System.out::println);
					for (TripPlan.StopData stopData : tripLeg.stopDataList) {
						if (stopData.use) {
							try {
								places.add( new VisitedPlace(stopData) );
								refuel = (stopData.refuel) ? StopMarker.FUEL : 0;
								markers.add( StopMarker.DRIVERS + refuel );
							} catch (IllegalValueException e) {
								e.printStackTrace();
							}
						}
					} // for stopData
					markers.set( markers.size()-1, StopMarker.OVERNIGHT + refuel);
					places.forEach( place -> {
						Main.logger.debug(place.toString());
					});
					days.add( Here2.computeRoute(places) );
				} // tripLeg
				markers.set( markers.size()-1, StopMarker.TERMINUS );
				tripPlan.setFinalizedRoute(days, markers);
			}
			waypoints = map.showRoute( days, markers );
			int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
			mainPanel.getRightTabbedPane().setSelectedIndex(index);
			logger.info("  Route shown on map");
		}

		private void route() {
			logger.info("Planning route...");
			tripPlan.setFinalizedRoute(null, null);
			//tripPlan.debugClear();
			if (tripPlan.getPlacesChanged()) {
				tripPlan.setRoute(null);
				tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
				logger.info("  Route points saved");
			}
			Route route = Here2.computeRoute(tripPlan);  // will reload from routeJson if not empty
			logger.info("  Route planned");
			if (route != null) {
				waypoints = map.showRoute(route);
				int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
				mainPanel.getRightTabbedPane().setSelectedIndex(index);
				logger.info("  Route shown on map");
				tripPlan.setRoute( route );
			}
			tripPlan.save(tripPlanFile);
			logger.info("  Trip plan saved");
		}

		private void optimizeStops() {
			if (!tripPlan.getFinalizedDays().isEmpty()) {
				waypoints = map.showRoute(tripPlan.getRoute());
				tripPlan.setFinalizedRoute(null, null);
			}
			EnumMap<Main.MarkerKinds, POISet> poiMap = new EnumMap<>(Main.MarkerKinds.class);
			EnumMap<Main.MarkerKinds, ArrayList<POIResult>> nearbyMap = new EnumMap<>(Main.MarkerKinds.class);
			Main.loadPOIMap(poiMap);
			nearbyMap.clear();
			poiMap.forEach((kind, set) -> {
				nearbyMap.put(kind, set.getPointsOfInterestAlongRoute(tripPlan.getRoute(), 5e3));
			});

			OptimizeStops optimizeStops = new OptimizeStops(tripPlan, controller, addressBook, poiMap, nearbyMap);

			dialog = new OptimizeStopsDialog(optimizeStops);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);

			int iLeg = 0; // start with first leg
			dialog.setCurrentLeg(tripPlan.getTripLeg(iLeg));

			dialog.addListeners(dialog.new OptimizeActionListener(), dialog.new OptimizeLegSelectionListener(),
					dialog.new OptimizeRoadModelListener());

			dialog.generateLegStopChoices( iLeg );
			final String html = dialog.updateTripData();
			
			SwingUtilities.invokeLater( new Runnable() {
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
						place.setFuelAvailable( getNearByFuel(place.getLatitude(), place.getLongitude(), 1e3) );
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
						tripPlan.setPlaces(places);
					}
				});
			}
		}

		private void routeAdd(boolean after) {
			Events.eventBus.register(new CallbackHandler(after));
			AddAddressDialog dialog = new AddAddressDialog(controller, addressBook);
			AddAddressActionListener listener = dialog.new AddAddressActionListener();
			dialog.setListener( listener );
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
			tripPlan.setPlaces(places);
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
			tripPlan.setPlaces(places);
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
			tripPlan.setPlaces(places);
			map.clearRoute();
		}
	}

	public static enum MarkerKinds {
		WALMARTS, SAMSCLUBS, COSTCOS, TRUCKSTOPS, RESTAREAS, MURPHY
	}

	// static support test use
	public static void loadPOIMap(EnumMap<Main.MarkerKinds, POISet> poiMap) {
		POISet pset = WalmartPOI.factory();
		poiMap.put(Main.MarkerKinds.WALMARTS, pset);
		pset = SamsClubPOI.factory();
		poiMap.put(Main.MarkerKinds.SAMSCLUBS, pset);
		pset = RestAreaPOI.factory();
		poiMap.put(Main.MarkerKinds.RESTAREAS, pset);
		pset = TruckStopPOI.factory();
		poiMap.put(Main.MarkerKinds.TRUCKSTOPS, pset);
		pset = MurphyPOI.factory();
		poiMap.put(Main.MarkerKinds.MURPHY, pset);
		// TODO Costco, Cabelas
	}
	
	
	public POI.FuelAvailable getNearByFuel(double latitude, double longitude, double radius ) {
		GeoPosition position = new GeoPosition( latitude, longitude );
		for (POISet pset : poiMap.values()) {
			java.util.Map<POI, POIResult> map = pset.nearBy( position, 0, radius );
			if (! map.isEmpty()) {
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

	public EnumMap<Main.MarkerKinds, ArrayList<POIResult>> getNearbyMap() {
		return nearbyMap;
	}

	public JTextPane getResultsPane() {
		return resultsPane;
	}

	public void setResultsPane(JTextPane resultsPane) {
		this.resultsPane = resultsPane;
	}

	
	
	public Main() {
		logger = LoggerFactory.getLogger("com.bluelightning.Robauto");
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.Logger rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
		TextAreaAppender textAreaAppender = new TextAreaAppender(lc);
		textAreaAppender.start();

		// Display the viewer in a JFrame
		JFrame frame = new JFrame("RobAuto RV Trip Planner");
		mainPanel = new MainPanel();
		mainPanel.getLeftPanel().setLayout(new BorderLayout());
		routePanel = new RoutePanel(new Events.EventActionListener());
		mainPanel.getRightTabbedPane().addTab("Route", null, routePanel, null);
		map = new com.bluelightning.Map();
		mapViewer = map.getMapViewer();
		mainPanel.getRightTabbedPane().addTab("Map", null, mapViewer, null);
		browserCanvas = WebBrowser.factory(mainPanel);
		controlPanel = new MainControlPanel();
		mainPanel.getLeftPanel().add(controlPanel);
		frame.setContentPane(mainPanel);
		
		resultsPane = new JTextPane();
		resultsPane.setVisible(true);
		resultsPane.setContentType("text/html");
		resultsPane.setEditable(false);
		resultsPane.setText( INITIAL_RESULTS_TEXT );
		JScrollPane scroll = new JScrollPane(resultsPane);
		mainPanel.getRightTabbedPane().addTab("Results", null, scroll, null);
		// frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		browserCanvas.initialize(frame);

		// logging to text area can start once frame is visible
		rootLogger.addAppender(textAreaAppender);
		logger.info("Robauto Planner starting");

		// load base POI sets
		logger.info("Loading points of interest");
		loadPOIMap(poiMap);

		// Bind event handlers
		Events.eventBus.register(new UiHandler());
		Events.eventBus.register(new POIClickHandler());
		try {
			controller = new Logic();
			controller.getStorage().load();
			addressBook = controller.getAddressBook();
		} catch (Exception x) {
			logger.trace( x.getMessage() );
		}
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
				tripPlan.save(tripPlanFile);
			}
		});

		logger.info("Loading previous trip plan");
		tripPlan = TripPlan.load(tripPlanFile);
		routePanel.getWaypointsModel().setData(tripPlan.getPlaces());
		if (! tripPlan.getFinalizedDays().isEmpty()) {
			Events.eventBus.post(new Events.UiEvent("ControlPanel.Finalize", null));
		} else if (tripPlan.getRoute() != null) {
			Events.eventBus.post(new Events.UiEvent("ControlPanel.Route", null));
		}
		Report report = tripPlan.getTripReport();
		if (report != null) {
			String html = report.toHtml();
			resultsPane.setText(html);
		}
	}

	public static void main(String[] args) {
		Main main = new Main();
	}

}
