package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.BaseCamp.Turn;
import com.bluelightning.Events.AddWaypointEvent;
import com.bluelightning.Events.POIClickEvent;
import com.bluelightning.Events.StopsCommitEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.data.FuelStop;
import com.bluelightning.data.TripPlan;
import com.bluelightning.gui.AddAddressDialog;
import com.bluelightning.gui.MainControlPanel;
import com.bluelightning.gui.MainPanel;
import com.bluelightning.gui.OptimizeStopsDialog;
import com.bluelightning.gui.RoutePanel;
import com.bluelightning.gui.AddAddressDialog.AddAddressActionListener;
import com.bluelightning.json.BottomRight;
import com.bluelightning.json.BoundingBox;
import com.bluelightning.json.Leg;
import com.bluelightning.json.Maneuver;
import com.bluelightning.json.Route;
import com.bluelightning.json.TopLeft;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.POIMarker;
import com.bluelightning.map.StopMarker;
import com.bluelightning.poi.AtlasObscura;
import com.bluelightning.poi.MurphyPOI;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIBase;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.RestAreaPOI;
import com.bluelightning.poi.SamsClubPOI;
import com.bluelightning.poi.TruckStopPOI;
import com.bluelightning.poi.WalmartPOI;
import com.bluelightning.poi.POI.FuelAvailable;
import com.google.common.eventbus.Subscribe;
//import com.sun.glass.ui.Window.Level;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.place.Place;
import seedu.addressbook.data.place.ReadOnlyPlace;
import seedu.addressbook.data.place.UniquePlaceList.DuplicatePlaceException;
import seedu.addressbook.data.place.VisitedPlace;
import seedu.addressbook.logic.Logic;
import seedu.addressbook.storage.StorageFile.StorageOperationException;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

// http://forecast.weather.gov/MapClick.php?lat=28.23&lon=-80.7&FcstType=digitalDWML

// TODO:  Need to be able to stop for gas and not switch drivers

