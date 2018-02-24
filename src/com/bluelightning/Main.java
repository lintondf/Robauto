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
import com.bluelightning.Events.AddWaypointEvent;
import com.bluelightning.Events.POIClickEvent;
import com.bluelightning.Events.StopsCommitEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.OptimizeStops.DriverAssignments;
import com.bluelightning.OptimizeStops.LegData;
import com.bluelightning.OptimizeStops.RoadDirectionData;
import com.bluelightning.OptimizeStops.StopData;
import com.bluelightning.data.TripPlan;
import com.bluelightning.gui.AddAddressDialog;
import com.bluelightning.gui.MainControlPanel;
import com.bluelightning.gui.MainPanel;
import com.bluelightning.gui.OptimizeStopsDialog;
import com.bluelightning.gui.RoutePanel;
import com.bluelightning.gui.WebBrowser;
import com.bluelightning.gui.OptimizeStopsDialog.OptimizeActionListener;
import com.bluelightning.gui.OptimizeStopsDialog.OptimizeLegSelectionListener;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.POIMarker;
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
import seedu.addressbook.data.place.VisitedPlace;
import seedu.addressbook.logic.Logic;

// http://forecast.weather.gov/MapClick.php?lat=28.23&lon=-80.7&FcstType=digitalDWML

public class Main {

	protected File tripPlanFile = new File("RobautoTripPlan.obj");
	protected TripPlan tripPlan;
	protected List<ButtonWaypoint> waypoints;
	protected Map map;
	protected JXMapViewer mapViewer;
	protected RoutePanel routePanel;
	protected MainControlPanel controlPanel;
	protected Route route;
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
			System.out.println(event.source + " " + event.awtEvent);
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
						if (route != null) {
							optimizeStops();
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
							insureNearbyMapLoaded(route, key, pset);
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

			case "Window.Closing":
				tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
				tripPlan.save(tripPlanFile);
				// System.out.println("Saved: " + tripPlan.toString() );
				break;
			default:
				break;
			}
		}

		private void route() {
			logger.info("Planning route...");
			if (tripPlan.getPlacesChanged()) {
				tripPlan.setRouteJson("");
				tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
				logger.info("  Route points saved");
			}
			route = Here2.computeRoute(tripPlan);
			logger.info("  Route planned");
			if (route != null) {
				waypoints = map.showRoute(route);
				int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
				mainPanel.getRightTabbedPane().setSelectedIndex(index);
				logger.info("  Route shown on map");
				// insureNearbyMapLoaded(route, Main.MarkerKinds.RESTAREAS,
				// poiMap.get(Main.MarkerKinds.RESTAREAS));
				// OptimizeStops optimizeStops = new OptimizeStops(route,
				// poiMap, nearbyMap);
			}
			tripPlan.save(tripPlanFile);
			logger.info("  Trip plan saved");
		}

		private void optimizeStops() {
			EnumMap<Main.MarkerKinds, POISet> poiMap = new EnumMap<>(Main.MarkerKinds.class);
			EnumMap<Main.MarkerKinds, ArrayList<POIResult>> nearbyMap = new EnumMap<>(Main.MarkerKinds.class);
			Main.loadPOIMap(poiMap);
			poiMap.forEach((kind, set) -> {
				nearbyMap.put(kind, set.getPointsOfInterestAlongRoute(route, 5e3));
			});

			OptimizeStops optimizeStops = new OptimizeStops(route, poiMap, nearbyMap);
			optimizeStops.updateTripPlan(tripPlan);

			OptimizeStopsDialog dialog = new OptimizeStopsDialog(optimizeStops);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);

			int iLeg = 0; // start with first leg
			final String html = dialog.updateTripData();
			dialog.setCurrentLeg(tripPlan.getTripLegs().get(iLeg));

			dialog.addListeners(dialog.new OptimizeActionListener(), dialog.new OptimizeLegSelectionListener());

			dialog.generateLegStopChoices( iLeg );
			
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					resultsPane.setText(html);
				}
			});
		}


		private CallbackHandler handler = null;

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
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
						VisitedPlace place = new VisitedPlace(event.place);
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
					}
				});
			}
		}

		private void routeAdd(boolean after) {
			Events.eventBus.register(new CallbackHandler(after));
			AddAddressDialog dialog = new AddAddressDialog(controller, addressBook);
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
		}
	}

	public static enum MarkerKinds {
		WALMARTS, SAMSCLUBS, COSTCOS, TRUCKSTOPS, RESTAREAS
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
		// TODO Costco, Cabelas
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
		map = new Map();
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
			x.printStackTrace();
		}
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Events.eventBus.post(new Events.UiEvent("Window.Closing", e));
			}
		});

		logger.info("Loading previous trip plan");
		tripPlan = TripPlan.load(tripPlanFile);
		System.out.println("Loaded: " + tripPlan.toString());
		routePanel.getWaypointsModel().setData(tripPlan.getPlaces());
		if (!tripPlan.getRouteJson().isEmpty()) {
			Events.eventBus.post(new Events.UiEvent("ControlPanel.Route", null));
		}
	}

	public EnumMap<Main.MarkerKinds, ArrayList<POIResult>> getNearbyMap() {
		return nearbyMap;
	}

	public static void main(String[] args) {
		Main main = new Main();
	}

	public JTextPane getResultsPane() {
		return resultsPane;
	}

	public void setResultsPane(JTextPane resultsPane) {
		this.resultsPane = resultsPane;
	}

}
