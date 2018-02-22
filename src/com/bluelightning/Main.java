package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.Events.AddManualStopEvent;
import com.bluelightning.Events.AddWaypointEvent;
import com.bluelightning.Events.POIClickEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.gui.AddAddressDialog;
import com.bluelightning.gui.ControlPanel;
import com.bluelightning.gui.MainControlPanel;
import com.bluelightning.gui.MainPanel;
import com.bluelightning.gui.RoutePanel;
import com.bluelightning.gui.WebBrowser;
import com.bluelightning.gui.WebBrowser.UiHandler;
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

import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.place.VisitedPlace;
import seedu.addressbook.logic.Logic;

// http://forecast.weather.gov/MapClick.php?lat=28.23&lon=-80.7&FcstType=digitalDWML

public class Main {

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
		protected void handle(UiEvent event) {
			System.out.println(event.source + " " + event.awtEvent);
			switch (event.source) {
			case "RoutePanel.AddAfter":
				routeAdd(true);
				break;
			case "RoutePanel.AddBefore":
				routeAdd(false);
				break;
			case "RoutePanel.MoveDown":
				routeMoveDown();
				break;
			case "RoutePanel.MoveUp":
				routeMoveUp();
				break;
			case "ControlPanel.Route":
				route = Here2.computeRoute( routePanel.getWaypointsModel().getData() );
				if (route != null) {
					waypoints = map.showRoute(route);
					int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
					mainPanel.getRightTabbedPane().setSelectedIndex(index);
					insureNearbyMapLoaded(route, Main.MarkerKinds.RESTAREAS, poiMap.get(Main.MarkerKinds.RESTAREAS));
					OptimizeStops optimizeStops = new OptimizeStops(route, poiMap, nearbyMap);
				}
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
			default:
				break;
			}
		}
		
		private CallbackHandler handler = null;
		
		private class CallbackHandler {
			boolean addAfter = true;
			
			public CallbackHandler( boolean after ) {
				addAfter = after;
			}
			
			@Subscribe
			protected void handle( AddWaypointEvent event ) {
				System.out.println( event + " " + event.place );
				Events.eventBus.unregister(this); // one shot
				handler = null;
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
						VisitedPlace place = new VisitedPlace( event.place );
						ArrayList<VisitedPlace> places = routePanel.getWaypointsModel().getData();
						if (places == null)
							places = new ArrayList<>();
						if (selectedWaypointRow < 0) {
							places.add(place);
						} else if (addAfter) {
							places.add(selectedWaypointRow+1, place);
						} else {
							places.add(selectedWaypointRow, place);							
						}
						routePanel.getWaypointsModel().setData(places);
					}
				} );
			}			
		}

		private void routeAdd( boolean after) {
			Events.eventBus.register( new CallbackHandler(after) );
			AddAddressDialog dialog = new AddAddressDialog(controller, addressBook);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}

		private void routeMoveDown() { // increase index position
			ArrayList<VisitedPlace> places = routePanel.getWaypointsModel().getData();
			if (places == null)
				places = new ArrayList<>();
			int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
			if (selectedWaypointRow < 0 || selectedWaypointRow >= places.size()-1)
				return;  // nothing selected or selection already at end
			// swap this and next
			VisitedPlace place = places.get(selectedWaypointRow);
			places.set(selectedWaypointRow, places.get(selectedWaypointRow+1));
			places.set(selectedWaypointRow+1, place);
			routePanel.getWaypointsModel().setData(places);
		}

		private void routeMoveUp() { // decrease index position
			ArrayList<VisitedPlace> places = routePanel.getWaypointsModel().getData();
			if (places == null)
				places = new ArrayList<>();
			int selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
			if (selectedWaypointRow <= 0)
				return;  // nothing selected or selection already first
			// swap this and prior
			VisitedPlace place = places.get(selectedWaypointRow);
			places.set(selectedWaypointRow, places.get(selectedWaypointRow-1));
			places.set(selectedWaypointRow-1, place);
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

	public Main() {
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
		// frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		browserCanvas.initialize(frame);

		// load base POI sets
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
	}

	public EnumMap<Main.MarkerKinds, ArrayList<POIResult>> getNearbyMap() {
		return nearbyMap;
	}

	public static void main(String[] args) {
		Main main = new Main();
	}

}