public class PlannerMode extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
			RobautoMain.logger.debug(event.toString());
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

		BufferedImage image = new BufferedImage(component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_RGB);
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
			RobautoMain.logger.info("handle(UIEvent) " + event.source);
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
			case "ControlPanel.StopsToBasecamp":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						stopsToBasecamp();
					}
				});
				break;
			/*
			 * initialize() -> fires RouteFromBaseCamp routeFromBaseCamp() fires
			 * RouteLoad fires RouteLoad routeLoad() fires Finalize*
			 * finalizeRoute() fires RouteReport fires Route* route() fires
			 * RouteReport
			 */
			case "ControlPanel.RouteOpen":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeOpen();
					}
				});
				break;
			case "ControlPanel.RouteFromBaseCamp":
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						routeFromBaseCamp();
					}
				});
				t.start();
				break;
			case "ControlPanel.RouteLoad":
				t = new Thread(new Runnable() {
					@Override
					public void run() {
						routeLoad();
					}
				});
				t.start();
				break;
			case "ControlPanel.RouteDisplay":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeDisplay();
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
			case "ControlPanel.Finalize":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (RobautoMain.tripPlan.getRoute() != null) {
							routeFinalize();
						}
					}
				});
				break;
			case "ControlPanel.RouteReport":
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						routeReport();
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
						RobautoMain.logger.debug("Adding " + entry.getValue().size() + " " + entry.getKey().toString());
						nearby.addAll(POIMarker.factory(entry.getValue()));
					}
				}
				RobautoMain.logger.debug(nearby.size() + " total markers");
				map.updateWaypoints(nearby);
				break;

			case "ControlPanel.SetFuel":
				String gallons = JOptionPane.showInputDialog("Starting Fuel in Gallons: ");
				RobautoMain.startingFuel = Double.parseDouble(gallons);
				break;

			case "ControlPanel.ClearActions":
				RobautoMain.logger.debug(event.source + " " + event.awtEvent);
				JComboBox<?> box = (JComboBox<?>) event.awtEvent.getSource();
				String action = (String) box.getSelectedItem();
				RobautoMain.logger.debug(action);
				RobautoMain.tripPlan.clear(action);
				break;

			default:
				break;
			}
		}

		private void routeFinalize() {
			RobautoMain.logger.info("Finalizing route...");
			RobautoMain.tripPlan.log();
			// if (RobautoMain.tripPlan.getPlacesChanged()) {
			// RobautoMain.tripPlan.setRoute(null);
			// RobautoMain.tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
			// RobautoMain.logger.info("  Route points saved");
			// }
			map.clearRoute();
			ArrayList<Route> days = new ArrayList<>();
			ArrayList<Integer> markers = new ArrayList<>();
			ArrayList<ArrayList<VisitedPlace>> allPlaces = new ArrayList<>();
			Iterator<Route> rit = extractDays(RobautoMain.tripPlan.getRoute()).iterator();
			ArrayList<VisitedPlace> routePlaces = RobautoMain.tripPlan.getPlaces();
			int pi = 0;
			int iDay = 0;
			markers.add(StopMarker.ORIGIN);
			for (TripPlan.TripLeg tripLeg : RobautoMain.tripPlan.getTripLegs()) {
				iDay++;  // count days from 1
				ArrayList<VisitedPlace> places = new ArrayList<>();
				VisitedPlace start = new VisitedPlace(routePlaces.get(pi++));
				start.setVisitOrder(iDay);
				places.add(start);
				int refuel = 0;
				RobautoMain.logger.debug(tripLeg.legData.startLabel);
				tripLeg.stopDataList.forEach(System.out::println);
				for (TripPlan.StopData stopData : tripLeg.stopDataList) {
					if (stopData.use) {
						try {
							VisitedPlace stopPlace = new VisitedPlace(stopData);
							stopPlace.setVisitOrder(iDay);
							places.add(stopPlace);
							refuel = (stopData.refuel) ? StopMarker.FUEL : 0;
							markers.add(StopMarker.DRIVERS + refuel);
						} catch (IllegalValueException e) {
							e.printStackTrace();
						}
					}
				} // for stopData
				//markers.set(markers.size() - 1, StopMarker.OVERNIGHT + refuel);
				markers.add( StopMarker.OVERNIGHT + refuel );
				days.add(rit.next());
				allPlaces.add(places);
			} // tripLeg
			markers.set(markers.size() - 1, StopMarker.TERMINUS);
			RobautoMain.logger.info( String.format("Finalized %d, %d, %d", days.size(), markers.size(), allPlaces.size()) );
			RobautoMain.tripPlan.setFinalizedRoute(days, markers, allPlaces);
			waypoints = map.showRoute(days, markers, allPlaces, Map.ALL_DAYS);
			int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
			mainPanel.getRightTabbedPane().setSelectedIndex(index);
			
			ArrayList<POIResult> allPOI = new ArrayList<>();
			poiMap.forEach((kind, pset) -> {
				allPOI.addAll(pset.getPointsOfInterestAlongRoute(RobautoMain.tripPlan.getRoute(), 2000));
			});
			Iterator<POIResult> it = allPOI.iterator();
			TreeSet<FuelStop> gasStops = new TreeSet<>();
			while (it.hasNext()) {
				POIResult r = it.next();
				if (r.poi.getFuelAvailable().has(FuelAvailable.HAS_GAS)) {
					gasStops.add( new FuelStop(r));
				}
			}
			gasStops.forEach(System.out::println);
			RobautoMain.tripPlan.setFuelStops( new ArrayList<FuelStop>( gasStops ) );
			
			POISet attractions = AtlasObscura.factory();
			ArrayList<POIResult> near = attractions.getPointsOfInterestAlongRoute(RobautoMain.tripPlan.getRoute(), 30e3 );
			RobautoMain.tripPlan.setObscuraPlaces(near);
			StringBuilder sb = new StringBuilder();
			for (POIResult result : near ) {
				AtlasObscura ao = (AtlasObscura) result.poi;
				sb.append(ao.toHtml(result.distance));
			}
			RobautoMain.tripPlan.setObscuraHtml(sb.toString());

			RobautoMain.logger.info("  Route shown on map");
			RobautoMain.tripPlan.save(tripPlanFile);
			RobautoMain.tripPlan.tripPlanUpdated(tripPlanFile.getAbsolutePath());
			RobautoMain.logger.info("  Trip plan saved");
			RobautoMain.tripPlan.log();
			Events.eventBus.post(new Events.UiEvent("ControlPanel.RouteReport", null));
		}

		private void stopsToBasecamp() {
			RobautoMain.logger.info("Writing stops.GPX...");
			boolean pf = Garmin.writeToGPX(routePanel.getWaypointsModel().getData(), "/Users/lintondf/stops.GPX");
			RobautoMain.logger.info((pf) ? "  Stops written" : "  Stop write failed");
		}

		private void routeFromBaseCamp() {
			tripPlanFile = createTripPlanFromBaseCamp(tripPlanFile);
			Events.eventBus.post(new Events.UiEvent("ControlPanel.RouteLoad", null));
		}

		private void routeOpen() {
			String where = System.getProperty("user.home") + "/Google Drive/0Robauto";
			System.out.println(where);
			// Create a file chooser
			final JFileChooser fileChooser = new JFileChooser(where);
			fileChooser.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory())
						return true;
					String name = f.getName().toLowerCase();
					if (name.endsWith(".gdb"))
						return true;
					if (name.endsWith(".gpx"))
						return true;
					if (name.endsWith(".robauto"))
						return true;
					return false;
				}

				@Override
				public String getDescription() {
					return "Robauto TripPlans and BaseCamp exports and routes";
				}
			});
			fileChooser.addChoosableFileFilter(new FileFilter() {
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
			fileChooser.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory())
						return true;
					String name = f.getName().toLowerCase();
					if (name.endsWith(".gdb"))
						return true;
					return false;
				}

				@Override
				public String getDescription() {
					return "BaseCamp exports";
				}
			});
			fileChooser.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory())
						return true;
					String name = f.getName().toLowerCase();
					if (name.endsWith(".gpx"))
						return true;
					return false;
				}

				@Override
				public String getDescription() {
					return "BaseCamp routes";
				}
			});
			fileChooser.addChoosableFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isDirectory())
						return true;
					String name = f.getName().toLowerCase();
					if (name.endsWith(".dayfile"))
						return true;
					return false;
				}

				@Override
				public String getDescription() {
					return "Robauto TripPlans Day by Day Lists";
				}
			});
			// In response to a button click:
			int returnVal = fileChooser.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				// This is where a real application would open the file.
				RobautoMain.logger.trace("Opening: " + file.getName() + ".");
				if (file.exists() && file.isFile()) {
					if (! file.getName().toLowerCase().endsWith(".robauto")) {
						tripPlanFile = file;
						Events.eventBus.post(new Events.UiEvent("ControlPanel.RouteFromBaseCamp", null));
					} else {
						tripPlanFile = file; // new trip plan file
						Events.eventBus.post(new Events.UiEvent("ControlPanel.RouteLoad", null));
					}
				}
			} else {
				RobautoMain.logger.trace("Open command cancelled by user.");
				RobautoMain.logger.info("Loading previous trip plan: " + tripPlanFile.getName());
				Events.eventBus.post(new Events.UiEvent("ControlPanel.RouteLoad", null));
			}
		}

		private void routeLoad() {
			RobautoMain.tripPlan = TripPlan.load(tripPlanFile, frame);
			Events.eventBus.post(new Events.UiEvent("ControlPanel.RouteDisplay", null));
		}

		private void routeDisplay() {
			routePanel.getWaypointsModel().setData(RobautoMain.tripPlan.getPlaces());
			if (!RobautoMain.tripPlan.getFinalizedDays().isEmpty()) {
				RobautoMain.logger.info("Displaying finalized route.");
				Events.eventBus.post(new Events.UiEvent("ControlPanel.Finalize", null));
				reportDetails(RobautoMain.tripPlan.getFinalizedDays());
			} else if (RobautoMain.tripPlan.getRoute() != null) {
				RobautoMain.logger.info("Displaying unfinalized route");
				Events.eventBus.post(new Events.UiEvent("ControlPanel.Route", null));
			} else {
				RobautoMain.logger.info("No route loaded");
			}
		}

		private void routeReport() {
			Report report = RobautoMain.tripPlan.getTripReport();
			if (report != null) {
				String html = report.toHtml();
				resultsPane.setText(html);
				// Here2.reportRoute( RobautoMain.tripPlan.getRoute() );
			}
		}

		private void route() {
			RobautoMain.tripPlan.setFinalizedRoute(null, null, null);
			Route route = RobautoMain.tripPlan.getRoute();
			if (route == null) {
				RobautoMain.logger.info("Planning route...");
				if (RobautoMain.tripPlan.getPlacesChanged()) {
					RobautoMain.tripPlan.setRoute(null);
					RobautoMain.tripPlan.setPlaces(routePanel.getWaypointsModel().getData());
					RobautoMain.logger.info("  Route points saved");
				}
				route = Here2.computeRoute(RobautoMain.tripPlan); // will reload
																	// from
				// routeJson if not
				// empty
				RobautoMain.logger.info("  Route planned");
				RobautoMain.tripPlan.save(tripPlanFile);
				RobautoMain.tripPlan.tripPlanUpdated(tripPlanFile.getAbsolutePath());
				RobautoMain.logger.info("  Trip plan saved");
			}
			if (route != null) {
				waypoints = map.showRoute(route);
				int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
				mainPanel.getRightTabbedPane().setSelectedIndex(index);
				RobautoMain.logger.info("  Route shown on map");
				RobautoMain.tripPlan.setRoute(route);
			}
			Events.eventBus.post(new Events.UiEvent("ControlPanel.RouteReport", null));
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
				POISet.contains("optimizeStops1 ", set);
				nearbyMap.put(kind, set.getPointsOfInterestAlongRoute(RobautoMain.tripPlan.getRoute(), 2e3));
			});
//			for (ArrayList<POIResult> near : nearbyMap.values()) {
//				for (POIResult poi : near) {
//					RobautoMain.logger.debug(poi.toString());
//				}
//			}
			POISet.contains1("optimizeStops2", nearbyMap.values());

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
			try {
				PrintWriter out = new PrintWriter("report.html");
				out.println(html);
				out.close();
			} catch (Exception x) {
				x.printStackTrace();
			}

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					resultsPane.setText(html);
				}
			});

		}

		@SuppressWarnings("unused") // used in CallbackHandler::handle actually
		private CallbackHandler handler = null;
		protected OptimizeStopsDialog dialog = null;

		private class CallbackHandler {
			boolean addAfter = true;

			public CallbackHandler(boolean after) {
				addAfter = after;
			}

			@Subscribe
			protected void handle(AddWaypointEvent event) {
				RobautoMain.logger.debug(event + " " + event.place);
				Events.eventBus.unregister(this); // one shot
				handler = null;
				map.clearRoute();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
						VisitedPlace place = new VisitedPlace(event.place);
//						if (place.getFuelAvailable().get() == FuelAvailable.NO_FUEL)
//							place.setFuelAvailable(getNearByFuel(place.getLatitude(), place.getLongitude(), 200));
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
			//VisitedPlace place = places.get(selectedWaypointRow);
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
			layout.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
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
		
		private boolean filterEvent( ILoggingEvent event ) {
			ch.qos.logback.classic.Level level = event.getLevel();
			if (level == ch.qos.logback.classic.Level.ERROR)
				return true;
			if (level == ch.qos.logback.classic.Level.WARN)
				return true;
			if (level == ch.qos.logback.classic.Level.INFO)
				return true;
			return false;
		}

		@Override
		protected void append(ILoggingEvent event) {
			// RobautoMain.logger.debug("TAE: " + event.toString() + " " + layout);
			if (filterEvent(event)) {
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
		// configured via resources/logback.xml
		RobautoMain.logger = LoggerFactory.getLogger("com.bluelightning.RobautoPlanner");
		// add an appender to feed the text area
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
		rootLogger = lc.getLogger(Logger.ROOT_LOGGER_NAME);
		textAreaAppender = new TextAreaAppender(lc);
		textAreaAppender.start();

		setLayout(new BorderLayout());
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
			x.printStackTrace();
			RobautoMain.logger.trace(x.getMessage());
		}
		Events.eventBus.post(new Events.UiEvent("ControlPanel.RouteOpen", null));
	}

	private File createTripPlanFromBaseCamp(File file) {
		RobautoMain.logger.info("Importing Basecamp route...");
		ArrayList<BaseCamp> days = new ArrayList<>();
		String basePath = file.getAbsolutePath();
		basePath = basePath.substring(0, basePath.length() - 4); // drop extension
		BaseCamp baseCamp = null;
		TripSource garmin = null;
		if (file.getAbsoluteFile().toString().toLowerCase().endsWith(".gdb")) {  // GDB file
			String which = (System.getProperty("os.name").toLowerCase().startsWith("mac")) ? 
					"bin/mac/gpsbabel" :
					"C:/Program Files (x86)/GPSBabel/gpsbabel.exe";
					//"bin/win/GPSBabel.exe";
			try {
				File tmp = File.createTempFile("gpsbabel-out", ".gpx");
				String[] cmd = {
						which,
						"-i", "gdb,dropwpt",
						"-f", file.getAbsolutePath(),
						"-o", "gpx,garminextensions",
						"-F", tmp.getAbsolutePath()
				};
				Execute.run(cmd);
				file = tmp;
				garmin = new Garmin(file);
				ArrayList<BaseCamp.Turn> turns = new ArrayList<>();
				// build turn list from embedded RTEPT durations and maneuvers
				double durationSoFar = 0.0;
				BaseCamp.Turn prior = null;
				for (TrackPoint tp : garmin.getDays().get(0).trackPoints) {
					//System.out.println(durationSoFar + " : " + tp.toString());
					if (tp.maneuver != null) {
						BaseCamp.Turn turn = new BaseCamp.Turn(tp.maneuver, 
								tp.distancePriorToHere, tp.duration, tp.distanceStartToHere, durationSoFar);
						if (prior != null) {
							turn.distance = turn.totalDistance - prior.totalDistance;
							turn.duration = turn.totalDuration - prior.totalDuration;
						}
						prior = turn;
						System.out.println(turns.size() + ": " + turn.toString() );
						turns.add(turn);
					}
					durationSoFar += tp.duration;
				}
				
				baseCamp = new BaseCamp(garmin.getDays().get(0), turns );
				days.add(baseCamp);
			} catch (Exception x) {
				x.printStackTrace();
			}
		} else { // GPX file
			if (garmin.getDays().size() == 1) {
			    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			    DataFlavor flavor = DataFlavor.stringFlavor;
			    if (clipboard.isDataFlavorAvailable(flavor)) {
			    	try {
			            String text = (String) clipboard.getData(flavor);
			            ArrayList<String> lines = new ArrayList<>();
			            String[] array = text.split("\n");
			            RobautoMain.logger.info("  Loading BaseCamp details from clipboard, lines: " + array.length);
			            for (int i = 0; i < array.length; i++) {
				            if (array[i].startsWith("\tDirections")) {
				            	continue;
				            }
			            	if (array[i].startsWith("\t")) {
			            		String prefix = String.format("%d. ", i);
			            		lines.add(prefix + array[i].substring(1)
			            				.replaceAll("\t", ",") // Mac BaseCamp sends Tab separated to clipboard
			            				.replace("\"", "") );  // unquote strings
			            	} else {
			            		lines.add(array[i]);
			            	}
		            		System.out.println(lines.get(lines.size()-1));
			            }
			        
			            baseCamp = new BaseCamp(garmin.getDays().get(0), lines );
						days.add(baseCamp);
			    	} catch (Exception x) {
			    		x.printStackTrace();
			    	}
			    }			
			} 
		}
//		if (baseCamp == null) {
//			for (int i = 0; i < garmin.getDays().size(); i++) {
//				String path = String.format("%s_Day_%d.csv", basePath, i + 1);
//				RobautoMain.logger.info("  Loading BaseCamp details from: " + path);
//				baseCamp = new BaseCamp(garmin.getDays().get(i), path);
//				days.add(baseCamp);
//			}
//			RobautoMain.logger.info(String.format("  %d days loaded", days.size()));
//		}
		ArrayList<List<GeoPosition>> tracks = new ArrayList<>();
		Route route = mergeRoutes(days);
		route.setBoundingBox(generateBoundingBox(route.getShape()));
		tracks.add(route.getShape());

		final double MATCH_DISTANCE = 500.0;

		ArrayList<VisitedPlace> places = new ArrayList<>(garmin.getPlaces());
		for (int i = 0; i < places.size(); i++) {
			VisitedPlace place = places.get(i);
			RobautoMain.logger.debug(place.toString());
			// compare to address book entries
			boolean matched = false;
			if (addressBook != null ) {
				Iterator<Place> pit = addressBook.getAllPlaces().iterator();
				while (pit.hasNext()) {
					Place bookPlace = pit.next();
					double d = POIBase.distanceBetween(bookPlace.getLatitude(), bookPlace.getLongitude(), place.getLatitude(),
							place.getLongitude());
					if (d < MATCH_DISTANCE) {
						RobautoMain.logger.debug(" -> Book: " + bookPlace);
						places.set(i, new VisitedPlace(bookPlace));
						matched = true;
						break;
					}
				}
			}
			GeoPosition position = new GeoPosition(place.getLatitude(), place.getLongitude());
			if (!matched)
				for (POISet pset : poiMap.values()) {
					java.util.Map<POI, POIResult> nearBy = pset.nearBy(position, 0, MATCH_DISTANCE);
					if (!nearBy.isEmpty()) {
						POI poi = nearBy.keySet().iterator().next();
						try {
							RobautoMain.logger.debug(" -> POI: " + poi);
							places.set(i, new VisitedPlace(poi));
							matched = true;
						} catch (IllegalValueException e) {
							RobautoMain.logger.error("Place from POI Exception", e);
						}
						break;
					}
				}
			if (!matched) {
				try {
					addressBook.add(place);
					controller.getStorage().save(addressBook);
					RobautoMain.logger.info("Adding place: " + place.toString());
				} catch (DuplicatePlaceException | StorageOperationException e) {
					RobautoMain.logger.error("Add Place to AddressBook Exception", e);
				}
			}
			if (i > 0 && i < places.size() - 1) {
				places.get(i).setOvernight(true);
			}
			RobautoMain.logger.debug(places.get(i).toGeo() + " " + places.get(i).isOvernight() + " "
					+ places.get(i).getFuelAvailable().get());
		}

		for (VisitedPlace place : places) {
			List<ReadOnlyPlace> startMatches = addressBook.getPlacesWithAddress(place.getAddress().toString());
			RobautoMain.logger.debug(place + " " + startMatches.isEmpty());
		}

		File outputFile = new File(String.format("%s.robauto", basePath));
		RobautoMain.tripPlan = new TripPlan();
		RobautoMain.logger.info("  Route shown on map");
		RobautoMain.tripPlan.setRoute(route);
		RobautoMain.tripPlan.setPlaces(places);
		RobautoMain.logger.info("  Route points saved");
		RobautoMain.tripPlan.save(outputFile);
		RobautoMain.tripPlan.tripPlanUpdated(outputFile.getAbsolutePath());
		RobautoMain.logger.info("  Trip plan saved to " + outputFile.getAbsolutePath());
		return outputFile;
	}

	public <A extends GeodeticPosition> BoundingBox generateBoundingBox(List<A> position) {
		BoundingBox box = new BoundingBox();
		BottomRight br = new BottomRight();
		TopLeft tl = new TopLeft();
		if (position.size() > 0) {
			br.setLatitude(position.get(0).getLatitude());
			tl.setLatitude(position.get(0).getLatitude());
			br.setLongitude(position.get(0).getLongitude());
			tl.setLongitude(position.get(0).getLongitude());
			for (int i = 1; i < position.size(); i++) {
				double latitude = position.get(i).getLatitude();
				double longitude = position.get(i).getLongitude();
				if (latitude > tl.getLatitude()) {
					tl.setLatitude(latitude);
				}
				if (latitude < br.getLatitude()) {
					br.setLatitude(latitude);
				}
				if (longitude < tl.getLongitude()) {
					tl.setLongitude(longitude);
				}
				if (longitude > br.getLongitude()) {
					br.setLongitude(longitude);
				}
			}
		}
		box.setBottomRight(br);
		box.setTopLeft(tl);
		// System.out.printf( "BB %10.6f %10.6f  %10.6f %10.6f\n",
		// tl.getLatitude(), br.getLatitude(), tl.getLongitude(),
		// br.getLongitude() );
		return box;
	}

	private Route mergeRoutes(List<BaseCamp> baseCamps) {
		Route route = new Route();
		List<Object> routeShape = new ArrayList<>();
		List<TrackPoint> routeTrackPoints = new ArrayList<>();
		TreeSet<Leg> legs = new TreeSet<>();
		double firstPoint = 0;
		double lastPoint = 0;
		for (BaseCamp baseCamp : baseCamps) {
			Leg leg = baseCamp.route.getLeg().iterator().next();
			System.out.printf("Legs %d; %10.6f,%10.6f : %10.6f,%10.6f\n", baseCamp.route.getLeg().size(), leg.getStart()
					.getMappedPosition().getLatitude(), leg.getStart().getMappedPosition().getLongitude(), leg.getEnd()
					.getMappedPosition().getLatitude(), leg.getEnd().getMappedPosition().getLongitude());
			lastPoint += leg.getShape().size();
			leg.setFirstPoint(firstPoint);
			leg.setLastPoint(lastPoint);
			leg.setBoundingBox(generateBoundingBox(baseCamp.day.trackPoints));
			legs.add(leg);
			firstPoint += leg.getShape().size();
			routeTrackPoints.addAll(baseCamp.day.trackPoints);
			for (TrackPoint point : baseCamp.day.trackPoints) {
				routeShape.add(String.format("%15.8f, %15.8f", point.getLatitude(), point.getLongitude()));
			}

		}
		route.setLeg(legs);
		route.setShape(routeShape);
		if (!baseCamps.isEmpty())
			route.setBoundingBox(generateBoundingBox(routeTrackPoints));
		return route;
	}

	private List<Route> extractDays(Route route) {
		ArrayList<Route> days = new ArrayList<>();
		Iterator<Leg> lit = route.getLeg().iterator();
		while (lit.hasNext()) {
			Route day = new Route();
			Leg leg = lit.next();
			Leg one = new Leg();
			one.setBaseTime(leg.getBaseTime());
			one.setBoundingBox(leg.getBoundingBox());
			one.setEnd(leg.getEnd());
			one.setFirstPoint(0.0);
			one.setLastPoint(leg.getLastPoint() - leg.getFirstPoint());
			one.setLength(leg.getLength());
			one.setManeuver(leg.getManeuver());
			one.setShape(leg.getShape());
			one.setStart(leg.getStart());
			one.setSummary(leg.getSummary());
			one.setTrafficTime(leg.getTrafficTime());
			one.setTravelTime(leg.getTravelTime());
			TreeSet<Leg> legs = new TreeSet<>();
			legs.add(one);
			day.setLeg(legs);
			day.setShape(leg.getShape());
			day.setBoundingBox(leg.getBoundingBox());
			days.add(day);
		}
		return days;
	}

//	private ArrayList<ButtonWaypoint> mergeVias(List<BaseCamp> baseCamps) {
//		ArrayList<ButtonWaypoint> vias = new ArrayList<>();
//		for (BaseCamp baseCamp : baseCamps) {
//			vias.addAll(baseCamp.vias);
//			StopMarker marker = (StopMarker) vias.get(vias.size() - 1);
//			marker.setKind(StopMarker.OVERNIGHT);
//		}
//		StopMarker marker = (StopMarker) vias.get(vias.size() - 1);
//		marker.setKind(StopMarker.TERMINUS);
//		return vias;
//	}

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
					double speed = maneuver.getLength() / maneuver.getTrafficTime();
					System.out.printf("    %6.1f %-6s  %6.1f %-6s %6.1f %-6s %6.1f: %s\n", dayDistance * Here2.METERS_TO_MILES,
							Here2.toPeriod(dayTime), legDistance * Here2.METERS_TO_MILES, Here2.toPeriod(legTime),
							maneuver.getLength() * Here2.METERS_TO_MILES, Here2.toPeriod(maneuver.getTrafficTime()), speed
									* Here2.METERS_PER_SECOND_TO_MILES_PER_HOUR, maneuver.getInstruction());
				}
			}
		}
	}

	public static JFrame frame;
	public static boolean isSurface = true;

	private static void initLookAndFeel(double textSizeInPixels, double fontSize) {
		try {
			RobautoMain.logger.debug(Toolkit.getDefaultToolkit().getScreenSize().toString());
			// double fontSize = 0.8 * textSizeInPixels *
			// Toolkit.getDefaultToolkit().getScreenResolution() / 72.0;
			System.out.printf("%f %d %f\n", textSizeInPixels, Toolkit.getDefaultToolkit().getScreenResolution(), fontSize);
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
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		final String hostName = (localMachine != null) ? localMachine.getHostName() : "localhost";
		isSurface = hostName.toUpperCase().startsWith("SURFACE") || hostName.toUpperCase().startsWith("NOOK-PC");
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (isSurface) {
						initLookAndFeel(30, 24);
						ButtonWaypoint.setDefaultPreferredSize(new Dimension(48, 48));
					} else {
						ButtonWaypoint.setDefaultPreferredSize(new Dimension(24, 24));
					}
					frame = new JFrame();
					frame.setTitle("Robauto - Planner Mode");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					Image icon = ImageIO.read(new File("images/icon-planner.jpg")).getScaledInstance((int) 64, (int) 64,
							Image.SCALE_SMOOTH);
					frame.setIconImage(icon);

					PlannerMode plannerMode = new PlannerMode();

					frame.setContentPane(plannerMode);
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					SwingUtilities.updateComponentTreeUI(frame);
					frame.pack();
					frame.setVisible(true);
					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
							if (plannerMode.tripPlanFile != null) {
								RobautoMain.tripPlan.setPlaces(plannerMode.routePanel.getWaypointsModel().getData());
								RobautoMain.tripPlan.save(plannerMode.tripPlanFile);
								RobautoMain.tripPlan.tripPlanUpdated(plannerMode.tripPlanFile.getAbsolutePath());
							}
						}
					});
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					plannerMode.initialize();

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
	}
}
